package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
/*
 * 가맹점에서 보내온 이벤트 상세 보기 화면.
 * 
 * 화면 구성은 최상단에 타이틀,  상단부에 이미지(첨부 이미지). 하단부에 텍스트. 
 * 타이틀, 이미지는 고정상태고 하단 텍스트의 내용이 많을 경우 스크롤 되도록 한다. 
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
		 if(aa!=null && aa.length()>0){		// 데이터 전달이 가능하다.
			 Log.e(TAG, aa);
		 }else{
			 Log.e(TAG, "T.T");		
		 }
	}

}
