package com.example.none.pigmanbox.util.MyZipUtils;

import android.util.Log;
import android.widget.Toast;

import com.example.none.pigmanbox.application.MyApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * delete or add zip entry
 */
public class ModifyZip implements Runnable {
    // 4MB buffer
    private static final byte[] BUFFER = new byte[4096 * 1024];
    private final File mZipFile;
    private final List<String> mPlanDeleteStringList;
    private final List<File> mPlanAddFileList;
    private final String mPath;

    public ModifyZip(File mZipFile, List<String> mPlanDeleteStringLIst, List<File> mPlanAddFileList, String mPath) {
        this.mZipFile = mZipFile;
        this.mPlanDeleteStringList = mPlanDeleteStringLIst;
        this.mPlanAddFileList = mPlanAddFileList;
        this.mPath = mPath;
    }

    @Override
    public void run() {
        try {
            modifyZip(mZipFile,mPlanDeleteStringList,mPlanAddFileList,mPath);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MyApplication.getContext(),"修改数据包失败。",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * copy input to output stream - available in several StreamUtils or Streams classes
     */
    private static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }

    /**
     * modify zip File entry
     * @param zipFile zip file
     * @param planDeleteStringList plan delete
     * @param planAddFileList plan add
     * @param path add path
     * @throws Exception erorr
     */
    private void modifyZip(File zipFile,List<String> planDeleteStringList,List<File> planAddFileList,String path) throws Exception {
        File backFile = new File(zipFile.getPath()+".bak");
        zipFile.renameTo(backFile);
        ZipFile backFileZip = new ZipFile(backFile);
        Log.d("hydd", "backFile: "+backFile.getPath()+"\n||zipFile:"+zipFile.getPath());
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(zipFile));

        List<ZipEntry> deleteZipEntry = new ArrayList<>();
        getDeleteZipEntry(deleteZipEntry, backFileZip, planDeleteStringList);
        // first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = backFileZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            //if e is plan delete entry
            if (deleteZipEntry.contains(e)) {
                continue;
            }

            append.putNextEntry(e);
            if (!e.isDirectory()) {
                copy(backFileZip.getInputStream(e), append);
            }
            append.closeEntry();
        }
        // now append some extra content
        addZipFile(append,planAddFileList,path);
        // close
        append.close();
    }

    /**
     * get delete Zip Entry
     * @param zipEntryList delete Zip Entry
     * @param zipFile old zip
     * @param stringList delete zip file
     */
    private void getDeleteZipEntry(List<ZipEntry> zipEntryList, ZipFile zipFile, List<String> stringList) {
        for (String s : stringList) {
            getDeleteZipEntry(zipEntryList, zipFile, new ZipEntry(s));
        }
    }

    /**
     * get delete Zip Entry
     * @param zipEntryList delete Zip Entry
     * @param zipFile old zip
     * @param zipEntry delete zip entry
     */
    private void getDeleteZipEntry(List<ZipEntry> zipEntryList, ZipFile zipFile, ZipEntry zipEntry) {
        if (!zipEntry.isDirectory()) {
            zipEntryList.add(zipEntry);
            return;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry nowZipEntry = entries.nextElement();
            if (nowZipEntry.getName().startsWith(zipEntry.getName())) {
                zipEntryList.add(nowZipEntry);
            }
        }
    }

    /**
     * zip added fileEntry
     * @param zipOutputStream zip out stream
     * @param fileList plan add file list
     * @param path zip path
     * @throws IOException io
     */
    private void addZipFile(ZipOutputStream zipOutputStream,List<File> fileList,String path) throws IOException {
        for (File file:fileList){
            addZipFile(zipOutputStream,file,path);
        }
    }
    /**
     * zip added fileEntry
     * @param zipOutputStream zip out stream
     * @param file plan add file
     * @param path zip path
     * @throws IOException io
     */
    private void addZipFile(ZipOutputStream zipOutputStream,File file,String path) throws IOException {
        if (file.isDirectory()){
            for (File file1:file.listFiles()){
                addZipFile(zipOutputStream,file1,path+file.getName()+File.separator);
            }
        }else {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            zipOutputStream.putNextEntry(new ZipEntry(path+file.getName()));

            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }

            zipOutputStream.closeEntry();
        }
    }
}

