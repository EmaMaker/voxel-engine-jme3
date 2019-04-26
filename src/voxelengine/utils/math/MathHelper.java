package voxelengine.utils.math;

import com.jme3.math.Vector3f;
import java.math.BigDecimal;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.world.WorldManager.MAXX;
import static voxelengine.world.WorldManager.MAXY;
import static voxelengine.world.WorldManager.MAXZ;

public class MathHelper {

    public static double biggestNumberInList(double... n) {
        double d = 0;
        for (int i = 0; i < n.length; i++) {
            if (n[i] > d) {
                d = n[i];
            }
        }
        return d;
    }

    public static double lowestNumberInList(double... n) {
        double d = 0;
        for (int i = 0; i < n.length; i++) {
            if (n[i] < d) {
                d = n[i];
            }
        }
        return d;
    }

    public static Vector3f lowestVectorInList(Vector3f... n) {
        Vector3f v1 = Vector3f.POSITIVE_INFINITY;
        for (Vector3f v2 : n) {
            if (v2.x <= v1.x && v2.y <= v1.y && v2.z <= v1.z) {
                v1 = v2;
            }
        }
        return v1;
    }

    public static Vector3f castVectorToInt(Vector3f v) {
        return new Vector3f((int) v.x, (int) v.y, (int) v.z);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static int flatChunk3Dto1D(int x, int y, int z) {
        //return (z * MAXX * MAXY) + (y * MAXX) + x;
        return x + y * MAXX + z * MAXX * MAXZ;
    }

    public static int flatCell3Dto1D(int x, int y, int z) {
        return (z * chunkSize * chunkSize) + (y * chunkSize) + x;
    }

    public static int[] cell1Dto3D(int idx) {
        final int z = idx / (chunkSize * chunkSize);
        idx -= (z * chunkSize * chunkSize);
        final int y = idx / chunkSize;
        final int x = idx % chunkSize;
        return new int[]{x, y, z};
    }

}
