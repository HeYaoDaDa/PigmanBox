package com.example.none.pigmanbox.util.MyZipUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * UnZip utils
 */
public class UnZip implements Runnable {
    private final String sourcePath;
    private final String targetPath;

    public UnZip(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    @Override
    public void run() {
        unZip(sourcePath,targetPath);
    }

    public static void unZip(String sourcePath,String targetPath){
        File targetFile = new File(targetPath);
        // 如果目录不存在，则创建
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        try(ZipFile zipFile = new ZipFile(new File(sourcePath))) {
            Enumeration enumeration = zipFile.entries();
            while(enumeration.hasMoreElements()){
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                String name = entry.getName();
                if(entry.isDirectory()){
                    continue;
                }
                try(BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry))){
                    // 需要判断文件所在的目录是否存在，处理压缩包里面有文件夹的情况
                    String outName = targetPath + "/" + name;
                    File outFile = new File(outName);
                    File tempFile = new File(outName.substring(0,outName.lastIndexOf("/")));
                    if (!tempFile.exists()){
                        tempFile.mkdirs();
                    }
                    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))){
                        int len;
                        byte[] buffer = new byte[1024];
                        while((len = inputStream.read(buffer)) > 0){
                            outputStream.write(buffer,0,len);
                        }
                    }

                }

            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
