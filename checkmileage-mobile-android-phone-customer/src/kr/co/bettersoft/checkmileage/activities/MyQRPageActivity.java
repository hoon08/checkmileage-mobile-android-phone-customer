package kr.co.bettersoft.checkmileage.activities;
/*
 *  �� QR ���� ȭ��
 *  
 *  �׸��� onresume �� ��ǥ ���� ��� �� �־� ���� ���� �Ǵ� �� QR �ڵ带 �������� ���� ����� ��ġ�� ������ ���� �ϵ��� �Ͽ���.
 *   ȭ���� ���� ������ ���� �ڲ� ��Ű�°��� �����ϱ� ����  �÷��� ���� ��.  ���� ��κ��� ������ ��..
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

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;




import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class MyQRPageActivity extends Activity {
	String TAG = "MyQRPageActivity";
	
	// ���� ��� �� 
	String controllerName="";
	String methodName="";
	String serverName = CommonUtils.serverNames;
	URL postUrl2 ;
	HttpURLConnection connection2;
	int responseCode= 0;
	int isUpdating = 0;
	int app_end = 0;			// �ڷΰ��� ��ư���� ������ 2������ ��������
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;

	// �� ��ǥ ������
	int myLat = 0;
	int myLon = 0;

	// QR ����
	static Bitmap savedBMP = null;				// db ����� �̹��� (���޹���)
	int qrSize =300;							// QR�̹��� ũ��
	int deviceSize = 0;		
	static Bitmap bmp =null;					// �̹��� �����뵵
	 static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";			// qr ���̵�
	
	// �ڵ鷯 ���
	Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int showQR =  b.getInt("showQR");		// ���� ���� ������ 0 �� ��������.
    		if(showQR==1){
    			imgView.setImageBitmap(bmp);		// ȭ�鿡 QR �����ش�.
    		}
    	}
    };
    
    public Bitmap createQRself(String qrCode){		// ��ü QR ����
    	try { 
    	    // generate a 200x200 QR code 
    	    Bitmap bm = encodeAsBitmap(qrCode, BarcodeFormat.QR_CODE, 200, 200); 
    	    if(bm != null) { 
    	        Log.d(TAG,"S to createQRself");
    	        return bm;
    	    } 
    	} catch (Exception e) { 
    		e.printStackTrace();
    	}
		return null;
    }
    
    
    // ��ü QR ���� �뵵
    private static final int WHITE = 0xFFFFFFFF;  
    private static final int BLACK = 0xFF000000;
    static Bitmap encodeAsBitmap(String contents,
    		BarcodeFormat format, 
    		int desiredWidth,         
    		int desiredHeight) throws Exception {
    	MultiFormatWriter writer = new MultiFormatWriter();   
    	BitMatrix result = writer.encode(contents, format, desiredWidth, desiredHeight, null);   
    	int width = result.getWidth();  
    	int height = result.getHeight();  
    	int[] pixels = new int[width * height];   
    	// All are 0, or black, by default  
    	for (int y = 0; y < height; y++) {
    		int offset = y * width;    
    		for (int x = 0; x < width; x++) {   
    			pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;   
    			}   
    		}    
    	Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
    	bitmap.setPixels(pixels, 0, width, 0, 0, width, height); 
    	return bitmap; 
    	}
   
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_qr_page);
	    imgView = (ImageView)findViewById(R.id.myQRCode);
	    /*
	     *  QR ũ�⸦ ȭ�鿡 ���߱� ���� ȭ�� ũ�⸦ ����.
	     */
	    Log.i("qrCode : ", "" + qrCode);
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
		/*
		 *  QR �ڵ带 �޾ƿ�.  ���� ���������� ���� ���� --> ��ü ���̺귯�� ���� �غ��� �ȵǸ� �����.
		 */
	    new Thread(
        		new Runnable(){
        			public void run(){
        				if(savedBMP==null){
        					bmp = createQRself(qrCode);		// ��ü ���̺귯�� ����Ͽ� ����.
        					if(bmp==null){
        						Log.d(TAG,"bmp1==null");
        						bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode);		// �� ����Ͽ� ������ 
        						if(bmp==null){
        							Log.d(TAG,"bmp2==null");
        							finish();
        						}else{
        							saveBMPtoDB(bmp);
        						}
        						// QR �̹��� ���� ����. ó�� �ʿ� *** no qr img �� ���� �ҵ�.? �����?;
        					}else{
        						saveBMPtoDB(bmp);
        					}
        				}else{
        					bmp = savedBMP;
        				}
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
	
	// ������ QR�ڵ� �̹����� DB�� �����Ѵ�.
	public void saveBMPtoDB(Bitmap bmp){
		Log.d(TAG,"saveBMPtoDB");
		SQLiteDatabase db = null;
		db= openOrCreateDatabase( "sqlite_carrotDB.db",             
		          SQLiteDatabase.CREATE_IF_NECESSARY ,null );
		String data_key="";
		String data_value="";
		
		// BMP -> ���ڿ� 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();   
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
		byte[] b = baos.toByteArray();  
		data_value = Base64.encodeToString(b, Base64.DEFAULT); 

		// ��ȸ
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
	 * QR �̹����ޱ�. url ����Ͽ� ���� ������ �޾ƿ���.
	 */
	static Bitmap downloadBitmap(String url) {    
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");    
		final HttpGet getRequest = new HttpGet(url);    
		try {        
			HttpResponse response = client.execute(getRequest);        
			final int statusCode = response.getStatusLine().getStatusCode();        
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
		} catch (Exception e) {        
			getRequest.abort();        
			Log.w("MyQRPageActivity", "Error while retrieving bitmap from " + url +"  " + e.toString());    
		} finally {        
			if (client != null) {            
				client.close();        
			}    
		}    
		return null;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
//		myLocationIs();
	}
	
	/*
	 *  �ݱ� ��ư 2�� ������ ���� ��.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.d("MainTabActivity", "MyQRPage finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
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
	
	
	// ������ �� ��ġ ����.
	public void myLocationIs(){
		try{
			LocationManager  lm;
			Location location;
			String provider;
			String bestProvider;
			lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			provider = LocationManager.GPS_PROVIDER;
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // ��Ȯ��
			criteria.setPowerRequirement(Criteria.POWER_LOW); // ���� �Һ�
			criteria.setAltitudeRequired(false); // ��
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // �ӵ�
			criteria.setCostAllowed(true); // ������ ���
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// ����ġ�� ��ǥ ȹ��
				myLon = (int) (location.getLongitude()*1000000);	
				Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
				new backgroundUpdateLocationToServer().execute();	// �񵿱�� ������ ��ġ ����		
			}else{
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.d(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// ����ġ�� ��ǥ ȹ��
					myLon = (int) (location.getLongitude()*1000000);	
					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);			
					new backgroundUpdateLocationToServer().execute();	// �񵿱�� ��ȯ	
				}
			}
		}catch(Exception e){
			Log.w(TAG,"fail to update my location to server");
		}
	}
	
	
	
	
	// �񵿱�λ������ ��ġ ������ ����
	public class backgroundUpdateLocationToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyLocationtoServer");
//        		updateLocationToServer_pre();
        		updateLocationToServer();
			return null; 
		}
	}
	
	/*
	 * ������� ��ġ ������ ���� �Ѵ�.
	 * �� ����� 'SUCCESS' �� 'FAIL' �� ��Ʈ������ ��ȯ �Ѵ�.
	 * //checkMileageMemberController  updateMemberLocation   checkMileageMember  
	 *	// checkMileageId  latitude  longitude  activateYn  modifyDate
	 */
//	public void updateLocationToServer_pre(){
//		new Thread(
//				new Runnable(){
//					public void run(){
//						Log.d(TAG,"updateLocationToServer_pre");
//						try{
//							Thread.sleep(CommonUtils.threadWaitngTime);
//						}catch(Exception e){
//						}finally{
//							if(CommonUtils.usingNetwork<1){
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork +1;
//								updateLocationToServer();
//							}else{
//								updateLocationToServer_pre();
//							}
//						}
//					}
//				}
//			).start();
//	}
	public void updateLocationToServer(){
		if(isUpdating==0){
			isUpdating = 1;
			Log.i(TAG, "updateLocationToServer");
			controllerName = "checkMileageMemberController";
			methodName = "updateMemberLocation";
			final String myLat2 = Integer.toString(myLat);
			final String myLon2 = Integer.toString(myLon);
//			Log.e(TAG,todays+"//"+myLat+"//"+myLon);
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								// �ڽ��� ���̵� �־ ��ȸ
//								Log.d(TAG,"checkMileageId::"+qrCode);
//								Log.d(TAG,"latitude::"+myLat);
//								Log.d(TAG,"longitude::"+myLon);
//								Log.d(TAG,"activateYn::"+"Y");
//								Log.d(TAG,"modifyDate::"+todays);
								obj.put("checkMileageId", qrCode);
								obj.put("latitude", myLat2);
								obj.put("longitude", myLon2);
								obj.put("activateYn", "Y");
								
								String nowTime = getNow();
								
								obj.put("modifyDate", nowTime);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMember\":" + obj.toString() + "}";
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
//								connection2.connect();
								Thread.sleep(200);
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes("UTF-8"));
								os2.flush();
								Thread.sleep(200);
//								System.out.println("postUrl      : " + postUrl2);
//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
//								InputStream in =  connection2.getInputStream();
//								os2.close();
								// ��ȸ�� ����� ó��.
								if(responseCode==200 || responseCode==204){
//									Log.d(TAG,"S");
								}
//								connection2.disconnect();
							}catch(Exception e){ 
//								connection2.disconnect();
								Log.d(TAG,"updateLocationToServer->fail");
							}finally{
								isUpdating = 0;
//								CommonUtils.usingNetwork = CommonUtils.usingNetwork -1;
//								if(CommonUtils.usingNetwork < 0){	// 0 ���� ������ �ʰ�
//									CommonUtils.usingNetwork = 0;
//								}
							}
						}
					}
			).start();
		}else{
			Log.w(TAG,"already updating..");
		}
	}
	
	// ���� �ð�
	public String getNow(){
		// �ϴ� ����.
		Calendar c = Calendar.getInstance();
		int todayYear = 0;						// ���� -  �� �� �� �� ��
		int todayMonth = 0;
		int todayDay = 0;
		int todayHour = 0;
		int todayMinute = 0;
		int todaySecond = 0;
		todayYear = c.get(Calendar.YEAR);
		todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
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
	@Override
	public void onDestroy(){
		super.onDestroy();
//		try{
//			if(connection2!=null){
//				connection2.disconnect();
//			}
//		}catch(Exception e){}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
