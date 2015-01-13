package com.leidos.xchangecore.core.em.service;

import java.awt.Image;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.usersmarts.util.DOMUtils;
import com.vividsolutions.jts.geom.Envelope;
import com.leidos.xchangecore.core.em.util.MapRenderer;

/**
 * MapRendererTest
 * 
 * @author Patrick Neal - Image Matters, LLC
 * @package com.saic.dctd.uicds.core.service
 * @created Nov 24, 2008
 */
public class MapRendererTest
    extends Assert {

    MapRenderer renderer;

    @Before
    public void setUp() throws Exception {

        renderer = new MapRenderer();
    }

    @Test
    public void render() throws Exception {

        Document document = DOMUtils.parseDocument(new FileInputStream("src/test/resources/maps/vc.xml"));
        Node node = document.getDocumentElement();
        Element stPayload = DOMUtils.getChild(node, "StructuredPayload");
        Element viewContext = DOMUtils.getChild(stPayload, "ViewContext");
        Envelope envelope = new Envelope(-120.0, -66.0, 20.0, 50.0);
        String[] layers = new String[0];
        // {
        // "ATLAS_STATES","ATLAS_STATES_LABELS",
        // "ATLAS_COUNTIES_2001","ATLAS_COUNTIES_2001_LABELS"
        // // "Bathymetry",
        // // "Topography"
        // };

        Image image = renderer.render(viewContext, layers, envelope, 500, 500, "image/png");
        assertNotNull(image);
        // ImageIO.write((BufferedImage)image, "png", new File("target/map.png"));
    }
}
