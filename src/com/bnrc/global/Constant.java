package com.bnrc.global;



public class Constant {
	
	public static final boolean DEBUG = true;
	public static final String sharePath = "chxue8_share";
    public static final String USERSID = "user";
    //ҳ��Ĭ����ʾ�Ͼ�����½����ʾע���û��ĳ���
    public static final String CITYID = "cityId";
    public static final String CITYNAME = "cityName";
    public static final String DEFAULTCITYID = "1001";
    public static final String DEFAULTCITYNAME = "�Ͼ�";
    
    //cookies
    public static final String USERNAMECOOKIE = "chxue8Name";
    public static final String USERPASSWORDCOOKIE = "chxue8Password";
    public static final String USERPASSWORDREMEMBERCOOKIE = "chxue8remember";
    
    // ���ӳ�ʱ
 	public static final int timeOut = 12000;
 	// ��������
 	public static final int connectOut = 12000;
 	// ��ȡ����
 	public static final int getOut = 60000;
 	
 	//1��ʾ���������
 	public static final int downloadComplete = 1;
 	//1��ʾδ��ʼ����
 	public static final int undownLoad = 0;
 	//2��ʾ�ѿ�ʼ����
 	public static final int downInProgress = 2;
 	//3��ʾ������ͣ
 	public static final int downLoadPause = 3;
 	
 	public static final String BASEURL = "http://www.chxue8.com/";
    public final static String ADURL = BASEURL + "PublicServlet?methodName=getSetting";
    
    public final static String ADDOVERLAYURL = BASEURL + "action/gfoverlay.do?methodName=addOverlayMobile";
}
