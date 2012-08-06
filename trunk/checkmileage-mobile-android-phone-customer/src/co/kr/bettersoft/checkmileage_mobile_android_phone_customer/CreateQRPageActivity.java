package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 생성 페이지
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class CreateQRPageActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    /* 
		  * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
		  * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
		  */
		    
	    // QR 코드 자체 생성하는 부분..
	    // ... QR 코드를 생성하고, 서버에 등록한다.
	    
		
//	    try {
//			CreateQR();
//		} catch (WriterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		    
	    // 나의 QR 코드 보기로 이동.
	    Log.i("CreateQRPageActivity", "QR registered Success");
		 Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
	        startActivity(intent2);
	        finish();
	}

	
	
	
	
	
	@SuppressWarnings("null")
	public static void CreateQR() throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        String text = "test1234";
        text = new String(text.getBytes("UTF-8"), "ISO-8859-1");
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,
                100, 100);
//        
//        Log.i("CreateQRPageActivity", "lv2");
//        String st =  bitMatrix.toString();
//        Log.i("CreateQRPageActivity", "lv3");
//        byte[] data  = st.getBytes();
//        Log.i("CreateQRPageActivity", "lv4");
//        ByteArrayInputStream in = new ByteArrayInputStream(data);
//        Log.i("CreateQRPageActivity", "lv5");
//        Bitmap bm=null; 
//        BitmapDrawable bmd = new BitmapDrawable(in); 
//        //	        	in.read(data);		// null pointer exception
//        Log.i("CreateQRPageActivity", "lv6");
////        Bitmap bitmap = BitmapFactory.decodeStream(in);  
//        
//        bm = bmd.getBitmap(); 
////        Log.i("CreateQRPageActivity", "6-1"+bitmap.getHeight());
//
//        Log.i("CreateQRPageActivity", "lv7");

////        MatrixToImageWriter.writeToFile(bitMatrix, "png", new File("qrcode.png"));	// cant find class exception
    }

	
}
