package kr.co.bettersoft.checkmileage.activities;

/*
 *    --> 사용자 동의 받는 화면. 체크 후 하단 확인 버튼 누르면 다음 화면으로 이동한다.
 *    
 * x인증 1단계. - 폰번 인증 화면.    -- 사용 안함
 * x어플 실행하여 QR 저장소에 QR이 없을 경우에 실행 된다.
 * 
 * x서버와 통신하여 폰번이 있는지 여부를 체크한다.
 * x폰번이 있고 QR코드가 있으면 서버에서 받아온 QR 코드를 그대로 사용한다.
 * 
 * x서버에  정보가 없다면 신규 유저이기 때문에 2차 인증 화면으로 이동한다. 
 * x (동시에 서버에서 인증번호를 SMS로 발송하여 인증받도록 한다)
 *  
 * x <화면 구성>
 * x  모바일에서 폰번호를 가져와서 기본값으로 채움.(수정가능)
 * x  중단 버튼을 눌러 [인증번호 요청] 을 하여 서버와 통신을 한다. 
 * x  서버에서 가져온 결과를 분석하여 2차 인증으로 갈지, QR 생성화면으로 갈지 여부를 판단한다.
 *   
 *
 *    
 *    
 */



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.CertificationStep2;
import kr.co.bettersoft.checkmileage.activities.CommonUtils;
import kr.co.bettersoft.checkmileage.activities.Main_TabsActivity;
import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity;
import kr.co.bettersoft.checkmileage.pref.Password;
import kr.co.bettersoft.checkmileage.pref.PrefActivityFromResource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CertificationStep1 extends Activity {
	
	CheckBox checkBoxPrivacyTerms, checkBoxGPSTerms; // 개인정보 체크, 위치정보 체크
	Button seePrivacyTermsBtn, seeGPSTermsBtn, userConfirmBtn; 	// 개인정보방침 보기 버튼 , 위치정보 방침보기 버튼, 다음(인증)화면으로 이동하기 버튼
//	TextView txt2;
//	static String phoneNum = "";
//	String certificationNumber = "";
	String TAG = "CertificationStep0";
//	String registerDate = "";
//	int responseCode = 0;
	
//	String controllerName = "";		// 서버 조회시 컨트롤러 이름
//	String methodName = "";			// 서버 조회시 메서드 이름
//	String serverName = CommonUtils.serverNames;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){
					Toast.makeText(CertificationStep1.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showAlert")==1){					 // 경고창 . 
					//
					new AlertDialog.Builder(returnThis())
					.setTitle(CommonUtils.alertTitle)							// *** 하드코딩 얼럿 창 타이틀. --> Carrot
					.setMessage(b.getString("msg"))
					//					.setIcon(android.R.drawable.ic_dialog_alert)		// 경고창. 삼각형 느낌표..?
					.setIcon(R.drawable.ic_dialog_img)		// 경고창. 삼각형 느낌표 --> 어플 아이콘으로바꿈.
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// 그냥 사용자 확인 용이기 때문에 추가 조작 없음.
						}})
						.setNegativeButton("", null).show();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	public Context returnThis(){
		return this;
	}
	/**
	 * showResultDialog
	 * 토스트를 얼럿으로 바꾼다.
	 *
	 * @param msg
	 * @param 
	 * @return 
	 */
	public void showResultDialog(final String msg){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("showAlert", 1);
						b.putString("msg", msg);			// 화면에 보여줄 메시지
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}
	public void alertMsg(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						String alrtMsg = getString(R.string.certi_fail_msg);
						b.putInt("showErrToast", 1);
						b.putString("msg", alrtMsg);			
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.certification_step1);

	    seePrivacyTermsBtn = (Button) findViewById(R.id.seePrivacyTermsBtn);	// 개인정보 방침 보기 버튼
	    seeGPSTermsBtn = (Button) findViewById(R.id.seeGPSTermsBtn);			// 위치정보 방침 보기 버튼
	    userConfirmBtn = (Button) findViewById(R.id.userConfirmBtn);			// 하단 확인 버튼

	    checkBoxPrivacyTerms = (CheckBox) findViewById(R.id.checkBoxPrivacyTerms);	// 개인정보 수집 동의 체크
	    checkBoxGPSTerms = (CheckBox) findViewById(R.id.checkBoxGPSTerms);			// 위치정보 수집 동의 체크
	    
//	    button2 = (Button) findViewById(R.id.certi_btn2);
//	    txt2 = (TextView)findViewById(R.id.certi_text2);
//	    
////		button2.setText("인증확인");
//		button2.setText(R.string.certi_step2_btn1);
//		
	    seePrivacyTermsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 인텐트 - 웹뷰로 이용약관 창 띄움.
				Intent webIntent = new Intent(CertificationStep1.this, myWebView.class);
				webIntent.putExtra("loadingURL", CommonUtils.termsPolicyURL);		
				startActivity(webIntent);
			}
		});
	    seeGPSTermsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 인텐트 - 웹뷰로 개인정보 방침 보기 창 띄움
				Intent webIntent = new Intent(CertificationStep1.this, myWebView.class);
				webIntent.putExtra("loadingURL", CommonUtils.privacyPolicyURL);		 
				startActivity(webIntent);
			}
		});
	    userConfirmBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 체크박스 체크하고 체크되있으면 다음화면으로, 안되있으면 알림창 . 현재 체크되있는지 확인하는 부분은 미구현
				if(checkTheCheckBox()){
					Intent intent = new Intent(CertificationStep1.this, CertificationStep2.class);
					startActivity(intent);   
					finish();
				}else{
					showResultDialog(getString(R.string.you_did_not_agree));
				}
				
			}
		});
	}
	
	// 두개의 체크박스가 체크되어있는지 확인 한다.
	public Boolean checkTheCheckBox(){
		if(checkBoxPrivacyTerms.isChecked()&&checkBoxGPSTerms.isChecked()){
			return true;
		}else{
			return false;
		}
	}
	
}
