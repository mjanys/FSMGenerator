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

/**
 * Interface for accesing properties.
 * 
 * @author Martin Janyš
 */
public interface Properties {
    
    public static int VALUE = 0;
    
    public void setPropertiesFields(String[] propertiesFields);
    public String[] getPropertiesFields();
    
    public void initProperties();
    
}
