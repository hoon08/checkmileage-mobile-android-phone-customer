package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 내 QR 보기 화면
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class MyQRPageActivity extends Activity {
	int app_end = 0;			// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	/** Called when the activity is first created. */
	static Bitmap bmp =null;
	 static Bitmap bmp2 =null;
	static ImageView imgView;
	public static String qrCode = "";
	static // 핸들러 등록
	Handler handler = new Handler(){
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
		
		/*
		 *  QR 코드를 받아옴.  구글 웹페이지를 통한 생성
		 */
	    new Thread(
        		new Runnable(){
        			public void run(){
        				 bmp = downloadBitmap("http://chart.apis.google.com/chart?cht=qr&chs="+qrSize+"x"+qrSize+"&choe=UTF-8&chld=H&chl="+qrCode); 
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
	
	/*
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MyQRPageActivity.this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
}
