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
	private String merchantId;	// ���������̵�
	
	private String name;								// ��ǥ�� �̸�
	private String companyName;							// ������ �̸�
	
	private String workPhoneNumber;						// ������ ��ȭ��ȣ
	
	private String address01;							// ������ �ּ�
	private String address02;		
	
	private String latitude;							// ������ ��ǥ1
	private String longtitude;							// ������ ��ǥ2
	
	private String profileImageURL;						// ������ �̹��� URL
	private Bitmap merchantImage;						// ������ �̹���
	
	private String prSentence;							// ������ �ڶ�
	
	private String checkMileageMembersCheckMileageID;			// ���ϸ��� ��Ͽ� ���� ���� ID 
	private String mileage;										// �������� ���� ���ϸ���
	
	private String activateYN;									// Y 
	
	private String modifyDate;									// ������
	private String registerDate;								// �����
	
	// ������ �󼼺���� ���� �� ���ϸ���, �� ���ϸ����� ���� ���� ���̵� �ʿ��ϴ�.(���Ƶ�-�������Ƶ�-���ϸ��� ���� ������ �ε�����)  mileage  idCheckMileageMileages
	private String idCheckMileageMileages;				// ���� �ĺ� ��ȣ
	
	public CheckMileageMerchants(String merchantId, String companyName, String profileImageURL, String idCheckMileageMileages, String mileage) 
	{
		this.merchantId = merchantId;		// Ű �� --> �α׺��� �ʿ�
		this.companyName = companyName;										// ���ϸ���
		this.profileImageURL = profileImageURL;	
		this.idCheckMileageMileages = idCheckMileageMileages;
		this.mileage = mileage;
	}
	
	public CheckMileageMerchants(String merchantId, String companyName, String profileImageURL, String idCheckMileageMileages, String mileage, Bitmap merchantImage) 
	{
		this.merchantId = merchantId;		// Ű �� --> �α׺��� �ʿ�
		this.companyName = companyName;										// ���ϸ���
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
