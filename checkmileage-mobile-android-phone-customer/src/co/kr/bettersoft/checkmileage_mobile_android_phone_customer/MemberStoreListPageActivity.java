package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.kr.bettersoft.domain.CheckMileageMerchants;
import com.kr.bettersoft.domain.CheckMileageMileage;
import com.kr.bettersoft.domain.MemberStoreListViewWrapper;
import com.pref.DummyActivity;
import com.utils.adapters.ImageAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.os.AsyncTask;

import android.app.ListActivity;

public class MemberStoreListPageActivity extends Activity implements OnItemSelectedListener {
	
	String TAG = "MemberStoreListPageActivity";
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록	// 처음 두번 자동 실행  되는거.
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int dontTwice = 1;				// 스피너 리스너로 인한 초기 2회 조회 방지. 
	
	String myQRcode = "";			// 내 아이디
	
	int responseCode = 0;			// 서버 조회 결과 코드
	String controllerName = "";		// 서버 조회시 컨트롤러 이름
	String methodName = "";			// 서버 조회시 메서드 이름
	String searchWordArea = "";		// 서버 조회시 지역명
	String searchWordType = "";		// 서버 조회시 업종명
	
	
	
	
	
	Spinner searchSpinnerArea;		// 상단 지역 목록
	Spinner searchSpinnerType;		// 상단 업종 목록
	
	int indexDataFirst = 0;			// 부분 검색 위한 인덱스. 시작점
	int indexDataLast = 0;			// 부분 검색 위한 인덱스. 끝점
	int indexDataTotal = 0;			// 부분 검색 위한 인덱스. 전체 개수
	
	Boolean mIsLast = false;			// 끝까지 갔음. true 라면 더이상의 추가 없음. 새 조회시 false 로 초기화
	Boolean adding = false;			// 데이터 더하기 진행 중임.
	Boolean newSearch = false; 		// 새로운 조회인지 여부. 새로운 조회라면 기존 데이터는 지우고 새로 검색한 데이터만 사용. 새로운 조회가 아니라면 기존 데이터에 추가 데이터를 추가.
	
	private ImageAdapter imgAdapter;
	
	public ArrayList<CheckMileageMerchants> entries1 = new ArrayList<CheckMileageMerchants>();	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)   // 저장용.
	ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>();			// 잘라서 더하는 부분.
	List<CheckMileageMerchants> entriesFn = new ArrayList<CheckMileageMerchants>();			// 최종 산출물
	
	
	float fImgSize = 0;			// 이미지 사이즈 저장변수.
	int isRunning = 0;			// 연속 실행 방지. 실행 중에 다른 실행 요청이 들어올 경우, 무시한다.
	View emptyView;				// 데이터 없음 뷰

	// 진행바
	ProgressBar pb1;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// 받아온 마일리지 결과를 화면에 뿌려준다.
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 여기 리스트 레이아웃.
					if(entriesFn.size()>0){
						Log.e(TAG,"indexDataFirst::"+indexDataFirst);
						if(newSearch){		// 새로운 검색일 경우 새로 설정, 추가일 경우 알림만 하기 위함.
							setGriding();
							newSearch = false;		// 다시 돌려놓는다. 이제는 최초 검색이 아님.
						}else{
							Log.e(TAG,"notifyDataSetChanged");
							Log.e(TAG,"size:"+entriesFn.size());
							imgAdapter.notifyDataSetChanged();		// 알림 -> 변경사항이 화면상에 업데이트 되도록함.
						}
						adding = false;		// 추가 끝났음. 다른거 조회시 또 추가 가능.. (스크롤 리스너를 다룰때 사용)
					}else{
						Log.e(TAG,"no data");
						emptyView = findViewById(R.id.empty1);		// 데이터 없으면 '빈 페이지'(데이터 없음 메시지)표시
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = 0;		// 진행중이지 않음. - 이후 추가 조작으로 새 조회 가능.
					searchSpinnerArea.setEnabled(true);
					searchSpinnerType.setEnabled(true);
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	

	
	// ListView에 뿌릴 Data 를 위한 스피너 데이터들. --> 나중에 서버 통신하여 처음에 가져와서 만들어 지도록 한다.
	String[] areas = {"전지역", "홍대", "신촌", "영등포", "신림", "강남", "종로", "건대", "노원", "대학로", "여의도"};			// 나중에 조회 해 올 것..
	String[] jobs = {"모든 업종", "PX", "매점" , "식당"};
	
	GridView gridView;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);
		
		// 내 QR 코드. 
		myQRcode = MyQRPageActivity.qrCode;		
		
		entriesFn = new ArrayList<CheckMileageMerchants>();
		
		// 크기 측정
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
		if(screenWidth < screenHeight ){fImgSize = screenWidth;
	    }else{fImgSize = screenHeight;}
		
		URL imageURL = null;							
		URLConnection conn = null;
		InputStream is= null;
		
		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
		
		// spinner
		searchSpinnerArea = (Spinner)findViewById(R.id.searchSpinnerArea);
		searchSpinnerType = (Spinner)findViewById(R.id.searchSpinnerType);
		// spinner listener
		searchSpinnerArea.setOnItemSelectedListener(this);
		searchSpinnerType.setOnItemSelectedListener(this);
		// 데이터 세팅. 
		ArrayAdapter<String> aa1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, areas);
		aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		searchSpinnerArea.setAdapter(aa1);
		 ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobs);
		 aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 searchSpinnerType.setAdapter(aa2);
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
		   // TODO Auto-generated method stub
//			  if(indexDataFirst==indexDataLast){		// 시작이 더 크면 문제 있는거
//				  mIsLast = true;
//			  }
			  
			  // 리스트 가장 하단에 도달했을 경우.
			  if(firstVisibleItem+visibleItemCount==totalItemCount &&(!adding)&&(!mIsLast)){
				  Log.e(TAG, "onScroll event Occured."+"//view::"+view+"//firstVisibleItem::"+firstVisibleItem+"//visibleItemCount:"+visibleItemCount+"//totalItemCount::"+totalItemCount);
				  adding = true;
				  new backgroundGetMerchantInfo().execute();		// 비동기 실행
			  }
		  }
		  // 스크롤 시작, 끝, 스크롤 중.. 이라는 사실을 알수 있다. 사실 필요 없음.. 
		  @Override
		  public void onScrollStateChanged(AbsListView view, int scrollState) {}
		 };
     
	
	/*
	 * 서버와 통신하여   가맹점 목록을 가져온다. 새로 조회.
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
		Log.i(TAG, "getMemberStoreList");
		controllerName = "checkMileageMerchantController";
		methodName = "selectSearchMerchantList";
		indexDataFirst = 0; // 화면 첫 값 인덱스. 초기화.. 다시 0부터
		indexDataLast = 0;	// 화면에 보여지는 끝값 인덱스. 함께 초기화.. 0부터
		entriesFn.clear();		// 이것도 초기화 해보자. 화면에 보여지는 데이터 리스트.
		newSearch = true;			// 새로운 검색임. true 라면 기존 데이터는 지워야함.
		mIsLast = false;		// 초기화. 끝이 아니므로 추가 가능.
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
			searchSpinnerArea.setEnabled(false);
			searchSpinnerType.setEnabled(false);
			// 서버 통신부
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								obj.put("activateYn", "Y");
								obj.put("businessArea01", searchWordArea);		// 지역		  
								obj.put("businessKind03", searchWordType);		// 업종					// 고유 번호 얻으려면, 내 아이디도 필요...
								obj.put("checkMileageId", myQRcode);			// 내 아이디
								Log.e(TAG,"myQRcode::"+myQRcode);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
							try{
								URL postUrl2 = new URL("http://checkmileage.onemobileservice.com/"+controllerName+"/"+methodName);
								HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes());
								os2.flush();
								System.out.println("postUrl      : " + postUrl2);
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
								
							}
						}
					}
			).start();
		}else{
			Log.e(TAG,"이미 실행중입니다.");
		}
		
		
		
		
	}

	/*
	 * 가맹점 정보 1차 데이터 받음. entries 도메인에 저장. 이후 url 정보를 꺼내 이미지를 받아오는 함수를 호출한다.
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		String line =null;
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"수신::"+builder.toString());
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
//		Log.e(TAG,"max::"+max);
		try {
			entries1 = new ArrayList<CheckMileageMerchants>(max);
			if(max>0){
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
									jsonObj.getString("profileImageUrl"),
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

	
	// 가맹점 URL로 이미지 가져오기.가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다. + 결과물에 더하기
	public void getMerchantInfo(){
		Log.i(TAG, "merchantInfoGet");
		// 마지막 인덱스+10개가 전체 개수보다 커지면 전체 개수 까지만.
		if(indexDataLast+10>=indexDataTotal){
			indexDataLast = indexDataTotal;
			mIsLast = true;
		}else{		// 전체 개수보다 작다면 10개. 추가 가능.
			indexDataLast = indexDataLast + 10;
		}
		Log.i(TAG,"indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast+"//indexDataTotal::"+indexDataTotal);
		Log.e(TAG,"indexDataTotal::"+indexDataTotal);
		
		for(int i=indexDataFirst; i<indexDataLast; i++){
			Log.i(TAG,"I::"+i);
			String a= new String(entries1.get(i).getMerchantId()+"");
			String b= new String(entries1.get(i).getCompanyName()+"");
			String c= new String(entries1.get(i).getProfileImageURL()+"");
			if(c.length()<1){
				c="http://www.carsingh.com/img/noImage.jpg";	
			}
			String d= new String(entries1.get(i).getIdCheckMileageMileages()+"");
			String e= new String(entries1.get(i).getMileage()+"");
			CheckMileageMerchants tempMerch = new CheckMileageMerchants(a, b, c, d, e);
			Bitmap bm = null;
			bm = LoadImage(tempMerch.getProfileImageURL());
			tempMerch.setMerchantImage(BitmapResizePrc(bm, (float)(fImgSize*0.3), (float)(fImgSize*0.4) ).getBitmap());
			entriesFn.add(tempMerch);
		}
		
		if(indexDataFirst+10>indexDataLast){	// 마지막까지 도달했다면 마지막번호.
			indexDataFirst = indexDataLast;
		}else{									// 마지막까지 도달하지 않았다면 +10
			indexDataFirst = indexDataFirst + 10;
		}
		Log.d(TAG,"가맹점 정보 수신 완료. ");
		showInfo();
	}

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.
	public void showInfo(){
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
	private BitmapDrawable BitmapResizePrc( Bitmap Src, float newHeight, float newWidth)
	{
//		Log.e(TAG,"BitmapResizePrc");

		BitmapDrawable Result = null;
		int width = Src.getWidth();
		int height = Src.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// rotate the Bitmap 회전 시키려면 주석 해제!
		//matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(Src, 0, 0, width, height, matrix, true);

		// check
		width = resizedBitmap.getWidth();
		height = resizedBitmap.getHeight();
//		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );

		// make a Drawable from Bitmap to allow to set the BitMap
		// to the ImageView, ImageButton or what ever
		Result = new BitmapDrawable(resizedBitmap);
		
		return Result;
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		app_end = 0;
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
			Log.e(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// 더미도 종료
			DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MemberStoreListPageActivity.this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
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
		if(dontTwice>0){
			Log.e(TAG,"dontTwice");		
			dontTwice = dontTwice - 1;
		}else{
			// TODO Auto-generated method stub
//			Log.e(TAG,arg0+"//"+arg1+"//"+arg2+"//"+arg3);						// areas jobs
			if(searchSpinnerArea==arg0){		// 지역 변경한 경우 .	// 정상 동작. arg2 는 몇번째 거인지.. areas[arg2]
				Log.e(TAG,"searchSpinnerArea//"+areas[arg2]);	
				if(arg2==0){
					searchWordArea = "";  
				}else{
					searchWordArea = areas[arg2];
				}
			}else{								// 업종 변경한 경우 .	// 정상 동작.	areas[jobs]		// 0 은 전체니까 비워서 검색, 0이 아닐 경우 해당 값으로 검색.
				Log.e(TAG,"searchSpinnerJobs//"+jobs[arg2]);
				if(arg2==0){
					searchWordType = "";
				}else{
					searchWordType = jobs[arg2];
				}
			}
			try {
				getMemberStoreList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub			// 안바꾸면 마는거지
	}
	
	
	
	
	
	public class backgroundGetMerchantInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
			// TODO Auto-generated method stub  
			//			setListAdapter(new MyCustomAdapter(AndroidList.this, R.layout.row, month));  
			//			Toast.makeText(AndroidList.this,    "onPostExecute \n: setListAdapter after bitmap preloaded",    Toast.LENGTH_LONG).show(); 
		} 
		@Override protected void onPreExecute() {  
			// TODO Auto-generated method stub  
			//			Toast.makeText(AndroidList.this,    "onPreExecute \n: preload bitmap in AsyncTask",    Toast.LENGTH_LONG).show(); 
		} 
		@Override protected Void doInBackground(Void... params) {  
			// TODO Auto-generated method stub  
//			preLoadSrcBitmap();  
			
//			for(int i=0; i<entries1.size(); i++){
//				Log.e(TAG,"entries1.get("+i+").getProfileImageURL()"+entries1.get(i).getProfileImageURL());
//			}
			
			getMerchantInfo();
			return null; 
		}
	}

}
