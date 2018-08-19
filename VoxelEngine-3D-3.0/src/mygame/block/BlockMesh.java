package mygame.block;

import mygame.world.Chunk;
import mygame.utils.Reference;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import static mygame.utils.Reference.chunkSize;

public class BlockMesh extends Mesh {

    public int step = 0;

    public ArrayList<Vector3f> verticesList = new ArrayList<>();
    public ArrayList<Integer> indicesList = new ArrayList<>();
    public Vector2f[] texCoord = new Vector2f[(int) Math.pow(chunkSize, 3) * 4];

    Integer[] integer1;
    int[] indices;

    public Geometry geom;
    Material mat;

    public FacesId id;
    public Chunk chunk;

    public BlockMesh(FacesId id, Chunk chunk) {
        this.id = id;
        this.chunk = chunk;
    }

    //usually called at the end of the update() method of chunk. creates the mesh from the vertices, indices and texCoord set by Cell and adds it to a geometry with a material with correct texture loaded
    public void set() {

        mat = new Material(Reference.main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", Reference.main.getAssetManager().loadTexture("Textures/" + this.id.toString().toLowerCase() + ".png"));
        integer1 = indicesList.toArray(new Integer[indicesList.size()]);
        indices = new int[integer1.length];
        for (int i = 0; i < integer1.length; i++) {
            indices[i] = integer1[i];
        }

        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(verticesList.toArray(new Vector3f[verticesList.size()])));
        setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
        setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        updateBound();
        
        geom = new Geometry(this.toString() + "(" + this.id.toString() + ")", this);
        geom.setMaterial(mat);
    }
    
    public void addVertexToList(Vector3f v) {
        verticesList.add(v);
        step++;
    }

}
