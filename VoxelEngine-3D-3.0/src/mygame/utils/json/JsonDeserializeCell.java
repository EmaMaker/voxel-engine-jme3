package mygame.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import java.io.IOException;
import mygame.block.Cell;

public class JsonDeserializeCell extends StdDeserializer<Cell> {

    public JsonDeserializeCell() {
        this(null);
    }

    public JsonDeserializeCell(Class<?> vc) {
        super(vc);
    }

    @Override
    public Cell deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String id = node.get("id").asText();
        int x = (Integer) ((IntNode) node.get("x")).asInt();
        int y = (Integer) ((IntNode) node.get("y")).asInt();
        int z = (Integer) ((IntNode) node.get("z")).asInt();
        int chunkX = (Integer) ((IntNode) node.get("chunkX")).asInt();
        int chunkY = (Integer) ((IntNode) node.get("chunkY")).asInt();
        int chunkZ = (Integer) ((IntNode) node.get("chunkZ")).asInt();
        int worldX = (Integer) ((IntNode) node.get("worldX")).asInt();
        int worldY = (Integer) ((IntNode) node.get("worldY")).asInt();
        int worldZ = (Integer) ((IntNode) node.get("worldZ")).asInt();
        return new Cell(id, x, y, z, chunkX, chunkY, chunkZ, worldX, worldY, worldZ);
    }
}
