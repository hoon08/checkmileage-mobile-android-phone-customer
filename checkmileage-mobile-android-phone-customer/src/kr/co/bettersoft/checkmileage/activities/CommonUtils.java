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
	static int callCode = 0;		// 호출 모드 . 읽기:1, 쓰기:2, 초기화:3
	static int qrResult = 0;		// 처리 결과값. 성공:1, 실패:: 파일없음:-3,입출력오류:-2,그외:-1
	
	static String imgDomain = "http://www.mcarrot.net/upload/profile/"; 	
	static String imgthumbDomain = "http://www.mcarrot.net/upload/thumb/"; 	
	static String imgPushDomain = "http://www.mcarrot.net/upload/pushThumb/"; 	
	
	
	static String serverNames = "checkmileage.mcarrot.net";
	
	public static String packageNames = "kr.co.bettersoft.checkmileage.activities";
	
//	public static String prefPath = "kr.co.bettersoft.checkmileage.pref";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(callCode==1){	// 읽기 모드			// from main page
			qrResult = readProps();
			MainActivity.qrResult = qrResult;
		}else if(callCode==2){ // 쓰기 모드		// from main page (test)
			qrResult = writeProps();
			MainActivity.qrResult = qrResult;
		}else if(callCode==3){	// 초기화 모드		// from main page (test)
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
		else{		// 그외. 비정상적인 호출.
			Log.i("CommonUtils", "callCode:"+callCode);
			MainActivity.qrResult = qrResult;
		}
		finish();
	}
	
	public int writeProps(){
		//파일 쓰기					// 기존 파일을 덮어쓴다. 응용하여 더하기도 가능..
		FileOutputStream fos;
		String strFileContents = writeQRstr;
		try {
			fos = openFileOutput("Filename.txt",MODE_PRIVATE);			// 자신만.
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
		//파일 읽기
		String strFileName = "Filename.txt";
		StringBuffer strBuffer = new StringBuffer();
		try {
			FileInputStream fis = openFileInput(strFileName.toString());
			DataInputStream dataIO = new DataInputStream(fis);
			String strLine = null;
			String tmpStr = "";
			while( (strLine = dataIO.readLine()) != null)          // 파일 내 줄바꿈
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
		}catch(Exception e){		// 파일이 없는 경우
//			e.printStackTrace();
			return -1;
		}
//		TextView textView = new TextView(this);
//		textView.setText(strBuffer);
//		setContentView(textView);
	}
	
}
