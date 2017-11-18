package mytask;

import infoextra.group.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyNoteStore {
	private static Logger logger = LogManager.getLogger(MyNoteStore.class.getName());
	public static void main(String[] args) {
		Task task = new Task();
		if (!task.setConfig("mytasks/mynotestore.xml")) {
			logger.error("task配置文件读取失败");
			return;
		}
		logger.info("配置文件读取完成");
		task.runOneByOne();
		logger.info("完成任务");
	}
}
