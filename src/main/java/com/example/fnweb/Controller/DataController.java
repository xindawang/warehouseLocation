package com.example.fnweb.Controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileReader;
import java.util.*;

/**
 * Created by ACER on 2017/11/3.
 */
@Controller
public class DataController {

    String resultEntity = null;
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

    @RequestMapping(value = "/data",method = RequestMethod.GET)
    public String getDataSet(){
        return "iotMap";
    }


    @ResponseBody
    @RequestMapping(value = "/data/device",method = RequestMethod.GET)
    public String getDevicesInfo(int pageSize, int page) {
        PageHelper.startPage(page, pageSize);
        List<DeviceEntity> list = deviceMapper.getAllDevicesInfo();
        long total = ((Page<DeviceEntity>) list).getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", total);
        return JsonTool.objectToJson(map);
    }

    @ResponseBody
    @RequestMapping(value = "/data/store",method = RequestMethod.GET)
    public String storeData(){
        //step 1: receive data and store in server, return data path

        //step 2: import data from txt into database
        dataStoreService.insertData();
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/getLoc",method = RequestMethod.GET)
    public String getLoc(){

        return resultEntity;
    }

    @ResponseBody
    @RequestMapping(value = "/getAllRecord",method = RequestMethod.GET)
    public HashMap<Integer,String> getAllRecord(){
        allRecord.clear();
        count =0 ;
        allRecord.put(count++,"40,120");
        allRecord.put(count++,"200,120");
        allRecord.put(count++,"600,120");
        allRecord.put(count++,"1000,120");

        return allRecord;
    }

    @ResponseBody
    @RequestMapping(value = "/loc",method = RequestMethod.POST)
    public String getUserLoc(ApEntity apEntity) {
        RpEntity rpEntity = new RpEntity();
        HashMap<String,Float> apentities = new HashMap<>();
        if (apEntity.getAp1()!=null) apentities.put("ap1",Float.valueOf(apEntity.getAp1()));
        if (apEntity.getAp2()!=null) apentities.put("ap2",Float.valueOf(apEntity.getAp2()));
        if (apEntity.getAp3()!=null) apentities.put("ap3",Float.valueOf(apEntity.getAp3()));
        if (apEntity.getAp4()!=null) apentities.put("ap4",Float.valueOf(apEntity.getAp4()));
        if (apEntity.getAp5()!=null) apentities.put("ap5",Float.valueOf(apEntity.getAp5()));
        rpEntity.setApEntities(apentities);
        if (apEntity.getAlgorithm()!=null) {
            if (apEntity.getAlgorithm().equals("knn")) {
                knnService.getLocByKnn(rpEntity);
            }else if (apEntity.getAlgorithm()=="bayes") {
                naiveBayesService.getLocByBayes(rpEntity);
            }
        }else{
            return null;
        }
        resultEntity = rpEntity.getLocString();
        allRecord.put(count++,resultEntity);
        return rpEntity.getLocString();
    }
}
