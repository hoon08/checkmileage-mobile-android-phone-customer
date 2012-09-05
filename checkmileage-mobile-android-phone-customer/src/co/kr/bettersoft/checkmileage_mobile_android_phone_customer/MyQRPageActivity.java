package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
/*
 *  내 QR 보기 화면
 *  
 *  그리고 onresume 에 좌표 업뎃 기능 을 넣어 어플 실행 또는 내 QR 코드를 볼때마다 현재 사용자 위치를 서버에 업뎃 하도록 하였음.
 *   화면을 자주 오가며 업뎃 자꾸 시키는것을 방지하기 위해  플래그 값을 둠.  빨라서 대부분은 업뎃을 함..
 *  
 *  
 *  
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pref.DummyActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class MyQRPageActivity extends Activity {
	String TAG = "MyQRPageActivity";
	String controllerName="";
	String methodName="";
	int responseCode= 0;
	int isUpdating = 0;
	
	int app_end = 0;			// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	
	//	MyLocationOverlay2 mLocation;
	int myLat = 0;
	int myLon = 0;
	
	static Bitmap savedBMP = null;
	
	/** Called when the activity is first created. */
	static Bitmap bmp =null;
	 static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";
	// 핸들러 등록
	static Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int testData =  b.getInt("testData");		// 값을 넣지 않으면 0 을 꺼내었다.
    		if(testData==1234){
    			imgView.setImageBitmap(bmp);
    		}
    	}
    };
    
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
	    float fqrSize = 0;
	    if(screenWidth < screenHeight ){
	    	fqrSize = screenWidth;
	    }else{
	    	fqrSize = screenHeight;
	    }
		final int qrSize = (int) fqrSize;		// 작은 쪽을 선택
		
		if(savedBMP==null){
			Log.e(TAG,"savedBMP is null");
		}else{
			Log.e(TAG,"savedBMP is not null");
		}
		
		/*
		 *  QR 코드를 받아옴.  구글 웹페이지를 통한 생성
		 */
	    new Thread(
        		new Runnable(){
        			public void run(){
        				if(savedBMP==null){
        					bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode); 
        					saveBMPtoDB(bmp);
        				}else{
        					bmp = savedBMP;
        				}
//        				 bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode); 
//        				 bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs=500x500&choe=UTF-8&chld=H&chl=test1234"); 
//        				    Log.w("MyQRPageActivity", "bmp size getHeight" + bmp.getHeight()); 
//        					Log.w("MyQRPageActivity", "bmp size getWidth" + bmp.getWidth());  
        						Message message = handler.obtainMessage();
        						Bundle b = new Bundle();
        						b.putInt("testData", 1234);
        						message.setData(b);
        						handler.sendMessage(message);
        			}
        		}
        ).start();
	}
	
	// 생성한 QR코드 이미지를 DB에 저장한다.
	public void saveBMPtoDB(Bitmap bmp){
		Log.e(TAG,"saveBMPtoDB");
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
	public static void CreateQR() throws WriterException, IOException {
		new Thread(
				new Runnable(){
					public void run(){
						//	        				 bmp = "";
						QRCodeWriter qrCodeWriter = new QRCodeWriter();
						String text = "test1234";
						try {
							text = new String(text.getBytes("UTF-8"), "ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,
									100, 100);
							
							// 방법 1. 변환 -> 변환 -> 디코딩  :: null pointer 에러 (팩토리에서 디코딩하면 null 이 나옴)
//							String st =  bitMatrix.toString();
//							byte[] data  = st.getBytes();
//							Log.i("MyQRPageActivity", "lv4");
//							ByteArrayInputStream in = new ByteArrayInputStream(data);
//							bmp2 = BitmapFactory.decodeStream(in); 
							
							// 방법 2.자체 메소드 호출 -> class not def 에러.
//							try {
//								MatrixToImageWriter.writeToFile(bitMatrix, "png", new File("qrcode.png"));
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
						} catch (WriterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//	        					Log.w("MyQRPageActivity", "bmp size getHeight" + bmp.getHeight()); 
						//	        					Log.w("MyQRPageActivity", "bmp size getWidth" + bmp.getWidth());  
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("testData", 2222);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
//        Bitmap bm=null; 
//        BitmapDrawable bmd = new BitmapDrawable(in); 
//        //	        	in.read(data);		// null pointer exception
////        Bitmap bitmap = BitmapFactory.decodeStream(in);  
//        bm = bmd.getBitmap(); 
////        Log.i("MyQRPageActivity", "6-1"+bitmap.getHeight());
////        MatrixToImageWriter.writeToFile(bitMatrix, "png", new File("qrcode.png"));	// cant find class exception
	}
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		myLocationIs();
	}
	
	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "MyQRPage finish");		
		if(app_end == 1){
			Log.e(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
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
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소리량
			criteria.setAltitudeRequired(false); // 고도
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // 속도
			criteria.setCostAllowed(true); // 금전적 비용
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득 *** 로그용
				myLon = (int) (location.getLongitude()*1000000);	
				Log.e("runOnFirstFix", "location1:"+myLat+", "+myLon);			// 37529466 126921069
				updateLocationToServer(Integer.toString(myLat), Integer.toString(myLon));
			}else{
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.e(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득 *** 로그용
					myLon = (int) (location.getLongitude()*1000000);	
					Log.e("runOnFirstFix", "location2:"+myLat+", "+myLon);			// 37529466 126921069
					updateLocationToServer(Integer.toString(myLat), Integer.toString(myLon));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
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
			
			Calendar c = Calendar.getInstance();
			int todayYear = 0;						// 지금 -  년 월 일 시 분
			int todayMonth = 0;
			int todayDay = 0;
			int todayHour = 0;
			int todayMinute = 0;
			todayYear = c.get(Calendar.YEAR);
			todayMonth = c.get(Calendar.MONTH)+1;			// 꺼내면 0부터 시작이니까 +1 해준다.
			todayDay = c.get(Calendar.DATE);
			todayHour = c.get(Calendar.HOUR_OF_DAY);
			todayMinute = c.get(Calendar.MINUTE);
			String tempMonth = Integer.toString(todayMonth+1);
			String tempDay = Integer.toString(todayDay);
			String tempHour = Integer.toString(todayHour);
			String tempMinute = Integer.toString(todayMinute);
			
			if(tempMonth.length()==1) tempMonth = "0"+tempMonth;
			if(tempDay.length()==1) tempDay = "0"+tempDay;
			if(tempHour.length()==1) tempHour = "0"+tempHour;
			if(tempMinute.length()==1) tempMinute = "0"+tempMinute;
			
			final String todays = todayYear+"-"+tempMonth+"-"+tempDay+" "+tempHour+":"+tempMinute;
//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// 자신의 아이디를 넣어서 조회
//								Log.e(TAG,"checkMileageId::"+qrCode);
//								Log.e(TAG,"latitude::"+myLat);
//								Log.e(TAG,"longitude::"+myLon);
//								Log.e(TAG,"activateYn::"+"Y");
//								Log.e(TAG,"modifyDate::"+todays);
								
								obj.put("checkMileageId", qrCode);
								obj.put("latitude", myLat);
								obj.put("longitude", myLon);
								obj.put("activateYn", "Y");
								obj.put("modifyDate", todays);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
							try{
								URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
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
			Log.e(TAG,"이미 업데이트 중..");
		}
	}
	
	
	@Override			// 이 액티비티(인트로)가 종료될때 실행. (액티비티가 넘어갈때 종료됨)
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	

}
