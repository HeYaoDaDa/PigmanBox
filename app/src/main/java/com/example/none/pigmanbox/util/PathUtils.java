package com.example.none.pigmanbox.util;

import android.content.Context;
import android.os.Environment;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.example.none.pigmanbox.application.MyApplication;

import java.io.File;

/**
 * path
 */
public interface PathUtils {
    public static final String fileRootPath = com.blankj.utilcode.util.PathUtils.getExternalStoragePath() + File.separator + AppUtils.getAppPackageName() + File.separator;
    public static final String tempPath = fileRootPath + "temp/";
    public static final String zipPath = fileRootPath + "zip/";
    public static final String modPath = fileRootPath + "mod/";
    public static final String jsonPath = fileRootPath + "json/";

    /**
     * @param context
     */
    public static void init(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (fileRootPath != null) {
                File file = new File(modPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                file = new File(zipPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                file = new File(tempPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                file = new File(jsonPath);
                if (!file.exists()) {
                    file.mkdir();
                }
            }
        }
        clearTemp();
    }

    /**
     * clear Temp Dir
     */
    public static void clearTemp() {
        FileUtils.deleteAllInDir(tempPath);
    }
}
