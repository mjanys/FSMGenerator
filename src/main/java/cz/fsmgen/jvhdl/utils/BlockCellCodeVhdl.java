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
package cz.fsmgen.jvhdl.utils;

import cz.fsmgen.gui.GraphEditor;
import cz.fsmgen.gui.cells.BlockCellWithCode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import cz.jvhdl.VHDL;

/**
 *
 * @author Martin
 */
public class BlockCellCodeVhdl extends BlockCellVhdl {

    private boolean hasCode = false;

    public BlockCellCodeVhdl(BlockCellWithCode cell) {
        super(cell);
        entity.setRst(false);
        entity.setClk(false);
    }

    public BlockCellCodeVhdl(BlockCellWithCode cell, File f) {
        super(cell, f);
        entity.setRst(false);
        entity.setClk(false);
    }

    @Override
    public void writeVhdl() {
        BlockCellWithCode blck = (BlockCellWithCode) this.block;

        if (!blck.getCode().isEmpty()) {

            hasCode = true;
            try {
                File f = new File(file.getAbsoluteFile() + File.separator + blck.getValue() + VHDL.extension);

                if (GraphEditor.testFile(f)) {
                    try (PrintWriter out = new PrintWriter(
                            new FileOutputStream(f))) {
                        out.print(blck.getCode());
                        out.flush();
                    }
                }
            }
            catch (FileNotFoundException ex) {
                Logger.getLogger(BlockCellCodeVhdl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
//        else if (!blck.getArchitecture().isEmpty()) {
            // ports
            block.toEntity(entity);

            Architecture("description", entity.getId());
            {
                Raw(blck.getArchitecture());
            }

            try {
                String name = file.getAbsolutePath() + File.separator + id;
                File f = new File(name + VHDL.extension);
                if (GraphEditor.testFile(f)) {
                    this.write(name);
                }
            }
            catch (FileNotFoundException ex) {
                System.err.println(ex);
            }
        }
    }

    public boolean hasCode() {
        return this.hasCode;
    }
}
