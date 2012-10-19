package kr.co.bettersoft.checkmileage.activities;

/*
 * ���� 1�ܰ�. - ���� ���� ȭ��.
 * ���� �����Ͽ� QR ����ҿ� QR�� ���� ��쿡 ���� �ȴ�.
 * 
 * ������ ����Ͽ� ������ �ִ��� ���θ� üũ�Ѵ�.
 * ������ �ְ� QR�ڵ尡 ������ �������� �޾ƿ� QR �ڵ带 �״�� ����Ѵ�.
 * 
 * ������  ������ ���ٸ� �ű� �����̱� ������ 2�� ���� ȭ������ �̵��Ѵ�. 
 *  (���ÿ� �������� ������ȣ�� SMS�� �߼��Ͽ� �����޵��� �Ѵ�)
 *  
 *  
 *  <ȭ�� ����>
 *   ����Ͽ��� ����ȣ�� �����ͼ� �⺻������ ä��.(��������)
 *   �ߴ� ��ư�� ���� [������ȣ ��û] �� �Ͽ� ������ ����� �Ѵ�. 
 *   �������� ������ ����� �м��Ͽ� 2�� �������� ����, QR ����ȭ������ ���� ���θ� �Ǵ��Ѵ�.
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
	
	// �ڵ鷯
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
		//�ڽ��� ��ȭ��ȣ ��������
		TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
		// TODO Auto-generated method stub
		phoneNum = telManager.getLine1Number();
		txt1.setText(phoneNum);
		
//		button1.setText("������ȣ ��û�ϱ�);				// �ϵ��ڵ�
		button1.setText(R.string.certi_step1_btn1);			// �ٱ��� ����.

			// �ȳ���.: �ӽ� ���� ��ȣ�� SMS�� �߼� �˴ϴ�. ���ο� ���� ��ȣ�� ��û�Ͻ� ��� ���� ���� ��ȣ�� ����� �� �����ϴ�.
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
	 * ������ ����Ͽ� ���� 1�ܰ� ����.
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
					  		  System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
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
	 * ���� 1�ܰ��� ����� ����.
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
    	Log.d(TAG,"����::"+builder.toString());
    	String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�... �뵵�� �°� ������ ��.
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
    	if(responseCode==200 || responseCode==204){		// ���� ������
    		// QR�����͸� ������ ����..
    		// ��Ÿ �������� ���� ���Ͽ� ����....
    		if(qrcode.length()>0){		// ������ �ִ� ��� - �ش� QR ������ ������ �� QR ���� �������� �̵�.
    			Log.i(TAG, "Welcome Your ComeBack. Go to Main Page");
    			MyQRPageActivity.qrCode = qrcode;
    			new Thread(
    		    		new Runnable(){
    		    			public void run(){
    		    				try{
    		    					Thread.sleep(1000);
    		    					// ���� ������ ���� QR �ڵ� ����� �̵�..  �������� ���������� ���� ������ ����
    		    					Intent intent2 = new Intent(CertificationStep1.this, Main_TabsActivity.class);
    		    					startActivity(intent2);
    		    					finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
    		    				}catch(InterruptedException ie){
    		    					ie.printStackTrace();
    		    				}
    		    			}
    		    		}
    		    ).start();
    		}else{			// ������ ���� ��� - �ű� ���� �̹Ƿ� ��� �ʿ�. 2�� ���� �������� �̵�
    			Log.i(TAG, "Welcome New User, Go to Certification2 for Register");
    			new Thread(
    		    		new Runnable(){
    		    			public void run(){
    		    				try{
    		    					Thread.sleep(1000);
    		    					Intent intent2 = new Intent(CertificationStep1.this, CertificationStep2.class);
    		    					startActivity(intent2);
    		    					finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
    		    				}catch(InterruptedException ie){
    		    					ie.printStackTrace();
    		    				}
    		    			}
    		    		}
    		    ).start();
    		}
    	}else{			// ���� ���н�	 �佺Ʈ ���� ȭ�� ����.
    		alertMsg();
//    		Toast.makeText(CertificationStep1.this, R.string.certi_fail_msg, Toast.LENGTH_SHORT).show();
    	}
    }
}
