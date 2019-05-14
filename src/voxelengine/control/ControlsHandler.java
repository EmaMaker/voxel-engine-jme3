package voxelengine.control;

import voxelengine.utils.Globals;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import voxelengine.VoxelEngine;
import voxelengine.block.CellId;
import voxelengine.block.TextureManager;
import static voxelengine.utils.Globals.chunkSize;
import static voxelengine.utils.Globals.debug;
import static voxelengine.utils.Globals.pGX;
import static voxelengine.utils.Globals.pGY;
import static voxelengine.utils.Globals.pGZ;
import voxelengine.world.WorldManager;
import static voxelengine.utils.Globals.pX;
import static voxelengine.utils.Globals.pY;
import static voxelengine.utils.Globals.pZ;

public class ControlsHandler extends AbstractAppState implements ActionListener, AnalogListener {

    //MISC CONTROLS
    boolean fastBlock = false;

    DecimalFormat df = new DecimalFormat();
    DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();

    byte placeStep = 0;
    byte breakStep = 0;
    public boolean placeBlock = false;
    public boolean breakBlock = false;

    public int currentBlockId = CellId.ID_DIRT;
    public float currentBlockNum = 1;
    CollisionResults results = new CollisionResults();
    Vector3f top1 = new Vector3f(), top2 = new Vector3f(), top3 = new Vector3f(), top4 = new Vector3f();

    SimpleApplication app;
    VoxelEngine engine;
    WorldManager prov;
    AppStateManager stateManager;

    //PLAYER CONTROLS
    public Vector3f respawnPoint = new Vector3f(pGX, pGY, pGZ);

    Box box = new Box(0.4f, 0.8f, 0.4f);
    public Geometry playerModel = new Geometry("Player", box);
    Material mat;
    public CharacterControl playerControl;

    Vector3f walkDirection = new Vector3f();
    boolean left = false, right = false, up = false, down = false;
    Vector3f camDir = new Vector3f();
    Vector3f camLeft = new Vector3f();
    Vector3f camPos = new Vector3f();

    float speed = .4f, strafeSpeed = .2f, headHeight = 1.75f;

    int blockDistance = 12;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        //MISC
        prov = stateManager.getState(WorldManager.class);
        engine = stateManager.getState(VoxelEngine.class);
        this.stateManager = stateManager;
        this.app = (SimpleApplication) app;
        
        //PLAYER CONTROL
        playerModel.setMaterial(new Material(app.getAssetManager(), "Materials/UnshadedArray.j3md"));
        this.app.getRootNode().attachChild(playerModel);
        this.app.getCamera().setFrustumPerspective(45f, (float) this.app.getCamera().getWidth() / this.app.getCamera().getHeight(), 0.01f, 1000f);
        this.app.getCamera().setLocation(new Vector3f(0, 6, 0));

        playerControl = new CharacterControl(CollisionShapeFactory.createMeshShape(playerModel), 1f);
        playerControl.setJumpSpeed(10f);
        playerModel.addControl(playerControl);
        this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(playerControl);
        playerControl.warp(respawnPoint);
        //System.out.println(respawnPoint);

        initControls();
    }

    private void initControls() {
        //MISC CONTROLS
        app.getInputManager().addMapping("place", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("remove", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        app.getInputManager().addMapping("changeBlock+", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        app.getInputManager().addMapping("changeBlock-", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        //debug utils
        app.getInputManager().addMapping("boundingBox", new KeyTrigger(KeyInput.KEY_B));
        app.getInputManager().addMapping("debug", new KeyTrigger(KeyInput.KEY_V));
        app.getInputManager().addMapping("fastblock", new KeyTrigger(KeyInput.KEY_F));
        app.getInputManager().addMapping("camera", new KeyTrigger(KeyInput.KEY_C));

        app.getInputManager().addListener(this, "place");
        app.getInputManager().addListener(this, "remove");

        app.getInputManager().addListener(this, "changeBlock+");
        app.getInputManager().addListener(this, "changeBlock-");

        app.getInputManager().addListener(this, "boundingBox");
        app.getInputManager().addListener(this, "debug");
        app.getInputManager().addListener(this, "fastblock");
        app.getInputManager().addListener(this, "camera");

        //PLAYER CONTROLS
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
    public void onAction(String name, boolean keyPressed, float tpf) {
        switch (name) {
            //MISC CONTROLS
            case "boundingBox":
                if (keyPressed) {
                    Globals.main.getStateManager().getState(BulletAppState.class).setDebugEnabled(!Globals.main.getStateManager().getState(BulletAppState.class).isDebugEnabled());
                }
                break;
            case "debug":
                if (keyPressed) {
                    Globals.setDebugEnabled(!Globals.debugEnabled());
                }
                break;
            case "fastblock":
                if (keyPressed) {
                    fastBlock = !fastBlock;
                }
                break;
            case "place":
                placeStep = 25;
                break;
            case "break":
                breakStep = 25;
                break;
            case "camera":
                if (keyPressed) {
                    if (Globals.playerEnabled()) {
                        Globals.setPlayerEnabled(false);
                        Globals.main.getRootNode().detachChild(playerModel);
                    } else {
                        Globals.setPlayerEnabled(true);
                        Globals.main.getRootNode().attachChild(playerModel);
                        playerControl.warp(Globals.main.getCamera().getLocation());
                        playerModel.setLocalTranslation(Globals.main.getCamera().getLocation());
                    }
                }
                break;
            //PLAYER CONTROLS
            case "Left":
                left = keyPressed;
                break;
            case "Right":
                right = keyPressed;
                break;
            case "Up":
                up = keyPressed;
                break;
            case "Down":
                down = keyPressed;
                break;
            case "Jump":
                if (keyPressed) {
                    playerControl.jump();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        //removes the pointed
        switch (name) {
            case "remove":
                breakStep++;
                if (fastBlock || breakStep > 10) {
                    //breakBlock();
                    breakBlock = true;
                    breakStep = 0;
                }
                break;
            case "place":
                placeStep++;
                if (fastBlock || placeStep > 10) {
                    //placeBlock();
                    placeBlock = true;
                    placeStep = 0;
                }
                break;

            case "changeBlock+":
                if (currentBlockNum < TextureManager.textures.size() - 1) {
                    currentBlockNum += 0.35;
                    currentBlockId = (int) currentBlockNum;
                }
                break;
            case "changeBlock-":
                if (currentBlockNum > 0.35) {
                    currentBlockNum -= 0.35;
                    currentBlockId = (int) currentBlockNum;
                }
                break;
        }
    }

    @Override
    public void update(float tpf) {
        if (Globals.playerEnabled()) {
            /*START POSITION UTILS*/
            pX = (int) app.getCamera().getLocation().getX() / chunkSize;
            pY = (int) app.getCamera().getLocation().getY() / chunkSize;
            pZ = (int) app.getCamera().getLocation().getZ() / chunkSize;

            if (playerModel.getLocalTranslation().y < -10) {
                playerControl.warp(respawnPoint);
            }
            /*END POSITION UTILS
            
            START WALKING UTILS*/
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
        } else {
            pX = (int) app.getCamera().getLocation().getX() / chunkSize;
            pY = (int) app.getCamera().getLocation().getY() / chunkSize;
            pZ = (int) app.getCamera().getLocation().getZ() / chunkSize;
        }
    }

    Vector3f pt, v;
    ArrayList<Vector3f> vs;
    int newX, newY, newZ;

    public void breakBlock() {
        debug("|===========================================|");
        Ray ray = new Ray(Globals.main.getCamera().getLocation(), Globals.main.getCamera().getDirection());
        Globals.terrainNode.collideWith(ray, results);

        if (results.getClosestCollision() != null) {
            pt = fixCoords(results.getClosestCollision().getContactPoint());
            if (pt.distance(app.getCamera().getLocation()) < blockDistance) {
                prov.setCellFromVertices(findNearestVertices(pt), CellId.ID_AIR);
            }
        }
        results.clear();
        debug("|===========================================|");
    }

    public void placeBlock() {
        debug("\n|===========================================|");
        Ray ray = new Ray(Globals.main.getCamera().getLocation(), Globals.main.getCamera().getDirection());
        Globals.terrainNode.collideWith(ray, results);

        if (results.getClosestCollision() != null) {
            pt = fixCoords(results.getClosestCollision().getContactPoint());
            //if (Math.sqrt(Math.pow(pt.x - pX * chunkSize, 2) + Math.pow(pt.y - pY * chunkSize, 2) + Math.pow(pt.z - pZ * chunkSize, 2)) <= Globals.getPickingDistance()) {
            if (pt.distance(playerModel.getLocalTranslation()) < blockDistance) {

                vs = findNearestVertices(pt);
                v = prov.getCellPosFromVertices(vs);
                newX = (int) pt.x;
                newY = (int) pt.y;
                newZ = (int) pt.z;

                switch (getFaceFromVertices(vs, (int) pt.x, (int) pt.y, (int) pt.z)) {
                    case 0:
                        newX--;
                        break;
                    case 2:
                        newZ--;
                        break;
                    case 5:
                        newY++;
                        break;
                    default:
                        break;
                }

                prov.setCell(newX, newY, newZ, (byte) currentBlockId);
                results.clear();
            }
        }
        debug("|===========================================|\n");
    }

    public byte getFaceFromVertices(ArrayList<Vector3f> al, int worldX, int worldY, int worldZ) {
        System.out.println(al + ", " + worldX + ", " + worldY + ", " + worldZ);
        if (al.get(0).x == al.get(1).x && al.get(0).x == al.get(2).x && al.get(0).x == al.get(3).x) {
            if (al.get(0).x == worldX) {
                return 0;
            }
            return 1;
        } else if (al.get(0).y == al.get(1).y && al.get(0).y == al.get(2).y && al.get(0).y == al.get(3).y) {
            if (al.get(0).y == worldY) {
                return 5;
            }
            return 4;
        } else if (al.get(0).z == al.get(1).z && al.get(0).z == al.get(2).z && al.get(0).z == al.get(3).z) {
            if (al.get(0).z == worldZ) {
                return 2;
            }
            return 3;
        }
        return Byte.MAX_VALUE;
    }

    public Vector3f fixCoords(Vector3f v) {
        unusualSymbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(unusualSymbols);
        df.setMaximumFractionDigits(4);
        float fx = Float.parseFloat(df.format(v.x)), fy = Float.parseFloat(df.format(v.y)), fz = Float.parseFloat(df.format(v.z));
        if (fx - (int) v.x == .99) {
            fx += 0.1f;
        }
        if (fy - (int) v.y == .99) {
            fy += 0.1f;
        }
        if (fz - (int) v.z == .99) {
            fz += 0.1f;
        }
        return new Vector3f(fx, fy, fz);
    }

    //finds the 4 nearest vertices (a face of a cell) in the given chunk relative to the given vector
    public ArrayList<Vector3f> findNearestVertices(Vector3f s) {
        ArrayList<Vector3f> al = new ArrayList<>();
        //al.clear();

        //facing ±z
        if (s.z - (int) s.z == 0) {
            top1.set(((int) s.x) + 1, (int) s.y + 1, (int) s.z);
            top2.set(top1.x - 1, top1.y, top1.z);
            top3.set(top1.x, top1.y - 1, top1.z);
            top4.set(top1.x - 1, top1.y - 1, top1.z);
            //debug("Hit ±Z");
        } //facing ±y
        else if (s.y - (int) s.y == 0) {
            //only finds the top-right vertex, the others are relative to this
            top1.set(((int) s.x) + 1, s.y, ((int) s.z) + 1);
            top2.set(top1.x - 1, top1.y, top1.z);
            top3.set(top1.x, top1.y, top1.z - 1);
            top4.set(top1.x - 1, top1.y, top1.z - 1);
            //debug("Hit ±Y");
        } //facing ±x
        else if (s.x - (int) s.x == 0) {
            top1.set(((int) s.x), ((int) s.y) + 1, (((int) s.z) + 1));
            top2.set(top1.x, top1.y, top1.z - 1);
            top3.set(top1.x, top1.y - 1, top1.z);
            top4.set(top1.x, top1.y - 1, top1.z - 1);
            //debug("Hit ±X");
        }

        al.add(top1);
        al.add(top2);
        al.add(top3);
        al.add(top4);
        return al;
    }
}
