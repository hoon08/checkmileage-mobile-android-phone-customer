package kr.co.bettersoft.checkmileage.domain;

/*
 * 가맹점 정보, 해당 가맹점에 대한 마일리지가 있으므로 내 마일리지 목록에 사용된다.
 * 
 * 가맹점 이미지, 이름이 있으므로, 가맹점 목록 보기에서도 사용 된다.
 * 
 * 가맹점 이미지는 가맹점 정보를 받아오고 난 후에 그 중 가맹점 ID 를 통해 2차 검색을 하여 가져온다.
 */

import android.graphics.Bitmap;

public class CheckMileageMileage {
	private String idCheckMileageMileages;						// 고유식별 번호. key
	private String mileage;										// 가맹점에 대한 마일리지
	private String activateYN;									// Y 
	private String modifyDate;									// 수정일
	private String registerDate;								// 등록일
	private String checkMileageMembersCheckMileageID;			// 사용자 ID 
	private String checkMileageMerchantsMerchantID;				// 가맹점 ID
	private String introduction;				// 가맹점 ID
	
	private String workPhoneNumber;
	
	private String merchantName;						// 가맹점 이름
	private String merchantImg;							// 가맹점 이미지 URL
	private Bitmap merchantImage;						// 가맹점 이미지
	
	public String getIdCheckMileageMileages() {			// 고유 식별 번호
		return idCheckMileageMileages;
	}
	public void setIdCheckMileageMileages(String idCheckMileageMileages) {
		this.idCheckMileageMileages = idCheckMileageMileages;
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
	public String getCheckMileageMembersCheckMileageID() {
		return checkMileageMembersCheckMileageID;
	}
	public void setCheckMileageMembersCheckMileageID(
			String checkMileageMembersCheckMileageID) {
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;
	}
	public String getCheckMileageMerchantsMerchantID() {
		return checkMileageMerchantsMerchantID;
	}
	public void setCheckMileageMerchantsMerchantID(
			String checkMileageMerchantsMerchantID) {
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;
	}
	
	
	public String getWorkPhoneNumber() {
		return workPhoneNumber;
	}
	public void setWorkPhoneNumber(String workPhoneNumber) {
		this.workPhoneNumber = workPhoneNumber;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getMerchantImg() {
		return merchantImg;
	}
	public void setMerchantImg(String merchantImg) {
		this.merchantImg = merchantImg;
	}
	public Bitmap getMerchantImage() {
		return merchantImage;
	}
	public void setMerchantImage(Bitmap merchantImage) {
		this.merchantImage = merchantImage;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public CheckMileageMileage(String idCheckMileageMileages, String mileage, String modifyDate, 
			String checkMileageMembersCheckMileageID, String checkMileageMerchantsMerchantID
			,String companyName,String introduction, String workPhoneNumber, String profileThumbnailImageUrl, Bitmap merchantImage){
		this.idCheckMileageMileages = idCheckMileageMileages;		// 키 값 --> 로그볼때 필요
		this.mileage = mileage;										// 마일리지
		this.modifyDate = modifyDate;								// 수정일
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;	// 내 아이디
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;		// 가맹점 아이디.
		
		this.merchantName = companyName;
		this.introduction = introduction;
		this.workPhoneNumber = workPhoneNumber;
		this.merchantImg = profileThumbnailImageUrl;
		this.merchantImage = merchantImage;
	}
}
