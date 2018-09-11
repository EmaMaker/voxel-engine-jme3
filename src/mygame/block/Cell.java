package mygame.block;

import mygame.world.Chunk;
import static mygame.utils.Reference.chunkSize;import mygame.world.WorldProvider;
;

public class Cell {

    //keeping track of free sides
    public boolean[] sides = {false, false, false, false, false, false};  //west, east, north, south, top, bottom
    public boolean[] meshed = {false, false, false, false, false, false}; //west, east, north, south, top, bottom
    public int[] offsets = new int[6]; //west, east, north, south, top, bottom

    public transient Chunk chunk;
    public int x, y, z; //the coords RELATIVE inside the chunk
    public int chunkX, chunkY, chunkZ; // the chunk's coords
    public int worldX, worldY, worldZ; //the ABSOLUTE coords in the world

    public CellId id;

    public Cell(CellId id, int x, int y, int z, int chunkX, int chunkY, int chunkZ) {
        this(id, x, y, z, chunkX, chunkY, chunkZ, (chunkX * chunkSize) + x, (chunkY * chunkSize) + y, (chunkZ * chunkSize) + z);
    }

    public Cell(CellId id, int x, int y, int z, int chunkX, int chunkY, int chunkZ, int worldX, int worldY, int worldZ) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;

        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        this.chunk = WorldProvider.chunks[chunkX][chunkY][chunkZ];
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
        if (this.chunk.getCell(x + 1, y, z) == null) {
            sides[0] = true;
        }

        //sides[0] free
        if (this.chunk.getCell(x - 1, y, z) == null) {
            sides[1] = true;
        }

        //sides[2] free
        if (this.chunk.getCell(x, y, z + 1) == null) {
            sides[2] = true;
        }

        //sides[3] free
        if (this.chunk.getCell(x, y, z - 1) == null) {
            sides[3] = true;
        }

        //sides[4] free
        if (this.chunk.getCell(x, y + 1, z) == null) {
            sides[4] = true;
        }else{
            if(this.id == CellId.GRASS){
                setId(CellId.DIRT);
            }
        }

        //Bottom free
        if (this.chunk.getCell(x, y - 1, z) == null) {
            sides[5] = true;
        }
    }

    public void setId(CellId id) {
        this.id = id;

        switch (id) {
            case GRASS:
                offsets[0] = TextureOffsets.OFF_GRASS_SIDE;
                offsets[1] = TextureOffsets.OFF_GRASS_SIDE;
                offsets[2] = TextureOffsets.OFF_GRASS_SIDE;
                offsets[3] = TextureOffsets.OFF_GRASS_SIDE;
                offsets[4] = TextureOffsets.OFF_GRASS_TOP;
                offsets[5] = TextureOffsets.OFF_DIRT;
                break;
            case STONE:
                offsets[0] = TextureOffsets.OFF_STONE;
                offsets[1] = TextureOffsets.OFF_STONE;
                offsets[2] = TextureOffsets.OFF_STONE;
                offsets[3] = TextureOffsets.OFF_STONE;
                offsets[4] = TextureOffsets.OFF_STONE;
                offsets[5] = TextureOffsets.OFF_STONE;
                break;
            case DIRT:
                offsets[0] = TextureOffsets.OFF_DIRT;
                offsets[1] = TextureOffsets.OFF_DIRT;
                offsets[2] = TextureOffsets.OFF_DIRT;
                offsets[3] = TextureOffsets.OFF_DIRT;
                offsets[4] = TextureOffsets.OFF_DIRT;
                offsets[5] = TextureOffsets.OFF_DIRT;
                break;
            case WOOD:
                offsets[0] = TextureOffsets.OFF_WOOD_SIDE;
                offsets[1] = TextureOffsets.OFF_WOOD_SIDE;
                offsets[2] = TextureOffsets.OFF_WOOD_SIDE;
                offsets[3] = TextureOffsets.OFF_WOOD_SIDE;
                offsets[4] = TextureOffsets.OFF_WOOD_TOP_BOTTOM;
                offsets[5] = TextureOffsets.OFF_WOOD_TOP_BOTTOM;
                break;
        }
    }
}
