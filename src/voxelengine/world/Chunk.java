package voxelengine.world;

import com.jme3.bounding.BoundingSphere;
import voxelengine.utils.Globals;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import voxelengine.block.Cell;
import voxelengine.block.CellId;
import voxelengine.utils.math.MathHelper;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.utils.Globals.debug;
import voxelengine.utils.math.SimplexNoise;
import static voxelengine.world.WorldProvider.pX;
import static voxelengine.world.WorldProvider.pY;
import static voxelengine.world.WorldProvider.pZ;
import static voxelengine.world.WorldProvider.renderDistance;

public class Chunk extends AbstractControl {

    boolean toBeSet = true;
    boolean loaded = false;
    boolean phyLoaded = false;

    //the chunk coords in the world
    public int x, y, z;

    //the cells contained in the chunk, as an arraylist. using a three-dimensional array would cause to json-serialization to retrive StackOverflowException
    public Cell[] cells = new Cell[chunkSize * chunkSize * chunkSize];

    public ChunkMesh chunkMesh = new ChunkMesh(this);
    public Geometry chunkGeom;
    Vector3f pos = new Vector3f();

    public Chunk() {
        this(0, 0, 0);
    }

    public Chunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        pos = new Vector3f(x * chunkSize, y * chunkSize, z * chunkSize);
        chunkGeom = new Geometry(this.toString(), chunkMesh);
        chunkGeom.setMaterial(Globals.mat);
        Globals.terrainNode.addControl((AbstractControl) this);
        chunkGeom.setLocalTranslation(pos);

    }

    public void processCells() {
        if (toBeSet) {
            //unload();
            /*chunkMesh = new ChunkMesh(this);
            chunkGeom = new Geometry(this.toString(), chunkMesh);
            chunkGeom.setMaterial(Globals.mat);
             */
            if (Thread.currentThread() == Globals.engine.mainThread) {
                chunkMesh = new ChunkMesh(this);
                chunkGeom.setMesh(chunkMesh);
            }

            debug("Updating " + this.toString());

            for (Cell cell : cells) {
                if (cell != null) {
                    cell.update();
                }
            }

            dumbGreedy();
            chunkMesh.set();

            toBeSet = false;
            loaded = false;
        }
    }

    public void load() {
        chunkGeom.setModelBound(new BoundingSphere(chunkSize * 2f, new Vector3f(x + (chunkSize / 2), y + (chunkSize / 2), z + (chunkSize / 2))));
        if (!loaded && (Globals.main.getCamera().contains(chunkGeom.getWorldBound()) == Camera.FrustumIntersect.Inside
                || Globals.main.getCamera().contains(chunkGeom.getWorldBound()) == Camera.FrustumIntersect.Intersects)) {
            loaded = true;
            Globals.terrainNode.attachChild(chunkGeom);
        } else if (!loaded) {
            loaded = true;
            Globals.terrainNode.attachChild(chunkGeom);
        }
    }

    public void unload() {
        if (loaded) {
            loaded = false;
            Globals.terrainNode.detachChild(chunkGeom);
        }
    }

    //sets all the blocks in the bottom layer (relative y = 0) to grass
    public void genBase() {
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                setCell(i, 0, j, CellId.ID_GRASS);
            }
        }
    }

    public void loadPhysics() {
        if (!phyLoaded && Globals.phyEnabled()) {
            try {
                this.chunkGeom.addControl(new RigidBodyControl(CollisionShapeFactory.createMeshShape(chunkGeom), 0f));
                Globals.main.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(chunkGeom.getControl(RigidBodyControl.class));
                phyLoaded = true;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public void unloadPhysics() {
        if (phyLoaded && Globals.phyEnabled()) {
            phyLoaded = false;

            chunkGeom.getControl(RigidBodyControl.class).setEnabled(false);
            chunkGeom.removeControl(chunkGeom.getControl(RigidBodyControl.class));
            Globals.main.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(chunkGeom);
        }
    }

    public void refreshPhysics() {
        unloadPhysics();
        loadPhysics();

    }

    public void genCube() {
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                for (int k = 0; k < chunkSize; k++) {
                    setCell(i, j, k, CellId.ID_GRASS);
                    this.markForUpdate(true);
                }
            }
        }
    }

    //System.out.println(Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025)));
    public void genTerrain() {
        for (int i = 0; i < chunkSize; i++) {
            for (int k = 0; k < chunkSize; k++) {
                for (int a = 0; a <= Math.abs(SimplexNoise.noise((x * chunkSize + i) * 0.01, (z * chunkSize + k) * 0.01)) * 10; a++) {
                    setCell(i, a, k, CellId.ID_GRASS);
                }
            }
        }
        markForUpdate(true);
    }

    //returns the Cell object of the cells[i][j][k]. Could return null if index is null
    public Cell getCell(int i, int j, int k) {
        if (i >= 0 && j >= 0 && k >= 0 && i < chunkSize && j < chunkSize && k < chunkSize) {
            return cells[MathHelper.flat3Dto1D(i, j, k)];
        }
        return null;
    }

    //sets the cells index at x,y,z to the given ID, if index is null, it creates a new cell
    public void setCell(int i, int j, int k, int id) {
        try {
            if (cells[MathHelper.flat3Dto1D(i, j, k)] != null) {
                cells[MathHelper.flat3Dto1D(i, j, k)].setId(id);
                markForUpdate(true);
            } else {
                cells[MathHelper.flat3Dto1D(i, j, k)] = new Cell(id, i, j, k, this);
                markForUpdate(true);
            }
        } catch (Exception e) {

        }
    }

    File f;

    @Override
    protected void controlUpdate(float tpf) {
        if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) < renderDistance) {
            this.load();
            if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) <= 1) {
                this.refreshPhysics();
            } else {
                this.unloadPhysics();
            }

        } else {
            this.unload();
            this.unloadPhysics();

            if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) > renderDistance * 1.5f) {
                saveToFile();
            }
        }
    }

    public void saveToFile() {
        f = Paths.get(Globals.workingDir + x + "-" + y + "-" + z + ".chunk").toFile();

        if (!f.exists() && !isEmpty()) {
            try {
                PrintWriter writer = new PrintWriter(f);
                for (int i = 0; i < cells.length; i++) {
                    if (cells[i] != null) {
                        writer.println(cells[i].x + "," + cells[i].y + "," + cells[i].z
                                + "," + cells[i].id);
                    }
                }

                Globals.terrainNode.removeControl(this);
                WorldProvider.chunks[MathHelper.flat3Dto1D(x, y, z)] = null;
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isEmpty() {
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != null) {
                return false;
            }
        }
        return true;
    }

    public void markForUpdate(boolean b) {
        toBeSet = b;
    }

    /*THIS CODE HIS SUPER UGLY AND IT'S NOT NEEDED TO BE SO LONG. BUT IT ACTUALLY WORKS. AT LEAST BUGS HAVE BEEN FIXED*/
    public void dumbGreedy() {
        debug("Dumb gredding " + this);
        dumbGreedyWestEast(false);
        dumbGreedyWestEast(true);
        dumbGreedyNorthSouth(false);
        dumbGreedyNorthSouth(true);
        dumbGreedyTopBottom(true);
        dumbGreedyTopBottom(false);

        markForUpdate(true);
        debug("End dumb gredding " + this);
    }

    public void dumbGreedyWestEast(boolean backface) {
        Vector3f v0, v1, v2, v3;
        short i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 0 : 1;

        for (Cell c : cells) {
            if (c != null && c.id != CellId.ID_AIR) {
                offX = backface ? 1 : 0;
                offY = 0;
                offZ = 0;
                //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

                if (c.id != CellId.ID_AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                    startX = c.x;
                    startY = c.y;
                    startZ = c.z;

                    done = false;

                    while (!done) {
                        c1 = getCell(startX, startY + offY, startZ - 1);
                        if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                            startZ--;
                            offZ++;
                            c1.meshed[indexOfSide] = true;
                            //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                        } else {
                            while (!done) {
                                c1 = getCell(startX, startY + offY, startZ + offZ);
                                if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id) {
                                    c1.meshed[indexOfSide] = true;
                                    //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                    offZ++;
                                } else {
                                    while (!done) {
                                        offY++;
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX, startY + offY, k);

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                    chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i1, new Vector3f(0, offY, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i2, new Vector3f(offZ, offY, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i3, new Vector3f(offZ, 0, c.offsets[indexOfSide]));

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
    }

    public void dumbGreedyNorthSouth(boolean backface) {
        Vector3f v0, v1, v2, v3;
        short i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 2 : 3;

        for (Cell c : cells) {
            if (c != null && c.id != CellId.ID_AIR) {
                offX = 0;
                offY = 0;
                offZ = backface ? 1 : 0;
                //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

                if (c.id != CellId.ID_AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                    startX = c.x;
                    startY = c.y;
                    startZ = c.z;

                    done = false;

                    while (!done) {
                        c1 = getCell(startX - 1, startY + offY, startZ);
                        if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                            startX--;
                            offX++;
                            c1.meshed[indexOfSide] = true;
                            //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                        } else {
                            while (!done) {
                                c1 = getCell(startX + offX, startY + offY, startZ);
                                if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id) {
                                    c1.meshed[indexOfSide] = true;
                                    //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                    offX++;
                                } else {
                                    while (!done) {
                                        offY++;
                                        for (int k = startX; k < startX + offX; k++) {
                                            c1 = getCell(k, startY + offY, startZ);

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                    chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i1, new Vector3f(0, offY, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i2, new Vector3f(offX, offY, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i3, new Vector3f(offX, 0, c.offsets[indexOfSide]));

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

    public void dumbGreedyTopBottom(boolean backface) {
        Vector3f v0, v1, v2, v3;
        short i0, i1, i2, i3;
        boolean done;
        int startX, startY, startZ;
        int offX, offY, offZ;
        Cell c1;

        int indexOfSide = backface ? 4 : 5;

        for (Cell c : cells) {
            if (c != null && c.id != CellId.ID_AIR) {
                offX = 0;
                offY = backface ? 1 : 0;
                offZ = 0;
                //System.out.println(c + " at " + c.x + ", " + c.y + ", " + c.z + "    (" + c.sides[indexOfSide] + ")    (" + c.meshed[indexOfSide] + ")");

                if (c.id != CellId.ID_AIR && c.sides[indexOfSide] && !c.meshed[indexOfSide]) {
                    startX = c.x;
                    startY = c.y;
                    startZ = c.z;

                    done = false;

                    while (!done) {
                        c1 = getCell(startX + offX, startY, startZ - 1);
                        if (c1 != null && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id && c1.sides[indexOfSide]) {
                            startZ--;
                            offZ++;
                            c1.meshed[indexOfSide] = true;
                            //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z-");
                        } else {
                            while (!done) {
                                c1 = getCell(startX + offX, startY, startZ + offZ);
                                if (c1 != null && c1.sides[indexOfSide] && !c1.meshed[indexOfSide] && c1.id != CellId.ID_AIR && c1.id == c.id) {
                                    c1.meshed[indexOfSide] = true;
                                    //System.out.println(c1 + " at " + c1.x + ", " + c1.y + ", " + c1.z + " can be added z+");
                                    offZ++;
                                } else {
                                    while (!done) {
                                        offX++;
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX + offX, startY, k);

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                                            if (c1 == null || c1.meshed[indexOfSide] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[indexOfSide]) {
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

                    chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i1, new Vector3f(0, offX, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i2, new Vector3f(offZ, offX, c.offsets[indexOfSide]));
                    chunkMesh.addTextureVertex(i3, new Vector3f(offZ, 0, c.offsets[indexOfSide]));

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
}
