package com.leidos.xchangecore.core.em.service.impl;

import gov.ucore.ucore.x20.DigestDocument;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.sensorService.SensorObservationInfoDocument;
import org.uicds.sensorService.SensorObservationInfoDocument.SensorObservationInfo;

import com.leidos.xchangecore.core.em.service.SensorService;
import com.leidos.xchangecore.core.em.util.DigestGenerator;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.base.IdentificationType;

/**
 * The SensorService implementation.
 * 
 * @author Aruna Hau
 * @since 1.0
 * @see com.saic.uicds.core.infrastructure.model.WorkProduct WorkProduct Data Model
 * @ssdd
 */
public class SensorServiceImpl
    implements SensorService, ServiceNamespaces {

    Logger logger = LoggerFactory.getLogger(SensorServiceImpl.class);

    private WorkProductService workProductService;
    private DirectoryService directoryService;
    private DigestGenerator digestGenerator;

    private String xsltFilePath;
    private String iconConfigXmlFilePath;

    /**
     * Creates the soi workProduct and publishes it.
     * 
     * @param soi the soi
     * @param incidentID the incident id
     * 
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus createSOI(SensorObservationInfo soi, String incidentID) {

        //logger.error("createSOI incidentID:\n" + incidentID);
        //logger.error("createSOI SensorObservationInfo:\n" + soi.toString());

        WorkProduct wp = new WorkProduct();
        if (incidentID != null) {
            logger.debug(" incidentID=" + incidentID);
            wp.associateInterestGroup(incidentID);
        }
        wp.setProductType(SensorService.Type);

        SensorObservationInfoDocument soiDoc = SensorObservationInfoDocument.Factory.newInstance();
        soiDoc.addNewSensorObservationInfo().set(soi);

        //logger.error("SensorObservationInfoDocument:\n" + soiDoc.toString());

        // Create the digest using XSLT
        //if (xsltFilePath == null) 
        xsltFilePath = "xslt/SOIDigest.xsl";
        if (iconConfigXmlFilePath == null) {
            iconConfigXmlFilePath = "xml/types_icons.xml";
        }
        digestGenerator = new DigestGenerator(xsltFilePath, iconConfigXmlFilePath);
        DigestDocument digestDoc = digestGenerator.createDigest(soiDoc);

        wp.setProduct(soiDoc);

        wp.setDigest(digestDoc);
        //wp.setDigest(new EMDigestHelper(soi).getDigest());

        //System.out.println("createSOI: wp's product=[" + wp.getProduct() + "]");

        ProductPublicationStatus status = workProductService.publishProduct(wp);

        return status;
    }

    /**
     * Delete soi.
     * 
     * @param productID the product id
     * 
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus deleteSOI(String productID) {

        WorkProduct wp = workProductService.getProduct(productID);

        ProductPublicationStatus status;

        if (wp == null) {
            status = new ProductPublicationStatus();
            status.setStatus(ProductPublicationStatus.FailureStatus);
            status.setReasonForFailure(productID + " doesn't existed");
            return status;
        }

        // if it's still not closed, we need to close it first
        if (wp.isActive() == true) {
            status = getWorkProductService().closeProduct(WorkProductHelper.getWorkProductIdentification(wp));
            if (status.getStatus().equals(ProductPublicationStatus.FailureStatus)) {
                return status;
            }
        }

        return getWorkProductService().archiveProduct(WorkProductHelper.getWorkProductIdentification(wp));
    }

    public DirectoryService getDirectoryService() {

        return directoryService;
    }

    /**
     * @return the getIconConfigXmlFilePath
     */
    public String getIconConfigXmlFilePath() {

        return iconConfigXmlFilePath;
    }

    /**
     * Gets the SOI workProduct by productId.
     * 
     * @param productID the product id
     * 
     * @return the sOI
     * @ssdd
     */
    @Override
    public WorkProduct getSOI(String productID) {

        WorkProduct product = workProductService.getProduct(productID);
        return product;
    }

    /**
     * Gets the SOI list associated with a specific interest group incidentId.
     * 
     * @param incidentID the incident id
     * 
     * @return the sOI list
     * @ssdd
     */
    @Override
    public WorkProduct[] getSOIList(String incidentID) {

        List<WorkProduct> productList = workProductService.listByProductType(SensorService.Type);
        List<WorkProduct> associatedProducts = new ArrayList<WorkProduct>();
        if (productList != null) {
            for (WorkProduct product : productList) {
                if (product.getFirstAssociatedInterestGroupID().equals(incidentID)) {
                    associatedProducts.add(product);
                }
            }
        }
        WorkProduct[] products = new WorkProduct[associatedProducts.size()];
        return associatedProducts.toArray(products);
    }

    public WorkProductService getWorkProductService() {

        return workProductService;
    }

    /**
     * @return the xsltFilePath
     */
    public String getXsltFilePath() {

        return xsltFilePath;
    }

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    /**
     * @param getIconConfigXmlFilePath to set
     */
    public void setIconConfigXmlFilePath(String iconConfigXmlFilePath) {

        this.iconConfigXmlFilePath = iconConfigXmlFilePath;
    }

    public void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    /**
     * @param xsltFilePath the xsltFilePath to set
     */
    public void setXsltFilePath(String xsltFilePath) {

        this.xsltFilePath = xsltFilePath;
    }

    /** {@inheritDoc} */
    @Override
    public void systemInitializedHandler(String messgae) {

        WorkProductTypeListType typeList = WorkProductTypeListType.Factory.newInstance();
        typeList.addProductType(SensorService.Type);
        directoryService.registerUICDSService(NS_SensorService,
            SENSOR_SERVICE_NAME,
            typeList,
            typeList);
    }

    /**
     * Update SOI. Get the productId from the package identifier, retrieve the workProduct and
     * publish an updated SOI
     * 
     * @param soi the soi
     * @param pkgId the pkg id
     * 
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus updateSOI(SensorObservationInfo soi, IdentificationType pkgId) {

        String productID = pkgId.getIdentifier().getStringValue();
        logger.debug("updateSOI: " + soi.toString() + " productID=" + productID);

        WorkProduct wp = workProductService.getProduct(productID);

        SensorObservationInfoDocument soiDoc = SensorObservationInfoDocument.Factory.newInstance();
        soiDoc.addNewSensorObservationInfo().set(soi);
        wp.setProduct(soiDoc);

        System.out.println("updateSOI: wp's product=[" + wp.getProduct() + "]");

        wp = WorkProductHelper.setWorkProductIdentification(wp, pkgId);

        // Create the digest using XSLT
        //if (xsltFilePath == null) 
        xsltFilePath = "xslt/SOIDigest.xsl";
        if (iconConfigXmlFilePath == null) {
            iconConfigXmlFilePath = "xml/types_icons.xml";
        }
        digestGenerator = new DigestGenerator(xsltFilePath, iconConfigXmlFilePath);
        DigestDocument digestDoc = digestGenerator.createDigest(soiDoc);

        wp.setDigest(digestDoc);
        //newWP.setDigest(new EMDigestHelper(soi).getDigest());

        ProductPublicationStatus status = workProductService.publishProduct(wp);

        return status;
    }

}
