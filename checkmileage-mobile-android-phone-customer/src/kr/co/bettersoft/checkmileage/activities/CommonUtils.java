package kr.co.bettersoft.checkmileage.activities;
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

public class CommonUtils extends Activity {

	public String TAG = "CommonUtils";
	
	static String alertTitle = "Carrot";

	//	static String writeQRstr = "test1234";
	//	static int callCode = 0;		// 호출 모드 . 읽기:1, 쓰기:2, 초기화:3
	//	static int qrResult = 0;		// 처리 결과값. 성공:1, 실패:: 파일없음:-3,입출력오류:-2,그외:-1

	public static int serverConnectTimeOut = 10000;

	static String imgDomain = "http://www.mcarrot.net/upload/profile/"; 	
	static String imgthumbDomain = "http://www.mcarrot.net/upload/thumb/"; 	
	static String imgPushDomain = "http://www.mcarrot.net/upload/pushThumb/"; 	

	public static String termsPolicyURL = "http://www.mcarrot.net/mTerms.do";	// 이용 약관, +시작시 동의 받는 페이지
	public static String privacyPolicyURL = "http://mcarrot.net/mPrivacy.do";	// 개인정보 보호 정책, 시작시 동의 받는 페이지

//	public static String serverNames = "checkmileage.mcarrot.net";		//real *** 
	public static String serverNames = "checkmileage.onemobileservice.com";	// test  *** 


	public static String packageNames = "kr.co.bettersoft.checkmileage.activities";
	//	public static String qrFileSavedPath = "/sdcard/CarrotKeyFile.txt";
	public static String qrFileSavedPath = "/sdcard/Android/data/kr.co.bettersoft.carrot/";
	public static String qrFileSavedPathFile = "/sdcard/Android/data/kr.co.bettersoft.carrot/CarrotKeyFile.dat";

	// 암호화키
//	static String key = "Created_by_JohnK";						// 128 bit  16글자.
	static String key = "Created_by_JohnKim_in_Bettersoft";		// 256 bit  32글자. --> java 에러 뱉어서 서버에 별도 jar 설치해야함 
	
	
	//	public static String prefPath = "kr.co.bettersoft.checkmileage.pref";

	//	public static int usingNetwork = 0;
	//	public static int threadWaitngTime = 500;

	
	// 서버 통신 용 
	String controllerName="";
	String methodName="";
	String serverName = CommonUtils.serverNames;
	URL postUrl2 ;
	HttpURLConnection connection2;
	int responseCode= 0;
	// 내 좌표 업뎃용
	int myLat = 0;
	int myLon = 0;
	// 전번
	static String phoneNum = "";
	// 설정 파일 저장소  
	SharedPreferences sharedPrefCustom;
	// 서버에 업댓중 .. 반복 호출 방지
	int isUpdating = 0;
	public String qrCode = "";			// qr 아이디
	/*
	 * 프로퍼티 사용했었는데, 프리퍼런스 방식이 더 좋아서 프로퍼티는 더이상 사용하지 않음
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		if(callCode==1){	// 읽기 모드			// from main page		// *** 파일에 입출력 방식 - > pref 사용하면서 사용안하게 됨.
		//			qrResult = readProps();
		//			MainActivity.qrResult = qrResult;
		//		}else if(callCode==2){ // 쓰기 모드		// from main page (test)
		//			qrResult = writeProps();
		//			MainActivity.qrResult = qrResult;
		//		}else if(callCode==3){	// 초기화 모드		// from main page (test)
		//			writeQRstr="";
		//			qrResult = writeProps();
		//			MainActivity.qrResult = qrResult;
		//		}else if(callCode==22){				// from create qr page
		//			qrResult = writeProps();
		//			CreateQRPageActivity.qrResult = qrResult;
		//		}else if(callCode==32){				// from scan qr page
		//			qrResult = writeProps();
		//			ScanQRPageActivity.qrResult = qrResult;
		//		}
		//		else{		// 그외. 비정상적인 호출.
		//			Log.i("CommonUtils", "callCode:"+callCode);
		//			MainActivity.qrResult = qrResult;
		//		}
		
//		finish();
	}

	//	public int writeProps(){
	//		//파일 쓰기					// 기존 파일을 덮어쓴다. 응용하여 더하기도 가능..
	//		FileOutputStream fos;
	//		String strFileContents = writeQRstr;
	//		try {
	//			fos = openFileOutput("Filename.txt",MODE_PRIVATE);			// 자신만.
	//			fos.write(strFileContents.getBytes());
	//			fos.close();
	//			return 1;
	//		} catch (FileNotFoundException e) {
	//			//			e.printStackTrace();
	//			return -3;
	//		} catch (IOException e) {
	//			//			e.printStackTrace();
	//			return -2;
	//		} catch(Exception e){
	//			return -1;
	//		}
	//	}

	//	public  int readProps(){
	//		//파일 읽기
	//		String strFileName = "Filename.txt";
	//		StringBuffer strBuffer = new StringBuffer();
	//		try {
	//			FileInputStream fis = openFileInput(strFileName.toString());
	//			DataInputStream dataIO = new DataInputStream(fis);
	//			String strLine = null;
	//			String tmpStr = "";
	//			while( (strLine = dataIO.readLine()) != null)          // 파일 내 줄바꿈
	//			{
	//				strBuffer.append(strLine + "\n");
	//				tmpStr = strLine;
	//			}
	//			dataIO.close();
	//			fis.close();
	//			MainActivity.myQR = tmpStr;
	//			MyQRPageActivity.qrCode = tmpStr;
	//			return 1;
	//		} catch (FileNotFoundException e) {
	//			//			e.printStackTrace();
	//			return -3;
	//		} catch (IOException e) {
	//			//			e.printStackTrace();
	//			return -2;
	//		}catch(Exception e){		// 파일이 없는 경우
	//			//			e.printStackTrace();
	//			return -1;
	//		}
	//		//		TextView textView = new TextView(this);
	//		//		textView.setText(strBuffer);
	//		//		setContentView(textView);
	//	}

//	/**
//	 * 서버에 위치 및 로그 남김
//	 * loggingToServer
//	 */
//	public void loggingToServer(Context ctx, String getQRcode){
//		
//		Log.d(TAG,"loggingToServer");
//		try{
//			qrCode = getQRcode;
//			// prefs
//			if(sharedPrefCustom == null){
//				sharedPrefCustom = ctx.getSharedPreferences("MyCustomePref",
//						Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
//			}
//			phoneNum = sharedPrefCustom.getString("phoneNum", "");
//			if(phoneNum==null || phoneNum.length()<1){		// pref 에 전번 없을 경우
//				//자신의 전화번호 가져오기
//				try{		// 읽다가 에러 터질때에 대비하기
//					TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
//					phoneNum = telManager.getLine1Number();
//					SharedPreferences.Editor updatePhoneNum =   sharedPrefCustom.edit();
//					updatePhoneNum.putString("phoneNum", phoneNum);		// 전번 저장
//					updatePhoneNum.commit();
//				}catch(Exception e){}
//			}
//			
//			LocationManager  lm;
//			Location location;
//			String provider;
//			String bestProvider;
//			lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
//			provider = LocationManager.GPS_PROVIDER;
//			Criteria criteria = new Criteria();
//			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
//			criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소비량
//			criteria.setAltitudeRequired(false); // 고도
//			criteria.setBearingRequired(false); // ..
//			criteria.setSpeedRequired(false); // 속도
//			criteria.setCostAllowed(true); // 금전적 비용
//			bestProvider = lm.getBestProvider(criteria, true);
//			location =  lm.getLastKnownLocation(bestProvider);
//			if(location!=null){
//				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
//				myLon = (int) (location.getLongitude()*1000000);	
//				Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
//				new backgroundUpdateLogToServer().execute();	// 비동기로 서버에 위치 업뎃		
//			}else{
//				location =  lm.getLastKnownLocation(provider);
//				if(location==null){
//					Log.d(TAG,"location = null");	
//				}else{
//					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
//					myLon = (int) (location.getLongitude()*1000000);	
//					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);			
//					new backgroundUpdateLogToServer().execute();	// 비동기로 전환	
//				}
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//			Log.w(TAG,"fail to update my location to server");
//		}
//	}
//
//	/**
//	 * 비동기로 사용자의 위치 정보 및 정보 로깅
//	 * backgroundUpdateLogToServer
//	 */
//	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
//		@Override protected void onPostExecute(Void result) {  
//		} 
//		@Override protected void onPreExecute() {  
//		} 
//		@Override protected Void doInBackground(Void... params) {  
//			Log.d(TAG,"backgroundUpdateMyLocationtoServer");
//			updateLogToServer();
//			return null; 
//		}
//	}
//
//	/**
//	 * 사용자 위치 정보 및 정보 로깅
//	 * 
//	 */
//	public void updateLogToServer(){
//		if(isUpdating==0){
//			isUpdating = 1;
//			Log.i(TAG, "updateLocationToServer");
//			controllerName = "checkMileageLogController";
//			methodName = "registerLog";
//			final String myLat2 = Integer.toString(myLat);
//			final String myLon2 = Integer.toString(myLon);
//			//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
//			new Thread(
//					new Runnable(){
//						public void run(){
//							JSONObject obj = new JSONObject();
//							try{
//								// 자신의 아이디를 넣어서 조회
//								Date today = new Date();
//								SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//								String nowDate = sf.format(today);
//
//								Log.d(TAG,"checkMileageId :: "+qrCode);
//								Log.d(TAG,"parameter01 :: "+phoneNum);
//								Log.d(TAG,"parameter02 :: "+myLat2);
//								Log.d(TAG,"parameter03 :: "+myLon2);
//								Log.d(TAG,"registerDate :: "+nowDate);
//
//								obj.put("checkMileageId", qrCode);	// checkMileageId 	사용자 아이디
//								obj.put("merchantId", "");		// merchantId		가맹점 아이디.
//								obj.put("viewName", "CheckMileageCustomerQRView");		// viewName			출력된 화면.
//								obj.put("parameter01", phoneNum);		// parameter01		사용자 전화번호.
//								obj.put("parameter02", myLat2);		// parameter02		위도.
//								obj.put("parameter03", myLon2);		// parameter03		경도.
//								obj.put("parameter04", "");		// parameter04		검색일 경우 검색어.
//								obj.put("parameter05", "");		// parameter05		예비용도.
//								obj.put("parameter06", "");		// parameter06		예비용도.
//								obj.put("parameter07", "");		// parameter07		예비용도.
//								obj.put("parameter08", "");		// parameter08		예비용도.
//								obj.put("parameter09", "");		// parameter09		예비용도.
//								obj.put("parameter10", "");		// parameter10		예비용도.
//								obj.put("registerDate", nowDate);		// registerDate		등록 일자.
//							}catch(Exception e){
//								e.printStackTrace();
//							}
//							String jsonString = "{\"checkMileageLog\":" + obj.toString() + "}";
//							try{
//								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
//								connection2 = (HttpURLConnection) postUrl2.openConnection();
//								connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
//								connection2.setDoOutput(true);
//								connection2.setInstanceFollowRedirects(false);
//								connection2.setRequestMethod("POST");
//								connection2.setRequestProperty("Content-Type", "application/json");
//								//								connection2.connect();
//								Thread.sleep(200);
//								OutputStream os2 = connection2.getOutputStream();
//								os2.write(jsonString.getBytes("UTF-8"));
//								os2.flush();
//								Thread.sleep(200);
//								//								System.out.println("postUrl      : " + postUrl2);
//								//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
//								responseCode = connection2.getResponseCode();
//								//								InputStream in =  connection2.getInputStream();
//								//								os2.close();
//								// 조회한 결과를 처리.
//								if(responseCode==200 || responseCode==204){
//									Log.d(TAG,"updateLogToServer S");
//								}
//								//								connection2.disconnect();
//							}catch(Exception e){ 
//								//								connection2.disconnect();
//								Log.d(TAG,"updateLocationToServer->fail");
//							}finally{
//								isUpdating = 0;
//								//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//								//									CommonUtils.usingNetwork = 0;
//								//								}
//							}
//						}
//					}
//			).start();
//		}else{
//			Log.w(TAG,"already updating..");
//		}
//	}
}
