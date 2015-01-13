package com.leidos.xchangecore.core.em.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import x1.oasisNamesTcEmergencyCap1.AlertDocument;
import x1.oasisNamesTcEmergencyCap1.AlertDocument.Alert;

import com.leidos.xchangecore.core.em.service.AlertService;
import com.leidos.xchangecore.core.em.service.impl.AlertServiceImpl;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;
import com.saic.precis.x2009.x06.base.IdentificationType;

public class AlertServiceTest {

    // extends AbstractDependencyInjectionSpringContextTests {

    AlertServiceImpl alertService = new AlertServiceImpl();

    WorkProductService workProductService;

    private final static String incidentID = "";
    private final static String workProductID = "11";

    // @Override
    @Before
    public void setUp() {

        // Create a mock for the work product service
        //
        workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
        alertService.setWorkProductService(workProductService);

        // Create a mock for closeProduct
        // getWorkProductService().closeProduct

        // create a mock for archiveProduct
        // getWorkProductService().archiveProduct

        // mock getWorkProductService().getProduct(workProductId)

    }

    private void setupForTestAlertCancel() {

        // org.easymock.EasyMock.expect(
        // workProductService.getProductIdentification(org.easymock.EasyMock.isA(String.class))).andReturn(
        // id);

    }

    @Test
    public void testAlertCancelSuccess() throws Exception {

        resetMocks();
        // Create a list of work products that contains one work product
        // that is an alert work product with the identifier
        // in the references section to cancel

        // create list of alerts and store in list
        // use data from alerts.xml file
        // xml file contains generic alert, change the ID's to 1,2,3,4,5
        // alert to cancel will be AlertIdentifier3 - that is stored in another xml file

        WorkProduct wp = new WorkProduct();
        ArrayList<WorkProduct> wpList = new ArrayList<WorkProduct>();
        wp = createWorkProduct("AlertIdentifier1");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier2");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier3");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier4");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier5");
        wpList.add(wp);

        // do "expect mock work product service" and return that list created above
        // workProductService.listByProductType(AlertService.Type);
        org.easymock.EasyMock.expect(workProductService.listByProductType(AlertService.Type)).andReturn(wpList);

        // org.easymock.EasyMock.expect(
        // workProductService.getProduct("AlertIdentfier3")).andReturn(
        // PublicationStatus.SuccessStatus);
        // mock the getProduct
        WorkProduct wpReturn = new WorkProduct();
        wpReturn = createWorkProduct("AlertIdentifier3");

        // WorkProduct wp = getWorkProductService().getProduct(workProductId);
        // mock WorkProduct wp = getWorkProductService().getProduct(workProductId);
        // We have found the workProduct in our list so we mock getting the actual work product from
        // the core
        String workProductId = "";
        workProductId = wpReturn.getProductID();
        org.easymock.EasyMock.expect(workProductService.getProduct(workProductId)).andReturn(wpReturn);

        ProductPublicationStatus status_Success = new ProductPublicationStatus();
        status_Success.setStatus(ProductPublicationStatus.SuccessStatus);

        // mock the closeProduct method
        org.easymock.EasyMock.expect(workProductService.closeProduct(EasyMock.isA(IdentificationType.class))).andReturn(status_Success);

        // mock the closeProduct method
        org.easymock.EasyMock.expect(workProductService.archiveProduct(EasyMock.isA(IdentificationType.class))).andReturn(status_Success);

        // mock replay
        org.easymock.EasyMock.replay(workProductService);

        // create the alert to cancel
        AlertDocument alertDocumentCancel = getAlertDocumentToCancelFromFile();
        Alert alert = alertDocumentCancel.getAlert();
        assertNotNull(alert);

        // call alert service "create alert" with above alert to cancel
        ProductPublicationStatus status = alertService.createAlert(incidentID, alert);

        assertTrue(ProductPublicationStatus.SuccessStatus == status.getStatus());

    }

    @Test
    public void testAlertCancelNotFound() throws Exception {

        resetMocks();
        // Create a list of work products that contains one work product
        // that is an alert work product with the identifier
        // in the references section to cancel

        // create list of alerts and store in list
        // use data from alerts.xml file
        // xml file contains generic alert, change the ID's to 1,2,3,4,5
        // alert to cancel will be AlertIdentifier3 - that is stored in another xml file

        WorkProduct wp = new WorkProduct();
        ArrayList<WorkProduct> wpList = new ArrayList<WorkProduct>();
        wp = createWorkProduct("AlertIdentifier1");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier2");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier4");
        wpList.add(wp);
        wp = createWorkProduct("AlertIdentifier5");
        wpList.add(wp);

        // do "expect mock work product service" and return that list created above
        // workProductService.listByProductType(AlertService.Type);
        org.easymock.EasyMock.expect(workProductService.listByProductType(AlertService.Type)).andReturn(wpList);

        // org.easymock.EasyMock.expect(
        // workProductService.getProduct("AlertIdentfier3")).andReturn(
        // PublicationStatus.SuccessStatus);
        // mock the getProduct
        WorkProduct wpReturn = new WorkProduct();
        wpReturn = createWorkProduct("AlertIdentifier3");

        // WorkProduct wp = getWorkProductService().getProduct(workProductId);
        // mock WorkProduct wp = getWorkProductService().getProduct(workProductId);
        // We have found the workProduct in our list so we mock getting the actual work product from
        // the core
        String workProductId = "";
        workProductId = wpReturn.getProductID();
        org.easymock.EasyMock.expect(workProductService.getProduct(workProductId)).andReturn(wpReturn);

        ProductPublicationStatus status_Success = new ProductPublicationStatus();
        status_Success.setStatus(ProductPublicationStatus.SuccessStatus);

        // mock the closeProduct method
        org.easymock.EasyMock.expect(workProductService.closeProduct(EasyMock.isA(IdentificationType.class))).andReturn(status_Success);

        // mock the closeProduct method
        org.easymock.EasyMock.expect(workProductService.archiveProduct(EasyMock.isA(IdentificationType.class))).andReturn(status_Success);

        // mock replay
        org.easymock.EasyMock.replay(workProductService);

        // create the alert to cancel
        AlertDocument alertDocumentCancel = getAlertDocumentToCancelFromFile();
        Alert alert = alertDocumentCancel.getAlert();
        assertNotNull(alert);

        // call alert service "create alert" with above alert to cancel
        ProductPublicationStatus status = alertService.createAlert(incidentID, alert);

        assertTrue(status.getStatus() == null);

    }

    @Test
    public void testAlertDigest() throws Exception {

        AlertDocument alertDocument = AlertDocument.Factory.parse(new File("src/test/resources/alerts/Alert.xml"));
        assertNotNull(alertDocument);
        // System.out.println("Digest:\n" + new DigestHelper(alertDocument.getAlert()));
    }

    @Test
    public void testAlertCreate() throws Exception {

        AlertDocument alertDocument = AlertDocument.Factory.parse(new File("src/test/resources/alerts/Alert.xml"));
        assertNotNull(alertDocument);
        // System.out.println("Digest:\n" + new DigestHelper(alertDocument.getAlert()));
    }

    private void resetMocks() {

        org.easymock.EasyMock.reset(workProductService);
    }

    private AlertDocument getAlertDocumentFromFile() throws XmlException, IOException {

        AlertDocument alertDocument = AlertDocument.Factory.parse(new File("src/test/resources/alerts/Alert.xml"));

        assertTrue(ValidationUtil.validate(alertDocument, true));
        return alertDocument;
    }

    private AlertDocument getAlertDocumentToCancelFromFile() throws XmlException, IOException {

        AlertDocument alertDocument = AlertDocument.Factory.parse(new File("src/test/resources/alerts/AlertToCancel.xml"));

        assertTrue(ValidationUtil.validate(alertDocument, true));
        return alertDocument;
    }

    private WorkProduct createWorkProduct(String AlertIdentifier) throws Exception {

        WorkProduct wp = new WorkProduct();
        // get alert from file;
        AlertDocument alertDocument = getAlertDocumentFromFile();
        Alert alert = alertDocument.getAlert();
        // change the alert id
        alert.setIdentifier(AlertIdentifier);
        wp.setProductType(AlertService.Type);
        wp.setProduct(alertDocument);
        return wp;
    }

}
