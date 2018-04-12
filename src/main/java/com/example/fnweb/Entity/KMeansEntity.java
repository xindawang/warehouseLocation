package com.example.fnweb.Entity;

import java.util.HashMap;

/**
 * Created by ACER on 2018/4/12.
 */
public class KMeansEntity {

    private HashMap<String, Double> apEntities;
    private int coreNumer;

    public HashMap<String, Double> getApEntities() {
        return apEntities;
    }

    public void setApEntities(HashMap<String, Double> apEntities) {
        this.apEntities = apEntities;
    }

    public int getCoreNumer() {
        return coreNumer;
    }

    public void setCoreNumer(int coreNumer) {
        this.coreNumer = coreNumer;
    }
}
