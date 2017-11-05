package dyhelper.com.bean;

import java.util.ArrayList;
import java.util.List;

public enum PhoneCarrier {
	CMCC {
		public List<String> getCodes() {
			List<String> codes = new ArrayList<String>();
			codes.add("00");
			codes.add("02");
			codes.add("07");
			return codes;
		}
	},
	TELECOM {
		public List<String> getCodes() {
			List<String> codes = new ArrayList<String>();
			codes.add("03");
			codes.add("05");
			codes.add("11");
			return codes;
		}
	},
	UNICOM {
		public List<String> getCodes() {
			List<String> codes = new ArrayList<String>();
			codes.add("01");
			codes.add("06");
			return codes;
		}
	},
	UNKNOWN {
		public List<String> getCodes() {
			List<String> codes = new ArrayList<String>();
			codes.add("xx");
			return codes;
		}
	};
	public abstract List<String> getCodes();
}
