package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 내 마일리지 보기 화면


/*
 * 아답터를 꼬진거를 써서 페이지 올때마다 getView 한다.. 나중에 고쳐야 겠다..
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kr.bettersoft.domain.CheckMileageMileage;
import com.pref.DummyActivity;
import com.utils.adapters.ImageAdapterList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int dontTwice = 1;
	
	int responseCode = 0;
	String TAG = "MyMileagePageActivity";
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인.   
	public List<CheckMileageMileage> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	public List<CheckMileageMileage> dbInEntries;	// db에 넣을 거
	public List<CheckMileageMileage> dbOutEntries;	// db에서 꺼낸거
	Boolean dbSaveEnable = true;
	
	public static Boolean searched = false;		// 조회 했는가?
	
	
	int reTry = 5;
	
	int merchantNameMaxLength = 9;			// 가맹점명 표시될 최대 글자수.
	String newMerchantName="";
	
	public boolean connected = false;  // 인터넷 연결상태
	
	/*  구식 방법 사용 안함
	private ArrayAdapter<String> m_adapter = null;
	private ListView m_list = null;
	ArrayAdapter<CheckMileageMileage> adapter = null;
	MyAdapter mAdapter;
*/ 

	List<CheckMileageMileage> entriesFn = null;
	float fImgSize = 0;
	int isRunning = 0;
	
	View emptyView;
	
	// 진행바
	ProgressBar pb1;
	
	
	
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
	
	// 테이블 삭제 쿼리 ---> 테이블은 이닛에서 이미 만들었으니 안의 내용만 지우고...다시 하자
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
	public void saveDataToDB(){			//	db 테이블을 초기화 후 새 데이터를 넣습니다.	  // oncreate()에서 테이블 검사해서 만들었기 때문에 최초 등은 걱정하지 않는다.
		Log.i(TAG, "saveDataToDB");
		try{
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
	public void getDBData(){
		Log.i(TAG, "getDBData");
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
//			Log.i(TAG, Integer.toString(c.getCount()));			// qr img
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
			 entriesFn = dbOutEntries;						//  *** 꺼낸 데이터를 결과 데이터에 세팅 
		}catch(Exception e){e.printStackTrace();}
		showInfo();									//  *** 결과 데이터를 화면에 보여준다.		 데이터 있는지 여부는 결과 처리에서 함께..
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
						emptyView = findViewById(R.id.empty2);
						listView  = (ListView)findViewById(R.id.listview);
						listView.setEmptyView(emptyView);
						listView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					/* 구식 방법
					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);
					*/
					isRunning = isRunning -1;
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	ListView listView;
	
	public Context returnThis(){
		return this;
	}

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
	
	
	public void setListing(){
		listView  = (ListView)findViewById(R.id.listview);
		listView.setAdapter(new ImageAdapterList(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
//				Log.i(TAG, "checkMileageMerchantsMerchantID::"+entriesFn.get(position).getCheckMileageMerchantsMerchantID());
//				Log.i(TAG, "myMileage::"+entriesFn.get(position).getMileage());
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// 가맹점 아이디
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// 고유 식별 번호. (상세보기 조회용도)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// 내 마일리지    // 가맹점에 대한 내 마일리지
				startActivity(intent);
			}
		});
	}
	
	/*   // 구식 방법을 사용하지 않음.
	// 어댑터 클래스. 이곳에서 얻어온 데이터를 뷰 아이디를 통해 세팅한다.
	class MyAdapter extends BaseAdapter{
		Context context;
		int layoutId;
		ArrayList<CheckMileageMileage> myDataArr;
		LayoutInflater Inflater;
		MyAdapter(Context _context, int _layoutId, ArrayList<CheckMileageMileage> _myDataArr){
			context = _context;
			layoutId = _layoutId;
			myDataArr = _myDataArr;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			return myDataArr.size();
		}
		@Override
		public String getItem(int position) {
			return myDataArr.get(position).getCheckMileageMerchantsMerchantID();
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null)  {
				convertView = Inflater.inflate(layoutId, parent, false);
			}
			ImageView leftImg = (ImageView)convertView.findViewById(R.id.merchantImage);		// 가맹점 이미지 넣고
			// set the Drawable on the ImageView
			if(myDataArr.get(position).getMerchantImage()!=null){
				BitmapDrawable bmpResize = BitmapResizePrc(myDataArr.get(position).getMerchantImage(), fImgSize/2, fImgSize/2);  
				leftImg.setImageDrawable(bmpResize);	
			}
				
//			leftImg.setImageBitmap(myDataArr.get(position).getMerchantImage());			
			
			TextView nameTv = (TextView)convertView.findViewById(R.id.merchantName);			// 가맹점 이름 넣고
			nameTv.setText(myDataArr.get(position).getMerchantName());
			TextView mileage = (TextView)convertView.findViewById(R.id.mileage);				// 가맹점에 대한 내 마일리지 넣고		.. 더 넣을거 있으면 아래에 추가, XML 파일에도 뷰 등록..
			mileage.setText(myDataArr.get(position).getMileage()+"점");					
			
			TextView workPhone = (TextView)convertView.findViewById(R.id.merchantPhone);				// 가맹점 전번.
			workPhone.setText(myDataArr.get(position).getWorkPhoneNumber());		
			
//			Button btn = (Button)convertView.findViewById(R.id.sendBtn);		// 하단 버튼 넣어서 클릭시 어쩌구..
//			btn.setOnClickListener(new Button.OnClickListener()  {
//				public void onClick(View v)  {
//					String str = myDataArr.get(pos).name + "님의 전화번호는 [ "+
//					                                                   myDataArr.get(pos).phone+" ] 입니다.";
//					Toast.makeText(context, str,Toast.LENGTH_SHORT).show();
//				}
//			});
			return convertView;
		}
	}
	*/
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pb1 = (ProgressBar) findViewById(R.id.ProgressBar01);
//		final ProgressDialog dialog= ProgressDialog.show(MyMileagePageActivity.this, "타이틀","메시지",true);
////		b. Dialog를 화면에서 제거하는 코드를 작성한다. 예를 들어 3초쯤 있다가 다이얼로그를 없애고 싶다면...
//		new Thread(new Runnable() {
//		public void run() {
//		try { Thread.sleep(3000); } catch(Exception e) {}
//		dialog.dismiss();
//		}
//		});
		
		
		// DB 쓸거니까 초기화 해준다.
		 initDB();
		 
		 
		myQRcode = MyQRPageActivity.qrCode;			// 내 QR 코드. (확인용)
		
		// 크기 측정
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i(TAG, "screenWidth : " + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i(TAG, "screenHeight : " + screenHeight);
		if(screenWidth < screenHeight ){
	    	fImgSize = screenWidth;
	    }else{
	    	fImgSize = screenHeight;
	    }
		
		Log.i(TAG, myQRcode);		
		URL imageURL = null;							
		URLConnection conn = null;
		InputStream is= null;
		
		setContentView(R.layout.my_mileage);
		
		/* 구식 방법
		m_list = (ListView) findViewById(R.id.id_list);
		m_list.setOnItemClickListener(onItemClick);
		*/
		
		searched = false;		// ?
		
		if(isRunning<1){								// 이유가 있을것.??;
			isRunning = isRunning+1;
			try {
				myQRcode = MyQRPageActivity.qrCode;
				getMyMileageList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			Log.w(TAG, "already running..");
		}
	}

	
	/* 구식 방법을 사용하지 않음
	
	AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			// 실행문
//			Toast.makeText(MyMileagePageActivity.this, "터치터치"+arg2+"이곳은:"+entriesFn.get(arg2).getCheckMileageMerchantsMerchantID(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
			intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		// 가맹점 아이디
			intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());					// 고유 식별 번호
			intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());			// 가맹점에 대한 내 마일리지
			startActivity(intent);
		}
	};

*/
	
	
	
	
	/*
	 * 서버와 통신하여 내 마일리지 목록을 가져온다.
	 * 그 결과를 List<CheckMileageMileage> Object 로 반환 한다.
	 * 
	 * 보내는 정보 : 액티베이트Y, 내QR코드 스트링
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  받는 정보 : 가맹점 등록 이미지 , 가맹점 이름, 해당 가맹점에 대한 내 마일리지, 마지막 사용 일시, 
	 *  터치하면 가맹점 상세정보로 가야하기 때문에 키도 필요하다..
	 *  
	 * -----------------------------------
	 * |[이미지 상]  [가맹점 이름]  [내 포인트] |
	 * |[이미지 하]	[ 가 맹 점 이 용 시 각 ]    |  전번. 
	 * ------------------------------------
	 */
	public void getMyMileageList() throws JSONException, IOException {
//		Log.i(TAG, "getMyMileageList");
		if(CheckNetwork()){
			controllerName = "checkMileageMileageController";
			methodName = "selectMemberMerchantMileageList";
			showPb();
			
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 자신의 아이디를 넣어서 조회
								obj.put("activateYn", "Y");
								obj.put("checkMileageMembersCheckMileageId", myQRcode);
								Log.i(TAG, "myQRcode::"+myQRcode);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMileage\":" + obj.toString() + "}";
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
//								System.out.println("postUrl      : " + postUrl2);
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
								theData1(in);
							}catch(Exception e){ 
								// 다시?
								if(reTry>0){
									Log.w(TAG, "fail and retry remain : "+reTry);
									reTry = reTry-1;
									try {
										Thread.sleep(500);
										getMyMileageList();
									} catch (Exception e1) {
										Log.w(TAG,"again is failed() and again... ;");
									}	
								}else{
									Log.w(TAG,"reTry failed - init reTry");
									//e.printStackTrace();
									reTry = 5;
									hidePb();
									isRunning = isRunning-1;
									getDBData();						// 5회 재시도에도 실패하면 db에서 꺼내서 보여준다.
								}
								
//								// 에러니까 로딩바 없애고 다시 할수 있도록
//								new Thread(
//										new Runnable(){
//											public void run(){
//												Message message = handler.obtainMessage();
//												Bundle b = new Bundle();
//												b.putInt("order", 2);
//												message.setData(b);
//												handler.sendMessage(message);
//											}
//										}
//								).start();
//								isRunning = 0;
							}
						}
					}
			).start();
		}else{
			isRunning = isRunning-1;		// 돌려놔.
		}
	}

	/*
	 * 일단 마일리지 목록 결과를 받음. (가맹점 정보는 없이 아이디만 들어있는 상태) -- 1차 검색 결과 처리부
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
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
				entries = new ArrayList<CheckMileageMileage>(max);
				String tmp_idCheckMileageMileages = "";
				String tmp_mileage = "";
				String tmp_modifyDate = "";
				String tmp_shortDate = "";
				String tmp_checkMileageMembersCheckMileageId = "";
				String tmp_checkMileageMerchantsMerchantId = "";
				String tmp_companyName = "";
				String tmp_introduction = "";		//prstr = jsonobj2.getString("introduction");		// prSentence --> introduction
				String tmp_workPhoneNumber = "";
				String tmp_profileThumbnailImageUrl = "";
//				String tmp_profileImageUrl = "";
//				String tmp_ = "";
				Bitmap bm = null;
//				Bitmap bm2 = null;
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						doneCnt++;
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
						
						tmp_idCheckMileageMileages = jsonObj.getString("idCheckMileageMileages");
						try{
							tmp_mileage = jsonObj.getString("mileage");
						}catch(Exception e){
							tmp_mileage = "0";
						}
						try{
							tmp_modifyDate = jsonObj.getString("modifyDate");
							String tmpstr = getString(R.string.last_update);
							if(tmp_modifyDate.length()>9){
								tmp_shortDate = tmp_modifyDate.substring(0, 10);
								tmp_modifyDate = tmp_shortDate;
							}
							tmp_modifyDate = tmpstr+":"+tmp_modifyDate;
						}catch(Exception e){
							tmp_modifyDate = "";
						}
						try{
							tmp_checkMileageMembersCheckMileageId = jsonObj.getString("checkMileageMembersCheckMileageId");
						}catch(Exception e){
							tmp_checkMileageMembersCheckMileageId = "";
						}
						try{
							tmp_checkMileageMerchantsMerchantId = jsonObj.getString("checkMileageMerchantsMerchantId");
						}catch(Exception e){
							tmp_checkMileageMerchantsMerchantId = "";
						}
						try{  
							tmp_introduction = jsonObj.getString("introduction");
						}catch(Exception e){
							tmp_introduction = "";
						}
						try{
							tmp_companyName = jsonObj.getString("companyName");
						}catch(Exception e){
							tmp_companyName = "";
						}
						try{
							tmp_workPhoneNumber = jsonObj.getString("workPhoneNumber");
						}catch(Exception e){
							tmp_workPhoneNumber = "";
						}
						try{
							tmp_profileThumbnailImageUrl = jsonObj.getString("profileThumbnailImageUrl");
						}catch(Exception e){
							tmp_profileThumbnailImageUrl = "";
						}
//						try{
//							tmp_profileImageUrl = jsonObj.getString("profileImageUrl");
//						}catch(Exception e){
//							tmp_profileImageUrl = "";
//						}
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
							dbSaveEnable = false;
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
							bm = dw.getBitmap();
						}
						// 가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다.
//						Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
						// bm 이미지 크기 변환 .
//						BitmapDrawable bmpResize = BitmapResizePrc(bm, fImgSize/5, fImgSize/5);  
//						bm2 = (bmpResize.getBitmap());
						entries.add(new CheckMileageMileage(tmp_idCheckMileageMileages,
								tmp_mileage,
								tmp_modifyDate,
								tmp_checkMileageMembersCheckMileageId,
								tmp_checkMileageMerchantsMerchantId,
								tmp_companyName,
								tmp_introduction,
								tmp_workPhoneNumber,
								tmp_profileThumbnailImageUrl,
//								bm2
								bm
								// 그 외 섬네일 이미지, 가맹점 이름
						));
						
					}
					//    			 2차 작업. 가맹점 이름, 이미지 가져와서 추가로 넣음.
					//    			 array 채로 넘기고 돌려받을수 있도록 한다..
				}
			}catch (JSONException e) {
				doneCnt--;
				dbSaveEnable = false;
				e.printStackTrace();
			}finally{
//				entriesFn = entries;								// db 처리 위해 임시 주석 *** 
				dbInEntries = entries; 
				reTry = 5;				// 재시도 횟수 복구
				searched = true;
//				showInfo();											// db 처리 위해 임시 주석 *** 
				// db 에 데이터를 넣는다.
				try{
					if(dbSaveEnable){		// 이미지까지 성공적으로 가져온 경우.
						saveDataToDB();
					}else{
						alertToUser();		// 이미지 가져오는데 실패한 경우.
						// 어쨎든 처리가 끝나면 (공통) -  db를 검사하여 데이터가 있으면 보여주고 없으면 말고... entriesFn = dbOutEntries
					}	// 처리가 끝나면 공통으로 해야할 showInfo(); (그전에 entriesFn 설정 한다)
				}catch(Exception e){}
				finally{
					getDBData();			//db 에 잇으면 그거 쓰고 없으면 없다고 알림. * 에러나면 이전 데이터를 보여주기 때문에 db에 있는 정보가 정확하다고 볼수는 없음.. 
				}
			}
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지. -- 토스트는 에러남
			showMSG();
//			Toast.makeText(MyMileagePageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void alertToUser(){				// 	data 조회가 잘 안됐어요.
		Log.d(TAG,"Get Data from Server -> Error Occured..");
		
	}
	
	
	
	
	// 가맹점 아이디로 가맹점 정보 가져오기. .. Array채로 주고 받기..  -- 2차 검색  -- > 2차 검색 없앨 거.. 
	public void getMerchantInfo(final List<CheckMileageMileage> entries3, final int max){
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
//		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMileage> entries2 = new ArrayList<CheckMileageMileage>(max);
		final int max2 = max;
		// 각각에 대해서 돌린다.
		new Thread(
				new Runnable(){
					public void run(){
						
						for (int j = 0; j < max2; j++ ){
							// 가맹점 아이디를 꺼낸다.
							final String merchantId2 = entries3.get(j).getCheckMileageMerchantsMerchantID();
							// 요청할 문자열을 만들기 위함. (json 방식으로 보내기 위해 생성)
							JSONObject obj = new JSONObject();
							try{
								// 보낼 데이터 세팅
								obj.put("activateYn", "Y");
								obj.put("merchantId", merchantId2);
//								Log.i(TAG, "merchantId::"+merchantId2);
							}catch(Exception e){
								e.printStackTrace();
							}
							// 보낼 문자열. (위의 json 방식의 오브젝트를 문자열로)
							String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
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
//								System.out.println("postUrl      : " + postUrl2);				
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								InputStream in =  connection2.getInputStream();
								// 가맹점 아이디로 가맹점 정보를 가져온걸 처리..저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디. + 가맹점 이름, 가맹점 이미지 URL
								BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);	
								StringBuilder builder = new StringBuilder();
								String line =null;
								while((line=reader.readLine())!=null){
									builder.append(line).append("\n");
								}
//								Log.d(TAG,"수신::"+builder.toString());
								String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
								JSONObject jsonObject;	// 1차로 받은거.
								JSONObject jsonObject2;	// 1차로 받은거중 "가맹점아이디" 으로 꺼낸거. --> 여기서 스트링으로 값 하나씩 꺼낸다.
								if(connection2.getResponseCode()==200 || connection2.getResponseCode()==204){		// 요청 성공시
									jsonObject = new JSONObject(tempstr);
									jsonObject2 = jsonObject.getJSONObject("checkMileageMerchant");
									// 가맹점 이름.
									newMerchantName = jsonObject2.getString("companyName");			// 특정 글자수 초과시 뒤에 자르고 ... 붙인다.
									if(newMerchantName.length()>merchantNameMaxLength){
										newMerchantName = newMerchantName.substring(0,merchantNameMaxLength-2);		// 최대 글자수 -2 만큼 자르고 ... 붙인다.
										newMerchantName = newMerchantName + "...";
										entries3.get(j).setMerchantName(newMerchantName);
									}else{
										entries3.get(j).setMerchantName(newMerchantName);
									}
									entries3.get(j).setMerchantName(jsonObject2.getString("companyName"));// 가맹점 정보를 받는다. 이름
									
									// 전번.
									if(jsonObject2.getString("workPhoneNumber")==null || jsonObject2.getString("workPhoneNumber").length()<1){	// 가맹점 정보를 받는다. 전번
										entries3.get(j).setWorkPhoneNumber("");// 가맹점 정보를 받는다. 전번
									}else{
										entries3.get(j).setWorkPhoneNumber("(☎)"+jsonObject2.getString("workPhoneNumber"));
									}
									// 가맹점 URL
									Bitmap bm = null;
									// 가맹점 이미지 URL 저장한다. -- 이미지까지 생성
									try{
										entries3.get(j).setMerchantImg(jsonObject2.getString("profileThumbnailImageUrl"));				// 가맹점 이미지 URL     profileImageUrl --> profileThumbnailImageUrl
									}catch(Exception e){
										entries3.get(j).setMerchantImg("");
									}
									if(entries3.get(j).getMerchantImg()!=null && entries3.get(j).getMerchantImg().length()>0){
										if(entries3.get(j).getMerchantImg().contains("http")){
											try{
												bm = LoadImage(entries3.get(j).getMerchantImg());				 
											}catch(Exception e3){}
										}else{
											try{
												bm = LoadImage(imgthumbDomain+entries3.get(j).getMerchantImg());				 
											}catch(Exception e3){
												Log.w(TAG, imgthumbDomain+entries3.get(j).getMerchantImg()+" -- fail");
											}
										}
									}else{
										BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
										bm = dw.getBitmap();
									}
									if(bm==null){
										BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
										bm = dw.getBitmap();
									}
									// 가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다.
//									Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
									// bm 이미지 크기 변환 .
//									BitmapDrawable bmpResize = BitmapResizePrc(bm, fImgSize/4, fImgSize/4);  
//									entries3.get(j).setMerchantImage(bmpResize.getBitmap());
									entries3.get(j).setMerchantImage(bm);
								}
							}catch(Exception e){ 
								// Re Try
								if(reTry>0){
									Log.w(TAG,"failed, retry all again remain retry : "+reTry);
									reTry = reTry -1;
									try{
										Thread.sleep(300);
										getMerchantInfo(entries3, max);		// 재시도?
									}catch(Exception e2){}
								}else{
									Log.w(TAG,"reTry failed. -- init reTry");
									reTry = 5;			// 진행 ??
								}
							}
						}		// for문 종료
//						Log.d(TAG,"가맹점 정보 수신 완료. ");
						entriesFn = entries3;
						showInfo();					// ? 실패시에도 되나..?
					}
				}
		).start();
	}

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.		-- 2차 처리.
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

	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
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

	/*
	 * Bitmap 이미지 리사이즈
	 * Src : 원본 Bitmap
	 * newHeight : 새로운 높이
	 * newWidth : 새로운 넓이
	 * 참고 소스 : http://skyswim42.egloos.com/3477279 ( webview 에서 capture 화면 resizing 하는 source 도 있음 )
	 */
//	private BitmapDrawable BitmapResizePrc( Bitmap Src, float newHeight, float newWidth)
//	{
//		BitmapDrawable Result = null;
//		int width = Src.getWidth();
//		int height = Src.getHeight();
//
//		// calculate the scale - in this case = 0.4f
//		float scaleWidth = ((float) newWidth) / width;
//		float scaleHeight = ((float) newHeight) / height;
//
//		// createa matrix for the manipulation
//		Matrix matrix = new Matrix();
//
//		// resize the bit map
//		matrix.postScale(scaleWidth, scaleHeight);
//
//		// rotate the Bitmap 회전 시키려면 주석 해제!
//		//matrix.postRotate(45);
//
//		// recreate the new Bitmap
//		Bitmap resizedBitmap = Bitmap.createBitmap(Src, 0, 0, width, height, matrix, true);
//
//		// check
//		width = resizedBitmap.getWidth();
//		height = resizedBitmap.getHeight();
////		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );
//
//		// make a Drawable from Bitmap to allow to set the BitMap
//		// to the ImageView, ImageButton or what ever
//		Result = new BitmapDrawable(resizedBitmap);
//		return Result;
//	}
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		
		if(!searched){
			Log.w(TAG,"onResume, search");
			if(dontTwice==0){
				if(isRunning<1){
					isRunning = isRunning+1;
					try {
						myQRcode = MyQRPageActivity.qrCode;
						getMyMileageList();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					Log.w(TAG, "already running..");
				}
			}else{
				dontTwice = 0;
			}
		}
	}

	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
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
	      case Menu. FIRST+1:
//	    	  Toast.makeText(MyMileagePageActivity.this, "123123", Toast.LENGTH_SHORT).show();
//	                Intent intent = new Intent(UserManagementActivity.this,AddUserActivity.class);        // example에서 이름
//	            Intent intent = new Intent(MainActivity.this ,AddUserActivity.class);
//	            startActivity(intent);
	    	  if(isRunning<1){
	  			isRunning = isRunning+1;
	  			try {
	  				myQRcode = MyQRPageActivity.qrCode;
	  				getMyMileageList();
	  			} catch (JSONException e) {
	  				e.printStackTrace();
	  			} catch (IOException e) {
	  				e.printStackTrace();
	  			}
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
//				AlertShow("Wifi 혹은 3G 망이 연결되지 않았거나 원할하지 않습니다. 네트워크 확인 후 다시 접속해 주세요.");
				AlertShow_networkErr();
				hidePb();
				isRunning = 0;
				connected = false;
			}else{
				connected = true;
			}
			return connected;
		}
		public void AlertShow_networkErr(){		//R.string.network_error
			AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
			alert_internet_status.setTitle("Warning");
			alert_internet_status.setMessage(R.string.network_error);
			String tmpstr = getString(R.string.closebtn);
			alert_internet_status.setPositiveButton(tmpstr, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
//					finish();
				}
			});
			alert_internet_status.show();
		}
}
