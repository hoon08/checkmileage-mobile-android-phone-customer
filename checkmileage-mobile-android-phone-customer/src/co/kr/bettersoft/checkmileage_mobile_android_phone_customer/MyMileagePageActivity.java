package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 내 마일리지 보기 화면
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MyMileagePageActivity extends Activity {
	int responseCode = 0;
	String TAG = "MyMileagePageActivity";
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	public List<CheckMileageMileage> entries;	// 1차적으로 조회한 결과. (가맹점 상세 정보 제외)
	int returnYN = 0;		// 가맹점 상세정보 보고 리턴할지 여부 결정용도
	int flag = 0;
	private ArrayAdapter<String> m_adapter = null;
	private ListView m_list = null;
	ArrayAdapter<CheckMileageMileage> adapter = null;
	List<CheckMileageMileage> entriesFn = null;
    float fImgSize = 0;
	MyAdapter mAdapter;

	// 핸들러
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			try{
				Bundle b = msg.getData();		// 받아온 마일리지 결과를 화면에 뿌려준다.
				if(b.getInt("showYN")==1){
					// 최종 결과 배열은 entriesFn 에 저장되어 있다.. 
					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);
					
				}
			}catch(Exception e){
				Toast.makeText(MyMileagePageActivity.this, "에러가 발생하였습니다."+entriesFn.size(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	};

	public Context returnThis(){
		return this;
	}

	
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
			BitmapDrawable bmpResize = BitmapResizePrc(myDataArr.get(position).getMerchantImage(), fImgSize/2, fImgSize/2);  
			leftImg.setImageDrawable(bmpResize);	
//			leftImg.setImageBitmap(myDataArr.get(position).getMerchantImage());			
			
			TextView nameTv = (TextView)convertView.findViewById(R.id.merchantName);			// 가맹점 이름 넣고
			nameTv.setText(myDataArr.get(position).getMerchantName());
			TextView mileage = (TextView)convertView.findViewById(R.id.mileage);				// 가맹점에 대한 내 마일리지 넣고		.. 더 넣을거 있으면 아래에 추가, XML 파일에도 뷰 등록..
			mileage.setText(myDataArr.get(position).getMileage());					
			
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
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		m_list = (ListView) findViewById(R.id.id_list);
		m_list.setOnItemClickListener(onItemClick);
		
	}

	AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			// 실행문
//			Toast.makeText(MyMileagePageActivity.this, "터치터치"+arg2+"이곳은:"+entriesFn.get(arg2).getCheckMileageMerchantsMerchantID(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
			intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());
			intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());
			intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());
			startActivity(intent);
		}
	};
	


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
						JSONObject obj = new JSONObject();
						try{
							// 자신의 아이디를 넣어서 조회
							obj.put("activateYn", "Y");
							obj.put("checkMileageMembersCheckMileageId", myQRcode);
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
		if(responseCode==200 || responseCode==204){
			try {
				entries = new ArrayList<CheckMileageMileage>(max);
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// 객체 만들고 값 받은거 넣어서 저장..  저장값: 인덱스번호, 수정날짜, 아이디, 가맹점아이디.
						entries.add(new CheckMileageMileage(jsonObj.getString("idCheckMileageMileages"),
								jsonObj.getString("mileage"),jsonObj.getString("modifyDate"),
								jsonObj.getString("checkMileageMembersCheckMileageId"),jsonObj.getString("checkMileageMerchantsMerchantId")));
						
					}
					//    			 2차 작업. 가맹점 이름, 이미지 가져와서 추가로 넣음.
					//    			 array 채로 넘기고 돌려받을수 있도록 한다..
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				getMerchantInfo(entries,max);
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
									entries3.get(j).setMerchantImg(jsonObject2.getString("profileImageUrl"));  // 가맹점 이미지 URL 저장한다.
									// 가맹점 이미지 URL로부터 이미지 받아와서 도메인에 저장한다.
									Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
									entries3.get(j).setMerchantImage(bm);
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
	}

}
