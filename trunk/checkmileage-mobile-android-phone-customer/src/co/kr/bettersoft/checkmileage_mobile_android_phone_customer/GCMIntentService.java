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
//        displayMessage(context, getString(R.string.gcm_registered));			// ��ε� �ɽ�Ʈ�� ������.. ���ù��� ����. (��Ƽ�� ����)
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

    @Override		// GCM ���� ���� �� �޽��� ������.
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        
//        String message = getString(R.string.gcm_message);
        String message = intent.getStringExtra("MESSAGE");
//        displayMessage(context, message);
        Log.i(TAG, "Received message of onMessage():"+intent.getStringExtra("MESSAGE"));		// ������.
//        Toast.makeText(GCMIntentService.this, "(�׽�Ʈ)�޽����� �����Ͽ����ϴ�."+intent.getStringExtra("MESSAGE"), Toast.LENGTH_SHORT).show();
//        if(intent.getStringExtra("MESSAGE").equals("Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�.")){
//        	// ...Log.i(TAG, "112233");		// ������.
//        }
        /*
         * MILEAGE : ���ο� ���ϸ����� ��ϵǰų� ���� ���ϸ����� ������Ʈ�Ǿ���.
         * MARKETING : �������̳� ���񽺿��� ������������ ��Ÿ �˸��޽����� ���ŵǾ���.
         * Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�. : test �޽���..
         */
//        if(intent.getStringExtra("MESSAGE").contains("MILEAGE")){
        	// noti �� �ʿ� ����.. �� ���ϸ��� ��� ������ �� ��ȸ �ǵ��� ���� ���� �������ش�. -- ���� �̱��� �����̴�..
        	// --> noti ����� �Ѵ�. ���� ������ �����ϼ��� �ֱ� ����..(���ù��� ���� �����ص� ����)
//        }else{
        	// notifies user
        	generateNotification(context, message);	// ����ڿ��� ��Ƽ�� �ش�
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
         * MILEAGE : ���ο� ���ϸ����� ��ϵǰų� ���� ���ϸ����� ������Ʈ�Ǿ���.
         * MARKETING : �������̳� ���񽺿��� ������������ ��Ÿ �˸��޽����� ���ŵǾ���.
         * Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�. : test �޽���..
         * 
         * <string name="gcm_new_msgkkk">���ο� �޽���</string>
         */
        String new_msg = "���ο� �޽���";
        if(message.contains(new_msg)){
        	Log.d(TAG,"new msg - test");
//        	notificationIntent = new Intent(context, MainActivity.class);		// �̰� ����� ������ �ȴٸ�..
//            notificationIntent = new Intent(context, PushDetail.class);	// ������ ������ �����ϹǷ� ���� �θ��� �ʾƵ� �Ƿ���..
            notificationIntent = new Intent(context, DummyActivity.class);	
//            notificationIntent.putExtra("RunMode", "TEST");						// ������ ������ �����ϴ�. �̰��� ����.. ���ϴ� ��Ƽ��Ƽ�� ��������ټ� �ִ�..
            notificationIntent.putExtra("RunMode", "MILEAGE");						// �׽�Ʈ��..
            
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
//        	notificationIntent = new Intent(context, MainActivity.class);		// �̰� ����� ������ �ȴٸ�..
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
//        	notificationIntent = new Intent(context, MainActivity.class);		// �̰� ����� ������ �ȴٸ�..
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
