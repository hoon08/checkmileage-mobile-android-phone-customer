package kr.co.bettersoft.checkmileage.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CommonUtils extends Activity {
    
	static String writeQRstr = "test1234";
	static int callCode = 0;		// ȣ�� ��� . �б�:1, ����:2, �ʱ�ȭ:3
	static int qrResult = 0;		// ó�� �����. ����:1, ����:: ���Ͼ���:-3,����¿���:-2,�׿�:-1
	
	static String imgDomain = "http://www.mcarrot.net/upload/profile/"; 	
	static String imgthumbDomain = "http://www.mcarrot.net/upload/thumb/"; 	
	static String imgPushDomain = "http://www.mcarrot.net/upload/pushThumb/"; 	
	
	
	static String serverNames = "checkmileage.mcarrot.net";
	
	public static String packageNames = "kr.co.bettersoft.checkmileage.activities";
	
//	public static String prefPath = "kr.co.bettersoft.checkmileage.pref";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(callCode==1){	// �б� ���			// from main page
			qrResult = readProps();
			MainActivity.qrResult = qrResult;
		}else if(callCode==2){ // ���� ���		// from main page (test)
			qrResult = writeProps();
			MainActivity.qrResult = qrResult;
		}else if(callCode==3){	// �ʱ�ȭ ���		// from main page (test)
			writeQRstr="";
			qrResult = writeProps();
			MainActivity.qrResult = qrResult;
		}else if(callCode==22){				// from create qr page
			qrResult = writeProps();
			CreateQRPageActivity.qrResult = qrResult;
		}else if(callCode==32){				// from scan qr page
			qrResult = writeProps();
			ScanQRPageActivity.qrResult = qrResult;
		}
		else{		// �׿�. ���������� ȣ��.
			Log.i("CommonUtils", "callCode:"+callCode);
			MainActivity.qrResult = qrResult;
		}
		finish();
	}
	
	public int writeProps(){
		//���� ����					// ���� ������ �����. �����Ͽ� ���ϱ⵵ ����..
		FileOutputStream fos;
		String strFileContents = writeQRstr;
		try {
			fos = openFileOutput("Filename.txt",MODE_PRIVATE);			// �ڽŸ�.
			fos.write(strFileContents.getBytes());
			fos.close();
			return 1;
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			return -3;
		} catch (IOException e) {
//			e.printStackTrace();
			return -2;
		} catch(Exception e){
			return -1;
		}
	}
	
	public  int readProps(){
		//���� �б�
		String strFileName = "Filename.txt";
		StringBuffer strBuffer = new StringBuffer();
		try {
			FileInputStream fis = openFileInput(strFileName.toString());
			DataInputStream dataIO = new DataInputStream(fis);
			String strLine = null;
			String tmpStr = "";
			while( (strLine = dataIO.readLine()) != null)          // ���� �� �ٹٲ�
			{
				strBuffer.append(strLine + "\n");
				tmpStr = strLine;
			}
			dataIO.close();
			fis.close();
			MainActivity.myQR = tmpStr;
			MyQRPageActivity.qrCode = tmpStr;
			return 1;
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			return -3;
		} catch (IOException e) {
//			e.printStackTrace();
			return -2;
		}catch(Exception e){		// ������ ���� ���
//			e.printStackTrace();
			return -1;
		}
//		TextView textView = new TextView(this);
//		textView.setText(strBuffer);
//		setContentView(textView);
	}
	
}
