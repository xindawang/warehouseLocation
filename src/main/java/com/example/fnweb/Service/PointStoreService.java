package com.example.fnweb.Service;

import com.example.fnweb.Entity.PointLocEntity;
import com.example.fnweb.Mapper.DeviceMapper;
import com.example.fnweb.Mapper.PointLocMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by ACER on 2017/11/22.
 */
@Service
public class PointStoreService {

    @Autowired
    private DeviceMapper dataMapper;

    @Autowired
    private PointLocMapper pointLocMapper;

    private String path = "E:\\IndoorLocation\\FengNiao\\FMWeb\\src\\main\\resources\\static\\data\\point_loc2.txt";

    public boolean insertPointLoc(){
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            String str=br.readLine();
            while(str != null) {
                PointLocEntity pointLocEntity = new PointLocEntity();
                String [] eachPoint = str.split("\\s");

                //store the point info which meet the requirement to the database after transformation
                if (eachPoint.length==3) {
                    pointLocEntity.setPoint_name(eachPoint[0]);
                    DecimalFormat dcmFmt = new DecimalFormat("0.0000000");
                    double pointX = Double.valueOf(dcmFmt.format(Double.valueOf(eachPoint[1])-12735839))*Math.pow(10,7);
                    double pointY = Double.valueOf(dcmFmt.format(Double.valueOf(eachPoint[2])-3569534))*Math.pow(10,7);
                    pointLocEntity.setX((int)pointX);
                    pointLocEntity.setY((int)pointY);
                    if (!pointLocMapper.insertPointLoc2(pointLocEntity)) return false;
                }
                str=br.readLine();
            }
            br.close();
            reader.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void insertTestLoc(){
        PointLocEntity pointLocEntity;
        for (int i = 1; i < 75; i++) {
            pointLocEntity = new PointLocEntity();
            if (i<51) {
                pointLocEntity.setPoint_name("h" + i);
                pointLocEntity.setLeftpx((int)Math.round(30 + (i - 1) * 22.2));
                pointLocEntity.setToppx(120);
            }else if (i<63){
                pointLocEntity.setPoint_name("rv" + (i-50));
                pointLocEntity.setLeftpx(745);
                pointLocEntity.setToppx(160+(i-51)*20);
            }else{
                pointLocEntity.setPoint_name("lv" + (i-62));
                pointLocEntity.setLeftpx(400);
                pointLocEntity.setToppx(160+(i-63)*20);
            }
            System.out.println(pointLocEntity.getPoint_name()+" "
                    + pointLocEntity.getLeftpx()+" "+ pointLocEntity.getToppx());
            pointLocMapper.insertTestLoc(pointLocEntity);
        }
    }
}
