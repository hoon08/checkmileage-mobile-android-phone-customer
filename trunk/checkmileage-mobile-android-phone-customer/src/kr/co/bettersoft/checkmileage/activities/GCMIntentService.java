/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.bettersoft.checkmileage.activities;

import static kr.co.bettersoft.checkmileage.common.CommonConstant.SENDER_ID;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * GCMIntentService
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	static String tmpStr = "";

//	String controllerName="";
//	String methodName="";
//	String serverName = CommonUtils.serverNames;
//	URL postUrl2 ;
//	HttpURLConnection connection2;

	CheckMileageCustomerRest checkMileageCustomerRest = new CheckMileageCustomerRest();
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	// checkMileageCustomerRest = new CheckMileageCustomerRest();	// oncreate
	
	String regIdGCM = "";
	Boolean dontTwice = true;

	Context localContext;
	String localRegistrationId;

	
	// 설정 파일 저장소   -- 설정에 GCM ID 를 저장하여 GCM ID 가 변경된 경우에만 서버로 업데이트 한다.
	SharedPreferences sharedPrefCustom;
	String savedGCMId = "";
	private static final String TAG = "GCMIntentService";

	static String myQR = "";
	//    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);


	public GCMIntentService() {
		super(SENDER_ID);
	}

	/**
	 * onRegistered
	 *  gcm 등록되면 비동기로 서버에 업데이트한다
	 *
	 * @param context
	 * @param registrationId
	 * @return
	 */
	@Override
	public void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		savedGCMId = sharedPrefCustom.getString("savedGCMId", "");	
		if(dontTwice){			// 한번 하고 막는다.
			//        	MainActivity.REGISTRATION_ID = registrationId;
			regIdGCM = registrationId;
			if(!(savedGCMId.equals(regIdGCM))){
				SharedPreferences.Editor updatesavedGCMId =   sharedPrefCustom.edit();
				updatesavedGCMId.putString("savedGCMId", regIdGCM);		// GCM ID저장
				updatesavedGCMId.commit();
				new backgroundUpdateMyGCMtoServer().execute();	// 비동기로 전환 - 서버에 GCM 아이디 저장	
				//            displayMessage(context, getString(R.string.gcm_registered));			// 브로드 케스트로 보내줌.. 리시버가 잡음. (노티는 없음)
				localContext = context;
				localRegistrationId = registrationId;
				//            new backgroundServerRegister().execute();
				dontTwice = false;
			}
		}
	}

//	// 비동기로 gcm 서버에 레지스터.. 두번 등록 할 필요 없으므로 실행 안함
//	public class backgroundServerRegister extends  AsyncTask<Void, Void, Void> { 
//		@Override protected void onPostExecute(Void result) {  
//		} 
//		@Override protected void onPreExecute() {  
//		} 
//		@Override protected Void doInBackground(Void... params) {  
//			Log.d(TAG,"backgroundServerRegister");
//			ServerUtilities.register(localContext, localRegistrationId);
//			return null; 
//		}
//	} 

	// 비동기로 GCM 아이디 업뎃 호출  -- 캐럿 서버에 gcm 아이디 업뎃
	/**
	 * backgroundUpdateMyGCMtoServer
	 * 비동기로 서버에 gcm 업데이트한다
	 *
	 * @param context
	 * @param registrationId
	 * @return
	 */
	public class backgroundUpdateMyGCMtoServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyGCMtoServer");
			//        		updateMyGCMtoServer_pre();
			
			if(myQR==null || myQR.length()<1){	
				myQR = Main_TabsActivity.myQR;
			}
			if(myQR==null || myQR.length()<1){
				// qr 없으면 업데이트 하지 않음
			}else{			// qr 있을때에만 업데이트함.
				// 파리미터 세팅
				CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 	
				checkMileageMembersParam.setCheckMileageId(myQR);
				checkMileageMembersParam.setRegistrationId(regIdGCM);
				// 호출
	//			if(!pullDownRefreshIng){
//					showPb();
	//			}
				callResult = checkMileageCustomerRest.RestUpdateMyGCMtoServer(checkMileageMembersParam);
//				hidePb();
				// 결과 처리
//				if(callResult.equals("SUCCESS")){				// 인증 성공
//					Log.i(TAG, "SUCCESS");
//					showResultDialog(getString(R.string.certi_num_req_success));	
//				}else{														// 인증 실패
//					Log.i(TAG, "FAIL_ADMISSION");
//					showResultDialog(getString(R.string.certi_num_req_fail));
//				}
//				updateMyGCMtoServer();
			}
			return null; 
		}
	}

//	//서버에 GCM 아이디 업뎃한다.
//	/**
//	 * updateMyGCMtoServer
//	 *  서버에 GCM 아이디 업뎃한다.
//	 *
//	 * @param
//	 * @param
//	 * @return
//	 */
//	public void updateMyGCMtoServer(){
//		Log.i(TAG, "updateMyGCMtoServer");
//		controllerName = "checkMileageMemberController";
//		methodName = "updateRegistrationId";
//		// 서버 통신부
//		new Thread(
//				new Runnable(){
//					public void run(){
//						JSONObject obj = new JSONObject();
//						try{
//							obj.put("activateYn", "Y");
//							obj.put("checkMileageId", myQR);			  
//							obj.put("registrationId", regIdGCM);							
//							obj.put("modifyDate", getNow());			
//							Log.d(TAG, "checkMileageId:"+myQR);
//							Log.d(TAG, "registrationId:"+regIdGCM);
//							Log.d(TAG, "modifyDate:"+getNow());
//						}catch(Exception e){
//							e.printStackTrace();
//						}
//						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
//						try{
//							postUrl2 = new URL(serverName+"/"+controllerName+"/"+methodName);
//							connection2 = (HttpURLConnection) postUrl2.openConnection();
//							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
//							connection2.setDoOutput(true);
//							connection2.setInstanceFollowRedirects(false);
//							connection2.setRequestMethod("POST");
//							connection2.setRequestProperty("Content-Type", "application/json");
//							//							connection2.connect();		// *** 
//							Thread.sleep(200);
//							OutputStream os2 = connection2.getOutputStream();
//							os2.write(jsonString.getBytes("UTF-8"));
//							os2.flush();
//							//							System.out.println("postUrl      : " + postUrl2);
//							//							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
//							int responseCode = connection2.getResponseCode();
//							//							os2.close();
//							if(responseCode==200||responseCode==204){
//								Log.i(TAG, "S to update GCM ID to server");
//							}else{
//								Log.i(TAG, "F to update GCM ID to server");
//							}
//							//							connection2.disconnect();
//						}catch(Exception e){ 
//							//							connection2.disconnect();
//							e.printStackTrace();
//						}
//						//						CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//						//						if(CommonUtils.usingNetwork < 0){	// 0 보다 작지는 않게
//						//							CommonUtils.usingNetwork = 0;
//						//						}
//					}
//				}
//		).start();
//	}
//	// GCM 테스트 용
//	public void testGCM(String registrationId) throws JSONException, IOException {
//		Log.i(TAG, "testGCM");
//		JSONObject jsonMember = new JSONObject();
//		jsonMember.put("registrationId", registrationId);
//		String jsonString = "{\"checkMileageMember\":" + jsonMember.toString() + "}";
//
//		Log.i(TAG, "jsonMember : " + jsonString);
//
//		try {
//			postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageMemberController/testGCM");		 // test 용..
//			connection2 = (HttpURLConnection) postUrl2.openConnection();
//			connection2.setDoOutput(true);
//			connection2.setInstanceFollowRedirects(false);
//			connection2.setRequestMethod("POST");
//			connection2.setRequestProperty("Content-Type", "application/json");
//			//		         connection2.connect();		// *** 
//			OutputStream os2 = connection2.getOutputStream();
//			os2.write(jsonString.getBytes("UTF-8"));
//			os2.flush();
//			System.out.println("postUrl      : " + postUrl2);
//			System.out.println("responseCode : " + connection2.getResponseCode());
//			//		         connection2.disconnect();
//		} catch (Exception e) {
//			//			  connection2.disconnect();
//			Log.e(TAG, "Fail to register category.");
//		}
//	}
//
//	// 현시각 구하기
//	/**
//	 * getNow
//	 *  현시각 구한다
//	 *
//	 * @param
//	 * @param
//	 * @return nowTime
//	 */
//	public String getNow(){
//		Calendar c = Calendar.getInstance();
//		int todayYear = c.get(Calendar.YEAR);
//		int todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
//		int todayDay = c.get(Calendar.DATE);
//		int todayHour = c.get(Calendar.HOUR_OF_DAY);
//		int todayMinute = c.get(Calendar.MINUTE);
//		int todaySecond = c.get(Calendar.SECOND);
//
//		String tempMonth = Integer.toString(todayMonth);
//		String tempDay = Integer.toString(todayDay);
//		String tempHour = Integer.toString(todayHour);
//		String tempMinute = Integer.toString(todayMinute);
//		String tempSecond = Integer.toString(todaySecond);
//		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
//		if(tempDay.length()==1) tempDay = "0"+tempDay;
//		if(tempHour.length()==1) tempHour = "0"+tempHour;
//		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
//
//		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute+":"+tempSecond;
//		return nowTime;
//		//Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
//	}
	
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		//        displayMessage(context, getString(R.string.gcm_unregistered));
//		if (GCMRegistrar.isRegisteredOnServer(context)) {
//			ServerUtilities.unregister(context, registrationId);
//		} else {
//			// This callback results from the call to unregister made on
//			// ServerUtilities when the registration to the server failed.
//			Log.i(TAG, "Ignoring unregister callback");
//		}
	}

	/**
	 * onMessage
	 *  서버에서 푸시가 오면 사용자에게 노티를 준다
	 *
	 * @param context
	 * @param intent
	 * @return
	 */
	@Override		// GCM 서비스 에서 온 메시지 받은거.
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");

		//        String message = getString(R.string.gcm_message);
		String message = intent.getStringExtra("MESSAGE");
		//        displayMessage(context, message);
		Log.i(TAG, "Received message of onMessage():"+intent.getStringExtra("MESSAGE"));		// 동작함.

		// 앱위젯 테스트용
		//        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.my_widget_layout);
		//        int count = 50;//Or whatever value you set to it.
		//        views.setTextViewText(R.id.txtCount,Integer.toString(count));
		//        AppWidgetManager appWidgetManager;
		//        appWidgetManager.updateAppWidget(appWidgetId, views);


		//        Toast.makeText(GCMIntentService.this, "(테스트)메시지가 도착하였습니다."+intent.getStringExtra("MESSAGE"), Toast.LENGTH_SHORT).show();
		//        if(intent.getStringExtra("MESSAGE").equals("Check Mileage 로 부터 새로운 메시지가 도착했습니다.")){
		//        	// ...Log.i(TAG, "112233");		// 동작함.
		//        }

		/*
		 * MILEAGE : 새로운 마일리지가 등록되거나 기존 마일리지가 업데이트되었다.
		 * MARKETING : 가맹점이나 서비스에서 마케팅정보나 기타 알림메시지가 수신되었다.
		 * Check Mileage 로 부터 새로운 메시지가 도착했습니다. : test 메시지..
		 * 그 외 : 마케팅 메시지
		 */
		//        if(intent.getStringExtra("MESSAGE").contains("MILEAGE")){
		// noti 는 필요 없고.. 내 마일리지 목록 갔을때 재 조회 되도록 변수 값만 변경해준다. -- 아직 미구현 상태이다..
		// --> noti 해줘야 한다. 어플 종료한 상태일수도 있기 때문..(리시버는 어플 종료해도 동작)
		//        }else{
		// notifies user
		generateNotification(context, message);	// 사용자에게 노티를 준닷
		//        }

	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		//        displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		//        displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		//        displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * generateNotification
	 * Issues a notification to inform the user that server has sent a message.
	 * @param context
	 * @param message
	 * @return
	 */
	private  void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_stat_gcm;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager)
		context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);			// sdf
		String title = context.getString(R.string.app_name);
		String mileageUpdateStr = context.getString(R.string.mileage_noti);
		Intent notificationIntent;
		/*
		 * MILEAGE : 새로운 마일리지가 등록되거나 기존 마일리지가 업데이트되었다.
		 * MARKETING : 가맹점이나 서비스에서 마케팅정보나 기타 알림메시지가 수신되었다.
		 * Check Mileage 로 부터 새로운 메시지가 도착했습니다. : test 메시지..
		 * 
		 * <string name="gcm_new_msgkkk">새로운 메시지</string>
		 */
		String new_msg = "새로운 메시지";

		if(message.equals(new_msg)){
			Log.d(TAG,"new msg - test");
			notificationIntent = new Intent(context, DummyActivity.class);	
			//            notificationIntent.putExtra("RunMode", "TEST");						// 데이터 전달이 가능하다. 이것을 통해.. 원하는 액티비티를 실행시켜줄수 있다..
			notificationIntent.putExtra("RunMode", "MILEAGE");						//

			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
			//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
			//                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent =
				PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, title, message, intent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, notification);

			//        }else if(message.contains("MILEAGE")){
			//        }else if(message.contains(mileageUpdateStr)){			// 캐럿이 변경되었습니다. (다국어)
		}else if(message.equals(mileageUpdateStr)){			// 캐럿이 변경되었습니다. (다국어).
			Log.d(TAG,"update mileage");
			MyMileagePageActivity.searched = false;
			//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면..
			notificationIntent = new Intent(context, DummyActivity.class);	
			notificationIntent.putExtra("RunMode", "MILEAGE");

			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
			//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
			//                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent =
				PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, title, message, intent);					
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, notification);
		}else if(message.equals("MARKETING")){
			Log.d(TAG,"noti event push");
			//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면 더미 통한 호출..
			notificationIntent = new Intent(context, DummyActivity.class);	
			notificationIntent.putExtra("RunMode", "MARKETING");
			notificationIntent.putExtra("MESSAGE", "New Event");
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
			//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
			//                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent =
				PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, title, message, intent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, notification);
		}else{							// 마케팅 직구.
			Log.d(TAG,"noti event push");
			//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면 더미 통한 호출..
			notificationIntent = new Intent(context, DummyActivity.class);	
			notificationIntent.putExtra("RunMode", "MARKETING");
			notificationIntent.putExtra("MESSAGE", message);

			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
			//                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
			//                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent =
				PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, title, message, intent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, notification);
		}
	}
}
