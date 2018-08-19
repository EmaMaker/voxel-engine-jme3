package mygame.block;

import mygame.world.Chunk;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import static mygame.utils.Debugger.debug;
import mygame.utils.Reference;
import static mygame.utils.Reference.chunkSize;

public class Cell {

    transient Vector3f v1, v2, v3, v4;

    public transient ArrayList<Vector3f> allVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> upVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> downVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> leftVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> rightVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> frontVertices = new ArrayList<>();
    public transient ArrayList<Vector3f> backVertices = new ArrayList<>();

    FacesId UP_ID, DOWN_ID, LEFT_ID, RIGHT_ID, FRONT_ID, BACK_ID;

    //temporary variables used in the createTile method to store the index in the verticesList of each array at is created. Lately used to set the indices for the indicesList 
    //(View BlockMesh for vertices and indices declaration and usage)
    int var1, var2, var3, var4;

    public transient Chunk chunk;
    public CellId id;
    public int x, y, z; //the coords RELATIVE inside the chunk
    public int chunkX, chunkY, chunkZ; // the chunk's coords
    public int worldX, worldY, worldZ; //the ABSOLUTE coords in the world

    public Cell(String id, int x, int y, int z, int chunkX, int chunkY, int chunkZ) {
        this(CellId.valueOf(id), x, y, z, chunkX, chunkY, chunkZ);
    }

    public Cell(CellId id, int x, int y, int z, int chunkX, int chunkY, int chunkZ) {
        this(id, x, y, z, chunkX, chunkY, chunkZ, (chunkX * chunkSize) + x, (chunkY * chunkSize) + y, (chunkZ * chunkSize) + z);
    }

    public Cell(String id, int x, int y, int z, int chunkX, int chunkY, int chunkZ, int worldX, int worldY, int worldZ) {
        this(CellId.valueOf(id), x, y, z, chunkX, chunkY, chunkZ, worldX, worldY, worldZ);
    }

    public Cell(CellId id, int x, int y, int z, int chunkX, int chunkY, int chunkZ, int worldX, int worldY, int worldZ) {
        setId(id);
        this.x = x;
        this.y = y;
        this.z = z;

        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;

        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;

        this.chunk = Reference.prov.chunks[chunkX][chunkY][chunkZ];
    }

    public void update() {

        updateSideIds();

        if (this.id != CellId.AIR) {
            //Right TILE
            if (this.chunk.getCell(x + 1, y, z) == null || this.chunk.getCell(x + 1, y, z).id == CellId.AIR) {
                createTile("right");
            }

            //LEFT TILE
            if (this.chunk.getCell(x - 1, y, z) == null || this.chunk.getCell(x - 1, y, z).id == CellId.AIR) {
                createTile("left");
            }

            //Front TILE
            if (this.chunk.getCell(x, y, z + 1) == null || this.chunk.getCell(x, y, z + 1).id == CellId.AIR) {
                createTile("front");
            }

            //Back TILE
            if (this.chunk.getCell(x, y, z - 1) == null || this.chunk.getCell(x, y, z - 1).id == CellId.AIR) {
                createTile("back");
            }

            //Up TILE
            if (this.chunk.getCell(x, y + 1, z) == null || this.chunk.getCell(x, y + 1, z).id == CellId.AIR) {
                createTile("up");
            }

            //Down TILE
            if (this.chunk.getCell(x, y - 1, z) == null || this.chunk.getCell(x, y - 1, z).id == CellId.AIR) {
                createTile("down");
            }
        }
    }

    public void createTile(String tileType) {
        switch (tileType.toLowerCase()) {
            case "up":
                v1 = new Vector3f(worldX, worldY + 1, worldZ + 1);
                v2 = new Vector3f(worldX, worldY + 1, worldZ);
                v3 = new Vector3f(worldX + 1, worldY + 1, worldZ);
                v4 = new Vector3f(worldX + 1, worldY + 1, worldZ + 1);
                chunk.getMeshForId(UP_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(UP_ID).verticesList.size() - 1;
                chunk.getMeshForId(UP_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(UP_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(UP_ID).verticesList.size() - 1;
                chunk.getMeshForId(UP_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(UP_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(UP_ID).verticesList.size() - 1;
                chunk.getMeshForId(UP_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(UP_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(UP_ID).verticesList.size() - 1;
                chunk.getMeshForId(UP_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(UP_ID).indicesList.add(var1);
                chunk.getMeshForId(UP_ID).indicesList.add(var4);
                chunk.getMeshForId(UP_ID).indicesList.add(var3);
                chunk.getMeshForId(UP_ID).indicesList.add(var3);
                chunk.getMeshForId(UP_ID).indicesList.add(var2);
                chunk.getMeshForId(UP_ID).indicesList.add(var1);

                this.upVertices.add(v1);
                this.upVertices.add(v2);
                this.upVertices.add(v3);
                this.upVertices.add(v4);

                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }
                break;
            case "down":
                v1 = new Vector3f(worldX, worldY, worldZ + 1);
                v2 = new Vector3f(worldX, worldY, worldZ);
                v3 = new Vector3f(worldX + 1, worldY, worldZ);
                v4 = new Vector3f(worldX + 1, worldY, worldZ + 1);

                chunk.getMeshForId(DOWN_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(DOWN_ID).verticesList.size() - 1;
                chunk.getMeshForId(DOWN_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(DOWN_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(DOWN_ID).verticesList.size() - 1;
                chunk.getMeshForId(DOWN_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(DOWN_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(DOWN_ID).verticesList.size() - 1;
                chunk.getMeshForId(DOWN_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(DOWN_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(DOWN_ID).verticesList.size() - 1;
                chunk.getMeshForId(DOWN_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(DOWN_ID).indicesList.add(var1);
                chunk.getMeshForId(DOWN_ID).indicesList.add(var2);
                chunk.getMeshForId(DOWN_ID).indicesList.add(var3);
                chunk.getMeshForId(DOWN_ID).indicesList.add(var3);
                chunk.getMeshForId(DOWN_ID).indicesList.add(var4);
                chunk.getMeshForId(DOWN_ID).indicesList.add(var1);

                this.downVertices.add(v1);
                this.downVertices.add(v2);
                this.downVertices.add(v3);
                this.downVertices.add(v4);

                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }

                break;

            case "left":
                v1 = new Vector3f(worldX, worldY, worldZ + 1);
                v2 = new Vector3f(worldX, worldY, worldZ);
                v3 = new Vector3f(worldX, worldY + 1, worldZ);
                v4 = new Vector3f(worldX, worldY + 1, worldZ + 1);

                chunk.getMeshForId(LEFT_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(LEFT_ID).verticesList.size() - 1;
                chunk.getMeshForId(LEFT_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(LEFT_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(LEFT_ID).verticesList.size() - 1;
                chunk.getMeshForId(LEFT_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(LEFT_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(LEFT_ID).verticesList.size() - 1;
                chunk.getMeshForId(LEFT_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(LEFT_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(LEFT_ID).verticesList.size() - 1;
                chunk.getMeshForId(LEFT_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(LEFT_ID).indicesList.add(var4);
                chunk.getMeshForId(LEFT_ID).indicesList.add(var3);
                chunk.getMeshForId(LEFT_ID).indicesList.add(var2);
                chunk.getMeshForId(LEFT_ID).indicesList.add(var2);
                chunk.getMeshForId(LEFT_ID).indicesList.add(var1);
                chunk.getMeshForId(LEFT_ID).indicesList.add(var4);

                this.leftVertices.add(v1);
                this.leftVertices.add(v2);
                this.leftVertices.add(v3);
                this.leftVertices.add(v4);

                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }
                break;
            case "right":
                v1 = new Vector3f(worldX + 1, worldY, worldZ);
                v2 = new Vector3f(worldX + 1, worldY, worldZ + 1);
                v3 = new Vector3f(worldX + 1, worldY + 1, worldZ + 1);
                v4 = new Vector3f(worldX + 1, worldY + 1, worldZ);

                chunk.getMeshForId(RIGHT_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(RIGHT_ID).verticesList.size() - 1;
                chunk.getMeshForId(RIGHT_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(RIGHT_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(RIGHT_ID).verticesList.size() - 1;
                chunk.getMeshForId(RIGHT_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(RIGHT_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(RIGHT_ID).verticesList.size() - 1;
                chunk.getMeshForId(RIGHT_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(RIGHT_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(RIGHT_ID).verticesList.size() - 1;
                chunk.getMeshForId(RIGHT_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(RIGHT_ID).indicesList.add(var4);
                chunk.getMeshForId(RIGHT_ID).indicesList.add(var3);
                chunk.getMeshForId(RIGHT_ID).indicesList.add(var2);
                chunk.getMeshForId(RIGHT_ID).indicesList.add(var2);
                chunk.getMeshForId(RIGHT_ID).indicesList.add(var1);
                chunk.getMeshForId(RIGHT_ID).indicesList.add(var4);

                this.rightVertices.add(v1);
                this.rightVertices.add(v2);
                this.rightVertices.add(v3);
                this.rightVertices.add(v4);
                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }

                break;
            case "front":
                v1 = new Vector3f(worldX, worldY, worldZ + 1);
                v2 = new Vector3f(worldX + 1, worldY, worldZ + 1);
                v3 = new Vector3f(worldX + 1, worldY + 1, worldZ + 1);
                v4 = new Vector3f(worldX, worldY + 1, worldZ + 1);

                chunk.getMeshForId(BACK_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(BACK_ID).verticesList.size() - 1;
                chunk.getMeshForId(BACK_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(BACK_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(BACK_ID).verticesList.size() - 1;
                chunk.getMeshForId(BACK_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(BACK_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(BACK_ID).verticesList.size() - 1;
                chunk.getMeshForId(BACK_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(BACK_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(BACK_ID).verticesList.size() - 1;
                chunk.getMeshForId(BACK_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(BACK_ID).indicesList.add(var4);
                chunk.getMeshForId(BACK_ID).indicesList.add(var1);
                chunk.getMeshForId(BACK_ID).indicesList.add(var2);
                chunk.getMeshForId(BACK_ID).indicesList.add(var2);
                chunk.getMeshForId(BACK_ID).indicesList.add(var3);
                chunk.getMeshForId(BACK_ID).indicesList.add(var4);

                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }

                this.frontVertices.add(v1);
                this.frontVertices.add(v2);
                this.frontVertices.add(v3);
                this.frontVertices.add(v4);
                break;
            case "back":
                v1 = new Vector3f(worldX, worldY, worldZ);
                v2 = new Vector3f(worldX + 1, worldY, worldZ);
                v3 = new Vector3f(worldX + 1, worldY + 1, worldZ);
                v4 = new Vector3f(worldX, worldY + 1, worldZ);

                chunk.getMeshForId(FRONT_ID).addVertexToList(v1);
                var1 = chunk.getMeshForId(FRONT_ID).verticesList.size() - 1;
                chunk.getMeshForId(FRONT_ID).texCoord[var1] = new Vector2f(0, 0);

                chunk.getMeshForId(FRONT_ID).addVertexToList(v2);
                var2 = chunk.getMeshForId(FRONT_ID).verticesList.size() - 1;
                chunk.getMeshForId(FRONT_ID).texCoord[var2] = new Vector2f(1, 0);

                chunk.getMeshForId(FRONT_ID).addVertexToList(v3);
                var3 = chunk.getMeshForId(FRONT_ID).verticesList.size() - 1;
                chunk.getMeshForId(FRONT_ID).texCoord[var3] = new Vector2f(1, 1);

                chunk.getMeshForId(FRONT_ID).addVertexToList(v4);
                var4 = chunk.getMeshForId(FRONT_ID).verticesList.size() - 1;
                chunk.getMeshForId(FRONT_ID).texCoord[var4] = new Vector2f(0, 1);

                chunk.getMeshForId(FRONT_ID).indicesList.add(var4);
                chunk.getMeshForId(FRONT_ID).indicesList.add(var3);
                chunk.getMeshForId(FRONT_ID).indicesList.add(var2);
                chunk.getMeshForId(FRONT_ID).indicesList.add(var2);
                chunk.getMeshForId(FRONT_ID).indicesList.add(var1);
                chunk.getMeshForId(FRONT_ID).indicesList.add(var4);

                this.backVertices.add(v1);
                this.backVertices.add(v2);
                this.backVertices.add(v3);
                this.backVertices.add(v4);

                if (!this.allVertices.contains(v1)) {
                    this.allVertices.add(v1);
                }
                if (!this.allVertices.contains(v2)) {
                    this.allVertices.add(v2);
                }
                if (!this.allVertices.contains(v3)) {
                    this.allVertices.add(v3);
                }
                if (!this.allVertices.contains(v4)) {
                    this.allVertices.add(v4);
                }
                break;
        }
    }

    public void updateSideIds() {
        switch (this.id) {
            case DIRT:
                UP_ID = FacesId.DIRT;
                DOWN_ID = FacesId.DIRT;
                BACK_ID = FacesId.DIRT;
                FRONT_ID = FacesId.DIRT;
                RIGHT_ID = FacesId.DIRT;
                LEFT_ID = FacesId.DIRT;
                break;
            case GRASS:
                UP_ID = FacesId.GRASS_TOP;
                DOWN_ID = FacesId.DIRT;
                BACK_ID = FacesId.GRASS_SIDE;
                FRONT_ID = FacesId.GRASS_SIDE;
                RIGHT_ID = FacesId.GRASS_SIDE;
                LEFT_ID = FacesId.GRASS_SIDE;
                break;
            case STONE:
                UP_ID = FacesId.STONE;
                DOWN_ID = FacesId.STONE;
                BACK_ID = FacesId.STONE;
                FRONT_ID = FacesId.STONE;
                RIGHT_ID = FacesId.STONE;
                LEFT_ID = FacesId.STONE;
                break;
            case WOOD:
                UP_ID = FacesId.WOOD_BOTTOM_TOP;
                DOWN_ID = FacesId.WOOD_BOTTOM_TOP;
                BACK_ID = FacesId.WOOD_SIDE;
                FRONT_ID = FacesId.WOOD_SIDE;
                RIGHT_ID = FacesId.WOOD_SIDE;
                LEFT_ID = FacesId.WOOD_SIDE;
                break;
        }
    }

    public CellId getId() {
        return id;
    }

    public void setId(CellId id) {
        this.id = id;
        //this.chunk.setCell(x, y, z, id);
        debug(this.toString() + "(" + x + ", " + y + ", " + z + ") " + " id set to " + id.toString() + " in chunk " + chunkX + ", " + chunkY + ", " + chunkZ);
    }

    /*
    0 -> y+
    1 -> y-
    2 -> x+
    3 -> x-
    4 -> z+
    5 -> z-
     */
    public byte faceFromVertices(ArrayList<Vector3f> al) {
        if (upVertices.containsAll(al)) {
            return 0;
        } else if (downVertices.containsAll(al)) {
            return 1;
        } else if (rightVertices.containsAll(al)) {
            return 2;
        } else if (leftVertices.containsAll(al)) {
            return 3;
        } else if (frontVertices.containsAll(al)) {
            return 4;
        } else if (backVertices.containsAll(al)) {
            return 5;
        }
        return Byte.MIN_VALUE;
    }

}
