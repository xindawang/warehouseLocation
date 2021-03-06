package com.example.fnweb.Mapper;

import com.example.fnweb.Entity.PointLocEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ACER on 2017/11/30.
 */

@Mapper
public interface PointLocMapper {
    @Select("select * from point_loc where point_name=#{pointName}")
    PointLocEntity getPointLocInfoByName(String pointName);

    @Select("select * from point_loc2 where point_name=#{pointName}")
    PointLocEntity getPointLoc2InfoByName(String pointName);

    @Select("select * from point_warehouse where point_name=#{pointName}")
    PointLocEntity getTestLocInfoByName(String pointName);

    @Select("select * from point_loc where point_name=#{point_name}")
    PointLocEntity getPointLocInfoByEntity(PointLocEntity pointLocEntity);

    @Select("select distinct point_name from point_warehouse")
    List<String> getAllPointName();

    @Select("select distinct point_name from point_warehouse where point_name LIKE 'h%'")
    List<String> getHorizontalPointName();

    @Insert("insert into point_loc (point_name,x,y) values (#{point_name},#{x},#{y})")
    Boolean insertPointLoc(PointLocEntity pointLocEntity);

    @Insert("insert into point_loc2 (point_name,x,y) values (#{point_name},#{x},#{y})")
    Boolean insertPointLoc2(PointLocEntity pointLocEntity);

    @Insert("insert into point_warehouse (point_name,leftpx,toppx) values (#{point_name},#{leftpx},#{toppx})")
    Boolean insertTestLoc(PointLocEntity pointLocEntity);
}
