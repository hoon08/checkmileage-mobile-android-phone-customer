package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class myWebView extends Activity {
	WebView mWeb;
	String loadingURL = "";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_web_view);
	    
	    Intent rIntent = getIntent();
	    loadingURL = rIntent.getStringExtra("loadingURL");			// URL ������ ����.
	    
	    mWeb = (WebView)findViewById(R.id.web);
	    
	 // �̹��� Ȯ��/���/��ũ�� ����. -> ������� ����.  --> ����
//		mWeb.getSettings().setUseWideViewPort(true);		
	    
		mWeb.setWebChromeClient(new MyWebChromeClient());
		WebSettings webSet = mWeb.getSettings();
		
		// JavaScript ���.
		webSet.setJavaScriptEnabled(true);

		// Ȯ��/��� ����. -> ������� ����.  --> ����
//		webSet.setSupportZoom(true);
//		webSet.setBuiltInZoomControls(true);
		
		if(loadingURL.length()>0){
			mWeb.loadUrl(loadingURL);		// url
		}else{
			Toast.makeText(myWebView.this, "��û�Ͻ� �ּҸ� ã�� �� �����ϴ�.", Toast.LENGTH_SHORT).show();
		}
		
	}
	/**
	 * WebChromeClient �� ����ϴ� Ŭ�����̴�.
	 * alert �̳� ������ �ݱ� ���� web ������ �̺�Ʈ�� ���ϱ� ���� Ŭ�����̴�.
	 * 
	 * @author johnkim
	 *
	 */
	private class MyWebChromeClient extends WebChromeClient {

		//Javascript alert ȣ�� �� ����
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			final JsResult finalRes = result;

			//AlertDialog ����
			new AlertDialog.Builder(view.getContext())
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finalRes.confirm(); 
				}
			})
			.setCancelable(false)
			.create()
			.show();
			return true;
		}
	}
}
