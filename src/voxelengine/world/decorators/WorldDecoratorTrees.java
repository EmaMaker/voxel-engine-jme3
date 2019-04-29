package voxelengine.world.decorators;

import java.util.Random;
import voxelengine.block.CellId;
import voxelengine.utils.Globals;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.world.Chunk;

public class WorldDecoratorTrees extends WorldDecorator {

    Random rand = new Random();

    @Override
    public void decorate(Chunk c) {
        if (c.y == Globals.getWorldHeight() ) {
            int a;
            for (int i = 0; i < chunkSize; i++) {
                for (int k = 0; k < chunkSize; k++) {
                    a = c.y * chunkSize +(c.getHighestYAt(i, k) + 1);
                    if (rand.nextFloat() <= 0.0015f && a != Integer.MAX_VALUE && c.getCell(i, a - 1, k) == CellId.ID_GRASS) {
                        generateTree(c, i, a, k);
                        c.markForUpdate(true);
                    }
                }
            }
        }
    }

    public void generateTree(Chunk c, int startX, int startY, int startZ) {
        int height = 4 + rand.nextInt() % 2;

        generateLeavesSphere(c, startX, startY + height + 1, startZ, 3);
        for (int i = 0; i <= height; i++) {
            c.setCell(startX, startY + i, startZ, CellId.ID_WOOD);
        }
    }

    //this is handled in world coords, because of leaves
    public void generateLeavesSphere(Chunk c, int cx, int cy, int cz, int radius) {
        //System.out.println(cx + ", " + cy + ", " + cz);
        int x = c.x * chunkSize + cx, y = c.y * chunkSize + cy, z = c.z * chunkSize + cz;
        for (int i = x - radius; i <= x + radius; i++) {
            for (int j = y - radius + 2; j <= y + radius; j++) {
                for (int k = z - radius; k <= z + radius; k++) {
                    if (Math.sqrt(Math.pow(x - i, 2) + Math.pow(y - j, 2) + Math.pow(z - k, 2)) <= radius) {
                        //debug("Leave being placed in world coords: " + i + ", " + j + ", " + k);

                        Globals.prov.setCell(i, j, k, CellId.ID_LEAVES);
                    }
                }
            }
        }
    }
}
