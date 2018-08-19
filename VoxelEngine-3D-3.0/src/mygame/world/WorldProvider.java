package mygame.world;

import mygame.control.PlayerControlState;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import mygame.block.Cell;
import mygame.block.CellId;
import mygame.Main;
import mygame.utils.Reference;
import static mygame.utils.Reference.chunkSize;

public class WorldProvider extends AbstractAppState {

    Main app;
    Random rand = new Random();

    private final static int MAXX = 20, MAXY = 20, MAXZ = 20;

    public static Chunk[][][] chunks;

    //public static Chunk[][][] chunks = new Chunk[MAXX][MAXY][MAXZ];
    boolean worldGenerated = false;

    public int pX, pY, pZ; //player coords
    PlayerControlState playerControl;

    public byte renderDistance = 8;
    public byte interactionDistance = (byte) (renderDistance * 1.5);

    int updateX, updateY, updateZ;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        playerControl = this.app.getStateManager().getState(PlayerControlState.class);
    }

    @Override
    public void update(float tpf) {

        if (this.app.getStateManager().getState(PlayerControlState.class).isEnabled()) {
            pX = playerControl.getX() / chunkSize;
            pY = playerControl.getY() / chunkSize;
            pZ = playerControl.getZ() / chunkSize;
        } else {
            pX = (int) app.getCamera().getLocation().getX() / chunkSize;
            pY = (int) app.getCamera().getLocation().getY() / chunkSize;
            pZ = (int) app.getCamera().getLocation().getZ() / chunkSize;
        }

        for (int i = 0; i < MAXX; i++) {
            for (int j = 0; j < MAXY; j++) {
                for (int k = 0; k < MAXZ; k++) {
                    try {
                        if (Math.sqrt((Math.pow(pX - i, 2) + Math.pow(pY - j, 2) + Math.pow(pZ - k, 2))) <= renderDistance) {
                            if (chunks[i][j][k] != null) {
                                if (chunks[i][j][k].isEmpty()) {
                                    chunks[i][j][k] = null;
                                } else {
                                    if (!chunks[i][j][k].loaded || chunks[i][j][k].firstLoad) {
                                        chunks[i][j][k].update();
                                    }

                                    chunks[i][j][k].load();
                                }

                                try {
                                    if (chunks[i][j][k].x == pX && chunks[i][j][k].z == pZ || chunks[i][j][k].x == pX && chunks[i][j][k].z == pZ + 1
                                            || chunks[i][j][k].x == pX && chunks[i][j][k].z == pZ - 1 || chunks[i][j][k].x == pX + 1 && chunks[i][j][k].z == pZ
                                            || chunks[i][j][k].x == pX - 1 && chunks[i][j][k].z == pZ) {
                                        chunks[i][j][k].loadPhysics();
                                    } else {
                                        chunks[i][j][k].unloadPhysics();
                                    }
                                } catch (Exception e2) {
                                }
                            } else {
                                /*File file = new File(System.getProperty("user.dir") + "/chunks/" + i + "-" + j + "-" + k + ".chunk");
                                if (!file.exists()) {*/
                                    if (j == 0) {
                                        chunks[i][j][k] = new Chunk(i, j, k);
                                        chunks[i][j][k].genTerrain();
                                    }
                                /*} else {
                                    loadChunkFromFile(i, j, k);
                                }*/
                            }
                        } else {
                            chunks[i][j][k].unload();
                            /*if (Math.sqrt((Math.pow(pX - i, 2) + Math.pow(pY - j, 2) + Math.pow(pZ - k, 2))) > renderDistance * 2) {
                                Reference.fileHelper.chunkToJson(chunks[i][j][k].cells, new File(System.getProperty("user.dir") + "/chunks/" + i + "-" + j + "-" + k + ".chunk"));
                                chunks[i][j][k] = null;
                            }*/
                        }

                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void loadChunkFromFile(int chunkX, int chunkY, int chunkZ) {

        chunks[chunkX][chunkY][chunkZ] = new Chunk(chunkX, chunkY, chunkZ);
        chunks[chunkX][chunkY][chunkZ].cells = Reference.fileHelper.jsonToChunk(new File(System.getProperty("user.dir") + "/chunks/" + chunkX + "-" + chunkY + "-" + chunkZ + ".chunk"));
        //chunks[chunkX][chunkY][chunkZ].genBase();
        chunks[chunkX][chunkY][chunkZ].update();
        chunks[chunkX][chunkY][chunkZ].load();
    }

    public void preload() {
        /*DO NOT TOUCH THIS LINE*/
        chunks = new Chunk[MAXX][MAXY][MAXZ];
        /*FROM HERE BELOW YOU CAN MODIFY*/

        //Reference.fileHelper.chunkToJson(chunks[0][0][0].cells, new File(System.getProperty("user.dir") + "/chunks/" + 0 + "-" + 0 + "-" + 0 + ".chunk"));
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

    public Cell getCellFromVertices(ArrayList<Vector3f> al) {
        for (int i = 0; i < MAXX; i++) {
            for (int j = 0; j < MAXY; j++) {
                for (int k = 0; k < MAXZ; k++) {
                    if (chunks[i][j][k] != null) {
                        for (Cell c : chunks[i][j][k].cells) {
                            if (c.allVertices.containsAll(al) && c.id != CellId.AIR) {
                                return c;
                            }
                        }
                    }
                }
            }
        }
        return null;
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

}
