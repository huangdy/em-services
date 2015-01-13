/**
 * 
 */
package com.leidos.xchangecore.core.em.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.ucore.ucore.x20.DigestDocument;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.usersmarts.pub.atom.Entry;
import com.usersmarts.pub.atom.Link;
import com.usersmarts.pub.georss.GEORSS;
import com.vividsolutions.jts.geom.Geometry;
import com.leidos.xchangecore.core.em.service.AlertService;
import com.leidos.xchangecore.core.em.service.IncidentManagementService;
import com.leidos.xchangecore.core.em.service.ResourceManagementService;
import com.leidos.xchangecore.core.em.util.WorkProductFeedUtil;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.InfrastructureNamespaces;

/**
 * @author roger
 * 
 */
public class WorkProductFeedUtilTest
    implements InfrastructureNamespaces {

    private static final String ENTITY1_TITLE = "Track Dozer #7";
    private static final String ENTITY2_TITLE = "Track Dozer #48";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testIncidentDigest1() {

        // TODO: Should look at a better way to get digests for testing. The problem is that if the
        // digester changes we need to update these data files.
        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/IncidentDigest.1.xml",
            IncidentManagementService.InterestGroupType);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(1, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertNotNull(entry.get(GEORSS.WHERE));
            assertTrue(entry.getTitle().contains("Flood"));
        }
    }

    @Test
    public void testAlertDigest1() {

        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/AlertDigest.1.xml",
            AlertService.Type);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(1, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertNotNull(entry.get(GEORSS.WHERE));
            assertTrue(entry.getTitle().contains("CAP"));
        }
    }

    @Test
    public void testCommitResourceDigest1() {

        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/CommitResourceDigest.1.xml",
            ResourceManagementService.COMMIT_RESOURCE_PRODUCT_TYPE);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(2, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertNull(entry.get(GEORSS.WHERE));
            assertTrue(entry.getTitle().contains(ENTITY1_TITLE) ||
                       entry.getTitle().contains(ENTITY2_TITLE));
        }
    }

    @Test
    public void testCommitResourceDigest2() {

        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/CommitResourceDigest.2.xml",
            ResourceManagementService.COMMIT_RESOURCE_PRODUCT_TYPE);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(2, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertTrue(entry.getTitle().contains(ENTITY1_TITLE) ||
                       entry.getTitle().contains(ENTITY2_TITLE));
            assertNotNull(entry.get(GEORSS.WHERE));
            assertTrue(entry.get(GEORSS.WHERE) instanceof Geometry);
            Geometry geometry = (Geometry) entry.get(GEORSS.WHERE);
            assertEquals(1, geometry.getNumPoints());
            assertEquals(38.8, geometry.getCoordinate().y, 0.1);
            assertEquals(-77.1, geometry.getCoordinate().x, 0.1);
        }
    }

    @Test
    public void testRequestResourceDigest1() {

        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/RequestResourceDigest.1.xml",
            ResourceManagementService.REQUEST_RESOURCE_PRODUCT_TYPE);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(1, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertNull(entry.get(GEORSS.WHERE));
            assertNotNull(entry.getTitle());
            assertNotNull(entry.getSummary());
            assertTrue(entry.getSummary().contains("4"));
            assertTrue(entry.getSummary().contains("Track"));
        }
    }

    @Test
    public void testRequestResourceDigest2() {

        WorkProduct workProduct = getTestWorkProduct("src/test/resources/digests/RequestResourceDigest.2.xml",
            ResourceManagementService.REQUEST_RESOURCE_PRODUCT_TYPE);
        assertNotNull(workProduct);

        Set<Entry> entries = WorkProductFeedUtil.getFeedEntries(workProduct, "api/workProducts/");
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(1, entries.size());
        for (Entry entry : entries) {
            assertEntryWorkProductValuesCorrect(workProduct, entry);
            assertNotNull(entry.get(GEORSS.WHERE));
            assertTrue(entry.get(GEORSS.WHERE) instanceof Geometry);
            Geometry geometry = (Geometry) entry.get(GEORSS.WHERE);
            assertEquals(1, geometry.getNumPoints());
            assertEquals(38.8, geometry.getCoordinate().y, 0.1);
            assertEquals(-77.1, geometry.getCoordinate().x, 0.1);
            assertNotNull(entry.getTitle());
            assertNotNull(entry.getSummary());
            assertTrue(entry.getSummary().contains("4"));
            assertTrue(entry.getSummary().contains("Track"));
        }
    }

    private void assertEntryWorkProductValuesCorrect(WorkProduct workProduct, Entry entry) {

        assertEquals(workProduct.getProductID(), entry.getId());
        assertEquals(workProduct.getCreatedDate(), entry.getPublished());
        assertEquals(workProduct.getUpdatedDate(), entry.getUpdated());
        List<Link> links = entry.getLinks();
        assertNotNull(links);
        assertEquals(1, links.size());
        assertNotNull(entry.getLink("rel"));
        assertTrue(entry.getLink("rel").getHref().endsWith(workProduct.getProductID()));
    }

    private WorkProduct getTestWorkProduct(String digestFileName, String workProductType) {

        File file = new File(digestFileName);
        assertNotNull(file);
        assertTrue(file.exists());
        DigestDocument digest = null;
        try {
            digest = DigestDocument.Factory.parse(file);
            WorkProduct workProduct = new WorkProduct();
            workProduct.setProductType(workProductType);
            workProduct.setProductID("1");
            workProduct.setCreatedDate(new Date());
            workProduct.setUpdatedDate(new Date());
            workProduct.setDigest(digest);
            return workProduct;

        } catch (XmlException e) {
            fail("XML parsing error " + digestFileName + " " + e.getMessage());
        } catch (IOException e) {
            fail("IO Exception " + digestFileName + " " + e.getMessage());
        }
        return null;
    }
}
