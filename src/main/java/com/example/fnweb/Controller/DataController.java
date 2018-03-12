package com.example.fnweb.Controller;

import com.example.fnweb.Entity.AlgorithmEnum;
import com.example.fnweb.Entity.ApEntity;
import com.example.fnweb.Entity.DeviceEntity;
import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Mapper.DeviceMapper;
import com.example.fnweb.Mapper.PointLocMapper;
import com.example.fnweb.Service.DataStoreService;
import com.example.fnweb.Service.KNNService;
import com.example.fnweb.Service.NaiveBayesService;
import com.example.fnweb.tools.JsonTool;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.util.*;

/**
 * Created by ACER on 2017/11/3.
 */
@RestController
public class DataController {

    String resultEntityString = null;
    String lastEntityString =null;
    LinkedHashMap<Integer,String> allRecord = new LinkedHashMap<>();
    int count = 0;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private PointLocMapper pointLocMapper;

    @Autowired
    private DataStoreService dataStoreService;

    @Autowired
    private KNNService knnService;

    @Autowired
    private NaiveBayesService naiveBayesService;

    @Autowired
    private SimpMessagingTemplate template;


    @MessageMapping("/app_wifiMessage")
    public void websocket(String message) {
        JSONObject json = JSONObject.fromObject(message);
        RpEntity rpEntity = new RpEntity();
        HashMap<String,Double> apentities = new HashMap<>();
        if (json.keySet().contains("ap1")) apentities.put("ap1",Double.valueOf(json.getString("ap1")));
        if (json.keySet().contains("ap2")) apentities.put("ap2",Double.valueOf(json.getString("ap2")));
        if (json.keySet().contains("ap3")) apentities.put("ap3",Double.valueOf(json.getString("ap3")));
        if (json.keySet().contains("ap4")) apentities.put("ap4",Double.valueOf(json.getString("ap4")));
        if (json.keySet().contains("ap5")) apentities.put("ap5",Double.valueOf(json.getString("ap5")));
        rpEntity.setApEntities(apentities);
        if (json.keySet().contains("algorithm")) {
            switch (json.getString("algorithm")) {
                case "1":
                    knnService.getLocByKnnRelative(rpEntity);
                    break;
                case "2":
                    naiveBayesService.getLocByBayesAbsolute(rpEntity);
                    break;
                case "3":
                    naiveBayesService.getLocByBayesRelative(rpEntity);
                    break;
                default:
                    knnService.getLocByKnnAbsolute(rpEntity);
                    break;
            }
        }else{
            knnService.getLocByKnnAbsolute(rpEntity);
        }

        if (lastEntityString == null){
            lastEntityString =  rpEntity.getLocString();
        }
        resultEntityString = lastEntityString + "," + rpEntity.getLocString();
        lastEntityString = rpEntity.getLocString();

        allRecord.put(count++,resultEntityString);
        template.convertAndSend("/warehouse/loc" , rpEntity.getLocString());
        template.convertAndSend("/warehouse/path" , resultEntityString);
    }

    @RequestMapping(value = "/data",method = RequestMethod.GET)
    public String getDataSet(){
        return "iotMap";
    }


//    @ResponseBody
//    @RequestMapping(value = "/data/device",method = RequestMethod.GET)
//    public String getDevicesInfo(int pageSize, int page) {
//        PageHelper.startPage(page, pageSize);
//        List<DeviceEntity> list = deviceMapper.getAllDevicesInfo();
//        long total = ((Page<DeviceEntity>) list).getTotal();
//        Map<String, Object> map = new HashMap<>();
//        map.put("list", list);
//        map.put("total", total);
//        return JsonTool.objectToJson(map);
//    }

//    @ResponseBody
//    @RequestMapping(value = "/data/store",method = RequestMethod.GET)
//    public String storeData(){
//        //step 1: receive data and store in server, return data path
//
//        //step 2: import data from txt into database
//        dataStoreService.insertData();
//        return null;
//    }

    @ResponseBody
    @RequestMapping(value = "/getLoc",method = RequestMethod.GET)
    public String getLoc(){
//        resultEntity = "200,120";
        return resultEntityString;
    }

    @ResponseBody
    @RequestMapping(value = "/getAllRecord",method = RequestMethod.GET)
    public HashMap<Integer,String> getAllRecord(){
//        allRecord.clear();
//        count =0 ;
//        allRecord.put(count++,"40,120");
//        allRecord.put(count++,"400,300");
//        allRecord.put(count++,"1000,120");
//        allRecord.put(count++,"800,120");
//        allRecord.put(count++,"200,120");

        return allRecord;
    }

    @ResponseBody
    @RequestMapping(value = "/clearAllRecord",method = RequestMethod.GET)
    public String clearAllRecord(){
        allRecord.clear();
        resultEntityString = null;
        lastEntityString =null;
        return "清空完毕！";
    }

    @ResponseBody
    @RequestMapping(value = "/loc",method = RequestMethod.POST)
    public String getUserLoc(ApEntity apEntity) {
            RpEntity rpEntity = new RpEntity();
        HashMap<String,Double> apentities = new HashMap<>();
        if (apEntity.getAp1()!=null) apentities.put("ap1",Double.valueOf(apEntity.getAp1()));
        if (apEntity.getAp2()!=null) apentities.put("ap2",Double.valueOf(apEntity.getAp2()));
        if (apEntity.getAp3()!=null) apentities.put("ap3",Double.valueOf(apEntity.getAp3()));
        if (apEntity.getAp4()!=null) apentities.put("ap4",Double.valueOf(apEntity.getAp4()));
        if (apEntity.getAp5()!=null) apentities.put("ap5",Double.valueOf(apEntity.getAp5()));
        rpEntity.setApEntities(apentities);
        if (apEntity.getAlgorithm()!=null) {
            if (apEntity.getAlgorithm().equals("knn")) {
                knnService.getLocByKnnAbsolute(rpEntity);
            }else if (apEntity.getAlgorithm()=="bayes") {
                naiveBayesService.getLocByBayesAbsolute(rpEntity);
            }
        }else{
            return null;
        }
        resultEntityString = rpEntity.getLocString();
        allRecord.put(count++,resultEntityString);
        return rpEntity.getLocString();
    }
}
