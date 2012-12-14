package kr.co.bettersoft.checkmileage.activities;

/*
 * 설정 화면. - 하위- 비번 설정 화면.
 *    비번 잠금 기능 설정 및 비번 생성 및 변경.
 *      단 비번 생성 및 변경은 pref 의 password 페이지를 호출하여 진행.
 */
import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.Password;
import android.os.Bundle;


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class Settings_PasswordPageActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	static String TAG = "Settings_PasswordPageActivity";
	
	public Boolean resumeCalled = false;
	
	SharedPreferences sharedPrefCustom;	// 공용 프립스		 잠금 및 QR (잠금은 메인과 공유  위의 것(default,this)은 메인과 공유되지 않아 이 sharedPref 도 사용한다.)		
//	PreferenceCategory category1;			// 설정의 카테고리째로 비활성 시킬수 있다. 본 프로젝트에서 사용 안함
	SharedPreferences thePrefs;				// 어플 내 자체 프리퍼런스.  Resume 때 이곳에 연결하여 사용(탈퇴때 초기화 용도)-- 이건 사실 위에거랑 같음.. 삽질했음.
	
	int sharePrefsFlag = 1;					// 어플내 자체 프립스를 얻기 위한 미끼. 1,-1 값을 바꿔가며 저장하면 리스너가 낚인다.
	String password = "";					// 비번.
	
	static String controllerName = "";		// JSON 서버 통신명 컨트롤러 명
	static String methodName = "";			// JSON 서버 통신용 메소드 명
	static int responseCode = 0;			// JSON 서버 통신 결과
	
//	static int updateLv=0;							// 서버에 업뎃 칠지 여부 검사용도. 0이면 안하고, 1이면 한다, 2면 두번한다(업뎃중 값이 바뀐 경우임)
	    // -> 비번은 서버에 저장하지 않으므로 주석처리함.
	
	public boolean getprefYN = false;				// 프리퍼런스 캐치 여부. 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_password);
		/*
		 *  서버로부터 개인 정보를 가져와서 도메인 같은 곳에 담아둔다. 나중에 업데이트 할때 사용. 업데이트하고 나면 그 도메인 그대로 유지..
		 *  없는거는 null pointer 나므로 ""로 바꿔주는 처리가 필요.
		 */
		
		if(!resumeCalled){			// 한번만 호출하기 위함
			getPreferenceScreen().getSharedPreferences() 
			.registerOnSharedPreferenceChangeListener(this); 
			/*
			 *  비번 잠금 설정은 비번이 있을 경우에만 해준다.	(비번이 없다면 잠금 체크 설정을 사용할 수 없다.)		
			 *  비번 설정의 경우 리쥼에 넣지 않으면 한번밖에 안읽어서 변경시 적용되지 않는다. (어플 재기동해야 적용)
			 */
			// prefs 	// 어플 잠금 설정 용 프리퍼런스. 공용으로 쓸 것도 필요. 
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// 여기에도 등록해놔야 리시버가 제대로 반응한다.

//			Log.w(TAG, "onResume2");
//			Preference passwordCheck = findPreference("preference_lock_chk");
//			password = sharedPrefCustom.getString("password", "");
//			if(password.length()<1){				// 비번이 없다면 잠금 기능을 사용할 수 없다.  --> 비번 설정 이후 풀린다.
//				passwordCheck.setEnabled(false);
//			}else{
//				passwordCheck.setEnabled(true);		// 비번이 있다면 잠금 기능을 사용할 수 있다.
//			}
			
			SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// 강제 호출용도  .. 단어명은 의미없다.
			int someNum = sharedPrefCustom.getInt("pref_toggle", sharePrefsFlag);	// 이전 값과 같을수 있으므로..
			someNum = someNum * -1;													// 매번 다른 값이 들어가야 제대로 호출이 된다. 같은 값들어가면 변화 없다고 호출 안됨.			
			init2.putInt("pref_toggle", someNum); 		// 프리퍼런스 값 넣어 업데이트 시키면 강제로 리스너 호출.
			init2.commit();			
			// 자체 프리퍼를 지목할 수 있게 됨. 탈퇴 메소드때 초기값 세팅해준다.
//			Log.w(TAG, "onResume3");
			
			// password 외 설정 변경하고 온 경우 업뎃 한번 쳐주기.?  // -> 비번 은 서버에 저장하지 않기로..--> 주석 처리함
//			if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더) --> 여기서 사용할 필요 없음. 
//				Log.d(TAG,"Need Update one more time");
//			}
			resumeCalled = true;
		}
	}

	
	/*
	 * oncreate 에 있으면 한번밖에 못해서 두번 이상 하려면 Resume 에 둔다..
	 * 화면으로 올때마다 비번을 꺼낸다. 
	 * (비번 변경 이후 돌아왔을때 변경된 비번 꺼낼수 있도록 함)
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume(){		
		Log.w(TAG, "onResume");
		super.onResume();
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		Preference passwordCheck = findPreference("preference_lock_chk");
		password = sharedPrefCustom.getString("password", "");
		if(password.length()<1){				// 비번이 없다면 잠금 기능을 사용할 수 없다.  --> 비번 설정 이후 풀린다.
			passwordCheck.setEnabled(false);
		}else{
			passwordCheck.setEnabled(true);		// 비번이 있다면 잠금 기능을 사용할 수 있다.
		}
	}

	@Override 
	protected void onPause() { 
	    super.onPause(); 
	    // Unregister the listener whenever a key changes 			// 리스너 해제
	    getPreferenceScreen().getSharedPreferences() 
	            .unregisterOnSharedPreferenceChangeListener(this); 
	} 
	 

	// Preference에서 클릭 발생시 호출되는 call back
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		Log.w(TAG, "onPreferenceTreeClick");
		// 잠금 체크 또는 해제시 이벤트 발생. 공용에도 저장시켜준다. (자체 저장은 기본 제공)
		if(preference.equals((CheckBoxPreference)findPreference("preference_lock_chk"))) {
//				 Toast.makeText(Settings_PasswordPageActivity.this, "잠금 설정 변경", Toast.LENGTH_SHORT).show();
				 SharedPreferences.Editor saveLockCustom = sharedPrefCustom.edit();		// 공용으로 비번도 저장해 준다.
				 saveLockCustom.putBoolean("appLocked", ((CheckBoxPreference)findPreference("preference_lock_chk")).isChecked());
				 saveLockCustom. commit();
		}

		// 잠금 설정 - 비번 변경 preference_lock_password
		if(preference.equals(findPreference("preference_lock_password"))){
			//						Toast.makeText(Settings_PasswordPageActivity.this, "preference_lock_password", Toast.LENGTH_SHORT).show();
			Intent passwordIntent = new Intent(Settings_PasswordPageActivity.this, kr.co.bettersoft.checkmileage.pref.Password.class);
			if(password.length()>0){		// 비번이 있는 경우 변경을 해야 한다. 3회 입력. (기존, 새, 확인)
				passwordIntent.putExtra(Password.PASSWORD, password);		// 기존 비번을 보내준다.확인해야 하니까
				passwordIntent.putExtra(Password.MODE, Password.MODE_CHANGE_PASSWORD);	// 비번 변경 모드
			}else{							// 비번이 없는 경우 변경 대신 신규 생성을 해야 한다. 2회 입력 (새, 확인)
				passwordIntent.putExtra(Password.PASSWORD, password);
				passwordIntent.putExtra(Password.MODE, Password.MODE_INIT_PASSWORD);	// 신규 비번 모드
			}
			// 참고로 확인 모드일 경우 
			// passwordIntent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
			passwordIntent.putExtra(Password.NEXT_ACTIVITY, "com.pref.Profile");		// 다음 화면
			startActivity(passwordIntent);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	
	// 주 용도는 resume 때 자체 프리퍼런스 전달하여 컨트롤 할 수 있게 하는 것.   
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(!getprefYN){				 
			Log.w(TAG, "onSharedPreferenceChanged");
			if(key.equals("pref_toggle")){		// resume 에서 넣은 것과 이름 일치해야 동작한다.
				thePrefs = sharedPreferences;
			}
			getprefYN = true;				// 프리퍼런스 캐치 완료.
		}
	}
	
	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
//		resumeCalled = false;
		super.onDestroy();
	}
}