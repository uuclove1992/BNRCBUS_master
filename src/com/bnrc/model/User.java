package com.bnrc.model;

public class User {

	// �û�ID
	public int uId;
	// ��¼�û���
	public String name;
	// �û�����
	public String password;
	// �û�����
	public String email;
	// �û�Ȩ��,0��ʾ����Ա��1��ʾ��Ա��2��ʾѧԱ,3�н�
	public int rights;
	// ��¼����
	public int loginCount;
	// ��ʾ����
	public String question;
	// �����
	public String answer;
	// �Ƿ���ͷ��
	public int hasPhoto;
	// ͷ���ַ
	public String photoUrl;
	// ����ʱ��
	private String createTime;
	// ѧ�֣������Ǽ�
	public int point;
	// �ȼ��֣���Ա�ȼ�
	public int level;
	// �Ƿ���֤ 0δ��֤��1����֤
	public int auth;

	public User() {

	}

	public User(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public int getuId() {
		return uId;
	}

	public void setuId(int uId) {
		this.uId = uId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getRights() {
		return rights;
	}

	public void setRights(int rights) {
		this.rights = rights;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getHasPhoto() {
		return hasPhoto;
	}

	public void setHasPhoto(int hasPhoto) {
		this.hasPhoto = hasPhoto;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAuth() {
		return auth;
	}

	public void setAuth(int auth) {
		this.auth = auth;
	}
	
}
