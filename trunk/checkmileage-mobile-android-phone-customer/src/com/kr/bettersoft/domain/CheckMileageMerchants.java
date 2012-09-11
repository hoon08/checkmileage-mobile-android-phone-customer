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
	private String merchantId;	// 가맹점아이디
	
	private String name;								// 대표자 이름
	private String companyName;							// 가맹점 이름
	
	private String workPhoneNumber;						// 가맹점 전화번호
	
	private String address01;							// 가맹점 주소
	private String address02;		
	
	private String latitude;							// 가맹점 좌표1
	private String longtitude;							// 가맹점 좌표2
	
	private String profileImageURL;						// 프로필 이미지 URL
	private Bitmap merchantImage;						// 프로필 이미지
	
	private String prSentence;							// 가맹점 자랑
	
	private String checkMileageMembersCheckMileageID;			// 마일리지 등록에 대한 고유 ID 
	private String mileage;										// 가맹점에 대한 마일리지
	
	private String activateYN;									// Y 
	
	private String modifyDate;									// 수정일
	private String registerDate;								// 등록일
	
	// 가맹점 상세보기로 갈때 내 마일리지, 내 마일리지에 대한 고유 아이디가 필요하다.(내아디-가맹점아디-마일리지 매핑 정보의 인덱스값)  mileage  idCheckMileageMileages
	private String idCheckMileageMileages;				// 고유 식별 번호
	
	public CheckMileageMerchants(String merchantId, String companyName, String profileImageURL, String idCheckMileageMileages, String mileage) 
	{
		this.merchantId = merchantId;		// 키 값 --> 로그볼때 필요
		this.companyName = companyName;										// 마일리지
		this.profileImageURL = profileImageURL;	
		this.idCheckMileageMileages = idCheckMileageMileages;
		this.mileage = mileage;
	}
	
	public CheckMileageMerchants(String merchantId, String companyName, String profileImageURL, String idCheckMileageMileages, String mileage, Bitmap merchantImage) 
	{
		this.merchantId = merchantId;		// 키 값 --> 로그볼때 필요
		this.companyName = companyName;										// 마일리지
		this.profileImageURL = profileImageURL;	
		this.idCheckMileageMileages = idCheckMileageMileages;
		this.mileage = mileage;
		this.merchantImage = merchantImage;
	}
	
	
	public CheckMileageMerchants() {
		// TODO Auto-generated constructor stub
	}
	public String getMerchantID() {
		return merchantId;
	}
	public void setMerchantID(String merchantID) {
		this.merchantId = merchantID;
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
	public String getAddress02() {
		return address02;
	}
	public void setAddress02(String address02) {
		this.address02 = address02;
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
	public String getCheckMileageMembersCheckMileageID() {
		return checkMileageMembersCheckMileageID;
	}
	public void setCheckMileageMembersCheckMileageID(
			String checkMileageMembersCheckMileageID) {
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;
	}
	public String getMileage() {
		return mileage;
	}
	public void setMileage(String mileage) {
		this.mileage = mileage;
	}
	public String getActivateYN() {
		return activateYN;
	}
	public void setActivateYN(String activateYN) {
		this.activateYN = activateYN;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getIdCheckMileageMileages() {
		return idCheckMileageMileages;
	}
	public void setIdCheckMileageMileages(String idCheckMileageMileages) {
		this.idCheckMileageMileages = idCheckMileageMileages;
	}
	
	
}
