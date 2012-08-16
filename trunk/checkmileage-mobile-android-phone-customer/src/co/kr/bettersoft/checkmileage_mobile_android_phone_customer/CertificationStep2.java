package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
/*
 * ����2�ܰ�.
 * �ű� ������ ��� �������� ������ȣ�� SMS �� �߼��� �ش�.
 * ����Ͽ����� ������ȣ ��û ȭ�鿡�� ������ȣ Ȯ�� ȭ��(���� ȭ��) ���� ����ȴ�.
 * 
 * SMS�� ����� ������ȣ�� ȭ�� ����� ������ȣ �Է� ���� �ְ� 
 *   ȭ�� �ߴ��� [�����ϱ�] ��ư�� ���� ������ ����Ͽ� ������ �޴´�.
 *   
 * �����κ��� ���� ����� �޾Ƽ� �׿� ���� ȭ���� �б� �ȴ�.
 *  ���� ���� �� QR ���� - ���� ȭ������ �̵��Ѵ�.
 *  ���� ���� �� ����1 ȭ������ ���ư���. (ó�� ���� 1 ȭ�� ������ó�� ����ȣ �а�, ��ư ���� ������ ��û..���� ����.)  
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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.certification_step2);
	    
	    button2 = (Button) findViewById(R.id.certi_btn2);
	    txt2 = (TextView)findViewById(R.id.certi_text2);
	    
	    
		button2.setText("���� �ϱ�");
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
	 * ������ ����Ͽ� ����2�ܰ� ����.
	 */
	public void certificationStep2() throws JSONException, IOException {
    	Log.i(TAG, "certificationStep1");
    	new Thread(
    			new Runnable(){
    				public void run(){
    					 // ���� ������
						JSONObject obj = new JSONObject();
						
    					// �����Ͻ�
    					Date today = new Date();
//    				    registerDate = today.toString();
    				    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    				    registerDate = sf.format(today);
    				    // ���� ��ȣ
						certificationNumber = (String) button2.getText();
						try{
//							obj.put("phoneNumber", phoneNum);
//							obj.put("certificationNumber", certificationNumber);
//							obj.put("registerDate", registerDate);

							// �׽�Ʈ�� �ϵ��ڵ�
							obj.put("phoneNumber", "01022173645");
							obj.put("certificationNumber", "1234");
							obj.put("registerDate", registerDate);
							
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageCertification\":" + obj.toString() + "}";
						try{
							  URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/checkMileageCertificationController/selectCertificationNumber");
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
	 * ���� 2�ܰ��� ����� ����.
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
//    	try{
//    		String result = "";
//    		JSONArray ja = new JSONArray(tempstr);
//    		for(int i=0; i<ja.length(); i++){
//    			JSONObject order =ja.getJSONObject(i);
//    			result+=""
//    		}
//    	}
    	if(responseCode==200||responseCode==204){		// ���� ������ - QR ���� �������� �̵�.
    		Log.i(TAG, "Thanks Your Registering. Go to Get Your QR Code");
    		new Thread(
    				new Runnable(){
    					public void run(){
    						try{
    							Thread.sleep(1000);
    							// ���� QR �ڵ� ����� �̵�.
    							Intent intent2 = new Intent(CertificationStep2.this, No_QR_PageActivity.class);
    							startActivity(intent2);
    							finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
    						}catch(InterruptedException ie){
    							ie.printStackTrace();
    						}
    					}
    				}
    		).start();
    	}else{			// ���� ���н�	 �佺Ʈ ���� ���� 1�ܰ�� ���ư�.
    		Toast.makeText(CertificationStep2.this, "������ �߻��Ͽ� ������ �����Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
    		new Thread(
    				new Runnable(){
    					public void run(){
    						try{
    							Thread.sleep(1000);
    							// ���� QR �ڵ� ����� �̵�.
    							Intent intent2 = new Intent(CertificationStep2.this, CertificationStep1.class);
    							startActivity(intent2);
    							finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
    						}catch(InterruptedException ie){
    							ie.printStackTrace();
    						}
    					}
    				}
    		).start();
    	}
	}
}
