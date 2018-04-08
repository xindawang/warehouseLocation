package com.example.fnweb.Service;

import com.example.fnweb.tools.RssiTool;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ACER on 2018/3/19.
 */
@Service
public class CNNPrepareService {
    public boolean getArgsFromDir(String dirName) {
        List<String> fileList = traverseFolder(dirName);
        for (int i = 0; i < 50; i++){
            getApRssiOfRpFromTxt(fileList.get((i+31)%50),i,true);
        }
        return true;
    }

    private void getApRssiOfRpFromTxt(String filename, int i ,boolean combine) {
        HashMap<String, Integer> eachApData = new HashMap<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();
            String str = br.readLine();
            while (str != null) {
                System.out.println(str);
                br.readLine();
                br.readLine();
                str = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getApRssiOfRpFromTxt(String filename, int i) {
        HashMap<String, Integer> eachApData = new HashMap<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();
            String str = br.readLine();
            while (str != null) {
                String[] eachRpSet = str.split(";");
                for (int j = 0; j < eachRpSet.length; j++) {
                    String[] eachAp = eachRpSet[j].split(" ");
                    eachApData.put(eachAp[0],Integer.valueOf(eachAp[1]));
                }
                System.out.print(" "+ eachApData.get("Four-Faith-2"));
                System.out.print(" "+ eachApData.get("Four-Faith-3"));
                System.out.print(" "+ eachApData.get("TP-LINK_E7D2"));
                System.out.print(" "+ eachApData.get("TP-LINK_3625"));
                System.out.print(" "+ eachApData.get("TP-LINK_3051"));
                System.out.print(" "+ eachApData.get("TP-LINK_35EB"));
                System.out.print(" "+ eachApData.get("TP-LINK_5958"));
                System.out.println(" "+ i);
                br.readLine();
                br.readLine();
                str = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<String> traverseFolder(String path) {
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
