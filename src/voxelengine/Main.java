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
        Globals.setPlayerEnabled(true);
        Globals.setPhysicsEnabled(true);
        Globals.setTesting(false);
        Globals.setWireFrameEnabled(false);
        Globals.LOAD_FROM_FILE = false;
        Globals.SAVE_ON_EXIT = false;

        stateManager.attach(new VoxelEngine(settings));
    }

    @Override
    public void destroy() {
        stateManager.cleanup();
    }

}
