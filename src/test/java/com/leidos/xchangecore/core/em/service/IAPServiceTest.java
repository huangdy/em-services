/**
 * 
 */
package com.leidos.xchangecore.core.em.service;

import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.EntityType;
import gov.ucore.ucore.x20.ThingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.uicds.iapService.IncidentActionPlanDocument;
import org.uicds.iapService.IncidentActionPlanType;
import org.uicds.icsForm201.ICSForm201Document;
import org.uicds.icsFormCommon.ICSFormDocumentType;

import com.leidos.xchangecore.core.em.service.IAPService;
import com.leidos.xchangecore.core.em.service.impl.IAPServiceImpl;
import com.leidos.xchangecore.core.em.util.EMDigestHelper;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.DigestConstant;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.InfrastructureNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.base.PropertiesType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * NotificationServiceTest
 * 
 * @author roger, initial revision
 * 
 */
public class IAPServiceTest
    extends AbstractDependencyInjectionSpringContextTests {

    private static final String VERSION_1 = "1";

    private static final String IAP_1_CHECKSUM = "IAP-1-checksum";

    private static final String IAP_2_CHECKSUM = "IAP-2-checksum";

    private static final String ICS_WORK_PRODUCT_ID1 = "ICS-1";

    private static final String ICS_WORK_PRODUCT_ID2 = "ICS-2";

    private static final String ICS_201 = "201";

    private static final String EXPIRATION_DATE = "01/02/2010";

    private static final String EFFECTIVE_DATE = "01/01/2010";

    private static final String ICS_DESCRIPTION = "ICS Description";

    private static final String ACT_1 = "ACT-1";

    private final static String INCIDENT_ID = "l";

    private final static String IAP_WORK_PRODUCT_ID1 = "WP-1";

    private final static String IAP_WORK_PRODUCT_ID2 = "WP-2";

    private final static String IAP_WORK_PRODUCT_VERSION1 = "1";

    private final static String IAP_WORK_PRODUCT_VERSION2 = "2";

    private final static String IAP_NAME = "IAP Name";

    private final static String IAP_NAME2 = "UPDATED IAP Name";

    private final static String APPROVED_IAP = "Approved";

    private final static String DRAFT_IAP = "Draft";

    IAPServiceImpl iapService = new IAPServiceImpl();

    private org.easymock.Capture<WorkProduct> capture1;

    private org.easymock.Capture<WorkProduct> capture2;

    WorkProductService workProductService;

    @Override
    public void onSetUp() {

        workProductService = EasyMock.createMock(WorkProductService.class);
        iapService.setWorkProductService(workProductService);
    }

    @Test
    public void testCreateEmptyIAP() {

        assertNotNull(iapService);

        EasyMock.reset(workProductService);

        IncidentActionPlanType plan = createIAPType(IAP_NAME);

        WorkProduct product = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);

        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);

        // Capture the created work product
        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();

        EasyMock.expect(workProductService.publishProduct(EasyMock.and(EasyMock.isA(WorkProduct.class),
            EasyMock.capture(capture)))).andReturn(status);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.createIAP(plan);

        assertNotNull(returnStatus);
        checkCapturedWorkProductForIGAssociation(capture);
        EasyMock.verify(workProductService);
    }

    @Test
    public void testUpdateEmptyIAP() {

        assertNotNull(iapService);

        EasyMock.reset(workProductService);

        IncidentActionPlanType plan = createIAPType(IAP_NAME);

        WorkProduct product = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        IdentificationType wpid = WorkProductHelper.getWorkProductIdentification(product);

        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);

        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(product);
        EasyMock.expect(workProductService.publishProduct(EasyMock.isA(WorkProduct.class))).andReturn(status);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.updateIAP(plan, wpid, false);

        assertNotNull(returnStatus);
        assertEquals("failed update",
            ProductPublicationStatus.SuccessStatus,
            returnStatus.getStatus());
        assertNotNull("missing work product", returnStatus.getProduct());
        assertNotNull("missing work product content", returnStatus.getProduct().getProduct());
        EasyMock.verify(workProductService);
    }

    @Test
    public void testUpdateUnknownIAP() {

        assertNotNull(iapService);

        EasyMock.reset(workProductService);

        IncidentActionPlanType plan = createIAPType(IAP_NAME);

        WorkProduct product = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        IdentificationType wpid = WorkProductHelper.getWorkProductIdentification(product);

        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(null);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.updateIAP(plan, wpid, false);

        assertNotNull(returnStatus);
        assertEquals("incorrectly failed update",
            ProductPublicationStatus.FailureStatus,
            returnStatus.getStatus());
        EasyMock.verify(workProductService);
    }

    @Test
    public void testCreateICS() {

        EasyMock.reset(workProductService);

        ICSFormDocumentType form = createICS201Form();

        WorkProduct product = createWorkProductForResponse(ICS_WORK_PRODUCT_ID1,
            IAPService.ICSFORM_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            form);

        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);

        // Capture the created work product
        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();

        EasyMock.expect(workProductService.publishProduct(EasyMock.and(EasyMock.isA(WorkProduct.class),
            EasyMock.capture(capture)))).andReturn(status);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.createICSForm(form);

        assertNotNull(returnStatus);
        checkCapturedWorkProductForIGAssociation(capture);
        EasyMock.verify(workProductService);
    }

    @Test
    public void testUpdateICS() {

        EasyMock.reset(workProductService);

        ICSFormDocumentType form = createICS201Form();

        WorkProduct product = createWorkProductForResponse(ICS_WORK_PRODUCT_ID1,
            IAPService.ICSFORM_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            form);
        IdentificationType wpid = WorkProductHelper.getWorkProductIdentification(product);

        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);

        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(product);
        EasyMock.expect(workProductService.publishProduct(EasyMock.isA(WorkProduct.class))).andReturn(status);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.updateICSForm(form, wpid);

        assertNotNull(returnStatus);
        assertEquals("failed update",
            ProductPublicationStatus.SuccessStatus,
            returnStatus.getStatus());
        EasyMock.verify(workProductService);
    }

    @Test
    public void testUpdateUnknownICS() {

        EasyMock.reset(workProductService);

        ICSFormDocumentType form = createICS201Form();

        WorkProduct product = createWorkProductForResponse(ICS_WORK_PRODUCT_ID1,
            IAPService.ICSFORM_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            form);
        IdentificationType wpid = WorkProductHelper.getWorkProductIdentification(product);

        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(null);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.updateICSForm(form, wpid);

        assertNotNull(returnStatus);
        assertEquals("incorrectly failed update",
            ProductPublicationStatus.FailureStatus,
            returnStatus.getStatus());
        EasyMock.verify(workProductService);
    }

    @Test
    public void testAttachICSToIAP() {

        EasyMock.reset(workProductService);

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier().setStringValue(ICS_WORK_PRODUCT_ID1);
        IdentificationType[] componentIds = {
            id
        };

        IncidentActionPlanType plan = createIAPType(IAP_NAME);

        WorkProduct product = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        checkWorkProductForComponents(WorkProductHelper.toWorkProductDocument(product), 0);
        EasyMock.expect(workProductService.getProduct(EasyMock.isA(String.class))).andReturn(product);

        // Capture the updated work product
        org.easymock.Capture<WorkProduct> capture = new org.easymock.Capture<WorkProduct>();
        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);
        EasyMock.expect(workProductService.publishProduct(EasyMock.and(EasyMock.isA(WorkProduct.class),
            EasyMock.capture(capture)))).andReturn(status);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.attachWorkProductToIAP(componentIds,
            IAP_WORK_PRODUCT_ID1);

        assertNotNull(returnStatus);
        WorkProductDocument wpd = WorkProductHelper.toWorkProductDocument(capture.getValue());
        checkWorkProductForComponents(wpd, 1);

        EasyMock.verify(workProductService);
    }

    @Test
    public void testAttachICSToIAPTwice() {

        EasyMock.reset(workProductService);

        IdentificationType id = IdentificationType.Factory.newInstance();
        id.addNewIdentifier().setStringValue(ICS_WORK_PRODUCT_ID1);
        IdentificationType[] componentIds = {
            id
        };

        // Add the component to the starting work product
        IncidentActionPlanType plan = createIAPType(IAP_NAME);
        plan.addNewComponents().addNewComponent().addNewComponentIdentifier().addNewWorkProductIdentification().addNewIdentifier().setStringValue(ICS_WORK_PRODUCT_ID1);

        WorkProduct product = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        checkWorkProductForComponents(WorkProductHelper.toWorkProductDocument(product), 1);
        EasyMock.expect(workProductService.getProduct(EasyMock.isA(String.class))).andReturn(product);

        EasyMock.replay(workProductService);

        ProductPublicationStatus returnStatus = iapService.attachWorkProductToIAP(componentIds,
            IAP_WORK_PRODUCT_ID1);

        // The IAP should not have called publishProduct but should get a success status
        assertNotNull(returnStatus);
        assertEquals("failed attach",
            ProductPublicationStatus.SuccessStatus,
            returnStatus.getStatus());

        EasyMock.verify(workProductService);
    }

    @Test
    public void testActivateAndGetFirstIAP() {

        HashMap<String, IdentificationType> ids = setupForSetApprovedIAPCallWithNoCurrrentApprovedIAP();

        EasyMock.replay(workProductService);

        ProductPublicationStatus status = iapService.setApprovedIAP(ids.get(DRAFT_IAP), INCIDENT_ID);
        assertEquals("failed activate", ProductPublicationStatus.SuccessStatus, status.getStatus());

        // Use product with the digest that was created
        WorkProduct product = capture1.getValue();
        checkProductIsApproved(product);
        assertTrue("wrong id for approved wp", IAP_WORK_PRODUCT_ID1 != product.getProductID());
        assertTrue("wrong id for approved wp", IAP_WORK_PRODUCT_ID2 != product.getProductID());

        EasyMock.verify(workProductService);

        // Test get
        EasyMock.reset(workProductService);

        ArrayList<WorkProduct> list = new ArrayList<WorkProduct>();
        list.add(product);
        EasyMock.expect(workProductService.findByInterestGroupAndType(INCIDENT_ID,
            IAPService.IAP_WORKPRODUCT_TYPE)).andReturn(list);
        // EasyMock.expect(workProductService.getAllVersionsOfProduct(EasyMock.isA(String.class))).andReturn(
        // list);

        EasyMock.replay(workProductService);

        WorkProduct wp = iapService.getApprovedIAP(INCIDENT_ID);

        assertNotNull("iap not found", wp);

        EasyMock.verify(workProductService);
    }

    @Test
    public void testGetActiveWithDraftAndApprovedIAPs() {

        HashMap<String, WorkProduct> products = setupForGetApprovedIAPCallWithCurrrentApprovedIAP();

        EasyMock.replay(workProductService);

        WorkProduct wp = iapService.getApprovedIAP(INCIDENT_ID);

        assertNotNull("iap not found", wp);
        assertEquals("wrong iap wp", IAP_WORK_PRODUCT_ID2, wp.getProductID());

        WorkProduct expectedApprovedWP = products.get(IAP_WORK_PRODUCT_ID2 + "-" +
                                                      IAP_WORK_PRODUCT_VERSION1);

        EasyMock.verify(workProductService);
    }

    @Test
    public void testSetActiveWithDraftAndApprovedIAPs() {

        HashMap<String, WorkProduct> products = setupForSetApprovedIAPCallWithCurrrentApprovedIAP();

        WorkProduct draftToApprove = products.get(IAP_WORK_PRODUCT_ID1 + "-" +
                                                  IAP_WORK_PRODUCT_VERSION2);
        IdentificationType idToApprove = WorkProductHelper.getWorkProductIdentification(draftToApprove);

        EasyMock.replay(workProductService);

        ProductPublicationStatus status = iapService.setApprovedIAP(idToApprove, INCIDENT_ID);
        assertEquals("failed activate", ProductPublicationStatus.SuccessStatus, status.getStatus());

        WorkProduct approvedWP = capture1.getValue();
        checkProductIsApproved(approvedWP);
        checkIAPName(approvedWP, IAP_NAME2);

        EasyMock.verify(workProductService);
    }

    @Test
    public void testGetICSList() {

        EasyMock.reset(workProductService);

        ICSFormDocumentType form1 = createICS201Form();

        WorkProduct product1 = createWorkProductForResponse(ICS_WORK_PRODUCT_ID1,
            IAPService.ICSFORM_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            form1);

        ArrayList<WorkProduct> products = new ArrayList<WorkProduct>();
        products.add(product1);

        EasyMock.expect(workProductService.findByInterestGroupAndType(INCIDENT_ID,
            IAPService.ICSFORM_WORKPRODUCT_TYPE)).andReturn(products);

        EasyMock.replay(workProductService);

        WorkProduct[] list = iapService.getICSFormList(INCIDENT_ID);

        assertNotNull(list);
        assertEquals("wrong size", 1, list.length);
        assertEquals("wrong id", ICS_WORK_PRODUCT_ID1, list[0].getProductID());
        EasyMock.verify(workProductService);

        EasyMock.reset(workProductService);

        ICSFormDocumentType form2 = createICS201Form();

        WorkProduct product2 = createWorkProductForResponse(ICS_WORK_PRODUCT_ID2,
            IAPService.ICSFORM_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            form2);

        products.clear();
        products.add(product1);
        products.add(product2);

        EasyMock.expect(workProductService.findByInterestGroupAndType(INCIDENT_ID,
            IAPService.ICSFORM_WORKPRODUCT_TYPE)).andReturn(products);

        EasyMock.replay(workProductService);

        list = iapService.getICSFormList(INCIDENT_ID);

        assertNotNull(list);
        assertEquals("wrong size", 2, list.length);
        boolean foundOne = false;
        boolean foundTwo = false;
        for (WorkProduct wp : list) {
            if (wp.getProductID().equals(ICS_WORK_PRODUCT_ID1)) {
                foundOne = true;
            }
            if (wp.getProductID().equals(ICS_WORK_PRODUCT_ID2)) {
                foundTwo = true;
            }
        }
        assertTrue("ICS 1 not found", foundOne);
        assertTrue("ICS 2 not found", foundTwo);
        EasyMock.verify(workProductService);
    }

    private void checkProductIsApproved(WorkProduct product) {

        assertNotNull("product is null", product);
        assertNotNull("digest is null", product.getDigest());
        DigestDocument digest = product.getDigest();
        Set<ThingType> things = DigestHelper.getThingsByWhatType(digest.getDigest(),
            InfrastructureNamespaces.NS_UCORE_CODESPACE,
            DigestConstant.S_Document);
        assertEquals("wrong # of things", 1, things.size());
        ThingType thing = things.iterator().next();
        assertTrue("wrong type of thing", thing instanceof EntityType);
        assertEquals("no props", 1, thing.sizeOfSimplePropertyArray());
        assertEquals("not approved", "Approved", thing.getSimplePropertyArray(0).getCode());
    }

    private HashMap<String, IdentificationType> setupForSetApprovedIAPCallWithNoCurrrentApprovedIAP() {

        EasyMock.reset(workProductService);

        IdentificationType workProductID = getIAPWorkProductID1();

        // first return the product that is being set to approved
        IncidentActionPlanType plan = createIAPType(IAP_NAME);
        WorkProduct product1 = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        EasyMock.expect(workProductService.getProduct(workProductID)).andReturn(product1);

        // get list of wps for search for approved work product, should return empty list
        ArrayList<WorkProduct> list = new ArrayList<WorkProduct>();
        EasyMock.expect(workProductService.findByInterestGroupAndType(INCIDENT_ID,
            IAPService.IAP_WORKPRODUCT_TYPE)).andReturn(list);
        // EasyMock.expect(workProductService.getAllVersionsOfProduct(EasyMock.isA(String.class))).andReturn(
        // list);

        // Capture the updated work product
        capture1 = new org.easymock.Capture<WorkProduct>();

        // now a new work product with ID2 should be published
        WorkProduct product2 = createWorkProductForResponse(IAP_WORK_PRODUCT_ID2,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            plan);
        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product2);
        EasyMock.expect(workProductService.publishProduct(EasyMock.and(EasyMock.isA(WorkProduct.class),
            EasyMock.capture(capture1)))).andReturn(status);

        IdentificationType newWorkProductID = getIAPWorkProductID2();

        HashMap<String, IdentificationType> ids = new HashMap<String, IdentificationType>();
        ids.put(DRAFT_IAP, workProductID);
        ids.put(APPROVED_IAP, newWorkProductID);

        return ids;
    }

    private HashMap<String, WorkProduct> setupForGetApprovedIAPCallWithCurrrentApprovedIAP() {

        HashMap<String, WorkProduct> products = new HashMap<String, WorkProduct>();

        // Reset work product service mock
        EasyMock.reset(workProductService);

        // Setup list to contain two active work products with different version numbers
        ArrayList<WorkProduct> list = new ArrayList<WorkProduct>();

        IncidentActionPlanType iap = createIAPType(IAP_NAME);
        WorkProduct product1 = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            iap);
        IdentificationType workProductID = WorkProductHelper.getWorkProductIdentification(product1);
        product1.setDigest(new EMDigestHelper(iap,
                                              workProductID.getIdentifier().getStringValue(),
                                              false).getDigest());
        list.add(product1);
        products.put(IAP_WORK_PRODUCT_ID1 + "-" + IAP_WORK_PRODUCT_VERSION1, product1);

        IncidentActionPlanType iap2 = createIAPType(IAP_NAME2);
        WorkProduct product2 = createWorkProductForResponse(IAP_WORK_PRODUCT_ID1,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION2,
            iap2);
        workProductID = WorkProductHelper.getWorkProductIdentification(product2);
        product2.setDigest(new EMDigestHelper(iap,
                                              workProductID.getIdentifier().getStringValue(),
                                              false).getDigest());
        list.add(product2);
        products.put(IAP_WORK_PRODUCT_ID1 + "-" + IAP_WORK_PRODUCT_VERSION2, product2);

        WorkProduct product3 = createWorkProductForResponse(IAP_WORK_PRODUCT_ID2,
            IAPService.IAP_WORKPRODUCT_TYPE,
            IAP_WORK_PRODUCT_VERSION1,
            iap);
        workProductID = WorkProductHelper.getWorkProductIdentification(product3);
        product3.setDigest(new EMDigestHelper(iap,
                                              workProductID.getIdentifier().getStringValue(),
                                              true).getDigest());
        list.add(product3);
        products.put(IAP_WORK_PRODUCT_ID2 + "-" + IAP_WORK_PRODUCT_VERSION1, product3);

        EasyMock.expect(workProductService.findByInterestGroupAndType(INCIDENT_ID,
            IAPService.IAP_WORKPRODUCT_TYPE)).andReturn(list);

        return products;
    }

    private HashMap<String, WorkProduct> setupForSetApprovedIAPCallWithCurrrentApprovedIAP() {

        HashMap<String, WorkProduct> products = setupForGetApprovedIAPCallWithCurrrentApprovedIAP();

        // first return the product that is being set to approved
        WorkProduct draftToApprove = products.get(IAP_WORK_PRODUCT_ID1 + "-" +
                                                  IAP_WORK_PRODUCT_VERSION2);
        // IdentificationType idToApprove =
        // WorkProductHelper.getWorkProductIdentification(draftToApprove);
        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(draftToApprove);

        // Capture the updated work product
        capture1 = new org.easymock.Capture<WorkProduct>();

        // setup to expect the next version of the current approved work product
        WorkProduct product = products.get(IAP_WORK_PRODUCT_ID2 + "-" + IAP_WORK_PRODUCT_VERSION1);
        product.setProductVersion(product.getProductVersion() + 1);
        ProductPublicationStatus status = createSucessStatusFromWorkProduct(product);
        EasyMock.expect(workProductService.publishProduct(EasyMock.and(EasyMock.isA(WorkProduct.class),
            EasyMock.capture(capture1)))).andReturn(status);

        return products;
    }

    private IdentificationType getIAPWorkProductID2() {

        IdentificationType newWorkProductID = IdentificationType.Factory.newInstance();
        newWorkProductID.addNewChecksum().setStringValue(IAP_2_CHECKSUM);
        newWorkProductID.addNewIdentifier().setStringValue(IAP_WORK_PRODUCT_ID2);
        newWorkProductID.addNewType().setStringValue(IAPService.IAP_WORKPRODUCT_TYPE);
        newWorkProductID.addNewVersion().setStringValue(VERSION_1);
        return newWorkProductID;
    }

    private IdentificationType getIAPWorkProductID1() {

        IdentificationType workProductID = IdentificationType.Factory.newInstance();
        workProductID.addNewChecksum().setStringValue(IAP_1_CHECKSUM);
        workProductID.addNewIdentifier().setStringValue(IAP_WORK_PRODUCT_ID1);
        workProductID.addNewType().setStringValue(IAPService.IAP_WORKPRODUCT_TYPE);
        workProductID.addNewVersion().setStringValue(VERSION_1);
        return workProductID;
    }

    private void checkWorkProductForComponents(WorkProductDocument wpd, int numberOfComponents) {

        assertNotNull(wpd);
        assertEquals("no payload", 1, wpd.getWorkProduct().sizeOfStructuredPayloadArray());
        try {
            IncidentActionPlanDocument doc = (IncidentActionPlanDocument) WorkProductHelper.getPayload(wpd.getWorkProduct().getStructuredPayloadArray(0));
            assertNotNull("iap is null", doc.getIncidentActionPlan());
            if (numberOfComponents > 0) {
                assertNotNull("components are null", doc.getIncidentActionPlan().getComponents());
                assertEquals("no components",
                    numberOfComponents,
                    doc.getIncidentActionPlan().getComponents().sizeOfComponentArray());
            }
        } catch (Throwable e) {
            fail("Cannot parse payload into IAP: " +
                 WorkProductHelper.getPayload(wpd.getWorkProduct().getStructuredPayloadArray(0)));
        }
    }

    private void checkIAPName(WorkProduct wp, String name) {

        assertNotNull(wp);
        assertNotNull("no payload", wp.getProduct());
        IncidentActionPlanDocument doc = (IncidentActionPlanDocument) wp.getProduct();
        assertNotNull("iap is null", doc.getIncidentActionPlan());
        assertNotNull("iap name is null", doc.getIncidentActionPlan().getName());
        assertEquals("wrong name", name, doc.getIncidentActionPlan().getName());
    }

    private void checkCapturedWorkProductForIGAssociation(org.easymock.Capture<WorkProduct> capture) {

        WorkProductDocument wpd = WorkProductHelper.toWorkProductDocument(capture.getValue());
        assertNotNull("work product is null", wpd);
        PropertiesType properties = WorkProductHelper.getPropertiesElement(wpd.getWorkProduct());
        assertNotNull("properties is null", properties);
        assertNotNull("associated groups is null", properties.getAssociatedGroups());
        assertEquals("no associated groups",
            1,
            properties.getAssociatedGroups().sizeOfIdentifierArray());
        assertEquals("not associated with incident",
            INCIDENT_ID,
            properties.getAssociatedGroups().getIdentifierArray(0).getStringValue());
    }

    private ICSFormDocumentType createICS201Form() {

        ICSFormDocumentType form = ICSFormDocumentType.Factory.newInstance();
        form.setIncidentID(INCIDENT_ID);
        form.setDocumentDescriptionText(ICS_DESCRIPTION);
        form.setDocumentEffectiveDate(EFFECTIVE_DATE);
        form.setDocumentExpirationDate(EXPIRATION_DATE);
        form.setICSFormNumber(ICS_201);
        return form;
    }

    private ProductPublicationStatus createSucessStatusFromWorkProduct(WorkProduct product) {

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setAct(ACT_1);
        status.setProduct(product);
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        return status;
    }

    private WorkProduct createWorkProductForResponse(String id,
                                                     String type,
                                                     String version,
                                                     XmlObject productObject) {

        WorkProduct product = new WorkProduct();
        product.setActive(true);
        product.setProductID(id);
        product.setProductType(type);
        product.setProductVersion(new Integer(version));
        if (productObject != null) {
            if (type.equals(IAPService.IAP_WORKPRODUCT_TYPE)) {
                IncidentActionPlanDocument doc = IncidentActionPlanDocument.Factory.newInstance();
                doc.addNewIncidentActionPlan().set(productObject);
                product.setProduct(doc);
            } else if (type.equals(IAPService.ICSFORM_WORKPRODUCT_TYPE)) {
                ICSForm201Document doc = ICSForm201Document.Factory.newInstance();
                doc.addNewICSForm201().set(productObject);
                product.setProduct(doc);
            }
        }
        return product;
    }

    private IncidentActionPlanType createIAPType(String name) {

        IncidentActionPlanType plan = IncidentActionPlanType.Factory.newInstance();
        plan.setDescriptionText("IAP description");
        plan.setDocumentEffectiveDate(EFFECTIVE_DATE);
        plan.setDocumentExpirationDate(EXPIRATION_DATE);
        plan.setIncidentID(INCIDENT_ID);
        plan.setName(name);
        return plan;
    }

}
