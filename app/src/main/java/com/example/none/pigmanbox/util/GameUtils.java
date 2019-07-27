package com.example.none.pigmanbox.util;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.example.none.pigmanbox.modle.Game;
import com.example.none.pigmanbox.modle.Mod;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Class Utils
 */
public interface GameUtils {
    public static List<Game> gameList = new ArrayList<>();

    /**
     * 初始化，找出所有安装的饥荒游戏版本
     */
    public static void initGame() throws Exception {
        List<File> fileList = FileUtils.listFilesInDirWithFilter(new File(SettingUtils.PATH_ANDROID_OBB), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName();
                if (pathname.isFile() && fileName.endsWith(""))
                    if (pathname.isFile() && fileName.endsWith(".obb") && fileName.contains(SettingUtils.GAME_PAGE_NAME)) {
                        gameList.clear();
                        return true;
                    }
                return false;
            }
        }, true);
        for (File file : fileList) {
            String packNeme = getPackName(file);
            if (packNeme.equals("com.kleientertainment.doNotStarveShipWrecked"))
                packNeme = "com.kleientertainment.doNotStarveShipwrecked";
            String gameNeme = getGameName(packNeme);
            Game game = new Game(gameNeme, packNeme, file);
            gameList.add(game);
            initModlist(game);
        }
    }

    /**
     * 初始化游戏中mod列表
     *
     * @param game 游戏
     * @return
     */
    public static void initModlist(Game game) throws Exception {
        game.getModList().clear();//清空原来的数据
        ZipFile zipFile = new ZipFile(game.getObbFile());
        List<FileHeader> list = zipFile.getFileHeaders();
        for (FileHeader fileHeader : list) {
            String fileName = fileHeader.getFileName();
            if (fileName.endsWith(SettingUtils.MOD_MODINFO_NAME)) {
                String modFileName = fileName.substring(fileName.indexOf("mods/") + 5, fileName.lastIndexOf(SettingUtils.MOD_MODINFO_NAME) - 1);
                String modIDName = fileName.substring(fileName.indexOf("/ds") + 3, fileName.lastIndexOf(SettingUtils.MOD_MODINFO_NAME) - 1);
                if (StringUtil.isNumber(modIDName)) {
                    int id = Integer.parseInt(modIDName);
                    if(ModUtils.mods[id]!= null){
                        Mod mod = ModUtils.mods[id];
                        game.getModList().add(mod);
                    }else {
                        Mod mod = null;
                        mod = ModUtils.createMod(Zip4jUtils.readZipFileContent(zipFile,fileHeader),modIDName,false);
                        ModUtils.mods[mod.getId()] = mod;
                        game.getModList().add(mod);
                    }
                }else {
                    Mod mod = null;
                    mod = ModUtils.createMod(Zip4jUtils.readZipFileContent(zipFile,fileHeader),modFileName,false);
                    ModUtils.mods[mod.getId()] = mod;
                    game.getModList().add(mod);
                }
            }
        }
    }
    /**
     * 返回游戏名称
     *
     * @param packName 游戏包名
     * @return
     */
    public static String getGameName(String packName) {
        return AppUtils.getAppName(packName);
    }

    /**
     * 返回游戏包名
     *
     * @param obbFile obb文件
     * @return
     */
    public static String getPackName(File obbFile) {
        return obbFile.getParentFile().getName();
    }

    /**
     * 返回游戏的图标
     *
     * @return
     */
    public static Drawable getGameIcon(String packName) {
        return AppUtils.getAppIcon(packName);
    }
}
