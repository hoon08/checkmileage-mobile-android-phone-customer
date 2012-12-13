package kr.co.bettersoft.checkmileage.activities;
// ������ ���� ����

/*
 * ������ ���̵�, �� ���ϸ���  <-- ���� ȭ�鿡�� �޾ƿ´�..  (���⼭ ���ϴ� ���� ȭ���� �� ���ϸ��� ��� �Ǵ� ������ ���)
 * ������ �̹���URL, ������ �̸�, ��ǥ�� �̸�, ��ȭ��ȣ, �ּ�, ��ǥ1,2(��ǥ �̸� ������ ���� ���� �ȹ޾Ƶ� �ȴ�..), �� ����
 * ������ ������� ��Ϻ���, ��ȭ�ɱ�, �޴�/���� ���� ��..
 * 
 * 
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;

import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
//import kr.co.bettersoft.checkmileage.domain.CheckMileageMileage;

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;


//import android.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
//import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MemberStoreInfoPage extends Activity {
	String TAG = "MemberStoreInfoPage";
	public static final int VISIBLE = 0x00000000;
	public static final int INVISIBLE = 0x00000004;
	public static final int GONE = 0x00000008;

	Button callBtn ;
	Button mapBtn;
	Button logListBtn;
	Button serviceListBtn;
	Button closeBtn;

	int responseCode = 0;
	String controllerName ="";
	String methodName ="";
	String serverName = CommonUtils.serverNames;

	public CheckMileageMerchants merchantData = new CheckMileageMerchants();	// ��� �����ؼ� �����ֱ� ���� ������.
	String myMileage = "";
	String merchantId ="";
	String idCheckMileageMileages ="";
//	String imageFileStr="";			// ���ڿ��� �ٲ� �̹���
//	Bitmap imageFile= null;			// �̹��� ����
	
	
	String imgDomain = CommonUtils.imgDomain; 					// Img �����ö� ���ϸ� ���� ��� �տ� ���� ������.   
	Bitmap bm = null;
	String latatude = "";
	String longitude = "";

	URL postUrl2 = null;
	HttpURLConnection connection2 = null;

	int error=0;
	int reTry = 3;			 
	String tmpstr = "";
	String tmpstr2 = "";
	int maxPRstr = 200;					// ȭ�鿡 ������ �Ұ� ���� �ִ� ���ڼ�. �Ѿ�� �ڸ��� ... ���� ǥ������.

//	float fImgSize = 0;

	// �����
	ProgressBar pb1;		// �ߴ� �ε� �����

	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ°���� ȭ�鿡 �ѷ��ش�.
				if(b.getInt("showYN")==1){
					// merchantData ���� ������ ������ ȭ�鿡 �����Ѵ�.			// titleImg , name , companyName , phone , addr , pr ,,
					TextView mileage = (TextView)findViewById(R.id.mileage);
//					TextView type = (TextView)findViewById(R.id.type);
					TextView member_store_title = (TextView)findViewById(R.id.member_store_title);
					ImageView titleImg = (ImageView)findViewById(R.id.prImage);
					TextView name = (TextView)findViewById(R.id.name);	
					TextView phone = (TextView)findViewById(R.id.phone);
					TextView pre_phone = (TextView)findViewById(R.id.pre_phone);
					String tmpstr1 = pre_phone.getText()+" : ";
					pre_phone.setText(tmpstr1);
					TextView addr = (TextView)findViewById(R.id.addr);	
					TextView pr = (TextView)findViewById(R.id.pr);
					//					TextView companyName = (TextView)findViewById(R.id.merchantName2);	
					hidePb();
					mileage.setText(myMileage);
					mileage.setVisibility(VISIBLE);
					if(merchantData.getWorkPhoneNumber().length()>0){
						pre_phone.setVisibility(VISIBLE);
					}
					//					type.setText(text);
					//					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), fImgSize, (float)(fImgSize*1.5));  // height, width
					//					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), 400, 700);  		

					// set the Drawable on the ImageView
					//					titleImg.setImageDrawable(bmpResize);	
					titleImg.setImageBitmap(merchantData.getMerchantImage());		
					latatude = merchantData.getLatitude();
					longitude = merchantData.getLongtitude();
					tmpstr = getString(R.string.representative);
					name.setText(tmpstr+" : "+merchantData.getName());
					tmpstr = getString(R.string.phone_num);
					//					phone.setText(tmpstr+" : "+merchantData.getWorkPhoneNumber());
					phone.setText(merchantData.getWorkPhoneNumber());
					tmpstr = getString(R.string.addr);
					addr.setText(tmpstr+" : "+merchantData.getAddress01());
					//					tmpstr = getString(R.string.pr_str);	// �տ� �Ұ�: �� ������ ����.
					pr.setText(merchantData.getPrSentence());
					member_store_title.setText(merchantData.getCompanyName());			// ��� Ÿ��Ʋ �ȿ� ������ �̸�.
					//					tmpstr = getString(R.string.shop_name);
					//					companyName.setText(tmpstr+" : "+merchantData.getCompanyName());	// ������ �̸� - ��� Ÿ��Ʋ �ٷ� ��ü.

					callBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							setCallingPhoneNumber(merchantData.getWorkPhoneNumber());
						}
					});
					mapBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							Intent mapIntent = new Intent(MemberStoreInfoPage.this, MemberStoreMapPageActivity.class);
							mapIntent.putExtra("latatude", latatude);
							mapIntent.putExtra("longitude", longitude);
							startActivity(mapIntent);
						}
					});
					logListBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							Intent logIntent = new Intent(MemberStoreInfoPage.this, MemberStoreLogPageActivity.class);
							logIntent.putExtra("idCheckMileageMileages", idCheckMileageMileages);
							logIntent.putExtra("storeName", merchantData.getCompanyName());
							startActivity(logIntent);
						}
					});	
					serviceListBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							Toast.makeText(returnThis(), R.string.not_yet, Toast.LENGTH_SHORT).show();
						}
					});
					closeBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							finish();
						}
					});	

					if(merchantData.getWorkPhoneNumber().length()>3){		
						callBtn.setVisibility(View.VISIBLE);  // 	VISIBLE = 0;  INVISIBLE = 4;  GONE = 8;
					}
					if(latatude.length()>3&&longitude.length()>3){
						mapBtn.setVisibility(View.VISIBLE);  // 	VISIBLE = 0;  INVISIBLE = 4;  GONE = 8;
					}

					//					logListBtn.setVisibility(View.VISIBLE);			// ����Ϸ��� ���� �ּ� Ǯ� ���
					//					serviceListBtn.setVisibility(View.VISIBLE);		// ���� ���� ����..
					closeBtn.setVisibility(View.VISIBLE);

				}
				if(b.getInt("order")==1){
					// ���α׷����� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���α׷�����  ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				//				Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};


	// �߾� ���α׷����� ����, ����
	public void showPb(){
		new Thread( 
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 1);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}
	public void hidePb(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 2);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		setContentView(R.layout.member_store_info);

		callBtn = (Button)findViewById(R.id.callBtn);
		mapBtn = (Button)findViewById(R.id.mapBtn);
		logListBtn = (Button)findViewById(R.id.logListBtn);
		serviceListBtn = (Button)findViewById(R.id.serviceListBtn);
		closeBtn = (Button)findViewById(R.id.closeBtn);

		callBtn.setVisibility(View.GONE);
		mapBtn.setVisibility(View.GONE);

		logListBtn.setVisibility(View.GONE);		// ����Ϸ��� �� �� ���� INVISIBLE �� �ٲ�.  
		serviceListBtn.setVisibility(View.GONE);		// ������� �������� GONE ���� �ٲ�. �׸��� VISIBLE�� �������� ����. 

		closeBtn.setVisibility(View.INVISIBLE);

		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.memberstore_info_ProgressBar01);		// �ε�(�߾�)
		showPb();

		// ȭ�� ũ�� ����. (������ ���� �����ֱ� ����)
//		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
//		Log.i("screenWidth : ", "" + screenWidth);
//		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
//		Log.i("screenHeight : ", "" + screenHeight);

//		if(screenWidth < screenHeight ){
//			fImgSize = screenWidth;
//		}else{
//			fImgSize = screenHeight;
//		}

		Intent rIntent = getIntent();
		merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");			// ������ �Ƶ�
		myMileage = rIntent.getStringExtra("myMileage");							// �������� ���� �� ���ϸ���
		
		// �������̹Ƿ� ��� �ϸ� �ȵ�.
//		imageFileStr = rIntent.getStringExtra("imageFileStr");							// �������� ���� �� ���ϸ���		
//		if((imageFileStr!=null) && (imageFileStr.length()>0)){									// �̺�Ʈ �̹��� - �� ���ڿ��� �����Ͱ� �Ѿ�� ���. ��ȯ.
//			byte[] decodedString = Base64.decode(imageFileStr, Base64.DEFAULT); 
//			imageFile = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//		}
		
		if(myMileage==null||myMileage.length()<1){
			myMileage = "0";
		}
		idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");		// �� �Ƶ�
		merchantData.setMerchantID(merchantId);
		if(merchantId.length()>0){
			try {
				getMerchantInfo();				// ������ ���� ������
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			showMSG();		// ������ �ڵ鷯 ���� �佺Ʈ
			//			Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	public Context returnThis(){
		return this;
	}
	public void showMSG(){			// ȭ�鿡 error �佺Ʈ ���..
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



	/*
	 * ������ ����Ͽ� ������ ������ �����´�.				-- 1�� �˻�

	 * �� ����� <CheckMileageMileage> Object �� ��ȯ �Ѵ�.?? 
	 * 
	 * ������ ���� : ������ ���̵�
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  �޴� ���� : 
	 *    ������ �̸�, ������ �̹���URL, �������� ���� �� ���ϸ���.
	 *     ��ǥ�� �̸� , ��ȭ��ȣ 1, �ּ� 1, 
	 *      ��Ÿ �����, ��ǥ(1,2),  
	 *    @[���������̵�] �� ���� ó�� �������Ƿ� ���� ����
	 *  
	 * -----------------------------------
	 * |[      ��    ��    ��      [���ϸ���] ] |
	 * |[      ��    ��    ��                       ] |
	 * |��ǥ�� :                       |
	 * |���� :				[��ȭ�ɱ�]
	 * |�ּ� : 				[��������]
	 * 
	 * ��Ÿ ����.....
	 * 
	 *      [�޴�/����]  [�ݱ�]
	 * ------------------------------------
	 * 
	 * �ݱ� ��ư�� ��ܿ� �Ѽ���...������ ȭ�� ����.
	 *  ���ϸ��� ������ ���ϸ��� �̷� ����
	 *  ��ȭ�ɱ� ������ ��ȭ �ɱ�
	 *  �������� ������ �������� ������, �� ��ġ Ȯ��.
	 *    
	 *  ������ �Ķ����: merchantId  activateYn
	 *  �޴� �Ķ���� : CheckMileageMerchant
	 *    
	 */
	public void getMerchantInfo() throws JSONException, IOException {
		Log.i(TAG, "getMerchantInfo");
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";

		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// ������ ���̵� �־ ��ȸ
							obj.put("activateYn", "Y");
							obj.put("merchantId", merchantId);
							Log.w(TAG,"merchantId:"+merchantId);
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
						InputStream in = null;
						try{
							postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
							connection2.connect();		// ???
							Thread.sleep(200);	
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes("UTF-8"));
							os2.flush();
							Thread.sleep(200);	
							// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							in =  connection2.getInputStream();
							os2.close();
							// ��ȸ�� ����� ó��.
							theData1(in);
							connection2.disconnect();
						}catch(Exception e){ 
							//							e.printStackTrace();
							connection2.disconnect();
						}  
					}
				}).start();
	}

	/*
	 * ������ �� ������ ����
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		JSONObject jsonObject;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * checkMileageMerchant":{"merchantId":"m1","password":"m1","name":"����¯","companyName":"������",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"����Ʈ ��Ƽ��","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		//		Log.d(TAG,"shop detail info ::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMerchant");
				
				String prstr = "";
				// �����͸� ���� ���� �����ο� �����ϰ� �ڵ鷯�� ���� ������-> ȭ�鿡 �����ش�..
				try{
					merchantData.setName(jsonobj2.getString("name"));				// ��ǥ��
				}catch(Exception e){
					merchantData.setName("");
				}
				try{
					merchantData.setProfileImageURL(jsonobj2.getString("profileImageUrl"));				// ������ �̹��� URL
				}catch(Exception e){
					merchantData.setProfileImageURL("");
				}
				try{
					merchantData.setCompanyName(jsonobj2.getString("companyName"));					// ������ �̸�
				}catch(Exception e){
					merchantData.setCompanyName("");
				}
				try{
					merchantData.setWorkPhoneNumber(jsonobj2.getString("workPhoneNumber"));			// ����1		
				}catch(Exception e){
					merchantData.setWorkPhoneNumber("");
				}
				try{
					//					merchantData.setAddress01(jsonobj2.getString("address01"));			// �ּ�
					tmpstr = jsonobj2.getString("address01");
					tmpstr2 = jsonobj2.getString("address02");
					tmpstr = tmpstr + " "+ tmpstr2;
					merchantData.setAddress01(tmpstr);
				}catch(Exception e){
					merchantData.setAddress01("");
				}
				try{
					prstr = jsonobj2.getString("introduction");		// prSentence --> introduction
					if(prstr.length()>maxPRstr){								
						prstr = prstr.substring(0, maxPRstr-2) + "...";
					}
					merchantData.setPrSentence(prstr);			// ����
					//					merchantData.setPrSentence(jsonobj2.getString("prSentence"));			// ����
				}catch(Exception e){
					merchantData.setPrSentence("");
				}
				try{
					merchantData.setLatitude(jsonobj2.getString("latitude"));					// ��ǥ1,2
				}catch(Exception e){			
					merchantData.setLatitude("");		
				}
				try{
					merchantData.setLongtitude(jsonobj2.getString("longitude"));				// 
				}catch(Exception e){
					merchantData.setLongtitude("");
				}
				if(merchantData.getProfileImageURL()!=null && merchantData.getProfileImageURL().length()>0){
//					if(imageFile!=null){
//						bm = imageFile;
//					}else{
						try{
							Log.w(TAG,"LoadImage with URL :"+merchantData.getProfileImageURL());
							bm = LoadImage(imgDomain+merchantData.getProfileImageURL());				 
						}catch(Exception e3){
							Log.w(TAG, imgDomain+merchantData.getProfileImageURL()+" -- fail");
							try{
								BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_320_240);
								bm = dw.getBitmap();
							}catch(Exception e4){}
						}
//					}
				}
				if(bm==null){
					BitmapDrawable dw = (BitmapDrawable) this.getResources().getDrawable(R.drawable.empty_320_240);
					bm = dw.getBitmap();
				}
				merchantData.setMerchantImage(bm);

				// ȭ�鿡 ������ ���� ������ �̹���.
				// �̹��� ���ϴܿ�  ����1 > ����1
				// �̹��� ���ܿ� ���ϸ���
				// �̹��� �ϴܿ� ��ǥ�� / ���� (��ȭ�ɱ�) / �ּ� (��������) / ���� /  (�޴�/���񽺺���)
				showInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����.
			showMSG();
			//			Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
		}
	}

	// ������ �̹��� URL ���� �̹��� �޾ƿͼ� �����ο� �����ϴ� �κ�.
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}
	private InputStream OpenHttpConnection(String $imagePath) {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.w(TAG,"IOException");
		}
		return stream ;
	}

	// ȭ�鿡 �����ش�..
	public void showInfo(){
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


	/*
	 * Bitmap �̹��� �������� -- xml ��ü �������� ��ü
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
	 */
	private BitmapDrawable BitmapResizePrc(Bitmap Src, float newHeight, float newWidth)
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

		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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




	// ��ȭ �ɱ�
	public void setCallingPhoneNumber(final String phoneNumber) {
		handler.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG, "Received phoneNumber: " + phoneNumber);
				if(("").equals(phoneNumber)) {
					Toast.makeText(MemberStoreInfoPage.this, R.string.no_phone_num, Toast.LENGTH_SHORT).show();
				} else {
					Log.d(TAG, "Calling Phone.");
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
					//					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
				}
			}

		});
	}

	@Override			// �� ��Ƽ��Ƽ�� ����ɶ� ����. 
	protected void onDestroy() {
		super.onDestroy();
		error = 0;		// ���� ���� ���� ���̶�� ���� ���Ѿ� �ϱ⶧��..
		try{
			if(connection2!=null){
				connection2.disconnect();
			}
		}catch(Exception e){}
	}
}
