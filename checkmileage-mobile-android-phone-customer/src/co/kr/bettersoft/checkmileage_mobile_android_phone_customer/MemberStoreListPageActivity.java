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
	int app_end = 0;	// �ڷΰ��� ��ư���� ������ 2������ ��������	// ó�� �ι� �ڵ� ����  �Ǵ°�.
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int dontTwice = 1;				// ���ǳ� �����ʷ� ���� �ʱ� 2ȸ ��ȸ ����. 
	
	String myQRcode = "";			// �� ���̵�
	
	int responseCode = 0;			// ���� ��ȸ ��� �ڵ�
	String controllerName = "";		// ���� ��ȸ�� ��Ʈ�ѷ� �̸�
	String methodName = "";			// ���� ��ȸ�� �޼��� �̸�
	String searchWordArea = "";		// ���� ��ȸ�� ������
	String searchWordType = "";		// ���� ��ȸ�� ������
	
	
	
	
	
	Spinner searchSpinnerArea;		// ��� ���� ���
	Spinner searchSpinnerType;		// ��� ���� ���
	
	int indexDataFirst = 0;			// �κ� �˻� ���� �ε���. ������
	int indexDataLast = 0;			// �κ� �˻� ���� �ε���. ����
	int indexDataTotal = 0;			// �κ� �˻� ���� �ε���. ��ü ����
	
	Boolean mIsLast = false;			// ������ ����. true ��� ���̻��� �߰� ����. �� ��ȸ�� false �� �ʱ�ȭ
	Boolean adding = false;			// ������ ���ϱ� ���� ����.
	Boolean newSearch = false; 		// ���ο� ��ȸ���� ����. ���ο� ��ȸ��� ���� �����ʹ� ����� ���� �˻��� �����͸� ���. ���ο� ��ȸ�� �ƴ϶�� ���� �����Ϳ� �߰� �����͸� �߰�.
	
	private ImageAdapter imgAdapter;
	
	public ArrayList<CheckMileageMerchants> entries1 = new ArrayList<CheckMileageMerchants>();	// 1�������� ��ȸ�� ���. (������ �� ���� ����)   // �����.
	ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>();			// �߶� ���ϴ� �κ�.
	List<CheckMileageMerchants> entriesFn = new ArrayList<CheckMileageMerchants>();			// ���� ���⹰
	
	
	float fImgSize = 0;			// �̹��� ������ ���庯��.
	int isRunning = 0;			// ���� ���� ����. ���� �߿� �ٸ� ���� ��û�� ���� ���, �����Ѵ�.
	View emptyView;				// ������ ���� ��

	// �����
	ProgressBar pb1;
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. ���� ����Ʈ ���̾ƿ�.
					if(entriesFn.size()>0){
						Log.e(TAG,"indexDataFirst::"+indexDataFirst);
						if(newSearch){		// ���ο� �˻��� ��� ���� ����, �߰��� ��� �˸��� �ϱ� ����.
							setGriding();
							newSearch = false;		// �ٽ� �������´�. ������ ���� �˻��� �ƴ�.
						}else{
							Log.e(TAG,"notifyDataSetChanged");
							Log.e(TAG,"size:"+entriesFn.size());
							imgAdapter.notifyDataSetChanged();		// �˸� -> ��������� ȭ��� ������Ʈ �ǵ�����.
						}
						adding = false;		// �߰� ������. �ٸ��� ��ȸ�� �� �߰� ����.. (��ũ�� �����ʸ� �ٷ궧 ���)
					}else{
						Log.e(TAG,"no data");
						emptyView = findViewById(R.id.empty1);		// ������ ������ '�� ������'(������ ���� �޽���)ǥ��
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					isRunning = 0;		// ���������� ����. - ���� �߰� �������� �� ��ȸ ����.
					searchSpinnerArea.setEnabled(true);
					searchSpinnerType.setEnabled(true);
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
	String[] areas = {"������", "ȫ��", "����", "������", "�Ÿ�", "����", "����", "�Ǵ�", "���", "���з�", "���ǵ�"};			// ���߿� ��ȸ �� �� ��..
	String[] jobs = {"��� ����", "PX", "����" , "�Ĵ�"};
	
	GridView gridView;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.member_store_list);
		
		// �� QR �ڵ�. 
		myQRcode = MyQRPageActivity.qrCode;		
		
		entriesFn = new ArrayList<CheckMileageMerchants>();
		
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
		// spinner listener
		searchSpinnerArea.setOnItemSelectedListener(this);
		searchSpinnerType.setOnItemSelectedListener(this);
		// ������ ����. 
		ArrayAdapter<String> aa1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, areas);
		aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		searchSpinnerArea.setAdapter(aa1);
		 ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobs);
		 aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 searchSpinnerType.setAdapter(aa2);
	}
    
	
	// �����͸� ȭ�鿡 ����
	public void setGriding(){
		imgAdapter = new ImageAdapter(this, entriesFn);
		gridView  = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(imgAdapter);
		// Ŭ���� �󼼺��� ��������
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
			}
		});
		gridView.setOnScrollListener(listScrollListener);		// ������ ���. ��ũ�ѽ� �ϴܿ� �����ϸ� �߰� ������ ��ȸ�ϵ���.
	}
	
	
	
	// �� ��ũ�� �̺�Ʈ. 
	private OnScrollListener listScrollListener = new OnScrollListener(){
		// �ǵ�⸸ �ϸ� �ָ��� ���.  ���� �ֱ� �ѵ� �ʹ� ������.... boolean ���� �����Ѵ�.
		  @Override
		  public void onScroll(AbsListView view, int firstVisibleItem,
		    int visibleItemCount, int totalItemCount) {
		   // TODO Auto-generated method stub
//			  if(indexDataFirst==indexDataLast){		// ������ �� ũ�� ���� �ִ°�
//				  mIsLast = true;
//			  }
			  
			  // ����Ʈ ���� �ϴܿ� �������� ���.
			  if(firstVisibleItem+visibleItemCount==totalItemCount &&(!adding)&&(!mIsLast)){
				  Log.e(TAG, "onScroll event Occured."+"//view::"+view+"//firstVisibleItem::"+firstVisibleItem+"//visibleItemCount:"+visibleItemCount+"//totalItemCount::"+totalItemCount);
				  adding = true;
				  new backgroundGetMerchantInfo().execute();		// �񵿱� ����
			  }
		  }
		  // ��ũ�� ����, ��, ��ũ�� ��.. �̶�� ����� �˼� �ִ�. ��� �ʿ� ����.. 
		  @Override
		  public void onScrollStateChanged(AbsListView view, int scrollState) {}
		 };
     
	
	/*
	 * ������ ����Ͽ�   ������ ����� �����´�. ���� ��ȸ.
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
	 */
	public void getMemberStoreList() throws JSONException, IOException {
		Log.i(TAG, "getMemberStoreList");
		controllerName = "checkMileageMerchantController";
		methodName = "selectSearchMerchantList";
		indexDataFirst = 0; // ȭ�� ù �� �ε���. �ʱ�ȭ.. �ٽ� 0����
		indexDataLast = 0;	// ȭ�鿡 �������� ���� �ε���. �Բ� �ʱ�ȭ.. 0����
		entriesFn.clear();		// �̰͵� �ʱ�ȭ �غ���. ȭ�鿡 �������� ������ ����Ʈ.
		newSearch = true;			// ���ο� �˻���. true ��� ���� �����ʹ� ��������.
		mIsLast = false;		// �ʱ�ȭ. ���� �ƴϹǷ� �߰� ����.
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
		
		if(isRunning==0){		// �����߿� �ٸ� ���� ����
			isRunning=1;
			searchSpinnerArea.setEnabled(false);
			searchSpinnerType.setEnabled(false);
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
									// ����� ������ �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
								// ������ �������� �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
			Log.e(TAG,"�̹� �������Դϴ�.");
		}
		
		
		
		
	}

	/*
	 * ������ ���� 1�� ������ ����. entries �����ο� ����. ���� url ������ ���� �̹����� �޾ƿ��� �Լ��� ȣ���Ѵ�.
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
		indexDataTotal = max;
//		Log.e(TAG,"max::"+max);
		try {
			entries1 = new ArrayList<CheckMileageMerchants>(max);
			if(max>0){
				for ( int i = 0; i < max; i++ ){
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMerchant");		// ��ҹ��� ����
					/*
					 *
					 * ������ ID, ������ �̸�, ������ URL(�̹��� �����ֱ� �뵵)
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
			new backgroundGetMerchantInfo().execute();	// getMerchantInfo(entries1); �� �񵿱�� ����
		}
	}

	
	// ������ URL�� �̹��� ��������.������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�. + ������� ���ϱ�
	public void getMerchantInfo(){
		Log.i(TAG, "merchantInfoGet");
		// ������ �ε���+10���� ��ü �������� Ŀ���� ��ü ���� ������.
		if(indexDataLast+10>=indexDataTotal){
			indexDataLast = indexDataTotal;
			mIsLast = true;
		}else{		// ��ü �������� �۴ٸ� 10��. �߰� ����.
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
		
		if(indexDataFirst+10>indexDataLast){	// ���������� �����ߴٸ� ��������ȣ.
			indexDataFirst = indexDataLast;
		}else{									// ���������� �������� �ʾҴٸ� +10
			indexDataFirst = indexDataFirst + 10;
		}
		Log.d(TAG,"������ ���� ���� �Ϸ�. ");
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
	 * Bitmap �̹��� ��������
	 * Src : ���� Bitmap
	 * newHeight : ���ο� ����
	 * newWidth : ���ο� ����
	 * ���� �ҽ� : http://skyswim42.egloos.com/3477279 ( webview ���� capture ȭ�� resizing �ϴ� source �� ���� )
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

		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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
	 * ���ǳ�. �ٸ� ������ ���ý�, �Ǵ� ���� ������ ���ýÿ� ���� �̺�Ʈ. 
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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub			// �ȹٲٸ� ���°���
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
