package mygame.control;

import mygame.utils.Debugger;
import mygame.utils.Reference;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import mygame.block.Cell;
import mygame.block.CellId;
import mygame.Main;
import mygame.world.WorldProvider;
import static mygame.utils.Debugger.debug;

public class ControlState extends AbstractAppState implements ActionListener, AnalogListener {

    boolean fastBlock = false;

    DecimalFormat df = new DecimalFormat();
    DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();

    /*Possible values:
    0-Pos X
    1-Pos Z
    2-Neg X
    3-Neg Z
     */
    public byte pointingTo = 0;

    byte placeStep = 0;
    byte breakStep = 0;

    Main app;
    WorldProvider prov;

    Ray ray;

    public CellId currentBlockId = CellId.DIRT;
    public float currentBlockNum = 1;

    //used to know the block name
    public static BitmapText blockName;

    AppStateManager manager;
    CollisionResults results = new CollisionResults();

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        manager = stateManager;
        this.app = (Main) app;
        blockName = new BitmapText(this.app.getGuiFont(), false);
        this.app.getGuiNode().attachChild(blockName);
        prov = stateManager.getState(WorldProvider.class);
        updateIds();

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
    }

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("boundingBox") && keyPressed) {
            Reference.main.getStateManager().getState(BulletAppState.class).setDebugEnabled(!Reference.main.getStateManager().getState(BulletAppState.class).isDebugEnabled());
        } else if (name.equals("debug") && keyPressed) {
            Debugger.debugging = !Debugger.debugging;
        } else if (name.equals("fastblock") && keyPressed) {
            fastBlock = !fastBlock;
        } else if (name.equals("place") && !keyPressed) {
            placeStep = 25;
        } else if (name.equals("remove") && !keyPressed) {
            breakStep = 25;
        } else if (name.equals("camera") && keyPressed) {
            if (this.app.getStateManager().getState(PlayerControlState.class).isEnabled()) {
                this.app.getStateManager().getState(PlayerControlState.class).respawnPoint = this.app.getCamera().getLocation();
                this.app.getStateManager().getState(PlayerControlState.class).stop();
                this.app.getStateManager().getState(PlayerControlState.class).setEnabled(false);
            } else {
                this.app.getStateManager().getState(PlayerControlState.class).playerControl.warp(this.app.getStateManager().getState(PlayerControlState.class).respawnPoint);
                this.app.getStateManager().getState(PlayerControlState.class).speed = .2f;
                this.app.getStateManager().getState(PlayerControlState.class).strafeSpeed = .35f;
                this.app.getStateManager().getState(PlayerControlState.class).setEnabled(true);
            }
        }
    }

    public void updateIds() {
        currentBlockId = CellId.values()[(int) currentBlockNum];
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {

        //removes the pointed
        switch (name) {
            case "remove":
                if (!fastBlock) {
                    if (breakStep % 25 == 0) {
                        breakBlock();
                    }
                } else {
                    breakBlock();
                }
                if (breakStep < 127) {
                    breakStep++;
                } else {
                    breakStep = 0;
                }
                break;
            case "place":
                if (!fastBlock) {
                    if (placeStep % 25 == 0) {
                        placeblock();
                    }
                } else {
                    placeblock();
                }
                if (placeStep < 127) {
                    placeStep++;
                } else {
                    placeStep = 0;
                }
                debug("!===========================================!");
                break;
            case "changeBlock+":
                if (currentBlockNum < CellId.values().length - 1) {
                    currentBlockNum += 0.35;
                    updateIds();
                }
                break;
            case "changeBlock-":
                if (currentBlockNum > 1.35) { //first element (index 0) is AIR, so it's ignored (as remove function exists) and 1 is used instead of 0
                    currentBlockNum -= 0.35;
                    updateIds();
                }
                break;
        }
    }

    public void breakBlock() {

        debug("|===========================================|");
        results.clear();
        ray = new Ray(app.getCamera().getLocation(), app.getCamera().getDirection());
        Reference.terrainNode.collideWith(ray, results);
        if (results.getClosestCollision() != null) {
            Vector3f pt = results.getClosestCollision().getContactPoint();
            pt = fixCoords(pt);

            Cell cell = prov.getCellFromVertices(findNearestVertices(pt));
            if (cell != null) {
                prov.setCell((int) cell.worldX, (int) cell.worldY, (int) cell.worldZ, CellId.AIR);
            }
            debug("|===========================================|");
            breakStep = 0;
        }
    }

    public void placeblock() {

        debug("!===========================================!");
        results.clear();
        ray = new Ray(app.getCamera().getLocation(), app.getCamera().getDirection());
        Reference.terrainNode.collideWith(ray, results);
        if (results.getClosestCollision() != null) {
            Vector3f pt = results.getClosestCollision().getContactPoint();

            debug("Original PT " + pt);
            debug("Fixed PT " + fixCoords(pt));

            Vector3f v = null;
            pt = fixCoords(pt);

            ArrayList al = findNearestVertices(pt);
            Cell cell = prov.getCellFromVertices(al);
            if (cell != null) {
                switch (cell.faceFromVertices(al)) {
                    case 3:
                        v = new Vector3f((int) cell.worldX - 1, (int) cell.worldY, (int) cell.worldZ);
                        break;
                    case 2:
                        v = new Vector3f((int) cell.worldX + 1, (int) cell.worldY, (int) cell.worldZ);
                        break;
                    case 5:
                        v = new Vector3f((int) cell.worldX, (int) cell.worldY, (int) cell.worldZ - 1);
                        break;
                    case 4:
                        v = new Vector3f((int) cell.worldX, (int) cell.worldY, (int) cell.worldZ + 1);
                        break;
                    case 0:
                        v = new Vector3f((int) cell.worldX, (int) cell.worldY + 1, (int) cell.worldZ);
                        break;
                    case 1:
                        v = new Vector3f((int) cell.worldX, (int) cell.worldY - 1, (int) cell.worldZ);
                        break;
                }
                if (v != null) {
                    prov.setCell((int) v.x, (int) v.y, (int) v.z, currentBlockId);
                }
            }
        }
        placeStep = 0;
    }

    public Vector3f fixCoords(Vector3f v) {
        unusualSymbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(unusualSymbols);
        df.setMaximumFractionDigits(2);
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
        Vector3f top1 = null, //top right
                top2 = null, //top left
                top3 = null, //bottom right
                top4 = null; //bottom left
        ArrayList<Vector3f> al = new ArrayList<>();
        //al.clear();

        //facing ±z
        if (s.z - (int) s.z == 0) {
            top1 = new Vector3f(((int) s.x) + 1, (int) s.y + 1, (int) s.z);
            top2 = new Vector3f(top1.x - 1, top1.y, top1.z);
            top3 = new Vector3f(top1.x, top1.y - 1, top1.z);
            top4 = new Vector3f(top1.x - 1, top1.y - 1, top1.z);
            debug("Hit ±Z");
            /*debug("Nearest vertex at top-right is at " + top1);
            debug("Nearest vertex at top-left is at " + top2);
            debug("Nearest vertex at bottom-right is at " + top3);
            debug("Nearest vertex at bottom-left is at " + top4);*/
        } //facing ±y
        else if (s.y - (int) s.y == 0) {
            //only finds the top-right vertex, the others are relative to this
            top1 = new Vector3f(((int) s.x) + 1, s.y, ((int) s.z) + 1);
            top2 = new Vector3f(top1.x - 1, top1.y, top1.z);
            top3 = new Vector3f(top1.x, top1.y, top1.z - 1);
            top4 = new Vector3f(top1.x - 1, top1.y, top1.z - 1);
            /*debug("Hit ±Y");
            debug("Nearest vertex at top-right is at " + top1);
            debug("Nearest vertex at top-left is at " + top2);
            debug("Nearest vertex at bottom-right is at " + top3);
            debug("Nearest vertex at bottom-left is at " + top4);*/
        } //facing ±x
        else if (s.x - (int) s.x == 0) {
            top1 = new Vector3f(((int) s.x), (int) s.y + 1, ((int) s.z + 1));
            top2 = new Vector3f(top1.x, top1.y, top1.z - 1);
            top3 = new Vector3f(top1.x, top1.y - 1, top1.z);
            top4 = new Vector3f(top1.x, top1.y - 1, top1.z - 1);
            /*debug("Hit ±X");
            debug("Nearest vertex at top-right is at " + top1);
            debug("Nearest vertex at top-left is at " + top2);
            debug("Nearest vertex at bottom-right is at " + top3);
            debug("Nearest vertex at bottom-left is at " + top4);*/
        }

        al.add(top1);
        al.add(top2);
        al.add(top3);
        al.add(top4);
        return al;
    }

}