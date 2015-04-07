package com.leidos.xchangecore.core.em.util;

import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.incident.IncidentDocument;

import com.leidos.xchangecore.core.infrastructure.model.ExtendedMetadata;
import com.leidos.xchangecore.core.infrastructure.model.ShareRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class AgreementMatcher {

    private static final String INTEREST_GROUP_CODESPACE = "http://uicds.org/interestgroup#Incident";
    private static Logger logger = LoggerFactory.getLogger(AgreementMatcher.class);

    private static double calculateDistancekm(double lat1, double lon1, double lat2, double lon2) {

        double earthRadius = 6371; // in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) *
                   Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }

    public static boolean isMatch(String[] point,
                                  Set<ShareRule> shareRules,
                                  IncidentDocument incidentDoc) {

        if (shareRules.size() == 0) {
            return true;
        }

        String lat = point[0];
        String lon = point[1];

        // Incident Type rule
        String incidentType = null;
        if (incidentDoc.getIncident().sizeOfActivityCategoryTextArray() > 0) {
            incidentType = incidentDoc.getIncident().getActivityCategoryTextArray(0).getStringValue();
        }
        logger.debug("incidentType: " + incidentType);

        // Extended Metadata rule
        ArrayList<ExtendedMetadata> incidentExtendedMetadataList = new ArrayList<ExtendedMetadata>();
        logger.debug("incidentDoc.getIncident().sizeOfExtendedMetadataArray = " +
                     incidentDoc.getIncident().sizeOfExtendedMetadataArray());
        if (incidentDoc.getIncident().sizeOfExtendedMetadataArray() > 0) {
            ExtendedMetadata extendedMetadata = new ExtendedMetadata();
            for (int i = 0; i < incidentDoc.getIncident().sizeOfExtendedMetadataArray(); i++) {

                extendedMetadata.setCodespace(incidentDoc.getIncident().getExtendedMetadataArray(i).getCodespace());
                extendedMetadata.setCode(incidentDoc.getIncident().getExtendedMetadataArray(i).getCode());
                extendedMetadata.setValue(incidentDoc.getIncident().getExtendedMetadataArray(i).getStringValue());

                incidentExtendedMetadataList.add(extendedMetadata);
            }
        }

        // If all the rules are disabled then always share
        boolean evaluateRules = false;
        for (ShareRule rule : shareRules) {
            if (rule.isEnabled()) {
                evaluateRules = true;
                break;
            }
        }

        if (!evaluateRules) {
            return true;
        }

        boolean igMatches = false;
        boolean exMatches = false;
        boolean locMatches = false;

        for (ShareRule rule : shareRules) {

            igMatches = false;
            exMatches = false;
            locMatches = false;

            // only process rule if it is enabled and has a condition
            if (!rule.isEnabled()) {
                continue;
            }

            if (rule.getInterestGroup() != null && rule.getInterestGroup().getCodeSpace() != null &&
                rule.getInterestGroup().getValue() != null) {
                if (rule.getInterestGroup().getValue().equalsIgnoreCase("*") ||
                    incidentType != null &&
                    rule.getInterestGroup().getCodeSpace().equalsIgnoreCase(INTEREST_GROUP_CODESPACE) &&
                    rule.getInterestGroup().getValue().equalsIgnoreCase(incidentType)) {
                    igMatches = true;
                } else {
                    igMatches = false;
                }
            } else {
                // there was no interest group
                igMatches = true;
            }
            logger.debug("igMatches: " + igMatches);
            if (!igMatches) {
                continue;
            }

            // now check Extended Metadata
            if (rule.getExtendedMetadata() != null && rule.getExtendedMetadata().size() > 0) {
                ExtendedMetadata extendedMetadata = new ExtendedMetadata();

                logger.debug("Processing extended metadata condition");
                for (ExtendedMetadata data : rule.getExtendedMetadata()) {
                    extendedMetadata.setCodespace(data.getCodespace());
                    extendedMetadata.setCode(data.getCode());
                    extendedMetadata.setValue(data.getValue());

                    for (ExtendedMetadata iem : incidentExtendedMetadataList) {
                        logger.debug("\t" + iem.getCode() + " ? " + extendedMetadata.getCode());
                        logger.debug("\t" + iem.getCodespace() + " ? " +
                                     extendedMetadata.getCodespace());
                        logger.debug("\t" + iem.getValue() + " ? " + extendedMetadata.getValue());
                        if (iem.getCode().equalsIgnoreCase(extendedMetadata.getCode()) &&
                            iem.getCodespace().equalsIgnoreCase(extendedMetadata.getCodespace()) &&
                            iem.getValue().equalsIgnoreCase(extendedMetadata.getValue())) {
                            exMatches = true;
                            break;
                        } else {
                            exMatches = false;
                        }
                    }
                }
            } else {
                // no extended metadata was found
                exMatches = true;
            }
            logger.debug("exMatches: " + exMatches);
            if (!exMatches) {
                continue;
            }

            // process proximity switch
            if (rule.getRemoteCoreProximity() != null) {
                logger.debug("Processing remote proximity condition");

                if (!("".equals(lat) || "".equals(lon))) {
                    // get the remote core's location as a point object
                    GeometryFactory geoFactory = new GeometryFactory();
                    Point remoteCorePoint = geoFactory.createPoint(new Coordinate(Double.parseDouble(lat),
                                                                                  Double.parseDouble(lon)));

                    if (incidentDoc.getIncident().getIncidentLocationArray() != null) {
                        if (incidentDoc.getIncident().getIncidentLocationArray(0).getLocationAreaArray() != null) {
                            // get the incident location (EMGeoUtil is naive, but works for most cases)
                            Point incidentPoint = EMGeoUtil.parsePoint(incidentDoc.getIncident());
                            double radiuskm = Double.parseDouble(rule.getRemoteCoreProximity());

                            double lat1 = incidentPoint.getY(); // why is the incidentpoint backwards?
                            double lon1 = incidentPoint.getX();
                            double lat2 = remoteCorePoint.getX();
                            double lon2 = remoteCorePoint.getY();
                            logger.debug("incident: " + lat1 + " " + lon1);
                            logger.debug("core: " + lat2 + " " + lon2);;
                            double distancekm = calculateDistancekm(lat1, lon1, lat2, lon2);
                            logger.debug("distance: " + distancekm);
                            if (distancekm <= radiuskm) {
                                locMatches = true;
                            }
                        } else {
                            locMatches = false;
                        }
                    } else {
                        locMatches = false;
                    }

                } else if (Boolean.valueOf(rule.getShareOnNoLoc())) {
                    locMatches = Boolean.valueOf(rule.getShareOnNoLoc());
                } else {
                    locMatches = true;
                }

            } else {
                // there was no location condition
                locMatches = true;
            }
            logger.debug("locMatches: " + locMatches);
            if (locMatches) {
                break;
            }
        }

        logger.debug("igMatches = " + igMatches);
        logger.debug("exMatches = " + exMatches);
        logger.debug("locMatches = " + locMatches);

        return igMatches && exMatches && locMatches;
    }

}
