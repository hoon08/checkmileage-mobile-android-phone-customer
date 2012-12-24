package kr.co.bettersoft.checkmileage.activities;
/**
 * PushDetail
 * 
 * ���������� ������ �̺�Ʈ �� ���� ȭ��.
 * 
 * ȭ�� ������ �ֻ�ܿ� Ÿ��Ʋ,  ��ܺο� �̹���(÷�� �̹���). �ϴܺο� �ؽ�Ʈ. 
 * Ÿ��Ʋ, �̹����� �������°� �ϴ� �ؽ�Ʈ�� ������ ���� ��� ��ũ�� �ǵ��� �Ѵ�. 
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
	String subject="";				// ����
	String content="";				// ����
	String imageFileUrl="";			// �̹��� URL
	String imageFileStr="";			// ���ڿ��� �ٲ� �̹���
	String modifyDate="";			// ������ 
	String companyName="";			// ��������
	Bitmap imageFile= null;			// �̹��� ����

	TextView subjectView;
	ImageView imageFileView ;
	TextView contentView ;
	TextView companyNameView ;	
	int maxPRstr = 200;					// ȭ�鿡 ������ �Ұ� ���� �ִ� ���ڼ�. �Ѿ�� �ڸ��� ... ���� ǥ������.
	// �����
	ProgressBar pb1;		// �ߴ� �ε� �����
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// �޾ƿ°���� ȭ�鿡 �ѷ��ش�.
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
					// ���α׷����� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.push_detail_progressbar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���α׷�����  ����
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

	// �߾� ���α׷����� ����, ����
	/**
	 * showPb
	 *  �߾� ���α׷����� ����ȭ�Ѵ�
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
	 *  �߾� ���α׷����� �񰡽�ȭ�Ѵ�
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

		// ������ ����
		Intent receiveIntent = getIntent();
		subject = receiveIntent.getStringExtra("subject");				// �̺�Ʈ ����
		content = receiveIntent.getStringExtra("content");				// �̺�Ʈ �۱�
		imageFileUrl = receiveIntent.getStringExtra("imageFileUrl");	// �̺�Ʈ ���� �̹��� �ּ�	
		imageFileStr = receiveIntent.getStringExtra("imageFileStr");	// �̺�Ʈ ���� �̹��� ����ȭ
		modifyDate = receiveIntent.getStringExtra("modifyDate");		// �̺�Ʈ ���� ��¥
		companyName = receiveIntent.getStringExtra("companyName");		// �̺�Ʈ ��ü��

		//	    Log.d(TAG,"subject:"+subject+"//content:"+content+"//imageFileUrl:"+imageFileUrl);
		//	    Log.d(TAG,"imageFileStr:"+imageFileStr+"//modifyDate:"+modifyDate+"//companyName:"+companyName);

		if(imageFileUrl.length()>0){			// �̹����� �־�� �ϴ� ���
			if(imageFileStr.length()>0){									// �̺�Ʈ �̹��� - �� ���ڿ��� �����Ͱ� �Ѿ�� ���. ��ȯ.
				byte[] decodedString = Base64.decode(imageFileStr, Base64.DEFAULT); 
				imageFile = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			}else{													// �̹����� ���ڿ��� �Ѿ���� ���� ���. �ٽ� ����.
				new backgroundLoadImage().execute();
				//				LoadImage(imageFileUrl);
			}
		}
		if(imageFile==null){			// �̹����� ����� �ϴ� ���  �� ������ ������ ��� - �⺻ �̹����� ó��
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
		pb1 = (ProgressBar) findViewById(R.id.push_detail_progressbar01);		// �ε�(�߾�)
		showPb();
		showInfo();
	}

	/**
	 * returnThis
	 *  ���ý�Ʈ�� �����Ѵ� (�ڵ鷯���� ���)
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
	 *  ȭ�鿡 error �佺Ʈ ����
	 *
	 * @param
	 * @param
	 * @return
	 */
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



	// ȭ�鿡 �����ش�..
	/**
	 * showInfo
	 *  �̺�Ʈ ������ ȭ�鿡 �����ش�
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
	 * Bitmap �̹��� ��������   // ������������ ��� ����
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
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
	//		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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


	// �̺�Ʈ �̹��� : URL ���� �̹��� �޾ƿͼ� �����ο� �����ϴ� �κ�.
	// �񵿱�� �̺�Ʈ ��� �������� �Լ� ȣ��.
	/**
	 * backgroundLoadImage
	 *  �񵿱�� �� �̹����� �ε��Ѵ�
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
	 *  ������ �̹��� URL ���� �̹��� �޾ƿ� ��Ʈ���� ��Ʈ������ �����Ѵ�
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
	 *  ������ �̹��� URL ���� �̹��� �޾ƿͼ� ��Ʈ������ �����Ѵ�
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
