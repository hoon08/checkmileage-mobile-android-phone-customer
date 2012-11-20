package kr.co.bettersoft.checkmileage.pref;

import java.util.List;

import kr.co.bettersoft.checkmileage.activities.CommonUtils;
import kr.co.bettersoft.checkmileage.activities.GCMIntentService;
import kr.co.bettersoft.checkmileage.activities.MainActivity;
import kr.co.bettersoft.checkmileage.activities.Main_TabsActivity;
import kr.co.bettersoft.checkmileage.activities.MyMileagePageActivity;
import kr.co.bettersoft.checkmileage.activities.R;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
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
	
	String TAG = "DummyActivity";
	String RunMode = "";
	String message;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    dummyActivity = DummyActivity.this;
	    // TODO Auto-generated method stub
	   
	    Intent receiveIntent = getIntent();
	    RunMode = receiveIntent.getStringExtra("RunMode");					// TEST  MILEAGE  MARKETING  NORMAL
	    
		 if(RunMode!=null && RunMode.length()>0){		// ������ ������ �����ϴ�.
			 Log.d(TAG, RunMode);
		 }else{
			 Log.d(TAG, "NORMAL");	
			 RunMode = "NORMAL";
		 }
	    
	    // ����� true �� ���´�.
	    isRunningProcess(this, CommonUtils.packageNames);
	    if(count==1){		// ���� ����.(���ۿ�����)	
	    	// �׽�Ʈ �� ����� ����. �׳� ����. ���ϸ��� ������׵� �ǹ̰� ����. ������ ��ȸ.
	    	Intent intent = new Intent(DummyActivity.this, MainActivity.class);
	    	if(RunMode.equals("MILEAGE")){
	    		intent.putExtra("RunMode", "MILEAGE");
	    	}else if(RunMode.equals("MARKETING")){		// �̺�Ʈ Ǫ���� ��� �ش� �̺�Ʈ ȭ���� ������� �Ѵ�. �� ����Ʈ�� ��Ƽ��Ƽ�� �������ָ� �ȴ�. ������ ����. ������ �ϰ��� ���ش�.
	    		intent.putExtra("RunMode", "MARKETING");			// ���� ���ϸ��� ��常 �����Ѵ�.
	    		// �̺�Ʈ ȭ���� ���� ���� �ö�;� ����.
	    		// ... ����Ʈ �ۼ��ؼ� ��������ش�.
	    	}//  �� �ܿ��� ���� ���� �ʴ´�.
		    startActivity(intent);
	    }else{				// �̹� ������.
//	    	count = 0;		// �ʱ�ȭ ���ش�. �����ϰ� �ٽ� ������ �� �ְ�..
	    	count = count -1;
	    	if(count<0){
	    		count = 0;
	    	}
	    	if(RunMode.equals("MILEAGE")){			// ���ϸ����� ��쿡�� �� ���ϸ��� ��� �� ��ȸ �ǵ��� ���� ���� �������ش�.	
	    		MyMileagePageActivity.searched = false;		// ...		// �� ���ϸ��� ��� ��ȸ ���� �� ���� ���� ��..
	    		Main_TabsActivity.tabhost.setCurrentTab(1);				// �ϴ� �迡 �� ���ϸ��� ������ �̵������ش�.
	    	}else if(RunMode.equals("MARKETING")){		// �̺�Ʈ Ǫ���� ��� �ش� �̺�Ʈ ȭ���� ������� �Ѵ�. �̹� �������̴ϱ� �� ����Ʈ�� ��Ƽ��Ƽ�� �������ָ� �ȴ�.
	    		
	    		Log.d(TAG,"MARKETING");
	    		message = receiveIntent.getStringExtra("message");	
	    		Log.d(TAG,"receiveIntent.getStringExtra():"+message);
	    		
	    		Intent PushListIntent = new Intent(DummyActivity.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
				startActivity(PushListIntent);
	    		
	    		// ... ����Ʈ �ۼ��ؼ� ��������ش�.
	    	}//  �� �ܿ��� ���� ���� �ʴ´�.
	    }
//	    }
	    finish();
	}
	
	
	// �׻� �⵿��..
	public static boolean isRunningProcess(Context context, String packageName) {
    	boolean isRunning = false;
    	ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
    	List<RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
    	for(RunningAppProcessInfo rap : list)
    	{
    		Log.e("Log","rap.processName:"+rap.processName+"/packageName:"+packageName);
    		if(rap.processName.equals(packageName))
    		{
    			Log.d("Log","packageName=packageName/"+packageName);
    			isRunning = true;
    			count= count+1;		// �������� ĳ���� (�ߺ� ���� ���� ��)
    			Log.d("Log","count:"+count);
    			break;
    			}
    		}
    	return isRunning;
    	}
	
	// mainActivity.finish();		// ���� ���� -> ���ù� ����
	
}
