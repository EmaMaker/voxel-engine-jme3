package mygame.world;

import mygame.control.PlayerControlState;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.block.CellId;
import mygame.Main;
import mygame.block.Cell;
import static mygame.utils.Debugger.TESTING;
import mygame.utils.MathHelper;
import mygame.utils.Reference;
import static mygame.utils.Reference.chunkSize;

public class WorldProvider extends AbstractAppState {

    Main app;
    Random rand = new Random();

    public final static int MAXX = 20, MAXY = 20, MAXZ = 20;

    //public static Chunk[][][] chunks = new Chunk[MAXX][MAXY][MAXZ];
    public static Chunk[] chunks = new Chunk[MAXX * MAXY * MAXZ];

    boolean worldGenerated = false;

    public static int pX, pY, pZ; //player coords in chunks

    PlayerControlState playerControl;

    public static byte renderDistance = 8;
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

        if (updateChunks) {
            /*  for (int i = 0; i < MAXX; i++) {
                for (int j = 0; j < MAXY; j++) {
                    for (int k = 0; k < MAXZ; k++) {
                        int index = MathHelper.flat3Dto1D(i, j, k);

                        if (Math.sqrt(Math.pow(i - pX, 2) + Math.pow(j - pY, 2) + Math.pow(k - pZ, 2)) < renderDistance) {
                            if (chunks[index] != null) {
                                chunks[index].load();
                            }
                        } else {
                            if (chunks[index] != null) {
                                chunks[index].unload();
                                //chunks[index] = null;
                            }
                        }
                    }
                }
            }*/
        }
    }

    public void preload() {
        if (TESTING) {
            updateChunks = false;
        }

        Reference.executor.submit(chunkManager);
        if (TESTING) {
            //PUT TEST CODE IN HERE
            chunks[MathHelper.flat3Dto1D(0, 0, 0)] = new Chunk(0, 0, 0);
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].genTerrain();
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].processCells();
            chunks[MathHelper.flat3Dto1D(0, 0, 0)].load();
        }

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

        try {
            if (chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)] != null) {
                return chunks[MathHelper.flat3Dto1D(chunkX, chunkY, chunkZ)].getCell(plusX, plusY, plusZ);

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

    final Callable<Object> chunkManager = new Callable<Object>() {
        File f;

        @Override
        public Object call() {

            while (updateChunks) {
                //System.out.println("ciao1");
                for (int i = pX - renderDistance; i <= pX + renderDistance; i++) {
                    for (int j = pY - renderDistance; j <= pY + renderDistance; j++) {
                        for (int k = pZ - renderDistance; k <= pZ + renderDistance; k++) {

                            if (i >= 0 && j >= 0 && k >= 0 && i < MAXX && j < MAXY && k < MAXZ) {
                                if (chunks[MathHelper.flat3Dto1D(i, j, k)] != null) {
                                    chunks[MathHelper.flat3Dto1D(i, j, k)].processCells();
                                } else {

                                    chunks[MathHelper.flat3Dto1D(i, j, k)] = new Chunk(i, j, k);
                                    f = Paths.get(System.getProperty("user.dir") + "/chunks/" + i + "-" + j + "-" + k + ".chunk").toFile();

                                    if (!f.exists()) {
                                        if (j == 0) {
                                            chunks[MathHelper.flat3Dto1D(i, j, k)].genTerrain();
                                        }
                                    } else {
                                        try {
                                            chunks[MathHelper.flat3Dto1D(i, j, k)].cells = Reference.mapper.readValue(f, Cell[].class);
                                            f.delete();
                                        } catch (IOException ex) {
                                            Logger.getLogger(WorldProvider.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }

                                    /*if (f.exists()) {

                                            FileInputStream file = new FileInputStream(f);
                                            ObjectInputStream in = new ObjectInputStream(file);

                                            // Method for deserialization of object 
                                            Cell[] cells = (Cell[]) in.readObject();
                                            chunks[MathHelper.flat3Dto1D(i, j, k)].cells = cells;
                                            in.close();
                                            file.close();
                                        } else {
                                            if (j == 0) {
                                                chunks[MathHelper.flat3Dto1D(i, j, k)].genTerrain();
                                            }
                                        }*/
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    };

    /*public class ChunkUnloader implements Callable<Object> {

        @Override
        public Object call() throws Exception {
            Chunk c;
            while (updateChunks) {

                Iterator<Chunk> iter = loaded.iterator();
                while (iter.hasNext()) {
                    c = iter.next();
                    if (Math.sqrt(Math.pow(c.x - pX, 2) + Math.pow(c.y - pY, 2) + Math.pow(c.z - pZ, 2)) > renderDistance) {
                        FileOutputStream stream = new FileOutputStream(fChunk = Paths.get(System.getProperty("user.dir") + "/chunks/" + c.x + "-" + c.y + "-" + c.z).toFile());

                        try {
                            stream.write(serialize(chunks[c.x][c.y][c.z].cells));
                        } finally {
                            chunks[c.x][c.y][c.z] = null;
                            iter.remove();
                            stream.close();
                        }

                    }
                }
            }
            return null;
        }
    }*/
}
