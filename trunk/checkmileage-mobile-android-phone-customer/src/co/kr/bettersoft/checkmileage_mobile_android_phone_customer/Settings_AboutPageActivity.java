package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 어플 정보 보기 - 사용
/*
 *  What is this App.
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Settings_AboutPageActivity extends Activity {

	TextView tv1 ;
//	String contents;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.settings_about);
	    // TODO Auto-generated method stub
	    
	    tv1 = (TextView) findViewById(R.id.settings_about_text1);
	    
//	    contents = "이 앱은 마일리지를 쌓는 어플입니다. \n\n통합 아이디 한개로 등록된 여러 \n가맹점에서 사용하실 수 있습니다.\n\n지갑에 포인트 카드를 여러장 \n들고 다닐 필요가 없습니다. \n\n모바일 앱을 통해 아이디를 저장하여 \n분실 위험 없이 간편하게 사용하고, \n\n어플 삭제 후 재설치 했을 경우 \n인증을 통해  계속해서 사용할 수 있습니다.";
	    tv1.setText(R.string.settings_about_content);
	    
	    
	    
	}

}
