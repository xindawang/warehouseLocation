package com.example.fnweb.Mapper;

import com.example.fnweb.Entity.BayesArgsEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ACER on 2017/11/30.
 */
@Mapper
public interface BayesMapper {
    @Select("select count(id) from ${tableName} where point_name = #{pointName}")
    Boolean hasPoint(@Param("tableName") String tableName,@Param("pointName")String pointName);

    @Select("select distinct point_name from ${tableName}")
    List<String> getAllPointName(@Param("tableName") String tableName);

    @Select("select ${apNameAvg} as apNameAvg,${apNameVar} as apNameVar from ${tableName} where point_name = #{pointName}")
    BayesArgsEntity getEachApArgs(@Param("tableName") String tableName,@Param("apNameAvg") String apNameAvg, @Param("apNameVar") String apNameVar,
                                        @Param("pointName") String pointName);

    @Select("select ${apNameAvg} as apNameAvg from ${tableName} where point_name = #{pointName}")
    BayesArgsEntity getEachApAvg(@Param("tableName") String tableName,@Param("apNameAvg") String apNameAvg,@Param("pointName") String pointName);

    @Insert("insert into ${tableName} (point_name,${apNameAvg},${apNameVar}) " +
            "values (#{pointName},#{apAvg},#{apVar})")
    Boolean insertBayesArgs(@Param("tableName") String tableName,@Param("apNameAvg") String apNameAvg, @Param("apNameVar") String apNameVar,
                            @Param("pointName") String pointName, @Param("apAvg") double apAvg, @Param("apVar") double apVar);

    @Update("update ${tableName} set ${apNameAvg}=#{apAvg},${apNameVar}=#{apVar}" +
            "where point_name = #{pointName}")
    Boolean updateBayesArgs(@Param("tableName") String tableName,@Param("apNameAvg") String apNameAvg,@Param("apNameVar") String apNameVar,
                            @Param("pointName") String pointName,@Param("apAvg") double apAvg,@Param("apVar") double apVar);
}
