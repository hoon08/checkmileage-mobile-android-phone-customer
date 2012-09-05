package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
/*
 *  ������ ���. (�˻�?)
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
	int app_end = 0;	// �ڷΰ��� ��ư���� ������ 2������ ��������	// ó�� �ι� �ڵ� ����  �Ǵ°�.
	
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
	
	public List<CheckMileageMerchants> entries;	// 1�������� ��ȸ�� ���. (������ �� ���� ����)
	List<CheckMileageMerchants> entriesFn = null;			// ���� ���⹰
	
	int returnYN = 0;		// ������ ������ ���� �������� ���� �����뵵
	
	float fImgSize = 0;
	int isRunning = 0;			// ���� ���� ����. ���� �߿� �ٽ� ���� ��û�� ���� ���, �����Ѵ�.
	View emptyView;
	// �����
	ProgressBar pb1;
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. 		���� ����Ʈ ���̾ƿ�.
					if(entriesFn.size()>0){
						setGriding();
					}else{
						Log.e(TAG,"no data");
						emptyView = findViewById(R.id.empty1);		// ������ ������ '�� ������'(������ ���� �޽���)ǥ��
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// ���α׷����� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���α׷�����  ����
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
	

	
	// ListView�� �Ѹ� Data �� ���� ���ǳ� �����͵�. --> ���߿� ���� ����Ͽ� ó���� �����ͼ� ����� ������ �Ѵ�.
	String[] areas = {"������", "����", "�Ȼ�", "����"};			// ���߿� ��ȸ �� �� ��..
	String[] jobs = {"��� ����", "PX", "����" , "�Ĵ�"};
	
	GridView gridView;
	static final String[] MOBILE_OS = new String[] { 
		"Android", "iOS","Windows", "Blackberry" };
	
	@Override
	protected void onCreate(Bundle icicle) {
		
		
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);
		
		// �� QR �ڵ�. 
		myQRcode = MyQRPageActivity.qrCode;		
		// ũ�� ����
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
		
		// ������ ����. ������ ���޾���.
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
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getMerchantID());		// ������ ���̵�
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// ���� �ĺ� ��ȣ. (�󼼺��� ��ȸ�뵵)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// �� ���ϸ���
				startActivity(intent);
				/*
				 * �뷫 ������ ���� �۾��� �ʿ��ϴ�.
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
//			    // ������ ó��
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
////		         ListAppend();//�����Լ� ȣ�� �Ǵ� ������ ���
//		        //ListAppendThread thread = new ListAppendThread(0);
//		        //thread.start();
//		    }
//		   }
		  }
		 };
     
	
	/*
	 * ������ ����Ͽ�   ������ ����� �����´�.
	 * �� ����� List<CheckMileageMerchant> Object �� ��ȯ �Ѵ�.  
	 * 
	 *  ȣ�� ��� :: 
	 * checkMileageMerchantController // selectSearchMerchantList // checkMileageMerchant 
	 * 
	 *  ������ ���� :  businessArea01 / businessKind03 / activateYn
	 * 
	 *	�޴� ����.
	 *  merchantId // companyName / // profileImageUrl / 
	 *  workPhoneNumber / zipCode01 / address01/ address02 / businessArea01 // latitude // longitude
	 * 
	 *  ��ġ�ϸ� ������ �������� �����ϱ� ������ Ű�� �ʿ��ϴ�..  merchantId ������..
	 *  
	 * -----------------------------------
	 * |[�̹��� ��]  [������ �̸�]  [�� ����Ʈ] |
	 * |[�̹��� ��]	[ �� �� �� �� �� �� �� ]    |
	 * ------------------------------------
	 */
	public void getMemberStoreList() throws JSONException, IOException {
		Log.i(TAG, "getMemberStoreList");
		controllerName = "checkMileageMerchantController";
		methodName = "selectSearchMerchantList";
		// �ε����Դϴ�..  
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
		// ���� ��ź�
		new Thread(
				new Runnable(){
					public void run(){
						JSONObject obj = new JSONObject();
						try{
							obj.put("activateYn", "Y");
							obj.put("businessArea01", searchWordArea);		// ����		  
							obj.put("businessKind03", searchWordType);		// ����					// ���� ��ȣ ��������, �� ���̵� �ʿ�...
							obj.put("checkMileageId", myQRcode);			// �� ���̵�
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							if(responseCode==200||responseCode==204){
								InputStream in =  connection2.getInputStream();
								// ��ȸ�� ����� ó��.
								theData1(in);
							}else{
								// �������� �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
							// �������� �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
	 * �ϴ� ���ϸ��� ��� ����� ����. (������ ������ ���� ���̵� ����ִ� ����)
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
		Log.d(TAG,"����::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		// // // // // // // �ٷ� �ٷ� ȭ�鿡 add �ϰ� ��ġ�� �� �����ٰ� �� ���� ������....
		
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
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchant");		// ��ҹ��� ����
//					Log.e(TAG, "merchantId::"+jsonObj.getString("merchantId"));
//					Log.e(TAG, "companyName::"+jsonObj.getString("companyName"));
//					Log.e(TAG, "profileImageUrl::"+jsonObj.getString("profileImageUrl"));
					/*
					 * ����::[
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_1",   "companyName":"�ĸ��ٰ�Ʈ",   "profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_21442393.jpg",
					 * 					"workPhoneNumber":"02456789","zipCode01":"082","address01":"����","address02":"���ı�","latitude":5581265,"longitude":5578525}
					 * 				},
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_2","companyName":"�󸲼����","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_21443510.jpg",
					 * 					"workPhoneNumber":"026547897","zipCode01":"082","address01":"����","address02":"���ϱ�","latitude":5581265,"longitude":5578525}
					 * 			},
					 * 			{"checkMileageMerchant":
					 * 				{"merchantId":"memberstore_3","companyName":"����������","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_164411297c.jpg",
					 * 					"workPhoneNumber":"027896542","zipCode01":"082","address01":"��õ","address02":"��Ȳ��","latitude":5581265,"longitude":5578525}
					 * 			},
					 * 			{"checkMileageMerchant":{"merchantId":"memberstore_4","companyName":"�����絵","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0823\/nmms_14348250c.jpg","workPhoneNumber":"021236548","zipCode01":"082","address01":"���","address02":"�Ȼ�","latitude":5581265,"longitude":5578525}},{"checkMileageMerchant":{"merchantId":"memberstore_5","companyName":"�ֹ�����","profileImageUrl":"http:\/\/static.naver.net\/www\/u\/2012\/0828\/nmms_1845524c.jpg","workPhoneNumber":"024569872","zipCode01":"082","address01":"���","address02":"����","latitude":5581265,"longitude":5578525}}]
					 *
					 *			�� �� �ʿ��� ��. ������ ID, ������ �̸�, ������ URL(�̹��� �����ֱ� �뵵)
					 *
					 *
					 *
					 *
					private String idCheckMileageMileages;					// ���� �ĺ� ��ȣ.!!	
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
					 *   latitude  longitude								������������ ���� ���� ��ȸ ���ϵ��� ������ ������?..������ ��ȸ �ؾ� �ϴµ�.
					 *
					 * ������ ������ ���� �ʿ��� �͵�.
					 * intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		merchantId
					 *	intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());							�� �������� ���� �ʿ�.. - ��ȸ �ʿ�.. ����? ���Ƶ�
					 *	intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());														�������� ���� �� ���ϸ��� - ��ȸ �ʿ�.. �����Ұ�. 
					 *
					 */
					
					//  merchantId,  companyName,  profileImageUrl,  
					// ��ü ����� �� ������ �־ ����..  ���尪:  ���������̵�. ������ �̸�, ������ URL
					entries.add(
							new CheckMileageMerchants(
									jsonObj.getString("merchantId"),
									jsonObj.getString("companyName"),
									jsonObj.getString("profileImageUrl"),
//									""															// ���� �̰�. ���߿� �Ʒ���.
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

	// ������ URL�� �̹��� ��������.
	public void getMerchantInfo(final List<CheckMileageMerchants> entries3, int max){
		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>(max);
		final int max2 = max;
		Log.e(TAG,"max2::"+entries3.size());
		// ������ ���ؼ� ������.
		for (int j = 0; j < max2; j++ ){
			if((entries3.get(j).getProfileImageURL()).length()<1){
				entries3.get(j).setProfileImageURL("http://www.carsingh.com/img/noImage.jpg");	
			}
			//					 ������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�.
			Bitmap bm = LoadImage(entries3.get(j).getProfileImageURL());
			entries3.get(j).setMerchantImage(BitmapResizePrc(bm, (float)(fImgSize*0.3), (float)(fImgSize*0.4) ).getBitmap());		// ���� ����
		}		// for�� ����
		Log.d(TAG,"������ ���� ���� �Ϸ�. ");
		entriesFn = entries3;
		showInfo();
	}

	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.
	public void showInfo(){
		new Thread(
				new Runnable(){		// ���׹� ��
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
						b.putInt("showYN", 1);		// �����ֱ�.
						message.setData(b);
						handler.sendMessage(message);
					}
				}
		).start();
	}

	// ������ �̹��� URL ���� �̹��� �޾ƿͼ� �����ο� �����ϴ� �κ�.
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
	 * Bitmap �̹��� ��������
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
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

		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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
	 *  �ݱ� ��ư 2�� ������ ���� ��.
	 *  (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Log.i("MainTabActivity", "finish");		
		if(app_end == 1){
			Log.e(TAG,"kill all");
			mainActivity.finish();
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MemberStoreListPageActivity.this, "�ڷΰ��� ��ư�� �ѹ��� ������ ����˴ϴ�.", Toast.LENGTH_SHORT).show();
		}
	}

	
	/*
	 * ���ǳ�. ������ ���ý�, �Ǵ� ��ȭ ���� �ÿ� ���� �̺�Ʈ. 
	 * �ٸ��� �����ϸ� ���� ����Ͽ� ��ȸ�ؿ´�. ��ȭ ������ ��ȭ ����.
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
			if(searchSpinnerArea==arg0){		// ���� ������ ��� .	// ���� ����. arg2 �� ���° ������.. areas[arg2]
				Log.e(TAG,"searchSpinnerArea//"+areas[arg2]);	
				if(arg2==0){
					searchWordArea = "";  
				}else{
					searchWordArea = areas[arg2];
				}
			}else{								// ���� ������ ��� .	// ���� ����.	areas[jobs]		// 0 �� ��ü�ϱ� ����� �˻�, 0�� �ƴ� ��� �ش� ������ �˻�.
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
		// TODO Auto-generated method stub			// �ȹٲٸ� ���°���
		
	}
	
}
