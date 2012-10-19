package kr.co.bettersoft.checkmileage.pref;

import java.util.List;

import kr.co.bettersoft.checkmileage.GCMIntentService;
import kr.co.bettersoft.checkmileage.MainActivity;
import kr.co.bettersoft.checkmileage.Main_TabsActivity;
import kr.co.bettersoft.checkmileage.MyMileagePageActivity;


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
	    isRunningProcess(this, "co.kr.bettersoft.checkmileage_mobile_android_phone_customer");
	    if(count==1){		// ���� ����.(���ۿ�����)	
	    	// �׽�Ʈ �� ����� ����. �׳� ����. ���ϸ��� ������׵� �ǹ̰� ����. ������ ��ȸ.
	    	Intent intent = new Intent(DummyActivity.this, MainActivity.class);
	    	if(RunMode.equals("MILEAGE")){
	    		intent.putExtra("RunMode", "MILEAGE");
	    	}else if(RunMode.equals("MARKETING")){		// �̺�Ʈ Ǫ���� ��� �ش� �̺�Ʈ ȭ���� ������� �Ѵ�. �� ����Ʈ�� ��Ƽ��Ƽ�� �������ָ� �ȴ�. ������ ����. ������ �ϰ����� ���ش�.
	    		intent.putExtra("RunMode", "MARKETING");
	    		// �̺�Ʈ ȭ���� ���� ���� �ö�;� ����.
	    		// ... ����Ʈ �ۼ��ؼ� ��������ش�.
	    	}//  �� �ܿ��� ���� ���� �ʴ´�.
		    startActivity(intent);
		    
		    

		    finish();

	    }else{				// �̹� ������.
	    	count = 0;		// �ʱ�ȭ ���ش�. �����ϰ� �ٽ� ������ �� �ְ�..
	    	
	    	//// �׽�Ʈ
	    	if(RunMode.equals("TEST")){			// �׳� �׽�Ʈ��. ���߿� �����.
	    		MyMileagePageActivity.searched = false;		// �׳� �׽�Ʈ��. ���߿� �����.
	    		Main_TabsActivity.tabhost.setCurrentTab(2);
	    	}
	    	//// �׽�Ʈ
	    	
	    	if(RunMode.equals("MILEAGE")){			// ���ϸ����� ��쿡�� �� ���ϸ��� ��� �� ��ȸ �ǵ��� ���� ���� �������ش�.	
	    		MyMileagePageActivity.searched = false;		// ...		// �� ���ϸ��� ��� ��ȸ ���� �� ���� ���� ��..
	    		Main_TabsActivity.tabhost.setCurrentTab(1);				// �ϴ� �迡 �� ���ϸ��� ������ �̵������ش�.
	    	}else if(RunMode.equals("MARKETING")){		// �̺�Ʈ Ǫ���� ��� �ش� �̺�Ʈ ȭ���� ������� �Ѵ�. �̹� �������̴ϱ� �� ����Ʈ�� ��Ƽ��Ƽ�� �������ָ� �ȴ�.
	    		// ... ����Ʈ �ۼ��ؼ� ��������ش�.
	    	}//  �� �ܿ��� ���� ���� �ʴ´�.
	    	
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