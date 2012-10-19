package kr.co.bettersoft.checkmileage.activities;
/*
 *  내 QR 보기 화면
 *  
 *  그리고 onresume 에 좌표 업뎃 기능 을 넣어 어플 실행 또는 내 QR 코드를 볼때마다 현재 사용자 위치를 서버에 업뎃 하도록 하였음.
 *   화면을 자주 오가며 업뎃 자꾸 시키는것을 방지하기 위해  플래그 값을 둠.  빨라서 대부분은 업뎃을 함..
 *  
 *  
 *  
 */


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Hashtable;

import kr.co.bettersoft.checkmileage.domain.CheckMileageMemberSettings;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


//import java.awt.image.BufferedImage; 
//import com.google.zxing.BarcodeFormat; 
//import com.google.zxing.WriterException; 
//import com.google.zxing.client.j2se.MatrixToImageWriter; 
//import com.google.zxing.common.ByteMatrix; 
//import com.google.zxing.qrcode.QRCodeWriter; 
import com.google.zxing.BarcodeFormat; 
import com.google.zxing.WriterException;     
import android.app.Activity; 
import android.graphics.Bitmap; 
import android.graphics.Point; 
import android.os.Bundle; 
import android.util.Log; 
import android.view.Display; 
import android.view.View; 
import android.view.View.OnClickListener; 
import android.view.WindowManager; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.ImageView; 


public class MyQRPageActivity extends Activity {
	String TAG = "MyQRPageActivity";
	String controllerName="";
	String methodName="";
	String serverName = CommonUtils.serverNames;
	
	int responseCode= 0;
	int isUpdating = 0;
	
	int app_end = 0;			// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	
	SharedPreferences sharedPrefCustom;
	
	// 설정 정보 저장할 도메인.
	CheckMileageMemberSettings settings;
	
	//	MyLocationOverlay2 mLocation;
	int myLat = 0;
	int myLon = 0;
	
	static Bitmap savedBMP = null;
	int qrSize =300;
	
	int deviceSize = 0;
	
	/** Called when the activity is first created. */
	static Bitmap bmp =null;
	 static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";
	
	
	
	
	
	
	
	
	
	
	
//	public static BufferedImage createTextCode(String text){ 
//        QRCodeWriter qrCodeWriter= new QRCodeWriter(); 
//          
//        int qrWidth=128; 
//        int qrHeight=128; 
//        ByteMatrix byteMatrix=null; 
//        try { 
//            byteMatrix=qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, qrWidth, qrHeight); 
//        } catch (WriterException e) { 
//            e.printStackTrace(); 
//        } 
//        return toBufferedImage(byteMatrix); 
//    } 
//      
//    public static BufferedImage createURLCode(String url){ 
//        QRCodeWriter qrCodeWriter= new QRCodeWriter(); 
//          
//        int qrWidth=128; 
//        int qrHeight=128; 
//        ByteMatrix byteMatrix = null; 
//        try { 
//            byteMatrix=qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, qrWidth, qrHeight); 
//        } catch (WriterException e) { 
//            e.printStackTrace(); 
//        } 
//  
//        return toBufferedImage(byteMatrix); 
//    } 
//      
//    public static BufferedImage createDirectDownloadCode(){ 
//        return createURLCode(""); 
//    } 
//      
//    public static BufferedImage createMarketDownloadCode(){ 
//        return createURLCode("market://search?q=pname:org.openintents.wifiqr"); 
//    } 
//      
//      
//    public static BufferedImage toBufferedImage(ByteMatrix matrix) { 
//        int width = matrix.getWidth(); 
//        int height = matrix.getHeight(); 
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
//        for (int x = 0; x < width; x++) { 
//          for (int y = 0; y < height; y++) { 
//  
//            image.setRGB(x, y, matrix.get(x, y) == 0 ? BLACK : WHITE); 
//          } 
//        } 
//        return image; 
//    } 
//    private static final int BLACK = 0xFF000000; 
//    private static final int WHITE = 0xFFFFFFFF; 

	
	
	
	
	
	
	
	
	
	// 핸들러 등록
	Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int showQR =  b.getInt("showQR");		// 값을 넣지 않으면 0 을 꺼내었다.
    		if(showQR==1){
    			imgView.setImageBitmap(bmp);
    		}
    		if(b.getInt("showErrToast")==1){
				Toast.makeText(MyQRPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			}
    		
    	}
    };
    
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
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_qr_page);
	    imgView = (ImageView)findViewById(R.id.myQRCode);
	    /*
	     *  QR 크기를 화면에 맞추기 위해 화면 크기를 구함.
	     */
	    Log.i("qrCode : ", "" + qrCode);
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
	    
		
//		float fqrSize = 0;
//	    if(screenWidth < screenHeight ){
//	    	fqrSize = (float) (screenWidth * 1.0);
//	    }else{
//	    	fqrSize = (float) (screenHeight * 1.0);
//	    }
//		qrSize = (int) fqrSize;		// 작은 쪽을 선택
//		deviceSize = qrSize;
//		
//		if(qrSize>400){
//			qrSize = 400;
//		}
//		if(savedBMP==null){
//			Log.d(TAG,"savedBMP is null");
//		}else{
//			Log.d(TAG,"savedBMP is not null");
//		}
		
		
		
		/*
		 *  QR 코드를 받아옴.  구글 웹페이지를 통한 생성
		 */
	    new Thread(
        		new Runnable(){
        			public void run(){
        				if(savedBMP==null){
        					bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode); 
        					
//        					BitmapDrawable bmpResize = BitmapResizePrc(bmp, deviceSize, deviceSize);  // height, width
//        					bmp = bmpResize.getBitmap();
        					if(bmp==null){
        						Log.d(TAG,"bmp==null");
        					}else{
        						saveBMPtoDB(bmp);
        						getUserSettingsFromServer();
        					}
        				}else{
        					bmp = savedBMP;
        				}
//        				 bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode); 
//        				 bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs=500x500&choe=UTF-8&chld=H&chl=test1234"); 
//        				    Log.w("MyQRPageActivity", "bmp size getHeight" + bmp.getHeight()); 
//        					Log.w("MyQRPageActivity", "bmp size getWidth" + bmp.getWidth());  
						// showQR
        				Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("showQR", 1);
						message.setData(b);
						handler.sendMessage(message);
        			}
        		}
        ).start();
	}
	
	// 생성한 QR코드 이미지를 DB에 저장한다.
	public void saveBMPtoDB(Bitmap bmp){
		Log.d(TAG,"saveBMPtoDB");
		SQLiteDatabase db = null;
		db= openOrCreateDatabase( "sqlite_carrotDB.db",             
		          SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		String data_key="";
		String data_value="";
		
		// BMP -> 문자열 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();   
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
		byte[] b = baos.toByteArray();  
		data_value = Base64.encodeToString(b, Base64.DEFAULT); 

		// 조회
		ContentValues initialValues = new ContentValues(); 
		initialValues.put("key_of_data", "user_img"); 
		initialValues.put("value_of_data", data_value); 
		db.insert("user_info", null, initialValues); 
		try{
			db.replaceOrThrow("user_info", null, initialValues);
		}catch(Exception e){
			e.printStackTrace();
		}
		db.close();
	}
	
	
	
	
	/*
	 * QR 이미지받기. url 사용하여 구글 웹에서 받아오기.
	 */
	static Bitmap downloadBitmap(String url) {    
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");    
		final HttpGet getRequest = new HttpGet(url);    
		try {        
			HttpResponse response = client.execute(getRequest);        
			final int statusCode = response.getStatusLine().getStatusCode();        
//			Log.i("MyQRPageActivity", "lva3");
			if (statusCode != HttpStatus.SC_OK) {             
				Log.w("MyQRPageActivity", "Error " + statusCode + " while retrieving bitmap from " + url);             
				return null;        
			}                
			final HttpEntity entity = response.getEntity();        
			if (entity != null) {            
				InputStream inputStream = null;            
				try {                
					inputStream = entity.getContent();                 
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);                
					return bitmap;            
				} finally {                
					if (inputStream != null) {                    
						inputStream.close();                  
					}                
					entity.consumeContent();           
				}        
			}    
		} catch (Exception e) {        // Could provide a more explicit error message for IOException or IllegalStateException        
			getRequest.abort();        
			Log.w("MyQRPageActivity", "Error while retrieving bitmap from " + url +"  " + e.toString());    
		} finally {        
			if (client != null) {            
				client.close();        
			}    
		}    
		return null;
	}
	
	/*
	 * QR 이미지 생성. 자체 라이브러리 사용하여 QR 이미지 생성
	 */
//	public void CreateQR() throws WriterException, IOException {
//		new Thread(
//				new Runnable(){
//					public void run(){
//						//	        				 bmp = "";
//						QRCodeWriter qrCodeWriter = new QRCodeWriter();
//						String text = "test1234";
//						try {
//							text = new String(text.getBytes("UTF-8"), "ISO-8859-1");
//						} catch (UnsupportedEncodingException e) {
//							e.printStackTrace();
//						}
//						try {
//							BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,100, 100);
//							Bitmap bmpbmb = encodeAsBitmap(text, BarcodeFormat.QR_CODE,100, 100);
//							// 방법 1. 변환 -> 변환 -> 디코딩  :: null pointer 에러 (팩토리에서 디코딩하면 null 이 나옴)
////							String st =  bitMatrix.toString();
////							byte[] data  = st.getBytes();
////							Log.i("MyQRPageActivity", "lv4");
////							ByteArrayInputStream in = new ByteArrayInputStream(data);
////							bmp2 = BitmapFactory.decodeStream(in); 
//							
//							// 방법 2.자체 메소드 호출 -> class not def 에러.
////							try {
////								MatrixToImageWriter.writeToFile(bitMatrix, "png", new File("qrcode.png"));
////							} catch (IOException e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							}
//						} catch (WriterException e) {
//							e.printStackTrace();
//						}
//						//	        					Log.w("MyQRPageActivity", "bmp size getHeight" + bmp.getHeight()); 
//						//	        					Log.w("MyQRPageActivity", "bmp size getWidth" + bmp.getWidth());  
//						Message message = handler.obtainMessage();
//						Bundle b = new Bundle();
//						b.putInt("testData", 2222);
//						message.setData(b);
//						handler.sendMessage(message);
//					}
//				}
//		).start();
//	}
//	private void encodeBarcode(String type, String data) { 
//		//Encode with a QR Code image    
//		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,null,Contents.Type.TEXT,BarcodeFormat.QR_CODE.toString(),smallerDimension);
//		try {     
//			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
//			ImageView myImage = (ImageView) findViewById(R.id.imageView1);
//			myImage.setImageBitmap(bitmap);
//		} catch (WriterException e) {
//			e.printStackTrace(); 
//		}
//		  Intent intent = new Intent("com.google.zxing.client.android.ENCODE"); 
//		  intent.putExtra("ENCODE_TYPE", type); 
//		  intent.putExtra("ENCODE_DATA", data); 
//		  startActivity(intent); 
//	} 

	
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		myLocationIs();
		
		
//		test1();		// 가맹점 리스트 오는지 테스트용
	}
	
	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.d("MainTabActivity", "MyQRPage finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// 서버에 내 위치 업뎃.
	public void myLocationIs(){
		try{
			LocationManager  lm;
			Location location;
			String provider;
			String bestProvider;
			lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			provider = LocationManager.GPS_PROVIDER;
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소비량
			criteria.setAltitudeRequired(false); // 고도
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // 속도
			criteria.setCostAllowed(true); // 금전적 비용
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득 *** 로그용
				myLon = (int) (location.getLongitude()*1000000);	
				Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
				updateLocationToServer(Integer.toString(myLat), Integer.toString(myLon));
			}else{
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.d(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득 *** 로그용
					myLon = (int) (location.getLongitude()*1000000);	
					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);			// 37529466 126921069
					updateLocationToServer(Integer.toString(myLat), Integer.toString(myLon));
				}
			}
		}catch(Exception e){
			Log.w(TAG,"fail to update my location to server");
		}
	}
	
	
	/*
	 * 사용자의 위치 정보를 수정 한다.
	 * 그 결과를 'SUCCESS' 나 'FAIL' 의 스트링으로 반환 한다.
	 * //checkMileageMemberController  updateMemberLocation   checkMileageMember  
	 *	// checkMileageId  latitude  longitude  activateYn  modifyDate
	 *	// SUCCESS / FAIL /// 200 204
	 */
	public void updateLocationToServer(final String myLat, final String myLon){
		if(isUpdating==0){
			isUpdating = 1;
			Log.i(TAG, "updateLocationToServer");
			controllerName = "checkMileageMemberController";
			methodName = "updateMemberLocation";
			
			
//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 자신의 아이디를 넣어서 조회
//								Log.d(TAG,"checkMileageId::"+qrCode);
//								Log.d(TAG,"latitude::"+myLat);
//								Log.d(TAG,"longitude::"+myLon);
//								Log.d(TAG,"activateYn::"+"Y");
//								Log.d(TAG,"modifyDate::"+todays);
								
								obj.put("checkMileageId", qrCode);
								obj.put("latitude", myLat);
								obj.put("longitude", myLon);
								obj.put("activateYn", "Y");
								
								String nowTime = getNow();
								
								obj.put("modifyDate", nowTime);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
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
//								InputStream in =  connection2.getInputStream();
								// 조회한 결과를 처리.
//								theData1(in);
								if(responseCode==200 || responseCode==204){
//									Log.e(TAG,"S");
								}
							}catch(Exception e){ 
								e.printStackTrace();
							}finally{
								isUpdating = 0;
							}
						}
					}
			).start();
		}else{
			Log.w(TAG,"already updating..");
		}
	}
	
	// 업뎃 시각
	public String getNow(){
		// 일단 오늘.
		Calendar c = Calendar.getInstance();
		int todayYear = 0;						// 지금 -  년 월 일 시 분
		int todayMonth = 0;
		int todayDay = 0;
		int todayHour = 0;
		int todayMinute = 0;
		int todaySecond = 0;
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
		todayDay = c.get(Calendar.DATE);
		todayHour = c.get(Calendar.HOUR_OF_DAY);
		todayMinute = c.get(Calendar.MINUTE);
		todaySecond = c.get(Calendar.SECOND);
		String tempMonth = Integer.toString(todayMonth+1);
		String tempDay = Integer.toString(todayDay);
		String tempHour = Integer.toString(todayHour);
		String tempMinute = Integer.toString(todayMinute);
		String tempSecond = Integer.toString(todaySecond);
		if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
		if(tempDay.length()==1) tempDay = "0"+tempDay;
		if(tempHour.length()==1) tempHour = "0"+tempHour;
		if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
		if(tempSecond.length()==1) tempSecond = "0"+tempSecond;
		String nowTime = Integer.toString(todayYear)+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute+":"+tempSecond;
		return nowTime;
//		Log.e(TAG, "Now to millis : "+ Long.toString(c.getTimeInMillis()));
	}
	
	
	@Override			// 이 액티비티(인트로)가 종료될때 실행. (액티비티가 넘어갈때 종료됨)
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 *    인증 기능 보류로 인해 그 이후 기능을 먼저 구현함..
	 *     이 기능은 인증 기능 구현 이후 인증2 페이지로 이동한다.
	 *     
	 *     인증 성공. 이전 사용자일 경우 서버로부터 사용자 설정 정보를 가져와서 모바일의 설정 정보에 대입시킨다..
	 *     비번의 경우 분실시 어플 삭제후 재설치.. 재 인증 받는다. 그럼 비번 초기화.
	 *      (분실시 답이 없으므로) 서버에 별도 저장은 안하는게 좋을 듯 함..
	 *     
	 *     checkMileageMemberController selectMemberInformation 
	 *     
	 * 		checkMileageId activateYn  // checkMileageMember
	 *     CheckMileageMember
	 *     
	 *     받아서 설정 도메인 같은곳에 저장해두면 좋겠지
	 */
	public void getUserSettingsFromServer(){		// 서버로부터 설정 정보를 받는다.  아이디를 사용.모든데이터.  CheckMileageMember
		Log.d(TAG, "getUserSettingsFromServer");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";

		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 자신의 아이디를 넣어서 조회
							Log.d(TAG,"getUserSettingsFromServer,qrcode:"+qrCode);
							obj.put("checkMileageId", qrCode);		// 자신의 아이디 사용할 것.. 이전 사용자이다
							obj.put("activateYn", "Y");
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
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
							if(responseCode==200 || responseCode==204){
								// 조회한 결과를 처리.
								theData1(in);
								//		//	//		//	//			theData1(in);  // 성공시 -> 도메인에 담아서 어쩌구.. 호출
								//									Log.e(TAG,"S");
							}else{
								showMSG();
//								Toast.makeText(MemberStoreInfoPage.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
							}
						}catch(Exception e){ 
							e.printStackTrace();
						}
					}
				}
		).start();
	}
	
	public void theData1(InputStream in){
		Log.d(TAG,"theData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		settings = new CheckMileageMemberSettings(); // 객체 생성
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * checkMileageMember":{"merchantId":"m1","password":"m1","name":"내가짱","companyName":"우수기업",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"아지트 에티서","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
//		Log.d(TAG,"내 설정 상세정보::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				// 데이터를 전역 변수 도메인에 저장하고  설정에 저장..
				try{
					settings.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){
					settings.setEmail("");
				}
				try{
					settings.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){
					settings.setBirthday("");
				}
				try{
					settings.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){
					settings.setGender("");
				}
				try{
					settings.setReceive_notification_yn(jsonobj2.getString("receive_notification_yn"));				
				}catch(Exception e){
					settings.setReceive_notification_yn("");
				}
//				showInfo();
				setUserSettingsToPrefs();		// 설정에 전달 및 저장
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	}
	
	/*
	 * 서버에서 받은 설정 정보를 모바일 내 설정 정보에 저장한다.
	 * 
	 * 설정 도메인에넣어서 설정 페이지로 전달해주고 함수 호출해주면 좋겠지
	 * 
	 * 아마도 모든 정보.
	 * CHECKMILEAGE_ID  PASSWORD  PHONE_NUMBER  EMAIL  BIRTHDAY
	 * GENDER  LATITUDE  LONGITUDE  DEVICE_TYPE  REGISTRATION_ID
	 * RECEIVE_NOTIFICATION_YN   ACTIVATE_YN  MODIFY_DATE  REGISTER_DATE
	 * 
	 * 이중 사용할 정보
	 *   EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
	 *   4가지. -> 를 설정 도메인에 저장한 후 설정에서 세팅해준다..
	 */
	public void setUserSettingsToPrefs(){
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveUpdateYn = sharedPrefCustom.edit();		// 공용으로 비번도 저장해 준다.
		
//		saveUpdateYn.putString("updateYN", "y");
		
		saveUpdateYn.putString("server_birthday", settings.getBirthday());
		saveUpdateYn.putString("server_email", settings.getEmail());
		saveUpdateYn.putString("server_gender", settings.getGender());
//		saveUpdateYn.putString("server_receive_notification_yn", settings.getReceive_notification_yn());

//		saveUpdateYn.putString("server_birthday", "1999-02-12");
//		saveUpdateYn.putString("server_email", "a1@b.c");
//		saveUpdateYn.putString("server_gender", "여성");
//		saveUpdateYn.putBoolean("server_receive_notification_yn", false);
		saveUpdateYn.commit();
//		PrefActivityFromResource.mySettings = settings;
//		PrefActivityFromResource.saveServerSettingsToPrefs(); // db에 저장해야 하나? 설정탭으로 갈때? 업뎃 필요 여부 체크? 
		// 공용 셰어드에 저장을 해두고, 공용 셰여드에 여부 저장도 해두고, 해당 설정 들어갔을때 여부 저장 값을 읽어서 y이면 나머지 값 4개를 읽어서 자체에 저장하고, 아님말고.
		// ...
	}
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	
	/*
	 * 가맹점 전체 목록 날라오는지 테스트용.  --  사용 x
	 * http://"+serverName+"/checkMileageMerchant/selectMerchantList
	 * 
	 */
	public void test1(){		// 서버로부터 설정 정보를 받는다.  아이디를 사용.모든데이터.  CheckMileageMember
		Log.d(TAG, "test1");
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantList";
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("activateYn", "Y");
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
						try{
//							URL postUrl2 = new URL("http://"+serverName+"/checkMileageMerchant/selectMerchantList");
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
							if(responseCode==200 || responseCode==204){
								// 조회한 결과를 처리.
								theData2(in);
								//		//	//		//	//			theData1(in);  // 성공시 -> 도메인에 담아서 어쩌구.. 호출
								//									Log.e(TAG,"S");
							}else{
								showMSG();		//error_message
//								Toast.makeText(MemberStoreInfoPage.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
							}
						}catch(Exception e){ 
							e.printStackTrace();
						}
					}
				}
		).start();
	}
	public void theData2(InputStream in){
		Log.d(TAG,"theData1");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		settings = new CheckMileageMemberSettings(); // 객체 생성
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * checkMileageMember":{"merchantId":"m1","password":"m1","name":"내가짱","companyName":"우수기업",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"아지트 에티서","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		Log.d(TAG,"get data ::"+builder.toString());
		
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		Log.d(TAG,"max::"+max);
//		try {
//			entries1 = new ArrayList<CheckMileageMerchants>(max);
//			if(max>0){
//				for ( int i = 0; i < max; i++ ){
//					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchant");		// 대소문자 주의
//					/*
//					 *
//					 * 가맹점 ID, 가맹점 이름, 가맹점 URL(이미지 보여주기 용도)
//					 *
//					private String idCheckMileageMileages;					// 고유 식별 번호.!!	
//					private String mileage;											
//					private String activateYN;
//					private String modifyDate;
//					private String registerDate;
//					private String checkMileageMembersCheckMileageID;		
//					private String checkMileageMerchantsMerchantID;			merchantId
//					private String merchantName;							companyName
//					private String merchantImg;								profileImageUrl
//					private Bitmap merchantImage;
//					 *
//					 *  workPhoneNumber  zipCode01  address01  address02
//					 *   latitude  longitude								다음페이지로 갈때 따로 조회 안하도록 가지고 있으면?..어차피 조회 해야 하는듯.
//					 *
//					 * 가맹점 정보로 갈때 필요한 것들.
//					 * intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		merchantId
//					 *	intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());							상세 내역보기 위해 필요.. - 조회 필요.. 수정? 내아디
//					 *	intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());														가맹점에 대한 내 마일리지 - 조회 필요.. 수정할것. 
//					 *
//					 */
//					//  merchantId,  companyName,  profileImageUrl,  
//					// 객체 만들고 값 받은거 넣어서 저장..  저장값:  가맹점아이디. 가맹점 이름, 프로필 URL
//					entries1.add(
//							new CheckMileageMerchants(
//									jsonObj.getString("merchantId"),
//									jsonObj.getString("companyName"),
//									jsonObj.getString("profileImageUrl"),
//									jsonObj.getString("idCheckMileageMileages"),
//									jsonObj.getString("mileage")
//							)
//					);
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}finally{
//			new backgroundGetMerchantInfo().execute();	// getMerchantInfo(entries1); 를 비동기로 실행
//		}
		
		
//		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
//		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
//			try {
//				jsonObject = new JSONObject(tempstr);
//				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
//				// 데이터를 전역 변수 도메인에 저장하고  설정에 저장..
//				try{
//					settings.setEmail(jsonobj2.getString("email"));				
//				}catch(Exception e){
//					settings.setEmail("");
//				}
//				try{
//					settings.setBirthday(jsonobj2.getString("birthday"));				
//				}catch(Exception e){
//					settings.setBirthday("");
//				}
//				try{
//					settings.setGender(jsonobj2.getString("gender"));				
//				}catch(Exception e){
//					settings.setGender("");
//				}
//				try{
//					settings.setReceive_notification_yn(jsonobj2.getString("receive_notification_yn"));				
//				}catch(Exception e){
//					settings.setReceive_notification_yn("");
//				}
////				showInfo();
//				setUserSettingsToPrefs();		// 설정에 전달 및 저장
//			} catch (JSONException e) {
//				e.printStackTrace();
//			} 
	}
	
	
	
	
	
	
	/*
	 * Bitmap 이미지 리사이즈
	 * Src : 원본 Bitmap
	 * newHeight : 새로운 높이
	 * newWidth : 새로운 넓이
	 * 참고 소스 : http://skyswim42.egloos.com/3477279 ( webview 에서 capture 화면 resizing 하는 source 도 있음 )
	 */
	private BitmapDrawable BitmapResizePrc( Bitmap Src, float newHeight, float newWidth)
	{
		BitmapDrawable Result = null;
		int width = Src.getWidth();
		int height = Src.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// rotate the Bitmap 회전 시키려면 주석 해제!
		//matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(Src, 0, 0, width, height, matrix, true);

		// check
		width = resizedBitmap.getWidth();
		height = resizedBitmap.getHeight();
//		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );

		// make a Drawable from Bitmap to allow to set the BitMap
		// to the ImageView, ImageButton or what ever
		Result = new BitmapDrawable(resizedBitmap);
		return Result;
	}

	
	
	
	
	
	
	
}
