package kr.co.bettersoft.checkmileage.activities;
// QR 없을 경우 QR 추가 페이지. -> QR 획득 방법 선택. 1.QR생성. 2.QR스켄
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class No_QR_PageActivity extends Activity {

	String phoneNumber= "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Log.i("No_QR_PageActivity", "select method to get QR");
	    setContentView(R.layout.no_qr_page);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        
        Intent rIntent = getIntent();
        phoneNumber = rIntent.getStringExtra("phoneNumber");
        
//        button1.setText("새로운 QR 코드를 생성하시려면 여기를 클릭하십시오.");
//        button2.setText("가맹점에서 받은 QR 카드를 등록하시려면 여기를 클릭하십시오.");
//        button1.setText(R.string.no_qr_create);
//        button2.setText(R.string.no_qr_scan);
	    // TODO Auto-generated method stub
        
        // QR 생성 버튼 클릭시.
        button1.setOnClickListener(new OnClickListener() {
        	public void onClick(View V){
        		/* 버튼이 눌렸을 때 실행될 코드입니다. */
        		 Log.i("No_QR_PageActivity", "go Create QR");
        		 Intent intent = new Intent(No_QR_PageActivity.this, CreateQRPageActivity.class);
        		 intent.putExtra("phoneNumber", phoneNumber);
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
        		 intent.putExtra("phoneNumber", phoneNumber);
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
	
	
	@Override
	public void onBackPressed() {
		// 여기서 종료시킬때는 다음에 잘 동작하도록 카운팅을 조절해야한다.
		DummyActivity.count = 0;
		finish();
	}
}