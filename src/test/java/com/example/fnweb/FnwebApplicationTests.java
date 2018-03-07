package com.example.fnweb;

import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Service.KNNService;
import com.example.fnweb.Service.NaiveBayesService;
import com.example.fnweb.Service.PointStoreService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FnwebApplicationTests {

	@Autowired
	private KNNService knnService;

	@Autowired
	private PointStoreService pointStoreService;

	@Autowired
	private NaiveBayesService naiveBayesService;

	@Test
	public void getArgsFromData(){
		System.out.println(naiveBayesService.getArgsFromData());
	}

	@Test
	public void getRssiInfo(){
		List<RpEntity> rpList = knnService.getRssiEntityFromTxt("E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\1.txt");
		System.out.println(rpList);
	}

	@Test
	public void insertPointLoc2(){
		pointStoreService.insertPointLoc();
	}

	@Test
	public void insertTestLoc(){
		pointStoreService.insertTestLoc();
	}

	@Test
	public void getTestLoc(){
		RpEntity rpEntity = new RpEntity();
		HashMap<String,Double> apentities = new HashMap<>();
		apentities.put("ap1",Double.valueOf(-50));
		apentities.put("ap2",Double.valueOf(-44));
		apentities.put("ap3",Double.valueOf(-44));
		apentities.put("ap4",Double.valueOf(-43));
		apentities.put("ap5",Double.valueOf(-47));
		rpEntity.setApEntities(apentities);
//		knnService.getLocByKnn(rpEntity);
		naiveBayesService.getLocByBayes(rpEntity);
		System.out.println(rpEntity.getLocString());
	}


	@Test
	public void getPrecision(){
		knnService.getPrecision();
	}

}
