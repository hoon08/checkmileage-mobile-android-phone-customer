package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ���� ���� ���� - ���
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
	    
//	    contents = "�� ���� ���ϸ����� �״� �����Դϴ�. \n\n���� ���̵� �Ѱ��� ��ϵ� ���� \n���������� ����Ͻ� �� �ֽ��ϴ�.\n\n������ ����Ʈ ī�带 ������ \n��� �ٴ� �ʿ䰡 �����ϴ�. \n\n����� ���� ���� ���̵� �����Ͽ� \n�н� ���� ���� �����ϰ� ����ϰ�, \n\n���� ���� �� �缳ġ ���� ��� \n������ ����  ����ؼ� ����� �� �ֽ��ϴ�.";
	    tv1.setText(R.string.settings_about_content);
	    
	    
	    
	}

}
