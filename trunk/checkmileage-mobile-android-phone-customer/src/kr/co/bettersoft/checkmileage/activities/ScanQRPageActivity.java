package kr.co.bettersoft.checkmileage.activities;
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
	// �ð� ����
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// ���� -  �� �� �� �� ��
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
	
	// ���� ���� ������ ������.
	CheckMileageMemberSettings settings;
	
	String idExist = "";
	static int qrResult = 0;
	public static final String TAG = ScanQRPageActivity.class.getSimpleName();
	
	// �ڵ鷯 ���
	Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int showQR =  b.getInt("showQR");		// ���� ���� ������ 0 �� ��������.
    		
    		if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.fail_scan_qr, Toast.LENGTH_SHORT).show();
			}
    		if(b.getInt("getUserSetting")==1){		// �������� ���� ���� �����ͼ� ����
    			new backgroundGetUserSettingsFromServer().execute();     
			}
    		if(b.getInt("showIntroView")==1){		// �������� ���� ���� �����ͼ� ����
    			setContentView(R.layout.intro);
			}
    		if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			}
    	}
    };
    public void showErrMSG(){			// ȭ�鿡 ���� �佺Ʈ ���..
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

	// pref �� QR ���� .
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
				// 1. ���ÿ� QR �ڵ带 ������
				Log.i("ScanQRPageActivity", "save qrcode to file : "+qrcode);
				saveQRforPref(qrcode);		// ������ qr ����
				
				// ȭ�� ��ȯ- ����ȭ�� ��� ��Ʈ�� ȭ���� ������.
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
				
//				checkAlreadyExistID_pre();		// ������ ���̵� �ִ��� Ȯ���ؼ� ������ ����ϰ�,  ������ ���� ������ �����ͼ� ���ÿ� �����Ѵ�.
				checkAlreadyExistID();		// ������ ���̵� �ִ��� Ȯ���ؼ� ������ ����ϰ�,  ������ ���� ������ �����ͼ� ���ÿ� �����Ѵ�.
			} else if(resultCode == RESULT_CANCELED) {
				// ��� �Ǵ� ���н� ����ȭ������.
				showErrMSG();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
	}

	
	/*
	 * ���� ��������� Ȯ��.
	 *  ���̵�� ������ ��ȸ�ؼ� �̹� ��ϵ� ���̵����� Ȯ���Ѵ�.
	 *    �̹� ��ϵ� ���̵��� ��� �߰� ����� �ʿ䰡 ����. ��� ���� ������ �����ͼ� ���ÿ� �����Ѵ�.
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
								checkUserID(in);
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//									CommonUtils.usingNetwork = 0;
//								}
							}else{
								showErrMSG();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
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
//							if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
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
	// ����ڰ� �ִ��� Ȯ���� ����� ó��
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
					idExist = jsonobj2.getString("totalCount");				// ���̵� ������1 ������ 0
				}catch(Exception e){
					e.printStackTrace();
					idExist = "0";
				}
				if(idExist.equals("0")){		// ������ ���̵� ������ ������Ʈ ���ش�. 
//					saveQRtoServer_pre();		
					saveQRtoServer();		
				}else{							// ������ ���̵� ������ ������ �޾ƿͼ� �����ؾ� �Ѵ�.
					Log.d(TAG,"idExist, getSettingsFromServer = T");
					//���� ������ �����ͼ� ���� ��.  �ڵ鷯�� �̿��Ѵ�.
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
	
	// ������������ �̵��Ѵ�. qrCode ���� �����Ѵ�.
	public void goNextPage(){
		Log.i("ScanQRPageActivity", "load qrcode to img : "+qrcode);
		MyQRPageActivity.qrCode = qrcode;
		Main_TabsActivity.myQR = qrcode;
		new Thread(
				new Runnable(){
					public void run(){
						Log.i("ScanQRPageActivity", "qrResult::"+qrResult);		// �б� ��� ����.
						// ���� QR �ڵ� ����� �̵��Ѵ�.
						Log.i("ScanQRPageActivity", "QR registered Success");
						Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
						startActivity(intent2);
						finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� �����Ѵ�.
					}
				}
		).start();
	}
	
	// ��ܿ��� �������� ���� ���� �������� �޼��� ȣ��.
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
	 *     ���� ����(���� ��� ����) ����  
	 *     ���� ������� ��� �����κ��� ����� ���� ������ �����ͼ� ������� ���� ������ ���Խ�Ų��..
	 *     ����� ��� �нǽ� ���� ������ �缳ġ.. �� ���� �޴´�. �׷� ��� �ʱ�ȭ.
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
	public void getUserSettingsFromServer(){		// �����κ��� ���� ������ �޴´�.  ���̵� ���.��絥����.  CheckMileageMember
		Log.d(TAG, "getUserSettingsFromServer");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// �ڽ��� ���̵� �־ ��ȸ
							Log.d(TAG,"getUserSettingsFromServer,QR code:"+qrcode);
							obj.put("checkMileageId", qrcode);		// �ڽ��� ���̵� ����� ��.. ���� ������̴�
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							if(responseCode==200 || responseCode==204){
								// ��ȸ�� ����� ó��.
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
//						if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//							CommonUtils.usingNetwork = 0;
//						}
					}
				}
		).start();
	} 
	// ���� ������ ���ÿ� ����
	public void theMySettingData1(InputStream in){
		Log.d(TAG,"theMySettingData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		settings = new CheckMileageMemberSettings(); // ��ü ����
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"�� ���� ������::"+builder.toString());
		String tempstr = builder.toString();		
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				// �����͸� ���� ���� �����ο� �����ϰ�  ������ ����..
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
				setUserSettingsToPrefs();		// ������ ���� �� ����
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	}
	/*
	 * �������� ���� ���� ������ ����� �� ���� ������ �����Ѵ�.
	 *   EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
	 *   4����. -> �� ���� �����ο� ������ �� �������� �������ش�..
	 */
	public void setUserSettingsToPrefs(){
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveUpdateYn = sharedPrefCustom.edit();		// �������� ����� ������ �ش�.
		saveUpdateYn.putString("updateYN", "Y");
		saveUpdateYn.putString("server_birthday", settings.getBirthday());
		saveUpdateYn.putString("server_email", settings.getEmail());
		saveUpdateYn.putString("server_gender", settings.getGender());
		if(settings.getReceive_notification_yn().equals("N")){		// �ְ� N
			saveUpdateYn.putBoolean("server_receive_notification_yn", false);
		}else{		// ���ų� Y
			saveUpdateYn.putBoolean("server_receive_notification_yn", true);
		}
		saveUpdateYn.commit();
		
		goNextPage();		// ���� �������� �̵�.
	}
	
	
	 /*
     *   ������ ������ QR ���̵� ���.(������ ��ϵǾ����� ������� ȣ��)
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								Log.d(TAG, "register user S");
//								connection2.disconnect();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//									CommonUtils.usingNetwork = 0;
//								}
								goNextPage();				// ���� �������� �̵� 
							}else{
								Log.e(TAG, "register user F");
//								connection2.disconnect();
								showErrMSG();
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//									CommonUtils.usingNetwork = 0;
//								}
								 Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
								 startActivity(backToNoQRIntent);
								 finish();
							}
						}catch(Exception e){ 
//							CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//							if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
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
    
    // ���ð�
    public String getNow(){
		// �ϴ� ����.
    	c = Calendar.getInstance();
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
    
    @Override
	public void onDestroy(){
		super.onDestroy();
//		try{
//		connection2.disconnect();
//		}catch(Exception e){}
	}
}
