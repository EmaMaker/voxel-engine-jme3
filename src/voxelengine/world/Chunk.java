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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import voxelengine.block.Cell;
import voxelengine.block.CellId;
import voxelengine.utils.math.MathHelper;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.utils.Globals.debug;
import static voxelengine.utils.Globals.pX;
import static voxelengine.utils.Globals.pY;
import static voxelengine.utils.Globals.pZ;
import static voxelengine.utils.Globals.renderDistance;
import voxelengine.utils.math.SimplexNoise;

public class Chunk extends AbstractControl {

    boolean toBeSet = true;
    boolean loaded = false;
    boolean phyLoaded = false;

    //the chunk coords in the world
    public int x, y, z;

    //the cells contained in the chunk, as an arraylist. using a three-dimensional array would cause to json-serialization to retrive StackOverflowException
    public Cell[] cells = new Cell[chunkSize * chunkSize * chunkSize];

    public ChunkMesh chunkMesh = new ChunkMesh();
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

        markForUpdate(true);
    }

    public void processCells() {
        if (toBeSet) {
            debug("Updating " + this.toString());

            for (Cell cell : cells) {
                if (cell != null) {
                    cell.update();
                }
            }

            if (Thread.currentThread() == Globals.engine.mainThread) {
                /*CHECK OUT THE chunkMesh.updateBound() for a better chunkMesh managing instead of regenerating it:
                actually gives some problems with the buffers, it has to be tested*/
                chunkMesh = new ChunkMesh();

                kindaBetterGreedy();
                chunkGeom.setMesh(chunkMesh);

                chunkMesh.set();
            }
            toBeSet = false;
            loaded = false;
        }
    }

    public void load() {
        //on first load, Global material is null because it hasn't been initialized yet, so it's set here
        if (chunkGeom.getMaterial() == null) {
            chunkGeom.setMaterial(Globals.mat);
        }

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

    public Cell getCell(int i, int j, int k) {
        if (i >= 0 && j >= 0 && k >= 0 && i < chunkSize && j < chunkSize && k < chunkSize) {
            return cells[MathHelper.flat3Dto1D(i, j, k)];
        }
        return null;
    }

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

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

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
        File f = Paths.get(Globals.workingDir + x + "-" + y + "-" + z + ".chunk").toFile();

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
                WorldManager.chunks[MathHelper.flat3Dto1D(x, y, z)] = null;
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public void loadFromFile(File f) {
        List<String> lines;
        String[] datas = new String[4];

        if (f.exists()) {
            if (!(f.length() == 0)) {
                try {
                    lines = Files.readAllLines(f.toPath());

                    for (String s : lines) {
                        datas = s.split(",");
                        setCell(Integer.valueOf(datas[0]), Integer.valueOf(datas[1]), Integer.valueOf(datas[2]), Integer.valueOf(datas[3]));
                    }
                    f.delete();
                } catch (Exception e) {
                }
            } else {
                genTerrain();
            }
        } else {
            genTerrain();
        }
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

    public void kindaBetterGreedy() {
        int startX, startY, startZ, offX, offY, offZ, index;
        short i0 = 0, i1 = 0, i2 = 0, i3 = 0;
        Cell c1;
        Vector3f v0, v1, v2, v3;

        for (Cell c : cells) {

            //for every coord (x,y,z. Actually x,z,y)
            for (int s = 0; s < 3; s++) {
                //for every face (two for each coord: face and backface)
                for (int i = 0; i < 2; i++) {
                    int backfaces[] = {0, 0, 0};

                    backfaces[s] = i;
                    index = s * 2 + i;

                    if (c != null && c.id != CellId.ID_AIR && c.sides[index] && !c.meshed[index]) {

                        startX = c.x;
                        startY = c.y;
                        startZ = c.z;

                        offX = 0;
                        offY = 0;
                        offZ = 0;

                        if (s == 0 || s == 2) {
                            offZ++;
                        } else {
                            offX++;
                        }

                        c1 = getCell(startX + offX, startY + offY, startZ + offZ);
                        while (c1 != null && c1.id != CellId.ID_AIR && c1.sides[index] && !c1.meshed[index] && c.id == c1.id) {
                            if (s == 0 || s == 2) {
                                offZ++;
                            } else {
                                offX++;
                            }

                            c1.meshed[index] = true;
                            c1 = getCell(startX + offX, startY + offY, startZ + offZ);
                        }

                        if (s == 0 || s == 1) {
                            //if considering x or z axis, increment the y axis
                            offY++;
                        } else {
                            //here's considering the y axis, so increment the x
                            offX++;
                        }

                        //finished, the cell has been used!
                        c.meshed[index] = true;

                        //sets the vertices
                        switch (s) {
                            case 0:
                                v0 = new Vector3f(startX + backfaces[0], startY, startZ);
                                v1 = new Vector3f(startX + backfaces[0], startY + offY, startZ);
                                v2 = new Vector3f(startX + backfaces[0], startY + offY, startZ + offZ);
                                v3 = new Vector3f(startX + backfaces[0], startY, startZ + offZ);

                                i0 = chunkMesh.addVertex(v0);
                                i1 = chunkMesh.addVertex(v1);
                                i2 = chunkMesh.addVertex(v2);
                                i3 = chunkMesh.addVertex(v3);

                                chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[index]));
                                chunkMesh.addTextureVertex(i1, new Vector3f(0, offY, c.offsets[index]));
                                chunkMesh.addTextureVertex(i2, new Vector3f(offZ, offY, c.offsets[index]));
                                chunkMesh.addTextureVertex(i3, new Vector3f(offZ, 0, c.offsets[index]));

                                break;
                            case 1:
                                v0 = new Vector3f(startX, startY, startZ + backfaces[1]);
                                v1 = new Vector3f(startX, startY + offY, startZ + backfaces[1]);
                                v2 = new Vector3f(startX + offX, startY + offY, startZ + backfaces[1]);
                                v3 = new Vector3f(startX + offX, startY, startZ + backfaces[1]);

                                i0 = chunkMesh.addVertex(v0);
                                i1 = chunkMesh.addVertex(v1);
                                i2 = chunkMesh.addVertex(v2);
                                i3 = chunkMesh.addVertex(v3);

                                chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[index]));
                                chunkMesh.addTextureVertex(i1, new Vector3f(0, offY, c.offsets[index]));
                                chunkMesh.addTextureVertex(i2, new Vector3f(offX, offY, c.offsets[index]));
                                chunkMesh.addTextureVertex(i3, new Vector3f(offX, 0, c.offsets[index]));

                                break;
                            case 2:
                                v0 = new Vector3f(startX, startY + backfaces[2], startZ);
                                v1 = new Vector3f(startX + offX, startY + backfaces[2], startZ);
                                v2 = new Vector3f(startX + offX, startY + backfaces[2], startZ + offZ);
                                v3 = new Vector3f(startX, startY + backfaces[2], startZ + offZ);

                                i0 = chunkMesh.addVertex(v0);
                                i1 = chunkMesh.addVertex(v1);
                                i2 = chunkMesh.addVertex(v2);
                                i3 = chunkMesh.addVertex(v3);

                                chunkMesh.addTextureVertex(i0, new Vector3f(0, 0, c.offsets[index]));
                                chunkMesh.addTextureVertex(i1, new Vector3f(0, offX, c.offsets[index]));
                                chunkMesh.addTextureVertex(i2, new Vector3f(offZ, offX, c.offsets[index]));
                                chunkMesh.addTextureVertex(i3, new Vector3f(offZ, 0, c.offsets[index]));
                                break;
                            default:
                                System.out.println("puzzette");
                                break;
                        }
                        //now constructs the mesh

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
}
