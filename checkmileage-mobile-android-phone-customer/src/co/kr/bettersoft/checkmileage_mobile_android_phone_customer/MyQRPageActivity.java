package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 내 QR 보기 화면
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyQRPageActivity extends Activity {
	int app_end = 0;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_qr_page);
	    // TODO Auto-generated method stub
	}
	// 닫기 버튼 막음. 막히지 않음.
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
		}
		// TODO Auto-generated method stub
//		super.onBackPressed();
	}
}
