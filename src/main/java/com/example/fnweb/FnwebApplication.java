package com.example.fnweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.example.fnweb.mapper")
//@ComponentScan(value="com.example.fnweb.mapper")
//@ComponentScan(basePackages = "com.example.fnweb")
public class FnwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnwebApplication.class, args);
	}
}
