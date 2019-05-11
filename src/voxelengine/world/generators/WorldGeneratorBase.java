package voxelengine.world.generators;

import voxelengine.block.CellId;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.world.Chunk;

public class WorldGeneratorBase extends WorldGenerator {

    int nBases = 1;

    public WorldGeneratorBase() {
        this(1);
    }

    public WorldGeneratorBase(int nBases_) {
        this.nBases = nBases_;
    }
    
    @Override
    public void generate(Chunk c) {
        if (c.y == 0) {
            for (int a = 0; a < nBases; a++) {
                for (int i = 0; i < chunkSize; i++) {
                    for (int j = 0; j < chunkSize; j++) {
                        c.setCell(i, a, j, CellId.ID_GRASS);
                    }
                }
            }
            c.markForUpdate(true);
        }
    }

}
