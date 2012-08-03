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
		  * QR 스켄모드가 되어 QR 카드를 스켄한다.
		  * QR 카드 스켄이 성공하여 QR 정보를 얻어오면, 해당 정보를 앱에 저장하고, 서버에 등록한다.
		  * 이후 나의 QR 코드 보기 화면으로 이동한다.
		  */
		    
		 // QR 카드 스켄하는 부분..
	     // ... QR 카드를 스켄하여 정보를 저장하고, 서버에 등록한다.
		    
		 // 나의 QR 코드 보기로 이동.
	    Log.i("ScanQRPageActivity", "QR registered Success");
		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
	        startActivity(intent2);
	        finish();
	}

}
