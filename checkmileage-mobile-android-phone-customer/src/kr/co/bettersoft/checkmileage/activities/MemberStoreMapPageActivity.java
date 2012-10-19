package kr.co.bettersoft.checkmileage.activities;
// ������ �� - ���� ����
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MemberStoreMapPageActivity extends MapActivity {
	OverlayItem overlayitem;
	private Drawable marker = null;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();

	MapView mMap;
	MyLocationOverlay2 mLocation;
	String latatude = "";
	String longitude = "";
	int myLat = 0;
	int myLon = 0;

	int storeLat = 0;
	int storeLon = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_store_map);

		Intent rIntent = getIntent();
		latatude = rIntent.getStringExtra("latatude");
		longitude = rIntent.getStringExtra("longitude");
		if(latatude.length()>0 && longitude.length()>0){
			storeLat = Integer.parseInt(latatude);
			storeLon = Integer.parseInt(longitude);
		}else{
			Toast.makeText(MemberStoreMapPageActivity.this, R.string.no_saved_shop_info, Toast.LENGTH_SHORT).show();
			finish();
		}


		mMap=(MapView)findViewById(R.id.mapview);
		final MapController mapControl = mMap.getController();
		mapControl.setZoom(13);
		mMap.setBuiltInZoomControls(true);

		mLocation = new MyLocationOverlay2(this, mMap); 

		List<Overlay> overlays = mMap.getOverlays();
		Drawable marker=getResources().getDrawable(R.drawable.pos);
		marker.setBounds(0,0,marker.getIntrinsicWidth() ,marker.getIntrinsicHeight());


		overlays.add(mLocation);

		mMap.getOverlays().add(new SitesOverlay(marker));

		mLocation.runOnFirstFix(new Runnable() {
			public void run() {
				// ������ �� ��ġ�κ���.
				GeoPoint pt = new GeoPoint(mLocation.getMyLocation().getLatitudeE6(), mLocation.getMyLocation().getLongitudeE6());				// ��ǥ�� ���� ����.!  �� ��ġ --> �ش� ��ġ�� ����.		
				mapControl.setCenter(pt);									// (����ڰ� ���� ȭ���� ��ǥ.)

				//                 mMap.getController().animateTo(mLocation.getMyLocation());		// ����ġ
				GeoPoint gp = new GeoPoint(storeLat,storeLon);
				mMap.getController().animateTo(gp);				//storeLat,storeLon

				myLat = mLocation.getMyLocation().getLatitudeE6();				// ����ġ�� ��ǥ ȹ�� *** �α׿�
				myLon = mLocation.getMyLocation().getLongitudeE6();
				Log.i("runOnFirstFix", "location:"+myLat+", "+myLon);			// 37529466 126921069

			}
		});
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	public void onResume(){
		super.onResume();
		mLocation.enableMyLocation();
		mLocation.enableCompass();
	}

	public void onPause(){
		super.onPause();
		mLocation.disableMyLocation();
		mLocation.disableCompass();
	}

	class MyLocationOverlay2 extends MyLocationOverlay{
		public MyLocationOverlay2(Context context,MapView mapView){
			super(context,mapView);
		}
		protected boolean dispatchTap(){	// �ȿ��־�� ����. ��ġ�� �佺Ʈ. 
			Toast.makeText(MemberStoreMapPageActivity.this, R.string.its_user_location, Toast.LENGTH_SHORT).show();
//			Toast.makeText(MemberStoreMapPageActivity.this, "���Ⱑ �������ġ�Դϴ�:"+myLat+","+myLon, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private Drawable marker = null;
		private List<OverlayItem> items = new ArrayList<OverlayItem>();

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;
			String shop_loc_msg = getString(R.string.its_shop_location);
			items.add(new OverlayItem(new GeoPoint(storeLat,storeLon), "Store", shop_loc_msg));
//			items.add(new OverlayItem(getPoint(40.748963847316034,-73.96807193756104), "UN", "United Nations"));
//			items.add(new OverlayItem(getPoint(40.76866299974387,-73.98268461227417), "Lincoln Center","Home of Jazz at Lincoln Center"));
//			items.add(new OverlayItem(getPoint(40.765136435316755,-73.97989511489868), "Carnegie Hall","Where you go with practice, practice, practice"));
//			items.add(new OverlayItem(getPoint(40.70686417491799,-74.01572942733765), "The Downtown Club","Original home of the Heisman Trophy"));
			// ������ OverlayItem �� ������� ����
			populate();
		}

		// ������ ��ȣ�� ���� OverlayItem�� return

		@Override
		protected OverlayItem createItem(int i) {
			return items.get(i);
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			// ��Ŀ�� �Ʒ��� �߰� �κ��� ��ǥ�� ��ġ�ϵ��� ����
			boundCenterBottom(marker);
		}



		@Override

		protected boolean onTap(int i) {
			Toast.makeText(MemberStoreMapPageActivity.this, items.get(i).getSnippet(),Toast.LENGTH_SHORT).show();
			return true;
		}

		//���̾ ó���� �� �ִ� �׸��� ������ ����
		@Override
		public int size() {
			return items.size();
		}
	}
	
}
