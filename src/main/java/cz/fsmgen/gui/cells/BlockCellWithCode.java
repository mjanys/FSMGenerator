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
package cz.fsmgen.gui.cells;

import com.mxgraph.view.mxGraph;

/**
 *
 * @author Martin
 */
public class BlockCellWithCode extends BlockCell {

    private String code = "";
    private String architecture ="";
    
    public BlockCellWithCode() {
        super();
    }

    public BlockCellWithCode(String code) {
        super();
        this.code = code;
    }

    public BlockCellWithCode(mxGraph graph, String value, int inputs, int outputs, String code) {
        super(graph, value, inputs, outputs);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getArchitecture() {
        return architecture;
    }    
    
}
