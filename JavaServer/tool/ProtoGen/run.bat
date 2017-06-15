::protogen.exe -i:protos\ReturnMessage.proto -o:cs\ReturnMessage.cs
::protogen.exe -i:protos\Login.proto -o:cs\Login.cs
protogen.exe -i:protos\GameDataV2.proto -o:cs\GameData.cs

pause