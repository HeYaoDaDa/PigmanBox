package com.example.none.pigmanbox.util.MyZipUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    public final static int STARTE = 10000;
    public final static int FULFILL = 10001;
    public final static int ERORR = 10002;
    public final static int PROGRESS = 10003;
    public final static String PROGRESS_STRING = "PROGRESS_STRING";
    // 4MB buffer
    private static final byte[] BUFFER = new byte[4096 * 1024];
    private final File mZipFile;
    private final List<String> mPlanDeleteStringList;
    private final List<File> mPlanAddFileList;
    private final String mPath;
    private final Handler mHandler;

    public ModifyZip(File mZipFile, List<String> mPlanDeleteStringLIst, List<File> mPlanAddFileList, String mPath,Handler handler) {
        this.mZipFile = mZipFile;
        this.mPlanDeleteStringList = mPlanDeleteStringLIst;
        this.mPlanAddFileList = mPlanAddFileList;
        this.mPath = mPath;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        try {
            mHandler.sendEmptyMessage(STARTE);
            modifyZip(mZipFile,mPlanDeleteStringList,mPlanAddFileList,mPath,mHandler);
            mHandler.sendEmptyMessage(FULFILL);
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(ERORR);
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
    private void modifyZip(File zipFile, List<String> planDeleteStringList, List<File> planAddFileList, String path, Handler handler) throws Exception {
        Bundle bundle = new Bundle();
        Message msg = new Message();
        File backFile = new File(zipFile.getPath()+".bak");
        zipFile.renameTo(backFile);
        ZipFile backFileZip = new ZipFile(backFile);
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(zipFile));

        List<String> deleteZipEntry = new ArrayList<>();
        getDeleteZipEntry(deleteZipEntry, backFileZip, planDeleteStringList);
        Log.d("hydd||", "modifyZip: "+deleteZipEntry);
        // first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = backFileZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            //if e is plan delete entry
            if (deleteZipEntry.contains(e.getName())) {
                continue;
            }

            append.putNextEntry(e);
            if (!e.isDirectory()) {
                copy(backFileZip.getInputStream(e), append);
            }
            Message message = new Message();
            bundle.putString(PROGRESS_STRING,"写入："+e.getName());
            message.what = PROGRESS;
            message.setData(bundle);
            handler.sendMessage(message);
            append.closeEntry();
        }
        // now append some extra content
        addZipFile(append,planAddFileList,path,handler);
        // close
        append.close();
    }

    /**
     * get delete Zip Entry
     * @param zipEntryList delete Zip Entry
     * @param zipFile old zip
     * @param stringList delete zip file
     */
    private void getDeleteZipEntry(List<String> zipEntryList, ZipFile zipFile, List<String> stringList) {
        for (String s : stringList) {
            Log.d("hydd", "String s: "+s);
            getDeleteZipEntry(zipEntryList, zipFile, new ZipEntry(s));
        }
    }

    /**
     * get delete Zip Entry
     * @param zipEntryList delete Zip Entry
     * @param zipFile old zip
     * @param zipEntry delete zip entry
     */
    private void getDeleteZipEntry(List<String> zipEntryList, ZipFile zipFile, ZipEntry zipEntry) {
        if (!zipEntry.isDirectory()) {
            Log.d("hydd", zipEntry.getName()+" is File");
            zipEntryList.add(zipEntry.getName());
            return;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry nowZipEntry = entries.nextElement();
            Log.d("hydd", nowZipEntry.getName());
            if (nowZipEntry.getName().startsWith(zipEntry.getName())) {
                zipEntryList.add(nowZipEntry.getName());
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
    private void addZipFile(ZipOutputStream zipOutputStream,List<File> fileList,String path,Handler handler) throws IOException {
        for (File file:fileList){
            addZipFile(zipOutputStream,file,path,handler);
        }
    }
    /**
     * zip added fileEntry
     * @param zipOutputStream zip out stream
     * @param file plan add file
     * @param path zip path
     * @throws IOException io
     */
    private void addZipFile(ZipOutputStream zipOutputStream,File file,String path,Handler handler) throws IOException {
        Bundle bundle = new Bundle();
        Message msg = new Message();
        if (file.isDirectory()){
            for (File file1:file.listFiles()){
                addZipFile(zipOutputStream,file1,path+file.getName()+File.separator,handler);
            }
        }else {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            ZipEntry zipEntry = new ZipEntry(path+file.getName());
            zipOutputStream.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }

            bundle.putString(PROGRESS_STRING,"写入："+zipEntry.getName());
            msg.setData(bundle);
            handler.sendMessage(msg);

            zipOutputStream.closeEntry();
        }
    }
}

