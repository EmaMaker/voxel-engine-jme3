package mygame.block;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureArray;
import java.util.ArrayList;
import java.util.List;
import mygame.Main;
import mygame.utils.Reference;

public class TextureOffsets extends AbstractAppState {

    /* This class interfaces with the texture atlas. Every image in the atlas is 64x64 pixels. The Atlas is loaded in Reference class, this is just a bunch of static finals variables.
     * If Atlas image dimensions are changed, hard-code the dimensions in the code. The first texture is considered as offset 0.
     */
    public static final int ATLAS_DIM = 512;
    public static final int IMAGE_DIM = 64;
    public static final int OFF_IN_ROW = ATLAS_DIM / IMAGE_DIM;

    public static final int OFF_DIRT = 0;
    public static final int OFF_GRASS_SIDE = 1;
    public static final int OFF_GRASS_TOP = 2;
    public static final int OFF_WOOD_SIDE = 3;
    public static final int OFF_WOOD_TOP_BOTTOM = 4;
    public static final int OFF_STONE = 5;

    public static List<Image> images = new ArrayList<Image>();

    Main main;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        main = (Main) app;

        images.add(OFF_DIRT, main.getAssetManager().loadTexture("Textures/dirt.png").getImage());
        images.add(OFF_GRASS_SIDE, main.getAssetManager().loadTexture("Textures/grass_side.png").getImage());
        images.add(OFF_GRASS_TOP, main.getAssetManager().loadTexture("Textures/grass_top.png").getImage());
        images.add(OFF_WOOD_SIDE, main.getAssetManager().loadTexture("Textures/wood_side.png").getImage());
        images.add(OFF_WOOD_TOP_BOTTOM, main.getAssetManager().loadTexture("Textures/wood_bottom_top.png").getImage());
        //images.add(OFF_STONE, main.getAssetManager().loadTexture("Textures/stone.png").getImage());
        
        TextureArray array = new TextureArray(images);
        array.setWrap(Texture.WrapMode.Repeat);
        
        Reference.mat.setTexture("ColorMap", array);
    }

    /*
    //Following methods return the UV coordinates in the image for the given offset as a Vector2f to be used in the TextureBuffer for the mesh. 
    //All is divided by ATLAS_DIM so it gets the position in values between 0 and 1 instead of pixels
    //this is 0,1 in UV coords
    public static Vector2f topLeft(int offset) {
        return new Vector2f(minX(offset), maxY(offset));
    }

    //this is 1,1 in UV coords
    public static Vector2f topRight(int offset) {
        return new Vector2f(maxX(offset), maxY(offset));
    }

    //this is 0,0 in UV coords
    public static Vector2f bottomLeft(int offset) {
        return new Vector2f(minX(offset), minY(offset));
    }

    //this is 1,0 in UV coords
    public static Vector2f bottomRight(int offset) {
        return new Vector2f(maxX(offset), minY(offset));
    }

    public static float minX(int offset) {
        return (float) ((float) ((offset % OFF_IN_ROW) * IMAGE_DIM) / ATLAS_DIM);
    }

    public static float minY(int offset) {
        return (float) ((float) ((offset / OFF_IN_ROW) * IMAGE_DIM) / ATLAS_DIM);
    }

    public static float maxX(int offset) {
        return (float) ((float) (((offset + 1) % OFF_IN_ROW) * (IMAGE_DIM - 1)) / ATLAS_DIM);
    }

    public static float maxY(int offset) {
        return (float) ((float) (((offset + OFF_IN_ROW) / OFF_IN_ROW) * (IMAGE_DIM - 1)) / ATLAS_DIM);
    }

    // (float) ((float) ((offset % OFF_IN_ROW) * IMAGE_DIM) / ATLAS_DIM) minX
    // (float) ((float) ((offset / OFF_IN_ROW) * IMAGE_DIM) / ATLAS_DIM) minY
    // (float) ((float) (((offset + 1) % OFF_IN_ROW) *( IMAGE_DIM - 1)) / ATLAS_DIM) maxX
    // (float) ((float) (((offset + OFF_IN_ROW) / OFF_IN_ROW) * (IMAGE_DIM - 1)) / ATLAS_DIM) maxY*/
}
