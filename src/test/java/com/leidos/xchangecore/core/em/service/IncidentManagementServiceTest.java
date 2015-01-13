/**
 * 
 */
package com.leidos.xchangecore.core.em.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.niem.niem.niemCore.x20.LengthMeasureType;
import gov.niem.niem.niemCore.x20.MeasurePointValueDocument;
import gov.niem.niem.niemCore.x20.TwoDimensionalGeographicCoordinateType;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.LocationType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mil.dod.metadata.mdr.ns.ddms.x20.PostalAddressDocument.PostalAddress;

import org.apache.xmlbeans.XmlException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.uicds.incident.IncidentDocument;
import org.uicds.incidentManagementService.IncidentInfoDocument;
import org.uicds.incidentManagementService.IncidentInfoType;
import org.uicds.incidentManagementService.ShareIncidentRequestDocument.ShareIncidentRequest;

import com.leidos.xchangecore.core.em.dao.IncidentDAO;
import com.leidos.xchangecore.core.em.model.Incident;
import com.leidos.xchangecore.core.em.service.impl.IncidentManagementServiceImpl;
import com.leidos.xchangecore.core.em.util.DigestTester;
import com.leidos.xchangecore.core.em.util.EMDigestHelper;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidInterestGroupIDException;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.InterestGroupManagementComponent;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.InterestGroupInfo;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.base.StateType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * NotificationServiceTest
 * 
 * @author roger, initial revision
 */
public class IncidentManagementServiceTest {

    // extends AbstractDependencyInjectionSpringContextTests {

    IncidentManagementServiceImpl incidentManagementService = new IncidentManagementServiceImpl();

    WorkProductService workProductService;
    IncidentDAO incidentDAO;
    MessageChannel incidentStateNotificationChannel;
    InterestGroupManagementComponent interestGroupManagementComponent;
    ConfigurationService configurationService;

    private final static String incidentID = "l";
    private final static String owningCore = "uicds@core1";
    private final static String shareName1 = "uicds@core2";
    private final static String shareName2 = "uicds@core3";
    private final static String shareName3 = "uicds@core4";
    private final static String workProductID = "11";

    // @Override
    @Before
    public void setUp() {

        workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
        incidentManagementService.setWorkProductService(workProductService);

        incidentDAO = org.easymock.EasyMock.createMock(IncidentDAO.class);
        incidentManagementService.setIncidentDAO(incidentDAO);

        incidentStateNotificationChannel = org.easymock.EasyMock.createMock(MessageChannel.class);
        incidentManagementService.setIncidentStateNotificationChannel(incidentStateNotificationChannel);

        interestGroupManagementComponent = org.easymock.EasyMock.createMock(InterestGroupManagementComponent.class);
        incidentManagementService.setInterestGroupManagementComponent(interestGroupManagementComponent);

        configurationService = org.easymock.EasyMock.createMock(ConfigurationService.class);
        incidentManagementService.setConfigurationService(configurationService);
    }

    // This test passes in Eclipse but fails from the maven command line.
    @Ignore
    @Test
    public void testCreateRob() throws Exception {

        resetMocks();

        IncidentDocument incidentDocument = getIncidentDocumentFromFile("src/test/resources/incidents/RobZ-create-incident.xml");
        assertNotNull("null incident document", incidentDocument);

        org.easymock.Capture<WorkProduct> capture = setupForCreateIncident();

        ProductPublicationStatus status = incidentManagementService.createIncident(incidentDocument.getIncident());

        org.easymock.EasyMock.verify(workProductService,
            incidentDAO,
            interestGroupManagementComponent);

        resetMocks();

        setupForUpdateIncident(incidentDocument);

        System.out.println(incidentDocument.getIncident().xmlText());
        status = incidentManagementService.updateIncident(incidentDocument.getIncident(),
            WorkProductHelper.getWorkProductIdentification(status.getProduct()));

    }

    @Test
    public void testCreateIncidentDigestWithPolygon() throws Exception {

        resetMocks();

        IncidentDocument incidentDocument = getIncidentDocumentFromFile("src/test/resources/incidents/Incident.xml");

        org.easymock.Capture<WorkProduct> capture = setupForCreateIncident();

        incidentManagementService.createIncident(incidentDocument.getIncident());

        org.easymock.EasyMock.verify(workProductService,
            incidentDAO,
            interestGroupManagementComponent);

        DigestType digest = getDigestFromResponse(capture);

        checkLocationAddress(digest);

        checkEventWithOccursAt(digest);

        // Check the location element
        String[] pointArray = {
            "38.79989 -77.29989",
            "38.79989 -77.09989",
            "39.00000 -77.09989",
            "39.00000 -77.29989",
            "38.79989 -77.29989"
        };
        List<String> points = Arrays.asList(pointArray);
        assertTrue(DigestTester.checkLocationGMLPolygon(digest, points));
    }

    @Test
    public void testCreateIncidentDigestWithCircle() throws Exception {

        IncidentDocument incidentDocument = getIncidentDocumentFromFile("src/test/resources/incidents/Incident.xml");

        replacePolygonWithCircle(incidentDocument);

        assertTrue(ValidationUtil.validate(incidentDocument, true));

        // System.out.println(incidentDocument);

        resetMocks();

        org.easymock.Capture<WorkProduct> capture = setupForCreateIncident();

        incidentManagementService.createIncident(incidentDocument.getIncident());

        org.easymock.EasyMock.verify(workProductService,
            incidentDAO,
            interestGroupManagementComponent);

        DigestType digest = getDigestFromResponse(capture);

        checkEventWithOccursAt(digest);
        // System.out.println(digest);

        // Check the location element
        ArrayList<String> points = new ArrayList<String>();
        points.add("need to get the correct values here and then fix the next routine");
        assertTrue(DigestTester.checkLocationGMLCircle(digest, "4.5", "SMI"));
    }

    @Test
    public void testShareIncident() {

        ArrayList<String> coreNames = new ArrayList<String>();
        coreNames.add(shareName1);

        Incident incident = getIncident();
        WorkProduct workProduct = getIncidentWorkProduct();
        workProduct = shareIncidenToCore(incident, workProduct, shareName1, true);

        IncidentDocument newDoc = getIncidentDocumentFromWorkProduct(workProduct);
        checkSharedCores(newDoc, coreNames);
    }

    @Test
    public void testShareIncidentTwice() {

        ArrayList<String> coreNames = new ArrayList<String>();
        coreNames.add(shareName1);

        Incident incident = getIncident();
        WorkProduct workProduct = getIncidentWorkProduct();
        workProduct = shareIncidenToCore(incident, workProduct, shareName1, true);

        IncidentDocument newDoc = getIncidentDocumentFromWorkProduct(workProduct);
        checkSharedCores(newDoc, coreNames);

        workProduct = shareIncidenToCore(incident, workProduct, shareName1, false);

        newDoc = getIncidentDocumentFromWorkProduct(workProduct);
        checkSharedCores(newDoc, coreNames);
    }

    @Test
    public void testShareToThree() {

        ArrayList<String> coreNames = new ArrayList<String>();
        coreNames.add(shareName1);

        Incident incident = getIncident();
        WorkProduct workProduct = getIncidentWorkProduct();
        workProduct = shareIncidenToCore(incident, workProduct, shareName1, true);

        coreNames.add(shareName2);
        workProduct = shareIncidenToCore(incident, workProduct, shareName2, true);

        coreNames.add(shareName3);
        workProduct = shareIncidenToCore(incident, workProduct, shareName3, true);

        IncidentDocument newDoc = getIncidentDocumentFromWorkProduct(workProduct);
        checkSharedCores(newDoc, coreNames);
    }

    @Test
    public void testGetIncidentInfo() throws Exception {

        resetMocks();

        setupForGetIncidentInfo();

        IncidentInfoType info = incidentManagementService.getIncidentInfo(incidentID);

        assertNotNull("info is null", info);
        ValidationUtil.validate(info, true);

        IncidentInfoDocument incidentInfoDoc = IncidentInfoDocument.Factory.newInstance();
        incidentInfoDoc.setIncidentInfo(info);
        // System.out.println(incidentInfoDoc);
        ValidationUtil.validate(incidentInfoDoc, true);
    }

    private void setupForGetIncidentInfo() {

        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID(org.easymock.EasyMock.isA(String.class))).andReturn(getIncident());

        InterestGroupInfo igInfo = new InterestGroupInfo();
        igInfo.setDescription("description & > <");
        igInfo.setInterestGroupID("IG-123");
        igInfo.setInterestGroupSubType("subtype & < > ");
        igInfo.setInterestGroupType("type &< > ");
        igInfo.setName("name (BELLS RD) & EXIT 73");
        igInfo.setOwningCore(owningCore);
        org.easymock.EasyMock.expect(interestGroupManagementComponent.getInterestGroup(org.easymock.EasyMock.isA(String.class))).andReturn(igInfo);

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier().setStringValue("ID");
        id.addNewChecksum().setStringValue("checksum");
        id.addNewType().setStringValue("type");
        id.addNewVersion().setStringValue("1");
        id.setState(StateType.ACTIVE);
        org.easymock.EasyMock.expect(workProductService.getProductIdentification(org.easymock.EasyMock.isA(String.class))).andReturn(id);

        org.easymock.EasyMock.replay(workProductService,
            incidentDAO,
            interestGroupManagementComponent);
    }

    private IncidentDocument getIncidentDocumentFromFile(String fileName) throws XmlException,
        IOException {

        IncidentDocument incidentDocument = IncidentDocument.Factory.parse(new File(fileName));

        assertTrue(ValidationUtil.validate(incidentDocument, true));
        return incidentDocument;
    }

    private org.easymock.Capture<WorkProduct> setupForCreateIncident() {

        org.easymock.EasyMock.expect(configurationService.getCoreName()).andReturn(owningCore);

        // Capture the created work product
        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        status.setProduct(getIncidentWorkProduct());
        org.easymock.EasyMock.expect(workProductService.publishProduct(org.easymock.EasyMock.and(org.easymock.EasyMock.isA(WorkProduct.class),
            org.easymock.EasyMock.capture(capture)))).andReturn(status);

        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((org.easymock.EasyMock.isA(String.class)))).andReturn(getIncident());
        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((org.easymock.EasyMock.isA(String.class)))).andReturn(getIncident());

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier();
        org.easymock.EasyMock.expect(workProductService.getProductIdentification(org.easymock.EasyMock.isA(String.class))).andReturn(id);

        org.easymock.EasyMock.expect(incidentDAO.makePersistent(org.easymock.EasyMock.isA(Incident.class))).andReturn(getIncident());

        org.easymock.EasyMock.expect(interestGroupManagementComponent.createInterestGroup(org.easymock.EasyMock.isA(InterestGroupInfo.class))).andReturn(incidentID);

        org.easymock.EasyMock.replay(workProductService,
            incidentDAO,
            interestGroupManagementComponent);
        return capture;
    }

    private org.easymock.Capture<WorkProduct> setupForUpdateIncident(IncidentDocument incidentDocument)
        throws InvalidInterestGroupIDException {

        // add incident id
        gov.niem.niem.niemCore.x20.IdentificationType incidentID = incidentDocument.getIncident().addNewActivityIdentification();
        incidentID.addNewIdentificationID().setStringValue("111");

        // Capture the created work product
        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        status.setProduct(getIncidentWorkProduct());
        org.easymock.EasyMock.expect(workProductService.publishProduct(org.easymock.EasyMock.and(org.easymock.EasyMock.isA(WorkProduct.class),
            org.easymock.EasyMock.capture(capture)))).andReturn(status);

        org.easymock.EasyMock.expect(workProductService.getProduct(EasyMock.isA(String.class))).andReturn(status.getProduct());

        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((org.easymock.EasyMock.isA(String.class)))).andReturn(getIncident());
        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((org.easymock.EasyMock.isA(String.class)))).andReturn(getIncident());

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier();
        org.easymock.EasyMock.expect(workProductService.getProductIdentification(org.easymock.EasyMock.isA(String.class))).andReturn(id);

        org.easymock.EasyMock.expect(incidentDAO.makePersistent(org.easymock.EasyMock.isA(Incident.class))).andReturn(getIncident());

        interestGroupManagementComponent.updateInterestGroup(org.easymock.EasyMock.isA(InterestGroupInfo.class));

        org.easymock.EasyMock.replay(workProductService,
            incidentDAO,
            interestGroupManagementComponent);
        return capture;
    }

    private DigestType getDigestFromResponse(org.easymock.Capture<WorkProduct> capture) {

        WorkProductDocument wpd = WorkProductHelper.toWorkProductDocument(capture.getValue());
        // System.out.println(wpd);

        wpd.getWorkProduct().getDigestAbstract();
        DigestType digest = WorkProductHelper.getDigestElement(wpd.getWorkProduct());
        assertNotNull(digest);
        return digest;
    }

    private void replacePolygonWithCircle(IncidentDocument incidentDocument) {

        incidentDocument.getIncident().removeIncidentLocation(0);
        gov.niem.niem.niemCore.x20.LocationType circle = incidentDocument.getIncident().addNewIncidentLocation();
        TwoDimensionalGeographicCoordinateType coords = circle.addNewLocationArea().addNewAreaCircularRegion().addNewCircularRegionCenterCoordinate();
        coords.addNewGeographicCoordinateLatitude().addNewLatitudeDegreeValue().setStringValue("38");
        coords.getGeographicCoordinateLatitude().addNewLatitudeMinuteValue().setStringValue("47");
        coords.getGeographicCoordinateLatitude().addNewLatitudeSecondValue().setStringValue("59.59");
        coords.addNewGeographicCoordinateLongitude().addNewLongitudeDegreeValue().setStringValue("-77");
        coords.getGeographicCoordinateLongitude().addNewLongitudeMinuteValue().setStringValue("17");
        coords.getGeographicCoordinateLongitude().addNewLongitudeSecondValue().setStringValue("59.59");
        LengthMeasureType radius = circle.getLocationAreaArray(0).getAreaCircularRegionArray(0).addNewCircularRegionRadiusLengthMeasure();
        radius.addNewLengthUnitCode().set(gov.niem.niem.uneceRec20Misc.x20.LengthCodeSimpleType.SMI);
        MeasurePointValueDocument value = MeasurePointValueDocument.Factory.newInstance();
        value.addNewMeasurePointValue().setBigDecimalValue(new BigDecimal(4.5));
        // Don't use the radius.setMeasureValueArray(arg0) here as it creates
        // invalid XML
        radius.set(value);
        ValidationUtil.validate(radius, true);
    }

    private void checkLocationAddress(DigestType digest) {

        List<LocationType> locations = EMDigestHelper.getLocationElements(digest);
        assertTrue(locations.size() == 1);
        LocationType location = locations.get(0);
        assertTrue(location.sizeOfPhysicalAddressArray() == 1);
        PostalAddress address = location.getPhysicalAddressArray(0).getPostalAddress();
        assertTrue(address.getCountryCode().getValue().equals("US"));
        assertTrue(address.getState().equals("VA"));
        assertTrue(address.getCity().equals("Arlington"));
        assertTrue(address.sizeOfStreetArray() > 0);
        assertTrue(address.getStreetArray(0).equals("4001 North Fairfax Drive"));
        assertTrue(address.getPostalCode().equals("22203"));

        // assert
    }

    private void checkEventWithOccursAt(DigestType digest) {

        // Get the incident Event
        HashMap<String, String> codes = new HashMap<String, String>();
        codes.put(DigestTester.NS_UCORE_CODESPACE, "CBRNE");
        EventType event = DigestTester.getEventElementFromDigest(digest, codes);
        assertNotNull(event);

        // System.out.println(digest);

        List<LocationType> locations = EMDigestHelper.getLocationElements(digest);
        assertTrue(locations.size() == 1);
        assertTrue(DigestTester.hasAOccursAtElement(digest, event.getId(), locations.get(0).getId()));
    }

    private WorkProduct shareIncidenToCore(Incident incident,
                                           WorkProduct workProduct,
                                           String coreName,
                                           boolean expectPublish) {

        resetMocks();

        InterestGroupInfo interestGroupInfo = new InterestGroupInfo();
        interestGroupInfo.setInterestGroupID(incidentID);
        org.easymock.EasyMock.expect(interestGroupManagementComponent.getInterestGroup(incidentID)).andReturn(interestGroupInfo);

        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((incidentID))).andReturn(incident);

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier();
        org.easymock.EasyMock.expect(workProductService.getProductIdentification(org.easymock.EasyMock.isA(String.class))).andReturn(id);

        try {
            interestGroupManagementComponent.shareInterestGroup(org.easymock.EasyMock.eq(incidentID),
                org.easymock.EasyMock.eq(coreName),
                org.easymock.EasyMock.isA(String.class),
                org.easymock.EasyMock.eq(false));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        org.easymock.EasyMock.expect(incidentStateNotificationChannel.send(org.easymock.EasyMock.isA(Message.class))).andReturn(true);

        org.easymock.EasyMock.expect(incidentDAO.findByIncidentID((incidentID))).andReturn(incident);

        org.easymock.EasyMock.expect(workProductService.getProduct(incident.getWorkProductID())).andReturn(workProduct);

        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();

        if (expectPublish) {
            ProductPublicationStatus status = new ProductPublicationStatus();
            org.easymock.EasyMock.expect(workProductService.publishProduct(org.easymock.EasyMock.and(org.easymock.EasyMock.isA(WorkProduct.class),
                org.easymock.EasyMock.capture(capture)))).andReturn(status);
        }

        org.easymock.EasyMock.replay(incidentDAO,
            workProductService,
            incidentStateNotificationChannel,
            interestGroupManagementComponent);

        ShareIncidentRequest request = createShareRequest(incidentID, coreName);
        boolean shared = false;
        try {
            shared = incidentManagementService.shareIncident(request);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(shared);

        org.easymock.EasyMock.verify(incidentDAO,
            workProductService,
            incidentStateNotificationChannel,
            interestGroupManagementComponent);

        if (expectPublish) {
            // return the captured work product
            return capture.getValue();
        } else {
            // new work product was not published to return the current one
            return workProduct;
        }
    }

    private void resetMocks() {

        org.easymock.EasyMock.reset(workProductService,
            incidentDAO,
            incidentStateNotificationChannel,
            interestGroupManagementComponent);
    }

    private void checkSharedCores(IncidentDocument newDoc, ArrayList<String> coreNames) {

        assertNotNull(newDoc);

        assertTrue(newDoc.getIncident().sizeOfSharedCoreNameArray() == coreNames.size());
        ArrayList<String> coreList = new ArrayList<String>(Arrays.asList(newDoc.getIncident().getSharedCoreNameArray()));
        assertTrue(coreList.containsAll(coreNames));
    }

    private ShareIncidentRequest createShareRequest(String incidentID, String coreName) {

        ShareIncidentRequest request = ShareIncidentRequest.Factory.newInstance();
        request.setIncidentID(incidentID);
        request.setCoreName(coreName);
        return request;
    }

    private Incident getIncident() {

        Incident incident = new Incident();
        incident.setIncidentId(incidentID);
        incident.setWorkProductID(workProductID);
        incident.setLatitude(74.0);
        incident.setLongitude(74.0);
        return incident;
    }

    private IncidentDocument getIncidentDocumentFromWorkProduct(WorkProduct wp) {

        IncidentDocument incidentDoc = null;

        try {
            incidentDoc = (IncidentDocument) wp.getProduct();
        } catch (Exception e) {
            incidentDoc = null;
        }
        return incidentDoc;
    }

    private WorkProduct getIncidentWorkProduct() {

        WorkProduct workProduct = new WorkProduct();
        IncidentDocument doc = IncidentDocument.Factory.newInstance();
        doc.addNewIncident();
        workProduct.setProduct(doc);
        workProduct.setProductVersion(new Integer(1));
        workProduct.setProductID("id");
        return workProduct;
    }

}
