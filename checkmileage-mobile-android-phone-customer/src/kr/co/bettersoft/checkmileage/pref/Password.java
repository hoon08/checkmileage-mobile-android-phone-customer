/*
 * Copyright (C) 2011 Donghyun, Hwang (hbull@hanmail.net)
 */

/*
 *  ��� �ϴ� �̴ϴ�. Settings_PasswordPageActivity  ����ȭ�鿡�� ��� ���� �� ����� ȣ��˴ϴ�.
 *  
 *  ������ �ɰ��� ��� ���� �������� �ִµ� �ű⼭ �� �������� ȣ���ؼ� ����մϴ�.
 * 
 */

package kr.co.bettersoft.checkmileage.pref;
/**
 * Password
 * 
 * ��� �Է� �� ���� �� ����� ������ ��� ȭ��
 */
import kr.co.bettersoft.checkmileage.activities.R;
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
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Password extends Activity {
	String TAG = "Password";
	public static final String NEXT_ACTIVITY = "nextActivity";
	public static final String PASSWORD = "password";
	public static final String RESULT_PASSWORD = "resultPassword";
	public static final String MODE = "mode";

	public static final int MODE_CHANGE_PASSWORD = 0;		// ���� ���
	public static final int MODE_INIT_PASSWORD = 1;			// �ʱ�ȭ ���
	public static final int MODE_CHECK_PASSWORD = 2;		// Ȯ�� ���

	public static final int PHASE_INIT_PASSWORD = 0;		// �ʱ�ȭ
	public static final int PHASE_INPUT_PASSWORD = 1;		// �Է�
	public static final int PHASE_CONFIRM_PASSWORD = 2;		// Ȯ��

	private int currentMode = PHASE_CONFIRM_PASSWORD;		
	private int initMode = PHASE_CONFIRM_PASSWORD;			
	private String currentPassword;						// ������ ������ ���

	//	private int passwordLength = 4;		// 1ĭ¥���� ��� ��� �ִ� ����.
	private Intent nextActivity;
	//	private EditText passwordForm, passwordConfirmForm;

	// ����Է�1, ���Ȯ��2 ���̾ƿ�
	LinearLayout password_linear1, password_linear2;
	// �Է� 4ĭ + Ȯ�� 4ĭ  (1ĭ�� 1����)
	private EditText pwpart1, pwpart2, pwpart3, pwpart4, pw_cnfrmpart1, pw_cnfrmpart2, pw_cnfrmpart3, pw_cnfrmpart4;		// ��� 4ĭ¥��
	String pwForms, pw_cnfrmForms;				// ��� 4ĭ¥�� �� ���� ��Ʈ��. - ��� �����
	String tempStr1="", tempStr2="", tempStr3="", tempStr4="";		// ��� 4ĭ¥�� �ӽ� �����. ���ĭ���� ���׶�̸� �����ְ� ���� �ӽ� ����ҿ� ����.
	private String passwordString;
	//	private ViewFlipper passwordFlipper;
	private ViewFlipper passwordFlipper2;
	private TextView textMessage;
	private TranslateAnimation pushLeftIn, pushLeftOut, shakeAni;			// ����, ����, Ʋ������ �ִϸ��̼� ȿ��

	int dontTwice = 1;		// �ߺ� ���� ����

	SharedPreferences sharedPrefCustom;	// ���� ������
	Boolean loginYN = false;			// �α��� ����		-- (�α����� ��� = ��� �Է¹ް� ���� ���� �ϴ� ���)
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		setContentView(R.layout.password);
		sharedPrefCustom = getSharedPreferences("MyCustomePref",		// �����۷��� - �� ��� ����Ǿ� ����
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		init();
		initAnimation();
	}

	private Runnable passwordRunnable = new Runnable(){		// ������� ��� üũ
		@Override
		public void run() {
			checkPassword();
		}
	};

	/**
	 * checkPassword
	 *  ��й�ȣ�� üũ�Ѵ�. ù�Է� ��ģ���, �ι�°�Է� ����/����
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void checkPassword(){  	
		Log.i("Password", "checkPassword");
		switch(currentMode){
		case PHASE_INIT_PASSWORD:			// 0 ��� ���� ���. ���� ����� ���Ͽ� �����Ѵ�. 
			Log.i("Password", "checkPassword--PHASE_INIT_PASSWORD");

			// 1��¥��
			//    		if( passwordString.equals(passwordForm.getText().toString()) ){		// �Է���  ����� ��ġ��. (���� �����)
			//    			textMessage.setText("�ٲ� ��й�ȣ�� �Է��� �ֽʽÿ�.");
			//    			goToNextPhase();
			//    		}else{																// �Է���  ����� ��ġ���� ����.
			//                passwordForm.startAnimation(shakeAni);
			//                textMessage.setText("�Է��Ͻ� ��й�ȣ�� ��ġ���� �ʽ��ϴ�.");
			//    		}

			// 4��¥��
			if( passwordString.equals(pwForms) ){		// �Է���  ����� ��ġ��. (���� �����)
				textMessage.setText(R.string.password_input_new);
				pwpart4.setFocusableInTouchMode(false);
				pwpart1.setFocusableInTouchMode(true);
				pwpart1.requestFocus();
				goToNextPhase();
			}else{																// �Է���  ����� ��ġ���� ����.
				password_linear1.startAnimation(shakeAni);
				textMessage.setText(R.string.password_not_equals);				// 4�ڸ� �ʱ�ȭ�ϰ� ��� ���� �ʿ�...		
				pwpart4.setFocusableInTouchMode(false);
				pwpart1.setFocusableInTouchMode(true);
				pwpart1.requestFocus();
			}
			break;
		case PHASE_INPUT_PASSWORD:		// �� ��� �Է� 1								// �� ���1 �Է��� ����. ��� 2�� �޾ƾ� �Ѵ�.
			Log.i("Password", "checkPassword--PHASE_INPUT_PASSWORD");
			textMessage.setText(R.string.password_input_again);
			goToNextPhase();
			break;
		case PHASE_CONFIRM_PASSWORD:	// �� ��� Ȯ�� 2 �� �α��� Ȯ��
			Log.i("Password", "checkPassword--PHASE_CONFIRM_PASSWORD");

			// 1ĭ��
			//    		EditText currentForm = passwordConfirmForm; 											// ���� �ؽ�Ʈ. (��� ���� �� �񱳿�)
			//    		if(initMode == PHASE_CONFIRM_PASSWORD) currentForm = passwordForm;		// Ȯ�� �ܰ� 
			//    		if( currentPassword.equals(currentForm.getText().toString()) ){		// �¾ƿ�
			//    			goToNextPhase();
			//    		}else{																// Ʋ����
			//    			passwordForm.startAnimation(shakeAni);
			//    			textMessage.setText("�Է��Ͻ� ��й�ȣ�� ��ġ���� �ʽ��ϴ�.");
			//    		}

			// 4ĭ��
			String confStr = pw_cnfrmForms;
			//    		EditText currentForm = passwordConfirmForm; 												// ���� �ؽ�Ʈ. (��� ���� �� �񱳿�)		pwForms, pw_cnfrmForms
			// 1�� �Է� ���� ���Ѵ�.  (����� �Է°��� �ι�° �Է�, ���� ����� ù�Է����� ����). �α����Ҷ��� ���� �� �����Ҷ�.
			if(initMode == PHASE_CONFIRM_PASSWORD) 	// ���� �ܰ� 	(�α���)								
			{
				confStr = pwForms;	
				if( currentPassword.equals(confStr) ){		// �¾ƿ�
					goToNextPhase();
				}else{																// Ʋ����
					password_linear1.startAnimation(shakeAni);
					textMessage.setText(R.string.password_not_equals);
					pwpart4.setFocusableInTouchMode(false);
					pwpart1.setFocusableInTouchMode(true);
					pwpart1.requestFocus();
				}
			}else{									// ���� �ƴ� ���. ���� �� ����																							
				if( currentPassword.equals(confStr) ){		// �¾ƿ�			// ��� ������ ���Է�(Ȯ�ο�)
					goToNextPhase();
				}else{																// Ʋ����
					password_linear2.startAnimation(shakeAni);
					textMessage.setText(R.string.password_not_equals);
					pw_cnfrmpart4.setFocusableInTouchMode(false);
					pw_cnfrmpart1.setFocusableInTouchMode(true);
					pw_cnfrmpart1.requestFocus();
				}
			}
		}
		// ȭ�鿡 ���� �Է�ĭ û��
		//    	passwordForm.setText("");			// 1ĭ��
		//    	passwordConfirmForm.setText("");
		pwpart1.setText("");			// 4ĭ��		
		pwpart2.setText("");
		pwpart3.setText("");
		pwpart4.setText("");
		pw_cnfrmpart1.setText("");
		pw_cnfrmpart2.setText("");
		pw_cnfrmpart3.setText("");
		pw_cnfrmpart4.setText("");								
	}

	/**
	 * goToNextPhase
	 *  ��� - (���� ��忡 ����)�����ܰ�� �Ѿ��.  
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void goToNextPhase(){
		Log.i("Password", "goToNextPhase,, currentMode::"+currentMode);		// ��� ���� ��� : 0
		switch(currentMode){
		case PHASE_INIT_PASSWORD:		// 0
			Log.i("Password", "goToNextPhase--PHASE_INIT_PASSWORD");
			currentMode = PHASE_INPUT_PASSWORD;			// 1����. (�� ���1 �Է¸��)
			break;

		case PHASE_INPUT_PASSWORD:		// 1
			Log.i("Password", "goToNextPhase--PHASE_INPUT_PASSWORD");

			//    		currentPassword = passwordForm.getText().toString();			// 1ĭ��
			currentPassword = pwForms;									// 4ĭ��

			currentMode = PHASE_CONFIRM_PASSWORD;
			//    		passwordFlipper.setInAnimation(pushLeftIn);					// 1ĭ��
			//    		passwordFlipper.setOutAnimation(pushLeftOut);
			//    		passwordFlipper.showPrevious();
			passwordFlipper2.setInAnimation(pushLeftIn);					// 4ĭ��
			passwordFlipper2.setOutAnimation(pushLeftOut);
			passwordFlipper2.showPrevious();
			break;

		case PHASE_CONFIRM_PASSWORD:			// 2 ó�� ������ ���� ��Ƽ��Ƽ�� ����. 		 ��� �ű� �Ǵ� ������ �Ϸ�� �����̹Ƿ� ����� �����ؾ� �Ѵ�. currentPassword �� pref �� ����.
			Log.i("Password", "goToNextPhase--PHASE_CONFIRM_PASSWORD::"+currentPassword);
			//    		nextActivity.putExtra(RESULT_PASSWORD, currentPassword);		// �׽�Ʈ��. �ٲ� ��� Ȯ�� ȭ��.
			if(loginYN){		// �α����� ��쿡�� �ٽ� ���� �������� ���ư���. (�α��� ���� �ܰ踦 �����Ѵ�.)
				kr.co.bettersoft.checkmileage.activities.MainActivity.loginYN = true;
				startActivity(nextActivity);    	
			}else{				// ��� ���� ���� ���� ��� ���� �������� ���ư���.(��� ������ �����ϸ� ���ư�����.)
				SharedPreferences.Editor savePWcustom = sharedPrefCustom.edit();		// �������� ����� ������ �ش�.
				savePWcustom.putString("password" , currentPassword);
				savePWcustom.commit();

				PrefActivityFromResource.memberInfo.setPassword(currentPassword);		// ����� ����� ������ �����Ų��..
				PrefActivityFromResource.updateLv = PrefActivityFromResource.updateLv+1;

				//    			 PrefActivityFromResource.updateToServer();		// resume �������� �˻��ؼ� ó�� �ϵ��� ����.. *** 
				Toast.makeText(Password.this, R.string.password_set_success, Toast.LENGTH_SHORT).show();
			}
			finish();
			break;
		}
	}

	/**
	 * init
	 *  �ʱ�ȭ - ��� Ȯ�� �� ���� ���� �غ� �Ѵ�. 
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void init(){
		Intent intent = getIntent();
		String nextActivityClassString = intent.getStringExtra(NEXT_ACTIVITY);		// ������ ������ ��
		Log.i("Password", "nextActivityClassString::"+nextActivityClassString);	
		nextActivity = new Intent();						
		nextActivity.setClassName(Password.this, nextActivityClassString);		

		initMode = intent.getIntExtra(MODE, MODE_CHECK_PASSWORD);
		Log.i("Password", "initMode::"+initMode);
		if(initMode==2){		// �α����� ��� - ��� �Է¹ް� ���� ���� �ϴ� ���
			loginYN = true;
		}
		passwordString = intent.getStringExtra(PASSWORD);
		Log.i("Password", "passwordString::"+passwordString);
		currentMode = initMode;
		currentPassword = passwordString;
		//        passwordLength = 4;		// ����� ������ 4���ڿ��߸� �Ѵ�.  --> 4ĭ���� ������ �̷� ���� �ʿ����� ����

		// ��� �Է�â�� �ȳ� �޽���â.
		textMessage = (TextView)findViewById(R.id.password_message);
		//        passwordFlipper = (ViewFlipper)findViewById(R.id.password_flipper);		// 1ĭ��
		passwordFlipper2 = (ViewFlipper)findViewById(R.id.password_flipper2);		// 4ĭ��

		//        passwordForm = (EditText)findViewById(R.id.password); 						// 1ĭ��

		password_linear1 = (LinearLayout)findViewById(R.id.password_linear1);		// 4ĭ��
		password_linear2 = (LinearLayout)findViewById(R.id.password_linear2);
		pwpart1 = (EditText)findViewById(R.id.pwpart1); 
		pwpart2 = (EditText)findViewById(R.id.pwpart2); 
		pwpart3 = (EditText)findViewById(R.id.pwpart3); 
		pwpart4 = (EditText)findViewById(R.id.pwpart4); 
		pw_cnfrmpart1 = (EditText)findViewById(R.id.pw_cnfrmpart1); 
		pw_cnfrmpart2 = (EditText)findViewById(R.id.pw_cnfrmpart2); 
		pw_cnfrmpart3 = (EditText)findViewById(R.id.pw_cnfrmpart3); 
		pw_cnfrmpart4 = (EditText)findViewById(R.id.pw_cnfrmpart4); 

		if(passwordString.length()>0){		// ��� ���� ���.  & �α��� ���.
			if(loginYN){
				textMessage.setText(R.string.password_input_pw);
			}else{
				textMessage.setText(R.string.password_input_old);
			}
		}else{								// ���� ��� �Է� ���
			textMessage.setText(R.string.password_input_new);
		}

		// ��� �Է� �� üũ�ϱ�  -- 1ĭ��
		//        passwordForm.addTextChangedListener(new TextWatcher() {
		//        	public void  afterTextChanged (Editable s){}
		//            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){}
		//            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
		//            	// ���̰� ���ٸ� ��� Ȯ�� �޼��� ����
		//            	if(passwordForm.getText().toString().length() == passwordLength){
		//            		Handler passwordHandler = new Handler();
		//            		passwordHandler.postDelayed(passwordRunnable, 200);
		//        		}
		//            } 
		//        });

		// ��� Ȯ�� â -- 1ĭ��
		//        passwordConfirmForm = (EditText)findViewById(R.id.password_confirm);    
		//        // �� ����Ǹ� �˻�
		//        passwordConfirmForm.addTextChangedListener(new TextWatcher() {
		//        	public void  afterTextChanged (Editable s){
		//        	}
		//            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){
		//            }
		//            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
		//            	// ����� ���̰� ���ٸ� ��� Ȯ�� �˻�.
		//            	if(passwordConfirmForm.getText().toString().length() == passwordLength){
		//            		Handler passwordHandler = new Handler();
		//            		passwordHandler.postDelayed(passwordRunnable, 200);
		//        		}
		//            } 
		//        });

		// 4ĭ¥��. ��� �Է�â.
		/**
		 * pwpart1.setOnKeyListener , pwpart1.addTextChangedListener
		 *  ùĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pwpart1.setOnKeyListener(new OnKeyListener() {                  // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    (ùĭ�� ���۾���) 
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//        		Log.e(TAG,"pwpart1");
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// ù ������ ���� �Ұ� ����..
						pwpart1.setText("");
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}
			}
		}); 
		pwpart1.addTextChangedListener(new TextWatcher() {		// ùĭ ������
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart1.length()==1) && !((pwpart1.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr1 = pwpart1.getText()+"";		// ������ ������ �����ϰ� ���׶�̷� ������
					pwpart1.setText("��");
					pwpart1.setFocusableInTouchMode(false);
					pwpart2.setFocusableInTouchMode(true);
					pwpart2.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart2.setOnKeyListener , pwpart2.addTextChangedListener
		 *  �ι�°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pwpart2.setOnKeyListener(new OnKeyListener() {          // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// pwpart1 �� ����� �������� ����.
						pwpart1.setText("");
						pwpart2.setFocusableInTouchMode(false);
						pwpart1.setFocusableInTouchMode(true);
						pwpart1.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����. (ȭ�� ���� �ٲٸ� �����ʰ� �ٽ� ���۵Ǳ� ����)
					return true; 
				}else{
					return false;
				}     
			}
		}); 
		pwpart2.addTextChangedListener(new TextWatcher() {			 // �ι�°ĭ ������
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if((pwpart2.length()==1) && !((pwpart2.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr2 = pwpart2.getText()+"";
					pwpart2.setText("��");
					pwpart2.setFocusableInTouchMode(false);
					pwpart3.setFocusableInTouchMode(true);
					pwpart3.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart3.setOnKeyListener , pwpart3.addTextChangedListener
		 *  ����°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pwpart3.setOnKeyListener(new OnKeyListener() {                  // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						pwpart2.setText("");
						pwpart3.setFocusableInTouchMode(false);
						pwpart2.setFocusableInTouchMode(true);
						pwpart2.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pwpart3.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart3.length()==1) && !((pwpart3.getText()+"").equals("��"))){
					tempStr3 = pwpart3.getText()+"";
					pwpart3.setText("��");
					pwpart3.setFocusableInTouchMode(false);
					pwpart4.setFocusableInTouchMode(true);
					pwpart4.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart4.setOnKeyListener , pwpart4.addTextChangedListener
		 *  �׹�°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pwpart4.setOnKeyListener(new OnKeyListener() {                  // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						pwpart3.setText("");
						pwpart4.setText("");
						pwpart4.setFocusableInTouchMode(false);
						pwpart3.setFocusableInTouchMode(true);
						pwpart3.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}      
			}
		}); 
		pwpart4.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart4.length()==1) && !((pwpart4.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr4 = pwpart4.getText()+"";
					pwpart4.setText("��");
					// ��� 4�� �ٹ޾Ҵ�..
					pwForms = tempStr1+tempStr2+tempStr3+tempStr4;
					Log.d(TAG, "got password:"+pwForms);
					Handler passwordHandler = new Handler();
					passwordHandler.postDelayed(passwordRunnable, 200);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});


		// 4ĭ¥�� ��� Ȯ��â.
		/**
		 * pw_cnfrmpart1.setOnKeyListener , pw_cnfrmpart1.addTextChangedListener
		 *  Ȯ�ο� ùĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pw_cnfrmpart1.setOnKeyListener(new OnKeyListener() {             // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�(ùĭ�̶� ���� ����)     
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				Log.d(TAG,"pw_cnfrmpart1");
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// ù ������ ���� �Ұ� ����..
						pw_cnfrmpart1.setText("");
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pw_cnfrmpart1.addTextChangedListener(new TextWatcher() {		
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {					
				if((pw_cnfrmpart1.length()==1) && !((pw_cnfrmpart1.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr1 = pw_cnfrmpart1.getText()+"";
					pw_cnfrmpart1.setText("��");
					pw_cnfrmpart1.setFocusableInTouchMode(false);
					pw_cnfrmpart2.setFocusableInTouchMode(true);
					pw_cnfrmpart2.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart2.setOnKeyListener , pw_cnfrmpart2.addTextChangedListener
		 *  Ȯ�ο� �ι�°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pw_cnfrmpart2.setOnKeyListener(new OnKeyListener() {                  
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// pw_cnfrmpart1 �� ����� �������� ����.
						pw_cnfrmpart1.setText("");
						pw_cnfrmpart2.setFocusableInTouchMode(false);
						pw_cnfrmpart1.setFocusableInTouchMode(true);
						pw_cnfrmpart1.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}     
			}
		}); 
		pw_cnfrmpart2.addTextChangedListener(new TextWatcher() {		// ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart2.length()==1) && !((pw_cnfrmpart2.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr2 = pw_cnfrmpart2.getText()+"";
					pw_cnfrmpart2.setText("��");
					pw_cnfrmpart2.setFocusableInTouchMode(false);
					pw_cnfrmpart3.setFocusableInTouchMode(true);
					pw_cnfrmpart3.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart3.setOnKeyListener , pw_cnfrmpart3.addTextChangedListener
		 *  Ȯ�ο� ����°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pw_cnfrmpart3.setOnKeyListener(new OnKeyListener() {               		// ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�       
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						pw_cnfrmpart2.setText("");
						pw_cnfrmpart3.setFocusableInTouchMode(false);
						pw_cnfrmpart2.setFocusableInTouchMode(true);
						pw_cnfrmpart2.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pw_cnfrmpart3.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart3.length()==1) && !((pw_cnfrmpart3.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr3 = pw_cnfrmpart3.getText()+"";
					pw_cnfrmpart3.setText("��");
					pw_cnfrmpart3.setFocusableInTouchMode(false);
					pw_cnfrmpart4.setFocusableInTouchMode(true);
					pw_cnfrmpart4.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart4.setOnKeyListener , pw_cnfrmpart4.addTextChangedListener
		 *  Ȯ�ο� �׹�°ĭ ������. �� ���ö�, ����� ��ư ���������� ���� ����
		 */
		pw_cnfrmpart4.setOnKeyListener(new OnKeyListener() {              // ����� ��ư ��������. �ձ��� ����� Ŀ�� �̵�    
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						pw_cnfrmpart3.setText("");
						pw_cnfrmpart4.setText("");
						pw_cnfrmpart4.setFocusableInTouchMode(false);
						pw_cnfrmpart3.setFocusableInTouchMode(true);
						pw_cnfrmpart3.requestFocus();
					}
					dontTwice = dontTwice * -1;	// ���. 2�� �ǽ� ����.
					return true; 
				}else{
					return false;
				}      
			}
		}); 
		pw_cnfrmpart4.addTextChangedListener(new TextWatcher() {	// �� �Է½� ������. �� ������ ���׶�̷� ����
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart4.length()==1) && !((pw_cnfrmpart4.getText()+"").equals("��"))){  // edit1  ���� ���Ѱ��� 6�̶�� ����������
					tempStr4 = pw_cnfrmpart4.getText()+"";
					pw_cnfrmpart4.setText("��");
					// ��� 4�� �ٹ޾Ҵ�..
					pw_cnfrmForms = tempStr1 + tempStr2 + tempStr3 + tempStr4;
					//       	    	Log.i(TAG, "pw all get(4th) 2. "+pw_cnfrmForms);
					Handler passwordHandler = new Handler();
					passwordHandler.postDelayed(passwordRunnable, 200);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	// �ִϸ��̼� ȿ��.
	/**
	 * initAnimation
	 *  ���â ���� �Ǵ� Ʋ���� ��쿡 ���� �ִϸ��̼� ȿ���� �ش�
	 *
	 * @param
	 * @param
	 * @return
	 */
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

		shakeAni = new TranslateAnimation(						// ����. Ʋ������.
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,   
				TranslateAnimation.RELATIVE_TO_SELF, 0.05f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		shakeAni.setDuration(300);
		shakeAni.setInterpolator(new CycleInterpolator(2.0f));
	}
}