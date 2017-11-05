package dyhelper.com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;
import dyhelper.com.services.ProgressUpdate;

public class FileUtils {
	private final static String TAG = "FileUtils";
	private String SDPath;
	private ProgressUpdate progressUpdate;

	public String getSDPath() {
		return SDPath;
	}

	public void setSDPath(String sDPath) {
		SDPath = sDPath;
	}

	public FileUtils(ProgressUpdate progressUpdate) {
		// 获取当前外部存储设备的目录
		SDPath = Environment.getExternalStorageDirectory() + "/";
		this.progressUpdate = progressUpdate;
	}

	// 在sd卡的路径下创建文件
	public File createSDFile(String filename) throws IOException {
		File file = new File(filename);
		file.createNewFile();
		return file;
	}

	// 在sd卡的路径下创建目录
	public File createSDDir(String dirname) {
		File dir = new File(dirname);
		dir.mkdir();
		return dir;
	}

	// 判断文件是否已经存在
	public String isFileExist(String filename) {
		File file = new File(filename);
		if (file.exists())
			return file.getAbsolutePath();
		return null;
	}

	// 将一个InputStream里的数据写进SD卡/或者手机存储
	public File inputSD(String fileName, InputStream inputstream, int len) {
		int count = 0;
		int curlen = 0;
		File file = null;
		OutputStream outputstream = null;
		// boolean sdCardExist = Environment.getExternalStorageState()
		// .equals(android.os.Environment.MEDIA_MOUNTED);

		// if(!sdCardExist){
		// return file;
		// }

		try {
			// createSDDir(path);
			file = createSDFile(fileName);
			outputstream = new FileOutputStream(file, true);
			byte[] buffer = new byte[1024];
			while ((curlen = inputstream.read(buffer)) != -1) {
				outputstream.write(buffer, 0, curlen);
				count += curlen;
				Log.i("qhb", "downLoad " + count);
				this.progressUpdate.setProgress(count, len);
				if (count >= len) {
					break;
				}
			}
			outputstream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				outputstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}
}