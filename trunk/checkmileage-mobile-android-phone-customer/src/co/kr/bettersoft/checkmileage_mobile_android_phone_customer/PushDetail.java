package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
/*
 * ���������� ������ �̺�Ʈ �� ���� ȭ��.
 * 
 * ȭ�� ������ �ֻ�ܿ� Ÿ��Ʋ,  ��ܺο� �̹���(÷�� �̹���). �ϴܺο� �ؽ�Ʈ. 
 * Ÿ��Ʋ, �̹����� �������°� �ϴ� �ؽ�Ʈ�� ������ ���� ��� ��ũ�� �ǵ��� �Ѵ�. 
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushDetail extends Activity {
	String TAG = "PushDetail";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.push_detail);
	    // TODO Auto-generated method stub
	    String aa = "";
	    Intent receiveIntent = getIntent();
		 aa = receiveIntent.getStringExtra("aa");
		 if(aa!=null && aa.length()>0){		// ������ ������ �����ϴ�.
			 Log.e(TAG, aa);
		 }else{
			 Log.e(TAG, "T.T");		
		 }
	}

}
