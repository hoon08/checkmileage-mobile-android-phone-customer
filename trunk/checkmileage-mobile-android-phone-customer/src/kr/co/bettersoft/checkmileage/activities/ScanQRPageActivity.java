package kr.co.bettersoft.checkmileage.activities;
// QR 스켄 페이지


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMemberSettings;

import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ScanQRPageActivity extends Activity {
	SharedPreferences sharedPrefCustom;
	
	String serverName = CommonUtils.serverNames;
	String controllerName = "";
	String methodName = "";
	String qrcode = "";
		
	int responseCode= 0;
	
	String phoneNumber = "";
	// 시간 관련
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	
	URL postUrl2;
	HttpURLConnection connection2;
	
	//Locale
	Locale systemLocale = null;
//	String strDisplayCountry = "";
	String strCountry = "";
	String strLanguage = "";
	
	// 설정 정보 저장할 도메인.
	CheckMileageMemberSettings settings;
	
	String idExist = "";
	static int qrResult = 0;
	public static final String TAG = ScanQRPageActivity.class.getSimpleName();
	
	// 핸들러 등록
	Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int showQR =  b.getInt("showQR");		// 값을 넣지 않으면 0 을 꺼내었다.
    		
    		if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.fail_scan_qr, Toast.LENGTH_SHORT).show();
			}
    		if(b.getInt("getUserSetting")==1){		// 서버에서 설정 정보 가져와서 저장
    			new backgroundGetUserSettingsFromServer().execute();     
			}
    		if(b.getInt("showIntroView")==1){		// 서버에서 설정 정보 가져와서 저장
    			setContentView(R.layout.intro);
			}
    		if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			}
    	}
    };
    public void showErrMSG(){			// 화면에 에러 토스트 띄움..
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("showErrToast", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}

	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_qr_page);
		
		Intent rIntent = getIntent();
        phoneNumber = rIntent.getStringExtra("phoneNumber");
        if(phoneNumber==null){
        	phoneNumber="";
        }
		/* 
		 * QR 스켄모드가 되어 QR 카드를 스켄한다.
		 * QR 카드 스켄이 성공하여 QR 정보를 얻어오면, 해당 정보를 앱에 저장하고, 서버에 등록한다.
		 * 이후 나의 QR 코드 보기 화면으로 이동한다.
		 */

		// QR 카드 스켄하는 부분..
		// ... QR 카드를 스켄하여 정보를 저장하고, 서버에 등록한다.

		// 나의 QR 코드 보기로 이동.
		Log.i("ScanQRPageActivity", "QR registered Success");
		//		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
		//	        startActivity(intent2);
		//	        finish();

		//	    Intent intent = new Intent("com.google.zxing.client.android.CaptureActivityHandler");
		Intent intent = new Intent(ScanQRPageActivity.this,com.google.zxing.client.android.CaptureActivity.class);
		//	    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("com.google.zxing.client.android");
		//        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		try{
			startActivityForResult(intent, 0);
		}catch(Exception e){
			e.printStackTrace();
			Log.i("ScanQRPageActivity", "error occured");
			Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
			startActivity(intent2);
			finish();
		}
	}

	// pref 에 QR 저장 .
    public void saveQRforPref(String qrCode){
    	sharedPrefCustom = getSharedPreferences("MyCustomePref",
    			Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    	SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
    	saveQR.putString("qrcode", qrCode);
    	saveQR.commit();
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == 0) {
			if(resultCode == RESULT_OK) {  // 성공시
				qrcode = intent.getStringExtra("SCAN_RESULT");
				// 1. 로컬에 QR 코드를 저장함
				Log.i("ScanQRPageActivity", "save qrcode to file : "+qrcode);
				saveQRforPref(qrcode);		// 설정에 qr 저장
				
				// 화면 전환- 스켄화면 대신 인트로 화면을 보여줌.
				new Thread(
						new Runnable(){
							public void run(){
								Message message = handler.obtainMessage();				
								Bundle b = new Bundle();
								b.putInt("showIntroView", 1);
								message.setData(b);
								handler.sendMessage(message);
							}
						}
				).start();
				
//				checkAlreadyExistID_pre();		// 서버에 아이디 있는지 확인해서 없으면 등록하고,  있으면 설정 정보를 가져와서 로컬에 저장한다.
				checkAlreadyExistID();		// 서버에 아이디 있는지 확인해서 없으면 등록하고,  있으면 설정 정보를 가져와서 로컬에 저장한다.
			} else if(resultCode == RESULT_CANCELED) {
				// 취소 또는 실패시 이전화면으로.
				showErrMSG();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
	}

	
	/*
	 * 기존 사용자인지 확인.
	 *  아이디로 서버에 조회해서 이미 등록된 아이디인지 확인한다.
	 *    이미 등록된 아이디인 경우 추가 등록할 필요가 없다. 대신 설정 정보를 가져와서 로컬에 저장한다.
	 */
//	public void checkAlreadyExistID_pre(){
//		new Thread(
//				new Runnable(){
//					public void run(){
//						Log.d(TAG,"updateMyGCMtoServer_pre");
//						try{
//							Thread.sleep(CommonUtils.threadWaitngTime);
//						}catch(Exception e){
//						}finally{
//							if(CommonUtils.usingNetwork<1){
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
//								checkAlreadyExistID();
//							}else{
//								checkAlreadyExistID_pre();
//							}
//						}
//					}
//				}
//			).start();
//	}
	public void checkAlreadyExistID(){
		Log.i(TAG, "checkAlreadyExistID");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberExist";
		// 서버 통신부
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("checkMileageId", qrcode);			  
							obj.put("activateYn", "Y");			
							Log.e(TAG,"myQRcode::"+qrcode);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
//							connection2.connect();
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							System.out.println("postUrl      : " + postUrl2);
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
								checkUserID(in);
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//									CommonUtils.usingNetwork = 0;
//								}
							}else{
								showErrMSG();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//									CommonUtils.usingNetwork = 0;
//								}
								 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
//							connection2.disconnect();
						}catch(Exception e){ 
//							connection2.disconnect();
//							CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//							if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//								CommonUtils.usingNetwork = 0;
//							}
							e.printStackTrace();
							showErrMSG();
							 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
							 startActivity(backToNoQRIntent);
							 finish();
						}
					}
				}
		).start();
	}
	// 사용자가 있는지 확인한 결과를 처리
	public void checkUserID(InputStream in){
		Log.d(TAG,"alalyzeData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"get data ::"+builder.toString());
		String tempstr = builder.toString();		
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				try{
					idExist = jsonobj2.getString("totalCount");				// 아이디가 있으면1 없으면 0
				}catch(Exception e){
					e.printStackTrace();
					idExist = "0";
				}
				if(idExist.equals("0")){		// 서버에 아이디가 없으면 업데이트 해준다. 
//					saveQRtoServer_pre();		
					saveQRtoServer();		
				}else{							// 서버에 아이디가 있으면 설정을 받아와서 저장해야 한다.
					Log.d(TAG,"idExist, getSettingsFromServer = T");
					//설정 정보를 가져와서 저장 함.  핸들러를 이용한다.
					new Thread(
							new Runnable(){
								public void run(){
									Message message = handler.obtainMessage();				
									Bundle b = new Bundle();
									b.putInt("getUserSetting", 1);
									message.setData(b);
									handler.sendMessage(message);
								}
							}
					).start();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	}
	
	// 다음페이지로 이동한다. qrCode 값을 전달한다.
	public void goNextPage(){
		Log.i("ScanQRPageActivity", "load qrcode to img : "+qrcode);
		MyQRPageActivity.qrCode = qrcode;
		Main_TabsActivity.myQR = qrcode;
		new Thread(
				new Runnable(){
					public void run(){
						Log.i("ScanQRPageActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
						// 나의 QR 코드 보기로 이동한다.
						Log.i("ScanQRPageActivity", "QR registered Success");
						Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
						startActivity(intent2);
						finish();		// 다른 액티비티를 호출하고 자신은 종료한다.
					}
				}
		).start();
	}
	
	// 백단에서 서버에서 설정 정보 가져오는 메서드 호출.
	public class backgroundGetUserSettingsFromServer extends   AsyncTask<Void, Void, Void> {
        @Override protected void onPostExecute(Void result) {  }
        @Override protected void onPreExecute() {  }
        @Override protected Void doInBackground(Void... params) { 
        	Log. d(TAG,"backgroundGetUserSettingsFromServer");
//        	getUserSettingsFromServer_pre();
        	getUserSettingsFromServer();
        	return null ;
        }
	}
	/*
	 *     인증 성공(현재 기능 보류) 이후  
	 *     이전 사용자일 경우 서버로부터 사용자 설정 정보를 가져와서 모바일의 설정 정보에 대입시킨다..
	 *     비번의 경우 분실시 어플 삭제후 재설치.. 재 인증 받는다. 그럼 비번 초기화.
	 */
//	public void getUserSettingsFromServer_pre(){
//		new Thread(
//				new Runnable(){
//					public void run(){
//						Log.d(TAG,"getUserSettingsFromServer_pre");
//						try{
//							Thread.sleep(CommonUtils.threadWaitngTime);
//						}catch(Exception e){
//						}finally{
//							if(CommonUtils.usingNetwork<1){
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
//								getUserSettingsFromServer();
//							}else{
//								getUserSettingsFromServer_pre();
//							}
//						}
//					}
//				}
//			).start();
//	}
	public void getUserSettingsFromServer(){		// 서버로부터 설정 정보를 받는다.  아이디를 사용.모든데이터.  CheckMileageMember
		Log.d(TAG, "getUserSettingsFromServer");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 자신의 아이디를 넣어서 조회
							Log.d(TAG,"getUserSettingsFromServer,QR code:"+qrcode);
							obj.put("checkMileageId", qrcode);		// 자신의 아이디 사용할 것.. 이전 사용자이다
							obj.put("activateYn", "Y");
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
//							connection2.setConnectTimeout(10000);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
//							connection2.connect();
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							if(responseCode==200 || responseCode==204){
								// 조회한 결과를 처리.
								theMySettingData1(in);
								//									Log.d(TAG,"S");
							}else{
								showErrMSG();
							}
//							connection2.disconnect();
						}catch(Exception e){ 
//							connection2.disconnect();
							e.printStackTrace();
						}
//						CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//						if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//							CommonUtils.usingNetwork = 0;
//						}
					}
				}
		).start();
	} 
	// 설정 정보를 로컬에 저장
	public void theMySettingData1(InputStream in){
		Log.d(TAG,"theMySettingData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		settings = new CheckMileageMemberSettings(); // 객체 생성
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"내 설정 상세정보::"+builder.toString());
		String tempstr = builder.toString();		
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				// 데이터를 전역 변수 도메인에 저장하고  설정에 저장..
				try{
					settings.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){
					settings.setEmail("");
				}
				try{
					settings.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){
					settings.setBirthday("");
				}
				try{
					settings.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){
					settings.setGender("");
				}
				try{
					settings.setReceive_notification_yn(jsonobj2.getString("receiveNotificationYn"));	
				}catch(Exception e){
					settings.setReceive_notification_yn("");
				}
				setUserSettingsToPrefs();		// 설정에 전달 및 저장
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	}
	/*
	 * 서버에서 받은 설정 정보를 모바일 내 설정 정보에 저장한다.
	 *   EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
	 *   4가지. -> 를 설정 도메인에 저장한 후 설정에서 세팅해준다..
	 */
	public void setUserSettingsToPrefs(){
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveUpdateYn = sharedPrefCustom.edit();		// 공용으로 비번도 저장해 준다.
		saveUpdateYn.putString("updateYN", "Y");
		saveUpdateYn.putString("server_birthday", settings.getBirthday());
		saveUpdateYn.putString("server_email", settings.getEmail());
		saveUpdateYn.putString("server_gender", settings.getGender());
		if(settings.getReceive_notification_yn().equals("N")){		// 있고 N
			saveUpdateYn.putBoolean("server_receive_notification_yn", false);
		}else{		// 없거나 Y
			saveUpdateYn.putBoolean("server_receive_notification_yn", true);
		}
		saveUpdateYn.commit();
		
		goNextPage();		// 다음 페이지로 이동.
	}
	
	
	 /*
     *   서버에 생성한 QR 아이디를 등록.(서버에 등록되어있지 않은경우 호출)
     *   
     */
//	public void saveQRtoServer_pre(){
//		new Thread(
//				new Runnable(){
//					public void run(){
//						Log.d(TAG,"saveQRtoServer_pre");
//						try{
//							Thread.sleep(CommonUtils.threadWaitngTime);
//						}catch(Exception e){
//						}finally{
//							if(CommonUtils.usingNetwork<1){
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
//								saveQRtoServer();
//							}else{
//								saveQRtoServer_pre();
//							}
//						}
//					}
//				}
//			).start();
//	}
    public void saveQRtoServer(){
    	Log.i(TAG, "saveQRtoServer");
		controllerName = "checkMileageMemberController";
		methodName = "registerMember";
		systemLocale = getResources().getConfiguration().locale;
//		strDisplayCountry = systemLocale.getDisplayCountry();
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();
		// 서버 통신부
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("checkMileageId", qrcode);			  
							obj.put("password", "");				
							obj.put("phoneNumber", phoneNumber);					// 따로 넣어야함
							obj.put("email", "");			
							obj.put("birthday", "");			
							obj.put("gender", "");			
							obj.put("latitude", "");			
							obj.put("longitude", "");			
							obj.put("deviceType", "AS");			
							obj.put("registrationId", "");			
							obj.put("activateYn", "Y");			
							obj.put("receiveNotificationYn", "Y");			
							obj.put("countryCode", strCountry);	
							obj.put("languageCode", strLanguage);	
							String nowTime = getNow();
							Log.i(TAG, "nowTime::"+nowTime);
							obj.put("modifyDate", nowTime);			
							obj.put("registerDate", nowTime);		
							Log.e(TAG,"myQRcode::"+qrcode);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);		 
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							System.out.println("postUrl      : " + postUrl2);
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								Log.d(TAG, "register user S");
//								connection2.disconnect();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//									CommonUtils.usingNetwork = 0;
//								}
								goNextPage();				// 다음 페이지로 이동 
							}else{
								Log.e(TAG, "register user F");
//								connection2.disconnect();
								showErrMSG();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//									CommonUtils.usingNetwork = 0;
//								}
								 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
						}catch(Exception e){ 
//							CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//							if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//								CommonUtils.usingNetwork = 0;
//							}
//							connection2.disconnect();
							e.printStackTrace();
							showErrMSG();
							 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
							 startActivity(backToNoQRIntent);
							 finish();
						}
					}
				}
		).start();
    }
    
    // 현시각
    public String getNow(){
		// 일단 오늘.
    	c = Calendar.getInstance();
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
		todayDay = c.get(Calendar.DATE);
		todayHour = c.get(Calendar.HOUR_OF_DAY);
		todayMinute = c.get(Calendar.MINUTE);
		todaySecond = c.get(Calendar.SECOND);
		String tempMonth = Integer.toString(todayMonth);
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
		return nowTime;
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
    
    @Override
	public void onDestroy(){
		super.onDestroy();
//		try{
//		connection2.disconnect();
//		}catch(Exception e){}
	}
}
