package kr.co.bettersoft.checkmileage.activities;

/*
 * �������� ���並 ����ؼ� ���������� ������� �Ұ�� ���Ǵ� ���� ��Ƽ��Ƽ.
 *  �ʿ��� URL�� ���޹��� ���� ���.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class myWebView extends Activity {
	WebView mWeb;
	String loadingURL = "";
	
	// �����
	ProgressBar pb1;		// �ߴ� �ε� �����
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		
				if(b.getInt("order")==1){
					// ���α׷����� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.webview_progressbar1);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���α׷�����  ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.webview_progressbar1);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE );	// Ÿ��Ʋ�� ����
	    setContentView(R.layout.my_web_view);
	    Intent rIntent = getIntent();
	    loadingURL = rIntent.getStringExtra("loadingURL");			// URL ������ ����.
	    mWeb = (WebView)findViewById(R.id.web);
	    
	 // �̹��� Ȯ��/���/��ũ�� ����. -> ������� ����.  --> ����
//		mWeb.getSettings().setUseWideViewPort(true);		
	    mWeb.setWebViewClient(new MyWebViewClient());  // WebViewClient ����          
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
			Toast.makeText(myWebView.this, R.string.cant_find_url, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 			// ��ҹ�ư ������ ������ ���ư
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWeb.canGoBack()) { 
        	mWeb.goBack(); 
            return true; 
        } 
        return super.onKeyDown(keyCode, event); 
    }
     
    private class MyWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			hidePb();
		}
		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			showPb();
		}
	}
	/**
	 * WebChromeClient �� ����ϴ� Ŭ�����̴�.
	 * alert �̳� ������ �ݱ� ���� web ������ �̺�Ʈ�� ���ϱ� ���� Ŭ�����̴�.
	 * @author johnkim
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
	// �߾� ���α׷����� ����, ����
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
}
