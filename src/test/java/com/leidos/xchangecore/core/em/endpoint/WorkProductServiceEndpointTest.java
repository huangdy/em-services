package com.leidos.xchangecore.core.em.endpoint;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.uicds.incident.IncidentDocument;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

public class WorkProductServiceEndpointTest
    extends Assert {

    @Test
    public void getWorkProductDocument() {

        Set<String> interestGroupSet = new HashSet<String>();
        interestGroupSet.add(UUID.randomUUID().toString());
        interestGroupSet.add(UUID.randomUUID().toString());

        WorkProduct wp = new WorkProduct();
        IncidentDocument incidentDoc = IncidentDocument.Factory.newInstance();
        incidentDoc.addNewIncident().addNewActivityName().setStringValue("Incident Name");
        wp.setProduct(incidentDoc);
        wp.setProductID("Incident-0123456789");
        wp.setProductVersion(new Integer(1));
        wp.setSize(new Integer(wp.getProduct().toString().length()));
        wp.setProductType("Incident Document");
        wp.setChecksum("CheckSum-0123456789");
        wp.setCreatedDate(new Date());
        wp.setCreatedBy("User One");
        wp.setUpdatedDate(new Date());
        wp.setUpdatedBy("User Two");
        wp.setMimeType("text/xml");
        wp.setAssociatedInterestGroupIDs(interestGroupSet);

        WorkProductDocument wpDocument = WorkProductHelper.toWorkProductDocument(wp);
        ValidationUtil.validate(wpDocument, true);

        WorkProductDocument summaryDocument = WorkProductDocument.Factory.newInstance();
        summaryDocument.addNewWorkProduct().set(WorkProductHelper.toWorkProduct(wp));
        ValidationUtil.validate(summaryDocument, true);

        WorkProduct wpModel = WorkProductHelper.toModel(wpDocument.getWorkProduct());
        System.out.println("convert from WorkProductDocument back to Work Product Model .....");

        try {
            WorkProductDocument wpd = WorkProductDocument.Factory.parse(new File("../testing/data/WorkProductDocument.xml"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
