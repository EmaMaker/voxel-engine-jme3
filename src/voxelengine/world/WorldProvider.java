package voxelengine.world;

import voxelengine.control.PlayerControlState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.sun.javafx.collections.NonIterableChange;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import voxelengine.block.CellId;
import voxelengine.Main;
import voxelengine.block.Cell;
import voxelengine.utils.math.MathHelper;
import voxelengine.utils.Globals;
import static voxelengine.utils.Globals.TESTING;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.utils.Globals.debug;

public class WorldProvider extends AbstractAppState {

    public final static int MAXX = 20, MAXY = 20, MAXZ = 20;
    public static Chunk[] chunks = new Chunk[MAXX * MAXY * MAXZ];

    SimpleApplication app;
    Random rand = new Random();

    //player coords in chunks
    public static int pX = 8, pY = 8, pZ = 8;

    PlayerControlState playerControl;

    public static byte renderDistance = 8;
    public boolean updateChunks = true;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        playerControl = this.app.getStateManager().getState(PlayerControlState.class);

        preload();
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

    }

    public void preload() {
        if (TESTING) {
            updateChunks = false;

            chunks[MathHelper.flat3Dto1D(0, 0, 0)] = new Chunk(0, 0, 0);
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].genTerrain();
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].processCells();
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].load();
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].loadPhysics();

            chunks[MathHelper.flat3Dto1D(0, 0, 0)].chunkMesh.contains(new Vector3f(0, 0, 0));

        } else {
            Globals.executor.submit(chunkManager);
        }
    }

    //replaces the Cell.setId(id), and replaces making all the cell air when chunk is created. Commento storico del 2016 (Si, lo so che è il 2019 ora) - historical comment from 2016 (Yes, I know it's 2019 now)
    public void setCell(int i, int j, int k, int id) {
        //debug("Cell being placed in world coords: " + i + ", " + j + ", " + k);

        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunkX < MAXX && chunkY < MAXY && chunkZ < MAXZ) {
            if (chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] != null) {
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].setCell(plusX, plusY, plusZ, id);
            } else {
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] = new Chunk(chunkX, chunkY, chunkZ);
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].setCell(plusX, plusY, plusZ, id);
            }
            //unloaded here, so next update cycle it will be showed
            chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].unload();
        }/* else {
            debug("Cell at: " + chunkX * chunkSize + plusX + ", " + chunkY * chunkSize + plusY + ", " + chunkZ * chunkSize + plusZ + " is out of the world");
        }*/
    }

    public Cell getCell(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] != null) {
            return chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].getCell(plusX, plusY, plusZ);

        }
        return null;
    }

    //returns the chunk is the specified coords
    public Chunk getChunk(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        return chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)];
    }

    public Cell getCellFromVertices(ArrayList<Vector3f> al) {
        return getCell(getCellPosFromVertices(al));
    }

    public void setCellFromVertices(ArrayList<Vector3f> al, int id) {
        setCell(getCellPosFromVertices(al), id);
    }

    public Vector3f getCellPosFromVertices(ArrayList<Vector3f> al) {
        Vector3f v = MathHelper.lowestVectorInList(al.get(0), al.get(1), al.get(2), al.get(3));

        if (al.get(0).x == al.get(1).x && al.get(0).x == al.get(2).x && al.get(0).x == al.get(3).x) {
            if (getCell(v) != null && getCell(v).id != CellId.ID_AIR) {
                debug(v.toString());
                return v;
            } else {
                v.set((int) (v.x - 1), (int) v.y, (int) v.z);
                debug(v.toString());
                return v;
            }
        } else if (al.get(0).y == al.get(1).y && al.get(0).y == al.get(2).y && al.get(0).y == al.get(3).y) {
            if (getCell(v) != null && getCell(v).id != CellId.ID_AIR) {
                debug(v.toString());
                return v;
            } else {
                v.set((int) v.x, (int) (v.y - 1), (int) v.z);
                debug(v.toString());
                return v;
            }
        } else if (al.get(0).z == al.get(1).z && al.get(0).z == al.get(2).z && al.get(0).z == al.get(3).z) {
            if (getCell(v) != null && getCell(v).id != CellId.ID_AIR) {
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

    public Cell getHighestCellAt(int i, int j) {
        for (int a = MAXY * chunkSize; a >= 0; a--) {
            if (getCell(i, a, j) != null) {
                return getCell(i, a, j);
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        updateChunks = false;
        Globals.executor.shutdownNow();
    }

    final Callable<Object> chunkManager = new Callable<Object>() {

        File f;
        List<String> lines;

        String[] datas = new String[4];
        int j = 0;

        @Override
        public Object call() {
            while (updateChunks) {
                try {
                    for (int i = pX - renderDistance; i < pX + renderDistance; i++) {
                        for (int k = pZ - renderDistance; k < pZ + renderDistance; k++) {

                            if (i >= 0 && i < MAXX && j >= 0 && j < MAXY && k >= 0 && k < MAXZ) {

                                if (chunks[MathHelper.flat3Dto1D(i, j, k)] != null) {
                                    chunks[MathHelper.flat3Dto1D(i, j, k)].processCells();
                                } else {
                                    chunks[MathHelper.flat3Dto1D(i, j, k)] = new Chunk(i, j, k);
                                    f = Paths.get(System.getProperty("user.dir") + "/chunks/" + i + "-" + j + "-" + k + ".chunk").toFile();

                                    if (f.exists()) {
                                        if (!(f.length() == 0)) {
                                            try {

                                                lines = Files.readAllLines(f.toPath());

                                                for (String s : lines) {
                                                    datas = s.split(",");
                                                    chunks[MathHelper.flat3Dto1D(i, j, k)].setCell(Integer.valueOf(datas[0]), Integer.valueOf(datas[1]), Integer.valueOf(datas[2]), Integer.valueOf(datas[3]));
                                                }

                                                f.delete();

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            //if (j == 0) {
                                            chunks[MathHelper.flat3Dto1D(i, j, k)].genTerrain();
                                            //}
                                        }
                                    } else {
                                        if (j == 0) {
                                            chunks[MathHelper.flat3Dto1D(i, j, k)].genTerrain();
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
            return null;
        }
    };

    /*SOME USEFUL METHOD OVERRIDING*/
    public void setCell(Cell c, int id) {
        this.setCell(c.worldX, c.worldY, c.worldZ, id);
    }

    public void setCell(Vector3f v, int id) {
        this.setCell((int) v.x, (int) v.y, (int) v.z, id);
    }

    public void setCell(float i, float j, float k, int id) {
        this.setCell((int) i, (int) j, (int) k, id);
    }

    public Cell getCell(Vector3f v) {
        return v != null ? getCell((int) v.x, (int) v.y, (int) v.z) : null;
    }

    public Cell getCell(float i, float j, float k) {
        return getCell((int) i, (int) j, (int) k);
    }

    public Chunk getChunk(Vector3f v) {
        return this.getChunk((int) v.x, (int) v.y, (int) v.z);
    }
}
