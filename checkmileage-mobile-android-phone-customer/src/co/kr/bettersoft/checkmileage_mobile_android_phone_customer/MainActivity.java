package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ��Ʈ��
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
    static String myQR = "";
    static int qrResult = 0;
    
    static CommonUtils cu = new CommonUtils();
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i("MainActivity", "Success Starting MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        readQR();			// �ϴ� ����� QR ���� �ִ������� Ȯ�� �Ѵ�.. 
//        saveQR();			// QR �ڵ� ����ҿ� �ӽ� �� ����. �׽�Ʈ��. 
//        initialQR();		// QR �ڵ� ����ҿ� �� �ʱ�ȭ. �׽�Ʈ��.
//        Log.i("MainActivity", "qrResult::"+qrResult);		// ���� ��� �ޱ� ���̱� ������ ���⼭ Ȯ�� �Ұ�.. �Ʒ� thread ���ο��� Ȯ�� ����.
        
        new Thread(
        		new Runnable(){
        			public void run(){
        				try{
        					Thread.sleep(1000);
        					// ��ݱ�� ���� ��� �Է� �������� �̵�..
        					     // �� ���� �̱���.
        					Log.i("MainActivity", "qrResult::"+qrResult);		// �б� ��� ����.
        					while(qrResult!=1){		// ���� ����� ���� �б� ������(���Ͼ�������. �����ڵ�:-3) --> ���� �����Ѵ�.
        						Log.i("MainActivity", "there is no saved file detected.. generate new one.");	
        						initialQR();
        						Thread.sleep(1000);
        					}
        					// QR �ڵ尡 �ִٸ� QR ȭ������ �̵��ϰ�, QR �ڵ尡 ���ٸ� QR ��� ȭ������ �̵��Ѵ�.
        					if(myQR.length()>0){ // QR�ڵ尡 �ִ��� Ȯ��. ������ �ٷ� �� QR �������� �̵�.
        						Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);
        						Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
        						startActivity(intent);
        						finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����. 
        					}else {				// QR �ڵ尡 ������ ��ġ�� ���� �����ϴ� ���. 
        						/*
        						 *  ������ ���� ���� ��������� Ȯ���� �ʿ��ϴ�.
        						 *  QR ���� ���Ͽ� QR ���� ���� �ÿ��� ���� ��ġ �� ���� �����̹Ƿ� ������ �޾ƾ� �Ѵ�..
        						 *  ���� 1�ܰ��� [�޴��� ��ȣ ����]����  ������ ����� �Ͽ� ���� ��ϵ� ��������� Ȯ���� �Ѵ�. (���� ��ϵ� ����ڶ�� ���� ����� QR �ڵ带 �޾Ƽ� �״�� ���) 
        						 *  �������� QR ���� ���� ��쿡�� 2�� ����(������ȣ ����) �Ŀ� QR ���� ���� â���� �̵��Ѵ�.
        						 *  1�� ������ ���� �������� QR ���� �޾ƿ� ��� ���� 2�ܰ��� [������ȣ Ȯ��] ������ �����ϰ� �� QR���� ȭ������ �̵��Ѵ�. 
        						 */
        				        
        						//QR ���� ���� â���� �̵�.
        						Log.i("MainActivity", "There is no saved QR code.. Go get QR");
        						Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);
        						startActivity(intent);
        						finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
        					}
        				}catch(InterruptedException ie){
        					ie.printStackTrace();
        				}
        			}
        		}
        ).start();
    }
    
    // QR �ڵ� ����ҿ��� QR �ڵ带 �о�´�.
    public void readQR(){
    	CommonUtils.callCode = 1;			// �б� ���.
    	Intent getQRintent = new Intent(MainActivity.this, CommonUtils.class);		// ȣ��
    	startActivity(getQRintent);
    }
    
    // QR �ڵ� ����ҿ� QR �ڵ带 �����Ѵ�. (�׽�Ʈ��)
    public void saveQR(){		
    	CommonUtils.callCode = 2;		// ���� ���
    	Intent saveQRintent = new Intent(MainActivity.this, CommonUtils.class);			// ȣ��
    	startActivity(saveQRintent);
    }
    
    // QR �ڵ� ����ҿ� �ִ� QR �ڵ� ������ �ʱ�ȭ�Ѵ�. (�׽�Ʈ��)
    public void initialQR(){		
    	CommonUtils.callCode = 3;		// �ʱ�ȭ ���
    	Intent initQRintent = new Intent(MainActivity.this, CommonUtils.class);		// ȣ��
    	startActivity(initQRintent);
    }
    
    
    // �ϵ���� �޴� ��ư ������ ���... ��� ���� ���� ����.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    // ����Ʈ�� ����� �޴� �뵵�� ȣ���Ͽ��� ��� (� ȣ������ ������ requestCode �� ���) �� ����� �޾Ƽ� ó���ϴ� �κ�.
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
