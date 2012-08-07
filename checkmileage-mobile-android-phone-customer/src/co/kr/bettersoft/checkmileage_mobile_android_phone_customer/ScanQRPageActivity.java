package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR ���� ������

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScanQRPageActivity extends Activity {
	static int qrResult = 0;
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
		 * QR ���˸�尡 �Ǿ� QR ī�带 �����Ѵ�.
		 * QR ī�� ������ �����Ͽ� QR ������ ������, �ش� ������ �ۿ� �����ϰ�, ������ ����Ѵ�.
		 * ���� ���� QR �ڵ� ���� ȭ������ �̵��Ѵ�.
		 */

		// QR ī�� �����ϴ� �κ�..
		// ... QR ī�带 �����Ͽ� ������ �����ϰ�, ������ ����Ѵ�.

		// ���� QR �ڵ� ����� �̵�.
		Log.i("ScanQRPageActivity", "QR registered Success");
		//		 Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
		//	        startActivity(intent2);
		//	        finish();

		//	    Intent intent = new Intent("com.google.zxing.client.android.CaptureActivityHandler");
		Intent intent = new Intent(ScanQRPageActivity.this,com.google.zxing.client.android.CaptureActivity.class);
		//	    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("com.google.zxing.client.android");
		//        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
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

	// QR �ڵ� ����ҿ� QR �ڵ带 �����Ѵ�. 
	public void saveQR(){		
		CommonUtils.callCode = 32;		// ���� ���
		Intent saveQRintent = new Intent(ScanQRPageActivity.this, CommonUtils.class);			// ȣ��
		startActivity(saveQRintent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);

		if(requestCode == 0) {
			if(resultCode == RESULT_OK) {  // ������
				String qrcode = intent.getStringExtra("SCAN_RESULT");
				// 1. ���� ���Ͽ� QR �ڵ带 ������
				Log.i("ScanQRPageActivity", "save qrcode to file : "+qrcode);
				CommonUtils.writeQRstr = qrcode;
				saveQR();	
				// 2. ���� �������� �̵�. qrCode �� �� �����ؼ� ��.
				Log.i("ScanQRPageActivity", "load qrcode to img : "+qrcode);
				MyQRPageActivity.qrCode = qrcode;

				new Thread(
						new Runnable(){
							public void run(){
								try{
									Thread.sleep(100);
									Log.i("ScanQRPageActivity", "qrResult::"+qrResult);		// �б� ��� ����.
									// ���� QR �ڵ� ����� �̵�.
									Log.i("ScanQRPageActivity", "QR registered Success");
									Intent intent2 = new Intent(ScanQRPageActivity.this, Main_TabsActivity.class);
									startActivity(intent2);
									finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
								}catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
						}
				).start();
			} else if(resultCode == RESULT_CANCELED) {
				// ��� �Ǵ� ���н� ����ȭ������.
				Toast.makeText(ScanQRPageActivity.this, "QR�ڵ� �νĿ� �����߽��ϴ�." + "\n" + "�ٽ� �õ��� �ּ���.", Toast.LENGTH_SHORT).show();
				Intent intent2 = new Intent(ScanQRPageActivity.this, No_QR_PageActivity.class);
				startActivity(intent2);
				finish();
			}
		}
	}



}
