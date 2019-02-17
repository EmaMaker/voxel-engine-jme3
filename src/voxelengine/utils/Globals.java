package voxelengine.utils;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.scene.Node;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import voxelengine.VoxelEngine;
import voxelengine.world.WorldManager;
import voxelengine.world.decorators.WorldDecorator;
import voxelengine.world.decorators.WorldDecoratorTrees;
import voxelengine.world.generators.WorldGenerator;
import voxelengine.world.generators.WorldGeneratorBase;

public class Globals extends AbstractAppState {

    //the lenght of a chunk side
    public static int chunkSize = 16;

    //a static instantiate of Main class
    public static SimpleApplication main;
    public static VoxelEngine engine;
    public static WorldManager prov;
    public static Material mat;
    public static Node terrainNode = new Node();

    //settings
    static boolean TESTING = true;
    static boolean enableDebug = false;
    static boolean enablePhysics = true;
    static boolean enablePlayer = true;
    static boolean enableWireframe = false;

    static WorldGenerator generator = new WorldGeneratorBase();
    static WorldDecorator decorator = new WorldDecoratorTrees();

    public static boolean LOAD_FROM_FILE = false;
    public static boolean SAVE_ON_EXIT = true;

    public static String workingDir = System.getProperty("user.dir") + "/bleble/";
    public static String permtableName = "perm.table";

    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    public static int pX = 8, pY = 8, pZ = 8;
    public static int renderDistance = 8;
    static int pickingDistance = 6;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        Globals.main = (SimpleApplication) app;
        prov = stateManager.getState(WorldManager.class);
        engine = stateManager.getState(VoxelEngine.class);
        mat = new Material(main.getAssetManager(), "Materials/UnshadedArray.j3md");

        main.getRootNode().attachChild(terrainNode);
        mat.getAdditionalRenderState().setWireframe(enableWireframe);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
    }

    //Actually only does prints in console things, but it's useful to not comment the debug lines each time, but only pressing a key
    public static void debug(String s) {
        if (enableDebug) {
            System.out.println("Debugging: " + s);
        }
    }

    public static void debug(Exception e) {
        debug(Arrays.toString(e.getStackTrace()));
    }

    public static void setPhysicsEnabled(boolean enable) {
        enablePhysics = enable;
    }

    public static boolean playerEnabled() {
        return enablePlayer;
    }

    public static void setPlayerEnabled(boolean enable) {
        enablePlayer = enable;
    }

    public static boolean phyEnabled() {
        return enablePhysics;
    }

    public static void setDebugEnabled(boolean enable) {
        enableDebug = enable;
    }

    public static boolean debugEnabled() {
        return enableDebug;
    }

    public static void setWireFrameEnabled(boolean enable) {
        enableWireframe = enable;
    }

    public static boolean wireFrameEnabled() {
        return enableWireframe;
    }

    public static void setWorkingDir(String s) {
        workingDir = s;
    }

    public static String getWorkingDir() {
        return workingDir;
    }

    public static void setStartPoint(int x, int y, int z) {
        pX = x;
        pY = y;
        pZ = z;
    }

    public static void setRenderDistance(int render) {
        renderDistance = render;
    }

    public static int getRenderDistance() {
        return renderDistance;
    }


    public static void setPickingDistance(int picking) {
        pickingDistance = picking;
    }

    public static int getPickingDistance() {
        return pickingDistance;
    }

    public static void setTesting(boolean b) {
        TESTING = b;
    }

    public static boolean isTesting() {
        return TESTING;
    }

    public static void setWorldGenerator(WorldGenerator g) {
        generator = g;
    }

    public static WorldGenerator getWorldGenerator() {
        return generator;
    }

    public static void setWorldDecorator(WorldDecorator d) {
        decorator = d;
    }

    public static WorldDecorator getWorldDecorator() {
        return decorator;
    }

}
