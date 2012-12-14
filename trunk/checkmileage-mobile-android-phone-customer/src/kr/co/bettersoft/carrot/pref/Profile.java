/*
 * Copyright (C) 2011 Donghyun, Hwang (hbull@hanmail.net)
 */

package kr.co.bettersoft.carrot.pref;
/*
 *  테스트 용으로 비번 변경 하고 나면 뭘로 설정되었는지 알려주는 페이지 였는데 보여주지 않고 바로 닫도록 함으로써
 *   사용하지 않게 된 클래스 페이지.
 *  비번 변경 후 이 액티비티 이름을 전달하면 이 페이지로 오면서 변경된 비번을 알려줌.
 */


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import kr.co.bettersoft.carrot.activities.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class Profile extends Activity {
	EditText editText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        TextView passwordResult = (TextView)findViewById(R.id.password_result);
        passwordResult.setText("you set the PASSWORD : " + getIntent().getStringExtra(Password.RESULT_PASSWORD));
    }
}
