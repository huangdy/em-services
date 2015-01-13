package com.leidos.xchangecore.core.em.dao;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.organizationElement.OrganizationElementDocument;
import org.uicds.organizationElement.OrganizationElementType;
import org.uicds.organizationElement.OrganizationNameType;
import org.uicds.organizationElement.OrganizationTypeType;

import com.leidos.xchangecore.core.em.dao.IncidentCommandDAO;
import com.leidos.xchangecore.core.em.model.IncidentCommandStructure;
import com.leidos.xchangecore.core.em.model.OrganizationElement;
import com.leidos.xchangecore.core.em.model.OrganizationPositionType;

public class IncidendCommandDAOTest
    extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(IncidendCommandDAOTest.class);

    @Autowired
    IncidentCommandDAO incidentCommandDAO;

    @Test
    public void testFindById() {

        String icsID;
        IncidentCommandStructure ics = new IncidentCommandStructure();

        ics = incidentCommandDAO.makePersistent(ics);

        IncidentCommandStructure result = incidentCommandDAO.findById(ics.getId());

        assertNotNull(result);
        assertEquals(ics, result);
        assertNotNull(ics.getId());
        icsID = ics.getId();
        log.debug("ID: " + ics.getId());

        result.addIncident("12345");
        result.setOrganization(buildSampleOrganzationElement());

        result = incidentCommandDAO.makePersistent(result);
        assertEquals(icsID, result.getId());
        log.debug("ID2: " + result.getId());

        IncidentCommandStructure result2 = incidentCommandDAO.findById(ics.getId());

        assertNotNull(result2);
        assertEquals(ics, result2);
        assertNotNull(result.getId());
        log.debug("IDResult2: " + result2.getId());
    }

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/applicationContext-em.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml"
        };
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {

        // Incident incident = new Incident();
        // incident.setIncidentId(incidentId);
        // incidentDAO.makePersistent(incident);
    }

    private OrganizationElement buildSampleOrganzationElement() {

        OrganizationElement element = new OrganizationElement();
        element.setOrganizationName("INCIDENT_COMMAND");
        element.setOrganizationType("INCIDENT_COMMAND");
        OrganizationPositionType inCharge = new OrganizationPositionType("inChargePerson",
                                                                         "Incident_Command");
        element.setPersonInCharge(inCharge);
        // set up staff in ICS
        OrganizationPositionType position = new OrganizationPositionType("roleName1", "role1");
        Set<OrganizationPositionType> staffs = new HashSet<OrganizationPositionType>();
        staffs.add(position);
        element.setStaffs(staffs);
        // set up sub organization within ICS
        OrganizationElement subElement = new OrganizationElement();
        subElement.setOrganizationName("subOrgName");
        subElement.setOrganizationType("subOrgType");
        OrganizationPositionType subCharge = new OrganizationPositionType("inChargeSub",
                                                                          "Sub_Incident_Command");
        subElement.setPersonInCharge(subCharge);
        OrganizationPositionType subPosition = new OrganizationPositionType("subStaff1",
                                                                            "subStaff1");
        Set<OrganizationPositionType> subStaffs = new HashSet<OrganizationPositionType>();
        subStaffs.add(subPosition);
        subElement.setStaffs(subStaffs);

        return element;
    }

    private OrganizationElementDocument buildSampleOrganzationDocument() {

        OrganizationElementDocument orgDoc = OrganizationElementDocument.Factory.newInstance();
        OrganizationElementType element = orgDoc.addNewOrganizationElement();
        element.setOrganizationName(OrganizationNameType.INCIDENT_COMMAND.toString());
        element.setOrganizationType(OrganizationTypeType.OFFICE.toString());
        org.uicds.organizationElement.OrganizationPositionType posInCharge = orgDoc.getOrganizationElement().addNewPersonInCharge();
        posInCharge.setPersonProfileRef("personINCharge");
        posInCharge.setRoleProfileRef("role1");

        // set up staff in ICS
        org.uicds.organizationElement.OrganizationPositionType position = orgDoc.getOrganizationElement().addNewStaff();
        position.setPersonProfileRef("roleName1");
        position.setRoleProfileRef("role1");
        OrganizationPositionType[] staffs = new OrganizationPositionType[1];

        return orgDoc;
    }
}
