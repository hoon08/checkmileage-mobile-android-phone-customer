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

import android.app.ListActivity;

public class MemberStoreListPageActivity extends Activity implements OnItemSelectedListener {
	
	String TAG = "MemberStoreListPageActivity";
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록	// 처음 두번 자동 실행  되는거.
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int dontTwice = 1;
	
	String myQRcode = "";
	
	int responseCode = 0;
	String controllerName = "";
	String methodName = "";
	String searchWordArea = "";
	String searchWordType = "";
	
	Spinner searchSpinnerArea;
	Spinner searchSpinnerType;
	
	public List<CheckMileageMerchants> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	List<CheckMileageMerchants> entriesFn = null;			// 최종 산출물
	
	int returnYN = 0;		// 가맹점 상세정보 보고 리턴할지 여부 결정용도
	
	float fImgSize = 0;
	int isRunning = 0;			// 연속 실행 방지. 실행 중에 다시 실행 요청이 들어올 경우, 무시한다.
	View emptyView;
	// 진행바
	ProgressBar pb1;
	
	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// 받아온 마일리지 결과를 화면에 뿌려준다.
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 		여기 리스트 레이아웃.
					if(entriesFn.size()>0){
						setGriding();
					}else{
						Log.e(TAG,"no data");
						emptyView = findViewById(R.id.empty1);		// 데이터 없으면 '빈 페이지'(데이터 없음 메시지)표시
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = isRunning -1;
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
	String[] areas = {"전지역", "강변", "안산", "종로"};			// 나중에 조회 해 올 것..
	String[] jobs = {"모든 업종", "PX", "매점" , "식당"};
	
	GridView gridView;
	static final String[] MOBILE_OS = new String[] { 
		"Android", "iOS","Windows", "Blackberry" };
	
	@Override
	protected void onCreate(Bundle icicle) {
		
		
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);
		
		// 내 QR 코드. 
		myQRcode = MyQRPageActivity.qrCode;		
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
		
		searchSpinnerArea.setOnItemSelectedListener(this);
		searchSpinnerType.setOnItemSelectedListener(this);
		
		// 데이터 세팅. 리스너 못달았음.
		ArrayAdapter<String> aa1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, areas);
		aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		searchSpinnerArea.setAdapter(aa1);
		 ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobs);
		 aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 searchSpinnerType.setAdapter(aa2);
	}
    
	public void setGriding(){
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(new ImageAdapter(this, entriesFn));
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
				/*
				 * 대략 다음과 같은 작업이 필요하다.
				 * Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);		//MemberStoreListPageActivity
						intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());
						intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());
						intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());
						startActivity(intent);
				 */
			}
		});
		gridView.setOnScrollListener(listScrollListener);
//			  @Override
//			  public void onScrollStateChanged(AbsListView view, int scrollState){}
//			  
//			 @Override
//			  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
//			  if(totalItemCount>0 && firstVisibleItem + visibleItemCount ==totalItemCount){
//			    // 데이터 처리
//				  Log.e(TAG, "onScroll event Occured.");
//			  }
//			 }
//		});
	}
	
	private OnScrollListener listScrollListener = new OnScrollListener(){
		  @Override
		  public void onScroll(AbsListView view, int firstVisibleItem,
		    int visibleItemCount, int totalItemCount) {
		   // TODO Auto-generated method stub
			  Log.e(TAG, "onScroll event Occured.");
		  }
		  @Override
		  public void onScrollStateChanged(AbsListView view, int scrollState) {
//		   if(mIsLoding==true)
//		    return;
			  
//		   int totalcount = mListItems.size();
			  
//		   if(totalcount==0)
//		    return;
			  
//		   if (scrollState == OnScrollListener.SCROLL_STATE_IDLE ) 
//		   {
//			   
//		    if( view.getLastVisiblePosition()==(totalcount-1)){
//		    	
//		     if(offset>mTotalRow){
//		      mIsLoding = false;
//		      
//		         return;
//		     }
//		     
////		         ListAppend();//직접함수 호출 또는 스레드 사용
//		        //ListAppendThread thread = new ListAppendThread(0);
//		        //thread.start();
//		    }
//		   }
		  }
		 };
     
	
	/*
	 * 서버와 통신하여   가맹점 목록을 가져온다.
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
	 * -----------------------------------
	 * |[이미지 상]  [가맹점 이름]  [내 포인트] |
	 * |[이미지 하]	[ 가 맹 점 이 용 시 각 ]    |
	 * ------------------------------------
	 */
	public void getMemberStoreList() throws JSONException, IOException {
		Log.i(TAG, "getMemberStoreList");
		controllerName = "checkMileageMerchantController";
		methodName = "selectSearchMerchantList";
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
								// 에러나면 로딩바 없애고 다시 할수 있도록
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
							// 에러나면 로딩바 없애고 다시 할수 있도록
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
	}

	/*
	 * 일단 마일리지 목록 결과를 받음. (가맹점 정보는 없이 아이디만 들어있는 상태)
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
//		Log.e(TAG,"max::"+max);
		try {
			entries = new ArrayList<CheckMileageMerchants>(max);
			if(max>0){
				for ( int i = 0; i < max; i++ ){
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchant");		// 대소문자 주의
//					Log.e(TAG, "merchantId::"+jsonObj.getString("merchantId"));
//					Log.e(TAG, "companyName::"+jsonObj.getString("companyName"));
//					Log.e(TAG, "profileImageUrl::"+jsonObj.getString("profileImageUrl"));
					/*
					 * 수신::[
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_1",   "companyName":"파리바게트",   "profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_21442393.jpg",
					 * 					"workPhoneNumber":"02456789","zipCode01":"082","address01":"서울","address02":"송파구","latitude":5581265,"longitude":5578525}
					 * 				},
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_2","companyName":"농림수산부","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_21443510.jpg",
					 * 					"workPhoneNumber":"026547897","zipCode01":"082","address01":"서울","address02":"강북구","latitude":5581265,"longitude":5578525}
					 * 			},
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_3","companyName":"지구방위대","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_164411297c.jpg",
					 * 					"workPhoneNumber":"027896542","zipCode01":"082","address01":"인천","address02":"봉황시","latitude":5581265,"longitude":5578525}
					 * 			},
					 * 			{"checkMileageMerchant":{"merchantId":"memberstore_4","companyName":"무한재도","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0823\/nmms_14348250c.jpg","workPhoneNumber":"021236548","zipCode01":"082","address01":"경기","address02":"안산","latitude":5581265,"longitude":5578525}},{"checkMileageMerchant":{"merchantId":"memberstore_5","companyName":"애벌랜드","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_1845524c.jpg","workPhoneNumber":"024569872","zipCode01":"082","address01":"경기","address02":"구미","latitude":5581265,"longitude":5578525}}]
					 *
					 *			이 중 필요한 것. 가맹점 ID, 가맹점 이름, 가맹점 URL(이미지 보여주기 용도)
					 *
					 *
					 *
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
					entries.add(
							new CheckMileageMerchants(
									jsonObj.getString("merchantId"),
									jsonObj.getString("companyName"),
									jsonObj.getString("profileImageUrl"),
//									""															// 아직 이거. 나중에 아래거.
									jsonObj.getString("idCheckMileageMileages"),
									jsonObj.getString("mileage")
							)
					);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			getMerchantInfo(entries,max);
		}
	}

	// 가맹점 URL로 이미지 가져오기.
	public void getMerchantInfo(final List<CheckMileageMerchants> entries3, int max){
		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>(max);
		final int max2 = max;
		Log.e(TAG,"max2::"+entries3.size());
		// 각각에 대해서 돌린다.
		for (int j = 0; j < max2; j++ ){
			if((entries3.get(j).getProfileImageURL()).length()<1){
				entries3.get(j).setProfileImageURL("http://www.carsingh.com/img/noImage.jpg");	
			}
			//					 가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다.
			Bitmap bm = LoadImage(entries3.get(j).getProfileImageURL());
			entries3.get(j).setMerchantImage(BitmapResizePrc(bm, (float)(fImgSize*0.3), (float)(fImgSize*0.4) ).getBitmap());		// 세로 가로
		}		// for문 종료
		Log.d(TAG,"가맹점 정보 수신 완료. ");
		entriesFn = entries3;
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
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		Log.e(TAG,"BitmapResizePrc");

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
		Log.i("ImageResize", "Image Resize Result : " + Boolean.toString((newHeight==height)&&(newWidth==width)) );

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
	 * 스피너. 아이템 선택시, 또는 변화 없을 시에 대한 이벤트. 
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub			// 안바꾸면 마는거지
		
	}
	
}
