package kr.co.bettersoft.checkmileage.activities;
/**
 * No_QR_PageActivity
 * 
 *  QR 없을 경우 QR 추가 페이지. -> QR 획득 방법 선택. 1.QR생성. 2.QR스켄
 */

import org.json.JSONObject;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;
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

public class No_QR_PageActivity extends Activity {
	String TAG = "No_QR_PageActivity";
	final int GET_QR_NUM_FROM_SERVER_BY_PHONE_NUMBER = 701;
	
	String phoneNumber= "";
	
	// 설정 파일 저장소  --> 전번 꺼내기 용도
	SharedPreferences sharedPrefCustom;
	
	// 화면 구성
	Button button1;
	Button button2;
	Button button3;
	
	// 서버 통신용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		
				if(b.getInt("showAlert")==1){					 // 경고창 . 
					//
					new AlertDialog.Builder(returnThis())
					.setTitle(CommonConstant. alertTitle )                                        // *** "Carrot" 하드코딩 --> 변수사용 
					.setMessage(b.getString("msg"))
					.setIcon(R.drawable.ic_dialog_img)		
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							//
						}})
						.setNegativeButton("", null).show();
				}
				
				switch (msg.what)
				{
					case GET_QR_NUM_FROM_SERVER_BY_PHONE_NUMBER : runOnUiThread(new RunnableGetQRNumFromServerByPhoneNumber());
					break;
					default : 
					break;
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

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
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
				
			}
		});

		
//		handler.sendEmptyMessage(GET_QR_NUM_FROM_SERVER_BY_PHONE_NUMBER);	// 기능 보류. 실행시 에러 발생함.
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 러너블.  사용자 전번을 가지고 서버와 통신하여 QR 코드가 있다면 가져오는 함수 호출한다
	 */
	class RunnableGetQRNumFromServerByPhoneNumber implements Runnable {
		public void run(){
			new backgroundGetQRNumFromServerByPhoneNumber().execute();
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
			
			// 파리미터 세팅
			 CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			 checkMileageMembersParam.setPhoneNumber(phoneNumber);
			// 호출
			// showPb();
			callResult = checkMileageCustomerRest.RestGetQRNumFromServerByPhoneNumber(checkMileageMembersParam);
			// hidePb();
			// 결과 처리
			 if(callResult.equals("S")){ // 인증 성공
			     Log.i(TAG, "S");
			     tempstr = checkMileageCustomerRest.getTempstr();
			     // ... 이후 처리. 전번 꺼내서 사용하는 부분 필요. @@@ 
			     Log.d(TAG,"tempstr:"+tempstr);
		     }else{ // 인증 실패
			     Log.i(TAG, "F");
		     }
			
			return null; 
		}
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