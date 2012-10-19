package kr.co.bettersoft.checkmileage.activities;
// ������ �� - �̿� ����
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kr.co.bettersoft.checkmileage.adapters.MileageLogAdapter;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMemberMileageLogs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberStoreLogPageActivity extends Activity {
	String idCheckMileageMileages ="";
	public static String storeName = "";
	
	int responseCode = 0;
	String TAG = "MemberStoreLogPageActivity";
	
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	public List<CheckMileageMemberMileageLogs> entries;	// 1�������� ��ȸ�� ���.(����Ʈ)
	
	private ListView m_list = null;											// ����Ʈ ��
	List<CheckMileageMemberMileageLogs> entriesFn = null;					// ����Ʈ. ���������� �� �༮��. ���ϸ��� �α� ����Ʈ.

	private MileageLogAdapter logAdapter;			// ���� ���� �ƴ���.
	TextView emptyText = null;				// ������ ���� �ؽ�Ʈ.
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ� ����� ȭ�鿡 �ѷ��ش�.
				if(b.getInt("showYN")==1){		// ȭ�鿡 �����൵ ���ٴ� �޽���. �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. ���� ����Ʈ ���̾ƿ�.
					if(entriesFn.size()>0){
						emptyText.setText("");
							setListing();
					}else{
						Log.d(TAG,"no data");
						emptyText.setText(R.string.no_used_logs);
					}
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(MemberStoreLogPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				String tmpstr = getString(R.string.error_occured);
				Toast.makeText(MemberStoreLogPageActivity.this, tmpstr, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			
		}
	};
	// �ڵ鷯���� ���ؽ�Ʈ �ޱ� ���� ���.
	public Context returnThis(){	
		return this;
	}
	
	// �����͸� ȭ�鿡 ����
	public void setListing(){
		logAdapter = new MileageLogAdapter(this, entriesFn);
		m_list  = (ListView)findViewById(R.id.memberstore_log_list);
		m_list.setAdapter(logAdapter);
//		gridView.setOnScrollListener(listScrollListener);		// ������ ���. ��ũ�ѽ� �ϴܿ� �����ϸ� �߰� ������ ��ȸ�ϵ���.
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.member_store_log);
	    Intent rIntent = getIntent();
	    idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");			// �ֿ� ������ �޴´�. �ֿ������� Ű ��. idCheckMileageMileages
	    storeName = rIntent.getStringExtra("storeName");	
	    try {
			getMyMileageList();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_list = (ListView) findViewById(R.id.memberstore_log_list);
		emptyText = (TextView) findViewById(R.id.memberstore_log_list_empty);
	}

	/*
	 * ������ ����Ͽ� ������ �̿� ���� �α׸� �����´�.
	 * �� ����� List<CheckMileageMemberMileageLogs> Object �� ��ȯ �Ѵ�.
	 * 
	 * ������ ���� : ��Ƽ����ƮY, Ű�� : checkMileageMileagesIdCheckMileageMileages  <- ������������ ����.
	 *  
	 *  �޴� ���� : �̿��Ѽ��� content., ���� �Ǵ� ����� ���ϸ��� mileage,   ������  modifyDate
	 *  
	 * -----------------------------------
	 * |  [������ �̸�]					   |
	 * |  �̿뼭��						   |
	 * |  ���ϸ��� 	[ �� �� �� �� �� �� �� ]  	   |
	 * ------------------------------------
	 */
	public void getMyMileageList() throws JSONException, IOException {
		Log.i(TAG, "getMyMileageList:::"+idCheckMileageMileages);		// �ε��� ��ȣ..
		controllerName = "checkMileageMemberMileageLogController";
		methodName = "selectMemberMileageLogList";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// �ڽ��� ���̵� �־ ��ȸ
							obj.put("activateYn", "Y");
							obj.put("checkMileageMileagesIdCheckMileageMileages", idCheckMileageMileages);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMemberMileageLog\":" + obj.toString() + "}";
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// ��ȸ�� ����� ó��.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
							try{
								Thread.sleep(500);		// �����ٰ� �ٽ�
								getMyMileageList();
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}  
					}
				}
		).start();
	}

	/*
	 * ��ȸ�� �α� ����Ʈ�� ����.
	 */
	public void theData1(InputStream in){
//		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"����::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
		
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		if(responseCode==200 || responseCode==204){
			try {
				entries = new ArrayList<CheckMileageMemberMileageLogs>(max);
				if(max>0){
					/*
					 * ����::[
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":1,"checkMileageId":"test1234","merchantId":"m1","content":"���",
					 * 				  "mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		},
					 * 		...
					 * 		]
					 */
					for ( int i = 0; i < max; i++ ){
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMemberMileageLog");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
//						Log.d(TAG,"���� checkMileageMileagesIdCheckMileageMileages::"+jsonObj.getString("checkMileageMileagesIdCheckMileageMileages"));
//						Log.d(TAG,"���� content::"+jsonObj.getString("content"));
//						Log.d(TAG,"���� mileage::"+jsonObj.getString("mileage"));
//						Log.d(TAG,"���� modifyDate::"+jsonObj.getString("modifyDate"));
						
						entries.add(new CheckMileageMemberMileageLogs(jsonObj.getString("checkMileageMileagesIdCheckMileageMileages"),			
								jsonObj.getString("content"),
								jsonObj.getString("mileage"),
								jsonObj.getString("modifyDate")));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
//				getMerchantInfo(entries,max);
				
				entriesFn = entries;		// ó�� ����� ������ ����.
//				Log.d(TAG,"���� entriesFn::"+entriesFn.size());
				showInfo();					// ������ �� ����� ������ ȭ�鿡 �ѷ��ִ� �۾��� �Ѵ�.
			}
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����.
			showMSG();
//			Toast.makeText(MemberStoreLogPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.
	public void showInfo(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("showYN", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}
	
	public void showMSG(){			// ȭ�鿡 error �佺Ʈ ���..
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("showErrToast", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	} 
}
