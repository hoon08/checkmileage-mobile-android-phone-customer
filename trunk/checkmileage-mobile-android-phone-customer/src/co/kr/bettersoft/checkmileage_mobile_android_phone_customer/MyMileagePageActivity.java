package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 내 마일리지 보기 화면


/*
 * 아답터를 꼬진거를 써서 페이지 올때마다 getView 한다.. 나중에 고쳐야 겠다..
 * 
 */
import java.io.BufferedReader;

import java.io.BufferedInputStream;
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

import com.kr.bettersoft.domain.CheckMileageMileage;
import com.pref.DummyActivity;
import com.utils.adapters.ImageAdapter;
import com.utils.adapters.ImageAdapterList;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MyMileagePageActivity extends Activity {
	int app_end = 0;	// 뒤로가기 버튼으로 닫을때 2번만에 닫히도록
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int responseCode = 0;
	String TAG = "MyMileagePageActivity";
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	public List<CheckMileageMileage> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	int returnYN = 0;		// 가맹점 상세정보 보고 리턴할지 여부 결정용도
	int flag = 0;
	
	
	/*  구식 방법 사용 안함
	private ArrayAdapter<String> m_adapter = null;
	private ListView m_list = null;
	ArrayAdapter<CheckMileageMileage> adapter = null;
	MyAdapter mAdapter;
*/ 

	List<CheckMileageMileage> entriesFn = null;
	float fImgSize = 0;
	int isRunning = 0;
	
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
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 
					
					if(entriesFn.size()>0){
						setListing();
					}else{
						Log.e(TAG,"no data");
						emptyView = findViewById(R.id.empty2);
						listView  = (ListView)findViewById(R.id.listview);
						listView.setEmptyView(emptyView);
						listView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					
					/* 구식 방법
					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);
					*/
					
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// 러닝바 실행
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// 러닝바 종료
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
			}catch(Exception e){
//				Toast.makeText(MyMileagePageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};

	ListView listView;
	
	public Context returnThis(){
		return this;
	}

	public void setListing(){
		listView  = (ListView)findViewById(R.id.listview);
		listView.setAdapter(new ImageAdapterList(this, entriesFn));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
//				Log.i(TAG, "checkMileageMerchantsMerchantID::"+entriesFn.get(position).getCheckMileageMerchantsMerchantID());
//				Log.i(TAG, "myMileage::"+entriesFn.get(position).getMileage());
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// 가맹점 아이디
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// 고유 식별 번호. (상세보기 조회용도)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// 내 마일리지    // 가맹점에 대한 내 마일리지
				startActivity(intent);
			}
		});
	}
	
	/*   // 구식 방법을 사용하지 않음.
	// 어댑터 클래스. 이곳에서 얻어온 데이터를 뷰 아이디를 통해 세팅한다.
	class MyAdapter extends BaseAdapter{
		Context context;
		int layoutId;
		ArrayList<CheckMileageMileage> myDataArr;
		LayoutInflater Inflater;
		MyAdapter(Context _context, int _layoutId, ArrayList<CheckMileageMileage> _myDataArr){
			context = _context;
			layoutId = _layoutId;
			myDataArr = _myDataArr;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			return myDataArr.size();
		}
		@Override
		public String getItem(int position) {
			return myDataArr.get(position).getCheckMileageMerchantsMerchantID();
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null)  {
				convertView = Inflater.inflate(layoutId, parent, false);
			}
			ImageView leftImg = (ImageView)convertView.findViewById(R.id.merchantImage);		// 가맹점 이미지 넣고
			// set the Drawable on the ImageView
			if(myDataArr.get(position).getMerchantImage()!=null){
				BitmapDrawable bmpResize = BitmapResizePrc(myDataArr.get(position).getMerchantImage(), fImgSize/2, fImgSize/2);  
				leftImg.setImageDrawable(bmpResize);	
			}
				
//			leftImg.setImageBitmap(myDataArr.get(position).getMerchantImage());			
			
			TextView nameTv = (TextView)convertView.findViewById(R.id.merchantName);			// 가맹점 이름 넣고
			nameTv.setText(myDataArr.get(position).getMerchantName());
			TextView mileage = (TextView)convertView.findViewById(R.id.mileage);				// 가맹점에 대한 내 마일리지 넣고		.. 더 넣을거 있으면 아래에 추가, XML 파일에도 뷰 등록..
			mileage.setText(myDataArr.get(position).getMileage()+"점");					
			
			TextView workPhone = (TextView)convertView.findViewById(R.id.merchantPhone);				// 가맹점 전번.
			workPhone.setText(myDataArr.get(position).getWorkPhoneNumber());		
			
//			Button btn = (Button)convertView.findViewById(R.id.sendBtn);		// 하단 버튼 넣어서 클릭시 어쩌구..
//			btn.setOnClickListener(new Button.OnClickListener()  {
//				public void onClick(View v)  {
//					String str = myDataArr.get(pos).name + "님의 전화번호는 [ "+
//					                                                   myDataArr.get(pos).phone+" ] 입니다.";
//					Toast.makeText(context, str,Toast.LENGTH_SHORT).show();
//				}
//			});
			return convertView;
		}
	}
	*/
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pb1 = (ProgressBar) findViewById(R.id.ProgressBar01);
//		final ProgressDialog dialog= ProgressDialog.show(MyMileagePageActivity.this, "타이틀","메시지",true);
////		b. Dialog를 화면에서 제거하는 코드를 작성한다. 예를 들어 3초쯤 있다가 다이얼로그를 없애고 싶다면...
//		new Thread(new Runnable() {
//		public void run() {
//		try { Thread.sleep(3000); } catch(Exception e) {}
//		dialog.dismiss();
//		}
//		});

		myQRcode = MyQRPageActivity.qrCode;			// 내 QR 코드. (확인용)
		
		// 크기 측정
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
		if(screenWidth < screenHeight ){
	    	fImgSize = screenWidth;
	    }else{
	    	fImgSize = screenHeight;
	    }
		
		Log.e(TAG, myQRcode);
		URL imageURL = null;							
		URLConnection conn = null;
		InputStream is= null;
		
		setContentView(R.layout.my_mileage);
		
		/* 구식 방법
		m_list = (ListView) findViewById(R.id.id_list);
		m_list.setOnItemClickListener(onItemClick);
		*/
		
		if(isRunning<1){
			isRunning = isRunning+1;
			try {
				myQRcode = MyQRPageActivity.qrCode;
				getMyMileageList();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			Log.e(TAG, "이미 실행중..");
		}
	}

	
	/* 구식 방법을 사용하지 않음
	
	AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			// 실행문
//			Toast.makeText(MyMileagePageActivity.this, "터치터치"+arg2+"이곳은:"+entriesFn.get(arg2).getCheckMileageMerchantsMerchantID(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
			intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		// 가맹점 아이디
			intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());					// 고유 식별 번호
			intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());			// 가맹점에 대한 내 마일리지
			startActivity(intent);
		}
	};

*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	          
	          
	          
	
	
	
	
	
	
	
	
	/*
	 * 서버와 통신하여 내 마일리지 목록을 가져온다.
	 * 그 결과를 List<CheckMileageMileage> Object 로 반환 한다.
	 * 
	 * 보내는 정보 : 액티베이트Y, 내QR코드 스트링
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  받는 정보 : 가맹점 등록 이미지 , 가맹점 이름, 해당 가맹점에 대한 내 마일리지, 마지막 사용 일시, 
	 *  터치하면 가맹점 상세정보로 가야하기 때문에 키도 필요하다..
	 *  
	 * -----------------------------------
	 * |[이미지 상]  [가맹점 이름]  [내 포인트] |
	 * |[이미지 하]	[ 가 맹 점 이 용 시 각 ]    |
	 * ------------------------------------
	 */
	public void getMyMileageList() throws JSONException, IOException {
		Log.i(TAG, "getMyMileageList");
		controllerName = "checkMileageMileageController";
		methodName = "selectMemberMerchantMileageList";
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
		
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							// 자신의 아이디를 넣어서 조회
							obj.put("activateYn", "Y");
							obj.put("checkMileageMembersCheckMileageId", myQRcode);
							Log.i(TAG, "myQRcode::"+myQRcode);
							
						}catch(Exception e){
							e.printStackTrace();
						}
						String jsonString = "{\"checkMileageMileage\":" + obj.toString() + "}";
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
							InputStream in =  connection2.getInputStream();
							// 조회한 결과를 처리.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
							// 에러니까 로딩바 없애고 다시 할수 있도록
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
		int doneCnt = 0;
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
		if(responseCode==200 || responseCode==204){
			try {
				entries = new ArrayList<CheckMileageMileage>(max);
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						doneCnt++;
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
						entries.add(new CheckMileageMileage(jsonObj.getString("idCheckMileageMileages"),
								jsonObj.getString("mileage"),jsonObj.getString("modifyDate"),
								jsonObj.getString("checkMileageMembersCheckMileageId"),jsonObj.getString("checkMileageMerchantsMerchantId")
						));
						
					}
					//    			 2차 작업. 가맹점 이름, 이미지 가져와서 추가로 넣음.
					//    			 array 채로 넘기고 돌려받을수 있도록 한다..
				}
			} catch (JSONException e) {
				doneCnt--;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				Log.e(TAG, ""+doneCnt);
				if(doneCnt>0){
					getMerchantInfo(entries,max);
				}else{		// 데이터 없어도 로딩은 끝내쟈
					entriesFn = entries;
					showInfo();
				}
			}
		}else{			// 요청 실패시	 토스트 띄우고 화면 유지.
			Toast.makeText(MyMileagePageActivity.this, "오류가 발생하였습니다.\n잠시 후 다시 시도하여 주십시오.", Toast.LENGTH_SHORT).show();
		}
	}

	// 가맹점 아이디로 가맹점 정보 가져오기. .. Array채로 주고 받기..
	public void getMerchantInfo(final List<CheckMileageMileage> entries3, int max){
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMileage> entries2 = new ArrayList<CheckMileageMileage>(max);
		final int max2 = max;
		// 각각에 대해서 돌린다.
		new Thread(
				new Runnable(){
					public void run(){
						for (int j = 0; j < max2; j++ ){
							// 가맹점 아이디를 꺼낸다.
							final String merchantId2 = entries3.get(j).getCheckMileageMerchantsMerchantID();
							// 요청할 문자열을 만들기 위함. (json 방식으로 보내기 위해 생성)
							JSONObject obj = new JSONObject();
							try{
								// 보낼 데이터 세팅
								obj.put("activateYn", "Y");
								obj.put("merchantId", merchantId2);
								Log.i(TAG, "merchantId::"+merchantId2);
							}catch(Exception e){
								e.printStackTrace();
							}
							// 보낼 문자열. (위의 json 방식의 오브젝트를 문자열로)
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
								InputStream in =  connection2.getInputStream();
								// 가맹점 아이디로 가맹점 정보를 가져온걸 처리..저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디. + 가맹점 이름, 가맹점 이미지 URL
								BufferedReader reader = new BufferedReader(new InputStreamReader(in));	
								StringBuilder builder = new StringBuilder();
								String line =null;
								while((line=reader.readLine())!=null){
									builder.append(line).append("\n");
								}
								Log.d(TAG,"수신::"+builder.toString());
								String tempstr = builder.toString();		// 받은 데이터를 가공하여 사용할 수 있다... 용도에 맞게 구현할 것.
								JSONObject jsonObject;	// 1차로 받은거.
								JSONObject jsonObject2;	// 1차로 받은거중 "가맹점아이디" 으로 꺼낸거. --> 여기서 스트링으로 값 하나씩 꺼낸다.
								if(connection2.getResponseCode()==200 || connection2.getResponseCode()==204){		// 요청 성공시
									jsonObject = new JSONObject(tempstr);
									jsonObject2 = jsonObject.getJSONObject("checkMileageMerchant");
									entries3.get(j).setMerchantName(jsonObject2.getString("companyName"));// 가맹점 정보를 받는다. 이름
									if(jsonObject2.getString("workPhoneNumber")==null || jsonObject2.getString("workPhoneNumber").length()<1){	// 가맹점 정보를 받는다. 전번
										entries3.get(j).setWorkPhoneNumber("");// 가맹점 정보를 받는다. 전번
									}else{
										entries3.get(j).setWorkPhoneNumber("(☎)"+jsonObject2.getString("workPhoneNumber"));
									}
									// 가맹점 이미지 URL 저장한다.
									if(jsonObject2.getString("profileImageUrl").length()>0){
										entries3.get(j).setMerchantImg(jsonObject2.getString("profileImageUrl"));
									}else{
										entries3.get(j).setMerchantImg("http://www.carsingh.com/img/noImage.jpg");		// 인터넷에서 퍼온 기본 이미지 url --> 나중에 안전한 url 또는 이미지파일로 변경할 것.
									}
									
									// 가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다.
									Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
									// bm 이미지 크기 변환 .
									BitmapDrawable bmpResize = BitmapResizePrc(bm, fImgSize/4, fImgSize/4);  
									entries3.get(j).setMerchantImage(bmpResize.getBitmap());
								}
							}catch(Exception e){ 
								e.printStackTrace();
							}
						}		// for문 종료
						Log.d(TAG,"가맹점 정보 수신 완료. ");
						entriesFn = entries3;
						showInfo();
					}
				}
		).start();
	}

	// entries3 를 전역에 저장후 스레드 이용하여 돌린다. 화면에 보여준다.
	public void showInfo(){
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
		
		
		//  가져온 데이터 화면에 보여주기.
		new Thread(
				new Runnable(){
					public void run(){
						Message message = handler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("showYN", 1);
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
	 *  닫기 버튼 2번 누르면 종료 됨.(non-Javadoc)
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
			Toast.makeText(MyMileagePageActivity.this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	////////////////////////   하드웨어 메뉴 버튼.  ////////////////
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, "새로 고침" );             // 신규등록 메뉴 추가.
//	          getMenuInflater().inflate(R.menu.activity_main, menu);
	        return (super .onCreateOptionsMenu(menu));
	    }
	   
	    // 옵션 메뉴 특정 아이템 클릭시 필요한 일 처리
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item){
	      return (itemCallback(item)|| super.onOptionsItemSelected(item));
	    }
	   
	    // 아이템 아이디 값 기준 필요한 일 처리
	    public boolean itemCallback(MenuItem item){
	      switch(item.getItemId()){
	      case Menu. FIRST+1:
//	    	  Toast.makeText(MyMileagePageActivity.this, "123123", Toast.LENGTH_SHORT).show();
//	                Intent intent = new Intent(UserManagementActivity.this,AddUserActivity.class);        // example에서 이름
//	            Intent intent = new Intent(MainActivity.this ,AddUserActivity.class);
//	            startActivity(intent);
	    	  if(isRunning<1){
	  			isRunning = isRunning+1;
	  			try {
	  				myQRcode = MyQRPageActivity.qrCode;
	  				getMyMileageList();
	  			} catch (JSONException e) {
	  				// TODO Auto-generated catch block
	  				e.printStackTrace();
	  			} catch (IOException e) {
	  				// TODO Auto-generated catch block
	  				e.printStackTrace();
	  			}
	  		}else{
	  			Log.e(TAG, "이미 실행중..");
	  		}
	             return true ;
	      }
	      return false;
	    }
	
	////////////////////////////////////////////////////////////
	
	
	
}
