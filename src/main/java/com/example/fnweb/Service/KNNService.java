package com.example.fnweb.Service;

import com.example.fnweb.Entity.DeviceEntity;
import com.example.fnweb.Entity.PointLocEntity;
import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Mapper.DeviceMapper;
import com.example.fnweb.Mapper.PointLocMapper;
import com.example.fnweb.Mapper.RssiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ACER on 2017/11/18.
 */
@Service
public class KNNService {

    @Autowired
    private DeviceMapper dataMapper;

    @Autowired
    private RssiMapper rpMapper;

    @Autowired
    private PointLocMapper pointLocMapper;

    private int k = 3;

    boolean useDatabase = false;

    private String fileName = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\1.txt";

    public void getLocByKnn(RpEntity rpEntity){

        //appoint the number of minimum AP point
        List<RpEntity> rpList = getRssiEntityFromTxt(fileName);

        List<RpEntity> rpListOfH = rpList.subList(0,46);
        List<RpEntity> rpListOfRv = rpList.subList(47,57);
        List<RpEntity> rpListOfLv = rpList.subList(58,68);

        RpEntity[] rpEntitiesH = getMinK(rpEntity,rpListOfH);
        RpEntity[] rpEntitiesRv = getMinK(rpEntity,rpListOfRv);
        RpEntity[] rpEntitiesLv = getMinK(rpEntity,rpListOfLv);

        RpEntity[] rpEntities;

        List<RpEntity[]> rpListEntities = new ArrayList<>();
        rpListEntities.add(rpEntitiesRv);
        rpListEntities.add(rpEntitiesLv);

        rpEntities = rpEntitiesH;
        for (RpEntity[] r:rpListEntities) {
            if (getDif(r,rpEntity)< getDif(rpEntities,rpEntity))
            rpEntities = r;
        }

        PointLocEntity[] pointLocEntities = getRelPointInfo(rpEntities);
        double sum = 0, x=0, y=0;

        //calculate the sum of k rp
        for (int i = 0; i <rpEntities.length ; i++) {
            if (rpEntities[i].getKnnResult()!= 0)
            sum += 1/rpEntities[i].getKnnResult();
        }

        //get ratio and location info of each point and add up for the result
        for (int i = 0; i <rpEntities.length ; i++) {
            if (rpEntities[i].getKnnResult()!= 0) {
                double ratio = 1 / rpEntities[i].getKnnResult() / sum;
                int point_x = pointLocEntities[i].getLeftpx();
                int point_y = pointLocEntities[i].getToppx();
                x += ratio * point_x;
                y += ratio * point_y;
            }else {
                x = pointLocEntities[i].getLeftpx();
                y = pointLocEntities[i].getToppx();
                break;
            }
        }

        //convert the format of location info according to how it store into database
        rpEntity.setLeftpx((int)Math.round(x));
        rpEntity.setToppx((int)Math.round(y));
    }

    private double getDif(RpEntity[] rpEntities, RpEntity rpEntity) {
        double result = 0;
        int commonCount = 0;
        for (int i = 0; i < k; i++) {
            for (String apName: rpEntity.getApEntities().keySet()) {
                if (rpEntities[i].getApEntities().containsKey(apName)) {
                    result += Math.sqrt(Math.abs(rpEntities[i].getApEntities().get(apName) - rpEntity.getApEntities().get(apName)));
                    commonCount++;
                }
            }
        }
        if (commonCount>0) result /= commonCount;
        return result;
    }

    //get all the point location information from database
    private PointLocEntity[] getRelPointInfo(RpEntity[] rpEntities) {
        PointLocEntity[] pointLocEntity = new PointLocEntity[k];
        int i = 0 ;
        for (RpEntity rpEntity :rpEntities) {
            pointLocEntity[i++]=pointLocMapper.getTestLocInfoByName(rpEntity.getPoint());
        }
        return pointLocEntity;
    }

    public RpEntity[] getMinK(RpEntity rpEntity, List<RpEntity> rpEntityList){
        //get rp info from database according to the given device_id

        //initialize big top heap
        int curNum = 0;
        RpEntity[] minK = new RpEntity[k];

        for (RpEntity singleRp : rpEntityList){
            double result = 0;
            int commonCount = 0;
            for (String apName: rpEntity.getApEntities().keySet()) {
                if (singleRp.getApEntities().containsKey(apName)) {
                    result += Math.sqrt(Math.abs(singleRp.getApEntities().get(apName) - rpEntity.getApEntities().get(apName)));
                    commonCount++;
                }
            }
            if (commonCount>0) result /= commonCount;
            singleRp.setKnnResult(result);
            if(curNum < k) minK[curNum++] = singleRp;
            else if (singleRp.getKnnResult() < minK[0].getKnnResult()){
                minK[0] = singleRp;

                //sort the array by big top heap(using method of array)
                minK =maxHeapify(minK, 0, k-1);
            }
//            System.out.println(result);
        }
        return minK;
    }

    //similar to heap rank
    private RpEntity[] maxHeapify(RpEntity[] rpEntities, int index, int len){
        int li = (index << 1) + 1; // 左子节点索引
        int ri = li + 1;           // 右子节点索引
        int cMax = li;             // 子节点值最大索引，默认左子节点。

        if(li > len) return rpEntities;       // 左子节点索引超出计算范围，直接返回。
        if(ri <= len && rpEntities[ri].getKnnResult() > rpEntities[li].getKnnResult()) // 先判断左右子节点，哪个较大。
            cMax = ri;
        if(rpEntities[cMax].getKnnResult() > rpEntities[index].getKnnResult()){
            RpEntity tmpRpEntity = rpEntities[index];
            rpEntities[index] = rpEntities[cMax];
            rpEntities[cMax] = tmpRpEntity;
            rpEntities = maxHeapify(rpEntities,cMax, len);  // 则需要继续判断换下后的父节点是否符合堆的特性。
        }
        return rpEntities;
    }

    public List<RpEntity> getRssiEntityFromTxt(String filename){

        HashMap<String, String> changeName = new HashMap<>();
        changeName.put("Four-Faith-2", "ap1");
        changeName.put("Four-Faith-3", "ap2");
        changeName.put("TP-LINK_E7D2", "ap3");
        changeName.put("TP-LINK_3625", "ap4");
        changeName.put("TP-LINK_3051", "ap5");

        List<RpEntity> rpEntities = new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            String str = br.readLine();
            int count = 1;
            while (str != null) {
                RpEntity rpEntity = new RpEntity();
                HashMap<String, Float> apEntities = new HashMap<>();
                String[] eachRpSet = str.split(";");
                for (int i=0;i< eachRpSet.length;i++) {
                    String[] eachAp = eachRpSet[i].split(" ");
                    apEntities.put(changeName.get(eachAp[0]),Float.valueOf(eachAp[1]));
                }
                if (count < 48) rpEntity.setPoint("h"+count);
                else if (count < 59) rpEntity.setPoint("rv"+(count-47));
                else rpEntity.setPoint("lv"+(count-58));
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

}
