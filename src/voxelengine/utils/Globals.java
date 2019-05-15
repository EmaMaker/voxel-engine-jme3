package voxelengine.utils;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import voxelengine.VoxelEngine;
import voxelengine.control.ControlsHandler;
import voxelengine.world.WorldManager;
import voxelengine.world.decorators.WorldDecorator;
import voxelengine.world.decorators.WorldDecoratorTrees;
import voxelengine.world.generators.WorldGenerator;
import voxelengine.world.generators.WorldGeneratorBase;
import voxelengine.world.generators.WorldGeneratorCube;
import voxelengine.world.generators.WorldGeneratorTerrain;

public class Globals extends AbstractAppState {

    //the lenght of a chunk side
    public static int chunkSize = 16;

    //max world height to be generated
    //basically it's the number of cubic chunks to generator under the simplex-noise generated ones
    public static int worldHeight = 1;

    //a static instantiate of Main class
    public static SimpleApplication main;
    public static VoxelEngine engine;
    public static ControlsHandler control;
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
    static String generatorS = "", decoratorS = "";
    static WorldDecorator decorator = new WorldDecoratorTrees();
    static boolean enableDecorators = true;

    public static boolean LOAD_FROM_FILE = false;
    public static boolean SAVE_ON_EXIT = true;

    public static String workingDir = System.getProperty("user.dir") + "/chunk-saves/";
    public static String permtableName = "perm.table";

    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    public static int MAXX = 100, MAXY = 40, MAXZ = 100;
    public static int pX = 8, pY = 16, pZ = 8;
    public static int pGX = 8, pGY = 8, pGZ = 8;
    public static int renderDistance = 8;
    static int pickingDistance = 6;

    static HashMap<String, WorldDecorator> decorators = new HashMap<String, WorldDecorator>() {
        {
            put("decoratorTrees", new WorldDecoratorTrees());
        }
    };
    static HashMap<String, WorldGenerator> generators = new HashMap<String, WorldGenerator>() {
        {
            put("generatorBase", new WorldGeneratorBase());
            put("generatorCube", new WorldGeneratorCube());
            put("generatorTerrain", new WorldGeneratorTerrain());
        }
    };

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        

        Globals.main = (SimpleApplication) app;
        prov = stateManager.getState(WorldManager.class);
        engine = stateManager.getState(VoxelEngine.class);
        control = stateManager.getState(ControlsHandler.class);

        mat = new Material(main.getAssetManager(), "Materials/UnshadedArray.j3md");

        main.getRootNode().attachChild(terrainNode);
        mat.getAdditionalRenderState().setWireframe(enableWireframe);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        pGY = prov.getHighestCellAt(pGX, pGZ);

        if (Globals.LOAD_FROM_FILE) {
            Globals.loadFromFile();
        }
    }

    public static void saveToFile() {
        String save = "";
        save += "chunkSize=" + chunkSize + "\n";
        save += "worldHeight=" + worldHeight + "\n";
        save += "TESTING=" + TESTING + "\n";
        save += "enableDebug=" + enableDebug + "\n";
        save += "enablePhysics=" + enablePhysics + "\n";
        save += "enablePlayer=" + enablePlayer + "\n";
        save += "enableWireframe=" + enableWireframe + "\n";
        save += "worldGenerator=" + generatorS + "\n";
        save += "worldDecorator=" + decoratorS + "\n";
        save += "enableDecorators=" + enableDecorators + "\n";
        save += "LOAD_FROM_FILE=" + LOAD_FROM_FILE + "\n";
        save += "SAVE_ON_EXIT=" + SAVE_ON_EXIT + "\n";
        save += "workingDir=" + workingDir + "\n";
        save += "permtableName=" + permtableName + "\n";
        save += "MAXX=" + MAXX + "\n";
        save += "MAXY=" + MAXY + "\n";
        save += "MAXZ=" + MAXZ + "\n";
        save += "pX=" + pX + "\n";
        save += "pY=" + pY + "\n";
        save += "pZ=" + pZ + "\n";
        save += "pGX=" + (int) engine.getCamera().getLocation().getX() + "\n";
        save += "pGY=" + (int) engine.getCamera().getLocation().getY() + "\n";
        save += "pGZ=" + (int) engine.getCamera().getLocation().getZ() + "\n";
        save += "renderDistance=" + renderDistance + "\n";
        save += "pickingDistance=" + pickingDistance + "\n";

        File f = Paths.get(Globals.workingDir + "settings").toFile();
        f.delete();

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f);
                writer.print(save);
                writer.close();
            } catch (FileNotFoundException e) {
            }
        }
    }

    public static void loadFromFile() {
        File f = Paths.get(Globals.workingDir + "settings").toFile();
        String[] s1;

        try {
            for (String s : Files.readAllLines(f.toPath())) {
                s1 = s.split("=");
                //System.out.println(s1[1]);
                switch (s1[0]) {
                    case "chunkSize":
                        Globals.chunkSize = Integer.valueOf(s1[1]);
                        break;
                    case "worldHeight":
                        Globals.worldHeight = Integer.valueOf(s1[1]);
                        break;
                    case "TESTING":
                        Globals.TESTING = Boolean.valueOf(s1[1]);
                        break;
                    case "enableDebug":
                        Globals.enableDebug = Boolean.valueOf(s1[1]);
                        break;
                    case "enablePhysics":
                        Globals.enablePhysics = Boolean.valueOf(s1[1]);
                        break;
                    case "enablePlayer":
                        Globals.enablePlayer = Boolean.valueOf(s1[1]);
                        break;
                    case "enableWireframe":
                        Globals.enableWireframe = Boolean.valueOf(s1[1]);
                        break;
                    case "worldGenerator":
                        Globals.setWorldGenerator(s1[1]);
                        break;
                    case "worldDecorator":
                        Globals.setWorldDecorator(s1[1]);
                        break;
                    case "enableDecorators":
                        Globals.enableDecorators = Boolean.valueOf(s1[1]);
                        break;
                    case "LOAD_FROM_FILE":
                        Globals.LOAD_FROM_FILE = Boolean.valueOf(s1[1]);
                        break;
                    case "SAVE_ON_EXIT":
                        Globals.SAVE_ON_EXIT = Boolean.valueOf(s1[1]);
                        break;
                    case "workingDir":
                        Globals.workingDir = s1[1];
                        break;
                    case "permtableName":
                        Globals.permtableName = s1[1];
                        break;
                    case "MAXX":
                        Globals.MAXX = Integer.valueOf(s1[1]);
                        break;
                    case "MAXY":
                        
                        Globals.MAXY = Integer.valueOf(s1[1]);
                        break;
                    case "MAXZ":
                        Globals.MAXZ = Integer.valueOf(s1[1]);
                        break;
                    case "pX":
                        Globals.pX = Integer.valueOf(s1[1]);
                        break;
                    case "pY":
                        Globals.pY = Integer.valueOf(s1[1]);
                        break;
                    case "pZ":
                        Globals.pZ = Integer.valueOf(s1[1]);
                        break;
                    case "pGX":
                        Globals.pGX = Integer.valueOf(s1[1]);
                        break;
                    case "pGY":
                        Globals.pGY = Integer.valueOf(s1[1]);
                        break;
                    case "pGZ":
                        Globals.pGZ = Integer.valueOf(s1[1]);
                        break;
                    case "renderDistance":
                        renderDistance = Integer.valueOf(s1[1]);
                        break;
                    case "pickingDistance":
                        pickingDistance = Integer.valueOf(s1[1]);
                        break;
                    default:
                        System.out.println("Ouch " + s);
                        break;
                }
            }
            f.delete();
        } catch (IOException | NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    public static HashMap getGenerators() {
        return generators;
    }

    public static HashMap getDecorators() {
        return decorators;
    }

    //Actually only does prints in console things, but it's useful to not comment the debug lines each time, but only pressing a key
    public static void debug(Object... s) {
        if (enableDebug) {
            System.out.println("Debugging: " + Arrays.toString(s));
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

    public static void setPermTable(String s) {
        permtableName = s;
    }

    public static String getPermTable() {
        return permtableName;
    }

    public static void setWorldHeight(int s) {
        worldHeight = s;
    }

    public static int getWorldHeight() {
        return worldHeight;
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

    public static void setWorldDecorator(WorldDecorator d) {
        decorator = d;
    }

    public static void setWorldGenerator(String s) {
        if (Globals.generators.containsKey(s)) {
            generator = Globals.generators.get(s);
            generatorS = s;
        }
    }

    public static WorldGenerator getWorldGenerator() {
        return generator;
    }

    public static void setWorldDecorator(String s) {
        if (Globals.decorators.containsKey(s)) {
            decorator = Globals.decorators.get(s);
            decoratorS = s;
        }
    }

    public static WorldDecorator getWorldDecorator() {
        return decorator;
    }

    public static void enableDecorators(boolean b) {
        enableDecorators = b;
    }

    public static boolean decoratorsEnabled() {
        return enableDecorators;
    }

    public static void setWorldSize(int x, int y, int z) {
        MAXX = x;
        MAXY = y;
        MAXZ = z;
    }

}
