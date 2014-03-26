package kr.co.bettersoft.checkmileage.activities;
/**
 * ScanQRPageActivity
 * 
 *  QR 스켄 페이지
 */
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMemberSettings;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;

import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

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

public class ScanQRPageActivity extends Activity {
	
	public static final String TAG = ScanQRPageActivity.class.getSimpleName();
	final int GET_USER_SETTINGS_FROM_SERVER = 901; 
	final int CHECK_ALREADY_EXIST_ID = 902; 
	final int SAVE_QR_TO_SERVER = 903; 
	
	SharedPreferences sharedPrefCustom;					// 프리퍼런스

	// 서버 통신 용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	
	String qrcode = "";
	String phoneNumber = "";
	String idExist = "";
	static int qrResult = 0;
	
	//Locale
	Locale systemLocale = null;
	String strCountry = "";
	String strLanguage = "";

	// 설정 정보 저장할 도메인.
	CheckMileageMemberSettings settings;

	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 핸들러 등록
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
//			int showQR =  b.getInt("showQR");		// 값을 넣지 않으면 0 을 꺼내었다.

			if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.fail_scan_qr, Toast.LENGTH_SHORT).show();
			}
			if(b.getInt("getUserSetting")==1){		// 서버에서 설정 정보 가져와서 저장
				handler.sendEmptyMessage(GET_USER_SETTINGS_FROM_SERVER);
			}
			if(b.getInt("showIntroView")==1){		// 서버에서 설정 정보 가져와서 저장
				setContentView(R.layout.intro);
			}
			if(b.getInt("showErrToast")==1){
				Toast.makeText(ScanQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			}
			
			switch (msg.what)
			{
				case GET_USER_SETTINGS_FROM_SERVER : runOnUiThread(new RunnableGetUserSettingsFromServer());	
				break;
				case CHECK_ALREADY_EXIST_ID : runOnUiThread(new RunnableCheckAlreadyExistID());	
				break;
				case SAVE_QR_TO_SERVER : runOnUiThread(new RunnableSaveQRtoServer());	
				break;
				default : 
				break;
			}
			
		}
	};
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_qr_page);

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
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
		Intent intent = new Intent(ScanQRPageActivity.this,com.google.zxing.client.android.CaptureActivity.class);
		intent.setPackage("com.google.zxing.client.android");
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * saveQRforPref
	 *  프리퍼런스(설정)에 QR을 저장한다
	 *
	 * @param qrCode
	 * @param
	 * @return
	 */
	public void saveQRforPref(String qrCode){
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
		saveQR.putString("qrcode", qrCode);
		saveQR.commit();
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
	
	/*
	 * 서버에서 받은 설정 정보를 모바일 내 설정 정보에 저장한다.
	 *   EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
	 *   4가지. -> 를 설정 도메인에 저장한 후 설정에서 세팅해준다..
	 */
	/**
	 * setUserSettingsToPrefs
	 *  서버에서 받은 설정 정보를 모바일 내 설정 정보에 저장한다.
	 *
	 * @param
	 * @param
	 * @return
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * onActivityResult
	 *  qr 스켄 정보를 받아 처리한다
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 * @return
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == 0) {
			if(resultCode == RESULT_OK) {  // 성공시
				qrcode = intent.getStringExtra("SCAN_RESULT");
				if(qrcode.contains("http")){			// URL로 읽었을 경우.
					qrcode = qrcode.substring(7);			// 앞의 http:// 을 자른다. (7글자)
				}
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

				handler.sendEmptyMessage(CHECK_ALREADY_EXIST_ID);
			} else if(resultCode == RESULT_CANCELED) {
				// 취소 또는 실패시 이전화면으로.
				showErrMSG();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * 러너블. 기존 사용자인지 확인한다
	 */
	class RunnableCheckAlreadyExistID implements Runnable {
		public void run(){
			new backgroundCheckAlreadyExistID().execute();
		}
	}
	/**
	 * backgroundCheckAlreadyExistID
	 *  비동기로 기존 사용자인지 확인한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundCheckAlreadyExistID extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundCheckAlreadyExistID");
			
			// 파리미터 세팅
			 CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			 checkMileageMembersParam.setCheckMileageId(qrcode);
			// 호출
			callResult = checkMileageCustomerRest.RestCheckAlreadyExistID(checkMileageMembersParam);
			// 결과 처리
			 if(callResult.equals("S")){ //  성공
			     Log.i(TAG, "S");
			     tempstr = checkMileageCustomerRest.getTempstr();
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
							handler.sendEmptyMessage(SAVE_QR_TO_SERVER);
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
		     }else{ //  실패
			     Log.i(TAG, "F");
		     }
			
			return null ;
		}
	}


	/**
	 * 러너블. 서버에서 설정 정보 가져온다
	 */
	class RunnableGetUserSettingsFromServer implements Runnable {
		public void run(){
			new backgroundGetUserSettingsFromServer().execute();
		}
	}
	/**
	 * backgroundGetUserSettingsFromServer
	 *  비동기로 서버에서 설정 정보 가져오는 메서드 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetUserSettingsFromServer extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundGetUserSettingsFromServer");

			// 파리미터 세팅
			 CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			 checkMileageMembersParam.setCheckMileageId(qrcode);
			// 호출
			callResult = checkMileageCustomerRest.RestGetUserSettingsFromServer(checkMileageMembersParam);
			// 결과 처리
			 if(callResult.equals("S")){ //  성공
			     settings = checkMileageCustomerRest.getCheckMileageMemberSettings(); 
			     setUserSettingsToPrefs();		// 설정에 전달 및 저장
		     }else{ 
		    	//  실패
		     }
			return null ;
		}
	}

	
	
	/**
	 * 러너블. qr 을 서버에 저장한다
	 */
	class RunnableSaveQRtoServer implements Runnable {
		public void run(){
			new backgroundSaveQRtoServer().execute();
		}
	}
	/**
	 * backgroundSaveQRtoServer
	 *  비동기로 qr 을 서버에 저장한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundSaveQRtoServer extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundSaveQRtoServer");

			// 파리미터 세팅
			 CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			 checkMileageMembersParam.setCheckMileageId(qrcode);
			 checkMileageMembersParam.setPhoneNumber(phoneNumber);
			 getLocale();
			 checkMileageMembersParam.setCountryCode(strCountry);
			 checkMileageMembersParam.setLanguageCode(strLanguage);
			// 호출
			callResult = checkMileageCustomerRest.RestSaveQRtoServer(checkMileageMembersParam);
			// 결과 처리
			 if(callResult.equals("S")){ //  성공
				 Log.d(TAG, "register user S");
			    goNextPage();
		     }else{ 
		    	 Log.e(TAG, "register user F");
					showErrMSG();
					Intent backToNoQRIntent = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
					startActivity(backToNoQRIntent);
					finish();
		     }
			return null ;
		}
	}
	
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public void getLocale(){
		systemLocale = getResources().getConfiguration().locale;
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();
	}
	
	
	/**
	 * showErrMsg
	 *  화면에 error 토스트 띄운다
	 *
	 * @param
	 * @param
	 * @return
	 */
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

}
