package com.kr.bettersoft.domain;
/*
 * 서버에서 받아온 설정 정보를 모바일에..
 * 
 * 서버에서 받아온 정보중 사용할 것  = EMAIL  // BIRTHDAY //  GENDER // RECEIVE_NOTIFICATION_YN
 * 
 */
public class CheckMileageMemberSettings {
	private String email;
	private String birthday;
	private String gender;
	private String receive_notification_yn;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getReceive_notification_yn() {
		return receive_notification_yn;
	}
	public void setReceive_notification_yn(String receive_notification_yn) {
		this.receive_notification_yn = receive_notification_yn;
	}
	
	
}
