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
package cz.fsmgen.jvhdl.utils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.BlocksPort;
import cz.fsmgen.gui.cells.FsmEdgeCell;
import cz.fsmgen.gui.cells.FsmStateCell;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import javax.swing.JOptionPane;
import cz.jvhdl.*;
import cz.jvhdl.datatypes.*;
import cz.jvhdl.datatypes.std.StdLogicVhdl;
import cz.jvhdl.exception.SyntaxErrorVhdl;
import cz.jvhdl.vdhdlgenerator.VhdlWriter;

/**
 *
 * @author Martin
 */
public class BlockCellVhdl extends VHDL implements VhdlWriter {

    private static final String pstateId = "pstate";
    private static final String nstateId = "nstate";
    protected BlockCell block;
    protected List<FsmStateCell> fsmChilds = new ArrayList<>();
    protected Set<String> undefinedVars = new HashSet<>();
    protected EntityVhdl entity = null;
    protected final File file;

    public BlockCellVhdl(BlockCell cell) {
        this.block = cell;
        this.file = new File(".");
        this.addLib(UsesAndLibs.IEEE);
        this.addLib(UsesAndLibs.IEEE_STD_LOGIC);

        id = block.getValue().toString();
        entity = Entity(id);
        entity.setRst(true);
        entity.setClk(true);
    }

    public BlockCellVhdl(BlockCell cell, File f) {
        this.block = cell;
        this.file = f;
        this.addLib(UsesAndLibs.IEEE);
        this.addLib(UsesAndLibs.IEEE_STD_LOGIC);

        id = block.getValue().toString();
        entity = Entity(id);
        entity.setRst(true);
        entity.setClk(true);
    }

    public void output() {
        this.writeVhdl();
    }

    public boolean isGrounded(BlocksPort p) {
        return block.isGrounded(p);
    }

    @Override
    public void writeVhdl() {
        try {
            // ports
            block.toEntity(entity);

            fsmChilds = block.getFsmChilds();
            // sort states
            Collections.sort(fsmChilds);

            Architecture("behavioral", entity.getId());
            {
                List<String> states = new ArrayList<>();
                List<BlocksPort> mealy = new ArrayList<>(block.getMealyOutputsList());
                List<BlocksPort> moore = new ArrayList<>(block.getMooreOutputsList());

                Collections.sort(mealy);
                Collections.sort(moore);
                // output - present state logic
                presentState();
                // sensitivity list
                List<String> sensitivityList = new ArrayList<>();
                List<String> sList = block.getInputSensitivityList();
                Collections.sort(sList);
                sensitivityList.add(pstateId);
                sensitivityList.addAll(sList);

                // states
                Process("nstate_logic", sensitivityList.toArray());
                {
                    AssignmentToSignal(nstateId, pstateId);
                    NewLine();
                    // INIT
                    for (BlocksPort p : block.getInputsList()) {
                        String val = p.getInitValue();
                        if (val != null && !val.isEmpty()) {
                            AssignmentToSignal(p.getName(), p.getInitValue());
                        }
                    }

                    for (BlocksPort p : block.getOutputsList()) {
                        String val = p.getInitValue();
                        if (val != null && !val.isEmpty()) {
                            AssignmentToSignal(p.getName(), p.getInitValue());
                        }
                    }
                    NewLine();

                    Cases(new SignalVhdl(pstateId, null));
                    {
                        for (mxCell cell : fsmChilds) {

                            if (cell instanceof FsmStateCell) {
                                FsmStateCell state = (FsmStateCell) cell;
                                List<FsmEdgeCell> edges = state.getEdgesFrom();
                                // save state name for declaration
                                if (!states.contains(state.getName())) {
                                    states.add(state.getName());
                                }

                                // if pstate = cell.name
                                if (state.getName() != null && !state.getName().isEmpty()) {
                                    When(state.getName());
//                                    AssignmentToSignal(nstateId, state.getName());
//                                    NewLine();
                                    
                                    moore(moore, state);
                                }
                                else {
                                    GraphEditor.app().getGraph().setSelectionCell(cell);
                                    GraphEditor.app().propertiesPanel.updateBy((cz.fsmgen.gui.utils.Properties) cell);

                                    // error
                                    GraphEditor.app().displayMessage(
                                            "Error",
                                            "Selected cell missing name!",
                                            JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                ConditionVhdl c = null;
                                // iterate edges to with condition to other states
                                List<FsmEdgeCell> directEdges = new ArrayList<>();
                                boolean firstCond = true;

                                // in state - conditions and assig new state
                                for (FsmEdgeCell edge : edges) {
                                    c = new ConditionVhdl(new ExprVhdl(edge.conditionStr()));
                                    setUndefinedVars(edge.getUndefinedVars());

                                    // mealy withou cond
                                    if (!c.isEmpty()) {
                                        if (firstCond) {
                                            If(c);
                                            firstCond = false;
                                        }
                                        else {
                                            Elseif(c);
                                        }
                                        mealy(mealy, edge);
                                        // body of if branch
                                        AssignmentToSignal(nstateId,
                                                ((FsmStateCell) edge.getTarget()).getName());
                                    }
                                    // mealy only out
                                    else if (edge.hasOutput()) {
                                        if (!firstCond) {
                                            Else();
                                        }
                                        // true if
                                        else {
                                            If(new ConditionVhdl(new ExprVhdl("true")));
                                        }
                                        mealy(mealy, edge);
                                        if (firstCond) {
                                            EndIf();
                                        }
                                        AssignmentToSignal(nstateId,
                                                ((FsmStateCell) edge.getTarget()).getName());
                                    }
                                    else {
                                        // withou cond
                                        directEdges.add(edge);
                                    }
                                }
                                // firstCond == has some if
                                if (!firstCond && directEdges.isEmpty()) { // has condition
                                    EndIf(); // cond
                                }
                                // edge without condition
                                if (!directEdges.isEmpty()) {
                                    if (!firstCond) {
                                        Else();
                                    }

                                    for (FsmEdgeCell edge : directEdges) {
                                        AssignmentToSignal(nstateId,
                                                ((FsmStateCell) edge.getTarget()).getName());
                                    }

                                    if (!firstCond) {
                                        EndIf();
                                    }
                                }
                            }
                        }
                        EndIf(); // if pstate = cell.name
                    }
                    WhenOthers();
                    EndCases(); // main case
                }
                EndProcess();
//              Architecture declaration

                EnumVhdl Enum = Enum(new EnumVhdl("states", states.toArray()));
                Signal(pstateId, Enum);
                Signal(nstateId, Enum);

                List<String> l = new ArrayList<>(undefinedVars);
                Collections.sort(l);

                for (String var : l) {
                    Signal(var, new StdLogicVhdl(), "0");
                }
            }
            EndArchitecture();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String name = file.getAbsolutePath() + File.separator + id;
            File f = new File(name + VHDL.extension);
            if (GraphEditor.testFile(f)) {
                this.write(name);
            }
        }
        catch (FileNotFoundException ex) {
            System.err.println(ex);
        }
    }

    private void presentState() throws SyntaxErrorVhdl, Exception {

//          --Present State register 
//          pstatereg: process(RST, CLK) begin 
//           if (RST='1') then 
//               pstate <= S0; 
//           elsif (CLK'event) and (CLK='1') then
//               pstate <= nstate;
//           end if; 
//          end process;
        Process("pstatereg", new String[]{RST, CLK});
        {
            If(new ConditionVhdl(new ExprVhdl(
                    RST + " = '1'")));
            if (!block.getInitStateName().isEmpty()) {
                Assignment(new SignalVhdl(pstateId, null), block.getInitStateName());
            }
            Elseif(new ConditionVhdl(new ExprVhdl(
                    "(" + CLK + "'event) and (" + CLK + " = '1')")));
            Assignment(new SignalVhdl(pstateId, null), nstateId);
            EndIf();
        }
        EndProcess();
    }

    private void mealy(List<BlocksPort> mealy, FsmEdgeCell edge) throws SyntaxErrorVhdl, Exception {
        for (BlocksPort port : mealy) {
            Object o = edge.getInOut().get(port.getName());

            if (o != null && !o.toString().isEmpty()) {
                AssignmentToSignal(port.getName(), setUndefinedVars(o.toString()));
            }
//            else if (port.getDefaultValue() != null && !port.getDefaultValue().isEmpty()) {
//                AssignmentToSignal(port.getName(), port.getDefaultValue());
//            }
        }
    }

    private void moore(List<BlocksPort> moore, FsmStateCell state) throws SyntaxErrorVhdl, Exception {
        for (BlocksPort port : moore) {
            Object o = state.getInOut().get(port.getName());

            if (o != null && !o.toString().isEmpty()) {
                AssignmentToSignal(port.getName(), setUndefinedVars(o.toString()));
            }
//            else if (port.getDefaultValue() != null && !port.getDefaultValue().isEmpty()) {
//                AssignmentToSignal(port.getName(), port.getDefaultValue());
//            }
        }
    }

    private void setUndefinedVars(Set<String> undefinedVars) {
        this.undefinedVars.addAll(undefinedVars);
    }

    private String setUndefinedVars(String var) {
        if (var != null && !var.isEmpty()) {
            if (VHDL.identifierPattern.matcher(var).matches()) {
                undefinedVars.add(var);
            }
        }

        return var;
    }

    public EntityVhdl getEntity() {
        return entity;
    }

    public BlocksPort getPortByName(String name) {
        return block.getPortByName(name);
    }

    public int getEdgeCount() {
        return block.getEdgeCount();
    }

    public mxICell getEdgeAt(int index) {
        return block.getEdgeAt(index);
    }

    public mxICell blockGetParent() {
        return block.getParent();
    }

    mxICell getBlock() {
        return this.block;
    }
}
