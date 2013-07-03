package kr.co.bettersoft.checkmileage.pref;
/**
 * PrefActivityFromResource
 * 
 * 설정 화면. 
 * 
 * 터치시 이벤트 설정도 여기서한다
 */

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.MainActivity;
import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity;
import kr.co.bettersoft.checkmileage.activities.Settings_AboutPageActivity;
import kr.co.bettersoft.checkmileage.activities.myWebView;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;


public class PrefActivityFromResource extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	static String TAG = "PrefActivityFromResource";
	final int GET_USER_INFO = 1101; 
	final int UPDATE_TO_SERVER = 1102; 
	final int UPDATE_GCM_TO_SERVER = 1103; 
	final int UPDATE_LOG_TO_SERVER = 1104; 

	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	// 내 좌표 업뎃용				///////////////////////////////////////////////
	String myLat2;
	String myLon2;
	// 전번(업뎃용)
	String phoneNum = "";
	// qr
	String qrCode = "";
	// GCM 받을지 여부 저장. 메서드용.
	String strYorN = "";
	Boolean yn = false;

	public Boolean resumeCalled = false;  // 처음 열렸는지, 다른 화면 갔다온건지. -- 처음열렸을때만 프리퍼런스 지정하기 위함.
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	/////////////////////////////////////////////////////////////////////////////
	
	SharedPreferences sharedPrefCustom;	// 공용 프립스		 잠금 및 QR (잠금은 메인과 공유  위의 것(default,this)은 메인과 공유되지 않아 이 sharedPref 도 사용한다.)		
	SharedPreferences thePrefs;				// 어플 내 자체 프리퍼런스.  Resume 때 이곳에 연결하여 사용(탈퇴때 초기화 용도)--  
	SharedPreferences defaultPref;			// default --   자체 프리퍼런스. 
	int sharePrefsFlag = 1;					// 어플내 자체 프립스를 얻기 위한 미끼. 1,-1 값을 바꿔가며 저장하면 리스너가 동작한다. - 그때 동작하는 프리퍼런스를 잡는다.

	// 시간관련
	Calendar c = Calendar.getInstance();
	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	int birthYear = 0;						// 생년월일 - 년 월 일
	int birthMonth= 0;
	int birthDay = 0;
	
	// 서버 통신 관련
	String serverName = CommonConstant.serverNames;
	static String controllerName = "";		// JSON 서버 통신명 컨트롤러 명
	static String methodName = "";			// JSON 서버 통신용 메소드 명
	static int responseCode = 0;			// JSON 서버 통신 결과
	URL postUrl2 ;
	HttpURLConnection connection2;
	
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	
	int isUpdating = 0;		// 중복 실행 방지용
	static int updateLv=0;							// 서버에 업뎃 칠지 여부 검사용도. 0이면 안하고, 1이면 한다, 2면 두번한다(업뎃중 값이 바뀐 경우임)

	
	// Locale
	Locale systemLocale = null ;
	//    String strDisplayCountry = "" ;
	String strCountry = "" ;				// 국가 코드
	String strLanguage = "" ;				// 언어 코드

	// 케릭 설정 정보 저장해 놓을 도메인. 항상 최신 정보를 유지해야 한다.
	static CheckMileageMembers memberInfo;
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 핸들러 등록
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
	
			switch (msg.what)
			{
			case GET_USER_INFO : runOnUiThread(new RunnableGetUserInfo());		
			break;
			case UPDATE_TO_SERVER : runOnUiThread(new RunnableUpdateToServer());	
			break;
			case UPDATE_GCM_TO_SERVER : runOnUiThread(new RunnableUpdateGCMToServer());	
			break;
			case UPDATE_LOG_TO_SERVER : runOnUiThread(new RunnableUpdateLogToServer());	
			break;
			default : 
				break;
			}
		}
	};
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		getNow();
		addPreferencesFromResource(R.xml.settings);

		/*
		 *  서버로부터 개인 정보를 가져와서 도메인 같은 곳에 담아둔다. 나중에 업데이트 할때 사용 . 업데이트하고 나면 그 도메인 그대로 유지 ..
		 *  없는거는 null pointer 나므로 ""로 바꿔주는 처리가 필요하다.
		 */
		memberInfo = new CheckMileageMembers();

		
		isUpdating=1;		// onresume 의 것과 충돌하지 않고 하나만 실행되도록..
		handler.sendEmptyMessage(GET_USER_INFO);
		
		if(!resumeCalled){			// 한번만 .. 느리니까
			getPreferenceScreen().getSharedPreferences() 
			.registerOnSharedPreferenceChangeListener(this);		// 리스너 등록 

			/*
			 *  비번 잠금 설정은 비번이 있을 경우에만 해준다.	(비번이 없다면 잠금 체크 설정을 사용할 수 없다.)		
			 *  비번 설정의 경우 리쥼에 넣지 않으면 한번밖에 안읽어서 변경시 적용되지 않는다. (어플 재기동해야 적용)
			 */
			// prefs 	// 어플 잠금 설정. 공용으로 쓸것도 필요하다. 
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// 여기에도 등록해놔야 리시버가 제대로 반응한다.

			// default 도  등록해야 함
			defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
			defaultPref.registerOnSharedPreferenceChangeListener(this);			 


			// 프리퍼런스 동기화 (공유용 - 디폴트 간 동기화)
			SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// 강제 호출용도  .. 단어명은 의미없다.
			int someNum = sharedPrefCustom.getInt("pref_app_hi", sharePrefsFlag);	// 이전 값과 같을수 있으므로..
			someNum = someNum * -1;													// 매번 다른 값이 들어가야 제대로 호출이 된다. 같은 값들어가면 변화 없다고 호출 안됨.			
			init2.putInt("pref_app_hi", someNum); 		// 프리퍼런스 값 넣어 업데이트 시키면 강제로 리스너 호출.
			init2.commit();			
			// 자체 프리퍼를 지목할 수 있게 됨. 탈퇴 메소드때 초기값 세팅해준다.
			resumeCalled = true;
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	// Preference에서 클릭 발생시 호출되는 call back
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
	/**
	 * onPreferenceTreeClick
	 *  프리퍼런스 클릭시 호출되는 콜백메서드이다. 설정 값 변화 또는 설정 메뉴 호출 등이 있다.
	 *
	 * @param preferenceScreen
	 * @param preference
	 * @return
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		// 알림 수신 설정 여부.
		if(preference.equals((CheckBoxPreference)findPreference("preference_alarm_chk"))){
			SharedPreferences.Editor saveGCMCustom = sharedPrefCustom.edit();		// 공용에 저장해 준다.
			yn = ((CheckBoxPreference)findPreference("preference_alarm_chk")).isChecked();
			saveGCMCustom.putBoolean("gcmReceive", yn);
			saveGCMCustom.commit();
			// 서버에도 업뎃 시켜준다.
			if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
				updateLv = updateLv+1;
				if(updateLv==1){
					handler.sendEmptyMessage(UPDATE_GCM_TO_SERVER );
				}
			}
		}


		// 자주 묻는 질문 등의 경우 인텐트로 웹뷰 실시
		if(preference.equals(findPreference("pref_app_qna"))){
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", "http://www.mcarrot.net/mFaq.do");
			startActivity(webIntent);
		}

		// 공지사항.  pref_app_notify
		if(preference.equals(findPreference("pref_app_notify"))){
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", "http://www.mcarrot.net/mNoticeBoardList.do");
			startActivity(webIntent);
		}

		// 이용 약관..  pref_app_terms
		if(preference.equals(findPreference("pref_app_terms"))){
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", CommonConstant.termsPolicyURL);
			startActivity(webIntent);
		}
		// 개인정보 보호정책..  pref_app_privacy
		if(preference.equals(findPreference("pref_app_privacy"))){
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", CommonConstant.privacyPolicyURL);
			startActivity(webIntent);
		}

		// 이벤트 알림  pref_push_list
		if(preference.equals(findPreference("pref_push_list"))){
			Intent PushListIntent = new Intent(PrefActivityFromResource.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
			startActivity(PushListIntent);

		}

		// 이 앱은 ? pref_app_what
		if(preference.equals(findPreference("pref_app_what"))){
			Intent aboutIntent = new Intent(PrefActivityFromResource.this, Settings_AboutPageActivity.class);
			startActivity(aboutIntent);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	// 주 용도는 resume 때 자체 프리퍼런스 전달하여 컨트롤 할 수 있게 하는 것. 
	/**
	 * onSharedPreferenceChanged
	 *  default 프리퍼런스 변화가 있을때의 콜백 메서드 이다.
	 *   default 프리퍼런스와 공유 preference 간 동기화를 위한 작업을 한다
	 *
	 * @param sharedPreferences
	 * @param key
	 * @return
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("pref_app_hi")){		// resume 에서 넣은 것과 이름 일치해야 동작한다.
			thePrefs = sharedPreferences;
		}
	}
	
	/**
	 * updateServerSettingsToPrefs
	 * 
	 *  공용설정에서 업뎃 여부 확인 이후, y 이면 자체 설정에 업뎃 친다. 아니면 pass.  비번은 해당사항 없다.
	 *  서버에서 받은 설정 정보를 모바일 설정에 저장한다. 업데이트 여부는 updateYN 를 이용한다.
	 *  
	 * @param
	 * @param
	 * @return
	 */
	public void updateServerSettingsToPrefs(){
		Log.d(TAG,"updateServerSettingsToPrefs");
		String updateYN = sharedPrefCustom.getString("updateYN", "N");
		if(updateYN.equals("Y")){		
			Log.w(TAG,"need update o");
			String server_birthday = sharedPrefCustom.getString("server_birthday", "");
			String server_email = sharedPrefCustom.getString("server_email", "");
			String server_gender = sharedPrefCustom.getString("server_gender", "");
			Boolean server_receive_notification_yn = sharedPrefCustom.getBoolean("server_receive_notification_yn", true);			
			// 생년월일은 꺼내서 쪼개서 다시 저장
			if(server_birthday.length()>8){		// 2012-08-25		0123 4x 56 7x 89
				String server_birth_year = server_birthday.substring(0,4);
				String server_birth_month = server_birthday.substring(5,7);
				String server_birth_day = server_birthday.substring(8,10);
				Log.d(TAG,"server_birth_year::"+server_birth_year+"//server_birth_month::"+server_birth_month+"//server_birth_day::"+server_birth_day);
				int int_server_birth_year = Integer.parseInt(server_birth_year);
				int int_server_birth_month = Integer.parseInt(server_birth_month);
				int int_server_birth_day = Integer.parseInt(server_birth_day);
				SharedPreferences.Editor birthDate = sharedPrefCustom.edit();
				birthDate.putInt("birthYear", int_server_birth_year);
				birthDate.putInt("birthMonth", int_server_birth_month);
				birthDate.putInt("birthDay", int_server_birth_day);
				Log.d(TAG,"birthYear::"+int_server_birth_year+"//birthMonth::"+int_server_birth_month+"//birthDay::"+int_server_birth_day);
				birthDate.commit();
			}else{
				Log.d(TAG,"server_birthday.length()");
			}
			SharedPreferences.Editor updateDone =   sharedPrefCustom.edit();
			SharedPreferences.Editor sets = defaultPref.edit();
			if(!server_receive_notification_yn){
				sets.putBoolean("preference_alarm_chk", false);			// 자체 설정 바꿈
				CheckBoxPreference preference_alarm_chk = (CheckBoxPreference)findPreference("preference_alarm_chk");
				preference_alarm_chk.setChecked(false);					// 화면에서도 바꿈
			}			
			if(server_gender!=null && server_gender.length()>0){
				sets.putString("pref_user_sex", server_gender);		// 자체 설정만 바꿈 (화면에서는 하위 페이지에서 바꿈)
			}
			if(server_email!=null && server_email.length()>0){
				sets.putString("pref_user_email", server_email);	// 자체 설정만 바꿈 (화면에서는 하위 페이지에서 바꿈)
			}
			sets.commit();		// 자체 설정 바꿈 저장.
			updateDone.putString("updateYN", "N");		// 폰으로 업뎃 끝 이후. 
			updateDone.putString("updateYN2", "Y");		// 폰으로 업뎃 끝 이후. --> 하위 페이지에서 완료됨.
			updateDone.commit();
			Log.i(TAG,"update settings to mobile step1 done");
		}else{
			//			Log.e(TAG,"업뎃 필요 x");
		}
		isUpdating=0;		// oncreate 흐름도의 끝자락에서 서버 로깅 호출을 위해.. (onresume 의 것은 이후 두번째 onresume 부터 호출되도록..) 
		handler.sendEmptyMessage(UPDATE_LOG_TO_SERVER);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 러너블. 서버로부터 개인 정보를 받아와서 도메인에 저장한다
	 */
	class RunnableGetUserInfo implements Runnable {
		public void run(){
			new backgroundGetUserInfo().execute();
		}
	}
	/**
	 * backgroundGetUserInfo
	 * 비동기로 사용자 정보를 업뎃하는 함수를 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetUserInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetUserInfo");
			// 파리미터 세팅
			CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			checkMileageMembersParam.setCheckMileageId(MyQRPageActivity.qrCode);
			getLocale();
			checkMileageMembersParam.setCountryCode(strCountry);
			checkMileageMembersParam.setLanguageCode(strLanguage);
			// 호출
			callResult = checkMileageCustomerRest.RestGetUserInfo(checkMileageMembersParam);
			// 결과 처리
			if(callResult.equals("S")){ //  성공
				memberInfo = checkMileageCustomerRest.getCheckMileageMembers();
			}else{ 
				Log.d(TAG, returnThis().getString(R.string.error_message));
			}
			return null; 
		}
	}
	

	/**
	 * 러너블. 서버로 변경된 설정 등을 업데이트 한다
	 */
	class RunnableUpdateToServer implements Runnable {
		public void run(){
			new backgroundUpdateToServer().execute();
		}
	}
	/**
	 * backgroundUpdateToServer
	 *  서버로 변경된 설정 등을 업데이트 한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundUpdateToServer extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundUpdateToServer");

			// 파리미터 세팅
			CheckMileageMembers checkMileageMembersParam = memberInfo; 
			// 호출
			callResult = checkMileageCustomerRest.RestUpdateToServer(checkMileageMembersParam);
			// 결과 처리
			if(callResult.equals("S")){	// 성공이라 딱히..
				updateLv = updateLv-1;
				if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더)
					Log.d(TAG,"Need Update one more time");
					handler.sendEmptyMessage(UPDATE_TO_SERVER);
				}
			}else{
				Log.e(TAG,"fail to update");
			}
			return null ;
		}
	}

	
	/**
	 * 러너블. 서버에 알림 수신 설정 값을 업뎃한다
	 */
	class RunnableUpdateGCMToServer implements Runnable {
		public void run(){
			new backgroundUpdateGCMToServer().execute();
		}
	}
	/**
	 * backgroundUpdateGCMToServer
	 *  서버에 알림 수신 설정 값을 업뎃한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundUpdateGCMToServer extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundUpdateGCMToServer");

			// 파리미터 세팅
			if(((CheckBoxPreference)findPreference("preference_alarm_chk")).isChecked()){
				strYorN="Y";
			}else{
				strYorN="N";
			}
			CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers();
			checkMileageMembersParam.setCheckMileageId(memberInfo.getCheckMileageId());
			checkMileageMembersParam.setReceiveNotificationYn(strYorN);
			
			// 호출
			callResult = checkMileageCustomerRest.RestUpdateGCMToServer(checkMileageMembersParam);
			// 결과 처리
			if(callResult.equals("S")){	// 성공이라 딱히..
				Log.d(TAG, "S to receive GCM option update");
				updateLv = updateLv-1;
				if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더)
					Log.d(TAG,"Need Update one more time");
					handler.sendEmptyMessage(UPDATE_GCM_TO_SERVER );
				}
			}else{
				Log.w(TAG,"fail to update");
			}
			return null ;
		}
	}

	
	/**
	 * 러너블. 서버에 위치 및 로그 남김
	 */
	class RunnableUpdateLogToServer implements Runnable {
		public void run(){
			new backgroundUpdateLogToServer().execute();
		}
	}
	/**
	 * 비동기로 사용자의 위치 정보 및 정보 로깅
	 * backgroundUpdateLogToServer
	 */
	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateLogToServer");
			
			if(isUpdating==0){
				isUpdating = 1;
				
				// 파리미터 세팅
				phoneNum = sharedPrefCustom.getString("phoneNum", "");	
				myLat2 = sharedPrefCustom.getString("myLat2", "");	
				myLon2 = sharedPrefCustom.getString("myLon2", "");	
				qrCode = sharedPrefCustom.getString("qrCode", "");		
				CheckMileageLogs checkMileageLogsParam = new CheckMileageLogs();
				checkMileageLogsParam.setCheckMileageId(qrCode);
				checkMileageLogsParam.setParameter01(phoneNum);
				checkMileageLogsParam.setParameter04("");
				checkMileageLogsParam.setViewName("CheckMileageCustomerPreferenceView");
				// 호출
				callResult = checkMileageCustomerRest.RestUpdateLogToServer(checkMileageLogsParam);
//				
				isUpdating = 0;
			}else{
				Log.w(TAG,"already updating..");
			}
			return null; 
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// utils
	
	public Context returnThis(){
		return this;
	}

	public void getLocale(){
		systemLocale = getResources().getConfiguration().locale;
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();
	}
	
	/**
	 * getNow
	 *  현재 시각 구한다
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
		c = Calendar.getInstance();
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
	}

	

	// 준비중입니다.. -> 이벤트 목록 미구현 상태일때 알림 용도 --> 현재는사용하지 않음
	public void AlertShow_Message(){		//R.string.network_error
		AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
		alert_internet_status.setTitle("Carrot");
		alert_internet_status.setMessage(R.string.not_yet);
		String tmpstr = getString(R.string.closebtn);
		alert_internet_status.setPositiveButton(tmpstr, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert_internet_status.show();
	}


	
	/**
	 * onBackPressed
	 *  닫기 버튼 2번 누르면 종료 한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(PrefActivityFromResource.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
			new Thread( 
					new Runnable(){
						public void run(){
							try {
								Thread.sleep(3000);
								app_end = 0;
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
			).start();
		}
	}
	/*
	 * oncreate 에 있으면 한번밖에 못해서 두번 이상 하려면 Resume 에 둔다..
	 * 화면으로 올때마다 비번을 꺼낸다. 
	 * (비번 변경 이후 돌아왔을때 변경된 비번 꺼낼수 있도록 함)  -- 이제 비번 사용 안하므로..
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume(){		
		super.onResume();
		app_end = 0;
		// Set up a listener whenever a key changes 
		getPreferenceScreen().getSharedPreferences() 
		.registerOnSharedPreferenceChangeListener(this); 		// 리스너 등록
		
		if(isUpdating==0){
			handler.sendEmptyMessage(UPDATE_LOG_TO_SERVER);
		}
	}

	@Override 
	protected void onPause() { 
		super.onPause(); 
		// Unregister the listener whenever a key changes 
		getPreferenceScreen().getSharedPreferences() 
		.unregisterOnSharedPreferenceChangeListener(this); 		// 리스너 해제
	} 

	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
		resumeCalled = false;		// 또 불러 주십시오.
		super.onDestroy();
	}
	
}