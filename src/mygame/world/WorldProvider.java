package mygame.world;

import mygame.control.PlayerControlState;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.block.CellId;
import mygame.Main;
import mygame.block.Cell;
import static mygame.utils.Debugger.TESTING;
import mygame.utils.MathHelper;
import mygame.utils.Reference;
import static mygame.utils.Reference.chunkSize;

public class WorldProvider extends AbstractAppState {

    public final static int MAXX = 20, MAXY = 20, MAXZ = 20;
    public static Chunk[] chunks = new Chunk[MAXX * MAXY * MAXZ];

    boolean worldGenerated = false;

    Main app;
    Random rand = new Random();

    public static int pX, pY, pZ; //player coords in chunks

    PlayerControlState playerControl;

    public static byte renderDistance = 10;
    public boolean updateChunks = true;
    //ChunkUnloader unloader = new ChunkUnloader();
    File fChunk;

    public ConcurrentLinkedQueue<Integer> loaded = new ConcurrentLinkedQueue<>();

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
    }

    public void preload() {
        Reference.executor.submit(chunkManager);

        if (TESTING) {
            updateChunks = false;
            //PUT TEST CODE IN HERE

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    chunks[MathHelper.flat3Dto1D(i, 0, j)] = new Chunk(i, 0, j);
                    chunks[MathHelper.flat3Dto1D(i, 0, j)].genTerrain();
                    chunks[MathHelper.flat3Dto1D(i, 0, j)].processCells();
                    chunks[MathHelper.flat3Dto1D(i, 0, j)].load();
                    chunks[MathHelper.flat3Dto1D(i, 0, j)].loadPhysics();
                }
            }
        }

    }

    public void setCell(Cell c, CellId id) {
        this.setCell(c.x, c.y, c.z, id);
    }

    public void setCell(Vector3f v, CellId id) {
        this.setCell((int) v.x, (int) v.y, (int) v.z, id);
    }

    public void setCell(float i, float j, float k, CellId id) {
        this.setCell((int) i, (int) j, (int) k, id);
    }

    //replaces the Cell.setId(id), and replaces making all the cell air when chunk is created. Commento storico del 2016
    public void setCell(int i, int j, int k, CellId id) {
        //System.out.println("Cell being placed in world coords: " + i + ", " + j + ", " + k);

        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        if (chunkX < MAXX && chunkY < MAXY && chunkZ < MAXZ) {
            if (chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] != null) {
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].setCell(plusX, plusY, plusZ, id);
            } else {
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] = new Chunk(chunkX, chunkY, chunkZ);
                chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].setCell(plusX, plusY, plusZ, id);
            }
            chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].unload(); //unloaded here, so next update cycle it will be showed
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

        //try {
        if (chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] != null) {
            return chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].getCell(plusX, plusY, plusZ);

        }
        /*} catch (Exception e1) {
            return null;

        }*/
        return null;
    }

    //returns the chunk is the specified coords
    public Chunk getChunk(int i, int j, int k) {
        int plusX = i % chunkSize, plusY = j % chunkSize, plusZ = k % chunkSize;
        int chunkX = (i - plusX) / chunkSize, chunkY = (j - plusY) / chunkSize, chunkZ = (k - plusZ) / chunkSize;

        return chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)];

    }

    public Chunk getChunk(Vector3f v) {
        return this.getChunk((int) v.x, (int) v.y, (int) v.z);
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public Cell getCellFromVertices(ArrayList<Vector3f> al) {
        Vector3f v = MathHelper.lowestVectorInList(al.get(0), al.get(1), al.get(2), al.get(3));
        System.out.println(v);
        if (al.get(0).x == al.get(1).x && al.get(0).x == al.get(2).x && al.get(0).x == al.get(3).x) {
            if (getCell(v) != null || getCell(v).id != CellId.AIR) {
                return getCell(v);
            } else {
                return getCell(v.x - 1, v.y, v.z);
            }
        }
        return null;
    }

    final Callable<Object> chunkManager = new Callable<Object>() {
        File f;

        @Override
        public Object call() {

            while (updateChunks) {
                for (int i = pX - renderDistance; i <= pX + renderDistance; i++) {
                    for (int j = pY - renderDistance; j <= pY + renderDistance; j++) {
                        for (int k = pZ - renderDistance; k <= pZ + renderDistance; k++) {

                            if (i >= 0 && j >= 0 && k >= 0 && i < MAXX && j < MAXY && k < MAXZ) {
                                if (chunks[MathHelper.flat3Dto1D(i, j, k)] != null) {
                                    chunks[MathHelper.flat3Dto1D(i, j, k)].processCells();
                                } else if (chunks[MathHelper.flat3Dto1D(i, j, k)] == null) {
                                    if (j == 0) {
                                        chunks[MathHelper.flat3Dto1D(i, j, k)] = new Chunk(i, j, k);
                                        chunks[MathHelper.flat3Dto1D(i, j, k)].genTerrain();
                                        //System.out.println("Generated chunk at " + i + ", " + j + ", " + k);
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
}
