package kr.co.bettersoft.checkmileage.activities;
/**
 * Main_TabsActivity
 *  ���� �޴���. ��1:��QR���� , ��2:�����ϸ���, ��3:���������, ��4:����
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

	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;			// ����� ���̵� �Բ� ���� ��Ű�� ����

	static String barCode = "";
	public static TabHost tabhost;

	////////////////////////////////////  // GCM 
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String REGISTRATION_ID = "";		// ��Ͼ��̵�

	int waitEnd = 0;		// test GCM ����

	String RunMode = "";		// push ���� ������ ���� ��ġ


	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("unregGCM")==1){				//GCM  unreg -- > ��� ����. 
					Log.d(TAG,"unregGCM - do nothing");
					unregisterReceiver(mMyBroadcastReceiver);				// unregister ����.
					GCMRegistrar.unregister(getThis());
				}
				if(b.getInt("set_tab_0")==1){				//GCM  unreg -- > ��� ����. 
					Log.d(TAG,"set_tab_0");
					tabhost.setCurrentTab(1);		// ���� �� ������ ���� ���.. --> ���ۺ��� ���ϸ��� ��
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
		//		requestWindowFeature(Window.FEATURE_LEFT_ICON);		// Ÿ��Ʋ ���ʿ� ������ �ֱ�- �ȵ�.			FEATURE_NO_TITLE ��   FEATURE_RIGHT_ICON ..
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);
		main_TabsActivity = Main_TabsActivity.this;		// �ٸ����� ���� �����Ű�� ����.

		Intent receiveIntent = getIntent();							// ����Ʈ ���� ���� ���� �� ������.
		if(myQR.length()<1){
			myQR = receiveIntent.getStringExtra("myQR");
		}

		RunMode = receiveIntent.getStringExtra("RunMode");	
		if(RunMode==null){
			RunMode="";
		}
		nextProcessing();			// GCM ���� 
		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));		//  gcm reg..

		//		tabhost = (TabHost) findViewById(android.R.id.tabhost);
		tabhost = getTabHost();

		tabhost.setOnTabChangedListener(this);		// �̰� ����� onTabChanged() ü���� ȿ���� �ִ�

		// ����

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
				//        		.setIndicator("��QR�ڵ�", getResources().getDrawable(R.drawable.tab01_indicator))
				//				.setIndicator((View)tvTab1)
				//				.setIndicator("", getResources().getDrawable(R.drawable.bottom_menu1))			// �ϴ� ��ư�� �̹��� �����.
				//				.setIndicator("12345678901234567890", getResources().getDrawable(R.drawable.tab01_indicator))			// �ϴ� ��ư�� �̹��� �����.
				.setIndicator(getResources().getString(R.string.my_qr_title), getResources().getDrawable(R.drawable.tab01_indicator))			// �ϴ� ��ư�� �̹��� �����.
				.setContent(new Intent(this, MyQRPageActivity.class)));
		// Optimizer.class �ҽ��� tab_1 �ǿ��� ����. Optimizer.java
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






		// Tab�� ���� ����
		new Thread(			// unreg ����
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

						// ���ϸ��� ���� ����ÿ� ���� ��ġ ����
						if(RunMode.length()>0){
							if(RunMode.equals("MILEAGE")){
								Message message = handler.obtainMessage();				
								Bundle b = new Bundle();
								b.putInt("set_tab_0", 1);
								message.setData(b);
								handler.sendMessage(message);
							}else if(RunMode.equals("MARKETING")){		// �������̸� Ǫ�� ����Ʈ�� �߰��� ���
								Intent PushListIntent = new Intent(Main_TabsActivity.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
								MyQRPageActivity.qrCode = myQR;
								startActivity(PushListIntent);
							}
						}
					}
				}
		).start();		



		// locale ���.
		getLocale();
	}

	/**
	 * getLocale
	 *  ����̽����� ����,��� �ڵ� ��´�
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

		// tab ���� ����
		//		for(int i=0; i<tabhost.getTabWidget().getChildCount(); i++){
		//			tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#393939"));
		//		}
		//		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#595959"));

	}

	////////////////////////////////////////////GCM ����        ///////////////////////////////////////////////////////////////
	/**
	 * nextProcessing
	 *  GCM �����Ͽ� ����Ѵ�
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void nextProcessing(){
		////////////////////////////////////////////GCM ����        ///////////////////////////////////////////////////////////////		
		GCMRegistrar.checkDevice(this);					// �ӽ� ����  ->����
		GCMRegistrar.checkManifest(this);				
		Log.i(TAG, "registerReceiver1 ");
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {	// ������ GCM ����Ѵ�. �������̵� �����̵� ����ϰ� //��� ����� ������ �����ϴ� �κ��� GCM���񽺿��� ó���Ѵ�.
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
	///////////////////////////////////////// GCM ��� ���� �޼ҵ�� //////////////////////////////////    
	/**
	 * getThis
	 *  ���ý�Ʈ �����Ѵ�
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
	 *  ���ð� ���Ѵ�
	 *
	 * @param
	 * @param
	 * @return nowTime
	 */
	public String getNow(){
		// �� �ð�
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
			Log.w(TAG,"EXTRA_MESSAGE:"+intent.getStringExtra("MESSAGE"));
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
		//		registerReceiver(mMyBroadcastReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));			// oncreate �� �ű�
		super.onResume();
	};
	/**
	 * onPause
	 *  Ȩ��ư ������ �����Ų��
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	protected void onPause() {
		//		new Thread(			// unreg ����
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
	/**
	 * isForeGround
	 *  ���μ����� �ֻ����� ���������� �˻��Ѵ�. (Ȩ��ư �������� ���� Ȯ�ο�)
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
	 *  ����� ���� ���� �Ѵ�
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
