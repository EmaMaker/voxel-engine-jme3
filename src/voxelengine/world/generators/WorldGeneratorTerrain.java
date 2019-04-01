package voxelengine.world.generators;

import java.util.Random;
import voxelengine.block.CellId;
import voxelengine.utils.Globals;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.utils.math.SimplexNoise;
import voxelengine.world.Chunk;

public class WorldGeneratorTerrain extends WorldGenerator {

    Random rand = new Random();

    @Override
    public void generate(Chunk c) {
        double p;
        //System.out.println(Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025)));
        if (c.y < Globals.getWorldHeight()) {
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    for (int k = 0; k < chunkSize; k++) {
                        c.setCell(i, j, k, CellId.ID_DIRT);
                    }
                }
            }
        } else if (c.y == Globals.getWorldHeight()) {
            for (int i = 0; i < chunkSize; i++) {
                for (int k = 0; k < chunkSize; k++) {
                    p = Math.abs(SimplexNoise.noise((c.x * chunkSize + i) * 0.01, (c.z * chunkSize + k) * 0.01)) * 10;
                    for (int a = 0; a <= p; a++) {
                        c.setCell(i, a, k, CellId.ID_GRASS);
                        c.generated = true;
                        c.markForUpdate(true);
                    }
                }
            }
        }
    }
}
