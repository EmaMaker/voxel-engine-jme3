package voxelengine;

import com.jme3.app.SimpleApplication;
import voxelengine.utils.Globals;
import voxelengine.world.decorators.*;
import voxelengine.world.generators.*;

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
        Globals.setWorldGenerator((WorldGenerator) Globals.getGenerators().get("generatorTerrain"));
        Globals.setWorldDecorator((WorldDecorator) Globals.getDecorators().get("decoratorTrees"));
        Globals.enableDecorators(true);
        Globals.setRenderDistance(12);
        Globals.setDebugEnabled(false);
        Globals.setWorldHeight(0);
        Globals.setWorldSize(100, 20, 100);
        
        
        Globals.LOAD_FROM_FILE = false;
        Globals.SAVE_ON_EXIT = true;

        stateManager.attach(new VoxelEngine(settings));
    }

    @Override
    public void destroy() {
        stateManager.cleanup();
    }

}
