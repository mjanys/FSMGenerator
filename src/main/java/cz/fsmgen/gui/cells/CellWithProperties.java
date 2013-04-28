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
import cz.fsmgen.gui.utils.Properties;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Martin
 */
public abstract class CellWithProperties extends mxCell implements Properties, Serializable {

    protected static int maxVisibleLen = 30;
    protected static String delim = "/";
    protected static String[] initValues = null;
    protected String[] propertiesFields = null;
    protected Map<String, Object> inout = new HashMap<>();

    @Override
    public void setPropertiesFields(String[] propertiesFields) {
        this.propertiesFields = propertiesFields;
    }

    @Override
    public String[] getPropertiesFields() {
        if (this.propertiesFields == null) {
            initProperties();
        }
        return this.propertiesFields;
    }

    @Override
    public abstract void initProperties();

    public Collection<BlocksPort> getOutputs() {
        if (getParent() instanceof BlockCell) {
            return ((BlockCell) getParent()).getOutputsList();
        }
        return null;
    }

    public Collection<BlocksPort> getInputs() {
        if (getParent() instanceof BlockCell) {
            return ((BlockCell) getParent()).getInputsList();
        }
        return null;
    }

    public Map<String, Object> getInOut() {
        return inout;
    }

    public void setInOut(Map<String, Object> inout) {
        this.inout = inout;
    }

    public void setInout(Map<String, Object> inout) {
        this.inout = inout;
    }

    public BlocksPort getPort(String name) {
        if (getParent() instanceof BlockCell) {
            Collection<BlocksPort> inputsList = ((BlockCell) getParent()).getInputsList();
            for (BlocksPort p : inputsList) {
                if (p.getValue().equals(name)) {
                    return p;
                }
            }
            Collection<BlocksPort> outputList = ((BlockCell) getParent()).getOutputsList();
            for (BlocksPort p : outputList) {
                if (p.getValue().equals(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        CellWithProperties cell = (CellWithProperties) super.clone();
        cell.setInOut(new HashMap<>(getInOut()));
        return cell;
    }
}
