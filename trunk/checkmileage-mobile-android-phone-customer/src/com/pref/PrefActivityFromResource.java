package com.pref;
/*
 * 설정 화면. 
 * 
 * 터치시 이벤트 설정도 여기서
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.kr.bettersoft.domain.CheckMileageMembers;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MainActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MemberStoreInfoPage;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MemberStoreListPageActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MyMileagePageActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MyQRPageActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.Settings_AboutPageActivity;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.myWebView;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.Toast;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PrefActivityFromResource extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	static String TAG = "PrefActivityFromResource";
	
	public Boolean resumeCalled = false;
	
	SharedPreferences sharedPrefCustom;	// 공용 프립스		 잠금 및 QR (잠금은 메인과 공유  위의 것(default,this)은 메인과 공유되지 않아 이 sharedPref 도 사용한다.)		
//	PreferenceCategory category1;			// 설정의 카테고리째로 비활성 시킬수 있다. 본 프로젝트에서 사용 안함
//	WebView mWeb;							// 도움말, 공지 등 볼때 사용하는 웹뷰		--> 다른 액티비티 통해 호출함.
	
	SharedPreferences thePrefs;				// 어플 내 자체 프리퍼런스.  Resume 때 이곳에 연결하여 사용(탈퇴때 초기화 용도)-- 이건 사실 위에거랑 같음.. 삽질했음.
	SharedPreferences defaultPref;			// default --  이것이 자체 프리퍼런스!!. 
	
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
	
	int sharePrefsFlag = 1;					// 어플내 자체 프립스를 얻기 위한 미끼. 1,-1 값을 바꿔가며 저장하면 리스너가 낚인다.
	
	static String controllerName = "";		// JSON 서버 통신명 컨트롤러 명
	static String methodName = "";			// JSON 서버 통신용 메소드 명
	static int responseCode = 0;			// JSON 서버 통신 결과
	
	static int updateLv=0;							// 서버에 업뎃 칠지 여부 검사용도. 0이면 안하고, 1이면 한다, 2면 두번한다(업뎃중 값이 바뀐 경우임)
	
	// Locale
    Locale systemLocale = null ;
//    String strDisplayCountry = "" ;
    String strCountry = "" ;
    String strLanguage = "" ;
	
	// GCM 받을지 여부 저장. 메서드용.
	String strYorN = "";
	Boolean yn = false;
	// 케릭 설정 정보 저장해 놓을 도메인. 항상 최신 정보를 유지해야 한다.
	static CheckMileageMembers memberInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getNow();
		//		Toast.makeText(PrefActivityFromResource.this, "Year:"+todayYear+",Month:"+todayMonth+",Day:"+todayDay, Toast.LENGTH_SHORT).show();

		// 1. \res\xml\preferences.xml로 부터 Preference 계층구조를 읽어와
		// 2. 이 PreferenceActivity의 계층구조로 지정/표현 하고
		// 3. \data\data\패키지이름\shared_prefs\패키지이름_preferences.xml 생성
		// 4. 이 후 Preference에 변경 사항이 생기면 파일에 자동 저장
		addPreferencesFromResource(R.xml.settings);
		
		/*
		 *  서버로부터 개인 정보를 가져와서 도메인 같은 곳에 담아둔다. 나중에 업데이트 할때 사용해야 하니까. 업데이트하고 나면 그 도메인 그대로 유지해야 한다..
		 *  없는거는 null pointer 나므로 ""로 바꿔주는 처리가 필요하다.
		 */
		memberInfo = new CheckMileageMembers();
		getUserInfo();
		
		
		if(!resumeCalled){			// 한번만 하자.. 느리니까
			getPreferenceScreen().getSharedPreferences() 
			.registerOnSharedPreferenceChangeListener(this); 

			/*
			 *  비번 잠금 설정은 비번이 있을 경우에만 해준다.	(비번이 없다면 잠금 체크 설정을 사용할 수 없다.)		
			 *  비번 설정의 경우 리쥼에 넣지 않으면 한번밖에 안읽어서 변경시 적용되지 않는다. (어플 재기동해야 적용)
			 */
			// prefs 	// 어플 잠금 설정. 공용으로 쓸것도 필요하다. 
			sharedPrefCustom = getSharedPreferences("MyCustomePref",
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			sharedPrefCustom.registerOnSharedPreferenceChangeListener(this);			// 여기에도 등록해놔야 리시버가 제대로 반응한다.

			// default 도 한번 테스트
			defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
			defaultPref.registerOnSharedPreferenceChangeListener(this);			// test 용.. 혹시나..

			//		category1 = (PreferenceCategory)findPreference("category1");
			Preference passwordCheck = findPreference("preference_lock_chk");

			SharedPreferences.Editor init2 = sharedPrefCustom.edit();		// 강제 호출용도  .. 단어명은 의미없다.
			int someNum = sharedPrefCustom.getInt("pref_app_hi", sharePrefsFlag);	// 이전 값과 같을수 있으므로..
			someNum = someNum * -1;													// 매번 다른 값이 들어가야 제대로 호출이 된다. 같은 값들어가면 변화 없다고 호출 안됨.			
			init2.putInt("pref_app_hi", someNum); 		// 프리퍼런스 값 넣어 업데이트 시키면 강제로 리스너 호출.
			init2.commit();			
			// 자체 프리퍼를 지목할 수 있게 됨. 탈퇴 메소드때 초기값 세팅해준다.

			// password 변경하고 온 경우 업뎃 한번 쳐주기.
			if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더)
				Log.d(TAG,"Need Update one more time");
				updateToServer();
			}
			updateServerSettingsToPrefs();				// 서버 설정 자체 설정으로 저장 - 테스트
			resumeCalled = true;
		}
	}

	public String getNow(){
		// 일단 오늘.
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
	
	/*
	 * oncreate 에 있으면 한번밖에 못해서 두번 이상 하려면 Resume 에 둔다..
	 * 화면으로 올때마다 비번을 꺼낸다. 
	 * (비번 변경 이후 돌아왔을때 변경된 비번 꺼낼수 있도록 함)
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume(){		
		super.onResume();
		app_end = 0;
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

	// Preference에서 클릭 발생시 호출되는 call back
	// Parameters:
	//  - PreferenceScreen : 이벤트가 발생한 Preference의 root
	//  - Preference : 이벤트를 발생시킨 Preference 항목
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		// sub_checkbox란 키를 가지고 있는 Preference항목이 이벤트 발생 시 실행 
		//		if(preference.equals((CheckBoxPreference)findPreference("sub_checkbox"))) {
		// Preference 데이터 파일중 "sub_checkbox" 키와 연결된 boolean 값에 따라
		// category1 (ListPreference, RingtonePreference 포함)을 활성화/비활성화

		// //     테스트용.  체크박스가 체크되었는지 여부를 통해 이벤트 발생..비번 에서는 비번이 있는지 여부로 할것..
		//			category1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
		//			if(((CheckBoxPreference)findPreference("sub_checkbox")).isChecked()){
		//				// 조건에 따라 특정 항목을 dis enable 할수 있다.
		//				Preference pref = findPreference("pref_app_qna");
		//				pref.setEnabled(false);
		//			}else{
		//				Preference pref = findPreference("pref_app_qna");
		//				pref.setEnabled(true);
		//			}
		//테스트용 . ////
		//		}
/*
 * 		 *  checkMileageId /		 그냥 쓰기.
		 *  password /			업뎃											비번에서 별도.	 처리						.
		 *  phoneNumber /			그냥 쓰기.
		 *  email /				업뎃											pref_user_email						//
		 *  birthday /			업뎃											pref_user_birth						//
		 *  gender /			업뎃											pref_user_sex						//
		 *  latitude /				그냥 쓰기. 별도 업뎃? 어플 실행시?
		 *  longitude /				그냥 쓰기. 별도 업뎃? 어플 실행시?
		 *  deviceType /			그냥 쓰기.
		 *  registrationId /		그냥 쓰기.
		 *  activateYn /		업뎃 (탈퇴시)																		//
		 *  modifyDate /		업뎃 - 										현시각. 년월일시분  yyyyMMdd-hh:mm	(그때그때)	/	
 */
		
		
		// 알림 수신 설정 여부.
		if(preference.equals((CheckBoxPreference)findPreference("preference_alarm_chk"))){
			//	Toast.makeText(PrefActivityFromResource.this, "preference_lock_password", Toast.LENGTH_SHORT).show();
			SharedPreferences.Editor saveGCMCustom = sharedPrefCustom.edit();		// 공용으로 비번도 저장해 준다.
			yn = ((CheckBoxPreference)findPreference("preference_alarm_chk")).isChecked();
			saveGCMCustom.putBoolean("gcmReceive", yn);
			saveGCMCustom.commit();
			// 서버에도 업뎃 시켜준다.
			if(updateLv<2){		// 0또는 1일경우. 1 증가. (최대 2까지)
				updateLv = updateLv+1;
				if(updateLv==1){
					updateGCMToServer(yn);
				}
			}
		}
		

		// 자주 묻는 질문 등의 경우 인텐트로 웹뷰 실시
		if(preference.equals(findPreference("pref_app_qna"))){
			//			Toast.makeText(PrefActivityFromResource.this, "웹뷰 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", "http://www.mcarrot.net/mFaq.do");
			startActivity(webIntent);
		}

		// 공지사항.  pref_app_notify
		if(preference.equals(findPreference("pref_app_notify"))){
			//			Toast.makeText(PrefActivityFromResource.this, "웹뷰 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", "http://www.mcarrot.net/mNoticeBoardList.do");
			startActivity(webIntent);
		}

		// 이용 약관..  pref_app_terms
		if(preference.equals(findPreference("pref_app_terms"))){
			//			Toast.makeText(PrefActivityFromResource.this, "웹뷰 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			webIntent.putExtra("loadingURL", "http://www.mcarrot.net/mTerms.do");
			startActivity(webIntent);
		}

//		// 탈퇴.  pref_app_leave
//		if(preference.equals(findPreference("pref_app_leave"))){
//		//	//		//	Toast.makeText(PrefActivityFromResource.this, R.string.leave_toast_message, Toast.LENGTH_SHORT).show();
//			new AlertDialog.Builder(this)
//			.setTitle("회원 탈퇴")
//			.setMessage("탈퇴 시 기존 마일리지가 소멸됩니다.\n정말로 탈퇴하시겠습니까?")
//			.setIcon(android.R.drawable.ic_dialog_alert)
//			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					memberInfo.setActivateYn("N");	// 서버에 비활성화 시킨다.
//					memberDeactivation();
//					// 모바일 내에서도 QR코드,비번 설정 등을 날려 버린다. 그 외 정보들은 인증 받으면서 서버에서 받은걸로 업뎃 해준다....
//					SharedPreferences.Editor init = sharedPrefCustom.edit();
//					init.putString("qrcode", "");		init.putBoolean("appLocked", false);	
//					init.putString("password", "");
//					init.commit();
//					goodBye(thePrefs);
//					// db 의 사용자 테이블 드랍.
//					try{
//						SQLiteDatabase db = null;
//						db= openOrCreateDatabase( "sqlite_carrotDB.db",             
//						          SQLiteDatabase.CREATE_IF_NECESSARY ,null );
//						db.execSQL("DROP TABLE user_info");
//						db.close();
//					}catch(Exception e){
//						e.printStackTrace();
//					}
//					Toast.makeText(PrefActivityFromResource.this, "이용해 주셔서 감사합니다.", Toast.LENGTH_SHORT).show(); 
//					finish();
//				}
//			})
//			.setNegativeButton(android.R.string.no, null).show();

			//			Intent webIntent = new Intent(PrefActivityFromResource.this, myWebView.class);
			//			webIntent.putExtra("loadingURL", "http://m.naver.com");
			//			startActivity(webIntent);
//		}

		
		
		// 이벤트 알림  pref_push_list
		if(preference.equals(findPreference("pref_push_list"))){
			AlertShow_Message();				// 준비중입니다.
			
			// 이벤트 목록 구현 이후 주석 해제.
//			Intent PushListIntent = new Intent(PrefActivityFromResource.this, co.kr.bettersoft.checkmileage_mobile_android_phone_customer.PushList.class);
//			startActivity(PushListIntent);
			
		}
		
		// 이 앱은 ? pref_app_what
		if(preference.equals(findPreference("pref_app_what"))){
			//			Toast.makeText(PrefActivityFromResource.this, "웹뷰 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
			Intent aboutIntent = new Intent(PrefActivityFromResource.this, Settings_AboutPageActivity.class);
//			webIntent.putExtra("loadingURL", "http://m.naver.com");
			startActivity(aboutIntent);
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	

	
	// 준비중입니다.. -> 이벤트 목록 현재 미구현.
	public void AlertShow_Message(){		//R.string.network_error
		AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
		alert_internet_status.setTitle("Carrot");
		alert_internet_status.setMessage(R.string.not_yet);
		String tmpstr = getString(R.string.closebtn);
		alert_internet_status.setPositiveButton(tmpstr, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
//				finish();
			}
		});
		alert_internet_status.show();
	}
	
	
	
	
	
	
	
	/*
	 *  서버로부터 개인 정보를 받아와서 도메인에 저장해 둔다. 나중에 업데이트 할때 사용해야하니까.
	 *  checkMileageMemberController 컨/ selectMemberInformation  메/ checkMileageMember 도/ 
	 *  checkMileageId 변<-qrCode , activateYn : Y  /  CheckMileageMember 결과
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
							// 사용자 아이디를 넣어서 조회
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
						}catch(Exception e){ 
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
									Log.w(TAG,"fail to update");
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
	 *  서버로 알림 수신 설정 업뎃.
	 *    플래그 값을 두어 0->1로 바꾸고 업뎃 친다. 
	 *    1일경우 2로 바꾸고 업뎃 안친다. 
	 *    2일경우 아무것도 하지 않는다. 
	 *    업뎃 치고 나서 1을 내리고 나서 확인 -> 0이 아닐 경우 다시 업뎃 친다.
	 */
	public void updateGCMToServer(Boolean checked){  
		Log.i(TAG, "updateGCMToServer");
		controllerName = "checkMileageMemberController";
		methodName = "updateReceiveNotification";
		if(checked){
			strYorN="Y";
		}else{
			strYorN="N";
		}
		if(updateLv>0){
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 사용자 정보 업뎃.
								
								/*
								 * checkMileageId
									receiveNotificationYn
									activateYn
									modifyDate
								 */
								// checkMileageMember    CheckMileageMember
								obj.put("checkMileageId", memberInfo.getCheckMileageId());
								obj.put("receiveNotificationYn", strYorN);						// 정해서 넣어.
								obj.put("activateYn", memberInfo.getActivateYn());
								
								String nowTime = getNow();
								obj.put("modifyDate", nowTime);		// 지금 시간으로.
								
								Log.i(TAG, "checkMileageId::"+memberInfo.getCheckMileageId());
								Log.i(TAG, "receiveNotificationYn::"+strYorN);
								Log.i(TAG, "activateYn::"+memberInfo.getActivateYn());
								Log.i(TAG, "nowTime::"+nowTime);
								
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
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
								if(responseCode==200 || responseCode == 204){	// 성공이라 딱히..
//									theData1(in);		// 서버 결과 가지고 재 세팅하면안됨 <- 는 멤버 정보 받아서 처리하는 녀석임 호출 금지
									Log.d(TAG, "S to receive GCM option update");
									updateLv = updateLv-1;
									if(updateLv>0){		// 2였던 경우. (업뎃중 또 변경된 경우 한번더)
										Log.d(TAG,"Need Update one more time");
										updateGCMToServer(yn);
									}
								}else{
									Log.w(TAG,"fail to update");
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
	 * 회원 탈퇴 전용 메서드.
	 * memberDeactivation
	 */
	public void memberDeactivation(){
		Log.i(TAG, "memberDeactivation");
		controllerName = "checkMileageMemberController";
		methodName = "updateDeactivateMember";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 사용자 탈퇴.
							/*
							 * checkMileageId
									activateYn
									modifyDate
							 */
							obj.put("checkMileageId", memberInfo.getCheckMileageId());
							obj.put("activateYn", memberInfo.getActivateYn());
							Log.i(TAG, "activateYn::"+memberInfo.getActivateYn());

							String nowTime = getNow();
							Log.i(TAG, "nowTime::"+nowTime);
							obj.put("modifyDate", nowTime);		// 지금 시간으로.
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							if(responseCode==200 || responseCode == 204){	// 성공이라 딱히..
								//									theData1(in);		// 서버 결과 가지고 재 세팅하면안됨 <- 는 멤버 정보 받아서 처리하는 녀석임 호출 금지
								// ... 할거 없음. 탈퇴 성공했는데 무슨..
							}else{
								Log.w(TAG,"fail to update");
							}
						}catch(Exception e){ 
							e.printStackTrace();
						}  
					}
				}
		).start();
	}
	/*
	 * 서버로부터 받아온 개인 정보를 파싱해서 도메인에 저장하는 부분. 업뎃, 탈퇴에서 호출하면 안됨. 멤버 데이터 모두 날아감
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		
		// Locale
	      systemLocale = getResources().getConfiguration(). locale;
//        strDisplayCountry = systemLocale.getDisplayCountry();
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
		 *  일단 받은 내용 여기에 적을것
		 *  고객 상세정보::{"checkMileageMember":
		 *  {"checkMileageId":"test1234","password":"","phoneNumber":"01022173645",
		 *  "email":"","birthday":"","gender":"","latitude":"","longitude":"","deviceType":"AS",
		 *  "registrationId":"aaqw","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 *  
		 *   업데이트 할 것들.  도메인에 저장.
		 *  checkMileageId /password /phoneNumber /email /birthday /gender /latitude /longitude /deviceType /registrationId /activateYn /modifyDate /
		 */
//		Log.d(TAG,"서버에서 받은 고객 상세정보::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
//				Bitmap bm = null;
				// 데이터를 전역 변수 도메인에 저장하고 핸들러를 통해 도메인-> 화면에 보여준다..
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
					if((jsonobj2.getString("activateYn")==null)||(jsonobj2.getString("activateYn").length()<1)){
						memberInfo.setActivateYn("Y");
					}
				}catch(Exception e){ memberInfo.setActivateYn("Y"); }
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
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			Toast.makeText(PrefActivityFromResource.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	// ...

	
	// 주 용도는 resume 때 자체 프리퍼런스 전달하여 컨트롤 할 수 있게 하는 것.  추후 단일 프리퍼런스 사용으로 전환도 가능하다(이걸 메인으로). 현재는 프리퍼런스 3개 사용중;;
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("pref_app_hi")){		// resume 에서 넣은 것과 이름 일치해야 동작한다.
//			Toast.makeText(PrefActivityFromResource.this, "???"+key, Toast.LENGTH_SHORT).show();
			/*  // 테스트용
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
			*/		// 테스트용(확인용)
			thePrefs = sharedPreferences;
		}
	}
	
	// 탈퇴 - 어플 내 기본 프리퍼런스 초기화.
	public void goodBye(SharedPreferences sharedPreferences){
		/*
		 * birthYear//2009
		 * birthMonth//8
		 * birthDay//23
		 * 
		 * preference_alarm_chk//true
		 * preference_lock_chk//true
		 * 
		 * pref_user_email//ㄱㄴㄱㄴㄴ
		 * pref_user_sex//남성
		 * password//1234
		 */
		SharedPreferences.Editor init = sharedPreferences.edit();
		init.putInt("birthYear", todayYear);	
		init.putInt("birthMonth", todayMonth);	
		init.putInt("birthDay", todayDay);	
		
		init.putString("password", "");
		init.putString("pref_user_sex", null);
		init.putString("pref_user_email", "");
		
		init.putBoolean("preference_alarm_chk", true);	
		init.putBoolean("preference_lock_chk", false);	
		
		init.commit();
	}
	
	
	/**
	 *  공용설정에서 업뎃 여부 확인 이후, y 이면 자체 설정에 업뎃 친다. 아니면 말고.  비번은 해당사항 없다.
	 *  
	 */
	public void updateServerSettingsToPrefs(){
		String updateYN = sharedPrefCustom.getString("updateYN", "n");
		if(updateYN.equals("y")){
			Log.w(TAG,"need update o");
			String server_birthday = sharedPrefCustom.getString("server_birthday", "");
			String server_email = sharedPrefCustom.getString("server_email", "");
			String server_gender = sharedPrefCustom.getString("server_gender", "");
			Boolean server_receive_notification_yn = sharedPrefCustom.getBoolean("server_receive_notification_yn", true);
			
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
			updateDone.putString("updateYN", "n");
			updateDone.commit();
			Log.i(TAG,"update settings to mobile done");
			
//			birthYear = sharedPrefCustom.getInt("birthYear", todayYear);		
//			birthMonth = sharedPrefCustom.getInt("birthMonth", todayMonth)-1;		// 저장할때 1 더해서 넣었으니 꺼낼때는 1 빼서..	
//			birthDay = sharedPrefCustom.getInt("birthDay", todayDay);
//			DatePickerDialog DatePickerDialog2 = new DatePickerDialog(this, mDateSetListener, birthYear, birthMonth, birthDay);
//			DatePickerDialog2.setTitle("생년월일 설정");		// 달력 타이틀 설정
//			DatePickerDialog2.show();
			
			SharedPreferences.Editor sets = defaultPref.edit();
			if(!server_receive_notification_yn){
				sets.putBoolean("preference_alarm_chk", false);	
				CheckBoxPreference preference_alarm_chk = (CheckBoxPreference)findPreference("preference_alarm_chk");
				preference_alarm_chk.setChecked(false);
			}
			if(server_gender.length()>0){
				Log.d(TAG,server_gender);
//				ListPreference pref_user_sex = (ListPreference)findPreference("pref_user_sex");
//				pref_user_sex.setValue(server_gender);
				sets.putString("pref_user_sex", server_gender);
			}
			if(server_email.length()>0){
				Log.d(TAG,server_email);
//				EditTextPreference pref_user_email = (EditTextPreference)findPreference("pref_user_email");
//				pref_user_email.setText(server_email);
				sets.putString("pref_user_email", server_email);
			}
			sets.commit();		// 자체 설정도 바꾸고 화면상에서도 바꿔준다.
			
//			(Preference)findPreference("preference_alarm_chk").
			
			if(defaultPref!=null){		// 자체 설정. 
				Map<String, ?> map = defaultPref.getAll();
				Log.d(TAG, "map.size"+map.size());	
				Set set  = map.keySet();
				Iterator ii = set.iterator();
				String iikey="";
				String iivalue="";
				Object obj = null;
				while(ii.hasNext()){
					iikey =( String)ii.next();
					obj = (Object) map.get(iikey);
//					iivalue  = (String)obj;
					Log.d(TAG, iikey+"//"+obj);	
				}
			}
			
		}else{
//			Log.e(TAG,"업뎃 필요 x");
		}
	}
	
	
	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
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
		}
	}
	
	
	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
		resumeCalled = false;
		super.onDestroy();
	}
}