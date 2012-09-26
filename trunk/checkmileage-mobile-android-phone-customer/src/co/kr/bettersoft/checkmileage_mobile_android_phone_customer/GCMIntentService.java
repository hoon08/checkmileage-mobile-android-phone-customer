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
	
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        MainActivity.REGISTRATION_ID = registrationId;
//        displayMessage(context, getString(R.string.gcm_registered));			// 브로드 케스트로 보내줌.. 리시버가 잡음. (노티는 없음)
        ServerUtilities.register(context, registrationId);
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
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        
        Intent notificationIntent;
        /*
         * MILEAGE : 새로운 마일리지가 등록되거나 기존 마일리지가 업데이트되었다.
         * MARKETING : 가맹점이나 서비스에서 마케팅정보나 기타 알림메시지가 수신되었다.
         * Check Mileage 로 부터 새로운 메시지가 도착했습니다. : test 메시지..
         * 
         * <string name="gcm_new_msgkkk">새로운 메시지</string>
         */
        String new_msg = "새로운 메시지";
        if(message.contains(new_msg)){
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
        }else if(message.contains("MILEAGE")){
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
        }else if(message.contains("MARKETING")){
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
