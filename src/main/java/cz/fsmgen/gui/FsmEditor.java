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
package cz.fsmgen.gui;

import com.mxgraph.swing.editor.EditorActions;
import com.mxgraph.swing.editor.EditorMenuBar;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import java.awt.Color;
import java.io.File;
import javax.swing.UIManager;

/**
 *
 * @author Martin
 */
public class FsmEditor extends GraphEditor {

    public FsmEditor() {
        this("FSM generator", new CustomGraphComponent(new CustomGraph()));

        mxResources.add("resources.fsmgen");

    }

    public FsmEditor(String appTitle, mxGraphComponent component) {
        super(appTitle, component);
    }

    public void show() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }

        mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
        mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

        FsmEditor editor = new FsmEditor();
        editor.createFrame(new EditorMenuBar(editor)).setVisible(true);

//        try {
//            XmlUtils.Xml2Graph((CustomGraph) editor.getGraph(), new File("components.xml"));
//            editor.getGraphComponent().getGraph().clearSelection();
//            GraphEditor.app().getGraph().refresh();
//        }
//        catch (Exception ex) {
//            Logger.getLogger(GraphEditor.class.getName()).log(Level.SEVERE, null, ex);
//        }

        // load component palette
        File components = new File("./lib/components.xml");
        if (components.exists()) {
            EditorActions.LoadBlockAction.load(components);
        }
    }

    public static void main(String[] args) {
        new FsmEditor().show();
    }
}
