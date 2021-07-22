package com.jme3.recast4j.Recast.Utils;

/**
 * This is a helper class to deal with "Flags". It handles setting multiple single flags in a typesafe way and
 * abstracts the bitwise operations.<br>
 * There are generally two types of methods in this class: setFlagByBitmask/ofBitmask and setFlag/of.<br>
 * Typically you want to use the regular methods, e.g. of(3)/setFlag(3) will set the 3th bit into those flags.<br>
 * Sometimes however (especially in c-style-header files) flags aren't counted like 1, 2, 3, 4 but instead 1, 2, 4, 8,
 * matching directly the bitmasks (<code>1 << id</code>). To provide compatibility with these code-bases, the Bitmask
 * methods exist.<br>
 *
 * @author MeFisto94
 */
public class Flags {
    int value;

    public Flags(int value) {
        this.value = value;
    }

    public Flags setFlag(int flagId) {
        return setFlagByBitmask(1 << flagId);
    }

    public Flags setFlagByBitmask(int flag) {
        value |= flag;
        return this;
    }

    public static Flags of(int... flags) {
        Flags f = new Flags(0);

        for (int i = 0; i < flags.length; i++) {
            f.setFlag(i);
        }

        return f;
    }

    public static Flags ofBitmask(int... flags) {
        Flags f = new Flags(0);

        for (int i = 0; i < flags.length; i++) {
            f.setFlagByBitmask(i);
        }

        return f;
    }

    public int getValue() {
        return value;
    }
}
