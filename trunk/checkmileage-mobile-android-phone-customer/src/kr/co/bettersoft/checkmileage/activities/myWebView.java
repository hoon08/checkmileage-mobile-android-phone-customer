package kr.co.bettersoft.checkmileage.activities;

/*
 * 설정에서 웹뷰를 사용해서 웹페이지를 보여줘야 할경우 사용되는 웹뷰 액티비티.
 *  필요한 URL은 전달받은 값을 사용.
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
	
	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		
				if(b.getInt("order")==1){
					// 프로그래스바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.webview_progressbar1);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 프로그래스바  종료
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
	    requestWindowFeature(Window.FEATURE_NO_TITLE );	// 타이틀바 제거
	    setContentView(R.layout.my_web_view);
	    Intent rIntent = getIntent();
	    loadingURL = rIntent.getStringExtra("loadingURL");			// URL 정보를 받음.
	    mWeb = (WebView)findViewById(R.id.web);
	    
	 // 이미지 확대/축소/스크롤 금지. -> 허용으로 수정.  --> 금지
//		mWeb.getSettings().setUseWideViewPort(true);		
	    mWeb.setWebViewClient(new MyWebViewClient());  // WebViewClient 지정          
		mWeb.setWebChromeClient(new MyWebChromeClient());
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
    public boolean onKeyDown(int keyCode, KeyEvent event) { 			// 취소버튼 누르면 웹뷰의 백버튼
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
	 * WebChromeClient 를 상속하는 클래스이다.
	 * alert 이나 윈도우 닫기 등의 web 브라우저 이벤트를 구하기 위한 클래스이다.
	 * @author johnkim
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
	// 중앙 프로그래스바 보임, 숨김
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
