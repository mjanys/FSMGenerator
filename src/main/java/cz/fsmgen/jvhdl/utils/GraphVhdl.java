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
import com.mxgraph.view.mxGraph;
import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.BlockCellWithCode;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import jvhdl.VHDL;

/**
 *
 * @author Martin
 */
public class GraphVhdl {

    public static final String topLevelFilename = "top_level";

    public static Boolean process(mxGraph graph, File f) {
        try {
            List<BlockCellVhdl> entities = new LinkedList<>();
            // file is not selected
            if (f == null) {
                return false;
            }

            mxCell root = (mxCell) graph.getDefaultParent();
            root = (mxCell) GraphEditor.getTopLevelParent(root);

            mxCell parent = (mxCell) root.getChildAt(0);
            
            for (int i = 0; i < parent.getChildCount(); i++) {
                mxCell child = (mxCell) parent.getChildAt(i);

                if (child instanceof BlockCellWithCode) {
                    BlockCellCodeVhdl b = new BlockCellCodeVhdl(
                            (BlockCellWithCode) child, f);
                    if (!b.hasCode()) { // generated entity and inserted arch
                        entities.add(b);
                    }
                    b.output();
                }
                else if (child instanceof BlockCell) {
                    BlockCellVhdl b = new BlockCellVhdl((BlockCell) child, f);
                    entities.add(b);
                    b.output();
                }
            }

            if (entities.size() > 1) {
                topLevel(entities, f);
            }
        }
        catch (Exception ex) {
            GraphEditor.app().displayMessage("Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        return true;
    }

    private static void topLevel(List<BlockCellVhdl> entities, File f) {
        String filename = f.getAbsolutePath()
                + File.separator
                + topLevelFilename;

        if (GraphEditor.testFile(new File(filename
                + VHDL.extension))) {
            try {
                TopLevelVhdl topLevelVhdl = new TopLevelVhdl(entities);
                if (topLevelVhdl.hasEdge()) {
                    topLevelVhdl.write(filename);
                }
            }
            catch (Exception ex) {
                GraphEditor.app().displayMessage("Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
