package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// �������� ����
/*
 * ���� ȭ��. -- ����. �� ���� ����.
 * 
 * ��ġ�� �̺�Ʈ ������ ���⼭
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.kr.bettersoft.domain.CheckMileageMembers;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MyQRPageActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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


public class Settings_MyInfoPageActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	static String TAG = "Settings_MyInfoPageActivity";
	
	public Boolean resumeCalled = false;
	
	SharedPreferences sharedPrefCustom;		// ���� ������		 ��� �� QR (����� ���ΰ� ����  ���� ��(default,this)�� ���ΰ� �������� �ʾ� �� sharedPref �� ����Ѵ�.)		
	
	SharedPreferences thePrefs;				// ���� �� ��ü �����۷���.  Resume �� �̰��� �����Ͽ� ���(Ż�� �ʱ�ȭ �뵵)-- �̰� ��� �����Ŷ� ����.. ��������.
	
	SharedPreferences defaultPref;			// default --  �̰��� ��ü �����۷���!!. 
	
	Calendar c = Calendar.getInstance();
	
	int todayYear = 0;						// ���� -  �� �� �� �� ��
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	
	int birthYear = 0;						// ������� - �� �� ��
	int birthMonth= 0;
	int birthDay = 0;
	
	int sharePrefsFlag = 1;					// ���ó� ��ü �������� ��� ���� �̳�. 1,-1 ���� �ٲ㰡�� �����ϸ� �����ʰ� ���δ�.
	
	static String controllerName = "";		// JSON ���� ��Ÿ� ��Ʈ�ѷ� ��
	static String methodName = "";			// JSON ���� ��ſ� �޼ҵ� ��
	static int responseCode = 0;			// JSON ���� ��� ���
	
	static int updateLv=0;							// ������ ���� ĥ�� ���� �˻�뵵. 0�̸� ���ϰ�, 1�̸� �Ѵ�, 2�� �ι��Ѵ�(������ ���� �ٲ� �����)
	
	String updateYN = "";
	String server_birthday = "";			// �������� ���� ���� ������ �ʿ��� ������.
	String server_email = "";
	String server_gender = "";
	
	// Locale
    Locale systemLocale = null ;
//    String strDisplayCountry = "" ;
    String strCountry = "" ;
    String strLanguage = "" ;
	
	// �ɸ� ���� ���� ������ ���� ������. �׻� �ֽ� ������ �����ؾ� �Ѵ�.
	static CheckMileageMembers memberInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//		Toast.makeText(Settings_MyInfoPageActivity.this, "Year:"+todayYear+",Month:"+todayMonth+",Day:"+todayDay, Toast.LENGTH_SHORT).show();

		// 1. \res\xml\preferences.xml�� ���� Preference ���������� �о��
		// 2. �� PreferenceActivity�� ���������� ����/ǥ�� �ϰ�
		// 3. \data\data\��Ű���̸�\shared_prefs\��Ű���̸�_preferences.xml ����
		// 4. �� �� Preference�� ���� ������ ����� ���Ͽ� �ڵ� ����
		addPreferencesFromResource(R.xml.settings_myinfo);
		
		/*
		 *  �����κ��� ���� ������ �����ͼ� ������ ���� ���� ��Ƶд�. ���߿� ������Ʈ �Ҷ� ����ؾ� �ϴϱ�. ������Ʈ�ϰ� ���� �� ������ �״�� �����ؾ� �Ѵ�..
		 *  ���°Ŵ� null pointer ���Ƿ� ""�� �ٲ��ִ� ó���� �ʿ��ϴ�.
		 *  ���� ���� ���� ��ũ�� �ִ°� ������...������ ������ �Ŷ��.. 
		 */
		memberInfo = new CheckMileageMembers();
		getUserInfo();
		
		/*
		 * ���� ����� ������ �޾Ƽ� ������ ����
		 */
		Preference pref_user_sex = (Preference)findPreference("pref_user_sex");
		pref_user_sex.setPersistent(true);
		pref_user_sex.setOnPreferenceChangeListener(new OnPreferenceChangeListener()  {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {		// ���� ������ �����մϴ� // ����  ���� �����
				String tmpStrMale = getString(R.string.sex_male);
				String tmpStrFemale = getString(R.string.sex_male);
				String tmpStr = (String) arg1;
				if(tmpStr.equals(tmpStrMale)){
					memberInfo.setGender("MALE");
				}else if(tmpStr.equals(tmpStrFemale)){
					memberInfo.setGender("FEMALE");
				}else{
					memberInfo.setGender("ETC");
				}
				// TODO Auto-generated method stub
//				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// ����� ���� �޾ƿ�.
				
				if(updateLv<2){		// 0�Ǵ� 1�ϰ��. 1 ����. (�ִ� 2����)
					updateLv = updateLv+1;
					if(updateLv==1){
						updateToServer();
					}
				}
				return true;		// ��ü ������ �Ե��� ����. false �϶� ��ü ������ ���� ����.
			}
		});
		/*
		 * ���� �����. ������ ����.
		 */
		Preference pref_user_email = (Preference)findPreference("pref_user_email");
		pref_user_email.setOnPreferenceChangeListener(new OnPreferenceChangeListener()  {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {		// ���� ���� ����.  ����� �Է� ��
				// TODO Auto-generated method stub
				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// ����� ���� �޾ƿ�.
				if(isValidEmail((String) arg1)){
					memberInfo.setEmail((String) arg1);
					if(updateLv<2){		// 0�Ǵ� 1�ϰ��. 1 ����. (�ִ� 2����)
						updateLv = updateLv+1;
						if(updateLv==1){
							updateToServer();
						}
					}
					return true;
				}else{
					Toast.makeText(Settings_MyInfoPageActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
					return false;
				}
				
			}
		});
		
		if(!resumeCalled){			// �ѹ��� ����.. �����ϱ�
			getPreferenceScreen().getSharedPreferences() 
			.registerOnSharedPreferenceChangeListener(this); 

			/*
			 *  ��� ��� ������ ����� ���� ��쿡�� ���ش�.	(����� ���ٸ� ��� üũ ������ ����� �� ����.)		
			 *  ��� ������ ��� ���� ���� ������ �ѹ��ۿ� ���о ����� ������� �ʴ´�. (���� ��⵿�ؾ� ����)
			 */
			// prefs 	// ���� ��� ����. �������� ���͵� �ʿ��ϴ�. 
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// ���⿡�� ����س��� ���ù��� ����� �����Ѵ�.


			// default �� �ѹ� �׽�Ʈ
			defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
			defaultPref.registerOnSharedPreferenceChangeListener(this);			// test ��.. Ȥ�ó�..

			SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// ���� ȣ��뵵  .. �ܾ���� �ǹ̾���.
			int someNum = sharedPrefCustom.getInt("pref_app_leave", sharePrefsFlag);	// ���� ���� ������ �����Ƿ�..
			someNum = someNum * -1;													// �Ź� �ٸ� ���� ���� ����� ȣ���� �ȴ�. ���� ������ ��ȭ ���ٰ� ȣ�� �ȵ�.			
			init2.putInt("pref_app_leave", someNum); 		// �����۷��� �� �־� ������Ʈ ��Ű�� ������ ������ ȣ��.
			init2.commit();			
			// ��ü �����۸� ������ �� �ְ� ��. Ż�� �޼ҵ嶧 �ʱⰪ �������ش�.

			// password �����ϰ� �� ��� ���� �ѹ� ���ֱ�.
			if(updateLv>0){		// 2���� ���. (������ �� ����� ��� �ѹ���)
				Log.d(TAG,"Need Update one more time");
				updateToServer();
			}
//			updateServerSettingsToPrefs();				// ���� ���� ��ü �������� ���� - �׽�Ʈ
			resumeCalled = true;
		}
	}

	public String getNow(){
		// �ϴ� ����.
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
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
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
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
		super.onResume();
//		Log.i(TAG, "onResume");		// yyyyMMdd
		    // Set up a listener whenever a key changes 
	}

	@Override 
	protected void onPause() { 
	    super.onPause(); 
	    // Unregister the listener whenever a key changes 
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

		// �������� ���� - ������� pref_user_birth
		if(preference.equals(findPreference("pref_user_birth"))){
			// ������ ������ ����.
			birthYear = sharedPrefCustom.getInt("birthYear", todayYear);		
			birthMonth = sharedPrefCustom.getInt("birthMonth", todayMonth)-1;		// �����Ҷ� 1 ���ؼ� �־����� �������� 1 ����..	
			birthDay = sharedPrefCustom.getInt("birthDay", todayDay);
//			Log.e(TAG, "preference .. birthYear::"+birthYear+"//birthMonth::"+birthMonth+"//birthDay::"+birthDay);
			DatePickerDialog DatePickerDialog2 = new DatePickerDialog(this, mDateSetListener, birthYear, birthMonth, birthDay);
			DatePickerDialog2.setTitle(R.string.set_birth);		// �޷� Ÿ��Ʋ ����
			DatePickerDialog2.show();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	// �������� - ��¥ ���ý� �����۷����� ����.
	DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			///���������� ���õ� ��¥�� Set�ϴ� ���� ���
//			Toast.makeText(Settings_MyInfoPageActivity.this, year+"."+(monthOfYear+1)+"."+dayOfMonth, Toast.LENGTH_SHORT).show();
			SharedPreferences.Editor birthDate = sharedPrefCustom.edit();
			birthDate.putInt("birthYear", year);
			birthDate.putInt("birthMonth", monthOfYear+1);
			birthDate.putInt("birthDay", dayOfMonth);
			birthDate.commit();
			
			// �������� birthday �� ������Ʈ.
//			String birthday = Integer.toString(year) + Integer.toString(monthOfYear+1) + Integer.toString(dayOfMonth);
			String tempMonth = Integer.toString(monthOfYear+1);
			String tempDay = Integer.toString(dayOfMonth);
			if(tempMonth.length()==1){
				tempMonth = "0"+tempMonth;
			}
			if(tempDay.length()==1){
				tempDay = "0"+tempDay;
			}
			String birthday = Integer.toString(year)+ "-" + tempMonth + "-" + tempDay;
			Log.i(TAG, "birthday::"+birthday);		// yyyyMMdd
			Log.i(TAG, "act::"+memberInfo.getActivateYn());		
			memberInfo.setBirthday(birthday);
			if(updateLv<2){		// 0�Ǵ� 1�ϰ��. 1 ����. (�ִ� 2����)
				updateLv = updateLv+1;
				if(updateLv==1){
					updateToServer();
				}
			}
		}
	};
	
	/*
	 *  �����κ��� ���� ������ �޾ƿͼ� �����ο� ������ �д�. ���߿� ������Ʈ �Ҷ� ����ؾ��ϴϱ�.
	 *  checkMileageMemberController ��/ selectMemberInformation  ��/ checkMileageMember ��/ 
	 *  checkMileageId ��<-qrCode , activateYn : Y  /  CheckMileageMember ���
	 */
	public void getUserInfo(){
		// ...
		Log.i(TAG, "getUserInfo");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";

		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// ����� ���̵� �־ ��ȸ
							obj.put("activateYn", "Y");
							obj.put("checkMileageId", MyQRPageActivity.qrCode);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
							HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes());
							os2.flush();
//							System.out.println("postUrl      : " + postUrl2);
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// ��ȸ�� ����� ó��.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
						}  
					}
				}
		).start();
	}
	
	/*
	 *  ������ ����� ���� ���� ������Ʈ �Ѵ�. �׶��׶� ����� �Ѵ�. ���� �����ο� �����ϰ� ������ ä�� ������Ʈ ģ��. 
	 *    �÷��� ���� �ξ� 0->1�� �ٲٰ� ���� ģ��. 
	 *    1�ϰ�� 2�� �ٲٰ� ���� ��ģ��. 
	 *    2�ϰ�� �ƹ��͵� ���� �ʴ´�. 
	 *    ���� ġ�� ���� 1�� ������ ���� Ȯ�� -> 0�� �ƴ� ��� �ٽ� ���� ģ��.
	 */
	public void updateToServer(){
		Log.i(TAG, "updateToServer");
		controllerName = "checkMileageMemberController";
		methodName = "updateMemberInformation";
		if(updateLv>0){
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// ����� ���� ����.
								obj.put("checkMileageId", memberInfo.getCheckMileageId());
								obj.put("password", memberInfo.getPassword());
								obj.put("phoneNumber", memberInfo.getPhoneNumber());
								obj.put("email", memberInfo.getEmail());
								obj.put("birthday", memberInfo.getBirthday());
								obj.put("gender", memberInfo.getGender());
								obj.put("latitude", memberInfo.getLatitude());
								obj.put("longitude", memberInfo.getLongitude());
								obj.put("deviceType", memberInfo.getDeviceType());
								obj.put("registrationId", memberInfo.getRegistrationId());
								obj.put("activateYn", memberInfo.getActivateYn());
								
								obj.put("countryCode", memberInfo.getCountryCode());
								obj.put("languageCode", memberInfo.getLanguageCode());
								
								obj.put("receiveNotificationYn", memberInfo.getReceiveNotificationYn());
								Log.i(TAG, "activateYn::"+memberInfo.getActivateYn());
								
								String nowTime = getNow();
								Log.i(TAG, "nowTime::"+nowTime);
								obj.put("modifyDate", nowTime);		// ���� �ð�����.
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
							try{
								URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
								HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes());
								os2.flush();
								System.out.println("postUrl      : " + postUrl2);
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
								if(responseCode==200 || responseCode == 204){	// �����̶� ����..
//									theData1(in);		// ���� ��� ������ �� �����ϸ�ȵ� <- �� ��� ���� �޾Ƽ� ó���ϴ� �༮�� ȣ�� ����
									updateLv = updateLv-1;
									if(updateLv>0){		// 2���� ���. (������ �� ����� ��� �ѹ���)
										Log.d(TAG,"Need Update one more time");
										updateToServer();
									}
								}else{
									Log.e(TAG,"fail to update");
								}
							}catch(Exception e){ 
								e.printStackTrace();
							}  
						}
					}
			).start();
		}
		// ...
	}
	
	
	
	/*
	 * �����κ��� �޾ƿ� ���� ������ �Ľ��ؼ� �����ο� �����ϴ� �κ�. ����, Ż�𿡼� ȣ���ϸ� �ȵ�. ��� ������ ��� ���ư�
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		
		// Locale
	      systemLocale = getResources().getConfiguration(). locale;
//      strDisplayCountry = systemLocale.getDisplayCountry();
         strCountry = systemLocale .getCountry();
         strLanguage = systemLocale .getLanguage();
		
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 *  �ϴ� ���� ���� ���⿡ ������
		 *  �� ������::{"checkMileageMember":
		 *  {"checkMileageId":"test1234","password":"","phoneNumber":"01022173645",
		 *  "email":"","birthday":"","gender":"","latitude":"","longitude":"","deviceType":"AS",
		 *  "registrationId":"aaqw","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 *  
		 *   ������Ʈ �� �͵�.  �����ο� ����.
		 *  checkMileageId /password /phoneNumber /email /birthday /gender /latitude /longitude /deviceType /registrationId /activateYn /modifyDate /
		 */
//		Log.d(TAG,"�������� ���� �� ������::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
//				Bitmap bm = null;
				// �����͸� ���� ���� �����ο� �����ϰ� �ڵ鷯�� ���� ������-> ȭ�鿡 �����ش�..
				try{  // ���̵�
//					Log.i(TAG, "checkMileageId:::"+jsonobj2.getString("checkMileageId"));
					memberInfo.setCheckMileageId(jsonobj2.getString("checkMileageId"));				
				}catch(Exception e){ memberInfo.setCheckMileageId(""); }
				try{  // ���
					memberInfo.setPassword(jsonobj2.getString("password"));				
				}catch(Exception e){ memberInfo.setPassword(""); }
				try{  //���� 
					memberInfo.setPhoneNumber(jsonobj2.getString("phoneNumber"));				
				}catch(Exception e){ memberInfo.setPhoneNumber(""); }
				try{	// ��
					memberInfo.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){ memberInfo.setEmail(""); }
				try{	// ����
					memberInfo.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){ memberInfo.setBirthday(""); }
				try{	// ����
					memberInfo.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){ memberInfo.setGender(""); }
				try{	// ����
					memberInfo.setLatitude(jsonobj2.getString("latitude"));				
				}catch(Exception e){ memberInfo.setLatitude(""); }
				try{	// �浵
					memberInfo.setLongitude(jsonobj2.getString("longitude"));				
				}catch(Exception e){ memberInfo.setLongitude(""); }
				try{	// Ÿ��
					memberInfo.setDeviceType(jsonobj2.getString("deviceType"));				
				}catch(Exception e){ memberInfo.setDeviceType(""); }
				try{	// ���ID
					memberInfo.setRegistrationId(jsonobj2.getString("registrationId"));				
				}catch(Exception e){ memberInfo.setRegistrationId(""); }
				try{	// ��Ƽ����Ʈ
					memberInfo.setActivateYn(jsonobj2.getString("activateYn"));				
				}catch(Exception e){ memberInfo.setActivateYn(""); }
				try{	// ������
					memberInfo.setModifyDate(jsonobj2.getString("modifyDate"));				
				}catch(Exception e){ memberInfo.setModifyDate(""); }
				try{	// �˸� ���� ���� 
					memberInfo.setReceiveNotificationYn(jsonobj2.getString("receiveNotificationYn"));				
				}catch(Exception e){ memberInfo.setReceiveNotificationYn(""); }
				try{	// ���� �ڵ�
					memberInfo.setCountryCode(jsonobj2.getString("countryCode"));				
				}catch(Exception e){ memberInfo.setCountryCode(strCountry); }
				try{	// ��� �ڵ�
					memberInfo.setLanguageCode(jsonobj2.getString("languageCode"));				
				}catch(Exception e){ memberInfo.setLanguageCode(strLanguage); }
				// �� �� activateYn �� ���� ����. �̽����� ���� �Ϸ�.
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����. -- toast �� ���� �߻�.
			Log.d(TAG, this.getString(R.string.error_message));
//			Toast.makeText(Settings_MyInfoPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	// ...

	
	// �� �뵵�� resume �� ��ü �����۷��� �����Ͽ� ��Ʈ�� �� �� �ְ� �ϴ� ��.  ���� ���� �����۷��� ������� ��ȯ�� �����ϴ�(�̰� ��������). ����� �����۷��� 3�� �����;;
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("pref_app_leave")){		// resume ���� ���� �Ͱ� �̸� ��ġ�ؾ� �����Ѵ�.
//			Toast.makeText(Settings_MyInfoPageActivity.this, "???"+key, Toast.LENGTH_SHORT).show();
			/*  // �׽�Ʈ��
			Map<String, ?> map = sharedPreferences.getAll();
			Log.e(TAG, "map.size"+map.size());	
			Set set  = map.keySet();
			Iterator ii = set.iterator();
			String iikey="";
			String iivalue="";
			Object obj = null;
			while(ii.hasNext()){
				iikey =( String)ii.next();
				obj = (Object) map.get(iikey);
//				iivalue  = (String)obj;
				Log.e(TAG, iikey+"//"+obj);	
			}
			*/		// �׽�Ʈ��(Ȯ�ο�)
			thePrefs = sharedPreferences;
		}
	}
	
	
//	/**
//	 *  ���뼳������ ���� ���� Ȯ�� ����, y �̸� ��ü ������ ���� ģ��. �ƴϸ� ����.  ������ �Դµ� ���� ���� �Դ´�..
//	 *  
//	 */
//	public void updateServerSettingsToPrefs(){
//		if(updateYN.equals("y")){
//			Log.e(TAG,"���� �ʿ� o");
//			
//			if(server_birthday.length()>8){		// 2012-08-25		0123 4x 56 7x 89
//				String server_birth_year = server_birthday.substring(0,4);
//			    String server_birth_month = server_birthday.substring(5,7);
//			    String server_birth_day = server_birthday.substring(8,10);
//			    Log.e(TAG,"server_birth_year::"+server_birth_year+"//server_birth_month::"+server_birth_month+"//server_birth_day::"+server_birth_day);
//			    int int_server_birth_year = Integer.parseInt(server_birth_year);
//				int int_server_birth_month = Integer.parseInt(server_birth_month);
//				int int_server_birth_day = Integer.parseInt(server_birth_day);
//				SharedPreferences.Editor birthDate = sharedPrefCustom.edit();
//				birthDate.putInt("birthYear", int_server_birth_year);
//				birthDate.putInt("birthMonth", int_server_birth_month);
//				birthDate.putInt("birthDay", int_server_birth_day);
//				Log.e(TAG,"birthYear::"+int_server_birth_year+"//birthMonth::"+int_server_birth_month+"//birthDay::"+int_server_birth_day);
//				birthDate.commit();
//			}else{
//				Log.e(TAG,"server_birthday.length()");
//			}
//			
//			SharedPreferences.Editor updateDone =   sharedPrefCustom.edit();
//			updateDone.putString("updateYN", "n");
//			updateDone.commit();
//			Log.i(TAG,"update settings to mobile done");
//			
//			SharedPreferences.Editor sets = defaultPref.edit();
//			if(server_gender.length()>0){
//				Log.e(TAG,server_gender);
//				ListPreference pref_user_sex = (ListPreference)findPreference("pref_user_sex");
//				pref_user_sex.setValue(server_gender);
//				sets.putString("pref_user_sex", server_gender);
//			}
//			if(server_email.length()>0){
//				Log.e(TAG,server_email);
//				EditTextPreference pref_user_email = (EditTextPreference)findPreference("pref_user_email");
//				pref_user_email.setText(server_email);
//				sets.putString("pref_user_email", server_email);
//			}
//			sets.commit();		// ��ü ������ �ٲٰ� ȭ��󿡼��� �ٲ��ش�.
//			
//		}else{
////			Log.e(TAG,"���� �ʿ� x");
//		}
//	}
	
	/**
     * �̸����ּ� ��ȿ�˻�
     * 
     * @author   Sehwan Noh <sehnoh@gmail.com>
     * @version  1.0 - 2006. 08. 22
     * @since    JDK 1.4
     */
    public static boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$");
        Matcher m = p.matcher(email);
        return m.matches();
    }
	
	@Override			// �� ��Ƽ��Ƽ�� ����ɶ� ����. 
	protected void onDestroy() {
//		resumeCalled = false;
		super.onDestroy();
	}
}