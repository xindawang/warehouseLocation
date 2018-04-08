package com.example.fnweb;

import com.example.fnweb.Entity.RpEntity;
import com.example.fnweb.Service.CNNPrepareService;
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

    @Autowired
    private CNNPrepareService cnnPrepareService;

	@Test
	public void getArgsFromData(){
		String filename = "C:\\Users\\ACER\\Desktop\\捷众项目\\第三批\\relative.txt";
		System.out.println(naiveBayesService.getArgsFromData("tablet_zhang_args",filename));
	}

	@Test
	public void getArgsFromDir(){
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\pointDirHonor8";
		System.out.println(naiveBayesService.getArgsFromDir("horizontal_honor",filename));
	}

    @Test
    public void printArgsFromDir(){
        String dirName = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\pointDir";
        System.out.println(cnnPrepareService.getArgsFromDir(dirName));
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
		apentities.put("ap6",Double.valueOf(-43));
		apentities.put("ap7",Double.valueOf(-47));
		rpEntity.setApEntities(apentities);
		knnService.getLocByKnnAbsolute(rpEntity);
//		knnService.getLocByKnnRelative(rpEntity);
//		naiveBayesService.getLocByBayesAbsolute(rpEntity);
//		naiveBayesService.getLocByBayesRelative(rpEntity);
		System.out.println(rpEntity.getLocString());
	}


	@Test
	public void getPrecision(){
		String filename = "C:\\Users\\ACER\\Desktop\\捷众项目\\第三批\\relative.txt";
		//		knnService.getPrecision(filename,74,19);
		naiveBayesService.getPrecision("tablet_zhang_args",filename,50,19);
	}

	@Test
	public void getTestPrecision(){
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\allRecord.txt";
		//		knnService.getPrecision(filename,74,19);
		naiveBayesService.getRandPrecision("horizontal_args",filename,1000);
	}

	@Test
	public void changeToRelativeValue(){
		String filename = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\Tablet.txt";
		pointStoreService.changeToMinusRelativeValue(filename);
	}

	@Test
	public void dirChangeToRelativeValue(){
		String dirName = "E:\\IndoorLocation\\warehouseLocation\\src\\main\\resources\\static\\data\\projectSrc\\pointDir";
		pointStoreService.dirChangeToRelativeValue(dirName);
	}


}
