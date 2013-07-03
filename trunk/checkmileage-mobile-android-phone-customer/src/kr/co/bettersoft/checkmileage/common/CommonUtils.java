package kr.co.bettersoft.checkmileage.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {

	// 현 시각
	static Date today ;
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static String nowDate;
	static String nowTime = "";	// 현시각
	
	// 현시각
	public static String getNowDate(){
		today = new Date();
		nowDate = sf.format(today);
		return nowDate;
	}
	
}
