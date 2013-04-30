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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.FsmEdgeCell;
import cz.fsmgen.gui.cells.FsmStateCell;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class InaccessibleStates implements HighlightWarnings {

    private final BlockCell fsmBlock;
    private final List<FsmStateCell> avaibleStates = new ArrayList();

    public InaccessibleStates(BlockCell fsmBlock) {
        this.fsmBlock = fsmBlock;
    }

    private void spillAlgorithm() {
        if (fsmBlock != null) {
            spill(fsmBlock.getInitState());
        }
    }

    private void spill(FsmStateCell state) {
        avaibleStates.add(state);
        for (FsmEdgeCell edge : state.getEdgesFrom()) {
           
            mxICell target = edge.getTarget();
            if (!avaibleStates.contains(target) && target instanceof FsmStateCell) {
                spill((FsmStateCell) target);
            }
        }
    }

    public void highlight() {
        spillAlgorithm();
        
        for (int i = 0; i < fsmBlock.getChildCount(); i++) {
            mxCell child = (mxCell) fsmBlock.getChildAt(i);
            if (child instanceof FsmStateCell) {
                if (!avaibleStates.contains((FsmStateCell) child)) {
                    GraphEditor.app().getGraphComponent().setCellWarning(child, "Inaccessible state");
                }
            }
        }
    }
}
