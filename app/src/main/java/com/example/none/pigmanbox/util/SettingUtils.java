package com.example.none.pigmanbox.util;

import com.blankj.utilcode.util.PathUtils;

/**
 * app setting,url.
 */
public interface SettingUtils {
    public static boolean backup = false;//is obb file unzip backup.
    public static final String ZIP_PASSWOID = "614255348";
    public static final String PATH_ANDROID_OBB = PathUtils.getExternalStoragePath() +"/Android/obb/";
    public static final String GAME_PAGE_NAME = "com.kleientertainment.doNotStarve";//backName
    public static final String MOD_MODINFO_NAME = "modinfo.lua";//modinfo.lua
    public static final String[] MOD_MODSETTINGS = new String[]{
            "modsettings.lua",
            "dsmods",
            "bmmods"
    };
}
