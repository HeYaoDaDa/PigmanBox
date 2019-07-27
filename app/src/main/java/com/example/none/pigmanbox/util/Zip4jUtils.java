package com.example.none.pigmanbox.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * zip4j lib Utils
 */
public interface Zip4jUtils {
    /**
     * read in zip file
     *
     * @param zipFile    zip
     * @param fileHeader fileHeader
     * @return file
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readZipFileContent(ZipFile zipFile, FileHeader fileHeader) throws ZipException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(fileHeader)));
        List<String> stringList = new ArrayList<>();
        String linr;
        while ((linr = reader.readLine()) != null) {
            stringList.add(linr);
        }
        return stringList;
    }
    /**
     * read in zip file
     *
     * @param filePath    zip path
     * @param fileHeader fileHeader
     * @return file
     * @throws ZipException no zip
     * @throws IOException io
     */
    public static List<String> readZipFileContent(String filePath, FileHeader fileHeader) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(filePath);
        return readZipFileContent(zipFile,fileHeader);
    }
}
