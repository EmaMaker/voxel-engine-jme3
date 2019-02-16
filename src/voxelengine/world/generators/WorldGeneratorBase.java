package voxelengine.world.generators;

import voxelengine.block.CellId;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.world.Chunk;

public class WorldGeneratorBase extends WorldGenerator {

    @Override
    public void generate(Chunk c) {
        if (c.y == 0) {
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    c.setCell(i, 0, j, CellId.ID_GRASS);
                }
            }
            c.markForUpdate(true);
        }
    }

}
