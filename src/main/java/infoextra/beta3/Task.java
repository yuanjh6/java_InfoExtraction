package infoextra.beta3;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Task {

    private static Logger logger = LogManager.getLogger(Task.class.getName());
    private String taskConfig;
    private XmlConfig config;
    private List<Map<String, String>> resultsList;
    private List<Seed> seedsList;

    // private String LOGFILE = "config/task_log";

    public static void main(String[] args) {
        Task task = new Task();
        if (!task.setConfig("mytasks/test.xml")) {
            logger.error("task配置文件读取失败");
            return;
        }
        logger.info("task读入配置文件完成");
        task.run();
        logger.info("完成任务");
        // for (Map<String, String> map : task.getUpdateResults()) {
        // System.out.println(map);
        // }
    }


    public Task() {
        this.resultsList = new ArrayList<Map<String, String>>();
        this.seedsList = new ArrayList<Seed>();
    }

    public boolean setConfig(String taskConfig) {

        if (taskConfig == null || taskConfig.isEmpty()) {
            logger.error("配置文件路径为空，请检查");
            return false;
        }
        if (!new File(taskConfig).exists()) {
            logger.error("配置文件不存在，请检查");
            return false;
        }
        // 载入配置文件
        this.taskConfig = taskConfig;
        this.config = new XmlConfig(this.taskConfig);

        // 载入种子信息到种子map
        for (Map<String, String> seedConfig : this.config.getSeeds()) {
            if (!seedConfig.containsKey("seedEnable")
                    || !seedConfig.containsKey("seedType")) {
                logger.error("必要参数缺失，无法初始化种子");
            }
            // 种子有效性判断
            if (seedConfig.get("seedEnable").equalsIgnoreCase("false"))
                continue;

            // 判断种子类型，不同初始化方式,默认为page模式
            String type = seedConfig.get("seedType");

            Seed seed = null;
            if (type != null && !type.isEmpty()) {
                if (type.equalsIgnoreCase("page")) {
                    seed = new Page();
                } else if (type.equalsIgnoreCase("rss")) {
                    seed = new Rss();
                } else {
                    logger.info("无法识别的类型，请检查" + " type:" + type);
                    continue;
                }
            } else {
                seed = new Page();
            }

            if (seed != null && seed.setConfig(seedConfig)) {
                this.seedsList.add(seed);
            }
        }
        return true;
    }

    public void run() {

        this.resultsList = this.getUpdateResults();
        this.cleanResults(this.resultsList);
        if (this.storeResults(this.resultsList)) {
            this.updateMark();
        }
        this.config.save();
    }

    //
    // private void storeUpdateResults() {
    // BufferedWriter bw;
    // try {
    // bw = new BufferedWriter(new FileWriter(LOGFILE, true));
    //
    // for (Seed seed : this.seeds) {
    // List<Map<String, String>> result = seed.getUpdateResults();
    // if (result != null) {
    // // this.cleanResults(result);
    // this.storeResults(result);
    // String markValue = seed.updateMark();
    // bw.write(seed.name() + ":" + seed.failPage() + "\n");
    // bw.flush();
    // this.config.update(seed.name(), markValue);
    // this.config.save();
    // }
    // }
    // bw.close();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }

    /**
     * @return 获取更新的结果结合
     */
    private List<Map<String, String>> getUpdateResults() {
        List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
        for (Seed seed : this.seedsList) {

            List<Map<String, String>> result = seed.getUpdateResults();

            if (result != null) {
                resultsList.addAll(result);
                logger.info("种子:" + seed.name() + " 更新结果条数:" + result.size());
            }
        }
        logger.info("总更新结果数量:" + resultsList.size());
        return resultsList;
    }

    /**
     * @param resultsList 带处理的结果集合
     */
    private void cleanResults(List<Map<String, String>> resultsList) {

        // 清除已经抓取但是数据库中不存在的字段(只保留需要写入数据库的字段)
        String tableField = this.config.getTableField();
        Set<String> tableFieldSet = new HashSet<String>();
        if (tableField != null && !tableField.isEmpty()) {
            for (String s : tableField.split(",")) {
                if (s != null && !s.isEmpty()) {
                    tableFieldSet.add(s.trim());
                }
            }
            // 清除map中不存在于tableFieldSet的字段
            for (Map<String, String> map : resultsList) {
                if (map == null || map.isEmpty())
                    continue;
                // 求差集
                Set<String> difSet = new HashSet<String>(map.keySet());
                difSet.removeAll(tableFieldSet);
                for (String s : difSet) {
                    map.remove(s);
                }
            }
        }
        // 字段长度限制
        for (Map<String, String> map : resultsList) {
            if (map == null || map.isEmpty())
                continue;
            if (map.containsKey("description")) {
                String tmps = map.get("description");
                if (tmps.length() > 511) {
                    tmps = tmps.substring(0, 511);
                    map.put("description", tmps);
                }
            }
            if (map.containsKey("link")) {
                String tmps = map.get("link");
                if (tmps.length() > 127) {
                    tmps = tmps.substring(0, 127);
                    map.put("link", tmps);
                }
            }
        }

    }

    private boolean storeResults(List<Map<String, String>> resultsList) {
        try {
            Class.forName(this.config.dbDriver());
            Connection conn = DriverManager.getConnection(this.config.dbUrl());

            Statement sta = conn.createStatement();
            String sql;
            int e = 0, r = 0;
            for (Map<String, String> map : resultsList) {
                if (map == null)
                    continue;
                String[] s = mapToStr(map);
                if (s == null)
                    continue;
                sql = "insert into " + this.config.tableName() + "(" + s[0]
                        + ") values " + "(" + s[1] + ");";

                try {
                    if (sta.executeUpdate(sql) > 0) {
                        r++;
                    } else {
                        logger.debug("sql语句执行失败:" + sql);
                        e++;
                    }
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    e++;
                }

            }
            logger.info("成功插入数据:" + r + " 插入数据失败:" + e);
            sta.close();
            conn.close();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 调用config方法更新配置文件中的结果最新标记
     */
    private void updateMark() {
        for (Seed seed : this.seedsList) {
            // xml文件中的mark字段更新
            String markValue = seed.updateMark();
            // seed-class中的变量更新
            if (markValue != null && !markValue.isEmpty()) {
                this.config.update(seed.name(), markValue);
            }

        }

    }

    private static String[] mapToStr(Map<String, String> map) {
        String[] tmpret = new String[2];
        if (map == null) {
            logger.error("参数Map为空指针");
            return null;
        }

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        String tmp1 = "", tmp2 = "";
        for (Map.Entry<String, String> entry : entrySet) {

            if (entry.getKey().trim().length() == 0 || entry.getValue() == null
                    || entry.getValue().trim().length() == 0)
                continue;
            tmp1 += "," + entry.getKey();
            tmp2 += "," + "\"" + entry.getValue().replace("\"", "'") + "\"";
        }
        if (tmp1.length() < 1 || tmp2.length() < 1)
            return null;

        tmpret[0] = tmp1.substring(1);
        tmpret[1] = tmp2.substring(1);

        return tmpret;
    }

    public static String urlFormat(String url) {
        if (url == null || url.length() == 0)
            return null;
        url = url.trim();
        if (url.startsWith("http://"))
            url = url.substring(7);
        if (url.contains("/"))
            url = url.substring(0, url.indexOf("/"));
        if (url.contains("?"))
            url = url.substring(0, url.indexOf("?"));
        return url;
    }

}
