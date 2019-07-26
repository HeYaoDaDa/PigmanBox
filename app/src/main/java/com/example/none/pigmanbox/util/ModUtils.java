package com.example.none.pigmanbox.util;

import com.example.none.pigmanbox.modle.Mod;

/**
 * Mod Utils
 */
public interface ModUtils {
    //exits mod
    public static Mod[] mods = new Mod[10000];

    /**
     * no id in mod name.create a id.
     * @return
     * @throws Exception
     */
    public static int createId() throws Exception {
        for (int i = mods.length - 1; i >= 0; i--) {
            if (mods[i] == null) {
                return i;
            }
        }
        throw new Exception("Mod.mods full.");
    }
}
