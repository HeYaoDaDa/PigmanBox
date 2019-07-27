package com.example.none.pigmanbox.util;

import android.util.Log;

import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File Utils
 */
public interface MyFileUtils {
    /**
     * read file content
     * @param file file
     * @return content list
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readFileList(File file) throws ZipException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> stringList = new ArrayList<>();
        String linr;
        while ((linr = reader.readLine()) != null) {
            stringList.add(linr);
        }
        return stringList;
    }
    /**
     * read file content
     * @param fileString fileString
     * @return content list
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readFileList(String fileString) throws ZipException, IOException {
        File file = new File(fileString);
        return readFileList(file);
    }

    /**
     * backup
     * @param file plan backup file
     * @return backup file.bak
     */
    public static File backupFile(File file){
        if (!SettingUtils.backup){
            return file;
        }
        File bak = new File(file.getPath()+".bak");
        if (bak.exists()){
            bak.delete();
        }
        file.renameTo(bak);
        return bak;
    }

    /**
     * backup file
     * @param fileSting plan backup fileString
     * @return file.bak
     */
    public static File backupFile(String fileSting){
        File file = new File(fileSting);
        return backupFile(file);
    }
}
