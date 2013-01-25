package kr.co.bettersoft.checkmileage.activities;
// 인트로

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
 *  intro 화면
 * 기능 : 인트로 화면을 보여줌.
 * QR 코드가 있는지 검사하여
 *  QR코드가 있으면 메인시리즈로 이동(메인 시리즈 중 첫화면)
 *  QR코드가 없다면 QR 선택 페이지로 이동하여 신규 생성 또는 있는 것 등록. 후 메인시리즈로 이동.
 *  
 */

public class MainActivity extends Activity {
	public static Activity mainActivity;

	String TAG = "MainActivity";

	String controllerName = "";
	String methodName = "";
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;

	// 내 QR 코드
	static String myQR = "";
	// QR 저장소이용 결과.
	static int qrResult = 0;

	String qrFromPref = ""; //설정에서 읽은 qr 코드
	String qrFromFile = "";		// 파일에서 읽은 qr 코드
	
	// 설정 파일 저장소  --> QR 코드도 저장하는걸로..
	String strForLog = "";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;

	public static Boolean loginYN = false;
	Boolean finishApp = false;

	//	public static String REGISTRATION_ID;			

	// 테이블 생성 쿼리.
	private static final String Q_CREATE_TABLE = "CREATE TABLE user_info (" +
	"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
	"key_of_data TEXT," +
	"value_of_data TEXT" +
	");" ;

	// 테이블 조회 쿼리
	private final String Q_GET_LIST = "SELECT * FROM user_info"
		+ " WHERE key_of_data = 'user_img'";


	//----------------------- SQLite -----------------------//
	SQLiteDatabase db = null;
	/**
	 * initDB
	 *  DB 초기화한다.
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void initDB(){
		Log.i(TAG,"initDB");
		// db 관련 작업 초기화, DB 열어 SQLiteDatabase 인스턴스 생성          db 열거나 없으면 생성
		if(db== null ){
			db= openOrCreateDatabase( "sqlite_carrotDB.db",             
					SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		}
		// 테이블에서 데이터 가져오기 전 테이블 생성 확인 없으면 생성.
		checkTableIsCreated(db);
	}
	/**
	 * checkTableIsCreated
	 *  db 테이블을 준비한다
	 *
	 * @param db
	 * @param
	 * @return
	 */
	public void checkTableIsCreated(SQLiteDatabase db){		// user_info 라는 이름의 테이블을 검색하고 없으면 생성.
		Log.i(TAG, "checkTableIsCreated");
		Cursor c = db.query( "sqlite_master" , new String[] { "count(*)"}, "name=?" , new String[] { "user_info"}, null ,null , null);
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
	}
	/**
	 * getDBData
	 *  db 데이터를 꺼낸다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void getDBData(){				// db 에 있는 데이터 꺼내어 사용
		Log.i(TAG, "getDBData");
		String data_key="";
		String data_value="";
		// 조회
		Cursor c = db.rawQuery( Q_GET_LIST, null );
		//		Log.i(TAG, Integer.toString(c.getCount()));			// qr img
		if(c.getCount()==0){
			Log.i(TAG, "saved QR Image NotExist");
		}else{
			Log.i(TAG, "saved QR Image Exist");				// 데이터 있으면 꺼내서 사용함.
			c.moveToFirst();                                 // 커서를 첫라인으로 옮김
			while(c.isAfterLast()== false ){                   // 마지막 라인이 될때까지 1씩 증가하면서 본다
				data_key = c.getString(1);	
				data_value = c.getString(2);	
				c.moveToNext();
			}
			//			Log.i(TAG, "key:"+data_key+"/value:"+data_value);		// idx / key / value				// qr 문자타입 데이터 -> 이미지로 되돌림
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

		Intent receiveIntent = getIntent();						// 푸쉬 로 인한 실행에 대한 조치.
		RunMode = receiveIntent.getStringExtra("RunMode");		
		if(RunMode==null || RunMode.length()<1){
			RunMode = "";
		}

		mainActivity = MainActivity.this;		// 다른데서 여기 종료시키기 위함.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		//		CommonUtils.usingNetwork = 0;		// 서버 통신 카운터 초기화

		initDB();
		getDBData();
		db.close();
		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);


		// prefs 를 읽어서 비번 입력 창을 띄울지 여부를 결정한다.. 여기가 첫 페이지니까 여기서 한다.. 
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getBoolean("appLocked", false), Toast.LENGTH_SHORT).show();	
		//        Toast.makeText(MainActivity.this, "::"+sharedPrefCustom.getString("password", ""), Toast.LENGTH_SHORT).show();	
		Boolean locked = sharedPrefCustom.getBoolean("appLocked", false);
		// 잠금 설정 상태
		if(locked&&(!loginYN)){
			//			Toast.makeText(MainActivity.this, "locked", Toast.LENGTH_SHORT).show();	
			Intent intent = new Intent(MainActivity.this, Password.class);
			// 비번 이후 액티비티 설정(나)
			intent.putExtra(Password.NEXT_ACTIVITY, CommonUtils.packageNames+".MainActivity");
			// 현재 화면 비번 전달
			intent.putExtra(Password.PASSWORD, sharedPrefCustom.getString("password", "1234"));
			// 비번 입력 모드
			intent.putExtra(Password.MODE, Password.MODE_CHECK_PASSWORD);
			startActivity(intent);   
			finish();
			// 잠금 해제 상태
		}else{
			//			Toast.makeText(MainActivity.this, "opened", Toast.LENGTH_SHORT).show();	
			loginYN = false;		// 문단속. 다시 켰을때 또 뜨라고
			nextProcessing();		// 다음 단계
		}
	}

	/**
	 * nextProcessing
	 *  다음 단계 - 로딩화면, 저장된 qr 있는지 확인하여 메인갈지, qr 생성화면 갈지 결정
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void nextProcessing(){

		//		////////////////////////////////////////////GCM 세팅  --> 안함      ///////////////////////////////////////////////////////////////		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   


		readQR();			// 일단 저장된 QR 값이 있는지부터 확인 한다.. 있다면 인증을하지 않는다..

		// 시작. 검사를 통해 다음 진로 결정.
		new Thread(
				new Runnable(){
					public void run(){
						try{
							Thread.sleep(2000);		// 초기 로딩 시간. 2초간 인트로 화면 보여줌
							// 잠금기능 사용시 비번 입력 페이지로 이동.. // 은 아직 미구현.
							if(finishApp){							// 로딩중 뒤로가기 누르면 다른 행동 안하고 조용히 끝남.
								Log.d(TAG,"finishApp"+DummyActivity.count);
								DummyActivity.count=0;
								finishApp = false;
								finish();
							}else{
								/*
								 * QR 을 파일을 통한 읽기 쓰기시 사용.  PREF 설정을 사용할 예정 이므로  사용 안함.
								 */
								Log.i("MainActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
								//--------------------------------------------------------------------------------------//

								// QR 코드가 있다면 QR 화면으로 이동하고, QR 코드가 없다면 QR 등록 화면으로 이동한다.
								if((myQR!=null) &&  myQR.length()>0){ // QR코드가 있는지 확인. 있으면 바로 내 QR 페이지로 이동.	// 저장된 QR이 null 일 경우에도 생성으로 이동..
									Log.i("MainActivity", "QR code checked success, Go Main Pages::"+myQR);

									Intent intent = new Intent(MainActivity.this, Main_TabsActivity.class);
									intent.putExtra("RunMode", RunMode);
									intent.putExtra("myQR", myQR);
									if(DummyActivity.count>0){		// 강제 종료하면 다음 액티비티도 없다.
										startActivity(intent);
									}
									finish();		// 다른 액티비티를 호출하고 자신은 종료. 
								}else {				// QR 코드가 없으면 설치후 최초 실행하는 사람. 
									/*
									 *  기존에 인증 받은 사용자인지 확인이 필요하다.
									 *  QR 저장 파일에 QR 값이 없을 시에는 어플 설치 후 최초 실행이므로 인증을 받아야 한다..
									 *  인증 1단계인 [휴대폰 번호 인증]으로  서버와 통신을 하여 이전 등록된 사용자인지 확인을 한다. (이전 등록된 사용자라면 이전 등록한 QR 코드를 받아서 그대로 사용) 
									 *  서버에도 QR 값이 없을 경우에는 2차 인증(인증번호 인증) 후에 QR 생성 선택 창으로 이동한다.
									 *  1차 인증을 통해 서버에서 QR 값을 받아온 경우 인증 2단계인 [인증번호 확인] 절차를 생략하고 내 QR보기 화면으로 이동한다. 
									 */
									// 인증 화면으로 이동한다. (정상 사용) --> 인증 사용 안함
									//Log.i("MainActivity", "There is no saved QR code.. Go to Certification");
									//Intent intent = new Intent(MainActivity.this, CertificationStep1.class);

									// 인증 화면 2로 이동(테스트용)  -->인증 사용 안함
									//									Log.i("MainActivity", "Test for Certification2");
									//									Intent intent = new Intent(MainActivity.this, CertificationStep2.class);

									// QR 생성 선택 창으로 이동. (인증 개발 전까지 임시 사용) -- 인증 사용 안하면서 정상 호출 방식이 됨
									Log.i("MainActivity", "There is no saved QR code.. Go get QR");
									Intent intent = new Intent(MainActivity.this, No_QR_PageActivity.class);

									if(DummyActivity.count>0){			// 강제 종료하면 다음 액티비티도 없다.
										startActivity(intent);
									}
									finish();		// 다른 액티비티를 호출하고 자신은 종료.
								}
							}
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
		).start();
	}

	////////////// 설정 파일을 이용한 정보 얻기. 설정 정보를 읽도록 할 예정. QR. ////////////////////////////
	/**
	 * readQRFromPref
	 *  프리퍼런스에서 qr 정보를 얻는다
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
	 * 파일로부터 qr을 읽는다.
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


	/////////////////////////////////////// 파일을 이용한 QR 읽기, 쓰기, 초기화 //////////////////////////    
	// QR 코드 저장소에서 QR 코드를 읽어온다. --> 설정파일에서 읽는다
	public void readQR(){
		readQRFromPref();
		readQRFromFile();
		
		if(qrFromPref==null || qrFromPref.length()<1){		// 설정에 없는 경우
			Log.d(TAG,"pref no qr");
			if(qrFromFile==null || qrFromFile.length()<1){	
				//(파일에도 없는 경우 -> 양쪽에 모두 없으면 패스 --> 생성화면으로 이동됨.)	
			}else{		// 파일에는 있는 경우 
				myQR = qrFromFile;	// 파일것을 사용
				// 파일 데이터를 설정에 저장  -- 파일에 있는걸로 쓰기로 했기 때문에 설정에 저장해둔다.
				sharedPrefCustom = getSharedPreferences("MyCustomePref",
						Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
				SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
				saveQR.putString("qrcode", qrFromFile);
				saveQR.commit();
				
				// 다음 액티비티로 전달 (파일값사용)
				myQR = qrFromFile;	
				qrResult = 1;
				MyQRPageActivity.qrCode = myQR;
			}
		}else if(qrFromFile==null || qrFromFile.length()<1){		// 설정에 있는 경우 + 파일에 없는 경우
			// 설정에 있는 것을 파일로 복사
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
			
			myQR = qrFromPref;		// 다음 액티비티로 전달 (설정값사용)
			qrResult = 1;
			MyQRPageActivity.qrCode = myQR;
		}else{			// 설정에 있는 경우 + 파일에도 있는 경우
			// 비교하여 다르다면 설정에 있는 것을 파일로 복사
			if(!(qrFromPref.equals(qrFromFile))){
				Log.d(TAG,"not equals qrFromFile,qrFromPref ");
				// 설정에 있는 것을 파일로 복사
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
			myQR = qrFromPref;				// 다음 액티비티로 전달 (설정값사용)
			qrResult = 1;
			MyQRPageActivity.qrCode = myQR;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////   

	/**
	 * onPause
	 *  홈버튼 눌렀을때 어플을 종료시킨다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// 홈버튼 눌렀을때 종료 여부..
		if(!isForeGround()){
			Log.d(TAG,"go home, bye");
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}
	}

	/*
	 * 프로세스가 최상위로 실행중인지 검사.
	 * @return true = 최상위
	 */
	/**
	 * isForeGround
	 * 홈버튼 눌렀는지 확인하기 위해 프로세스가 최상위로 실행중인지 검사한다.
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

	// 로딩중에 취소버튼으로 종료 못하게 막음. 종료해도 메인 페이지가 뜨기 때문.		// --< 종료하면 로딩끝나고 종료하도록 함.
	@Override
	public void onBackPressed() {
		finishApp = true;
		Log.i("MainActivity", "onBackPressed");		
	}
}
