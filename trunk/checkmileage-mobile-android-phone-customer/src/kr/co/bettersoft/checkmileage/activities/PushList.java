package kr.co.bettersoft.checkmileage.activities;
/**
 * PushList
 * 
 * 가맹점에서 보내온 이벤트 목록보기. 특정 버튼 터치하여 상세 화면으로 이동 가능.
 * 
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.adapters.PushEventListAdapter;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;
import kr.co.bettersoft.checkmileage.domain.CheckMileagePushEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PushList extends Activity {

	String TAG = "PushList";
	final int GET_MY_EVENT_LIST = 801;

	String myQRcode = "";
	String imgPushDomain = CommonConstant.imgPushDomain;			// 푸시 이미지 전용 도메인

	// 받은 데이터 임시 저장용 -> 변수에 임시 저장 후 도메인에 저장
	String tmp_subject = "";			
	String tmp_content = "";
	String tmp_imageFileUrl = "";
	String tmp_modifyDate = "";
	String tmpstr2 = "";
	String tmp_companyName = "";
	Bitmap tmp_imageFile = null;
	
	// 서버 통신 용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;

	Boolean dbSaveEnable = true;			// db 저장 가능 여부
	public static Boolean searched = false;		// 조회 했는가?
	int isRunning = 0;						// 중복 실행 방지
	int doneCnt = 0;
	
	// 화면 구성
	View emptyView;
	ListView listView;
	// 진행바
	ProgressBar pb1;

	public List<CheckMileagePushEvent> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	public List<CheckMileagePushEvent> dbInEntries;	// db에 넣을 거
	public List<CheckMileagePushEvent> dbOutEntries;	// db에서 꺼낸거
	List<CheckMileagePushEvent> entriesFn = null;
	
/////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	/*
	 * 모바일 sqlite 를 사용하여 내 이벤트 목록을 받아와서 저장. 
	 * 이후 통신 불가일때 마지막으로 저장한 데이터를 보여준다.
	 * 
	 * 통신 실패시 알림창을 띄워준다.
	 * 통신 성공시 이전 db 테이블을 지우고 새로 테이블을 만들어서 데이터를 넣어준다.
	 * 
	 * 통신 성공 여부와 상관없이 db 테이블이 있고 데이터가 있으면 해당 데이터를 보여준다.
	 */
	////----------------------- SQLite  Query-----------------------//

	// 테이블 삭제 쿼리 ---> 테이블은 init 에서 이미 만들었으니 안의 내용만 지우고...다시 하자
	private static final String Q_INIT_TABLE = "DELETE FROM push_event;" ;

	// 테이블 생성 쿼리.
	private static final String Q_CREATE_TABLE = "CREATE TABLE push_event (" +
	"_id INTEGER PRIMARY KEY AUTOINCREMENT," +					// 모바일 db 저장되는 자동증가  인덱스 키
	"subject TEXT," +											// 이벤트 제목
	"content TEXT," +											// 이벤트 글귀
	"imageFileUrl TEXT," +										// 이벤트이미지 주소
	"modifyDate TEXT," +											// 이벤트 등록일
	"companyName TEXT," +										// 업체명
	"imageFile TEXT" +											// 이미지 파일(문자화)
	");" ;

	// 테이블 조회 쿼리
	private final String Q_GET_LIST = "SELECT * FROM push_event";


	//----------------------- SQLite -----------------------//

	// 초기화작업- db 및 테이블 검사하고 없으면 만들기.
	SQLiteDatabase db = null;
	/**
	 * initDB
	 *  db 초기화한다, 없으면 생성한다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	public void initDB(){
		Log.i(TAG,"initDB");
		// db 관련 작업 초기화, DB 열어 SQLiteDatabase 인스턴스 생성          db 열거나 없으면 생성
		if(db== null ){
			db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		}
		// 테이블에서 데이터 가져오기 전 테이블 생성 확인 없으면 생성.
		checkTableIsCreated(db);
	}

	/**
	 * checkTableIsCreated
	 *  db 확인하여 없으면 생성한다
	 *
	 * @param db
	 * @param
	 * @return
	 */
	public void checkTableIsCreated(SQLiteDatabase db){		// mileage_info 라는 이름의 테이블을 검색하고 없으면 생성.
		Log.i(TAG, "checkTableIsCreated");
		try{
			//			Cursor c = db.query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
			Cursor c = db.query("sqlite_master" , new String[] {"count(*)"}, "name=?" , new String[] {"push_event"}, null ,null , null);
			Integer cnt=0;
			c.moveToFirst();                                 // 커서를 첫라인으로 옮김
			while(c.isAfterLast()== false ){                   // 마지막 라인이 될때까지 1씩 증가하면서 본다
				cnt=c.getInt(0);
				c.moveToNext();
			}
			//커서는 사용 직후 닫는다
			c.close();
			//테이블 없으면 생성
			if(cnt==0){
				db.execSQL(Q_CREATE_TABLE);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// server에서 받은 data를 db로
	/**
	 * saveEventDataToDB
	 * server에서 받은 data를 db로 저장한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void saveEventDataToDB(){			//	db 테이블을 초기화 후 새 데이터를 넣습니다.	  // oncreate()에서 테이블 검사해서 만들었기 때문에 최초 등은 걱정하지 않는다.
		Log.i(TAG, "saveEventDataToDB");
		try{
			db.execSQL(Q_INIT_TABLE);
			ContentValues initialValues = null;
			int entrySize = dbInEntries.size();
			if(entrySize>0){
				for(int i =0; i<entrySize; i++){
					initialValues = new ContentValues(); 			//  데이터 넣을때
					initialValues.put("subject", dbInEntries.get(i).getSubject()); 
					initialValues.put("content", dbInEntries.get(i).getContent()); 
					initialValues.put("imageFileUrl", dbInEntries.get(i).getImageFileUrl()); 
					initialValues.put("modifyDate", dbInEntries.get(i).getModifyDate()); 
					initialValues.put("companyName", dbInEntries.get(i).getCompanyName()); 
					// img 는 문자열로 바꿔서 넣는다. 꺼낼땐 역순임.			 // BMP -> 문자열 		
					ByteArrayOutputStream baos = new ByteArrayOutputStream();   
					String bitmapToStr = "";
					dbInEntries.get(i).getImageFile().compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
					byte[] b = baos.toByteArray();  
					bitmapToStr = Base64.encodeToString(b, Base64.DEFAULT); 
					initialValues.put("imageFile", bitmapToStr); 
					db.insert("push_event", null, initialValues); 
				}
			}
			Log.i(TAG, "saveEventDataToDB success");
		}catch(Exception e){e.printStackTrace();}
	}


	// db 에 저장된 데이터를 화면에
	/**
	 * getEventDBData
	 * db 에 저장된 데이터를 화면에 보여주기 위해 꺼낸다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void getEventDBData(){
		Log.i(TAG, "getEventDBData");
		if(!db.isOpen()){
			Log.d(TAG,"getEventDBData-> db is closed. need to open");
			db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		}
		String tmp_subject = "";
		String tmp_content = "";
		String tmp_imageFileUrl = "";
		String tmp_modifyDate = "";
		String tmp_companyName = "";
		String tmp_imageFile_str = "";
		Bitmap tmp_imageFile = null;
		try{
			// 조회
			Cursor c = db.rawQuery( Q_GET_LIST, null );
			if(c.getCount()==0){
				Log.i(TAG, "saved event data NotExist");
			}else{
				Log.i(TAG, "saved event data Exist");				// 데이터 있으면 꺼내서 사용함.			// 데이터 꺼낼때
				dbOutEntries = new ArrayList<CheckMileagePushEvent>(c.getCount());		// 개수만큼 생성하기.
				c.moveToFirst();                                 // 커서를 첫라인으로 옮김
				while(c.isAfterLast()== false ){                   // 마지막 라인이 될때까지 1씩 증가하면서 본다
					tmp_subject = c.getString(1);	
					tmp_content = c.getString(2);	
					tmp_imageFileUrl = c.getString(3);	
					tmp_modifyDate = c.getString(4);	
					tmp_companyName = c.getString(5);	
					tmp_imageFile_str = c.getString(6);	
					byte[] decodedString = Base64.decode(tmp_imageFile_str, Base64.DEFAULT); 
					tmp_imageFile = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
					dbOutEntries.add(new CheckMileagePushEvent(tmp_subject,
							tmp_content,
							tmp_imageFileUrl,
							tmp_modifyDate,
							tmp_companyName,
							tmp_imageFile_str,
							tmp_imageFile
					));
					c.moveToNext();
				}
			}
			c.close();
			entriesFn = dbOutEntries;						//  *** 꺼낸 데이터를 결과 데이터에 세팅 
		}catch(Exception e){e.printStackTrace();}
		showEventList();									//  *** 결과 데이터를 화면에 보여준다.		 데이터 있는지 여부는 결과 처리에서 함께..
	}
	////---------------------SQLite ----------------------////

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// 받아온 마일리지 결과를 화면에 뿌려준다.
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 
					if(entriesFn!=null && entriesFn.size()>0){
						setListing();
					}else{
						Log.d(TAG,"no data");
						emptyView = findViewById(R.id.push_list_empty2);
						listView  = (ListView)findViewById(R.id.push_list_listview);
						listView.setEmptyView(emptyView);
						listView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// 러닝바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 러닝바 종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_list_ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){		// 일반 에러 토스트
					Toast.makeText(PushList.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showNetErrToast")==1){		// 네트워크 에러 토스트
					Toast.makeText(PushList.this, R.string.network_error, Toast.LENGTH_SHORT).show();
				}
				
				switch (msg.what)
				{
					case GET_MY_EVENT_LIST : runOnUiThread(new RunnableGetMyEventList());	
					break;
					default : 
					break;
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};


////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		
		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		pb1 = (ProgressBar) findViewById(R.id.push_list_ProgressBar01);
		// DB 쓸거니까 초기화 해준다.
		initDB();

		myQRcode = MyQRPageActivity.qrCode;			// 내 QR 코드. 

		Log.i(TAG, myQRcode);		

		setContentView(R.layout.push_list);

		searched = false;		 

		if(isRunning<1){								// 중복 실행 방지
			isRunning = isRunning+1;
			myQRcode = MyQRPageActivity.qrCode;
			handler.sendEmptyMessage(GET_MY_EVENT_LIST);
		}else{
			Log.w(TAG, "already running..");
		}
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	// 조회한 데이터를 화면에+ 클릭시 이벤트(상세화면으로)
	/**
	 * setListing
	 *  조회한 데이터를 화면에출력하고 클릭시 상세화면으로 이동하도록 이벤트를 등록한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void setListing(){
		listView  = (ListView)findViewById(R.id.push_list_listview);
		listView.setAdapter(new PushEventListAdapter(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(PushList.this, PushDetail.class);
				intent.putExtra("subject", entriesFn.get(position).getSubject());		// 이벤트 제목
				intent.putExtra("content", entriesFn.get(position).getContent());		// 이벤트 글귀
				intent.putExtra("imageFileUrl", entriesFn.get(position).getImageFileUrl());		// 이벤트 광고 이미지 주소
				intent.putExtra("imageFileStr", entriesFn.get(position).getImageFileStr());		// 이벤트 광고 이미지 문자화
				intent.putExtra("modifyDate", entriesFn.get(position).getModifyDate());		// 이벤트 업뎃 날짜
				intent.putExtra("companyName", entriesFn.get(position).getCompanyName());		// 이벤트 업체명
				startActivity(intent);
			}
		});
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 러너블. 이벤트 목록 가져오는 함수 호출한다
	 */
	class RunnableGetMyEventList implements Runnable {
		public void run(){
			new backgroundGetMyEventList().execute();
		}
	}
	// 비동기로 이벤트 목록 가져오는 함수 호출.
	/**
	 * backgroundGetMyEventList
	 *  비동기로 이벤트 목록 가져오는 함수 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetMyEventList extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) { 
		}
		@Override protected void onPreExecute() { 
		}
		@Override protected Void doInBackground(Void... params) { 

			// 파리미터 세팅
			 CheckMileageMembers checkMileageMembersParam = new CheckMileageMembers(); 
			 checkMileageMembersParam.setCheckMileageId(myQRcode);
			// 호출
			// if(!pullDownRefreshIng){
			 showPb();
			// }
			callResult = checkMileageCustomerRest.RestGetMyEventList(checkMileageMembersParam);
			 hidePb();
			// 결과 처리
			 if(callResult.equals("S")){ //  성공
			     Log.i(TAG, "S");
			     processMyEventListData();
//			     tempstr = checkMileageCustomerRest.getTempstr();
//			     Log.d(TAG,"tempstr:"+tempstr);
			     // ... 이후 처리. 전번 꺼내서 사용하는 부분 필요. @@@ 
		     }else{ //  실패
			     Log.i(TAG, "F");
		     }
			 
			return null ;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	
	public void processMyEventListData(){
		tempstr = checkMileageCustomerRest.getTempstr();
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
			try {
				entries = new ArrayList<CheckMileagePushEvent>(max);

				//				String tmp_imageFileStr = "";
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						doneCnt++;
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchantMarketing");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.

						//						tmp_idCheckMileageMileages = jsonObj.getString("idCheckMileageMileages");
						try{
							tmp_subject = jsonObj.getString("subject");
						}catch(Exception e){
							Log.d(TAG,"subject F");
							tmp_subject = "";
						}
						try{
							tmp_content = jsonObj.getString("content");
						}catch(Exception e){
							Log.d(TAG,"content F");
							tmp_content = "";
						}
						try{
							if(jsonObj.getString("imageFileUrl").length()>0){
								tmp_imageFileUrl = imgPushDomain+jsonObj.getString("imageFileUrl");
							}else{
								tmp_imageFileUrl = "";
							}

						}catch(Exception e){
							Log.d(TAG,"imageFileUrl F");
							tmp_imageFileUrl = "";
						}
						try{
							tmp_modifyDate = jsonObj.getString("modifyDate");
							if(tmp_modifyDate!=null && tmp_modifyDate.length()>15){
								Log.d(TAG,"tmp_modifyDate:"+tmp_modifyDate);
								Log.d(TAG,"tmp_modifyDate.substring(0, 4):"+tmp_modifyDate.substring(0, 4)+"//tmp_modifyDate.substring(5, 7):"+tmp_modifyDate.substring(5, 7)+"//tmp_modifyDate.substring(8, 10):"+tmp_modifyDate.substring(8, 10));
								Log.d(TAG,"tmp_modifyDate.substring(11, 13):"+tmp_modifyDate.substring(11, 13)+"//tmp_modifyDate.substring(14, 16):"+tmp_modifyDate.substring(14, 16));
								tmpstr2 = tmp_modifyDate.substring(0, 4)+ getString(R.string.year) 		// 년
								+ tmp_modifyDate.substring(5, 7)+ getString(R.string.month) 					// 월
								+ tmp_modifyDate.substring(8, 10)+ getString(R.string.day) 					// 일
								+ tmp_modifyDate.substring(11, 13)+ getString(R.string.hour)					// 시
								+ tmp_modifyDate.substring(14, 16)+ getString(R.string.minute)					// 분
								;
							}

							tmpstr2 = tmp_modifyDate.substring(0, 4)+ getString(R.string.year) 		// 년
							+ tmp_modifyDate.substring(5, 7)+ getString(R.string.month) 					// 월
							+ tmp_modifyDate.substring(8, 10)+ getString(R.string.day) 					// 일
							//							+ tmp_modifyDate.substring(0, 4)+ getString(R.string.year)					// 시
							//							+ tmp_modifyDate.substring(0, 4)+ getString(R.string.year)					// 분
							;
							tmp_modifyDate = tmpstr2;
						}catch(Exception e){
							Log.d(TAG,"modifyDate F");
							tmp_modifyDate = "";
						}
						try{
							tmp_companyName = jsonObj.getString("companyName");
						}catch(Exception e){
							Log.d(TAG,"companyName F");
							tmp_companyName = "";
						}
						// tmp_imageFileUrl 있을때.
						if(tmp_imageFileUrl.length()>0){
							try{
								tmp_imageFile = LoadImage(tmp_imageFileUrl);
							}catch(Exception e3){
								Log.w(TAG, tmp_imageFileUrl+" -- fail");
							}
						}else{
							Log.d(TAG,"tmp_imageFileUrl length 0");
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_320_240);
							tmp_imageFile = dw.getBitmap();
						}
						if(tmp_imageFile==null){		//  없을때.. 
							Log.d(TAG,"last tmp_imageFileUrl null");
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_320_240);
							tmp_imageFile = dw.getBitmap();
						}
						entries.add(new CheckMileagePushEvent(
								tmp_subject,  tmp_content,
								tmp_imageFileUrl,  tmp_modifyDate,
								tmp_companyName,  "",
								tmp_imageFile
								// 그 외 섬네일 이미지, 가맹점 이름
						));

					}
				}
			}catch (JSONException e) {
				doneCnt--;
				//				dbSaveEnable = false;
				e.printStackTrace();
			}finally{
				dbInEntries = entries; 
				searched = true;
				// db 에 데이터를 넣는다.
				try{
					if(dbSaveEnable){		// 이미지까지 성공적으로 가져온 경우.
						saveEventDataToDB();
					}else{
						alertToUser();		// 이미지 가져오는데 실패한 경우.
						// 어쨎든 처리가 끝나면 (공통) -  db를 검사하여 데이터가 있으면 보여주고  entriesFn = dbOutEntries
					}	// 처리가 끝나면 공통으로 해야할 showInfo(); (그전에 entriesFn 설정 한다)
				}catch(Exception e){}
				finally{
					getEventDBData();			//db 에 잇으면 그거 쓰고 없으면 없다고 알림. * 에러나면 이전 데이터를 보여주기 때문에 db에 있는 정보가 정확하다고 볼수는 없음.. 
				}
			}
	}
	



//////////////////////////////////////////////////////////////////////////////////////////	
	
	// 이벤트 이미지 : URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	/**
	 * LoadImage
	 *  가맹점 이미지 URL 에서 이미지 받아온 스트림을 비트맵으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return bm
	 */
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}

	/**
	 * OpenHttpConnection
	 *  가맹점 이미지 URL 에서 이미지 받아와서 스트림으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return stream
	 */
	private InputStream OpenHttpConnection(String $imagePath) {
		InputStream stream = null ;
		try {
			URL url = new URL( $imagePath ) ;
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection() ;
			urlConnection.setRequestMethod( "GET" ) ;
			urlConnection.connect() ;
			if( urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK ) {
				stream = urlConnection.getInputStream() ;
			}
		} catch (MalformedURLException e) {
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			Log.w(TAG,"IOException");
		}
		return stream ;
	}


	////////////////////////   하드웨어 메뉴 버튼.  ////////////////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String tmpstr = getString(R.string.refresh);
		menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, tmpstr );             // 신규등록 메뉴 추가. -- 새로고침
		//	          getMenuInflater().inflate(R.menu.activity_main, menu);
		return (super .onCreateOptionsMenu(menu));
	}
	// 옵션 메뉴 특정 아이템 클릭시 필요한 일 처리
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		return (itemCallback(item)|| super.onOptionsItemSelected(item));
	}
	// 아이템 아이디 값 기준 필요한 일 처리
	public boolean itemCallback(MenuItem item){
		switch(item.getItemId()){
		case Menu. FIRST+1:
			if(isRunning<1){					// 조회중
				isRunning = isRunning+1;
				myQRcode = MyQRPageActivity.qrCode;		// 내 qr 로
//				new backgroundGetMyEventList().execute();		// 조회 한다--> 새로고침 기능 이 된다
				handler.sendEmptyMessage(GET_MY_EVENT_LIST);
			}else{
				Log.w(TAG, "already running..");
			}
			return true ;
		}
		return false;
	}
	////////////////////////////////////////////////////////////
	
	public void alertToUser(){				// 	data 조회가 잘 안됐어요. -- 로그남김
		Log.d(TAG,"Get Data from Server -> Error Occured..");
	}
	
	/**
	 * returnThis
	 *  컨택스트를 리턴한다.(핸들러에서 필요)
	 *
	 * @param
	 * @param
	 * @return
	 */
	public Context returnThis(){
		return this;
	}

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.		-- 2차 처리.
	/**
	 * showEventList
	 *  결과 도메인을 핸들러를 통해 화면에 보여준다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showEventList(){
		hidePb();
		//  가져온 데이터 화면에 보여주기.
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
	
	
	// 진행창 보이기/숨기기
	/**
	 * showPb
	 *  중앙 프로그래스바 가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showPb(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("order", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}
	/**
	 * hidePb
	 *  중앙 프로그래스바 비가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void hidePb(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("order", 2);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}

	/**
	 * showMSG
	 *  화면에 error 토스트 띄운다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showMSG(){			// 화면에 토스트 띄움..
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


	@Override
	public void onDestroy(){
		db.close();			//사용이 끝났으니 db 는 닫아준다
		super.onDestroy();
	}
}
