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

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import cz.fsmgen.gui.cells.BlocksOutputPort;
import cz.fsmgen.gui.cells.BlocksPort;
import cz.fsmgen.gui.cells.CellWithProperties;
import cz.fsmgen.gui.cells.FsmEdgeCell;
import cz.fsmgen.gui.cells.FsmStateCell;
import cz.fsmgen.gui.utils.Properties;
import cz.fsmgen.gui.utils.PropertiesUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.*;
import javax.swing.JCheckBox;
import static cz.fsmgen.gui.cells.BlocksOutputPort.Type.*;

/**
 *
 * @author Martin
 */
public class PropertiesPanel extends javax.swing.JPanel {
    
    Set<String> info = new HashSet<>(Arrays.asList(
            new String[]{"code", "architecture"}));
    private Properties currentInstance = null;
    protected final JTable customTable = new JTable() {
        /**
         * Combo box table cell
         */
        @Override
        public TableCellEditor getCellEditor(final int row, final int column) {

            if (column == 1) {
                final String[] fields = currentInstance.getPropertiesFields();
                if (row < fields.length) {
                    Object tableCell = getValueFor(currentInstance, fields[row]);

                    if (tableCell.getClass().isEnum()) {

                        List<String> l = new ArrayList<>();
//                    l.add("");
                        for (Field f : tableCell.getClass().getDeclaredFields()) {
                            if (f.isEnumConstant()) {
                                l.add(f.getName().toString());
                            }
                        }
                        JComboBox comboBox = new JComboBox(l.toArray());
                        comboBox.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                Object selected = ((JComboBox) e.getSource()).getSelectedItem();
                                if (selected != null && !selected.toString().isEmpty()) {
                                    try {
                                        setValueFor(currentInstance, table.getModel().getValueAt(row, 0).toString(), selected);
                                        updateBy(currentInstance);
                                    }
                                    catch (Exception ex) {
                                        Logger.getLogger(PropertiesPanel.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        });
                        return new DefaultCellEditor(comboBox);
                    }
                    else if (info.contains(fields[row])) {
                        boolean selected = false;
                        if (tableCell instanceof String) {
                            selected = !((String)tableCell).isEmpty();
                        }
                        
                        JCheckBox box = new JCheckBox("", selected);
                        box.setEnabled(selected);
                        return new DefaultCellEditor(box);
                    }
                }
            }

            return super.getCellEditor(row, column);
        }
    };
    public DefaultTableModel model = defaultTableModel();

    /**
     * Creates new form PropertiesPanel
     */
    public PropertiesPanel() {
        initComponents();
        customTable.setRowHeight(20);
        customTable.putClientProperty("terminateEditOnFocusLost", true);

        table.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {
            private Object field = null;

            @Override
            public Component getTableCellEditorComponent(JTable table,
                    Object value, boolean isSelected, int row, int column) {

                // get selected value label
                field = model.getValueAt(row, column - 1);

                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public Object getCellEditorValue() {
                try {
                    setValueFor(currentInstance, (String) field, super.getCellEditorValue());
                }
                catch (Throwable e) {
                    return getValueFor(currentInstance, (String) field);
                }
                return super.getCellEditorValue();
            }
        });
    }

    private void setValueFor(Properties currentInstance, String field, Object cellEditorValue) {
        try {
            PropertiesUtils.setValueFor(currentInstance, field, cellEditorValue);
        }
        catch (Exception e) {
            if (currentInstance instanceof CellWithProperties) {
                CellWithProperties cell = (CellWithProperties) currentInstance;
                BlocksPort p = cell.getPort(field);
                cell.getInOut().put(p.getName(), cellEditorValue);
                GraphEditor.app().getGraph().refresh();
            }
        }
    }

    private Object getValueFor(Properties currentInstance, String string) {
        try {
            return PropertiesUtils.getValueFor(currentInstance, string);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = customTable
        ;

        setLayout(new java.awt.CardLayout());

        table.setModel(this.model);
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        add(jPanel1, "card2");
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    public String get(String key) {
        for (int i = 0; i < this.model.getRowCount(); i++) {
            if (this.model.getValueAt(i, 0).equals(key)) {
                return this.model.getValueAt(i, 1).toString();
            }
        }

        throw new RuntimeException("Key not found");
    }

    public void updateBy(Properties cell) {
        int selected = table.getSelectedRow();
        currentInstance = cell;

        String[] fields = cell.getPropertiesFields();

        List<List<Object>> dataVector = new ArrayList<>();

        for (int i = 0; i < fields.length; i++) {
            Object value = getValueFor(cell, fields[i]);
            if (value instanceof Collection) {
                // add ports
                for (Object p : ((Collection) value)) {
                    if (p instanceof BlocksPort) {
                        BlocksPort port = (BlocksPort) p;

                        if (port instanceof BlocksOutputPort) {
                            BlocksOutputPort out = (BlocksOutputPort) port;
                            // filter mealy/moore
                            if ((cell instanceof FsmStateCell && out.getOutputType() == MEALY)) {
                                continue;
                            }
                            if ((cell instanceof FsmEdgeCell && out.getOutputType() == MOORE)) {
                                continue;
                            }
                        }

                        List<Object> l = new ArrayList<>();
                        l.add(port.getValue());
                        l.add(((CellWithProperties) cell).getInOut().get(port.getName()));
                        dataVector.add(l);

                    }
                }
            }
            else {
                List<Object> l = new ArrayList<>();
                l.add(fields[i]);
                l.add(value);
                dataVector.add(l);
            }
        }

        Object[][] o = new Object[dataVector.size()][];
        int i = 0;
        for (List<Object> l : dataVector) {
            o[i] = l.toArray();
            i++;
        }

        model.setDataVector(o, columnNames());
        model.fireTableDataChanged();
        if (selected > 0) {
            selected = Math.min(selected, table.getRowCount() - 1);
            table.setRowSelectionInterval(selected, selected);
        }
        table.repaint();
    }

    public void clear() {
        currentInstance = null;
        model.setDataVector(new Object[0][0], columnNames());
        model.fireTableDataChanged();
        table.repaint();
    }

    public static final mxGraph getGraph(ActionEvent e) {
        Object source = e.getSource();

        if (source instanceof mxGraphComponent) {
            return ((mxGraphComponent) source).getGraph();
        }

        return null;
    }

    protected String[] columnNames() {
        return new String[]{
            "Properties", ""
        };
    }

    protected DefaultTableModel defaultTableModel() {
        return new DefaultTableModel(
                new Object[0][0], columnNames()) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, true
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
