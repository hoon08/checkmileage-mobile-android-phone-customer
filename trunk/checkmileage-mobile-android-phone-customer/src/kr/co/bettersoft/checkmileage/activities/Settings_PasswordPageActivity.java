package kr.co.bettersoft.checkmileage.activities;

/*
 * ���� ȭ��. - ����- ��� ���� ȭ��.
 *    ��� ��� ��� ���� �� ��� ���� �� ����.
 *      �� ��� ���� �� ������ pref �� password �������� ȣ���Ͽ� ����.
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
	
	SharedPreferences sharedPrefCustom;	// ���� ������		 ��� �� QR (����� ���ΰ� ����  ���� ��(default,this)�� ���ΰ� �������� �ʾ� �� sharedPref �� ����Ѵ�.)		
//	PreferenceCategory category1;			// ������ ī�װ�°�� ��Ȱ�� ��ų�� �ִ�. �� ������Ʈ���� ��� ����
	SharedPreferences thePrefs;				// ���� �� ��ü �����۷���.  Resume �� �̰��� �����Ͽ� ���(Ż�� �ʱ�ȭ �뵵)-- �̰� ��� �����Ŷ� ����.. ��������.
	
	int sharePrefsFlag = 1;					// ���ó� ��ü �������� ��� ���� �̳�. 1,-1 ���� �ٲ㰡�� �����ϸ� �����ʰ� ���δ�.
	String password = "";					// ���.
	
	static String controllerName = "";		// JSON ���� ��Ÿ� ��Ʈ�ѷ� ��
	static String methodName = "";			// JSON ���� ��ſ� �޼ҵ� ��
	static int responseCode = 0;			// JSON ���� ��� ���
	
//	static int updateLv=0;							// ������ ���� ĥ�� ���� �˻�뵵. 0�̸� ���ϰ�, 1�̸� �Ѵ�, 2�� �ι��Ѵ�(������ ���� �ٲ� �����)
	    // -> ����� ������ �������� �����Ƿ� �ּ�ó����.
	
	public boolean getprefYN = false;				// �����۷��� ĳġ ����. 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_password);
		/*
		 *  �����κ��� ���� ������ �����ͼ� ������ ���� ���� ��Ƶд�. ���߿� ������Ʈ �Ҷ� ���. ������Ʈ�ϰ� ���� �� ������ �״�� ����..
		 *  ���°Ŵ� null pointer ���Ƿ� ""�� �ٲ��ִ� ó���� �ʿ�.
		 */
		
		if(!resumeCalled){			// �ѹ��� ȣ���ϱ� ����
			getPreferenceScreen().getSharedPreferences() 
			.registerOnSharedPreferenceChangeListener(this); 
			/*
			 *  ��� ��� ������ ����� ���� ��쿡�� ���ش�.	(����� ���ٸ� ��� üũ ������ ����� �� ����.)		
			 *  ��� ������ ��� ���� ���� ������ �ѹ��ۿ� ���о ����� ������� �ʴ´�. (���� ��⵿�ؾ� ����)
			 */
			// prefs 	// ���� ��� ���� �� �����۷���. �������� �� �͵� �ʿ�. 
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// ���⿡�� ����س��� ���ù��� ����� �����Ѵ�.

//			Log.w(TAG, "onResume2");
//			Preference passwordCheck = findPreference("preference_lock_chk");
//			password = sharedPrefCustom.getString("password", "");
//			if(password.length()<1){				// ����� ���ٸ� ��� ����� ����� �� ����.  --> ��� ���� ���� Ǯ����.
//				passwordCheck.setEnabled(false);
//			}else{
//				passwordCheck.setEnabled(true);		// ����� �ִٸ� ��� ����� ����� �� �ִ�.
//			}
			
			SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// ���� ȣ��뵵  .. �ܾ���� �ǹ̾���.
			int someNum = sharedPrefCustom.getInt("pref_toggle", sharePrefsFlag);	// ���� ���� ������ �����Ƿ�..
			someNum = someNum * -1;													// �Ź� �ٸ� ���� ���� ����� ȣ���� �ȴ�. ���� ������ ��ȭ ���ٰ� ȣ�� �ȵ�.			
			init2.putInt("pref_toggle", someNum); 		// �����۷��� �� �־� ������Ʈ ��Ű�� ������ ������ ȣ��.
			init2.commit();			
			// ��ü �����۸� ������ �� �ְ� ��. Ż�� �޼ҵ嶧 �ʱⰪ �������ش�.
//			Log.w(TAG, "onResume3");
			
			// password �� ���� �����ϰ� �� ��� ���� �ѹ� ���ֱ�.?  // -> ��� �� ������ �������� �ʱ��..--> �ּ� ó����
//			if(updateLv>0){		// 2���� ���. (������ �� ����� ��� �ѹ���) --> ���⼭ ����� �ʿ� ����. 
//				Log.d(TAG,"Need Update one more time");
//			}
			resumeCalled = true;
		}
	}

	
	/*
	 * oncreate �� ������ �ѹ��ۿ� ���ؼ� �ι� �̻� �Ϸ��� Resume �� �д�..
	 * ȭ������ �ö����� ����� ������. 
	 * (��� ���� ���� ���ƿ����� ����� ��� ������ �ֵ��� ��)
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
		if(password.length()<1){				// ����� ���ٸ� ��� ����� ����� �� ����.  --> ��� ���� ���� Ǯ����.
			passwordCheck.setEnabled(false);
		}else{
			passwordCheck.setEnabled(true);		// ����� �ִٸ� ��� ����� ����� �� �ִ�.
		}
	}

	@Override 
	protected void onPause() { 
	    super.onPause(); 
	    // Unregister the listener whenever a key changes 			// ������ ����
	    getPreferenceScreen().getSharedPreferences() 
	            .unregisterOnSharedPreferenceChangeListener(this); 
	} 
	 

	// Preference���� Ŭ�� �߻��� ȣ��Ǵ� call back
	// Parameters:
	//  - PreferenceScreen : �̺�Ʈ�� �߻��� Preference�� root
	//  - Preference : �̺�Ʈ�� �߻���Ų Preference �׸�
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		Log.w(TAG, "onPreferenceTreeClick");
		// ��� üũ �Ǵ� ������ �̺�Ʈ �߻�. ���뿡�� ��������ش�. (��ü ������ �⺻ ����)
		if(preference.equals((CheckBoxPreference)findPreference("preference_lock_chk"))) {
//				 Toast.makeText(Settings_PasswordPageActivity.this, "��� ���� ����", Toast.LENGTH_SHORT).show();
				 SharedPreferences.Editor saveLockCustom = sharedPrefCustom.edit();		// �������� ����� ������ �ش�.
				 saveLockCustom.putBoolean("appLocked", ((CheckBoxPreference)findPreference("preference_lock_chk")).isChecked());
				 saveLockCustom. commit();
		}

		// ��� ���� - ��� ���� preference_lock_password
		if(preference.equals(findPreference("preference_lock_password"))){
			//						Toast.makeText(Settings_PasswordPageActivity.this, "preference_lock_password", Toast.LENGTH_SHORT).show();
			Intent passwordIntent = new Intent(Settings_PasswordPageActivity.this, kr.co.bettersoft.checkmileage.pref.Password.class);
			if(password.length()>0){		// ����� �ִ� ��� ������ �ؾ� �Ѵ�. 3ȸ �Է�. (����, ��, Ȯ��)
				passwordIntent.putExtra(Password.PASSWORD, password);		// ���� ����� �����ش�.Ȯ���ؾ� �ϴϱ�
				passwordIntent.putExtra(Password.MODE, Password.MODE_CHANGE_PASSWORD);	// ��� ���� ���
			}else{							// ����� ���� ��� ���� ��� �ű� ������ �ؾ� �Ѵ�. 2ȸ �Է� (��, Ȯ��)
				passwordIntent.putExtra(Password.PASSWORD, password);
				passwordIntent.putExtra(Password.MODE, Password.MODE_INIT_PASSWORD);	// �ű� ��� ���
			}
			// ����� Ȯ�� ����� ��� 
			// passwordIntent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
			passwordIntent.putExtra(Password.NEXT_ACTIVITY, "com.pref.Profile");		// ���� ȭ��
			startActivity(passwordIntent);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	
	// �� �뵵�� resume �� ��ü �����۷��� �����Ͽ� ��Ʈ�� �� �� �ְ� �ϴ� ��.   
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(!getprefYN){				 
			Log.w(TAG, "onSharedPreferenceChanged");
			if(key.equals("pref_toggle")){		// resume ���� ���� �Ͱ� �̸� ��ġ�ؾ� �����Ѵ�.
				thePrefs = sharedPreferences;
			}
			getprefYN = true;				// �����۷��� ĳġ �Ϸ�.
		}
	}
	
	@Override			// �� ��Ƽ��Ƽ�� ����ɶ� ����. 
	protected void onDestroy() {
//		resumeCalled = false;
		super.onDestroy();
	}
}