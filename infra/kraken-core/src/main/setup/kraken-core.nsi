; Kraken Installer

Name "Kraken Core"
Caption "Kraken Core"
OutFile "Kraken-Core-1.7.0-Setup.exe"

RequestExecutionLevel admin
AutoCloseWindow false


!include UAC.nsh

!define PRODUCT_NAME "Kraken"
!define JRE_VERSION "1.6"
!define JVMDLL "jvm.dll"
!define JAVAEXE "javaw.exe"


!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare
!include "x64.nsh"

Function .onInit
uac_tryagain:
!insertmacro UAC_RunElevated
#MessageBox mb_TopMost "0=$0 1=$1 2=$2 3=$3"
${Switch} $0
${Case} 0
	${IfThen} $1 = 1 ${|} Quit ${|} ;we are the outer process, the inner process has done its work, we are done
	${IfThen} $3 <> 0 ${|} ${Break} ${|} ;we are admin, let the show go on
	${If} $1 = 3 ;RunAs completed successfully, but with a non-admin user
		MessageBox mb_IconExclamation|mb_TopMost|mb_SetForeground "This installer requires admin access, try again" /SD IDNO IDOK uac_tryagain IDNO 0
	${EndIf}
	;fall-through and die
${Case} 1223
	MessageBox mb_IconStop|mb_TopMost|mb_SetForeground "This installer requires admin privileges, aborting!"
	Quit
${Case} 1062
	MessageBox mb_IconStop|mb_TopMost|mb_SetForeground "Logon service not running, aborting!"
	Quit
${Default}
	MessageBox mb_IconStop|mb_TopMost|mb_SetForeground "Unable to elevate , error $0"
	Quit
${EndSwitch}
FunctionEnd


Section
;  Call CheckPlatform
	Var /GLOBAL JavaHome
	Call GetJRE
	Call CopyFiles
	Call AddService
	Call StartService
	Call WriteRegistry
	writeUninstaller $INSTDIR\uninstaller.exe
SectionEnd

Section "Uninstall"
  Call un.StopService	
  Call un.RemoveService
  Call un.RemoveFiles
  Call un.RemoveRegistry
SectionEnd


Function CopyFiles
	ClearErrors
	${If} ${RunningX64}
		SetOutPath "$PROGRAMFILES64\Kraken"
		StrCpy $INSTDIR "$PROGRAMFILES64\Kraken"
;		MessageBox MB_OK "64bit"
	${Else}
		SetOutPath "$PROGRAMFILES\Kraken"
		StrCpy $INSTDIR "$PROGRAMFILES\Kraken"
;		MessageBox MB_OK "32bit"
	${Endif}
	MessageBox MB_OK "Installed in $INSTDIR"
	File /r Kraken\*.*
	IfErrors Error Next
	Error:
	    MessageBox MB_OK "Could not install Kraken Core. Please remove existing Kraken Core first."
		Abort
	Next:
FunctionEnd

Function un.RemoveFiles
	RMDir /r $INSTDIR
FunctionEnd


Function AddService
    Push $R0
    Push $R1
    Push $2
	${If} ${RunningX64}
		SetRegView 64
	${Endif}
;	MessageBox MB_OK "$JavaHome"
	
  CheckJavaHome:
    ClearErrors
	ReadEnvStr $R0 "JAVA_HOME"
	StrCpy $R0 "$R0\jre"
	IfErrors CheckJRERegistry CheckClientDll

	CheckJRERegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
;    MessageBox MB_OK "JRE: jre? $R0"
	IfErrors CheckJDKRegistry CheckClientDll	

	CheckJDKRegistry:
    ClearErrors
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$R1" "JavaHome"
	StrCpy $R0 "$R0\jre"
;    MessageBox MB_OK "JDK: jre? $R0"
	IfErrors Error CheckClientDll
  CheckClientDll:
  	ClearErrors
    StrCpy $R1 "$R0\bin\client\${JVMDLL}"
;	MessageBox MB_OK "Client $R1"
    IfFileExists $R1 JavaService CheckServerDll
	
  CheckServerDll:
    ClearErrors
    StrCpy $R1 "$R0\bin\server\${JVMDLL}"
;	MessageBox MB_OK "Server $R1"
    IfFileExists $R1 JavaService Error
	
  Error:
    MessageBox MB_OK "Could not find JRE."
    Abort
  JavaService:
;    MessageBox MB_OK "$R1"
	${If} ${RunningX64}
		StrCpy $R0 "$INSTDIR\procrun\amd64\krakencore.exe"
	${Else}
		StrCpy $R0 "$INSTDIR\procrun\krakencore.exe"
	${Endif}
;	MessageBox MB_OK "$R0"
	ExecWait `"$R0$\" //IS//$\"Kraken_Core$\" --Install=$\"$R0$\" --Description=$\"Kraken Core 1.7.0$\" --Jvm=$\"$R1$\" --Startup=auto --StartMode=jvm --Classpath=$\"$INSTDIR\core\kraken-core-1.7.0-package.jar$\" --StartClass=org.krakenapps.main.Kraken --StartMethod=windowsService --StartParams=start --StopMode=jvm --StopClass=org.krakenapps.main.Kraken --StopMethod=windowsService --StopParams=stop --LogPath=$\"$INSTDIR\log$\" --StdError=auto --StdOutput=auto --StartPath=$\"$INSTDIR"`

FunctionEnd

Function un.RemoveService
	${If} ${RunningX64}
		StrCpy $R0 "$INSTDIR\procrun\amd64\krakencore.exe"
	${Else}
		StrCpy $R0 "$INSTDIR\procrun\krakencore.exe"
	${Endif}
	ExecWait `"$R0$\" //DS//$\"Kraken_Core$\"`
FunctionEnd

Function StartService
  ExecWait "net start Kraken_core"
FunctionEnd

Function un.StopService
	ExecWait "net stop Kraken_core"
FunctionEnd

Function GetJRE
    Push $R0
    Push $R1
    Push $2
	${If} ${RunningX64}
		SetRegView 64
	${Endif}
  ; 2) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $JavaHome $R0	
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckJRERegistry     
    IfFileExists $R0 0 CheckJRERegistry
    Call CheckJREVersion
    IfErrors CheckJRERegistry JreFound
 
  ; 3) Check for registry
  CheckJRERegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $JavaHome $R0
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckJDKRegistry
;	MessageBox MB_OK "JRE found"
    IfFileExists $R0 0 CheckJDKRegistry
    Call CheckJREVersion
    IfErrors CheckJDKRegistry JreFound
  CheckJDKRegistry:
    ClearErrors
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$R1" "JavaHome"
    StrCpy $JavaHome $R0
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors InstallJRE
;	MessageBox MB_OK "JDK found"
    IfFileExists $R0 0 InstallJRE
    Call CheckJREVersion
    IfErrors InstallJRE JreFound
  InstallJRE:
	MessageBox MB_OK "Install JRE first."
	Abort
;    MessageBox MB_ICONINFORMATION "${PRODUCT_NAME} uses Java Runtime Environment ${JRE_VERSION}, it will be installed."
	
	${If} ${RunningX64}
;		File /oname=$TEMP\jre-6u22-windows-x64.exe "jre\jre-6u22-windows-x64.exe"
;		Execwait "$TEMP\jre-6u22-windows-x64.exe"
	${Else}
;		File /oname=$TEMP\jre-6u22-windows-i586.exe "jre\jre-6u22-windows-i586.exe"
;		Execwait "$TEMP\jre-6u22-windows-i586.exe"
	${Endif}
 
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound
 
  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    ; MessageBox MB_ICONSTOP "Cannot find appropriate Java Runtime Environment."
    ; Abort
 
  JreFound:
    Pop $2
    Pop $R1
    Exch $R0
FunctionEnd
 
; Pass the "javaw.exe" path by $R0
Function CheckJREVersion
    Push $R1
 
    ; Get the file version of javaw.exe
    ${GetFileVersion} $R0 $R1
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
 
    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors
 
  CheckDone:
    Pop $R1
FunctionEnd

Function WriteRegistry
	${If} ${RunningX64}
		SetRegView 64
	${Endif}

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken" "DisplayName" "Kraken"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken" "Publisher" "NCHOVY Inc."
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken" "UninstallString" "$\"$INSTDIR\uninstaller.exe$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken" "URLInfoAbout" "http://krakenapps.org"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken" "DisplayVersion" "1.7.0"
FunctionEnd

Function un.RemoveRegistry
	${If} ${RunningX64}
		SetRegView 64
	${Endif}
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Kraken"
FunctionEnd