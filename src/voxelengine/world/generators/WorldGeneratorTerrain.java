package voxelengine.world.generators;

import java.util.Random;
import voxelengine.block.CellId;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.utils.math.SimplexNoise;
import voxelengine.world.Chunk;

public class WorldGeneratorTerrain extends WorldGenerator {

    Random rand = new Random();

    @Override
    public void generate(Chunk c) {
        double j;
        //System.out.println(Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025)));
        if (c.y == 0) {
            for (int i = 0; i < chunkSize; i++) {
                for (int k = 0; k < chunkSize; k++) {
                    j = Math.abs(SimplexNoise.noise((c.x * chunkSize + i) * 0.01, (c.z * chunkSize + k) * 0.01)) * 10;
                    for (int a = 0; a <= j; a++) {
                        c.setCell(i, a, k, CellId.ID_GRASS);
                        c.generated = true;
                        c.markForUpdate(true);
                    }
                }
            }
        }
    }
}
