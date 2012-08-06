package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CommonUtils extends Activity {
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        //파일 쓰기
	        FileOutputStream fos;
	        String strFileContents = "Test testtetests ttekltjskl tetjkelt ttejt  tjt tj t ek tk ek  kek e";
	        try {
	   fos = openFileOutput("Filename.txt",MODE_PRIVATE);
	   fos.write(strFileContents.getBytes());
	   fos.close();
	  } catch (FileNotFoundException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	  }      

	  //파일 읽기
	  String strFileName = "Filename.txt";
	  StringBuffer strBuffer = new StringBuffer();
	  try {
	   FileInputStream fis = openFileInput(strFileName.toString());
	   
	   DataInputStream dataIO = new DataInputStream(fis);
	   
	   String strLine = null;
	   
	   while( (strLine = dataIO.readLine()) != null)          // 파일 내 줄바꿈
	    strBuffer.append(strLine + "\n");
	   
	   dataIO.close();
	   fis.close();
	  } catch (FileNotFoundException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	  
	  TextView textView = new TextView(this);
	  textView.setText(strBuffer);
	  
	  setContentView(textView);
	    }
	}
