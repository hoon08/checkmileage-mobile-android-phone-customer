package kr.co.bettersoft.checkmileage.activities;
/**
 * Settings_MyInfoPageActivity
 * 
 * 설정 화면. -- 하위. 내 정보 설정.
 */
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONObject;
//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;

//개인정보 변경
public class Settings_MyInfoPageActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	static String TAG = "Settings_MyInfoPageActivity";
	final int GET_USER_INFO = 1001; 
	final int UPDATE_TO_SERVER = 1002; 


	SharedPreferences sharedPrefCustom;		// 공용 프립스		 잠금 및 QR (잠금은 메인과 공유  위의 것(default,this)은 메인과 공유되지 않아 이 sharedPref 도 사용한다.)		
	SharedPreferences defaultPref;			// default --  자체 프리퍼런스. 
	int sharePrefsFlag = 1;					// 어플내 자체 프립스를 얻기 위한 미끼. 1,-1 값을 바꿔가며 저장하면 리스너가 낚인다.

	// 시간 관련
	Calendar c = Calendar.getInstance();	
	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 1;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	int birthYear = 0;						// 생년월일 - 년 /월/ 일
	int birthMonth= 0;
	int birthDay = 0;

	// 서버 통신 용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;

	static int updateLv=0;							// 서버에 업뎃 칠지 여부 검사용도. 0이면 안하고, 1이면 한다, 2면 두번한다(업뎃중 값이 바뀐 경우이다)
	String updateYN = "";

	String server_email = "";
	String server_gender = "";

	// Locale
	Locale systemLocale = null ;
	String strCountry = "" ;
	String strLanguage = "" ;

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
			default : 
				break;
			}
		}
	};
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_myinfo);
		
		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		/*
		 *  서버로부터 개인 정보를 가져와서 도메인 같은 곳에 담아둔다. 나중에 업데이트 할때 사용. 업데이트하고 나면 그 도메인 그대로 유지..
		 */
		memberInfo = new CheckMileageMembers();
		handler.sendEmptyMessage(GET_USER_INFO);
		updateServerSettingsToPrefs();				// 서버 설정 자체 설정으로 저장 

		/*
		 * 성별 변경시 리스너 달아서 서버에 업뎃
		 */
		Preference pref_user_sex = (Preference)findPreference("pref_user_sex");
		pref_user_sex.setPersistent(true);
		pref_user_sex.setOnPreferenceChangeListener(new OnPreferenceChangeListener()  {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {		// 성별 성별을 선택합니다 // 남성  여성 비공개
				String tmpStrMale = getString(R.string.sex_male);
				String tmpStrFemale = getString(R.string.sex_female);
				String tmpStrEtc = getString(R.string.sex_etc);
				String tmpStr = (String) arg1;
				if(tmpStr.equals(tmpStrMale)){
					memberInfo.setGender("MALE");
				}else if(tmpStr.equals(tmpStrFemale)){
					memberInfo.setGender("FEMALE");
				}else if(tmpStr.equals(tmpStrEtc)){
					memberInfo.setGender("ETC");
				}
				//				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// 변경된 값을 받아옴.
				if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
					updateLv = updateLv+1;
					if(updateLv==1){
						handler.sendEmptyMessage(UPDATE_TO_SERVER);
					}
				}
				return true;		// 자체 설정도 먹도록 해줌. false 일땐 자체 설정이 먹지 않음.
			}
		});

		/*
		 * 메일 변경시. 서버에 업뎃.
		 */
		Preference pref_user_email = (Preference)findPreference("pref_user_email");
		pref_user_email.setOnPreferenceChangeListener(new OnPreferenceChangeListener()  {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {		// 메일 정보 변경.  사용자 입력 값
				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// 변경된 값을 받아옴.
				if(isValidEmail((String) arg1)){
					memberInfo.setEmail((String) arg1);
					if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
						updateLv = updateLv+1;
						if(updateLv==1){
							handler.sendEmptyMessage(UPDATE_TO_SERVER);
						}
					}
					return true;
				}else{
					Toast.makeText(Settings_MyInfoPageActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});

		//		if(!resumeCalled){			// 한번만 실행시키기 위해서 flag 사용
		getPreferenceScreen().getSharedPreferences() 
		.registerOnSharedPreferenceChangeListener(this); 
		/*
		 *  비번 잠금 설정은 비번이 있을 경우에만 해준다.	(비번이 없다면 잠금 체크 설정을 사용할 수 없다.)		
		 *  비번 설정의 경우 리쥼에 넣지 않으면 한번밖에 안읽어서 변경시 적용되지 않는다. (어플 재기동해야 적용)
		 */
		// prefs 	// 어플 잠금 설정. 공용으로 쓸것도 필요 
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// 여기에도 등록해놔야 리시버가 제대로 반응한다.
		// default pref
		defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
		defaultPref.registerOnSharedPreferenceChangeListener(this);			

		SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// 강제 호출용도  .. 단어명은 의미없음.
		int someNum = sharedPrefCustom.getInt("someNum", sharePrefsFlag);	// 이전 값과 같을수 있으므로..
		someNum = someNum * -1;													// 매번 다른 값이 들어가야 제대로 호출이 된다. 같은 값들어가면 변화 없다고 호출 안됨.			
		init2.putInt("someNum", someNum); 		// 프리퍼런스 값 넣어 업데이트 시키면 강제로 리스너 호출.
		init2.commit();			
	}
	//	}

	///////////////////////////////////////////////////////////////////////////////////////////////////	
	// PREFERENCE	


	/**
	 * updateServerSettingsToPrefs
	 *  공용설정에서 업뎃 여부 확인 이후, y 이면 자체 설정에 업뎃 친다. 아니면 말고.  비번은 해당사항 없다.
	 *  설정에서 값을 꺼내어 화면에 세팅한다. 
	 *  (설정에 값이 있다고 화면에 그대로 보여지지 않기 때문에 화면에 보여지도록 처리해줘야 함)
	 *  
	 * @param
	 * @param
	 * @return
	 */
	public void updateServerSettingsToPrefs(){
		if(sharedPrefCustom==null){								// 프리퍼런스
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		}
		updateYN = sharedPrefCustom.getString("updateYN2", "N");		// 설정값 세팅 해야하는지 여부
		if(updateYN.equals("Y")){
			Log.w(TAG,"need update o");
			String server_email = sharedPrefCustom.getString("server_email", "");
			String server_gender = sharedPrefCustom.getString("server_gender", "");
			if(defaultPref==null){
				defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
			}
			if(server_gender.length()>0){			// 성별 설정을 꺼내어 화면에 셋팅.
				Log.d(TAG,server_gender);
				ListPreference pref_user_sex = (ListPreference)findPreference("pref_user_sex");
				if (server_gender.equals("MALE" )){
					pref_user_sex.setValue(getString(R.string.sex_male));
				} else if (server_gender.equals("FEMALE")){
					pref_user_sex.setValue(getString(R.string.sex_female));
				} else if (server_gender.equals("ETC")){
					pref_user_sex.setValue(getString(R.string.sex_etc));
				}
			}
			if(server_email.length()>0){			// 이메일 설정을 꺼내어 화면에 셋팅.
				Log.d(TAG,server_email);
				EditTextPreference pref_user_email = (EditTextPreference)findPreference("pref_user_email");
				pref_user_email.setText(server_email);
			}
			SharedPreferences.Editor updateDone =   sharedPrefCustom.edit();
			updateDone.putString("updateYN2", "N");
			updateDone.commit();
			Log.i(TAG,"update settings to mobile step2 done");
		}else{
			//			Log.e(TAG,"업뎃 필요 x");
		}
	}

	// Preference에서 클릭 발생시 호출되는 call back    -- 해당 설정창.
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
	/**
	 * onPreferenceTreeClick
	 *  Preference에서 클릭 발생시 호출된다. 생년월일 변경 메뉴로 들어가면 기존 생일 또는 오늘 날짜를 기본값으로 날짜피커를 보여준다.
	 *
	 * @param preferenceScreen
	 * @param preference
	 * @return
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		// 개인정보 변경 - 생년월일 pref_user_birth
		if(preference.equals(findPreference("pref_user_birth"))){
			// 꺼내서 없으면 현시각.
			getNow();
			birthYear = sharedPrefCustom.getInt("birthYear", todayYear);		
			birthMonth = sharedPrefCustom.getInt("birthMonth", todayMonth)-1;		// 저장할때 1 더해서 넣었으니 꺼낼때는 1 빼서..	
			birthDay = sharedPrefCustom.getInt("birthDay", todayDay);
			Log.i(TAG, "preference .. birthYear::"+birthYear+"//birthMonth::"+birthMonth+"//birthDay::"+birthDay);
			DatePickerDialog DatePickerDialog2 = new DatePickerDialog(this, mDateSetListener, birthYear, birthMonth, birthDay);
			DatePickerDialog2.setTitle(R.string.set_birth);		// 달력 타이틀 설정
			DatePickerDialog2.show();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	// 개인정보 - 날짜 선택시 프리퍼런스에 저장.
	/**
	 * mDateSetListener
	 *  개인정보 - 날짜 선택시 프리퍼런스에 저장하는 리스너이다
	 *
	 */
	DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			///실제적으로 선택된 날짜를 Set하는 등의 명령
			SharedPreferences.Editor birthDate = sharedPrefCustom.edit();
			birthDate.putInt("birthYear", year);
			birthDate.putInt("birthMonth", monthOfYear+1);
			birthDate.putInt("birthDay", dayOfMonth);
			birthDate.commit();

			// 도메인의 birthday 를 업데이트.
			String tempMonth = Integer.toString(monthOfYear+1);
			String tempDay = Integer.toString(dayOfMonth);
			if(tempMonth.length()==1){
				tempMonth = "0"+tempMonth;
			}
			if(tempDay.length()==1){
				tempDay = "0"+tempDay;
			}
			String birthday = Integer.toString(year)+ "-" + tempMonth + "-" + tempDay;
			Log.i(TAG, "birthday::"+birthday);		// yyyy-MM-dd
			Log.i(TAG, "act::"+memberInfo.getActivateYn());		
			memberInfo.setBirthday(birthday);
			if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
				updateLv = updateLv+1;
				if(updateLv==1){
					handler.sendEmptyMessage(UPDATE_TO_SERVER);
				}
			}
		}
	};

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("someNum")){		// resume 에서 넣은 것과 이름 일치해야 동작한다.
			sharedPrefCustom = sharedPreferences;		// 프리퍼런스 동기화
		}
	}	
	////////////////////////////////////////////////////////////////////////////////////////////////////	

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
	 *  서버로부터 개인 정보를 받아와서 도메인에 저장한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetUserInfo extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) {  }
		@Override protected void onPreExecute() {  }
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundGetUserInfo");

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
			return null ;
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

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * isValidEmail
	 * 이메일주소 유효검사
	 * 
	 * @author   Sehwan Noh <sehnoh@gmail.com>
	 * @version  1.0 - 2006. 08. 22
	 * @since    JDK 1.4
	 * @param email
	 */
	public static boolean isValidEmail(String email) {
		Pattern p = Pattern.compile("^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$");
		Matcher m = p.matcher(email);
		return m.matches();
	}

	public void getLocale(){
		systemLocale = getResources().getConfiguration().locale;
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();
	}

		/**
		 * getNow
		 * 현시각을 구한다
		 *
		 * @param
		 * @param
		 * @return nowTime
		 */
		public String getNow(){
			// 현시각
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

	public Context returnThis(){
		return this;
	}

	@Override 
	protected void onPause() { 
		super.onPause(); 
		// Unregister the listener whenever a key changes 	
		getPreferenceScreen().getSharedPreferences() 		
		.unregisterOnSharedPreferenceChangeListener(this); 
	} 


}