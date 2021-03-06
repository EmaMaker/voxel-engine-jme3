package voxelengine.world;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import voxelengine.block.CellId;
import voxelengine.control.ControlsHandler;
import voxelengine.utils.math.MathHelper;
import voxelengine.utils.Globals;
import static voxelengine.utils.Globals.*;

public class WorldManager extends AbstractAppState {

    Chunk[][][] chunks;

    SimpleApplication app;
    Random rand = new Random();
    AppStateManager stateManager;
    ControlsHandler controlHandler;

    public boolean updateChunks = true;
    public boolean generateChunks = true;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.stateManager = stateManager;
        controlHandler = stateManager.getState(ControlsHandler.class);

        chunks = new Chunk[MAXX][MAXY][MAXZ];
        preload();
    }

    public void preload() {
        updateChunks = true;
        generateChunks = true;

        if (Globals.isTesting()) {
            updateChunks = true;
            generateChunks = false;

            Globals.setWorldGenerator("generatorBase");

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    newChunk(i, 0, j);
                }
            }
        }
        Globals.executor.submit(chunkManager);
    }

    @Override
    public void update(float tpf) {

        for (int i = pX - renderDistance; i < pX + renderDistance * 1.5; i++) {
            for (int j = pY - renderDistance; j < pY + renderDistance * 1.5; j++) {
                for (int k = pZ - renderDistance; k < pZ + renderDistance * 1.5; k++) {

                    if (i >= 0 && i < MAXX && j >= 0 && j < MAXY && k >= 0 && k < MAXZ) {
                        if (getChunk(i, j, k) != null) {
                            getChunk(i, j, k).updateMesh();
                        }
                    }
                }
            }
        }
    }

    //replaces the Cell.setId(id), and replaces making all the cell air when chunk is created. Commento storico del 2016 (Si, lo so che è il 2019 ora) - historical comment from 2016 (Yes, I know it's 2019 now)
    public void setCell(int i, int j, int k, byte id) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunkX >= 0 && chunkY >= 0 && chunkZ >= 0 && chunkX < MAXX && chunkY < MAXY && chunkZ < MAXZ) {
            if (getChunk(chunkX, chunkY, chunkZ) == null) {
                setChunk(chunkX, chunkY, chunkZ, new Chunk(chunkX, chunkY, chunkZ));
            }
            getChunk(chunkX, chunkY, chunkZ).setCell(plusX, plusY, plusZ, id);
        }
    }

    public byte getCell(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunkX >= 0 && chunkY >= 0 && chunkZ >= 0 && chunkX < MAXX && chunkY < MAXY && chunkZ < MAXZ) {
            if (getChunk(chunkX, chunkY, chunkZ) != null) {
                return getChunk(chunkX, chunkY, chunkZ).getCell(plusX, plusY, plusZ);
            }
        }
        return Byte.MIN_VALUE;
    }

    //returns the chunk is the specified coords
    public Chunk getChunkInWorld(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        return chunks[chunkX][chunkY][chunkZ];
    }

    public Chunk getChunk(int i, int j, int k) {
        return chunks[i][j][k];
    }

    public void setChunk(int i, int j, int k, Chunk c) {
        chunks[i][j][k] = c;
    }

    public void newChunk(int i, int j, int k) {
        Chunk c = new Chunk(i, j, k);
        chunks[i][j][k] = c;
    }

    public void setCellFromVertices(ArrayList<Vector3f> al, byte id) {
        setCell(getCellPosFromVertices(al), id);
    }

    public byte getCellFromVertices(ArrayList<Vector3f> al) {
        return getCell(getCellPosFromVertices(al));
    }

    public Vector3f getCellPosFromVertices(ArrayList<Vector3f> al) {
        Vector3f v = MathHelper.lowestVectorInList(al.get(0), al.get(1), al.get(2), al.get(3));

        if (al.get(0).x == al.get(1).x && al.get(0).x == al.get(2).x && al.get(0).x == al.get(3).x) {
            if (getCell(v) != CellId.ID_AIR) {
                debug(v.toString());
                return v;
            } else {
                v.set((int) (v.x - 1), (int) v.y, (int) v.z);
                debug(v.toString());
                return v;
            }
        } else if (al.get(0).y == al.get(1).y && al.get(0).y == al.get(2).y && al.get(0).y == al.get(3).y) {
            if (getCell(v) != CellId.ID_AIR) {
                debug(v.toString());
                return v;
            } else {
                v.set((int) v.x, (int) (v.y - 1), (int) v.z);
                debug(v.toString());
                return v;
            }
        } else if (al.get(0).z == al.get(1).z && al.get(0).z == al.get(2).z && al.get(0).z == al.get(3).z) {
            if (getCell(v) != CellId.ID_AIR) {
                debug(v.toString());
                return v;
            } else {
                v.set((int) v.x, (int) v.y, (int) (v.z - 1));
                debug(v.toString());
                return v;
            }
        }
        return null;
    }

    public byte getHighestCellAt(int i, int j) {
        for (int a = MAXY * chunkSize; a >= 0; a--) {
            if (getCell(i, a, j) != CellId.ID_AIR) {
                return getCell(i, a, j);
            }
        }
        return Byte.MIN_VALUE;
    }

    @Override
    public void cleanup() {
        updateChunks = false;
        Globals.executor.shutdownNow();
    }

    public void loadFromFile(int i, int j, int k) {
        File f = Paths.get(Globals.workingDir + i + "-" + j + "-" + k + ".chunk").toFile();
        getChunk(i, j, k).loadFromFile(f);
    }

    public boolean canLoadFromFile(int i, int j, int k) {
        File f = Paths.get(Globals.workingDir + i + "-" + j + "-" + k + ".chunk").toFile();
        return f.exists();
    }

    final Callable<Object> chunkManager = new Callable<Object>() {
        @Override
        public Object call() {
            while (updateChunks) {
                updateChunks();
            }
            return null;
        }
    };

    void updateChunks() {
        try {
            if (controlHandler.placeBlock) {
                controlHandler.placeBlock();
                controlHandler.placeBlock = false;

            }
            if (controlHandler.breakBlock) {
                controlHandler.breakBlock();
                controlHandler.breakBlock = false;
            }
            for (int i = pX - renderDistance; i < pX + renderDistance * 1.5; i++) {
                for (int j = pY - renderDistance; j < pY + renderDistance * 1.5; j++) {
                    for (int k = pZ - renderDistance; k < pZ + renderDistance * 1.5; k++) {

                        if (i >= 0 && i < MAXX && j >= 0 && j < MAXY && k >= 0 && k < MAXZ) {
                            if (getChunk(i, j, k) != null) {
                                if (generateChunks) {
                                    getChunk(i, j, k).generate();
                                    getChunk(i, j, k).decorate();
                                }
                                getChunk(i, j, k).processCells();
                            } else {
                                if (canLoadFromFile(i, j, k)) {
                                    newChunk(i, j, k);
                                    loadFromFile(i, j, k);
                                } else {
                                    if (j <= Globals.getWorldHeight()) {
                                        newChunk(i, j, k);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*SOME USEFUL METHOD OVERRIDING*/
    public void setCell(Vector3f v, byte id) {
        if (v != null) {
            this.setCell((int) v.x, (int) v.y, (int) v.z, id);
        }
    }

    public void setCell(float i, float j, float k, byte id) {
        this.setCell((int) i, (int) j, (int) k, id);
    }

    public byte getCell(Vector3f v) {
        return v != null ? getCell((int) v.x, (int) v.y, (int) v.z) : null;
    }

    public byte getCell(float i, float j, float k) {
        return getCell((int) i, (int) j, (int) k);
    }

    public Chunk getChunk(Vector3f v) {
        return this.getChunk((int) v.x, (int) v.y, (int) v.z);
    }
}
