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
import cz.fsmgen.gui.cells.BlocksPort;
import cz.fsmgen.gui.cells.FsmEdgeCell;
import java.util.*;
import jvhdl.ComponentVhdl;
import jvhdl.EntityVhdl;
import jvhdl.VHDL;
import jvhdl.datatypes.DataTypeVhdl;
import jvhdl.datatypes.SignalVhdl;
import jvhdl.exception.InvalidVhdlTypeException;
import jvhdl.exception.SyntaxErrorVhdl;

/**
 *
 * @author Martin
 */
public class TopLevelVhdl extends VHDL {

    private boolean hasEdge = false;

    public TopLevelVhdl(List<BlockCellVhdl> entities) throws SyntaxErrorVhdl, InvalidVhdlTypeException, Exception {
        Collections.sort(entities, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((BlockCellVhdl) o1).getId()
                        .compareTo(
                        ((BlockCellVhdl) o2).getId());
            }
        });

        Map<String, DataTypeVhdl> entityPorts = new TreeMap<>();
        SignalVhdl tmp = null;

        addLib(UsesAndLibs.IEEE);
        addLib(UsesAndLibs.IEEE_STD_LOGIC);

        Architecture("main", "fpga");
        {
            // declare signals
            tmp = null;
            for (BlockCellVhdl block : entities) {
                EntityVhdl entity = block.getEntity();
                for (String key : entity) {
                    DataTypeVhdl value = entity.get(key);
                    String name = entity.getId() + "_" + key;
                    // ports connected to each other
                    if (block.isGrounded(block.getPortByName(key))) {
                        tmp = Signal(name, value.getUndirected());
                    }
                    else {
                        entityPorts.put(name, value);
                    }
                }

                if (tmp != null) {
                    tmp.commentEnd = "\n";
                }
            }

            // components
            for (BlockCellVhdl block : entities) {
                EntityVhdl entity = block.getEntity();
                if (entity != null) {
                    ComponentVhdl component = Component(entity, true);

                    for (String key : entity) {
                        component.put(key, entity.getId() + "_" + key);
                    }

                    if (tmp != null) {
                        tmp.commentEnd = "\n";
                    }
                }
            }
        }
        // connect to each other
        for (BlockCellVhdl block : entities) {

            for (int i = 0; i < block.getEdgeCount(); i++) {
                mxCell e = (mxCell) block.getEdgeAt(i);
                if (e instanceof FsmEdgeCell) {
                    hasEdge = true;
                    FsmEdgeCell edge = (FsmEdgeCell) e;
                    // only edges thats starts in actual block
                    if (block.getBlock() == edge.getSource()) {
                        BlocksPort targetPort = edge.getTargetPort();
                        BlocksPort sourcePort = edge.getSourcePort();
                        mxICell target = edge.getTarget();
                        mxICell source = edge.getSource();

                        AssignmentToSignal(
                                target.getValue() + "_" + targetPort.getName(),
                                source.getValue() + "_" + sourcePort.getName());
                    }

                }
            }
        }

        EndArchitecture();

        // entity
        Entity("fpga", entityPorts);
    }

    public boolean hasEdge() {
        return hasEdge;
    }
}
