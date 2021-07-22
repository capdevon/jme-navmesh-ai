package mygame.controls;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import mygame.interfaces.DataKey;
import mygame.interfaces.EnumPosType;
import mygame.interfaces.ListenerKey;

/**
 * Controls the spatials movement. Speed is derived from EnumPosType.
 * 
 * @author mitm
 */
public class PCControl extends BetterCharacterControl implements ActionListener {

    private boolean forward;
    private float moveSpeed;
    private int position;

    public PCControl(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        this.moveSpeed = 0;
        Vector3f modelForwardDir = spatial.getWorldRotation().mult(Vector3f.UNIT_Z);  
        walkDirection.set(0, 0, 0);
        if (forward) {
            position = getPositionType();
            for (EnumPosType pos : EnumPosType.values()) {
                if (pos.pos() == position) {
                    switch (pos) {
                        case POS_SWIMMING:
                            moveSpeed = EnumPosType.POS_SWIMMING.speed();
                            break;
                        case POS_WALKING:
                            moveSpeed = EnumPosType.POS_WALKING.speed();
                            break;
                        case POS_RUNNING:
                            moveSpeed = EnumPosType.POS_RUNNING.speed();
                            break;
                        default:
                            moveSpeed = 0f;
                            break;
                    }
                }
            }
//            if (this.rigidBody.getLinearVelocity().length() > this.getMoveSpeed()) {
//                System.out.println("Velocity = " + this.rigidBody.getLinearVelocity().length());
//            }
            walkDirection.addLocal(modelForwardDir.mult(moveSpeed));
        }
        setWalkDirection(walkDirection);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(ListenerKey.MOVE_FORWARD)) {
            forward = isPressed;
        }
        if (name.equals((ListenerKey.JUMP))) {
            jump();
        }
    }

    //Override default collisionshape due to .7 offset.
    @Override
    protected CollisionShape getShape() {
        float radius = getFinalRadius();
        float height = getFinalHeight();
        float cylinder_height = height - (2.0f * radius);
        CylinderCollisionShape cylinder = new CylinderCollisionShape(
                new Vector3f(radius, cylinder_height / 2f, radius)/*NB constructor want half extents*/, 1);
        SphereCollisionShape sphere = new SphereCollisionShape(getFinalRadius());
        CompoundCollisionShape compoundCollisionShape = new CompoundCollisionShape();
        compoundCollisionShape.addChildShape(sphere,
                new Vector3f(0,/*sphere half height*/ radius, 0)); // bottom sphere
        compoundCollisionShape.addChildShape(cylinder,
                new Vector3f(0,/*half sphere height*/ (radius) +/*cylinder half height*/ (cylinder_height / 2.f), 0)); // cylinder, on top of the bottom sphere
        compoundCollisionShape.addChildShape(sphere,
                new Vector3f(0,/*half sphere height*/ (radius) +/*cylinder height*/ (cylinder_height), 0)); // top sphere       
        return compoundCollisionShape;
    }
    
    //need to overide because we extended BetterCharacterControl
    @Override
    public PCControl cloneForSpatial(Spatial spatial) {
        try {
            PCControl control = (PCControl) super.clone();
            control.setSpatial(spatial); 
            return control;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }
    }

    //need to override because we extended BetterCharacterControl
    @Override
    public PCControl jmeClone() {
        try {
            return (PCControl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }
    }
    
    //gets the physical pos of spatial
    private int getPositionType() {
        return (int) spatial.getUserData(DataKey.POSITION_TYPE);
    }
}