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
 * ���� ���۽� ���� ���� �����.
 * ������ �̹� �����߿� Ǫ�÷� �ٽ� ����� ��� �ϴ��� ���� �׾��ش�.
 * ������ �������� �ƴ϶�� ���� ���� ����ȴ�.
 * 
 * ��ġ�� �߻� �̺�Ʈ�ϱ� Ǫ�� ��� ������ ���� �������� �����ִ� �͵� ����....
 *  �� Ǫ�� ����� �������� ���°ǵ� ���⼭ �����°� �װ� ���� �Ǵٸ� ������. 
 *  (����� �� ���� ������ ���� �Ǵ°ɷ�.. �Դµ� �ǿ����� ��ϳ�.. 3���� ����?)
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
	   
	    // ����� true �� ���´�.
	    isRunningProcess(this, "co.kr.bettersoft.checkmileage_mobile_android_phone_customer");
	    if(count==1){
	    	Intent intent = new Intent(DummyActivity.this, MainActivity.class);
		    startActivity(intent);
		    finish();
	    }else{
	    	count = 0;		// �ʱ�ȭ ���ش�. �����ϰ� �ٽ� ������ �� �ְ�..
	    	finish();
	    }
//	    }
//	    
	}
	
	
	// �׻� �⵿��..
	public static boolean isRunningProcess(Context context, String packageName) {
    	boolean isRunning = false;
    	ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
    	List<RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
    	for(RunningAppProcessInfo rap : list)
    	{
    		if(rap.processName.equals(packageName))
    		{
    			isRunning = true;
    			count= count+1;		// �������� ĳ���� (�ߺ� ���� ���� ��)
    			break;
    			}
    		}
    	return isRunning;
    	}
	
	
	
	
	// mainActivity.finish();		// ���� ���� -> ���ù� ����
	
	
	
	
	
}
