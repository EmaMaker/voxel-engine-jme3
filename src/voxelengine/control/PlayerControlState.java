package voxelengine.control;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import voxelengine.Main;
import voxelengine.utils.Globals;

public class PlayerControlState extends AbstractAppState implements ActionListener {

    public Vector3f respawnPoint = new Vector3f(8, 8, 8);

    SimpleApplication app;

    Box box = new Box(0.4f, 0.8f, 0.4f);
    public Geometry playerModel = new Geometry("Player", box);
    Material mat;
    public CharacterControl playerControl;

    Vector3f walkDirection = new Vector3f();
    boolean left = false, right = false, up = false, down = false;
    Vector3f camDir = new Vector3f();
    Vector3f camLeft = new Vector3f();
    Vector3f camPos = new Vector3f();
            
    float speed = .4f, strafeSpeed = .2f, headHeight = 3f;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        playerModel.setMaterial(mat);
        this.app.getRootNode().attachChild(playerModel);
        this.app.getCamera().setFrustumPerspective(45f, (float) this.app.getCamera().getWidth() / this.app.getCamera().getHeight(), 0.01f, 1000f);
        this.app.getCamera().setLocation(new Vector3f(0, 6, 0));

        playerControl = new CharacterControl(CollisionShapeFactory.createMeshShape(playerModel), 1f);
        playerControl.setJumpSpeed(10f);
        playerModel.addControl(playerControl);
        this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(playerControl);
        playerControl.warp(respawnPoint);

        this.app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        this.app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        this.app.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        this.app.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        this.app.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        this.app.getInputManager().addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LCONTROL));
        this.app.getInputManager().addListener(this, "Left");
        this.app.getInputManager().addListener(this, "Right");
        this.app.getInputManager().addListener(this, "Up");
        this.app.getInputManager().addListener(this, "Down");
        this.app.getInputManager().addListener(this, "Jump");
        this.app.getInputManager().addListener(this, "Sprint");
    }

    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {

        /*if (binding.equals("Sprint")) {
            speed = .4f;
            strafeSpeed = .2f;
        } else {
            speed = .2f;
            strafeSpeed = .1f;

        }*/
        switch (binding) {
            case "Left":
                left = isPressed;
                break;
            case "Right":
                right = isPressed;
                break;
            case "Up":
                up = isPressed;
                break;
            case "Down":
                down = isPressed;
                break;
            case "Jump":
                if (isPressed) {
                    playerControl.jump();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void update(float tpf) {

        /*START POSITION UTILS*/
        //this makes player always stay in positive Y
        if (playerModel.getLocalTranslation().y < -10) {
            playerControl.warp(new Vector3f(8, 8, 8));
        }
        /*END POSITION UTILS*/

 /*START WALKING UTILS*/
        camDir.set(app.getCamera().getDirection()).multLocal(speed, 0.0f, speed);
        camLeft.set(app.getCamera().getLeft()).multLocal(strafeSpeed);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        playerControl.setWalkDirection(walkDirection);

        camPos.set(playerModel.getLocalTranslation().x, playerModel.getLocalTranslation().y + headHeight, playerModel.getLocalTranslation().z);
        app.getCamera().setLocation(camPos);

        /*END WALKING UTILS*/
    }

    public int getX() {
        return (int) playerModel.getLocalTranslation().x;
    }

    public int getY() {
        return (int) playerModel.getLocalTranslation().y;
    }

    public int getZ() {
        return (int) playerModel.getLocalTranslation().z;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public void stop() {
        speed = 0;
        strafeSpeed = 0;
    }

}
