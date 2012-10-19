package kr.co.bettersoft.checkmileage.activities;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class myWebView extends Activity {
	WebView mWeb;
	String loadingURL = "";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE );	// 타이틀바 제거
	    setContentView(R.layout.my_web_view);
	    Intent rIntent = getIntent();
	    loadingURL = rIntent.getStringExtra("loadingURL");			// URL 정보를 받음.
	    
	    mWeb = (WebView)findViewById(R.id.web);
	    
	 // 이미지 확대/축소/스크롤 금지. -> 허용으로 수정.  --> 금지
//		mWeb.getSettings().setUseWideViewPort(true);		
	    mWeb.setWebViewClient(new myWebViewClient());  // WebViewClient 지정          
//		mWeb.setWebChromeClient(new MyWebChromeClient());
		WebSettings webSet = mWeb.getSettings();
		
		// JavaScript 허용.
		webSet.setJavaScriptEnabled(true);

		// 확대/축소 금지. -> 허용으로 수정.  --> 금지
//		webSet.setSupportZoom(true);
//		webSet.setBuiltInZoomControls(true);
		
		if(loadingURL.length()>0){
			mWeb.loadUrl(loadingURL);		// url
		}else{
			Toast.makeText(myWebView.this, R.string.cant_find_url, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWeb.canGoBack()) { 
        	mWeb.goBack(); 
            return true; 
        } 
        return super.onKeyDown(keyCode, event); 

    }
     
    private class myWebViewClient extends WebViewClient { 
        @Override 
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            view.loadUrl(url); 
            return true; 
        } 
    }
	
	
	/**
	 * WebChromeClient 를 상속하는 클래스이다.
	 * alert 이나 윈도우 닫기 등의 web 브라우저 이벤트를 구하기 위한 클래스이다.
	 * 
	 * @author johnkim
	 *
	 */
	private class MyWebChromeClient extends WebChromeClient {
		//Javascript alert 호출 시 실행
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			final JsResult finalRes = result;
			//AlertDialog 생성
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
