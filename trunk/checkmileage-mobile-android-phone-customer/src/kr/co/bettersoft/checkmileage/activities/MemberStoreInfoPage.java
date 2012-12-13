package kr.co.bettersoft.checkmileage.activities;
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

	public CheckMileageMerchants merchantData = new CheckMileageMerchants();	// 결과 저장해서 보여주기 위한 도메인.
	String myMileage = "";
	String merchantId ="";
	String idCheckMileageMileages ="";
//	String imageFileStr="";			// 문자열로 바꾼 이미지
//	Bitmap imageFile= null;			// 이미지 파일
	
	
	String imgDomain = CommonUtils.imgDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인.   
	Bitmap bm = null;
	String latatude = "";
	String longitude = "";

	URL postUrl2 = null;
	HttpURLConnection connection2 = null;

	int error=0;
	int reTry = 3;			 
	String tmpstr = "";
	String tmpstr2 = "";
	int maxPRstr = 200;					// 화면에 보여줄 소개 글의 최대 글자수. 넘어가면 자르고 ... 으로 표시해줌.

//	float fImgSize = 0;

	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바

	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){
					// merchantData 에서 데이터 꺼내어 화면에 세팅한다.			// titleImg , name , companyName , phone , addr , pr ,,
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
					//					tmpstr = getString(R.string.pr_str);	// 앞에 소개: 를 붙이지 않음.
					pr.setText(merchantData.getPrSentence());
					member_store_title.setText(merchantData.getCompanyName());			// 상단 타이틀 안에 가맹점 이름.
					//					tmpstr = getString(R.string.shop_name);
					//					companyName.setText(tmpstr+" : "+merchantData.getCompanyName());	// 가맹점 이름 - 상단 타이틀 바로 대체.

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

					//					logListBtn.setVisibility(View.VISIBLE);			// 사용하려면 이줄 주석 풀어서 사용
					//					serviceListBtn.setVisibility(View.VISIBLE);		// 서비스 내역 보기..
					closeBtn.setVisibility(View.VISIBLE);

				}
				if(b.getInt("order")==1){
					// 프로그래스바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 프로그래스바  종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				//				Toast.makeText(MyMileagePageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};


	// 중앙 프로그래스바 보임, 숨김
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

		logListBtn.setVisibility(View.GONE);		// 사용하려면 이 두 줄을 INVISIBLE 로 바꿈.  
		serviceListBtn.setVisibility(View.GONE);		// 사용하지 않으려면 GONE 으로 바꿈. 그리고 VISIBLE로 돌려놓지 않음. 

		closeBtn.setVisibility(View.INVISIBLE);

		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.memberstore_info_ProgressBar01);		// 로딩(중앙)
		showPb();

		// 화면 크기 측정. (가맹점 사진 보여주기 위함)
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
		merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");			// 가맹점 아디
		myMileage = rIntent.getStringExtra("myMileage");							// 가맹점에 대한 내 마일리지
		
		// 섬네일이므로 사용 하면 안됨.
//		imageFileStr = rIntent.getStringExtra("imageFileStr");							// 가맹점에 대한 내 마일리지		
//		if((imageFileStr!=null) && (imageFileStr.length()>0)){									// 이벤트 이미지 - 가 문자열로 데이터가 넘어온 경우. 변환.
//			byte[] decodedString = Base64.decode(imageFileStr, Base64.DEFAULT); 
//			imageFile = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//		}
		
		if(myMileage==null||myMileage.length()<1){
			myMileage = "0";
		}
		idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");		// 내 아디
		merchantData.setMerchantID(merchantId);
		if(merchantId.length()>0){
			try {
				getMerchantInfo();				// 가맹점 정보 가져옴
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			showMSG();		// 에러시 핸들러 통한 토스트
			//			Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	public Context returnThis(){
		return this;
	}
	public void showMSG(){			// 화면에 error 토스트 띄움..
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
	 * 서버와 통신하여 가맹점 정보를 가져온다.				-- 1차 검색

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
							// 200 , 204 : 정상
							responseCode = connection2.getResponseCode();
							in =  connection2.getInputStream();
							os2.close();
							// 조회한 결과를 처리.
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
	 * 가맹점 상세 정보를 받음
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
		 * checkMileageMerchant":{"merchantId":"m1","password":"m1","name":"내가짱","companyName":"우수기업",
		 * "profileImageUrl":"http:\/\/imgshop.daum-img.net\/image\/content\/set\/A_ds_view\/daum_B0_20120814172515_9723.jpg",
		 * "email":"m1@m1.net","country":"ko","workPhoneNumber":"02-123-1231","address01":"아지트 에티서","businessType":"qwer",
		 * "businessRegistrationNumber01":1123,"businessRegistrationNumber02":4433,"businessKind01":"mm",
		 * "decreaseMileage":0,"prSentence":1,"restrictionYn":"N","activateYn":"Y","modifyDate":"2012-08-10","registerDate":"2012-08-10"}}
		 */
		//		Log.d(TAG,"shop detail info ::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		if(responseCode==200 || responseCode==204){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMerchant");
				
				String prstr = "";
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
					//					merchantData.setAddress01(jsonobj2.getString("address01"));			// 주소
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
					merchantData.setPrSentence(prstr);			// 설명
					//					merchantData.setPrSentence(jsonobj2.getString("prSentence"));			// 설명
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

				// 화면에 보여줄 것은 가맹점 이미지.
				// 이미지 좌하단에  지역1 > 업종1
				// 이미지 우상단에 마일리지
				// 이미지 하단에 대표자 / 전번 (전화걸기) / 주소 (지도보기) / 설명 /  (메뉴/서비스보기)
				showInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			showMSG();
			//			Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
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
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.w(TAG,"IOException");
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
	 * Bitmap 이미지 리사이즈 -- xml 자체 설정으로 대체
	 * Src : 원본 Bitmap
	 * newHeight : 새로운 높이
	 * newWidth : 새로운 넓이
	 * 참고 소스 : http://skyswim42.egloos.com/3477279 ( webview 에서 capture 화면 resizing 하는 source 도 있음 )
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




	// 전화 걸기
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

	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
		super.onDestroy();
		error = 0;		// 서버 무한 접속 중이라면 종료 시켜야 하기때문..
		try{
			if(connection2!=null){
				connection2.disconnect();
			}
		}catch(Exception e){}
	}
}
