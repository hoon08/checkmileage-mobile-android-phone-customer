package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.kr.bettersoft.domain.CheckMileageMemberSettings;
import com.pref.DummyActivity;
import com.pref.PrefActivityFromResource;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.CheckBoxPreference;
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
	
	int app_end = 0;			// �ڷΰ��� ��ư���� ������ 2������ ��������
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	
	SharedPreferences sharedPrefCustom;
	
	// ���� ���� ������ ������.
	CheckMileageMemberSettings settings;
	
	//	MyLocationOverlay2 mLocation;
	int myLat = 0;
	int myLon = 0;
	
	static Bitmap savedBMP = null;
	
	/** Called when the activity is first created. */
	static Bitmap bmp =null;
	 static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";
	// �ڵ鷯 ���
	static Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		Bundle b = msg.getData();
    		int testData =  b.getInt("testData");		// ���� ���� ������ 0 �� ��������.
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
	     *  QR ũ�⸦ ȭ�鿡 ���߱� ���� ȭ�� ũ�⸦ ����.
	     */
	    Log.i("qrCode : ", "" + qrCode);
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
	    float fqrSize = 0;
	    if(screenWidth < screenHeight ){
	    	fqrSize = (float) (screenWidth * 1.0);
	    }else{
	    	fqrSize = (float) (screenHeight * 1.0);
	    }
		final int qrSize = (int) fqrSize;		// ���� ���� ����
		
		if(savedBMP==null){
			Log.e(TAG,"savedBMP is null");
		}else{
			Log.e(TAG,"savedBMP is not null");
		}
		
		/*
		 *  QR �ڵ带 �޾ƿ�.  ���� ���������� ���� ����
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
	    
	    getUserSettingsFromServer();
	}
	
	// ������ QR�ڵ� �̹����� DB�� �����Ѵ�.
	public void saveBMPtoDB(Bitmap bmp){
		Log.e(TAG,"saveBMPtoDB");
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
	 * QR �̹��� ����. ��ü ���̺귯�� ����Ͽ� QR �̹��� ����
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
							
							// ��� 1. ��ȯ -> ��ȯ -> ���ڵ�  :: null pointer ���� (���丮���� ���ڵ��ϸ� null �� ����)
//							String st =  bitMatrix.toString();
//							byte[] data  = st.getBytes();
//							Log.i("MyQRPageActivity", "lv4");
//							ByteArrayInputStream in = new ByteArrayInputStream(data);
//							bmp2 = BitmapFactory.decodeStream(in); 
							
							// ��� 2.��ü �޼ҵ� ȣ�� -> class not def ����.
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
	 *  �ݱ� ��ư 2�� ������ ���� ��.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "MyQRPage finish");		
		if(app_end == 1){
			Log.e(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, "�ڷΰ��� ��ư�� �ѹ��� ������ ����˴ϴ�.", Toast.LENGTH_SHORT).show();
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
			criteria.setPowerRequirement(Criteria.POWER_LOW); // ���� �Ҹ���
			criteria.setAltitudeRequired(false); // ��
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // �ӵ�
			criteria.setCostAllowed(true); // ������ ���
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// ����ġ�� ��ǥ ȹ�� *** �α׿�
				myLon = (int) (location.getLongitude()*1000000);	
				Log.e("runOnFirstFix", "location1:"+myLat+", "+myLon);			// 37529466 126921069
				updateLocationToServer(Integer.toString(myLat), Integer.toString(myLon));
			}else{
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.e(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// ����ġ�� ��ǥ ȹ�� *** �α׿�
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
	 * ������� ��ġ ������ ���� �Ѵ�.
	 * �� ����� 'SUCCESS' �� 'FAIL' �� ��Ʈ������ ��ȯ �Ѵ�.
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
			int todayYear = 0;						// ���� -  �� �� �� �� ��
			int todayMonth = 0;
			int todayDay = 0;
			int todayHour = 0;
			int todayMinute = 0;
			todayYear = c.get(Calendar.YEAR);
			todayMonth = c.get(Calendar.MONTH)+1;			// ������ 0���� �����̴ϱ� +1 ���ش�.
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
								// �ڽ��� ���̵� �־ ��ȸ
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
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
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
			Log.e(TAG,"�̹� ������Ʈ ��..");
		}
	}
	
	
	@Override			// �� ��Ƽ��Ƽ(��Ʈ��)�� ����ɶ� ����. (��Ƽ��Ƽ�� �Ѿ�� �����)
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 *    ���� ��� ������ ���� �� ���� ����� ���� ������..
	 *     �� ����� ���� ��� ���� ���� ����2 �������� �̵��Ѵ�.
	 *     
	 *     ���� ����. ���� ������� ��� �����κ��� ����� ���� ������ �����ͼ� ������� ���� ������ ���Խ�Ų��..
	 *     ����� ��� �нǽ� ���� ������ �缳ġ.. �� ���� �޴´�. �׷� ��� �ʱ�ȭ.
	 *      (�нǽ� ���� �����Ƿ�) ������ ���� ������ ���ϴ°� ���� �� ��..
	 *     
	 *     checkMileageMemberController selectMemberInformation 
	 *     
	 * 		checkMileageId activateYn  // checkMileageMember
	 *     CheckMileageMember
	 *     
	 *     �޾Ƽ� ���� ������ �������� �����صθ� ������
	 */
	public void getUserSettingsFromServer(){		// �����κ��� ���� ������ �޴´�.  ���̵� ���.��絥����.  CheckMileageMember
		Log.i(TAG, "getUserSettingsFromServer");
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";

		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// �ڽ��� ���̵� �־ ��ȸ
							obj.put("checkMileageId", qrCode);		// �ڽ��� ���̵� ����� ��.. ���� ������̴�
							obj.put("activateYn", "Y");
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							if(responseCode==200 || responseCode==204){
								// ��ȸ�� ����� ó��.
								theData1(in);
								//		//	//		//	//			theData1(in);  // ������ -> �����ο� ��Ƽ� ��¼��.. ȣ��
								//									Log.e(TAG,"S");
							}else{
//								Toast.makeText(MemberStoreInfoPage.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		settings = new CheckMileageMemberSettings(); // ��ü ����
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * checkMileageMember":{"merchantId":"m1","password":"m1","name":"����¯","companyName":"������",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"����Ʈ ��Ƽ��","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		Log.d(TAG,"�� ���� ������::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				// �����͸� ���� ���� �����ο� �����ϰ�  ������ ����..
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
				setUserSettingsToPrefs();		// ������ ���� �� ����
			} catch (JSONException e) {
				e.printStackTrace();
			} 

	}
	
	/*
	 * �������� ���� ���� ������ ����� �� ���� ������ �����Ѵ�.
	 * 
	 * ���� �����ο��־ ���� �������� �������ְ� �Լ� ȣ�����ָ� ������
	 * 
	 * �Ƹ��� ��� ����.
	 * CHECKMILEAGE_ID  PASSWORD  PHONE_NUMBER  EMAIL  BIRTHDAY
	 * GENDER  LATITUDE  LONGITUDE  DEVICE_TYPE  REGISTRATION_ID
	 * RECEIVE_NOTIFICATION_YN   ACTIVATE_YN  MODIFY_DATE  REGISTER_DATE
	 * 
	 * ���� ����� ����
	 *   EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
	 *   4����. -> �� ���� �����ο� ������ �� �������� �������ش�..
	 */
	public void setUserSettingsToPrefs(){
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor saveUpdateYn = sharedPrefCustom.edit();		// �������� ����� ������ �ش�.
		
//		saveUpdateYn.putString("updateYN", "y");
		
//		saveUpdateYn.putString("server_birthday", settings.getBirthday());
//		saveUpdateYn.putString("server_email", settings.getEmail());
//		saveUpdateYn.putString("server_gender", settings.getGender());
//		saveUpdateYn.putString("server_receive_notification_yn", settings.getReceive_notification_yn());

//		saveUpdateYn.putString("server_birthday", "1999-02-12");
//		saveUpdateYn.putString("server_email", "a1@b.c");
//		saveUpdateYn.putString("server_gender", "����");
//		saveUpdateYn.putBoolean("server_receive_notification_yn", false);
		
//		saveUpdateYn.commit();
		
		
		
//		PrefActivityFromResource.mySettings = settings;
//		PrefActivityFromResource.saveServerSettingsToPrefs(); // db�� �����ؾ� �ϳ�? ���������� ����? ���� �ʿ� ���� üũ? 
		// ���� �ξ�忡 ������ �صΰ�, ���� �ο��忡 ���� ���嵵 �صΰ�, �ش� ���� ������ ���� ���� ���� �о y�̸� ������ �� 4���� �о ��ü�� �����ϰ�, �ƴԸ���.
		// ...
	}
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
