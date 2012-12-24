package kr.co.bettersoft.checkmileage.activities;
/**
 * PushDetail
 * 
 * 가맹점에서 보내온 이벤트 상세 보기 화면.
 * 
 * 화면 구성은 최상단에 타이틀,  상단부에 이미지(첨부 이미지). 하단부에 텍스트. 
 * 타이틀, 이미지는 고정상태고 하단 텍스트의 내용이 많을 경우 스크롤 되도록 한다. 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.domain.CheckMileagePushEvent;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PushDetail extends Activity {
	String TAG = "PushDetail";

	Button closeBtn;
	public CheckMileagePushEvent checkMileagePushEvent = new CheckMileagePushEvent();
	String subject="";				// 제목
	String content="";				// 내용
	String imageFileUrl="";			// 이미지 URL
	String imageFileStr="";			// 문자열로 바꾼 이미지
	String modifyDate="";			// 수정일 
	String companyName="";			// 가맹점명
	Bitmap imageFile= null;			// 이미지 파일

	TextView subjectView;
	ImageView imageFileView ;
	TextView contentView ;
	TextView companyNameView ;	
	int maxPRstr = 200;					// 화면에 보여줄 소개 글의 최대 글자수. 넘어가면 자르고 ... 으로 표시해줌.
	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){
					//					String modifyDate="";
					subjectView.setText(checkMileagePushEvent.getSubject());
					contentView.setText(checkMileagePushEvent.getContent());
					companyNameView.setText(checkMileagePushEvent.getCompanyName());
					imageFileView.setImageBitmap(checkMileagePushEvent.getImageFile());
					hidePb();

					closeBtn.setVisibility(View.VISIBLE);
				}
				if(b.getInt("order")==1){
					// 프로그래스바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_detail_progressbar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 프로그래스바  종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_detail_progressbar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(PushDetail.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );		// no title
		setContentView(R.layout.push_detail);
		subjectView = (TextView)findViewById(R.id.push_detail_title);
		imageFileView = (ImageView)findViewById(R.id.push_detail_image);
		contentView = (TextView)findViewById(R.id.push_detail_content);
		companyNameView = (TextView)findViewById(R.id.push_detail_store_name);	

		// 데이터 받음
		Intent receiveIntent = getIntent();
		subject = receiveIntent.getStringExtra("subject");				// 이벤트 제목
		content = receiveIntent.getStringExtra("content");				// 이벤트 글귀
		imageFileUrl = receiveIntent.getStringExtra("imageFileUrl");	// 이벤트 광고 이미지 주소	
		imageFileStr = receiveIntent.getStringExtra("imageFileStr");	// 이벤트 광고 이미지 문자화
		modifyDate = receiveIntent.getStringExtra("modifyDate");		// 이벤트 업뎃 날짜
		companyName = receiveIntent.getStringExtra("companyName");		// 이벤트 업체명

		//	    Log.d(TAG,"subject:"+subject+"//content:"+content+"//imageFileUrl:"+imageFileUrl);
		//	    Log.d(TAG,"imageFileStr:"+imageFileStr+"//modifyDate:"+modifyDate+"//companyName:"+companyName);

		if(imageFileUrl.length()>0){			// 이미지가 있어야 하는 경우
			if(imageFileStr.length()>0){									// 이벤트 이미지 - 가 문자열로 데이터가 넘어온 경우. 변환.
				byte[] decodedString = Base64.decode(imageFileStr, Base64.DEFAULT); 
				imageFile = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			}else{													// 이미지가 문자열로 넘어오지 못한 경우. 다시 생성.
				new backgroundLoadImage().execute();
				//				LoadImage(imageFileUrl);
			}
		}
		if(imageFile==null){			// 이미지가 없어야 하는 경우  및 위에서 실패한 경우 - 기본 이미지로 처리
			Log.d(TAG,"imageFile==null");
			BitmapDrawable dw = (BitmapDrawable) returnThis().getResources().getDrawable(R.drawable.empty_320_240);
			imageFile = dw.getBitmap();
		}
		checkMileagePushEvent.setSubject(subject);
		checkMileagePushEvent.setContent(content);
		checkMileagePushEvent.setImageFileUrl(imageFileUrl);
		checkMileagePushEvent.setImageFileStr(imageFileStr);
		checkMileagePushEvent.setModifyDate(modifyDate);
		checkMileagePushEvent.setCompanyName(companyName);
		checkMileagePushEvent.setImageFile(imageFile);

		closeBtn = (Button)findViewById(R.id.push_detail_closebtn);
		closeBtn.setVisibility(View.INVISIBLE);
		closeBtn.setOnClickListener(new Button.OnClickListener()  {
			public void onClick(View v)  {
				finish();
			}
		});	
		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.push_detail_progressbar01);		// 로딩(중앙)
		showPb();
		showInfo();
	}

	/**
	 * returnThis
	 *  컨택스트를 리턴한다 (핸들러에서 사용)
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



	// 화면에 보여준다..
	/**
	 * showInfo
	 *  이벤트 정보를 화면에 보여준다
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


	/*
	 * Bitmap 이미지 리사이즈   // 본페이지에서 사용 안함
	 * Src : 원본 Bitmap
	 * newHeight : 새로운 높이
	 * newWidth : 새로운 넓이
	 * 참고 소스 : http://skyswim42.egloos.com/3477279 ( webview 에서 capture 화면 resizing 하는 source 도 있음 )
	 */
	//	private BitmapDrawable BitmapResizePrc(Bitmap Src, float newHeight, float newWidth)
	//	{
	//		BitmapDrawable Result = null;
	//		int width = Src.getWidth();
	//		int height = Src.getHeight();
	//
	//		// calculate the scale - in this case = 0.4f
	//		float scaleWidth = ((float) newWidth) / width;
	//		float scaleHeight = ((float) newHeight) / height;
	//
	//		// createa matrix for the manipulation
	//		Matrix matrix = new Matrix();
	//
	//		// resize the bit map
	//		matrix.postScale(scaleWidth, scaleHeight);
	//
	//		// rotate the Bitmap 회전 시키려면 주석 해제!
	//		//matrix.postRotate(45);
	//
	//		// recreate the new Bitmap
	//		Bitmap resizedBitmap = Bitmap.createBitmap(Src, 0, 0, width, height, matrix, true);
	//
	//		// check
	//		width = resizedBitmap.getWidth();
	//		height = resizedBitmap.getHeight();
	////		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );
	//
	//		// make a Drawable from Bitmap to allow to set the BitMap
	//		// to the ImageView, ImageButton or what ever
	//		Result = new BitmapDrawable(resizedBitmap);
	//		return Result;
	//	}


	// 이벤트 이미지 : URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	// 비동기로 이벤트 목록 가져오는 함수 호출.
	/**
	 * backgroundLoadImage
	 *  비동기로 웹 이미지를 로드한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public class backgroundLoadImage extends   AsyncTask<Void, Void, Void> {
		@Override protected void onPostExecute(Void result) { 
		}
		@Override protected void onPreExecute() { 
		}
		@Override protected Void doInBackground(Void... params) { 
			Log. d(TAG,"backgroundLoadImage");
			imageFile = LoadImage(imageFileUrl);
			return null ;
		}
	}

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
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			Log.w(TAG,"IOException");
		}
		return stream ;
	}
}
