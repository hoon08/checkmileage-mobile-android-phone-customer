package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ScanQRPageActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.scan_qr_page);
	    /* 
		  * QR ���˸�尡 �Ǿ� QR ī�带 �����Ѵ�.
		  * QR ī�� ������ �����Ͽ� QR ������ ������, �ش� ������ �ۿ� �����ϰ�, ������ ����Ѵ�.
		  * ���� ���� QR �ڵ� ���� ȭ������ �̵��Ѵ�.
		  */
		    
		 // QR ī�� �����ϴ� �κ�..
	     // ... QR ī�带 �����Ͽ� ������ �����ϰ�, ������ ����Ѵ�.
		    
		 // ���� QR �ڵ� ����� �̵�.
	    Log.i("ScanQRPageActivity", "QR registered Success");
		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
	        startActivity(intent2);
	        finish();
	}

}
