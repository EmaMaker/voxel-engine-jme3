package voxelengine.utils.math;

import com.jme3.math.Vector3f;
import java.math.BigDecimal;
import static voxelengine.utils.Reference.chunkSize;

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
            if (n[i]  < d) {
                d = n[i];
            }
        }
        return d;
    }

    public static Vector3f lowestVectorInList(Vector3f... n) {
        Vector3f v1 = Vector3f.POSITIVE_INFINITY;
        for(Vector3f v2 : n){
            if(v2.x <= v1.x && v2.y <= v1.y && v2.z <= v1.z){
                v1 = v2;
            }
        }
        return v1;
    }
    
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    
    public static int flat3Dto1D(int x, int y, int z){
        return (z * chunkSize * chunkSize) + (y * chunkSize) + x;
    }

}
