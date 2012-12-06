package kr.co.bettersoft.checkmileage.activities;
// ���� ���� ���� - ���
/*
 *  What is this App.
 *  
 *  
 *  ������ �����ϸ� ������ ����� ��.
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
	View parentLayout;		// �ƹ��볪 ��ġ�� ����
	
//	TextView tv1 ;
//	String contents;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
	    setContentView(R.layout.settings_about);
	    // TODO Auto-generated method stub
	    
	    parentLayout = findViewById(R.id.settings_about_parent);		// �θ� ���̾ƿ�- �����ʸ� �޾Ƽ� Ű���� �ڵ� ���迡 ���
	    
//	    tv1 = (TextView) findViewById(R.id.settings_about_text1);
	    
//	    contents = "�� ���� ���ϸ����� �״� �����Դϴ�. \n\n���� ���̵� �Ѱ��� ��ϵ� ���� \n���������� ����Ͻ� �� �ֽ��ϴ�.\n\n������ ����Ʈ ī�带 ������ \n��� �ٴ� �ʿ䰡 �����ϴ�. \n\n����� ���� ���� ���̵� �����Ͽ� \n�н� ���� ���� �����ϰ� ����ϰ�, \n\n���� ���� �� �缳ġ ���� ��� \n������ ����  ����ؼ� ����� �� �ֽ��ϴ�.";
//	    tv1.setText(R.string.settings_about_content);
	    
	 // �θ� ���̾ƿ� ������ - �ܺ� ��ġ �� Ű���� ���� �뵵
	    parentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.w(TAG,"parentLayout click");
				finish();
			}
		});
	}

}
