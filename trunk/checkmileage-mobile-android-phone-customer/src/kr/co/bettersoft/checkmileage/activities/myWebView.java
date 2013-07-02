package kr.co.bettersoft.checkmileage.activities;

/**
 * myWebView
 * 
 * 설정에서 웹뷰를 사용해서 웹페이지를 보여줘야 할경우 사용되는 웹뷰 액티비티.
 *  필요한 URL은 전달받은 값을 사용.
 */

import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.common.CommonConstant;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

	// 로케일
	Locale systemLocale = null;
	String strCountry = "";
	String strLanguage = "";

	String postData;
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
				if(b.getInt("showErrToast")==1){
					Toast.makeText(myWebView.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
					// 페이지를 불러오는데 실패했습니다.\n잠시후 다시 시도해주시기 바랍니다.		
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//	    requestWindowFeature(Window.FEATURE_NO_TITLE );	// 타이틀바 제거
		setContentView(R.layout.my_web_view);
		Intent rIntent = getIntent();
		loadingURL = rIntent.getStringExtra("loadingURL");			// URL 정보를 받음.
		mWeb = (WebView)findViewById(R.id.web);

		//사용자 지역, 언어
		systemLocale = getResources().getConfiguration().locale;
		strCountry = systemLocale.getCountry();
		strLanguage = systemLocale.getLanguage();

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
			////			postData = "Merchant-Language="+strLanguage+"&Merchant-Country="+strCountry;				// 파라미터 : Merchant-Language / Merchant-Country
			////			mWeb.postUrl(loadingURL, EncodingUtils.getBytes(postData, "BASE64"));
			//			mWeb.getSettings().setJavaScriptEnabled(true);


			//			new backgroundWebView().execute();		// 비동기로 URL 오픈 실행
			// 비동기 -> 바로 열도록 수정
			postData = "Merchant-Language="+strLanguage+"&Merchant-Country="+strCountry;				// 파라미터 : Merchant-Language / Merchant-Country
			mWeb.postUrl(loadingURL, EncodingUtils.getBytes(postData, "BASE64"));
			mWeb.getSettings().setJavaScriptEnabled(true);
			//			mWeb.loadUrl(loadingURL);		// url
		}else{
			Toast.makeText(myWebView.this, R.string.cant_find_url, Toast.LENGTH_SHORT).show();
		}
	}
	// 비동기 실행
	//	public class backgroundWebView extends  AsyncTask<Void, Void, Void> { 
	//		@Override protected void onPostExecute(Void result) {  
	//		} 
	//		@Override protected void onPreExecute() {  
	//		} 
	//		@Override protected Void doInBackground(Void... params) {  
	//			runOnUiThread(new Runnable(){
	//				public void run(){
	//					postData = "Merchant-Language="+strLanguage+"&Merchant-Country="+strCountry;				// 파라미터 : Merchant-Language / Merchant-Country
	//					mWeb.postUrl(loadingURL, EncodingUtils.getBytes(postData, "BASE64"));
	//					mWeb.getSettings().setJavaScriptEnabled(true);
	//					}
	//			});
	////			postData = "Merchant-Language="+strLanguage+"&Merchant-Country="+strCountry;				// 파라미터 : Merchant-Language / Merchant-Country
	////			mWeb.postUrl(loadingURL, EncodingUtils.getBytes(postData, "BASE64"));
	//			return null; 
	//		}
	//	}


	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 			// 취소버튼 누르면 웹뷰의 백버튼
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWeb.canGoBack()) { 
			mWeb.goBack(); 
			return true; 
		} 
		return super.onKeyDown(keyCode, event); 
	}
	/**
	 * MyWebViewClient
	 * 페이지 로드, 완료 이벤트발생 가능한 웹뷰 클라이언트
	 *
	 */
	private class MyWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		/**
		 * onPageFinished
		 * 로딩 끝나면 프로그래스바 숨기고 재로딩 가능하도록한다
		 *
		 * @param view
		 * @param url
		 * @return
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			hidePb();
		}
		/**
		 * onPageStarted
		 * 웹뷰 로딩 시작하면 시간 재서 로딩 안되면 멈추고 알린다.
		 *
		 * @param view
		 * @param url
		 * @param favicon
		 * @return
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			showPb();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {		// 기다렸다가 체크해서 안끝났으면 중지
						Thread.sleep(CommonConstant.serverConnectTimeOut);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					checkMyWebViewLoaded();
				}
			}).start();
		}
	}
	public void checkMyWebViewLoaded(){
		runOnUiThread(new Runnable(){
			public void run(){
				if(mWeb.getProgress()<100) {
					// do what you want
					mWeb.stopLoading();
					hidePb();
					showErrMsg();
					finish();
				}
			}
		});


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
	/**
	 * showPb
	 *  중앙 프로그래스바 가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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
	/**
	 * hidePb
	 *  중앙 프로그래스바 비가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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

	/**
	 * showErrMsg
	 *  화면에 error 토스트 띄운다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void showErrMsg(){			
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();				
						Bundle b = new Bundle();
						b.putInt("showErrToast", 1);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	} 

	/**
	 * onStop
	 *  액티비티 정지할때 웹뷰 로딩도 정지한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Override
	public void onStop(){
		if(mWeb!=null){
			runOnUiThread(new Runnable(){
				public void run(){
					mWeb.stopLoading();
				}
			});

		}
		super.onStop();
	}

}
