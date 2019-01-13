package mygame.world;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class ChunkMesh extends Mesh {

    public int step = 0;

    public ArrayList<Vector3f> verticesList = new ArrayList<>();
    public ArrayList<Vector3f> textureList = new ArrayList<>();
    public ArrayList<Short> indicesList = new ArrayList<>();

    Short[] short1;
    short[] indices;

    public Chunk chunk;

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
    }

    //usually called at the end of the update() method of chunk. creates the mesh from the vertices, indices and texCoord set by Cell and adds it to a geometry with a material with correct texture loaded
    public void set() {
        short1 = indicesList.toArray(new Short[indicesList.size()]);
        indices = new short[short1.length];
        for (int i = 0; i < short1.length; i++) {
            indices[i] = Short.valueOf(Integer.toString(short1[i]));
        }

        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(verticesList.toArray(new Vector3f[verticesList.size()])));
        setBuffer(Type.TexCoord, 3, BufferUtils.createFloatBuffer(textureList.toArray(new Vector3f[textureList.size()])));
        setBuffer(Type.Index, 3, BufferUtils.createShortBuffer(indices));

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

    public static ByteBuffer createByteBuffer(Vector3f... data) {
        if (data == null) {
            return null;
        }
        ByteBuffer buff = BufferUtils.createByteBuffer(3 * data.length);
        for (Vector3f element : data) {
            if (element != null) {
                buff.put((byte) element.x).put((byte) element.y).put((byte) element.z);
            } else {
                buff.put((byte) 0).put((byte) 0).put((byte) 0);
            }
        }
        buff.flip();
        return buff;
    }
    
    public static ShortBuffer createShortBuffer(Vector3f... data) {
        if (data == null) {
            return null;
        }
        ShortBuffer buff = BufferUtils.createShortBuffer(3 * data.length);
        for (Vector3f element : data) {
            if (element != null) {
                buff.put((short) element.x).put((short) element.y).put((short) element.z);
            } else {
                buff.put((short) 0).put((short) 0).put((short) 0);
            }
        }
        buff.flip();
        return buff;
    }
}
