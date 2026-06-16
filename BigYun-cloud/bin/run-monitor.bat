@echo off
echo.
echo [信息] 使用 Jar 启动 Monitor 服务。
echo.

cd %~dp0
cd ../bigyun-visual/bigyun-monitor/target

set JAVA_OPTS=-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -Dfile.encoding=utf-8 %JAVA_OPTS% -jar bigyun-visual-monitor.jar

cd bin
pause
