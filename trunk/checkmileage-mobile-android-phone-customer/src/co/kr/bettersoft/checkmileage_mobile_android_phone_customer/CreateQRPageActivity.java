package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// QR ���� ������
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
 * QR �� �����ϰ� �ٷ� �����ܰ��� ���� QR �ڵ� �����Ƽ��Ƽ�� �Ѿ��.
 * ����ڿ��� �� ��Ƽ��Ƽ�� �������� �ʰ� �ٷ� ���� QR �ڵ庸�� ȭ���� ��Ÿ���� ��1��.
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
	     *  ������ ����Ͽ� QR ����.
	     */
	    // QR �ڵ� ��ü �����ϴ� �κ�..
	    // ... QR �ڵ带 �����ϰ�, ������ ����Ѵ�.
	    // ���� ���� �ϵ��ڵ� �ؽ�Ʈ �����.
	    
	    /*
	     * QR ����� ���Ͽ� ����.
	     */
	    Log.i("CreateQRPageActivity", "save qrcode to file : "+qrcode);
	    
//	    CommonUtils.writeQRstr = qrcode;	// qr ����� ������.
//	    saveQR();							// qr ����� ��� ����.
	    saveQRforPref(qrcode);				// ���� ���� �����.
	    
	  
	    
	    /*
	     * MyQR�������� ������ QR�� QR�̹��� �޾Ƽ� ������.
	     */
	    Log.i("CreateQRPageActivity", "load qrcode to img : "+qrcode);
	    MyQRPageActivity.qrCode = qrcode;

	    new Thread(
	    		new Runnable(){
	    			public void run(){
	    				try{
	    					Thread.sleep(1000);
	    					Log.i("CreateQRPageActivity", "qrResult::"+qrResult);		// �б� ��� ����.
	    					// ���� QR �ڵ� ����� �̵�.
	    					Log.i("CreateQRPageActivity", "QR registered Success");
	    					Intent intent2 = new Intent(CreateQRPageActivity.this, Main_TabsActivity.class);
	    					startActivity(intent2);
	    					finish();		// �ٸ� ��Ƽ��Ƽ�� ȣ���ϰ� �ڽ��� ����.
	    				}catch(InterruptedException ie){
	    					ie.printStackTrace();
	    				}
	    			}
	    		}
	    ).start();
	}
	
	
	// QR �ڵ� ����ҿ� QR �ڵ带 �����Ѵ�. 
    public void saveQR(){		
    	CommonUtils.callCode = 22;		// ���� ���
    	Intent saveQRintent = new Intent(CreateQRPageActivity.this, CommonUtils.class);			// ȣ��
    	startActivity(saveQRintent);
    }
    // pref �� QR ���� ���. ������ ��� ����.
    public void saveQRforPref(String qrCode){
    	sharedPrefCustom = getSharedPreferences("MyCustomePref",
    			Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    	SharedPreferences.Editor saveQR = sharedPrefCustom.edit();
    	saveQR.putString("qrcode", qrCode);
    	saveQR.commit();
    }
}
