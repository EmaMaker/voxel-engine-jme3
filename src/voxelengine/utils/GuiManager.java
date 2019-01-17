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
import voxelengine.control.ControlState;
import voxelengine.VoxelEngine;
import voxelengine.control.PlayerControlState;

public class GuiManager extends AbstractAppState {

    BitmapText blockName;
    BitmapText debugging;
    BitmapText playerPos;

    VoxelEngine main;
    ControlState controlState;
    PlayerControlState playerState;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        main = stateManager.getState(VoxelEngine.class);

        controlState = main.getStateManager().getState(ControlState.class);
        playerState = main.getStateManager().getState(PlayerControlState.class);

        blockName = new BitmapText(this.main.getGuiFont(), false);
        playerPos = new BitmapText(this.main.getGuiFont(), false);
        debugging = new BitmapText(this.main.getGuiFont(), false);

        this.main.getGuiNode().attachChild(blockName);
        this.main.getGuiNode().attachChild(playerPos);
    }

    @Override
    public void update(float tpf) {

        /*START PLAYER COORDS UPDATE*/
        if (main.getStateManager().getState(PlayerControlState.class).isEnabled()) {
            playerPos.setSize(this.main.getGuiFont().getCharSet().getRenderedSize());
            playerPos.setText("X: " + (int) playerState.playerModel.getLocalTranslation().x + ", " + "Y: " + (int) playerState.playerModel.getLocalTranslation().y + ", " + "Z: " + (int) playerState.playerModel.getLocalTranslation().z + "   (PLAYER)"); // crosshairs
            playerPos.setLocalTranslation(main.getSettings().getWidth() - playerPos.getLineWidth() * 1.1f, main.getSettings().getHeight() - playerPos.getLineHeight(), 0);
            /*END PLAYER COORDS UPDATE*/
        } else {
            playerPos.setSize(this.main.getGuiFont().getCharSet().getRenderedSize());
            playerPos.setText("X: " + (int) this.main.getCamera().getLocation().x + ", " + "Y: " + (int) this.main.getCamera().getLocation().y + ", " + "Z: " + (int) this.main.getCamera().getLocation().z + "   (FREE CAMERA)"); // crosshairs
            playerPos.setLocalTranslation(main.getSettings().getWidth() - playerPos.getLineWidth() * 1.1f, main.getSettings().getHeight() - playerPos.getLineHeight(), 0);
        }
        
       /* blockName.setText(String.valueOf(curr));
        blockName.setSize(main.getGuiFont().getCharSet().getRenderedSize());
        blockName.setLocalTranslation(main.getSettings().getWidth() - blockName.getLineWidth() * 1.2f, 0 + blockName.getLineHeight() * 1.2f, 0);*/


        if (Reference.debugging) {
            debugging.setText("Debugging" + "\n\n");
            debugging.setColor(ColorRGBA.Red);
            debugging.setSize(main.getGuiFont().getCharSet().getRenderedSize());
            debugging.setLocalTranslation(/*main.getSettings().getWidth() -*/debugging.getLineWidth() * 0.2f, main.getSettings().getHeight() - debugging.getLineHeight() * 0.6f, 0);
            this.main.getGuiNode().attachChild(debugging);
        } else {
            this.main.getGuiNode().detachChild(debugging);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

}
