package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ������ ���� ����

/*
 * ������ ���̵�, �� ���ϸ���  <-- ���� ȭ�鿡�� �޾ƿ´�..  (���⼭ ���ϴ� ���� ȭ���� �� ���ϸ��� ��� �Ǵ� ������ ���)
 * ������ �̹���URL, ������ �̸�, ��ǥ�� �̸�, ��ȭ��ȣ, �ּ�, ��ǥ1,2(��ǥ �̸� ������ ���� ���� �ȹ޾Ƶ� �ȴ�..), �� ����
 * ������ ������� ��Ϻ���, ��ȭ�ɱ�, �޴�/���� ���� ��..
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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.MyMileagePageActivity.MyAdapter;

import com.kr.bettersoft.domain.CheckMileageMerchants;
import com.kr.bettersoft.domain.CheckMileageMileage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class MemberStoreInfoPage extends Activity {
	String TAG = "MemberStoreInfoPage";
	int responseCode = 0;
	String controllerName ="";
	String methodName ="";
	public CheckMileageMerchants merchantData ;	// ��� �����ؼ� �����ֱ� ���� ������.
	
	String merchantId ="";
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
				if(b.getInt("showYN")==1){
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. 
//					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
//					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//					m_list.setAdapter(mAdapter);
					
				}
			}catch(Exception e){
//				Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.member_store_info);
	    
	    Intent rIntent = getIntent();
	    merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");
	    
	}


	public Context returnThis(){
		return this;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * ������ ����Ͽ� ������ ������ �����´�.
	 
	 * �� ����� <CheckMileageMileage> Object �� ��ȯ �Ѵ�.?? 
	 * 
	 * ������ ���� : ������ ���̵�
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  �޴� ���� : 
	 *    ������ �̸�, ������ �̹���URL, �������� ���� �� ���ϸ���.
	 *     ��ǥ�� �̸� , ��ȭ��ȣ 1, �ּ� 1, 
	 *      ��Ÿ �����, ��ǥ(1,2),  
	 *    @[���������̵�] �� ���� ó�� �������Ƿ� ���� ����
	 *  
	 * -----------------------------------
	 * |[      ��    ��    ��      [���ϸ���] ] |
	 * |[      ��    ��    ��                       ] |
	 * |��ǥ�� :                       |
	 * |���� :				[��ȭ�ɱ�]
	 * |�ּ� : 				[��������]
	 * 
	 * ��Ÿ ����.....
	 * 
	 *      [�޴�/����]  [�ݱ�]
	 * ------------------------------------
	 * 
	 * �ݱ� ��ư�� ��ܿ� �Ѽ���...������ ȭ�� ����.
	 *  ���ϸ��� ������ ���ϸ��� �̷� ����
	 *  ��ȭ�ɱ� ������ ��ȭ �ɱ�
	 *  �������� ������ �������� ������, �� ��ġ Ȯ��.
	 *    
	 *  ������ �Ķ����: merchantId  activateYn
	 *  �޴� �Ķ���� : CheckMileageMerchant
	 *    
	 */
	public void getMerchantInfo() throws JSONException, IOException {
		Log.i(TAG, "getMerchantInfo");
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
		
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// ������ ���̵� �־ ��ȸ
							obj.put("activateYn", "Y");
							obj.put("merchantId", merchantId);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"CheckMileageMerchant\":" + obj.toString() + "}";
						try{
							URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
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
						}  
					}
				}
		).start();
	}
	
	/*
	 * ������ �� ������ ����
	 */
	public void theData1(InputStream in){
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
//		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
//		if(responseCode==200 || responseCode==204){
//			try {
//				JSONArray jsonArray2 = new JSONArray(tempstr);
//				int max = jsonArray2.length();
//				entries = new ArrayList<CheckMileageMileage>(max);
//				for ( int i = 0; i < max; i++ ){
//					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
//					//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
//					// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
//					entries.add(new CheckMileageMileage(jsonObj.getString("idCheckMileageMileages"),
//							jsonObj.getString("mileage"),jsonObj.getString("modifyDate"),
//							jsonObj.getString("checkMileageMembersCheckMileageId"),jsonObj.getString("checkMileageMerchantsMerchantId")));
//				}
//				//    			 2�� �۾�. ������ �̸�, �̹��� �����ͼ� �߰��� ����.
//				//    			 array ä�� �ѱ�� ���������� �ֵ��� �Ѵ�..
//				if(max>0){
//					getMerchantInfo(entries,max);
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����.
//			Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
//		}
	}
	
	
	
	
}
