package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR 생성 페이지
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import com.pref.PrefActivityFromResource;
/* 
 * QR 을 생성하고 바로 다음단계인 나의 QR 코드 보기액티비티로 넘어간다.
 * 사용자에게 이 액티비티는 보여지지 않고 바로 나의 QR 코드보기 화면이 나타나게 된1다.
 */
public class CreateQRPageActivity extends Activity {
	SharedPreferences sharedPrefCustom;
	
	static int qrResult = 0;
	String qrcode = "test1234";
//	String qrcode = "createdNewQRCodeOne";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    /*
	     *  서버와 통신하여 QR 생성.
	     */
	    // QR 코드 자체 생성하는 부분..
	    // ... QR 코드를 생성하고, 서버에 등록한다.
	    // 현재 위의 하드코딩 텍스트 사용함.
	    
	    /*
	     * QR 저장소 파일에 저장.
	     */
	    Log.i("CreateQRPageActivity", "save qrcode to file : "+qrcode);
	    
//	    CommonUtils.writeQRstr = qrcode;	// qr 저장소 사용안함.
//	    saveQR();							// qr 저장소 사용 안함.
	    saveQRforPref(qrcode);				// 설정 파일 사용함.
	    
	  
	    
	    /*
	     * MyQR페이지에 생성된 QR로 QR이미지 받아서 보여줌.
	     */
	    Log.i("CreateQRPageActivity", "load qrcode to img : "+qrcode);
	    MyQRPageActivity.qrCode = qrcode;

	    new Thread(
	    		new Runnable(){
	    			public void run(){
	    				try{
	    					Thread.sleep(1000);
	    					Log.i("CreateQRPageActivity", "qrResult::"+qrResult);		// 읽기 결과 받음.
	    					// 나의 QR 코드 보기로 이동.
	    					Log.i("CreateQRPageActivity", "QR registered Success");
	    					Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
	    					startActivity(intent2);
	    					finish();		// 다른 액티비티를 호출하고 자신은 종료.
	    				}catch(InterruptedException ie){
	    					ie.printStackTrace();
	    				}
	    			}
	    		}
	    ).start();
	}
	
	
	// QR 코드 저장소에 QR 코드를 저장한다. 
    public void saveQR(){		
    	CommonUtils.callCode = 22;		// 쓰기 모드
    	Intent saveQRintent = new Intent(CreateQRPageActivity.this, CommonUtils.class);			// 호출
    	startActivity(saveQRintent);
    }
    // pref 에 QR 저장 방식. 위에거 대신 쓸것.
    public void saveQRforPref(String qrCode){
    	sharedPrefCustom = getSharedPreferences("MyCustomePref",
    			Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    	SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
    	saveQR.putString("qrcode", qrCode);
    	saveQR.commit();
    }
}
