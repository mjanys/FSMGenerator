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

import cz.jvhdl.datatypes.DataTypeVhdl;
import cz.jvhdl.datatypes.std.StdLogicVectorVhdl;
import cz.jvhdl.datatypes.std.StdLogicVhdl;
import static cz.fsmgen.gui.cells.BlocksOutputPort.Type.*;
import static cz.jvhdl.datatypes.DataTypeVhdl.Std.STD_LOGIC;
import static cz.jvhdl.datatypes.DataTypeVhdl.Std.STD_LOGIC_VECTOR;

/**
 *
 * Class represents output pin/port.
 * 
 * @author Martin Janyš
 */
public class BlocksOutputPort extends BlocksPort implements Cloneable {

    /**
     * Type of output.
     */
    public enum Type {

        MOORE,
        MEALY,
        COMMON
    }

    private Type outputType = MEALY;

    public BlocksOutputPort() {
    }

    public BlocksOutputPort(int serialNum, int count) {
        super(BlocksPort.RIGHT, serialNum, count);
    }

    @Override
    public void initProperties() {
        if (type == STD_LOGIC) {
            propertiesFields = new String[]{"name", "outputType", "type", "initValue"};
        }
        else if (type == STD_LOGIC_VECTOR) {
            propertiesFields = new String[]{"name", "outputType", "type", "initValue", "from", "to", "dir"};
        }
        else {
            propertiesFields = new String[]{"name", "outputType", "type", "initValue"};
        }
    }

    public Type getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = Type.valueOf(outputType);
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    @Override
    public DataTypeVhdl getStdLogic() {
        switch (getType()) {
            case STD_LOGIC:
                return new StdLogicVhdl(DataTypeVhdl.Type.OUT);
            case STD_LOGIC_VECTOR:
                return new StdLogicVectorVhdl(getFrom(), getTo(), getDir(), DataTypeVhdl.Type.OUT);
            default:
                return null;
        }
    }
}
