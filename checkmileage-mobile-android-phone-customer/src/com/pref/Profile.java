/*
 * Copyright (C) 2011 Donghyun, Hwang (hbull@hanmail.net)
 */

package com.pref;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
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
