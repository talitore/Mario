package Mario;

import Mario.CameraControl.ControlDirection;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends SimpleApplication implements PhysicsCollisionListener{

    /** Prepare the Application States */
    private BulletAppState bulletAppState;
    private StartScreenState startScreenState;
    private HudScreenState hudScreenState;
    private PauseScreenState pauseScreenState;
    /** Prepare Materials */
    Material player_mat;
    Material ground_mat;
    Material brick_mat;
    Material pipe_mat;
    Material question_mat;
    Material super_mushroom_mat;
    Material one_up_mushroom_mat;
    Material star_mat;
    Material coin_mat;
    /** Prepare geometries and physical nodes for bricks and cannon balls. */
    private RigidBodyControl brick_phy;
    private static final Box brick;
    private RigidBodyControl ground_phy;
    private static final Box ground;
    private static Node brickNode;
    private static Node physicsNodeA;
    private static Node physicsNodeB;
        /** dimensions used */
    private static final float brickLength = .5f;
    private static final float brickWidth  = .5f;
    private static final float brickHeight = .5f;
    /** Initialize the brick geometry */
    static {
        brick = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
        brick.scaleTextureCoordinates(new Vector2f(1f, 1f));
        /** Initialize the floor geometry */
        ground = new Box(Vector3f.ZERO, 69.0f / 2.0f, 1.0f, 0.5f);
        ground.scaleTextureCoordinates(new Vector2f(16f, 1f));
    }
    /** App globals */
    Boolean isRunning = false;
    Boolean newGame = true;
    ColorRGBA backgroundColor = new ColorRGBA(104f / 255f, 136f / 255f, 1f, 0f);
    DirectionalLight sun = new DirectionalLight();
    private final int totalBricks =  43;
    private String brickLocationStringArray[];
    List<Vector2f> brickLocationVector2fArray = new ArrayList<Vector2f>(50);
    FileArrayProvider fap = new FileArrayProvider();
        /** Camera */
//    static int facing = 2; // 1 - left, 2 - Right, 0 - reset
    boolean left = false, right = false, up = false, down = false;
    CameraNode camNode;
    Vector3f cameraHeight = new Vector3f(0.0f, 5.0f, 0.0f);
    Vector3f camDistance = new Vector3f(-15.0f, 5.0f, 0.0f);
        /** Prepare character */
    CharacterControl charControl;
    private Spatial charSpatial;
    private Node charNode;
    float runSpeed = 1.0f;
    float charRightSpeed = 10.0f;
    float charLeftSpeed  = 10.0f;
    Vector3f charRightVelocity = Vector3f.UNIT_X.mult(charRightSpeed);
    Vector3f charLeftVelocity  = Vector3f.UNIT_X.negate().mult(charLeftSpeed);
    Vector3f charUpSpeed = Vector3f.UNIT_Y.mult(0.25f);    
        /** Temp Vectors */
    Vector3f walkDirection = Vector3f.ZERO;
        /** Temp Terrain */
    RigidBodyControl terrainPhysicsNode;
        /** Nifty shared fields */
    public NiftyJmeDisplay niftyDisplay;
    public Nifty nifty;
    /*
     * Required method to run applet
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    /*
     * Initialize App
     */
    @Override
    public void simpleInitApp() {
        /*
         * Create new ScreenState instances
         */
        bulletAppState = new BulletAppState();
        startScreenState = new StartScreenState(this);
        hudScreenState = new HudScreenState(this);
        pauseScreenState = new PauseScreenState(this);
        /*
         * Initialize GUI - Must come first always as states are attached here
         */
        initGUI();
        /*
         * Initialize Materials
         */
        initMaterials();
        /*
         * Initialize World
         */
        initWorld();
        /*
         * Initialize Camera
         */
        initCamera();
        /*
         * Initialize Keys
         */
        initKeys();
        
        /** Collision debug */
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    }
    /*
     * Initialize GUI
     */
    private void initGUI() {
        /*
         * Set background color
         */
        viewPort.setBackgroundColor(backgroundColor);
        /*
         * Disable the fly cam
         */
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(10.f);
        /* 
         * Allow mouse be visible much easier to interact with GUI
         */
        inputManager.setCursorVisible(true);
        /**
         * Create new NiftyDisplay instance - will be only one
         * used throughout program
         */
        niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/start.xml", "start", startScreenState);
        nifty.addXml("Interface/hud.xml");
        nifty.addXml("Interface/pause.xml");
        /*
         * Attach ScreenStates which includes controller
         */
        stateManager.attach(startScreenState);
        stateManager.attach(hudScreenState);
        stateManager.attach(pauseScreenState);
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        /*
         * Attach the Nifty display to the gui view port as a processor
         */
        guiViewPort.addProcessor(niftyDisplay);
    }
    /*
     * Initialize Materials
     */
    private void initMaterials() {
        /*
         * Temp brick floor mat
         */
        ground_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture ground_DMap = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg");
        ground_DMap.setWrap(Texture.WrapMode.Repeat);
        ground_mat.setTexture("DiffuseMap", ground_DMap);
        Texture ground_NMap = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall_normal.jpg");
        ground_NMap.setWrap(Texture.WrapMode.Repeat);
        ground_mat.setTexture("NormalMap", ground_NMap);
//        ground_mat.setColor("Color", ColorRGBA.Brown);
    }
    /*
     * Initialize World
     */
    private void initWorld() {
        /** Read level from text into array */
        try {
            /** String array from text */
            brickLocationStringArray = fap.readLines("assets/Scenes/level/1-1.txt");
            for (String line: brickLocationStringArray) {
                System.out.println(line);
            }
            /** float array from String array*/
            for (int i = 0; i < brickLocationStringArray.length; i = i + 2) {
                brickLocationVector2fArray.add(new Vector2f(Float.parseFloat(brickLocationStringArray[i]), Float.parseFloat(brickLocationStringArray[i+1])));
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        /** Add AmbientLight */
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
        /** Add directional lighting */ 
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(2.0f, -2.0f, -2.0f).normalizeLocal());
        rootNode.addLight(sun);
        /* Make a solid floor and add it to the scene */
        Geometry ground_geo = new Geometry("Ground", ground);
            /** Attach material */
        ground_geo.setMaterial(ground_mat);
            /** Move ground */
        ground_geo.setLocalTranslation(69.0f / 2f + 0.5f, -0.5f, 0.0f);
//        System.out.println("**********************************");
//        System.out.println("Ground GEO: " + ground_geo.getWorldTranslation());
//        System.out.println("**********************************");
            /* Make the floor physical with mass 0.0f! */
        ground_phy = new RigidBodyControl(0.0f);
        ground_geo.addControl(ground_phy);
        bulletAppState.getPhysicsSpace().add(ground_phy);
        rootNode.attachChild(ground_geo);
            /** Add bricks */
        brickNode = new Node("Brick Node");
        rootNode.attachChild(brickNode);
        for (int i = 0; i < brickLocationVector2fArray.size() ; i++) {
            Geometry brick_geo = new Geometry("brick", brick);
            brickNode.attachChild(brick_geo);
                /** Attach material */
            brick_geo.setMaterial(ground_mat);
                /** Move brick */
            brick_geo.setLocalTranslation(brickLocationVector2fArray.get(i).getX(), brickLocationVector2fArray.get(i).getY(), 0.0f);
            brick_phy = new RigidBodyControl(0.0f);
            brick_geo.addControl(brick_phy);
            bulletAppState.getPhysicsSpace().add(brick_phy);
        }
        /*
         * Mario
         */
            /** Load Mario Model */
        charSpatial = assetManager.loadModel("Models/Mario/Mario.j3o");
            /** Move Mario Model */
        charSpatial.scale(.02f);
        charNode = new Node("Char Node");
        charNode.attachChild(charSpatial);
        charNode.setLocalTranslation(new Vector3f(2.5f, 2.0f, 0.0f));
            // Create a appropriate physical shape for it
        CapsuleCollisionShape collisionShape = new CapsuleCollisionShape(.5f, 1.20f);
        charControl = new CharacterControl(collisionShape, .1f);
            // Attach physical properties to model and PhysicsSpace
        charNode.addControl(charControl);
        // Rotate CharControl which rotates spatial
        charControl.setViewDirection(Vector3f.UNIT_X);
        bulletAppState.getPhysicsSpace().add(charControl);
        // Attach charNode to rootNode
        rootNode.attachChild(charNode);
    }
    /*
     * Initialize Camera method
     */
    private void initCamera() {
//        /** Create a camera Node; Method 1 */
        camNode = new CameraNode("Camera Node", cam);
        //This mode means that camera copies the movements of the spatial:
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        //Attach the camNode to the target:
        charNode.attachChild(camNode);
        //Position the camNode above and to the -X of the model/ground so it looks like a side-scroller
        camNode.setLocalTranslation(camDistance);
        //Rotate the camNode to look at the node, add height to make the camera focus above the model
        camNode.lookAt(charNode.getLocalTranslation().add(cameraHeight), Vector3f.UNIT_Y); 
        
//        /** Create chase cam; Method 2 */
//        ChaseCamera chaseCam = new ChaseCamera(cam, charNode, inputManager);
//        chaseCam.setSmoothMotion(true);
//        chaseCam.setInvertVerticalAxis(true);
//        chaseCam.setTrailingEnabled(false);
//        chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(3));
    }
    /*
     * Initialize Keys
     */
    private void initKeys() {
        /*
         * Mapping key inputs to events
         */
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_W),
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S),
                new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        /*
         * Add the events to the action listener.
         */
        inputManager.addListener(actionListener, new String[]{"Pause", "Left", "Right", "Jump", "Down", "Shoot"});
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Pause") && !keyPressed) {
                /*
                 * Toggle pause state
                 */
                isRunning = !isRunning;
                System.out.println("Paused: " + isRunning);
            }
            if (isRunning) {
                if (name.equals("Left")) {
                    if (keyPressed) {
                        left = true;
//                        facing = 1;
                    } else {
                        left = false;
//                        facing = 0;
                    }
                } else if (name.equals("Right")) {
                    if (keyPressed) {
                        right = true;
//                        facing = 2;
                    } else {
                        right = false;
//                        facing = 0;
                    }
                } else if (name.equals("Jump")) {
                    if (keyPressed) {
                        up = true;
                    } else {
                        up = false;
                    }
                } else if (name.equals("Down")) {
                    if (keyPressed) {
                        down = true;
                    } else {
                        down = false;
                    }
                }
                if (name.equals("Shoot")) {
//                    shoot();
                }
            } else {
                if (!name.equals("Pause")) {
                    System.out.println("Press P to unpause.");
                }
            }
        }
    };
    /*
     * Physics Collision Event
     */
    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (event.getNodeA().getName().equals("Char Node") ) {
            physicsNodeA = (Node) event.getNodeB();
            if (event.getNodeB() instanceof Node) {
                physicsNodeB = (Node) event.getNodeB();
            }
            else {
                physicsNodeB = event.getNodeB().getParent();
            }
//            System.out.println("Collision detected!! Node A: " + physicsNodeA.getName()
//                    + ". Node B: " + physicsNodeB.getName());
        } else if (event.getNodeB().getName().equals("Char Node") ) {
            if (event.getNodeA() instanceof Node) {
                physicsNodeA = (Node) event.getNodeA();
            }
            else {
                physicsNodeA = event.getNodeA().getParent();
            }
            physicsNodeB = (Node) event.getNodeB();
//            System.out.println("Collision detected!! Node B: " + physicsNodeB.getName()
//                    + ". Node A: " + physicsNodeA.getName());
        }
    }
    /* 
     * Update loop 
     */
    @Override
    public void simpleUpdate(float tpf) {
//        System.out.println(charGeom.getWorldRotation().getX() + " " + charGeom.getWorldRotation().getY() 
//                + " " + charGeom.getWorldRotation().getZ() + " " + charGeom.getWorldRotation().getW());
//        System.out.println("camNode.getWorldTranslation()");
//        System.out.println(camNode.getWorldTranslation());
//        System.out.println("camNode.getWorldRotation()");
//        System.out.println(camNode.getWorldRotation());
        if (isRunning) {
            walkDirection.set(0,0,0);
            /* Temp enable cam */
//            flyCam.setEnabled(true);
            /*
             * Hide mouse when unpaused
             */
            if (inputManager.isCursorVisible()) {
                inputManager.setCursorVisible(false);
            }
            if (pauseScreenState.isEnabled()) {
                pauseScreenState.setEnabled(false);
            }
            else if (!hudScreenState.isEnabled()) {
                hudScreenState.setEnabled(true);
            }
            if (right) {
                walkDirection.addLocal(Vector3f.UNIT_X.mult(tpf * charRightSpeed * runSpeed));
            }
            if (left) {
                walkDirection.addLocal(Vector3f.UNIT_X.negate().mult(tpf * charLeftSpeed * runSpeed));
            }
            if (up) {
                walkDirection.addLocal(Vector3f.UNIT_Y);
            }
            if (down) {
                walkDirection.addLocal(Vector3f.UNIT_Y.negate());
            }
//            if (!character.onGround()) {
//                airTime = airTime + tpf;
//            } else {
//                airTime = 0;
//            }
            if (walkDirection.length() == 0) {
//                System.out.println("WD = 0");
                charControl.setWalkDirection(walkDirection);
//                if (!"stand".equals(animationChannel.getAnimationName())) {
//                    animationChannel.setAnim("stand", 1f);
//                }
            } else {
                if (up || down) {
                    //don't face up or down for now                    
                }
                else {
                    charControl.setViewDirection(walkDirection);                    
                }
//                if (airTime > .3f) {
//                    if (!"stand".equals(animationChannel.getAnimationName())) {
//                        animationChannel.setAnim("stand");
//                    }
//                } else if (!"Walk".equals(animationChannel.getAnimationName())) {
//                    animationChannel.setAnim("Walk", 0.7f);
//                }
            }
//            System.out.println(camNode.getLocalTranslation());
//            System.out.println(camNode.getLocalTranslation().add(walkDirection));
            charControl.setWalkDirection(walkDirection);
        } else {
            if (newGame) {
                if (!startScreenState.isEnabled()) {
                    startScreenState.setEnabled(true);
                }
            } else {
                /* Temp disable cam */
                flyCam.setEnabled(false);
                inputManager.setCursorVisible(true);
                if (!pauseScreenState.isEnabled()) {
                    pauseScreenState.setEnabled(true);
                }
                if (hudScreenState.isEnabled()) {
                    hudScreenState.setEnabled(false);
                }
            }
        }
    }
}