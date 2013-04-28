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

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;
import cz.fsmgen.gui.GraphEditor;
import static cz.fsmgen.gui.cells.CellWithProperties.initValues;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static cz.fsmgen.gui.cells.BlocksOutputPort.Type.*;
import cz.jvhdl.EntityVhdl;
import cz.jvhdl.datatypes.DataTypeVhdl;

/**
 *
 * @author Martin
 */
public class BlockCell extends CellWithProperties {

    public static final int INPUTS = 1;
    public static final int OUTPUTS = 2;
    private int inputs;
    private int outputs;
    private transient mxGraph graph;
    private List<BlocksPort> inputsList = new ArrayList<>();
    private List<BlocksPort> outputsList = new ArrayList<>();
    private List<FsmStateCell> fsmChilds = new ArrayList<>();

    static {
        mxCodecRegistry.addPackage("fsmgenerator.gui.cells");

        mxCodecRegistry.register(new mxObjectCodec(
                new BlockCell()));
//        mxCodecRegistry.register(new mxObjectCodec(
//                new BlockCell().getGeometry()));
        mxCodecRegistry.register(new mxObjectCodec(
                new BlocksInputPort(1, 1)));
        mxCodecRegistry.register(new mxObjectCodec(
                new BlocksOutputPort(1, 1)));
        mxCodecRegistry.register(new mxObjectCodec(
                new FsmEdgeCell("")));
        mxCodecRegistry.register(new mxObjectCodec(
                new FsmStateCell()));
    }

    public BlockCell() {
        this.setVertex(true);
        this.setStyle("editable=false;resizable=false;editable=true");
        this.setConnectable(false);

        mxGeometry geo = new mxGeometry();

        this.setGeometry(geo);
    }

    public BlockCell(final mxGraph graph, String value, int inputs, int outputs) {
        this();
        this.inputs = inputs;
        this.outputs = outputs;

        this.value = value;
        this.graph = graph;

        initProperties();

        int max = Math.max(getInputsCount(), getOutputsCount());
        double h = (((BlocksPort.PORT_DIAMETER + 10) * max) * 1.2);
        geometry.setHeight(h);
        geometry.setWidth(150);

        createInputs();
        createOutputs();
    }

    public void createInputs() {
        for (int i = 1; i <= inputs; i++) {
            addInput(i, inputs);
        }
    }

    public void createOutputs() {
        for (int i = 1; i <= outputs; i++) {
            addOutput(i, outputs);
        }
    }

    private BlocksPort addInput(int i, int inputs) {
        graph = GraphEditor.app().getGraph();
        BlocksPort port = new BlocksInputPort(i, inputs);
//        inputsList.add(port);
        graph.addCell(port, this);
        return port;
    }

    private BlocksPort addOutput(int i, int outputs) {
        graph = GraphEditor.app().getGraph();
        BlocksPort port = new BlocksOutputPort(i, outputs);
//        outputsList.add(port);
        graph.addCell(port, this);
        return port;
    }

    public int getInputsCount() {
        return inputs;
    }

    public void setInputsCount(String inputs) {
        this.inputs = Integer.valueOf(inputs);
    }

    public int getOutputsCount() {
        return outputs;
    }

    public void setOutputsCount(String s) {
        this.outputs = Integer.valueOf(s);
    }

    public static String[] getInitValues() {
        if (initValues == null) {
            initValues = new String[]{"Block", "4", "2", "MEALY"};
        }
        return initValues;
    }

    public static String getInitAt(int i) {
        return getInitValues()[i];
    }

    @Override
    public void initProperties() {
        propertiesFields = new String[]{"value", "inputsCount", "outputsCount"};
    }

    public void update() {
        int count = getChildCount();
        List<Object> in = new ArrayList<>();
        List<Object> out = new ArrayList<>();

        // filtre pins
        for (int i = 0; i < count; i++) {
            Object child = this.getChildAt(i);
            if (child instanceof BlocksPort) {
                ((BlocksPort) child).setDeletable(true);
                if (((BlocksPort) child).site == BlocksPort.LEFT) {
                    in.add(child);
                }
                else if (((BlocksPort) child).site == BlocksPort.RIGHT) {
                    out.add(child);
                }
            }
        }

        int newInCount = this.inputs;
        int oldInCount = in.size();

        int newOutCount = this.outputs;
        int oldOutCount = out.size();

//        System.out.println("newInCount = " + this.inputs + "\n"
//                + "oldInCount = " + in.size() + "\n"
//                + "newOutCount = " + this.outputs + "\n"
//                + "oldOutCount = " + out.size());
        List<Object> inHead = null;
        List<Object> outHead = null;

        // remove pins
        if (newInCount < oldInCount) {
            inHead = in.subList(0, newInCount);
            List<Object> inRest = in.subList(newInCount, in.size());

            GraphEditor.app().getGraph().removeCells(
                    inRest.toArray());
        }
        // add pins
        else {
            inHead = in;
            for (int i = oldInCount; i < newInCount; i++) {
                inHead.add(addInput(i, newInCount));
            }
        }

        // remove pins
        if (newOutCount < oldOutCount) {
            outHead = out.subList(0, newOutCount);
            List<Object> outRest = out.subList(newOutCount, out.size());

            GraphEditor.app().getGraph().removeCells(
                    outRest.toArray());
        }
        // add pins
        else {
            outHead = out;
            for (int i = oldOutCount; i < newOutCount; i++) {
                outHead.add(addOutput(i, newOutCount));
            }
        }

        // move
        int i = 1;
        for (Object o : inHead) {
            ((BlocksPort) o).updateY(i, inHead.size());
            ((BlocksPort) o).setDeletable(false);
            i++;
        }

        // move
        i = 1;
        for (Object o : outHead) {
            ((BlocksPort) o).updateY(i, outHead.size());
            ((BlocksPort) o).setDeletable(false);
            i++;
        }

        // heigth of wrapper
        updateHeight();
        GraphEditor.app().getGraph().refresh();
        GraphEditor.app().getGraph().clearSelection();
        GraphEditor.app().getGraph().setSelectionCell(this);
        createPortsList();
    }

    private void updateHeight() {
        int max = Math.max(getInputsCount(), getOutputsCount());
        if (max == 0) {
            max = 1;
        }
        double h = (((BlocksPort.PORT_DIAMETER + 10) * max) * 1.2);

        mxGeometry g = getGeometry();
        g.setHeight(h);

    }

    public mxGraph getGraph() {
        return graph;
    }

    public void setGraph(mxGraph graph) {
        this.graph = graph;
    }

    public Collection<BlocksPort> getInputsList() {
        return inputsList;
    }

    public Collection<BlocksPort> getOutputsList() {
        return outputsList;
    }

    public List<String> getInputSensitivityList() {
        List<String> l = new ArrayList<>(inputsList.size());
        for (BlocksPort port : inputsList) {
            String name = port.getName();
            if (name != null && !name.isEmpty()) {
                l.add(name);
            }
        }
        return l;
    }

    public Set<BlocksPort> getMooreOutputsList() {
        Set<BlocksPort> set = new LinkedHashSet<>();
        for (BlocksPort blocksPort : getOutputsList()) {
            if (blocksPort instanceof BlocksOutputPort) {
                BlocksOutputPort o = (BlocksOutputPort) blocksPort;
                if (o.getOutputType() == MOORE || o.getOutputType() == COMMON) {
                    set.add(blocksPort);
                }
            }
        }
        return set;
    }

    public Set<BlocksPort> getMealyOutputsList() {
        Set<BlocksPort> set = new LinkedHashSet<>();
        for (BlocksPort blocksPort : getOutputsList()) {
            if (blocksPort instanceof BlocksOutputPort) {
                BlocksOutputPort o = (BlocksOutputPort) blocksPort;
                if (o.getOutputType() == MEALY || o.getOutputType() == COMMON) {
                    set.add(blocksPort);
                }
            }
        }
        return set;
    }

    public int getPortsCount() {
        return inputs + outputs;
    }

    public BlocksPort getInputPort() {
        return this.getInputPort("");
    }

    public BlocksPort getInputPort(String value) {

        for (BlocksPort blocksPort : inputsList) {
            if (blocksPort.getValue().equals(value)) {
                return blocksPort;
            }
        }
        return null;
    }

    public BlocksPort getOutputPort() {
        return this.getOutputPort("");
    }

    public BlocksPort getOutputPort(String value) {

        for (BlocksPort blocksPort : outputsList) {
            if (blocksPort.getValue().equals(value)) {
                return blocksPort;
            }
        }

        return null;
    }

    public void updatesPortNames(List<BlocksPort> inputPorts, List<BlocksPort> outputPorts) {
        int i = 0, j = 0;
        for (int k = 0; k < getChildCount(); k++) {
            mxCell child = (mxCell) getChildAt(k);
            if (child instanceof BlocksInputPort) {
                child.setValue(inputPorts.get(i).getValue());
                i++;
            }
            else if (child instanceof BlocksOutputPort) {
                child.setValue(outputPorts.get(j).getValue());
                j++;
            }
        }

        this.update();
    }

    public void createPortsList() {
        inputsList.clear();
        outputsList.clear();
        for (int i = 0; i < getChildCount(); i++) {
            mxICell child = getChildAt(i);
            if (child instanceof BlocksInputPort) {
                inputsList.add((BlocksPort) child);
            }
            else if (child instanceof BlocksOutputPort) {
                outputsList.add((BlocksPort) child);
            }
        }
    }

    public String getInitState() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof FsmInitStateCell) {
                return ((FsmInitStateCell) getChildAt(i)).getName();
            }
        }
        return "";
    }

    public void toEntity(EntityVhdl e) {
        fsmChilds.clear();
        for (int i = 0; i < getChildCount(); i++) {
            mxCell child = (mxCell) getChildAt(i);
            if (child instanceof BlocksPort) {
                BlocksPort p = (BlocksPort) child;
                try {
                    DataTypeVhdl var = p.getStdLogic();
                    e.put(p.getValue().toString(), var);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            else {
                if (child instanceof FsmStateCell) {
                    fsmChilds.add((FsmStateCell) child);
                }
            }
        }
    }

    public List<FsmStateCell> getFsmChilds() {
        return fsmChilds;
    }

    public boolean isGrounded(BlocksPort p) {
        if (p == null) {
            return true; // CLK || RST
        }
        return p.getEdges().length > 0;
    }

    public BlocksPort getPortByName(String name) {

        for (BlocksPort port : inputsList) {
            if (port.getName().equals(name)) {
                return port;
            }
        }

        for (BlocksPort port : outputsList) {
            if (port.getName().equals(name)) {
                return port;
            }
        }

        return null;
    }

    public boolean isComplete() {
        createPortsList();
        return getPortByName("") == null;
    }
}