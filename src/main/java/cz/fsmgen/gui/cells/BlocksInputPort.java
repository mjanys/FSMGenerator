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

import jvhdl.datatypes.DataTypeVhdl;
import static jvhdl.datatypes.DataTypeVhdl.Std.STD_LOGIC;
import static jvhdl.datatypes.DataTypeVhdl.Std.STD_LOGIC_VECTOR;
import jvhdl.datatypes.std.StdLogicVectorVhdl;
import jvhdl.datatypes.std.StdLogicVhdl;

/**
 *
 * @author Martin
 */
public class BlocksInputPort extends BlocksPort {

    public BlocksInputPort() {
    }

    public BlocksInputPort(int serialNum, int count) {
        super(BlocksPort.LEFT, serialNum, count);
    }

    @Override
    public DataTypeVhdl getStdLogic() {
        switch (getType()) {
            case STD_LOGIC:
                return new StdLogicVhdl(DataTypeVhdl.Type.IN);
            case STD_LOGIC_VECTOR:
                return new StdLogicVectorVhdl(getFrom(), getTo(), getDir(), DataTypeVhdl.Type.IN);
            default:
                return null;
        }
    }
}
