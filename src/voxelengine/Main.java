package voxelengine;

import com.jme3.app.SimpleApplication;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
        app.setShowSettings(false);
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new VoxelEngine(settings));
    }

    protected void initCrossHairs() {
    }

    @Override
    public void destroy() {
        stateManager.cleanup();
    }

}
