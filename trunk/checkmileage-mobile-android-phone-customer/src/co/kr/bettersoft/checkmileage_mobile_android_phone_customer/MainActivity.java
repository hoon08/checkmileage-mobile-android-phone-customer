package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 인트로
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i("MainActivity", "Success Starting MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        
        new Thread(
        		new Runnable(){
        			public void run(){
        				try{
        					Thread.sleep(1000);
        					// 잠금기능 사용시 비번 입력 페이지로 이동..
        					     // 은 아직 미구현.
        					
        					// QR 코드가 있다면 QR 화면으로 이동하고, QR 코드가 없다면 QR 등록 화면으로 이동한다.
        					if(false){
        						// ... // QR코드가 있는지 확인. properties 파일 이용.
        						Log.i("MainActivity", "Go Main Pages");
        						Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
        						
        						startActivity(intent);
        						
        						
        						
        						
        						finish();		// 다른 액티비티를 호출하고 자신은 종료. 
        					}else {
        						Log.i("MainActivity", "Go get QR");
//        						Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);
        						Intent intent = new Intent(MainActivity.this, CommonUtils.class);
        						startActivity(intent);
        						finish();		// 다른 액티비티를 호출하고 자신은 종료.
        						// ...
        					}
        				}catch(InterruptedException ie){
        					ie.printStackTrace();
        				}
        			}
        		}
        ).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
