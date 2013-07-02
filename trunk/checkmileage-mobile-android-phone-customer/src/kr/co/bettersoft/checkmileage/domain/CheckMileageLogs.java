package kr.co.bettersoft.checkmileage.domain;

/**
 * 일반 로그용 도메인 클래스이다.
 * @author blue
 *
 */

public class CheckMileageLogs {

	private String checkMileageId;	// 사용자 아이디
	private String viewName;		// 화면 명
	private String parameter01;		// 전번
	private String parameter04;		// 검색어
	public String getCheckMileageId() {
		return checkMileageId;
	}
	public void setCheckMileageId(String checkMileageId) {
		this.checkMileageId = checkMileageId;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getParameter01() {
		return parameter01;
	}
	public void setParameter01(String parameter01) {
		this.parameter01 = parameter01;
	}
	public String getParameter04() {
		return parameter04;
	}
	public void setParameter04(String parameter04) {
		this.parameter04 = parameter04;
	}
	
	public CheckMileageLogs(){		// constructor - default 값 설정
		this.checkMileageId = "";
		this.viewName = "";
		this.parameter01 = "";
		this.parameter04 = "";
	}
}
