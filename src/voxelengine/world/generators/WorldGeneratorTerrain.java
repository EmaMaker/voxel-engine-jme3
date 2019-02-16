package voxelengine.world.generators;

import static com.jme3.texture.Texture.WrapAxis.R;
import java.util.Random;
import voxelengine.block.CellId;
import voxelengine.utils.Globals;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.utils.Globals.pX;
import static voxelengine.utils.Globals.pY;
import static voxelengine.utils.Globals.pZ;
import static voxelengine.utils.Globals.renderDistance;
import voxelengine.utils.math.MathHelper;
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
                    }
                    if (rand.nextFloat() <= 0.0009f) {
                        generateTree(c, i, ((int) j) + 1, k);
                    }
                }
            }
            c.markForUpdate(true);
        }
    }

    public void generateTree(Chunk c, int startX, int startY, int startZ) {
        int height = 4 + rand.nextInt() % 2;
        for (int i = 0; i <= height; i++) {
            c.setCell(startX, startY + i, startZ, CellId.ID_WOOD);
        }
        generateLeavesSphere(c.x * chunkSize + startX, c.y * chunkSize + startY + height + 2, c.z * chunkSize + startZ, 3);

    }

    //this is handled in world coords, because of leaves
    public void generateLeavesSphere(int cx, int cy, int cz, int radius) {
        for (int i = cx - radius; i <= cx + radius; i++) {
            for (int j = cy - radius; j <= cy + radius; j++) {
                for (int k = cz - radius; k <= cz + radius; k++) {
                    if (Math.sqrt(Math.pow(cx - i, 2) + Math.pow(cy - j, 2) + Math.pow(cz - k, 2)) < radius) {
                        Globals.prov.setCell(i, j, k, CellId.ID_LEAVES);
                    }
                }
            }
        }
    }
}
