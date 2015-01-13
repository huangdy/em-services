/**
 * 
 */
package com.leidos.xchangecore.core.em.service;

import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.OrganizationType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.easymock.EasyMock;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.uicds.resourceManagementService.EdxlDeResponseDocument;

import x0.oasisNamesTcEmergencyEDXLDE1.EDXLDistributionDocument;
import x0.oasisNamesTcEmergencyEDXLDE1.ValueSchemeType;
import x0Msg.oasisNamesTcEmergencyEDXLRM1.CommitResourceDocument;
import x0Msg.oasisNamesTcEmergencyEDXLRM1.RequestResourceDocument;

import com.leidos.xchangecore.core.em.exceptions.SendMessageErrorException;
import com.leidos.xchangecore.core.em.service.impl.ResourceManagementServiceImpl;
import com.leidos.xchangecore.core.em.util.BroadcastUtil;
import com.leidos.xchangecore.core.em.util.DigestTester;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptyCoreNameListException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;
import com.leidos.xchangecore.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.leidos.xchangecore.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.leidos.xchangecore.core.infrastructure.messages.Core2CoreMessage;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;

/**
 * NotificationServiceTest
 * 
 * @author roger, initial revision
 * 
 */
public class ResourceManagementServiceTest
    extends AbstractDependencyInjectionSpringContextTests {

    ResourceManagementServiceImpl resourceManagementService = new ResourceManagementServiceImpl();

    WorkProductService workProductService;

    CommunicationsService communicationsService;

    NotificationService notificationService;

    static final String rmApp1Name = "RMApplication1";
    static final String rmApp2Name = "RMApplication2";
    static final String rmApp1 = rmApp1Name + "@core1";
    static final String rmApp2 = rmApp2Name + "@core2";

    static final String RESOURCE_REQUEST_ID_1 = "RequestResource.1-ResourceInfoElementID.1";
    static final String RESOURCE_REQUEST_ID_2 = "RequestResource.2-ResourceInfoElementID.1";
    static final String RESOURCE_REQUEST_ID_3 = "RequestResource.3-ResourceInfoElementID.1";
    static final String RESOURCE_REQUEST_ID_4 = "RequestResource.4-ResourceInfoElementID.1";
    static final String INCIDENT_ID = "uicds-incident-id";
    static final String COMITTED_TD48_ID_1 = "RequestResource.1-ResourceInfoElementID.1-TD48";
    static final String COMITTED_TD48_ID_2 = "RequestResource.2-ResourceInfoElementID.1-TD48";
    static final String COMITTED_TD48_ID_3 = "RequestResource.3-ResourceInfoElementID.1-TD48";
    static final String COMITTED_TD48_ID_4 = "RequestResource.4-ResourceInfoElementID.1-TD48";
    static final String COMITTED_TD7_ID_1 = "RequestResource.1-ResourceInfoElementID.2-TD7";
    static final String COMITTED_TD7_ID_2 = "RequestResource.2-ResourceInfoElementID.2-TD7";
    static final String COMITTED_TD7_ID_3 = "RequestResource.3-ResourceInfoElementID.2-TD7";
    static final String COMITTED_TD7_ID_4 = "RequestResource.4-ResourceInfoElementID.2-TD7";
    static final String CONTACT_DESCRIPTOR = "Arlington Fire Station";
    static final String INCIDENT_LOCATION_DESCRIPTION = "Arlington Waste Facility";
    static final String INCIDENT_LOCATION_POINT = "38.801168 -77.127685";
    static final String REQUESTER_LOCATION_POINT = "38.866014 -77.193162";

    static final String UCORE_CODESPACE_NS = "http://ucore.gov/ucore/2.0/codespace/";
    static final String UICDS_CODESPACE_NS = "http://uicds.gov/1.0/codespace";

    @Override
    public void onSetUp() {

        workProductService = EasyMock.createMock(WorkProductService.class);
        resourceManagementService.setWorkProductService(workProductService);

        communicationsService = EasyMock.createMock(CommunicationsService.class);
        resourceManagementService.setCommunicationsService(communicationsService);

        notificationService = EasyMock.createMock(NotificationService.class);
        resourceManagementService.setNotificationService(notificationService);
    }

    public void testResourceManagementServiceCreate() {

        assertNotNull(resourceManagementService);
        assertNotNull(workProductService);
    }

    public void testRequestResourceWithLocationDescriptionOnly() {

        try {
            EdxlDeResponseDocument response = sendAndCheckRequestResource("src/test/resources/EDXL-RM/RequestResource.1.xml");

            checkCommonRequestResourceResponseElements(response, RESOURCE_REQUEST_ID_1, true);

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testRequestResourceWithLocationGMLPoint() {

        try {
            EdxlDeResponseDocument response = sendAndCheckRequestResource("src/test/resources/EDXL-RM/RequestResource.2.xml");
            // System.out.println(response);

            // System.out.println(response);
            checkCommonRequestResourceResponseElements(response, RESOURCE_REQUEST_ID_2, true);

            // Check that the resource info schedule element and contact information element
            // created a valid location with a GML point
            assertTrue(DigestTester.checkLocationGMLPoint(response.getEdxlDeResponse().getDigest(),
                INCIDENT_LOCATION_POINT));
            assertTrue(DigestTester.checkLocationGMLPoint(response.getEdxlDeResponse().getDigest(),
                REQUESTER_LOCATION_POINT));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testRequestResourceWithLocationAddress() {

        try {
            EdxlDeResponseDocument response = sendAndCheckRequestResource("src/test/resources/EDXL-RM/RequestResource.3.xml");

            // System.out.println(response);
            checkCommonRequestResourceResponseElements(response, RESOURCE_REQUEST_ID_3, true);

            // Check that the resource info schedule element and contact information element
            // created a valid location with an address
            assertTrue(DigestTester.checkLocationAddresses(response.getEdxlDeResponse().getDigest()));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testRequestResourceWithNoIncident() {

        try {
            EdxlDeResponseDocument response = sendAndCheckRequestResource("src/test/resources/EDXL-RM/RequestResource.4.xml");

            // System.out.println(response);
            checkCommonRequestResourceResponseElements(response, RESOURCE_REQUEST_ID_4, true);

            // Check that the resource info schedule element and contact information element
            // created a valid location with an address
            assertTrue(DigestTester.checkLocationAddresses(response.getEdxlDeResponse().getDigest()));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testRequestResourceWithNoLocation() {

        try {
            EdxlDeResponseDocument response = sendAndCheckRequestResource("src/test/resources/EDXL-RM/RequestResource.NoLocation.xml");

            // System.out.println(response);
            checkCommonRequestResourceResponseElements(response, RESOURCE_REQUEST_ID_4, false);

            // Check that the resource info schedule element and contact information element
            // created a valid location with an address
            assertTrue(DigestTester.checkLocationAddresses(response.getEdxlDeResponse().getDigest()));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithLocationDescriptionOnly() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource.1.xml");

            // System.out.println(response);
            checkCommonCommitResourceResponseElements(response,
                COMITTED_TD48_ID_1,
                COMITTED_TD7_ID_1,
                true);

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithLocationGMLPoint() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource.2.xml");

            checkCommonCommitResourceResponseElements(response,
                COMITTED_TD48_ID_2,
                COMITTED_TD7_ID_2,
                true);

            // Check that the resource info schedule element and contact information element
            // created a valid location with a GML point
            assertTrue(DigestTester.checkLocationGMLPoint(response.getEdxlDeResponse().getDigest(),
                INCIDENT_LOCATION_POINT));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithLocationAddress() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource.3.xml");

            checkCommonCommitResourceResponseElements(response,
                COMITTED_TD48_ID_3,
                COMITTED_TD7_ID_3,
                true);
            // System.out.println(response);

            // Check that the resource info schedule element and contact information element
            // created a valid location with a GML point
            assertTrue(DigestTester.checkLocationAddresses(response.getEdxlDeResponse().getDigest()));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithNoIncident() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource.4.xml");

            // System.out.println(response);
            checkCommonCommitResourceResponseElements(response,
                COMITTED_TD48_ID_4,
                COMITTED_TD7_ID_4,
                true);

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithNoLocation() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource.3.xml");

            checkCommonCommitResourceResponseElements(response,
                COMITTED_TD48_ID_3,
                COMITTED_TD7_ID_3,
                false);
            // System.out.println(response);

            // Check that the resource info schedule element and contact information element
            // created a valid location with a GML point
            assertTrue(DigestTester.checkLocationAddresses(response.getEdxlDeResponse().getDigest()));

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testCommitResourceWithDecline() {

        try {
            EdxlDeResponseDocument response = sendAndCheckCommitResource("src/test/resources/EDXL-RM/CommitResource-Decline.xml");

            checkDeclineResourceResponseElements(response,
                COMITTED_TD48_ID_1,
                COMITTED_TD7_ID_1,
                false);

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testGetRequestedResourceList() {

        EasyMock.reset(workProductService);

        List<WorkProduct> wps = getWorkProductList();
        try {
            EasyMock.expect(workProductService.getProductByTypeAndXQuery("RequestResource",
                null,
                null)).andReturn(wps);
        } catch (InvalidXpathException e) {
            e.printStackTrace();
        }

        EasyMock.replay(workProductService);

        WorkProduct[] workProductList = resourceManagementService.getRequestedResources(INCIDENT_ID);
        assertNotNull(workProductList);
        assertTrue(workProductList.length == 2);

        EasyMock.verify(workProductService);

    }

    public void testGetCommittedResourceList() {

        EasyMock.reset(workProductService);

        List<WorkProduct> wps = getWorkProductList();
        try {
            EasyMock.expect(workProductService.getProductByTypeAndXQuery("CommitResource",
                null,
                null)).andReturn(wps);
        } catch (InvalidXpathException e) {
            e.printStackTrace();
        }

        EasyMock.replay(workProductService);

        WorkProduct[] workProductList = resourceManagementService.getCommittedResources(INCIDENT_ID);
        assertNotNull(workProductList);
        assertTrue(workProductList.length == 2);

        EasyMock.verify(workProductService);

    }

    // TODO: Test each exception from edxldeRequest

    // TODO: Test an update (EDXL-RM message recall) to a request

    private List<WorkProduct> getWorkProductList() {

        ArrayList<WorkProduct> listOfProducts = new ArrayList<WorkProduct>();

        WorkProduct wp1 = new WorkProduct();
        wp1.setProductID("wp1");
        listOfProducts.add(wp1);

        WorkProduct wp2 = new WorkProduct();
        wp2.setProductID("wp2");
        listOfProducts.add(wp2);

        return listOfProducts;
    }

    private void checkCommonCommitResourceResponseElements(EdxlDeResponseDocument response,
                                                           String td48ID,
                                                           String td7ID,
                                                           boolean hasLocation) {

        // Check a couple simple properties
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.ENTITY_THING_TYPE,
            td48ID,
            ResourceManagementServiceImpl.UICDS_EVENT_STATUS_CODESPACE,
            "Comitted",
            "Status",
            null));
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.ENTITY_THING_TYPE,
            td7ID,
            ResourceManagementServiceImpl.UICDS_EVENT_STATUS_CODESPACE,
            "Comitted",
            "Status",
            null));

        // Check that there is at least one UCore what element
        assertTrue(DigestTester.checkEntityWhatElement(response.getEdxlDeResponse().getDigest(),
            td48ID,
            UCORE_CODESPACE_NS,
            "Equipment",
            null,
            null));
        assertTrue(DigestTester.checkEntityWhatElement(response.getEdxlDeResponse().getDigest(),
            td7ID,
            UCORE_CODESPACE_NS,
            "Equipment",
            null,
            null));

        // Check that each entity has a HasDestinationOf element
        if (hasLocation) {
            assertTrue(DigestTester.hasAHasDestinationOfElement(response.getEdxlDeResponse().getDigest(),
                td48ID));
            assertTrue(DigestTester.hasAHasDestinationOfElement(response.getEdxlDeResponse().getDigest(),
                td7ID));
        }

        // Validate the response
        assertTrue(ValidationUtil.validate(response, true));
    }

    private void checkDeclineResourceResponseElements(EdxlDeResponseDocument response,
                                                      String td48ID,
                                                      String td7ID,
                                                      boolean hasLocation) {

        // Check a couple simple properties
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.ENTITY_THING_TYPE,
            td48ID,
            ResourceManagementServiceImpl.UICDS_EVENT_STATUS_CODESPACE,
            "Declined",
            "Status",
            null));
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.ENTITY_THING_TYPE,
            td7ID,
            ResourceManagementServiceImpl.UICDS_EVENT_STATUS_CODESPACE,
            "Declined",
            "Status",
            null));

        // Check that there is at least one UCore what element
        assertTrue(DigestTester.checkEntityWhatElement(response.getEdxlDeResponse().getDigest(),
            td48ID,
            UCORE_CODESPACE_NS,
            "Equipment",
            null,
            null));
        assertTrue(DigestTester.checkEntityWhatElement(response.getEdxlDeResponse().getDigest(),
            td7ID,
            UCORE_CODESPACE_NS,
            "Equipment",
            null,
            null));

        // Check that each entity has a HasDestinationOf element
        if (hasLocation) {
            assertTrue(DigestTester.hasAHasDestinationOfElement(response.getEdxlDeResponse().getDigest(),
                td48ID));
            assertTrue(DigestTester.hasAHasDestinationOfElement(response.getEdxlDeResponse().getDigest(),
                td7ID));
        }

        // Validate the response
        assertTrue(ValidationUtil.validate(response, true));
    }

    private EdxlDeResponseDocument sendAndCheckCommitResource(String fileName) throws XmlException,
        IOException {

        String[] cores = {
            rmApp1, rmApp2
        };
        EDXLDistributionDocument edxl = getCommitResource(fileName, cores);

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);

        EasyMock.expect(workProductService.publishProduct(EasyMock.isA(WorkProduct.class))).andReturn(status);

        // expect that the message will get sent twice to the cores from the JIDs

        try {
            communicationsService.sendMessage(EasyMock.isA(String.class),
                EasyMock.eq(CommunicationsService.CORE2CORE_MESSAGE_TYPE.RESOURCE_MESSAGE),
                EasyMock.eq(BroadcastUtil.getCoreFromJID(rmApp1)));

            communicationsService.sendMessage(EasyMock.isA(String.class),
                EasyMock.eq(CommunicationsService.CORE2CORE_MESSAGE_TYPE.RESOURCE_MESSAGE),
                EasyMock.eq(BroadcastUtil.getCoreFromJID(rmApp2)));
        } catch (IllegalArgumentException e1) {
            fail(e1.getMessage());
        } catch (RemoteCoreUnknownException e1) {
            fail(e1.getMessage());
        } catch (RemoteCoreUnavailableException e1) {
            fail(e1.getMessage());
        } catch (LocalCoreNotOnlineException e1) {
            fail(e1.getMessage());
        } catch (NoShareAgreementException e1) {
            fail(e1.getMessage());
        } catch (NoShareRuleInAgreementException e1) {
            fail(e1.getMessage());
        }

        EasyMock.replay(workProductService, communicationsService);

        EdxlDeResponseDocument response = null;
        try {
            response = resourceManagementService.edxldeRequest(edxl.getEDXLDistribution());
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        } catch (EmptyCoreNameListException e) {
            fail(e.getMessage());
        } catch (SendMessageErrorException e) {
            fail(e.getMessage());
        } catch (LocalCoreNotOnlineException e) {
            fail(e.getMessage());
        } catch (NoShareAgreementException e) {
            fail(e.getMessage());
        } catch (NoShareRuleInAgreementException e) {
            fail(e.getMessage());
        }

        // System.out.println(response);
        assertNotNull(response);
        assertNotNull(response.getEdxlDeResponse());
        assertNotNull(response.getEdxlDeResponse().getErrorExists());
        assertFalse(response.getEdxlDeResponse().getErrorExists());

        EasyMock.verify(workProductService, communicationsService);
        return response;
    }

    public void testMessageReceipt() {

        String[] cores = {
            rmApp1, rmApp2
        };
        try {
            EDXLDistributionDocument edxl = getRequestResource("src/test/resources/EDXL-RM/RequestResource.1.xml",
                cores);

            Core2CoreMessage message = new Core2CoreMessage();
            message.setMessage(edxl.toString());

            // Expect calls to notify for each user
            notificationService.notify(EasyMock.eq(rmApp1Name),
                (NotificationMessageHolderType[]) EasyMock.anyObject());
            notificationService.notify(EasyMock.eq(rmApp2Name),
                (NotificationMessageHolderType[]) EasyMock.anyObject());

            EasyMock.replay(notificationService);

            resourceManagementService.resourceMessageNotificationHandler(message);

            EasyMock.verify(notificationService);

        } catch (XmlException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    private EdxlDeResponseDocument sendAndCheckRequestResource(String fileName)
        throws XmlException, IOException {

        String[] cores = {
            rmApp1, rmApp2
        };
        EDXLDistributionDocument edxl = getRequestResource(fileName, cores);

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);

        EasyMock.expect(workProductService.publishProduct(EasyMock.isA(WorkProduct.class))).andReturn(status);

        // expect that the message will get sent twice to the cores from the JIDs

        try {
            communicationsService.sendMessage(EasyMock.isA(String.class),
                EasyMock.eq(CommunicationsService.CORE2CORE_MESSAGE_TYPE.RESOURCE_MESSAGE),
                EasyMock.eq(BroadcastUtil.getCoreFromJID(rmApp1)));
            communicationsService.sendMessage(EasyMock.isA(String.class),
                EasyMock.eq(CommunicationsService.CORE2CORE_MESSAGE_TYPE.RESOURCE_MESSAGE),
                EasyMock.eq((BroadcastUtil.getCoreFromJID(rmApp2))));
        } catch (IllegalArgumentException e1) {
            fail(e1.getMessage());
        } catch (RemoteCoreUnknownException e1) {
            fail(e1.getMessage());
        } catch (RemoteCoreUnavailableException e1) {
            fail(e1.getMessage());
        } catch (LocalCoreNotOnlineException e1) {
            fail(e1.getMessage());
        } catch (NoShareAgreementException e1) {
            fail(e1.getMessage());
        } catch (NoShareRuleInAgreementException e1) {
            fail(e1.getMessage());
        }

        EasyMock.replay(workProductService, communicationsService);

        EdxlDeResponseDocument response = null;
        try {
            response = resourceManagementService.edxldeRequest(edxl.getEDXLDistribution());
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        } catch (EmptyCoreNameListException e) {
            fail(e.getMessage());
        } catch (SendMessageErrorException e) {
            fail(e.getMessage());
        } catch (LocalCoreNotOnlineException e) {
            fail(e.getMessage());
        } catch (NoShareAgreementException e) {
            fail(e.getMessage());
        } catch (NoShareRuleInAgreementException e) {
            fail(e.getMessage());
        }

        // System.out.println(response);
        assertNotNull(response);
        assertNotNull(response.getEdxlDeResponse());
        assertNotNull(response.getEdxlDeResponse().getErrorExists());
        assertFalse(response.getEdxlDeResponse().getErrorExists());

        EasyMock.verify(workProductService, communicationsService);

        return response;
    }

    private void checkCommonRequestResourceResponseElements(EdxlDeResponseDocument response,
                                                            String requestID,
                                                            boolean hasRequestedLocation) {

        // Check a couple simple properties
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.EVENT_THING_TYPE,
            requestID,
            ResourceManagementServiceImpl.UICDS_EVENT_STATUS_CODESPACE,
            "Open",
            "Status",
            null));
        assertTrue(DigestTester.checkSimpleProperty(response.getEdxlDeResponse().getDigest(),
            DigestTester.EVENT_THING_TYPE,
            requestID,
            ResourceManagementServiceImpl.NIMS_NS,
            null,
            "Kind",
            "Equipment"));

        // Check that there is at least one UCore what element
        assertTrue(DigestTester.checkEventWhatElement(response.getEdxlDeResponse().getDigest(),
            requestID,
            UCORE_CODESPACE_NS,
            "CommunicationEvent",
            null,
            null));

        // Get the request resource Event
        HashMap<String, String> codes = new HashMap<String, String>();
        codes.put(UICDS_CODESPACE_NS, "RequestResource");
        codes.put(UCORE_CODESPACE_NS, "CommunicationEvent");
        EventType event = DigestTester.getEventElementFromDigest(response.getEdxlDeResponse().getDigest(),
            codes);
        assertNotNull(event);

        // Check for the incident located at location
        codes.clear();
        codes.put(UCORE_CODESPACE_NS, "Event");
        EventType incidentEvent = DigestTester.getEventElementFromDigest(response.getEdxlDeResponse().getDigest(),
            codes);
        if (incidentEvent != null) {
            String schLocID = DigestTester.checkLocationDescriptor(response.getEdxlDeResponse().getDigest(),
                INCIDENT_LOCATION_DESCRIPTION);
            assertNotNull(schLocID);
            assertTrue(DigestTester.hasAOccursAtElement(response.getEdxlDeResponse().getDigest(),
                INCIDENT_ID,
                schLocID));

            // Check that the incident caused the request resource communication event
            assertTrue(DigestTester.hasACauseOfElement(response.getEdxlDeResponse().getDigest(),
                INCIDENT_ID,
                requestID));
        }

        // Check for the location for the requester (Organization) located at
        String reqLocID = null;
        if (hasRequestedLocation) {
            reqLocID = DigestTester.checkLocationDescriptor(response.getEdxlDeResponse().getDigest(),
                "Arlington");
            assertNotNull(reqLocID);
        }
        OrganizationType org = DigestTester.getOrganizationElementFromDigest(response.getEdxlDeResponse().getDigest(),
            CONTACT_DESCRIPTOR);
        assertNotNull(org);
        if (hasRequestedLocation) {
            assertTrue(DigestTester.hasLocatedAtElement(response.getEdxlDeResponse().getDigest(),
                org.getId(),
                reqLocID));
        }

        // Check that the requesting organization is involvedIn the request resource communication
        // event
        assertTrue(DigestTester.hasInvolvedInElement(response.getEdxlDeResponse().getDigest(),
            org.getId(),
            event.getId()));

        // Validate the response
        assertTrue(ValidationUtil.validate(response, true));
    }

    private EDXLDistributionDocument getRequestResource(String fileName, String[] coreNames)
        throws XmlException, IOException {

        File file = new File(fileName);
        assertNotNull(file);
        assertTrue(file.exists());
        RequestResourceDocument requestResource = RequestResourceDocument.Factory.parse(file);
        // System.out.println(requestResource.type.getDocumentElementName());
        EDXLDistributionDocument edxl = EDXLDistributionDocument.Factory.newInstance();
        edxl.addNewEDXLDistribution().addNewContentObject().addNewXmlContent().addNewEmbeddedXMLContent().set(requestResource);
        for (String core : coreNames) {
            addExplicitAddress(edxl, core);
        }
        return edxl;
    }

    private EDXLDistributionDocument getCommitResource(String fileName, String[] coreNames)
        throws XmlException, IOException {

        File file = new File(fileName);
        assertNotNull(file);
        assertTrue(file.exists());
        CommitResourceDocument commitResource = CommitResourceDocument.Factory.parse(file);
        // System.out.println(requestResource.type.getDocumentElementName());
        EDXLDistributionDocument edxl = EDXLDistributionDocument.Factory.newInstance();
        edxl.addNewEDXLDistribution().addNewContentObject().addNewXmlContent().addNewEmbeddedXMLContent().set(commitResource);
        for (String core : coreNames) {
            addExplicitAddress(edxl, core);
        }
        return edxl;
    }

    private void addExplicitAddress(EDXLDistributionDocument edxl, String name) {

        ValueSchemeType explictAddress = edxl.getEDXLDistribution().addNewExplicitAddress();
        explictAddress.setExplicitAddressScheme(CommunicationsService.UICDSExplicitAddressScheme);
        explictAddress.addExplicitAddressValue(name);
    }
}
