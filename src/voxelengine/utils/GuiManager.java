/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxelengine.utils;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import voxelengine.control.ControlsHandler;
import voxelengine.VoxelEngine;

public class GuiManager extends AbstractAppState {

    BitmapText blockName;
    BitmapText debugging;
    BitmapText playerPos;

    VoxelEngine engine;
    ControlsHandler controlState;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        engine = stateManager.getState(VoxelEngine.class);

        controlState = engine.getStateManager().getState(ControlsHandler.class);

        blockName = new BitmapText(this.engine.getGuiFont(), false);
        playerPos = new BitmapText(this.engine.getGuiFont(), false);
        debugging = new BitmapText(this.engine.getGuiFont(), false);

        this.engine.getGuiNode().attachChild(blockName);
        this.engine.getGuiNode().attachChild(playerPos);
    }

    @Override
    public void update(float tpf) {

        /*START PLAYER COORDS UPDATE*/
        if (Globals.playerEnabled()) {
            playerPos.setSize(this.engine.getGuiFont().getCharSet().getRenderedSize());
            playerPos.setText("X: " + (int) controlState.playerModel.getLocalTranslation().x + ", " + "Y: " + (int) controlState.playerModel.getLocalTranslation().y + ", " + "Z: " + (int) controlState.playerModel.getLocalTranslation().z + "   (PLAYER)"); // crosshairs
            playerPos.setLocalTranslation(engine.getSettings().getWidth() - playerPos.getLineWidth() * 1.1f, engine.getSettings().getHeight() - playerPos.getLineHeight(), 0);
            /*END PLAYER COORDS UPDATE*/
        } else {
            playerPos.setSize(this.engine.getGuiFont().getCharSet().getRenderedSize());
            playerPos.setText("X: " + (int) this.engine.getCamera().getLocation().x + ", " + "Y: " + (int) this.engine.getCamera().getLocation().y + ", " + "Z: " + (int) this.engine.getCamera().getLocation().z + "   (FREE CAMERA)"); // crosshairs
            playerPos.setLocalTranslation(engine.getSettings().getWidth() - playerPos.getLineWidth() * 1.1f, engine.getSettings().getHeight() - playerPos.getLineHeight(), 0);
        }

        blockName.setText(String.valueOf(controlState.currentBlockId));
        blockName.setSize(engine.getGuiFont().getCharSet().getRenderedSize());
        blockName.setLocalTranslation(engine.getSettings().getWidth() - blockName.getLineWidth() * 1.2f, 0 + blockName.getLineHeight() * 1.2f, 0);

        if (Globals.debugEnabled()) {
            debugging.setText("Debugging" + "\n\n");
            debugging.setColor(ColorRGBA.Red);
            debugging.setSize(engine.getGuiFont().getCharSet().getRenderedSize());
            debugging.setLocalTranslation(/*engine.getSettings().getWidth() -*/debugging.getLineWidth() * 0.2f, engine.getSettings().getHeight() - debugging.getLineHeight() * 0.6f, 0);
            this.engine.getGuiNode().attachChild(debugging);
        } else {
            this.engine.getGuiNode().detachChild(debugging);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

}
