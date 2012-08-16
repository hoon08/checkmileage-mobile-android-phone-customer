package com.pref;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PrefActivityFromResource extends PreferenceActivity {
	
	SharedPreferences mainPreference;
	PreferenceCategory category1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 1. \res\xml\preferences.xml로 부터 Preference 계층구조를 읽어와
		// 2. 이 PreferenceActivity의 계층구조로 지정/표현 하고
		// 3. \data\data\패키지이름\shared_prefs\패키지이름_preferences.xml 생성
		// 4. 이 후 Preference에 변경 사항이 생기면 파일에 자동 저장
		addPreferencesFromResource(R.xml.settings);
				
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this); 
		category1 = (PreferenceCategory)findPreference("category1");
		
		// category1의 활성화 비활성화 여부를 Preference 파일 중 "sub_checkbox"의 키 값으로 결정
		category1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
	}
	
	
	
	// Preference에서 클릭 발생시 호출되는 call back
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		// sub_checkbox란 키를 가지고 있는 Preference항목이 이벤트 발생 시 실행 
		if(preference.equals((CheckBoxPreference)findPreference("sub_checkbox"))) {
			// Preference 데이터 파일중 "sub_checkbox" 키와 연결된 boolean 값에 따라
			// category1 (ListPreference, RingtonePreference 포함)을 활성화/비활성화
			category1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	
	
	
	
}