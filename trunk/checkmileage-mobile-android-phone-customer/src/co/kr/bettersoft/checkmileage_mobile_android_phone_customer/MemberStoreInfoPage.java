package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kr.bettersoft.domain.CheckMileageMerchants;
import com.kr.bettersoft.domain.CheckMileageMileage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberStoreInfoPage extends Activity {
	String TAG = "MemberStoreInfoPage";
	int responseCode = 0;
	String controllerName ="";
	String methodName ="";
	public CheckMileageMerchants merchantData = new CheckMileageMerchants();	// ��� �����ؼ� �����ֱ� ���� ������.
	String myMileage = "";
	String merchantId ="";
	String idCheckMileageMileages ="";
	
	String latatude = "";
	String longitude = "";
	
	int error=0;
	
	float fImgSize = 0;
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ°���� ȭ�鿡 �ѷ��ش�.
				if(b.getInt("showYN")==1){
					// merchantData ���� ������ ������ ȭ�鿡 �����Ѵ�.			// titleImg , name , companyName , phone , addr , pr ,,
					TextView mileage = (TextView)findViewById(R.id.mileage);
					TextView type = (TextView)findViewById(R.id.type);

					ImageView titleImg = (ImageView)findViewById(R.id.prImage);
					TextView name = (TextView)findViewById(R.id.name);	
					TextView phone = (TextView)findViewById(R.id.phone);
					TextView addr = (TextView)findViewById(R.id.addr);	
					TextView pr = (TextView)findViewById(R.id.pr);
					TextView companyName = (TextView)findViewById(R.id.merchantName2);	

					Button callBtn = (Button)findViewById(R.id.callBtn);
					Button mapBtn = (Button)findViewById(R.id.mapBtn);

					Button logListBtn = (Button)findViewById(R.id.logListBtn);
					Button serviceListBtn = (Button)findViewById(R.id.serviceListBtn);
					Button closeBtn = (Button)findViewById(R.id.closeBtn);
					
					mileage.setText("��"+myMileage);
					//					type.setText(text);
					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), fImgSize, fImgSize);  // height, width
//					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), 400, 700);  		

					// set the Drawable on the ImageView
					titleImg.setImageDrawable(bmpResize);	
//					titleImg.setImageBitmap(merchantData.getMerchantImage());		
					latatude = merchantData.getLatitude();
					longitude = merchantData.getLongtitude();
					
					
					name.setText("��ǥ�� : "+merchantData.getName());
					phone.setText("��ȭ��ȣ : "+merchantData.getWorkPhoneNumber());
					addr.setText("�ּ� : "+merchantData.getAddress01());
					pr.setText("�Ұ� : "+merchantData.getPrSentence());

					companyName.setText("������ �̸� : "+merchantData.getCompanyName());

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
//							Toast.makeText(returnThis(), "strr33",Toast.LENGTH_SHORT).show();
							Intent logIntent = new Intent(MemberStoreInfoPage.this, MemberStoreLogPageActivity.class);
							logIntent.putExtra("idCheckMileageMileages", idCheckMileageMileages);
							logIntent.putExtra("storeName", merchantData.getCompanyName());
							startActivity(logIntent);
						}
					});	
					serviceListBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							Toast.makeText(returnThis(), "�غ����Դϴ�.",Toast.LENGTH_SHORT).show();
						}
					});
					closeBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							finish();
						}
					});	
				}
			}catch(Exception e){
				//				Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_store_info);

		// ȭ�� ũ�� ����. (������ ���� �����ֱ� ����)
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
	    
	    if(screenWidth < screenHeight ){
	    	fImgSize = screenWidth;
	    }else{
	    	fImgSize = screenHeight;
	    }
		
		Intent rIntent = getIntent();
		merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");			// ������ �Ƶ�
		myMileage = rIntent.getStringExtra("myMileage");							// �������� ���� �� ���ϸ���
		if(myMileage==null||myMileage.length()<1){
			myMileage = "0";
		}
		idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");		// �� �Ƶ�
		merchantData.setMerchantID(merchantId);
		if(merchantId.length()>0){
			try {
				getMerchantInfo();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			Toast.makeText(MemberStoreInfoPage.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	public Context returnThis(){
		return this;
	}




	/*
	 * ������ ����Ͽ� ������ ������ �����´�.

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
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
						URL postUrl2 = null;
						HttpURLConnection connection2 = null;
						InputStream in = null;
						try{
							error = 1;
							postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
							connection2 = (HttpURLConnection) postUrl2.openConnection();
							connection2.setDoOutput(true);
							connection2.setInstanceFollowRedirects(false);
							connection2.setRequestMethod("POST");
							connection2.setRequestProperty("Content-Type", "application/json");
//							connection2.connect();		// ???
							OutputStream os2 = connection2.getOutputStream();
							os2.write(jsonString.getBytes());
							os2.flush();
//							Thread.sleep(500);	
							// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							in =  connection2.getInputStream();
							// ��ȸ�� ����� ó��.
							theData1(in);
							error = 0;
						}catch(Exception e){ 
//							e.printStackTrace();
							while(error==1){			// ���� �߻��� �ٽ� ������ �����´�. �ɶ����� �ݺ�..
								try{
									System.out.println("errored");
									postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
									connection2 = (HttpURLConnection) postUrl2.openConnection();
									connection2.setDoOutput(true);
									connection2.setInstanceFollowRedirects(false);
									connection2.setRequestMethod("POST");
									connection2.setRequestProperty("Content-Type", "application/json");
//									System.out.println("postUrl      : " + postUrl2);
//									connection2.connect();
									OutputStream os2 = connection2.getOutputStream();
									os2.write(jsonString.getBytes());
									os2.flush();
									Thread.sleep(500);
									System.out.println("responseCode : " + connection2.getResponseCode());
									error=0;
									responseCode = connection2.getResponseCode();
									in =  connection2.getInputStream();
									// ��ȸ�� ����� ó��.
									theData1(in);
								}catch(Exception e2){}
						}  
					}
				}
					
				}).start();
	}

	/*
	 * ������ �� ������ ����
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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
		Log.d(TAG,"������ ������::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMerchant");
				Bitmap bm = null;
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
					merchantData.setAddress01(jsonobj2.getString("address01"));			// �ּ�
				}catch(Exception e){
					merchantData.setAddress01("");
				}
				try{
					merchantData.setPrSentence(jsonobj2.getString("prSentence"));			// ����
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
				if(jsonobj2.getString("profileImageUrl")!=null && jsonobj2.getString("profileImageUrl").length()>0){
					try{
						bm = LoadImage(jsonobj2.getString("profileImageUrl"));				// 
					}catch(Exception e){}
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
			Toast.makeText(MemberStoreInfoPage.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
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
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Bitmap �̹��� ��������
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
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


	
	public void setCallingPhoneNumber(final String phoneNumber) {
		handler.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Log.e(TAG, "Received phoneNumber: " + phoneNumber);
				if(("").equals(phoneNumber)) {
					Toast.makeText(MemberStoreInfoPage.this, "��ȭ��ȣ�� �����ϴ�." + "\n" + "Ȯ���� �ּ���.", Toast.LENGTH_SHORT).show();
				} else {
					Log.e(TAG, "Calling Phone.");
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
//					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
				}
			}
			
		});
	}

	
	@Override			// �� ��Ƽ��Ƽ�� ����ɶ� ����. 
	protected void onDestroy() {
		super.onDestroy();
		// ���� ���� ���� ���̶�� ���� ���Ѿ� �ϱ⶧��..
		error = 0;
	}
}
