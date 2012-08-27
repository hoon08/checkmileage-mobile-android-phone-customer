/*
 * Copyright (C) 2011 Donghyun, Hwang (hbull@hanmail.net)
 */

package com.pref;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.app.Activity; 
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Password extends Activity {
	public static final String NEXT_ACTIVITY = "nextActivity";
	public static final String PASSWORD = "password";
	public static final String RESULT_PASSWORD = "resultPassword";
	public static final String MODE = "mode";
	
	public static final int MODE_CHANGE_PASSWORD = 0;
	public static final int MODE_INIT_PASSWORD = 1;
	public static final int MODE_CHECK_PASSWORD = 2;
	
	public static final int PHASE_INIT_PASSWORD = 0;
	public static final int PHASE_INPUT_PASSWORD = 1;
	public static final int PHASE_CONFIRM_PASSWORD = 2;
	
	private int currentMode = PHASE_CONFIRM_PASSWORD;
	private int initMode = PHASE_CONFIRM_PASSWORD;
	private String currentPassword;
	private int passwordLength = 9999;
	private Intent nextActivity;
	private EditText passwordForm, passwordConfirmForm;
	private String passwordString;
	private ViewFlipper passwordFlipper;
	private TextView textMessage;
	private TranslateAnimation pushLeftIn, pushLeftOut, shakeAni;
	
	SharedPreferences sharedPrefCustom;	// ���� ������
	Boolean loginYN = false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        init();
        initAnimation();
    }
    
    private Runnable passwordRunnable = new Runnable(){
        @Override
        public void run() {
        	checkPassword();
        }
    };
     
    private void checkPassword(){  	
    	Log.i("Password", "checkPassword");
    	switch(currentMode){
    	case PHASE_INIT_PASSWORD:			// 0 ��� ���� ���. ���� ����� ���Ͽ� �����Ѵ�. 
    		Log.i("Password", "checkPassword--PHASE_INIT_PASSWORD");
    		if( passwordString.equals(passwordForm.getText().toString()) ){		// �Է���  ����� ��ġ��. (���� �����)
    			textMessage.setText("�ٲ� ��й�ȣ�� �Է��� �ֽʽÿ�.");
    			goToNextPhase();
    		}else{																// �Է���  ����� ��ġ���� ����.
                passwordForm.startAnimation(shakeAni);
                textMessage.setText("�Է��Ͻ� ��й�ȣ�� ��ġ���� �ʽ��ϴ�.");
    		}
    		break;
    	case PHASE_INPUT_PASSWORD:		// �� ��� �Է� 1								// �� ���1 �Է��� ����. ��� 2�� �޾ƾ� �Ѵ�.
    		Log.i("Password", "checkPassword--PHASE_INPUT_PASSWORD");
    		textMessage.setText("Ȯ���� ���� �ٽ��ѹ� ��й�ȣ�� �Է��� �ֽʽÿ�.");
    		goToNextPhase();
    		break;
    	case PHASE_CONFIRM_PASSWORD:	// �� ��� Ȯ�� 2 �� �α��� Ȯ��
    		Log.i("Password", "checkPassword--PHASE_CONFIRM_PASSWORD");
    		EditText currentForm = passwordConfirmForm; 
    		if(initMode == PHASE_CONFIRM_PASSWORD) currentForm = passwordForm;		// Ȯ�� �ܰ� 
    		if( currentPassword.equals(currentForm.getText().toString()) ){		// �¾ƿ�
    			goToNextPhase();
    		}else{																// Ʋ����
    			passwordForm.startAnimation(shakeAni);
    			textMessage.setText("�Է��Ͻ� ��й�ȣ�� ��ġ���� �ʽ��ϴ�.");
    		}
    	}
    	passwordForm.setText("");
    	passwordConfirmForm.setText("");
    }
    
    private void goToNextPhase(){
    	Log.i("Password", "goToNextPhase,, currentMode::"+currentMode);		// ��� ���� ��� 0
    	switch(currentMode){
    	case PHASE_INIT_PASSWORD:		// 0
    		Log.i("Password", "goToNextPhase--PHASE_INIT_PASSWORD");
    		currentMode = PHASE_INPUT_PASSWORD;			// 1����. (�� ���1 �Է¸��)
    		break;
    		
    	case PHASE_INPUT_PASSWORD:		// 1
    		Log.i("Password", "goToNextPhase--PHASE_INPUT_PASSWORD");
    		currentPassword = passwordForm.getText().toString();
    		currentMode = PHASE_CONFIRM_PASSWORD;
    	             
    		passwordFlipper.setInAnimation(pushLeftIn);
    		passwordFlipper.setOutAnimation(pushLeftOut);
    		passwordFlipper.showPrevious();
    		break;
    		
    	case PHASE_CONFIRM_PASSWORD:			// 2 ó�� ������ ���� ��Ƽ��Ƽ�� ����. 		 ��� �ű� �Ǵ� ������ �Ϸ�� �����̹Ƿ� ����� �����ؾ� �Ѵ�. currentPassword �� pref �� ����.
    		Log.i("Password", "goToNextPhase--PHASE_CONFIRM_PASSWORD::"+currentPassword);
//    		nextActivity.putExtra(RESULT_PASSWORD, currentPassword);		// �׽�Ʈ��. �ٲ� ��� Ȯ�� ȭ��.
    		if(loginYN){		// �α����� ��쿡�� �ٽ� ���� �������� ���ư���. (�α��� ���� �ܰ踦 �����Ѵ�.)
    			co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MainActivity.loginYN = true;
            	startActivity(nextActivity);    	
    		}else{				// ��� ���� ���� ���� ��� ���� �������� ���ư���.(��� ������ �����ϸ� ���ư�����.)
        		
        		 SharedPreferences.Editor savePWcustom = sharedPrefCustom.edit();		// �������� ����� ������ �ش�.
    			 savePWcustom.putString("password" , currentPassword);
    			 savePWcustom. commit();
        		
    			 PrefActivityFromResource.memberInfo.setPassword(currentPassword);		// ����� ����� ������ �����Ų��..
    			 PrefActivityFromResource.updateLv = PrefActivityFromResource.updateLv+1;
    			 
//    			 PrefActivityFromResource.updateToServer();		// resume �������� �˻��ؼ� ó�� �ϵ��� ����.. *** 
        		 Toast.makeText(Password.this, "�� ��й�ȣ�� �����Ǿ����ϴ�.", Toast.LENGTH_SHORT).show();
    		}
    		finish();
        	break;
    	}
    }
    
    private void init(){
    	Intent intent = getIntent();
        String nextActivityClassString = intent.getStringExtra(NEXT_ACTIVITY);
        Log.i("Password", "nextActivityClassString::"+nextActivityClassString);
        nextActivity = new Intent();
        nextActivity.setClassName(Password.this, nextActivityClassString);
        
        initMode = intent.getIntExtra(MODE, MODE_CHECK_PASSWORD);
        Log.i("Password", "initMode::"+initMode);
        if(initMode==2){		// �α����� ���
        	loginYN = true;
        	// ����Ʈ Ű���� ������.
//        	passwordConfirmForm = (EditText)findViewById(R.id.password_confirm);    
//        	passwordConfirmForm.requestFocus(); 		// Ŀ�� Ȱ��ȭ �Ǿ�����.
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.showSoftInputFromInputMethod (passwordConfirmForm .getApplicationWindowToken(),InputMethodManager.SHOW_FORCED);
//        	imm.showSoftInput(passwordConfirmForm, InputMethodManager.SHOW_FORCED);
        }
        passwordString = intent.getStringExtra(PASSWORD);
        Log.i("Password", "passwordString::"+passwordString);
        currentMode = initMode;
        currentPassword = passwordString;
        passwordLength = 4;		// ����� ������ 4���ڿ��߸� �Ѵ�.
        
        // ��� �Է�â�� �ȳ� �޽���â.
        textMessage = (TextView)findViewById(R.id.password_message);
        passwordFlipper = (ViewFlipper)findViewById(R.id.password_flipper);
        passwordForm = (EditText)findViewById(R.id.password); 
        if(passwordString.length()>0){		// ��� ���� ���.  & �α��� ���.
        	if(loginYN){
        		textMessage.setText("��й�ȣ�� �Է��� �ֽʽÿ�.");
        	}else{
        		textMessage.setText("���� ��й�ȣ�� �Է��� �ֽʽÿ�.");
        	}
        }else{								// ���� ��� �Է� ���
        	textMessage.setText("�� ��й�ȣ�� �Է��� �ֽʽÿ�.");
        }
        
        // ��� �Է� �� üũ�ϱ�
        passwordForm.addTextChangedListener(new TextWatcher() {
        	public void  afterTextChanged (Editable s){
        	}
            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){
            }
            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
            	// ���̰� ���ٸ� ��� Ȯ�� �޼��� ����
            	if(passwordForm.getText().toString().length() == passwordLength){
            		Handler passwordHandler = new Handler();
            		passwordHandler.postDelayed(passwordRunnable, 200);
        		}
            } 
        });
        
        // ��� Ȯ�� â
        passwordConfirmForm = (EditText)findViewById(R.id.password_confirm);    
        // �� ����Ǹ� �˻�
        passwordConfirmForm.addTextChangedListener(new TextWatcher() {
        	public void  afterTextChanged (Editable s){
        	}
            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){
            }
            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
            	// ����� ���̰� ���ٸ� ��� Ȯ�� �˻�.
            	if(passwordConfirmForm.getText().toString().length() == passwordLength){
            		Handler passwordHandler = new Handler();
            		passwordHandler.postDelayed(passwordRunnable, 200);
        		}
            } 
        });
    }
    
    private void initAnimation(){
    	pushLeftIn = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 1.0f,   	// ����.�� ��� �Է� �޴� â�� ��Ÿ����. 
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,		
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
    	pushLeftIn.setDuration(200);
    	pushLeftIn.setFillAfter(true);
    	
    	pushLeftOut = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,   
                TranslateAnimation.RELATIVE_TO_SELF, -2.0f,		// ����.���� ����� ȭ�� ������ �������. 
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
    	pushLeftOut.setDuration(200);
    	pushLeftOut.setFillAfter(true);
    	
    	shakeAni = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,   
                TranslateAnimation.RELATIVE_TO_SELF, 0.05f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
    	shakeAni.setDuration(300);
    	shakeAni.setInterpolator(new CycleInterpolator(2.0f));
    }
}