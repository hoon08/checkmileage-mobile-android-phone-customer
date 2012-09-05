package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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
	
	public List<CheckMileageMemberMileageLogs> entries;	// 1차적으로 조회한 결과.(리스트)
//	int returnYN = 0;		// 리턴할지 여부 결정용도
//	int flag = 0;
//	private ArrayAdapter<String> m_adapter = null;
	
	private ListView m_list = null;											// 리스트 뷰
//	ArrayAdapter<CheckMileageMemberMileageLogs> adapter = null;				// 어레이 어댑터. 들어갈 녀석들은 마일리지 로그 도메인 리스트.
	List<CheckMileageMemberMileageLogs> entriesFn = null;					// 리스트. 최종적으로 들어갈 녀석들. 마일리지 로그 리스트.
//    float fImgSize = 0;
	MyAdapter mAdapter;								// 아답터 하나 만듬. 하나씩 실제로 대입용도.?

	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온 결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){			// 화면에 보여줘도 좋다는 메시지.
					// (최종 결과 배열은 entriesFn 에 저장되어 있다.. )
					
					
					mAdapter = new MyAdapter(returnThis(), R.layout.member_store_log_list, (ArrayList<CheckMileageMemberMileageLogs>) entriesFn);		// 아답터를 통해 마일리지 로그 도메인 리스트(데이터)를 화면에세팅
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);					// 리스트뷰에 아답터 적용.
				}
			}catch(Exception e){
				Toast.makeText(MemberStoreLogPageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	// 핸들러에서 컨텍스트 받기 위해 사용.
	public Context returnThis(){	
		return this;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_list = (ListView) findViewById(R.id.memberstore_log_list);
	}


	// 어댑터 클래스. 이곳에서 얻어온 데이터를 뷰 아이디를 통해 세팅한다.
	class MyAdapter extends BaseAdapter{
		Context context;
		int layoutId;
		ArrayList<CheckMileageMemberMileageLogs> myDataArr;			// 마일리지 로그 도메인 클래스에 대한 리스트 만들기.
		LayoutInflater Inflater;
		MyAdapter(Context _context, int _layoutId, ArrayList<CheckMileageMemberMileageLogs> _myDataArr){
			context = _context;
			layoutId = _layoutId;
			myDataArr = _myDataArr;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {				// 개수 세기
			return myDataArr.size();
		}
		@Override
		public String getItem(int position) {
			return myDataArr.get(position).getCheckMileageMileagesIdCheckMileageMileages();			// 키값 꺼냄..
		}
		@Override
		public long getItemId(int position) {			// 그냥 포지션 리턴.
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			// 하나씩 뷰에 넣기..
			final int pos = position;
			if (convertView == null)  {
				convertView = Inflater.inflate(layoutId, parent, false);
			}
			
			// 리스트 만들기 위한 뷰를 읽어와서 데이터 하나씩 대입한다. 이후 아답터를 통해 하나씩 화면에 추가해준다.

			TextView merchant_log_info = (TextView)convertView.findViewById(R.id.merchant_log_info2);					// 나니아(홍대점)   <--   / +"에서 적립"
			TextView merchant_log_content = (TextView)convertView.findViewById(R.id.merchant_log_content);				//  김밥.
			
			TextView merchant_log_mileage = (TextView)convertView.findViewById(R.id.merchant_log_mileage);				// +"x"  +   3  <--
			TextView merchant_log_time = (TextView)convertView.findViewById(R.id.merchant_log_time2);					// 2012-08-12 13:52
		
			Log.i(TAG, "myDataArr22:::"+myDataArr.size()+"??"+position);
			merchant_log_content.setText(myDataArr.get(position).getContent());
			merchant_log_info.setText(storeName);							// 가맹점 이름 / 업종? 위치? 는 앞페이지에서 받는걸로.. (조회 정보에 없음)
			merchant_log_mileage.setText("("+myDataArr.get(position).getMileage()+"점)");
			merchant_log_time.setText(myDataArr.get(position).getModifyDate());
			Log.i(TAG, "merchant_log_time:::"+myDataArr.get(position).getModifyDate());
			return convertView;
		}
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
						}  
					}
				}
		).start();
	}

	/*
	 * 조회한 로그 리스트를 받음.
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
		Log.d(TAG,"수신::"+builder.toString());
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
					 * 
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":2,"checkMileageId":"test1234","merchantId":"m1","content":"떡볶이",
					 * 				"mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		},
					 * 
					 * 		{"checkMileageMemberMileageLog":
					 * 				{"idCheckMileageMemberMileageLogs":3,"checkMileageId":"test1234","merchantId":"m1","content":"라면",
					 * 				"mileage":1,"activateYn":"Y","modifyDate":"2012-08-17","registerDate":"2012-08-17","checkMileageMileagesIdCheckMileageMileages":1}
					 * 		}
					 * 		]
					 */
					
					for ( int i = 0; i < max; i++ ){
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMemberMileageLog");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
						
						Log.d(TAG,"수신 checkMileageMileagesIdCheckMileageMileages::"+jsonObj.getString("checkMileageMileagesIdCheckMileageMileages"));
						Log.d(TAG,"수신 content::"+jsonObj.getString("content"));
						Log.d(TAG,"수신 mileage::"+jsonObj.getString("mileage"));
						Log.d(TAG,"수신 modifyDate::"+jsonObj.getString("modifyDate"));
						
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
				Log.d(TAG,"수신 entriesFn::"+entriesFn.size());
				showInfo();					// 밖으로 뺀 결과를 가지고 화면에 뿌려주는 작업을 한다.
			}
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			Toast.makeText(MemberStoreLogPageActivity.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
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
	
	
	
}
