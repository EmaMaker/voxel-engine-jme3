package mygame;

import mygame.world.WorldProvider;
import mygame.control.ControlState;
import mygame.utils.GuiManager;
import mygame.utils.Reference;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import java.io.File;
import mygame.control.PlayerControlState;

public class Main extends SimpleApplication {

    boolean preloaded = false;

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings sets = new AppSettings(false);
        sets.setFullscreen(true);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setEnabled(true);
        
        File f = new File(System.getProperty("user.dir") + "/chunks/");
        
        if(!f.exists()){
            f.mkdir();
        }

        flyCam.setZoomSpeed(0);

        stateManager.attach(bulletAppState);
        stateManager.attach(new Reference());
        stateManager.attach(new ControlState());
        stateManager.attach(new PlayerControlState());
        stateManager.attach(new GuiManager());
        stateManager.attach(new WorldProvider());

        initCrossHairs();
        flyCam.setMoveSpeed(60f);
        viewPort.setBackgroundColor(ColorRGBA.Cyan);

    }

    protected void initCrossHairs() {
        setDisplayStatView(true);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        File folder = new File(System.getProperty("user.dir") + "/chunks/");
        File list[] = folder.listFiles();
        
        for(int i = 0; i < list.length; i++){
            if(list[i].getName().endsWith(".chunk")){
                list[i].delete();
            }
        }
        
        stateManager.getState(BulletAppState.class).setEnabled(false);
        stateManager.getState(Reference.class).executor.shutdown();

    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!preloaded) {
            stateManager.getState(WorldProvider.class).preload();
            preloaded = true;
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public BitmapFont getGuiFont() {
        return this.guiFont;
    }

    public AppSettings getSettings() {
        return settings;
    }
}
