/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author Martin
 */
public class DeterminicticTransition implements HighlightWarnings {

    private final BlockCell fsmBlock;
    private final List<FsmEdgeCell> edges = new ArrayList<>();
    private final List<String> edgeConds = new ArrayList<>();

    public DeterminicticTransition(BlockCell fsmBlock) {
        this.fsmBlock = fsmBlock;
    }

    @Override
    public void highlight() {
        for (int i = 0; i < fsmBlock.getChildCount(); i++) {
            mxICell child = fsmBlock.getChildAt(i);
            if (child instanceof FsmEdgeCell) {
                FsmEdgeCell e = (FsmEdgeCell) child;
                String input = e.input();
                if (!input.isEmpty()) {
                    edges.add(e);
                    if (edgeConds.contains(input)) {
                        int index = edges.indexOf(e);
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
                }
            }
        }
    }
}
