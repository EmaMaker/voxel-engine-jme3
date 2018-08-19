package mygame.world;

import com.google.gson.annotations.Expose;
import mygame.utils.Reference;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import mygame.block.BlockMesh;
import mygame.block.Cell;
import mygame.block.CellId;
import mygame.block.FacesId;
import static mygame.utils.Reference.chunkSize;
import static mygame.utils.Debugger.*;
import mygame.utils.SimplexNoise;

public class Chunk {

    transient boolean firstLoad = true;
    transient boolean loaded = false;
    transient boolean phyLoaded = false;

    Node node = new Node();

    //the chunk coords in the world
    public int x, y, z;

    //the cells contained in the chunk, as an arraylist. using a three-dimensional array would cause to json-serialization to retrive StackOverflowException
    public ArrayList<Cell> cells = new ArrayList<>();

    //this doesn't have the expose annotation because it will be recalculated once updating
    public transient ArrayList<BlockMesh> meshList = new ArrayList<>();

    //the list of the various mesh in the chunk (blocks with same id are contained in the same mesh, look up in BlockMesh and Cell for vertices, indexes and texture coords control)
    public Chunk() {
        this(0, 0, 0);
    }

    public Chunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    public void update() {

        debug("-----------------------------------------");
        debug("Updating " + this);

        meshList.clear();

        for (BlockMesh mesh : meshList) {
            debug(mesh.toString());
        }

        for (Cell c : cells) {
            c.update();
        }

        for (BlockMesh mesh : meshList) {
            mesh.set();
            debug(mesh.toString() + " (" + mesh.id.toString() + ")");
        }

        firstLoad = false;

        debug("Updated " + this);
        debug("-----------------------------------------");

        this.unload();
    }

    public void load() {
        if (!loaded) {
            for (BlockMesh mesh : meshList) {
                node.attachChild(mesh.geom);
            }
            Reference.terrainNode.attachChild(node);
            loaded = true;
        }
    }

    public void unload() {
        if (loaded) {
            node.detachAllChildren();
            meshList.clear();
            Reference.terrainNode.detachChild(node);
            loaded = false;
            unloadPhysics();
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
    }

    //returns mesh from id passed as argument, if it's null it creates the new mesh and then returns it
    public BlockMesh getMeshForId(FacesId id) {
        if (getMeshForId1(id) == null) {
            createNewMesh(id);
        }

        return getMeshForId1(id);
    }

    //just returns the mesh with id passed as argument, if null returns null
    private BlockMesh getMeshForId1(FacesId id) {
        for (BlockMesh mesh : meshList) {
            if (mesh.id == id) {
                return mesh;
            }
        }
        return null;
    }

    //adds a new mesh to the mesh list
    public void createNewMesh(FacesId id) {
        meshList.add(new BlockMesh(id, this));
        debug("Created BlockMesh for " + id.toString() + " in " + this + " at " + this.x + ", " + this.y + ", " + this.z);
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

    public Cell getCellFromVertices(ArrayList<Vector3f> al) {

        if (al != null) {
            for (Cell c : cells) {
                if (c.allVertices.containsAll(al));
                return c;
            }
        } else {

            debug("CAN'T GET CELL FROM VERTICES: VERTICES LIST IS NULL!");
            return null;
        }
        return null;
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

    public void genTerrain() {
        for (int i = 0; i < chunkSize; i++) {
            for (int k = 0; k < chunkSize; k++) {
                //System.out.println(Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025)));
                for (int a = 0; a <= Math.abs(SimplexNoise.noise((x*chunkSize+i)*0.025, (z*chunkSize+k)*0.025))*10; a++) {
                    setCell(i, a, k, CellId.GRASS);
                }
            }
        }
    }
}
