package com.example.fnweb.Entity;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ACER on 2017/11/17.
 */
public class RpEntity {
    private Integer id;
    private HashMap<String, Double> apEntities;
    private String point_name;
    private Double knnResult;
    private Integer leftpx;
    private Integer toppx;
    private int kmeansGroupNum;

    public int getKmeansGroupNum() {
        return kmeansGroupNum;
    }

    public void setKmeansGroupNum(int kmeansGroupNum) {
        this.kmeansGroupNum = kmeansGroupNum;
    }

    public Integer getLeftpx() {
        return leftpx;
    }

    public void setLeftpx(Integer leftpx) {
        this.leftpx = leftpx;
    }

    public Integer getToppx() {
        return toppx;
    }

    public void setToppx(Integer toppx) {
        this.toppx = toppx;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPoint() {
        return point_name;
    }

    public void setPoint(String point) {
        this.point_name = point;
    }

    public Double getKnnResult() {
        return knnResult;
    }

    public void setKnnResult(Double knnResult) {
        this.knnResult = knnResult;
    }

    public HashMap<String, Double> getApEntities() {
        return apEntities;
    }

    public void setApEntities(HashMap<String, Double> apEntities) {
        this.apEntities = apEntities;
    }

    public String getLocString(){
        return this.getLeftpx()+","+this.getToppx();
    }
}
