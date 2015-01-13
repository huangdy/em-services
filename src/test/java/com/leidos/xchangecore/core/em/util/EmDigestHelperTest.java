package com.leidos.xchangecore.core.em.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.ucore.ucore.x20.DigestDocument;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import com.leidos.xchangecore.core.em.util.DigestTester;
import com.leidos.xchangecore.core.em.util.EMDigestHelper;

import x1.oasisNamesTcEmergencyCap1.AlertDocument;

public class EmDigestHelperTest {

    EMDigestHelper helper;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testAlert() throws XmlException, IOException {

        AlertDocument alertDoc = getAlertDocumentFromFile("src/test/resources/alerts/Alert.xml");
        assertNotNull("null alert", alertDoc);

        helper = new EMDigestHelper(alertDoc.getAlert());
        assertNotNull("helper not constructed", helper);

        DigestDocument digest = helper.getDigest();

        // Make sure polygon is not found
        boolean foundPolygon = DigestTester.hasALocationElement(digest.getDigest());
        assertTrue("did not find a polygon", foundPolygon);

        String[] pointArray = {
            "38.8,-77.3", "38.8,-77.1", "39.0,-77.1", "39.0,-77.3", "38.8,-77.3"
        };
        List<String> points = Arrays.asList(pointArray);
        assertTrue("wrong polygon",
            DigestTester.checkLocationGMLPolygon(digest.getDigest(), points));
    }

    @Test
    public void testTrac586() throws XmlException, IOException {

        AlertDocument alertDoc = getAlertDocumentFromFile("src/test/resources/alerts/NPE_alert.xml");
        assertNotNull("null alert", alertDoc);

        helper = new EMDigestHelper(alertDoc.getAlert());
        assertNotNull("helper not constructed", helper);

        DigestDocument digest = helper.getDigest();

        // Make sure polygon is not found
        boolean foundPolygon = DigestTester.hasALocationElement(digest.getDigest());
        assertFalse("found a polygon", foundPolygon);
    }

    @Test
    public void testTrac587() throws XmlException, IOException {

        AlertDocument alertDoc = getAlertDocumentFromFile("src/test/resources/alerts/IOB_alert.xml");
        assertNotNull("null alert", alertDoc);

        helper = new EMDigestHelper(alertDoc.getAlert());
        assertNotNull("helper not constructed", helper);

        DigestDocument digest = helper.getDigest();

        // Make sure polygon is found
        boolean foundPolygon = DigestTester.hasALocationElement(digest.getDigest());
        assertTrue("did not find a polygon", foundPolygon);

        String[] pointArray = {
            "38.83,-77.08", "38.79,-77.06", "38.79,-77.04", "38.80,-77.03", "38.83,-77.08"
        };
        List<String> points = Arrays.asList(pointArray);
        assertTrue("wrong polygon",
            DigestTester.checkLocationGMLPolygon(digest.getDigest(), points));
    }

    private AlertDocument getAlertDocumentFromFile(String fileName) throws XmlException,
        IOException {

        // trac 586 attachment
        AlertDocument alert = AlertDocument.Factory.parse(new File(fileName));
        return alert;
    }

}
