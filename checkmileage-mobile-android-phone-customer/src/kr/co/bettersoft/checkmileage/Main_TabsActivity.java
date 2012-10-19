package kr.co.bettersoft.checkmileage;
// ���� �޴���. ��1:��QR���� , ��2:�����ϸ���, ��3:���������, ��4:����
import static kr.co.bettersoft.checkmileage.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static kr.co.bettersoft.checkmileage.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import com.google.android.gcm.GCMRegistrar;
import com.pref.DummyActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.TabActivity;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class Main_TabsActivity extends TabActivity implements OnTabChangeListener {
	String TAG ="Main_TabsActivity";
	public static Activity main_TabsActivity;
	
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	int maxRetry = 5;
	static String myQR = "";

	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	
	/*
	 * // �� ���������� �����ϴ°� Ȯ���ϹǷ�..�ּ�ó����. 
	 * (������ Ǫ�� ���ý� �� �������� onDesroy �� �ùٸ��� ������� �ʴ� ������ ����)
	 * //	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;			
		//	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	 * 
	 */

	static String barCode = "";
	public static TabHost tabhost;

	///////////////////////////////  // GCM 
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// ��Ͼ��̵�
	
	int waitEnd = 0;		// test GCM ����
	int slow = 0;
	
	String RunMode = "";		// push ���� ������ ���� ��ġ
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
//		requestWindowFeature(Window.FEATURE_LEFT_ICON);		// ���ʿ� ������ �ֱ�- �ȵ�.			FEATURE_NO_TITLE ��   FEATURE_RIGHT_ICON ..
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);
		main_TabsActivity = Main_TabsActivity.this;		// �ٸ����� ���� �����Ű�� ����.
		
		Intent receiveIntent = getIntent();
		if(myQR.length()<1){
			myQR = receiveIntent.getStringExtra("myQR");
		}
		
		RunMode = receiveIntent.getStringExtra("RunMode");	
		if(RunMode==null){
			RunMode="";
		}
		nextProcessing();

//		tabhost = (TabHost) findViewById(android.R.id.tabhost);
		tabhost = getTabHost();
		
		tabhost.setOnTabChangedListener(this);		// �̰� ����� ü���� ȿ����..
		
		// ����
		tabhost.addTab(
				tabhost.newTabSpec("tab_1")
				//        		.setIndicator("��QR�ڵ�", getResources().getDrawable(R.drawable.tab01_indicator))
//				.setIndicator((View)tvTab1)
				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu1))			// �ϴ� ��ư�� �̹��� �����.
				.setContent(new Intent(this, MyQRPageActivity.class)));
		// Optimizer.class �ҽ��� tab_1 �ǿ��� ����. Optimizer.java
		//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabhost.addTab(tabhost.newTabSpec("tab_2")
//				.setIndicator((View)tvTab2)
				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu2))
				.setContent(new Intent(this, MyMileagePageActivity.class)));  
		tabhost.addTab(tabhost.newTabSpec("tab_3")
//				.setIndicator((View)tvTab3)
				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu3))
				.setContent(new Intent(this, MemberStoreListPageActivity.class)));
		
		tabhost.addTab(tabhost.newTabSpec("tab_4")
//				.setIndicator((View)tvTab4)
				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu4))
				.setContent(new Intent(this, com.pref.PrefActivityFromResource.class)));  
		
		// Tab�� �� ����
        for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
         tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
        }
        tabhost.getTabWidget().setCurrentTab(0);
        tabhost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#595959"));
        
     // ���ϸ��� ���� ����ÿ� ���� ��ġ ����
		if(RunMode.length()>0){
			if(RunMode.equals("MILEAGE")){
				tabhost.setCurrentTab(1);		// ���� �� ������ ���� ���..
			}
		}
		
		// locale
		getLocale();
	}

	public void getLocale(){
		Locale systemLocale = getResources().getConfiguration().locale;
		String strDisplayCountry = systemLocale.getDisplayCountry();
		String strCountry = systemLocale.getCountry();
		String strLanguage = systemLocale.getLanguage();
		Log.d(TAG,"strDisplayCountry:"+strDisplayCountry+"/strCountry:"+strCountry+"/strLanguage:"+strLanguage);
	}
	
	
	
	@Override
	public void onTabChanged(String tabId) {
//		Log.d(TAG, "onTabChanged");
//		String strMsg;
//        strMsg = "onTabChanged : " + tabId;
//        Toast.makeText( this, strMsg, Toast.LENGTH_SHORT ).show();
		
		// tab ���� ����
		for(int i=0; i<tabhost.getTabWidget().getChildCount(); i++){
			tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
		}
		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#595959"));
		
	}

	////////////////////////////////////////////GCM ����        ///////////////////////////////////////////////////////////////		
	public void nextProcessing(){
		////////////////////////////////////////////GCM ����        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);					// �ӽ� ����  ->����
		GCMRegistrar.checkManifest(this);				
		Log.i(TAG, "registerReceiver1 ");
		final String regId = GCMRegistrar.getRegistrationId(this);
//		final Context context = this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				if(regId==null || regId.length()<1){		// ��� �Ǿ������� ����. --> �����غ�.. �ȵ�������?
					reg();
				}else{
					Log.d(TAG,"already have a reg ID::"+regId);					// ���߿� �޾�..
					try {
						REGISTRATION_ID = regId;			// ���ص� ������ ������ ���� �߸� ��� ���� ��� ������ �ɼ� �ֱ� ������ ���� �ִٸ� �ѹ��� ������ ���ش�. 
						updateMyGCMtoServer();
//						testGCM(REGISTRATION_ID);				
//					} catch (JSONException e) {
//						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				mRegisterTask = null;
			}
		};
		mRegisterTask.execute(null, null, null);
	}


	///////////////////////////////////////// GCM ��� �޼ҵ� ///////////////////////////////////////    
	//GCM ���
	public void reg(){
		new Thread(
				new Runnable(){
					public void run(){
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}finally{
							checkDoneAndDoGCM();
						}
					}
				}
		).start();
	}

	///////////////////////////////////////// GCM ��� ���� �޼ҵ�� //////////////////////////////////    
	
	
	public void testGCM(String registrationId) throws JSONException, IOException {
		Log.i("testGCM", "testGCM");
		JSONObject jsonMember = new JSONObject();
		jsonMember.put("registrationId", registrationId);
		String jsonString = "{\"checkMileageMember\":" + jsonMember.toString() + "}";
		//Log.i("testGCM", "jsonMember : " + jsonString);
		try {
			URL postUrl2 = new URL("http://"+serverName+"/checkMileageMemberController/testGCM");
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
		} catch (Exception e) {
			Log.d("testGCM", "Fail to register category.");
		}
	}


	public void checkDoneAndDoGCM(){
		slow = slow +1;
		if(slow==3){				// 3�� �����Ҷ����� �߰� ���.(�����������)
			slow = 0;
			slowingReg();
		}
		new Thread(
				new Runnable(){
					public void run(){
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
						}finally{
							REGISTRATION_ID = GCMRegistrar.getRegistrationId(getThis());	
							if(REGISTRATION_ID.length()<1){
								Log.i("testGCM", "wait..");
								if(maxRetry>0){
									maxRetry = maxRetry -1;
									checkDoneAndDoGCM();
								}else{
									maxRetry = 5;
								}
							}else{
								Log.i("testGCM", "now go with : "+REGISTRATION_ID);
								try {
//									updateMyGCMtoServer();
									testGCM(REGISTRATION_ID);
								} catch (JSONException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
		).start();
	}

	public Context getThis(){
		return this;
	}

	public void slowingReg(){
		GCMRegistrar.register(this, SENDER_ID);
	}


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
							obj.put("checkMileageId", myQR);			  
							obj.put("registrationId", REGISTRATION_ID);							
							obj.put("modifyDate", getNow());			

							Log.d(TAG, "checkMileageId:"+myQR);
							Log.d(TAG, "registrationId:"+REGISTRATION_ID);
							Log.d(TAG, "modifyDate:"+getNow());

						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							URL postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
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
							int responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								Log.i(TAG, "S to update GCM ID to server");
								// ��ȸ�� ����� ó��.
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
		// �ϴ� ����.
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

	
	// ���ù� ���, �����Ͽ� ���۽� ������ ���ù� �����߳Ĵ� ���� �αװ� ������ �ʵ��� �Ѵ�. ���� Ǫ�� �޴°��� ���񽺴ܿ���..
	BroadcastReceiver mMyBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w(TAG,"intent.getExtras().getString(EXTRA_MESSAGE):"+intent.getStringExtra("MESSAGE"));
			//		if(intent.getAction().equals(DISPLAY_MESSAGE_ACTION)) {
			// Broadcast�� ������ �� ��
			//			Toast.makeText(Main_TabsActivity.this, "(�׽�Ʈ)�޽����� �����Ͽ����ϴ�."+intent.getExtras().getString(EXTRA_MESSAGE), Toast.LENGTH_SHORT).show();
			//		}
		}
	};
	@Override
	protected void onResume() {
//		Log.i(TAG, "onResume");
//		registerReceiver(mMyBroadcastReceiver, new IntentFilter("receive���� �̸�"));
		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
		super.onResume();
	};

	@Override
	protected void onPause() {
		unregisterReceiver(mMyBroadcastReceiver);
		super.onPause();
		 // Ȩ��ư �������� ���� ����..
      if(!isForeGround()){
            Log.d(TAG,"go home, bye");
            dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
            finish();
      }
	}
	
	/*
     * ���μ����� �ֻ����� ���������� �˻�.
     * @return true = �ֻ���
     */
     public Boolean isForeGround(){
          ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE );
          List<RunningTaskInfo> list = am.getRunningTasks(1);
          ComponentName cn = list.get(0). topActivity;
          String name = cn.getPackageName();
          Boolean rtn = false;
           if(name.indexOf(getPackageName()) > -1){
                rtn = true;
          } else{
                rtn = false;
          }
           return rtn;
    }

}
