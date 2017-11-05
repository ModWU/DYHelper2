package dyhelper.com.bean;

public class PageActLogInfo {
	private String timeStamp;
	private String functionId;
	private String remark;
	
	public PageActLogInfo() {}
	
	public PageActLogInfo(String timeStamp, String functionId, String remark) {
		this.timeStamp = timeStamp;
		this.functionId = functionId;
		this.remark = remark;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Override
	public String toString() {
		return "{\"TS\":\"" + timeStamp + "\", \"FI\":\"" + functionId + "\", \"R\":\"" + remark + "\"}";
	}
	
}
