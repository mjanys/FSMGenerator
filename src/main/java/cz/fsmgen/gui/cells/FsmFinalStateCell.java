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

/**
 *
 * @author Martin
 */
public class FsmFinalStateCell extends FsmStateCell {

    public FsmFinalStateCell() {
        super();
        this.setStyle("ellipse;shape=doubleEllipse;editable=false;");
    }

    
}