package mygame.interfaces;
/**
 * A physical pos with speed settings.
 * 
 * @author mitm
 */
public enum EnumPosType {

    POS_DEAD(0, 0.0f),
    POS_MORTAL(1, 0.0f),
    POS_INCAP(2, 0.0f),
    POS_STUNNED(3, 0.0f),
    POS_SLEEPING(4, 0.0f),
    POS_RESTING(5, 0.0f),
    POS_SITTING(6, 0.0f),
    POS_FIGHTING(7, 0.0f),
    POS_TPOSE(8, 0.0f),
    POS_STANDING(9, 0.0f),
    POS_SWIMMING(10, 1.5f),
    POS_WALKING(11, 3.0f),
    POS_RUNNING(12, 6.0f);

    private final float speed;
    private final int pos;

    EnumPosType(int positionType, float speed) {
        this.speed = speed;
        this.pos = positionType;
    }

    /**
     * @return the speed
     */
    public float speed() {
        return speed;
    }

    /**
     * @return the pos
     */
    public int pos() {
        return pos;
    }
    
    public static EnumPosType getPosType(int pos) {
        for(EnumPosType mt : EnumPosType.values())
            if(mt.pos == pos)
                return mt;

        throw new IllegalArgumentException();
    }

}