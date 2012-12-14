package kr.co.bettersoft.checkmileage.activities;
// �� ���ϸ��� ���� ȭ��


/*
 * �ƴ��͸� �����Ÿ� �Ἥ ������ �ö����� getView �Ѵ�.. ���߿� ���ľ� �ڴ�..
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

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.adapters.MyMileageListAdapter;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMileage;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;


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
	int app_end = 0;	// �ڷΰ��� ��ư���� ������ 2������ ��������
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int dontTwice = 1;
	
	int responseCode = 0;
	String TAG = "MyMileagePageActivity";
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img �����ö� ���ϸ� ���� ��� �տ� ���� ������.   
	public List<CheckMileageMileage> entries;	// 1�������� ��ȸ�� ���. (������ �� ���� ����)
	public List<CheckMileageMileage> dbInEntries;	// db�� ���� ��
	public List<CheckMileageMileage> dbOutEntries;	// db���� ������
	Boolean dbSaveEnable = true;
	
	public static Boolean searched = false;		// ��ȸ �ߴ°�?
	
	URL postUrl2;
	HttpURLConnection connection2;
	int reTry = 1;		// ��õ� Ƚ��
	
	int merchantNameMaxLength = 9;			// �������� ǥ�õ� �ִ� ���ڼ�.
	String newMerchantName="";
	
	public boolean connected = false;  // ���ͳ� �������
	
	List<CheckMileageMileage> entriesFn = null;
	float fImgSize = 0;
	int isRunning = 0;
	
	View emptyView;
	
	// �����
	ProgressBar pb1;
	
	
	
	/*
	 * ����� sqlite �� ����Ͽ� �� ���ϸ��� ����� �޾ƿͼ� ����. 
	 * ���� ��� �Ұ��϶� ���������� ������ �����͸� �����ش�.
	 * ������ ����.. 
	 * tmp_idCheckMileageMileages  / tmp_mileage  / tmp_modifyDate  / tmp_checkMileageMembersCheckMileageId  / 
	 * tmp_checkMileageMerchantsMerchantId  / tmp_companyName  / tmp_introduction  / tmp_workPhoneNumber  / tmp_profileThumbnailImageUrl  / bm
	 * 
	 * ��� ���н� �˸�â�� ����ش�.
	 * ��� ������ ���� db ���̺��� ����� ���� ���̺��� ���� �����͸� �־��ش�.
	 * 
	 * ��� ���� ���ο� ������� db ���̺��� �ְ� �����Ͱ� ������ �ش� �����͸� �����ش�.
	 */
	////----------------------- SQLite  Query-----------------------//
	
	// ���̺� ���� ���� ---> ���̺��� init ���� �̹� ��������� ���� ���븸 �����...�ٽ� ����
	private static final String Q_INIT_TABLE = "DELETE FROM mileage_info;" ;

	// ���̺� ���� ����.
	private static final String Q_CREATE_TABLE = "CREATE TABLE mileage_info (" +
	       "_id INTEGER PRIMARY KEY AUTOINCREMENT," +					// ����� db ����Ǵ� �ڵ�����  �ε��� Ű
	       "idCheckMileageMileages TEXT," +								// ���� db�� ����� �ε��� Ű
	       "mileage TEXT," +											// ���ϸ��� ��
	       "modifyDate TEXT," +											// �����Ͻ�
	       "checkMileageMembersCheckMileageId TEXT," +					// ����� ���̵�
	       "checkMileageMerchantsMerchantId TEXT," +					// ������ ���̵�
	       "companyName TEXT," +										// ������ �̸�
	       "introduction TEXT," +										// ������ �Ұ���
	       "workPhoneNumber TEXT," +									// ������ ����
	       "profileThumbnailImageUrl TEXT," +							// ������ �̹��� url
	       "bm TEXT" +													// ������ �̹���(stringȭ ��Ų ��)
	       ");" ;
	
	// ���̺� ��ȸ ����
	private final String Q_GET_LIST = "SELECT * FROM mileage_info";
	
	
	//----------------------- SQLite  Query-----------------------////
	
	
	//----------------------- SQLite -----------------------//
	
	// �ʱ�ȭ�۾�- db �� ���̺� �˻��ϰ� ������ �����.
	SQLiteDatabase db = null;
	public void initDB(){
		Log.i(TAG,"initDB");
		// db ���� �۾� �ʱ�ȭ, DB ���� SQLiteDatabase �ν��Ͻ� ����          db ���ų� ������ ����
	     if(db== null ){
	          db= openOrCreateDatabase( "sqlite_carrotDB.db", SQLiteDatabase.CREATE_IF_NECESSARY ,null );
	    }
	     // ���̺��� ������ �������� �� ���̺� ���� Ȯ�� ������ ����.
	      checkTableIsCreated(db);
	}
	public void checkTableIsCreated(SQLiteDatabase db){		// mileage_info ��� �̸��� ���̺��� �˻��ϰ� ������ ����.
		Log.i(TAG, "checkTableIsCreated");
		try{
//			Cursor c = db.query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
			Cursor c = db.query("sqlite_master" , new String[] {"count(*)"}, "name=?" , new String[] {"mileage_info"}, null ,null , null);
		      Integer cnt=0;
		      c.moveToFirst();                                 // Ŀ���� ù�������� �ű�
		       while(c.isAfterLast()== false ){                   // ������ ������ �ɶ����� 1�� �����ϸ鼭 ����
		            cnt=c.getInt(0);
		            c.moveToNext();
		      }
		       //Ŀ���� ��� ���� �ݴ´�
		      c.close();
		       //���̺� ������ ����
		       if(cnt==0){
		            db.execSQL(Q_CREATE_TABLE);
		      }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// server���� ���� data�� db��
	public void saveDataToDB(){			//	db ���̺��� �ʱ�ȭ �� �� �����͸� �ֽ��ϴ�.	  // oncreate()���� ���̺� �˻��ؼ� ������� ������ ���� ���� �������� �ʴ´�.
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
					initialValues = new ContentValues(); 			// ������ �־��. ��� ����. ������ ���°Ŷ�...  --> ������ ������
					initialValues.put("idCheckMileageMileages", dbInEntries.get(i).getIdCheckMileageMileages()); 
					initialValues.put("mileage", dbInEntries.get(i).getMileage()); 
					initialValues.put("modifyDate", dbInEntries.get(i).getModifyDate()); 
					initialValues.put("checkMileageMembersCheckMileageId", dbInEntries.get(i).getCheckMileageMembersCheckMileageID()); 
					initialValues.put("checkMileageMerchantsMerchantId", dbInEntries.get(i).getCheckMileageMerchantsMerchantID()); 
					initialValues.put("companyName", dbInEntries.get(i).getMerchantName()); 
					initialValues.put("introduction", dbInEntries.get(i).getIntroduction()); 
					initialValues.put("workPhoneNumber", dbInEntries.get(i).getWorkPhoneNumber()); 
					initialValues.put("profileThumbnailImageUrl", dbInEntries.get(i).getMerchantImg()); 		
					// img �� ���ڿ��� �ٲ㼭 �ִ´�. ������ ������.			 // BMP -> ���ڿ� 		
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
	
	
	// db �� ����� �����͸� ȭ�鿡
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
			// ��ȸ
			Cursor c = db.rawQuery( Q_GET_LIST, null );
			if(c.getCount()==0){
				Log.i(TAG, "saved mileage data NotExist");
			}else{
				Log.i(TAG, "saved mileage data Exist");				// ������ ������ ������ �����.			// ������ ������
				dbOutEntries = new ArrayList<CheckMileageMileage>(c.getCount());		// ������ŭ �����ϱ�.
				c.moveToFirst();                                 // Ŀ���� ù�������� �ű�
				while(c.isAfterLast()== false ){                   // ������ ������ �ɶ����� 1�� �����ϸ鼭 ����
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
			 entriesFn = dbOutEntries;						//  *** ���� �����͸� ��� �����Ϳ� ���� 
		}catch(Exception e){e.printStackTrace();}
		showInfo();									//  *** ��� �����͸� ȭ�鿡 �����ش�.		 ������ �ִ��� ���δ� ��� ó������ �Բ�..
	}
	////---------------------SQLite ----------------------////
	
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. 
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
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// ���׹� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���׹� ����
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	ListView listView;
	
	public Context returnThis(){
		return this;
	}
	// ����� ���� / ����
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
	public void showMSG(){			// ȭ�鿡 �佺Ʈ ���..
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
	
	// ����Ʈ �����ְ� Ŭ�� �̺�Ʈ ��� (������ �� ����)
	public void setListing(){
		listView  = (ListView)findViewById(R.id.listview);
		listView.setAdapter(new MyMileageListAdapter(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// ������ ���̵�
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// ���� �ĺ� ��ȣ. (�󼼺��� ��ȸ�뵵)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// �� ���ϸ���    // �������� ���� �� ���ϸ���
//				// img �� ���ڿ��� �ٲ㼭 �ִ´�. ������ ������.			 // BMP -> ���ڿ� 		
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();   
//				String bitmapToStr = "";
//				entriesFn.get(position).getMerchantImage().compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
//				byte[] b = baos.toByteArray();  
//				bitmapToStr = Base64.encodeToString(b, Base64.DEFAULT); 
//				intent.putExtra("imageFileStr", bitmapToStr);	
				startActivity(intent);
			}
		});
	}
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pb1 = (ProgressBar) findViewById(R.id.ProgressBar01);
		
		// DB ���Ŵϱ� �ʱ�ȭ ���ش�.
		 initDB();
		 
		myQRcode = MyQRPageActivity.qrCode;			// �� QR �ڵ�. 
		
		// ũ�� ����
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
		
		setContentView(R.layout.my_mileage);
		
		searched = false;		 
		
		if(isRunning<1){								// ���� ���� ����. 
			isRunning = isRunning+1;
				myQRcode = MyQRPageActivity.qrCode;
				new backgroundGetMyMileageList().execute();	// �񵿱�. �����κ��� ���ϸ��� ����Ʈ ��ȸ
		}else{
			Log.w(TAG, "already running..");
		}
	}

	
	
	// �񵿱�� ���ϸ��� ��� �������� �Լ� ȣ��.
	public class backgroundGetMyMileageList extends   AsyncTask<Void, Void, Void> {
        @Override protected void onPostExecute(Void result) { 
       }
        @Override protected void onPreExecute() { 
       }
        @Override protected Void doInBackground(Void... params) { 
        	Log. d(TAG,"backgroundGetMyMileageList");
        	showPb();
			try {
				getMyMileageList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	return null ;
        }
 }

	
	/*
	 * ������ ����Ͽ� �� ���ϸ��� ����� �����´�.
	 * �� ����� List<CheckMileageMileage> Object �� ��ȯ �Ѵ�.
	 * 
	 * ������ ���� : ��Ƽ����ƮY, ��QR�ڵ� ��Ʈ��
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  �޴� ���� : ������ ��� �̹��� , ������ �̸�, �ش� �������� ���� �� ���ϸ���, ������ ��� �Ͻ�, 
	 *  ��ġ�ϸ� ������ �������� �����ϱ� ������ Ű�� �ʿ��ϴ�..
	 *  
	 * -----------------------------------
	 * |[�̹��� ��]  [������ �̸�]  [�� ����Ʈ] |
	 * |[�̹��� ��]	[ �� �� �� �� �� �� �� ]    |  ����. 
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
								// �ڽ��� ���̵� �־ ��ȸ
								obj.put("activateYn", "Y");
								obj.put("checkMileageMembersCheckMileageId", myQRcode);
								Log.i(TAG, "myQRcode::"+myQRcode);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMileage\":" + obj.toString() + "}";
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								Thread.sleep(200);
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
//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
//								os2.close();
								// ��ȸ�� ����� ó��.
								theData1(in);
								connection2.disconnect();
							}catch(Exception e){ 
								// �ٽ�
								e.printStackTrace();
								connection2.disconnect();
								if(reTry>0){
									Log.w(TAG, "fail and retry remain : "+reTry);
									reTry = reTry-1;
									try {
										Thread.sleep(200);
										getMyMileageList();
									} catch (Exception e1) {
										Log.w(TAG,"again is failed() and again... ;");
									}	
								}else{
									Log.w(TAG,"reTry failed - init reTry");
									reTry = 1;
									hidePb();
									isRunning = isRunning-1;
									getDBData();						// 5ȸ ��õ����� �����ϸ� db���� ������ �����ش�.
								}
							}
						}
					}
			).start();
		}else{
			isRunning = isRunning-1;		// �۾����� ī����-1
		}
	}

	/*
	 * �ϴ� ���ϸ��� ��� ����� ����. (������ ������ ���� ���̵� ����ִ� ����) -- 1�� �˻� ��� ó����
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
//		Log.d(TAG,"����::"+builder.toString());
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
						doneCnt++;
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
						
						tmp_idCheckMileageMileages = jsonObj.getString("idCheckMileageMileages");
						try{
							tmp_mileage = jsonObj.getString("mileage");
						}catch(Exception e){
							tmp_mileage = "0";
						}
						try{
							tmp_modifyDate = jsonObj.getString("modifyDate");
//							Log.d(TAG,"tmp_modifyDate:"+tmp_modifyDate);
							String tmpstr = getString(R.string.last_update);
							if(tmp_modifyDate.length()>9){
//								tmp_shortDate = tmp_modifyDate.substring(0, 10);
//								tmp_modifyDate = tmp_shortDate;
//								Log.d(TAG,"tmp_modifyDate.substring(0, 4):"+tmp_modifyDate.substring(0, 4)+"//tmp_modifyDate.substring(5, 7):"+tmp_modifyDate.substring(5, 7)+"//tmp_modifyDate.substring(8, 10):"+tmp_modifyDate.substring(8, 10));
								tmpstr2 = tmp_modifyDate.substring(0, 4)+ getString(R.string.year) 		// ��
								+ tmp_modifyDate.substring(5, 7)+ getString(R.string.month) 					// ��
								+ tmp_modifyDate.substring(8, 10)+ getString(R.string.day) 					// ��
//								+ tmp_modifyDate.substring(0, 4)+ getString(R.string.year)					// ��
//								+ tmp_modifyDate.substring(0, 4)+ getString(R.string.year)					// ��
								;
								tmp_modifyDate = tmpstr2;
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
						// tmp_profileThumbnailImageUrl ������.
						if(tmp_profileThumbnailImageUrl!=null && tmp_profileThumbnailImageUrl.length()>0){
							if(tmp_profileThumbnailImageUrl.contains("http")){		// url ������ ���
								try{
									bm = LoadImage(tmp_profileThumbnailImageUrl);				 
								}catch(Exception e3){}
							}else{		// url �������� ������ �ٿ��ش�.
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
						if(bm==null){		//  ������.. 
//							dbSaveEnable = false;
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
//								bm2
								bm
								// �� �� ������ �̹���, ������ �̸�
						));
					}
				}
			}catch (JSONException e) {
				doneCnt--;
				dbSaveEnable = false;
				e.printStackTrace();
			}finally{
				dbInEntries = entries; 
				reTry = 1;				// ��õ� Ƚ�� ����
				searched = true;
				// db �� �����͸� �ִ´�.
				try{
					if(dbSaveEnable){		// �̹������� ���������� ������ ���.
						saveDataToDB();
					}else{
						alertToUser();		// �̹��� �������µ� ������ ���.
						// ��e�� ó���� ������ (����) -  db�� �˻��Ͽ� �����Ͱ� ������ �����ְ�  entriesFn = dbOutEntries
					}	// ó���� ������ �������� �ؾ��� showInfo(); (������ entriesFn ���� �Ѵ�)
				}catch(Exception e){}
				finally{
					getDBData();			//db �� ������ �װ� ���� ������ ���ٰ� �˸�. * �������� ���� �����͸� �����ֱ� ������ db�� �ִ� ������ ��Ȯ�ϴٰ� ������ ����.. 
				}
			}
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����. -- �佺Ʈ�� ������
			showMSG();
//			Toast.makeText(MyMileagePageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void alertToUser(){				// 	data ��ȸ�� �� �ȵƾ��. // ���� �˸� ���� �α׸� ��´�.
		Log.d(TAG,"Get Data from Server -> Error Occured..");
	}
	
	
	
	

	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.		-- 2�� ó��.
	public void showInfo(){
		hidePb();
		//  ������ ������ ȭ�鿡 �����ֱ�.
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

	// ������ �̹��� URL ���� �̹��� �޾ƿͼ� �����ο� �����ϴ� �κ�.
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

	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		dbSaveEnable = true;
		if(!searched){
			Log.w(TAG,"onResume, search");
			if(dontTwice==0){
				if(isRunning<1){
					isRunning = isRunning+1;
						myQRcode = MyQRPageActivity.qrCode;
						new backgroundGetMyMileageList().execute();
				}else{
					Log.w(TAG, "already running..");
				}
			}else{
				dontTwice = 0;
			}
		}
	}

	
	
	
	
	
	
	/*
	 *  �ݱ� ��ư 2�� ������ ���� ��.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.w(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
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
	
	
	////////////////////////   �ϵ���� �޴� ��ư.  ////////////////
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
		 String tmpstr = getString(R.string.refresh);
	        menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, tmpstr );             // �űԵ�� �޴� �߰�.
//	          getMenuInflater().inflate(R.menu.activity_main, menu);
	        return (super .onCreateOptionsMenu(menu));
	    }
	   
	 
	    // �ɼ� �޴� Ư�� ������ Ŭ���� �ʿ��� �� ó��
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item){
	      return (itemCallback(item)|| super.onOptionsItemSelected(item));
	    }
	   
	    // ������ ���̵� �� ���� �ʿ��� �� ó��
	    public boolean itemCallback(MenuItem item){
	      switch(item.getItemId()){
	      case Menu. FIRST+1:
	    	  if(isRunning<1){
	  			isRunning = isRunning+1;
	  				myQRcode = MyQRPageActivity.qrCode;
	  				new backgroundGetMyMileageList().execute();
	  		}else{
	  			Log.w(TAG, "already running..");
	  		}
	             return true ;
	      }
	      return false;
	    }
	
	////////////////////////////////////////////////////////////
	
	
	    /*
		 * ��Ʈ��ũ ���� ����
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
				getDBData();		// ��� �ȵǸ� db�� �����ֱ��..
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
			try{
				if(connection2!=null){
					connection2.disconnect();
				}
			}catch(Exception e){}
		}
}
