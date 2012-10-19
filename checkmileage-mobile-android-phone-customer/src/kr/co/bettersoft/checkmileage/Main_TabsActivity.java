package kr.co.bettersoft.checkmileage;
// 메인 메뉴들. 탭1:내QR보기 , 탭2:내마일리지, 탭3:가맹점목록, 탭4:설정
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
	 * // 각 페이지에서 제거하는게 확실하므로..주석처리함. 
	 * (실행후 푸시 선택시 각 페이지의 onDesroy 가 올바르게 실행되지 않는 현상이 있음)
	 * //	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;			
		//	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	 * 
	 */

	static String barCode = "";
	public static TabHost tabhost;

	///////////////////////////////  // GCM 
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// 등록아이디
	
	int waitEnd = 0;		// test GCM 대기용
	int slow = 0;
	
	String RunMode = "";		// push 통한 실행을 위한 조치
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
//		requestWindowFeature(Window.FEATURE_LEFT_ICON);		// 왼쪽에 아이콘 넣기- 안됨.			FEATURE_NO_TITLE 됨   FEATURE_RIGHT_ICON ..
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);
		main_TabsActivity = Main_TabsActivity.this;		// 다른데서 여기 종료시키기 위함.
		
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
		
		tabhost.setOnTabChangedListener(this);		// 이걸 해줘야 체인지 효과가..
		
		// 설정
		tabhost.addTab(
				tabhost.newTabSpec("tab_1")
				//        		.setIndicator("내QR코드", getResources().getDrawable(R.drawable.tab01_indicator))
//				.setIndicator((View)tvTab1)
				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu1))			// 하단 버튼을 이미지 사용함.
				.setContent(new Intent(this, MyQRPageActivity.class)));
		// Optimizer.class 소스는 tab_1 탭에에 속함. Optimizer.java
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
		
		// Tab에 색 지정
        for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
         tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
        }
        tabhost.getTabWidget().setCurrentTab(0);
        tabhost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#595959"));
        
     // 마일리지 통한 실행시에 대한 조치 사항
		if(RunMode.length()>0){
			if(RunMode.equals("MILEAGE")){
				tabhost.setCurrentTab(1);		// 시작 탭 설정을 원할 경우..
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
		
		// tab 색상 변경
		for(int i=0; i<tabhost.getTabWidget().getChildCount(); i++){
			tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
		}
		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#595959"));
		
	}

	////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
	public void nextProcessing(){
		////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);					// 임시 중지  ->해제
		GCMRegistrar.checkManifest(this);				
		Log.i(TAG, "registerReceiver1 ");
		final String regId = GCMRegistrar.getRegistrationId(this);
//		final Context context = this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				if(regId==null || regId.length()<1){		// 등록 되어있으면 재등록. --> 유지해봄.. 안되있으면?
					reg();
				}else{
					Log.d(TAG,"already have a reg ID::"+regId);					// 나중에 달아..
					try {
						REGISTRATION_ID = regId;			// 안해도 되지만 서버에 값이 잘못 들어 있을 경우 문제가 될수 있기 때문에 값이 있다면 한번더 업뎃을 해준다. 
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


	///////////////////////////////////////// GCM 등록 메소드 ///////////////////////////////////////    
	//GCM 등록
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

	///////////////////////////////////////// GCM 등록 위한 메소드들 //////////////////////////////////    
	
	
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
			System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
		} catch (Exception e) {
			Log.d("testGCM", "Fail to register category.");
		}
	}


	public void checkDoneAndDoGCM(){
		slow = slow +1;
		if(slow==3){				// 3번 실행할때마다 추가 등록.(실패했을까봐)
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

	
	// 리시버 등록, 해제하여 시작시 나오는 리시버 해제했냐는 질문 로그가 나오지 않도록 한다. 실제 푸시 받는것은 서비스단에서..
	BroadcastReceiver mMyBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w(TAG,"intent.getExtras().getString(EXTRA_MESSAGE):"+intent.getStringExtra("MESSAGE"));
			//		if(intent.getAction().equals(DISPLAY_MESSAGE_ACTION)) {
			// Broadcast를 들으면 할 일
			//			Toast.makeText(Main_TabsActivity.this, "(테스트)메시지가 도착하였습니다."+intent.getExtras().getString(EXTRA_MESSAGE), Toast.LENGTH_SHORT).show();
			//		}
		}
	};
	@Override
	protected void onResume() {
//		Log.i(TAG, "onResume");
//		registerReceiver(mMyBroadcastReceiver, new IntentFilter("receive받을 이름"));
		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
		super.onResume();
	};

	@Override
	protected void onPause() {
		unregisterReceiver(mMyBroadcastReceiver);
		super.onPause();
		 // 홈버튼 눌렀을때 종료 여부..
      if(!isForeGround()){
            Log.d(TAG,"go home, bye");
            dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
            finish();
      }
	}
	
	/*
     * 프로세스가 최상위로 실행중인지 검사.
     * @return true = 최상위
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
