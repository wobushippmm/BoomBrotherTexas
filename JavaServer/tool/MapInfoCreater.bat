java -jar MapInfoCreater.jar map/Map.png 20
ping -n 1 127.0>nul
xcopy /y /e /h map\*.map ..\src\map\
pause