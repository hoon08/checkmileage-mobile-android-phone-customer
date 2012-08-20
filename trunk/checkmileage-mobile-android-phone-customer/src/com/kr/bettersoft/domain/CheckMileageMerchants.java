package com.kr.bettersoft.domain;


// ������ �� ���� �ޱ� �뵵.
import android.graphics.Bitmap;

public class CheckMileageMerchants {
	/*
	 * ������ ���� : ������ ���̵�
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  �޴� ���� : 
	 *    ������ �̸�, ������ �̹���URL, �������� ���� �� ���ϸ���.    // ���ϸ��� ����.���� ��ȸ.�Ǵ� ����?
	 *     ��ǥ�� �̸� , ��ȭ��ȣ 1, �ּ� 1, 
	 *      ��Ÿ �����, ��ǥ(1,2),  
	 *    @[���������̵�] �� ���� ó�� �������Ƿ� ���� ����
	 */
	private String merchantID;	// ���������̵�
	
	private String name;								// ��ǥ�� �̸�
	private String companyName;							// ������ �̸�
	private String workPhoneNumber;						// ������ ��ȭ��ȣ
	private String address01;							// ������ �ּ�
	private String latitude;							// ������ ��ǥ1
	private String longtitude;							// ������ ��ǥ2
	private String profileImageURL;						// ������ �̹��� URL
	private Bitmap merchantImage;						// ������ �̹���
	private String prSentence;							// ������ �ڶ�
	
	public String getMerchantID() {
		return merchantID;
	}
	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getWorkPhoneNumber() {
		return workPhoneNumber;
	}
	public void setWorkPhoneNumber(String workPhoneNumber) {
		this.workPhoneNumber = workPhoneNumber;
	}
	public String getAddress01() {
		return address01;
	}
	public void setAddress01(String address01) {
		this.address01 = address01;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongtitude() {
		return longtitude;
	}
	public void setLongtitude(String longtitude) {
		this.longtitude = longtitude;
	}
	public String getProfileImageURL() {
		return profileImageURL;
	}
	public void setProfileImageURL(String profileImageURL) {
		this.profileImageURL = profileImageURL;
	}
	public Bitmap getMerchantImage() {
		return merchantImage;
	}
	public void setMerchantImage(Bitmap merchantImage) {
		this.merchantImage = merchantImage;
	}
	public String getPrSentence() {
		return prSentence;
	}
	public void setPrSentence(String prSentence) {
		this.prSentence = prSentence;
	}
	
	
}
