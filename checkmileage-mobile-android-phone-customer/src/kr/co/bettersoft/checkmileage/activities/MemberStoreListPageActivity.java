package kr.co.bettersoft.checkmileage.activities;
/**
 * MemberStoreListPageActivity
 *  가맹점 목록. 
 *  
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.adapters.MemberStoreSearchListAdapter;
import kr.co.bettersoft.checkmileage.common.CheckMileageCustomerRest;
import kr.co.bettersoft.checkmileage.common.CommonConstant;
import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.domain.Locales;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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

	final int GET_BUSINESS_KIND_LIST = 401; 
	final int GET_MEMBER_STORE_LIST = 402; 
	final int GET_MERCHANT_INFO = 403; 
	final int UPDATE_LOG_TO_SERVER = 404; 

	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록	// 처음 두번 자동 실행  되는거.
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;

	// 설정 파일 저장소  - 사용자 전번 읽기 / 쓰기 용도	
	SharedPreferences sharedPrefCustom;

	// 서버 통신용
	CheckMileageCustomerRest checkMileageCustomerRest;
	String callResult = "";
	String tempstr = "";
	JSONObject jsonObject;
	// checkMileageCustomerRest = new CheckMileageCustomerRest();	// oncreate
	// 중복 실행 방지용
	int isUpdating = 0;
	int isRunning = 0;			// 연속 실행 방지. 실행 중에 다른 실행 요청이 들어올 경우, 무시한다.

	// 서버 통신용 파라미터
	String myQRcode = "";			// 내 아이디
	// 내 좌표 업뎃용				///////////////////////////////////////////////
	String myLat2;
	String myLon2;
	// 전번(업뎃용)
	String phoneNum = "";
	// qr
	String qrCode = "";
	// Locale
	Locale systemLocale = null ;
	String strCountry = "" ;
	String strLanguage = "" ;
	// 스피너
	String searchWordArea = "";		// 서버 조회시 지역명
	String searchWordType = "";		// 서버 조회시 업종명
	// ListView에 뿌릴 Data 를 위한 스피너 데이터들.
	String[] jobs = {"", ""};
	String[] tmpJobs = null;
	// 이미지
	float fImgSize = 0;			// 이미지 사이즈 저장변수.
	String imgthumbDomain = CommonConstant.imgthumbDomain; 					// Img 가져올때 파일명만 있을 경우 앞에 붙일 도메인. 

	// 화면 구성
	Spinner searchSpinnerType;		// 상단 업종 목록
	TextView searchText;			//검색어
	Button searchBtn;				//검색버튼
	View parentLayout;			// 키보드 자동 숨김용도
	GridView gridView;
	View emptyView;				// 데이터 없음 뷰
	// 진행바
	ProgressBar pb1;		// 중단 로딩 진행바
	ProgressBar pb2;		// 하단 추가 진행바

	// 유틸
	InputMethodManager imm;			// 키보드 제어용
	int indexDataFirst = 0;			// 부분 검색 위한 인덱스. 시작점
	int indexDataLast = 0;			// 부분 검색 위한 인덱스. 끝점
	int indexDataTotal = 0;			// 부분 검색 위한 인덱스. 전체 개수
	Boolean mIsLast = false;			// 끝까지 갔음. true 라면 더이상의 추가 없음. 새 조회시 false 로 초기화
	Boolean adding = false;			// 데이터 더하기 진행 중임.
	Boolean newSearch = false; 		// 새로운 조회인지 여부. 새로운 조회라면 기존 데이터는 지우고 새로 검색한 데이터만 사용. 새로운 조회가 아니라면 기존 데이터에 추가 데이터를 추가.
	Boolean jobKindSearched = false;
	Bitmap bm = null;

	// 아답터
	private MemberStoreSearchListAdapter imgAdapter;
	// 데이터 저장 배열
	public ArrayList<CheckMileageMerchants> entries1 = new ArrayList<CheckMileageMerchants>();	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)   // 저장용.
	ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>();			// 잘라서 더하는 부분.
	List<CheckMileageMerchants> entriesFn = new ArrayList<CheckMileageMerchants>();			// 최종 산출물


	//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
						if(gridView==null){
							gridView  = (GridView)findViewById(R.id.gridview);
						}
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
				if(b.getInt("enableOrDisable")==1){
					searchSpinnerType.setEnabled(true);
					searchText.setEnabled(true); 
					searchBtn.setEnabled(true);
				}else if(b.getInt("enableOrDisable")==2){
					searchSpinnerType.setEnabled(false);
					searchText.setEnabled(false); 
					searchBtn.setEnabled(false);
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
				if(b.getInt("showErrToast")==1){			// 일반 에러 토스트
					Toast.makeText(MemberStoreListPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showNetErrToast")==1){			// 네트워크 에러 토스트
					Toast.makeText(MemberStoreListPageActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("setJobsList")==1){			// 업종 목록 가져왔을때 스피너에 세팅
					jobs = tmpJobs;
					// 스피너 데이터 세팅. 
					ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(getThis(), android.R.layout.simple_spinner_item, jobs);
					aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					searchSpinnerType.setAdapter(aa2);
					jobKindSearched = true;			// 업종 검색 끝.
				}
				
				switch (msg.what)
				{
					case GET_BUSINESS_KIND_LIST   : runOnUiThread(new RunnableGetBusinessKindList());	
						break;
					case GET_MEMBER_STORE_LIST   : runOnUiThread(new RunnableGetMemberStoreList());	
						break;
					case GET_MERCHANT_INFO   : runOnUiThread(new RunnableGetMerchantInfo());	
						break;
					case UPDATE_LOG_TO_SERVER   : runOnUiThread(new RunnableUpdateLogToServer());	
						break;
					default : 
						break;
				}	

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};



	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);

		checkMileageCustomerRest = new CheckMileageCustomerRest();
		
		checkMileageCustomerRest = new CheckMileageCustomerRest();

		// prefs
		sharedPrefCustom = getSharedPreferences("MyCustomePref",
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

		// 내 QR 코드. 
		myQRcode = MyQRPageActivity.qrCode;		
		entriesFn = new ArrayList<CheckMileageMerchants>();

		parentLayout = findViewById(R.id.member_store_list_parent_layout);		// 부모 레이아웃- 리스너를 달아서 키보드 자동 숨김에 사용

		//		parentLayout2 = findViewById(R.id.member_store_list_parent_layout2);		// 부모 레이아웃- 리스너를 달아서 키보드 자동 숨김에 사용
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 	// 가상키보드 닫기위함


		// locale get
		systemLocale = getResources().getConfiguration(). locale;
		strCountry = systemLocale .getCountry();
		strLanguage = systemLocale .getLanguage();

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

		// spinner
		searchSpinnerType = (Spinner)findViewById(R.id.searchSpinnerType);
		// spinner listener
		searchSpinnerType.setOnItemSelectedListener(this);

		// 부모 레이아웃 리스너 - 외부 터치 시 키보드 숨김 용도
		parentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.w(TAG,"parentLayout click");
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm .hideSoftInputFromWindow(searchText.getWindowToken(), 0);
			}
		});
		searchBtn.setOnClickListener(new Button.OnClickListener()  {
			public void onClick(View v)  {
				goSearch();		 // 단어로 검색 ㄱㄱ 
			}
		});
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * setGriding
	 *   데이터를 화면에 세팅한다
	 *
	 * @param
	 * @param
	 * @return
	 */
	public void setGriding(){
		imgAdapter = new MemberStoreSearchListAdapter(this, entriesFn);
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(imgAdapter);

		// 클릭시 상세보기 페이지로
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MemberStoreListPageActivity.this, MemberStoreInfoPage.class);
				Log.i(TAG, "checkMileageMerchantsMerchantID::"+entriesFn.get(position).getMerchantId());
				Log.i(TAG, "myMileage::"+entriesFn.get(position).getMileage());
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getMerchantId());		// 가맹점 아이디
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// 고유 식별 번호. (상세보기 조회용도)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// 내 마일리지
				startActivity(intent);
			}
		});
		gridView.setOnScrollListener(listScrollListener);		// 리스너 등록. 스크롤시 하단에 도착하면 추가 데이터 조회하도록.
	}

	// 온 스크롤 이벤트. 
	/**
	 * listScrollListener
	 *   온 스크롤 이벤트이다. 리스트 끝으로 갔을때 추가 이미지가 있다면 로드한다.
	 */
	private OnScrollListener listScrollListener = new OnScrollListener(){
		// 건들기만 하면 주르륵 뜬다.  쓸수 있긴 한데 너무 막떠서  boolean 으로 조절한다.
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if((indexDataFirst + indexDataLast < indexDataTotal)||(indexDataLast!=indexDataTotal)){	// 아직 남음 또는 끝에 도달하지 않음.
				mIsLast = false;
			}
			if((totalItemCount<10) ||(indexDataLast==indexDataTotal)){		// 10개 이하(이미다 보여줌) 또는 마지막=전체 (끝에 도달)
				mIsLast = true;
			}
			// 리스트 가장 하단에 도달했을 경우.. 
			//			if(firstVisibleItem+visibleItemCount==totalItemCount &&(!adding)&&(!mIsLast)){			// 가장 하단.
			if(firstVisibleItem+visibleItemCount>=(totalItemCount-2) &&(!adding)&&(!mIsLast)){		// 가장 하단 -2일때 미리동작? - 좀더 나은듯.
				showPb2();
				indexDataTotal = entries1.size();
				Log.d(TAG,"onScroll indexDataTotal:"+indexDataTotal);
				handler.sendEmptyMessage(GET_MERCHANT_INFO);
			}
		}
		// 스크롤 시작, 끝, 스크롤 중.. 이라는 사실을 알수 있다.  스크롤중 조회시 에러가 발생하기 때문에 스크롤 중에는 조회가 되지 않도록 한다.. 
		/**
		 * onScrollStateChanged
		 *  스크롤중 조회시 에러가 발생하기 때문에 스크롤 중에는 조회가 되지 않도록 한다.. 
		 *
		 * @param view
		 * @param scrollState
		 * @return
		 */
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {			// status0 : stop  / status1 : touch / status2 : scrolling
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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/*
	 * 스피너. 다른 아이템 선택시, 또는 기존 아이템 선택시에 대한 이벤트. 
	 * 다른거 선택하면 서버 통신하여 조회해온다. 변화 없을시 변화 없음.
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	/**
	 * onItemSelected
	 *  스피너  다른거 선택하면 서버 통신하여 조회해온다
	 *
	 */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setEnabled(false);					// 그리드 뷰 허용 안함. 검색 도중 이전 검색 리스트를 스크롤하면 어플 강제 종료됨. -- 인덱스 문제 때문.
		Log.i(TAG,"searchSpinnerJobs//"+jobs[arg2]);

		searchWordType = jobs[arg2];

		handler.sendEmptyMessage(GET_MEMBER_STORE_LIST);
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// 스피너 안바꾸면 반응x
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 비동기

	/**
	 * 러너블. 검색을 위한 가맹점 업종리스트를 가져온다.
	 */
	class RunnableGetBusinessKindList implements Runnable {
		public void run(){
			new backgroundGetBusinessKindList().execute();
		}
	}
	/**
	 * backgroundGetBusinessKindList
	 *  비동기로 검색을 위한 가맹점 업종리스트를 가져온다.
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	public class backgroundGetBusinessKindList extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetBusinessKindList");

			// 파리미터 세팅
			Locales localesParam = new Locales();
			localesParam.setCountryCode(strCountry);
			localesParam.setLanguageCode(strLanguage);

			// 호출
			//				showPb();

			// 로딩중입니다..  
			new Thread(	
					new Runnable(){
						public void run(){
							Message message = handler.obtainMessage();
							Bundle b = new Bundle();
							b.putInt("order", 1);
							b.putInt("enableOrDisable", 2);
							message.setData(b);
							handler.sendMessage(message);
						}
					}
			).start();	

			callResult = checkMileageCustomerRest.RestGetBusinessKindList(localesParam);
			hidePb();
			// 결과 처리
			if(callResult.equals("S")){				
				processBusinessKindListData();
			}else{		
				showMSG();
				showInfo();		// 핸들러에서 함께 처리
			}

			return null; 
		}
	}


	/**
	 * 러너블.  가맹점 목록을 가져온다
	 */
	class RunnableGetMemberStoreList implements Runnable {
		public void run(){
			new backgroundGetMemberStoreList().execute();
		}
	}
	/**
	 * backgroundGetMemberStoreList
	 *  비동기로 가맹점 목록을 가져온다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	public class backgroundGetMemberStoreList extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetMemberStoreList");

			// 파리미터 세팅
			CheckMileageMerchants checkMileageMerchantsParam = new CheckMileageMerchants();
			checkMileageMerchantsParam.setCompanyName(searchText.getText()+"");			// 가맹점 이름
			checkMileageMerchantsParam.setCheckMileageId(myQRcode);	// 내 아이디
			checkMileageMerchantsParam.setBusinessKind03(searchWordType);		// 업종	

			// 호출
			// 로딩중입니다..  
			if(isRunning==0){		// 진행중에 다른 조작 사절
				isRunning=1;
				new Thread(	
						new Runnable(){
							public void run(){
								Message message = handler.obtainMessage();
								Bundle b = new Bundle();
								b.putInt("order", 1);
								b.putInt("enableOrDisable", 2);
								message.setData(b);
								handler.sendMessage(message);
							}
						}
				).start();
				indexDataFirst = 0; // 화면 첫 값 인덱스. 초기화.. 다시 0부터
				indexDataLast = 0;	// 화면에 보여지는 끝값 인덱스. 함께 초기화.. 0부터
				entriesFn.clear();		// 이것도 초기화 해보자. 화면에 보여지는 데이터 리스트.
				newSearch = true;			// 새로운 검색임. true 라면 기존 데이터는 지워야함.
				callResult = checkMileageCustomerRest.RestGetMemberStoreList(checkMileageMerchantsParam);
				// 결과 처리
				if(callResult.equals("S")){		
					processStoreListData();
				}else{	// 실패	
					new Thread(
							new Runnable(){
								public void run(){
									Message message = handler.obtainMessage();
									Bundle b = new Bundle();
									b.putInt("order", 2);
									b.putInt("enableOrDisable", 1);
									message.setData(b);
									handler.sendMessage(message);
								}
							}
					).start();
					isRunning = 0;
					showMSG();
				}
			}else{
				Log.w(TAG,"already running..");
			}
			return null; 
		}
	}


	/**
	 * 러너블.   가맹점 정보 얻어오는함수 호출
	 */
	class RunnableGetMerchantInfo implements Runnable {
		public void run(){
			new backgroundGetMerchantInfo().execute();
		}
	}
	/**
	 * backgroundGetMerchantInfo
	 *  비동기로 가맹점 정보 얻어오는함수 호출
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	public class backgroundGetMerchantInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetMerchantInfo");
			Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast+"/adding:"+adding);
			if(indexDataTotal==0){				// 전체 개수가 0일 경우..  보여줌 -> "없습니다"
				showInfo();
			}else{								// 개수가 0이 아닐때.
				if(!((indexDataTotal<indexDataFirst)||(indexDataTotal<indexDataLast))){		// 정상적인 경우
					if(!adding){
						adding = true;
						getMerchantInfo();						// 이미지 데이터를 가져옴
					}
				}else{
					indexDataLast = indexDataTotal;				// 비정상적인 경우. 마지막 데이터가 최대값을 넘겼을때.
					Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast);
				}
			}
			return null; 
		}
	}
	// 가맹점 URL로 이미지 가져오기.가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다. + 결과물에 더하기			-- 2차 검색
	/**
	 * getMerchantInfo
	 *  가맹점 URL로 이미지 가져오기.가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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


	/**
	 * goSearch
	 *  단어명으로 가맹점 검색을 실시하도록 함수를 호출한다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	public void goSearch(){		// 단어 검색 ㄱㄱ
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//가상키보드 끄기
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setEnabled(false);
		indexDataTotal =0;
		handler.sendEmptyMessage(GET_MEMBER_STORE_LIST);
	}


	
	/**
	 * 러너블.사용자의 위치 정보 및 정보 로깅
	 */
	class RunnableUpdateLogToServer implements Runnable {
		public void run(){
			new backgroundUpdateLogToServer().execute();
		}
	}
	/**
	 * 비동기로 사용자의 위치 정보 및 정보 로깅
	 * backgroundUpdateLogToServer
	 */
	public class backgroundUpdateLogToServer extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundUpdateLogToServer");

			if(isUpdating==0){
				isUpdating = 1;

				phoneNum = sharedPrefCustom.getString("phoneNum", "");	
				myLat2 = sharedPrefCustom.getString("myLat2", "");	
				myLon2 = sharedPrefCustom.getString("myLon2", "");	
				qrCode = sharedPrefCustom.getString("qrCode", "");	

				// 파라미터 세팅
				CheckMileageLogs checkMileageLogsParam = new CheckMileageLogs();
				checkMileageLogsParam.setCheckMileageId(qrCode);			// 사용자 아이디(qr코드)
				checkMileageLogsParam.setViewName("CheckMileageCustomerSearchMerchantView");	// 뷰 이름
				checkMileageLogsParam.setParameter01(phoneNum);			// 전번
				if((searchText.getText()+"").length()>0){				// parameter04		검색일 경우 검색어.
					checkMileageLogsParam.setParameter04(searchText.getText()+"");
				}else{
					checkMileageLogsParam.setParameter04("");
				}
				// 서버 호출
				callResult = checkMileageCustomerRest.RestUpdateLogToServer(checkMileageLogsParam);
				// 결과 처리
				if(callResult.equals("S")){				 
					Log.d(TAG,"updateLogToServer S");
				}else{														 
					Log.d(TAG,"updateLogToServer F");
				}
				isUpdating = 0;
				// 가맹점 업종 목록 가져오기.
				if((!jobKindSearched) && (isRunning==0)){				// 업종 검색이 완료되지 않았고, 실행중인 작업이 없을 경우.
					isRunning = 1;		// 연속 실행 방지 (다른 실행 거부)
					handler.sendEmptyMessage(GET_BUSINESS_KIND_LIST);
				}
			}else{
				Log.w(TAG,"already updating..");
			}
			return null; 
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 서버 데이터 처리

	/**
	 * 서버로부터 받은 가맹점 종류 리스트를 파싱하여 화면에 적용한다.
	 */
	public void processBusinessKindListData(){
		tempstr= checkMileageCustomerRest.getTempstr();
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		Log.d(TAG,"max:"+max);				// 0 번째 모든 업종 - 제거됨.
		try {
			tmpJobs = new String[max];		// 0번째 제거로 max+1 --> max
			//			tmpJobs[0] = "모든 업종";			// 나중에 바꿔야 하는데.. 다국어로.		--> 0번째 모든 업종 항목 제거
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
							Message message = handler.obtainMessage();					// 업종 목록 받아온것 스피너에 세팅..
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

	/**
	 * 서버로부터 받은 가맹점 목록 데이터를 파싱하여 화면에 적용한다.
	 */
	public void processStoreListData(){
		////////////////
		tempstr = checkMileageCustomerRest.getTempstr();
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
					// 객체 만들고 값 받은거 넣어서 저장..  저장값:  가맹점아이디. 가맹점 이름, 프로필 URL
					String tempMerchantId="";
					String tempCompanyName="";
					String tempProfileThumbnailImageUrl="";
					String tempIdCheckMileageMileages="";
					String tempMileage="";
					try{
						tempMerchantId = jsonObj.getString("merchantId");
					}catch(Exception e1){ tempMerchantId = ""; }
					try{
						tempCompanyName = jsonObj.getString("companyName");
					}catch(Exception e1){ tempCompanyName = ""; }
					try{
						tempProfileThumbnailImageUrl = jsonObj.getString("profileThumbnailImageUrl");
					}catch(Exception e1){ tempProfileThumbnailImageUrl = ""; }
					try{
						tempIdCheckMileageMileages = jsonObj.getString("idCheckMileageMileages");
					}catch(Exception e1){ tempIdCheckMileageMileages = ""; }
					try{
						tempMileage = jsonObj.getString("mileage");
					}catch(Exception e1){ tempMileage = ""; }

					entries1.add(
							new CheckMileageMerchants(
									tempMerchantId,
									tempCompanyName,
									tempProfileThumbnailImageUrl,		//profileImageUrl--> profileThumbnailImageUrl
									tempIdCheckMileageMileages,
									tempMileage
							)
					);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		handler.sendEmptyMessage(GET_MERCHANT_INFO);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 기타


	// 가맹점 이미지 URL 에서 이미지 받아와서 도메인에 저장하는 부분.
	/**
	 * LoadImage
	 *  가맹점 이미지 URL 에서 이미지 받아온 스트림을 비트맵으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return bm
	 */
	private Bitmap LoadImage( String $imagePath ) {
		InputStream inputStream = OpenHttpConnection( $imagePath ) ;
		Bitmap bm = BitmapFactory.decodeStream( inputStream ) ;
		return bm;
	}
	/**
	 * OpenHttpConnection
	 *  가맹점 이미지 URL 에서 이미지 받아와서 스트림으로 저장한다
	 *
	 * @param $imagePath
	 * @param
	 * @return stream
	 */
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

	/**
	 * onEditorAction
	 *  검색창에서 엔터를 눌러도 검색되도록 한다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
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


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// utils


	/**
	 * showInfo
	 *  entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.
	 *
	 * @param
	 * @param
	 * @return
	 */
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
		handler.sendEmptyMessage(UPDATE_LOG_TO_SERVER);
		
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


	public Context getThis(){
		return this;
	}


	// 중단 프로그래스바 보임, 숨김
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
	// 하단 프로그래스바 보임, 숨김
	/**
	 * showPb2
	 *  하단 프로그래스바 가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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
	/**
	 * hidePb2
	 *  하단 프로그래스바 비가시화한다
	 *
	 * @param
	 * @param
	 * @return
	 */
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
	/**
	 * showMSG
	 *  화면에 error 토스트 띄운다
	 *
	 * @param
	 * @param
	 * @return
	 */
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





	/**
	 * onResume
	 *  리쥼시마다 가맹점 목록을 갱신하도록 한다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
		if(isUpdating==0){
			new backgroundUpdateLogToServer().execute();	// 비동기로 전환	
		}
	}

	/**
	 * onBackPressed
	 *  닫기 버튼 2번 누르면 종료한다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.d(TAG,"kill all");
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MemberStoreListPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
			new Thread( 
					new Runnable(){
						public void run(){
							try {
								Thread.sleep(3000);		// 3초후 복원. 다시 뒤로 가기 눌렀을때 종료 여부 확인.
								app_end = 0;
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
			).start();
		}
	}



	/**
	 * onPause
	 *  pause 에 화면 초기화하고 가상 키보드를 숨긴다
	 *
	 * @param 
	 * @param
	 * @return 
	 */
	@Override
	public void onPause(){
		super.onPause();
		searchText.setText("");
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//가상키보드 끄기
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
