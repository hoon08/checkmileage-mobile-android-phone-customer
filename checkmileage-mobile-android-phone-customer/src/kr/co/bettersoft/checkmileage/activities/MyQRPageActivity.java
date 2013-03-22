package kr.co.bettersoft.checkmileage.activities;
/**
 * MyQRPageActivity
 * 
 *  내 QR 보기 화면
 *  
 *  그리고 onresume 에 좌표 업뎃 기능 을 넣어 어플 실행 또는 내 QR 코드를 볼때마다 현재 사용자 위치를 서버에 업뎃 하도록 하였음.
 *   화면을 자주 오가며 업뎃 자꾸 시키는것을 방지하기 위해  플래그 값을 둠.  빨라서 대부분은 업뎃을 함..
 *  
 */


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
import kr.co.bettersoft.checkmileage.utils.AES256Cipher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;




import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class MyQRPageActivity extends Activity {
	String TAG = "MyQRPageActivity";

	
	// 서버 통신 용	 			///////////////////////////////////////////////
	String controllerName="";
	String methodName="";
	String serverName = CommonUtils.serverNames;
	URL postUrl2 ;
	HttpURLConnection connection2;
	int responseCode= 0;
	int isUpdating = 0;
	int app_end = 0;			// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	///////////////////////////////////////////////////////////////////////////

	

	// 내 좌표 업뎃용				///////////////////////////////////////////////
	int myLat = 0;
	int myLon = 0;
	String myLat2;
	String myLon2;
	// 전번(업뎃용)
	String phoneNum = "";
	// 설정 파일 저장소  - 사용자 전번 읽기 / 쓰기 용도	
	SharedPreferences sharedPrefCustom;
	/////////////////////////////////////////////////////////////////////////////

	
	// QR 관련					////////////////////////////////////////////////
	static Bitmap savedBMP = null;				// db 저장된 이미지 (전달받음)
	int qrSize =300;							// QR이미지 크기
	int deviceSize = 0;		
	static Bitmap bmp =null;					// 이미지 생성용도
	static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";			// qr 아이디
	/////////////////////////////////////////////////////////////////////////////



	// 핸들러 등록					//////////////////////////////////////////////////
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			int showQR =  b.getInt("showQR");		// 값을 넣지 않으면 0 을 꺼내었다.
			if(showQR==1){
				imgView.setImageBitmap(bmp);		// 화면에 QR 보여준다.
			}
		}
	};
	/////////////////////////////////////////////////////////////////////////////

	

	/**
	 * createQRself
	 *  자체 QR 생성 함수 호출한다
	 *
	 * @param qrCode
	 * @param
	 * @return bm
	 */
	public Bitmap createQRself(String qrCode){		// 자체 QR 생성
		try { 
			// generate a 200x200 QR code 
			Bitmap bm = encodeAsBitmap(qrCode, BarcodeFormat.QR_CODE, 200, 200); 
			if(bm != null) { 
				Log.d(TAG,"S to createQRself");
				return bm;
			} 
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return null;
	}

	// 자체 QR 생성 용도
	private static final int WHITE = 0xFFFFFFFF;  
	private static final int BLACK = 0xFF000000;
	/**
	 * encodeAsBitmap
	 *  자체 QR 생성 한다
	 *
	 * @param contents
	 * @param format
	 * @return bitmap
	 */
	static Bitmap encodeAsBitmap(String contents,
			BarcodeFormat format, 
			int desiredWidth,         
			int desiredHeight) throws Exception {
		MultiFormatWriter writer = new MultiFormatWriter();   
		BitMatrix result = writer.encode(contents, format, desiredWidth, desiredHeight, null);   
		int width = result.getWidth();  
		int height = result.getHeight();  
		int[] pixels = new int[width * height];   
		// All are 0, or black, by default  
		for (int y = 0; y < height; y++) {
			int offset = y * width;    
			for (int x = 0; x < width; x++) {   
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;   
			}   
		}    
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height); 
		return bitmap; 
	}


	// 생성한 QR코드 이미지를 DB에 저장한다.
	/**
	 * saveBMPtoDB
	 *  생성한 QR코드 이미지를 DB에 저장한다.
	 *
	 * @param bmp
	 * @param
	 * @return
	 */
	public void saveBMPtoDB(Bitmap bmp){
		Log.d(TAG,"saveBMPtoDB");
		SQLiteDatabase db = null;
		db= openOrCreateDatabase( "sqlite_carrotDB.db",             
				SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		String data_key="";
		String data_value="";

		// BMP -> 문자열 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();   
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
		byte[] b = baos.toByteArray();  
		data_value = Base64.encodeToString(b, Base64.DEFAULT); 

		// 조회
		ContentValues initialValues = new ContentValues(); 
		initialValues.put("key_of_data", "user_img"); 
		initialValues.put("value_of_data", data_value); 
		db.insert("user_info", null, initialValues); 
		try{
			db.replaceOrThrow("user_info", null, initialValues);
		}catch(Exception e){
			e.printStackTrace();
		}
		db.close();
	}

	/*
	 * QR 이미지받기. url 사용하여 구글 웹에서 받아오기.
	 */
	/**
	 * downloadBitmap
	 *  url 사용하여 구글 웹에서  QR 이미지 받아온다
	 *
	 * @param url
	 * @param
	 * @return bitmap
	 */
	static Bitmap downloadBitmap(String url) {    
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");    
		final HttpGet getRequest = new HttpGet(url);    
		try {        
			HttpResponse response = client.execute(getRequest);        
			final int statusCode = response.getStatusLine().getStatusCode();        
			if (statusCode != HttpStatus.SC_OK) {             
				Log.w("MyQRPageActivity", "Error " + statusCode + " while retrieving bitmap from " + url);             
				return null;        
			}                
			final HttpEntity entity = response.getEntity();        
			if (entity != null) {            
				InputStream inputStream = null;            
				try {                
					inputStream = entity.getContent();                 
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);                
					return bitmap;            
				} finally {                
					if (inputStream != null) {                    
						inputStream.close();                  
					}                
					entity.consumeContent();           
				}        
			}    
		} catch (Exception e) {        
			getRequest.abort();        
			Log.w("MyQRPageActivity", "Error while retrieving bitmap from " + url +"  " + e.toString());    
		} finally {        
			if (client != null) {            
				client.close();        
			}    
		}    
		return null;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_qr_page);
		imgView = (ImageView)findViewById(R.id.myQRCode);

		/*
		 *  QR 크기를 화면에 맞추기 위해 화면 크기를 구함.
		 */
		Log.i("qrCode : ", "" + qrCode);
		//		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		//		Log.i("screenWidth : ", "" + screenWidth);
		//		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		//		Log.i("screenHeight : ", "" + screenHeight);
		/*
		 *  QR 코드를 받아옴.  구글 웹페이지를 통한 생성 --> 자체 라이브러리 먼저 해보고 안되면 웹통신.
		 */
		new Thread(
				new Runnable(){
					public void run(){
						if(savedBMP==null){	// 기존 저장된 파일 없는경우.
							if(qrCode!=null && qrCode.length()>0){
								bmp = createQRself(qrCode);		// 자체 라이브러리 사용하여 생성.
								if(bmp==null){			// 자체 생성 실패한 경우.
									Log.d(TAG,"bmp1==null");
									bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode);		// 웹 통신하여 가져옴 
									if(bmp==null){
										Log.d(TAG,"bmp2==null");
										finish();
									}else{
										saveBMPtoDB(bmp);
									}
									// QR 이미지 생성 실패. 처리 필요 ** no qr img 로 가야 할듯.? 재실행?;
								}else{	// 자체 성공 성공한 경우
									saveBMPtoDB(bmp);
								}
							}else{
								// finish();	// 종료. qr 없이 올수 없음.
							}
						}else{	// 기존 저장된 파일 있는 경우
							bmp = savedBMP;
						}
						// showQR
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("showQR", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();

		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		
		// 서버 로깅에 필요한 전번 좌표 등을 설정에 저장해둔다.
		saveDataForLogToPref();
	}


	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		//		myLocationIs();

		
		
		// 서버에 로그를 남긴다.			// *** 서버 로깅 일단 주석 처리함. 나중에 구현 이후 주석 풀것.
		if(!(phoneNum==null || phoneNum.length()<1)){	
			if(qrCode!=null && qrCode.length()>0){
				//				applicationClass = (ApplicationClass)getApplicationContext();
				//				applicationClass.loggingToServer(qrCode);		// 실패
				if(isUpdating==0){
					//					getNowStart();		// **** 테스트용. 얼마나 걸리는지 확인하기 위함.
					loggingToServer();
				}
			}
		}
	}
	

	@Override
	public void onDestroy(){
		super.onDestroy();
		//		try{
		//			if(connection2!=null){
		//				connection2.disconnect();
		//			}
		//		}catch(Exception e){}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	/**
	 * onBackPressed
	 *   닫기 버튼 2번 누르면 종료 한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	public void onBackPressed() {
		Log.d("MainTabActivity", "MyQRPage finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
			new Thread( 
					new Runnable(){
						public void run(){
							try {
								Thread.sleep(3000);
								app_end = 0;
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
			).start();
		}
	}
	

	// 업뎃 시각
	/**
	 * getNow
	 *  현시각을 구한다
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
		// 일단 오늘.
		Calendar c = Calendar.getInstance();
		int todayYear = 0;						// 지금 -  년 월 일 시 분
		int todayMonth = 0;
		int todayDay = 0;
		int todayHour = 0;
		int todayMinute = 0;
		int todaySecond = 0;
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
		todayDay = c.get(Calendar.DATE);
		todayHour = c.get(Calendar.HOUR_OF_DAY);
		todayMinute = c.get(Calendar.MINUTE);
		todaySecond = c.get(Calendar.SECOND);
		String tempMonth = Integer.toString(todayMonth+1);
		String tempDay = Integer.toString(todayDay);
		String tempHour = Integer.toString(todayHour);
		String tempMinute = Integer.toString(todayMinute);
		String tempSecond = Integer.toString(todaySecond);
		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
		if(tempDay.length()==1) tempDay = "0"+tempDay;
		if(tempHour.length()==1) tempHour = "0"+tempHour;
		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
		if(tempSecond.length()==1) tempSecond = "0"+tempSecond;
		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute+":"+tempSecond;

		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
		return nowTime;
	}
	
	// 시간 측정 용
	//	public void getNowStart(){
	//		Calendar c = Calendar.getInstance();
	//		Log.e(TAG, "getNowStart to millis : "+ Long.toString(c.getTimeInMillis()));
	//	}
	//	public void getNowEnd(){
	//		Calendar c = Calendar.getInstance();
	//		Log.e(TAG, "getNowEnd to millis : "+ Long.toString(c.getTimeInMillis()));
	//	}
	
	
	/**
	 * AES256  -  Base64  암호화/복호화
	 */
	// 암호화
	public String encodeAES(String plainText){
		String encodeText = "";
		try {
			encodeText = AES256Cipher.AES_Encode(plainText, CommonUtils.key);
			Log.d(TAG,"plainText::"+plainText+"//encodeText::"+encodeText);
		} catch (Exception e) {
			e.printStackTrace();
			encodeText = "";
		}
		return encodeText;
	}
	// 복호화
	public String decodeAES(String encodeText){
		String decodeText="";
		try {
			decodeText = AES256Cipher.AES_Decode(encodeText, CommonUtils.key);
			Log.d(TAG,"encodeText::"+encodeText+"//decodeText::"+decodeText);
		} catch (Exception e) {
			e.printStackTrace();
			decodeText = "";
		}
		return decodeText;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 서버에 위치 및 로그 남김
	 * loggingToServer
	 */
	public void loggingToServer(){
//		try{
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
					new backgroundUpdateLogToServer().execute();	// 비동기로 전환	
//				}
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//			Log.w(TAG,"fail to update my location to server");
//		}
	}

	/**
	 * 비동기로 사용자의 위치 정보 및 정보 로깅
	 * backgroundUpdateLogToServer
	 */
	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyLocationtoServer");
			//        		updateLocationToServer_pre();
			updateLogToServer();
			return null; 
		}
	}

	/**
	 * 사용자 위치 정보 및 정보 로깅
	 * 
	 */
	public void updateLogToServer(){
		if(isUpdating==0){
			isUpdating = 1;
			Log.i(TAG, "updateLocationToServer");
			controllerName = "checkMileageLogController";
			methodName = "registerLog";

//			myLat2 = Integer.toString(myLat);		//  int -> str
//			myLon2 = Integer.toString(myLon);
//
//			Float tempFloat;		
//			//		DecimalFormat format = new DecimalFormat(".###");			// 소수점 세번째 자리 까지만 나오도록 한다.
//
//			//		myLat2 = Float.toString(Float.parseFloat(myLat2)/1000000);		// 0 x 6개 앞으로
//			//		myLon2 = Float.toString(Float.parseFloat(myLon2)/1000000);		// 0 x 6개 앞으로
//
//			tempFloat = Float.parseFloat(myLat2)/1000000;		// str - float -> 나누기 백만 (소수점 처리)
//			myLat2 = String.format("%.3f", tempFloat);
//			//		myLat2 = format.format(tempFloat);					// 소수점 세번째 자리까지만
//			tempFloat = Float.parseFloat(myLon2)/1000000;		// str - float -> 나누기 백만 (소수점 처리)
//			myLon2 = String.format("%.3f", tempFloat);
//			//		myLon2 = format.format(tempFloat);					// 소수점 세번째 자리까지만
//
////			decodeAES(encodeAES(myLat2));		// 암복호화 통합 test ****	// 나중에 주석 처리
////			decodeAES(encodeAES(myLon2));		// 암복호화 통합 test ****	// 나중에 주석 처리 
//
//			myLat2 = encodeAES(myLat2);		// 암호화		// **** 나중에 주석 풀어서 사용
//			myLon2 = encodeAES(myLon2);		// 암호화		// **** 나중에 주석 풀어서 사용

			
			
			/////  위에 까지 oncreate 에서 처리.. 아래 저장하는 부분은 onresume 에서 할것...	
					
			phoneNum = sharedPrefCustom.getString("phoneNum", "");	
			myLat2 = sharedPrefCustom.getString("myLat2", "");	
			myLon2 = sharedPrefCustom.getString("myLon2", "");	
			qrCode = sharedPrefCustom.getString("qrCode", "");			
			//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
			
			
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 자신의 아이디를 넣어서 조회
								Date today = new Date();
								SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String nowDate = sf.format(today);
								//	    				    Log.d(TAG,"checkMileageId :: "+qrCode);
								//	    				    Log.d(TAG,"parameter01 :: "+phoneNum);
								//	    				    Log.d(TAG,"parameter02 :: "+myLat2);
								//	    				    Log.d(TAG,"parameter03 :: "+myLon2);
								//	    				    Log.d(TAG,"registerDate :: "+nowDate);
								obj.put("checkMileageId", qrCode);	// checkMileageId 	사용자 아이디
								obj.put("merchantId", "");		// merchantId		가맹점 아이디.
								obj.put("viewName", "CheckMileageCustomerQRView");		// viewName			출력된 화면.
								obj.put("parameter01", phoneNum);		// parameter01		사용자 전화번호.
								
//								obj.put("parameter02", myLat2);		// parameter02		위도.	// **** 일단 공백 보냄. 나중에 교체
//								obj.put("parameter03", myLon2);		// parameter03		경도.	// **** 일단 공백 보냄. 나중에 교체
								obj.put("parameter02", "");		// parameter02		위도.		// **** 일단 공백 보냄. 나중에 교체
								obj.put("parameter03", "");		// parameter03		경도.		// **** 일단 공백 보냄. 나중에 교체
								
								obj.put("parameter04", "");		// parameter04		검색일 경우 검색어.
								obj.put("parameter05", "");		// parameter05		예비용도.
								obj.put("parameter06", "");		// parameter06		예비용도.
								obj.put("parameter07", "");		// parameter07		예비용도.
								obj.put("parameter08", "");		// parameter08		예비용도.
								obj.put("parameter09", "");		// parameter09		예비용도.
								obj.put("parameter10", "");		// parameter10		예비용도.
								obj.put("registerDate", nowDate);		// registerDate		등록 일자.
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageLog\":" + obj.toString() + "}";
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								//								connection2.connect();
								Thread.sleep(200);
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes("UTF-8"));
								os2.flush();
								Thread.sleep(200);
								//								System.out.println("postUrl      : " + postUrl2);
								//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								//								InputStream in =  connection2.getInputStream();
								//								os2.close();
								// 조회한 결과를 처리.
								if(responseCode==200 || responseCode==204){
									Log.d(TAG,"updateLogToServer S");
								}else{
									Log.d(TAG,"updateLogToServer F / "+responseCode);
								}
								//								connection2.disconnect();
							}catch(Exception e){ 
								//								connection2.disconnect();
								Log.d(TAG,"updateLocationToServer->fail");
							}finally{
								isUpdating = 0;
								//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
								//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
								//									CommonUtils.usingNetwork = 0;
								//								}
								//							getNowEnd();	// **** 테스트용. 얼마나 걸리는지 확인하기 위함.
							}
						}
					}
			).start();
		}else{
			Log.w(TAG,"already updating..");
		}
	}
	
	/*
	 * oncreate 에서는 pref 에 저장만 하고 resume 에서 꺼내서 사용할 것....
	 * 
	 * 서버 로깅에 필요한 좌표/전번 을 pref 설정에 저장한다. 이후 다른 페이지에서는 이것을 사용한다.
	 * 
	 */
	public void saveDataForLogToPref(){
		// 좌표 구하기
		try{
			LocationManager  lm;
			Location location;
			String provider;
			String bestProvider;
			lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			provider = LocationManager.GPS_PROVIDER;
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소비량
			criteria.setAltitudeRequired(false); // 고도
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // 속도
			criteria.setCostAllowed(true); // 금전적 비용
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
				myLon = (int) (location.getLongitude()*1000000);	
				Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
//				new backgroundUpdateLogToServer().execute();	// 비동기로 서버에 위치 업뎃	// **** 		
			}else{
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.d(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
					myLon = (int) (location.getLongitude()*1000000);	
					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);		
//					new backgroundUpdateLogToServer().execute();	// 비동기로 전환	// **** 
				}
			}
			myLat2 = Integer.toString(myLat);		//  int -> str
			myLon2 = Integer.toString(myLon);
			Float tempFloat;		
			tempFloat = Float.parseFloat(myLat2)/1000000;		// str - float -> 나누기 백만 (소수점 처리)
			myLat2 = String.format("%.3f", tempFloat);
			tempFloat = Float.parseFloat(myLon2)/1000000;		// str - float -> 나누기 백만 (소수점 처리)
			myLon2 = String.format("%.3f", tempFloat);
//			decodeAES(encodeAES(myLat2));		// 암복호화 통합 test ****	// 나중에 주석 처리
//			decodeAES(encodeAES(myLon2));		// 암복호화 통합 test ****	// 나중에 주석 처리 
			myLat2 = encodeAES(myLat2);		// 암호화		// **** 나중에 주석 풀어서 사용
			myLon2 = encodeAES(myLon2);		// 암호화		// **** 나중에 주석 풀어서 사용
		}catch(Exception e){
			e.printStackTrace();
//			Log.w(TAG,"fail to update my location to server");
		}
		
		// 전번 꺼내고 없으면 구해서 저장함.
		phoneNum = sharedPrefCustom.getString("phoneNum", "");
		if(phoneNum==null || phoneNum.length()<1){		// pref 에 전번 없을 경우
			//자신의 전화번호 가져오기
			try{		// 읽다가 에러 터질때에 대비하기
				TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
				phoneNum = telManager.getLine1Number();
				SharedPreferences.Editor updatePhoneNum =   sharedPrefCustom.edit();
				updatePhoneNum.putString("phoneNum", phoneNum);		// 전번 저장
				updatePhoneNum.commit();
			}catch(Exception e){
				phoneNum = "";
			}
		}
		// 좌표 저장함
		SharedPreferences.Editor updateDataForLog =   sharedPrefCustom.edit();
		updateDataForLog.putString("phoneNum", phoneNum);		// 전번 저장 --> 위에서 저장
		updateDataForLog.putString("myLat2", myLat2);		// 위도
		updateDataForLog.putString("myLon2", myLon2);		// 경도
		updateDataForLog.putString("qrCode", qrCode);		// qr
		updateDataForLog.commit();
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	/**
	//	 * 서버에 위치 및 사용자 정보 로깅
	//	 * updateLogToServer
	//	 * 
	//	 */
	//	public void updateLogToServer(){
	//	try{
	//		LocationManager  lm;
	//		Location location;
	//		String provider;
	//		String bestProvider;
	//		lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
	//		provider = LocationManager.GPS_PROVIDER;
	//		Criteria criteria = new Criteria();
	//		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
	//		criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소비량
	//		criteria.setAltitudeRequired(false); // 고도
	//		criteria.setBearingRequired(false); // ..
	//		criteria.setSpeedRequired(false); // 속도
	//		criteria.setCostAllowed(true); // 금전적 비용
	//		bestProvider = lm.getBestProvider(criteria, true);
	//		location =  lm.getLastKnownLocation(bestProvider);
	//		if(location!=null){
	//			myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
	//			myLon = (int) (location.getLongitude()*1000000);	
	//			Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
	//			new backgroundUpdateLocationToServer().execute();	// 비동기로 서버에 위치 업뎃		
	//		}else{
	//			location =  lm.getLastKnownLocation(provider);
	//			if(location==null){
	//				Log.d(TAG,"location = null");	
	//			}else{
	//				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
	//				myLon = (int) (location.getLongitude()*1000000);	
	//				Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);			
	//				new backgroundUpdateLocationToServer().execute();	// 비동기로 전환	
	//			}
	//		}
	//	}catch(Exception e){
	//		Log.w(TAG,"fail to update my location to server");
	//	}
	//}

	// 서버에 내 위치 업뎃.
	//	/**
	//	 * myLocationIs
	//	 *  서버에 내 위치 업뎃한다
	//	 *
	//	 * @param
	//	 * @param
	//	 * @return
	//	 */
	//	public void myLocationIs(){
	//		try{
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
	//				new backgroundUpdateLocationToServer().execute();	// 비동기로 서버에 위치 업뎃		
	//			}else{
	//				location =  lm.getLastKnownLocation(provider);
	//				if(location==null){
	//					Log.d(TAG,"location = null");	
	//				}else{
	//					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
	//					myLon = (int) (location.getLongitude()*1000000);	
	//					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);			
	//					new backgroundUpdateLocationToServer().execute();	// 비동기로 전환	
	//				}
	//			}
	//		}catch(Exception e){
	//			Log.w(TAG,"fail to update my location to server");
	//		}
	//	}

	// 비동기로사용자의 위치 정보를 수정
	//	/**
	//	 * backgroundUpdateLocationToServer
	//	 *  비동기로사용자의 위치 정보를 수정하는 함수 호출한다
	//	 *
	//	 * @param
	//	 * @param
	//	 * @return
	//	 */
	//	public class backgroundUpdateLocationToServer extends  AsyncTask<Void, Void, Void> { 
	//		@Override protected void onPostExecute(Void result) {  
	//		} 
	//		@Override protected void onPreExecute() {  
	//		} 
	//		@Override protected Void doInBackground(Void... params) {  
	//			Log.d(TAG,"backgroundUpdateMyLocationtoServer");
	//			//        		updateLocationToServer_pre();
	//			updateLocationToServer();
	//			return null; 
	//		}
	//	}

	/*
	 * 사용자의 위치 정보를 수정 한다.
	 * 그 결과를 'SUCCESS' 나 'FAIL' 의 스트링으로 반환 한다.
	 * //checkMileageMemberController  updateMemberLocation   checkMileageMember  
	 *	// checkMileageId  latitude  longitude  activateYn  modifyDate
	 */
	//	public void updateLocationToServer_pre(){
	//		new Thread(
	//				new Runnable(){
	//					public void run(){
	//						Log.d(TAG,"updateLocationToServer_pre");
	//						try{
	//							Thread.sleep(CommonUtils.threadWaitngTime);
	//						}catch(Exception e){
	//						}finally{
	//							if(CommonUtils.usingNetwork<1){
	//								CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
	//								updateLocationToServer();
	//							}else{
	//								updateLocationToServer_pre();
	//							}
	//						}
	//					}
	//				}
	//			).start();
	//	}


	//	/**
	//	 * updateLocationToServer
	//	 *  사용자의 위치 정보를 수정 한다.
	//	 *
	//	 * @param
	//	 * @param
	//	 * @return
	//	 */
	//	public void updateLocationToServer(){
	//		if(isUpdating==0){
	//			isUpdating = 1;
	//			Log.i(TAG, "updateLocationToServer");
	//			controllerName = "checkMileageMemberController";
	//			methodName = "updateMemberLocation";
	//			final String myLat2 = Integer.toString(myLat);
	//			final String myLon2 = Integer.toString(myLon);
	//			//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
	//			new Thread(
	//					new Runnable(){
	//						public void run(){
	//							JSONObject obj = new JSONObject();
	//							try{
	//								// 자신의 아이디를 넣어서 조회
	//								//								Log.d(TAG,"checkMileageId::"+qrCode);
	//								//								Log.d(TAG,"latitude::"+myLat);
	//								//								Log.d(TAG,"longitude::"+myLon);
	//								//								Log.d(TAG,"activateYn::"+"Y");
	//								//								Log.d(TAG,"modifyDate::"+todays);
	//								obj.put("checkMileageId", qrCode);
	//								obj.put("latitude", myLat2);
	//								obj.put("longitude", myLon2);
	//								obj.put("activateYn", "Y");
	//
	//								String nowTime = getNow();
	//
	//								obj.put("modifyDate", nowTime);
	//							}catch(Exception e){
	//								e.printStackTrace();
	//							}
	//							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
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
	//									//									Log.d(TAG,"S");
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

	
	
	
	//	/**
//	 * 서버에 위치 및 로그 남김
//	 * loggingToServer
//	 */
//	public void loggingToServer(){
//					new backgroundUpdateLogToServer().execute();	// 비동기로 전환	
//	}
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
//					
//			phoneNum = sharedPrefCustom.getString("phoneNum", "");	
//			myLat2 = sharedPrefCustom.getString("myLat2", "");	
//			myLon2 = sharedPrefCustom.getString("myLon2", "");	
//			qrCode = sharedPrefCustom.getString("qrCode", "");	
//			
//			new Thread(
//					new Runnable(){
//						public void run(){
//							JSONObject obj = new JSONObject();
//							try{
//								// 자신의 아이디를 넣어서 조회
//								Date today = new Date();
//								SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//								String nowDate = sf.format(today);
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
//								responseCode = connection2.getResponseCode();
//								// 조회한 결과를 처리.
//								if(responseCode==200 || responseCode==204){
//									Log.d(TAG,"updateLogToServer S");
//								}else{
//									Log.d(TAG,"updateLogToServer F / "+responseCode);
//								}
//							}catch(Exception e){ 
//								Log.d(TAG,"updateLocationToServer->fail");
//							}finally{
//								isUpdating = 0;
//							}
//						}
//					}
//			).start();
//		}else{
//			Log.w(TAG,"already updating..");
//		}
//	}
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
