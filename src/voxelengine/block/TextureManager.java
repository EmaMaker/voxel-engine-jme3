package voxelengine.block;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureArray;
import java.util.ArrayList;
import java.util.List;
import voxelengine.Main;
import voxelengine.utils.Globals;

public class TextureManager extends AbstractAppState {

    public static final int OFF_DIRT = 0;
    public static final int OFF_GRASS_SIDE = 1;
    public static final int OFF_GRASS_TOP = 2;
    public static final int OFF_WOOD_SIDE = 3;
    public static final int OFF_WOOD_TOP_BOTTOM = 4;
    public static final int OFF_STONE = 5;
    public static final int OFF_LEAVES = 6;

    public static List<Image> images = new ArrayList<Image>();
    public static List<int[]> textures = new ArrayList<int[]>();

    SimpleApplication main;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        main = (SimpleApplication) app;

        addImage(OFF_DIRT, main.getAssetManager().loadTexture("Textures/dirt.png").getImage());
        addImage(OFF_GRASS_SIDE, main.getAssetManager().loadTexture("Textures/grass_side.png").getImage());
        addImage(OFF_GRASS_TOP, main.getAssetManager().loadTexture("Textures/grass_top.png").getImage());
        addImage(OFF_WOOD_SIDE, main.getAssetManager().loadTexture("Textures/wood_side.png").getImage());
        addImage(OFF_WOOD_TOP_BOTTOM, main.getAssetManager().loadTexture("Textures/wood_bottom_top.png").getImage());
        addImage(OFF_STONE, main.getAssetManager().loadTexture("Textures/stone.jpg").getImage());
        addImage(OFF_LEAVES, main.getAssetManager().loadTexture("Textures/leaves.jpg").getImage());

        TextureArray array = new TextureArray(images);
        array.setWrap(Texture.WrapMode.Repeat);

        Globals.mat.setTexture("ColorMap", array);

        setIdTexture(CellId.ID_GRASS, OFF_GRASS_SIDE, OFF_GRASS_SIDE, OFF_GRASS_SIDE, OFF_GRASS_SIDE, OFF_DIRT, OFF_GRASS_TOP);
        setIdTexture(CellId.ID_DIRT, OFF_DIRT, OFF_DIRT, OFF_DIRT, OFF_DIRT, OFF_DIRT, OFF_DIRT);
        setIdTexture(CellId.ID_WOOD, OFF_WOOD_SIDE, OFF_WOOD_SIDE, OFF_WOOD_SIDE, OFF_WOOD_SIDE, OFF_WOOD_TOP_BOTTOM, OFF_WOOD_TOP_BOTTOM);
        setIdTexture(CellId.ID_LEAVES, OFF_LEAVES, OFF_LEAVES, OFF_LEAVES, OFF_LEAVES, OFF_LEAVES, OFF_LEAVES);
        setIdTexture(CellId.ID_STONE, OFF_STONE, OFF_STONE, OFF_STONE, OFF_STONE, OFF_STONE, OFF_STONE);
    }

    public void setIdTexture(int id, int offWest, int offEast, int offNorth, int offSouth, int offBottom, int offTop) {
        int[] offsets = {offWest, offEast, offNorth, offSouth,  offBottom, offTop};
        textures.add(id, offsets);
    }

    public void addImage(int offset, Image img) {
        images.add(offset, img);
    }

}
