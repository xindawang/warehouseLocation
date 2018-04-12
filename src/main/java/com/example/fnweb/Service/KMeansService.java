package com.example.fnweb.Service;

import com.example.fnweb.Entity.KMeansEntity;
import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.tools.RssiTool;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by ACER on 2018/4/12.
 */
@Service
public class KMeansService {

    public int apNum = 7;
    public int k = 50;
    public double threshold = 0;

    public List<RpEntity> rpEntities = new ArrayList<>();
    public List<KMeansEntity> kMeansEntities = new ArrayList<>();
    List<KMeansEntity> newKMeansEntities = new ArrayList<>();

    public void init(){
        String fileName = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\allRecord.txt";
        getRssiEntityFromTxt(fileName,100);
        Set<Integer> coreIndexSet = new HashSet<>();
        for (int i = 0; i <k ; i++) {
            int randomCoreIndex = (int) (Math.random()*5000);
            if (!coreIndexSet.contains(randomCoreIndex)){
                KMeansEntity kMeansEntity = new KMeansEntity();
                kMeansEntity.setCoreNumer(i);
                kMeansEntity.setApEntities(rpEntities.get(randomCoreIndex).getApEntities());
                kMeansEntities.add(kMeansEntity);
            }else{
                i--;
            }
        }
    }

    public void moveCore(){
        setGroupIndex();
        calculateAvgOfEachGroups();
        if (!lookLikeCorrect()){
            kMeansEntities = newKMeansEntities;
            newKMeansEntities = new ArrayList<>();
            moveCore();
        }else{
            for (KMeansEntity kMeansEntity : kMeansEntities){
                for (String apName : kMeansEntity.getApEntities().keySet()){
                    System.out.print(apName+" " + kMeansEntity.getApEntities().get(apName)+";");
                }
                System.out.println();
            }
        }
    }

    private boolean lookLikeCorrect() {
        for (int i = 0; i < k; i++) {
            for (String apName : kMeansEntities.get(i).getApEntities().keySet()){
                if (Math.abs(kMeansEntities.get(i).getApEntities().get(apName)-
                        newKMeansEntities.get(i).getApEntities().get(apName))> threshold)
                    return false;
            }
        }
        return true;
    }

    public void calculateAvgOfEachGroups(){
        for (int i = 0; i < k; i++) {
            KMeansEntity kMeansEntity = new KMeansEntity();
            HashMap<String,Double> apMap = new HashMap<>();
            for (int j = 0; j < apNum; j++){
                int entityEachApCount =0;
                double entityEachApValue = 0.0;
                for (RpEntity rpEntity : rpEntities) {
                    if (rpEntity.getKmeansGroupNum() == i) {
                        if (rpEntity.getApEntities().containsKey("ap"+(j+1))){
                            entityEachApCount++;
                            entityEachApValue += rpEntity.getApEntities().get("ap"+(j+1));
                        }
                    }
                }
                apMap.put("ap"+(j+1),entityEachApValue/entityEachApCount);
            }
            kMeansEntity.setApEntities(apMap);
            newKMeansEntities.add(kMeansEntity);
        }
    }

    public void setGroupIndex(){
        for (RpEntity rpEntity : rpEntities){
            int groupCount = 0;
            double minDistance = Double.MAX_VALUE;
            for (int i = 0; i < k; i++) {
                double distance =0.0;
                for (String apName: kMeansEntities.get(i).getApEntities().keySet()){
                    if (rpEntity.getApEntities().containsKey(apName)){
                        distance +=Math.sqrt(Math.abs(kMeansEntities.get(i).getApEntities().get(apName) - rpEntity.getApEntities().get(apName)));
                    }
                }
                if (distance<minDistance){
                    minDistance = distance;
                    groupCount = i;
                }
            }
            rpEntity.setKmeansGroupNum(groupCount);
        }
    }

    public void getRssiEntityFromTxt(String filename, int repeatTimes){
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
                rpEntity.setPoint(String.valueOf(index));
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
    }
}
