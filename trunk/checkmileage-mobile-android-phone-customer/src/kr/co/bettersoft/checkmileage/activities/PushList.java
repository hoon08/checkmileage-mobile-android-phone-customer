package kr.co.bettersoft.checkmileage.activities;
/*
 * ���������� ������ �̺�Ʈ ��Ϻ���. Ư�� ��ư ��ġ�Ͽ� �� ȭ������ �̵� ����.
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
	
	// ���� ��� ��
	int responseCode = 0;
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	String serverName = CommonUtils.serverNames;
	
	URL postUrl2;
	HttpURLConnection connection2;
	
//	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img �����ö� ���ϸ� ���� ��� �տ� ���� ������.  
//	String imgDomain = CommonUtils.imgDomain; 					// Img �����ö� ���ϸ� ���� ��� �տ� ���� ������.  
	String imgPushDomain = CommonUtils.imgPushDomain;			// Ǫ�� �̹��� ���� ������
	
	public List<CheckMileagePushEvent> entries;	// 1�������� ��ȸ�� ���. (������ �� ���� ����)
	public List<CheckMileagePushEvent> dbInEntries;	// db�� ���� ��
	public List<CheckMileagePushEvent> dbOutEntries;	// db���� ������
	
	// ���� ������ �ӽ� ����� -> ������ �ӽ� ���� �� �����ο� ����
	String tmp_subject = "";			
	String tmp_content = "";
	String tmp_imageFileUrl = "";
	String tmp_modifyDate = "";
	String tmp_companyName = "";
	Bitmap tmp_imageFile = null;
	
	Boolean dbSaveEnable = true;			// db ���� ���� ����
	public static Boolean searched = false;		// ��ȸ �ߴ°�?
	
	List<CheckMileagePushEvent> entriesFn = null;
	int isRunning = 0;						// �ߺ� ���� ����
	
	public boolean connected = false;  // ���ͳ� �������
	View emptyView;
	
	// �����
	ProgressBar pb1;
	
	int reTry = 3;
	
	/*
	 * ����� sqlite �� ����Ͽ� �� �̺�Ʈ ����� �޾ƿͼ� ����. 
	 * ���� ��� �Ұ��϶� ���������� ������ �����͸� �����ش�.
	 * 
	 * ��� ���н� �˸�â�� ����ش�.
	 * ��� ������ ���� db ���̺��� ����� ���� ���̺��� ���� �����͸� �־��ش�.
	 * 
	 * ��� ���� ���ο� ������� db ���̺��� �ְ� �����Ͱ� ������ �ش� �����͸� �����ش�.
	 */
	////----------------------- SQLite  Query-----------------------//
	
	// ���̺� ���� ���� ---> ���̺��� init ���� �̹� ��������� ���� ���븸 �����...�ٽ� ����
	private static final String Q_INIT_TABLE = "DELETE FROM push_event;" ;

	// ���̺� ���� ����.
	private static final String Q_CREATE_TABLE = "CREATE TABLE push_event (" +
	       "_id INTEGER PRIMARY KEY AUTOINCREMENT," +					// ����� db ����Ǵ� �ڵ�����  �ε��� Ű
	       "subject TEXT," +											// �̺�Ʈ ����
	       "content TEXT," +											// �̺�Ʈ �۱�
	       "imageFileUrl TEXT," +										// �̺�Ʈ�̹��� �ּ�
	       "modifyDate TEXT," +											// �̺�Ʈ �����
	       "companyName TEXT," +										// ��ü��
	       "imageFile TEXT" +											// �̹��� ����(����ȭ)
//	       "idCheckMileageMileages TEXT," +								// ���� db�� ����� �ε��� Ű				// ������ ������ ��..
//	       "mileage TEXT," +											// ���ϸ��� ��
//	       "modifyDate TEXT," +											// �����Ͻ�
//	       "checkMileageMembersCheckMileageId TEXT," +					// ����� ���̵�
//	       "checkMileageMerchantsMerchantId TEXT," +					// ������ ���̵�
//	       "companyName TEXT," +										// ������ �̸�
//	       "introduction TEXT," +										// ������ �Ұ���
//	       "workPhoneNumber TEXT," +									// ������ ����
//	       "profileThumbnailImageUrl TEXT," +							// ������ �̹��� url
//	       "bm TEXT" +													// ������ �̹���(stringȭ ��Ų ��)
	       ");" ;
	
	// ���̺� ��ȸ ����
	private final String Q_GET_LIST = "SELECT * FROM push_event";
	

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
			Cursor c = db.query("sqlite_master" , new String[] {"count(*)"}, "name=?" , new String[] {"push_event"}, null ,null , null);
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
	public void saveEventDataToDB(){			//	db ���̺��� �ʱ�ȭ �� �� �����͸� �ֽ��ϴ�.	  // oncreate()���� ���̺� �˻��ؼ� ������� ������ ���� ���� �������� �ʴ´�.
		Log.i(TAG, "saveEventDataToDB");
		try{
			db.execSQL(Q_INIT_TABLE);
			ContentValues initialValues = null;
			int entrySize = dbInEntries.size();
			if(entrySize>0){
				for(int i =0; i<entrySize; i++){
					initialValues = new ContentValues(); 			//  ������ ������
					initialValues.put("subject", dbInEntries.get(i).getSubject()); 
					initialValues.put("content", dbInEntries.get(i).getContent()); 
					initialValues.put("imageFileUrl", dbInEntries.get(i).getImageFileUrl()); 
					initialValues.put("modifyDate", dbInEntries.get(i).getModifyDate()); 
					initialValues.put("companyName", dbInEntries.get(i).getCompanyName()); 
					// img �� ���ڿ��� �ٲ㼭 �ִ´�. ������ ������.			 // BMP -> ���ڿ� 		
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
	
	
	// db �� ����� �����͸� ȭ�鿡
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
			// ��ȸ
			Cursor c = db.rawQuery( Q_GET_LIST, null );
			if(c.getCount()==0){
				Log.i(TAG, "saved event data NotExist");
			}else{
				Log.i(TAG, "saved event data Exist");				// ������ ������ ������ �����.			// ������ ������
				dbOutEntries = new ArrayList<CheckMileagePushEvent>(c.getCount());		// ������ŭ �����ϱ�.
				c.moveToFirst();                                 // Ŀ���� ù�������� �ű�
				while(c.isAfterLast()== false ){                   // ������ ������ �ɶ����� 1�� �����ϸ鼭 ����
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
//			 db.close();		// db �� �������� �ѹ� ����.
			 entriesFn = dbOutEntries;						//  *** ���� �����͸� ��� �����Ϳ� ���� 
		}catch(Exception e){e.printStackTrace();}
		showEventList();									//  *** ��� �����͸� ȭ�鿡 �����ش�.		 ������ �ִ��� ���δ� ��� ó������ �Բ�..
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
						emptyView = findViewById(R.id.push_list_empty2);
						listView  = (ListView)findViewById(R.id.push_list_listview);
						listView.setEmptyView(emptyView);
						listView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// ���׹� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���׹� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_list_ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){		// �Ϲ� ���� �佺Ʈ
					Toast.makeText(PushList.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showNetErrToast")==1){		// ��Ʈ��ũ ���� �佺Ʈ
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

	// ����â ���̱�/�����
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
	
	// ��ȸ�� �����͸� ȭ�鿡+ Ŭ���� �̺�Ʈ(��ȭ������)
	public void setListing(){
		listView  = (ListView)findViewById(R.id.push_list_listview);
		listView.setAdapter(new PushEventListAdapter(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(PushList.this, PushDetail.class);
				intent.putExtra("subject", entriesFn.get(position).getSubject());		// �̺�Ʈ ����
				intent.putExtra("content", entriesFn.get(position).getContent());		// �̺�Ʈ �۱�
				intent.putExtra("imageFileUrl", entriesFn.get(position).getImageFileUrl());		// �̺�Ʈ ���� �̹��� �ּ�
				intent.putExtra("imageFileStr", entriesFn.get(position).getImageFileStr());		// �̺�Ʈ ���� �̹��� ����ȭ
				intent.putExtra("modifyDate", entriesFn.get(position).getModifyDate());		// �̺�Ʈ ���� ��¥
				intent.putExtra("companyName", entriesFn.get(position).getCompanyName());		// �̺�Ʈ ��ü��
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
		// DB ���Ŵϱ� �ʱ�ȭ ���ش�.
		 initDB();
		 
		myQRcode = MyQRPageActivity.qrCode;			// �� QR �ڵ�. 
		
		Log.i(TAG, myQRcode);		
		
		setContentView(R.layout.push_list);
		
		searched = false;		 
		
		if(isRunning<1){								// �ߺ� ���� ����
			isRunning = isRunning+1;
				myQRcode = MyQRPageActivity.qrCode;
				new backgroundGetMyEventList().execute();	// �̺�Ʈ ����Ʈ ��ȸ
		}else{
			Log.w(TAG, "already running..");
		}
	}


	// �񵿱�� �̺�Ʈ ��� �������� �Լ� ȣ��.
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
	 * �� �̺�Ʈ ��� �� �����´�.
	 * 
	 * ������ : checkMileageMerchantMarketing  
	 * ��Ʈ�ѷ� : checkMileageMerchantMarketingController
	 * �޼��� : selectMemberMerchantMarketingList
	 * ������ �Ķ���� : checkMileageId  activateYn
	 * �޴� ������ : List<CheckMileageMerchantMarketing>
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
								// �ڽ��� ���̵� �־ ��ȸ
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
//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
//								os2.close();
								// ��ȸ�� ����� ó��.
								getMyEventListResult(in);
								connection2.disconnect();
							}catch(Exception e){ 
								connection2.disconnect();
								// �ٽ�
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
//									getEventDBData();						// nȸ ��õ����� �����ϸ� db���� ������ �����ش�.
//								}
							}
						}
					}
			).start();
		}else{
			isRunning = isRunning-1;		// �۾����� ī���ø� �ٽ� �ǵ��� -1
		}
	}

	 // �̺�Ʈ ��ȸ ����� ó���ϴ� �κ�
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
		Log.d(TAG,"����::"+builder.toString());
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
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
						
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
						// tmp_imageFileUrl ������.
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
						if(tmp_imageFile==null){		//  ������.. 
							Log.d(TAG,"last tmp_imageFileUrl null");
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_320_240);
							tmp_imageFile = dw.getBitmap();
						}
						entries.add(new CheckMileagePushEvent(
								tmp_subject,  tmp_content,
								tmp_imageFileUrl,  tmp_modifyDate,
								tmp_companyName,  "",
								tmp_imageFile
								// �� �� ������ �̹���, ������ �̸�
						));
						
					}
				}
			}catch (JSONException e) {
				doneCnt--;
//				dbSaveEnable = false;
				e.printStackTrace();
			}finally{
				dbInEntries = entries; 
				reTry = 3;				// ��õ� Ƚ�� ����
				searched = true;
				// db �� �����͸� �ִ´�.
				try{
					if(dbSaveEnable){		// �̹������� ���������� ������ ���.
						saveEventDataToDB();
					}else{
						alertToUser();		// �̹��� �������µ� ������ ���.
						// ��e�� ó���� ������ (����) -  db�� �˻��Ͽ� �����Ͱ� ������ �����ְ�  entriesFn = dbOutEntries
					}	// ó���� ������ �������� �ؾ��� showInfo(); (������ entriesFn ���� �Ѵ�)
				}catch(Exception e){}
				finally{
					getEventDBData();			//db �� ������ �װ� ���� ������ ���ٰ� �˸�. * �������� ���� �����͸� �����ֱ� ������ db�� �ִ� ������ ��Ȯ�ϴٰ� ������ ����.. 
				}
			}
		}else{			// ��û ���н�	 �佺Ʈ�� ������ - 
			showMSG();    // �ڵ鷯 ���� �佺Ʈ
		}
	}
	
	public void alertToUser(){				// 	data ��ȸ�� �� �ȵƾ��. -- �α׳���
		Log.d(TAG,"Get Data from Server -> Error Occured..");
		
	}
	
	

	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.		-- 2�� ó��.
	public void showEventList(){
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

	// �̺�Ʈ �̹��� : URL ���� �̹��� �޾ƿͼ� �����ο� �����ϴ� �κ�.
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

	
	////////////////////////   �ϵ���� �޴� ��ư.  ////////////////
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
		 String tmpstr = getString(R.string.refresh);
	        menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, tmpstr );             // �űԵ�� �޴� �߰�. -- ���ΰ�ħ
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
	  				new backgroundGetMyEventList().execute();		// ��ȸ --> ���ΰ�ħ ���
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
				getEventDBData();		// ��� �ȵǸ� db�� �����ֱ��..
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
