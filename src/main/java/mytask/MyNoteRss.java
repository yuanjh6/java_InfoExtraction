package mytask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import infoextra.rss.Task;

public class MyNoteRss {
	private static Logger logger = LogManager.getLogger(MyNoteRss.class.getName());
	public static void main(String[] args) {

		Task task = new Task();
		if (!task.setConfig("mytasks/mynoterss.xml")) {
			logger.error("task配置文件读取失败");
			return;
		}
		logger.info("配置文件读取完成");
		task.runOneByOne();
		logger.info("完成任务");
	}
}
