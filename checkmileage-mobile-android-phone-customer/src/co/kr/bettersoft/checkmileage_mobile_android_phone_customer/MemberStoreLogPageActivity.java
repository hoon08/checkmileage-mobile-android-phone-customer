package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kr.bettersoft.domain.CheckMileageMemberMileageLogs;
import com.kr.bettersoft.domain.CheckMileageMileage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberStoreLogPageActivity extends Activity {
	String idCheckMileageMileages ="";
	String storeName = "";
	
	int responseCode = 0;
	String TAG = "MemberStoreLogPageActivity";
	
	String controllerName = "";
	String methodName = "";
	
	public List<CheckMileageMemberMileageLogs> entries;	// 1�������� ��ȸ�� ���.(����Ʈ)
//	int returnYN = 0;		// �������� ���� �����뵵
//	int flag = 0;
//	private ArrayAdapter<String> m_adapter = null;
	
	private ListView m_list = null;											// ����Ʈ ��
//	ArrayAdapter<CheckMileageMemberMileageLogs> adapter = null;				// ��� �����. �� �༮���� ���ϸ��� �α� ������ ����Ʈ.
	List<CheckMileageMemberMileageLogs> entriesFn = null;					// ����Ʈ. ���������� �� �༮��. ���ϸ��� �α� ����Ʈ.
//    float fImgSize = 0;
	MyAdapter mAdapter;								// �ƴ��� �ϳ� ����. �ϳ��� ������ ���Կ뵵.?

	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ� ����� ȭ�鿡 �ѷ��ش�.
				if(b.getInt("showYN")==1){			// ȭ�鿡 �����൵ ���ٴ� �޽���.
					// (���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. )
					
					
					mAdapter = new MyAdapter(returnThis(), R.layout.member_store_log_list, (ArrayList<CheckMileageMemberMileageLogs>) entriesFn);		// �ƴ��͸� ���� ���ϸ��� �α� ������ ����Ʈ(������)�� ȭ�鿡����
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);					// ����Ʈ�信 �ƴ��� ����.
				}
			}catch(Exception e){
				Toast.makeText(MemberStoreLogPageActivity.this, "������ �߻��Ͽ����ϴ�."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	// �ڵ鷯���� ���ؽ�Ʈ �ޱ� ���� ���.
	public Context returnThis(){	
		return this;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_list = (ListView) findViewById(R.id.memberstore_log_list);
	}


	// ����� Ŭ����. �̰����� ���� �����͸� �� ���̵� ���� �����Ѵ�.
	class MyAdapter extends BaseAdapter{
		Context context;
		int layoutId;
		ArrayList<CheckMileageMemberMileageLogs> myDataArr;			// ���ϸ��� �α� ������ Ŭ������ ���� ����Ʈ �����.
		LayoutInflater Inflater;
		MyAdapter(Context _context, int _layoutId, ArrayList<CheckMileageMemberMileageLogs> _myDataArr){
			context = _context;
			layoutId = _layoutId;
			myDataArr = _myDataArr;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {				// ���� ����
			return myDataArr.size();
		}
		@Override
		public String getItem(int position) {
			return myDataArr.get(position).getCheckMileageMileagesIdCheckMileageMileages();			// Ű�� ����..
		}
		@Override
		public long getItemId(int position) {			// �׳� ������ ����.
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			// �ϳ��� �信 �ֱ�..
			final int pos = position;
			if (convertView == null)  {
				convertView = Inflater.inflate(layoutId, parent, false);
			}
			
			// ����Ʈ ����� ���� �並 �о�ͼ� ������ �ϳ��� �����Ѵ�. ���� �ƴ��͸� ���� �ϳ��� ȭ�鿡 �߰����ش�.

			TextView merchant_log_info = (TextView)convertView.findViewById(R.id.merchant_log_info2);					// ���Ͼ�(ȫ����)   <--   / +"���� ����"
			TextView merchant_log_content = (TextView)convertView.findViewById(R.id.merchant_log_content);				//  ���.
			
			TextView merchant_log_mileage = (TextView)convertView.findViewById(R.id.merchant_log_mileage);				// +"x"  +   3  <--
			TextView merchant_log_time = (TextView)convertView.findViewById(R.id.merchant_log_time2);					// 2012-08-12 13:52
		
			Log.i(TAG, "myDataArr22:::"+myDataArr.size()+"??"+position);
			merchant_log_content.setText(myDataArr.get(position).getContent());
			merchant_log_info.setText(storeName);							// ������ �̸� / ����? ��ġ? �� ������������ �޴°ɷ�.. (��ȸ ������ ����)
			merchant_log_mileage.setText("("+myDataArr.get(position).getMileage()+"��)");
			merchant_log_time.setText(myDataArr.get(position).getModifyDate());
			Log.i(TAG, "merchant_log_time:::"+myDataArr.get(position).getModifyDate());
			return convertView;
		}
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
	 * ��ȸ�� �α� ����Ʈ�� ����.
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
					 * 
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":2,"checkMileageId":"test1234","merchantId":"m1","content":"������",
					 * 				"mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		},
					 * 
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":3,"checkMileageId":"test1234","merchantId":"m1","content":"���",
					 * 				"mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		}
					 * 		]
					 */
					
					for ( int i = 0; i < max; i++ ){
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMemberMileageLog");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
						
						Log.d(TAG,"���� checkMileageMileagesIdCheckMileageMileages::"+jsonObj.getString("checkMileageMileagesIdCheckMileageMileages"));
						Log.d(TAG,"���� content::"+jsonObj.getString("content"));
						Log.d(TAG,"���� mileage::"+jsonObj.getString("mileage"));
						Log.d(TAG,"���� modifyDate::"+jsonObj.getString("modifyDate"));
						
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
				Log.d(TAG,"���� entriesFn::"+entriesFn.size());
				showInfo();					// ������ �� ����� ������ ȭ�鿡 �ѷ��ִ� �۾��� �Ѵ�.
			}
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����.
			Toast.makeText(MemberStoreLogPageActivity.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
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
	
	
	
}
