package voxelengine.world.generators;

import voxelengine.world.Chunk;

public abstract class WorldGenerator {
    public abstract void generate(Chunk c);
}
