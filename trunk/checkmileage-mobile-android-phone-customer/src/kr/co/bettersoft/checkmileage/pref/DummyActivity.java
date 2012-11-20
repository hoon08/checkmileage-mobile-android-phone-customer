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
	    
		 if(RunMode!=null && RunMode.length()>0){		// 데이터 전달이 가능하다.
			 Log.d(TAG, RunMode);
		 }else{
			 Log.d(TAG, "NORMAL");	
			 RunMode = "NORMAL";
		 }
	    
	    // 결과는 true 가 나온다.
	    isRunningProcess(this, CommonUtils.packageNames);
	    if(count==1){		// 최초 실행.(나밖에없음)	
	    	// 테스트 및 노멀은 같다. 그냥 실행. 마일리지 변경사항도 의미가 없다. 어차피 조회.
	    	Intent intent = new Intent(DummyActivity.this, MainActivity.class);
	    	if(RunMode.equals("MILEAGE")){
	    		intent.putExtra("RunMode", "MILEAGE");
	    	}else if(RunMode.equals("MARKETING")){		// 이벤트 푸쉬일 경우 해당 이벤트 화면을 보여줘야 한다. 새 인텐트로 액티비티를 실행해주면 된다. 문제는 순서. 위에거 하고나서 해준다.
	    		intent.putExtra("RunMode", "MARKETING");			// 현재 마일리지 모드만 반응한다.
	    		// 이벤트 화면이 가장 위에 올라와야 인정.
	    		// ... 인텐트 작성해서 실행시켜준다.
	    	}//  그 외에는 동작 하지 않는다.
		    startActivity(intent);
	    }else{				// 이미 실행중.
//	    	count = 0;		// 초기화 해준다. 종료하고 다시 실행할 수 있게..
	    	count = count -1;
	    	if(count<0){
	    		count = 0;
	    	}
	    	if(RunMode.equals("MILEAGE")){			// 마일리지일 경우에는 내 마일리지 목록 재 조회 되도록 변수 값을 설정해준다.	
	    		MyMileagePageActivity.searched = false;		// ...		// 내 마일리지 목록 조회 변수 값 설정 해줄 것..
	    		Main_TabsActivity.tabhost.setCurrentTab(1);				// 하는 김에 내 마일리지 탭으로 이동시켜준다.
	    	}else if(RunMode.equals("MARKETING")){		// 이벤트 푸쉬일 경우 해당 이벤트 화면을 보여줘야 한다. 이미 실행중이니까 새 인텐트로 액티비티만 실행해주면 된다.
	    		
	    		Log.d(TAG,"MARKETING");
	    		message = receiveIntent.getStringExtra("message");	
	    		Log.d(TAG,"receiveIntent.getStringExtra():"+message);
	    		
	    		Intent PushListIntent = new Intent(DummyActivity.this, kr.co.bettersoft.checkmileage.activities.PushList.class);
				startActivity(PushListIntent);
	    		
	    		// ... 인텐트 작성해서 실행시켜준다.
	    	}//  그 외에는 동작 하지 않는다.
	    }
//	    }
	    finish();
	}
	
	
	// 항상 기동중..
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
    			count= count+1;		// 실행중인 캐럿수 (중복 실행 방지 용)
    			Log.d("Log","count:"+count);
    			break;
    			}
    		}
    	return isRunning;
    	}
	
	// mainActivity.finish();		// 메인 종료 -> 리시버 종료
	
}
