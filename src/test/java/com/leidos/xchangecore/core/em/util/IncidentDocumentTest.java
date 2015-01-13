package com.leidos.xchangecore.core.em.util;

import gov.niem.niem.niemCore.x20.ContactEmailIDDocument;
import gov.niem.niem.niemCore.x20.ContactTelephoneNumberDocument;
import gov.niem.niem.niemCore.x20.NANPTelephoneNumberDocument;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.uicds.incident.IncidentDocument;

import com.leidos.xchangecore.core.em.util.EMDigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.InfrastructureNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;

public class IncidentDocumentTest
    implements InfrastructureNamespaces {

    @Test
    public void testWithJurisdictionOrganization() throws Exception {

        IncidentDocument incident = getIncidentDocumentFromFile();
        ValidationUtil.validate(incident, true);

        incident.getIncident().getIncidentJurisdictionalOrganizationArray(0).addNewOrganizationName().setStringValue("Organization One");

        ContactEmailIDDocument email = ContactEmailIDDocument.Factory.newInstance();
        email.addNewContactEmailID().setStringValue("daniel.d.huang@saic.com");
        /*
        XmlUtil.substitute(
            incident.getIncident().getIncidentJurisdictionalOrganizationArray(0).addNewOrganizationPrimaryContactInformation().addNewContactMeans(),
            NS_NIEM_CORE, "AnyType", ContactEmailIDDocument.type, email);
        incident.getIncident().getIncidentJurisdictionalOrganizationArray(0).addNewOrganizationPrimaryContactInformation().addNewContactMeans().set(
            email);
            */
        incident.getIncident().getIncidentJurisdictionalOrganizationArray(0).addNewOrganizationPrimaryContactInformation().set(email);
        ValidationUtil.validate(incident, true);

        ContactTelephoneNumberDocument telno = ContactTelephoneNumberDocument.Factory.newInstance();
        NANPTelephoneNumberDocument number = NANPTelephoneNumberDocument.Factory.newInstance();
        number.addNewNANPTelephoneNumber().addNewTelephoneAreaCodeID().setStringValue("703");
        number.getNANPTelephoneNumber().addNewTelephoneExchangeID().setStringValue("907");
        number.getNANPTelephoneNumber().addNewTelephoneLineID().setStringValue("2512");
        telno.addNewContactTelephoneNumber().set(number);
        incident.getIncident().getIncidentJurisdictionalOrganizationArray(0).addNewOrganizationPrimaryContactInformation().set(telno);
        ValidationUtil.validate(incident, true);

        EMDigestHelper digestHelper = new EMDigestHelper(incident.getIncident());
        ValidationUtil.validate(digestHelper.getDigest(), true);

        String digestText = digestHelper.getDigest().toString();
    }

    private IncidentDocument getIncidentDocumentFromFile() throws XmlException, IOException {

        IncidentDocument incidentDocument = IncidentDocument.Factory.parse(new File("src/test/resources/incidents/Incident.xml"));
        return incidentDocument;
    }
}
