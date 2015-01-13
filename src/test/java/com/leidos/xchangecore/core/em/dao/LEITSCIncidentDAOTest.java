package com.leidos.xchangecore.core.em.dao;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.em.dao.LEITSCIncidentDAO;
import com.leidos.xchangecore.core.em.model.LEITSCIncident;

public class LEITSCIncidentDAOTest
    extends AbstractJpaTests {

    @Autowired
    LEITSCIncidentDAO leitscIncidentDAO;

    @Test
    public void testLEITSCIncidentDAOCreation() {

        assertNotNull(leitscIncidentDAO);
    }

    @Test
    public void testFindAll() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        List<LEITSCIncident> leitscIncidents = leitscIncidentDAO.findAll();

        assertEquals(2, leitscIncidents.size());

        leitscIncidentDAO.makeTransient(leitscIncidents.get(0));
        leitscIncidents = leitscIncidentDAO.findAll();
        assertEquals(1, leitscIncidents.size());

    }

    @Test
    public void testFindByLEITSCIncident() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        LEITSCIncident result1 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident1");
        assertNotNull(result1);
        assertEquals("Incident1", result1.getIncidentID());

        LEITSCIncident result2 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident2");
        assertNotNull(result2);
        assertEquals("Incident2", result2.getIncidentID());

        leitscIncidentDAO.makeTransient(result1);
        result1 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident1");
        assertNull(result1);

        leitscIncidentDAO.makeTransient(result2);
        result2 = leitscIncidentDAO.findByLEITSCIncident("LEITSCIncident2");
        assertNull(result2);
    }

    @Test
    public void testFindByIncident() {

        LEITSCIncident leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident1");
        leitscIncident.setLeitscIncidentID("LEITSCIncident1");
        leitscIncidentDAO.makePersistent(leitscIncident);

        leitscIncident = new LEITSCIncident();
        leitscIncident.setIncidentID("Incident2");
        leitscIncident.setLeitscIncidentID("LEITSCIncident2");
        leitscIncidentDAO.makePersistent(leitscIncident);

        LEITSCIncident result1 = leitscIncidentDAO.findByIncident("Incident1");
        assertNotNull(result1);
        assertEquals("LEITSCIncident1", result1.getLeitscIncidentID());

        LEITSCIncident result2 = leitscIncidentDAO.findByIncident("Incident2");
        assertNotNull(result2);
        assertEquals("LEITSCIncident2", result2.getLeitscIncidentID());

        leitscIncidentDAO.makeTransient(result1);
        result1 = leitscIncidentDAO.findByIncident("Incident1");
        assertNull(result1);

        leitscIncidentDAO.makeTransient(result2);
        result2 = leitscIncidentDAO.findByIncident("Incident2");
        assertNull(result2);

    }

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/applicationContext-em.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml"
        };
    }
}
