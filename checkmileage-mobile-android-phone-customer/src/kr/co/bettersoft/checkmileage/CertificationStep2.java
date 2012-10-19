package kr.co.bettersoft.checkmileage;
/*
 * 인증2단계.
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
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.bettersoft.checkmileage.CertificationStep1;

import org.json.JSONException;
import org.json.JSONObject;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CertificationStep2 extends Activity {
	Button button2 ;
	TextView txt2;
	static String phoneNum = "";
	String certificationNumber = "";
	String TAG = "CertificationStep2";
	String registerDate = "";
	int responseCode = 0;
	
	String controllerName = "";		// 서버 조회시 컨트롤러 이름
	String methodName = "";			// 서버 조회시 메서드 이름
	String serverName = CommonUtils.serverNames;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){
					Toast.makeText(CertificationStep2.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
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
	    setContentView(R.layout.certification_step2);
	    
	    button2 = (Button) findViewById(R.id.certi_btn2);
	    txt2 = (TextView)findViewById(R.id.certi_text2);
	    
//		button2.setText("인증확인");
		button2.setText(R.string.certi_step2_btn1);
		
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					certificationStep2();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/*
	 * 서버와 통신하여 인증2단계 수행.
	 */
	public void certificationStep2() throws JSONException, IOException {
    	Log.i(TAG, "certificationStep2");
    	
    	controllerName = "checkMileageCertificationController";		// 서버 조회시 컨트롤러 이름
    	methodName = "selectCertificationNumber";			// 서버 조회시 메서드 이름
    	
    	new Thread(
    			new Runnable(){
    				public void run(){
    					 // 전달 데이터
						JSONObject obj = new JSONObject();
    					// 가입일시
    					Date today = new Date();
//    				    registerDate = today.toString();
//    				    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    				    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    				    registerDate = sf.format(today);
    				    // 승인 번호
						certificationNumber = (String) txt2.getText();
						try{
//							obj.put("phoneNumber", phoneNum);					// 실사용.
//							obj.put("certificationNumber", certificationNumber);
//							obj.put("registerDate", registerDate);
							// 테스트용 하드코딩
							obj.put("phoneNumber", "01022173645");
							obj.put("certificationNumber", "1122");
							obj.put("registerDate", registerDate);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageCertification\":" + obj.toString() + "}";
						try{
							  URL postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
					  		  HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
					  		  connection2.setDoOutput(true);
					  		  connection2.setInstanceFollowRedirects(false);
					  		  connection2.setRequestMethod("POST");
					  		  connection2.setRequestProperty("Content-Type", "application/json");
					  		  OutputStream os2 = connection2.getOutputStream();
					  		  os2.write(jsonString.getBytes());
					  		  os2.flush();
					  		  System.out.println("postUrl      : " + postUrl2);
					  		  System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
					  		  responseCode = connection2.getResponseCode();
					  		  InputStream in =  connection2.getInputStream();
					  		  theData(in);
						}catch(Exception e){ 
						 e.printStackTrace();
						}  
    				}
    			}
    	).start();
	}
	/*
	 * 인증 2단계의 결과를 받음.
	 */
	public void theData(InputStream in){
		Log.d(TAG,"theData");
    	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    	StringBuilder builder = new StringBuilder();
    	String line =null;
    	try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Log.d(TAG,"get ::"+builder.toString());
    	String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
//    	try{
//    		String result = "";
//    		JSONArray ja = new JSONArray(tempstr);
//    		for(int i=0; i<ja.length(); i++){
//    			JSONObject order =ja.getJSONObject(i);
//    			result+=""
//    		}
//    	}
    	if(responseCode==200||responseCode==204){		// 인증 성공시 - QR 생성 페이지로 이동.
    		Log.i(TAG, "Thanks Your Registering. Go to Get Your QR Code");
    		new Thread(
    				new Runnable(){
    					public void run(){
    						try{
    							Thread.sleep(1000);
    							// 나의 QR 코드 보기로 이동.
    							Intent intent2 = new Intent(CertificationStep2.this, No_QR_PageActivity.class);
    							intent2.putExtra("phoneNumber", phoneNum);
    							startActivity(intent2);
    							finish();		// 다른 액티비티를 호출하고 자신은 종료.
    						}catch(InterruptedException ie){
    							ie.printStackTrace();
    						}
    					}
    				}
    		).start();
    	}else{			// 인증 실패시	 토스트 띄우고 인증 1단계로 돌아감.
    		alertMsg();
//    		Toast.makeText(CertificationStep2.this, R.string.certi_fail_msg, Toast.LENGTH_SHORT).show();
    		new Thread(
    				new Runnable(){
    					public void run(){
    						try{
    							Thread.sleep(1000);
    							// 나의 QR 코드 보기로 이동.
    							Intent intent2 = new Intent(CertificationStep2.this, CertificationStep1.class);
    							startActivity(intent2);
    							finish();		// 다른 액티비티를 호출하고 자신은 종료.
    						}catch(InterruptedException ie){
    							ie.printStackTrace();
    						}
    					}
    				}
    		).start();
    	}
	}
}
