REM @echo off

call stockEnv.bat

echo %1

set arg1=%1

for /F "tokens=2,3,4" %%x in (%arg1%) do (
    cp %%y\%%x %%z
    %rootDir%\bin\uSleep.exe 60000
REM    %rootDir%\bin\uSleep.exe 0
)
