package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ��Ʈ��
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

/* intro ȭ��
 * ��� : ��Ʈ�� ȭ���� ������.
 * QR �ڵ尡 �ִ��� �˻��Ͽ�
 *  QR�ڵ尡 ������ ���νø���� �̵�(���� �ø��� �� ùȭ��)
 *  QR�ڵ尡 ���ٸ� QR ���� �������� �̵��Ͽ� �ű� ���� �Ǵ� �ִ� �� ���. �� ���νø���� �̵�.
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
    			Toast.makeText(MainActivity.this, "������ �߻��Ͽ����ϴ�.", Toast.LENGTH_SHORT).show();
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
        					// ��ݱ�� ���� ��� �Է� �������� �̵�..
        					     // �� ���� �̱���.
        					
        					// QR �ڵ尡 �ִٸ� QR ȭ������ �̵��ϰ�, QR �ڵ尡 ���ٸ� QR ��� ȭ������ �̵��Ѵ�.
        					if(false){
        						// ... // QR�ڵ尡 �ִ��� Ȯ��. properties ���� �̿�.
        						Log.i("MainActivity", "Go Main Pages");
        						Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
        						
        						startActivity(intent);
        						
        						
        						
        						
        						finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����. 
        					}else {
        						Log.i("MainActivity", "Go get QR");
//        						Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);
        						Intent intent = new Intent(MainActivity.this, CommonUtils.class);
        						startActivity(intent);
        						finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
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
