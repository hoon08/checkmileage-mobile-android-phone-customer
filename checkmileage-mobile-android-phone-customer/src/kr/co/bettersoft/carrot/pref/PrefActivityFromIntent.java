// Intent를 이용해 Preference를 구성하는 방법인데 
// 아직 해결이 안되는 critical한 문제점이 있습니다.
// main preference는 정상적으로 표현이 되는데
// dialog를 띄우려고 하면 WindowManager가 BadTokenException을 발생시킵니다.
// 이유와 해결 방법을 아시는 분은 꼭 연락 바랍니다.

package kr.co.bettersoft.carrot.pref;
/*
 * 에러나므로 사용하지 않음. 본 프로젝트에서 사용 안함
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PrefActivityFromIntent extends PreferenceActivity {
	
	SharedPreferences mainPreference;
	PreferenceCategory cat1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		// Intent 사용해 Preference 구성시
		// Preference를 제공하는 Activity는 androidManifest.xml의 <activity> 밑에
		// <meta-data> 테그를 선언 해야 함. androidManefist.xml 참고
		addPreferencesFromIntent(new Intent(this, PrefActivityFromResource.class));
				
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this); 
		cat1 = (PreferenceCategory)findPreference("category1");	
	}
	
	
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if(preference.equals((CheckBoxPreference)findPreference("sub_checkbox"))) {
			cat1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
		}	
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}