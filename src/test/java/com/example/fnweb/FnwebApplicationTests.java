package com.example.fnweb;

import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Service.KNNService;
import com.example.fnweb.Service.NaiveBayesService;
import com.example.fnweb.Service.PointStoreService;
import com.example.fnweb.tools.RssiTool;
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
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\TabletMinusRelative.txt";
		System.out.println(naiveBayesService.getArgsFromData("tablet_minus_args",filename));
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
    public void insertScreenshotLoc(){
        pointStoreService.insertScreenshotLoc();
    }

	@Test
	public void getTestLocByString(){

		HashMap<String,Double> apentities = new HashMap<>();
		RpEntity rpEntity = new RpEntity();

		String rssiString =
                "TP-LINK_3625 -45;TP-LINK_3051 -43;Four-Faith-2 -45;Four-Faith-3 -44;TP-LINK_E7D2 -44;";
		String[] eachRpSet = rssiString.split(";");
		for (int i=0;i< eachRpSet.length;i++) {
			String[] eachAp = eachRpSet[i].split(" ");
			apentities.put(RssiTool.getNewName(eachAp[0]),Double.valueOf(eachAp[1]));
		}

		rpEntity.setApEntities(apentities);
//		knnService.getLocByKnnAbsolute(rpEntity);
//		knnService.getLocByKnnRelative(rpEntity);
//		naiveBayesService.getLocByBayesAbsolute(rpEntity);
//		naiveBayesService.getLocByBayesRelative(rpEntity);
		System.out.println(rpEntity.getLocString());
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
//		knnService.getLocByKnnAbsolute(rpEntity);
//		knnService.getLocByKnnRelative(rpEntity);
//		naiveBayesService.getLocByBayesAbsolute(rpEntity);
		naiveBayesService.getLocByBayesRelative(rpEntity);
		System.out.println(rpEntity.getLocString());
	}


	@Test
	public void getPrecision(){
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\Tablet.txt";
//		knnService.getPrecision(filename,74,19);
		naiveBayesService.getPrecision("tablet_args",filename,74,19);
	}

	@Test
	public void changeToRelativeValue(){
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\Tablet.txt";
		pointStoreService.changeToMinusRelativeValue(filename);
	}


}
