package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 생성 페이지
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pref.PrefActivityFromResource;
/* 
 * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
 * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
 */
public class CreateQRPageActivity extends Activity {
	String TAG = "CreateQRPageActivity";
	SharedPreferences sharedPrefCustom;
	
	String controllerName = "";
	String methodName = "";
	
	static int qrResult = 0;
	String qrcode = "test1234";
//	String qrcode = "createdNewQRCodeOne";
	String phoneNumber = "";
	
	// 시간 관련
	Calendar c = Calendar.getInstance();
	
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

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	 // 시간 -> 생성할 아이디
	    Calendar c = Calendar. getInstance();
	    String timeID = Long.toString( c.getTimeInMillis());
	    Log.e(TAG, "Now to millis : "+ timeID);
	    
	    Intent rIntent = getIntent();
        phoneNumber = rIntent.getStringExtra("phoneNumber");

        
        
        
//	    qrcode = timeID;			// 이 줄을  주석 처리하면 기본 값 test1234 사용 - test용도. , 주석 풀면 새로 만든 시간 아이디 사용- 실제 사용 용도.. *** 
	    
        
        
        
        
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
	    
//	    CommonUtils.writeQRstr = qrcode;	// qr 저장소 사용안함.
//	    saveQR();							// qr 저장소 사용 안함.
	    saveQRforPref(qrcode);				// 설정 파일 사용함.

	    saveQRtoServer();					// 서버에도 저장함.			// test1234 아이디로 테스트시에 주석처리하지 않으면 에러가 발생한다.
	    
	    
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
	    					Thread.sleep(1000);
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
	
	
	// QR 코드 저장소에 QR 코드를 저장한다. 
    public void saveQR(){		
    	CommonUtils.callCode = 22;		// 쓰기 모드
    	Intent saveQRintent = new Intent(CreateQRPageActivity.this, CommonUtils.class);			// 호출
    	startActivity(saveQRintent);
    }
    // pref 에 QR 저장 방식. 위에거 대신 쓸것.
    public void saveQRforPref(String qrCode){
    	sharedPrefCustom = getSharedPreferences("MyCustomePref",
    			Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    	SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
    	saveQR.putString("qrcode", qrCode);
    	saveQR.commit();
    }
    
    /*
     *  서버에 생성한 QR 저장.
     *  checkMileageMemberController registerMember 
     *  
     *  checkMileageId  password  phoneNumber email birthday  gender  latitude  longitude
     *  deviceType  registrationId  activateYn  modifyDate  registerDate
     *  
     *  checkMileageMember   CheckMileageMember
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
							URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
							HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes());
							os2.flush();
							System.out.println("postUrl      : " + postUrl2);
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
//								theData1(in);
								Log.e(TAG, "register user S");
							}else{
								Log.e(TAG, "register user F");		// 오류 발생시 에러 창 띄우고 돌아간다.. 통신에러 발생할수 있다.
								Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
								 Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
						}catch(Exception e){ 
							 e.printStackTrace();			// 오류 발생시 에러 창 띄우고 돌아간다.. 통신에러 발생할수 있다.
							 Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
							 Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
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
}
