package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR ���� ������
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
		  * QR �� �����ϰ� �ٷ� �����ܰ��� ���� QR �ڵ� �����Ƽ��Ƽ�� �Ѿ��.
		  * ����ڿ��� �� ��Ƽ��Ƽ�� �������� �ʰ� �ٷ� ���� QR �ڵ庸�� ȭ���� ��Ÿ���� ��1��.
		  */
		    
	    // QR �ڵ� ��ü �����ϴ� �κ�..
	    // ... QR �ڵ带 �����ϰ�, ������ ����Ѵ�.
		    
	    // ���� QR �ڵ� ����� �̵�.
	    Log.i("CreateQRPageActivity", "QR registered Success");
		 Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
	        startActivity(intent2);
	        finish();
	}

}
