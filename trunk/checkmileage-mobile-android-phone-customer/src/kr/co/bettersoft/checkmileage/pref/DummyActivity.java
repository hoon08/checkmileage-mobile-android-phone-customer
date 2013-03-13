package kr.co.bettersoft.checkmileage.pref;

import java.util.List;

import kr.co.bettersoft.checkmileage.activities.CertificationStep1;
import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.CommonUtils;
import kr.co.bettersoft.checkmileage.activities.GCMIntentService;
import kr.co.bettersoft.checkmileage.activities.MainActivity;
import kr.co.bettersoft.checkmileage.activities.Main_TabsActivity;
import kr.co.bettersoft.checkmileage.activities.MyMileagePageActivity;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
/**
 * DummyActivity
 * 
 * 어플 시작시 가장 먼저 실행됨.
 * 어플이 이미 실행중에 푸시로 다시 실행된 경우 하는일 없이 죽어준다.
 * 어플이 실행중이 아니라면 어플 정상 실행된다.
 * 
 *  단 푸시 목록은 설정에서 가는건데 여기서 보내는건 그거 말고 새 페이지. 
 *  (연결된 곳 없이 닫으면 종료 되는걸로.. )
 */
public class DummyActivity extends Activity {
	RunningAppProcessInfo runningappprocessinfo = new RunningAppProcessInfo();
	public static int count = 0;
	public static Activity dummyActivity;

	// 설정 파일 저장소  
	SharedPreferences sharedPrefCustom;
	
	String TAG = "DummyActivity";
	String RunMode = "";			// 전달받은 실행모드. 기본/마일리지/이벤트 등
	String message;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		dummyActivity = DummyActivity.this;
		// TODO Auto-generated method stub

		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		
		// receive intent
		Intent receiveIntent = getIntent();
		RunMode = receiveIntent.getStringExtra("RunMode");					// TEST  MILEAGE  MARKETING  NORMAL

		if(RunMode!=null && RunMode.length()>0){		// 데이터 전달이 가능하다.
			Log.d(TAG, RunMode);
		}else{
			Log.d(TAG, "NORMAL");		// 없으면 기본
			RunMode = "NORMAL";
		}
		count = 0;		// 버그 수정용 초기화
		// 결과는 true 가 나온다.
		isRunningProcess(this, CommonUtils.packageNames);		// 실행중인지 확인.
		if(count==1){		// 최초 실행.(나밖에없음)	
			// 테스트 및 노멀은 같다. 그냥 실행한다. 마일리지 변경사항, 이벤트는 알려줘야 한다.
			
			Intent intent;
			// 동의 여부 체크하여 진행한다.
			if(checkUserAgreed()){	// 이전에 동의한 경우 : 다음 프로세스 진행.
				Log.d(TAG,"checkUserAgreed");
				intent = new Intent(DummyActivity.this, MainActivity.class);	
			}else{			// 이전에 동의 하지 않은 경우 : 동의를 받는다. 사용자 동의 -> 전번인증 -> 다음 단계.. 는 인트로. MainActivity
				intent = new Intent(DummyActivity.this, CertificationStep1.class);
			}
			if(RunMode.equals("MILEAGE")){
				intent.putExtra("RunMode", "MILEAGE");		// 마일리지 변경일때는 알려준다.
			}else if(RunMode.equals("MARKETING")){		// 이벤트 푸쉬일 경우 해당 이벤트 화면을 보여줘야 한다. 새 인텐트로 액티비티를 실행해주면 된다. 문제는 순서. 위에거 하고나서 해준다.
				intent.putExtra("RunMode", "MARKETING");			// 현재 마일리지 모드만 반응한다.
				// 이벤트 화면이 가장 위에 올라와야 인정. (새 인텐트를 실행) -- 메인에서 처리한다.
				// ... 인텐트 작성해서 실행시켜준다. 
			}//  그 외에는 추가 동작 하지 않는다.
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
			}//  그 외에는(일반,테스트) 동작 하지 않는다. (이미 실행중이므로)
		}
		//	    }
		finish();
	}


	// 항상 기동중이기때문에 결과는 true.    중복 실행 방지를 위해 카운팅 체크를 한다.
	/**
	 * isRunningProcess
	 *  중복 실행 방지를 위해 카운팅 체크를 한다.
	 *
	 * @param context
	 * @param packageName
	 * @return isRunning
	 */
	public static boolean isRunningProcess(Context context, String packageName) {
		boolean isRunning = false;
		ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
		List<RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
		for(RunningAppProcessInfo rap : list)
		{
			//    		Log.e("Log","rap.processName:"+rap.processName+"/packageName:"+packageName);
			if(rap.processName.equals(packageName))
			{
				//    			Log.d("Log","packageName=packageName/"+packageName);
				isRunning = true;
				count= count+1;		// 실행중인 캐럿수 (중복 실행 방지 용)
				Log.d("Log","count:"+count);
				break;
			}
		}
		return isRunning;
	}
	// mainActivity.finish();		// 메인 종료 -> 리시버 종료  --> 여기서 처리하지 않는다.
	
	
	
	
	// 이전에 동의한 적이 있는지 여부를 확인한다.
	public Boolean checkUserAgreed(){
		String agreedYN = sharedPrefCustom.getString("agreedYN", "N");		// 동의 했는지 여부
		if(agreedYN.equals("Y")){		// 이전에 동의한 경우
			Log.d(TAG,"already agree");
//			return false; // test용. test *** 
			return true;	// real
		}else{							// 이전에 동의 안한 경우
			Log.d(TAG,"need agree");
			return false;
		}
	}
	
	
	
	
}
