package kr.co.bettersoft.checkmileage.activities;
// ��Ʈ��

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;
import kr.co.bettersoft.checkmileage.pref.Password;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import java.util.List;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * MainActivity
 *  intro ȭ��
 * ��� : ��Ʈ�� ȭ���� ������.
 * QR �ڵ尡 �ִ��� �˻��Ͽ�
 *  QR�ڵ尡 ������ ���νø���� �̵�(���� �ø��� �� ùȭ��)
 *  QR�ڵ尡 ���ٸ� QR ���� �������� �̵��Ͽ� �ű� ���� �Ǵ� �ִ� �� ���. �� ���νø���� �̵�.
 *  
 */

public class MainActivity extends Activity {
	public static Activity mainActivity;

	String TAG = "MainActivity";

	String controllerName = "";
	String methodName = "";
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;

	// �� QR �ڵ�
	static String myQR = "";
	// QR ������̿� ���.
	static int qrResult = 0;

	String qrFromPref = ""; //�������� ���� qr �ڵ�
	String qrFromFile = "";		// ���Ͽ��� ���� qr �ڵ�
	
	// ���� ���� �����  --> QR �ڵ嵵 �����ϴ°ɷ�..
	String strForLog = "";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;

	public static Boolean loginYN = false;
	Boolean finishApp = false;

	//	public static String REGISTRATION_ID;			

	// ���̺� ���� ����.
	private static final String Q_CREATE_TABLE = "CREATE TABLE user_info (" +
	"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
	"key_of_data TEXT," +
	"value_of_data TEXT" +
	");" ;

	// ���̺� ��ȸ ����
	private final String Q_GET_LIST = "SELECT * FROM user_info"
		+ " WHERE key_of_data = 'user_img'";


	//----------------------- SQLite -----------------------//
	SQLiteDatabase db = null;
	/**
	 * initDB
	 *  DB �ʱ�ȭ�Ѵ�.
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void initDB(){
		Log.i(TAG,"initDB");
		// db ���� �۾� �ʱ�ȭ, DB ���� SQLiteDatabase �ν��Ͻ� ����          db ���ų� ������ ����
		if(db== null ){
			db= openOrCreateDatabase( "sqlite_carrotDB.db",             
					SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		}
		// ���̺��� ������ �������� �� ���̺� ���� Ȯ�� ������ ����.
		checkTableIsCreated(db);
	}
	/**
	 * checkTableIsCreated
	 *  db ���̺��� �غ��Ѵ�
	 *
	 * @param db
	 * @param
	 * @return
	 */
	public void checkTableIsCreated(SQLiteDatabase db){		// user_info ��� �̸��� ���̺��� �˻��ϰ� ������ ����.
		Log.i(TAG, "checkTableIsCreated");
		Cursor c = db.query( "sqlite_master" , new String[] { "count(*)"}, "name=?" , new String[] { "user_info"}, null ,null , null);
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
	}
	/**
	 * getDBData
	 *  db �����͸� ������
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void getDBData(){				// db �� �ִ� ������ ������ ���
		Log.i(TAG, "getDBData");
		String data_key="";
		String data_value="";
		// ��ȸ
		Cursor c = db.rawQuery( Q_GET_LIST, null );
		//		Log.i(TAG, Integer.toString(c.getCount()));			// qr img
		if(c.getCount()==0){
			Log.i(TAG, "saved QR Image NotExist");
		}else{
			Log.i(TAG, "saved QR Image Exist");				// ������ ������ ������ �����.
			c.moveToFirst();                                 // Ŀ���� ù�������� �ű�
			while(c.isAfterLast()== false ){                   // ������ ������ �ɶ����� 1�� �����ϸ鼭 ����
				data_key = c.getString(1);	
				data_value = c.getString(2);	
				c.moveToNext();
			}
			//			Log.i(TAG, "key:"+data_key+"/value:"+data_value);		// idx / key / value				// qr ����Ÿ�� ������ -> �̹����� �ǵ���
			byte[] decodedString = Base64.decode(data_value, Base64.DEFAULT); 
			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			MyQRPageActivity.savedBMP = decodedByte;
			Log.i(TAG,"pass QR img");
		}
		c.close();
	}
	////---------------------SQLite ----------------------////

	String RunMode = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("MainActivity", "Success Starting MainActivity");
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title

		Intent receiveIntent = getIntent();						// Ǫ�� �� ���� ���࿡ ���� ��ġ.
		RunMode = receiveIntent.getStringExtra("RunMode");		
		if(RunMode==null || RunMode.length()<1){
			RunMode = "";
		}

		mainActivity = MainActivity.this;		// �ٸ����� ���� �����Ű�� ����.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		//		CommonUtils.usingNetwork = 0;		// ���� ��� ī���� �ʱ�ȭ

		initDB();
		getDBData();
		db.close();
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);


		// prefs �� �о ��� �Է� â�� ����� ���θ� �����Ѵ�.. ���Ⱑ ù �������ϱ� ���⼭ �Ѵ�.. 
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getBoolean("appLocked", false), Toast.LENGTH_SHORT).show();	
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getString("password", ""), Toast.LENGTH_SHORT).show();	
		Boolean locked = sharedPrefCustom.getBoolean("appLocked", false);
		// ��� ���� ����
		if(locked&&(!loginYN)){
			//			Toast.makeText(MainActivity.this, "locked", Toast.LENGTH_SHORT).show();	
			Intent intent = new Intent(MainActivity.this, Password.class);
			// ��� ���� ��Ƽ��Ƽ ����(��)
			intent.putExtra(Password.NEXT_ACTIVITY, CommonUtils.packageNames+".MainActivity");
			// ���� ȭ�� ��� ����
			intent.putExtra(Password.PASSWORD, sharedPrefCustom.getString("password", "1234"));
			// ��� �Է� ���
			intent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
			startActivity(intent);   
			finish();
			// ��� ���� ����
		}else{
			//			Toast.makeText(MainActivity.this, "opened", Toast.LENGTH_SHORT).show();	
			loginYN = false;		// ���ܼ�. �ٽ� ������ �� �߶��
			nextProcessing();		// ���� �ܰ�
		}
	}

	/**
	 * nextProcessing
	 *  ���� �ܰ� - �ε�ȭ��, ����� qr �ִ��� Ȯ���Ͽ� ���ΰ���, qr ����ȭ�� ���� ����
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void nextProcessing(){

		//		////////////////////////////////////////////GCM ����  --> ����      ///////////////////////////////////////////////////////////////		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   


		readQR();			// �ϴ� ����� QR ���� �ִ������� Ȯ�� �Ѵ�.. �ִٸ� ���������� �ʴ´�..

		// ����. �˻縦 ���� ���� ���� ����.
		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(2000);		// �ʱ� �ε� �ð�. 2�ʰ� ��Ʈ�� ȭ�� ������
							// ��ݱ�� ���� ��� �Է� �������� �̵�.. // �� ���� �̱���.
							if(finishApp){							// �ε��� �ڷΰ��� ������ �ٸ� �ൿ ���ϰ� ������ ����.
								Log.d(TAG,"finishApp"+DummyActivity.count);
								DummyActivity.count=0;
								finishApp = false;
								finish();
							}else{
								/*
								 * QR �� ������ ���� �б� ����� ���.  PREF ������ ����� ���� �̹Ƿ�  ��� ����.
								 */
								Log.i("MainActivity", "qrResult::"+qrResult);		// �б� ��� ����.
								//--------------------------------------------------------------------------------------//

								// QR �ڵ尡 �ִٸ� QR ȭ������ �̵��ϰ�, QR �ڵ尡 ���ٸ� QR ��� ȭ������ �̵��Ѵ�.
								if((myQR!=null) &&  myQR.length()>0){ // QR�ڵ尡 �ִ��� Ȯ��. ������ �ٷ� �� QR �������� �̵�.	// ����� QR�� null �� ��쿡�� �������� �̵�..
									Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);

									Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
									intent.putExtra("RunMode", RunMode);
									intent.putExtra("myQR", myQR);
									if(DummyActivity.count>0){		// ���� �����ϸ� ���� ��Ƽ��Ƽ�� ����.
										startActivity(intent);
									}
									finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����. 
								}else {				// QR �ڵ尡 ������ ��ġ�� ���� �����ϴ� ���. 
									/*
									 *  ������ ���� ���� ��������� Ȯ���� �ʿ��ϴ�.
									 *  QR ���� ���Ͽ� QR ���� ���� �ÿ��� ���� ��ġ �� ���� �����̹Ƿ� ������ �޾ƾ� �Ѵ�..
									 *  ���� 1�ܰ��� [�޴��� ��ȣ ����]����  ������ ����� �Ͽ� ���� ��ϵ� ��������� Ȯ���� �Ѵ�. (���� ��ϵ� ����ڶ�� ���� ����� QR �ڵ带 �޾Ƽ� �״�� ���) 
									 *  �������� QR ���� ���� ��쿡�� 2�� ����(������ȣ ����) �Ŀ� QR ���� ���� â���� �̵��Ѵ�.
									 *  1�� ������ ���� �������� QR ���� �޾ƿ� ��� ���� 2�ܰ��� [������ȣ Ȯ��] ������ �����ϰ� �� QR���� ȭ������ �̵��Ѵ�. 
									 */
									// ���� ȭ������ �̵��Ѵ�. (���� ���) --> ���� ��� ����
									//Log.i("MainActivity", "There is no saved QR code.. Go to Certification");
									//Intent intent = new Intent(MainActivity.this, CertificationStep1.class);

									// ���� ȭ�� 2�� �̵�(�׽�Ʈ��)  -->���� ��� ����
									//									Log.i("MainActivity", "Test for Certification2");
									//									Intent intent = new Intent(MainActivity.this, CertificationStep2.class);

									// QR ���� ���� â���� �̵�. (���� ���� ������ �ӽ� ���) -- ���� ��� ���ϸ鼭 ���� ȣ�� ����� ��
									Log.i("MainActivity", "There is no saved QR code.. Go get QR");
									Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);

									if(DummyActivity.count>0){			// ���� �����ϸ� ���� ��Ƽ��Ƽ�� ����.
										startActivity(intent);
									}
									finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
								}
							}
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}

	////////////// ���� ������ �̿��� ���� ���. ���� ������ �е��� �� ����. QR. ////////////////////////////
	/**
	 * readQRFromPref
	 *  �����۷������� qr ������ ��´�
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void readQRFromPref(){
		strForLog = sharedPrefCustom.getString("qrcode", "");		
		Log.i(TAG,"pref qrcode:"+strForLog);
		qrFromPref = strForLog;
	}
	
	/**
	 * ���Ϸκ��� qr�� �д´�.
	 */
	public void readQRFromFile(){
		Log.d(TAG,"try get qr from file");
		try{
			File myFile = new File(CommonUtils.qrFileSavedPathFile);	
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
//				aBuffer += aDataRow + "\n";
				aBuffer += aDataRow;
			}
			qrFromFile = aBuffer;
		}catch(Exception e){
//			e.printStackTrace();
			qrFromFile = "";
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////    


	/////////////////////////////////////// ������ �̿��� QR �б�, ����, �ʱ�ȭ //////////////////////////    
	// QR �ڵ� ����ҿ��� QR �ڵ带 �о�´�. --> �������Ͽ��� �д´�
	public void readQR(){
		readQRFromPref();
		readQRFromFile();
		
		if(qrFromPref==null || qrFromPref.length()<1){		// ������ ���� ���
			Log.d(TAG,"pref no qr");
			if(qrFromFile==null || qrFromFile.length()<1){	
				//(���Ͽ��� ���� ��� -> ���ʿ� ��� ������ �н� --> ����ȭ������ �̵���.)	
			}else{		// ���Ͽ��� �ִ� ��� 
				myQR = qrFromFile;	// ���ϰ��� ���
				// ���� �����͸� ������ ����  -- ���Ͽ� �ִ°ɷ� ����� �߱� ������ ������ �����صд�.
				sharedPrefCustom = getSharedPreferences("MyCustomePref",
						Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
				SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
				saveQR.putString("qrcode", qrFromFile);
				saveQR.commit();
				
				// ���� ��Ƽ��Ƽ�� ���� (���ϰ����)
				myQR = qrFromFile;	
				qrResult = 1;
				MyQRPageActivity.qrCode = myQR;
			}
		}else if(qrFromFile==null || qrFromFile.length()<1){		// ������ �ִ� ��� + ���Ͽ� ���� ���
			// ������ �ִ� ���� ���Ϸ� ����
			try {
				File qrFileDirectory = new File(CommonUtils.qrFileSavedPath);
				qrFileDirectory.mkdirs();

				File myFile = new File(CommonUtils.qrFileSavedPathFile);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter myOutWriter = 
										new OutputStreamWriter(fOut);
				myOutWriter.append(qrFromPref);
				myOutWriter.close();
				fOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			myQR = qrFromPref;		// ���� ��Ƽ��Ƽ�� ���� (���������)
			qrResult = 1;
			MyQRPageActivity.qrCode = myQR;
		}else{			// ������ �ִ� ��� + ���Ͽ��� �ִ� ���
			// ���Ͽ� �ٸ��ٸ� ������ �ִ� ���� ���Ϸ� ����
			if(!(qrFromPref.equals(qrFromFile))){
				Log.d(TAG,"not equals qrFromFile,qrFromPref ");
				// ������ �ִ� ���� ���Ϸ� ����
				try {
					File qrFileDirectory = new File(CommonUtils.qrFileSavedPath);
					qrFileDirectory.mkdirs();

					File myFile = new File(CommonUtils.qrFileSavedPathFile);
					myFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(myFile);
					OutputStreamWriter myOutWriter = 
											new OutputStreamWriter(fOut);
					myOutWriter.append(qrFromPref);
					myOutWriter.close();
					fOut.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			myQR = qrFromPref;				// ���� ��Ƽ��Ƽ�� ���� (���������)
			qrResult = 1;
			MyQRPageActivity.qrCode = myQR;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////   

	/**
	 * onPause
	 *  Ȩ��ư �������� ������ �����Ų��
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Ȩ��ư �������� ���� ����..
		if(!isForeGround()){
			Log.d(TAG,"go home, bye");
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
			finish();
		}
	}

	/*
	 * ���μ����� �ֻ����� ���������� �˻�.
	 * @return true = �ֻ���
	 */
	/**
	 * isForeGround
	 * Ȩ��ư �������� Ȯ���ϱ� ���� ���μ����� �ֻ����� ���������� �˻��Ѵ�.
	 *
	 * @param
	 * @param
	 * @return rtn
	 */
	public Boolean isForeGround(){
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE );
		List<RunningTaskInfo> list = am.getRunningTasks(1);
		ComponentName cn = list.get(0). topActivity;
		String name = cn.getPackageName();
		Boolean rtn = false;
		if(name.indexOf(getPackageName()) > -1){
			rtn = true;
		} else{
			rtn = false;
		}
		return rtn;
	}

	// �ε��߿� ��ҹ�ư���� ���� ���ϰ� ����. �����ص� ���� �������� �߱� ����.		// --< �����ϸ� �ε������� �����ϵ��� ��.
	@Override
	public void onBackPressed() {
		finishApp = true;
		Log.i("MainActivity", "onBackPressed");		
	}
}
