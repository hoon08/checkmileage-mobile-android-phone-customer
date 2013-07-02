package kr.co.bettersoft.checkmileage.activities;
/*
 * 인증2단계.     -- 사용 안함 --> 다시 사용. 인증 화면.
 * 
 *  
*   --> 사용자 동의 받고나서 인증하는 화면. 1페이지 체제.
 *   상단 전번넣고 인증번호 요청 버튼 누른다.
 *    그럼 SMS로 인증번호가 오는데 그번호 넣고 우측 OK 버튼 눌러 인증한다.
 *    
 *    인증 완료되면 하단 확인 버튼 눌러 진행한다..(사실 하단 확인버튼 없이 오토로 진행해도 될것 같긴 하다)
 *    
 *    다음 페이지는 NoQR페이지이다...
 *    
 *    그러니까 qr이 없다는 가정하에 띄우면 될 것 같다. 인트로 끝나고 noqr 로 갈때..그러면 체크 안해도 되니까..? 기존사용자가 문제가 되므로..
 *    
 *    
 *    제대로 만들자면 인트로 로딩 끝나고. 동의 받았는지 체크하여 동의 받고.
 *    그 이후에 qr 있는지 확인하여 noqr 또는 메인화면으로 이동한다.
 *    
 *    중간 건너가는 액티비티가 필요할 것이다.. 또는 1페이지 안에 여러 기능들을 합쳐놓거나..
 *    
 *    
 *    
 * 신규 유저일 경우 서버에서 인증번호를 SMS 로 발송해 준다.
 * 모바일에서는 인증번호 요청 화면에서 인증번호 확인 화면(현재 화면) 으로 변경된다.
 * 
 * SMS로 날라온 인증번호를 화면 상단의 인증번호 입력 란에 넣고 
 *   화면 중단의 [인증하기] 버튼을 눌러 서버와 통신하여 인증을 받는다.
 *   
 * 서버로부터 인증 결과를 받아서 그에 따라 화면이 분기 된다.
 *  인증 성공 시 QR 생성 - 선택 화면으로 이동한다.
 *  인증 실패 시 인증1 화면으로 돌아간다. (처음 인증 1 화면 열릴때처럼 폰번호 읽고, 버튼 눌러 서버에 요청..등은 같다.)  
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.CertificationStep1;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonUtils;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CertificationStep2 extends Activity {
	

	Button requestCertiNumBtn, certiNumConfirmBtn;	// 인증번호 요청 버튼, 인증 버튼 , 하단 확인 버튼	
	EditText userPhoneNumber, userCertiNumber;			// 사용자 전화번호 입력창 , 인증번호 입력창
	String TAG = "CertificationStep1";
	
	String phoneNum = "";
	String qrcode ="";
	
//	int responseCode = 0;
//	String serverName = CommonUtils.serverNames;
//	String controllerName = "";
//	String methodName = "";
//	URL postUrl2;
//	HttpURLConnection connection2 ;
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	// checkMileageCustomerRest = new CheckMileageCustomerRest();	// oncreate
	
	Boolean loading = false;
	
	
	Boolean certified = false;	
	String certiResult="";
	// 설정 파일 저장소  --> QR 코드도 저장하는걸로..
	SharedPreferences sharedPrefCustom;
	
	
	// 키보드 자동 숨기기 위한 부모 레이아웃(리스너 달아서 키보드 숨김)과 입력 매니저
	View parentLayout;
	InputMethodManager imm;		// 키보드 제어
	
	// 시간관련
	Calendar c = Calendar.getInstance();
	int todayYear = 0;						// 지금 -  년 월 일 시 분
	int todayMonth = 0;
	int todayDay = 0;
	int todayHour = 0;
	int todayMinute = 0;
	int todaySecond = 0;
	int birthYear = 0;						// 생년월일 - 년 월 일
	int birthMonth= 0;
	int birthDay = 0;
	
	
	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){
					Toast.makeText(CertificationStep2.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showAlert")==1){					 // 알림 및 경고창 . 
					new AlertDialog.Builder(returnThis())
					.setTitle(CommonUtils.alertTitle)							
					.setMessage(b.getString("msg"))
					//					.setIcon(android.R.drawable.ic_dialog_alert)		// 경고창. 삼각형 느낌표..?
					.setIcon(R.drawable.ic_dialog_img)		// 경고창. 삼각형 느낌표 --> 어플 아이콘으로바꿈.
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// 그냥 사용자 확인 용이기 때문에 추가 조작 없음.
						}})
						.setNegativeButton("", null).show();
				}
				if(b.getInt("showAlert")==2){					 // 인증 성공 
					// 토스트만 띄우고 바로 패스한다.
					Toast.makeText(CertificationStep2.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(CertificationStep2.this, MainActivity.class);	
					startActivity(intent);   
					finish();
					
					// 알림창 띄우고 확인 누르면 패스 하는 방식 --> 스피드를 위해 토스트만 띄우고 패스하도록함.
//					new AlertDialog.Builder(returnThis())
//					.setTitle(CommonUtils.alertTitle)							
//					.setMessage(b.getString("msg"))
//					//					.setIcon(android.R.drawable.ic_dialog_alert)		// 경고창. 삼각형 느낌표..?
//					.setIcon(R.drawable.ic_dialog_img)		// 경고창. 삼각형 느낌표 --> 어플 아이콘으로바꿈.
//					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int whichButton) {
//							Intent intent = new Intent(CertificationStep2.this, MainActivity.class);	
//							startActivity(intent);   
//							finish();
//						}})
//						.setNegativeButton("", null).show();
				}
				if(b.getInt("order")==1){
					// 프로그래스바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.certi_progressbar1);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 프로그래스바  종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.certi_progressbar1);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	public void alertMsg(){			// 인증 실패 메시지
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
	// 인증 확인 메시지를 띄우고 확인을 누르면 다음 화면으로.
	public void showResultDialog2(final String msg){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("showAlert", 2);
						b.putString("msg", msg);			// 화면에 보여줄 메시지
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
		setContentView(R.layout.certification_step2);
		
		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		requestCertiNumBtn = (Button) findViewById(R.id.requestCertiNumBtn);	// 인증번호 요청 버튼
		certiNumConfirmBtn = (Button) findViewById(R.id.certiNumConfirmBtn);	// 인증 버튼
		
		userPhoneNumber = (EditText)findViewById(R.id.userPhoneNumber);		// 전화번호 입력창
		userCertiNumber = (EditText)findViewById(R.id.userCertiNumber);		// 인증번호 입력창
		
		
		// 부모 레이아웃 리스너 - 외부 터치 시 키보드 숨김 용도 
		parentLayout = findViewById(R.id.certi_step2_parent_layout);		
		parentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Log.w(TAG,"parentLayout click");
				hideKeyboard();
			}
		});
		
		
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		
		
//		//자신의 전화번호 가져오기  --> 기능 제거 . 모든 사용자가 직접 입력하도록 한다.
//		try{		// 읽다가 에러 터질때에 대비하기
//			TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
//			phoneNum = telManager.getLine1Number();
//			
//			String pnTemp = phoneNum;
//			if(pnTemp.contains("+82")){
//				pnTemp = pnTemp.replace("+82", "0");							
//				Log.d(TAG,"pnTemp"+pnTemp);
//				phoneNum = pnTemp;
//			}
//		}catch(Exception e){}
//		// 못읽거나 문제 생겼을때는 그냥 공백 (직접 입력 하도록 한다)
		
		if(phoneNum==null || phoneNum.length()<1){
			phoneNum = "";
		}
		userPhoneNumber.setText(phoneNum);
		
//		button1.setText("인증번호 요청하기);				// 하드코딩
		requestCertiNumBtn.setText(R.string.certi_step1_btn1);			// 다국어 지원.

			// 안내문.: 임시 인증 번호가 SMS로 발송 됩니다. 새로운 인증 번호를 요청하실 경우 이전 인증 번호는 사용할 수 없습니다.
		// 인증번호 요청 버튼. 전화번호를 가지고 서버로 인증번호를 요청한다. 보낸 번호로 인증번호가 날아올 것이다.
		
		// 서버로 전화번호를 보내서 인증번호를 요청한다.
		requestCertiNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if((userPhoneNumber.getText()+"").length()>0){
					new backgroundThreadCertificationStep_1().execute();
//					try {
//						showPb();
//						certificationStep1();
//					} catch (JSONException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				}else{		// 전번없음
					showResultDialog(getString(R.string.no_phone_num));
				}
			}
		});
		// 인증버튼. 인증번호를 가지고 서버에 인증 절차를 밟는다.
		certiNumConfirmBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if((userCertiNumber.getText()+"").length()!=4){
//					userCertifiedComplete();		// 패스 시켜줌		// test 용 *** 
					showResultDialog(getString(R.string.input_certi_4nums));		// 원본. 나중에 주석 제거하여 사용. *** 
				}else{
					// 서버로 인증번호를 보내서 인증번호를 확인한다.
					hideKeyboard();
					showPb();
					new backgroundThreadCertificationStep_2().execute();
//					try {
//						certificationStep2();
//					} catch (JSONException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				}
			}
		});
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 싱글 스레드 사용	
		/**
		 * backgroundThreadCertificationStep_1
		 * 비동기 스레드  서버와 통신하여 채팅방 목록을 가져온다.
		 * @author blue
		 *
		 */
		public class backgroundThreadCertificationStep_1 extends AsyncTask<Void, Void, Void>{
			@Override protected void onPostExecute(Void result) {  
			} 
			@Override protected void onPreExecute() {  
			} 
			@Override protected Void doInBackground(Void... params) {  
				Log.d(TAG,"backgroundThreadCertificationStep_1");

				// 파리미터 세팅
				CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 	
				checkMileageMembersParam.setPhoneNumber(userPhoneNumber.getText()+"");	
				// 호출
//				if(!pullDownRefreshIng){
					showPb();
//				}
				callResult = checkMileageCustomerRest.RestCertificationStep_1(checkMileageMembersParam);
				hidePb();
				// 결과 처리
				if(callResult.equals("SUCCESS")){				// 인증 성공
	    			Log.i(TAG, "SUCCESS");
	    			showResultDialog(getString(R.string.certi_num_req_success));	
	    		}else{														// 인증 실패
	    			Log.i(TAG, "FAIL_ADMISSION");
	    			showResultDialog(getString(R.string.certi_num_req_fail));
	    		}
				return null; 
			}
		}
	
	
	
	/*
	 * 서버와 통신하여 인증 1단계 수행.
	 * request certi number
	 * 인증번호 요청
	 * 
	 * http://checkmileage.onemobileservice.com/checkMileageCertificationController/requestCertification
	 * 파라메터 : phoneNumber, activateYn, modifyDate, registerDate
	 * 예 ) phoneNumber : 01085858025
	 * activateYn : Y
	 * modifyDate : 2013-03-06 14:20:12
	 * registerDate : 2013-03-06 14:20:12
	 * 
	 * (인증 제한 시간은 2분. 나중에 변경 가능하다..)
	 * 
	 * checkMileageCertification	결과.
	 * String idCheckMileageCertifications;                // 테이블 아이디.
	 * String phoneNumber;                                 // 휴대전화 번호.
	 * String certificationNumber;                         // 인증 번호.
	 * String activateYn;                                  // 활성화 여부.
	 * String modifyDate;                                  // 수정 일자.
	 * String registerDate;                                // 등록 일자.
	 * String totalCount;                                  // 갯수.
	 * String result;                                      // 처리 결과.
	 */
//	public void certificationStep1() throws JSONException, IOException {
//		Log.i("certificationStep1", "certificationStep1");
//		controllerName = "checkMileageCertificationController";		
//		methodName = "requestCertification";						
//		new Thread(
//				new Runnable(){
//					public void run(){
//						JSONObject obj = new JSONObject();
//    					// 시각
//    					Date today = new Date();
//    				    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    				    String nowDate = sf.format(today);
////						String nowTime = getNow();
//						try{
//							obj.put("phoneNumber", userPhoneNumber.getText()+"");	
////							Log.d(TAG,"phoneNumber::"+ userPhoneNumber.getText()+"");
//							obj.put("activateYn", "Y");
//							obj.put("modifyDate", nowDate);
//							obj.put("registerDate", nowDate);
//						}catch(Exception e){
//							e.printStackTrace();
//						}
//						String jsonString = "{\"checkMileageCertification\":" + obj.toString() + "}";
//						try{
//							  postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
//							  connection2 = (HttpURLConnection) postUrl2.openConnection();
//					  		  connection2.setDoOutput(true);
//					  		  connection2.setInstanceFollowRedirects(false);
//					  		  connection2.setRequestMethod("POST");
//					  		  connection2.setRequestProperty("Content-Type", "application/json");
//					  		  OutputStream os2 = connection2.getOutputStream();
//					  		  os2.write(jsonString.getBytes("UTF-8"));
//					  		  os2.flush();
//					  		  System.out.println("postUrl      : " + postUrl2);
//					  		  System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
//					  		  responseCode = connection2.getResponseCode();
//					  		  InputStream in =  connection2.getInputStream();
//					  		  theData1(in);
//						}catch(Exception e){ 
//						 e.printStackTrace();
//						 hidePb();
//						}  
//    				}
//    			}
//    	).start();
//	}
//	/*
//	 * 인증 1단계의 결과를 받음.
//	 */
//	public void theData1(InputStream in){
//		Log.d(TAG,"theData");
//		hidePb();
//    	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//    	StringBuilder builder = new StringBuilder();
//    	String line =null;
//    	try {
//			while((line=reader.readLine())!=null){
//				builder.append(line).append("\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
////    	Log.d(TAG,"수신::"+builder.toString());
//    	String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
//    	
//    	JSONObject jsonObject;
//		try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	certiResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+certiResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 
//    	if(responseCode==200 || responseCode==204){		// 인증 성공시
//    		// QR데이터를 꺼내어 저장..
//    		// 기타 정보들을 설정 파일에 저장....
//    		if(certiResult.equals("SUCCESS")){				// 인증 성공
//    			Log.i(TAG, "SUCCESS");
//    			showResultDialog(getString(R.string.certi_num_req_success));	
//    		}else{														// 인증 실패
//    			Log.i(TAG, "FAIL_ADMISSION");
//    			showResultDialog(getString(R.string.certi_num_req_fail));
//    		}
//    	}else{			// 인증 실패시	 토스트 띄우고 화면 유지.
//    		alertMsg();
//    	}
//    }
	
		
		/**
		 * backgroundThreadCertificationStep_2
		 * 비인증2단계 수행.
		 * @author blue
		 *
		 */
		public class backgroundThreadCertificationStep_2 extends AsyncTask<Void, Void, Void>{
			@Override protected void onPostExecute(Void result) {  
			} 
			@Override protected void onPreExecute() {  
			} 
			@Override protected Void doInBackground(Void... params) {  
				Log.d(TAG,"backgroundThreadCertificationStep_2");

				// 파리미터 세팅
				CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 	
				checkMileageMembersParam.setPhoneNumber(userPhoneNumber.getText()+"");	
				checkMileageMembersParam.setCertiNum(userCertiNumber.getText()+"");
				// 호출
//				if(!pullDownRefreshIng){
					showPb();
//				}
				callResult = checkMileageCustomerRest.RestCertificationStep_2(checkMileageMembersParam);
				hidePb();
				// 결과 처리
				if(certiResult.equals("SUCCESS_ADMISSION")){				// 인증 성공
	    			Log.i(TAG, "SUCCESS_ADMISSION");
	    	    	userCertifiedComplete();		// 패스 시켜줌
	    		}else{														// 인증 실패
	    			Log.i(TAG, "FAIL_ADMISSION");
	    			showResultDialog(getString(R.string.certi_fail));
	    		}
				return null; 
			}
		}	
		
		
//	/*
//	 * 서버와 통신하여 인증2단계 수행.
//	 * http://checkmileage.onemobileservice.com/checkMileageCertificationController/requestAdmission
//	 * 파라메터 : phoneNumber, certificationNumber, activateYn
//	 * 예 ) phoneNumber : 01085858025
//	 * certificationNumber : 4792
//	 * activateYn : Y
//	 * 
//	 * 
//	 */
//	public void certificationStep2() throws JSONException, IOException {
//    	Log.i(TAG, "certificationStep2");
//    	
//    	controllerName = "checkMileageCertificationController";		// 서버 조회시 컨트롤러 이름
//    	methodName = "requestAdmission";							// 서버 조회시 메서드 이름
//    	
//    	new Thread(
//    			new Runnable(){
//    				public void run(){
//    					 // 전달 데이터
//						JSONObject obj = new JSONObject();
//						phoneNum = userPhoneNumber.getText()+"";
//						try{
//							obj.put("phoneNumber", phoneNum);			// 전번
//							obj.put("certificationNumber", userCertiNumber.getText()+"");	// 승인번호		
//							obj.put("activateYn", "Y");
//						}catch(Exception e){
//							e.printStackTrace();
//						}
//						String jsonString = "{\"checkMileageCertification\":" + obj.toString() + "}";
//						try{
//							  URL postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);	
//					  		  HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
//					  		  connection2.setDoOutput(true);
//					  		  connection2.setInstanceFollowRedirects(false);
//					  		  connection2.setRequestMethod("POST");
//					  		  connection2.setRequestProperty("Content-Type", "application/json");
//					  		  OutputStream os2 = connection2.getOutputStream();
//					  		  os2.write(jsonString.getBytes("UTF-8"));
//					  		  os2.flush();
//					  		  System.out.println("postUrl      : " + postUrl2);
//					  		  System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
//					  		  responseCode = connection2.getResponseCode();
//					  		  InputStream in =  connection2.getInputStream();
//					  		  theData2(in);
//						}catch(Exception e){ 
//						 e.printStackTrace();
//						}finally{
//							hidePb();
//						}
//    				}
//    			}
//    	).start();
//	}
//	/*
//	 * 인증 2단계의 결과를 받음.
//	 */
//	public void theData2(InputStream in){
//		Log.d(TAG,"theData");
//    	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//    	StringBuilder builder = new StringBuilder();
//    	String line =null;
//    	try {
//			while((line=reader.readLine())!=null){
//				builder.append(line).append("\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
////    	Log.d(TAG,"get ::"+builder.toString());
//    	String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
//    	
//    	JSONObject jsonObject;
//		try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	certiResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+certiResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 
//    	if(responseCode==200 || responseCode==204){		// 인증 성공시
//    		// QR데이터를 꺼내어 저장..
//    		// 기타 정보들을 설정 파일에 저장....
//    		if(certiResult.equals("SUCCESS_ADMISSION")){				// 인증 성공
//    			Log.i(TAG, "SUCCESS_ADMISSION");
//    	    	userCertifiedComplete();		// 패스 시켜줌
//    		}else{														// 인증 실패
//    			Log.i(TAG, "FAIL_ADMISSION");
//    			showResultDialog(getString(R.string.certi_fail));
//    		}
//    	}else{			// 인증 실패시	 토스트 띄우고 화면 유지.
//    		alertMsg();
////    		Toast.makeText(CertificationStep1.this, R.string.certi_fail_msg, Toast.LENGTH_SHORT).show();
//    	}
//	}
	
	public void userCertifiedComplete(){
		// 인증 완료 후 작업
		SharedPreferences.Editor updateDone =   sharedPrefCustom.edit();
		updateDone.putString("agreedYN", "Y");			// 인증 여부 저장
		updateDone.putString("phoneNum", phoneNum);		// 전번도 저장
		updateDone.commit();
		showResultDialog2(getString(R.string.certi_success));	
		certified = true;		
	}
	
	
//	// 현재 시각 구하기.
//	/**
//	 * getNow
//	 *  현재 시각 구한다
//	 *
//	 * @param
//	 * @param
//	 * @return nowTime
//	 */
//	public String getNow(){
//		c = Calendar.getInstance();
//		todayYear = c.get(Calendar.YEAR);
//		todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
//		todayDay = c.get(Calendar.DATE);
//		todayHour = c.get(Calendar.HOUR_OF_DAY);
//		todayMinute = c.get(Calendar.MINUTE);
//		todaySecond = c.get(Calendar.SECOND);
//		String tempMonth = Integer.toString(todayMonth);
//		String tempDay = Integer.toString(todayDay);
//		String tempHour = Integer.toString(todayHour);
//		String tempMinute = Integer.toString(todayMinute);
//		String tempSecond = Integer.toString(todaySecond);
//		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
//		if(tempDay.length()==1) tempDay = "0"+tempDay;
//		if(tempHour.length()==1) tempHour = "0"+tempHour;
//		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
//		if(tempSecond.length()==1) tempSecond = "0"+tempSecond;
//		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute+":"+tempSecond;
//		return nowTime;
//		//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
//	}
	
	// 키보드 숨김
	/**
	 * hideKeyboard
	 * 키보드 숨긴다
	 *
	 * @param 
	 * @param 
	 * @return nowTime
	 */
	public void hideKeyboard(){
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE );
		imm .hideSoftInputFromWindow(userCertiNumber.getWindowToken(), 0);
	}

	// 중앙 프로그래스바 보임, 숨김
	/**
	 * showPb
	 *  중앙 프로그래스바 가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showPb(){
		new Thread( 
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 1);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}
	/**
	 * hidePb
	 *  중앙 프로그래스바 비가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void hidePb(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 2);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}

	
}
