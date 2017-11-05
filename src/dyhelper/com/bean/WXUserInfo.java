package dyhelper.com.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import android.util.Log;

public class WXUserInfo {
	private String openid;
	private String nickname;
	private int sex;
	private String province;
	private String city;
	private String country;
	private String headimgurl;
	private String privilege[];
	private String unionid;
	
	
	
	public WXUserInfo() {
	}
	
	public WXUserInfo(WXUserInfo wxUserInfo) {
		openid = wxUserInfo.openid;
		nickname = wxUserInfo.nickname;
		sex = wxUserInfo.sex;
		province = wxUserInfo.province;
		city = wxUserInfo.city;
		country = wxUserInfo.country;
		
		headimgurl = wxUserInfo.headimgurl;
		privilege = wxUserInfo.privilege;
		unionid = wxUserInfo.unionid;
	}
	
	public WXUserInfo(String openid, String nickname, int sex, String province, String city, String country,
			String headimgurl, String[] privilege, String unionid) {
		this.openid = openid;
		this.nickname = nickname;
		this.sex = sex;
		this.province = province;
		this.city = city;
		this.country = country;
		this.headimgurl = headimgurl;
		this.privilege = privilege;
		this.unionid = unionid;
	}
	public void urlEncoding(String encoding) {
		if(encoding == null)
			encoding = "UTF-8";
		if(openid == null)
			openid = "";
		if(nickname == null)
			nickname = "";
		if(province == "")
			province = "";
		if(city == null)
			city = "";
		if(country == null)
			country = "";
		if(headimgurl == null) 
			headimgurl = "";
		if(privilege != null) {
			for(int i = 0; i < privilege.length; i++) {
				if(privilege[i] == null) {
					privilege[i] = "";
				}
			}
		}
		
		if(unionid == null)
			unionid = "";
		
		try {
			openid = URLEncoder.encode(openid, encoding);
			nickname = URLEncoder.encode(nickname, encoding);
			province = URLEncoder.encode(province, encoding);
			city = URLEncoder.encode(city, encoding);
			country = URLEncoder.encode(country, encoding);
			headimgurl = URLEncoder.encode(headimgurl, encoding);
			if(privilege != null) {
				for(int i = 0; i < privilege.length; i++) {
					privilege[i] = URLEncoder.encode(privilege[i], encoding);;
				}
			}
			unionid = URLEncoder.encode(unionid, encoding);
		} catch (UnsupportedEncodingException e) {
			Log.i("INFO", "WXUserInfo->±‡¬Î ß∞‹:" + encoding);
		}
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public String[] getPrivilege() {
		return privilege;
	}
	public void setPrivilege(String[] privilege) {
		this.privilege = privilege;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	
	@Override
	public String toString() {
		return  "openid: " + openid + "\r\n" +
				"nickname: " + nickname + "\r\n" +
			    "sex: " + sex + "\r\n" +
			    "province: " + province + "\r\n" +
			    "city: " + city + "\r\n" +
			    "country: " + country + "\r\n" +
			    "headimgurl: " + headimgurl + "\r\n" +
			    "privilege: " + Arrays.toString(privilege) + "\r\n" +
			    "unionid: " + unionid;
	}
	
}
