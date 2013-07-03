package kr.co.bettersoft.checkmileage.activities;
// 가맹점 정보 보기

/**
 * MemberStoreInfoPage
 * 
 * 가맹점 아이디, 내 마일리지  <-- 이전 화면에서 받아온다..  (여기서 말하는 이전 화면은 내 마일리지 목록 또는 가맹점 목록)
 * 가맹점 이미지URL, 가맹점 이름, 대표자 이름, 전화번호, 주소, 좌표1,2(좌표 미리 받으면 지도 볼때 안받아도 된다..), 상세 설명
 * 이후의 기능으로 기록보기, 전화걸기, 메뉴/서비스 보기 등..
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MemberStoreInfoPage extends Activity {
	String TAG = "MemberStoreInfoPage";

	final int GET_MERCHANT_INFO = 301; 
	final int UPDATE_LOG_TO_SERVER = 302; 
	
	// 내 좌표 업뎃용				///////////////////////////////////////////////
	String myLat2;
	String myLon2;
	// 전번(업뎃용)
	String phoneNum = "";
	// qr
	String qrCode = "";
	// 설정 파일 저장소  - 사용자 전번 읽기 / 쓰기 용도	
	SharedPreferences sharedPrefCustom;
	// 중복 실행 방지용
	int isUpdating = 0;
	/////////////////////////////////////////////////////////////////////////////


	Button callBtn ;
	Button mapBtn;
	Button logListBtn;
	Button serviceListBtn;
	Button closeBtn;

	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	// checkMileageCustomerRest = new CheckMileageCustomerRest();	// oncreate


	public CheckMileageMerchants merchantData = new CheckMileageMerchants();	// 결과 저장해서 보여주기 위한 도메인.
	String myMileage = "";
	String merchantId ="";
	String idCheckMileageMileages ="";


	String imgDomain = CommonConstant.imgDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인.   
	Bitmap bm = null;
	String latatude = "";
	String longitude = "";

	int error=0;
	String tmpstr = "";
	String tmpstr2 = "";
	int maxPRstr = 200;					// 화면에 보여줄 소개 글의 최대 글자수. 넘어가면 자르고 ... 으로 표시해줌.

	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
					hidePb();
					mileage.setText(myMileage);
					mileage.setVisibility(View.VISIBLE);
					if(merchantData.getWorkPhoneNumber().length()>0){
						pre_phone.setVisibility(View.VISIBLE);
					}

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

							Log.d(TAG,"latatude:"+latatude+"//longitude:"+longitude);	// *** 

							if(latatude.contains(".") && longitude.contains(".")){
								latatude = (Float.parseFloat(latatude) * 10000)+"";
								longitude = (Float.parseFloat(longitude) * 10000)+"";
							}
							if(latatude.length()>0 && latatude.length()<10){
								Intent mapIntent = new Intent(MemberStoreInfoPage.this, MemberStoreMapPageActivity.class);	// *** 
								mapIntent.putExtra("latatude", latatude);
								mapIntent.putExtra("longitude", longitude);
								mapIntent.putExtra("companyName", merchantData.getCompanyName());
								startActivity(mapIntent);
							}
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
				
				switch (msg.what)
				{
					case GET_MERCHANT_INFO   : runOnUiThread(new RunnableGetMerchantInfo());	
						break;
					case UPDATE_LOG_TO_SERVER   : runOnUiThread(new RunnableUpdateLogToServer());	
					break;
					default : 
						break;
				}	
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		setContentView(R.layout.member_store_info);

		checkMileageCustomerRest = new CheckMileageCustomerRest();

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

		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	public void getMerchantInfoForOnCreate(){
		Intent rIntent = getIntent();
		merchantId = rIntent.getStringExtra("checkMileageMerchantsMerchantID");			// 가맹점 아디
		myMileage = rIntent.getStringExtra("myMileage");							// 가맹점에 대한 내 마일리지

		if(myMileage==null||myMileage.length()<1){
			myMileage = "0";
		}
		idCheckMileageMileages = rIntent.getStringExtra("idCheckMileageMileages");		// 내 아디
		merchantData.setMerchantId(merchantId);
		if(merchantId.length()>0){
			handler.sendEmptyMessage(GET_MERCHANT_INFO);
		}else{
			showMSG();		// 에러시 핸들러 통한 토스트
			finish();
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////
// 비동기 통신.

	/**
	 * 러너블. 가맹점 정보를 가져온다.
	 */
	class RunnableGetMerchantInfo implements Runnable {
		public void run(){
			new backgroundGetMerchantInfo().execute();
		}
	}
	/**
	 * 비동기로 가맹점 정보를 가져온다.
	 * backgroundGetMerchantInfo
	 */
	public class backgroundGetMerchantInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetMerchantInfo");

			if(isUpdating==0){
				isUpdating = 1;
				// 파리미터 세팅
				CheckMileageMerchants checkMileageMerchantsParam = new CheckMileageMerchants();
				checkMileageMerchantsParam.setMerchantId(merchantId);

				// 호출
				showPb();
				callResult = checkMileageCustomerRest.RestGetMerchantInfo(checkMileageMerchantsParam);
				hidePb();
				// 결과 처리
				if(callResult.equals("S")){		
					processMerchantStoreInfoData();	// 화면에 뿌려준다.		ㄴㅇㄹ
				}else{														 
					showMSG();
				}
				isUpdating = 0;
			}else{
				Log.d(TAG,"already updating..");
			}

			return null; 
		}
	}

	
	// 서버에 로깅하기.
	/**
	 * 러너블. 서버에 로깅한다.
	 */
	class RunnableUpdateLogToServer implements Runnable {
		public void run(){
			new backgroundUpdateLogToServer().execute();
		}
	}
	/**
	 * 비동기로 사용자의 위치 정보 및 정보 로깅
	 * backgroundUpdateLogToServer
	 */
	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateMyLocationtoServer");
			if(isUpdating==0){
				isUpdating = 1;
				// 파리미터 세팅
				CheckMileageLogs checkMileageLogsParam = new CheckMileageLogs();
				checkMileageLogsParam.setCheckMileageId(qrCode);
				checkMileageLogsParam.setViewName("CheckMileageCustomerMerchantInformationView");
				checkMileageLogsParam.setParameter01(phoneNum);
				checkMileageLogsParam.setParameter04("");
				// 호출
								showPb();
				callResult = checkMileageCustomerRest.RestUpdateLogToServer(checkMileageLogsParam);
								hidePb();
				// 결과 처리
				if(callResult.equals("S")){				 
					Log.d(TAG,"updateLogToServer S");
				}else{														 
					Log.d(TAG,"updateLogToServer F");
				}
				isUpdating = 0;
				getMerchantInfoForOnCreate();		// 페이지별 업무 - 가맹점 상세 정보 가져오기.
			}else{
				Log.d(TAG,"already updating..");
			}
			return null; 
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 서버로부터 받아온 가맹점 정보를 파싱하여 화면에 뿌려준다.
	 */
	public void processMerchantStoreInfoData(){
		tempstr = checkMileageCustomerRest.getTempstr();
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
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
				tempstr = jsonobj2.getString("latitude");
				if(tempstr.contains(".")){
					tempstr = (int)(Float.parseFloat(tempstr)*1000000)+"";
					Log.d(TAG,"tempstr:"+tempstr);
				}
				merchantData.setLatitude(tempstr);					// 좌표1,2
				//					merchantData.setLatitude(jsonobj2.getString("latitude"));					// 좌표1,2
			}catch(Exception e){			
				merchantData.setLatitude("");		
			}
			try{
				tempstr = jsonobj2.getString("longitude");
				if(tempstr.contains(".")){
					tempstr = (int)(Float.parseFloat(tempstr)*1000000)+"";
					Log.d(TAG,"tempstr:"+tempstr);
				}
				merchantData.setLongtitude(tempstr);					// 좌표1,2
				//					merchantData.setLongtitude(jsonobj2.getString("longitude"));				// 
			}catch(Exception e){
				merchantData.setLongtitude("");
			}
			if(merchantData.getProfileImageURL()!=null && merchantData.getProfileImageURL().length()>0){
				//					if(imageFile!=null){
				//						bm = imageFile;
				//					}else{
				try{
					//						Log.w(TAG,"LoadImage with URL :"+merchantData.getProfileImageURL());
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
		//			Toast.makeText(MemberStoreInfoPage.this, R.string.error_message, Toast.LENGTH_SHORT).show();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	/**
	 * LoadImage
	 *  가맹점 이미지 URL 에서 이미지 받아온 스트림을 비트맵으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return bm
	 */
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}
	/**
	 * OpenHttpConnection
	 *  가맹점 이미지 URL 에서 이미지 받아와서 스트림으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return stream
	 */
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


	///////////////////////////////////////////////////////////////////////////////////////////////////
	// 기타 기능


	// 전화 걸기
	/**
	 * setCallingPhoneNumber
	 *  전화를 건다
	 *
	 * @param phoneNumber
	 * @param
	 * @return
	 */
	public void setCallingPhoneNumber(final String phoneNumber) {
		handler.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG, "Received phoneNumber: " + phoneNumber);
				if(("").equals(phoneNumber)) {
					Toast.makeText(MemberStoreInfoPage.this, R.string.no_phone_num, Toast.LENGTH_SHORT).show();
				} else {
					Log.d(TAG, "Calling Phone.");
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));							// 다이얼
					//					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));		// 통화
				}
			}

		});
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 유틸

	/**
	 * returnThis
	 *  컨택스트를 리턴한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public Context returnThis(){
		return this;
	}
	/**
	 * showMSG
	 *  화면에 error 토스트 띄운다
	 *
	 * @param
	 * @param
	 * @return
	 */
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

	// 중앙 프로그래스바 보임, 숨김
	/**
	 * showPb
	 *  중앙 프로그래스바 가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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
	/**
	 * hidePb
	 *  중앙 프로그래스바 비가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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


	// 화면에 보여준다..
	/**
	 * showInfo
	 *  받아온 정보를 화면에 보여준다.
	 *
	 * @param
	 * @param
	 * @return
	 */
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


	@Override
	protected void onResume(){
		super.onResume();
		//		getMerchantInfoForOnCreate();		//  *** 페이지별 업무 - 가맹점 상세 정보 가져오기..  로깅 구현 이후 주석 처리 할것.
		if(isUpdating==0){
//			loggingToServer();		// *** 일단 보류. 나중에 주석 풀것.. 원래 있던 getMerchantInfoForOnCreate 을 대신 수행..
//			new backgroundUpdateLogToServer().execute();
			handler.sendEmptyMessage(UPDATE_LOG_TO_SERVER);
		}
	}

	@Override			// 이 액티비티가 종료될때 실행. 
	protected void onDestroy() {
		super.onDestroy();
		error = 0;		// 서버 무한 접속 중이라면 종료 시켜야 하기때문..
	}



	///////////////////////////////////////////////////////////////////////////////////////////////////////
}
