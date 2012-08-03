package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 생성 페이지
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CreateQRPageActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    /* 
		  * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
		  * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
		  */
		    
	    // QR 코드 자체 생성하는 부분..
	    // ... QR 코드를 생성하고, 서버에 등록한다.
		    
	    // 나의 QR 코드 보기로 이동.
	    Log.i("CreateQRPageActivity", "QR registered Success");
		 Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
	        startActivity(intent2);
	        finish();
	}

}
