package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 인트로

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

/* intro 화면
 * 기능 : 인트로 화면을 보여줌.
 * QR 코드가 있는지 검사하여
 *  QR코드가 있으면 메인시리즈로 이동(메인 시리즈 중 첫화면)
 *  QR코드가 없다면 QR 선택 페이지로 이동하여 신규 생성 또는 있는 것 등록. 후 메인시리즈로 이동.
 *  
 */

public class MainActivity extends Activity {
	String TAG = "MainActivity";
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();
			}catch(Exception e){
				Toast.makeText(MainActivity.this, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	// 내 QR 코드
	static String myQR = "";
	// QR 저장소이용 결과.
	static int qrResult = 0;

	// 설정 파일 저장소  --> QR 코드도 저장하는걸로..
	String strForLog = "";
	public static final String KEY_PREF_STRING_TEST01 = "String test 01";
	public static final String KEY_PREF_STRING_TEST02 = "String test 02";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;

	public static Boolean loginYN = false;
	
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// 등록아이디


	int waitEnd = 0;		// test GCM 대기용

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i("MainActivity", "Success Starting MainActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		

		// prefs 를 읽어서 비번 입력 창을 띄울지 여부를 결정한다.. 여기가 첫 페이지니까 여기서 한다.. 음...
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getBoolean("appLocked", false), Toast.LENGTH_SHORT).show();	
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getString("password", ""), Toast.LENGTH_SHORT).show();	
		Boolean locked = sharedPrefCustom.getBoolean("appLocked", false);
		// 잠금 설정 상태
		if(locked&&(!loginYN)){
//			Toast.makeText(MainActivity.this, "locked", Toast.LENGTH_SHORT).show();	
			Intent intent = new Intent(MainActivity.this, Password.class);
        	// 비번 이후 액티비티 설정(나)
        	intent.putExtra(Password.NEXT_ACTIVITY, "co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MainActivity");
        	// 현재 화면 비번 전달
            intent.putExtra(Password.PASSWORD, sharedPrefCustom.getString("password", "1234"));
            // 비번 입력 모드
            intent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
            startActivity(intent);   
            finish();
        // 잠금 해제 상태
		}else{
//			Toast.makeText(MainActivity.this, "opened", Toast.LENGTH_SHORT).show();	
			loginYN = false;		// 문단속. 다시 켰을때 또 뜨라고
			nextProcessing();
		}
	}

	public void nextProcessing(){
		////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
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
			registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// 리시버 등록.
		}catch(Exception e){
			e.printStackTrace();
		}

		GCMRegistrar.register(this, SENDER_ID);
		reg();		// 천천히 등록한다. (GCM 서버에 등록할 시간이 필요..)
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        

		// 설정 파일 통해 QR 등 설정 정보 얻기... 추후 설정 때 사용.
		//readQRFromPref();

		///////////////////        // QR 파일일 경우 사용.. //////////////////////////////////////////////////////////////////////////////
		readQR();			// 일단 저장된 QR 값이 있는지부터 확인 한다.. 있다면 인증을하지 않는다..
		//saveQR();			// QR 코드 저장소에 임시 값 저장. (테스트용.) 
		//initialQR();		// QR 코드 저장소에 값 초기화. (테스트용.)
		//Log.i("MainActivity", "qrResult::"+qrResult);		// 아직 결과 받기 전이기 때문에 여기서 확인 불가.. 아래 thread 내부에서 확인 가능.
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////            
		//testJson_log();			// JSON 통신하여 로그 남기기 테스트하기.

		// 시작. 검사를 통해 다음 진로 결정.
		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(500);		// 초기 로딩 시간.
							// 잠금기능 사용시 비번 입력 페이지로 이동.. // 은 아직 미구현.

							/*
							 * QR 을 파일을 통한 읽기 쓰기시 사용.  PREF 설정을 사용할 예정 이므로  사용 안함.
							 */
							Log.i("MainActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
							while(qrResult!=1){		// 최초 실행시 파일 읽기 실패함(파일없음에러. 에러코드:-3) --> 새로 생성한다.
								Log.i("MainActivity", "there is no saved file detected.. generate new one.");	
								initialQR();
								Thread.sleep(300);
							}
							//--------------------------------------------------------------------------------------//

							// QR 코드가 있다면 QR 화면으로 이동하고, QR 코드가 없다면 QR 등록 화면으로 이동한다.
							if(myQR.length()>0){ // QR코드가 있는지 확인. 있으면 바로 내 QR 페이지로 이동.
								Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);
								
								Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
								startActivity(intent);
								finish();		// 다른 액티비티를 호출하고 자신은 종료. 
							}else {				// QR 코드가 없으면 설치후 최초 실행하는 사람. 
								/*
								 *  기존에 인증 받은 사용자인지 확인이 필요하다.
								 *  QR 저장 파일에 QR 값이 없을 시에는 어플 설치 후 최초 실행이므로 인증을 받아야 한다..
								 *  인증 1단계인 [휴대폰 번호 인증]으로  서버와 통신을 하여 이전 등록된 사용자인지 확인을 한다. (이전 등록된 사용자라면 이전 등록한 QR 코드를 받아서 그대로 사용) 
								 *  서버에도 QR 값이 없을 경우에는 2차 인증(인증번호 인증) 후에 QR 생성 선택 창으로 이동한다.
								 *  1차 인증을 통해 서버에서 QR 값을 받아온 경우 인증 2단계인 [인증번호 확인] 절차를 생략하고 내 QR보기 화면으로 이동한다. 
								 */
								// 인증 화면으로 이동한다. (정상 사용)
								//Log.i("MainActivity", "There is no saved QR code.. Go to Certification");
								//Intent intent = new Intent(MainActivity.this, CertificationStep1.class);


								// QR 생성 선택 창으로 이동. (인증 개발 전까지 임시 사용..-test 용도)
								Log.i("MainActivity", "There is no saved QR code.. Go get QR");
								Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);

								// test 용 설정 화면으로..-test 용도
								//Log.i("MainActivity", "test -> pref..");
								//Intent intent = new Intent(MainActivity.this, com.pref.MainActivity.class);
								startActivity(intent);
								finish();		// 다른 액티비티를 호출하고 자신은 종료.
							}
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}



	////////////// 설정 파일을 이용한 정보 얻기. 설정 정보를 읽도록 할 예정. QR은 나중에.. ////////////////////////////
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


	/////////////////////////////////////// 파일을 이용한 QR 읽기, 쓰기, 초기화 //////////////////////////    
	// QR 코드 저장소에서 QR 코드를 읽어온다.
	public void readQR(){
		readQRFromPref();
		//    	CommonUtils.callCode = 1;			// 읽기 모드.
		//    	Intent getQRintent = new Intent(MainActivity.this, CommonUtils.class);		// 호출
		//    	startActivity(getQRintent);
	}

	// QR 코드 저장소에 QR 코드를 저장한다. (테스트용)
	public void saveQR(){		
		CommonUtils.callCode = 2;		// 쓰기 모드
		Intent saveQRintent = new Intent(MainActivity.this, CommonUtils.class);			// 호출
		startActivity(saveQRintent);
	}

	// QR 코드 저장소에 있는 QR 코드 정보를 초기화한다. (테스트용)
	public void initialQR(){		
		CommonUtils.callCode = 3;		// 초기화 모드
		Intent initQRintent = new Intent(MainActivity.this, CommonUtils.class);		// 호출
		startActivity(initQRintent);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////


	///////////////////  사용하지 않는 ... ///////////////////////////////////////////////////////////
	// 하드웨어 메뉴 버튼 눌렀을 경우... 사용 하지 않을 예정.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// 인텐트를 결과를 받는 용도로 호출하였을 경우 (어떤 호출인지 구분은 requestCode 를 사용) 그 결과를 받아서 처리하는 부분. 아직 사용 x
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
	///////////////////////////////////////// GCM 등록 메소드 ///////////////////////////////////////    
	// GCM 등록
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
	// 등록 해제
	public void unreg(){
		GCMRegistrar.unregister(this);			// delete from server for re reg
	}
	///////////////////////////////////////////////////////////////////////////////////////////////


	/////////////////////////////////// override 해서 GCM 리시버 해제 /////////////////////////////////////////////////////////////////
	//    @Override
	//	protected void onPause() {
	//		super.onPause();		
	//	    try{
	//	    	unregisterReceiver(mHandleMessageReceiver);		// 리시버 해제
	//	    }catch (Exception e){}
	//	}

	@Override			// 이 액티비티(인트로)가 종료될때 실행. (액티비티가 넘어갈때 종료됨)
	protected void onDestroy() {
		super.onDestroy();
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try{
			unregisterReceiver(mHandleMessageReceiver);		// 리시버 해제
			Log.e(TAG, "unregisterReceiver ");
		}catch (Exception e){}
		//        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// 리시버 등록.
		//        Log.e(TAG, "registerReceiver and bye");		
		//        GCMRegistrar.onDestroy(this);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    


	///////////////////////////////////////// GCM 등록 위한 메소드들 //////////////////////////////////    
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
//			        	Toast.makeText(MainActivity.this, "(테스트)메시지가 도착하였습니다.", Toast.LENGTH_SHORT).show();		// 동작 됨..
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
			System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
			//    		  connection2.getInputStream()  -> buffered reader 에 넣고 읽는다.  str 을 jsonobject 에 넣고 도메인 이름으로 꺼낸다..
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




	/////////////////////////////////JSON 테스트. 로그.. //////////////////////////////////////////////////////////////////////////////////
	// 테스트용. 온크레이트 에서 호출 -  마일리지 로그를 남긴다
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
	 *  (테스트용). 마일리지 로그를 남긴다. 
	 *  서버와 통신할때에는 스레드 사용 또는 비동기 통신을 하지 않으면 네트워크 에러가 발생한다. 
	 *  (모바일의 OS 에서 통신이 방해받는것을 싫어하기 때문)
	 *   테스트 용이기 때문에 등록 할때마다 푸쉬 알림 메시지를 받는다.
	 *    정식 버전에서는 필요시에만 날라온다. 
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							InputStream in =  connection2.getInputStream();
							theData(in);
						}catch(Exception e){ 
							e.printStackTrace();
						}  
					}
				}
		).start();
	}
	// JSON 통신 결과 받아서 처리
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
		Log.d(TAG,"수신::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
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
