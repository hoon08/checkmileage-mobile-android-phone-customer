package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class No_QR_PageActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Log.i("No_QR_PageActivity", "select method to get QR");
	    setContentView(R.layout.no_qr_page);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        
        button1.setText("새로운 QR 코드를 생성하시려면 여기를 클릭하십시오.");
        button2.setText("가맹점에서 받은 QR 카드를 등록하시려면 여기를 클릭하십시오.");
	    // TODO Auto-generated method stub
        
        
        // QR 생성 버튼 클릭시.
        button1.setOnClickListener(new OnClickListener() {
        	public void onClick(View V){
        		/* 버튼이 눌렸을 때 실행될 코드입니다. */
        		 Log.i("No_QR_PageActivity", "go Create QR");
        		 Intent intent = new Intent(No_QR_PageActivity.this, CreateQRPageActivity.class);
        	        startActivity(intent);
        	        finish();
        	}
        });
        
        // QR 스켄 버튼 클릭시.
        button2.setOnClickListener(new OnClickListener() {
        	public void onClick(View V){
        		/* 버튼이 눌렸을 때 실행될 코드입니다. */
        		 Log.i("No_QR_PageActivity", "go Scan QR");
        		 Intent intent = new Intent(No_QR_PageActivity.this, ScanQRPageActivity.class);
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
		// 자식 인텐트. createQR, ScanQR 로부터 종료 사인을 받음.
		if(requestCode == 111){
			if(resultCode == RESULT_OK){
				 Log.i("No_QR_PageActivity", "No_QR_PageActivity activity off");
				finish();		// 종료함.
			}
		}
	}
	
}