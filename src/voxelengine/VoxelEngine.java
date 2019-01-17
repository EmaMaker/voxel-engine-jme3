/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxelengine;

import com.jme3.app.Application;
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
import voxelengine.control.ControlState;
import voxelengine.control.PlayerControlState;
import voxelengine.utils.GuiManager;
import voxelengine.utils.Reference;
import voxelengine.world.WorldProvider;

/**
 *
 * @author emamaker
 */
public class VoxelEngine extends AbstractAppState{
    
    FlyByCamera  flyCam;
    AppStateManager stateManager;
    BitmapFont guiFont;
    Main main;

    
    AppSettings settings;
    
    public VoxelEngine(AppSettings sets){
        settings = sets;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        main = (Main) app;
        stateManager = main.getStateManager();
        flyCam = main.getFlyByCamera();
        
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setEnabled(true);

        File f = new File(System.getProperty("user.dir") + "/chunks/");

        if (!f.exists()) {
            f.mkdir();
        }

        flyCam.setZoomSpeed(0);
        flyCam.setMoveSpeed(60f);

        stateManager.attach(bulletAppState);
        stateManager.attach(new Reference());
        stateManager.attach(new ControlState());
        stateManager.attach(new PlayerControlState());
        stateManager.attach(new GuiManager());
        stateManager.attach(new TextureManager());
        stateManager.attach(new WorldProvider());
        
        stateManager.getState(PlayerControlState.class).setEnabled(true);
        
        initCrossHairs();
        main.getViewPort().setBackgroundColor(ColorRGBA.Cyan);
    }
   
    
    
    @Override
    public void cleanup(){
        File folder = new File(System.getProperty("user.dir") + "/chunks/");
        File list[] = folder.listFiles();

        for (int i = 0; i < list.length; i++) {
            list[i].delete();
            if (list[i].getName().endsWith(".chunk")) {
                list[i].delete();
            }
        }

        super.cleanup();
        
    }


    protected void initCrossHairs() {
        main.setDisplayStatView(true);
        guiFont = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        main.getGuiNode().attachChild(ch);
    }
    
    public AppStateManager getStateManager(){
        return main.getStateManager();
    }
    
    public AppSettings getSettings(){
        return settings;
    }
    
    public BitmapFont getGuiFont(){
        return guiFont;
    }
    
    public Node getGuiNode(){
        return main.getGuiNode();
    }
    
    public Camera getCamera(){
        return main.getCamera();
    }

    
   
}
