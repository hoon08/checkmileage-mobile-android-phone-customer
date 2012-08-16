package com.pref;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {	

	public static final int ID_MENU_PREFERENCES_XML = Menu.FIRST+1;
	public static final int ID_MENU_PREFERENCES_INTENT = Menu.FIRST+2;
		
	SharedPreferences defaultSharedPref;
		
	String strForLog = "";
	// 본 프로젝트에서 사용 안함
//	TextView tvGreeting;
//	TextView tvOptional;
//	TextView tvRingtone;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// PreferenceActivity를 사용하지 않는 Preference 구현 시 필요 (onCreate, onPause, onResume 참고)
	////////////////////////////////////////////////////////////////////////////////////////
	public static final String KEY_PREF_STRING_TEST01 = "String test 01";
	public static final String KEY_PREF_STRING_TEST02 = "String test 02";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;
	////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // 본 프로젝트에서 사용 안함
//        tvGreeting = (TextView)findViewById(R.id.GreetingMsg);
//        tvOptional = (TextView)findViewById(R.id.OptionalMsg);
//        tvRingtone = (TextView)findViewById(R.id.Ringtone);
        
        // /data/data/패키지이름/shared_prefs/패키지이름_preferences.xml 파일과
        // 연결되는 SharedPreferences 인터페이스 얻기. 해당 파일이 없다면 안드로이드가 자동으로 생성
        defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this); 
          
        ///////////////////////////////////////////////////////////////////////////////
        // PreferenceActivity를 사용하지 않는 Preference 구현 시 필요 (onPause, onResume 참고)
        ///////////////////////////////////////////////////////////////////////////////
        sharedPrefForThis = getPreferences(Context.MODE_PRIVATE);
//        sharedPrefCustom = getSharedPreferences("MyCustomePref",
//        					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_PRIVATE);
        
        ///////////////////////////////////////////////////////////////////////////////
    }
    
    
    
    @Override
	protected void onPause() {
		super.onPause();		
		///////////////////////////////////////////////////////////////////
		// PreferenceActivity를 사용하지 않는 Preference 구현 시 참고
		///////////////////////////////////////////////////////////////////
		SharedPreferences.Editor editor01 = sharedPrefForThis.edit();
		SharedPreferences.Editor editor02 = sharedPrefCustom.edit();
		
		editor01.putString(KEY_PREF_STRING_TEST01, "getPreferences() 메서드를 사용해 Activity 이름을 따라 자동 생성된 Preference 데이터 파일에 저장된 Text");
		editor02.putString(KEY_PREF_STRING_TEST02, "getSharedPreferences() 메서드르 사용해 개발자가 임의로 생성한 Preference 데이터 파일(MyCustomePref.xml)에 저장된 text");
		
		// **** 중요 ******
		// commit을 실행시키지 않으면 변경된 설정이 저장되지 않을 뿐더러
		// Preference 데이터 파일이 생성되지도 않음
		editor01.commit();
		editor02.commit();
		///////////////////////////////////////////////////////////////////
	}
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
		// App의 DefaultSharedPreference 파일로 부터 환경 설정값 얻기		//-- 본 프로젝트 에서 사용 안함
//		boolean isOptionalMsgShown = defaultSharedPref.getBoolean("GreetingMsg", false);
//		String strOptionalMsg = defaultSharedPref.getString("AdditionalMsg", "<None>");
//		String strColor = defaultSharedPref.getString("TextColor", "FFFFFFFF");
//		String strRingtone = defaultSharedPref.getString("Rington", "<None Selected>");
		
		// Activity의 child view들의 환경 설정값 복원			// -- 본 프로젝트에서 사용 안함
//		tvGreeting.setTextColor((int)Long.parseLong(strColor, 16));
//		tvOptional.setTextColor((int)Long.parseLong(strColor, 16));
//		tvOptional.setVisibility(isOptionalMsgShown? View.VISIBLE : View.INVISIBLE);
//		tvOptional.setText(strOptionalMsg);
//		tvRingtone.setText(strRingtone);		
//		tvRingtone.setTextColor((int)Long.parseLong(strColor, 16));
		
		//////////////////////////////////////////////////////////////////////////
		// PreferenceActivity를 사용하지 않는 Preference 구현 시 참고 
		//////////////////////////////////////////////////////////////////////////
		sharedPrefForThis.getString(KEY_PREF_STRING_TEST01, "Not configured yet");
		sharedPrefCustom.getString(KEY_PREF_STRING_TEST02, "Not configured yet");
    		// test 삼아 로그 확인 - 정상 작동 함.
//		strForLog = sharedPrefForThis.getString(KEY_PREF_STRING_TEST01, "Not configured yet");
//		Log.e("prefTest","1:"+strForLog);
//		strForLog = sharedPrefCustom.getString(KEY_PREF_STRING_TEST02, "Not configured yet");
//		Log.e("prefTest","2:"+strForLog);
		//////////////////////////////////////////////////////////////////////////
		
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		// 값을 가져온다. 오른쪽 파라미터는 디폴트 값.
		boolean check_value = pref.getBoolean("keycheck", false);
		String list_value = pref.getString("keylist", "");
		
		
	}
    
	
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ID_MENU_PREFERENCES_XML, Menu.NONE, "환경설정(XML 리소스)");
		menu.add(Menu.NONE, ID_MENU_PREFERENCES_INTENT, Menu.NONE, "환경설정(Intent)");		// --> 에러 발생함..
		return super.onCreateOptionsMenu(menu);
	}
    
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case ID_MENU_PREFERENCES_XML:
    		// Intent를 사용해 다른 Activity를 실행 (Intent 관련 포스트에서 자세히 다룰 예정
    		startActivity(new Intent(this, PrefActivityFromResource.class));
    		return true;
    	case ID_MENU_PREFERENCES_INTENT:
    		startActivity(new Intent(this, PrefActivityFromIntent.class));
    		return true;
    	default:
    		finish();
    		return super.onOptionsItemSelected(item);
    	}	
	}
}