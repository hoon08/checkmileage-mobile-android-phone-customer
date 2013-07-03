package kr.co.bettersoft.checkmileage.activities;
/**
 * MyMileagePageActivity
 *  내 마일리지 보기 화면
 */


/*
 * 아답터를 꼬진거를 써서 페이지 올때마다 getView 한다.. 나중에 고쳐야 겠다..
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
import kr.co.bettersoft.checkmileage.adapters.MyMileageListAdapter;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMileage;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MyMileagePageActivity extends Activity {
	String TAG = "MyMileagePageActivity";
	final int GET_MY_MILEAGE_LIST = 501; 
	final int UPDATE_LOG_TO_SERVER = 502;

	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;


	float fImgSize = 0;	// 화면 크기
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록

	// 내 좌표 업뎃용				///////////////////////////////////////////////
	String myLat2;
	String myLon2;
	// 전번(업뎃용)
	String phoneNum = "";
	// qr
	String qrCode = "";
	String myQRcode = "";

	String imgthumbDomain = CommonConstant.imgthumbDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인. 

	// 설정 파일 저장소  - 사용자 전번 읽기 / 쓰기 용도	
	SharedPreferences sharedPrefCustom;


	/////////////////////////////////////////////////////////////////////////////



	// 서버 통신용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;

	// 중복 실행 방지용
	int isUpdating = 0;
	int dontTwice = 1;
	public static Boolean searched = false;		// 조회 했는가?
	int isRunning = 0;



	String newMerchantName="";
	int merchantNameMaxLength = 9;			// 가맹점명 표시될 최대 글자수.

	// 화면 구성
	View emptyView;
	ListView listView;
	// 진행바
	ProgressBar pb1;


	public List<CheckMileageMileage> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	public List<CheckMileageMileage> dbInEntries;	// db에 넣을 거
	public List<CheckMileageMileage> dbOutEntries;	// db에서 꺼낸거
	Boolean dbSaveEnable = true;
	List<CheckMileageMileage> entriesFn = null;

	///////////////////////////////////////////////////////////////////////////////////////////////////	
	/*
	 * 모바일 sqlite 를 사용하여 내 마일리지 목록을 받아와서 저장. 
	 * 이후 통신 불가일때 마지막으로 저장한 데이터를 보여준다.
	 * 저장할 값들.. 
	 * tmp_idCheckMileageMileages  / tmp_mileage  / tmp_modifyDate  / tmp_checkMileageMembersCheckMileageId  / 
	 * tmp_checkMileageMerchantsMerchantId  / tmp_companyName  / tmp_introduction  / tmp_workPhoneNumber  / tmp_profileThumbnailImageUrl  / bm
	 * 
	 * 통신 실패시 알림창을 띄워준다.
	 * 통신 성공시 이전 db 테이블을 지우고 새로 테이블을 만들어서 데이터를 넣어준다.
	 * 
	 * 통신 성공 여부와 상관없이 db 테이블이 있고 데이터가 있으면 해당 데이터를 보여준다.
	 */
	////----------------------- SQLite  Query-----------------------//

	// 테이블 삭제 쿼리 ---> 테이블은 init 에서 이미 만들었으니 안의 내용만 지우고...다시 하자
	private static final String Q_INIT_TABLE = "DELETE FROM mileage_info;" ;

	// 테이블 생성 쿼리.
	private static final String Q_CREATE_TABLE = "CREATE TABLE mileage_info (" +
	"_id INTEGER PRIMARY KEY AUTOINCREMENT," +					// 모바일 db 저장되는 자동증가  인덱스 키
	"idCheckMileageMileages TEXT," +								// 서버 db에 저장된 인덱스 키
	"mileage TEXT," +											// 마일리지 값
	"modifyDate TEXT," +											// 수정일시
	"checkMileageMembersCheckMileageId TEXT," +					// 사용자 아이디
	"checkMileageMerchantsMerchantId TEXT," +					// 가맹점 아이디
	"companyName TEXT," +										// 가맹점 이름
	"introduction TEXT," +										// 가맹점 소개글
	"workPhoneNumber TEXT," +									// 가맹점 전번
	"profileThumbnailImageUrl TEXT," +							// 섬네일 이미지 url
	"bm TEXT" +													// 섬네일 이미지(string화 시킨 값)
	");" ;

	// 테이블 조회 쿼리
	private final String Q_GET_LIST = "SELECT * FROM mileage_info";


	//----------------------- SQLite  Query-----------------------////


	//----------------------- SQLite -----------------------//

	// 초기화작업- db 및 테이블 검사하고 없으면 만들기.
	SQLiteDatabase db = null;
	/**
	 * initDB
	 *  db 를 초기화한다
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
	 *  db 생성 확인하여 없으면 생성한다
	 *
	 * @param db
	 * @param
	 * @return
	 */
	public void checkTableIsCreated(SQLiteDatabase db){		// mileage_info 라는 이름의 테이블을 검색하고 없으면 생성.
		Log.i(TAG, "checkTableIsCreated");
		try{
			//			Cursor c = db.query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
			Cursor c = db.query("sqlite_master" , new String[] {"count(*)"}, "name=?" , new String[] {"mileage_info"}, null ,null , null);
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
	 * saveDataToDB
	 *  server에서 받은 data를 db로저장한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void saveDataToDB(){			//	db 테이블을 초기화 후 새 데이터를 넣습니다.	  // oncreate()에서 테이블 검사해서 만들었기 때문에 최초 등은 걱정하지 않는다.
		Log.i(TAG, "saveDataToDB");
		try{
			if(db==null){
				db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
			}
			if(!(db.isOpen())){
				Log.i(TAG, "db is not open.. open db");
				db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
			}

			db.execSQL(Q_INIT_TABLE);
			ContentValues initialValues = null;
			int entrySize = dbInEntries.size();
			if(entrySize>0){
				for(int i =0; i<entrySize; i++){
					initialValues = new ContentValues(); 			// 데이터 넣어본거. 사용 안함. 없으면 없는거라...  --> 데이터 넣을때
					initialValues.put("idCheckMileageMileages", dbInEntries.get(i).getIdCheckMileageMileages()); 
					initialValues.put("mileage", dbInEntries.get(i).getMileage()); 
					initialValues.put("modifyDate", dbInEntries.get(i).getModifyDate()); 
					initialValues.put("checkMileageMembersCheckMileageId", dbInEntries.get(i).getCheckMileageMembersCheckMileageID()); 
					initialValues.put("checkMileageMerchantsMerchantId", dbInEntries.get(i).getCheckMileageMerchantsMerchantID()); 
					initialValues.put("companyName", dbInEntries.get(i).getMerchantName()); 
					initialValues.put("introduction", dbInEntries.get(i).getIntroduction()); 
					initialValues.put("workPhoneNumber", dbInEntries.get(i).getWorkPhoneNumber()); 
					initialValues.put("profileThumbnailImageUrl", dbInEntries.get(i).getMerchantImg()); 		
					// img 는 문자열로 바꿔서 넣는다. 꺼낼땐 역순임.			 // BMP -> 문자열 		
					ByteArrayOutputStream baos = new ByteArrayOutputStream();   
					String bitmapToStr = "";
					dbInEntries.get(i).getMerchantImage().compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
					byte[] b = baos.toByteArray();  
					bitmapToStr = Base64.encodeToString(b, Base64.DEFAULT); 
					initialValues.put("bm", bitmapToStr); 
					db.insert("mileage_info", null, initialValues); 
				}
			}
			Log.i(TAG, "saveDataToDB success");

		}catch(Exception e){e.printStackTrace();}
	}


	// db 에 저장된 데이터를 화면에
	/**
	 * getDBData
	 *  db 에 저장된 데이터를 화면에보여주기 위해 꺼낸다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void getDBData(){
		Log.i(TAG, "getDBData");
		if(!db.isOpen()){
			Log.d(TAG,"getDBData-> db is closed. need to open");
			db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		}
		String tmp_idCheckMileageMileages = "";
		String tmp_mileage = "";
		String tmp_modifyDate = "";
		String tmp_checkMileageMembersCheckMileageId = "";
		String tmp_checkMileageMerchantsMerchantId = "";
		String tmp_companyName = "";
		String tmp_introduction = "";
		String tmp_workPhoneNumber = "";
		String tmp_profileThumbnailImageUrl = "";
		String tmp_bm_str = "";
		Bitmap tmp_bm = null;
		try{
			// 조회
			Cursor c = db.rawQuery( Q_GET_LIST, null );
			if(c.getCount()==0){
				Log.i(TAG, "saved mileage data NotExist");
			}else{
				Log.i(TAG, "saved mileage data Exist");				// 데이터 있으면 꺼내서 사용함.			// 데이터 꺼낼때
				dbOutEntries = new ArrayList<CheckMileageMileage>(c.getCount());		// 개수만큼 생성하기.
				c.moveToFirst();                                 // 커서를 첫라인으로 옮김
				while(c.isAfterLast()== false ){                   // 마지막 라인이 될때까지 1씩 증가하면서 본다
					tmp_idCheckMileageMileages = c.getString(1);	
					tmp_mileage = c.getString(2);	
					tmp_modifyDate = c.getString(3);	
					tmp_checkMileageMembersCheckMileageId = c.getString(4);	
					tmp_checkMileageMerchantsMerchantId = c.getString(5);	
					tmp_companyName = c.getString(6);	
					tmp_introduction = c.getString(7);	
					tmp_workPhoneNumber = c.getString(8);	
					tmp_profileThumbnailImageUrl = c.getString(9);	
					tmp_bm_str = c.getString(10);	
					byte[] decodedString = Base64.decode(tmp_bm_str, Base64.DEFAULT); 
					tmp_bm = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
					dbOutEntries.add(new CheckMileageMileage(tmp_idCheckMileageMileages,
							tmp_mileage,
							tmp_modifyDate,
							tmp_checkMileageMembersCheckMileageId,
							tmp_checkMileageMerchantsMerchantId,
							tmp_companyName,
							tmp_introduction,
							tmp_workPhoneNumber,
							tmp_profileThumbnailImageUrl,
							tmp_bm
					));
					c.moveToNext();
				}
			}
			c.close();
			db.close();
			entriesFn = dbOutEntries;						//  꺼낸 데이터를 결과 데이터에 세팅 
		}catch(Exception e){e.printStackTrace();}
		showInfo();									//   결과 데이터를 화면에 보여준다.		 데이터 있는지 여부는 결과 처리에서 함께..
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
						emptyView = findViewById(R.id.empty2);
						listView  = (ListView)findViewById(R.id.listview);
						listView.setEmptyView(emptyView);
						listView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = 0;
				}
				if(b.getInt("order")==1){
					// 러닝바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 러닝바 종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(MyMileagePageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showNetErrToast")==1){			
					Toast.makeText(MyMileagePageActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
				}

				switch (msg.what)
				{
				case GET_MY_MILEAGE_LIST  : runOnUiThread(new RunnableGetMyMileageList());	
				break;
				case UPDATE_LOG_TO_SERVER  : runOnUiThread(new RunnableUpdateLogToServer());	
				break;
				default : 
					break;
				}				

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		pb1 = (ProgressBar) findViewById(R.id.ProgressBar01);

		// DB 쓸거니까 초기화 해준다.
		initDB();

		myQRcode = MyQRPageActivity.qrCode;			// 내 QR 코드. 

		// 크기 측정
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		//		Log.i(TAG, "screenWidth : " + screenWidth);
		//		Log.i(TAG, "screenHeight : " + screenHeight);
		if(screenWidth < screenHeight ){
			fImgSize = screenWidth;
		}else{
			fImgSize = screenHeight;
		}

		Log.i(TAG, myQRcode);		

		setContentView(R.layout.my_mileage);

		searched = false;		 


		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////

	// 리스트 보여주고 클릭 이벤트 등록 (가맹점 상세 보기)
	/**
	 * setListing
	 *  리스트 보여주고 클릭 이벤트 등록 (가맹점 상세 보기)한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void setListing(){
		listView  = (ListView)findViewById(R.id.listview);
		listView.setAdapter(new MyMileageListAdapter(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// 가맹점 아이디
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// 고유 식별 번호. (상세보기 조회용도)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// 내 마일리지    // 가맹점에 대한 내 마일리지
				startActivity(intent);
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////	



	/**
	 * 러너블. 마일리지 목록 가져오는 함수 
	 */
	class RunnableGetMyMileageList implements Runnable {
		public void run(){
			new backgroundGetMyMileageList().execute();
		}
	}
	/**
	 * backgroundGetMyMileageList
	 *  비동기로 마일리지 목록 가져오는 함수 호출한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundGetMyMileageList extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) { 
		}
		@Override protected void onPreExecute() { 
		}
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundGetMyMileageList");
			

			// 파리미터 세팅
			CheckMileageMileage checkMileageMileageParam = new CheckMileageMileage();
			checkMileageMileageParam.setCheckMileageMembersCheckMileageID(myQRcode);
			// 호출
			// if(!pullDownRefreshIng){
			 showPb();
			// }
			callResult = checkMileageCustomerRest.RestGetMyMileageList(checkMileageMileageParam);
			 hidePb();
			// 결과 처리
			if(callResult.equals("S")){ //  성공
				processMyMileageListData();
			}else{ 					//  실패
				getDBData();	
			}
			isRunning = 0;
			return null ;
		}
	}


	/**
	 *  러너블. 서버에 위치 및 로그 남김
	 * loggingToServer
	 */
	class RunnableUpdateLogToServer implements Runnable {
		public void run(){
			new backgroundUpdateLogToServer().execute();	// 비동기로 전환	
		}
	}
	/**
	 * 비동기로 사용자의 위치 정보 및 정보 로깅
	 * backgroundUpdateLogToServer
	 */
	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyLocationtoServer");

			if(isUpdating==0){
				isUpdating = 1;
				
				// 파리미터 세팅
				phoneNum = sharedPrefCustom.getString("phoneNum", "");	
				myLat2 = sharedPrefCustom.getString("myLat2", "");	
				myLon2 = sharedPrefCustom.getString("myLon2", "");	
				qrCode = sharedPrefCustom.getString("qrCode", "");		
				CheckMileageLogs checkMileageLogsParam = new CheckMileageLogs();
				checkMileageLogsParam.setCheckMileageId(qrCode);
				checkMileageLogsParam.setParameter01(phoneNum);
				checkMileageLogsParam.setParameter04("");
				checkMileageLogsParam.setViewName("CheckMileageCustomerMerchantListView");

				// 호출
								showPb();
				callResult = checkMileageCustomerRest.RestUpdateLogToServer(checkMileageLogsParam);
				hidePb();
				isUpdating = 0;
				// 페이지별 업무. 마일리지 조회.
					Log.w(TAG,"onResume, search");
					//									if(dontTwice==0){
					if(isRunning<1){
						isRunning = 1;
						myQRcode = MyQRPageActivity.qrCode;
//						new backgroundGetMyMileageList().execute();
						handler.sendEmptyMessage(GET_MY_MILEAGE_LIST); 
					}else{
						Log.w(TAG, "already running..");
					}
					//									}else{
					//										dontTwice = 0;
					//									}
					
					
			}else{
				Log.w(TAG,"already updating..");
			}
			
			return null; 
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	// 서버에서 받아온 내 마일리지 리스트 데이터 처리하여 화면에 뿌려준다.
	public void processMyMileageListData(){
		try {
			tempstr = CheckMileageCustomerRest.getTempstr();
			JSONArray jsonArray2 = new JSONArray(tempstr);
			int max = jsonArray2.length();
			try {
				entries = new ArrayList<CheckMileageMileage>(max);
				String tmp_idCheckMileageMileages = "";
				String tmp_mileage = "";
				String tmp_modifyDate = "";
				String tmp_shortDate = "";
				String tmpstr2 = "";
				String tmp_checkMileageMembersCheckMileageId = "";
				String tmp_checkMileageMerchantsMerchantId = "";
				String tmp_companyName = "";
				String tmp_introduction = "";		//prstr = jsonobj2.getString("introduction");		// prSentence --> introduction
				String tmp_workPhoneNumber = "";
				String tmp_profileThumbnailImageUrl = "";
				Bitmap bm = null;
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						jsonObject = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.

						tmp_idCheckMileageMileages = jsonObject.getString("idCheckMileageMileages");
						try{
							tmp_mileage = jsonObject.getString("mileage");
						}catch(Exception e){
							tmp_mileage = "0";
						}
						try{
							tmp_modifyDate = jsonObject.getString("modifyDate");
							String tmpstr = getString(R.string.last_update);
							if(tmp_modifyDate.length()>9){
								tmpstr2 = tmp_modifyDate.substring(0, 4)+ getString(R.string.year) 		// 년
								+ tmp_modifyDate.substring(5, 7)+ getString(R.string.month) 					// 월
								+ tmp_modifyDate.substring(8, 10)+ getString(R.string.day) 					// 일
								;
								tmp_modifyDate = tmpstr2;
							}
							tmp_modifyDate = tmpstr+":"+tmp_modifyDate;
						}catch(Exception e){
							tmp_modifyDate = "";
						}
						try{
							tmp_checkMileageMembersCheckMileageId = jsonObject.getString("checkMileageMembersCheckMileageId");
						}catch(Exception e){
							tmp_checkMileageMembersCheckMileageId = "";
						}
						try{
							tmp_checkMileageMerchantsMerchantId = jsonObject.getString("checkMileageMerchantsMerchantId");
						}catch(Exception e){
							tmp_checkMileageMerchantsMerchantId = "";
						}
						try{  
							tmp_introduction = jsonObject.getString("introduction");
						}catch(Exception e){
							tmp_introduction = "";
						}
						try{
							tmp_companyName = jsonObject.getString("companyName");
						}catch(Exception e){
							tmp_companyName = "";
						}
						try{
							tmp_workPhoneNumber = jsonObject.getString("workPhoneNumber");
						}catch(Exception e){
							tmp_workPhoneNumber = "";
						}
						try{
							tmp_profileThumbnailImageUrl = jsonObject.getString("profileThumbnailImageUrl");
						}catch(Exception e){
							tmp_profileThumbnailImageUrl = "";
						}
						// tmp_profileThumbnailImageUrl 있을때.
						if(tmp_profileThumbnailImageUrl!=null && tmp_profileThumbnailImageUrl.length()>0){
							if(tmp_profileThumbnailImageUrl.contains("http")){		// url 포함한 경우
								try{
									bm = LoadImage(tmp_profileThumbnailImageUrl);				 
								}catch(Exception e3){}
							}else{		// url 포함하지 않으면 붙여준다.
								try{
									bm = LoadImage(imgthumbDomain+tmp_profileThumbnailImageUrl);				 
								}catch(Exception e3){
									Log.w(TAG, imgthumbDomain+tmp_profileThumbnailImageUrl+" -- fail");
								}
							}
						}else{
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
							bm = dw.getBitmap();
						}
						if(bm==null){		//  없을때.. 
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
							bm = dw.getBitmap();
						}
						entries.add(new CheckMileageMileage(tmp_idCheckMileageMileages,
								tmp_mileage,
								tmp_modifyDate,
								tmp_checkMileageMembersCheckMileageId,
								tmp_checkMileageMerchantsMerchantId,
								tmp_companyName,
								tmp_introduction,
								tmp_workPhoneNumber,
								tmp_profileThumbnailImageUrl,
								bm
								// 그 외 섬네일 이미지, 가맹점 이름
						));
					}
				}
			}catch (JSONException e) {
				dbSaveEnable = false;
				e.printStackTrace();
			}
			dbInEntries = entries; 
			// db 에 데이터를 넣는다.
			try{
				if(dbSaveEnable){		// 이미지까지 성공적으로 가져온 경우.
					saveDataToDB();
				}else{
					alertToUser();		// 이미지 가져오는데 실패한 경우.
					// 어쨎든 처리가 끝나면 (공통) -  db를 검사하여 데이터가 있으면 보여주고  entriesFn = dbOutEntries
				}	// 처리가 끝나면 공통으로 해야할 showInfo(); (그전에 entriesFn 설정 한다)
			}catch(Exception e){}
			getDBData();			//db 에 잇으면 그거 쓰고 없으면 없다고 알림. * 에러나면 이전 데이터를 보여주기 때문에 db에 있는 정보가 정확하다고 볼수는 없음.. 
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}


	////////////////////////   하드웨어 메뉴 버튼.  ////////////////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String tmpstr = getString(R.string.refresh);
		menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, tmpstr );             // 신규등록 메뉴 추가.
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
		case Menu.FIRST+1:
			if(isRunning<1){
				isRunning = 1;
				myQRcode = MyQRPageActivity.qrCode;
				//				new backgroundGetMyMileageList().execute();	
				handler.sendEmptyMessage(GET_MY_MILEAGE_LIST);
			}else{
				Log.w(TAG, "already running..");
			}
			return true ;
		}
		return false;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////


	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
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

	/////////////////////////////////////////////////////////////////////////////////////////////////////////


	public void alertToUser(){				// 	data 조회가 잘 안됐어요. // 별도 알림 없이 로그만 찍는다.
		Log.d(TAG,"Get Data from Server -> Error Occured..");
	}


	/**
	 * showInfo
	 *  결과 도메인을 화면에 뿌려준다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showInfo(){
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

	/**
	 * onResume
	 *  마일리지 리스트 조회가 되지 않았다면 액티비티 리쥼시 마일리지 리스트를 재조회한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		dbSaveEnable = true;

		if(isUpdating==0){			
			//			loggingToServer();		// ***  서버로깅. 나중에 주석 풀것.		
			handler.sendEmptyMessage(UPDATE_LOG_TO_SERVER);
		}

	}







	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	/**
	 * onBackPressed
	 *  닫기 버튼 2번 누르면 종료한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.w(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyMileagePageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
			new Thread( 
					new Runnable(){
						public void run(){
							try {
								Thread.sleep(3000);
								app_end = 0;
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
			).start();
		}
	}


	/**
	 * returnThis
	 *  컨택스트를 리턴한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public Context returnThis(){
		return this;
	}
	// 진행바 보임 / 숨김
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

	/////////////////////////////////////////////////////////////////////////////////////////////////////////	

}
