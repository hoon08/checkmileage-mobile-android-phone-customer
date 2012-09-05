package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 메인 메뉴들. 탭1:내QR보기 , 탭2:내마일리지, 탭3:가맹점목록, 탭4:설정
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.EXTRA_MESSAGE;
import static co.kr.bettersoft.checkmileage_mobile_android_phone_customer.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.pref.DummyActivity;

import android.R.drawable;
import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Button;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class Main_TabsActivity extends TabActivity {
	String TAG ="Main_TabsActivity";
	public static Activity main_TabsActivity;

	String controllerName = "";
	String methodName = "";

	String myQR = "";

	/*
	 * // 각 페이지에서 제거하는게 확실하므로..주석처리함. 
	 * (실행후 푸시 선택시 각 페이지의 onDesroy 가 올바르게 실행되지 않는 현상이 있음)
	 * //	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;			
		//	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	 * 
	 */

	static String barCode = "";
	TabHost tabhost;

	///////////////////////////////  // GCM 
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// 등록아이디

	int waitEnd = 0;		// test GCM 대기용
	int slow = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);
		main_TabsActivity = Main_TabsActivity.this;		// 다른데서 여기 종료시키기 위함.
		//        tabhost = getTabHost();

		Intent receiveIntent = getIntent();
		myQR = receiveIntent.getStringExtra("myQR");
		nextProcessing();

		tabhost = (TabHost) findViewById(android.R.id.tabhost);
		//            setupTab(new TextView(this), "Tab 1");
		//            setupTab(new TextView(this), "Tab 2");
		//            setupTab(new TextView(this), "Tab 3");
		//            setupTab(new TextView(this), "Tab 4");


		//         Drawable d1 = getResources().getDrawable(R.drawable.tab01_indicator);
		//        View tapParent = findViewById(R.id.tapParent);
		final View tvTab1 = findViewById(R.id.tabText1); 
		((ViewGroup)tvTab1.getParent()).removeView(tvTab1); 
		final View tvTab2 = findViewById(R.id.tabText2); 
		((ViewGroup)tvTab2.getParent()).removeView(tvTab2); 
		final View tvTab3 = findViewById(R.id.tabText3); 
		((ViewGroup)tvTab3.getParent()).removeView(tvTab3); 
		final View tvTab4 = findViewById(R.id.tabText4); 
		((ViewGroup)tvTab4.getParent()).removeView(tvTab4); 

		tvTab1.setBackgroundColor(Color.GRAY);		// 시작 색

		
//		tvTab1.setOnClickListener((new OnClickListener(){			// 안먹음
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				tvTab1.setBackgroundColor(Color.GRAY);
//				tvTab2.setBackgroundColor(Color.BLACK);
//				tvTab3.setBackgroundColor(Color.BLACK);
//				tvTab4.setBackgroundColor(Color.BLACK);
//			}
//		}));
//		tvTab2.setOnClickListener((new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				tvTab1.setBackgroundColor(Color.BLACK);
//				tvTab2.setBackgroundColor(Color.GRAY);
//				tvTab3.setBackgroundColor(Color.BLACK);
//				tvTab4.setBackgroundColor(Color.BLACK);
//			}
//		}));
//		tvTab3.setOnClickListener((new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				tvTab1.setBackgroundColor(Color.BLACK);
//				tvTab2.setBackgroundColor(Color.BLACK);
//				tvTab3.setBackgroundColor(Color.GRAY);
//				tvTab4.setBackgroundColor(Color.BLACK);
//			}
//		}));
//		tvTab4.setOnClickListener((new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				tvTab1.setBackgroundColor(Color.BLACK);
//				tvTab2.setBackgroundColor(Color.BLACK);
//				tvTab3.setBackgroundColor(Color.BLACK);
//				tvTab4.setBackgroundColor(Color.GRAY);
//			}
//		}));
		tvTab1.setOnTouchListener((new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//				Toast.makeText(Main_TabsActivity.this, "strr33",Toast.LENGTH_SHORT).show();
				tvTab1.setBackgroundColor(Color.GRAY);
				tvTab2.setBackgroundColor(Color.BLACK);
				tvTab3.setBackgroundColor(Color.BLACK);
				tvTab4.setBackgroundColor(Color.BLACK);
				return false;
			}
		}));
		tvTab2.setOnTouchListener((new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//				Toast.makeText(Main_TabsActivity.this, "strr33",Toast.LENGTH_SHORT).show();
				tvTab1.setBackgroundColor(Color.BLACK);
				tvTab2.setBackgroundColor(Color.GRAY);
				tvTab3.setBackgroundColor(Color.BLACK);
				tvTab4.setBackgroundColor(Color.BLACK);
				return false;
			}
		}));
		tvTab3.setOnTouchListener((new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//				Toast.makeText(Main_TabsActivity.this, "strr33",Toast.LENGTH_SHORT).show();
				tvTab1.setBackgroundColor(Color.BLACK);
				tvTab2.setBackgroundColor(Color.BLACK);
				tvTab3.setBackgroundColor(Color.GRAY);
				tvTab4.setBackgroundColor(Color.BLACK);
				return false;
			}
		}));
		tvTab4.setOnTouchListener((new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//				Toast.makeText(Main_TabsActivity.this, "strr33",Toast.LENGTH_SHORT).show();
				tvTab1.setBackgroundColor(Color.BLACK);
				tvTab2.setBackgroundColor(Color.BLACK);
				tvTab3.setBackgroundColor(Color.BLACK);
				tvTab4.setBackgroundColor(Color.GRAY);
				return false;
			}
		}));

		//Toast.makeText(Main_TabsActivity.this, "strr33",Toast.LENGTH_SHORT).show();

		//        TextView tvTab1 = (TextView) findViewById(R.id.tabText1); 
		//        tvTab1.setOnClickListener(new Button.OnClickListener()  {
		//			public void onClick(View v)  {
		//				tabhost.setCurrentTab(0);
		//				
		//				
		////				Intent myqrIntent = new Intent(Main_TabsActivity.this, MyQRPageActivity.class);
		////				startActivity(myqrIntent);
		//			}
		//		});
		//        ViewParent parentview =  tvTab1.getParent();
		tabhost.addTab(
				tabhost.newTabSpec("tab_1")
				//        		.setIndicator("내QR코드", getResources().getDrawable(R.drawable.tab01_indicator))
				.setIndicator((View)tvTab1)
				.setContent(new Intent(this, MyQRPageActivity.class)));
		// Optimizer.class 소스는 tab_1 탭에에 속함. Optimizer.java
		//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

		tabhost.addTab(tabhost.newTabSpec("tab_2")
				//        		.setIndicator("마일리지", getResources().getDrawable(R.drawable.tab02_indicator))
				//        		.setIndicator("마일리지")
				.setIndicator((View)tvTab2)
				.setContent(new Intent(this, MyMileagePageActivity.class)));  
		tabhost.addTab(tabhost.newTabSpec("tab_3")
				//        		.setIndicator("가맹점", getResources().getDrawable(R.drawable.tab03_indicator))
				//        		.setIndicator("가맹점")
				.setIndicator((View)tvTab3)
				.setContent(new Intent(this, MemberStoreListPageActivity.class)));

		// 설정
		tabhost.addTab(tabhost.newTabSpec("tab_4")
				//        		.setIndicator("탭4", getResources().getDrawable(R.drawable.tab04_indicator))
				//        		.setContent(R.id.fourth));
				//        		.setIndicator("설정", getResources().getDrawable(R.drawable.tab04_indicator))
				//        		.setIndicator("설정")
				.setIndicator((View)tvTab4)
				.setContent(new Intent(this, com.pref.PrefActivityFromResource.class)));  
	}


	//	private void setupTab(final View view, final String tag) {
	//	    View tabview = createTabView(tabhost.getContext(), tag);
	//	        TabSpec setContent = tabhost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
	//	        public View createTabContent(String tag) {return view;}
	//	    });
	//	        tabhost.addTab(setContent);
	//	}
	//
	//	private static View createTabView(final Context context, final String text) {
	//	    View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
	//	    TextView tv = (TextView) view.findViewById(R.id.tabsText);
	//	    tv.setText(text);
	//	    return view;
	//	}


	////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
	public void nextProcessing(){
		////////////////////////////////////////////GCM 세팅        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);					// 임시 중지  ->해제
		GCMRegistrar.checkManifest(this);				
		Log.i(TAG, "registerReceiver1 ");
		final String regId = GCMRegistrar.getRegistrationId(this);
		final Context context = this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				//boolean registered =
				//ServerUtilities.register(context, regId);
				//if (!registered) {
				if(regId==null || regId.length()<1){		// 등록 되어있으면 재등록. --> 유지해봄.. 안되있으면?
					//GCMRegistrar.unregister(context);
					reg();
				}else{
					Log.e(TAG,"already have a reg ID::"+regId);
					try {
						REGISTRATION_ID = regId;
						updateMyGCMtoServer();
						testGCM(REGISTRATION_ID);				
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
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
		try{
			//reg();
			registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// 리시버 등록.

		}catch(Exception e){
			e.printStackTrace();
		}
	}


	///////////////////////////////////////// GCM 등록 메소드 ///////////////////////////////////////    
	//GCM 등록
	public void reg(){
		//    	REGISTRATION_ID = GCMRegistrar.getRegistrationId(this);
		new Thread(
				new Runnable(){
					public void run(){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally{
							checkDoneAndDoGCM();
						}
					}
				}
		).start();
	}
	//등록 해제
	public void unreg(){
		GCMRegistrar.unregister(this);			// delete from server for re reg
	}

	///////////////////////////////////////// GCM 등록 위한 메소드들 //////////////////////////////////    
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
					getString(R.string.error_config, name));
		}
	}
	private final BroadcastReceiver mHandleMessageReceiver =
		new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			//            mDisplay.append(newMessage + "\n");
			Toast.makeText(Main_TabsActivity.this, "(테스트)메시지가 도착하였습니다."+intent.getExtras().getString(EXTRA_MESSAGE), Toast.LENGTH_SHORT).show();		// 동작 됨..
		}
	};
	public void testGCM(String registrationId) throws JSONException, IOException {
		Log.i("testGCM", "testGCM");
		JSONObject jsonMember = new JSONObject();
		jsonMember.put("registrationId", registrationId);
		String jsonString = "{\"checkMileageMember\":" + jsonMember.toString() + "}";
		//Log.i("testGCM", "jsonMember : " + jsonString);
		try {
			URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageMemberController/testGCM");
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
			//    		  connection2.getInputStream()  -> buffered reader 에 넣고 읽는다.  str 을 jsonobject 에 넣고 도메인 이름으로 꺼낸다..
		} catch (Exception e) {
			// TODO: handle exception
			//   resultGatheringMessage.setResult("FAIL");
			Log.e("testGCM", "Fail to register category.");
		}
	}


	public void checkDoneAndDoGCM(){
		slow = slow +1;
		if(slow==3){
			slow = 0;
			slowingReg();
		}
		new Thread(
				new Runnable(){
					public void run(){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally{
							REGISTRATION_ID = GCMRegistrar.getRegistrationId(getThis());	
							if(REGISTRATION_ID.length()<1){
								Log.i("testGCM", "wait..");
								checkDoneAndDoGCM();
							}else{
								Log.i("testGCM", "now go with : "+REGISTRATION_ID);
								try {
									updateMyGCMtoServer();
									testGCM(REGISTRATION_ID);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
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

							Log.e(TAG, "checkMileageId:"+myQR);
							Log.e(TAG, "registrationId:"+REGISTRATION_ID);
							Log.e(TAG, "modifyDate:"+getNow());

							/*
							 * checkMileageId
registerationId
activateYn
modifyDate
							 */
							//  checkMileageMember  CheckMileageMember
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

		String tempMonth = Integer.toString(todayMonth);
		String tempDay = Integer.toString(todayDay);
		String tempHour = Integer.toString(todayHour);
		String tempMinute = Integer.toString(todayMinute);

		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
		if(tempDay.length()==1) tempDay = "0"+tempDay;
		if(tempHour.length()==1) tempHour = "0"+tempHour;
		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;

		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute;
		return nowTime;
		//Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}

	@Override			// 이 액티비티(인트로)가 종료될때 실행. (액티비티가 넘어갈때 종료됨)
	protected void onDestroy() {
		super.onDestroy();
		/* 
		 * 각페이지에서 하는게 확실하므로.. 설명은 위에..
		 * //		Log.e("Main_TabsActivity","kill all");
//		mainActivity.finish();
//		dummyActivity.finish();		// 더미도 종료
//		DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
		 * 
		 */

	}

}
