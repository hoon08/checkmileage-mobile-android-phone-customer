package com.kr.bettersoft.domain;
/*
 * ������ �̿� ���� ��ȸ�Ҷ� ����ϴ� ������ Ŭ����.
 * 
 * ���ϸ��� ���̺� ����, ���� ���� �Ͼ�µ� �� ������,���̵� - ���� Ű ���� ����.
 *  
 *  ���⼭�� �� Ű ���� ���� �ش� ������-���̵� �� �߻��� ���ϸ��� ������ ��ȸ�ؼ� ����Ʈ�� ��ƿ´�.
 */
public class CheckMileageMemberMileageLogs {

	private String checkMileageId;		// �� �Ƶ�
	private String merchantId;			// ������ �Ƶ�
	private String content;				// ���� ������? �̿��Ѱ�.
	private String mileage;				// ����,����� ���ϸ���
//	private String activateYn;			// Ȱ�� ��Ȱ��
	private String modifyDate;			// ������  - ���o
	private String registerDate;		// ����� - ���x
	private String checkMileageMileagesIdCheckMileageMileages;		// Ű��. �߿�. Ű������ ��ȸ��.
	public String getCheckMileageId() {
		return checkMileageId;
	}
	public void setCheckMileageId(String checkMileageId) {
		this.checkMileageId = checkMileageId;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getMileage() {
		return mileage;
	}
	public void setMileage(String mileage) {
		this.mileage = mileage;
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
	public String getCheckMileageMileagesIdCheckMileageMileages() {
		return checkMileageMileagesIdCheckMileageMileages;
	}
	public void setCheckMileageMileagesIdCheckMileageMileages(String checkMileageMileagesIdCheckMileageMileages) {
		this.checkMileageMileagesIdCheckMileageMileages = checkMileageMileagesIdCheckMileageMileages;
	}
	public CheckMileageMemberMileageLogs(String checkMileageMileagesIdCheckMileageMileages, String content, String mileage, String modifyDate){
		this.checkMileageMileagesIdCheckMileageMileages = checkMileageMileagesIdCheckMileageMileages;
		this.content = content;
		this.mileage = mileage;
		this.modifyDate = modifyDate;
	}
}
