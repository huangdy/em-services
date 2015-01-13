package com.leidos.xchangecore.core.em.endpoint;

import gov.ucore.ucore.x20.DataItemStatusType;
import gov.ucore.ucore.x20.DisseminationCriteriaDocument.DisseminationCriteria;

import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mil.dod.metadata.mdr.ns.ddms.x20.PointOfContactType;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uicds.mapService.UpdateMapResponseDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import x0.messageStructure1.DomainAttributeType;
import x0.messageStructure1.StructuredPayloadType;

import com.usersmarts.util.DOMUtils;
import com.leidos.xchangecore.core.em.endpoint.MapServiceEndpoint;
import com.leidos.xchangecore.core.em.service.MapService;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ValidationUtil;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.base.PropertiesType;
import com.saic.precis.x2009.x06.base.StateType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * MapServiceEndpointTest
 * 
 * @author Patrick Neal - Image Matters, LLC
 * @package com.saic.dctd.uicds.core.endpoint
 * @created Feb 18, 2009
 */
public class MapServiceEndpointTest
    extends Assert {

    MapServiceEndpoint endpoint = new MapServiceEndpoint();
    MapService service;
    String path = "src/test/resources/maps/";

    private static final String PRECISS_NS = "http://www.saic.com/precis/2009/06/structures";
    private static final String PRECISB_NS = "http://www.saic.com/precis/2009/06/base";
    private static final String INCIDENT_ID = "incidentId";
    private static final String MAP_ID = "mapId";
    private static final Integer WP_VERSION = 1;
    private static final String LAYER_ID = "layerId";
    private static final String PKG_ID = "WorkProductIdentification";

    @Before
    public void setUp() throws Exception {

        service = EasyMock.createMock(MapService.class);
        endpoint.setMapService(service);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void create() throws Exception {

        assertNotNull(service);
        assertNotNull(endpoint);
    }

    @Test
    public void submitMap() throws Exception {

        Document request = getRequestDocument(path + "request-submitmap.xml", "SubmitMapRequest");
        Node submitContent = request.getDocumentElement().getElementsByTagNameNS("http://www.opengis.net/context",
            "ViewContext").item(0);

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        status.setProduct(product);
        // System.out.println(product.getProductVersion());

        EasyMock.expect(service.createMap(INCIDENT_ID, submitContent)).andReturn(status);
        EasyMock.replay(service);
        Element response = endpoint.createMap(request.getDocumentElement(), request);
        assertNotNull(response);

        // SubmitMapResponseDocument resDoc = SubmitMapResponseDocument.Factory.parse(response);
        // assertTrue(ValidationUtil.validate(resDoc, true));
    }

    @Test
    public void getMap() throws Exception {

        // Create return value for getMap
        Document workProductDoc = getWorkProductDocument(INCIDENT_ID, MAP_ID);
        Node node = DOMUtils.getChild(workProductDoc, "WorkProduct");
        // System.out.println(DOMUtils.toString(node));

        // Get the request example
        Document request = getRequestDocument(path + "request-getmap.xml", "GetMapRequest");
        assertNotNull("request-getmap.xml parse error", request);

        // Get the WorkProductIdentification that will be used in the getMap call
        Node requestNode = DOMUtils.getChild(request, "GetMapRequest");
        Node packageIdNode = DOMUtils.getChild(requestNode, new QName(PRECISS_NS, PKG_ID));
        assertNotNull(packageIdNode);

        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        product.setProduct(XmlObject.Factory.parse("<stuff>stuff</stuff>"));

        EasyMock.expect(service.getMapWP(packageIdNode)).andReturn(product);
        EasyMock.replay(service);

        Element response = endpoint.getMap(request.getDocumentElement(), request);
        assertNotNull(response);
        assertEquals(MAP_ID, getWorkProductId(response));

        // GetMapResponseDocument resDoc = GetMapResponseDocument.Factory.parse(response);
        // assertTrue(ValidationUtil.validate(resDoc, true));
    }

    @Test
    public void getMaps() throws Exception {

        UpdateMapResponseDocument getMapResponse = UpdateMapResponseDocument.Factory.newInstance();
        com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct s = getMapResponse.addNewUpdateMapResponse().addNewWorkProductPublicationResponse().addNewWorkProduct();
        // s.set(WorkProductHelper.toWorkProduct(wp));
        // WorkProductHelper.
        // s.addNewDigest();
        // s.addNewWorkProductIdentification();
        // s.addNewWorkProductProperties();

        // System.out.println(getMapResponse);
        Node node = coerce(getMapResponse);

        Document request = getRequestDocument(path + "request-getmaps.xml", "GetMapsRequest");
        EasyMock.expect(service.getMap("test")).andReturn(node);

        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        product.setProduct(XmlObject.Factory.parse("<stuff>stuff</stuff>"));

        EasyMock.expect(service.getMaps("test")).andReturn(Collections.singletonList(product));
        EasyMock.replay(service);
        Element response = endpoint.getMaps(request.getDocumentElement(), request);
        assertNotNull(response);
    }

    @Test
    public void updateMap() throws Exception {

        Document request = getRequestDocument(path + "request-updatemap.xml", "UpdateMapRequest");
        Node submitContent = request.getDocumentElement().getElementsByTagNameNS("http://www.opengis.net/context",
            "ViewContext").item(0);
        Node packageId = request.getDocumentElement().getElementsByTagNameNS(PRECISS_NS, PKG_ID).item(0);

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);

        // assign value to ProductVersion and Size to avoid the NullPointerException
        product.setProductVersion(new Integer(1));
        product.setSize(new Integer(0));

        status.setProduct(product);

        EasyMock.expect(service.updateMap(packageId, submitContent)).andReturn(status);
        EasyMock.replay(service);
        Element response = endpoint.updateMap(request.getDocumentElement(), request);
        assertNotNull(response);
    }

    @Test
    public void submitLayer() throws Exception {

        Document request = getRequestDocument(path + "request-submitlayer.xml",
            "SubmitLayerRequest");
        Node submitContent = request.getDocumentElement().getElementsByTagNameNS("http://www.opengis.net/context",
            "Layer").item(0);

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        WorkProduct product = new WorkProduct();
        product.setProductID(LAYER_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        status.setProduct(product);
        // System.out.println(product.getProductVersion());

        EasyMock.expect(service.createLayer(INCIDENT_ID, submitContent)).andReturn(status);
        EasyMock.replay(service);
        Element response = endpoint.createLayer(request.getDocumentElement(), request);
        assertNotNull(response);

        // Document document = DOMUtils.newDocument();
        // Node node = DOMUtils.appendElement(document, new QName("Response"));

        // Document request = getRequestDocument(path + "request-submitlayer.xml",
        // "SubmitLayerRequest");
        // Node submitContent = request.getDocumentElement().getElementsByTagNameNS(
        // "http://www.opengis.net/context", "Layer").item(0);

        // System.out.println("==> submitLayer");
        // System.out.println("==> request: " + request);
        // System.out.println("==> submitContent=" + submitContent);

        // ProductPublicationStatus status = new ProductPublicationStatus();
        // status.setStatus(ProductPublicationStatus.SuccessStatus);
        // WorkProduct product = new WorkProduct();
        // product.setProductID(LAYER_ID);
        // product.setProductType(MapService.MapType);
        // product.setProductVersion(WP_VERSION);
        // product.setSize(WP_VERSION);
        // status.setProduct(product);

        // EasyMock.expect(service.createLayer(INCIDENT_ID, submitContent)).andReturn(status);
        // EasyMock.expect(service.getLayer(INCIDENT_ID, LAYER_ID)).andReturn(node);
        // EasyMock.replay(service);
        // Element response = endpoint.createLayer(request.getDocumentElement(), request);
        // assertNotNull(response);
    }

    @Test
    public void getLayer() throws Exception {

        Document document = DOMUtils.newDocument();
        Node node = DOMUtils.appendElement(document, new QName("Response"));
        Document request = getRequestDocument(path + "request-getlayer.xml", "GetLayerRequest");

        // Get the WorkProductIdentification that will be used in the getMap call
        Node requestNode = DOMUtils.getChild(request, "GetLayerRequest");
        Node packageIdNode = DOMUtils.getChild(requestNode, new QName(PRECISS_NS, PKG_ID));
        assertNotNull(packageIdNode);

        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        product.setProduct(XmlObject.Factory.parse("<stuff>stuff</stuff>"));

        EasyMock.expect(service.getLayerWP(packageIdNode)).andReturn(product);
        EasyMock.replay(service);
        Element response = endpoint.getLayer(request.getDocumentElement(), request);
        assertNotNull(response);
    }

    @Test
    public void getLayers() throws Exception {

        Document document = DOMUtils.newDocument();
        Node node = DOMUtils.appendElement(document, new QName("Response"));
        Document request = getRequestDocument(path + "request-getlayers.xml", "GetLayersRequest");

        // Get the WorkProductIdentification that will be used in the getMap call
        Node requestNode = DOMUtils.getChild(request, "GetLayerRequest");

        WorkProduct product = new WorkProduct();
        product.setProductID(MAP_ID);
        product.setProductVersion(WP_VERSION);
        product.setSize(WP_VERSION);
        product.setProduct(XmlObject.Factory.parse("<stuff>stuff</stuff>"));

        EasyMock.expect(service.getLayers("test")).andReturn(Collections.singletonList(product));
        EasyMock.replay(service);
        Element response = endpoint.getLayers(request.getDocumentElement(), request);
        assertNotNull(response);
    }

    @Test
    public void submitShapefile() throws Exception {

        byte[] bytes = "something".getBytes();
        Document request = DOMUtils.newDocument();
        Node node = DOMUtils.appendElement(request, new QName("SubmitShapefileRequest"));
        DOMUtils.appendElementWithText(node, new QName("IncidentId"), "test");
        DOMUtils.appendElementWithText(node,
            new QName("ContentData"),
            new String(Base64.encodeBase64(bytes)));

        ProductPublicationStatus status = new ProductPublicationStatus();
        status.setStatus(ProductPublicationStatus.SuccessStatus);
        WorkProduct product = new WorkProduct();
        product.setProductID("test");
        status.setProduct(product);

        EasyMock.expect(service.submitShapefile(EasyMock.matches("test"), EasyMock.aryEq(bytes))).andReturn(Collections.singletonList(status));
        EasyMock.replay(service);
        Element response = endpoint.submitShapefile(request.getDocumentElement(), request);
        assertNotNull(response);
    }

    protected Document getRequestDocument(String filename, String requestName) throws Exception {

        Document document = DOMUtils.parseDocument(filename);
        Node body = DOMUtils.getChild(document.getDocumentElement(), "Body");
        Node child = DOMUtils.getChild(body, requestName);
        Document result = DOMUtils.newDocument();
        result.appendChild(result.importNode(child, true));
        return result;
    }

    protected Document getWorkProductDocument(String incidentId, String wpId) throws Exception {

        // Document document = DOMUtils.newDocument();
        // Node workProductNode = DOMUtils.appendElement(document, new
        // QName(PRECISS_NS,"WorkProduct"));
        // Node packageIdNode = DOMUtils.appendElement(workProductNode, new
        // QName(PRECISB_NS,PKG_ID));
        // DOMUtils.appendElementWithText(packageIdNode, new QName(PRECISB_NS, "Identifier"), wpId);
        //
        // Node packagePropsNode = DOMUtils.appendElement(workProductNode, new
        // QName(PRECISB_NS,PKG_PROPS));
        // DOMUtils.appendElementWithText(packagePropsNode, new QName(PRECISB_NS, "Identifier"),
        // incidentId);
        // return document;

        WorkProductDocument wpd = WorkProductDocument.Factory.newInstance();
        wpd.addNewWorkProduct();
        StructuredPayloadType payload = wpd.getWorkProduct().addNewStructuredPayload();
        XmlObject object = XmlObject.Factory.parse(new String("<data>stuff</data>"));
        payload.set(object);
        payload.setId("id");
        payload.addNewStructuredPayloadMetadata();
        payload.getStructuredPayloadMetadata().setCommunityURI("uicds.org");
        payload.getStructuredPayloadMetadata().setCommunityVersion("0.8");

        // XmlCursor xc = payload.newCursor();
        // xc.toFirstChild();
        // xc.insertElementWithText("data", "stuff");

        // XmlObject obj = XmlObject.Factory.newInstance();
        // XmlCursor xc = obj.newCursor();
        // xc.toNextToken();
        // xc.beginElement("elem");
        // xc.insertElementWithText("abs", "value");
        // xc.dispose();

        wpd.getWorkProduct().addNewPackageMetadata();
        wpd.getWorkProduct().getPackageMetadata().setDataItemID("dataItem");
        wpd.getWorkProduct().getPackageMetadata().setDataItemReferenceID("refID");

        // DataItemStatusAbstractDocument dabs =
        // DataItemStatusAbstractDocument.Factory.newInstance();
        // dabs.addNewDataItemStatusAbstract();

        /*
         * first happy <WorkProduct xmlns="http://www.saic.com/precis/2009/06/structures"
         * xmlns:ulex="ulex:message:structure:1.0" xmlns:ns="http://ucore.gov/ucore/2.0"
         * xmlns:base="http://www.saic.com/precis/2009/06/base"> <ulex:PackageMetadata>
         * <ulex:DataItemID>dataItem</ulex:DataItemID>
         * <ulex:DataItemReferenceID>refID</ulex:DataItemReferenceID> <ns:DataItemStatus
         * ns:label="label"/> <ulex:DataOwnerMetadata> <ulex:DataOwnerIdentifierAbstract/>
         * <ulex:DataOwnerContactAbstract/> <ulex:DataOwnerMetadataExtensionAbstract/>
         * </ulex:DataOwnerMetadata>
         * 
         * DataItemStatusDocument disd = DataItemStatusDocument.Factory.newInstance();
         * disd.addNewDataItemStatus();
         * disd.getDataItemStatus().addNewLabel().setStringValue("label"); System.out.println(disd);
         * 
         * XmlCursor xc = wpd.getWorkProduct().newCursor(); xc.toFirstChild(); xc.toLastChild();
         * xc.toNextToken(); xc.toNextToken(); xc.toNextToken(); XmlCursor dc = disd.newCursor();
         * dc.copyXmlContents(xc); xc.dispose(); dc.dispose();
         */
        // DataItemStatusDocument disd = DataItemStatusDocument.Factory.newInstance();
        // disd.addNewDataItemStatus();
        // disd.getDataItemStatus().addNewLabel().setStringValue("label");
        XmlObject o = wpd.getWorkProduct().getPackageMetadata().addNewDataItemStatusAbstract();
        XmlObject no = o.substitute(new QName("http://ucore.gov/ucore/2.0", "DataItemStatus"),
            DataItemStatusType.type);
        if (no == o) {
            System.out.println("cannot change to DataItemStatusType");
        } else {
            DataItemStatusType dis = (DataItemStatusType) no;
            dis.addNewLabel().setStringValue("label");
        }

        // XmlCursor cursor = dist.newCursor();
        // System.out.println(cursor.getAttributeText(new
        // QName("http://www.w3.org/2001/XMLSchema-instance", "type")));
        // cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "type"),
        // "ulex:DataItemStatusAbstractType");
        // cursor.setName(new QName("http://ucore.gov/ucore/2.0", "DataItemStatusType"));
        // cursor.dispose();

        wpd.getWorkProduct().getPackageMetadata().addNewDataOwnerMetadata().addNewDataOwnerContactAbstract();
        o = wpd.getWorkProduct().getPackageMetadata().getDataOwnerMetadata().getDataOwnerContactAbstract();
        no = o.substitute(new QName("http://ucore.gov/ucore/2.0", "DataOwnerContact"),
            PointOfContactType.type);
        if (no == o) {
            System.out.println("cannot change to DataOwnerContact");
            System.out.println(no);
        } else {
            PointOfContactType dis = (PointOfContactType) no;
            dis.addNewService().addName("DataOnwerContract");
        }

        wpd.getWorkProduct().getPackageMetadata().getDataOwnerMetadata().addNewDataOwnerIdentifierAbstract();
        o = wpd.getWorkProduct().getPackageMetadata().getDataOwnerMetadata().getDataOwnerIdentifierAbstract();
        no = o.substitute(new QName("http://ucore.gov/ucore/2.0", "DataOwnerIdentifier"),
            PointOfContactType.type);
        if (no == o) {
            System.out.println("cannot change to DataOwnerIdentifier");
            System.out.println(no);
        } else {
            PointOfContactType dis = (PointOfContactType) no;
            dis.addNewService().addName("DataOwnerIdentifier");
        }

        o = wpd.getWorkProduct().getPackageMetadata().getDataOwnerMetadata().addNewDataOwnerMetadataExtensionAbstract();
        no = o.substitute(new QName("ulex:message:structure:1.0",
                                    "DataOwnerMetadataDomainAttribute"), DomainAttributeType.type);
        if (no == o) {
            System.out.println("cannot change to DataOwnerMetadataDomainAttribute");
            System.out.println(no);
        } else {
            DomainAttributeType dis = (DomainAttributeType) no;
            dis.setDomainName("DataOwnerMetadataDomainAttribute");
        }

        o = wpd.getWorkProduct().getPackageMetadata().addNewDisseminationCriteriaAbstract();
        no = o.substitute(new QName("http://ucore.gov/ucore/2.0", "DisseminationCriteria"),
            DisseminationCriteria.type);
        if (no == o) {
            System.out.println("cannot change to DisseminationCriteria");
            System.out.println(no);
        } else {
            assertTrue(no instanceof DisseminationCriteria);
        }

        o = wpd.getWorkProduct().getPackageMetadata().addNewPackageMetadataExtensionAbstract();
        no = o.substitute(new QName(PRECISS_NS, "WorkProductIdentification"),
            IdentificationType.type);
        if (no == o) {
            System.out.println("cannot change to WorkProductIdentification");
            System.out.println(no);
        } else {
            IdentificationType dis = (IdentificationType) no;
            dis.addNewIdentifier().setStringValue(wpId);
            dis.addNewVersion().setStringValue(WP_VERSION.toString());
            dis.addNewType().setStringValue(MapService.MapType);
            dis.addNewChecksum().setStringValue(WP_VERSION.toString());
            dis.setState(StateType.ACTIVE);
        }

        o = wpd.getWorkProduct().getPackageMetadata().addNewPackageMetadataExtensionAbstract();
        no = o.substitute(new QName(PRECISS_NS, "WorkProductProperties"), PropertiesType.type);
        if (no == o) {
            System.out.println("cannot change to PropertiesType");
            System.out.println(no);
        } else {
            PropertiesType dis = (PropertiesType) no;
            dis.addNewAssociatedGroups().addNewIdentifier().setStringValue(incidentId);
        }

        ValidationUtil.validate(wpd, true);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(wpd.newInputStream());

        return document;

        // wpd.getWorkProduct().addNewPackageMetadata().addNewDataItemCategory().setDataItemCategoryText("category");
        // wpd.getWorkProduct().getPackageMetadata().setDataItemID("dataItemId");
        // XmlObject nobj = dabs.substitute(new QName("http://ucore.gov/ucore/2.0",
        // "DataItemStatusType"), DataItemStatusType.type);
        // if (nobj == dabs) {
        // System.out.println("cannot change to DataItemStatusType");
        // System.out.println(dabs);
        // }
        // XmlObject dabs =
        // wpd.getWorkProduct().getPackageMetadata().addNewDataItemStatusAbstract();
        // DataItemStatusType dist = (DataItemStatusType) dabs.changeType(DataItemStatusType.type);
        // dist.addNewLabel().setStringValue("nothing");

        // XmlCursor cursor = dist.newCursor();
        // System.out.println(cursor.getAttributeText(new
        // QName("http://www.w3.org/2001/XMLSchema-instance", "type")));
        // cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "type"),
        // "ulex:DataItemStatusAbstractType");
        // cursor.setName(new QName("http://ucore.gov/ucore/2.0", "DataItemStatusType"));
        // cursor.dispose();

    }

    protected String getIncidentId(Node node) {

        String id = null;
        Node workProductNode = DOMUtils.getChild(node, new QName(PRECISS_NS, "WorkProduct"));
        try {
            WorkProductDocument wpd = WorkProductDocument.Factory.parse(workProductNode);
            if (wpd.getWorkProduct().getPackageMetadata().sizeOfPackageMetadataExtensionAbstractArray() > 0) {
                // for each of the PackageMetadataExtension objects
                for (XmlObject pmd : wpd.getWorkProduct().getPackageMetadata().getPackageMetadataExtensionAbstractArray()) {
                    // See if there are any WorkProductProperties children
                    if (pmd instanceof PropertiesType) {
                        PropertiesType prop = (PropertiesType) pmd;
                        id = prop.getAssociatedGroups().getIdentifierArray(0).getStringValue();
                    }
                }
            }
        } catch (XmlException e) {
            System.err.println("Failed to find incident id");
            id = null;
        }
        return id;
    }

    protected String getWorkProductId(Node node) {

        String id = null;
        Node workProductNode = DOMUtils.getChild(node, new QName(PRECISS_NS, "WorkProduct"));
        try {
            WorkProductDocument wpd = WorkProductDocument.Factory.parse(workProductNode);
            if (wpd.getWorkProduct().getPackageMetadata().sizeOfPackageMetadataExtensionAbstractArray() > 0) {
                // for each of the PackageMetadataExtension objects
                for (XmlObject pmd : wpd.getWorkProduct().getPackageMetadata().getPackageMetadataExtensionAbstractArray()) {
                    // See if there are any WorkProductProperties children
                    if (pmd instanceof IdentificationType) {
                        IdentificationType idType = (IdentificationType) pmd;
                        id = idType.getIdentifier().getStringValue();
                    }
                }
            }
        } catch (XmlException e) {
            System.err.println("Failed to find incident id");
            id = null;
        }
        return id;
    }

    protected Node coerce(XmlObject obj) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(obj.newInputStream());
        return document.getDocumentElement();
    }
}
