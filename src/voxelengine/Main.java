package voxelengine;

import com.jme3.app.SimpleApplication;
import voxelengine.utils.Globals;
import voxelengine.world.decorators.WorldDecoratorTrees;
import voxelengine.world.generators.WorldGeneratorTerrain;

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
        Globals.setTesting(false);
        Globals.setWireFrameEnabled(false);
        Globals.setWorldGenerator(new WorldGeneratorTerrain());
        Globals.setWorldDecorator(new WorldDecoratorTrees());
        Globals.setRenderDistance(4);
        Globals.setDebugEnabled(false);
        Globals.setWorldHeight(2);
        
        Globals.LOAD_FROM_FILE = false;
        Globals.SAVE_ON_EXIT = false;

        stateManager.attach(new VoxelEngine(settings));
    }

    @Override
    public void destroy() {
        stateManager.cleanup();
    }

}
