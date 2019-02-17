/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxelengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.io.File;
import voxelengine.block.TextureManager;
import voxelengine.control.ControlsHandler;
import voxelengine.utils.GuiManager;
import voxelengine.utils.Globals;
import voxelengine.utils.math.MathHelper;
import voxelengine.utils.math.SimplexNoise;
import voxelengine.world.Chunk;
import voxelengine.world.WorldManager;

public class VoxelEngine extends AbstractAppState {
    
    public static Thread mainThread;
    
    FlyByCamera flyCam;
    AppStateManager stateManager;
    BitmapFont guiFont;
    SimpleApplication main;
    
    AppSettings settings;
    
    public VoxelEngine(AppSettings sets) {
        settings = sets;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        //saves an instance of the main thread because it's needed in the chunk update method
        mainThread = Thread.currentThread();
        
        main = (SimpleApplication) app;
        stateManager = main.getStateManager();
        flyCam = main.getFlyByCamera();
        
        File f = new File(Globals.workingDir);
        
        if (!f.exists()) {
            f.mkdir();
        }
        
        flyCam.setZoomSpeed(0);
        flyCam.setMoveSpeed(45f);
        
        stateManager.attach(new BulletAppState());
        stateManager.attach(new Globals());
        stateManager.attach(new ControlsHandler());
        stateManager.attach(new GuiManager());
        stateManager.attach(new TextureManager());
        stateManager.attach(new WorldManager());
        
        initCrossHairs();
        main.getViewPort().setBackgroundColor(ColorRGBA.Cyan);
        
        stateManager.getState(BulletAppState.class).setEnabled(Globals.phyEnabled());
        
        //if we're instead going to load from files, the processes is already handled by the update in the WorldManager
        if (!Globals.LOAD_FROM_FILE) {
            deleteSaveFiles();
        }
    }
    
    /*private void loadSaveFiles() {
        //SIMPLEX NOISE IN A STATIC CLASS, SO IT'S INIT METHOD IS AUTOMATICALLY CALLED AND IT DOES ITS OWN FILE INITIALIZATION
        File folder = new File(Globals.workingDir);
        File list[] = folder.listFiles();
        
        String s;
        String data[] = new String[3];
        int x, y, z;
        for (File f : list) {
            if (f.getName().endsWith(".chunk")) {
                s = f.getName();
                s = s.replace(".chunk", "");
                data = s.split("-");
                
                x = Integer.valueOf(data[0]);
                y = Integer.valueOf(data[1]);
                z = Integer.valueOf(data[2]);
                //System.out.println(Arrays.toString(data));
                Globals.prov.chunks[MathHelper.flatChunk3Dto1D(x, y, z)] = new Chunk(x, y, z);
                Globals.prov.chunks[MathHelper.flatChunk3Dto1D(x, y, z)].loadFromFile(f);
                
            }
        }
    }*/
    
    private void exitAndSave() {
        SimplexNoise.saveToFile();
        for (Chunk chunk : WorldManager.chunks) {
            if (chunk != null) {
                chunk.saveToFile();
            }
        }
    }
    
    private void deleteSaveFiles() {
        File folder = new File(Globals.workingDir);
        File list[] = folder.listFiles();
        
        for (File f : list) {
            f.delete();
            if (f.getName().endsWith(".chunk") || f.getName().endsWith(".table")) {
                f.delete();
            }
        }
    }
    
    protected void initCrossHairs() {
        main.setDisplayStatView(true);
        guiFont = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        main.getGuiNode().attachChild(ch);
    }
    
    @Override
    public void cleanup() {
        if (Globals.SAVE_ON_EXIT) {
            exitAndSave();
        } else {
            deleteSaveFiles();
        }
        super.cleanup();
    }
    
    public AppStateManager getStateManager() {
        return main.getStateManager();
    }
    
    public AppSettings getSettings() {
        return settings;
    }
    
    public BitmapFont getGuiFont() {
        return guiFont;
    }
    
    public Node getGuiNode() {
        return main.getGuiNode();
    }
    
    public Camera getCamera() {
        return main.getCamera();
    }
    
}
