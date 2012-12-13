package kr.co.bettersoft.checkmileage.activities;
// 개인정보 변경

/*
 * 설정 화면. -- 하위. 내 정보 설정.
 * 
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
//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
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

import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;


public class Settings_MyInfoPageActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	static String TAG = "Settings_MyInfoPageActivity";
	
//	public Boolean resumeCalled = false;		// (설정 정보가져와서 저장하는 등의 처리를 )한번만 실행 되도록 하기 위해서. -> 최초 1회 실행 후 true 가 됨.
	
	SharedPreferences sharedPrefCustom;		// 공용 프립스		 잠금 및 QR (잠금은 메인과 공유  위의 것(default,this)은 메인과 공유되지 않아 이 sharedPref 도 사용한다.)		
	
	SharedPreferences defaultPref;			// default --  자체 프리퍼런스. 
	
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
	
	int sharePrefsFlag = 1;					// 어플내 자체 프립스를 얻기 위한 미끼. 1,-1 값을 바꿔가며 저장하면 리스너가 낚인다.
	
	// 서버 통신 용
	String serverName = CommonUtils.serverNames;
	static String controllerName = "";		// JSON 서버 통신명 컨트롤러 명
	static String methodName = "";			// JSON 서버 통신용 메소드 명
	static int responseCode = 0;			// JSON 서버 통신 결과
	
	URL postUrl2;
	HttpURLConnection connection2;
	
	static int updateLv=0;							// 서버에 업뎃 칠지 여부 검사용도. 0이면 안하고, 1이면 한다, 2면 두번한다(업뎃중 값이 바뀐 경우이다)
	
	String updateYN = "";
//	String server_birthday = "";			// 서버에서 받은 설정 파일중 필요한 정보들. 생일,이메일,성별	// 생일은 나누어 저장됨.
	String server_email = "";
	String server_gender = "";
	
	// Locale
    Locale systemLocale = null ;
//    String strDisplayCountry = "" ;
    String strCountry = "" ;
    String strLanguage = "" ;
	
	// 케릭 설정 정보 저장해 놓을 도메인. 항상 최신 정보를 유지해야 한다.
	static CheckMileageMembers memberInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_myinfo);
		/*
		 *  서버로부터 개인 정보를 가져와서 도메인 같은 곳에 담아둔다. 나중에 업데이트 할때 사용. 업데이트하고 나면 그 도메인 그대로 유지..
		 */
		memberInfo = new CheckMileageMembers();
		getUserInfo();
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
				// TODO Auto-generated method stub
//				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// 변경된 값을 받아옴.
				
				if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
					updateLv = updateLv+1;
					if(updateLv==1){
						updateToServer();
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
				// TODO Auto-generated method stub
				Log.e(TAG, "arg0::"+arg0+",,arg1::"+arg1);		// 변경된 값을 받아옴.
				if(isValidEmail((String) arg1)){
					memberInfo.setEmail((String) arg1);
					if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
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
			// 자체 프리퍼를 지목할 수 있게 됨. 탈퇴 메소드때 초기값 세팅해준다.
			
//			resumeCalled = true;
		}
//	}

	
	/**
	 *  공용설정에서 업뎃 여부 확인 이후, y 이면 자체 설정에 업뎃 친다. 아니면 말고.  비번은 해당사항 없다.
	 *  설정에서 값을 꺼내어 화면에 세팅한다. 
	 *  (설정에 값이 있다고 화면에 그대로 보여지지 않기 때문에 화면에 보여지도록 처리해줘야 함)
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
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
	
	@Override
	public void onResume(){		
		super.onResume();
	}

	@Override 
	protected void onPause() { 
	    super.onPause(); 
	    // Unregister the listener whenever a key changes 
	    getPreferenceScreen().getSharedPreferences() 
	            .unregisterOnSharedPreferenceChangeListener(this); 
	} 
	 

	// Preference에서 클릭 발생시 호출되는 call back    -- 해당 설정창.
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
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
	DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			///실제적으로 선택된 날짜를 Set하는 등의 명령
//			Toast.makeText(Settings_MyInfoPageActivity.this, year+"."+(monthOfYear+1)+"."+dayOfMonth, Toast.LENGTH_SHORT).show();
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
					updateToServer();		 // 서버에 업뎃
				}
			}
		}
	};
	
	/*
	 *  서버로부터 개인 정보를 받아와서 도메인에 저장. 나중에 업데이트 할때 사용.
	 *  checkMileageMemberController 컨/ selectMemberInformation  메/ checkMileageMember 도/ 
	 *  checkMileageId 변<-qrCode , activateYn : Y  /  CheckMileageMember 결과
	 */
	public void getUserInfo(){
		Log.i(TAG, "getUserInfo");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";

		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 사용자 아이디를 넣어서 조회
							obj.put("activateYn", "Y");
							obj.put("checkMileageId", MyQRPageActivity.qrCode);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							connection2.connect();		// *** 
							Thread.sleep(200);
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							Thread.sleep(200);
//							System.out.println("postUrl      : " + postUrl2);
//							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
							connection2.disconnect();
						}catch(Exception e){ 
							connection2.disconnect();
							e.printStackTrace();
						}  
					}
				}
		).start();
	}
	
	/*
	 *  서버로 변경된 설정 등을 업데이트 한다. 그때그때 해줘야 한다. 기존 도메인에 세팅하고 도메인 채로 업데이트 친다. 
	 *    플래그 값을 두어 0->1로 바꾸고 업뎃 친다. 
	 *    1일경우 2로 바꾸고 업뎃 안친다. 
	 *    2일경우 아무것도 하지 않는다. 
	 *    업뎃 치고 나서 1을 내리고 나서 확인 -> 0이 아닐 경우 다시 업뎃 친다.
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
								// 사용자 정보 업뎃.
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
								obj.put("modifyDate", nowTime);		// 지금 시간으로.
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);		 
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								connection2.connect();		// *** 
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes("UTF-8"));
								os2.flush();
								System.out.println("postUrl      : " + postUrl2);
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
								if(responseCode==200 || responseCode == 204){	// 성공이라 딱히..
//									theData1(in);		// 서버 결과 가지고 재 세팅하면안됨 <- 는 멤버 정보 받아서 처리하는 녀석임 호출 금지
									updateLv = updateLv-1;
									if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더)
										Log.d(TAG,"Need Update one more time");
										updateToServer();
									}
								}else{
									Log.e(TAG,"fail to update");
								}
								connection2.disconnect();
							}catch(Exception e){ 
								connection2.disconnect();
								e.printStackTrace();
							}  
						}
					}
			).start();
		}
	}
	
	/*
	 * 서버로부터 받아온 개인 정보를 파싱해서 도메인에 저장하는 부분. 업뎃, 탈퇴할때 호출하면 안됨. 멤버 데이터 모두 날아감
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
//		Log.d(TAG,"서버에서 받은 고객 상세정보::"+builder.toString());
		String tempstr = builder.toString();		
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				try{  // 아이디
//					Log.i(TAG, "checkMileageId:::"+jsonobj2.getString("checkMileageId"));
					memberInfo.setCheckMileageId(jsonobj2.getString("checkMileageId"));				
				}catch(Exception e){ memberInfo.setCheckMileageId(""); }
				try{  // 비번
					memberInfo.setPassword(jsonobj2.getString("password"));				
				}catch(Exception e){ memberInfo.setPassword(""); }
				try{  //전번 
					memberInfo.setPhoneNumber(jsonobj2.getString("phoneNumber"));				
				}catch(Exception e){ memberInfo.setPhoneNumber(""); }
				try{	// 멜
					memberInfo.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){ memberInfo.setEmail(""); }
				try{	// 생일
					memberInfo.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){ memberInfo.setBirthday(""); }
				try{	// 성별
					memberInfo.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){ memberInfo.setGender(""); }
				try{	// 위도
					memberInfo.setLatitude(jsonobj2.getString("latitude"));				
				}catch(Exception e){ memberInfo.setLatitude(""); }
				try{	// 경도
					memberInfo.setLongitude(jsonobj2.getString("longitude"));				
				}catch(Exception e){ memberInfo.setLongitude(""); }
				try{	// 타입
					memberInfo.setDeviceType(jsonobj2.getString("deviceType"));				
				}catch(Exception e){ memberInfo.setDeviceType(""); }
				try{	// 등록ID
					memberInfo.setRegistrationId(jsonobj2.getString("registrationId"));				
				}catch(Exception e){ memberInfo.setRegistrationId(""); }
				try{	// 액티베이트
					memberInfo.setActivateYn(jsonobj2.getString("activateYn"));				
				}catch(Exception e){ memberInfo.setActivateYn(""); }
				try{	// 변경일
					memberInfo.setModifyDate(jsonobj2.getString("modifyDate"));				
				}catch(Exception e){ memberInfo.setModifyDate(""); }
				try{	// 알림 수신 여부 
					memberInfo.setReceiveNotificationYn(jsonobj2.getString("receiveNotificationYn"));				
				}catch(Exception e){ memberInfo.setReceiveNotificationYn(""); }
				try{	// 국가 코드
					memberInfo.setCountryCode(jsonobj2.getString("countryCode"));				
				}catch(Exception e){ memberInfo.setCountryCode(strCountry); }
				try{	// 언어 코드
					memberInfo.setLanguageCode(jsonobj2.getString("languageCode"));				
				}catch(Exception e){ memberInfo.setLanguageCode(strLanguage); }
				// 그 외 activateYn 는 수동 조작. 이시점에 저장 완료.
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지. -- toast 는 에러 발생.
			Log.d(TAG, this.getString(R.string.error_message));
		}
	}

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("someNum")){		// resume 에서 넣은 것과 이름 일치해야 동작한다.
		}
	}
	
	
	/**
     * 이메일주소 유효검사
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
	
    @Override
	public void onDestroy(){
		super.onDestroy();
		try{
		connection2.disconnect();
		}catch(Exception e){}
	}
}