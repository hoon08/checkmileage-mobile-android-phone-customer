package kr.co.bettersoft.checkmileage.activities;
// ���� �޴���. ��1:��QR���� , ��2:�����ϸ���, ��3:���������, ��4:����
import static kr.co.bettersoft.checkmileage.activities.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static kr.co.bettersoft.checkmileage.activities.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

public class Main_TabsActivity extends TabActivity implements OnTabChangeListener {
	String TAG ="Main_TabsActivity";
	public static Activity main_TabsActivity;
	
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
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
				if(b.getInt("unregGCM")==1){
					Log.d(TAG,"unregGCM");
					unregisterReceiver(mMyBroadcastReceiver);
					GCMRegistrar.unregister(getThis());
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
				.setContent(new Intent(this, kr.co.bettersoft.checkmileage.pref.PrefActivityFromResource.class)));  
		
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
			}else if(RunMode.equals("MARKETING")){
				Intent PushListIntent = new Intent(Main_TabsActivity.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
				MyQRPageActivity.qrCode = myQR;
				startActivity(PushListIntent);
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
	public Context getThis(){
		return this;
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
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("unregGCM", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();		
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
