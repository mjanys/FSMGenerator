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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author Martin Janyš
 */
public class FsmEditor extends GraphEditor {

    public FsmEditor() {
        this("FSM generator", new CustomGraphComponent(new CustomGraph()));

        mxResources.add("cz.fsmgen.resources.fsmgen");

    }

    public FsmEditor(String appTitle, mxGraphComponent component) {
        super(appTitle, component);
    }

    @Override
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
        
        // load component palette
        try {
            URL resource = FsmEditor.class.getResource("components.xml");
            File components = new File(resource.toURI());

            if (components.exists()) {
                EditorActions.LoadBlockAction.load(components);
            }
        }
        catch (URISyntaxException ex) {
            Logger.getLogger(FsmEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        editor.repaint();
    }

    public static void main(String[] args) {
        new FsmEditor().show();
    }
}
