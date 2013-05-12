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

import cz.fsmgen.gui.cells.BlockCell;
import cz.fsmgen.gui.cells.BlocksOutputPort;
import cz.fsmgen.gui.cells.BlocksPort;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * Information panel. 
 * Panel contains table with ports and number of bits.
 * 
 * @author Martin Janyš
 */
public class InfoPanel extends PropertiesPanel {

    public InfoPanel() {
        this.setVisible(false);
    }

    @Override
    protected String[] columnNames() {
        return new String[]{
            "Port - type", "Bits"
        };
    }

    void updateBy(BlockCell block) {
        if (block != null) {
            int dimentsion = block.getPortsCount();
            Object[][] dataVector = new Object[dimentsion][2];
            ArrayList<BlocksPort> inputList =
                    new ArrayList<>(block.getInputsList());
            ArrayList<BlocksPort> outputList =
                    new ArrayList(block.getOutputsList());

            for (int i = 0; i < inputList.size(); i++) {
                if (!inputList.get(i).getValue().toString().isEmpty()) {
                    dataVector[i][0] = inputList.get(i).getValue() + " - " + inputList.get(i).getPortTypeStr();
                    dataVector[i][1] = inputList.get(i).bits();
                }
            }

            int j = 0;
            for (int i = inputList.size();
                    i < block.getPortsCount();
                    i++) {
                if (outputList.size() > 0) {
                    if (!outputList.get(j).getValue().toString().isEmpty()) {
                        dataVector[i][0] = outputList.get(j).getValue() + " - " + ((BlocksOutputPort) outputList.get(j)).getOutputType();
                        dataVector[i][1] = outputList.get(j).bits();
                    }
                }
                j++;
            }

            model.setDataVector(dataVector, columnNames());
        }
    }

    @Override
    protected DefaultTableModel defaultTableModel() {
        return new DefaultTableModel(new Object[0][0], columnNames()) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }
}
