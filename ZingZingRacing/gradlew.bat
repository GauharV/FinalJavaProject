@echo off
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m" "-Djavax.net.ssl.trustAll=true" "-Djdk.tls.client.protocols=TLSv1.2"

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

:findJavaFromJavaHome
set JAVA_HOME_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME_EXE%" goto execute

for %%i in (java.exe) do set AUTO_JAVA_EXE=%%~$PATH:i
if exist "%AUTO_JAVA_EXE%" (
    set JAVA_HOME_EXE="%AUTO_JAVA_EXE%"
    goto execute
)

echo Error: JAVA_HOME is not set and no 'java' command could be found.
exit /b 1

:execute
"%JAVA_HOME_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
