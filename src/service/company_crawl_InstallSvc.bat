set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.6.0_30
set SysPath=E:\crawl

SET jvmdll=%JAVA_HOME%\jre\bin\server\jvm.dll
SET toolsjar=%JAVA_HOME%\lib\tools.jar
SET dtjar=%JAVA_HOME%\lib\dt.jar
set jtds=%SysPath%\lib\mysql-connector-java-5[1].0.4-bin.jar
set ibatis=%SysPath%\lib\ibatis-2.3.4.726.jar
set cc=%SysPath%\lib\commons-codec-1.3.jar;
set ch=%SysPath%\lib\commons-httpclient.jar;
set cl=%SysPath%\lib\commons-logging-1.1.1.jar;
set d=%SysPath%\lib\dom4j-1.6.1.jar;
set h=%SysPath%\lib\htmlparser.jar;
set log4j=%SysPath%\lib\log4j-1.2.11.jar;
set trs=%SysPath%\lib\trsbean.jar;
set bot=%SysPath%\lib\bbbot.jar;
set n=%SysPath%\lib\nekohtml.jar;
set x=%SysPath%\lib\xalan-2.7.1.jar;
set xe=%SysPath%\lib\xercesImpl.jar;
set xa=%SysPath%\lib\xml-apis.jar

JavaService -install companyCrawlService "%jvmdll%" -Djava.class.path="%dtjar%";"%ibatis%";"%toolsjar%";"%jtds%";"%xa%";"%xe%";"%x%";"%n%";"%bot%";"%trs%";"%cc%";"%ch%";"%cl%";"%d%";"%h%";"%log4j%";%SysPath% -start edu.xmu.zj.process.TaskAutoRun -out %SysPath%\log\log.txt -err %SysPath%\log\log.txt -current %SysPath%
net start companyCrawlService
pause