@echo off
echo.
echo [INFO] Package BigYun Cloud services with dev profile.
echo.

%~d0
cd %~dp0

cd ..
call mvn clean package -Pdev -Dmaven.test.skip=true

pause
