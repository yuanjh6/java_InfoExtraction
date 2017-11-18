package mytask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import infoextra.beta3.Task;


public class DownloadAdver {
	private static Logger logger = LogManager.getLogger(DownloadAdver.class.getName());
	public static void main(String[] args) {
		Task task = new Task();
		if (!task.setConfig("mytasks/downloadAd.xml")) {
			logger.error("task配置文件读取失败");
			return;
		}
		logger.info("配置文件完成");
		task.run();
		logger.info("完成任务");
	}
}
