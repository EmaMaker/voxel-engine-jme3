package voxelengine.world;

import voxelengine.utils.Globals;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.BufferUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import voxelengine.block.Cell;
import voxelengine.block.CellId;
import voxelengine.block.TextureManager;
import static voxelengine.utils.Globals.chunkSize;
import voxelengine.utils.math.MathHelper;
import static voxelengine.utils.Globals.debug;
import static voxelengine.utils.Globals.pX;
import static voxelengine.utils.Globals.pY;
import static voxelengine.utils.Globals.pZ;
import static voxelengine.utils.Globals.renderDistance;
import static voxelengine.world.WorldManager.MAXX;
import static voxelengine.world.WorldManager.MAXY;
import static voxelengine.world.WorldManager.MAXZ;

public class Chunk extends AbstractControl {

    boolean toBeSet = true;
    boolean loaded = false;
    boolean phyLoaded = false;
    public boolean generated = false;
    public boolean decorated = false;

    //the chunk coords in the world
    public int x, y, z;

    public Cell[] cells = new Cell[chunkSize * chunkSize * chunkSize];

    public Mesh chunkMesh = new Mesh();
    public Geometry chunkGeom;
    Vector3f pos = new Vector3f();
    Random rand = new Random();

    public Chunk() {
        this(0, 0, 0);
    }

    public Chunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        pos = new Vector3f(x * chunkSize, y * chunkSize, z * chunkSize);
        debug("Creating chunk starting at world coords" + pos.toString());
        chunkGeom = new Geometry(this.toString() + pos.toString(), chunkMesh);
        chunkGeom.setMaterial(Globals.mat);
        Globals.terrainNode.addControl((AbstractControl) this);
        chunkGeom.setLocalTranslation(pos);

        markForUpdate(true);
    }

    public void processCells() {
        if (toBeSet) {
            debug("Updating " + this.toString() + " at " + x + ", " + y + ", " + z);

            for (Cell cell : cells) {
                if (cell != null) {
                    cell.update();
                }
            }

//            unload();
            kindaBetterGreedy();
            
            toBeSet = false;
            loaded = false;

            //makes Cells with ID AIR null, to save up  bit of memory
//            for (int i = 0; i < cells.length; i++) {
//                if (cells[i] != null && cells[i].id == CellId.ID_AIR) {
//                    cells[i] = null;
//                }
//            }
        }
    }

    public void load() {
        //on first load, Global material is null because it hasn't been initialized yet, so it's set here
        if (chunkGeom.getMaterial() == null) {
            chunkGeom.setMaterial(Globals.mat);
        }

        if (!isEmpty()) {
            if (!loaded) {
                loaded = true;
                Globals.terrainNode.attachChild(chunkGeom);
                chunkGeom.setCullHint(Spatial.CullHint.Never);
            }
        } else {
            unload();
        }
    }

    public void unload() {
        if (loaded) {
            loaded = false;
            Globals.terrainNode.detachChild(chunkGeom);
        }
    }

    public void loadPhysics() {
        if (!phyLoaded && Globals.phyEnabled()) {
            try {
                this.chunkGeom.addControl(new RigidBodyControl(CollisionShapeFactory.createMeshShape(chunkGeom), 0f));
                Globals.main.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(chunkGeom.getControl(RigidBodyControl.class));
                phyLoaded = true;
            } catch (Exception e) {
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

    public Cell getHighestCellAt(int i, int j) {
        return getCell(i, getHighestYAt(i, j), j);
    }

    public int getHighestYAt(int i, int j) {
        for (int a = MAXY * chunkSize; a >= 0; a--) {
            if (getCell(i, a, j) != null) {
                return getCell(i, a, j).y;
            }
        }
        return Integer.MAX_VALUE;
    }

    public Cell getCell(int i, int j, int k) {
        if (i >= 0 && j >= 0 && k >= 0 && i < chunkSize && j < chunkSize && k < chunkSize) {
            return cells[MathHelper.flatCell3Dto1D(i, j, k)];
        }
        return null;
    }

    public void setCell(int i, int j, int k, int id) {
        if (i >= 0 && j >= 0 && k >= 0 && i < chunkSize && j < chunkSize && k < chunkSize) {
            if (cells[MathHelper.flatCell3Dto1D(i, j, k)] != null) {
                cells[MathHelper.flatCell3Dto1D(i, j, k)].setId(id);
            } else {
                cells[MathHelper.flatCell3Dto1D(i, j, k)] = new Cell(id, i, j, k, this);
            }
            markForUpdate(true);
        }
    }

    public void generate() {
        if (!generated) {
            Globals.getWorldGenerator().generate(this);
            generated = true;
        }
    }

    public void decorate() {
        if (!decorated) {
            Globals.getWorldDecorator().decorate(this);
            decorated = true;
        }
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) > renderDistance /*|| !isVisible()*/) {
            //if (rand.nextFloat() < 0.25f) {
            this.unload();
            this.unloadPhysics();

            if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) > renderDistance * 1.5f) {
                saveToFile();
            }
            //}
        } else {
            this.load();
            if (Math.sqrt(Math.pow(x - pX, 2) + Math.pow(y - pY, 2) + Math.pow(z - pZ, 2)) <= 1) {
                this.refreshPhysics();
            } else {
                this.unloadPhysics();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isEmpty() {
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != null) {
                if (cells[i].id != CellId.ID_AIR) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    //Saves the chunk to text file, with format X Y Z ID, separated by commas
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
                WorldManager.chunks[MathHelper.flatChunk3Dto1D(x, y, z)] = null;
                writer.close();
            } catch (FileNotFoundException e) {
            }
        }
    }

    //Retrieves back from the text file (X Y Z ID separated by commas)
    public void loadFromFile(File f) {
        List<String> lines;

        if (f.exists()) {
            if (!(f.length() == 0)) {
                try {
                    lines = Files.readAllLines(f.toPath());

                    for (String s : lines) {
                        setCell(Integer.valueOf(s.split(",")[0]), Integer.valueOf(s.split(",")[1]), Integer.valueOf(s.split(",")[2]), Integer.valueOf(s.split(",")[3]));
                    }

                    generated = true;
                    decorated = true;
                    markForUpdate(true);
                    f.delete();
                } catch (IOException | NumberFormatException e) {
                }
            } else {
                generated = false;
                decorated = false;
                generate();
            }
        } else {
            generated = false;
            decorated = false;
            generate();
        }
    }

    public void markForUpdate(boolean b) {
        toBeSet = b;
    }

    public boolean isVisible() {
        boolean v = false;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (x + i >= 0 && x + i < MAXX && y + j >= 0 && y + j < MAXY && z + k >= 0 && z + k < MAXZ) {
                        if (Globals.prov.chunks[MathHelper.flatChunk3Dto1D(x + i, y + j, z + k)] != null) {
                            v = true;
                        }
                    } else {
                        v = true;
                    }
                }
            }
        }
        return v;
    }

    /**
     * MESH CONSTRUCTING STUFF*
     */
    //Kinda better greedy meshing algorithm than before. Now expanding in both axis (X-Y, Z-Y, X-Z), not gonna try to connect in negative side, it's not needed
    public void kindaBetterGreedy() {
        clearAll();
        
        int startX, startY, startZ, offX, offY, offZ, index;
        short i0 = 0, i1 = 0, i2 = 0, i3 = 0;
        Cell c1;
        Vector3f v0, v1, v2, v3;
        boolean done;

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

                        done = false;
                        switch (s) {
                            case 0:
                                //offY++;
                                while (!done) {
                                    offY++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX + offX, startY + offY, k);

                                        if (c1 == null || c1.meshed[index] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[index]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX + offX, startY + offY, k);
                                            c1.meshed[index] = true;
                                        }
                                    }
                                }
                                break;
                            case 1:
                                //offY++;
                                while (!done) {
                                    offY++;
                                    for (int k = startX; k < startX + offX; k++) {
                                        c1 = getCell(k, startY + offY, startZ + offZ);

                                        if (c1 == null || c1.meshed[index] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[index]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startX; k < startX + offX; k++) {
                                            c1 = getCell(k, startY + offY, startZ + offZ);
                                            c1.meshed[index] = true;
                                        }
                                    }
                                }
                                break;
                            case 2:
                                //offX++;
                                while (!done) {
                                    offX++;
                                    for (int k = startZ; k < startZ + offZ; k++) {
                                        c1 = getCell(startX + offX, startY + offY, k);

                                        if (c1 == null || c1.meshed[index] || c1.id == CellId.ID_AIR || c1.id != c.id || !c1.sides[index]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (!done) {
                                        for (int k = startZ; k < startZ + offZ; k++) {
                                            c1 = getCell(startX + offX, startY + offY, k);
                                            c1.meshed[index] = true;
                                        }
                                    }
                                }
                                break;
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

                                i0 = addVertex(v0);
                                i1 = addVertex(v1);
                                i2 = addVertex(v2);
                                i3 = addVertex(v3);

                                addTextureVertex(i0, new Vector3f(0, 0, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i1, new Vector3f(0, offY, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i2, new Vector3f(offZ, offY, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i3, new Vector3f(offZ, 0, TextureManager.textures.get(c.id)[index]));

                                break;
                            case 1:
                                v0 = new Vector3f(startX, startY, startZ + backfaces[1]);
                                v1 = new Vector3f(startX, startY + offY, startZ + backfaces[1]);
                                v2 = new Vector3f(startX + offX, startY + offY, startZ + backfaces[1]);
                                v3 = new Vector3f(startX + offX, startY, startZ + backfaces[1]);

                                i0 = addVertex(v0);
                                i1 = addVertex(v1);
                                i2 = addVertex(v2);
                                i3 = addVertex(v3);

                                addTextureVertex(i0, new Vector3f(0, 0, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i1, new Vector3f(0, offY, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i2, new Vector3f(offX, offY, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i3, new Vector3f(offX, 0, TextureManager.textures.get(c.id)[index]));

                                break;
                            case 2:
                                v0 = new Vector3f(startX, startY + backfaces[2], startZ);
                                v1 = new Vector3f(startX + offX, startY + backfaces[2], startZ);
                                v2 = new Vector3f(startX + offX, startY + backfaces[2], startZ + offZ);
                                v3 = new Vector3f(startX, startY + backfaces[2], startZ + offZ);

                                i0 = addVertex(v0);
                                i1 = addVertex(v1);
                                i2 = addVertex(v2);
                                i3 = addVertex(v3);

                                addTextureVertex(i0, new Vector3f(0, 0, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i1, new Vector3f(0, offX, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i2, new Vector3f(offZ, offX, TextureManager.textures.get(c.id)[index]));
                                addTextureVertex(i3, new Vector3f(offZ, 0, TextureManager.textures.get(c.id)[index]));
                                break;
                            default:
                                System.out.println("puzzette");
                                break;
                        }
                        //now constructs the mesh

                        indicesList.add(i0);
                        indicesList.add(i3);
                        indicesList.add(i2);
                        indicesList.add(i2);
                        indicesList.add(i1);
                        indicesList.add(i0);

                        setMesh();
                    }
                }
            }
        }
    }

    public ArrayList<Vector3f> verticesList = new ArrayList<>();
    public ArrayList<Vector3f> textureList = new ArrayList<>();
    public ArrayList<Short> indicesList = new ArrayList<>();

    Short[] short1;
    short[] indices;

    //usually called at the end of the update() method of chunk. creates the mesh from the vertices, indices and texCoord set by Cell and adds it to a geometry with a material with correct texture loaded
    public void setMesh() {
        //checking if there are empty buffers is important: loading empty buffers causes a core dumped crash
        if (!verticesList.isEmpty() && !textureList.isEmpty() && !indicesList.isEmpty()) {
            short1 = indicesList.toArray(new Short[indicesList.size()]);
            indices = new short[short1.length];
            for (int i = 0; i < short1.length; i++) {
                indices[i] = Short.valueOf(Integer.toString(short1[i]));
            }
            
            chunkMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verticesList.toArray(new Vector3f[verticesList.size()])));
            chunkMesh.setBuffer(VertexBuffer.Type.TexCoord, 3, BufferUtils.createFloatBuffer(textureList.toArray(new Vector3f[textureList.size()])));
            chunkMesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createShortBuffer(indices));

            chunkMesh.updateBound();
//            clearAll();

        }
    }

    public void clearAll() {
        indicesList.clear();
        verticesList.clear();
        textureList.clear();
    }

    public Vector3f getVectorFor(int x, int y, int z) {
        for (Vector3f v : verticesList) {
            if (v.x == x && v.y == y && v.z == z) {
                return v;
            }
        }
        return null;
    }

    public short addVertex(Vector3f v) {
        verticesList.add(v);
        return (short) (verticesList.size() - 1);
    }

    public void addTextureVertex(short index, Vector3f texVec) {
        try {
            textureList.add(index, texVec);
        } catch (IndexOutOfBoundsException e) {
            while (textureList.size() < index + 1) {
                textureList.add(Vector3f.NAN);
            }
            textureList.add(index, texVec);

        }
    }
}
