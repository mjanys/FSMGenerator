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

import com.mxgraph.model.mxGeometry;
import static cz.fsmgen.gui.cells.BlockCell.getInitValues;
import static cz.fsmgen.gui.cells.CellWithProperties.initValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Martin
 */
public class FsmStateCell extends CellWithProperties implements Comparable<FsmStateCell> {

    private String name = "";
    public long counter = 0;

    public FsmStateCell() {
        super();
        initProperties();

        this.setGeometry(new mxGeometry(0, 0, 50, 50));
        this.setStyle("ellipse;editable=false");
        this.setVertex(true);
    }

    public static String[] getInitValues() {
        if (initValues == null) {
            initValues = new String[]{"State", ""};
        }
        return initValues;
    }

    public static String getInitAt(int i) {
        return getInitValues()[i];
    }

    @Override
    public void initProperties() {
        propertiesFields = new String[]{/*"value",*/"name", "outputs"};
    }

    @Override
    public String[] getPropertiesFields() {
        if (getParent() != null && getParent() instanceof BlockCell) {
            propertiesFields = new String[]{/*"value",*/"name", "outputs"};
        }
        return this.propertiesFields;
    }

    public String getName() {
        return name.replaceAll("\\s+", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getValue() {
        StringBuilder sb = new StringBuilder();

        if (getParent() instanceof BlockCell) {
            BlockCell p = (BlockCell) getParent();
            Set<BlocksPort> moore = p.getMooreOutputsList();

            if (!moore.isEmpty()) {
                boolean hasOtputs = false;
                sb.append("<hr>");
                Map<String, Object> inOut = this.getInOut();
                for (BlocksPort port : moore) {
                    Object o = inOut.get(port.getName());
                    if (o != null && !o.toString().isEmpty()) {
                        hasOtputs = true;
                        sb.append(o);
                    }
                    else {
                        sb.append("X");
                    }
                    sb.append(";");
                }
                int index = sb.lastIndexOf(";");
                if (index > 0) {
                    sb.deleteCharAt(index);
                }

                if (!hasOtputs) {
                    sb = new StringBuilder();
                }
            }
        }

        if (sb.toString().equals("<hr>")) {
            return name;
        }

        return name + sb.toString();
    }

    public List<FsmEdgeCell> getEdgesFrom() {
        List<FsmEdgeCell> edgeList = new ArrayList<>();

        for (int i = 0; i < getEdgeCount(); i++) {
            if (getEdgeAt(i) instanceof FsmEdgeCell) {
                FsmEdgeCell e = (FsmEdgeCell) getEdgeAt(i);
                if (e.getSource() == this) {
                    edgeList.add(e);
                }
            }
        }

        return edgeList;
    }

    public List<FsmEdgeCell> getEdgesTo() {
        List<FsmEdgeCell> edgeList = new ArrayList<>();

        for (int i = 0; i < getEdgeCount(); i++) {
            if (getEdgeAt(i) instanceof FsmEdgeCell) {
                FsmEdgeCell e = (FsmEdgeCell) getEdgeAt(i);
                if (e.getTarget() == this) {
                    edgeList.add(e);
                }
            }
        }

        return edgeList;
    }

    @Override
    public int compareTo(FsmStateCell o) {

        if ((this instanceof FsmInitStateCell && o instanceof FsmStateCell)
                || (this instanceof FsmStateCell && o instanceof FsmFinalStateCell)) {
            return Integer.MIN_VALUE;
        }
        else if ((this instanceof FsmFinalStateCell && o instanceof FsmStateCell)
                || (this instanceof FsmStateCell && o instanceof FsmInitStateCell)) {
            return Integer.MAX_VALUE;
        }
        else if ((this instanceof FsmInitStateCell && o instanceof FsmFinalStateCell)
                || (this instanceof FsmFinalStateCell && o instanceof FsmInitStateCell)) {
            return Integer.MIN_VALUE;
        }
        else {
            return this.getName().compareTo(o.getName());
        }
    }
}
