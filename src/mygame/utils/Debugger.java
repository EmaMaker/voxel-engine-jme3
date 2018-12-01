package mygame.utils;

public class Debugger {

    public static boolean debugging = false;
    public static final boolean TESTING = false;

    //Actually only does prints in console things, but it's useful to not comment the debug lines each time, but only pressing a button
    public static void debug(String s) {

        if (debugging) {
            System.out.println(s);
        }

    }

    public static void debug(Exception e) {
        debug(e.getStackTrace().toString());
    }


}
