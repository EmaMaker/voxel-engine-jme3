package mygame.world;

import mygame.utils.Reference;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.ArrayList;
import mygame.block.Cell;
import mygame.block.CellId;
import static mygame.utils.Reference.chunkSize;
import mygame.utils.math.SimplexNoise;

public class Chunk {

    transient boolean toBeUpdated = true;
    transient boolean loaded = false;
    transient boolean phyLoaded = false;

    //the chunk coords in the world
    public int x, y, z;

    //the cells contained in the chunk, as an arraylist. using a three-dimensional array would cause to json-serialization to retrive StackOverflowException
    public ArrayList<Cell> cells = new ArrayList<>();

    public ChunkMesh chunkMesh = new ChunkMesh(this);
    Vector3f pos = new Vector3f();
    Geometry chunkGeom;
    Node node = new Node();
    long t;

    public Chunk() {
        this(0, 0, 0);
    }

    public Chunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        pos = new Vector3f(x * chunkSize, y * chunkSize, z * chunkSize);
        chunkGeom = new Geometry(this.toString(), chunkMesh);
        chunkGeom.setMaterial(Reference.mat);
        node.setLocalTranslation(pos);
    }

    public void update() {

        if (toBeUpdated) {
            //t = System.currentTimeMillis();
            for (Cell c : cells) {
                c.update();
            }

            dumbGreedy();
            chunkMesh.set();

            chunkMesh.indicesList.clear();
            chunkMesh.verticesList.clear();
            chunkMesh.textureList.clear();
            //this.unload();

            toBeUpdated = false;
            //System.out.println("Updating " + this + " took " + (System.currentTimeMillis() - t) + " ms");
        }

    }

    public void load() {
        if (!loaded && !toBeUpdated /* Reference.app.getCamera().contains(node.getWorldBound()) == FrustumIntersect.Inside || Reference.app.getCamera().contains(node.getWorldBound()) == FrustumIntersect.Interstects*/) {
            loaded = true;
            node.attachChild(chunkGeom);
            Reference.terrainNode.attachChild(node);
        }
    }

    public void unload() {
        if (loaded) {
            loaded = false;
            node.detachChild(chunkGeom);
            Reference.terrainNode.detachChild(node);
        }
    }

    //sets all the blocks in the bottom layer (relative y = 0) to grass
    public void genBase() {
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                setCell(i, 0, j, CellId.GRASS);
            }
        }
    }

    //returns the Cell object of the cells[i][j][k]. Could return null if index is null
    public Cell getCell(int x, int y, int z) {
        for (Cell c : cells) {
            if (c.x == x && c.y == y && c.z == z) {
                return c;
            }
        }
        return null;
    }

    //sets the cells index at x,y,z to the given ID, if index is null, it creates a new cell
    public void setCell(int x, int y, int z, CellId id) {
        boolean added = false;
        for (Cell c : cells) {
            if (c.x == x && c.y == y && c.z == z) {
                c.setId(id);
                added = true;
            } else {
                c = null;
            }
        }
        if (!added) {
            cells.add(new Cell(id, x, y, z, this.x, this.y, this.z));
        }

        toBeUpdated = true;
    }

    public void loadPhysics() {
        if (!phyLoaded) {
            node.addControl(new RigidBodyControl(CollisionShapeFactory.createMeshShape(node), 0f));
            Reference.main.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(node);
            phyLoaded = true;
        }
    }

    public void unloadPhysics() {
        if (phyLoaded) {
            phyLoaded = false;

            node.getControl(RigidBodyControl.class).setEnabled(false);
            node.removeControl(node.getControl(RigidBodyControl.class));
            Reference.main.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(node);
        }
    }

    public void refreshPhysics() {
        unloadPhysics();
        loadPhysics();

    }

    public void copyAll(ArrayList<Cell> al) {
        cells = new ArrayList<>();
        for (Cell c : al) {
            setCell(c.x, c.y, c.z, c.id);
        }
    }

    //checks if all the cells in the list are null. Actualy this is a smart thing because if the array has even one cell, it suddently return true, otherwhise the loop is not even executed and returns false
    public boolean isEmpty() {
        for (Cell c : cells) {
            if (c.id != CellId.AIR) {
                return false;
            }
        }
        return true;
    }

    //System.out.println(Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025)));
    public void genTerrain() {

        for (int i = 0; i < chunkSize; i++) {
            for (int k = 0; k < chunkSize; k++) {
                for (int a = 0; a <= Math.abs(SimplexNoise.noise((x * chunkSize + i) * 0.005, (z * chunkSize + k) * 0.005)) * 10; a++) {
                    setCell(i, a, k, CellId.GRASS);
                }
            }
        }

        toBeUpdated = true;
    }

    public void dumbGreedy() {
        dumbGreedyWestEast(false);
        dumbGreedyWestEast(true);
        dumbGreedyNorthSouth(false);
        dumbGreedyNorthSouth(true);
        dumbGreedyTopBottom(true);
        dumbGreedyTopBottom(false);

        toBeUpdated = true;
    }

    public void dumbGreedyWestEast(boolean backface) {
        Vector3f v0, v1, v2, v3;
        int i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 0 : 1;

        for (Cell c : cells) {
            offX = backface ? 1 : 0;
            offY = 0;
            offZ = 0;
            //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

            if (c.id != CellId.AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                startX = c.x;
                startY = c.y;
                startZ = c.z;

                done = false;

                while (!done) {
                    c1 = getCell(startX, startY + offY, startZ - 1);
                    if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                        startZ--;
                        offZ++;
                        c1.meshed[indexOfSide] = true;
                        //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                    } else {
                        while (!done) {
                            c1 = getCell(startX, startY + offY, startZ + offZ);
                            if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id) {
                                c1.meshed[indexOfSide] = true;
                                //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                offZ++;
                            } else {
                                while (!done) {
                                    offY++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX, startY + offY, k);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX, startY + offY, k);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }

                                done = false;
                                while (!done) {
                                    startY--;
                                    offY++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX, startY, k);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            startY++;
                                            offY--;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX, startY, k);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }
                                done = true;
                            }
                        }
                    }
                }

                v0 = new Vector3f(startX + offX, startY, startZ);
                v1 = new Vector3f(startX + offX, startY + offY, startZ);
                v2 = new Vector3f(startX + offX, startY + offY, startZ + offZ);
                v3 = new Vector3f(startX + offX, startY, startZ + offZ);

                i0 = chunkMesh.addVertex(v0);
                i1 = chunkMesh.addVertex(v1);
                i2 = chunkMesh.addVertex(v2);
                i3 = chunkMesh.addVertex(v3);

                chunkMesh.textureList.add(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i1, new Vector3f(0, offY, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i2, new Vector3f(offZ, offY, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i3, new Vector3f(offZ, 0, c.offsets[indexOfSide]));

                if (backface) {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i0);
                } else {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i0);
                }
            }
        }
    }

    public void dumbGreedyNorthSouth(boolean backface) {
        Vector3f v0, v1, v2, v3;
        int i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 2 : 3;

        for (Cell c : cells) {
            offX = 0;
            offY = 0;
            offZ = backface ? 1 : 0;
            //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

            if (c.id != CellId.AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                startX = c.x;
                startY = c.y;
                startZ = c.z;

                done = false;

                while (!done) {
                    c1 = getCell(startX - 1, startY + offY, startZ);
                    if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                        startX--;
                        offX++;
                        c1.meshed[indexOfSide] = true;
                        //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                    } else {
                        while (!done) {
                            c1 = getCell(startX + offX, startY + offY, startZ);
                            if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id) {
                                c1.meshed[indexOfSide] = true;
                                //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                offX++;
                            } else {
                                while (!done) {
                                    offY++;
                                    for (int k = startX; k < startX + offX; k++) {
                                        c1 = getCell(k, startY + offY, startZ);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startX; k < startX + offX; k++) {
                                            c1 = getCell(k, startY + offY, startZ);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }

                                done = false;
                                while (!done) {
                                    startY--;
                                    offY++;
                                    for (int k = startX; k < startX + offX; k++) {
                                        c1 = getCell(k, startY + offY, startZ);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            startY++;
                                            offY--;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startX; k < startX + offX; k++) {
                                            c1 = getCell(k, startY + offY, startZ);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }
                                done = true;
                            }
                        }
                    }
                }

                v0 = new Vector3f(startX, startY, startZ + offZ);
                v1 = new Vector3f(startX, startY + offY, startZ + offZ);
                v2 = new Vector3f(startX + offX, startY + offY, startZ + offZ);
                v3 = new Vector3f(startX + offX, startY, startZ + offZ);

                i0 = chunkMesh.addVertex(v0);
                i1 = chunkMesh.addVertex(v1);
                i2 = chunkMesh.addVertex(v2);
                i3 = chunkMesh.addVertex(v3);

                chunkMesh.textureList.add(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i1, new Vector3f(0, offY, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i2, new Vector3f(offX, offY, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i3, new Vector3f(offX, 0, c.offsets[indexOfSide]));

                if (backface) {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i0);
                } else {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i0);
                }
            }
        }
    }

    public void dumbGreedyTopBottom(boolean backface) {
        Vector3f v0, v1, v2, v3;
        int i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 4 : 5;

        for (Cell c : cells) {
            offX = 0;
            offY = backface ? 1 : 0;
            offZ = 0;
            //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

            if (c.id != CellId.AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                startX = c.x;
                startY = c.y;
                startZ = c.z;

                done = false;

                while (!done) {
                    c1 = getCell(startX + offX, startY, startZ - 1);
                    if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                        startZ--;
                        offZ++;
                        c1.meshed[indexOfSide] = true;
                        //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                    } else {
                        while (!done) {
                            c1 = getCell(startX + offX, startY, startZ + offZ);
                            if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.AIR && c1.id == c.id) {
                                c1.meshed[indexOfSide] = true;
                                //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                offZ++;
                            } else {
                                while (!done) {
                                    offX++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX + offX, startY, k);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX + offX, startY, k);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }

                                done = false;
                                while (!done) {
                                    startX--;
                                    offX++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX, startY, k);

                                        if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
                                            done = true;
                                            startX++;
                                            offX--;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX, startY, k);
                                            c1.meshed[indexOfSide] = true;
                                        }
                                    }
                                }
                                done = true;
                            }
                        }
                    }
                }

                v0 = new Vector3f(startX, startY + offY, startZ);
                v1 = new Vector3f(startX + offX, startY + offY, startZ);
                v2 = new Vector3f(startX + offX, startY + offY, startZ + offZ);
                v3 = new Vector3f(startX, startY + offY, startZ + offZ);

                i0 = chunkMesh.addVertex(v0);
                i1 = chunkMesh.addVertex(v1);
                i2 = chunkMesh.addVertex(v2);
                i3 = chunkMesh.addVertex(v3);

                chunkMesh.textureList.add(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i1, new Vector3f(0, offX, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i2, new Vector3f(offZ, offX, c.offsets[indexOfSide]));
                chunkMesh.textureList.add(i3, new Vector3f(offZ, 0, c.offsets[indexOfSide]));

                if (backface) {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i0);
                } else {
                    chunkMesh.indicesList.add(i0);
                    chunkMesh.indicesList.add(i1);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i2);
                    chunkMesh.indicesList.add(i3);
                    chunkMesh.indicesList.add(i0);
                }
            }
        }
    }
}
