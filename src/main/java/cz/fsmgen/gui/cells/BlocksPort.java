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
package cz.fsmgen.gui.cells;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import cz.fsmgen.gui.GraphEditor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import jvhdl.datatypes.DataTypeVhdl;
import static jvhdl.datatypes.DataTypeVhdl.Std.*;
import static jvhdl.datatypes.DataTypeVhdl.Direction.*;

/**
 *
 * @author Martin
 */
public abstract class BlocksPort extends CellWithProperties implements Comparable<BlocksPort> {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    static final int PORT_DIAMETER = 15;
    static final int PORT_RADIUS = PORT_DIAMETER / 2;
    protected int site;
    protected DataTypeVhdl.Std type = DataTypeVhdl.Std.STD_LOGIC;
    private boolean deletable = false;
    private int from = 0;
    private int to = 1;
    private DataTypeVhdl.Direction dir = TO;
    private String initValue = "";

    public BlocksPort() {
    }

    public BlocksPort(int site, int serialNum, int count) {
        this.initProperties();
        this.site = site;

        String portStyle = "editable=true";

        double y = getY(serialNum, count);

        mxGeometry geo = createGeometry(site, y);

        if (site == LEFT) {
            this.value = "";
            this.setStyle("align=right;labelPosition=left;" + portStyle);
        }
        else {
            this.value = "";
            this.setStyle("align=left;labelPosition=right;" + portStyle);
        }
//        this.setConnectable(false);
        this.setGeometry(geo);
        this.setVertex(true);

    }

    @Override
    public void initProperties() {
        if (type == STD_LOGIC) {
            propertiesFields = new String[]{"name", "type", "initValue"};
        }
        else if (type == STD_LOGIC_VECTOR) {
            propertiesFields = new String[]{"name", "type", "initValue", "from", "to", "dir"};
        }
        else {
            propertiesFields = new String[]{"name", "type", "initValue"};
        }
    }

    private mxGeometry createGeometry(int site, double y) {
        mxGeometry geo = new mxGeometry(site, y,
                PORT_DIAMETER, PORT_DIAMETER);
        geo.setRelative(true);

        if (site == LEFT) {
            geo.setOffset(new mxPoint(0, -PORT_RADIUS));
        }
        else {
            geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
        }

        return geo;
    }

    private double getY(int serialNum, int count) {
        return (1 / ((double) count + 1)) * serialNum;
    }

    public void updateY(int serialNum, int count) {
        double y = getY(serialNum, count);

        mxGeometry geo = createGeometry(site, y);

        this.setGeometry(geo);
    }

    public abstract DataTypeVhdl getStdLogic();

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isDeleteEnabled() {
        return this.deletable;
    }

    public DataTypeVhdl.Std getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = DataTypeVhdl.Std.valueOf(type.toString());
        this.initProperties();
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = Integer.valueOf(from);
    }

    public int getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = Integer.valueOf(to);
    }

    public DataTypeVhdl.Direction getDir() {
        return dir;
    }

    public void setDir(Object dir) {
        this.dir = DataTypeVhdl.Direction.valueOf(dir.toString());
    }

    public void setName(Object value) {
        if (value != null && this.getValue() != null && !this.getValue().equals(value)) {
            if (getParent() instanceof BlockCell) {
                Collection<BlocksPort> siblings = null;
                if (this instanceof BlocksInputPort) {
                    siblings = ((BlockCell) getParent()).getInputsList();
                }
                else if (this instanceof BlocksOutputPort) {
                    siblings = ((BlockCell) getParent()).getOutputsList();
                }

                if (siblings != null) {
                    for (BlocksPort p : siblings) {
                        if (p.getValue().equals(value)) {
                            GraphEditor.app().displayMessage("Error", "Value already exists!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }

        this.setValue(value);
    }

    public String getName() {
        return (String) this.getValue();
    }

    public String getPortTypeStr() {
        if (site == LEFT) {
            return "IN";
        }
        else {
            return "OUT";
        }
    }

    public int bits() {
        if (type == DataTypeVhdl.Std.STD_LOGIC) {
            return 1;
        }
        else {
            int max = Math.max(Math.abs(from), Math.abs(to));
            int min = Math.min(Math.abs(from), Math.abs(to));
            return max - min + 1;
        }
    }

    public FsmEdgeCell[] getEdges() {
        List<FsmEdgeCell> l = new LinkedList<>();

        mxCell p = (mxCell) getParent();
        if (p != null) {
            for (int i = 0; i < p.getEdgeCount(); i++) {
                mxCell e = (mxCell) p.getEdgeAt(i);
                if (e != null) {
                    if (e.getStyle().contains("sourcePort=" + getId())
                            || e.getStyle().contains("targetPort=" + getId())) {
                        if (e instanceof FsmEdgeCell) {
                            l.add((FsmEdgeCell) e);
                        }
                    }
                }
            }
        }

        return l.toArray(new FsmEdgeCell[0]);
    }

    @Override
    public String toString() {
        return "BlocksPort{" + getValue() + ":"
                + hashCode() + ", "
                + System.identityHashCode(this) + '}';
    }

    @Override
    public int compareTo(BlocksPort o) {
        return getName().compareTo(o.getName());
    }

    public String getInitValue() {
        return initValue;
    }

    public void setInitValue(String defaultValue) {
        this.initValue = defaultValue;
    }
}
