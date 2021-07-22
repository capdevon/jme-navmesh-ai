package mygame.controls;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import mygame.interfaces.AnimKey;
import mygame.interfaces.DataKey;
import mygame.interfaces.EnumPosType;

/**
 * Implements all animations of a spatial by reading the spatials physical 
 * pos. Spatial must have AnimControl to use this control.
 *
 * @author mitm
 */
public class AnimationControl extends AbstractControl {

    private AnimChannel animChannel;
    private AnimControl animControl;
    private SkeletonControl skeletonControl;
    private static final Logger LOG = Logger.getLogger(AnimationControl.class.getName());
    private int posType;

    public AnimationControl() {

    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            return;
        }
        
        spatial.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getControl(AnimControl.class) != null) {
                    animControl = node.getControl(AnimControl.class);
                    animControl.addListener(new AnimationEventListener());
                    animChannel = animControl.createChannel();
                }
                
                if (node.getControl(SkeletonControl.class) != null) {
                    skeletonControl = node.getControl(SkeletonControl.class);
                }
            }
        });

        
        //no animControl so bail
        if (animControl == null) {
            LOG.log(Level.SEVERE, "No AnimControl {0}", spatial);
            throw new RuntimeException();
        }

        //no SkeletonControl so bail
        if (skeletonControl == null) {
            LOG.log(Level.SEVERE, "No SkeletonControl {0}", spatial);
            throw new RuntimeException();
        }
        
        posType = getPosType();
        for (EnumPosType pos : EnumPosType.values()) {
            if (pos.pos() == posType) {
                switch (pos) {
                    case POS_STANDING:
                        animChannel.setAnim(AnimKey.IDLE);
                        animChannel.setLoopMode(LoopMode.Loop);
                        //channel.setSpeed(1f);
                        break;
                    case POS_WALKING:
                        animChannel.setAnim(AnimKey.WALK);
                        animChannel.setLoopMode(LoopMode.Loop);
                        //channel.setSpeed(1f);
                        break;
                    case POS_RUNNING:
                        animChannel.setAnim(AnimKey.RUN);
                        animChannel.setLoopMode(LoopMode.Loop);
                        //channel.setSpeed(1f);
                        break;
                    default:
                        animChannel.setAnim(AnimKey.TPOSE);
                        animChannel.setLoopMode(LoopMode.DontLoop);
                        //channel.setSpeed(1f);
                        break;
                }
            }
        }

    }

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        AnimationControl control = new AnimationControl();
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }

    //Checks spatial physical pos whenver an animation ends. Sets animation 
    //based off that pos.
    private class AnimationEventListener implements AnimEventListener {

        @Override
        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            //position is set by MovementControl after game start
            posType = getPosType();
            //One animation must run otherwise default to TPose to show theres a
            //problem. Has to be an int, boolean, string, float, array pos
            //because it's stored in userData.
            for (EnumPosType pos : EnumPosType.values()) {
                if (pos.pos() == posType) {
                    switch (pos) {
                        case POS_STANDING:
                            channel.setAnim(AnimKey.IDLE);
                            channel.setLoopMode(LoopMode.Loop);
                            //channel.setSpeed(1f);
                            break;
                        case POS_SWIMMING:
                        case POS_WALKING:
                            channel.setAnim(AnimKey.WALK);
                            channel.setLoopMode(LoopMode.Loop);
                            //channel.setSpeed(1f);
                            break;
                        case POS_RUNNING:
                            channel.setAnim(AnimKey.RUN);
                            channel.setLoopMode(LoopMode.Loop);
                            //channel.setSpeed(1f);
                            break;
                        default:
                            channel.setAnim(AnimKey.TPOSE);
                            channel.setLoopMode(LoopMode.DontLoop);
                            //channel.setSpeed(1f);
                            break;
                    }
                }
            }
        }

        @Override
        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

        }
    }

    /**
     * @return the AnimChannel
     */
    public AnimChannel getAnimChannel() {
        return animChannel;
    }

    /**
     *
     * @return spatials physical pos
     */
    private int getPosType() {
        return (Integer) spatial.getUserData(DataKey.POSITION_TYPE);
    }

}