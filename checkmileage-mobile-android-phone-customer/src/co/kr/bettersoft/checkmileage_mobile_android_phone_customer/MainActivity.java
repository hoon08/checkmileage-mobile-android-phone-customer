package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ��Ʈ��

import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.EXTRA_MESSAGE;
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.SENDER_ID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pref.Password;
import com.pref.PrefActivityFromResource;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.ServerUtilities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/* intro ȭ��
 * ��� : ��Ʈ�� ȭ���� ������.
 * QR �ڵ尡 �ִ��� �˻��Ͽ�
 *  QR�ڵ尡 ������ ���νø���� �̵�(���� �ø��� �� ùȭ��)
 *  QR�ڵ尡 ���ٸ� QR ���� �������� �̵��Ͽ� �ű� ���� �Ǵ� �ִ� �� ���. �� ���νø���� �̵�.
 *  
 */

public class MainActivity extends Activity {
	String TAG = "MainActivity";
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();
			}catch(Exception e){
				Toast.makeText(MainActivity.this, "������ �߻��Ͽ����ϴ�.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	// �� QR �ڵ�
	static String myQR = "";
	// QR ������̿� ���.
	static int qrResult = 0;

	// ���� ���� �����  --> QR �ڵ嵵 �����ϴ°ɷ�..
	String strForLog = "";
	public static final String KEY_PREF_STRING_TEST01 = "String test 01";
	public static final String KEY_PREF_STRING_TEST02 = "String test 02";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;

	public static Boolean loginYN = false;
	
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// ��Ͼ��̵�


	int waitEnd = 0;		// test GCM ����

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i("MainActivity", "Success Starting MainActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		

		// prefs �� �о ��� �Է� â�� ����� ���θ� �����Ѵ�.. ���Ⱑ ù �������ϱ� ���⼭ �Ѵ�.. ��...
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getBoolean("appLocked", false), Toast.LENGTH_SHORT).show();	
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getString("password", ""), Toast.LENGTH_SHORT).show();	
		Boolean locked = sharedPrefCustom.getBoolean("appLocked", false);
		// ��� ���� ����
		if(locked&&(!loginYN)){
//			Toast.makeText(MainActivity.this, "locked", Toast.LENGTH_SHORT).show();	
			Intent intent = new Intent(MainActivity.this, Password.class);
        	// ��� ���� ��Ƽ��Ƽ ����(��)
        	intent.putExtra(Password.NEXT_ACTIVITY, "co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MainActivity");
        	// ���� ȭ�� ��� ����
            intent.putExtra(Password.PASSWORD, sharedPrefCustom.getString("password", "1234"));
            // ��� �Է� ���
            intent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
            startActivity(intent);   
            finish();
        // ��� ���� ����
		}else{
//			Toast.makeText(MainActivity.this, "opened", Toast.LENGTH_SHORT).show();	
			loginYN = false;		// ���ܼ�. �ٽ� ������ �� �߶��
			nextProcessing();
		}
	}

	public void nextProcessing(){
		////////////////////////////////////////////GCM ����        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);

		Log.e(TAG, "registerReceiver1 ");
		final String regId = GCMRegistrar.getRegistrationId(this);
		final Context context = this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				boolean registered =
					ServerUtilities.register(context, regId);
				if (!registered) {
					GCMRegistrar.unregister(context);
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				mRegisterTask = null;
			}
		};
		mRegisterTask.execute(null, null, null);
		try{
			registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// ���ù� ���.
		}catch(Exception e){
			e.printStackTrace();
		}

		GCMRegistrar.register(this, SENDER_ID);
		reg();		// õõ�� ����Ѵ�. (GCM ������ ����� �ð��� �ʿ�..)
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        

		// ���� ���� ���� QR �� ���� ���� ���... ���� ���� �� ���.
		//readQRFromPref();

		///////////////////        // QR ������ ��� ���.. //////////////////////////////////////////////////////////////////////////////
		readQR();			// �ϴ� ����� QR ���� �ִ������� Ȯ�� �Ѵ�.. �ִٸ� ���������� �ʴ´�..
		//saveQR();			// QR �ڵ� ����ҿ� �ӽ� �� ����. (�׽�Ʈ��.) 
		//initialQR();		// QR �ڵ� ����ҿ� �� �ʱ�ȭ. (�׽�Ʈ��.)
		//Log.i("MainActivity", "qrResult::"+qrResult);		// ���� ��� �ޱ� ���̱� ������ ���⼭ Ȯ�� �Ұ�.. �Ʒ� thread ���ο��� Ȯ�� ����.
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////            
		//testJson_log();			// JSON ����Ͽ� �α� ����� �׽�Ʈ�ϱ�.

		// ����. �˻縦 ���� ���� ���� ����.
		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(500);		// �ʱ� �ε� �ð�.
							// ��ݱ�� ���� ��� �Է� �������� �̵�.. // �� ���� �̱���.

							/*
							 * QR �� ������ ���� �б� ����� ���.  PREF ������ ����� ���� �̹Ƿ�  ��� ����.
							 */
							Log.i("MainActivity", "qrResult::"+qrResult);		// �б� ��� ����.
							while(qrResult!=1){		// ���� ����� ���� �б� ������(���Ͼ�������. �����ڵ�:-3) --> ���� �����Ѵ�.
								Log.i("MainActivity", "there is no saved file detected.. generate new one.");	
								initialQR();
								Thread.sleep(300);
							}
							//--------------------------------------------------------------------------------------//

							// QR �ڵ尡 �ִٸ� QR ȭ������ �̵��ϰ�, QR �ڵ尡 ���ٸ� QR ��� ȭ������ �̵��Ѵ�.
							if(myQR.length()>0){ // QR�ڵ尡 �ִ��� Ȯ��. ������ �ٷ� �� QR �������� �̵�.
								Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);
								
								Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
								startActivity(intent);
								finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����. 
							}else {				// QR �ڵ尡 ������ ��ġ�� ���� �����ϴ� ���. 
								/*
								 *  ������ ���� ���� ��������� Ȯ���� �ʿ��ϴ�.
								 *  QR ���� ���Ͽ� QR ���� ���� �ÿ��� ���� ��ġ �� ���� �����̹Ƿ� ������ �޾ƾ� �Ѵ�..
								 *  ���� 1�ܰ��� [�޴��� ��ȣ ����]����  ������ ����� �Ͽ� ���� ��ϵ� ��������� Ȯ���� �Ѵ�. (���� ��ϵ� ����ڶ�� ���� ����� QR �ڵ带 �޾Ƽ� �״�� ���) 
								 *  �������� QR ���� ���� ��쿡�� 2�� ����(������ȣ ����) �Ŀ� QR ���� ���� â���� �̵��Ѵ�.
								 *  1�� ������ ���� �������� QR ���� �޾ƿ� ��� ���� 2�ܰ��� [������ȣ Ȯ��] ������ �����ϰ� �� QR���� ȭ������ �̵��Ѵ�. 
								 */
								// ���� ȭ������ �̵��Ѵ�. (���� ���)
								//Log.i("MainActivity", "There is no saved QR code.. Go to Certification");
								//Intent intent = new Intent(MainActivity.this, CertificationStep1.class);


								// QR ���� ���� â���� �̵�. (���� ���� ������ �ӽ� ���..-test �뵵)
								Log.i("MainActivity", "There is no saved QR code.. Go get QR");
								Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);

								// test �� ���� ȭ������..-test �뵵
								//Log.i("MainActivity", "test -> pref..");
								//Intent intent = new Intent(MainActivity.this, com.pref.MainActivity.class);
								startActivity(intent);
								finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
							}
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}



	////////////// ���� ������ �̿��� ���� ���. ���� ������ �е��� �� ����. QR�� ���߿�.. ////////////////////////////
	public void readQRFromPref(){
		strForLog = sharedPrefCustom.getString("qrcode", "");		
		Log.e("prefTest","pref qrcode:"+strForLog);
		myQR = strForLog;
		if(myQR.length()>1){
			qrResult = 1;
			MyQRPageActivity.qrCode = myQR;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////    


	/////////////////////////////////////// ������ �̿��� QR �б�, ����, �ʱ�ȭ //////////////////////////    
	// QR �ڵ� ����ҿ��� QR �ڵ带 �о�´�.
	public void readQR(){
		readQRFromPref();
		//    	CommonUtils.callCode = 1;			// �б� ���.
		//    	Intent getQRintent = new Intent(MainActivity.this, CommonUtils.class);		// ȣ��
		//    	startActivity(getQRintent);
	}

	// QR �ڵ� ����ҿ� QR �ڵ带 �����Ѵ�. (�׽�Ʈ��)
	public void saveQR(){		
		CommonUtils.callCode = 2;		// ���� ���
		Intent saveQRintent = new Intent(MainActivity.this, CommonUtils.class);			// ȣ��
		startActivity(saveQRintent);
	}

	// QR �ڵ� ����ҿ� �ִ� QR �ڵ� ������ �ʱ�ȭ�Ѵ�. (�׽�Ʈ��)
	public void initialQR(){		
		CommonUtils.callCode = 3;		// �ʱ�ȭ ���
		Intent initQRintent = new Intent(MainActivity.this, CommonUtils.class);		// ȣ��
		startActivity(initQRintent);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////


	///////////////////  ������� �ʴ� ... ///////////////////////////////////////////////////////////
	// �ϵ���� �޴� ��ư ������ ���... ��� ���� ���� ����.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// ����Ʈ�� ����� �޴� �뵵�� ȣ���Ͽ��� ��� (� ȣ������ ������ requestCode �� ���) �� ����� �޾Ƽ� ó���ϴ� �κ�. ���� ��� x
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "onActivityResult");
		if(requestCode == 201) {							
			if(resultCode == RESULT_OK) {			
				// ...
			}
		}
	}
	///////////////////////////////////////// GCM ��� �޼ҵ� ///////////////////////////////////////    
	// GCM ���
	public void reg(){
		//    	REGISTRATION_ID = GCMRegistrar.getRegistrationId(this);
		new Thread(
				new Runnable(){
					public void run(){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally{
							checkDoneAndDoGCM();
						}
					}
				}
		).start();
	}
	// ��� ����
	public void unreg(){
		GCMRegistrar.unregister(this);			// delete from server for re reg
	}
	///////////////////////////////////////////////////////////////////////////////////////////////


	/////////////////////////////////// override �ؼ� GCM ���ù� ���� /////////////////////////////////////////////////////////////////
	//    @Override
	//	protected void onPause() {
	//		super.onPause();		
	//	    try{
	//	    	unregisterReceiver(mHandleMessageReceiver);		// ���ù� ����
	//	    }catch (Exception e){}
	//	}

	@Override			// �� ��Ƽ��Ƽ(��Ʈ��)�� ����ɶ� ����. (��Ƽ��Ƽ�� �Ѿ�� �����)
	protected void onDestroy() {
		super.onDestroy();
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try{
			unregisterReceiver(mHandleMessageReceiver);		// ���ù� ����
			Log.e(TAG, "unregisterReceiver ");
		}catch (Exception e){}
		//        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// ���ù� ���.
		//        Log.e(TAG, "registerReceiver and bye");		
		//        GCMRegistrar.onDestroy(this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    


	///////////////////////////////////////// GCM ��� ���� �޼ҵ�� //////////////////////////////////    
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
					getString(R.string.error_config, name));
		}
	}
	private final BroadcastReceiver mHandleMessageReceiver =
		new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			//            mDisplay.append(newMessage + "\n");
//			        	Toast.makeText(MainActivity.this, "(�׽�Ʈ)�޽����� �����Ͽ����ϴ�.", Toast.LENGTH_SHORT).show();		// ���� ��..
		}
	};
	public void testGCM(String registrationId) throws JSONException, IOException {
		Log.i("testGCM", "testGCM");
		JSONObject jsonMember = new JSONObject();
		jsonMember.put("registrationId", registrationId);
		String jsonString = "{\"checkMileageMember\":" + jsonMember.toString() + "}";
		Log.i("testGCM", "jsonMember : " + jsonString);
		try {
			URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageMemberController/testGCM");
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
			//    		  connection2.getInputStream()  -> buffered reader �� �ְ� �д´�.  str �� jsonobject �� �ְ� ������ �̸����� ������..
		} catch (Exception e) {
			// TODO: handle exception
			//   resultGatheringMessage.setResult("FAIL");
			Log.e("testGCM", "Fail to register category.");
		}
	}

	public void checkDoneAndDoGCM(){
		REGISTRATION_ID = GCMRegistrar.getRegistrationId(this);
		if(REGISTRATION_ID.length()<1){
			new Thread(
					new Runnable(){
						public void run(){
							try {
								Log.i("testGCM", "wait..");
								Thread.sleep(1000);
								checkDoneAndDoGCM();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
			).start();
		}else{
			Log.i("testGCM", "now go with : "+REGISTRATION_ID);
//			try {
//				testGCM(REGISTRATION_ID);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////   




	/////////////////////////////////JSON �׽�Ʈ. �α�.. //////////////////////////////////////////////////////////////////////////////////
	// �׽�Ʈ��. ��ũ����Ʈ ���� ȣ�� -  ���ϸ��� �α׸� �����
	public void testJson_log(){
		try {
			jsontest1();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 *  (�׽�Ʈ��). ���ϸ��� �α׸� �����. 
	 *  ������ ����Ҷ����� ������ ��� �Ǵ� �񵿱� ����� ���� ������ ��Ʈ��ũ ������ �߻��Ѵ�. 
	 *  (������� OS ���� ����� ���ع޴°��� �Ⱦ��ϱ� ����)
	 *   �׽�Ʈ ���̱� ������ ��� �Ҷ����� Ǫ�� �˸� �޽����� �޴´�.
	 *    ���� ���������� �ʿ�ÿ��� ����´�. 
	 */
	public void jsontest1() throws JSONException, IOException {
		Log.i("jsontest1", "jsontest1");
		new Thread(
				new Runnable(){
					public void run(){
						HttpClient client = new DefaultHttpClient(); 
						HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); 
						HttpResponse response = null; 
						Log.e(TAG, "1");
						JSONObject obj = new JSONObject();
						try{
							Log.e(TAG, "2");
							obj.put("merchantId", "a1b2");
							obj.put("checkMileageId", "11");
							obj.put("registerDate", "2012-08-08");
							obj.put("viewName", "AE");
						}catch(Exception e){}
						String jsonString = "{\"checkMileageLog\":" + obj.toString() + "}";
						try{
							URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageLogController/registerLog");
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
							InputStream in =  connection2.getInputStream();
							theData(in);
						}catch(Exception e){ 
							e.printStackTrace();
						}  
					}
				}
		).start();
	}
	// JSON ��� ��� �޾Ƽ� ó��
	public void theData(InputStream in){
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		String line =null;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"����::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�... �뵵�� �°� ������ ��.
		//    	try{
		//    		String result = "";
		//    		JSONArray ja = new JSONArray(tempstr);
		//    		for(int i=0; i<ja.length(); i++){
		//    			JSONObject order =ja.getJSONObject(i);
		//    			result+=""
		//    		}
		//    	}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    




}
