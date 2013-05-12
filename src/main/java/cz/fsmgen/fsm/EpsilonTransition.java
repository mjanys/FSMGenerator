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

/**
 * Class methods highlights empty transitions.
 * 
 * @author Martin Janyš
 */
public class EpsilonTransition implements HighlightWarnings {

    private final BlockCell fsmBlock;

    public EpsilonTransition(BlockCell fsmBlock) {
        this.fsmBlock = fsmBlock;
    }

    @Override
    public void highlight() {
        for (int i = 0; i < fsmBlock.getChildCount(); i++) {
            mxICell child = fsmBlock.getChildAt(i);
            if (child instanceof FsmEdgeCell) {
                FsmEdgeCell e = (FsmEdgeCell) child;
                if (e.input().isEmpty() && e.output().isEmpty()){
                    GraphEditor.app().getGraphComponent().setCellWarning(child, "Empty transition");
                }
            }
        }
    }

}
