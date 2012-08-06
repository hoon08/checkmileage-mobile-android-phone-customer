package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 스켄 페이지

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScanQRPageActivity extends Activity {
	
	public static final String TAG = ScanQRPageActivity.class.getSimpleName();

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	
	
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
//		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
//	        startActivity(intent2);
//	        finish();
	    
	    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.setPackage("com.google.zxing.client.android");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try{
        	startActivityForResult(intent, 0);
        }catch(Exception e){
        	e.printStackTrace();
        	Log.i("ScanQRPageActivity", "error occured");
        	Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
 	        startActivity(intent2);
 	        finish();
        }
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		
		if(requestCode == 0) {
              if(resultCode == RESULT_OK) {
            	  // 성공시 다음 페이지로 이동. qrCode 에 값 세팅해서 줌.
                     MyQRPageActivity.qrCode = intent.getStringExtra("SCAN_RESULT");
            		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
         	        startActivity(intent2);
         	        finish();
              } else if(resultCode == RESULT_CANCELED) {
            	  // 취소 또는 실패시 이전화면으로.
              	Toast.makeText(ScanQRPageActivity.this, "QR코드 인식에 실패했습니다." + "\n" + "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
              	Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
     	        startActivity(intent2);
     	        finish();
              }
        }
	}

}
