package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR ���� ������

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
	// �ð� ����
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// ���� -  �� �� �� �� ��
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
				saveQRtoServer();			// ������ ����
				
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
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
    
    // ���ð�
    public void getNow(){
		// �ϴ� ����.
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
		todayDay = c.get(Calendar.DATE);
		todayHour = c.get(Calendar.HOUR_OF_DAY);
		todayMinute = c.get(Calendar.MINUTE);
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
}
