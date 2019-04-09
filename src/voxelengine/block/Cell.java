package voxelengine.block;

import com.jme3.math.Vector3f;
import java.io.Serializable;
import java.util.ArrayList;
import voxelengine.world.Chunk;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.utils.Globals;

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

        //System.out.println("Creating cell at: " + worldX + ", " + worldY + ", " + worldZ + " with id " + id);
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

        if (id != CellId.ID_AIR) {
            sides[0] = (Globals.prov.getCell(worldX - 1, worldY, worldZ) == null || Globals.prov.getCell(worldX - 1, worldY, worldZ).id == CellId.ID_AIR);
            sides[1] = (Globals.prov.getCell(worldX + 1, worldY, worldZ) == null || Globals.prov.getCell(worldX + 1, worldY, worldZ).id == CellId.ID_AIR);
            sides[2] = (Globals.prov.getCell(worldX, worldY, worldZ - 1) == null || Globals.prov.getCell(worldX, worldY, worldZ - 1).id == CellId.ID_AIR);
            sides[3] = (Globals.prov.getCell(worldX, worldY, worldZ + 1) == null || Globals.prov.getCell(worldX, worldY, worldZ + 1).id == CellId.ID_AIR);
            sides[4] = (Globals.prov.getCell(worldX, worldY - 1, worldZ) == null || Globals.prov.getCell(worldX, worldY - 1, worldZ).id == CellId.ID_AIR);

            if (Globals.prov.getCell(worldX, worldY + 1, worldZ) == null || Globals.prov.getCell(worldX, worldY + 1, worldZ).id == CellId.ID_AIR) {
                sides[5] = true;
                if (this.id == CellId.ID_DIRT) {
                    setId(CellId.ID_GRASS);
                }
            } else {
                if (this.id == CellId.ID_GRASS) {
                    setId(CellId.ID_DIRT);
                }
            }

            /*sides[0] = (this.chunk.getCell(x - 1, y, z) == null || this.chunk.getCell(x - 1, y, z).id == CellId.ID_AIR);
            sides[1] = (this.chunk.getCell(x + 1, y, z) == null || this.chunk.getCell(x + 1, y, z).id == CellId.ID_AIR);
            sides[2] = (this.chunk.getCell(x, y, z - 1) == null || this.chunk.getCell(x, y, z - 1).id == CellId.ID_AIR);
            sides[3] = (this.chunk.getCell(x, y, z + 1) == null || this.chunk.getCell(x, y, z + 1).id == CellId.ID_AIR);
            sides[4] = (this.chunk.getCell(x, y - 1, z) == null || this.chunk.getCell(x, y - 1, z).id == CellId.ID_AIR);

            if (Globals.prov.getCell(worldX, worldY + 1, worldZ) == null || Globals.prov.getCell(worldX, worldY + 1, worldZ).id == CellId.ID_AIR) {
                sides[5] = true;
                if (this.id == CellId.ID_DIRT) {
                    setId(CellId.ID_GRASS);
                }
            } else {
                if (this.id == CellId.ID_GRASS) {
                    setId(CellId.ID_DIRT);
                }
            }*/
        }
    }

    public void setId(int id) {
        this.id = id;

        if (id != CellId.ID_AIR) {
            offsets = TextureManager.textures.get(id);
        }
    }

    public byte getFaceFromVertices(ArrayList<Vector3f> al) {
        if (al.get(0).x == al.get(1).x && al.get(0).x == al.get(2).x && al.get(0).x == al.get(3).x) {
            if (al.get(0).x == worldX) {
                return 0;
            }
            return 1;
        } else if (al.get(0).y == al.get(1).y && al.get(0).y == al.get(2).y && al.get(0).y == al.get(3).y) {
            if (al.get(0).y == worldY) {
                return 5;
            }
            return 4;
        } else if (al.get(0).z == al.get(1).z && al.get(0).z == al.get(2).z && al.get(0).z == al.get(3).z) {
            if (al.get(0).z == worldZ) {
                return 2;
            }
            return 3;
        }
        return Byte.MAX_VALUE;
    }
}
