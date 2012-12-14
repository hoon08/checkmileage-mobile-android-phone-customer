package kr.co.bettersoft.checkmileage.activities;
/*
 * 가맹점에서 보내온 이벤트 목록보기. 특정 버튼 터치하여 상세 화면으로 이동 가능.
 * 
 * 
 */
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.adapters.PushEventListAdapter;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	
	// 서버 통신 용
	int responseCode = 0;
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	URL postUrl2;
	HttpURLConnection connection2;
	
//	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인.  
//	String imgDomain = CommonUtils.imgDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인.  
	String imgPushDomain = CommonUtils.imgPushDomain;			// 푸시 이미지 전용 도메인
	
	public List<CheckMileagePushEvent> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	public List<CheckMileagePushEvent> dbInEntries;	// db에 넣을 거
	public List<CheckMileagePushEvent> dbOutEntries;	// db에서 꺼낸거
	
	// 받은 데이터 임시 저장용 -> 변수에 임시 저장 후 도메인에 저장
	String tmp_subject = "";			
	String tmp_content = "";
	String tmp_imageFileUrl = "";
	String tmp_modifyDate = "";
	String tmp_companyName = "";
	Bitmap tmp_imageFile = null;
	
	Boolean dbSaveEnable = true;			// db 저장 가능 여부
	public static Boolean searched = false;		// 조회 했는가?
	
	List<CheckMileagePushEvent> entriesFn = null;
	int isRunning = 0;						// 중복 실행 방지
	
	public boolean connected = false;  // 인터넷 연결상태
	View emptyView;
	
	// 진행바
	ProgressBar pb1;
	
	int reTry = 3;
	
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
//	       "idCheckMileageMileages TEXT," +								// 서버 db에 저장된 인덱스 키				// 저장할 데이터 들..
//	       "mileage TEXT," +											// 마일리지 값
//	       "modifyDate TEXT," +											// 수정일시
//	       "checkMileageMembersCheckMileageId TEXT," +					// 사용자 아이디
//	       "checkMileageMerchantsMerchantId TEXT," +					// 가맹점 아이디
//	       "companyName TEXT," +										// 가맹점 이름
//	       "introduction TEXT," +										// 가맹점 소개글
//	       "workPhoneNumber TEXT," +									// 가맹점 전번
//	       "profileThumbnailImageUrl TEXT," +							// 섬네일 이미지 url
//	       "bm TEXT" +													// 섬네일 이미지(string화 시킨 값)
	       ");" ;
	
	// 테이블 조회 쿼리
	private final String Q_GET_LIST = "SELECT * FROM push_event";
	

	//----------------------- SQLite -----------------------//
	
	// 초기화작업- db 및 테이블 검사하고 없으면 만들기.
	SQLiteDatabase db = null;
	public void initDB(){
		Log.i(TAG,"initDB");
		// db 관련 작업 초기화, DB 열어 SQLiteDatabase 인스턴스 생성          db 열거나 없으면 생성
	     if(db== null ){
	          db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
	    }
	     // 테이블에서 데이터 가져오기 전 테이블 생성 확인 없으면 생성.
	      checkTableIsCreated(db);
	}
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
//			 db.close();		// db 는 마지막에 한번 닫음.
			 entriesFn = dbOutEntries;						//  *** 꺼낸 데이터를 결과 데이터에 세팅 
		}catch(Exception e){e.printStackTrace();}
		showEventList();									//  *** 결과 데이터를 화면에 보여준다.		 데이터 있는지 여부는 결과 처리에서 함께..
	}
	////---------------------SQLite ----------------------////
	
	
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	ListView listView;
	
	public Context returnThis(){
		return this;
	}

	// 진행창 보이기/숨기기
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
	
	// 조회한 데이터를 화면에+ 클릭시 이벤트(상세화면으로)
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
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
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
				new backgroundGetMyEventList().execute();	// 이벤트 리스트 조회
		}else{
			Log.w(TAG, "already running..");
		}
	}


	// 비동기로 이벤트 목록 가져오는 함수 호출.
	public class backgroundGetMyEventList extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) { 
		}
		@Override protected void onPreExecute() { 
		}
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundGetMyEventList");
			try {
				getMyEventList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null ;
		}
	}
	/*
	 * 내 이벤트 목록 을 가져온다.
	 * 
	 * 도메인 : checkMileageMerchantMarketing  
	 * 컨트롤러 : checkMileageMerchantMarketingController
	 * 메서드 : selectMemberMerchantMarketingList
	 * 보내는 파라미터 : checkMileageId  activateYn
	 * 받는 데이터 : List<CheckMileageMerchantMarketing>
	 */
	public void getMyEventList() throws JSONException, IOException {
		Log.i(TAG, "getMyEventList");
		if(CheckNetwork()){
			controllerName = "checkMileageMerchantMarketingController";
			methodName = "selectMemberMerchantMarketingList";
			showPb();
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 자신의 아이디를 넣어서 조회
								obj.put("activateYn", "Y");
								obj.put("checkMileageId", myQRcode);
								Log.i(TAG, "myQRcode::"+myQRcode);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMerchantMarketing\":" + obj.toString() + "}";
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								connection2.connect();		// *** 
								Thread.sleep(200);	
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes("UTF-8"));
								os2.flush();
								Thread.sleep(200);
//								System.out.println("postUrl      : " + postUrl2);
//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
//								os2.close();
								// 조회한 결과를 처리.
								getMyEventListResult(in);
								connection2.disconnect();
							}catch(Exception e){ 
								connection2.disconnect();
								// 다시
//								if(reTry>0){
//									Log.w(TAG, "fail and retry remain : "+reTry);
//									reTry = reTry-1;
//									try {
//										Thread.sleep(200);
//										getMyMileageList();
//									} catch (Exception e1) {
//										Log.w(TAG,"again is failed() and again... ;");
//									}	
//								}else{
//									Log.w(TAG,"reTry failed - init reTry");
//									reTry = 3;
//									hidePb();
//									isRunning = isRunning-1;
//									getEventDBData();						// n회 재시도에도 실패하면 db에서 꺼내서 보여준다.
//								}
							}
						}
					}
			).start();
		}else{
			isRunning = isRunning-1;		// 작업중인 카운팅만 다시 되돌림 -1
		}
	}

	 // 이벤트 조회 결과를 처리하는 부분
	public void getMyEventListResult(InputStream in){
		Log.d(TAG,"getMyEventListResult");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		int doneCnt = 0;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"수신::"+builder.toString());
		String tempstr = builder.toString();		
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		if(responseCode==200 || responseCode==204){
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
				reTry = 3;				// 재시도 횟수 복구
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
		}else{			// 요청 실패시	 토스트는 에러남 - 
			showMSG();    // 핸들러 통한 토스트
		}
	}
	
	public void alertToUser(){				// 	data 조회가 잘 안됐어요. -- 로그남김
		Log.d(TAG,"Get Data from Server -> Error Occured..");
		
	}
	
	

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.		-- 2차 처리.
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

	// 이벤트 이미지 : URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}
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
	    	  if(isRunning<1){
	  			isRunning = isRunning+1;
	  				myQRcode = MyQRPageActivity.qrCode;
	  				new backgroundGetMyEventList().execute();		// 조회 --> 새로고침 기능
	  		}else{
	  			Log.w(TAG, "already running..");
	  		}
	             return true ;
	      }
	      return false;
	    }
	////////////////////////////////////////////////////////////
	
	    /*
		 * 네트워크 상태 감지
		 * 
		 */
		public Boolean CheckNetwork(){
			ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			boolean isWifiAvailable = ni.isAvailable();
			boolean isWifiConn = ni.isConnected();
			ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			boolean isMobileAvail = ni.isAvailable();
			boolean isMobileConn = ni.isConnected();
			
			String status = "WiFi Avail="+isWifiAvailable+"//Conn="+isWifiConn
			+"//Mobile Avail="+isMobileAvail
			+"//Conn="+isMobileConn;
			if(!(isWifiConn||isMobileConn)){
				Log.w(TAG,status);
//				AlertShow_networkErr();
				new Thread( 
						new Runnable(){
							public void run(){
								Message message = handler .obtainMessage();
								Bundle b = new Bundle();
								b.putInt( "showNetErrToast" , 1);
								message.setData(b);
								handler .sendMessage(message);
							}
						}
				).start();
				hidePb();
				getEventDBData();		// 통신 안되면 db거 보여주기로..
				isRunning = 0;
				connected = false;
			}else{
				connected = true;
			}
			return connected;
		}
		
		@Override
		public void onDestroy(){
			super.onDestroy();
			db.close();
			try{
				connection2.disconnect();
				}catch(Exception e){}
		}
}
