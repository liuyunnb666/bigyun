@echo off
echo.
echo [信息] 使用 Jar 启动 File 服务。
echo.

cd %~dp0
cd ../bigyun-modules/bigyun-file/bigyun-file-service/target

set JAVA_OPTS=-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -Dfile.encoding=utf-8 %JAVA_OPTS% -jar bigyun-file-service.jar

cd bin
pause
