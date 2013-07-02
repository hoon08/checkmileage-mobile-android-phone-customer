package kr.co.bettersoft.checkmileage.common;
/**
 * CommonUtils
 * 이미지 도메인 및 URL 주소 저장용 클래스이다
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity.backgroundUpdateLogToServer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class CommonConstant {

	public String TAG = "CommonUtils";
	
	public static String alertTitle = "Carrot";

	//	static String writeQRstr = "test1234";
	//	static int callCode = 0;		// 호출 모드 . 읽기:1, 쓰기:2, 초기화:3
	//	static int qrResult = 0;		// 처리 결과값. 성공:1, 실패:: 파일없음:-3,입출력오류:-2,그외:-1

	/**
	 * Google API project id registered to use GCM.
	 */
	//    static final String SENDER_ID = "568602772620";				// yes. blue.
	public static final String SENDER_ID = "944691534021";				// yes. server / gcm register 할때 사용
	/**
	 * Intent used to display a message in the screen.
	 */
	public static final String DISPLAY_MESSAGE_ACTION =
		"co.kr.bettersoft.checkmileage_mobile_android_phone_customer.DISPLAY_MESSAGE";
	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	public static final String EXTRA_MESSAGE = "message";
	public static void displayMessage(Context context, String message) {
	Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
	intent.putExtra(EXTRA_MESSAGE, message);
	context.sendBroadcast(intent);
	}
	
	public static int serverConnectTimeOut = 10000;

	public static String imgDomain = "http://www.mcarrot.net/upload/profile/"; 	
	public static String imgthumbDomain = "http://www.mcarrot.net/upload/thumb/"; 	
	public static String imgPushDomain = "http://www.mcarrot.net/upload/pushThumb/"; 	

	public static String termsPolicyURL = "http://www.mcarrot.net/mTerms.do";	// 이용 약관, +시작시 동의 받는 페이지
	public static String privacyPolicyURL = "http://mcarrot.net/mPrivacy.do";	// 개인정보 보호 정책, 시작시 동의 받는 페이지

	public static String serverNames = "http://checkmileage.mcarrot.net";		//real *** 
//	public static String serverNames = "http://checkmileage.onemobileservice.com";	// test  *** 


	public static String packageNames = "kr.co.bettersoft.checkmileage.activities";
	//	public static String qrFileSavedPath = "/sdcard/CarrotKeyFile.txt";
	public static String qrFileSavedPath = "/sdcard/Android/data/kr.co.bettersoft.carrot/";
	public static String qrFileSavedPathFile = "/sdcard/Android/data/kr.co.bettersoft.carrot/CarrotKeyFile.dat";

	// 암호화키
//	static String key = "Created_by_JohnK";						// 128 bit  16글자.
	public static String key = "Created_by_JohnKim_in_Bettersoft";		// 256 bit  32글자. --> java 에러 뱉어서 서버에 별도 jar 설치해야함 
	
	
	//	public static String prefPath = "kr.co.bettersoft.checkmileage.pref";

	//	public static int usingNetwork = 0;
	//	public static int threadWaitngTime = 500;
	
}
