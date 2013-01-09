package Mario;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class StartScreenState extends AbstractAppState implements ScreenController {
    /*
     * Initialize familiar vars
     */
    private Nifty nifty;
    private Screen screen;
    private ViewPort viewPort;
    private AssetManager assetManager;
    private Node rootNode, guiNode;
    private Node localRootNode = new Node("Hud Screen RootNode");
    private Node localGuiNode = new Node("Hud Screen GuidNode");
    /*
     * Initialize app as MAIN to have access to nifty
     */
    private Main app;
    /*
     * Custom methods
     */
    public Node getRootNode() {
        return rootNode;
    }
    /*
     * Constructor
     */
    public StartScreenState(SimpleApplication app) {
        this.rootNode = app.getRootNode();
        this.viewPort = app.getViewPort();
        this.guiNode = app.getGuiNode();
        this.assetManager = app.getAssetManager();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        System.out.println("Attached startScreenState");
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        System.out.println("Detached startScreenState");
    }
    /*
     * Nifty GUI ScreenControl methods
     */
    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        System.out.println("StartScreenState bind complete");
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
    /*
     * jME3 AppState methods 
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        nifty = this.app.nifty;
        /*
         * Switch to startscreen on initialize
         */
        nifty.gotoScreen("start");
        System.out.println("Initialized: app is " + this.app);
    }

    @Override
    public void update(float tpf) {
        /** jME update loop! */
    }
    
    @Override
    public void cleanup() {
      super.cleanup();
      // Unregister/detach all
      //nifty.exit();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            /*
             * Switch XML screens when attaching state
             */
            nifty.gotoScreen("start");
            System.out.println("StartScreenState setEnabled: " + enabled);
        }
        else {
            System.out.println("StartScreenState setEnabled: " + enabled);
        }
    }

    /** Custom methods from GUI */
    public void switchScreen(String screenName, String buttonPressed) {
        nifty.gotoScreen(screenName);
        /*
         * Set newGame to false to prevent startScreenstate from enabling
         */
        app.isRunning = true;
        app.newGame = false;
        System.out.println("!!Button pressed: " + buttonPressed);
    }
}