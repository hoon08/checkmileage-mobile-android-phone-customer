package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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
	
	
	int reTry = 5;
	
	int merchantNameMaxLength = 9;			// �������� ǥ�õ� �ִ� ���ڼ�.
	String newMerchantName="";
	
	public boolean connected = false;  // ���ͳ� �������
	
	/*  ���� ��� ��� ����
	private ArrayAdapter<String> m_adapter = null;
	private ListView m_list = null;
	ArrayAdapter<CheckMileageMileage> adapter = null;
	MyAdapter mAdapter;
*/ 

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
	
	// ���̺� ���� ���� ---> ���̺��� �̴ֿ��� �̹� ��������� ���� ���븸 �����...�ٽ� ����
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
//			Log.i(TAG, Integer.toString(c.getCount()));			// qr img
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
					/* ���� ���
					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);
					*/
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
	
	
	public void setListing(){
		listView  = (ListView)findViewById(R.id.listview);
		listView.setAdapter(new ImageAdapterList(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
//				Log.i(TAG, "checkMileageMerchantsMerchantID::"+entriesFn.get(position).getCheckMileageMerchantsMerchantID());
//				Log.i(TAG, "myMileage::"+entriesFn.get(position).getMileage());
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// ������ ���̵�
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// ���� �ĺ� ��ȣ. (�󼼺��� ��ȸ�뵵)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// �� ���ϸ���    // �������� ���� �� ���ϸ���
				startActivity(intent);
			}
		});
	}
	
	/*   // ���� ����� ������� ����.
	// ����� Ŭ����. �̰����� ���� �����͸� �� ���̵� ���� �����Ѵ�.
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
			ImageView leftImg = (ImageView)convertView.findViewById(R.id.merchantImage);		// ������ �̹��� �ְ�
			// set the Drawable on the ImageView
			if(myDataArr.get(position).getMerchantImage()!=null){
				BitmapDrawable bmpResize = BitmapResizePrc(myDataArr.get(position).getMerchantImage(), fImgSize/2, fImgSize/2);  
				leftImg.setImageDrawable(bmpResize);	
			}
				
//			leftImg.setImageBitmap(myDataArr.get(position).getMerchantImage());			
			
			TextView nameTv = (TextView)convertView.findViewById(R.id.merchantName);			// ������ �̸� �ְ�
			nameTv.setText(myDataArr.get(position).getMerchantName());
			TextView mileage = (TextView)convertView.findViewById(R.id.mileage);				// �������� ���� �� ���ϸ��� �ְ�		.. �� ������ ������ �Ʒ��� �߰�, XML ���Ͽ��� �� ���..
			mileage.setText(myDataArr.get(position).getMileage()+"��");					
			
			TextView workPhone = (TextView)convertView.findViewById(R.id.merchantPhone);				// ������ ����.
			workPhone.setText(myDataArr.get(position).getWorkPhoneNumber());		
			
//			Button btn = (Button)convertView.findViewById(R.id.sendBtn);		// �ϴ� ��ư �־ Ŭ���� ��¼��..
//			btn.setOnClickListener(new Button.OnClickListener()  {
//				public void onClick(View v)  {
//					String str = myDataArr.get(pos).name + "���� ��ȭ��ȣ�� [ "+
//					                                                   myDataArr.get(pos).phone+" ] �Դϴ�.";
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
//		final ProgressDialog dialog= ProgressDialog.show(MyMileagePageActivity.this, "Ÿ��Ʋ","�޽���",true);
////		b. Dialog�� ȭ�鿡�� �����ϴ� �ڵ带 �ۼ��Ѵ�. ���� ��� 3���� �ִٰ� ���̾�α׸� ���ְ� �ʹٸ�...
//		new Thread(new Runnable() {
//		public void run() {
//		try { Thread.sleep(3000); } catch(Exception e) {}
//		dialog.dismiss();
//		}
//		});
		
		
		// DB ���Ŵϱ� �ʱ�ȭ ���ش�.
		 initDB();
		 
		 
		myQRcode = MyQRPageActivity.qrCode;			// �� QR �ڵ�. (Ȯ�ο�)
		
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
		URL imageURL = null;							
		URLConnection conn = null;
		InputStream is= null;
		
		setContentView(R.layout.my_mileage);
		
		/* ���� ���
		m_list = (ListView) findViewById(R.id.id_list);
		m_list.setOnItemClickListener(onItemClick);
		*/
		
		searched = false;		// ?
		
		if(isRunning<1){								// ������ ������.??;
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

	
	/* ���� ����� ������� ����
	
	AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			// ���๮
//			Toast.makeText(MyMileagePageActivity.this, "��ġ��ġ"+arg2+"�̰���:"+entriesFn.get(arg2).getCheckMileageMerchantsMerchantID(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
			intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		// ������ ���̵�
			intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());					// ���� �ĺ� ��ȣ
			intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());			// �������� ���� �� ���ϸ���
			startActivity(intent);
		}
	};

*/
	
	
	
	
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
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
								theData1(in);
							}catch(Exception e){ 
								// �ٽ�?
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
									getDBData();						// 5ȸ ��õ����� �����ϸ� db���� ������ �����ش�.
								}
								
//								// �����ϱ� �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
			isRunning = isRunning-1;		// ������.
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
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
						
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
							dbSaveEnable = false;
							BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_60_60);
							bm = dw.getBitmap();
						}
						// ������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�.
//						Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
						// bm �̹��� ũ�� ��ȯ .
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
								// �� �� ������ �̹���, ������ �̸�
						));
						
					}
					//    			 2�� �۾�. ������ �̸�, �̹��� �����ͼ� �߰��� ����.
					//    			 array ä�� �ѱ�� ���������� �ֵ��� �Ѵ�..
				}
			}catch (JSONException e) {
				doneCnt--;
				dbSaveEnable = false;
				e.printStackTrace();
			}finally{
//				entriesFn = entries;								// db ó�� ���� �ӽ� �ּ� *** 
				dbInEntries = entries; 
				reTry = 5;				// ��õ� Ƚ�� ����
				searched = true;
//				showInfo();											// db ó�� ���� �ӽ� �ּ� *** 
				// db �� �����͸� �ִ´�.
				try{
					if(dbSaveEnable){		// �̹������� ���������� ������ ���.
						saveDataToDB();
					}else{
						alertToUser();		// �̹��� �������µ� ������ ���.
						// ��e�� ó���� ������ (����) -  db�� �˻��Ͽ� �����Ͱ� ������ �����ְ� ������ ����... entriesFn = dbOutEntries
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
	
	public void alertToUser(){				// 	data ��ȸ�� �� �ȵƾ��.
		Log.d(TAG,"Get Data from Server -> Error Occured..");
		
	}
	
	
	
	
	// ������ ���̵�� ������ ���� ��������. .. Arrayä�� �ְ� �ޱ�..  -- 2�� �˻�  -- > 2�� �˻� ���� ��.. 
	public void getMerchantInfo(final List<CheckMileageMileage> entries3, final int max){
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
//		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMileage> entries2 = new ArrayList<CheckMileageMileage>(max);
		final int max2 = max;
		// ������ ���ؼ� ������.
		new Thread(
				new Runnable(){
					public void run(){
						
						for (int j = 0; j < max2; j++ ){
							// ������ ���̵� ������.
							final String merchantId2 = entries3.get(j).getCheckMileageMerchantsMerchantID();
							// ��û�� ���ڿ��� ����� ����. (json ������� ������ ���� ����)
							JSONObject obj = new JSONObject();
							try{
								// ���� ������ ����
								obj.put("activateYn", "Y");
								obj.put("merchantId", merchantId2);
//								Log.i(TAG, "merchantId::"+merchantId2);
							}catch(Exception e){
								e.printStackTrace();
							}
							// ���� ���ڿ�. (���� json ����� ������Ʈ�� ���ڿ���)
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
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								InputStream in =  connection2.getInputStream();
								// ������ ���̵�� ������ ������ �����°� ó��..���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�. + ������ �̸�, ������ �̹��� URL
								BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);	
								StringBuilder builder = new StringBuilder();
								String line =null;
								while((line=reader.readLine())!=null){
									builder.append(line).append("\n");
								}
//								Log.d(TAG,"����::"+builder.toString());
								String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�... �뵵�� �°� ������ ��.
								JSONObject jsonObject;	// 1���� ������.
								JSONObject jsonObject2;	// 1���� �������� "���������̵�" ���� ������. --> ���⼭ ��Ʈ������ �� �ϳ��� ������.
								if(connection2.getResponseCode()==200 || connection2.getResponseCode()==204){		// ��û ������
									jsonObject = new JSONObject(tempstr);
									jsonObject2 = jsonObject.getJSONObject("checkMileageMerchant");
									// ������ �̸�.
									newMerchantName = jsonObject2.getString("companyName");			// Ư�� ���ڼ� �ʰ��� �ڿ� �ڸ��� ... ���δ�.
									if(newMerchantName.length()>merchantNameMaxLength){
										newMerchantName = newMerchantName.substring(0,merchantNameMaxLength-2);		// �ִ� ���ڼ� -2 ��ŭ �ڸ��� ... ���δ�.
										newMerchantName = newMerchantName + "...";
										entries3.get(j).setMerchantName(newMerchantName);
									}else{
										entries3.get(j).setMerchantName(newMerchantName);
									}
									entries3.get(j).setMerchantName(jsonObject2.getString("companyName"));// ������ ������ �޴´�. �̸�
									
									// ����.
									if(jsonObject2.getString("workPhoneNumber")==null || jsonObject2.getString("workPhoneNumber").length()<1){	// ������ ������ �޴´�. ����
										entries3.get(j).setWorkPhoneNumber("");// ������ ������ �޴´�. ����
									}else{
										entries3.get(j).setWorkPhoneNumber("(��)"+jsonObject2.getString("workPhoneNumber"));
									}
									// ������ URL
									Bitmap bm = null;
									// ������ �̹��� URL �����Ѵ�. -- �̹������� ����
									try{
										entries3.get(j).setMerchantImg(jsonObject2.getString("profileThumbnailImageUrl"));				// ������ �̹��� URL     profileImageUrl --> profileThumbnailImageUrl
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
									// ������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�.
//									Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
									// bm �̹��� ũ�� ��ȯ .
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
										getMerchantInfo(entries3, max);		// ��õ�?
									}catch(Exception e2){}
								}else{
									Log.w(TAG,"reTry failed. -- init reTry");
									reTry = 5;			// ���� ??
								}
							}
						}		// for�� ����
//						Log.d(TAG,"������ ���� ���� �Ϸ�. ");
						entriesFn = entries3;
						showInfo();					// ? ���нÿ��� �ǳ�..?
					}
				}
		).start();
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

	/*
	 * Bitmap �̹��� ��������
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
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
//		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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
//	    	  Toast.makeText(MyMileagePageActivity.this, "123123", Toast.LENGTH_SHORT).show();
//	                Intent intent = new Intent(UserManagementActivity.this,AddUserActivity.class);        // example���� �̸�
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
//				AlertShow("Wifi Ȥ�� 3G ���� ������� �ʾҰų� �������� �ʽ��ϴ�. ��Ʈ��ũ Ȯ�� �� �ٽ� ������ �ּ���.");
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
