package kr.co.bettersoft.checkmileage.activities;
/**
 * Main_TabsActivity
 *  메인 메뉴들. 탭1:내QR보기 , 탭2:내마일리지, 탭3:가맹점목록, 탭4:설정
 */
import static kr.co.bettersoft.checkmileage.activities.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static kr.co.bettersoft.checkmileage.activities.CommonUtilities.SENDER_ID;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
import kr.co.bettersoft.checkmileage.pref.PrefActivityFromResource;

import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import com.google.android.gcm.GCMRegistrar;

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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

public class Main_TabsActivity extends TabActivity implements OnTabChangeListener {
	String TAG ="Main_TabsActivity";
	public static Activity main_TabsActivity;

	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;

	static String myQR = "";

	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;			// 종료시 더미도 함께 종료 시키기 위함

	static String barCode = "";
	public static TabHost tabhost;

	////////////////////////////////////  // GCM 
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// 등록아이디

	int waitEnd = 0;		// test GCM 대기용

	String RunMode = "";		// push 통한 실행을 위한 조치


	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("unregGCM")==1){				//GCM  unreg -- > 사용 안함. 
					Log.d(TAG,"unregGCM - do nothing");
					unregisterReceiver(mMyBroadcastReceiver);				// unregister 안함.
					GCMRegistrar.unregister(getThis());
				}
				if(b.getInt("set_tab_0")==1){				//GCM  unreg -- > 사용 안함. 
					Log.d(TAG,"set_tab_0");
					tabhost.setCurrentTab(1);		// 시작 탭 설정을 원할 경우.. --> 시작부터 마일리지 탭
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		//		requestWindowFeature(Window.FEATURE_LEFT_ICON);		// 타이틀 왼쪽에 아이콘 넣기- 안됨.			FEATURE_NO_TITLE 됨   FEATURE_RIGHT_ICON ..
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);
		main_TabsActivity = Main_TabsActivity.this;		// 다른데서 여기 종료시키기 위함.

		Intent receiveIntent = getIntent();							// 인텐트 통해 전달 받은 값 꺼내기.
		if(myQR.length()<1){
			myQR = receiveIntent.getStringExtra("myQR");
		}

		RunMode = receiveIntent.getStringExtra("RunMode");	
		if(RunMode==null){
			RunMode="";
		}
		nextProcessing();			// GCM 세팅 
		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));		//  gcm reg..

		//		tabhost = (TabHost) findViewById(android.R.id.tabhost);
		tabhost = getTabHost();

		tabhost.setOnTabChangedListener(this);		// 이걸 해줘야 onTabChanged() 체인지 효과가 있다

		// 설정

		////		tabhost.getTabWidget().setBackgroundDrawable( getResources().getDrawable(R.drawable.bluenavbar)); 
		//		TextView txtTab = new TextView(this); 
		//		txtTab.setText(getString(R.string.my_qr_title)); 
		//		txtTab.setPadding(0, 0, 0, 0); 
		//		txtTab.setTextColor(Color.WHITE); 
		//		txtTab.setTextSize(8); 
		//////		txtTab.setTypeface(localTypeface1); 
		//		txtTab.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL); 
		//		txtTab.setBackgroundResource(R.drawable.tab01_indicator); 
		//		// Initialize a TabSpec for each tab and add it to the TabHost 
		//		TabSpec spec = tabhost.newTabSpec("spec")
		//				 .setIndicator(txtTab)
		//				 .setContent(new Intent(this, MyQRPageActivity.class)
		//				 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		//		
		//		tabhost.addTab(spec); 


		tabhost.addTab(
				tabhost.newTabSpec("tab_1")
				//        		.setIndicator("내QR코드", getResources().getDrawable(R.drawable.tab01_indicator))
				//				.setIndicator((View)tvTab1)
				//				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu1))			// 하단 버튼을 이미지 사용함.
				//				.setIndicator("12345678901234567890", getResources().getDrawable(R.drawable.tab01_indicator))			// 하단 버튼을 이미지 사용함.
				.setIndicator(getResources().getString(R.string.my_qr_title), getResources().getDrawable(R.drawable.tab01_indicator))			// 하단 버튼을 이미지 사용함.
				.setContent(new Intent(this, MyQRPageActivity.class)));
		// Optimizer.class 소스는 tab_1 탭에에 속함. Optimizer.java
		//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));



		tabhost.addTab(tabhost.newTabSpec("tab_2")
				//				.setIndicator((View)tvTab2)
				//				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu2))
				.setIndicator(getResources().getString(R.string.my_mileage_title), getResources().getDrawable(R.drawable.tab02_indicator))
				.setContent(new Intent(this, MyMileagePageActivity.class)));  
		tabhost.addTab(tabhost.newTabSpec("tab_3")
				//				.setIndicator((View)tvTab3)
				//				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu3))
				.setIndicator(getResources().getString(R.string.search), getResources().getDrawable(R.drawable.tab03_indicator))
				.setContent(new Intent(this, MemberStoreListPageActivity.class)));

		tabhost.addTab(tabhost.newTabSpec("tab_4")
				//				.setIndicator((View)tvTab4)
				//				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu4))
				.setIndicator(getResources().getString(R.string.menu_settings), getResources().getDrawable(R.drawable.tab04_indicator))
				.setContent(new Intent(this, kr.co.bettersoft.checkmileage.pref.PrefActivityFromResource.class)));  






		// Tab에 색상 지정
		new Thread(			// unreg 안함
				new Runnable(){
					public void run(){
						for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
							//							tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#000000"));
							tabhost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.design_bg_tab_menu));
							//							RelativeLayout relLayout = (RelativeLayout)tabhost.getTabWidget().getChildAt(i); 
							//							TextView tv = (TextView)relLayout.getChildAt(i); 
							//							tv.setTextSize(8);
						}
						//						tabhost.getTabWidget().setCurrentTab(0);
						//						tabhost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#000000"));

						// 마일리지 통한 실행시에 대한 조치 사항
						if(RunMode.length()>0){
							if(RunMode.equals("MILEAGE")){
								Message message = handler.obtainMessage();				
								Bundle b = new Bundle();
								b.putInt("set_tab_0", 1);
								message.setData(b);
								handler.sendMessage(message);
							}else if(RunMode.equals("MARKETING")){		// 마케팅이면 푸시 리스트만 추가로 띄움
								Intent PushListIntent = new Intent(Main_TabsActivity.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
								MyQRPageActivity.qrCode = myQR;
								startActivity(PushListIntent);
							}
						}
					}
				}
		).start();		



		// locale 얻기.
		getLocale();
	}

	/**
	 * getLocale
	 *  디바이스에서 국가,언어 코드 얻는다
	 *
	 * @param
	 * @param
	 * @return
	 */
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
		//		for(int i=0; i<tabhost.getTabWidget().getChildCount(); i++){
		//			tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
		//		}
		//		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#595959"));

	}

	////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////
	/**
	 * nextProcessing
	 *  GCM 세팅하여 등록한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void nextProcessing(){
		////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);					// 임시 중지  ->해제
		GCMRegistrar.checkManifest(this);				
		Log.i(TAG, "registerReceiver1 ");
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {	// 무조건 GCM 등록한다. 이전값이든 새값이든 등록하고 //등록 결과를 서버에 업뎃하는 부분은 GCM서비스에서 처리한다.
				GCMRegistrar.register(getThis(), SENDER_ID);	
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				mRegisterTask = null;
			}
		};
		mRegisterTask.execute(null, null, null);
	}
	///////////////////////////////////////// GCM 등록 위한 메소드들 //////////////////////////////////    
	/**
	 * getThis
	 *  컨택스트 리턴한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public Context getThis(){
		return this;
	}

	/**
	 * getNow
	 *  현시각 구한다
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
		// 현 시각
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
			Log.w(TAG,"EXTRA_MESSAGE:"+intent.getStringExtra("MESSAGE"));
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
		//		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// oncreate 로 옮김
		super.onResume();
	};
	/**
	 * onPause
	 *  홈버튼 누르면 종료시킨다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	protected void onPause() {
		//		new Thread(			// unreg 안함
		//				new Runnable(){
		//					public void run(){
		//						Message message = handler.obtainMessage();				
		//						Bundle b = new Bundle();
		//						b.putInt("unregGCM", 1);
		//						message.setData(b);
		//						handler.sendMessage(message);
		//					}
		//				}
		//		).start();		
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
	/**
	 * isForeGround
	 *  프로세스가 최상위로 실행중인지 검사한다. (홈버튼 눌렀는지 여부 확인용)
	 *
	 * @param
	 * @param
	 * @return rtn
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
	/**
	 * onDestroy
	 *  종료시 정상 종료 한다
	 *
	 * @param
	 * @param
	 * @return 
	 */
	@Override
	public void onDestroy(){
		android.os.Process.killProcess(android.os.Process.myPid()); 
		super.onDestroy();
	}
}
