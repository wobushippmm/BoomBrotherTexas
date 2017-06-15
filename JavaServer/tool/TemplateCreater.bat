java -jar TemplateCreater.jar template template
ping -n 1 127.0>nul
xcopy /y /e /h template\*.java ..\src\template\
ping -n 1 127.0>nul
xcopy /y /e /h template\*.xml ..\src\template\
pause