package kr.co.bettersoft.checkmileage.activities;
/*
 *  가맹점 목록. (검색?)
 *  
 *  
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.adapters.ImageAdapter;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.os.AsyncTask;

public class MemberStoreListPageActivity extends Activity implements OnItemSelectedListener, OnEditorActionListener{
	
	String TAG = "MemberStoreListPageActivity";
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록	// 처음 두번 자동 실행  되는거.
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	String serverName = CommonUtils.serverNames;
	
	// Locale
    Locale systemLocale = null ;
//    String strDisplayCountry = "" ;
    String strCountry = "" ;
    String strLanguage = "" ;
    
//	int dontTwice = 1;				// 스피너 리스너로 인한 초기 2회 조회 방지. 
	public boolean connected = false;  // 인터넷 연결상태
	String myQRcode = "";			// 내 아이디
	
	int responseCode = 0;			// 서버 조회 결과 코드
	String controllerName = "";		// 서버 조회시 컨트롤러 이름
	String methodName = "";			// 서버 조회시 메서드 이름
	String searchWordArea = "";		// 서버 조회시 지역명
	String searchWordType = "";		// 서버 조회시 업종명
	
	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인. 
	
//	Spinner searchSpinnerArea;		// 상단 지역 목록
	Spinner searchSpinnerType;		// 상단 업종 목록
	
	TextView searchText;			//검색어
	Button searchBtn;				//검색버튼
	InputMethodManager imm;
	int indexDataFirst = 0;			// 부분 검색 위한 인덱스. 시작점
	int indexDataLast = 0;			// 부분 검색 위한 인덱스. 끝점
	int indexDataTotal = 0;			// 부분 검색 위한 인덱스. 전체 개수
	
	Boolean mIsLast = false;			// 끝까지 갔음. true 라면 더이상의 추가 없음. 새 조회시 false 로 초기화
	Boolean adding = false;			// 데이터 더하기 진행 중임.
	Boolean newSearch = false; 		// 새로운 조회인지 여부. 새로운 조회라면 기존 데이터는 지우고 새로 검색한 데이터만 사용. 새로운 조회가 아니라면 기존 데이터에 추가 데이터를 추가.
	Boolean jobKindSearched = false;
	Bitmap bm = null;
	int reTry = 5;
	
	private ImageAdapter imgAdapter;
	
	public ArrayList<CheckMileageMerchants> entries1 = new ArrayList<CheckMileageMerchants>();	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)   // 저장용.
	ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>();			// 잘라서 더하는 부분.
	List<CheckMileageMerchants> entriesFn = new ArrayList<CheckMileageMerchants>();			// 최종 산출물
	
	
	float fImgSize = 0;			// 이미지 사이즈 저장변수.
	int isRunning = 0;			// 연속 실행 방지. 실행 중에 다른 실행 요청이 들어올 경우, 무시한다.
	View emptyView;				// 데이터 없음 뷰

	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바
	ProgressBar pb2;		// 하단 추가 진행바
	
	// ListView에 뿌릴 Data 를 위한 스피너 데이터들. --> 나중에 서버 통신하여 처음에 가져와서 만들어 지도록 한다.
	String[] areas = {"전지역", "홍대", "신촌", "영등포", "신림", "강남", "종로", "건대", "노원", "대학로", "여의도"};			// 나중에 조회 해 올 것..
	String[] jobs = {"", ""};
	String[] tmpJobs = null;
	GridView gridView;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// 받아온 마일리지 결과를 화면에 뿌려준다.
					Log.d(TAG,"showYN");
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 여기 리스트 레이아웃.
					if((entriesFn!=null)&&(entriesFn.size()>0)){
//						Log.e(TAG,"indexDataFirst::"+indexDataFirst);
						if(newSearch){		// 새로운 검색일 경우 새로 설정, 추가일 경우 알림만 하기 위함.
							setGriding();
							newSearch = false;		// 다시 돌려놓는다. 이제는 최초 검색이 아님.
						}else{
//							Log.e(TAG,"notifyDataSetChanged");
							imgAdapter.notifyDataSetChanged();		// 알림 -> 변경사항이 화면상에 업데이트 되도록함.
						}
						gridView.setEnabled(true);			// 그리드 뷰 허용함.
					}else{
						Log.d(TAG,"no data");
						emptyView = findViewById(R.id.empty1);		// 데이터 없으면 '빈 페이지'(데이터 없음 메시지)표시
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					
					adding = false;		// 조회 및 추가 끝났음. 다른거 조회시 또 추가 가능.. (스크롤 리스너를 다룰때 사용)
					// 하단 로딩바를 숨긴다.
					hidePb2();
					
					isRunning = 0;		// 진행중이지 않음. - 이후 추가 조작으로 새 조회 가능.
//					searchSpinnerArea.setEnabled(true);
					searchSpinnerType.setEnabled(true);
					searchText.setEnabled(true); 
					searchBtn.setEnabled(true);
				}
				if(b.getInt("order")==1){
					// 프로그래스바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 프로그래스바  종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
				
				if(b.getInt("order")==3){
					// 하단 프로그래스바 실행
					if(pb2==null){
						pb2=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);
					}
					pb2.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==4){
					// 하단 프로그래스바  종료
					if(pb2==null){
						pb2=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);
					}
					pb2.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){
					Toast.makeText(MemberStoreListPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("setJobsList")==1){
					jobs = tmpJobs;
					// 스피너 데이터 세팅. 
					 ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(getThis(), android.R.layout.simple_spinner_item, jobs);
					 aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					 searchSpinnerType.setAdapter(aa2);
					 jobKindSearched = true;			// 업종 검색 끝.
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	public Context getThis(){
		return this;
	}
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);
		
		// 내 QR 코드. 
		myQRcode = MyQRPageActivity.qrCode;		
		entriesFn = new ArrayList<CheckMileageMerchants>();
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 	// 가상키보드 닫기위함
		
		// 크기 측정
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
		if(screenWidth < screenHeight ){fImgSize = screenWidth;
	    }else{fImgSize = screenHeight;}
		
		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);		// 로딩(중앙)
		pb2 = (ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);		// 로딩(하단)
		
		searchText = (TextView) findViewById(R.id.store_search_text);			//검색어
		searchBtn = (Button) findViewById(R.id.store_search_btn);				//검색버튼
		searchText.setOnEditorActionListener(this);  
		searchText.addTextChangedListener(textWatcherInput);  

//		searchBtn.hasFocus();
//		searchBtn.isFocused()
		searchBtn.setOnClickListener(new Button.OnClickListener()  {
			public void onClick(View v)  {
				goSearch();		 // 단어로 검색 ㄱㄱ 
			}
		});

		// spinner
		searchSpinnerType = (Spinner)findViewById(R.id.searchSpinnerType);
		// spinner listener
		searchSpinnerType.setOnItemSelectedListener(this);
		
		
	}
    
	
	// 데이터를 화면에 세팅
	public void setGriding(){
		imgAdapter = new ImageAdapter(this, entriesFn);
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(imgAdapter);
		// 클릭시 상세보기 페이지로
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				//					Toast.makeText(getApplicationContext(),((TextView) v.findViewById(R.id.label)).getText(), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MemberStoreListPageActivity.this, MemberStoreInfoPage.class);
				Log.i(TAG, "checkMileageMerchantsMerchantID::"+entriesFn.get(position).getMerchantID());
				//					Log.i(TAG, "idCheckMileageMileages::"+myQRcode);
				Log.i(TAG, "myMileage::"+entriesFn.get(position).getMileage());
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getMerchantID());		// 가맹점 아이디
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// 고유 식별 번호. (상세보기 조회용도)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// 내 마일리지
				startActivity(intent);
			}
		});
		gridView.setOnScrollListener(listScrollListener);		// 리스너 등록. 스크롤시 하단에 도착하면 추가 데이터 조회하도록.
	}
	
	
	
	// 온 스크롤 이벤트. 
	private OnScrollListener listScrollListener = new OnScrollListener(){
		// 건들기만 하면 주르륵 뜬다.  쓸수 있긴 한데 너무 막떠서.... boolean 으로 조절한다.
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
//			Log.d(TAG,"indexDataFirst:"+indexDataFirst+"/indexDataLast:"+indexDataLast+"/indexDataTotal:"+indexDataTotal);
			if((indexDataFirst + indexDataLast < indexDataTotal)||(indexDataLast!=indexDataTotal)){	// 아직 남음 또는 끝에 도달하지 않음.
				mIsLast = false;
			}
			if((totalItemCount<10) ||(indexDataLast==indexDataTotal)){		// 10개 이하(이미다 보여줌) 또는 마지막=전체 (끝에 도달)
				mIsLast = true;
			}
//			Log.d(TAG,"adding:"+adding+",mIsLast:"+mIsLast);
			//			  if(indexDataFirst==indexDataLast){		// 시작이 더 크면 문제 있는거
			//				  mIsLast = true;
			//			  }
			// 리스트 가장 하단에 도달했을 경우.. 
//			if(firstVisibleItem+visibleItemCount==totalItemCount &&(!adding)&&(!mIsLast)){			// 가장 하단.
			if(firstVisibleItem+visibleItemCount>=(totalItemCount-2) &&(!adding)&&(!mIsLast)){		// 가장 하단 -2일때 미리동작? - 좀더 나은듯.
//				Log.e(TAG, "onScroll event Occured."+"//view::"+view+"//firstVisibleItem::"+firstVisibleItem+"//visibleItemCount:"+visibleItemCount+"//totalItemCount::"+totalItemCount);
				showPb2();
				indexDataTotal = entries1.size();
				Log.d(TAG,"onScroll indexDataTotal:"+indexDataTotal);
				new backgroundGetMerchantInfo().execute();		// 비동기 실행
			}
		}
		// 스크롤 시작, 끝, 스크롤 중.. 이라는 사실을 알수 있다. 사실 필요 없음..  --> 필요해짐.. 스크롤중 조회시 에러가 발생하기 때문에 스크롤 중에는 조회가 되지 않도록 한다.. 
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {			// status0 : stop  / status1 : touch / status2 : scrolling
//			Log.d(TAG,"status:"+scrollState);
			if(scrollState==SCROLL_STATE_IDLE){
				searchSpinnerType.setEnabled(true);
				searchText.setEnabled(true); 
				searchBtn.setEnabled(true);
			}else{
				searchSpinnerType.setEnabled(false);
				searchText.setEnabled(false); 
				searchBtn.setEnabled(false);
			}
			
			
		}
	};
	
	
	// 하단 프로그래스바 보임, 숨김
	public void showPb2(){
		new Thread( 
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 3);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}
	public void hidePb2(){
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler .obtainMessage();
						Bundle b = new Bundle();
						b.putInt( "order" , 4);
						message.setData(b);
						handler .sendMessage(message);
					}
				}
		).start();
	}
	public void showMSG(){			// 화면에 error 토스트 띄움..
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
	
	
	
	
	/*
	 * 검색을 위한 가맹점 업종리스트를 가져온다.
	 *   도메인 이름 : checkMileageBusinessKind
	 *   컨트롤러 : checkMileageBusinessKindController
	 *   메소드 : selectBusinessKindList
	 *   필요 파라미터 : countryCode / languageCode / activateYn
	 *   앞의 두개는 모바일에서 값을 꺼내서 사용한다. 액티브는 Y 값을 사용.
	 *   결과 값 : List<checkMileageBusinessKind>  의 content 를 사용한다.
	 */
	public void getBusinessKindList(){
		if(CheckNetwork()){
			Log.i(TAG, "getBusinessKindList");
			
			// 로딩중입니다..  
			new Thread(	
					new Runnable(){
						public void run(){
							Message message = handler.obtainMessage();
							Bundle b = new Bundle();
							b.putInt("order", 1);
							message.setData(b);
							handler.sendMessage(message);
						}
					}
			).start();
			searchSpinnerType.setEnabled(false);
			searchText.setEnabled(false); 
			searchBtn.setEnabled(false);
			
			controllerName = "checkMileageBusinessKindController";
			methodName = "selectBusinessKindList";
			
			// locale get
			systemLocale = getResources().getConfiguration(). locale;
			//      strDisplayCountry = systemLocale.getDisplayCountry();
			strCountry = systemLocale .getCountry();
			strLanguage = systemLocale .getLanguage();
			
			// 서버 통신부
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								obj.put("countryCode", strCountry);		// 국가 코드
								obj.put("languageCode", strLanguage);			// 언어코드
								obj.put("activateYn", "Y");
								Log.w(TAG,"countryCode::"+strCountry+",languageCode:"+strLanguage);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageBusinessKind\":" + obj.toString() + "}";
							try{
								URL postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								Thread.sleep(200);
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes());
								os2.flush();
								Thread.sleep(200);
								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
								responseCode = connection2.getResponseCode();
								if(responseCode==200||responseCode==204){
									InputStream in =  connection2.getInputStream();
									// 조회한 결과를 처리.
									setBusinessKindList(in);
								}
							}catch(Exception e){ 
								e.printStackTrace();
								if(reTry>0){
									reTry = reTry-1;
									try {
										Thread.sleep(500);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									getBusinessKindList();
								}else{
									Log.w(TAG,"reTry failed. -- init reTry");
									reTry = 5;			
									searchSpinnerType.setEnabled(true);
									searchText.setEnabled(true); 
									searchBtn.setEnabled(true);
									showMSG();
								}
							}
						}
					}
			).start();
		}
	}
	
	/*
	 * 가맹점 정보 1차 데이터 받음. entries 도메인에 저장. 이후 url 정보를 꺼내 이미지를 받아오는 함수를 호출한다.
	 */
	public void setBusinessKindList(InputStream in){		
		Log.d(TAG,"setBusinessKindList");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"수신::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		Log.d(TAG,"max:"+max);				// 0 번째 모든 업종 제거.
		try {
			tmpJobs = new String[max];		// 0번째 제거로 max+1 --> max
//			tmpJobs[0] = "모든 업종";			// 나중에 바꿔야 하는데.. 다국어로. *** 		--> 0번째 모든 업종 항목 제거
			if(max>0){
				for ( int i = 0; i < max; i++ ){
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageBusinessKind");		// 대소문자 주의
					tmpJobs[i] = jsonObj.getString("content");		// 0 번째 항목 "모든 업종" 제거 --> i+1 --> i
				}
			}else{
				tmpJobs = new String[1];
				tmpJobs[0] = "Not Available";				// 검색 불가. (서버에서 받아온 업종 개수가 0개임.)
			}
			
			isRunning = 0;			// 다른 검색 가능.
			new Thread(
					new Runnable(){
						public void run(){
							Message message = handler.obtainMessage();				
							Bundle b = new Bundle();
							b.putInt("setJobsList", 1);
							message.setData(b);
							handler.sendMessage(message);
						}
					}
			).start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/*
	 * 서버와 통신하여   가맹점 목록을 가져온다. 새로 조회.			 조회 1.
	 * 그 결과를 List<CheckMileageMerchant> Object 로 반환 한다.  
	 * 
	 *  호출 대상 :: 
	 * checkMileageMerchantController // selectSearchMerchantList // checkMileageMerchant 
	 * 
	 *  보내는 정보 :  businessArea01 / businessKind03 / activateYn
	 * 
	 *	받는 정보.
	 *  merchantId // companyName / // profileImageUrl / 
	 *  workPhoneNumber / zipCode01 / address01/ address02 / businessArea01 // latitude // longitude
	 * 
	 *  터치하면 가맹점 상세정보로 가야하기 때문에 키도 필요하다..  merchantId 같은거..
	 *  
	 */
	public void getMemberStoreList() throws JSONException, IOException {
		if(CheckNetwork()){
			Log.i(TAG, "getMemberStoreList");
			controllerName = "checkMileageMerchantController";
			methodName = "selectSearchMerchantList";
			indexDataFirst = 0; // 화면 첫 값 인덱스. 초기화.. 다시 0부터
			indexDataLast = 0;	// 화면에 보여지는 끝값 인덱스. 함께 초기화.. 0부터
			entriesFn.clear();		// 이것도 초기화 해보자. 화면에 보여지는 데이터 리스트.
			newSearch = true;			// 새로운 검색임. true 라면 기존 데이터는 지워야함.
//			mIsLast = false;		// 초기화. 끝이 아니므로 추가 가능.
			// 로딩중입니다..  
			new Thread(	
					new Runnable(){
						public void run(){
							Message message = handler.obtainMessage();
							Bundle b = new Bundle();
							b.putInt("order", 1);
							message.setData(b);
							handler.sendMessage(message);
						}
					}
			).start();
			
			if(isRunning==0){		// 진행중에 다른 조작 사절
				isRunning=1;
//				searchSpinnerArea.setEnabled(false);
				searchSpinnerType.setEnabled(false);
				searchText.setEnabled(false); 
				searchBtn.setEnabled(false);
				// 서버 통신부
				new Thread(
						new Runnable(){
							public void run(){
								JSONObject obj = new JSONObject();
								try{
									obj.put("activateYn", "Y");
//									obj.put("businessArea01", searchWordArea);		// 지역		  
									obj.put("businessKind03", searchWordType);		// 업종					// 고유 번호 얻으려면, 내 아이디도 필요...
									obj.put("checkMileageId", myQRcode);			// 내 아이디
									obj.put("companyName", searchText.getText());			// 내 아이디
									
//									Log.w(TAG,"myQRcode::"+myQRcode+",searchWordArea:"+searchWordArea+",searchWordType:"+searchWordType+",companyName:"+searchText.getText());
									Log.w(TAG,"myQRcode::"+myQRcode+",searchWordType:"+searchWordType+",companyName:"+searchText.getText());
								}catch(Exception e){
									e.printStackTrace();
								}
								String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
								try{
									URL postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
									HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
									connection2.setDoOutput(true);
									connection2.setInstanceFollowRedirects(false);
									connection2.setRequestMethod("POST");
									Thread.sleep(200);
									connection2.setRequestProperty("Content-Type", "application/json");
									Thread.sleep(200);
									OutputStream os2 = connection2.getOutputStream();
									os2.write(jsonString.getBytes());
									os2.flush();
	//								System.out.println("postUrl      : " + postUrl2);
									System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : 정상
									responseCode = connection2.getResponseCode();
									if(responseCode==200||responseCode==204){
										InputStream in =  connection2.getInputStream();
										// 조회한 결과를 처리.
										theData1(in);
									}else{
										// 결과가 에러면 로딩바 없애고 다시 할수 있도록
										new Thread(
												new Runnable(){
													public void run(){
														Message message = handler.obtainMessage();
														Bundle b = new Bundle();
														b.putInt("order", 2);
														message.setData(b);
														handler.sendMessage(message);
													}
												}
										).start();
										isRunning = 0;
									}
								}catch(Exception e){ 
									e.printStackTrace();
									// 실행중 에러나면 로딩바 없애고 다시 할수 있도록
									new Thread(
											new Runnable(){
												public void run(){
													Message message = handler.obtainMessage();
													Bundle b = new Bundle();
													b.putInt("order", 2);
													message.setData(b);
													handler.sendMessage(message);
												}
											}
									).start();
									isRunning = 0;
									if(reTry>0){
										try{
											Log.w(TAG,"failed, retry all again. remain retry : "+reTry);
											reTry = reTry -1;
											Thread.sleep(200);		// 재시도?
											getMemberStoreList();
										}catch(Exception e2){}
									}else{
										Log.w(TAG,"reTry failed. -- init reTry");
										try{
											reTry = 5;	
										}catch(Exception e1){
											e1.printStackTrace();
										}
										searchSpinnerType.setEnabled(true);
										searchText.setEnabled(true); 
										searchBtn.setEnabled(true);
										showMSG();
//										Toast.makeText(MemberStoreListPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
									}
									
								}
							}
						}
				).start();
			}else{
				Log.w(TAG,"already running..");
			}
		}
	}

	/*
	 * 가맹점 정보 1차 데이터 받음. entries 도메인에 저장. 이후 url 정보를 꺼내 이미지를 받아오는 함수를 호출한다.
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		reTry = 5;			
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"수신::"+builder.toString());
		String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다
		// // // // // // // 바로 바로 화면에 add 하고 터치시 값 가져다가 상세 정보 보도록....
		
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		indexDataTotal = max;
		Log.w(TAG,"indexDataTotal=max::"+max);
		try {
			if(max>0){
				entries1 = new ArrayList<CheckMileageMerchants>(max);
				for ( int i = 0; i < max; i++ ){
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchant");		// 대소문자 주의
					/*
					 *
					 * 가맹점 ID, 가맹점 이름, 가맹점 URL(이미지 보여주기 용도)
					 *
					private String idCheckMileageMileages;					// 고유 식별 번호.!!	
					private String mileage;											
					private String activateYN;
					private String modifyDate;
					private String registerDate;
					private String checkMileageMembersCheckMileageID;		
					private String checkMileageMerchantsMerchantID;			merchantId
					private String merchantName;							companyName
					private String merchantImg;								profileImageUrl
					private Bitmap merchantImage;
					 *
					 *  workPhoneNumber  zipCode01  address01  address02
					 *   latitude  longitude								다음페이지로 갈때 따로 조회 안하도록 가지고 있으면?..어차피 조회 해야 하는듯.
					 *
					 * 가맹점 정보로 갈때 필요한 것들.
					 * intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		merchantId
					 *	intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());							상세 내역보기 위해 필요.. - 조회 필요.. 수정? 내아디
					 *	intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());														가맹점에 대한 내 마일리지 - 조회 필요.. 수정할것. 
					 *
					 */
					//  merchantId,  companyName,  profileImageUrl,  
					// 객체 만들고 값 받은거 넣어서 저장..  저장값:  가맹점아이디. 가맹점 이름, 프로필 URL
					entries1.add(
							new CheckMileageMerchants(
									jsonObj.getString("merchantId"),
									jsonObj.getString("companyName"),
									jsonObj.getString("profileThumbnailImageUrl"),		//profileImageUrl--> profileThumbnailImageUrl
									jsonObj.getString("idCheckMileageMileages"),
									jsonObj.getString("mileage")
							)
					);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}finally{
			new backgroundGetMerchantInfo().execute();	// getMerchantInfo(entries1); 를 비동기로 실행
		}
	}

	
	// 가맹점 URL로 이미지 가져오기.가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다. + 결과물에 더하기			-- 2차 검색
	public void getMerchantInfo(){
		try{
			Log.i(TAG, "merchantInfoGet   indexDataLast:"+indexDataLast+",indexDataTotal:"+indexDataTotal);
			// 마지막 인덱스+10개가 전체 개수보다 커지면 전체 개수 까지만.
			if(indexDataLast+10>=indexDataTotal){
				indexDataLast = indexDataTotal;
//				mIsLast = true;
				Log.d(TAG,"indexDataLast:"+indexDataLast+",indexDataTotal:"+indexDataTotal );
			}else{		// 전체 개수보다 작다면 10개. 추가 가능.
				indexDataLast = indexDataLast + 10;
			}
			Log.i(TAG,"indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast+"//indexDataTotal::"+indexDataTotal);
			for(int i=indexDataFirst; i<indexDataLast; i++){
				try{
					String a= new String(entries1.get(i).getMerchantId()+"");
					String b= new String(entries1.get(i).getCompanyName()+"");
					String c= new String(entries1.get(i).getProfileImageURL()+"");
					String d= new String(entries1.get(i).getIdCheckMileageMileages()+"");
					String e= new String(entries1.get(i).getMileage()+"");
					CheckMileageMerchants tempMerch = new CheckMileageMerchants(a, b, c, d, e);
					
					if(tempMerch.getProfileImageURL()!=null && tempMerch.getProfileImageURL().length()>0){
						if(tempMerch.getProfileImageURL().contains("http")){
							try{
								bm = LoadImage(tempMerch.getProfileImageURL());	
							}catch(Exception e2){
								Log.w(TAG,"LoadImage failed();"+tempMerch.getProfileImageURL());
								try{
									Thread.sleep(300);
									bm = LoadImage(tempMerch.getProfileImageURL());		
								}catch(Exception e3){
									Log.w(TAG,"LoadImage failed again();"+tempMerch.getProfileImageURL());
									BitmapDrawable dw = (BitmapDrawable) this.getResources().getDrawable(R.drawable.empty_140_140);
									bm = dw.getBitmap();
								}
							}
						}else{
							try{
								bm = LoadImage(imgthumbDomain+tempMerch.getProfileImageURL());		
								
							}catch(Exception e3){
//								e3.printStackTrace();
								Log.w(TAG, imgthumbDomain+tempMerch.getProfileImageURL()+" -- fail");
								try{
									BitmapDrawable dw = (BitmapDrawable) this.getResources().getDrawable(R.drawable.empty_140_140);
									bm = dw.getBitmap();
								}catch(Exception e4){}
							}
						}
					}else{
							BitmapDrawable dw = (BitmapDrawable) this.getResources().getDrawable(R.drawable.empty_140_140);
							bm = dw.getBitmap();
					}
					if(bm==null){
						BitmapDrawable dw = (BitmapDrawable) this.getResources().getDrawable(R.drawable.empty_140_140);
						bm = dw.getBitmap();
					}
//					tempMerch.setMerchantImage(BitmapResizePrc(bm, (float)(fImgSize*0.4), (float)(fImgSize*0.4) ).getBitmap());
					tempMerch.setMerchantImage(bm);
					entriesFn.add(tempMerch);
				}catch(Exception e){
					e.printStackTrace();
					Log.w(TAG,"The I is .."+i);
					hidePb2();
				}
			}
			
			if(indexDataFirst+10>indexDataLast){	// 마지막까지 도달했다면 마지막번호.
				indexDataFirst = indexDataLast;
			}else{									// 마지막까지 도달하지 않았다면 +10
				indexDataFirst = indexDataFirst + 10;
			}
//			Log.d(TAG,"가맹점 정보 수신 완료. ");
			showInfo();
		}catch(Exception e){
			e.printStackTrace();
			hidePb2();
			adding = false;
		}
	}

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.
	public void showInfo(){
		Log.d(TAG, "showInfo");
		new Thread(
				new Runnable(){		// 러닝바 끝
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("order", 2);
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("showYN", 1);		// 보여주기.
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}

	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}
	private InputStream OpenHttpConnection(String $imagePath) {
		InputStream stream = null ;
		try {
			URL url = new URL( $imagePath ) ;
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection() ;
			urlConnection.setRequestMethod( "GET" ) ;
			urlConnection.connect() ;
			if( urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK ) {
				stream = urlConnection.getInputStream() ;
			}
		} catch (MalformedURLException e) {
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			Log.w(TAG,"IOException");
		}
		return stream ;
	}

	/*
	 * Bitmap 이미지 리사이즈
	 * Src : 원본 Bitmap
	 * newHeight : 새로운 높이
	 * newWidth : 새로운 넓이
	 * 참고 소스 : http://skyswim42.egloos.com/3477279 ( webview 에서 capture 화면 resizing 하는 source 도 있음 )
	 */
//	private BitmapDrawable BitmapResizePrc( Bitmap Src, float newHeight, float newWidth)
//	{
////		Log.e(TAG,"BitmapResizePrc");
//
//		BitmapDrawable Result = null;
//		int width = Src.getWidth();
//		int height = Src.getHeight();
//
//		// calculate the scale - in this case = 0.4f
//		float scaleWidth = ((float) newWidth) / width;
//		float scaleHeight = ((float) newHeight) / height;
//
//		// createa matrix for the manipulation
//		Matrix matrix = new Matrix();
//
//		// resize the bit map
//		matrix.postScale(scaleWidth, scaleHeight);
//
//		// rotate the Bitmap 회전 시키려면 주석 해제!
//		//matrix.postRotate(45);
//
//		// recreate the new Bitmap
//		Bitmap resizedBitmap = Bitmap.createBitmap(Src, 0, 0, width, height, matrix, true);
//
//		// check
//		width = resizedBitmap.getWidth();
//		height = resizedBitmap.getHeight();
////		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );
//
//		// make a Drawable from Bitmap to allow to set the BitMap
//		// to the ImageView, ImageButton or what ever
//		Result = new BitmapDrawable(resizedBitmap);
//		
//		return Result;
//	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		
		// 가맹점 업종 목록 가져오기.
		if((!jobKindSearched) && (isRunning==0)){				// 업종 검색이 완료되지 않았고, 실행중인 작업이 없을 경우.
			isRunning = 1;		// 연속 실행 방지 (다른 실행 거부)
			getBusinessKindList();
		}
	}
	
	/*
	 *  닫기 버튼 2번 누르면 종료 됨.
	 *  (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
//			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MemberStoreListPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
		}
	}

	
	/*
	 * 스피너. 다른 아이템 선택시, 또는 기존 아이템 선택시에 대한 이벤트. 
	 * 다른거 선택하면 서버 통신하여 조회해온다. 변화 없을시 변화 없음.
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
//		if(dontTwice>0){				// ( dont 2
//			Log.i(TAG,"dontTwice");		
//			dontTwice = dontTwice - 1;
//		}else{
			gridView  = (GridView)findViewById(R.id.gridview);
			gridView.setEnabled(false);					// 그리드 뷰 허용 안함. 검색 도중 이전 검색 리스트를 스크롤하면 어플 강제 종료됨. -- 인덱스 문제 때문.
			Log.i(TAG,"searchSpinnerJobs//"+jobs[arg2]);
			
			// 0번째 모든 업종 --> 제거.
//			if(arg2==0){
//				searchWordType = "";
//			}else{
//				searchWordType = jobs[arg2];
//			}
			
			searchWordType = jobs[arg2];
//			Log.e(TAG,arg0+"//"+arg1+"//"+arg2+"//"+arg3);	
			// areas jobs
//			if(searchSpinnerArea==arg0){		// 지역 변경한 경우 .	// 정상 동작. arg2 는 몇번째 거인지.. areas[arg2]
//				Log.i(TAG,"searchSpinnerArea//"+areas[arg2]);	
//				if(arg2==0){
//					searchWordArea = "";  
//				}else{
//					searchWordArea = areas[arg2];
//				}
//			}else{								// 업종 변경한 경우 .	// 정상 동작.	areas[jobs]		// 0 은 전체니까 비워서 검색, 0이 아닐 경우 해당 값으로 검색.
//				Log.i(TAG,"searchSpinnerJobs//"+jobs[arg2]);
//				if(arg2==0){
//					searchWordType = "";
//				}else{
//					searchWordType = jobs[arg2];
//				}
//			}
			try {
				getMemberStoreList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}		// dont 2 )
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub			// 안바꾸면 마는거지
	}
	
	public class backgroundGetMerchantInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
			//			setListAdapter(new MyCustomAdapter(AndroidList.this, R.layout.row, month));  
			//			Toast.makeText(AndroidList.this,    "onPostExecute \n: setListAdapter after bitmap preloaded",    Toast.LENGTH_LONG).show(); 
		} 
		@Override protected void onPreExecute() {  
			//			Toast.makeText(AndroidList.this,    "onPreExecute \n: preload bitmap in AsyncTask",    Toast.LENGTH_LONG).show(); 
		} 
		@Override protected Void doInBackground(Void... params) {  
//			preLoadSrcBitmap();  
			Log.d(TAG,"backgroundGetMerchantInfo");
//			for(int i=0; i<entries1.size(); i++){
//				Log.e(TAG,"entries1.get("+i+").getProfileImageURL()"+entries1.get(i).getProfileImageURL());
//			}
			Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast+"/adding:"+adding);
			if(indexDataTotal==0){
				showInfo();
			}else{
				if(!((indexDataTotal<indexDataFirst)||(indexDataTotal<indexDataLast))){		// 하극상 아닌 경우
					if(!adding){
						adding = true;
						getMerchantInfo();
					}
				}else{
					indexDataLast = indexDataTotal;
					Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast);
				}
			}
			
			return null; 
		}
	}

	
	/*
	 * 네트워크 상태 감지
	 * 
	 */
	public Boolean CheckNetwork(){
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiAvailable = ni.isAvailable();
		boolean isWifiConn = ni.isConnected();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileAvail = ni.isAvailable();
		boolean isMobileConn = ni.isConnected();
		
		String status = "WiFi Avail="+isWifiAvailable+"//Conn="+isWifiConn
		+"//Mobile Avail="+isMobileAvail
		+"//Conn="+isMobileConn;
		if(!(isWifiConn||isMobileConn)){
			Log.w(TAG,status);
//			AlertShow("Wifi 혹은 3G 망이 연결되지 않았거나 원할하지 않습니다. 네트워크 확인 후 다시 접속해 주세요.");
			Log.d(TAG,"1");
			gridView.setEnabled(true);
			searchSpinnerType.setEnabled(true);
			searchText.setEnabled(true); 
			searchBtn.setEnabled(true);
			AlertShow_networkErr();
			Log.d(TAG,"2");
			// 상태 복원. 검색 가능하도록. 
			connected = false;
		}else{
			connected = true;
		}
		return connected;
	}
	public void AlertShow_networkErr(){
		Log.i(TAG,"AlertShow_networkErr");
		AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
		alert_internet_status.setTitle("Warning");
		alert_internet_status.setMessage(R.string.network_error);
		alert_internet_status.setPositiveButton(R.string.closebtn, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
//				finish();
			}
		});
		alert_internet_status.show();
	}
	
	@Override
	public void onPause(){
		super.onPause();
			searchText.setText("");
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//가상키보드 끄기
	}


	public void goSearch(){		// 단어 검색 ㄱㄱ
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//가상키보드 끄기
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setEnabled(false);
		try {
			indexDataTotal =0;
			getMemberStoreList();		
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		 switch(v.getId())  
		         {  
		         case R.id.store_search_text:  
		         {  
		             if(event.getAction() == KeyEvent.ACTION_DOWN)  		// 엔터시에도 검색 ㄱㄱ
		             {  
		            	 String searchTxt =  searchText.getText()+"";
//		            	 searchTxt = searchTxt.substring(0, searchTxt.length()-1);	// 엔터 잘라?
		            	 searchText.setText(searchTxt);
		            	 goSearch();
		            	 return true;
		             }  
		             break;  
		         }  
		         }  
		         return false;  
	}
	TextWatcher textWatcherInput = new TextWatcher() {  
		        @Override  
		        public void onTextChanged(CharSequence s, int start, int before, int count) {  
//		            Log.i("onTextChanged", s.toString());             
		        }  
		        @Override  
		        public void beforeTextChanged(CharSequence s, int start, int count,  
		                int after) {  
//		            Log.i("beforeTextChanged", s.toString());         
		        }  
		        @Override  
		        public void afterTextChanged(Editable s) {  
//		            Log.i("afterTextChanged", s.toString());  
		        }
		    };    

//	public void AlertShow(String msg){
//		AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
//		alert_internet_status.setTitle("Warning");
//		alert_internet_status.setMessage(msg);
//		alert_internet_status.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
////				finish();
//			}
//		});
//		alert_internet_status.show();
//	}
}
