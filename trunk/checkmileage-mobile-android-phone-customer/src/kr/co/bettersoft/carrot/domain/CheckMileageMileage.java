package kr.co.bettersoft.carrot.domain;

/*
 * ������ ����, �ش� �������� ���� ���ϸ����� �����Ƿ� �� ���ϸ��� ��Ͽ� ���ȴ�.
 * 
 * ������ �̹���, �̸��� �����Ƿ�, ������ ��� ���⿡���� ��� �ȴ�.
 * 
 * ������ �̹����� ������ ������ �޾ƿ��� �� �Ŀ� �� �� ������ ID �� ���� 2�� �˻��� �Ͽ� �����´�.
 */

import android.graphics.Bitmap;

public class CheckMileageMileage {
	private String idCheckMileageMileages;						// �����ĺ� ��ȣ. key
	private String mileage;										// �������� ���� ���ϸ���
	private String activateYN;									// Y 
	private String modifyDate;									// ������
	private String registerDate;								// �����
	private String checkMileageMembersCheckMileageID;			// ����� ID 
	private String checkMileageMerchantsMerchantID;				// ������ ID
	private String introduction;				// ������ ID
	
	private String workPhoneNumber;
	
	private String merchantName;						// ������ �̸�
	private String merchantImg;							// ������ �̹��� URL
	private Bitmap merchantImage;						// ������ �̹���
	
	public String getIdCheckMileageMileages() {			// ���� �ĺ� ��ȣ
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
		this.idCheckMileageMileages = idCheckMileageMileages;		// Ű �� --> �α׺��� �ʿ�
		this.mileage = mileage;										// ���ϸ���
		this.modifyDate = modifyDate;								// ������
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;	// �� ���̵�
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;		// ������ ���̵�.
		
		this.merchantName = companyName;
		this.introduction = introduction;
		this.workPhoneNumber = workPhoneNumber;
		this.merchantImg = profileThumbnailImageUrl;
		this.merchantImage = merchantImage;
	}
}