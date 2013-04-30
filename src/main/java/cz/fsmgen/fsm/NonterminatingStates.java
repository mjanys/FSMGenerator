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
import cz.fsmgen.gui.cells.FsmFinalStateCell;
import cz.fsmgen.gui.cells.FsmStateCell;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class NonterminatingStates implements HighlightWarnings {

    private final BlockCell fsmBlock;
    private List<FsmStateCell> terminatingStates = new ArrayList();

    public NonterminatingStates(BlockCell fsmBlock) {
        this.fsmBlock = fsmBlock;
    }

    private void spillAlgorithm() {
        if (fsmBlock != null) {
            List<FsmFinalStateCell> finStates = fsmBlock.getFinState();
            if (finStates.isEmpty()) {
                terminatingStates = null;
            }
            else {
                for (FsmFinalStateCell fin : finStates) {
                    spill(fin);
                }
            }
        }
    }

    private void spill(FsmStateCell state) {
        terminatingStates.add(state);
        for (FsmEdgeCell edge : state.getEdgesTo()) {

            mxICell source = edge.getSource();
            if (!terminatingStates.contains(source) && source instanceof FsmStateCell) {
                spill((FsmStateCell) source);
            }
        }
    }

    public void highlight() {
        spillAlgorithm();

        if (terminatingStates != null) {
            for (int i = 0; i < fsmBlock.getChildCount(); i++) {
                mxCell child = (mxCell) fsmBlock.getChildAt(i);
                if (child instanceof FsmStateCell) {
                    if (!terminatingStates.contains((FsmStateCell) child)) {
                        GraphEditor.app().getGraphComponent().setCellWarning(child, "Nonterminating state");
                    }
                }
            }
        }
    }
}
