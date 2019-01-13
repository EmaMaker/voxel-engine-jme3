package mygame.block;

import java.io.Serializable;
import mygame.utils.MathHelper;
import mygame.world.Chunk;
import static mygame.utils.Reference.chunkSize;
import mygame.world.WorldProvider;

public class Cell implements Serializable {

    //keeping track of free sides
    public transient boolean[] sides = {false, false, false, false, false, false};  //west, east, north, south, top, bottom
    public transient boolean[] meshed = {false, false, false, false, false, false}; //west, east, north, south, top, bottom
    public transient int[] offsets = new int[6]; //west, east, north, south, top, bottom

    public transient Chunk chunk;
    public int x, y, z; //the coords RELATIVE inside the chunk
    public int chunkX, chunkY, chunkZ; // the chunk's coords
    public int worldX, worldY, worldZ; //the ABSOLUTE coords in the world

    public int id;

    public Cell(int id, int x, int y, int z, int chunkX, int chunkY, int chunkZ) {
        this(id, x, y, z, chunkX, chunkY, chunkZ, (chunkX * chunkSize) + x, (chunkY * chunkSize) + y, (chunkZ * chunkSize) + z);
    }

    public Cell(int id, int x, int y, int z, int chunkX, int chunkY, int chunkZ, int worldX, int worldY, int worldZ) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;

        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        this.chunk = WorldProvider.chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)];
        setId(id);
    }

    public synchronized void update() {

        sides[0] = false;
        sides[1] = false;
        sides[2] = false;
        sides[3] = false;
        sides[4] = false;
        sides[5] = false;
        meshed[0] = false;
        meshed[1] = false;
        meshed[2] = false;
        meshed[3] = false;
        meshed[4] = false;
        meshed[5] = false;

        //est free
        if (this.chunk.getCell(x + 1, y, z) == null || this.chunk.getCell(x + 1, y, z).id == CellId.ID_AIR) {
            sides[0] = true;
        }

        //sides[0] free
        if (this.chunk.getCell(x - 1, y, z) == null || this.chunk.getCell(x - 1, y, z).id == CellId.ID_AIR) {
            sides[1] = true;
        }

        //sides[2] free
        if (this.chunk.getCell(x, y, z + 1) == null || this.chunk.getCell(x, y, z + 1).id == CellId.ID_AIR) {
            sides[2] = true;
        }

        //sides[3] free
        if (this.chunk.getCell(x, y, z - 1) == null || this.chunk.getCell(x, y, z - 1).id == CellId.ID_AIR) {
            sides[3] = true;
        }

        //sides[4] free
        if (this.chunk.getCell(x, y + 1, z) == null || this.chunk.getCell(x, y + 1, z).id == CellId.ID_AIR) {
            sides[4] = true;
            if (this.id == CellId.ID_DIRT) {
                setId(CellId.ID_GRASS);
            }
        } else {
            if (this.id == CellId.ID_GRASS) {
                setId(CellId.ID_DIRT);
            }
        }

        //Bottom free
        if (this.chunk.getCell(x, y - 1, z) == null || this.chunk.getCell(x, y - 1, z).id == CellId.ID_AIR) {
            sides[5] = true;
        }
    }

    public void setId(int id) {
        this.id = id;
        
        offsets = TextureManager.textures.get(id);
    }
}
