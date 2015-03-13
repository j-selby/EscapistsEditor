@echo off
echo Escapists Map Editor builder
echo ============================

echo Building .jar...
cd %~dp0
cd ..
call mvn clean package

cd build
echo.
echo Building .exe...
"C:\Program Files (x86)\Launch4j\launch4jc.exe" launch4j.xml

echo.
echo Signing...
echo (You need a certificate for this)
powershell -Command $pword = read-host "Enter password to sign" -AsSecureString ; ^
    $BSTR=[System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($pword) ; ^
        [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR) > .tmp.txt 
set /p password=<.tmp.txt & del .tmp.txt

lib\sign4j.exe java -jar lib\jsign.jar --certfile "T:\Programming\certs\codesigning.p7b" --keyfile "T:\Programming\certs\codesigning.pvk" --keypass "%password%" --tsaurl "http://timestamp.comodoca.com/authenticode" --name "Escapists Map Editor" --url "http://escapists.jselby.net" EscapistsEditor-1.0.exe