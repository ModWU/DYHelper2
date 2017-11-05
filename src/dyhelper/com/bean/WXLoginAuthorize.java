package dyhelper.com.bean;
/**
 * <font style="color:red"><strong>Œ¢–≈ ⁄»®ÃÂ</strong></font>
 * @author Administrator-2016/9/26
 */
public class WXLoginAuthorize {
	private String access_token;
	private int expires_in;
	private String refresh_token;
	private String openid;
	private String scope;
	public WXLoginAuthorize(String access_token, int expires_in, String refresh_token, String openid, String scope) {
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.refresh_token = refresh_token;
		this.openid = openid;
		this.scope = scope;
	}
	
	public WXLoginAuthorize(String access_token, int expires_in, String refresh_token, String openid) {
		this(access_token, expires_in, refresh_token, openid, null);
	}
	
	public WXLoginAuthorize() {
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public int getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Override
	public String toString() {
		return "access_token: " + access_token + "\r\n" +
			   "expires_in: " + expires_in + "\r\n" +
		       "refresh_token: " + refresh_token + "\r\n" +
		       "openid: " + openid + "\r\n" +
		       "scope: " + scope;
	}
	
}
