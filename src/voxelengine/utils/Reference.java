package mygame.utils;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import mygame.Main;
import mygame.world.WorldProvider;

public class Reference extends AbstractAppState {

    //the lenght of a chunk side
    public static int chunkSize = 16;

    //a static instantiate of Main class
    public static Main main;
    public static WorldProvider prov;
    public static Material mat;
    public static Node terrainNode = new Node();

    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        Reference.main = (Main) app;
        prov = stateManager.getState(WorldProvider.class);
        mat = new Material(main.getAssetManager(), "Materials/UnshadedArray.j3md");

        main.getRootNode().attachChild(terrainNode);
        //mat.getAdditionalRenderState().setWireframe(true);

    }
}
