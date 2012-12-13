package kr.co.bettersoft.checkmileage.activities;
/*
 *  ������ ���. (�˻�?)
 *  
 *  
 */
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

import kr.co.bettersoft.checkmileage.adapters.MemberStoreSearchListAdapter;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.pref.DummyActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;


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
import android.util.Base64;
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
	int app_end = 0;	// �ڷΰ��� ��ư���� ������ 2������ ��������	// ó�� �ι� �ڵ� ����  �Ǵ°�.
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	String serverName = CommonUtils.serverNames;
	
	// Locale
    Locale systemLocale = null ;
//    String strDisplayCountry = "" ;
    String strCountry = "" ;
    String strLanguage = "" ;
    
//	int dontTwice = 1;				// ���ǳ� �����ʷ� ���� �ʱ� 2ȸ ��ȸ ����. 
	public boolean connected = false;  // ���ͳ� �������
	String myQRcode = "";			// �� ���̵�
	
	int responseCode = 0;			// ���� ��ȸ ��� �ڵ�
	String controllerName = "";		// ���� ��ȸ�� ��Ʈ�ѷ� �̸�
	String methodName = "";			// ���� ��ȸ�� �޼��� �̸�
	String searchWordArea = "";		// ���� ��ȸ�� ������
	String searchWordType = "";		// ���� ��ȸ�� ������
	
	String imgthumbDomain = CommonUtils.imgthumbDomain; 					// Img �����ö� ���ϸ� ���� ��� �տ� ���� ������. 
	
//	Spinner searchSpinnerArea;		// ��� ���� ���
	Spinner searchSpinnerType;		// ��� ���� ���
	
	TextView searchText;			//�˻���
	Button searchBtn;				//�˻���ư
	View parentLayout;			// Ű���� �ڵ� ����뵵
	InputMethodManager imm;
	int indexDataFirst = 0;			// �κ� �˻� ���� �ε���. ������
	int indexDataLast = 0;			// �κ� �˻� ���� �ε���. ����
	int indexDataTotal = 0;			// �κ� �˻� ���� �ε���. ��ü ����
	
	URL postUrl2 ;
	HttpURLConnection connection2;
	
	Boolean mIsLast = false;			// ������ ����. true ��� ���̻��� �߰� ����. �� ��ȸ�� false �� �ʱ�ȭ
	Boolean adding = false;			// ������ ���ϱ� ���� ����.
	Boolean newSearch = false; 		// ���ο� ��ȸ���� ����. ���ο� ��ȸ��� ���� �����ʹ� ����� ���� �˻��� �����͸� ���. ���ο� ��ȸ�� �ƴ϶�� ���� �����Ϳ� �߰� �����͸� �߰�.
	Boolean jobKindSearched = false;
	Bitmap bm = null;
	int reTry = 1;
	
	private MemberStoreSearchListAdapter imgAdapter;
	
	public ArrayList<CheckMileageMerchants> entries1 = new ArrayList<CheckMileageMerchants>();	// 1�������� ��ȸ�� ���. (������ �� ���� ����)   // �����.
	ArrayList<CheckMileageMerchants> entries2 = new ArrayList<CheckMileageMerchants>();			// �߶� ���ϴ� �κ�.
	List<CheckMileageMerchants> entriesFn = new ArrayList<CheckMileageMerchants>();			// ���� ���⹰
	
	
	float fImgSize = 0;			// �̹��� ������ ���庯��.
	int isRunning = 0;			// ���� ���� ����. ���� �߿� �ٸ� ���� ��û�� ���� ���, �����Ѵ�.
	View emptyView;				// ������ ���� ��

	// �����
	ProgressBar pb1;		// �ߴ� �ε� �����
	ProgressBar pb2;		// �ϴ� �߰� �����
	
	// ListView�� �Ѹ� Data �� ���� ���ǳ� �����͵�. --> ���߿� ���� ����Ͽ� ó���� �����ͼ� ����� ������ �Ѵ�.
	String[] areas = {"������", "ȫ��", "����", "������", "�Ÿ�", "����", "����", "�Ǵ�", "���", "���з�", "���ǵ�"};			// ���߿� ��ȸ �� �� ��..
	String[] jobs = {"", ""};
	String[] tmpJobs = null;
	GridView gridView;
	
	// �ڵ鷯
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Bundle b = msg.getData();
			try{
				if(b.getInt("showYN")==1){		// �޾ƿ� ���ϸ��� ����� ȭ�鿡 �ѷ��ش�.
					Log.d(TAG,"showYN");
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. ���� ����Ʈ ���̾ƿ�.
					if((entriesFn!=null)&&(entriesFn.size()>0)){
//						Log.e(TAG,"indexDataFirst::"+indexDataFirst);
						if(newSearch){		// ���ο� �˻��� ��� ���� ����, �߰��� ��� �˸��� �ϱ� ����.
							setGriding();
							newSearch = false;		// �ٽ� �������´�. ������ ���� �˻��� �ƴ�.
						}else{
//							Log.e(TAG,"notifyDataSetChanged");
							imgAdapter.notifyDataSetChanged();		// �˸� -> ��������� ȭ��� ������Ʈ �ǵ�����.
						}
						gridView.setEnabled(true);			// �׸��� �� �����.
					}else{
						Log.d(TAG,"no data");
						if(gridView==null){
							gridView  = (GridView)findViewById(R.id.gridview);
						}
						emptyView = findViewById(R.id.empty1);		// ������ ������ '�� ������'(������ ���� �޽���)ǥ��
						gridView.setEmptyView(emptyView);
						gridView.setVisibility(8);			//   0 visible   4 invisible   8 gone
						emptyView.setVisibility(0);
					}
					adding = false;		// ��ȸ �� �߰� ������. �ٸ��� ��ȸ�� �� �߰� ����.. (��ũ�� �����ʸ� �ٷ궧 ���)
					// �ϴ� �ε��ٸ� �����.
					hidePb2();
					isRunning = 0;		// ���������� ����. - ���� �߰� �������� �� ��ȸ ����.
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
				
				if(b.getInt("order")==3){
					// �ϴ� ���α׷����� ����
					if(pb2==null){
						pb2=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);
					}
					pb2.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==4){
					// �ϴ� ���α׷�����  ����
					if(pb2==null){
						pb2=(ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);
					}
					pb2.setVisibility(View.INVISIBLE);
				}
				if(b.getInt("showErrToast")==1){			// �Ϲ� ���� �佺Ʈ
					Toast.makeText(MemberStoreListPageActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("showNetErrToast")==1){			// ��Ʈ��ũ ���� �佺Ʈ
					Toast.makeText(MemberStoreListPageActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
				}
				if(b.getInt("setJobsList")==1){			// ���� ��� ���������� ���ǳʿ� ����
					jobs = tmpJobs;
					// ���ǳ� ������ ����. 
					 ArrayAdapter<String> aa2 =  new ArrayAdapter<String>(getThis(), android.R.layout.simple_spinner_item, jobs);
					 aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					 searchSpinnerType.setAdapter(aa2);
					 jobKindSearched = true;			// ���� �˻� ��.
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
		
		// �� QR �ڵ�. 
		myQRcode = MyQRPageActivity.qrCode;		
		entriesFn = new ArrayList<CheckMileageMerchants>();
		
		parentLayout = findViewById(R.id.member_store_list_parent_layout);		// �θ� ���̾ƿ�- �����ʸ� �޾Ƽ� Ű���� �ڵ� ���迡 ���
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 	// ����Ű���� �ݱ�����
		
		// �θ� ���̾ƿ� ������ - �ܺ� ��ġ �� Ű���� ���� �뵵
	    parentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Log.w(TAG,"parentLayout click");
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm .hideSoftInputFromWindow(searchText.getWindowToken(), 0);
			}
		});
	    
	    
		// ũ�� ����
		float screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		Log.i("screenWidth : ", "" + screenWidth);
		float screenHeight = this.getResources().getDisplayMetrics().heightPixels;
		Log.i("screenHeight : ", "" + screenHeight);
		if(screenWidth < screenHeight ){fImgSize = screenWidth;
	    }else{fImgSize = screenHeight;}
		
		// progress bar
		pb1 = (ProgressBar) findViewById(R.id.memberstore_list_ProgressBar01);		// �ε�(�߾�)
		pb2 = (ProgressBar) findViewById(R.id.memberstore_list_ProgressBar02);		// �ε�(�ϴ�)
		
		searchText = (TextView) findViewById(R.id.store_search_text);			//�˻���
		searchBtn = (Button) findViewById(R.id.store_search_btn);				//�˻���ư
		searchText.setOnEditorActionListener(this);  
		searchText.addTextChangedListener(textWatcherInput);  

		// spinner
		searchSpinnerType = (Spinner)findViewById(R.id.searchSpinnerType);
		// spinner listener
		searchSpinnerType.setOnItemSelectedListener(this);
		
		searchBtn.setOnClickListener(new Button.OnClickListener()  {
			public void onClick(View v)  {
				goSearch();		 // �ܾ�� �˻� ���� 
			}
		});
	}
    
	// �����͸� ȭ�鿡 ����
	public void setGriding(){
		imgAdapter = new MemberStoreSearchListAdapter(this, entriesFn);
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
//				// img �� ���ڿ��� �ٲ㼭 �ִ´�. ������ ������.			 // BMP -> ���ڿ� 		 // �����ϰ� ������ �̹����� �ٸ��Ƿ� ���� ����.
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();   
//				String bitmapToStr = "";
//				entriesFn.get(position).getMerchantImage().compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object    
//				byte[] b = baos.toByteArray();  
//				bitmapToStr = Base64.encodeToString(b, Base64.DEFAULT); 
//				intent.putExtra("imageFileStr", bitmapToStr);	
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
//			Log.d(TAG,"indexDataFirst:"+indexDataFirst+"/indexDataLast:"+indexDataLast+"/indexDataTotal:"+indexDataTotal);
			if((indexDataFirst + indexDataLast < indexDataTotal)||(indexDataLast!=indexDataTotal)){	// ���� ���� �Ǵ� ���� �������� ����.
				mIsLast = false;
			}
			if((totalItemCount<10) ||(indexDataLast==indexDataTotal)){		// 10�� ����(�̴̹� ������) �Ǵ� ������=��ü (���� ����)
				mIsLast = true;
			}
//			Log.d(TAG,"adding:"+adding+",mIsLast:"+mIsLast);
			//			  if(indexDataFirst==indexDataLast){		// ������ �� ũ�� ���� �ִ°�
			//				  mIsLast = true;
			//			  }
			// ����Ʈ ���� �ϴܿ� �������� ���.. 
//			if(firstVisibleItem+visibleItemCount==totalItemCount &&(!adding)&&(!mIsLast)){			// ���� �ϴ�.
			if(firstVisibleItem+visibleItemCount>=(totalItemCount-2) &&(!adding)&&(!mIsLast)){		// ���� �ϴ� -2�϶� �̸�����? - ���� ������.
//				Log.e(TAG, "onScroll event Occured."+"//view::"+view+"//firstVisibleItem::"+firstVisibleItem+"//visibleItemCount:"+visibleItemCount+"//totalItemCount::"+totalItemCount);
				showPb2();
				indexDataTotal = entries1.size();
				Log.d(TAG,"onScroll indexDataTotal:"+indexDataTotal);
				new backgroundGetMerchantInfo().execute();		// �񵿱� ����
			}
		}
		// ��ũ�� ����, ��, ��ũ�� ��.. �̶�� ����� �˼� �ִ�. ��� �ʿ� ����..  --> �ʿ�����.. ��ũ���� ��ȸ�� ������ �߻��ϱ� ������ ��ũ�� �߿��� ��ȸ�� ���� �ʵ��� �Ѵ�.. 
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
	
	// �ߴ� ���α׷����� ����, ����
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
	// �ϴ� ���α׷����� ����, ����
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
	public void showMSG(){			// ȭ�鿡 error �佺Ʈ ���..
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
	 * �˻��� ���� ������ ��������Ʈ�� �����´�.
	 *   ������ �̸� : checkMileageBusinessKind
	 *   ��Ʈ�ѷ� : checkMileageBusinessKindController
	 *   �޼ҵ� : selectBusinessKindList
	 *   �ʿ� �Ķ���� : countryCode / languageCode / activateYn
	 *   ���� �ΰ��� ����Ͽ��� ���� ������ ����Ѵ�. ��Ƽ��� Y ���� ���.
	 *   ��� �� : List<checkMileageBusinessKind>  �� content �� ����Ѵ�.
	 */
	public void getBusinessKindList(){
		if(CheckNetwork()){
			Log.i(TAG, "getBusinessKindList");
			// �ε����Դϴ�..  
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
//			searchSpinnerType.setEnabled(false);			// handler ���� ó�� 		b.putInt("enableOrDisable", 2);
//			searchText.setEnabled(false); 
//			searchBtn.setEnabled(false);
			
			controllerName = "checkMileageBusinessKindController";
			methodName = "selectBusinessKindList";
			
			// locale get
			systemLocale = getResources().getConfiguration(). locale;
			//      strDisplayCountry = systemLocale.getDisplayCountry();
			strCountry = systemLocale .getCountry();
			strLanguage = systemLocale .getLanguage();
			
			// ���� ��ź�
			new Thread(
					new Runnable(){
						public void run(){
							JSONObject obj = new JSONObject();
							try{
								obj.put("countryCode", strCountry);		// ���� �ڵ�
								obj.put("languageCode", strLanguage);			// ����ڵ�
								obj.put("activateYn", "Y");
								Log.w(TAG,"countryCode::"+strCountry+",languageCode:"+strLanguage);
							}catch(Exception e){
								e.printStackTrace();
							}
							String jsonString = "{\"checkMileageBusinessKind\":" + obj.toString() + "}";
							
							try{
								postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
								connection2 = (HttpURLConnection) postUrl2.openConnection();
								connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
								connection2.setDoOutput(true);
								connection2.setInstanceFollowRedirects(false);
								connection2.setRequestMethod("POST");
								connection2.setRequestProperty("Content-Type", "application/json");
								connection2.connect();		// *** 
								Thread.sleep(200);
								OutputStream os2 = connection2.getOutputStream();
								os2.write(jsonString.getBytes("UTF-8"));
								os2.flush();
								Thread.sleep(200);
//								System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
								responseCode = connection2.getResponseCode();
								os2.close();
								if(responseCode==200||responseCode==204){
									InputStream in =  connection2.getInputStream();
									// ��ȸ�� ����� ó��.
									setBusinessKindList(in);
								}
								connection2.disconnect();
							}catch(Exception e){ 
								e.printStackTrace();
								connection2.disconnect();
								if(reTry>0){
									reTry = reTry-1;
									try {
										Thread.sleep(100);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									getBusinessKindList();
								}else{
									Log.w(TAG,"reTry failed. -- init reTry");
									reTry = 1;	
									showMSG();
//									searchSpinnerType.setEnabled(true);
//									searchText.setEnabled(true); 
//									searchBtn.setEnabled(true);
									showInfo();		// �ڵ鷯���� �Բ� ó��
								}
							}
						}
					}
			).start();
		}
	}
	
	/*
	 * ������ ���� 1�� ������ ����. entries �����ο� ����. ���� url ������ ���� �̹����� �޾ƿ��� �Լ��� ȣ���Ѵ�.
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
//		Log.d(TAG,"����::"+builder.toString());
		String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�
		JSONArray jsonArray2 = null;
		try {
			jsonArray2 = new JSONArray(tempstr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		int max = jsonArray2.length();
		Log.d(TAG,"max:"+max);				// 0 ��° ��� ���� ����.
		try {
			tmpJobs = new String[max];		// 0��° ���ŷ� max+1 --> max
//			tmpJobs[0] = "��� ����";			// ���߿� �ٲ�� �ϴµ�.. �ٱ����. *** 		--> 0��° ��� ���� �׸� ����
			if(max>0){
				for ( int i = 0; i < max; i++ ){
					JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageBusinessKind");		// ��ҹ��� ����
					tmpJobs[i] = jsonObj.getString("content");		// 0 ��° �׸� "��� ����" ���� --> i+1 --> i
				}
			}else{
				tmpJobs = new String[1];
				tmpJobs[0] = "Not Available";				// �˻� �Ұ�. (�������� �޾ƿ� ���� ������ 0����.)
			}
			
			isRunning = 0;			// �ٸ� �˻� ����.
			new Thread(
					new Runnable(){
						public void run(){
							Message message = handler.obtainMessage();					// ���� ��� �޾ƿ°� ���ǳʿ� ����..
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
	 * ������ ����Ͽ�   ������ ����� �����´�. ���� ��ȸ.			 ��ȸ 1.
	 * �� ����� List<CheckMileageMerchant> Object �� ��ȯ �Ѵ�.  
	 * 
	 *  ȣ�� ��� :: 
	 * checkMileageMerchantController // selectSearchMerchantList // checkMileageMerchant 
	 * 
	 *  ������ ���� :  businessArea01 / businessKind03 / activateYn							// �����ڵ�, ����ڵ�..
	 * 	
	 *	�޴� ����.
	 *  merchantId // companyName / // profileImageUrl / 
	 *  workPhoneNumber / zipCode01 / address01/ address02 / businessArea01 // latitude // longitude
	 * 
	 *  ��ġ�ϸ� ������ �������� �����ϱ� ������ Ű�� �ʿ��ϴ�..  merchantId ������..	
	 *  
	 */
	public void getMemberStoreList() throws JSONException, IOException {
		if(CheckNetwork()){
			Log.i(TAG, "getMemberStoreList");
			controllerName = "checkMileageMerchantController";
			methodName = "selectSearchMerchantList";
			indexDataFirst = 0; // ȭ�� ù �� �ε���. �ʱ�ȭ.. �ٽ� 0����
			indexDataLast = 0;	// ȭ�鿡 �������� ���� �ε���. �Բ� �ʱ�ȭ.. 0����
			entriesFn.clear();		// �̰͵� �ʱ�ȭ �غ���. ȭ�鿡 �������� ������ ����Ʈ.
			newSearch = true;			// ���ο� �˻���. true ��� ���� �����ʹ� ��������.
//			mIsLast = false;		// �ʱ�ȭ. ���� �ƴϹǷ� �߰� ����.
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
//				searchSpinnerArea.setEnabled(false);
				new Thread(	
						new Runnable(){
							public void run(){
								Message message = handler.obtainMessage();
								Bundle b = new Bundle();
								b.putInt("enableOrDisable", 2);
								message.setData(b);
								handler.sendMessage(message);
							}
						}
				).start();
//				searchSpinnerType.setEnabled(false);
//				searchText.setEnabled(false); 
//				searchBtn.setEnabled(false);
				// ���� ��ź�
				new Thread(
						new Runnable(){
							public void run(){
								JSONObject obj = new JSONObject();
								try{
									obj.put("activateYn", "Y");
//									obj.put("businessArea01", searchWordArea);		// ����		  
									obj.put("businessKind03", searchWordType);		// ����					// ���� ��ȣ ��������, �� ���̵� �ʿ�...
									obj.put("checkMileageId", myQRcode);			// �� ���̵�
									obj.put("companyName", searchText.getText());			// �� ���̵�
									
//									Log.w(TAG,"myQRcode::"+myQRcode+",searchWordArea:"+searchWordArea+",searchWordType:"+searchWordType+",companyName:"+searchText.getText());
									Log.w(TAG,"myQRcode::"+myQRcode+",searchWordType:"+searchWordType+",companyName:"+searchText.getText());
								}catch(Exception e){
									e.printStackTrace();
								}
								String jsonString = "{\"checkMileageMerchant\":" + obj.toString() + "}";
								try{
									postUrl2 = new URL("http://"+serverName+"/"+controllerName+"/"+methodName);
									connection2 = (HttpURLConnection) postUrl2.openConnection();
									connection2.setDoOutput(true);
									connection2.setInstanceFollowRedirects(false);
									connection2.setRequestMethod("POST");
									connection2.setRequestProperty("Content-Type", "application/json");
									connection2.connect();		// *** 
									Thread.sleep(200);
									OutputStream os2 = connection2.getOutputStream();
									os2.write(jsonString.getBytes("UTF-8"));
									os2.flush();
	//								System.out.println("postUrl      : " + postUrl2);
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
									connection2.disconnect();
								}catch(Exception e){ 
									e.printStackTrace();
									connection2.disconnect();
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
									if(reTry>0){
										try{
											Log.w(TAG,"failed, retry all again. remain retry : "+reTry);
											reTry = reTry -1;
											Thread.sleep(200);		// ��õ�?
											getMemberStoreList();
										}catch(Exception e2){}
									}else{
										Log.w(TAG,"reTry failed. -- init reTry");
										try{
											reTry = 2;	
										}catch(Exception e1){
											e1.printStackTrace();
										}
										new Thread(	
												new Runnable(){
													public void run(){
														Message message = handler.obtainMessage();
														Bundle b = new Bundle();
														b.putInt("enableOrDisable", 1);
														message.setData(b);
														handler.sendMessage(message);
													}
												}
										).start();
//										searchSpinnerType.setEnabled(true);
//										searchText.setEnabled(true); 
//										searchBtn.setEnabled(true);
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
	 * ������ ���� 1�� ������ ����. entries �����ο� ����. ���� url ������ ���� �̹����� �޾ƿ��� �Լ��� ȣ���Ѵ�.
	 */
	public void theData1(InputStream in){
		Log.d(TAG,"theData");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
		StringBuilder builder = new StringBuilder();
		String line =null;
		reTry = 3;			
		try {
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.d(TAG,"����::"+builder.toString());
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
		Log.w(TAG,"indexDataTotal=max::"+max);
		try {
			if(max>0){
				entries1 = new ArrayList<CheckMileageMerchants>(max);
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
		}finally{
			new backgroundGetMerchantInfo().execute();	// getMerchantInfo(entries1); �� �񵿱�� ����
		}
	}

	
	// ������ URL�� �̹��� ��������.������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�. + ������� ���ϱ�			-- 2�� �˻�
	public void getMerchantInfo(){
		try{
			Log.i(TAG, "merchantInfoGet   indexDataLast:"+indexDataLast+",indexDataTotal:"+indexDataTotal);
			// ������ �ε���+10���� ��ü �������� Ŀ���� ��ü ���� ������.
			if(indexDataLast+10>=indexDataTotal){
				indexDataLast = indexDataTotal;
//				mIsLast = true;
				Log.d(TAG,"indexDataLast:"+indexDataLast+",indexDataTotal:"+indexDataTotal );
			}else{		// ��ü �������� �۴ٸ� 10��. �߰� ����.
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
			
			if(indexDataFirst+10>indexDataLast){	// ���������� �����ߴٸ� ��������ȣ.
				indexDataFirst = indexDataLast;
			}else{									// ���������� �������� �ʾҴٸ� +10
				indexDataFirst = indexDataFirst + 10;
			}
//			Log.d(TAG,"������ ���� ���� �Ϸ�. ");
			showInfo();
		}catch(Exception e){
			e.printStackTrace();
			hidePb2();
			adding = false;
		}
	}

	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.
	public void showInfo(){
		Log.d(TAG, "showInfo");
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
			Log.w(TAG,"MalformedURLException");
		} catch (IOException e) {
			Log.w(TAG,"IOException");
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
//		// rotate the Bitmap ȸ�� ��Ű���� �ּ� ����!
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
		// ������ ���� ��� ��������.
		if((!jobKindSearched) && (isRunning==0)){				// ���� �˻��� �Ϸ���� �ʾҰ�, �������� �۾��� ���� ���.
			isRunning = 1;		// ���� ���� ���� (�ٸ� ���� �ź�)
			showPb();
//			getBusinessKindList();
			new backgroundGetBusinessKindList().execute();			// �񵿱�� ��ȯ
		}
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
			Log.d(TAG,"kill all");
//			mainActivity.finish();
			dummyActivity.finish();		// ���̵� ����
			DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
			finish();
		}else{
			app_end = 1;
			Toast.makeText(MemberStoreListPageActivity.this, R.string.noti_back_finish, Toast.LENGTH_SHORT).show();
			new Thread( 
					new Runnable(){
						public void run(){
							try {
								Thread.sleep(3000);		// 3���� ����. �ٽ� �ڷ� ���� �������� ���� ���� Ȯ��.
								app_end = 0;
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
			).start();
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
			gridView  = (GridView)findViewById(R.id.gridview);
			gridView.setEnabled(false);					// �׸��� �� ��� ����. �˻� ���� ���� �˻� ����Ʈ�� ��ũ���ϸ� ���� ���� �����. -- �ε��� ���� ����.
			Log.i(TAG,"searchSpinnerJobs//"+jobs[arg2]);
			
			searchWordType = jobs[arg2];
			try {
				getMemberStoreList();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// ���ǳ� �ȹٲٸ� ����x
	}
	
	public class backgroundGetMerchantInfo extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetMerchantInfo");
			Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast+"/adding:"+adding);
			if(indexDataTotal==0){				// ��ü ������ 0�� ���..  ������ -> "�����ϴ�"
				showInfo();
			}else{								// ������ 0�� �ƴҶ�.
				if(!((indexDataTotal<indexDataFirst)||(indexDataTotal<indexDataLast))){		// �������� ���
					if(!adding){
						adding = true;
						getMerchantInfo();						// �����͸� ������
					}
				}else{
					indexDataLast = indexDataTotal;				// ���������� ���. ������ �����Ͱ� �ִ밪�� �Ѱ�����.
					Log.w(TAG, "indexDataTotal::"+indexDataTotal+"//indexDataFirst::"+indexDataFirst+"//indexDataLast::"+indexDataLast);
				}
			}
			return null; 
		}
	}
	// �񵿱�� ���� ��� ��������.
	public class backgroundGetBusinessKindList extends  AsyncTask<Void, Void, Void> { 
		@Override protected void onPostExecute(Void result) {  
		} 
		@Override protected void onPreExecute() {  
		} 
		@Override protected Void doInBackground(Void... params) {  
			Log.d(TAG,"backgroundGetBusinessKindList");
			getBusinessKindList();
			return null; 
		}
	}
	
	/*
	 * ��Ʈ��ũ ���� ����
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
//			AlertShow("Wifi Ȥ�� 3G ���� ������� �ʾҰų� �������� �ʽ��ϴ�. ��Ʈ��ũ Ȯ�� �� �ٽ� ������ �ּ���.");
			Log.d(TAG,"1");
			if(gridView==null){
				gridView  = (GridView)findViewById(R.id.gridview);
			}
			gridView.setEnabled(true);
			searchSpinnerType.setEnabled(true);
			searchText.setEnabled(true); 
			searchBtn.setEnabled(true);
			
			new Thread( 
					new Runnable(){
						public void run(){
							Message message = handler .obtainMessage();
							Bundle b = new Bundle();
							b.putInt( "showNetErrToast" , 1);
							message.setData(b);
							handler .sendMessage(message);
						}
					}
			).start();
//			Log.i(TAG,"AlertShow_networkErr");
//			AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
//			alert_internet_status.setTitle("Warning");
//			alert_internet_status.setMessage(R.string.network_error);
//			alert_internet_status.setPositiveButton(R.string.closebtn, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
////					finish();
//				}
//			});
//			alert_internet_status.show();
////			AlertShow_networkErr();
			
			// ���� ����. �˻� �����ϵ���. 
			connected = false;
			isRunning = 0;		// ���߿� ��õ� �����ϵ���.
		}else{
			connected = true;
		}
		return connected;
	}
//	public void AlertShow_networkErr(){
//	}
	
	@Override
	public void onPause(){
		super.onPause();
			searchText.setText("");
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//����Ű���� ����
	}


	public void goSearch(){		// �ܾ� �˻� ����
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0); 		//����Ű���� ����
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
		             if(event.getAction() == KeyEvent.ACTION_DOWN)  		// ���ͽÿ��� �˻� ����
		             {  
		            	 String searchTxt =  searchText.getText()+"";
//		            	 searchTxt = searchTxt.substring(0, searchTxt.length()-1);	// ���� �߶�?
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
//		alert_internet_status.setPositiveButton("�ݱ�", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
////				finish();
//			}
//		});
//		alert_internet_status.show();
//	}

		    @Override
			public void onDestroy(){
				super.onDestroy();
				try{
					if(connection2!=null){
						connection2.disconnect();
					}
				}catch(Exception e){}
			}
}
