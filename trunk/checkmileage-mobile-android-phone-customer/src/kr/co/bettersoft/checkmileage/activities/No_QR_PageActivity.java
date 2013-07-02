package kr.co.bettersoft.checkmileage.activities;
/**
 * No_QR_PageActivity
 * 
 *  QR 없을 경우 QR 추가 페이지. -> QR 획득 방법 선택. 1.QR생성. 2.QR스켄
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CommonUtils;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class No_QR_PageActivity extends Activity {
	String TAG = "No_QR_PageActivity";
	String phoneNumber= "";

	Button button1;
	Button button2;
	Button button3;

	
	// 서버 통신용
	String controllerName ="";
	String methodName ="";
	String serverName = CommonUtils.serverNames;
	URL postUrl2 = null;
	HttpURLConnection connection2 = null;
	int responseCode = 0;
	
	// 설정 파일 저장소  --> 전번 꺼내기 용도
	SharedPreferences sharedPrefCustom;
	
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		
				if(b.getInt("showAlert")==1){					 // 경고창 . 
					//
					new AlertDialog.Builder(returnThis())
					.setTitle(CommonUtils. alertTitle )                                        // *** "Carrot" 하드코딩 --> 변수사용 
					.setMessage(b.getString("msg"))
					.setIcon(R.drawable.ic_dialog_img)		
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							//
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
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("No_QR_PageActivity", "select method to get QR");
		setContentView(R.layout.no_qr_page);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button3.setVisibility(View.GONE);
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		
		Intent rIntent = getIntent();
		phoneNumber = rIntent.getStringExtra("phoneNumber");
		String phoneNum = sharedPrefCustom.getString("phoneNum", "");
		
		if((phoneNumber==null || phoneNumber.length()<1) && (phoneNum.length()>0)){		// phoneNum ok
			phoneNumber = phoneNum;
		}
		
		// QR 생성 버튼 클릭시.
		button1.setOnClickListener(new OnClickListener() {
			public void onClick(View V){
				/* 버튼이 눌렸을 때 실행될 코드입니다. */
				Log.i("No_QR_PageActivity", "go Create QR");
				Intent intent = new Intent(No_QR_PageActivity.this, CreateQRPageActivity.class);
				intent.putExtra("phoneNumber", phoneNumber);
				button1.setEnabled(false);
				button2.setEnabled(false);
				startActivity(intent);
				finish();
			}
		});

		// QR 스켄 버튼 클릭시.
		button2.setOnClickListener(new OnClickListener() {
			public void onClick(View V){
				/* 버튼이 눌렸을 때 실행될 코드입니다. */
				Log.i("No_QR_PageActivity", "go Scan QR");
				Intent intent = new Intent(No_QR_PageActivity.this, ScanQRPageActivity.class);
				intent.putExtra("phoneNumber", phoneNumber);
				startActivity(intent);
				finish();
			}
		});
		// 전화번호로 가져오기
		button3.setOnClickListener(new OnClickListener() {
			public void onClick(View V){
				/* 버튼이 눌렸을 때 실행될 코드입니다. */
//				Log.i(TAG, "getQRFromServerByPhoneNumber");
				
				if(phoneNumber.length()<1){	// 전번이 없다.
					alert(getString(R.string.no_phone_num_info));
				}else{	// 전번으로 qr 정보 받아온다.
//					alert("전화번호 정보가 있습니다."+phoneNumber);					
					new backgroundGetQRNumFromServerByPhoneNumber().execute();	// *** 		phoneNum 
				}
				
				
//				Intent intent = new Intent(No_QR_PageActivity.this, ScanQRPageActivity.class);
//				intent.putExtra("phoneNumber", phoneNumber);
//				startActivity(intent);
//				finish();
			}
		});

	}

	
	/**
	 * showResultDialog
	 *  얼럿 띄운다
	 *
	 * @param msg
	 * @param
	 * @return 
	 */
	public void alert(final String msg){
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
	

	/**
	 * onActivityResult
	 *  QR 스켄 결과를 받아 처리한다
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("No_QR_PageActivity", "recieve sign kill");
		// 자식 인텐트. createQR, ScanQR 로부터 종료 사인을 받음.
		if(requestCode == 111){
			if(resultCode == RESULT_OK){
				Log.i("No_QR_PageActivity", "No_QR_PageActivity activity off");
				button1.setEnabled(false);
				button2.setEnabled(false);
				finish();		// 종료함.
			}
		}
	}


	/**
	 * backgroundGetQRFromServerByPhoneNumber
	 *  비동기로 사용자 전번을 가지고 서버와 통신하여 QR 코드가 있다면 가져오는 함수 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetQRNumFromServerByPhoneNumber extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetQRNumFromServerByPhoneNumber");
			getQRNumFromServerByPhoneNumber();
			return null; 
		}
	}
	/**
	 * getQRNumFromServerByPhoneNumber
	 *  사용자 전번을 가지고 서버와 통신하여 QR 코드가 있다면 가져온다.
	 * @param 
	 * @param
	 * @return
	 */
	public void getQRNumFromServerByPhoneNumber(){
		Log.i(TAG, "getQRNumFromServerByPhoneNumber");
		controllerName = "checkMileageMileageController";
		methodName = "selectMemberExistByPhoneNumber";
//		showPb();
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("phoneNumber", phoneNumber);
							obj.put("activateYn", "Y");
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						InputStream in = null;
						try{
							postUrl2 = new URL(serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							//							connection2.connect();		
							Thread. sleep(200);
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							Thread. sleep(200);
							responseCode = connection2.getResponseCode();
							in =  connection2.getInputStream();
							resultTotalCountByPhoneNumber(in);
						}catch(Exception e){ 
//							hidePb();
							e.printStackTrace();
							alert(getString(R.string.request_failed));
						}
					}
				}).start();
	}
	
	
	// 고객 숫자 확인
	/**
	 * resultTotalCountByPhoneNumber
	 *  전번 통한 고객 숫자 확인. 1명 있으면 qr 코드 가져와야 한다
	 *
	 * @param in
	 * @param
	 * @return
	 */
	public void resultTotalCountByPhoneNumber(InputStream in){
		Log.d(TAG,"resultTotalCountByPhoneNumber");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		hidePb();
		Log.w(TAG,"resultTotalCountByPhoneNumber ::"+builder.toString());
		
//		String tempstr = builder.toString();	
//		if(responseCode==200 || responseCode==204){		// 요청 성공
//			try {
//				jsonObject = new JSONObject(tempstr);
//				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMileage");
//				String result = "";
//				try{
//					result = jsonobj2.getString("result"); 
//					hidePb();
//					if(result.equals("SUCCESS")){
//						//						showMsg("캐럿을 사용하였습니다.");
//						showResultDialog(getString(R.string.carrot_use_success));				// 캐럿을 사용하였습니다.
//					}else{
//						//						showMsg("캐럿 사용에 실패하였습니다.\n 다시 시도해 주십시오.");
//						showResultDialog(getString(R.string.carrot_use_failed));			// 캐럿 사용에 실패하였습니다.\n 다시 시도해 주십시오.
//					}
//				}catch(Exception e){
//					//					showMsg("캐럿 사용에 실패하였습니다.\n 다시 시도해 주십시오.");
//					showResultDialog(getString(R.string.carrot_use_failed));			// 캐럿 사용에 실패하였습니다.\n 다시 시도해 주십시오.
//				}
//				Log.w(TAG,"result:"+result);
//				init();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			} 
//		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.  200, 204 이외의 경우.
//			//			showMsg("요청이 실패하였습니다.\n잠시후 다시 시도해 주십시오.");
//			showResultDialog(getString(R.string.request_failed));		// 요청이 실패하였습니다.\n잠시후 다시 시도해 주십시오.
//		}
	}
	
	
	
	
	
	
	
	
	@Override
	public void onBackPressed() {
		// 여기서 종료시킬때는 다음에 잘 동작하도록 카운팅을 조절해야한다.
		DummyActivity.count = 0;
		finish();
	}
	
	@Override
	protected void onResume() {		// 카메라 스켄하다 말고 왔거나 등의 이유로 돌아왔을때 기능 사용 가능하도록 함
		super.onResume();
		button1.setEnabled(true);
		button2.setEnabled(true);
	};
	
	
	
}