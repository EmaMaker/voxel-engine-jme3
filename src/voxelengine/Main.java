package voxelengine;

import com.jme3.app.SimpleApplication;
import voxelengine.utils.Globals;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
        app.setShowSettings(false);
    }

    @Override
    public void simpleInitApp() {
        Globals.setPlayerEnabled(false);
        Globals.setPhysicsEnabled(true);
        Globals.LOAD_FROM_FILE = false;
        Globals.SAVE_ON_EXIT = true;

        stateManager.attach(new VoxelEngine(settings));
    }

    @Override
    public void destroy() {
        stateManager.cleanup();
    }

}
