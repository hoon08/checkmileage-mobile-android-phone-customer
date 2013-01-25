package kr.co.bettersoft.checkmileage.activities;
// QR 생성 페이지
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.MemberStoreListPageActivity.backgroundGetMerchantInfo;

import org.json.JSONObject;

/**
 *  CreateQRPageActivity
 * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
 * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
 */
public class CreateQRPageActivity extends Activity {
	String TAG = "CreateQRPageActivity";
	SharedPreferences sharedPrefCustom;

	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;

	static int qrResult = 0;
	String qrcode = "";		 
	String phoneNumber = "";
	String tmpStr = "";
	// 시간 관련
	Calendar c = Calendar.getInstance();

	URL postUrl2;
	HttpURLConnection connection2;

	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;

	// Locale
	Locale systemLocale = null ;
	//    String strDisplayCountry = "" ;
	String strCountry = "" ;
	String strLanguage = "" ;


	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){				// 화면에 에러 토스트 띄움
					Toast.makeText(CreateQRPageActivity.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	/**
	 * alertMsg
	 *  화면에 error 토스트 띄운다
	 *
	 * @param alrtmsg
	 * @param
	 * @return
	 */
	public void alertMsg(final String alrtmsg){						// 에러 토스트 함수화
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						//						String alrtMsg = getString(R.string.certi_fail_msg);
						b.putInt("showErrToast", 1);
						b.putString("msg", alrtmsg);			
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

		// 시간 -> 생성할 아이디
		Calendar c = Calendar. getInstance();
		String timeID = Long.toString( c.getTimeInMillis());
		Log.e(TAG, "Now to millis : "+ timeID);

		Intent rIntent = getIntent();

		tmpStr = rIntent.getStringExtra("phoneNumber");
		if(tmpStr!=null && tmpStr.length()>0){
			phoneNumber = rIntent.getStringExtra("phoneNumber");
		}
		qrcode = timeID;			// 이 줄을  주석 처리하면 기본 값 test1234 사용 - test용도. , 주석 풀면 새로 만든 시간 아이디 사용- 실제 사용 용도.. *** 

		/*
		 *  서버와 통신하여 QR 생성.
		 */
		// QR 코드 자체 생성하는 부분..
		// ... QR 코드를 생성하고, 서버에 등록한다.
		// 현재 위의 하드코딩 텍스트 사용함. --> 만든거.

		/*
		 * QR 저장소 파일에 저장.
		 */
		Log.i("CreateQRPageActivity", "save qrcode to file : "+qrcode);

		new backgroundSaveQRforPref().execute();		// 비동기 실행 - 설정에 저장 -- 끝나면 서버에 저장 -- 이후 이동하는 걸로..
	}

	
	
	
	public void goMainTabs(){
		/*
		 * MyQR페이지에 생성된 QR로 QR이미지 받아서 보여줌.
		 */
		Log.i("CreateQRPageActivity", "load qrcode to img : "+qrcode);
		MyQRPageActivity.qrCode = qrcode;
		Main_TabsActivity.myQR = qrcode;

		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(300);
							Log.i("CreateQRPageActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
							// 나의 QR 코드 보기로 이동.
							Log.i("CreateQRPageActivity", "QR registered Success");
							Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
							startActivity(intent2);
							finish();		// 다른 액티비티를 호출하고 자신은 종료.
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}
	
	
	
	// 비동기로 호출. 설정에 저장
	/**
	 * backgroundSaveQRforPref
	 *  비동기로 설정에 qr 저장하는 함수 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundSaveQRforPref extends  AsyncTask<Void, Void, Void> { 			
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundSaveQRforPref");
			saveQRforPref(qrcode);				// 설정 파일 사용함.
			return null; 
		}
	}
	// pref 에 QR 저장 방식.
	/**
	 * saveQRforPref
	 *  설정에 qr 저장한다
	 *  --파일에도 저장한다.  20130125
	 * @param qrCode
	 * @param
	 * @return
	 */
	public void saveQRforPref(String qrCode){
		// 설정에 저장
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
		saveQR.putString("qrcode", qrCode);
		saveQR.commit();

		// 파일에 저장
		try {
			File myFile = new File(CommonUtils.qrFileSavedPath);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
									new OutputStreamWriter(fOut);
			myOutWriter.append(qrCode);
			myOutWriter.close();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new backgroundSaveQRtoServer().execute();		// 설정에 저장 끝나면 .. 비동기 실행 - 서버에 저장
	}

	// 비동기로 호출. 서버에 저장
	/**
	 * backgroundSaveQRtoServer
	 * 비동기로 서버에 qr 저장하는 함수 호출
	 *
	 * @param 
	 * @param
	 * @return
	 */
	public class backgroundSaveQRtoServer extends  AsyncTask<Void, Void, Void> { 			
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundSaveQRtoServer");
			saveQRtoServer();					// 서버에도 저장함.			// test1234 아이디로 테스트시에 주석처리하지 않으면 에러가 발생한다.
			return null; 
		}
	}
	/*
	 *  서버에 생성한 QR 저장
	 *  checkMileageMemberController registerMember 
	 *  
	 *  checkMileageId  password  phoneNumber email birthday  gender  latitude  longitude
	 *  deviceType  registrationId  activateYn  modifyDate  registerDate
	 *  
	 *  checkMileageMember   CheckMileageMember
	 */
	/**
	 * saveQRtoServer
	 *  서버에 생성한 qr 저장한다
	 *
	 * @param 
	 * @param
	 * @return
	 */
	public void saveQRtoServer(){
		Log.i(TAG, "saveQRtoServer");
		controllerName = "checkMileageMemberController";
		methodName = "registerMember";

		// locale get
		systemLocale = getResources().getConfiguration(). locale;
		//      strDisplayCountry = systemLocale.getDisplayCountry();
		strCountry = systemLocale .getCountry();
		strLanguage = systemLocale .getLanguage();

		// 서버 통신부
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("checkMileageId", qrcode);			  
							obj.put("password", "");				
							obj.put("phoneNumber", phoneNumber);			
							obj.put("email", "");			
							obj.put("birthday", "");			
							obj.put("gender", "");			
							obj.put("latitude", "");			
							obj.put("longitude", "");			
							obj.put("deviceType", "AS");			
							obj.put("registrationId", "");			
							obj.put("activateYn", "Y");			
							obj.put("receiveNotificationYn", "Y");			

							obj.put( "countryCode", strCountry ); 
							obj.put( "languageCode" , strLanguage );

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
							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							//							connection2.connect();		// *** 
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							Thread.sleep(200);	
							//							System.out.println("postUrl      : " + postUrl2);
							//							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							int responseCode = connection2.getResponseCode();
							//							os2.close();		// 
							if(responseCode==200||responseCode==204){
								Log.e(TAG, "register user S");
								//								connection2.disconnect();
								
								// 저장끝나고 나서 액티비티 이동.
								goMainTabs();
								
							}else{
								Log.e(TAG, "register user F");		// 오류 발생시 에러 창 띄우고 돌아간다.. 통신에러 발생할수 있다.
								String alrtMsg = getString(R.string.error_message);
								alertMsg(alrtMsg);		// toast 사용시 에러 발생하므로 핸들러 통한 토스트
								//								Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
								//								connection2.disconnect();
								Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
								startActivity(backToNoQRIntent);
								finish();
							}
						}catch(Exception e){ 
							//							connection2.disconnect();
							e.printStackTrace();			// 오류 발생시 에러 창 띄우고 돌아간다.. 통신에러 발생할수 있다.
							String alrtMsg = getString(R.string.error_message);
							alertMsg(alrtMsg);		// toast 사용시 에러 발생하므로 핸들러 통한 토스트
							//							 Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
							Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
							startActivity(backToNoQRIntent);
							finish();
						}
					}
				}
		).start();
	}

	// 현시각
	/**
	 * getNow
	 *  현시각을 추출한다
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
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
