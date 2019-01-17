package voxelengine.block;

import java.io.Serializable;
import voxelengine.utils.math.MathHelper;
import voxelengine.world.Chunk;
import static voxelengine.utils.Reference.chunkSize;
import voxelengine.world.WorldProvider;

public class Cell implements Serializable {

    //keeping track of free sides
    public boolean[] sides = {false, false, false, false, false, false};  //west, east, north, south, top, bottom
    public boolean[] meshed = {false, false, false, false, false, false}; //west, east, north, south, top, bottom
    public int[] offsets = new int[6]; //west, east, north, south, top, bottom

    public Chunk chunk;
    public int x, y, z; //the coords RELATIVE inside the chunk
    public int chunkX, chunkY, chunkZ; // the chunk's coords
    public int worldX, worldY, worldZ; //the ABSOLUTE coords in the world

    public int id;

    public Cell(int id, int x, int y, int z, Chunk c) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.chunkX = c.x;
        this.chunkY = c.y;
        this.chunkZ = c.z;

        this.worldX = chunkX * chunkSize + x;
        this.worldY = chunkY * chunkSize + y;
        this.worldZ = chunkZ * chunkSize + z;

        this.chunk = c;
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

        if (id != CellId.ID_AIR) {
            offsets = TextureManager.textures.get(id);
        }
    }
}
