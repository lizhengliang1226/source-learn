@echo off

set LIB_DIR=..\lib
set MAIN_CLASS=com.lzl.datagenerator.DataGenerator

java -cp "%LIB_DIR%\*" %MAIN_CLASS%
pause