package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 가맹점 정보 보기

/*
 * 가맹점 아이디, 내 마일리지  <-- 이전 화면에서 받아온다..  (여기서 말하는 이전 화면은 내 마일리지 목록 또는 가맹점 목록)
 * 가맹점 이미지URL, 가맹점 이름, 대표자 이름, 전화번호, 주소, 좌표1,2(좌표 미리 받으면 지도 볼때 안받아도 된다..), 상세 설명
 * 이후의 기능으로 기록보기, 전화걸기, 메뉴/서비스 보기 등..
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
	public CheckMileageMerchants merchantData ;	// 결과 저장해서 보여주기 위한 도메인.
	
	String merchantId ="";
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온 마일리지 결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 
//					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
//					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//					m_list.setAdapter(mAdapter);
					
				}
			}catch(Exception e){
//				Toast.makeText(MyMileagePageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
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
	 * 서버와 통신하여 가맹점 정보를 가져온다.
	 
	 * 그 결과를 <CheckMileageMileage> Object 로 반환 한다.?? 
	 * 
	 * 보내는 정보 : 가맹점 아이디
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  받는 정보 : 
	 *    가맹점 이름, 가맹점 이미지URL, 가맹점에 대한 내 마일리지.
	 *     대표자 이름 , 전화번호 1, 주소 1, 
	 *      기타 설명들, 좌표(1,2),  
	 *    @[가맹점아이디] 는 가장 처음 가져오므로 따로 저장
	 *  
	 * -----------------------------------
	 * |[      이    미    지      [마일리지] ] |
	 * |[      이    미    지                       ] |
	 * |대표자 :                       |
	 * |전번 :				[전화걸기]
	 * |주소 : 				[지도보기]
	 * 
	 * 기타 설명.....
	 * 
	 *      [메뉴/서비스]  [닫기]
	 * ------------------------------------
	 * 
	 * 닫기 버튼은 상단에 둘수도...눌러서 화면 닫음.
	 *  마일리지 눌러서 마일리지 이력 보기
	 *  전화걸기 눌러서 전화 걸기
	 *  지도보기 눌러서 지도상의 가맹점, 내 위치 확인.
	 *    
	 *  보내는 파라미터: merchantId  activateYn
	 *  받는 파라미터 : CheckMileageMerchant
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
							// 가맹점 아이디를 넣어서 조회
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
	 * 가맹점 상세 정보를 받음
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
//		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
//		if(responseCode==200 || responseCode==204){
//			try {
//				JSONArray jsonArray2 = new JSONArray(tempstr);
//				int max = jsonArray2.length();
//				entries = new ArrayList<CheckMileageMileage>(max);
//				for ( int i = 0; i < max; i++ ){
//					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
//					//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
//					// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
//					entries.add(new CheckMileageMileage(jsonObj.getString("idCheckMileageMileages"),
//							jsonObj.getString("mileage"),jsonObj.getString("modifyDate"),
//							jsonObj.getString("checkMileageMembersCheckMileageId"),jsonObj.getString("checkMileageMerchantsMerchantId")));
//				}
//				//    			 2차 작업. 가맹점 이름, 이미지 가져와서 추가로 넣음.
//				//    			 array 채로 넘기고 돌려받을수 있도록 한다..
//				if(max>0){
//					getMerchantInfo(entries,max);
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
//			Toast.makeText(MyMileagePageActivity.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
//		}
	}
	
	
	
	
}
