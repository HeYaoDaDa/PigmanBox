package com.example.none.pigmanbox.util.MyZipUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * create Zip File
 */
public class CreateZip implements Runnable {
    private final ZipOutputStream mZipOutputStream;
    private final File mFile;
    private final String mString;

    public CreateZip(ZipOutputStream mZipOutputStream, File mFile, String mString) {
        this.mZipOutputStream = mZipOutputStream;
        this.mFile = mFile;
        this.mString = mString;
    }

    @Override
    public void run() {
        zipOperation(mZipOutputStream,mFile,mString);
    }
    private static void zipOperation(ZipOutputStream zipOutputStream,File file,String path){
        // 如果是目录，则递归进行处理
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File tempFile : files) {
                zipOperation(zipOutputStream, tempFile,path + "/" + tempFile.getName());
            }
        }
        else{
            // 如果是单个文件，再进行压缩
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                ZipEntry entry = new ZipEntry(path);
                zipOutputStream.putNextEntry(entry);

                int len;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                zipOutputStream.closeEntry();
            }catch (Exception e){
                e.printStackTrace();
            }
            }
        }
    }
