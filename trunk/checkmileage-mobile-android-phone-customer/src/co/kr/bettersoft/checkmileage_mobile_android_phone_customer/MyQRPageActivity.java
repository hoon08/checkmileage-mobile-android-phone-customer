package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// �� QR ���� ȭ��
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
	// �ݱ� ��ư ����. ������ ����.
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, "�ڷΰ��� ��ư�� �ѹ��� ������ ����˴ϴ�.", Toast.LENGTH_SHORT).show();
		}
		// TODO Auto-generated method stub
//		super.onBackPressed();
	}
}
