package com.example.fnweb.Service;

import com.example.fnweb.Entity.BayesArgsEntity;
import com.example.fnweb.Entity.PointLocEntity;
import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Mapper.BayesMapper;
import com.example.fnweb.Mapper.PointLocMapper;
import com.example.fnweb.Mapper.RssiMapper;
import com.example.fnweb.tools.RssiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

import static java.lang.Math.E;

/**
 * Created by ACER on 2017/11/22.
 */
@Service
public class NaiveBayesService {

    @Autowired
    private BayesMapper bayesMapper;

    @Autowired
    private PointLocMapper pointLocMapper;

    @Autowired
    private RssiMapper rssiMapper;

    private int apAmount = 7;

    private int k = 3;

    //get all the rssi of each ap in each point and calculate the average and variance
    @Transactional
    public boolean getArgsFromData(String tableName,String filename){

        List<String> allPoints = pointLocMapper.getAllPointName();
        int rpCurCount = 1;
        for (String str : allPoints) {
            for (int i = 1; i <= apAmount; i++) {
                String apName = "ap"+i;
//                List<Double> eachApData = rssiMapper.getEachApRssiByPointName(apName,str);
                List<Double> eachApData = getApRssiOfRpFromTxt(filename,apName,rpCurCount,19);
                if (!computeAndInsertGaussArgs(tableName,eachApData,str,apName)) return false;
                System.out.println(eachApData);
            }
            rpCurCount++;
        }
        return true;
    }

    private boolean computeAndInsertGaussArgs(String tableName, List<Double> eachApData, String pointName, String apName) {
        double sum = 0.0, variance = 0.0;
        for (Double eachResult : eachApData) sum += eachResult;
        double average = sum/eachApData.size();

        for (Double eachResult : eachApData) variance += Math.pow((eachResult - average),2);
        variance = variance/eachApData.size();

        String avgName = apName+"_average";
        String varName = apName+"_variance";

        //insert a point if store the args for the first time
        //update the point info if the point has existed
        if (bayesMapper.hasPoint(tableName,pointName)){
            if(!bayesMapper.updateBayesArgs(tableName,avgName,varName,pointName,average,variance)) return false;
        }else{
            if (!bayesMapper.insertBayesArgs(tableName,avgName,varName,pointName,average,variance)) return false;
        }
        System.out.println(pointName +" "+apName+" " +average+" " +variance);
        return true;
    }

    //use absolute value in bayes method
    public void getLocByBayesAbsolute(RpEntity rpEntity){
        String tableName = "tablet_new_args";
        getLocByBayes(rpEntity, tableName);
    }

    //use relative value in bayes method
    public void getLocByBayesRelative(RpEntity rpEntity){
        String tableName = "tablet_relative_args";
        RssiTool.changeAbsEntityToRel(rpEntity);
        getLocByBayes(rpEntity, tableName);
    }

    public void getLocByBayesMinusRelative(RpEntity rpEntity){
        String tableName = "tablet_minus_args";
        RssiTool.changeAbsEntityToMinusRel(rpEntity);
        getLocByBayes(rpEntity, tableName);
    }

    public void getLocByBayesPart(RpEntity rpEntity){
        String tableName = "tablet_part";
        getLocByBayes(rpEntity, tableName);
    }

    public void getLocByBayesPartNew(RpEntity rpEntity){
        String tableName = "tablet_part_new";
        getLocByBayes(rpEntity, tableName);
    }


    public void getLocByBayes(RpEntity rpEntity, String tableName){

        //initialize the input data
        HashMap<String, Double> rpInfoSrc = rpEntity.getApEntities();

        //get ratio and location info of each point and add up for the result
        PointLocEntity[] maxKEntities = getKPointsWithHighestProb(tableName,rpInfoSrc,1,50);
//        PointLocEntity[] maxKEntitiesRv = getKPointsWithHighestProb(tableName,rpInfoSrc,51,62);
//        PointLocEntity[] maxKEntitiesLv = getKPointsWithHighestProb(tableName,rpInfoSrc,63,74);

//        List<PointLocEntity[]> pointLocEntities = new ArrayList<>();
//        pointLocEntities.add(maxKEntitiesH);
//        pointLocEntities.add(maxKEntitiesRv);
//        pointLocEntities.add(maxKEntitiesLv);
//        PointLocEntity[] maxKEntities = getMaxGroup(pointLocEntities);

        double sum = 0,x=0,y=0;
        for (int i = 0; i < maxKEntities.length; i++){
            sum += maxKEntities[i].getBayesResult();
        }
        for (int i = 0; i < maxKEntities.length; i++){
            double ratio = maxKEntities[i].getBayesResult()/sum;
            PointLocEntity locEntity = pointLocMapper.getTestLocInfoByName(maxKEntities[i].getPoint_name());
            x += ratio * locEntity.getLeftpx();
            y += ratio * locEntity.getToppx();
        }

        //convert the format of location info according to how it store into database in knnService
        rpEntity.setLeftpx((int)x);
        rpEntity.setToppx((int)y);
    }

    private PointLocEntity[] getMaxGroup(List<PointLocEntity[]> pointLocEntitiesList) {
        PointLocEntity[] maxKEntities = pointLocEntitiesList.get(0);
        double lastProb = 0;
        for (PointLocEntity pointLocEntity : maxKEntities) lastProb += pointLocEntity.getBayesResult();

        for (PointLocEntity[] pointLocEntities:pointLocEntitiesList) {
            double thisProb = 0;
            for (PointLocEntity pointLocEntity : pointLocEntities) thisProb += pointLocEntity.getBayesResult();
            if (thisProb> lastProb){
                maxKEntities = pointLocEntities;
                lastProb = thisProb;
            }
        }
        return maxKEntities;
    }

    private PointLocEntity[] getKPointsWithHighestProb(String tableName, HashMap<String, Double> rpInfoSrc,
                                                       int startCount, int endCount){

        //use the way of priority queue, which could be compared to the knn
        Comparator<PointLocEntity> cmp = new Comparator<PointLocEntity>() {
            @Override
            public int compare(PointLocEntity o1, PointLocEntity o2) {
                return o2.getBayesResult()>o1.getBayesResult()?1:-1;
            }
        };

        Queue<PointLocEntity> maxKPoints = new PriorityQueue<>(cmp);
        PointLocEntity[] pointLocEntities = new PointLocEntity[k];
        List<String> allPointNames = bayesMapper.getAllPointName(tableName);

        int count =1;
        //calculate the probability of each candidate point, pick the max k
        for (String pointName : allPointNames) {
            if (count>=startCount&&count<=endCount) {
                PointLocEntity candidateLocEntity = new PointLocEntity();
                double eachPointProb = 1.0;

                //put the online data into Gauss formula with Gauss index of each ap
                for (int i = 1; i <= apAmount; i++) {
                    String apName = "ap" + i;
                    String avgName = "ap" + i + "_average";
                    String varName = "ap" + i + "_variance";
                    BayesArgsEntity eachAp = bayesMapper.getEachApArgs(tableName,avgName, varName, pointName);
                    if (eachAp.getApNameVar() == 0)
                        eachAp.setApNameVar(0.000001);

                    //handle the situation when apX is not found
                    double thisApProb = 1;
                    if (rpInfoSrc.get(apName)!=null)
                    thisApProb = 1 / Math.sqrt(2 * Math.PI * eachAp.getApNameVar()) * Math.pow(E, -Math.pow(rpInfoSrc.get(apName) - eachAp.getApNameAvg(), 2) / (2 * eachAp.getApNameVar()));
                    eachPointProb *= thisApProb;
                }
                candidateLocEntity.setPoint_name(pointName);
                candidateLocEntity.setBayesResult(eachPointProb);
                maxKPoints.offer(candidateLocEntity);
            }
            count++;
        }
        for (int i = 0; i < k; i++) pointLocEntities[i] = maxKPoints.poll();
        return pointLocEntities;
    }


    public List<Double> getApRssiOfRpFromTxt(String filename,String apName, int rpCurCount,int repeatTimes){

        List<Double> eachApData= new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            String str = br.readLine();
            int count = 0;
            while (str != null) {

                int rpTextCount = count/repeatTimes;
                if (rpTextCount == rpCurCount -1) {

                    String[] eachRpSet = str.split(";");
                    for (int i = 0; i < eachRpSet.length; i++) {
                        String[] eachAp = eachRpSet[i].split(" ");
                        if (RssiTool.getNewName(eachAp[0]).equals(apName)){
                            eachApData.add(Double.valueOf(eachAp[1]));
                            break;
                        }

                    }
                }
                str = br.readLine();
                count++;
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return eachApData;
    }

    //计算误差
    public void getPrecision(String tableName,String filename,int pointCount,int repeatTimes){
        List<RpEntity> rpList = RssiTool.getRssiEntityFromTxt(filename,repeatTimes);
        float horizontalDeviation = 0;
        float verticalDeviation = 0;
        PointLocEntity pointLocEntity;
        int numberCount =1;
        double eachNumberHorizontalDeviation = 0;
        double eachNumberVerticalDeviation = 0;
        for (int i = 0; i < pointCount*repeatTimes; i++) {
            getLocByBayes(rpList.get(i),"horizontal_args");
            System.out.println(rpList.get(i).getLocString());
            pointLocEntity = pointLocMapper.getTestLocInfoByName(rpList.get(i).getPoint());
            horizontalDeviation += Math.abs(rpList.get(i).getLeftpx()-pointLocEntity.getLeftpx());
            verticalDeviation += Math.abs(rpList.get(i).getToppx()-pointLocEntity.getToppx());

            eachNumberHorizontalDeviation += Math.abs(rpList.get(i).getLeftpx()-pointLocEntity.getLeftpx());
            eachNumberVerticalDeviation += Math.abs(rpList.get(i).getToppx()-pointLocEntity.getToppx());

            if ((i+1)%19==0){
                System.err.print((i+1)/19+" ");
                System.err.print(Math.abs(eachNumberHorizontalDeviation/repeatTimes)/24.8+" ");
                System.err.println(Math.abs(eachNumberVerticalDeviation/repeatTimes)/30);
                eachNumberHorizontalDeviation = 0;
                eachNumberVerticalDeviation = 0;
            }
        }
        System.err.println(Math.abs(horizontalDeviation/pointCount/repeatTimes/24.8));
        System.err.println(Math.abs(verticalDeviation/pointCount/repeatTimes/30));
    }

    public boolean getArgsFromDir(String tableName, String filename) {
        List<String> allPoints = pointLocMapper.getHorizontalPointName();
        List<String> fileList = traverseFolder(filename);
        int rpCurCount = 1;
        for (String str : allPoints) {
            for (int i = 1; i <= apAmount; i++) {
                String apName = "ap"+i;
//                List<Double> eachApData = rssiMapper.getEachApRssiByPointName(apName,str);
                List<Double> eachApData = getApRssiOfRpFromTxt(fileList.get((rpCurCount+30)%50),apName);
                if (!computeAndInsertGaussArgs(tableName,eachApData,str,apName)) return false;
//                System.out.println(eachApData);
            }
            rpCurCount++;
        }
        return true;
    }

    private List<Double> getApRssiOfRpFromTxt(String filename, String apName) {
        List<Double> eachApData= new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();
            String str = br.readLine();
            while (str != null) {
                String[] eachRpSet = str.split(";");
                for (int i = 0; i < eachRpSet.length; i++) {
                    String[] eachAp = eachRpSet[i].split(" ");
                    if (RssiTool.getNewName(eachAp[0]).equals(apName)){
                        eachApData.add(Double.valueOf(eachAp[1]));
                        break;
                    }

                }
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
        return eachApData;
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
