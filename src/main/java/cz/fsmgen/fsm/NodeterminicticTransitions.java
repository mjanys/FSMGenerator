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

package cz.fsmgen.fsm;

import com.mxgraph.model.mxICell;
import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.FsmEdgeCell;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class methods highlights nodeterminictic transitions.
 * 
 * @author Martin Janyš
 */
public class NodeterminicticTransitions implements HighlightWarnings {

    private final BlockCell fsmBlock;
    private final List<FsmEdgeCell> edges = new ArrayList<>();
    private final List<String> edgeConds = new ArrayList<>();

    public NodeterminicticTransitions(BlockCell fsmBlock) {
        this.fsmBlock = fsmBlock;
    }

    @Override
    public void highlight() {
        for (int i = 0; i < fsmBlock.getChildCount(); i++) {
            mxICell child = fsmBlock.getChildAt(i);
            if (child instanceof FsmEdgeCell) {
                FsmEdgeCell e = (FsmEdgeCell) child;
                String input = e.input();
//                if (!input.isEmpty()) {
                edges.add(e);
                if (edgeConds.contains(input)) {
                    for (int j = 0; j < edgeConds.size(); j++) {
                        if (edgeConds.get(j).equals(input)) {
                            if (edges.get(j).getSource() == e.getSource()) {
                                GraphEditor.app().getGraphComponent().setCellWarning(e, "Multiple occurrence of transition condition");
                                GraphEditor.app().getGraphComponent().setCellWarning(edges.get(j), "Multiple occurrence of transition condition");
                            }
                        }
                    }
                }
                edgeConds.add(input);
//                }
            }
        }
    }
}
