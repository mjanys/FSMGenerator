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

import com.mxgraph.view.mxGraph;
import cz.fsmgen.gui.GraphEditor;
import javax.swing.SwingWorker;

/**
 *
 * Class is worker which do conversion of graph to VHDL in the background of application.
 * 
 * @author Martin Janyš
 */
public class GraphAnalyzatorWorker extends SwingWorker<Boolean, Void> {

    private final mxGraph graph;

    public GraphAnalyzatorWorker(mxGraph graph) {
        this.graph = graph;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        return GraphVhdl.process(graph, GraphEditor.app().getProjectDirectory());
    }

    @Override
    protected void done() {
        
    }
}
