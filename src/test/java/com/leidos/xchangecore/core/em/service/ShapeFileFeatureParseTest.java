package com.leidos.xchangecore.core.em.service;

import gov.ucore.ucore.x20.CollectionType;
import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.IdentifierType;
import gov.ucore.ucore.x20.LocationType;
import gov.ucore.ucore.x20.StringType;
import gov.ucore.ucore.x20.ThingRefType;
import gov.ucore.ucore.x20.ThingType;
import gov.ucore.ucore.x20.WhatType;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.junit.Assert;
import org.junit.Test;

import com.leidos.xchangecore.core.em.util.ShapefileIngester;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.DigestConstant;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.InfrastructureNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.UUIDUtil;
import com.leidos.xchangecore.core.infrastructure.util.XmlUtil;

public class ShapeFileFeatureParseTest
    extends Assert {

    @SuppressWarnings("deprecation")
    private void addCollectionTypeToDigest(DigestDocument digest, List<String> locationIds) {

        SchemaType type = CollectionType.type;
        CollectionType colType = CollectionType.Factory.newInstance();
        colType.setId(UUIDUtil.getID("Collection"));

        IdentifierType identifier = colType.addNewIdentifier();
        identifier.addNewLabel().set("Label");
        identifier.setCode("label");
        identifier.setCodespace("http://uicds.us/identifier");
        identifier.set("Collection (geo)");

        StringType description = colType.addNewDescriptor();
        description.set("Collection of LocationInfo of all features");

        WhatType what = colType.addNewWhat();
        what.setCode("GroupOfLocations");
        what.setCodespace(InfrastructureNamespaces.NS_UCORE_CODESPACE);

        for (String locationId : locationIds) {
            ThingRefType ref = colType.addNewThingRef();
            List<String> arg0 = new ArrayList<String>();
            arg0.add(locationId);
            ref.setRef(arg0);
        }

        XmlUtil.substitute(digest.getDigest().addNewThingAbstract(),
            InfrastructureNamespaces.NS_UCORE,
            DigestConstant.S_Collection,
            type,
            colType);
    }

    @Test
    public void parseMultiLineStringShapeFiles() throws Exception {

        ShapefileIngester ingester = new ShapefileIngester();
        InputStream in = new FileInputStream("src/test/resources/shapefiles/multilinestring/lineSF.zip");
        List<WorkProduct> features = ingester.parseFeatures("", in);
        WorkProduct product = new WorkProduct();
        DigestHelper digestHelper = new DigestHelper();
        List<String> locationIds = new ArrayList<String>();
        for (WorkProduct feature : features) {
            if (feature.getDigest() != null) {
                ThingType[] locationArray = feature.getDigest().getDigest().getThingAbstractArray();
                for (ThingType location : locationArray) {
                    locationIds.add(location.getId());
                    XmlUtil.substitute(digestHelper.getDigest().getDigest().addNewThingAbstract(),
                        InfrastructureNamespaces.NS_UCORE,
                        DigestConstant.S_Location,
                        LocationType.type,
                        location);
                }
                DigestDocument digest = feature.getDigest();
                assertTrue(digest.toString().contains("LineString"));
            }
        }
        addCollectionTypeToDigest(digestHelper.getDigest(), locationIds);
        product.setDigest(digestHelper.getDigest());
        writeToFile(product.getDigest().toString(), "src/test/resources/digestWithLineFeatures.xml");
    }

    @Test
    public void parsePointShapeFiles() throws Exception {

        ShapefileIngester ingester = new ShapefileIngester();
        InputStream in = new FileInputStream("src/test/resources/shapefiles/point/prop_text.zip");
        List<WorkProduct> features = ingester.parseFeatures("", in);
        WorkProduct product = new WorkProduct();
        DigestHelper digestHelper = new DigestHelper();
        List<String> locationIds = new ArrayList<String>();
        for (WorkProduct feature : features) {
            if (feature.getDigest() != null) {
                ThingType[] array = feature.getDigest().getDigest().getThingAbstractArray();
                XmlUtil.substitute(digestHelper.getDigest().getDigest().addNewThingAbstract(),
                    InfrastructureNamespaces.NS_UCORE,
                    DigestConstant.S_Location,
                    LocationType.type,
                    array[0]);
                locationIds.add(array[0].getId());
                DigestDocument digest = feature.getDigest();
                assertTrue(digest.toString().contains("Point"));
            }
        }
        addCollectionTypeToDigest(digestHelper.getDigest(), locationIds);
        product.setDigest(digestHelper.getDigest());
        writeToFile(product.getDigest().toString(),
            "src/test/resources/digestWithPointFeatures.xml");
    }

    @Test
    public void parsePolygonShapeFiles() throws Exception {

        ShapefileIngester ingester = new ShapefileIngester();
        InputStream in = new FileInputStream("src/test/resources/maps/IMAACexample_shape.shz");
        // "src/test/resources/shapefiles/polygon/polygonSF.zip");
        List<WorkProduct> features = ingester.parseFeatures("", in);
        WorkProduct product = new WorkProduct();
        DigestHelper digestHelper = new DigestHelper();
        List<String> locationIds = new ArrayList<String>();
        for (WorkProduct feature : features) {
            if (feature.getDigest() != null) {
                ThingType[] locationArray = feature.getDigest().getDigest().getThingAbstractArray();
                for (ThingType location : locationArray) {
                    locationIds.add(location.getId());
                    XmlUtil.substitute(digestHelper.getDigest().getDigest().addNewThingAbstract(),
                        InfrastructureNamespaces.NS_UCORE,
                        DigestConstant.S_Location,
                        LocationType.type,
                        location);
                }
                DigestDocument digest = feature.getDigest();
                assertTrue(digest.toString().contains("Polygon"));
            }
        }
        addCollectionTypeToDigest(digestHelper.getDigest(), locationIds);
        product.setDigest(digestHelper.getDigest());
        writeToFile(product.getDigest().toString(),
            "src/test/resources/digestWithPolygonFeatures.xml");
    }

    private void writeToFile(String digest, String fileName) {

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(fileName));
            out.write(digest);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
