/*
 * VYSOKÉ UČENÍ TECHNICKÉ V BRNĚ BRNO UNIVERSITY OF TECHNOLOGY
 *
 * FAKULTA INFORMAČNÍCH TECHNOLOGIÍ
 *
 * Baklářská práce
 *
 * Generátor konečných automatů z grafického popisu pro jazyk VHDL
 *
 * Author: Martin Janyš
 *
 * Brno 2013
 */
package cz.fsmgen.gui.utils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.BlockCellWithCode;
import cz.fsmgen.gui.cells.BlocksInputPort;
import cz.fsmgen.gui.cells.BlocksOutputPort;
import cz.fsmgen.gui.cells.BlocksPort;
import cz.fsmgen.gui.cells.CellWithProperties;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import cz.jvhdl.datatypes.DataTypeVhdl;
import cz.fsmgen.jvhdl.utils.GraphVhdl;
import org.w3c.dom.*;

/**
 *
 * @author Martin
 */
public class XmlUtils {

    static final String cellId = "cell";
    static final String edgeId = "edge";
    static final String geometryId = "geo";
    static final String propertiesId = "properties";
    static final String rootId = "fsmgenGraph";
    static final String blockId = "block";
    static final String defaultParentId = "defaultParent";
    static final String pointsId = "points";
    static final String pointId = "point";
    static final String codeId = "code";
    static final String archId = "arch";
    static final String StyleId = "style";

    private static Document getDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        //root elements
        Document doc = docBuilder.newDocument();
        return doc;
    }

    private static void printDoc(Document doc, OutputStream out, String indent) throws TransformerConfigurationException, TransformerException {
        //write the content into xml file
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute("indent-number", new Integer(2));
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, indent);
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(out);
        t.transform(source, result);

        try {
            result.getOutputStream().close();
        }
        catch (IOException ex) {
            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Graph2Xml(mxGraph graph) {
        try {
            Graph2Xml(graph, null);
        }
        catch (Exception ex) {
            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Graph2Xml(final mxGraph graph, final String filename) throws Exception {
        class Graph2XmlWorker extends SwingWorker<String, Object> {

            @Override
            public String doInBackground() throws Exception {
                PrintStream ps = null;
                if (filename == null) {
                    ps = System.out;
                }
                else {
                    try {
                        ps = new PrintStream(new File(filename));
                    }
                    catch (FileNotFoundException ex) {
                    }
                }

                // root
                mxCell defaultParent = (mxCell) GraphEditor.getTopLevelParent((mxCell) graph.getDefaultParent());
                // layer 0
                defaultParent = (mxCell) defaultParent.getChildAt(0);

                try {
                    Document doc = getDoc();
                    Element root = doc.createElement(rootId);
                    doc.appendChild(root);

                    // Default parent
                    Element defaultParentElement = doc.createElement(cellId);
                    defaultParentElement.setAttribute(defaultParentId, defaultParent.getId());

                    root.appendChild(defaultParentElement);

                    // Child nodes
                    appendChildNodes(doc, defaultParentElement, defaultParent);

                    printDoc(doc, ps, "yes");
                }
                catch (ParserConfigurationException | TransformerException ex) {
                    Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        }
        new Graph2XmlWorker().doInBackground();
    }

    private static void appendChildNodes(Document doc, Element parentElement, mxCell parent) throws Exception {
        for (int i = 0; i < parent.getChildCount(); i++) {
            mxCell child = (mxCell) parent.getChildAt(i);
            Element childElement = doc.createElement(cellId);

            if (child.isEdge()) {
                appendEdgeTerminals(doc, childElement, child);
            }
            appendCellGeometry(doc, childElement, child);

            if (child.isEdge()) {
                childElement.setAttribute("edge", "1");
            }
            // add atributes
            appendCellAtributes(doc, childElement, child);
            appendCellProperties(doc, childElement, child);
            appendPoints(doc, childElement, child);
            // childs
            if (child.getChildCount() != 0) {
                appendChildNodes(doc, childElement, child);
            }

            parentElement.appendChild(childElement);
        }
    }

    private static void appendCellGeometry(Document doc, Element parentElement, mxCell cell) throws Exception {
        mxGeometry geo = cell.getGeometry();
        Element geometryElement = doc.createElement(geometryId);

        geometryElement.setAttribute("x", new Double(geo.getX()).toString());
        geometryElement.setAttribute("y", new Double(geo.getY()).toString());
        geometryElement.setAttribute("height", new Double(geo.getHeight()).toString());
        geometryElement.setAttribute("width", new Double(geo.getWidth()).toString());

        // append
        parentElement.appendChild(geometryElement);
    }

    private static void appendEdgeTerminals(Document doc, Element parentElement, mxCell cell) throws Exception {
        Element edgeElement = doc.createElement(edgeId);
        edgeElement.setAttribute("source", cell.getSource().getId().toString());
        edgeElement.setAttribute("target", cell.getTarget().getId().toString());
//        edgeElement.setAttribute("style", cell.getStyle());
        parentElement.appendChild(edgeElement);
    }

    private static void appendCellAtributes(Document doc, Element cellElement, mxCell cell) {
        cellElement.setAttribute("id", cell.getId());
        cellElement.setAttribute("value", cell.getValue() == null ? ""
                : cell.getValue().toString());
        cellElement.setAttribute("class", cell.getClass().getName());

        if (cell instanceof BlockCellWithCode) {
            BlockCellWithCode b = (BlockCellWithCode) cell;

            Element codeElement = doc.createElement("code");
            Element archElement = doc.createElement("arch");

            Text codeText = doc.createTextNode(b.getCode());
            Text archText = doc.createTextNode(b.getArchitecture());

            codeElement.appendChild(codeText);
            archElement.appendChild(archText);

            cellElement.appendChild(codeElement);
            cellElement.appendChild(archElement);
        }
    }

    private static void appendCellProperties(Document doc, Element cellElement, mxCell cell) throws Exception {
        if (cell instanceof Properties) {
            Element propertiesElement = doc.createElement(propertiesId);

            for (String field : ((Properties) cell).getPropertiesFields()) {
                Object value = PropertiesUtils.getValueFor((Properties) cell, field);
                if (value instanceof Collection) {
                    StringBuilder sb = new StringBuilder();
                    for (Object o : (Collection) value) {
                        Object data = ((CellWithProperties) cell).getInOut().get(((BlocksPort) o).getName());
                        sb.append(((BlocksPort) o).getValue())
                                .append("=")
                                .append(data == null ? "" : data.toString())
                                .append(";");
                    }
                    propertiesElement.setAttribute(field, sb.toString());
                }
                else {
                    propertiesElement.setAttribute(field, value != null
                            ? value.toString() : "");
                }
            }
            // style
            propertiesElement.setAttribute(StyleId, cell.getStyle());

            cellElement.appendChild(propertiesElement);

        }
    }

    public static boolean Xml2Graph(GraphEditor.CustomGraph graph, File file) {
        try {
            graph.setRefreshAble(false);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            if (root.getNodeName().equals(rootId)) {
                NodeList childNodes = new Nodes(root.getChildNodes());
                // 1st - default parent ... else ignore
                Node defaultParentNode = childNodes.item(0);
                if (defaultParentNode.hasAttributes()) {
                    NamedNodeMap attributes = defaultParentNode.getAttributes();
                    if (attributes.getNamedItem(defaultParentId) != null) {
                        addNodesToGraph(new Nodes(defaultParentNode.getChildNodes()),
                                graph,
                                (mxCell) graph.getDefaultParent());
                    }
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }

            return true;
        }
        catch (Exception ex) {
            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally {
            graph.setRefreshAble(true);
            graph.refresh();
        }
    }

    private static void addNodesToGraph(Nodes nodes, mxGraph graph, mxCell parent) throws Exception {
        List<mxCell> edges = new ArrayList<>();
        List<Node> nodesEdge = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (child.getNodeName().equals(cellId)) {
                NamedNodeMap attributes = child.getAttributes();

                String cls = attributes.getNamedItem("class").getNodeValue();
                String id = attributes.getNamedItem("id").getNodeValue();
                String value = attributes.getNamedItem("value").getNodeValue();

                mxCell cell = (mxCell) createCellInstance(cls);

                cell.setId(id);

                cell.setValue(value);
                if (!(cell instanceof BlocksPort)) {
                    // insert before geo & properties -> due fsmType

                    if (cell.isEdge()) {
                        edges.add(cell);
                        nodesEdge.add(child);
                    }
                    else {
                        graph.addCell(cell, parent);

                        setPropertiesFor(cell, child);
                        setPointsFor(cell, child);
                    }

                    if (cell instanceof BlockCellWithCode) {
                        setCodeFor((BlockCellWithCode) cell, child);
                    }

                    // TODO: test
                    setGeometryFor(cell, child);
                    addNodesToGraph(new Nodes(child.getChildNodes()), graph, cell);
                }
                else {
                    if (parent instanceof BlockCell) {
                        BlockCell b = (BlockCell) parent;
                        BlocksPort port = null;
                        b.createPortsList();
                        if (cell instanceof BlocksInputPort) {
                            port = b.getInputPort();
                        }
                        else if (cell instanceof BlocksOutputPort) {
                            port = b.getOutputPort();
                        }

                        if (port != null) {
                            port.setName(value);
                            setPropertiesFor(port, child);
                        }

                        ((BlockCell) b).getGeometry().setWidth(150);
                    }
                }
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            mxCell edge = edges.get(i);
            Node node = nodesEdge.get(i);

            setEdgeTerminals(graph, edge, node);

            edge.getSource().insertEdge(edge, true);
            graph.addCell(edge, parent);

            setGeometryFor(edge, node);
            setPropertiesFor(edge, node);
            setPointsFor(edge, node);
        }
    }

    private static void setPropertiesFor(mxCell cell, Node node) throws Exception {
        if (cell instanceof Properties) {
            Nodes childs = new Nodes(node.getChildNodes());

            for (int i = 0; i < childs.getLength(); i++) {
                if (childs.item(i).getNodeName().equals(propertiesId)) {
                    // longer properties
                    if (cell instanceof BlocksPort) {
                        ((BlocksPort) cell).setType(DataTypeVhdl.Std.STD_LOGIC_VECTOR);
                    }

                    NamedNodeMap attributes = childs.item(i).getAttributes();
                    String[] fields = ((Properties) cell).getPropertiesFields();

                    if (fields != null) {
                        for (String field : fields) {
                            Node n = attributes.getNamedItem(field);
                            if (n != null) {
                                Object value = n.getNodeValue();
                                try {
                                    PropertiesUtils.setValueFor((Properties) cell, field, value);
                                }
                                catch (NoSuchMethodException e) {
                                    // inputs/outputs list
                                    if (cell instanceof CellWithProperties) {
                                        if (cell.getParent() instanceof BlockCell) {
                                            BlockCell b = ((BlockCell) cell.getParent());
                                            String[] values = value.toString().split(";");
                                            Map<String, Object> m = ((CellWithProperties) cell).getInOut();
                                            for (String string : values) {
                                                String[] keyValue = string.split("=", 2);
                                                Object v = keyValue.length > 1
                                                        ? keyValue[1] : null;

                                                if (field.equals("inputs")) {
                                                    m.put(keyValue[0], v);
                                                }
                                                else if (field.equals("outputs")) {
                                                    m.put(keyValue[0], v);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Node n = attributes.getNamedItem(StyleId);
                    if (n != null) {
                        Object value = n.getNodeValue();
                        cell.setStyle(value.toString());
                    }
                }
            }
            // style 
        }
    }

    private static void setGeometryFor(mxCell cell, Node node) throws Exception {
        Nodes childs = new Nodes(node.getChildNodes());

        for (int i = 0; i < childs.getLength(); i++) {
            if (childs.item(i).getNodeName().equals(geometryId)) {
                double x, y, w, h;
                NamedNodeMap attributes = childs.item(i).getAttributes();

                x = new Double(attributes.getNamedItem("x").getNodeValue().toString());
                y = new Double(attributes.getNamedItem("y").getNodeValue().toString());
                w = new Double(attributes.getNamedItem("width").getNodeValue().toString());
                h = new Double(attributes.getNamedItem("height").getNodeValue().toString());

                cell.getGeometry().setX(x);
                cell.getGeometry().setY(y);
                cell.getGeometry().setWidth(w);
                cell.getGeometry().setHeight(h);
            }
        }
    }

    private static void setEdgeTerminals(mxGraph graph, mxCell cell, Node node) throws Exception {
        Nodes childs = new Nodes(node.getChildNodes());

        for (int i = 0; i < childs.getLength(); i++) {
            Node edge = childs.item(i);
            if (edge.getNodeName().equals(edgeId)) {
                NamedNodeMap attributes = edge.getAttributes();
                String source = attributes.getNamedItem("source").getNodeValue();
                String target = attributes.getNamedItem("target").getNodeValue();
//                String style = attributes.getNamedItem("style").getNodeValue();

                mxCell sourceCell = (mxCell) ((mxGraphModel) graph.getModel()).getCell(source);
                mxCell targetCell = (mxCell) ((mxGraphModel) graph.getModel()).getCell(target);

                cell.setSource(sourceCell);
                cell.setTarget(targetCell);

//                cell.setStyle(style);
            }
        }
    }

    private static Object createCellInstance(String cls) throws Exception {
        Class<?> c = Class.forName(cls);
        Constructor<?> cons = c.getConstructor();
        Object object = cons.newInstance();

        return object;
    }

    private static void appendPoints(Document doc, Element parent, mxCell cell) throws Exception {
        List<mxPoint> points = cell.getGeometry().getPoints();

        if (points != null && !points.isEmpty()) {
            Element pointsElement = doc.createElement(pointsId);
            for (mxPoint point : points) {
                Element pointElem = doc.createElement(pointId);
                pointElem.setAttribute("x", new Double(point.getX()).toString());
                pointElem.setAttribute("y", new Double(point.getY()).toString());
                pointsElement.appendChild(pointElem);
            }
            parent.appendChild(pointsElement);
        }
    }

    private static void setPointsFor(mxCell cell, Node node) throws Exception {
        Nodes childs = new Nodes(node.getChildNodes());
        for (int i = 0; i < childs.getLength(); i++) {
            if (childs.item(i).getNodeName().equals(pointsId)) {
                Nodes points = new Nodes(childs.item(i).getChildNodes());
                List<mxPoint> pointsList = new ArrayList<>();
                for (int j = 0; j < points.getLength(); j++) {
                    double x, y;
                    NamedNodeMap attributes = points.item(j).getAttributes();

                    x = new Double(attributes.getNamedItem("x").getNodeValue().toString());
                    y = new Double(attributes.getNamedItem("y").getNodeValue().toString());

                    pointsList.add(new mxPoint(x, y));
                }
                if (cell.getGeometry() != null && cell.getGeometry().getPoints() != null) {
                    pointsList.addAll(cell.getGeometry().getPoints());
                }
                cell.getGeometry().setPoints(pointsList);

                if (cell.isEdge()) {
                    cell.getGeometry().setRelative(true);
                    cell.getGeometry().setOffset(null);
                }
            }
        }
    }

    private static void setCodeFor(BlockCellWithCode cell, Node node) throws Exception {
        Nodes childs = new Nodes(node.getChildNodes());
        for (int i = 0; i < childs.getLength(); i++) {
            if (childs.item(i).getNodeName().equals(codeId)) {
                Nodes n = new Nodes(childs.item(i).getChildNodes());
                if (n.getLength() > 0) {
                    Node item = n.item(0);
                    cell.setCode(item.getNodeValue());
                }
            }
            if (childs.item(i).getNodeName().equals(archId)) {
                Nodes n = new Nodes(childs.item(i).getChildNodes());
                if (n.getLength() > 0) {
                    Node item = n.item(0);
                    cell.setArchitecture(item.getNodeValue());
                }
            }
        }
    }

    /**
     * @src
     * http://stackoverflow.com/questions/229310/how-to-ignore-whitespace-while-reading-a-file-to-produce-an-xml-dom
     */
    private static class Nodes implements NodeList, Iterable<Node> {

        private final List<Node> nodes;

        public Nodes(NodeList list) {
            nodes = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                if (!isWhitespaceNode(list.item(i))) {
                    nodes.add(list.item(i));
                }
            }
//            Collections.sort(nodes, new Comparator<Node>() {
//                @Override
//                public int compare(Node o1, Node o2) {
//                    try {
//                        NamedNodeMap att1 = o1.getAttributes();
//                        NamedNodeMap att2 = o2.getAttributes();
//                        Node id1 = att1.getNamedItem("id");
//                        Node id2 = att2.getNamedItem("id");
//
//                        return id1.getNodeValue().compareTo(id2.getNodeValue());
//                    }
//                    catch (Exception e) {
//                        return 0;
//                    }
//                }
//            });
        }

        @Override
        public Node item(int index) {
            return nodes.get(index);
        }

        @Override
        public int getLength() {
            return nodes.size();
        }

        private static boolean isWhitespaceNode(Node n) {
            if (n.getNodeType() == Node.TEXT_NODE) {
                String val = n.getNodeValue();
                return val.trim().length() == 0;
            }
            else {
                return false;
            }
        }

        @Override
        public Iterator<Node> iterator() {
            return nodes.iterator();
        }
    }

    public static void main(String[] args) throws Exception {
        GraphEditor editor = new GraphEditor();
        Xml2Graph((GraphEditor.CustomGraph) editor.getGraph(), new File("tmp.xml"));
        GraphVhdl.process(editor.getGraph(), new File("e:\\Martin\\Fitkit svn\\apps\\demo\\test\\fpga\\"));
    }
}
