package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR ���� ������

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScanQRPageActivity extends Activity {
	SharedPreferences sharedPrefCustom;
	
	String controllerName = "";
	String methodName = "";
	String qrcode = "";
		
	String phoneNumber = "";
	// �ð� ����
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// ���� -  �� �� �� �� ��
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	
	//Locale
	Locale systemLocale = null;
//	String strDisplayCountry = "";
	String strCountry = "";
	String strLanguage = "";
	
	
	String idExist = "";
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
		 * QR ���˸�尡 �Ǿ� QR ī�带 �����Ѵ�.
		 * QR ī�� ������ �����Ͽ� QR ������ ������, �ش� ������ �ۿ� �����ϰ�, ������ ����Ѵ�.
		 * ���� ���� QR �ڵ� ���� ȭ������ �̵��Ѵ�.
		 */

		// QR ī�� �����ϴ� �κ�..
		// ... QR ī�带 �����Ͽ� ������ �����ϰ�, ������ ����Ѵ�.

		// ���� QR �ڵ� ����� �̵�.
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

	// QR �ڵ� ����ҿ� QR �ڵ带 �����Ѵ�.  --> ��� ����
//	public void saveQR(){		
//		CommonUtils.callCode = 32;		// ���� ���
//		Intent saveQRintent = new Intent(ScanQRPageActivity.this, CommonUtils.class);			// ȣ��
//		startActivity(saveQRintent);
//	}
	// pref �� QR ���� ���. ������ ��� ����.
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
			if(resultCode == RESULT_OK) {  // ������
				qrcode = intent.getStringExtra("SCAN_RESULT");
				// 1. ���� ���Ͽ� QR �ڵ带 ������
				Log.i("ScanQRPageActivity", "save qrcode to file : "+qrcode);
//				CommonUtils.writeQRstr = qrcode;
//				saveQR();	
				saveQRforPref(qrcode);		// ������ qr ����
				checkAlreadyExistID();		// ������ ���̵� �ִ��� Ȯ���ؼ� ������ ������Ʈ ����.
//				saveQRtoServer();			// ������ ����			// ����� ���.  ���߿� ���� �ʿ�. �̹� �ִ� ���, ���� ���. ***  --> ���� �Լ��� ���� ó��.
				
				// 2. ���� �������� �̵�. qrCode �� �� �����ؼ� ��.
				Log.i("ScanQRPageActivity", "load qrcode to img : "+qrcode);
				MyQRPageActivity.qrCode = qrcode;

				new Thread(
						new Runnable(){
							public void run(){
								try{
									Thread.sleep(100);
									Log.i("ScanQRPageActivity", "qrResult::"+qrResult);		// �б� ��� ����.
									// ���� QR �ڵ� ����� �̵�.
									Log.i("ScanQRPageActivity", "QR registered Success");
									Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
									startActivity(intent2);
									finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
								}catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
						}
				).start();
			} else if(resultCode == RESULT_CANCELED) {
				// ��� �Ǵ� ���н� ����ȭ������.
				Toast.makeText(ScanQRPageActivity.this, R.string.fail_scan_qr, Toast.LENGTH_SHORT).show();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
	}

	
	/*
	 * ���� ��������� Ȯ��.
	 *  ���̵�� ������ ��ȸ�ؼ� �̹� ��ϵ� ���̵����� Ȯ���Ѵ�.
	 *    �̹� ��ϵ� ���̵��� ��� �߰� ����� �ʿ䰡 ����.
	 */
	public void checkAlreadyExistID(){
		Log.i(TAG, "checkAlreadyExistID");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberExist";
		// ���� ��ź�
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
								checkUserID(in);
							}else{
								 Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
								 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
						}catch(Exception e){ 
							e.printStackTrace();
							 Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
							 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
							 startActivity(backToNoQRIntent);
							 finish();
						}
					}
				}
		).start();
	}
	
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
		/*
		 * checkMileageMerchant":{"merchantId":"m1","password":"m1","name":"����¯","companyName":"������",		///// CheckMileageMember
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"����Ʈ ��Ƽ��","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		Log.d(TAG,"get data ::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				try{
					idExist = jsonobj2.getString("totalCount");				// ���̵� ������1 ������ 0
				}catch(Exception e){
					e.printStackTrace();
					idExist = "0";
				}
				if(idExist.equals("0")){		// ������ ���̵� ������ ������Ʈ ���ش�. ������ ������Ʈ ���� �ʴ´�.
					saveQRtoServer();		
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	}
	
	
	
	 /*
     *  ������ ������ QR ����.
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
		
		systemLocale = getResources().getConfiguration().locale;
//		strDisplayCountry = systemLocale.getDisplayCountry();
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();
		
		// ���� ��ź�
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("checkMileageId", qrcode);			  
							obj.put("password", "");				
							obj.put("phoneNumber", phoneNumber);					// ���� �־����
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
//								theData1(in);
								Log.e(TAG, "register user S");
							}else{
								Log.e(TAG, "register user F");
								Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
								 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
						}catch(Exception e){ 
							e.printStackTrace();
							 Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
							 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
							 startActivity(backToNoQRIntent);
							 finish();
						}
					}
				}
		).start();
    }
    
    // ���ð�
    public String getNow(){
		// �ϴ� ����.
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
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
