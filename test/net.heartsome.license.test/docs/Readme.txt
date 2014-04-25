使用说明：
1、将本工程导出为 JAR 包，其中 lib 目录可以不要导出；
2、复制导出的 JAR 包到 JMeter 的 /lib/ext 目录；
3、复制 lib 目录中除以 ApacheJMeter 开头的两个包之外的其他 JAR 包到 JMeter 的 /lib/ext 目录；
4、启动 JMeter 后，在测试计划 Test Plan 中添加线程组 Thread Group，并配置线程数、循环次数等；
5、在线程组中添加 Java Request，在 Classname 下拉列表中选择要测试的类，配置好相应的参数，如许可证 ID 等；
6、在线程组中添加 View Result Tree、Aggregate Report 等结果监听器；
7、可视需要添加多个线程组，同时跑不同的测试类；
8、运行测试。

可参考本目录中的测试计划样本：Java-Request-License.jmx
