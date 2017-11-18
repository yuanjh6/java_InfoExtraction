本项目下所有文件均采用utf-8编码(项目默认编码为UTF8格式)
包括：源代码文件，配置文件，seed文件夹下的种子文件

很久之前的代码了，只有功能没有格式化，也 没有javadoc等规范，有兴趣的可以自行扩展

代码说明：
常规java项目（没有依赖maven,但是文件结构像maven因为自己想转为maven但是找jar包太痛苦了，下载速度慢不说，很多老旧jar尝试多次都不对），
所以jar包需要自己放到lib文件夹下，考虑到github空间有限，jar包将会另外提供

main/java/mytask
DownloadAdver
MyNoteRss
MyNoteStore
这三个类核心代码如下：
task.setConfig("mytasks/downloadAd.xml"))
task.run();
程序主框架在task中
task完成功能(infoextra/group(rss)/task.java)
1，从初始化的xml中读取信息提取配置。
2，根据配置的节点数据获取对应的内容
3，结果汇总存储数据库
4，输出获取的总结果个数
xml中会配置信息的类型（rss或者page（普通网页）），对page类型还需要配置各种信息的正则表达式
task会根据xml的信息类型来new Rss() 或new Page(),将相应信息传入关联的对象。
Rss()和Page()均实现了接口seed，为了屏蔽内部差异，对外提供统一接口服务。

GetPageInfo：独立测试代码，获取网页content并且根据正则表达式提取出对应内容。

阅读时可以先阅读infoextra/group的代码(infoextra/rss代码和group类似，后面做了微调优化)
java/myclass:和此项目关联较低，是自己写的一些工具类，现在看来太烂了，能不看就别看了吧。
java/mytask:
DownloadAdver
MyNoteRss
MyNoteStore
这三个文件前面说明过了，使用xml初始化了具体的task任务而已。
getPageinfo不用看。

java/mytask/stock
提取股票的基本信息（公司名称，代码，上市时间等）。分红数据，相关新闻和研报数据等。
这些信息定制化比较强，并且像新闻数据都是从多个站点采集而来所以并未使用框架程序，等价于独立开发的。

java/proxy
代理服务器模块，自动读取代理服务器配置，自动尝试连接，超过一定次数标记为不可用状态。

java/infoextra/beta1,beta2,beta3：废弃代码，不用看

config/,seed/里面是各种信息提取的配置文件，大多配置对应代码是beta1，2，3等旧代码的了，所以也不太容易看懂。

mytasks/里面配置文件较新
举例：
   <group> 
    <enable>true</enable>  	#在使用中，false时跳过，不采集
    <groupName>Job</groupName>  #分组的名称
    <page>			#组的第一个采集项目，page代表普通网页
      <enable>true</enable>	#在使用中
      <name>DajieCampus</name>	#名称，大街
      <url>http://campus.dajie.com/campusIndex</url>#地址
      <charset>utf-8</charset>#网页字符集，不填也可，可自动探测（根据head的charset如果没有使用charset包的探测工具）
      <enableJs>true</enableJs>#是否需要js支持，不需要使用jsoup做网页提取（and解析），需要使用htmlunit做网页提取
      <elementSelect>ul.project-list&gt;li</elementSelect>#用于识别各个子条目，用来split
      <elementInfo>
        <title>a.link,text</title>#标题的jsoup的提取语法（js定位语法）
        <link>a.link,attr,href</link>#链接的jsoup语法
        <description/>#描述信息提取
        <pubdate>em.time,text</pubdate>  #时间信息提取
        <author/>#作者信息提取
      </elementInfo>
      <markValue>http://www.dajie.com/corp/1000051/project/17396</markValue>#上次提取到的最新信息的url，为空时从数据库select出最新信息
      <finish>true</finish>  #是否完成提取，任务可能中断，finish为true是markvalue才有效。
      <baseUrl/>
    </page>  
<rss> 
      <enable>true</enable>  #有效，需要提取
      <name>商业价值杂志</name>  #名称
      <url>http://content.businessvalue.com.cn/feed</url>  #rss地址
      <markValue>http://content.businessvalue.com.cn/post/13989.html</markValue>  #上次提取到的最新url
      <finish>true</finish>#完成标记
    </rss>
 </group> 




