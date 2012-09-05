package com.pref;

import java.util.List;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.GCMIntentService;
import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MainActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
/*
 * 어플 시작시 가장 먼저 실행됨.
 * 어플이 이미 실행중에 푸시로 다시 실행된 경우 하는일 없이 죽어준다.
 * 어플이 실행중이 아니라면 어플 정상 실행된다.
 * 
 * 터치시 발생 이벤트니까 푸쉬 목록 페이지 만들어서 그쪽으로 보내주는 것도 좋음....
 *  단 푸시 목록은 설정에서 가는건데 여기서 보내는건 그거 말고 또다른 페이지. 
 *  (연결된 곳 없이 닫으면 종료 되는걸로.. 왔는데 또열리면 어떻하나.. 3개는 금지?)
 */
public class DummyActivity extends Activity {
	RunningAppProcessInfo runningappprocessinfo = new RunningAppProcessInfo();
	public static int count = 0;
	public static Activity dummyActivity;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    dummyActivity = DummyActivity.this;
	    // TODO Auto-generated method stub
	   
	    // 결과는 true 가 나온다.
	    isRunningProcess(this, "co.kr.bettersoft.checkmileage_mobile_android_phone_customer");
	    if(count==1){
	    	Intent intent = new Intent(DummyActivity.this, MainActivity.class);
		    startActivity(intent);
		    finish();
	    }else{
	    	count = 0;		// 초기화 해준다. 종료하고 다시 실행할 수 있게..
	    	finish();
	    }
//	    }
//	    
	}
	
	
	// 항상 기동중..
	public static boolean isRunningProcess(Context context, String packageName) {
    	boolean isRunning = false;
    	ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
    	List<RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
    	for(RunningAppProcessInfo rap : list)
    	{
    		if(rap.processName.equals(packageName))
    		{
    			isRunning = true;
    			count= count+1;		// 실행중인 캐럿수 (중복 실행 방지 용)
    			break;
    			}
    		}
    	return isRunning;
    	}
	
	
	
	
	// mainActivity.finish();		// 메인 종료 -> 리시버 종료
	
	
	
	
	
}
