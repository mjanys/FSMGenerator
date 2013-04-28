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
package cz.fsmgen.gui.utils;

import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.PropertiesPanel;
import cz.fsmgen.gui.cells.BlockCell;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin
 */
public class PropertiesUtils {

    public static void setValueFor(Properties cell, String field, Object value) throws Exception {

        try {
            Method setter;
            try {
                setter = cell.getClass().getMethod(
                        "set" + StringUtils.capitalize(field),
                        Object.class);
            }
            catch (NoSuchMethodException e) {
                setter = cell.getClass().getMethod(
                        "set" + StringUtils.capitalize(field),
                        value.getClass());
            }

            setter.invoke(cell, value);
            if (cell instanceof BlockCell) {
                ((BlockCell) cell).update();
                ((BlockCell) cell).update(); // TODO: fix better
            }
        }
        finally {
            GraphEditor.app().getGraph().refresh();
        }
    }

    public static Object getValueFor(Properties cell, String field) throws Exception {

        try {
            Method getter = cell.getClass().getMethod(
                    "get" + StringUtils.capitalize(field));

            return getter.invoke(cell);
        }
        catch (SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PropertiesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
