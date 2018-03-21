package com.example.fnweb.tools;

import com.example.fnweb.Entity.RpEntity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ACER on 2018/3/8.
 */
public class RssiTool {

    public static void changeAbsEntityToMinusRel(RpEntity rpEntity){
        HashMap<String,Double> apEntities = rpEntity.getApEntities();
        double minAbsRssi = -200;
        for (Double v : apEntities.values()){
            if (v > minAbsRssi){
                minAbsRssi = v;
            }
        }
        for (String s : apEntities.keySet()){
            apEntities.put(s,apEntities.get(s)-minAbsRssi);
        }
        rpEntity.setApEntities(apEntities);
    }

    public static void changeAbsEntityToRel(RpEntity rpEntity){
        HashMap<String,Double> apEntities = rpEntity.getApEntities();
        double minAbsRssi = -200;
        for (Double v : apEntities.values()){
            if (v > minAbsRssi){
                minAbsRssi = v;
            }
        }
        for (String s : apEntities.keySet()){
            apEntities.put(s,apEntities.get(s)/minAbsRssi);
        }
        rpEntity.setApEntities(apEntities);
    }

    public static String getNewName(String oldName){
        HashMap<String, String> changeName = new HashMap<>();
        changeName.put("Four-Faith-2", "ap1");
        changeName.put("Four-Faith-3", "ap2");
        changeName.put("TP-LINK_E7D2", "ap3");
        changeName.put("TP-LINK_3625", "ap4");
        changeName.put("TP-LINK_3051", "ap5");
        changeName.put("TP-LINK_35EB", "ap6");
        changeName.put("TP-LINK_5958", "ap7");
        return changeName.get(oldName);
    }

    public static HashMap<String, String> getNameChangeMap(){
        HashMap<String, String> changeName = new HashMap<>();
        changeName.put("Four-Faith-2", "ap1");
        changeName.put("Four-Faith-3", "ap2");
        changeName.put("TP-LINK_E7D2", "ap3");
        changeName.put("TP-LINK_3625", "ap4");
        changeName.put("TP-LINK_3051", "ap5");
        changeName.put("TP-LINK_35EB", "ap6");
        changeName.put("TP-LINK_5958", "ap7");
        return changeName;
    }

    public static List<RpEntity> getRssiEntityFromTxt(String filename, int repeatTimes){

        List<RpEntity> rpEntities = new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            String str = br.readLine();
            int count = 0;
            while (str != null) {
                RpEntity rpEntity = new RpEntity();
                HashMap<String, Double> apEntities = new HashMap<>();
                String[] eachRpSet = str.split(";");
                for (int i=0;i< eachRpSet.length;i++) {
                    String[] eachAp = eachRpSet[i].split(" ");
                    apEntities.put(RssiTool.getNewName(eachAp[0]),Double.valueOf(eachAp[1]));
                }
                int index = count/repeatTimes+1;
                if (index < 51) rpEntity.setPoint("h"+index);
                else if (index < 63) rpEntity.setPoint("rv"+(index-50));
                else rpEntity.setPoint("lv"+(index-62));
                rpEntity.setApEntities(apEntities);
                rpEntities.add(rpEntity);
                str = br.readLine();
                count++;
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return rpEntities;
    }

    public List<RpEntity> getRssiEntityFromTxt(String data1,String data2){
        List<RpEntity> rpList1 = getRssiEntityFromTxt(data1,1);
        List<RpEntity> rpList2 = getRssiEntityFromTxt(data2,1);
        List<RpEntity> rpList = new ArrayList<>();

        for (int i = 0; i < 69; i++) {
            RpEntity rpEntity = new RpEntity();
            HashMap<String, Double> apEntities = new HashMap<>();
            for(String apname:RssiTool.getNameChangeMap().values()){
                int count = 0;
                double result = 0;
                if (rpList1.get(i).getApEntities().containsKey(apname)){
                    count++;
                    result += rpList1.get(i).getApEntities().get(apname);
                }
                if (rpList2.get(i).getApEntities().containsKey(apname)){
                    count++;
                    result += rpList2.get(i).getApEntities().get(apname);
                }
                if(count > 0) apEntities.put(apname,result/count);
            }
            rpEntity.setApEntities(apEntities);
            rpEntity.setPoint(rpList1.get(i).getPoint());
            rpList.add(rpEntity);
        }
        return rpList;
    }
}
