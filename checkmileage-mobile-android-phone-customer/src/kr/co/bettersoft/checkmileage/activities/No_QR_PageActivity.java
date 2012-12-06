package kr.co.bettersoft.checkmileage.activities;
// QR ���� ��� QR �߰� ������. -> QR ȹ�� ��� ����. 1.QR����. 2.QR����
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class No_QR_PageActivity extends Activity {

	String phoneNumber= "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Log.i("No_QR_PageActivity", "select method to get QR");
	    setContentView(R.layout.no_qr_page);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        
        Intent rIntent = getIntent();
        phoneNumber = rIntent.getStringExtra("phoneNumber");
        
//        button1.setText("���ο� QR �ڵ带 �����Ͻ÷��� ���⸦ Ŭ���Ͻʽÿ�.");
//        button2.setText("���������� ���� QR ī�带 ����Ͻ÷��� ���⸦ Ŭ���Ͻʽÿ�.");
//        button1.setText(R.string.no_qr_create);
//        button2.setText(R.string.no_qr_scan);
	    // TODO Auto-generated method stub
        
        // QR ���� ��ư Ŭ����.
        button1.setOnClickListener(new OnClickListener() {
        	public void onClick(View V){
        		/* ��ư�� ������ �� ����� �ڵ��Դϴ�. */
        		 Log.i("No_QR_PageActivity", "go Create QR");
        		 Intent intent = new Intent(No_QR_PageActivity.this, CreateQRPageActivity.class);
        		 intent.putExtra("phoneNumber", phoneNumber);
        	        startActivity(intent);
        	        finish();
        	}
        });
        
        // QR ���� ��ư Ŭ����.
        button2.setOnClickListener(new OnClickListener() {
        	public void onClick(View V){
        		/* ��ư�� ������ �� ����� �ڵ��Դϴ�. */
        		 Log.i("No_QR_PageActivity", "go Scan QR");
        		 Intent intent = new Intent(No_QR_PageActivity.this, ScanQRPageActivity.class);
        		 intent.putExtra("phoneNumber", phoneNumber);
        	        startActivity(intent);
        	        finish();
        	}
        });
        
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		 Log.i("No_QR_PageActivity", "recieve sign kill");
		// �ڽ� ����Ʈ. createQR, ScanQR �κ��� ���� ������ ����.
		if(requestCode == 111){
			if(resultCode == RESULT_OK){
				 Log.i("No_QR_PageActivity", "No_QR_PageActivity activity off");
				finish();		// ������.
			}
		}
	}
	
	
	@Override
	public void onBackPressed() {
		// ���⼭ �����ų���� ������ �� �����ϵ��� ī������ �����ؾ��Ѵ�.
		DummyActivity.count = 0;
		finish();
	}
}