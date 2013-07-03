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

import java.util.Calendar;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;

import org.json.JSONObject;

/**
 *  CreateQRPageActivity
 * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
 * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
 */
public class CreateQRPageActivity extends Activity {
	String TAG = "CreateQRPageActivity";
	
	final int SAVE_QR_TO_SERVER = 201; 
	
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	// checkMileageCustomerRest = new CheckMileageCustomerRest();	// oncreate
	
	static int qrResult = 0;
	String qrcode = "";		 
	String phoneNumber = "";
	String tmpStr = "";
	// 시간 관련
	Calendar c = Calendar.getInstance();

	SharedPreferences sharedPrefCustom;

	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;

	// Locale
	Locale systemLocale = null ;
	String strCountry = "" ;
	String strLanguage = "" ;

///////////////////////////////////////////////////////////////////////////////////////////////////
	
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
			
			switch (msg.what)
			{
				case SAVE_QR_TO_SERVER   : runOnUiThread(new RunnableSaveQRtoServer());	
					break;
				default : 
					break;
			}	
			
		}
	};
	
///////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		// 시간 -> 생성할 아이디
		Calendar c = Calendar. getInstance();
		String timeID = Long.toString( c.getTimeInMillis());
//		Log.e(TAG, "Now to millis : "+ timeID);

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

		handler.sendEmptyMessage(SAVE_QR_TO_SERVER);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	

	
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

		// 저장끝나고 나서 액티비티 이동.
		goMainTabs();
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
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * 러너블.서버에 qr 저장
	 */
	class RunnableSaveQRtoServer implements Runnable {
		public void run(){
			new backgroundSaveQRtoServer().execute();
		}
	}
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

			// 파리미터 세팅
			systemLocale = getResources().getConfiguration(). locale;
			strCountry = systemLocale .getCountry();
			strLanguage = systemLocale .getLanguage();
			CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 	
			checkMileageMembersParam.setCheckMileageId(qrcode);
			checkMileageMembersParam.setPhoneNumber(phoneNumber);
			checkMileageMembersParam.setCountryCode(strCountry);
			checkMileageMembersParam.setLanguageCode(strLanguage);
			// 호출
			callResult = checkMileageCustomerRest.RestSaveQRtoServer(checkMileageMembersParam);
			// 결과 처리
			if(callResult.equals("S")){	
				Log.d(TAG, "register user S");
				new backgroundSaveQRforPref().execute();		// 비동기 실행 - 설정에 저장 - 이후 이동하는 걸로..
			}else{														
				Log.e(TAG, "register user F");		// 오류 발생시 에러 창 띄우고 돌아간다.. 통신에러 발생할수 있다.
				String alrtMsg = getString(R.string.error_message);
				alertMsg(alrtMsg);		 
				Intent backToNoQRIntent = new Intent(CreateQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(backToNoQRIntent);
				finish();
			}
			return null; 
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////

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

	///////////////////////////////////////////////////////////////////////////////////
	

}
