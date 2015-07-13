
package com.jovision.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private String SDPATH;
    /**
	 */
    private int FILESIZE = 4 * 1024;
    private final static boolean LOOP_WRITE_FILE = true;
    private onDownloadListener downLoadListener = null;

    public String getSDPATH() {
        return SDPATH;
    }

    public interface onDownloadListener {
        public void onRealTimeDownloadSize(int RTSize);

        public void onDownLoadFinished();
    }

    public void setOnDownloadListener(onDownloadListener listenter) {
        downLoadListener = listenter;
    }

    public FileUtils() {

        SDPATH = Environment.getExternalStorageDirectory() + "/";
        System.out.println("SDPATH: " + SDPATH);
    }

    public File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    public File createSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    public File write2SDFromInput(String path, String fileName,
            InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte[] buffer = new byte[FILESIZE];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len); // 在这里使用另一个重载，防止流写入的问题.
                if (null != downLoadListener) {
                    downLoadListener.onRealTimeDownloadSize(len);
                }
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("111", "file size is:" + file.length());
        return file;
    }

}
