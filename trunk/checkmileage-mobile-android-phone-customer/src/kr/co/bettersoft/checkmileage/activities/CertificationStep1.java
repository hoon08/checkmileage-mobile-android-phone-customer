package kr.co.bettersoft.checkmileage.activities;

/*
 * 인증 1단계. - 폰번 인증 화면.
 * 어플 실행하여 QR 저장소에 QR이 없을 경우에 실행 된다.
 * 
 * 서버와 통신하여 폰번이 있는지 여부를 체크한다.
 * 폰번이 있고 QR코드가 있으면 서버에서 받아온 QR 코드를 그대로 사용한다.
 * 
 * 서버에  정보가 없다면 신규 유저이기 때문에 2차 인증 화면으로 이동한다. 
 *  (동시에 서버에서 인증번호를 SMS로 발송하여 인증받도록 한다)
 *  
 *  
 *  <화면 구성>
 *   모바일에서 폰번호를 가져와서 기본값으로 채움.(수정가능)
 *   중단 버튼을 눌러 [인증번호 요청] 을 하여 서버와 통신을 한다. 
 *   서버에서 가져온 결과를 분석하여 2차 인증으로 갈지, QR 생성화면으로 갈지 여부를 판단한다.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.bettersoft.checkmileage.activities.CertificationStep2;
import kr.co.bettersoft.checkmileage.activities.CommonUtils;
import kr.co.bettersoft.checkmileage.activities.Main_TabsActivity;
import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CertificationStep1 extends Activity {
	Button button1 ;
	TextView txt1;
	String TAG = "CertificationStep1";
	String phoneNum = "";
	String qrcode ="";
	int responseCode = 0;
	String serverName = CommonUtils.serverNames;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showErrToast")==1){
					Toast.makeText(CertificationStep1.this,b.getString("msg"), Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.certification_step1);
		
		button1 = (Button) findViewById(R.id.certi_btn1);
		txt1 = (TextView)findViewById(R.id.certi_text1);
		//자신의 전화번호 가져오기
		TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
		// TODO Auto-generated method stub
		phoneNum = telManager.getLine1Number();
		txt1.setText(phoneNum);
		
//		button1.setText("인증번호 요청하기);				// 하드코딩
		button1.setText(R.string.certi_step1_btn1);			// 다국어 지원.

			// 안내문.: 임시 인증 번호가 SMS로 발송 됩니다. 새로운 인증 번호를 요청하실 경우 이전 인증 번호는 사용할 수 없습니다.
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					CertificationStep2.phoneNum = phoneNum;
					certificationStep1();
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
	 * 서버와 통신하여 인증 1단계 수행.
	 */
	public void certificationStep1() throws JSONException, IOException {
    	Log.i("certificationStep1", "certificationStep1");
    	new Thread(
    			new Runnable(){
    				public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("phoneNumber", phoneNum);
							obj.put("activateYn", "Y");
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
							  URL postUrl2 = new URL("http:/"+serverName+"/checkMileageMemberController/selectMemberInformationByPhoneNumber");
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
	 * 인증 1단계의 결과를 받음.
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
    	Log.d(TAG,"수신::"+builder.toString());
    	String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(tempstr);
			String jstring2 = jsonObject.getString("checkMileageMember").toString(); 
	    	JSONObject jsonObject2 = new JSONObject(jstring2);
	    	qrcode = jsonObject2.getString("checkMileageId").toString(); 
	    	Log.d(TAG,"qrcode:"+qrcode);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	if(responseCode==200 || responseCode==204){		// 인증 성공시
    		// QR데이터를 꺼내어 저장..
    		// 기타 정보들을 설정 파일에 저장....
    		if(qrcode.length()>0){		// 데이터 있는 경우 - 해당 QR 데이터 가지고 내 QR 보기 페이지로 이동.
    			Log.i(TAG, "Welcome Your ComeBack. Go to Main Page");
    			MyQRPageActivity.qrCode = qrcode;
    			new Thread(
    		    		new Runnable(){
    		    			public void run(){
    		    				try{
    		    					Thread.sleep(1000);
    		    					// 받은 정보로 나의 QR 코드 보기로 이동..  전번으로 인증했으니 별도 업뎃은 없음
    		    					Intent intent2 = new Intent(CertificationStep1.this, Main_TabsActivity.class);
    		    					startActivity(intent2);
    		    					finish();		// 다른 액티비티를 호출하고 자신은 종료.
    		    				}catch(InterruptedException ie){
    		    					ie.printStackTrace();
    		    				}
    		    			}
    		    		}
    		    ).start();
    		}else{			// 데이터 없는 경우 - 신규 유저 이므로 등록 필요. 2차 인증 페이지로 이동
    			Log.i(TAG, "Welcome New User, Go to Certification2 for Register");
    			new Thread(
    		    		new Runnable(){
    		    			public void run(){
    		    				try{
    		    					Thread.sleep(1000);
    		    					Intent intent2 = new Intent(CertificationStep1.this, CertificationStep2.class);
    		    					startActivity(intent2);
    		    					finish();		// 다른 액티비티를 호출하고 자신은 종료.
    		    				}catch(InterruptedException ie){
    		    					ie.printStackTrace();
    		    				}
    		    			}
    		    		}
    		    ).start();
    		}
    	}else{			// 인증 실패시	 토스트 띄우고 화면 유지.
    		alertMsg();
//    		Toast.makeText(CertificationStep1.this, R.string.certi_fail_msg, Toast.LENGTH_SHORT).show();
    	}
    }
}
