package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 가맹점 정보 보기

/*
 * 가맹점 아이디, 내 마일리지  <-- 이전 화면에서 받아온다..  (여기서 말하는 이전 화면은 내 마일리지 목록 또는 가맹점 목록)
 * 가맹점 이미지URL, 가맹점 이름, 대표자 이름, 전화번호, 주소, 좌표1,2(좌표 미리 받으면 지도 볼때 안받아도 된다..), 상세 설명
 * 이후의 기능으로 기록보기, 전화걸기, 메뉴/서비스 보기 등..
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
	public CheckMileageMerchants merchantData = new CheckMileageMerchants();	// 결과 저장해서 보여주기 위한 도메인.
	String myMileage = "";
	String merchantId ="";
	String idCheckMileageMileages ="";
	
	String latatude = "";
	String longitude = "";
	
	int error=0;
	
	float fImgSize = 0;
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){
					// merchantData 에서 데이터 꺼내어 화면에 세팅한다.			// titleImg , name , companyName , phone , addr , pr ,,
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
					
					mileage.setText("★"+myMileage);
					//					type.setText(text);
					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), fImgSize, fImgSize);  // height, width
//					BitmapDrawable bmpResize = BitmapResizePrc(merchantData.getMerchantImage(), 400, 700);  		

					// set the Drawable on the ImageView
					titleImg.setImageDrawable(bmpResize);	
//					titleImg.setImageBitmap(merchantData.getMerchantImage());		
					latatude = merchantData.getLatitude();
					longitude = merchantData.getLongtitude();
					
					
					name.setText("대표자 : "+merchantData.getName());
					phone.setText("전화번호 : "+merchantData.getWorkPhoneNumber());
					addr.setText("주소 : "+merchantData.getAddress01());
					pr.setText("소개 : "+merchantData.getPrSentence());

					companyName.setText("가맹점 이름 : "+merchantData.getCompanyName());

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
							Toast.makeText(returnThis(), "준비중입니다.",Toast.LENGTH_SHORT).show();
						}
					});
					closeBtn.setOnClickListener(new Button.OnClickListener()  {
						public void onClick(View v)  {
							finish();
						}
					});	
				}
			}catch(Exception e){
				//				Toast.makeText(MyMileagePageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_store_info);

		// 화면 크기 측정. (가맹점 사진 보여주기 위함)
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
		merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");			// 가맹점 아디
		myMileage = rIntent.getStringExtra("myMileage");							// 가맹점에 대한 내 마일리지
		if(myMileage==null||myMileage.length()<1){
			myMileage = "0";
		}
		idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");		// 내 아디
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
			Toast.makeText(MemberStoreInfoPage.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	public Context returnThis(){
		return this;
	}




	/*
	 * 서버와 통신하여 가맹점 정보를 가져온다.

	 * 그 결과를 <CheckMileageMileage> Object 로 반환 한다.?? 
	 * 
	 * 보내는 정보 : 가맹점 아이디
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  받는 정보 : 
	 *    가맹점 이름, 가맹점 이미지URL, 가맹점에 대한 내 마일리지.
	 *     대표자 이름 , 전화번호 1, 주소 1, 
	 *      기타 설명들, 좌표(1,2),  
	 *    @[가맹점아이디] 는 가장 처음 가져오므로 따로 저장
	 *  
	 * -----------------------------------
	 * |[      이    미    지      [마일리지] ] |
	 * |[      이    미    지                       ] |
	 * |대표자 :                       |
	 * |전번 :				[전화걸기]
	 * |주소 : 				[지도보기]
	 * 
	 * 기타 설명.....
	 * 
	 *      [메뉴/서비스]  [닫기]
	 * ------------------------------------
	 * 
	 * 닫기 버튼은 상단에 둘수도...눌러서 화면 닫음.
	 *  마일리지 눌러서 마일리지 이력 보기
	 *  전화걸기 눌러서 전화 걸기
	 *  지도보기 눌러서 지도상의 가맹점, 내 위치 확인.
	 *    
	 *  보내는 파라미터: merchantId  activateYn
	 *  받는 파라미터 : CheckMileageMerchant
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
							// 가맹점 아이디를 넣어서 조회
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
							// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
							error = 0;
						}catch(Exception e){ 
//							e.printStackTrace();
							while(error==1){			// 에러 발생시 다시 정보를 가져온다. 될때까지 반복..
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
									// 조회한 결과를 처리.
									theData1(in);
								}catch(Exception e2){}
						}  
					}
				}
					
				}).start();
	}

	/*
	 * 가맹점 상세 정보를 받음
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
		 * checkMileageMerchant":{"merchantId":"m1","password":"m1","name":"내가짱","companyName":"우수기업",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"아지트 에티서","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		Log.d(TAG,"가맹점 상세정보::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMerchant");
				Bitmap bm = null;
				// 데이터를 전역 변수 도메인에 저장하고 핸들러를 통해 도메인-> 화면에 보여준다..
				try{
					merchantData.setName(jsonobj2.getString("name"));				// 대표자
				}catch(Exception e){
					merchantData.setName("");
				}
				try{
					merchantData.setProfileImageURL(jsonobj2.getString("profileImageUrl"));				// 가맹점 이미지 URL
				}catch(Exception e){
					merchantData.setProfileImageURL("");
				}
				try{
					merchantData.setCompanyName(jsonobj2.getString("companyName"));					// 가맹점 이름
				}catch(Exception e){
					merchantData.setCompanyName("");
				}
				try{
					merchantData.setWorkPhoneNumber(jsonobj2.getString("workPhoneNumber"));			// 전번1		
				}catch(Exception e){
					merchantData.setWorkPhoneNumber("");
				}
				try{
					merchantData.setAddress01(jsonobj2.getString("address01"));			// 주소
				}catch(Exception e){
					merchantData.setAddress01("");
				}
				try{
					merchantData.setPrSentence(jsonobj2.getString("prSentence"));			// 설명
				}catch(Exception e){
					merchantData.setPrSentence("");
				}
				try{
					merchantData.setLatitude(jsonobj2.getString("latitude"));					// 좌표1,2
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
				// 화면에 보여줄 것은 가맹점 이미지.
				// 이미지 좌하단에  지역1 > 업종1
				// 이미지 우상단에 마일리지
				// 이미지 하단에 대표자 / 전번 (전화걸기) / 주소 (지도보기) / 설명 /  (메뉴/서비스보기)

				showInfo();

			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			Toast.makeText(MemberStoreInfoPage.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
		}
	}

	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
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

	// 화면에 보여준다..
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


	
	public void setCallingPhoneNumber(final String phoneNumber) {
		handler.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Log.e(TAG, "Received phoneNumber: " + phoneNumber);
				if(("").equals(phoneNumber)) {
					Toast.makeText(MemberStoreInfoPage.this, "전화번호가 없습니다." + "\n" + "확인해 주세요.", Toast.LENGTH_SHORT).show();
				} else {
					Log.e(TAG, "Calling Phone.");
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
//					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
				}
			}
			
		});
	}

	
	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
		super.onDestroy();
		// 서버 무한 접속 중이라면 종료 시켜야 하기때문..
		error = 0;
	}
}
