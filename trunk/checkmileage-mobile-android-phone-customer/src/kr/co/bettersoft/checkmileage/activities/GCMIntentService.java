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

import static kr.co.bettersoft.checkmileage.activities.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	static String tmpStr = "";
	
	String controllerName="";
	String methodName="";
	String serverName = CommonUtils.serverNames;
	
	String regIdGCM = "";
	Boolean dontTwice = true;
	
	Context localContext;
	String localRegistrationId;
	
	URL postUrl2 ;
	HttpURLConnection connection2;
	
    private static final String TAG = "GCMIntentService";

    
//    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);

    
    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    public void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        if(dontTwice){			// �ѹ� �ϰ� ���´�.
//        	MainActivity.REGISTRATION_ID = registrationId;
            regIdGCM = registrationId;
            new backgroundUpdateMyGCMtoServer().execute();	// �񵿱�� ��ȯ - ������ GCM ���̵� ����	
//            displayMessage(context, getString(R.string.gcm_registered));			// ��ε� �ɽ�Ʈ�� ������.. ���ù��� ����. (��Ƽ�� ����)
            localContext = context;
			localRegistrationId = registrationId;
//            new backgroundServerRegister().execute();
        	dontTwice = false;
        }
    }
    
    // �񵿱�� gcm ������ ��������.. �ι� �� �ʿ� ����
    public class backgroundServerRegister extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundServerRegister");
			ServerUtilities.register(localContext, localRegistrationId);
			return null; 
		}
	} 

	// �񵿱�� GCM ���̵� ���� ȣ��  -- ĳ�� ������ gcm ���̵� ����
	public class backgroundUpdateMyGCMtoServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyGCMtoServer");
//        		updateMyGCMtoServer_pre();
        		updateMyGCMtoServer();
//			try {						// gcm Ȯ�ο�
//				testGCM(regIdGCM);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			return null; 
		}
	}

//	public void updateMyGCMtoServer_pre(){
//		new Thread(
//			new Runnable(){
//				public void run(){
//					Log.d(TAG,"updateMyGCMtoServer_pre");
//					try{
//						Thread.sleep(CommonUtils.threadWaitngTime);
//					}catch(Exception e){
//					}finally{
//						if(CommonUtils.usingNetwork<1){
//							CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
//							updateMyGCMtoServer();
//						}else{
//							updateMyGCMtoServer_pre();
//						}
//					}
//				}
//			}
//		).start();
//	}
	
	
	//������ GCM ���̵� �����Ѵ�.
	public void updateMyGCMtoServer(){
		Log.i(TAG, "updateMyGCMtoServer");
		controllerName = "checkMileageMemberController";
		methodName = "updateRegistrationId";
		// ���� ��ź�
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
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
//							connection2.connect();		// *** 
							Thread.sleep(200);
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
//							System.out.println("postUrl      : " + postUrl2);
//							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							int responseCode = connection2.getResponseCode();
//							os2.close();
							if(responseCode==200||responseCode==204){
								Log.i(TAG, "S to update GCM ID to server");
							}else{
								Log.i(TAG, "F to update GCM ID to server");
							}
//							connection2.disconnect();
						}catch(Exception e){ 
//							connection2.disconnect();
							e.printStackTrace();
						}
//						CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//						if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//							CommonUtils.usingNetwork = 0;
//						}
					}
				}
		).start();
	}
    // GCM �׽�Ʈ ��
	public void testGCM(String registrationId) throws JSONException, IOException {
		  Log.i(TAG, "testGCM");
		  JSONObject jsonMember = new JSONObject();
		  jsonMember.put("registrationId", registrationId);
		  String jsonString = "{\"checkMileageMember\":" + jsonMember.toString() + "}";
		 
		  Log.i(TAG, "jsonMember : " + jsonString);
		 
		  try {
		   postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageMemberController/testGCM");		 // test ��..
		   connection2 = (HttpURLConnection) postUrl2.openConnection();
		         connection2.setDoOutput(true);
		         connection2.setInstanceFollowRedirects(false);
		         connection2.setRequestMethod("POST");
		         connection2.setRequestProperty("Content-Type", "application/json");
//		         connection2.connect();		// *** 
		         OutputStream os2 = connection2.getOutputStream();
		         os2.write(jsonString.getBytes("UTF-8"));
		         os2.flush();
		         System.out.println("postUrl      : " + postUrl2);
		         System.out.println("responseCode : " + connection2.getResponseCode());
//		         connection2.disconnect();
		  } catch (Exception e) {
//			  connection2.disconnect();
		   Log.e(TAG, "Fail to register category.");
		  }
		}
	
	// ���ð� ���ϱ�
	public String getNow(){
		Calendar c = Calendar.getInstance();
		int todayYear = c.get(Calendar.YEAR);
		int todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
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

    @Override		// GCM ���� ���� �� �޽��� ������.
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        
//        String message = getString(R.string.gcm_message);
        String message = intent.getStringExtra("MESSAGE");
//        displayMessage(context, message);
        Log.i(TAG, "Received message of onMessage():"+intent.getStringExtra("MESSAGE"));		// ������.
        
        // ������ �׽�Ʈ��
//        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.my_widget_layout);
//        int count = 50;//Or whatever value you set to it.
//        views.setTextViewText(R.id.txtCount,Integer.toString(count));
//        AppWidgetManager appWidgetManager;
//        appWidgetManager.updateAppWidget(appWidgetId, views);
        
        
//        Toast.makeText(GCMIntentService.this, "(�׽�Ʈ)�޽����� �����Ͽ����ϴ�."+intent.getStringExtra("MESSAGE"), Toast.LENGTH_SHORT).show();
//        if(intent.getStringExtra("MESSAGE").equals("Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�.")){
//        	// ...Log.i(TAG, "112233");		// ������.
//        }
        
        /*
         * MILEAGE : ���ο� ���ϸ����� ��ϵǰų� ���� ���ϸ����� ������Ʈ�Ǿ���.
         * MARKETING : �������̳� ���񽺿��� ������������ ��Ÿ �˸��޽����� ���ŵǾ���.
         * Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�. : test �޽���..
         * �� �� : ������ �޽���
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
         * MILEAGE : ���ο� ���ϸ����� ��ϵǰų� ���� ���ϸ����� ������Ʈ�Ǿ���.
         * MARKETING : �������̳� ���񽺿��� ������������ ��Ÿ �˸��޽����� ���ŵǾ���.
         * Check Mileage �� ���� ���ο� �޽����� �����߽��ϴ�. : test �޽���..
         * 
         * <string name="gcm_new_msgkkk">���ο� �޽���</string>
         */
        String new_msg = "���ο� �޽���";
        
        if(message.equals(new_msg)){
        	Log.d(TAG,"new msg - test");
            notificationIntent = new Intent(context, DummyActivity.class);	
//            notificationIntent.putExtra("RunMode", "TEST");						// ������ ������ �����ϴ�. �̰��� ����.. ���ϴ� ��Ƽ��Ƽ�� ��������ټ� �ִ�..
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
//        }else if(message.contains(mileageUpdateStr)){			// ĳ���� ����Ǿ����ϴ�. (�ٱ���)
        }else if(message.equals(mileageUpdateStr)){			// ĳ���� ����Ǿ����ϴ�. (�ٱ���).
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
        }else if(message.equals("MARKETING")){
        	Log.d(TAG,"noti event push");
//        	notificationIntent = new Intent(context, MainActivity.class);		// �̰� ����� ������ �ȴٸ� ���� ���� ȣ��..
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
        }else{							// ������ ����.
        	Log.d(TAG,"noti event push");
//        	notificationIntent = new Intent(context, MainActivity.class);		// �̰� ����� ������ �ȴٸ� ���� ���� ȣ��..
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
    
    @Override
	public void onDestroy(){
		super.onDestroy();
//		try{
//			if(connection2!=null){
//				connection2.disconnect();
//			}
//		}catch(Exception e){}
	}
}