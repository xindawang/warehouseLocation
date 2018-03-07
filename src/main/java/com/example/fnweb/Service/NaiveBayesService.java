package com.example.fnweb.Service;

import com.example.fnweb.Entity.BayesArgsEntity;
import com.example.fnweb.Entity.PointLocEntity;
import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Mapper.BayesMapper;
import com.example.fnweb.Mapper.PointLocMapper;
import com.example.fnweb.Mapper.RssiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    private int apAmount = 5;

    private int k = 3;

    HashMap<String, String> changeName;

    //get all the rssi of each ap in each point and calculate the average and variance
    @Transactional
    public boolean getArgsFromData(){

        changeName = new HashMap<>();

        changeName.put("Four-Faith-2", "ap1");
        changeName.put("Four-Faith-3", "ap2");
        changeName.put("TP-LINK_E7D2", "ap3");
        changeName.put("TP-LINK_3625", "ap4");
        changeName.put("TP-LINK_3051", "ap5");

        List<String> allPoints = pointLocMapper.getAllPointName();
        String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\Tablet.txt";
        int rpCurCount = 1;
        for (String str : allPoints) {
            for (int i = 1; i <= apAmount; i++) {
                String apName = "ap"+i;
//                List<Double> eachApData = rssiMapper.getEachApRssiByPointName(apName,str);
                List<Double> eachApData = getApRssiOfRpFromTxt(filename,apName,rpCurCount,19);
                if (!computeAndInsertGaussArgs(eachApData,str,apName)) return false;
                System.out.println(eachApData);
            }
            rpCurCount++;
        }
        return true;
    }

    private boolean computeAndInsertGaussArgs(List<Double> eachApData,String pointName, String apName) {
        double sum = 0.0, variance = 0.0;
        for (Double eachResult : eachApData) sum += eachResult;
        double average = sum/eachApData.size();

        for (Double eachResult : eachApData) variance += Math.pow((eachResult - average),2);
        variance = variance/eachApData.size();

        String avgName = apName+"_average";
        String varName = apName+"_variance";

        //insert a point if store the args for the first time
        //update the point info if the point has existed
        if (bayesMapper.hasPoint(pointName)){
            if(!bayesMapper.updateBayesArgs(avgName,varName,pointName,average,variance)) return false;
        }else{
            if (!bayesMapper.insertBayesArgs(avgName,varName,pointName,average,variance)) return false;
        }
        return true;
    }

    public void getLocByBayes(RpEntity rpEntity){

        //initialize the input data
        HashMap<String, Double> rpInfoSrc = rpEntity.getApEntities();
        List<String> allPointNames = bayesMapper.getAllPointName();

        //get ratio and location info of each point and add up for the result
        PointLocEntity[] maxKEntitiesH = getKPointsWithHighestProb(rpInfoSrc,allPointNames,1,50);
        PointLocEntity[] maxKEntitiesRv = getKPointsWithHighestProb(rpInfoSrc,allPointNames,51,62);
        PointLocEntity[] maxKEntitiesLv = getKPointsWithHighestProb(rpInfoSrc,allPointNames,63,74);

        List<PointLocEntity[]> pointLocEntities = new ArrayList<>();
        pointLocEntities.add(maxKEntitiesH);
        pointLocEntities.add(maxKEntitiesRv);
        pointLocEntities.add(maxKEntitiesLv);
        PointLocEntity[] maxKEntities = getMaxGroup(pointLocEntities,rpEntity);

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

    private PointLocEntity[] getMaxGroup(List<PointLocEntity[]> pointLocEntities,RpEntity rpEntity) {
        PointLocEntity[] maxKEntities = pointLocEntities.get(0);
        List<String> firstPointNames = new ArrayList<>();
        for (PointLocEntity f:maxKEntities){
            firstPointNames.add(f.getPoint_name());
        }
        for (PointLocEntity[] locEntities:pointLocEntities) {
            List<String> pointNames = new ArrayList<>();
            for (PointLocEntity p:locEntities){
                pointNames.add(p.getPoint_name());
            }
            if (getKPointsWithHighestProb(rpEntity.getApEntities(),pointNames,1,3)> getKPointsWithHighestProb(rpEntity.getApEntities(),firstPointNames,1,3))
                maxKEntities = locEntities;
        }
        return maxKEntities;
    }

    private PointLocEntity[] getKPointsWithHighestProb(HashMap<String, Double> rpInfoSrc,
                                                       List<String> allPointNames,int startCount, int endCount){

        //use the way of priority queue, which could be compared to the knn
        Comparator<PointLocEntity> cmp = new Comparator<PointLocEntity>() {
            @Override
            public int compare(PointLocEntity o1, PointLocEntity o2) {
                return o2.getBayesResult()>o1.getBayesResult()?1:-1;
            }
        };

        Queue<PointLocEntity> maxKPoints = new PriorityQueue<>(cmp);
        PointLocEntity[] pointLocEntities = new PointLocEntity[k];

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
                    BayesArgsEntity eachAp = bayesMapper.getEachApArgs(avgName, varName, pointName);
                    if (eachAp.getApNameVar() == 0)
                        eachAp.setApNameVar(0.000001);
                    double thisApProb = 1 / Math.sqrt(2 * Math.PI * eachAp.getApNameVar()) * Math.pow(E, -Math.pow(rpInfoSrc.get(apName) - eachAp.getApNameAvg(), 2) / (2 * eachAp.getApNameVar()));
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
                        if (changeName.get(eachAp[0]).equals(apName))
                            eachApData.add(Double.valueOf(eachAp[1]));
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

}
