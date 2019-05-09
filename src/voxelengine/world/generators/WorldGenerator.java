package voxelengine.world.generators;

import voxelengine.block.CellId;
import voxelengine.world.Chunk;

public abstract class WorldGenerator {
    public abstract void generate(Chunk c);
    
    public void dirtToGrass(Chunk c, int cellX, int cellY, int cellZ){
        if(c.getCell(cellX, cellY, cellZ) == CellId.ID_DIRT && c.getCell(cellX, cellY+1, cellZ) == CellId.ID_AIR){
            c.setCell(cellX, cellY, cellZ, CellId.ID_GRASS);
        }
    }
    public void grassToDirt(Chunk c, int cellX, int cellY, int cellZ){
        if(c.getCell(cellX, cellY, cellZ) == CellId.ID_GRASS && c.getCell(cellX, cellY+1, cellZ) != CellId.ID_AIR){
            c.setCell(cellX, cellY, cellZ, CellId.ID_DIRT);
        }
    }
}
