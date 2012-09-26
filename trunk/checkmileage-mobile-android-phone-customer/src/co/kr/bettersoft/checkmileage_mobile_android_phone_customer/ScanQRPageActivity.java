package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 스켄 페이지

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScanQRPageActivity extends Activity {
	SharedPreferences sharedPrefCustom;
	
	String controllerName = "";
	String methodName = "";
	String qrcode = "";
		
	String phoneNumber = "";
	// 시간 관련
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	
	
	static int qrResult = 0;
	public static final String TAG = ScanQRPageActivity.class.getSimpleName();
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_qr_page);
		
		Intent rIntent = getIntent();
        phoneNumber = rIntent.getStringExtra("phoneNumber");
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

	// QR 코드 저장소에 QR 코드를 저장한다.  --> 사용 안함
//	public void saveQR(){		
//		CommonUtils.callCode = 32;		// 쓰기 모드
//		Intent saveQRintent = new Intent(ScanQRPageActivity.this, CommonUtils.class);			// 호출
//		startActivity(saveQRintent);
//	}
	// pref 에 QR 저장 방식. 위에거 대신 쓸것.
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
				// 1. 로컬 파일에 QR 코드를 저장함
				Log.i("ScanQRPageActivity", "save qrcode to file : "+qrcode);
//				CommonUtils.writeQRstr = qrcode;
//				saveQR();	
				saveQRforPref(qrcode);		// 설정에 qr 저장
				saveQRtoServer();			// 서버에 업뎃
				
				// 2. 다음 페이지로 이동. qrCode 에 값 세팅해서 줌.
				Log.i("ScanQRPageActivity", "load qrcode to img : "+qrcode);
				MyQRPageActivity.qrCode = qrcode;

				
				
				new Thread(
						new Runnable(){
							public void run(){
								try{
									Thread.sleep(100);
									Log.i("ScanQRPageActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
									// 나의 QR 코드 보기로 이동.
									Log.i("ScanQRPageActivity", "QR registered Success");
									Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
									startActivity(intent2);
									finish();		// 다른 액티비티를 호출하고 자신은 종료.
								}catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
						}
				).start();
			} else if(resultCode == RESULT_CANCELED) {
				// 취소 또는 실패시 이전화면으로.
				Toast.makeText(ScanQRPageActivity.this, R.string.fail_scan_qr, Toast.LENGTH_SHORT).show();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
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
							
							getNow();
							String tempMonth = Integer.toString(todayMonth);
							String tempDay = Integer.toString(todayDay);
							String tempHour = Integer.toString(todayHour);
							String tempMinute = Integer.toString(todayMinute);
							if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
							if(tempDay.length()==1) tempDay = "0"+tempDay;
							if(tempHour.length()==1) tempHour = "0"+tempHour;
							if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
							String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute;
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
								Log.e(TAG, "register user F");
							}
						}catch(Exception e){ 
							e.printStackTrace();
						}
					}
				}
		).start();
    }
    
    // 현시각
    public void getNow(){
		// 일단 오늘.
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
		todayDay = c.get(Calendar.DATE);
		todayHour = c.get(Calendar.HOUR_OF_DAY);
		todayMinute = c.get(Calendar.MINUTE);
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
}
