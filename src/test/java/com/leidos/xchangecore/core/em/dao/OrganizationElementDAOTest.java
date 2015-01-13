package com.leidos.xchangecore.core.em.dao;

import org.junit.Test;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.em.model.OrganizationElement;

public class OrganizationElementDAOTest
    extends AbstractJpaTests {

    @SuppressWarnings("unused")
    private String incidentId = "I-0123456789";

    // @Autowired
    // OrganizationElementDAO organizationDAO;

    @Test
    public void testFindById() {

        /**
         * OrganizationElement org2 = new OrganizationElement();
         * 
         * org2 = organizationDAO.makePersistent(org2); log.debug("ID2: " + org2.getId());
         * OrganizationElement result2 = organizationDAO.findById(org2.getId());
         * 
         * assertNotNull(result2); assertEquals(org2, result2);
         * 
         * OrganizationElement org = buildSampleOrganzationElement();
         * 
         * org = organizationDAO.makePersistent(org); log.debug("ID: " + org.getId());
         * OrganizationElement result = organizationDAO.findById(org.getId());
         * 
         * assertNotNull(result); assertEquals(org, result);
         **/
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

        OrganizationElement rootOrg = new OrganizationElement();
        /**
         * rootOrg.setOrganizationName("Incident Command");
         * rootOrg.setOrganizationType("Incident Command"); OrganizationPositionType rootPIC = new
         * OrganizationPositionType(); rootPIC.setPersonProfileRef("Incident Commander");
         * rootPIC.setRoleProfileRef("joe.smith@eoc.org"); rootOrg.setPersonInCharge(rootPIC);
         * 
         * // set up staff in ICS OrganizationPositionType position = new
         * OrganizationPositionType(); position.setPersonProfileRef("joe.security@eoc.org");
         * position.setRoleProfileRef("Security Officer"); Set<OrganizationPositionType> staffs =
         * new HashSet<OrganizationPositionType>(); staffs.add(position); rootOrg.setStaffs(staffs);
         * // set up sub organization within ICS OrganizationElement opOrg = new
         * OrganizationElement(); opOrg.setOrganizationName("Operation");
         * opOrg.setOrganizationType("Section"); OrganizationPositionType opPIC = new
         * OrganizationPositionType(); opPIC.setPersonProfileRef("joe.operator@oec.org");
         * opPIC.setRoleProfileRef("Operation Section Chief"); opOrg.setPersonInCharge(opPIC);
         * OrganizationPositionType opStaff = new OrganizationPositionType();
         * opStaff.setPersonProfileRef("joe.deputy@eoc.org");
         * opStaff.setRoleProfileRef("Opeartion Section Deputy"); Set<OrganizationPositionType>
         * subStaffs = new HashSet<OrganizationPositionType>(); subStaffs.add(opStaff);
         * opOrg.setStaffs(subStaffs);
         * 
         * OrganizationElement[] subOrganizations = new OrganizationElement[1]; subOrganizations[0]
         * = opOrg; // rootOrg.setOrganizations(subOrganizations);
         * 
         * organizationDAO.makePersistent(rootOrg);
         * 
         * OrganizationElement org = organizationDAO.findById(rootOrg.getId());
         **/
        return rootOrg;

    }
}
