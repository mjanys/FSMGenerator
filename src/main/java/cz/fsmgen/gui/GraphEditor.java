/**
 * $Id: GraphEditor.java,v 1.18 2012-11-05 14:51:57 mate Exp $ Copyright (c)
 * 2006-2012, JGraph Ltd
 */
/**
 * Editor:
 *
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
package cz.fsmgen.gui;

import com.mxgraph.canvas.mxICanvas;
import java.awt.Color;
import java.awt.Point;
import java.net.URL;
import java.text.NumberFormat;
import javax.swing.ImageIcon;

import org.w3c.dom.Document;

import com.mxgraph.swing.editor.BasicGraphEditor;
import com.mxgraph.swing.editor.EditorPalette;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.editor.EditorToolBar;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.*;
import com.mxgraph.view.mxGraph;

import cz.fsmgen.gui.cells.*;
import cz.fsmgen.gui.utils.Properties;
import cz.fsmgen.gui.utils.PropertiesUtils;
import cz.fsmgen.gui.utils.StringUtils;
import cz.fsmgen.gui.cells.BlockCellWithCode;
import static cz.fsmgen.gui.cells.BlocksOutputPort.Type.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.JOptionPane;
import java.io.File;
import javax.swing.JFileChooser;

public class GraphEditor extends BasicGraphEditor {

    /**
     *
     */
    private static final long serialVersionUID = -4601740824088314699L;
    /**
     * Holds the shared number formatter.
     *
     * @see NumberFormat#getInstance()
     */
    public static final NumberFormat numberFormat = NumberFormat.getInstance();
    /**
     * Holds the URL for the icon to be used as a handle for creating new
     * connections. This is currently unused.
     */
    public static URL url = null;
    private static GraphEditor app;
    public static File destinationFile = new File(".");

    public static GraphEditor app() {
        return app;
    }
    private final mxGraph graph;

    public mxGraph getGraph() {
        return graph;
    }
    //GraphEditor.class.getResource("/com/mxgraph/swing/images/connector.gif");

    public GraphEditor() {
        this("mxGraph Editor", new CustomGraphComponent(new CustomGraph()));
    }
    private EditorPalette blocksPalette = insertPalette("Blocks");
    private EditorPalette placesPalette = insertPalette("Places");
    private EditorPalette edgePalette = insertPalette("Edges");

    public EditorPalette getBlocksPalette() {
        return blocksPalette;
    }

    /**
     *
     */
    public GraphEditor(String appTitle, mxGraphComponent component) {
        super(appTitle, component);
        app = this;
        graph = graphComponent.getGraph();

        setModified(false);

        blocksPalette.setVisible(true);
        placesPalette.setVisible(false);

        // Creates the shapes palette
        /**
         *
         */        // Adds some template cells for dropping into the graph
        placesPalette
                .addTemplate(
                mxResources.get("fsmInitPlace"),
                new ImageIcon(
                GraphEditor.class.getResource("/com/mxgraph/swing/images/ellipse.png")),
                new FsmInitStateCell());
        placesPalette
                .addTemplate(
                mxResources.get("fsmPlace"),
                new ImageIcon(
                GraphEditor.class.getResource("/com/mxgraph/swing/images/ellipse.png")),
                new FsmStateCell());
        placesPalette
                .addTemplate(
                mxResources.get("fsmFinPlace"),
                new ImageIcon(
                GraphEditor.class.getResource("/com/mxgraph/swing/images/doubleellipse.png")),
                new FsmFinalStateCell());
        blockPaletteInit();

        /**
         * EDGES
         */
        edgePalette
                .addEdgeTemplate(
                "Straight",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/straight.png")),
                new FsmEdgeCell("straight"));
        edgePalette
                .addEdgeTemplate(
                "Horizontal Connector",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/connect.png")),
                new FsmEdgeCell("horizontal;edgeStyle=elbowEdgeStyle"));
        edgePalette
                .addEdgeTemplate(
                "Vertical Connector",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/vertical.png")),
                new FsmEdgeCell("vertical;edgeStyle=elbowEdgeStyle"));
        edgePalette
                .addEdgeTemplate(
                "Entity Relation",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/entity.png")),
                new FsmEdgeCell("entity"));

        edgePalette.addListener(mxEvent.SELECT, new mxEventSource.mxIEventListener() {
            @Override
            public void invoke(Object sender, mxEventObject evt) {
                Object tmp = evt.getProperty("transferable");

                if (tmp instanceof mxGraphTransferable) {
                    mxGraphTransferable t = (mxGraphTransferable) tmp;
                    Object cell = t.getCells()[0];

                    if (graph.getModel().isEdge(cell)) {
                        ((CustomGraph) graph).setEdgeTemplate(cell);
                    }
                }
                Object[] select = graph.getSelectionCells();
                for (Object o : select) {
                    if (o instanceof FsmEdgeCell) {
                        FsmEdgeCell e = (FsmEdgeCell) o;
                        BlocksPort sourcePort = e.getSourcePort();
                        BlocksPort targetPort = e.getTargetPort();
                        String style = "";
                        if (sourcePort != null) {
                            style += "sourcePort=" + sourcePort.getId() + ";";
                        }
                        if (targetPort != null) {
                            style += "targetPort=" + targetPort.getId() + ";";
                        }
                        ((FsmEdgeCell) o).setStyle(style + ((mxCell) ((CustomGraph) graph).getEdgeTemplate()).getStyle());
                    }
                }
                if (select.length > 0) {
                    graph.refresh();
                }
            }
        });
        final PropertiesPanel propPanel = this.propertiesPanel;
        /*ONCLICK*/
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Object cell = graphComponent.getCellAt(e.getX(), e.getY());

                if (cell instanceof BlocksPort) {
                    graph.clearSelection();
//                    graph.setSelectionCell(cell);
                }

                if (cell != null) {
                    GraphEditor.DEBUG((mxCell) cell);
                    if (cell instanceof Properties) {
                        propPanel.updateBy((Properties) cell);

                    }
                    else {
                        propPanel.clear();
                    }
                }
                else {
                    propPanel.clear();
                }
            }
        });
    }

    public static void DEBUG(mxCell c) {
        System.out.println(c.getStyle());
    }

    public static boolean testFile(File f) {
//        if (f.exists()) {
//            return GraphEditor.app().displayConfirm(
//                    "File exists", "Would you like overwrite " + f.getName() + "?",
//                    JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION;
//        }
        return true;
    }

    public void setCellValue(Object cell, Object value) {
        graph.getModel().setValue(cell, value);
    }

    public void graphBeginUpdate() {
        graph.getModel().beginUpdate();
    }

    public void graphEndUpdate() {
        graph.getModel().endUpdate();
    }
    private File projectDirectory = null;

    public File getProjectDirectory() {
        if (projectDirectory != null) {
            return projectDirectory;
        }

        //Create a file chooser
        final JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Select directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

        //In response to a button click:
        int returnVal = fc.showOpenDialog(GraphEditor.app());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            projectDirectory = fc.getSelectedFile();
            return projectDirectory;
        }

        return null;
    }

    public static File getDestinationFile(int mode) {

        //Create a file chooser
        final JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Select file");
        fc.setFileSelectionMode(mode);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

        //In response to a button click:
        int returnVal = fc.showOpenDialog(GraphEditor.app());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            destinationFile = file;
            return destinationFile;
        }
        else {
            return null;
        }

    }

    public void blockPaletteInit() {
        blocksPalette
                .addTemplate(
                "Block",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/rectangle.png")),
                new BlockCell(
                graph,
                BlockCell.getInitAt(BlockCell.VALUE),
                Integer.valueOf(BlockCell.getInitAt(BlockCell.INPUTS)),
                Integer.valueOf(BlockCell.getInitAt(BlockCell.OUTPUTS))));
        blocksPalette
                .addTemplate(
                "Code Block",
                new ImageIcon(
                GraphEditor.class
                .getResource("/com/mxgraph/swing/images/rectangle.png")),
                new BlockCellWithCode(graph, "code", 2, 1, ""));
    }

    /**
     *
     */
    public static class CustomGraphComponent extends mxGraphComponent {

        /**
         *
         */
        private static final long serialVersionUID = -6833603133512882012L;

        /**
         *
         * @param graph
         */
        public CustomGraphComponent(mxGraph graph) {
            super(graph);

            // Sets switches typically used in an editor
            setPageVisible(false);
            setGridVisible(true);
            setToolTips(true);
            getConnectionHandler().setCreateTarget(false);

            // Loads the defalt stylesheet from an external file
            mxCodec codec = new mxCodec();
            Document doc = mxUtils.loadDocument(GraphEditor.class.getResource(
                    "/com/mxgraph/swing/resources/default-style.xml")
                    .toString());
            codec.decode(doc.getDocumentElement(), graph.getStylesheet());

            // Sets the background to white
            getViewport().setOpaque(true);
            getViewport().setBackground(Color.WHITE);
        }

        /**
         * Overrides drop behaviour to set the cell style if the target is not a
         * valid drop target and the cells are of the same type (eg. both
         * vertices or both edges).
         */
        @Override
        public Object[] importCells(Object[] cells, double dx, double dy,
                Object target, Point location) {
            if (target == null && cells.length == 1 && location != null) {
                target = getCellAt(location.x, location.y);

                if (target instanceof mxICell && cells[0] instanceof mxICell) {
                    mxICell targetCell = (mxICell) target;
                    mxICell dropCell = (mxICell) cells[0];

                    if (targetCell.isVertex() == dropCell.isVertex()
                            || targetCell.isEdge() == dropCell.isEdge()) {
                        mxIGraphModel model = graph.getModel();
                        model.setStyle(target, model.getStyle(cells[0]));
//                        graph.setSelectionCell(target);

                        return null;
                    }
                }
            }

            return super.importCells(cells, dx, dy, target, location);
        }
    }

    /**
     * A graph that creates new edges from a given template edge.
     */
    public static class CustomGraph extends mxGraph {

        private boolean refreshAble = true;

        enum Context {

            BLOCKS,
            FSM
        }
        private Context context = Context.BLOCKS;

        private void setBlockContext() {
            context = Context.BLOCKS;
        }

        private void setFsmContext() {
            context = Context.FSM;
        }

        public boolean isFsmContext() {
            return context == Context.FSM;
        }

        public boolean isBlockContext() {
            return context == Context.BLOCKS;
        }
        /**
         * Holds the edge to be used as a template for inserting new edges.
         */
        protected Object edgeTemplate = new FsmEdgeCell("straight");

        /**
         * Custom graph that defines the alternate edge style to be used when
         * the middle control point of edges is double clicked (flipped).
         */
        public CustomGraph() {
            setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");

            this.setHtmlLabels(true);
            this.setAllowLoops(true);
            this.setMultigraph(true);
            this.setExtendParentsOnAdd(false);
            this.setAllowDanglingEdges(false);

        }

        @Override
        public void refresh() {
            if (refreshAble) {
                super.refresh(); //To change body of generated methods, choose Tools | Templates.
            }
        }

        public boolean isRefreshAble() {
            return refreshAble;
        }

        public void setRefreshAble(boolean refreshAble) {
            this.refreshAble = refreshAble;
        }

        /**
         * Sets the edge template to be used to inserting edges.
         */
        public void setEdgeTemplate(Object template) {
            edgeTemplate = template;
        }

        public Object getEdgeTemplate() {
            return edgeTemplate;
        }

        /**
         * Prints out some useful information about the cell in the tooltip.
         */
        @Override
        public String getToolTipForCell(Object cell) {
            if (cell == null) {
                return "";
            }

            String h = "<html><table>";
            StringBuilder tip = new StringBuilder(h);
            if (isFsmContext()) {
                if (getModel().isEdge(cell)) {
                    if (cell instanceof FsmEdgeCell) {

                        FsmEdgeCell e = (FsmEdgeCell) cell;
                        if (e.getSource() instanceof FsmStateCell
                                && e.getTarget() instanceof FsmStateCell) {
                            FsmStateCell source = (FsmStateCell) e.getSource();
                            FsmStateCell target = (FsmStateCell) e.getTarget();
                            tip.insert(h.length(), "From " + source.getName() + " to " + target.getName() + "<hr>");
                        }

                        if (cell instanceof Properties) {
                            String[] fields = ((Properties) cell).getPropertiesFields();
                            for (String f : fields) {
                                try {
                                    Object value = PropertiesUtils.getValueFor((Properties) cell, f);
                                    if (value instanceof Collection) {
                                        for (Object o : (Collection) value) {
                                            if (o instanceof BlocksPort) {
                                                BlocksPort p = (BlocksPort) o;
                                                if (!(p instanceof BlocksOutputPort && ((BlocksOutputPort) p).getOutputType() == MOORE)) {
                                                    Object get = ((FsmEdgeCell) cell).getInOut().get(p.getName());
                                                    tip.append("<tr>")
                                                            .append("<td>")
                                                            .append(p.getName())
                                                            .append("<td>")
                                                            .append(" : ")
                                                            .append("<td>")
                                                            .append(get != null
                                                            ? get : "")
                                                            .append("</tr>");
                                                }

                                            }
                                        }
                                    }
                                    else {
                                        tip.append("<tr>")
                                                .append("<td>")
                                                .append(f)
                                                .append("<td>")
                                                .append(" : ")
                                                .append("<td>")
                                                .append(value != null
                                                ? value.toString() : "")
                                                .append("</tr>");
                                    }
                                }
                                catch (Exception ex) {
                                }
                            }
                        }
                    }
                }
                else {
                    if (cell instanceof FsmStateCell) {
                        if (cell instanceof Properties) {
                            String[] fields = ((Properties) cell).getPropertiesFields();
                            for (String f : fields) {
                                try {
                                    Object value = PropertiesUtils.getValueFor((Properties) cell, f);
                                    if (value instanceof Collection) {
                                        for (Object o : (Collection) value) {
                                            if (o instanceof BlocksPort) {
                                                BlocksPort p = (BlocksPort) o;

                                                if (!(p instanceof BlocksOutputPort && ((BlocksOutputPort) p).getOutputType() == MEALY)) {
                                                    Object get = ((FsmStateCell) cell).getInOut().get(p.getName());
                                                    tip.append("<tr>")
                                                            .append("<td>")
                                                            .append(p.getName())
                                                            .append("<td>")
                                                            .append(" : ")
                                                            .append("<td>")
                                                            .append(get != null
                                                            ? get : "")
                                                            .append("</tr>");
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        tip.append("<tr>")
                                                .append("<td>")
                                                .append(f)
                                                .append("<td>")
                                                .append(" : ")
                                                .append("<td>")
                                                .append(value != null
                                                ? value.toString() : "")
                                                .append("</tr>");
                                    }
                                }
                                catch (Exception ex) {
                                    System.err.println(ex);
                                }
                            }
                        }
                    }
                    else {
                        return "";
                    }
                }
            }
            else {
                tip.append(((mxCell) cell).getValue());
            }
            tip.append("</table></html>");
            return tip.toString();
        }

        @Override
        public void drawCell(mxICanvas canvas, Object cell) {
            super.drawCell(canvas, cell); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object createVertex(Object parent, String id, Object value,
                double x, double y, double width, double height, String style) {
            return super.createVertex(parent, id, value, x, y, width, height, style); //To change body of generated methods, choose Tools | Templates.
        }

        /**
         * Overrides the method to use the currently selected edge template for
         * new edges.
         */
        @Override
        public Object createEdge(Object parent, String id, Object value,
                Object source, Object target, String style) {

            FsmEdgeCell edge = new FsmEdgeCell();
            edge.setParent((mxICell) parent);
            edge.setId(id);
            edge.setValue(value);
            edge.setTarget((mxICell) target);
            edge.setSource((mxICell) source);
            edge.setStyle(((FsmEdgeCell) edgeTemplate).getStyle());

            return edge;
        }

        // Ports are not used as terminals for edges, they are
        // only used to compute the graphical connection point
        @Override
        public boolean isPort(Object cell) {
            mxGeometry geo = getCellGeometry(cell);

            return (geo != null) ? geo.isRelative() : false;
        }

        @Override
        public boolean isCellVisible(Object cell) {
            boolean visible = super.isCellVisible(cell);

            if (isFsmContext()) {
                visible = visible && !(cell instanceof BlocksPort);
            }

            visible = visible && ((((((mxCell) cell).getParent() instanceof BlockCell) && context == Context.BLOCKS)
                    ? ((cell instanceof BlocksPort)) : true));

            return visible;
        }

        @Override
        public boolean isCellFoldable(Object cell, boolean collapse) {
            return false;
        }

        @Override
        public Object[] cloneCells(Object[] cells) {
            Object[] o = super.cloneCells(cells);
            if (o.length == 1) {
                if (o[0] instanceof FsmInitStateCell) {
                    o[0] = new FsmStateCell();
                }
                else if (o[0] instanceof FsmStateCell) {
                    ((FsmStateCell) o[0]).setValue(null);
                }
            }
            return o;
        }

        @Override
        public void enterGroup(Object cell) {
            mxGraph graph = GraphEditor.app().getGraph();
            Object[] selected = graph.getSelectionCells();
            if (selected != null) {
                if (selected.length == 1 && selected[0] instanceof BlockCellWithCode) {
                    BlockCellWithCode b = (BlockCellWithCode) selected[0];
                    if (b.isComplete()) {
                        GetCode dialog
                                = new GetCode();
                        dialog.setCode(b.getCode());
                        dialog.setArch(b.getArchitecture());

                        dialog.setModal(true);
                        dialog.setLocationRelativeTo(GraphEditor.app());
                        dialog.setVisible(true);

                        b.setCode(dialog.getCode());
                        b.setArchitecture(dialog.getArch());
                        GraphEditor.app().propertiesPanel.updateBy(b);
                    }
                    else {
                        GraphEditor.app().displayMessage("Block ...", "Fill name of all ports", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return;
                }
                if (selected.length == 1 && selected[0] instanceof BlockCell) {
                    BlockCell b = ((BlockCell) selected[0]);
                    if (b.isComplete()) {
                        showBlockInfo();
                        setFsmContext();
                        GraphEditor.app().infoPanel.updateBy((BlockCell) selected[0]);
                        GraphEditor.app().propertiesPanel.clear();

                        EditorToolBar.fsmContextButton.setEnabled(false);
                        EditorToolBar.blockContextButton.setEnabled(true);
                        GraphEditor.app().libraryPane.setSelectedIndex(1);

                        GraphEditor.app().blocksPalette.setVisible(false);
                        GraphEditor.app().placesPalette.setVisible(true);

                        GraphEditor.app()
                                .getGraphComponent()
                                .getConnectionHandler()
                                .setCreateTarget(true);

                        GraphEditor.app().getUndoManager().clear();
                        super.enterGroup(cell);
                    }
                    else {
                        GraphEditor.app().displayMessage("Block ...", "Fill name of all ports", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

        }

        @Override
        public void exitGroup() {
            setBlockContext();

            EditorToolBar.fsmContextButton.setEnabled(true);
            EditorToolBar.blockContextButton.setEnabled(false);
            GraphEditor.app().propertiesPanel.clear();
            hideBlockInfo();

            GraphEditor.app().blocksPalette.setVisible(true);
            GraphEditor.app().placesPalette.setVisible(false);
            GraphEditor.app().libraryPane.setSelectedIndex(0);

            GraphEditor.app()
                    .getGraphComponent()
                    .getConnectionHandler()
                    .setCreateTarget(false);

            GraphEditor.app().getUndoManager().clear();
            super.exitGroup();
        }

        // Overrides method to store a cell label in the model
        @Override
        public void cellLabelChanged(Object cell, Object newValue,
                boolean autoSize) {

            String value = StringUtils.dropHtmlTags((String) newValue);

            if (newValue != null) {
                if (cell instanceof FsmStateCell || cell instanceof FsmEdgeCell) {
                    super.cellLabelChanged(cell, value, autoSize);
                }
            }

            super.cellLabelChanged(cell, value, autoSize);
        }

        @Override
        public boolean isCellDeletable(Object cell) {
            if (cell instanceof BlocksPort) {
                return ((BlocksPort) cell).isDeleteEnabled();
            }
            return super.isCellDeletable(cell); //To change body of generated methods, choose Tools | Templates.
        }

        /**
         * Stops resizing of parent cell
         *
         * @param cell
         */
        @Override
        public void constrainChild(Object cell) {
        }

        private void hideBlockInfo() {
            GraphEditor.app().rightPanel.setDividerSize(0);
            GraphEditor.app().infoPanel.setVisible(false);
        }

        private void showBlockInfo() {
            GraphEditor.app().infoPanel.setVisible(true);
            GraphEditor.app().rightPanel.setDividerLocation(0.5);
            GraphEditor.app().rightPanel.setResizeWeight(0.5);
            GraphEditor.app().rightPanel.setDividerSize(6);
        }

        @Override
        public boolean isExtendParent(Object cell) {
            return !getModel().isEdge(cell) && isExtendParents() && !(cell instanceof BlockCell);
        }
    }

    public static Object getTopLevelParent(mxCell cell) {
        if (cell.getParent() != null) {
            return getTopLevelParent((mxCell) cell.getParent());
        }
        else {
            return cell;
        }
    }

    public void displayMessage(String title, String msg, int type) {
        JOptionPane.showMessageDialog(this, msg,
                title, type);
    }

    public int displayConfirm(String title, String msg, int type) {
        return JOptionPane.showConfirmDialog(this, msg,
                title, type);
    }

    public void setInitState() {
        if (((CustomGraph) getGraph()).isFsmContext()) {
            getGraph().exitGroup();
        }
    }
}
