package com.kr.bettersoft.domain;


// 가맹점 상세 정보 받기 용도.
import android.graphics.Bitmap;

public class CheckMileageMerchants {
	/*
	 * 보내는 정보 : 가맹점 아이디
	 *   	checkMileageMileage :: activateYn, checkMileageMembersCheckMileageId
	 *  받는 정보 : 
	 *    가맹점 이름, 가맹점 이미지URL, 가맹점에 대한 내 마일리지.    // 마일리지 없음.따로 조회.또는 전달?
	 *     대표자 이름 , 전화번호 1, 주소 1, 
	 *      기타 설명들, 좌표(1,2),  
	 *    @[가맹점아이디] 는 가장 처음 가져오므로 따로 저장
	 */
	private String merchantID;	// 가맹점아이디
	
	private String name;								// 대표자 이름
	private String companyName;							// 가맹점 이름
	private String workPhoneNumber;						// 가맹점 전화번호
	private String address01;							// 가맹점 주소
	private String latitude;							// 가맹점 좌표1
	private String longtitude;							// 가맹점 좌표2
	private String profileImageURL;						// 프로필 이미지 URL
	private Bitmap merchantImage;						// 프로필 이미지
	private String prSentence;							// 가맹점 자랑
	
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
