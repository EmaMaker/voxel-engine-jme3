package mygame.world;

import mygame.world.Chunk;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import static mygame.utils.Reference.chunkSize;

public class ChunkMesh extends Mesh {

    public int step = 0;

    public ArrayList<Vector3f> verticesList = new ArrayList<>();
    public ArrayList<Vector3f> textureList = new ArrayList<>();
    public ArrayList<Integer> indicesList = new ArrayList<>();
    public Vector2f[] texCoord = new Vector2f[(int) Math.pow(chunkSize, 3) * 4];

    Integer[] integer1;
    int[] indices;

    public Chunk chunk;

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
    }

    //usually called at the end of the update() method of chunk. creates the mesh from the vertices, indices and texCoord set by Cell and adds it to a geometry with a material with correct texture loaded
    public void set() {
        integer1 = indicesList.toArray(new Integer[indicesList.size()]);
        indices = new int[integer1.length];
        for (int i = 0; i < integer1.length; i++) {
            indices[i] = integer1[i];
        }
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(verticesList.toArray(new Vector3f[verticesList.size()])));
        setBuffer(Type.TexCoord, 3, BufferUtils.createFloatBuffer(textureList.toArray(new Vector3f[textureList.size()])));
        setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
        
    }

    public Vector3f getVectorFor(int x, int y, int z) {
        for (Vector3f v : verticesList) {
            if (v.x == x && v.y == y && v.z == z) {
                return v;
            }
        }
        return null;
    }

    /*public int getIndexFor(Vector3f v) {
        for (int i = 0; i < verticesList.size(); i++) {
            if (v.x == verticesList.get(i).x && v.y == verticesList.get(i).y && v.z == verticesList.get(i).z) {
                return i;
            }
        }
        return Integer.BYTES;
    }

    public int getIndexFor(int x, int y, int z) {
        return getIndexFor(new Vector3f(x, y, z));
    }*/

    //adds the vertex to the verticesList and returns its index int the list, which is actually the verticesList.size() - 1.
    //this makes simpler the texturing because some vertices will be duplicated
    public int addVertex(Vector3f v) {
        verticesList.add(v);
        return (verticesList.size() - 1);
    }
}
