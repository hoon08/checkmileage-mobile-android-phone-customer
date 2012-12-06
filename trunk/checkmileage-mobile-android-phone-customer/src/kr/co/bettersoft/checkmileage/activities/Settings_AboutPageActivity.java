package kr.co.bettersoft.checkmileage.activities;
// 어플 정보 보기 - 사용
/*
 *  What is this App.
 *  
 *  
 *  디자인 도착하면 입혀서 사용할 것.
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class Settings_AboutPageActivity extends Activity {
	String TAG = "Settings_AboutPageActivity";
	View parentLayout;		// 아무대나 터치시 종료
	
//	TextView tv1 ;
//	String contents;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
	    setContentView(R.layout.settings_about);
	    // TODO Auto-generated method stub
	    
	    parentLayout = findViewById(R.id.settings_about_parent);		// 부모 레이아웃- 리스너를 달아서 키보드 자동 숨김에 사용
	    
//	    tv1 = (TextView) findViewById(R.id.settings_about_text1);
	    
//	    contents = "이 앱은 마일리지를 쌓는 어플입니다. \n\n통합 아이디 한개로 등록된 여러 \n가맹점에서 사용하실 수 있습니다.\n\n지갑에 포인트 카드를 여러장 \n들고 다닐 필요가 없습니다. \n\n모바일 앱을 통해 아이디를 저장하여 \n분실 위험 없이 간편하게 사용하고, \n\n어플 삭제 후 재설치 했을 경우 \n인증을 통해  계속해서 사용할 수 있습니다.";
//	    tv1.setText(R.string.settings_about_content);
	    
	 // 부모 레이아웃 리스너 - 외부 터치 시 키보드 숨김 용도
	    parentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.w(TAG,"parentLayout click");
				finish();
			}
		});
	}

}
