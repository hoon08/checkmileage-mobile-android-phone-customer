package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 인트로
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

/* intro 화면
 * 기능 : 인트로 화면을 보여줌.
 * QR 코드가 있는지 검사하여
 *  QR코드가 있으면 메인시리즈로 이동(메인 시리즈 중 첫화면)
 *  QR코드가 없다면 QR 선택 페이지로 이동하여 신규 생성 또는 있는 것 등록. 후 메인시리즈로 이동.
 *  
 *  
 */

public class MainActivity extends Activity {
	String TAG = "MainActivity";
	Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		try{
    			Bundle b = msg.getData();
    		}catch(Exception e){
    			Toast.makeText(MainActivity.this, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
    		}
    	}
    };
    static String myQR = "";
    static int qrResult = 0;
    
    static CommonUtils cu = new CommonUtils();
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i("MainActivity", "Success Starting MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        readQR();			// 일단 저장된 QR 값이 있는지부터 확인 한다.. 
//        saveQR();			// QR 코드 저장소에 임시 값 저장. 테스트용. 
//        initialQR();		// QR 코드 저장소에 값 초기화. 테스트용.
//        Log.i("MainActivity", "qrResult::"+qrResult);		// 아직 결과 받기 전이기 때문에 여기서 확인 불가.. 아래 thread 내부에서 확인 가능.
        
        new Thread(
        		new Runnable(){
        			public void run(){
        				try{
        					Thread.sleep(1000);
        					// 잠금기능 사용시 비번 입력 페이지로 이동..
        					     // 은 아직 미구현.
        					Log.i("MainActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
        					while(qrResult!=1){		// 최초 실행시 파일 읽기 실패함(파일없음에러. 에러코드:-3) --> 새로 생성한다.
        						Log.i("MainActivity", "there is no saved file detected.. generate new one.");	
        						initialQR();
        						Thread.sleep(1000);
        					}
        					// QR 코드가 있다면 QR 화면으로 이동하고, QR 코드가 없다면 QR 등록 화면으로 이동한다.
        					if(myQR.length()>0){ // QR코드가 있는지 확인. 있으면 바로 내 QR 페이지로 이동.
        						Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);
        						Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
        						startActivity(intent);
        						finish();		// 다른 액티비티를 호출하고 자신은 종료. 
        					}else {				// QR 코드가 없으면 설치후 최초 실행하는 사람. 
        						/*
        						 *  기존에 인증 받은 사용자인지 확인이 필요하다.
        						 *  QR 저장 파일에 QR 값이 없을 시에는 어플 설치 후 최초 실행이므로 인증을 받아야 한다..
        						 *  인증 1단계인 [휴대폰 번호 인증]으로  서버와 통신을 하여 이전 등록된 사용자인지 확인을 한다. (이전 등록된 사용자라면 이전 등록한 QR 코드를 받아서 그대로 사용) 
        						 *  서버에도 QR 값이 없을 경우에는 2차 인증(인증번호 인증) 후에 QR 생성 선택 창으로 이동한다.
        						 *  1차 인증을 통해 서버에서 QR 값을 받아온 경우 인증 2단계인 [인증번호 확인] 절차를 생략하고 내 QR보기 화면으로 이동한다. 
        						 */
        				        
        						//QR 생성 선택 창으로 이동.
        						Log.i("MainActivity", "There is no saved QR code.. Go get QR");
        						Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);
        						startActivity(intent);
        						finish();		// 다른 액티비티를 호출하고 자신은 종료.
        					}
        				}catch(InterruptedException ie){
        					ie.printStackTrace();
        				}
        			}
        		}
        ).start();
    }
    
    // QR 코드 저장소에서 QR 코드를 읽어온다.
    public void readQR(){
    	CommonUtils.callCode = 1;			// 읽기 모드.
    	Intent getQRintent = new Intent(MainActivity.this, CommonUtils.class);		// 호출
    	startActivity(getQRintent);
    }
    
    // QR 코드 저장소에 QR 코드를 저장한다. (테스트용)
    public void saveQR(){		
    	CommonUtils.callCode = 2;		// 쓰기 모드
    	Intent saveQRintent = new Intent(MainActivity.this, CommonUtils.class);			// 호출
    	startActivity(saveQRintent);
    }
    
    // QR 코드 저장소에 있는 QR 코드 정보를 초기화한다. (테스트용)
    public void initialQR(){		
    	CommonUtils.callCode = 3;		// 초기화 모드
    	Intent initQRintent = new Intent(MainActivity.this, CommonUtils.class);		// 호출
    	startActivity(initQRintent);
    }
    
    
    // 하드웨어 메뉴 버튼 눌렀을 경우... 사용 하지 않을 예정.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    // 인텐트를 결과를 받는 용도로 호출하였을 경우 (어떤 호출인지 구분은 requestCode 를 사용) 그 결과를 받아서 처리하는 부분.
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.e(TAG, "onActivityResult");
    	if(requestCode == 201) {							
    		if(resultCode == RESULT_OK) {			
    			// ...
    		}
    	}
    }
}
