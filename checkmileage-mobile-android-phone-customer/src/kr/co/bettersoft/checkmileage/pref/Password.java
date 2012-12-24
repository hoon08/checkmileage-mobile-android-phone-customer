/*
 * Copyright (C) 2011 Donghyun, Hwang (hbull@hanmail.net)
 */

/*
 *  사용 하는 겁니다. Settings_PasswordPageActivity  설정화면에서 비번 생성 및 변경시 호출됩니다.
 *  
 *  세팅을 쪼개서 비번 세팅 페이지가 있는데 거기서 이 페이지를 호출해서 사용합니다.
 * 
 */

package kr.co.bettersoft.checkmileage.pref;
/**
 * Password
 * 
 * 비번 입력 및 설정 및 변경시 나오는 비번 화면
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

	public static final int MODE_CHANGE_PASSWORD = 0;		// 변경 모드
	public static final int MODE_INIT_PASSWORD = 1;			// 초기화 모드
	public static final int MODE_CHECK_PASSWORD = 2;		// 확인 모드

	public static final int PHASE_INIT_PASSWORD = 0;		// 초기화
	public static final int PHASE_INPUT_PASSWORD = 1;		// 입력
	public static final int PHASE_CONFIRM_PASSWORD = 2;		// 확인

	private int currentMode = PHASE_CONFIRM_PASSWORD;		
	private int initMode = PHASE_CONFIRM_PASSWORD;			
	private String currentPassword;						// 마지막 설정된 비번

	//	private int passwordLength = 4;		// 1칸짜리일 경우 비번 최대 길이.
	private Intent nextActivity;
	//	private EditText passwordForm, passwordConfirmForm;

	// 비번입력1, 비번확인2 레이아웃
	LinearLayout password_linear1, password_linear2;
	// 입력 4칸 + 확인 4칸  (1칸에 1글자)
	private EditText pwpart1, pwpart2, pwpart3, pwpart4, pw_cnfrmpart1, pw_cnfrmpart2, pw_cnfrmpart3, pw_cnfrmpart4;		// 비번 4칸짜리
	String pwForms, pw_cnfrmForms;				// 비번 4칸짜리 값 모은 스트링. - 비번 저장용
	String tempStr1="", tempStr2="", tempStr3="", tempStr4="";		// 비번 4칸짜리 임시 저장용. 비번칸에는 동그라미를 보여주고 값은 임시 저장소에 저장.
	private String passwordString;
	//	private ViewFlipper passwordFlipper;
	private ViewFlipper passwordFlipper2;
	private TextView textMessage;
	private TranslateAnimation pushLeftIn, pushLeftOut, shakeAni;			// 등장, 퇴장, 틀렸을때 애니메이션 효과

	int dontTwice = 1;		// 중복 실행 방지

	SharedPreferences sharedPrefCustom;	// 공용 프립스
	Boolean loginYN = false;			// 로그인 여부		-- (로그인일 경우 = 비번 입력받고 메인 가야 하는 경우)
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		setContentView(R.layout.password);
		sharedPrefCustom = getSharedPreferences("MyCustomePref",		// 프리퍼런스 - 에 비번 저장되어 있음
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		init();
		initAnimation();
	}

	private Runnable passwordRunnable = new Runnable(){		// 스레드로 비번 체크
		@Override
		public void run() {
			checkPassword();
		}
	};

	/**
	 * checkPassword
	 *  비밀번호를 체크한다. 첫입력 마친경우, 두번째입력 성공/실패
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void checkPassword(){  	
		Log.i("Password", "checkPassword");
		switch(currentMode){
		case PHASE_INIT_PASSWORD:			// 0 비번 변경 모드. 기존 비번과 비교하여 진행한다. 
			Log.i("Password", "checkPassword--PHASE_INIT_PASSWORD");

			// 1개짜리
			//    		if( passwordString.equals(passwordForm.getText().toString()) ){		// 입력한  비번이 일치함. (기존 비번과)
			//    			textMessage.setText("바꿀 비밀번호를 입력해 주십시오.");
			//    			goToNextPhase();
			//    		}else{																// 입력한  비번이 일치하지 않음.
			//                passwordForm.startAnimation(shakeAni);
			//                textMessage.setText("입력하신 비밀번호가 일치하지 않습니다.");
			//    		}

			// 4개짜리
			if( passwordString.equals(pwForms) ){		// 입력한  비번이 일치함. (기존 비번과)
				textMessage.setText(R.string.password_input_new);
				pwpart4.setFocusableInTouchMode(false);
				pwpart1.setFocusableInTouchMode(true);
				pwpart1.requestFocus();
				goToNextPhase();
			}else{																// 입력한  비번이 일치하지 않음.
				password_linear1.startAnimation(shakeAni);
				textMessage.setText(R.string.password_not_equals);				// 4자리 초기화하고 등등 진행 필요...		
				pwpart4.setFocusableInTouchMode(false);
				pwpart1.setFocusableInTouchMode(true);
				pwpart1.requestFocus();
			}
			break;
		case PHASE_INPUT_PASSWORD:		// 새 비번 입력 1								// 새 비번1 입력한 상태. 비번 2를 받아야 한다.
			Log.i("Password", "checkPassword--PHASE_INPUT_PASSWORD");
			textMessage.setText(R.string.password_input_again);
			goToNextPhase();
			break;
		case PHASE_CONFIRM_PASSWORD:	// 새 비번 확인 2 및 로그인 확인
			Log.i("Password", "checkPassword--PHASE_CONFIRM_PASSWORD");

			// 1칸용
			//    		EditText currentForm = passwordConfirmForm; 											// 에딧 텍스트. (비번 저장 및 비교용)
			//    		if(initMode == PHASE_CONFIRM_PASSWORD) currentForm = passwordForm;		// 확인 단계 
			//    		if( currentPassword.equals(currentForm.getText().toString()) ){		// 맞아요
			//    			goToNextPhase();
			//    		}else{																// 틀려요
			//    			passwordForm.startAnimation(shakeAni);
			//    			textMessage.setText("입력하신 비밀번호가 일치하지 않습니다.");
			//    		}

			// 4칸용
			String confStr = pw_cnfrmForms;
			//    		EditText currentForm = passwordConfirmForm; 												// 에딧 텍스트. (비번 저장 및 비교용)		pwForms, pw_cnfrmForms
			// 1차 입력 값과 비교한다.  (사용자 입력값이 두번째 입력, 기존 비번이 첫입력으로 가정). 로그인할때와 생성 및 변경할때.
			if(initMode == PHASE_CONFIRM_PASSWORD) 	// 인증 단계 	(로그인)								
			{
				confStr = pwForms;	
				if( currentPassword.equals(confStr) ){		// 맞아요
					goToNextPhase();
				}else{																// 틀려요
					password_linear1.startAnimation(shakeAni);
					textMessage.setText(R.string.password_not_equals);
					pwpart4.setFocusableInTouchMode(false);
					pwpart1.setFocusableInTouchMode(true);
					pwpart1.requestFocus();
				}
			}else{									// 인증 아닌 경우. 생성 및 변경																							
				if( currentPassword.equals(confStr) ){		// 맞아요			// 비번 설정시 재입력(확인용)
					goToNextPhase();
				}else{																// 틀려요
					password_linear2.startAnimation(shakeAni);
					textMessage.setText(R.string.password_not_equals);
					pw_cnfrmpart4.setFocusableInTouchMode(false);
					pw_cnfrmpart1.setFocusableInTouchMode(true);
					pw_cnfrmpart1.requestFocus();
				}
			}
		}
		// 화면에 나온 입력칸 청소
		//    	passwordForm.setText("");			// 1칸용
		//    	passwordConfirmForm.setText("");
		pwpart1.setText("");			// 4칸용		
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
	 *  비번 - (현재 모드에 따라)다음단계로 넘어간다.  
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void goToNextPhase(){
		Log.i("Password", "goToNextPhase,, currentMode::"+currentMode);		// 비번 변경 모드 : 0
		switch(currentMode){
		case PHASE_INIT_PASSWORD:		// 0
			Log.i("Password", "goToNextPhase--PHASE_INIT_PASSWORD");
			currentMode = PHASE_INPUT_PASSWORD;			// 1모드로. (새 비번1 입력모드)
			break;

		case PHASE_INPUT_PASSWORD:		// 1
			Log.i("Password", "goToNextPhase--PHASE_INPUT_PASSWORD");

			//    		currentPassword = passwordForm.getText().toString();			// 1칸용
			currentPassword = pwForms;									// 4칸용

			currentMode = PHASE_CONFIRM_PASSWORD;
			//    		passwordFlipper.setInAnimation(pushLeftIn);					// 1칸용
			//    		passwordFlipper.setOutAnimation(pushLeftOut);
			//    		passwordFlipper.showPrevious();
			passwordFlipper2.setInAnimation(pushLeftIn);					// 4칸용
			passwordFlipper2.setOutAnimation(pushLeftOut);
			passwordFlipper2.showPrevious();
			break;

		case PHASE_CONFIRM_PASSWORD:			// 2 처리 끝나고 다음 액티비티로 간다. 		 비번 신규 또는 변경이 완료된 시점이므로 비번을 저장해야 한다. currentPassword 를 pref 에 저장.
			Log.i("Password", "goToNextPhase--PHASE_CONFIRM_PASSWORD::"+currentPassword);
			//    		nextActivity.putExtra(RESULT_PASSWORD, currentPassword);		// 테스트용. 바꾼 비번 확인 화면.
			if(loginYN){		// 로그인인 경우에는 다시 메인 페이지로 돌아간다. (로그인 이후 단계를 진행한다.)
				kr.co.bettersoft.checkmileage.activities.MainActivity.loginYN = true;
				startActivity(nextActivity);    	
			}else{				// 비번 변경 설정 등인 경우 설정 페이지로 돌아간다.(비번 저장후 종료하면 돌아가진다.)
				SharedPreferences.Editor savePWcustom = sharedPrefCustom.edit();		// 공용으로 비번도 저장해 준다.
				savePWcustom.putString("password" , currentPassword);
				savePWcustom.commit();

				PrefActivityFromResource.memberInfo.setPassword(currentPassword);		// 변경된 비번을 서버에 저장시킨다..
				PrefActivityFromResource.updateLv = PrefActivityFromResource.updateLv+1;

				//    			 PrefActivityFromResource.updateToServer();		// resume 같은데서 검사해서 처리 하도록 시켜.. *** 
				Toast.makeText(Password.this, R.string.password_set_success, Toast.LENGTH_SHORT).show();
			}
			finish();
			break;
		}
	}

	/**
	 * init
	 *  초기화 - 비번 확인 및 설정 사전 준비를 한다. 
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void init(){
		Intent intent = getIntent();
		String nextActivityClassString = intent.getStringExtra(NEXT_ACTIVITY);		// 끝나고 가야할 곳
		Log.i("Password", "nextActivityClassString::"+nextActivityClassString);	
		nextActivity = new Intent();						
		nextActivity.setClassName(Password.this, nextActivityClassString);		

		initMode = intent.getIntExtra(MODE, MODE_CHECK_PASSWORD);
		Log.i("Password", "initMode::"+initMode);
		if(initMode==2){		// 로그인일 경우 - 비번 입력받고 메인 가야 하는 경우
			loginYN = true;
		}
		passwordString = intent.getStringExtra(PASSWORD);
		Log.i("Password", "passwordString::"+passwordString);
		currentMode = initMode;
		currentPassword = passwordString;
		//        passwordLength = 4;		// 비번은 무조건 4글자여야만 한다.  --> 4칸으로 나눠서 이런 설정 필요하지 않음

		// 비번 입력창과 안내 메시지창.
		textMessage = (TextView)findViewById(R.id.password_message);
		//        passwordFlipper = (ViewFlipper)findViewById(R.id.password_flipper);		// 1칸용
		passwordFlipper2 = (ViewFlipper)findViewById(R.id.password_flipper2);		// 4칸용

		//        passwordForm = (EditText)findViewById(R.id.password); 						// 1칸용

		password_linear1 = (LinearLayout)findViewById(R.id.password_linear1);		// 4칸용
		password_linear2 = (LinearLayout)findViewById(R.id.password_linear2);
		pwpart1 = (EditText)findViewById(R.id.pwpart1); 
		pwpart2 = (EditText)findViewById(R.id.pwpart2); 
		pwpart3 = (EditText)findViewById(R.id.pwpart3); 
		pwpart4 = (EditText)findViewById(R.id.pwpart4); 
		pw_cnfrmpart1 = (EditText)findViewById(R.id.pw_cnfrmpart1); 
		pw_cnfrmpart2 = (EditText)findViewById(R.id.pw_cnfrmpart2); 
		pw_cnfrmpart3 = (EditText)findViewById(R.id.pw_cnfrmpart3); 
		pw_cnfrmpart4 = (EditText)findViewById(R.id.pw_cnfrmpart4); 

		if(passwordString.length()>0){		// 비번 변경 모드.  & 로그인 모드.
			if(loginYN){
				textMessage.setText(R.string.password_input_pw);
			}else{
				textMessage.setText(R.string.password_input_old);
			}
		}else{								// 최초 비번 입력 모드
			textMessage.setText(R.string.password_input_new);
		}

		// 비번 입력 중 체크하기  -- 1칸용
		//        passwordForm.addTextChangedListener(new TextWatcher() {
		//        	public void  afterTextChanged (Editable s){}
		//            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){}
		//            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
		//            	// 길이가 같다면 비번 확인 메서드 수행
		//            	if(passwordForm.getText().toString().length() == passwordLength){
		//            		Handler passwordHandler = new Handler();
		//            		passwordHandler.postDelayed(passwordRunnable, 200);
		//        		}
		//            } 
		//        });

		// 비번 확인 창 -- 1칸용
		//        passwordConfirmForm = (EditText)findViewById(R.id.password_confirm);    
		//        // 값 변경되면 검사
		//        passwordConfirmForm.addTextChangedListener(new TextWatcher() {
		//        	public void  afterTextChanged (Editable s){
		//        	}
		//            public void  beforeTextChanged  (CharSequence s, int start, int count, int after){
		//            }
		//            public void  onTextChanged  (CharSequence s, int start, int before, int count) {
		//            	// 비번과 길이가 같다면 비번 확인 검사.
		//            	if(passwordConfirmForm.getText().toString().length() == passwordLength){
		//            		Handler passwordHandler = new Handler();
		//            		passwordHandler.postDelayed(passwordRunnable, 200);
		//        		}
		//            } 
		//        });

		// 4칸짜리. 비번 입력창.
		/**
		 * pwpart1.setOnKeyListener , pwpart1.addTextChangedListener
		 *  첫칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pwpart1.setOnKeyListener(new OnKeyListener() {                  // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    (첫칸은 동작없음) 
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//        		Log.e(TAG,"pwpart1");
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// 첫 값에선 딱히 할거 없음..
						pwpart1.setText("");
					}
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}
			}
		}); 
		pwpart1.addTextChangedListener(new TextWatcher() {		// 첫칸 리스너
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart1.length()==1) && !((pwpart1.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr1 = pwpart1.getText()+"";		// 데이터 꺼내서 저장하고 동그라미로 보여줌
					pwpart1.setText("●");
					pwpart1.setFocusableInTouchMode(false);
					pwpart2.setFocusableInTouchMode(true);
					pwpart2.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart2.setOnKeyListener , pwpart2.addTextChangedListener
		 *  두번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pwpart2.setOnKeyListener(new OnKeyListener() {          // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// pwpart1 값 지우고 그쪽으로 간다.
						pwpart1.setText("");
						pwpart2.setFocusableInTouchMode(false);
						pwpart1.setFocusableInTouchMode(true);
						pwpart1.requestFocus();
					}
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지. (화면 값을 바꾸면 리스너가 다시 동작되기 때문)
					return true; 
				}else{
					return false;
				}     
			}
		}); 
		pwpart2.addTextChangedListener(new TextWatcher() {			 // 두번째칸 리스너
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if((pwpart2.length()==1) && !((pwpart2.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr2 = pwpart2.getText()+"";
					pwpart2.setText("●");
					pwpart2.setFocusableInTouchMode(false);
					pwpart3.setFocusableInTouchMode(true);
					pwpart3.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart3.setOnKeyListener , pwpart3.addTextChangedListener
		 *  세번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pwpart3.setOnKeyListener(new OnKeyListener() {                  // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    
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
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pwpart3.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart3.length()==1) && !((pwpart3.getText()+"").equals("●"))){
					tempStr3 = pwpart3.getText()+"";
					pwpart3.setText("●");
					pwpart3.setFocusableInTouchMode(false);
					pwpart4.setFocusableInTouchMode(true);
					pwpart4.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pwpart4.setOnKeyListener , pwpart4.addTextChangedListener
		 *  네번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pwpart4.setOnKeyListener(new OnKeyListener() {                  // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    
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
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}      
			}
		}); 
		pwpart4.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pwpart4.length()==1) && !((pwpart4.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr4 = pwpart4.getText()+"";
					pwpart4.setText("●");
					// 비번 4개 다받았다..
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


		// 4칸짜리 비번 확인창.
		/**
		 * pw_cnfrmpart1.setOnKeyListener , pw_cnfrmpart1.addTextChangedListener
		 *  확인용 첫칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pw_cnfrmpart1.setOnKeyListener(new OnKeyListener() {             // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동(첫칸이라 동작 없음)     
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				Log.d(TAG,"pw_cnfrmpart1");
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// 첫 값에선 딱히 할거 없음..
						pw_cnfrmpart1.setText("");
					}
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pw_cnfrmpart1.addTextChangedListener(new TextWatcher() {		
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {					
				if((pw_cnfrmpart1.length()==1) && !((pw_cnfrmpart1.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr1 = pw_cnfrmpart1.getText()+"";
					pw_cnfrmpart1.setText("●");
					pw_cnfrmpart1.setFocusableInTouchMode(false);
					pw_cnfrmpart2.setFocusableInTouchMode(true);
					pw_cnfrmpart2.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart2.setOnKeyListener , pw_cnfrmpart2.addTextChangedListener
		 *  확인용 두번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pw_cnfrmpart2.setOnKeyListener(new OnKeyListener() {                  
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) { 
				//You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_ 
				if(keyCode == KeyEvent.KEYCODE_DEL){   
					if(dontTwice==1){
						//this is for backspace 
						// pw_cnfrmpart1 값 지우고 그쪽으로 간다.
						pw_cnfrmpart1.setText("");
						pw_cnfrmpart2.setFocusableInTouchMode(false);
						pw_cnfrmpart1.setFocusableInTouchMode(true);
						pw_cnfrmpart1.requestFocus();
					}
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}     
			}
		}); 
		pw_cnfrmpart2.addTextChangedListener(new TextWatcher() {		// 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart2.length()==1) && !((pw_cnfrmpart2.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr2 = pw_cnfrmpart2.getText()+"";
					pw_cnfrmpart2.setText("●");
					pw_cnfrmpart2.setFocusableInTouchMode(false);
					pw_cnfrmpart3.setFocusableInTouchMode(true);
					pw_cnfrmpart3.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart3.setOnKeyListener , pw_cnfrmpart3.addTextChangedListener
		 *  확인용 세번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pw_cnfrmpart3.setOnKeyListener(new OnKeyListener() {               		// 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동       
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
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}    
			}
		}); 
		pw_cnfrmpart3.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart3.length()==1) && !((pw_cnfrmpart3.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr3 = pw_cnfrmpart3.getText()+"";
					pw_cnfrmpart3.setText("●");
					pw_cnfrmpart3.setFocusableInTouchMode(false);
					pw_cnfrmpart4.setFocusableInTouchMode(true);
					pw_cnfrmpart4.requestFocus(); // 두번째EditText 로 포커스가 넘어가게 됩니다
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		/**
		 * pw_cnfrmpart4.setOnKeyListener , pw_cnfrmpart4.addTextChangedListener
		 *  확인용 네번째칸 리스너. 값 들어올때, 지우기 버튼 눌렀을때에 대한 동작
		 */
		pw_cnfrmpart4.setOnKeyListener(new OnKeyListener() {              // 지우기 버튼 눌렀을때. 앞글자 지우고 커서 이동    
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
					dontTwice = dontTwice * -1;	// 토글. 2번 실시 방지.
					return true; 
				}else{
					return false;
				}      
			}
		}); 
		pw_cnfrmpart4.addTextChangedListener(new TextWatcher() {	// 값 입력시 리스너. 값 저장후 동그라미로 변경
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((pw_cnfrmpart4.length()==1) && !((pw_cnfrmpart4.getText()+"").equals("●"))){  // edit1  값의 제한값을 6이라고 가정했을때
					tempStr4 = pw_cnfrmpart4.getText()+"";
					pw_cnfrmpart4.setText("●");
					// 비번 4개 다받았다..
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
	// 애니메이션 효과.
	/**
	 * initAnimation
	 *  비번창 등장 또는 틀렸을 경우에 대한 애니메이션 효과를 준다
	 *
	 * @param
	 * @param
	 * @return
	 */
	private void initAnimation(){
		pushLeftIn = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f,   	// 등장.새 비번 입력 받는 창이 나타난다. 
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,		
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		pushLeftIn.setDuration(200);
		pushLeftIn.setFillAfter(true);

		pushLeftOut = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,   
				TranslateAnimation.RELATIVE_TO_SELF, -2.0f,		// 퇴장.기존 비번은 화면 밖으로 사라진다. 
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		pushLeftOut.setDuration(200);
		pushLeftOut.setFillAfter(true);

		shakeAni = new TranslateAnimation(						// 흔들기. 틀렸을때.
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,   
				TranslateAnimation.RELATIVE_TO_SELF, 0.05f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		shakeAni.setDuration(300);
		shakeAni.setInterpolator(new CycleInterpolator(2.0f));
	}
}