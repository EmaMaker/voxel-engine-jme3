package mygame.world;

import mygame.control.PlayerControlState;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.block.CellId;
import mygame.Main;
import mygame.block.Cell;
import mygame.utils.Reference;
import static mygame.utils.Reference.chunkSize;

public class WorldProvider extends AbstractAppState {

    Main app;
    Random rand = new Random();

    public final static int MAXX = 40, MAXY = 20, MAXZ = 40;

    public static Chunk[][][] chunks;

    //public static Chunk[][][] chunks = new Chunk[MAXX][MAXY][MAXZ];
    boolean worldGenerated = false;

    public static int pX, pY, pZ; //player coords in chunks

    PlayerControlState playerControl;

    public static byte renderDistance = 8;
    public boolean updateChunks = true;
    ChunkUnloader unloader = new ChunkUnloader();
    File fChunk;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        playerControl = this.app.getStateManager().getState(PlayerControlState.class);
    }

    @Override
    public synchronized void update(float tpf) {

        if (this.app.getStateManager().getState(PlayerControlState.class).isEnabled()) {
            pX = playerControl.getX() / chunkSize;
            pY = playerControl.getY() / chunkSize;
            pZ = playerControl.getZ() / chunkSize;
        } else {
            pX = (int) app.getCamera().getLocation().getX() / chunkSize;
            pY = (int) app.getCamera().getLocation().getY() / chunkSize;
            pZ = (int) app.getCamera().getLocation().getZ() / chunkSize;
        }

        if (updateChunks) {
            for (int i = 0; i < MAXX; i++) {
                for (int j = 0; j < MAXY; j++) {
                    for (int k = 0; k < MAXZ; k++) {
                        try {
                            if (Math.sqrt((Math.pow(pX - i, 2) + Math.pow(pY - j, 2) + Math.pow(pZ - k, 2))) <= renderDistance) {
                                if (chunks[i][j][k] != null) {
                                    if (chunks[i][j][k].isEmpty()) {
                                        chunks[i][j][k] = null;
                                    } else {
                                        if (!chunks[i][j][k].loaded || chunks[i][j][k].toBeUpdated) {
                                            chunks[i][j][k].update();
                                        }
                                        chunks[i][j][k].load();
                                    }

                                    try {
                                        /*if (Math.sqrt((Math.pow(pX - i, 2) + Math.pow(pY - j, 2) + Math.pow(pZ - k, 2))) <= 1) {
                                        chunks[i][j][k].loadPhysics();
                                    } else {
                                        chunks[i][j][k].unloadPhysics();
                                    }*/
                                    } catch (Exception e2) {
                                    }
                                }
                            } else {
                                chunks[i][j][k].unload();
                            }
                        } catch (Exception e1) {

                        }
                    }
                }
            }
        }
    }

    public void preload() {
        /*DO NOT TOUCH THESE LINES*/
        chunks = new Chunk[MAXX][MAXY][MAXZ];

        Reference.executor.submit(chunkManager);
        Reference.executor.submit(unloader);

        /*FROM HERE BELOW YOU CAN MODIFY*/
 /*chunks[0][0][0] = new Chunk(0,0,0);
        chunks[0][0][0].genTerrain();
        chunks[0][0][0].update();
        chunks[0][0][0].load();*/
    }

    public void setCell(Vector3f v, CellId id) {
        this.setCell((int) v.x, (int) v.y, (int) v.z, id);
    }

    public void setCell(float i, float j, float k, CellId id) {
        this.setCell((int) i, (int) j, (int) k, id);
    }

    //replaces the Cell.setId(id), and replaces making all the cell air when chunk is created
    public void setCell(int i, int j, int k, CellId id) {
        //System.out.println("Cell being placed in world coords: " + i + ", " + j + ", " + k);

        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunkX < MAXX && chunkY < MAXY && chunkZ < MAXZ) {
            if (chunks[chunkX][chunkY][chunkZ] != null) {
                chunks[chunkX][chunkY][chunkZ].setCell(plusX, plusY, plusZ, id);
            } else {
                chunks[chunkX][chunkY][chunkZ] = new Chunk(chunkX, chunkY, chunkZ);
                chunks[chunkX][chunkY][chunkZ].setCell(plusX, plusY, plusZ, id);
            }
            chunks[chunkX][chunkY][chunkZ].unload(); //unloaded here, so next update cycle it will be showed
        } else {
            System.out.println("Cell at: " + chunkX * chunkSize + plusX + ", " + chunkY * chunkSize + plusY + ", " + chunkZ * chunkSize + plusZ + " is out of the world");
        }
    }

    public Cell getCell(Vector3f v) {
        return getCell((int) v.x, (int) v.y, (int) v.z);
    }

    public Cell getCell(float i, float j, float k) {
        return getCell((int) i, (int) j, (int) k);
    }

    public Cell getCell(int i, int j, int k) {
        //System.out.println("Cell being placed in world coords: " + i + ", " + j + ", " + k);

        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        try {
            if (chunks[chunkX][chunkY][chunkZ] != null) {
                return chunks[chunkX][chunkY][chunkZ].getCell(plusX, plusY, plusZ);

            }
        } catch (Exception e1) {
            return null;

        }
        return null;
    }

    //returns the chunk is the specified coords
    public Chunk getChunk(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        return chunks[chunkX][chunkY][chunkZ];

    }

    public Chunk getChunk(Vector3f v) {
        return this.getChunk((int) v.x, (int) v.y, (int) v.z);
    }

    final Callable<Object> chunkManager = new Callable<Object>() {
        @Override
        public Object call() {

            while (updateChunks) {
                for (int i = pX - renderDistance; i <= pX + renderDistance; i++) {
                    for (int j = pY - renderDistance; j <= pY + renderDistance; j++) {
                        for (int k = pZ - renderDistance; k <= pZ + renderDistance; k++) {

                            if (i >= 0 && j >= 0 && k >= 0) {

                                if (chunks[i][j][k] == null) {
                                    chunks[i][j][k] = new Chunk(i, j, k);

                                    fChunk = Paths.get(System.getProperty("user.dir") + "/chunks/" + i + "-" + j + "-" + k).toFile();

                                    if (fChunk.exists()) {

                                        try {
                                            chunks[i][j][k].cells = (ArrayList<Cell>) Arrays.asList(Reference.mapper.readValue(fChunk, Cell[].class));
                                        } catch (IOException ex) {
                                        }

                                        fChunk.delete();
                                    } else {
                                        if (j == 0) {
                                            chunks[i][j][k].genTerrain();
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            return null;
        }
    };

    public class ChunkUnloader implements Callable<Object> {

        @Override
        public Object call() throws Exception {
            while (updateChunks) {
                for (int i = 0; i < MAXX; i++) {
                    for (int j = 0; j < MAXY; j++) {
                        for (int k = 0; k < MAXZ; k++) {
                            if (Math.sqrt((Math.pow(pX - i, 2) + Math.pow(pY - j, 2) + Math.pow(pZ - k, 2))) > renderDistance) {

                                fChunk = Paths.get(System.getProperty("user.dir") + "/chunks/" + chunks[i][j][k].x + "-" + chunks[i][j][k].y + "-" + chunks[i][j][k].z).toFile();

                                if (!fChunk.exists()) {
                                    Reference.mapper.writeValue(fChunk, chunks[i][j][k].cells);
                                    chunks[i][j][k] = null;

                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

    }

}
