package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// �� ���ϸ��� ���� ȭ��


/*
 * �ƴ��͸� �����Ÿ� �Ἥ ������ �ö����� getView �Ѵ�.. ���߿� ���ľ� �ڴ�..
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
	int app_end = 0;	// �ڷΰ��� ��ư���� ������ 2������ ��������
	
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
	MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
	
	int responseCode = 0;
	String TAG = "MyMileagePageActivity";
	String myQRcode = "";
	String controllerName = "";
	String methodName = "";
	public List<CheckMileageMileage> entries;	// 1�������� ��ȸ�� ���. (������ �� ���� ����)
	int returnYN = 0;		// ������ ������ ���� �������� ���� �����뵵
	int flag = 0;
	
	
	/*  ���� ��� ��� ����
	private ArrayAdapter<String> m_adapter = null;
	private ListView m_list = null;
	ArrayAdapter<CheckMileageMileage> adapter = null;
	MyAdapter mAdapter;
*/ 

	List<CheckMileageMileage> entriesFn = null;
	float fImgSize = 0;
	int isRunning = 0;
	
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
					// ���� ��� �迭�� entriesFn �� ����Ǿ� �ִ�.. 
					
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
					
					/* ���� ���
					mAdapter = new MyAdapter(returnThis(), R.layout.my_mileage_list, (ArrayList<CheckMileageMileage>) entriesFn);		// entriesFn   dataArr
					m_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					m_list.setAdapter(mAdapter);
					*/
					
					isRunning = isRunning -1;
				}
				if(b.getInt("order")==1){
					// ���׹� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.VISIBLE);
				}else if(b.getInt("order")==2){
					// ���׹� ����
					if(pb1==null){
						pb1=(ProgressBar) findViewById(R.id.ProgressBar01);
					}
					pb1.setVisibility(View.INVISIBLE);
				}
			}catch(Exception e){
//				Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�."+entriesFn.size(), Toast.LENGTH_SHORT).show();
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
				intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(position).getCheckMileageMerchantsMerchantID());		// ������ ���̵�
				intent.putExtra("idCheckMileageMileages", entriesFn.get(position).getIdCheckMileageMileages());		// ���� �ĺ� ��ȣ. (�󼼺��� ��ȸ�뵵)
				intent.putExtra("myMileage", entriesFn.get(position).getMileage());									// �� ���ϸ���    // �������� ���� �� ���ϸ���
				startActivity(intent);
			}
		});
	}
	
	/*   // ���� ����� ������� ����.
	// ����� Ŭ����. �̰����� ���� �����͸� �� ���̵� ���� �����Ѵ�.
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
			ImageView leftImg = (ImageView)convertView.findViewById(R.id.merchantImage);		// ������ �̹��� �ְ�
			// set the Drawable on the ImageView
			if(myDataArr.get(position).getMerchantImage()!=null){
				BitmapDrawable bmpResize = BitmapResizePrc(myDataArr.get(position).getMerchantImage(), fImgSize/2, fImgSize/2);  
				leftImg.setImageDrawable(bmpResize);	
			}
				
//			leftImg.setImageBitmap(myDataArr.get(position).getMerchantImage());			
			
			TextView nameTv = (TextView)convertView.findViewById(R.id.merchantName);			// ������ �̸� �ְ�
			nameTv.setText(myDataArr.get(position).getMerchantName());
			TextView mileage = (TextView)convertView.findViewById(R.id.mileage);				// �������� ���� �� ���ϸ��� �ְ�		.. �� ������ ������ �Ʒ��� �߰�, XML ���Ͽ��� �� ���..
			mileage.setText(myDataArr.get(position).getMileage()+"��");					
			
			TextView workPhone = (TextView)convertView.findViewById(R.id.merchantPhone);				// ������ ����.
			workPhone.setText(myDataArr.get(position).getWorkPhoneNumber());		
			
//			Button btn = (Button)convertView.findViewById(R.id.sendBtn);		// �ϴ� ��ư �־ Ŭ���� ��¼��..
//			btn.setOnClickListener(new Button.OnClickListener()  {
//				public void onClick(View v)  {
//					String str = myDataArr.get(pos).name + "���� ��ȭ��ȣ�� [ "+
//					                                                   myDataArr.get(pos).phone+" ] �Դϴ�.";
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
//		final ProgressDialog dialog= ProgressDialog.show(MyMileagePageActivity.this, "Ÿ��Ʋ","�޽���",true);
////		b. Dialog�� ȭ�鿡�� �����ϴ� �ڵ带 �ۼ��Ѵ�. ���� ��� 3���� �ִٰ� ���̾�α׸� ���ְ� �ʹٸ�...
//		new Thread(new Runnable() {
//		public void run() {
//		try { Thread.sleep(3000); } catch(Exception e) {}
//		dialog.dismiss();
//		}
//		});

		myQRcode = MyQRPageActivity.qrCode;			// �� QR �ڵ�. (Ȯ�ο�)
		
		// ũ�� ����
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
		
		/* ���� ���
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
			Log.e(TAG, "�̹� ������..");
		}
	}

	
	/* ���� ����� ������� ����
	
	AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			// ���๮
//			Toast.makeText(MyMileagePageActivity.this, "��ġ��ġ"+arg2+"�̰���:"+entriesFn.get(arg2).getCheckMileageMerchantsMerchantID(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MyMileagePageActivity.this, MemberStoreInfoPage.class);
			intent.putExtra("checkMileageMerchantsMerchantID", entriesFn.get(arg2).getCheckMileageMerchantsMerchantID());		// ������ ���̵�
			intent.putExtra("idCheckMileageMileages", entriesFn.get(arg2).getIdCheckMileageMileages());					// ���� �ĺ� ��ȣ
			intent.putExtra("myMileage", entriesFn.get(arg2).getMileage());			// �������� ���� �� ���ϸ���
			startActivity(intent);
		}
	};

*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	          
	          
	          
	
	
	
	
	
	
	
	
	/*
	 * ������ ����Ͽ� �� ���ϸ��� ����� �����´�.
	 * �� ����� List<CheckMileageMileage> Object �� ��ȯ �Ѵ�.
	 * 
	 * ������ ���� : ��Ƽ����ƮY, ��QR�ڵ� ��Ʈ��
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  �޴� ���� : ������ ��� �̹��� , ������ �̸�, �ش� �������� ���� �� ���ϸ���, ������ ��� �Ͻ�, 
	 *  ��ġ�ϸ� ������ �������� �����ϱ� ������ Ű�� �ʿ��ϴ�..
	 *  
	 * -----------------------------------
	 * |[�̹��� ��]  [������ �̸�]  [�� ����Ʈ] |
	 * |[�̹��� ��]	[ �� �� �� �� �� �� �� ]    |
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
							// �ڽ��� ���̵� �־ ��ȸ
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
							System.out.println("responseCode : " + connection2.getResponseCode());		// 200 , 204 : ����
							responseCode = connection2.getResponseCode();
							InputStream in =  connection2.getInputStream();
							// ��ȸ�� ����� ó��.
							theData1(in);
						}catch(Exception e){ 
							e.printStackTrace();
							// �����ϱ� �ε��� ���ְ� �ٽ� �Ҽ� �ֵ���
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
		int doneCnt = 0;
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
		if(responseCode==200 || responseCode==204){
			try {
				entries = new ArrayList<CheckMileageMileage>(max);
				if(max>0){
					for ( int i = 0; i < max; i++ ){
						doneCnt++;
						JSONObject jsonObj = jsonArray2.getJSONObject(i).getJSONObject("checkMileageMileage");
						//  idCheckMileageMileages,  mileage,  modifyDate,  checkMileageMembersCheckMileageID,  checkMileageMerchantsMerchantID
						// ��ü ����� �� ������ �־ ����..  ���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�.
						entries.add(new CheckMileageMileage(jsonObj.getString("idCheckMileageMileages"),
								jsonObj.getString("mileage"),jsonObj.getString("modifyDate"),
								jsonObj.getString("checkMileageMembersCheckMileageId"),jsonObj.getString("checkMileageMerchantsMerchantId")
						));
						
					}
					//    			 2�� �۾�. ������ �̸�, �̹��� �����ͼ� �߰��� ����.
					//    			 array ä�� �ѱ�� ���������� �ֵ��� �Ѵ�..
				}
			} catch (JSONException e) {
				doneCnt--;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				Log.e(TAG, ""+doneCnt);
				if(doneCnt>0){
					getMerchantInfo(entries,max);
				}else{		// ������ ��� �ε��� ������
					entriesFn = entries;
					showInfo();
				}
			}
		}else{			// ��û ���н�	 �佺Ʈ ���� ȭ�� ����.
			Toast.makeText(MyMileagePageActivity.this, "������ �߻��Ͽ����ϴ�.\n��� �� �ٽ� �õ��Ͽ� �ֽʽÿ�.", Toast.LENGTH_SHORT).show();
		}
	}

	// ������ ���̵�� ������ ���� ��������. .. Arrayä�� �ְ� �ޱ�..
	public void getMerchantInfo(final List<CheckMileageMileage> entries3, int max){
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
		Log.i(TAG, "merchantInfoGet");
		final ArrayList<CheckMileageMileage> entries2 = new ArrayList<CheckMileageMileage>(max);
		final int max2 = max;
		// ������ ���ؼ� ������.
		new Thread(
				new Runnable(){
					public void run(){
						for (int j = 0; j < max2; j++ ){
							// ������ ���̵� ������.
							final String merchantId2 = entries3.get(j).getCheckMileageMerchantsMerchantID();
							// ��û�� ���ڿ��� ����� ����. (json ������� ������ ���� ����)
							JSONObject obj = new JSONObject();
							try{
								// ���� ������ ����
								obj.put("activateYn", "Y");
								obj.put("merchantId", merchantId2);
								Log.i(TAG, "merchantId::"+merchantId2);
							}catch(Exception e){
								e.printStackTrace();
							}
							// ���� ���ڿ�. (���� json ����� ������Ʈ�� ���ڿ���)
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
								InputStream in =  connection2.getInputStream();
								// ������ ���̵�� ������ ������ �����°� ó��..���尪: �ε�����ȣ, ������¥, ���̵�, ���������̵�. + ������ �̸�, ������ �̹��� URL
								BufferedReader reader = new BufferedReader(new InputStreamReader(in));	
								StringBuilder builder = new StringBuilder();
								String line =null;
								while((line=reader.readLine())!=null){
									builder.append(line).append("\n");
								}
								Log.d(TAG,"����::"+builder.toString());
								String tempstr = builder.toString();		// ���� �����͸� �����Ͽ� ����� �� �ִ�... �뵵�� �°� ������ ��.
								JSONObject jsonObject;	// 1���� ������.
								JSONObject jsonObject2;	// 1���� �������� "���������̵�" ���� ������. --> ���⼭ ��Ʈ������ �� �ϳ��� ������.
								if(connection2.getResponseCode()==200 || connection2.getResponseCode()==204){		// ��û ������
									jsonObject = new JSONObject(tempstr);
									jsonObject2 = jsonObject.getJSONObject("checkMileageMerchant");
									entries3.get(j).setMerchantName(jsonObject2.getString("companyName"));// ������ ������ �޴´�. �̸�
									if(jsonObject2.getString("workPhoneNumber")==null || jsonObject2.getString("workPhoneNumber").length()<1){	// ������ ������ �޴´�. ����
										entries3.get(j).setWorkPhoneNumber("");// ������ ������ �޴´�. ����
									}else{
										entries3.get(j).setWorkPhoneNumber("(��)"+jsonObject2.getString("workPhoneNumber"));
									}
									// ������ �̹��� URL �����Ѵ�.
									if(jsonObject2.getString("profileImageUrl").length()>0){
										entries3.get(j).setMerchantImg(jsonObject2.getString("profileImageUrl"));
									}else{
										entries3.get(j).setMerchantImg("http://www.carsingh.com/img/noImage.jpg");		// ���ͳݿ��� �ۿ� �⺻ �̹��� url --> ���߿� ������ url �Ǵ� �̹������Ϸ� ������ ��.
									}
									
									// ������ �̹��� URL�κ��� �̹��� �޾ƿͼ� �����ο� �����Ѵ�.
									Bitmap bm = LoadImage(entries3.get(j).getMerchantImg());
									// bm �̹��� ũ�� ��ȯ .
									BitmapDrawable bmpResize = BitmapResizePrc(bm, fImgSize/4, fImgSize/4);  
									entries3.get(j).setMerchantImage(bmpResize.getBitmap());
								}
							}catch(Exception e){ 
								e.printStackTrace();
							}
						}		// for�� ����
						Log.d(TAG,"������ ���� ���� �Ϸ�. ");
						entriesFn = entries3;
						showInfo();
					}
				}
		).start();
	}

	// entries3 �� ������ ������ ������ �̿��Ͽ� ������. ȭ�鿡 �����ش�.
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
		
		
		//  ������ ������ ȭ�鿡 �����ֱ�.
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
	 *  �ݱ� ��ư 2�� ������ ���� ��.(non-Javadoc)
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
			Toast.makeText(MyMileagePageActivity.this, "�ڷΰ��� ��ư�� �ѹ��� ������ ����˴ϴ�.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	////////////////////////   �ϵ���� �޴� ��ư.  ////////////////
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        menu.add(Menu. NONE, Menu.FIRST+1, Menu.NONE, "���� ��ħ" );             // �űԵ�� �޴� �߰�.
//	          getMenuInflater().inflate(R.menu.activity_main, menu);
	        return (super .onCreateOptionsMenu(menu));
	    }
	   
	    // �ɼ� �޴� Ư�� ������ Ŭ���� �ʿ��� �� ó��
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item){
	      return (itemCallback(item)|| super.onOptionsItemSelected(item));
	    }
	   
	    // ������ ���̵� �� ���� �ʿ��� �� ó��
	    public boolean itemCallback(MenuItem item){
	      switch(item.getItemId()){
	      case Menu. FIRST+1:
//	    	  Toast.makeText(MyMileagePageActivity.this, "123123", Toast.LENGTH_SHORT).show();
//	                Intent intent = new Intent(UserManagementActivity.this,AddUserActivity.class);        // example���� �̸�
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
	  			Log.e(TAG, "�̹� ������..");
	  		}
	             return true ;
	      }
	      return false;
	    }
	
	////////////////////////////////////////////////////////////
	
	
	
}
