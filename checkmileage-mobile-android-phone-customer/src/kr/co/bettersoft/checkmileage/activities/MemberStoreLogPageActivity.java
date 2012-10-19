package kr.co.bettersoft.checkmileage.activities;
// 가맹점 상세 - 이용 내역
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
	
	public List<CheckMileageMemberMileageLogs> entries;	// 1차적으로 조회한 결과.(리스트)
	
	private ListView m_list = null;											// 리스트 뷰
	List<CheckMileageMemberMileageLogs> entriesFn = null;					// 리스트. 최종적으로 들어갈 녀석들. 마일리지 로그 리스트.

	private MileageLogAdapter logAdapter;			// 성능 좋은 아답터.
	TextView emptyText = null;				// 데이터 없음 텍스트.
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온 결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){		// 화면에 보여줘도 좋다는 메시지. 받아온 마일리지 결과를 화면에 뿌려준다.
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 여기 리스트 레이아웃.
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
	// 핸들러에서 컨텍스트 받기 위해 사용.
	public Context returnThis(){	
		return this;
	}
	
	// 데이터를 화면에 세팅
	public void setListing(){
		logAdapter = new MileageLogAdapter(this, entriesFn);
		m_list  = (ListView)findViewById(R.id.memberstore_log_list);
		m_list.setAdapter(logAdapter);
//		gridView.setOnScrollListener(listScrollListener);		// 리스너 등록. 스크롤시 하단에 도착하면 추가 데이터 조회하도록.
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.member_store_log);
	    Intent rIntent = getIntent();
	    idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");			// 주요 정보를 받는다. 주요정보는 키 값. idCheckMileageMileages
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
	 * 서버와 통신하여 가맹점 이용 내역 로그를 가져온다.
	 * 그 결과를 List<CheckMileageMemberMileageLogs> Object 로 반환 한다.
	 * 
	 * 보내는 정보 : 액티베이트Y, 키값 : checkMileageMileagesIdCheckMileageMileages  <- 앞페이지에서 받음.
	 *  
	 *  받는 정보 : 이용한서비스 content., 적립 또는 사용한 마일리지 mileage,   수정일  modifyDate
	 *  
	 * -----------------------------------
	 * |  [가맹점 이름]					   |
	 * |  이용서비스						   |
	 * |  마일리지 	[ 가 맹 점 이 용 시 각 ]  	   |
	 * ------------------------------------
	 */
	public void getMyMileageList() throws JSONException, IOException {
		Log.i(TAG, "getMyMileageList:::"+idCheckMileageMileages);		// 인덱스 번호..
		controllerName = "checkMileageMemberMileageLogController";
		methodName = "selectMemberMileageLogList";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 자신의 아이디를 넣어서 조회
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
							try{
								Thread.sleep(500);		// 쉬었다가 다시
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
	 * 조회한 로그 리스트를 받음.
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
//		Log.d(TAG,"수신::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		
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
					 * 수신::[
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":1,"checkMileageId":"test1234","merchantId":"m1","content":"김밥",
					 * 				  "mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		},
					 * 		...
					 * 		]
					 */
					for ( int i = 0; i < max; i++ ){
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMemberMileageLog");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
//						Log.d(TAG,"수신 checkMileageMileagesIdCheckMileageMileages::"+jsonObj.getString("checkMileageMileagesIdCheckMileageMileages"));
//						Log.d(TAG,"수신 content::"+jsonObj.getString("content"));
//						Log.d(TAG,"수신 mileage::"+jsonObj.getString("mileage"));
//						Log.d(TAG,"수신 modifyDate::"+jsonObj.getString("modifyDate"));
						
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
				
				entriesFn = entries;		// 처리 결과를 밖으로 뺀다.
//				Log.d(TAG,"수신 entriesFn::"+entriesFn.size());
				showInfo();					// 밖으로 뺀 결과를 가지고 화면에 뿌려주는 작업을 한다.
			}
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			showMSG();
//			Toast.makeText(MemberStoreLogPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.
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
	
	public void showMSG(){			// 화면에 error 토스트 띄움..
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
