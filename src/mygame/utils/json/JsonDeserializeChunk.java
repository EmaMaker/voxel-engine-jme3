/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import java.io.IOException;
import mygame.world.Chunk;

public class JsonDeserializeChunk extends StdDeserializer<Chunk> { 
 
    public JsonDeserializeChunk() { 
        this(null); 
    } 
 
    public JsonDeserializeChunk(Class<?> vc) { 
        super(vc); 
    }
 
    @Override
    public Chunk deserialize(JsonParser jp, DeserializationContext ctxt) 
      throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        int x = (Integer) ((IntNode) node.get("x")).asInt();
        int y = (Integer) ((IntNode) node.get("y")).asInt();
        int z = (Integer) ((IntNode) node.get("z")).asInt();
        return new Chunk( x, y, z);
    }
}