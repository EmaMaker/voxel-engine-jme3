package mygame.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.block.Cell;
import static mygame.utils.Debugger.debug;

public class FileHelper {

    File f;
    Thread t;

    public void chunkToJson(ArrayList<Cell> al, File f) {
        try {
            Reference.mapper.writeValue(f, al);
        } catch (IOException e) {
            debug(e);
        }
    }

    public ArrayList<Cell> jsonToChunk(File f) {
        try {
            return new ArrayList<>(Arrays.asList(Reference.mapper.readValue(f, Cell[].class)));
            
        } catch (IOException ex) {
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
