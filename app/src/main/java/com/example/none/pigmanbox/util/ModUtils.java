package com.example.none.pigmanbox.util;

import com.blankj.utilcode.util.FileUtils;
import com.example.none.pigmanbox.modle.Mod;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mod Utils
 */
public interface ModUtils {
    //exits mod
    public static Mod[] mods = new Mod[10000];

    /**
     * Determinr mods[id] is empty
     *
     * @param id   mod.id
     * @param mods find Mod Array
     * @return mods[id] == null retern true
     */
    public static boolean isEmpty(int id, Mod[] mods) {
        return mods[id] == null;
    }

    /**
     * Determinr mod is empty
     *
     * @param mod  mod object
     * @param mods find Mod Array
     * @return mods[id] == null retern true
     */
    public static boolean isEmpty(Mod mod, Mod[] mods) {
        return isEmpty(mod.getId(), mods);
    }

    /**
     * Determinr ModUtils.mods[id] is empty
     *
     * @param id mod.id
     * @return mods[id] == null retern true
     */
    public static boolean isEmpty(int id) {
        return isEmpty(id, mods);
    }

    /**
     * Determinr ModUtils.mod is empty
     *
     * @param mod mod
     * @return mods[id] == null retern true
     */
    public static boolean isEmpty(Mod mod) {
        return isEmpty(mod.getId());
    }

    /**
     * no id in mod name.create a id.
     *
     * @return null array id
     * @throws Exception array full
     */
    public static int createId() throws Exception {
        for (int i = mods.length - 1; i >= 0; i--) {
            if (mods[i] == null) {
                return i;
            }
        }
        throw new Exception("Mod.mods full.");
    }

    /**
     * no id in mod name.create a id.
     * @param data string
     * @return id
     */
    public static int createId(String data) throws Exception {
        String pattern = "\\d\\d\\d\\d";
        Pattern pattern1 = Pattern.compile(pattern);
        Matcher matcher = pattern1.matcher(data);
        if (matcher.find())
            return Integer.valueOf(matcher.group(0));
        else
            return createId();
    }

    /**
     * get dsmm accept mod name
     *
     * @param mod object
     * @return mod ds****
     */
    public static String getModDirName(Mod mod) {
        return getModDirName(mod.getId());
    }

    /**
     * get dsmm accept mod name
     *
     * @param id Mod id
     * @return mod ds****
     */
    public static String getModDirName(int id) {
        String s = String.valueOf(id);
        switch (s.length()) {
            case 1:
                s = "000" + s;
                break;
            case 2:
                s = "00" + s;
                break;
            case 3:
                s = "0" + s;
                break;
        }
        return "ds" + s;
    }

    /**
     * delete finsh mod
     *
     * @param id mod.id
     * @return delete is success
     */
    public static boolean removeFinishMod(int id) {
        if (isEmpty(id)) {
            return false;
        }
        File modFile = new File(PathUtils.modPath + ModUtils.getModDirName(id));
        return FileUtils.deleteDir(modFile);
    }

    /**
     * delete finsh mod
     * @param mod mod object
     * @return delete is success
     */
    public static boolean removeFinishMod(Mod mod){
        return removeFinishMod(mod.getId());
    }

    /**
     * get in app/mods mods
     * @return FinishMods
     */
    public static List<Mod> getFinishMods(){
        List<Mod> modList = new ArrayList<>();
        for (Mod mod:mods){
            if (mod!=null&&mod.isExist())
                modList.add(mod);
        }
        return modList;
    }

    /**
     * get in mod info one paramenter
     *
     * @param stringList file paramenter
     * @param parameter  paramenter
     * @return
     */
    public static String getModParameter(List<String> stringList, String parameter) {
        String value = "";
        for (String s : stringList) {
            value = s.replaceAll("\\s*", "");
            if (value.startsWith(parameter)) {
                value = value.substring(value.indexOf(parameter + "=") + 1);
                if (!StringUtil.isNumber(value) && value.length() > 2) {
                    int start = value.indexOf("\"") + 1;
                    int end = value.indexOf("\"", start);
                    if (end == -1) {
                        end = value.length() - 1;
                    }
                    value = value.substring(start, end);
                }
                return value;
            }
        }
        return value;
    }
    /**
     * init Finish mod
     */
    public static void initFileModList() throws Exception {
        File modsFile = new File(PathUtils.modPath);
        String[] modFiles = modsFile.list();
        for (String modName : modFiles) {
            if (modName.startsWith("ds")) {
                int id = Integer.valueOf(modName.substring(3));
                Mod mod = createMod(readModInfoList(new File(PathUtils.modPath + modName)),modName,true);
                mods[mod.getId()] = mod;
            }
        }
    }

    /**
     * create mod object
     * @param stringList modinfoContext
     * @param zipName modFileName|modZipName
     * @param exits mod is finish
     * @return mod Object
     * @throws Exception arrays is full
     */
    public static Mod createMod(final List<String> stringList,String zipName,boolean exits) throws Exception {
        int id = createId(zipName);
        String name = getModParameter(stringList, "name");
        String author = getModParameter(stringList, "author");
        String description = getModParameter(stringList, "description");
        return new Mod(id,exits,name,author,description);
    }

    /**
     * read infoContext
     *
     * @param zipFile modZip
     * @return content list
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readModInfoList(ZipFile zipFile) throws ZipException, IOException {
        List<String> stringList = new ArrayList<>();
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        for (FileHeader fileHeader : fileHeaders) {
            if (fileHeader.getFileName().endsWith(SettingUtils.MOD_MODINFO_NAME)) {
                stringList.addAll(Zip4jUtils.readZipFileContent(zipFile, fileHeader));
            }
        }
        return stringList;
    }

    /**
     * read infoContext
     *
     * @param file modFile
     * @return content list
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readModInfoList(File file) throws ZipException, IOException {
        List<String> stringList = new ArrayList<>();
        if (!file.isDirectory())
            throw new ZipException("no Directory");
        File[] files = file.listFiles();
        if (files == null) {
            return stringList;
        }
        for (File file1 : files) {
            if (file1.getName().endsWith(SettingUtils.MOD_MODINFO_NAME)) {
                stringList.addAll(MyFileUtils.readFileList(file1));
            }
        }
        return stringList;
    }

    /**
     * read infoContext
     *
     * @param s mod file/Zip string
     * @return content list
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readModInfoList(String s) throws ZipException, IOException {
        File file = new File(s);
        if (file.isDirectory()){
            return readModInfoList(file);
        }else {
            return readModInfoList(new ZipFile(file));
        }
    }
}
