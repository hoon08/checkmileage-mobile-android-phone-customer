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
package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;

import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.SENDER_ID;
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.displayMessage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.pref.DummyActivity;
import com.pref.Password;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	static String tmpStr = "";
	
	String controllerName="";
	String methodName="";
	String regIdGCM = "";
	Boolean dontTwice = true;
	
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        if(dontTwice){			// 한번 하고 막는다.
        	MainActivity.REGISTRATION_ID = registrationId;
            regIdGCM = registrationId;
            updateMyGCMtoServer();
//            displayMessage(context, getString(R.string.gcm_registered));			// 브로드 케스트로 보내줌.. 리시버가 잡음. (노티는 없음)
            ServerUtilities.register(context, registrationId);
        	dontTwice = false;
        }
    }

    
  //서버에 GCM 아이디 업뎃한다.
	public void updateMyGCMtoServer(){
		Log.i(TAG, "updateMyGCMtoServer");
		controllerName = "checkMileageMemberController";
		methodName = "updateRegistrationId";
		// 서버 통신부
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("activateYn", "Y");
							obj.put("checkMileageId", Main_TabsActivity.myQR);			  
							obj.put("registrationId", regIdGCM);							
							obj.put("modifyDate", getNow());			
							Log.d(TAG, "checkMileageId:"+Main_TabsActivity.myQR);
							Log.d(TAG, "registrationId:"+regIdGCM);
							Log.d(TAG, "modifyDate:"+getNow());

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
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								Log.i(TAG, "S to update GCM ID to server");
								// 조회한 결과를 처리.
							}else{
								Log.i(TAG, "F to update GCM ID to server");
							}
						}catch(Exception e){ 
							e.printStackTrace();
						}
					}
				}
		).start();
	}
    
	public String getNow(){
		// 일단 오늘.
		Calendar c = Calendar.getInstance();
		int todayYear = c.get(Calendar.YEAR);
		int todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
		int todayDay = c.get(Calendar.DATE);
		int todayHour = c.get(Calendar.HOUR_OF_DAY);
		int todayMinute = c.get(Calendar.MINUTE);
		int todaySecond = c.get(Calendar.SECOND);
		
		String tempMonth = Integer.toString(todayMonth);
		String tempDay = Integer.toString(todayDay);
		String tempHour = Integer.toString(todayHour);
		String tempMinute = Integer.toString(todayMinute);
		String tempSecond = Integer.toString(todaySecond);
		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
		if(tempDay.length()==1) tempDay = "0"+tempDay;
		if(tempHour.length()==1) tempHour = "0"+tempHour;
		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
		
		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute+":"+tempSecond;
		return nowTime;
		//Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
//        displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override		// GCM 서비스 에서 온 메시지 받은거.
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        
//        String message = getString(R.string.gcm_message);
        String message = intent.getStringExtra("MESSAGE");
//        displayMessage(context, message);
        Log.i(TAG, "Received message of onMessage():"+intent.getStringExtra("MESSAGE"));		// 동작함.
//        Toast.makeText(GCMIntentService.this, "(테스트)메시지가 도착하였습니다."+intent.getStringExtra("MESSAGE"), Toast.LENGTH_SHORT).show();
//        if(intent.getStringExtra("MESSAGE").equals("Check Mileage 로 부터 새로운 메시지가 도착했습니다.")){
//        	// ...Log.i(TAG, "112233");		// 동작함.
//        }
        /*
         * MILEAGE : 새로운 마일리지가 등록되거나 기존 마일리지가 업데이트되었다.
         * MARKETING : 가맹점이나 서비스에서 마케팅정보나 기타 알림메시지가 수신되었다.
         * Check Mileage 로 부터 새로운 메시지가 도착했습니다. : test 메시지..
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
     * Issues a notification to inform the user that server has sent a message.
     */
    private  void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
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
//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면..
//            notificationIntent = new Intent(context, PushDetail.class);	// 데이터 전달이 가능하므로 직접 부르지 않아도 되려나..
            notificationIntent = new Intent(context, DummyActivity.class);	
//            notificationIntent.putExtra("RunMode", "TEST");						// 데이터 전달이 가능하다. 이것을 통해.. 원하는 액티비티를 실행시켜줄수 있다..
            notificationIntent.putExtra("RunMode", "MILEAGE");						// 테스트용..
            
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
//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면..
            notificationIntent = new Intent(context, DummyActivity.class);	
            notificationIntent.putExtra("RunMode", "MARKETING");
            
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
//        	notificationIntent = new Intent(context, MainActivity.class);		// 이걸 띄워서 문제가 된다면..
            notificationIntent = new Intent(context, DummyActivity.class);	
            notificationIntent.putExtra("RunMode", "MARKETING");
            
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
