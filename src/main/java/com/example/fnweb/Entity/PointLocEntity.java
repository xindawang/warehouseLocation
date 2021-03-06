package com.example.fnweb.Entity;

/**
 * Created by ACER on 2017/11/21.
 */
public class PointLocEntity {
    private String point_name;
    private Integer x;
    private Integer y;

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

    private Integer leftpx;
    private Integer toppx;
    private Double bayesResult;

    public String getPoint_name() {
        return point_name;
    }

    public void setPoint_name(String point_name) {
        this.point_name = point_name;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Double getBayesResult() {
        return bayesResult;
    }

    public void setBayesResult(Double bayesResults) {
        this.bayesResult = bayesResults;
    }
}
