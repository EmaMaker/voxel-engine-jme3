package mygame.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import mygame.Main;
import mygame.block.Cell;
import mygame.utils.json.JsonDeserializeCell;
import mygame.utils.json.JsonDeserializeChunk;
import mygame.world.Chunk;
import mygame.world.WorldProvider;

public class Reference extends AbstractAppState {

    //json things
    public static ObjectMapper mapper = new ObjectMapper();
    public static SimpleModule module = new SimpleModule();

    //the lenght of a chunk size
    public static int chunkSize = 16;

    //a static instantiate of Main class
    public static Main main;
    public static WorldProvider prov;
    public static Material mat;
    public static Node terrainNode = new Node();
    public static FileHelper fileHelper = new FileHelper();

    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        Reference.main = (Main) app;
        prov = stateManager.getState(WorldProvider.class);
        mat = new Material(Reference.main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        main.getRootNode().attachChild(terrainNode);

        module.addDeserializer(Cell.class, new JsonDeserializeCell());
        module.addDeserializer(Chunk.class, new JsonDeserializeChunk());
        mapper.registerModule(module);
    }
}
