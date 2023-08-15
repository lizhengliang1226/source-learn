@echo off

setlocal
echo ------------------------Hello Data Boot------------------------
cd ..\

set "CURRENT_DIR=%cd%"
set "CONF_DIR=%CURRENT_DIR%/config"
set "LIB_DIR=%CURRENT_DIR%/lib"
set "MAIN_CLASS=com.lzl.App"

echo CONF_DIR:%CONF_DIR%
echo LIB_DIR:%LIB_DIR%

:checkconfdir
if exist "%CONF_DIR%" goto checklibdir
echo config folder is not exist.Startup failed!
pause
exit

:checklibdir
if exist "%LIB_DIR%" goto boot
echo lib folder is not exist.Startup failed!
pause
exit

:boot
set "RUN_JAVA=java"
set "ARGS=%*"

set "STARTUP=%RUN_JAVA%"
set "STARTUP=%STARTUP% -cp .;lib/* %MAIN_CLASS% %ARGS%"

@echo/
@echo/

echo ----------------------Hello Data Start CMD---------------------
echo %STARTUP%

@echo/
@echo/
echo ----------------------Hello Data Starting...-------------------
title HELLO-DATA
%STARTUP%
pause
exit

:end
pause
exit