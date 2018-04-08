package com.example.fnweb.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ACER on 2018/3/22.
 */
public class FileTool {
    public static List<String> traverseFolder(String path) {
        File dir = new File(path);
        List<String> fileList = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files.length == 0) {
                System.out.println("文件夹是空的!");
            } else {
                for (File file : files) {
                    fileList.add(file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        return fileList;
    }
}
