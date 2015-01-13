/**
 * 
 */
package com.leidos.xchangecore.core.em.dao;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate;
import com.leidos.xchangecore.core.em.model.Incident;

/**
 * @author roger
 * 
 */
public class IncidentDAOTest
    extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(IncidentDAOTest.class);

    @Autowired
    IncidentDAOHibernate incidentDAO;

    private final static String INCIDENT1_INCIDENT_ID = "incident1";
    private final static Double INCIDENT1_LATITUDE = 1.1;
    private final static Double INCIDENT1_LONGITUDE = 1.2;
    private final static String INCIDENT1_SUMMARY = "incident1 summary";
    private final static String INCIDENT1_WPID = "wp-incident1";

    private final static String INCIDENT2_INCIDENT_ID = "incident2";
    private final static Double INCIDENT2_LATITUDE = 2.1;
    private final static Double INCIDENT2_LONGITUDE = 2.2;
    private final static String INCIDENT2_SUMMARY = "incident2 summary";
    private final static String INCIDENT2_WPID = "wp-incident2";

    private final static String BIG_DESC = "Incident used in CreateIncident Test Media outlets are reporting that a person was shot and killed at the East Point MARTA station on E. Main St. and Cambridge Ave. DC Fire EMS is reporting a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE. Reports indicate that police have taken three suspects into custody following a shooting in the area of 2nd Ave and E 99th St. There were unconfirmed reports that a man was holding a woman hostage in the area of S 72nd Ave and Grover St. DC Fire EMS reported a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE. MARTA is reporting that the East Point station on E. Main St. and Cambridge Ave has reopened and trains service has resumed.  DC Fire EMS reported a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE.";

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

    }

    /**
     * Test method for
     * {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#delete(java.lang.String, boolean)}.
     * that makes the instance transient.
     */
    @Test
    public void testDeleteMakeTransient() {

        assertNotNull("DAO object is null", incidentDAO);

        Incident incident1 = addIncident1();
        assertNotNull("returned incident is null", incident1);
        assertTrue("incident is not active", incidentDAO.isActive(INCIDENT1_INCIDENT_ID));
        checkIncident(incident1, 1, true);

        checkNumberOfActiveIncidents(1);

        incidentDAO.delete(INCIDENT1_INCIDENT_ID, true);

        checkNumberOfActiveIncidents(0);
    }

    /**
     * Test method for
     * {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#delete(java.lang.String, boolean)}.
     * that makes the instance inactive.
     */
    @Test
    public void testDeleteMakeInactive() {

        Incident incident1 = addIncident1();
        assertNotNull("returned incident is null", incident1);

        checkNumberOfActiveIncidents(1);

        incidentDAO.delete(INCIDENT1_INCIDENT_ID, false);

        checkNumberOfActiveIncidents(0);

        assertFalse("incident is active", incidentDAO.isActive(INCIDENT1_INCIDENT_ID));
    }

    /**
     * Test method for {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#findAll()}.
     */
    @Test
    public void testFindAll() {

        Incident incident1 = addIncident1();
        assertNotNull("incident1 is null", incident1);

        checkNumberOfActiveIncidents(1);

        Incident incident2 = addIncident2();
        assertNotNull("incident2 failed", incident2);
        checkIncident(incident2, 2, true);

        checkNumberOfActiveIncidents(2);

    }

    /**
     * Test method for
     * {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#findAllClosedIncident()}.
     */
    @Test
    public void testFindAllClosedIncident() {

        Incident incident1 = addIncident1();
        assertNotNull("incident1 is null", incident1);
        Incident incident2 = addIncident2();
        assertNotNull("incident2 is null", incident2);

        List<Incident> list = incidentDAO.findAll();
        checkNumberOfActiveIncidents(2);

        incidentDAO.delete(INCIDENT1_INCIDENT_ID, false);

        checkNumberOfInactiveIncidents(1);
    }

    /**
     * Test method for
     * {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#findAllClosedIncident()}.
     */
    @Test
    public void testFindAllClosedIncident4Incidents() {

        Incident incident1 = addIncident1();
        assertNotNull("incident1 is null", incident1);
        Incident incident2 = addIncident2();
        assertNotNull("incident2 is null", incident2);
        Incident incident3 = addIncident3();
        assertNotNull("incident3 is null", incident3);
        Incident incident4 = addIncident4();
        assertNotNull("incident4 is null", incident4);
        checkNumberOfActiveIncidents(4);

        incidentDAO.delete(incident3.getIncidentId(), false);

        checkNumberOfInactiveIncidents(1);
        checkNumberOfActiveIncidents(3);
    }

    @Test
    public void testLargeSummary() {

        // char[] b = new char[2048];
        // Arrays.fill(b, 'c');

        Incident incident = new Incident();
        incident.setActive(true);
        incident.setIncidentId("3");
        incident.setLatitude(3.1);
        incident.setLongitude(3.2);
        incident.setSummary(BIG_DESC);
        incident.setWorkProductID("3wpid");
        incident = incidentDAO.makePersistent(incident);

        assertEquals("wrong summary", BIG_DESC, incident.getSummary());
    }

    /**
     * Test method for
     * {@link com.leidos.xchangecore.core.em.dao.hb.IncidentDAOHibernate#findByIncidentID(java.lang.String)}
     * .
     */
    @Test
    public void testFindByIncidentID() {

        Incident incident1 = addIncident1();
        assertNotNull("incident1 is null", incident1);
        Incident incident2 = addIncident2();
        assertNotNull("incident2 is null", incident2);
        Incident incident3 = addIncident3();
        assertNotNull("incident3 is null", incident3);
        Incident incident4 = addIncident4();
        assertNotNull("incident4 is null", incident4);
        checkNumberOfActiveIncidents(4);

        Incident incident = incidentDAO.findByIncidentID(INCIDENT1_INCIDENT_ID);
        assertNotNull("incident is null", incident);
        checkIncident(incident, 1, true);

        incident = incidentDAO.findByIncidentID(INCIDENT2_INCIDENT_ID);
        assertNotNull("incident is null", incident);
        checkIncident(incident, 2, true);
    }

    private void checkNumberOfActiveIncidents(int size) {

        List<Incident> list = incidentDAO.findAll();
        List<Incident> closedList = incidentDAO.findAllClosedIncident();
        assertEquals("wrong number of incidents", size, list.size() - closedList.size());
    }

    private Incident addIncident1() {

        Incident incident = new Incident();
        incident.setActive(true);
        incident.setIncidentId(INCIDENT1_INCIDENT_ID);
        incident.setLatitude(INCIDENT1_LATITUDE);
        incident.setLongitude(INCIDENT1_LONGITUDE);
        incident.setSummary(INCIDENT1_SUMMARY);
        incident.setWorkProductID(INCIDENT1_WPID);
        incident = incidentDAO.makePersistent(incident);
        return incident;
    }

    private Incident addIncident2() {

        Incident incident = new Incident();
        incident.setActive(true);
        incident.setIncidentId(INCIDENT2_INCIDENT_ID);
        incident.setLatitude(INCIDENT2_LATITUDE);
        incident.setLongitude(INCIDENT2_LONGITUDE);
        incident.setSummary(INCIDENT2_SUMMARY);
        incident.setWorkProductID(INCIDENT2_WPID);
        incident = incidentDAO.makePersistent(incident);
        return incident;
    }

    private Incident addIncident3() {

        Incident incident = new Incident();
        incident.setActive(true);
        incident.setIncidentId("3");
        incident.setLatitude(3.1);
        incident.setLongitude(3.2);
        incident.setSummary("3 summary");
        incident.setWorkProductID("3wpid");
        incidentDAO.makePersistent(incident);
        return incident;
    }

    private Incident addIncident4() {

        Incident incident = new Incident();
        incident.setActive(true);
        incident.setIncidentId("4");
        incident.setLatitude(4.1);
        incident.setLongitude(4.2);
        incident.setSummary("4 summary");
        incident.setWorkProductID("4wpid");
        incidentDAO.makePersistent(incident);
        return incident;
    }

    private void checkNumberOfInactiveIncidents(int size) {

        List<Incident> list;
        list = incidentDAO.findAllClosedIncident();
        assertEquals("wrong number of incidents", size, list.size());
    }

    private boolean checkIncident(Incident incident, int number, boolean active) {

        if (number == 1) {
            assertEquals("not active", active, incident.isActive());
            assertEquals("wrong incident id", INCIDENT1_INCIDENT_ID, incident.getIncidentId());
            assertEquals("wrong latitude", INCIDENT1_LATITUDE, incident.getLatitude());
            assertEquals("wrong longitude", INCIDENT1_LONGITUDE, incident.getLongitude());
            assertEquals("wrong summary", INCIDENT1_SUMMARY, incident.getSummary());
            assertEquals("wrong wpid", INCIDENT1_WPID, incident.getWorkProductID());
            return true;
        } else if (number == 2) {
            assertEquals("not active", active, incident.isActive());
            assertEquals("wrong incident id", INCIDENT2_INCIDENT_ID, incident.getIncidentId());
            assertEquals("wrong latitude", INCIDENT2_LATITUDE, incident.getLatitude());
            assertEquals("wrong longitude", INCIDENT2_LONGITUDE, incident.getLongitude());
            assertEquals("wrong summary", INCIDENT2_SUMMARY, incident.getSummary());
            assertEquals("wrong wpid", INCIDENT2_WPID, incident.getWorkProductID());
            return true;
        }
        return false;
    }
}
