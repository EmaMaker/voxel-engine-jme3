package mygame.utils;

import java.math.BigDecimal;

public class MathHelper {

    public static double biggerNumberInList(double... n) {
        double d = 0;
        for (int i = 0; i < n.length; i++) {
            if (n[i] > d) {
                d = n[i];
            }
        }
        return d;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
