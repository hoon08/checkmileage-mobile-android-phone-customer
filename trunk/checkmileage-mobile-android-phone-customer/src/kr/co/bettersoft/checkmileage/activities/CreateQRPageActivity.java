package kr.co.bettersoft.checkmileage.activities;
// QR ���� ������
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
 * QR �� �����ϰ� �ٷ� �����ܰ��� ���� QR �ڵ� �����Ƽ��Ƽ�� �Ѿ��.
 * ����ڿ��� �� ��Ƽ��Ƽ�� �������� �ʰ� �ٷ� ���� QR �ڵ庸�� ȭ���� ��Ÿ���� ��1��.
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
	// �ð� ����
	Calendar c = Calendar.getInstance();

	URL postUrl2;
	HttpURLConnection connection2;

	int todayYear = 0;						// ���� -  �� �� �� �� ��
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


	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){				// ȭ�鿡 ���� �佺Ʈ ���
					Toast.makeText(CreateQRPageActivity.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	/**
	 * alertMsg
	 *  ȭ�鿡 error �佺Ʈ ����
	 *
	 * @param alrtmsg
	 * @param
	 * @return
	 */
	public void alertMsg(final String alrtmsg){						// ���� �佺Ʈ �Լ�ȭ
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

		// �ð� -> ������ ���̵�
		Calendar c = Calendar. getInstance();
		String timeID = Long.toString( c.getTimeInMillis());
		Log.e(TAG, "Now to millis : "+ timeID);

		Intent rIntent = getIntent();

		tmpStr = rIntent.getStringExtra("phoneNumber");
		if(tmpStr!=null && tmpStr.length()>0){
			phoneNumber = rIntent.getStringExtra("phoneNumber");
		}
		qrcode = timeID;			// �� ����  �ּ� ó���ϸ� �⺻ �� test1234 ��� - test�뵵. , �ּ� Ǯ�� ���� ���� �ð� ���̵� ���- ���� ��� �뵵.. *** 

		/*
		 *  ������ ����Ͽ� QR ����.
		 */
		// QR �ڵ� ��ü �����ϴ� �κ�..
		// ... QR �ڵ带 �����ϰ�, ������ ����Ѵ�.
		// ���� ���� �ϵ��ڵ� �ؽ�Ʈ �����. --> �����.

		/*
		 * QR ����� ���Ͽ� ����.
		 */
		Log.i("CreateQRPageActivity", "save qrcode to file : "+qrcode);

		new backgroundSaveQRforPref().execute();		// �񵿱� ���� - ������ ���� -- ������ ������ ���� -- ���� �̵��ϴ� �ɷ�..
	}

	
	
	
	public void goMainTabs(){
		/*
		 * MyQR�������� ������ QR�� QR�̹��� �޾Ƽ� ������.
		 */
		Log.i("CreateQRPageActivity", "load qrcode to img : "+qrcode);
		MyQRPageActivity.qrCode = qrcode;
		Main_TabsActivity.myQR = qrcode;

		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(300);
							Log.i("CreateQRPageActivity", "qrResult::"+qrResult);		// �б� ��� ����.
							// ���� QR �ڵ� ����� �̵�.
							Log.i("CreateQRPageActivity", "QR registered Success");
							Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
							startActivity(intent2);
							finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}
	
	
	
	// �񵿱�� ȣ��. ������ ����
	/**
	 * backgroundSaveQRforPref
	 *  �񵿱�� ������ qr �����ϴ� �Լ� ȣ���Ѵ�
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
			saveQRforPref(qrcode);				// ���� ���� �����.
			return null; 
		}
	}
	// pref �� QR ���� ���.
	/**
	 * saveQRforPref
	 *  ������ qr �����Ѵ�
	 *  --���Ͽ��� �����Ѵ�.  20130125
	 * @param qrCode
	 * @param
	 * @return
	 */
	public void saveQRforPref(String qrCode){
		// ������ ����
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
		saveQR.putString("qrcode", qrCode);
		saveQR.commit();

		// ���Ͽ� ����
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
		new backgroundSaveQRtoServer().execute();		// ������ ���� ������ .. �񵿱� ���� - ������ ����
	}

	// �񵿱�� ȣ��. ������ ����
	/**
	 * backgroundSaveQRtoServer
	 * �񵿱�� ������ qr �����ϴ� �Լ� ȣ��
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
			saveQRtoServer();					// �������� ������.			// test1234 ���̵�� �׽�Ʈ�ÿ� �ּ�ó������ ������ ������ �߻��Ѵ�.
			return null; 
		}
	}
	/*
	 *  ������ ������ QR ����
	 *  checkMileageMemberController registerMember 
	 *  
	 *  checkMileageId  password  phoneNumber email birthday  gender  latitude  longitude
	 *  deviceType  registrationId  activateYn  modifyDate  registerDate
	 *  
	 *  checkMileageMember   CheckMileageMember
	 */
	/**
	 * saveQRtoServer
	 *  ������ ������ qr �����Ѵ�
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

		// ���� ��ź�
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
							//							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
							//							os2.close();		// 
							if(responseCode==200||responseCode==204){
								Log.e(TAG, "register user S");
								//								connection2.disconnect();
								
								// ���峡���� ���� ��Ƽ��Ƽ �̵�.
								goMainTabs();
								
							}else{
								Log.e(TAG, "register user F");		// ���� �߻��� ���� â ���� ���ư���.. ��ſ��� �߻��Ҽ� �ִ�.
								String alrtMsg = getString(R.string.error_message);
								alertMsg(alrtMsg);		// toast ���� ���� �߻��ϹǷ� �ڵ鷯 ���� �佺Ʈ
								//								Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
								//								connection2.disconnect();
								Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
								startActivity(backToNoQRIntent);
								finish();
							}
						}catch(Exception e){ 
							//							connection2.disconnect();
							e.printStackTrace();			// ���� �߻��� ���� â ���� ���ư���.. ��ſ��� �߻��Ҽ� �ִ�.
							String alrtMsg = getString(R.string.error_message);
							alertMsg(alrtMsg);		// toast ���� ���� �߻��ϹǷ� �ڵ鷯 ���� �佺Ʈ
							//							 Toast.makeText(CreateQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
							Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
							startActivity(backToNoQRIntent);
							finish();
						}
					}
				}
		).start();
	}

	// ���ð�
	/**
	 * getNow
	 *  ���ð��� �����Ѵ�
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
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
