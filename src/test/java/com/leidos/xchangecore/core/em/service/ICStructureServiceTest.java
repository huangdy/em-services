package com.leidos.xchangecore.core.em.service;

import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.uicds.incidentManagementService.IncidentInfoType;
import org.uicds.organizationElement.OrganizationElementDocument;
import org.uicds.organizationElement.OrganizationElementType;
import org.uicds.organizationElement.OrganizationPositionType;

import com.leidos.xchangecore.core.em.service.IncidentManagementService;
import com.leidos.xchangecore.core.em.service.impl.IncidentCommandServiceImpl;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.saic.precis.x2009.x06.base.IdentificationType;

public class ICStructureServiceTest {

    // private WorkProductService workProductService;

    private IncidentCommandServiceImpl incidentCommandService;

    private WorkProductService workProductService;
    private IncidentManagementService incidentManagementService;

    private String incidentID = "43211";
    private String operationPeriod = "November";
    private String organizationName = "Incident Command";
    private String organizationType = "Incident Command";
    private String personInCharge = "Maverick";
    private String roleName1 = "http://john/";
    private String role1 = "http://commander/";

    private String subOrgName = "Planning";
    private String subOrgType = "Branch";
    private String subPersonCharge = "subMaverick";
    private String subName1 = "Nathan";
    private String subrole1 = "CEO";

    private IncidentInfoType incidentInfo = IncidentInfoType.Factory.newInstance();

    @Before
    public void setUp() {

        incidentCommandService = new IncidentCommandServiceImpl();

        workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
        incidentCommandService.setWorkProductService(workProductService);
    }

    /*
     */
    @Test
    @Ignore
    public void testGetICStructure() throws Exception {

        assertNotNull(incidentCommandService);
        /*
         * // workProductService = org.easymock.EasyMock.createMock(WorkProductService.class); //
         * incidentCommandService.setWorkProductService(workProductService);
         * UpdateCommandStructureRequestDocument req =
         * UpdateCommandStructureRequestDocument.Factory.parse(new File(
         * "../testing/data/inc1Demo/Services/UpdateCommandStructure.xml")); OrganizationDocument
         * orgDoc = OrganizationDocument.Factory.newInstance();
         * orgDoc.setOrganization(req.getUpdateCommandStructureRequest().getOrganization()); String
         * incidentID = req.getUpdateCommandStructureRequest().getIncidentID();
         * ProductPublicationStatus status = incidentCommandService.updateCommandStructure(
         * req.getUpdateCommandStructureRequest().getWorkProductIdentification(), orgDoc,
         * incidentID);
         * 
         * WorkProductIdentificationDocument pkgId =
         * WorkProductIdentificationDocument.Factory.newInstance(); WorkProductSummary summary =
         * WorkProductHelper.getWorkProductSummary(status.getProduct());
         * pkgId.addNewWorkProductIdentification().set(summary.getWorkProductIdentification()); //
         * Test returning empty Incident Command Structure // IncidentCommandStructureDocument
         * icsModel = // incidentCommandService.getCommandStructureByIncident(pkgId); //
         * assertNotNull(icsModel); //
         * assertNotNull(icsModel.getIncidentCommandStructure().getId()); // should return //
         * autogeneratedID // log.debug("ICS MODEL:  GENERATED ID:  " + //
         * icsModel.getIncidentCommandStructure().getId());
         * 
         * // Test Returning real IncidentCommandStructure
         */}

    @Test
    @Ignore
    public void testConfigureICStructure() {

        assertNotNull(incidentCommandService);
        /*
         * 
         * IncidentCommandStructureDocument ics =
         * incidentCommandService.getCommandStructureByIncident(null); assertNotNull(ics);
         * assertNotNull(ics.getIncidentCommandStructure().getId()); log.debug("ICS ID==== " +
         * ics.getIncidentCommandStructure().getId());
         * 
         * // get empty organization and fill in information OrganizationDocument orgDoc =
         * incidentCommandService.getCommandStructure(null);
         * 
         * assertNotNull(orgDoc);
         * 
         * UICDSOrganizationElementType orgElem = orgDoc.addNewOrganization();
         * orgElem.setOrganizationName(organizationName);
         * orgElem.setOrganizationType(organizationType); UICDSOrganizationPositionType posInCharge
         * = UICDSOrganizationPositionType.Factory.newInstance();
         * posInCharge.setPersonProfileRef(personInCharge);
         * posInCharge.setRoleProfileRef("IncidentCommander");
         * orgElem.setPersonInCharge(posInCharge); // set up staff in ICS
         * UICDSOrganizationPositionType position =
         * UICDSOrganizationPositionType.Factory.newInstance();
         * position.setPersonProfileRef(roleName1); position.setRoleProfileRef(role1);
         * UICDSOrganizationPositionType[] staffs = new UICDSOrganizationPositionType[1]; staffs[0]
         * = position; orgElem.setStaffArray(staffs);
         * 
         * // update organization element ProductPublicationStatus status =
         * incidentCommandService.updateCommandStructure(orgDoc, null);
         * 
         * // TODO need to be uncommented !!! // assertNotNull(status); //
         * assertEquals(status.getStatus(), ProductPublicationStatus.SuccessStatus); //
         * assertNotNull(status.getProduct().getProductID());
         * 
         * // associate organization to ICS workProductService =
         * org.easymock.EasyMock.createMock(WorkProductService.class);
         * incidentCommandService.setWorkProductService(workProductService); String icsWPID = "";
         * 
         * status = new ProductPublicationStatus();
         * status.setStatus(ProductPublicationStatus.SuccessStatus); WorkProduct product = new
         * WorkProduct(); product.setProductID(ics.getIncidentCommandStructure().getId());
         * status.setProduct(product);
         * 
         * org.easymock.EasyMock.expect(
         * workProductService.publishProduct(org.easymock.EasyMock.isA(
         * WorkProduct.class))).andReturn( status);
         * org.easymock.EasyMock.replay(workProductService);
         * 
         * incidentCommandService.associateOrganizationToICStructure(
         * status.getProduct().getProductID(),
         * ics.getIncidentCommandStructure().getId().toString());
         * 
         * // TODO need to be uncommented !!! // org.easymock.EasyMock.verify(workProductService);
         * 
         * assertNotNull(icsWPID);
         */

    }

    @Test
    @Ignore
    public void testGetOrganization() {

        /*
         * assertNotNull(incidentCommandService);
         * 
         * // workProductService = org.easymock.EasyMock.createMock(WorkProductService.class); //
         * incidentCommandService.setWorkProductService(workProductService);
         * 
         * // Test returning empty organization OrganizationDocument orgDoc =
         * incidentCommandService.getCommandStructure(null);
         * 
         * assertNotNull(orgDoc);
         * 
         * // Test returning real organization // done in testUpdateOrganization after an update
         */}

    /**
     * 
     */
    @Test
    public void testUpdateOrganization() {

        // Test returning empty organization
        OrganizationElementDocument orgDoc = buildSampleOrganzationDocument();
        assertNotNull(orgDoc);

        ProductPublicationStatus pubStatus = new ProductPublicationStatus();
        WorkProduct wp = new WorkProduct();
        IdentificationType wpID = IdentificationType.Factory.newInstance();
        wpID.addNewIdentifier().setStringValue("1");
        wp.setProduct(orgDoc);
        pubStatus.setProduct(wp);
        pubStatus.setStatus(ProductPublicationStatus.SuccessStatus);
        EasyMock.expect(workProductService.publishProduct(EasyMock.isA(WorkProduct.class))).andReturn(pubStatus);
        EasyMock.expect(workProductService.getProduct(EasyMock.isA(IdentificationType.class))).andReturn(wp);

        EasyMock.replay(workProductService);

        ProductPublicationStatus status = incidentCommandService.updateCommandStructure(wpID,
            orgDoc,
            null);
        assertNotNull(status);

        // Make sure the new org work product was published
        EasyMock.verify(workProductService);
    }

    /**
     * 
     */
    @Test
    @Ignore
    public void testAssociateOrganizationToICStructure() {

        assertNotNull(incidentCommandService);

        /*
         * workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
         * incidentCommandService.setWorkProductService(workProductService);
         * 
         * // Test associating organizationDocument to new ICS OrganizationDocument orgDoc =
         * buildSampleOrganzationDocument(); ProductPublicationStatus status =
         * incidentCommandService.updateCommandStructure(null, orgDoc, null);
         * 
         * // TODO: need to be uncommneted // assertNotNull(status); //
         * assertEquals(status.getStatus(), ProductPublicationStatus.SuccessStatus); // String orgID
         * = status.getProduct().getProductID();
         * 
         * String icsWPID = "";
         * 
         * status = new ProductPublicationStatus();
         * status.setStatus(ProductPublicationStatus.SuccessStatus); WorkProduct product = new
         * WorkProduct(); product.setProductID(icsWPID); status.setProduct(product);
         * 
         * org.easymock.EasyMock.expect(
         * workProductService.publishProduct(org.easymock.EasyMock.isA(
         * WorkProduct.class))).andReturn( status);
         * org.easymock.EasyMock.replay(workProductService);
         */
        // TODO: need to be uncommneted
        // status = incidentCommandService.associateOrganizationToICStructure(orgID, null);
        // assertNotNull(status);
        // assertEquals(status.getStatus(), ProductPublicationStatus.SuccessStatus);
        // String icsID = status.getProduct().getProductID();
        // assertNotNull(icsID);
        // org.easymock.EasyMock.verify(workProductService);
        // Test associating organizationDocument to existingICS
    }

    /**
     * 
     */
    @Test
    @Ignore
    public void testAssociateIncidentToICStructure() {

        assertNotNull(incidentCommandService);

        /*
         * incidentManagementService =
         * org.easymock.EasyMock.createMock(IncidentManagementService.class);
         * incidentCommandService.setIncidentManagementService(incidentManagementService);
         * 
         * workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
         * incidentCommandService.setWorkProductService(workProductService);
         * 
         * String icsWPID = ""; ProductPublicationStatus status = new ProductPublicationStatus();
         * status.setStatus(ProductPublicationStatus.SuccessStatus); WorkProduct product = new
         * WorkProduct(); product.setProductID(icsWPID); status.setProduct(product);
         * 
         * org.easymock.EasyMock.expect(
         * workProductService.publishProduct(org.easymock.EasyMock.isA(
         * WorkProduct.class))).andReturn( status); org.easymock.EasyMock.expect(
         * workProductService.associateWorkProductToInterestGroup(
         * org.easymock.EasyMock.isA(String.class),
         * org.easymock.EasyMock.isA(String.class))).andReturn(icsWPID);
         * org.easymock.EasyMock.replay(workProductService);
         * 
         * org.easymock.EasyMock.expect(incidentManagementService.getIncidentInfo(incidentID)).andReturn
         * ( incidentInfo); org.easymock.EasyMock.replay(incidentManagementService);
         * 
         * String icsID = incidentCommandService.associateCommandStructureToIncident(incidentID,
         * null);
         * 
         * assertNotNull(icsID);
         */
        // TODO - need rework org.easymock.EasyMock.verify(workProductService);
        // assertNotNull(status);
    }

    private OrganizationElementDocument buildSampleOrganzationDocument() {

        OrganizationElementDocument orgDoc = OrganizationElementDocument.Factory.newInstance();
        OrganizationElementType element = orgDoc.addNewOrganizationElement();
        element.setOrganizationName(organizationName);
        element.setOrganizationType(organizationType);
        OrganizationPositionType posInCharge = OrganizationPositionType.Factory.newInstance();
        posInCharge.setPersonProfileRef(personInCharge);
        posInCharge.setRoleProfileRef(role1);
        element.setPersonInCharge(posInCharge);

        // set up staff in ICS
        OrganizationPositionType position = OrganizationPositionType.Factory.newInstance();
        position.setPersonProfileRef(roleName1);
        position.setRoleProfileRef(role1);
        OrganizationPositionType[] staffs = new OrganizationPositionType[1];
        staffs[0] = position;
        element.setStaffArray(staffs);

        OrganizationElementType opOrg = element.addNewOrganizationElement();
        opOrg.setOrganizationName("Operation");
        opOrg.setOrganizationType("Section");
        opOrg.addNewPersonInCharge();
        opOrg.getPersonInCharge().setPersonProfileRef("joe.operation.chief@eoc.org");
        opOrg.getPersonInCharge().setRoleProfileRef("Operation Section Chief");
        opOrg.addNewStaff();
        opOrg.getStaffArray(0).setPersonProfileRef("joe.operation.deputy@eoc.org");
        opOrg.getStaffArray(0).setRoleProfileRef("Operation Section Deputy");

        // log.debug("Full Organization:\n" + orgDoc);
        return orgDoc;
    }
}
