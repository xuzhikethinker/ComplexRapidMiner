# Auto-generated by EclipseNSIS Script Wizard
# 29.07.2006 03:07:42

Name RapidMiner
# force compression with lzma
SetCompress force
SetCompressor /SOLID lzma

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 4.0beta2
!define COMPANY ""
!define URL http://www.rapidminer.com

# MUI defines
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY Software\RapidMiner
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULT_FOLDER RapidMiner
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

# Included files
!include Sections.nsh
!include MUI.nsh

# Reserved Files

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE ..\release\windows_installer\rapidminer\LICENSE.txt
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile ..\release\rapidminer-${VERSION}-install.exe
InstallDir $PROGRAMFILES\RapidMiner
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 4.0.0.0
VIAddVersionKey ProductName RapidMiner
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion ""
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    SetOutPath $INSTDIR\RapidMiner-${VERSION}
    SetOverwrite on
    File /r ..\release\windows_installer\rapidminer\*
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut $SMPROGRAMS\$StartMenuGroup\RapidMiner.lnk $INSTDIR\RapidMiner-${VERSION}\RapidMiner.exe
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\RapidMiner (console).lnk" $INSTDIR\RapidMiner-${VERSION}\scripts\RapidMinerGUI.bat
    CreateShortcut $SMPROGRAMS\$StartMenuGroup\Readme.lnk $INSTDIR\RapidMiner-${VERSION}\README.txt
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\RapidMiner web site.lnk" http://www.rapidminer.com
    CreateShortcut $DESKTOP\RapidMiner.lnk $INSTDIR\RapidMiner-${VERSION}\RapidMiner.exe
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section -post SEC0001
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteUninstaller $INSTDIR\RapidMiner-${VERSION}\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\RapidMiner-${VERSION}\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_END
    WriteRegStr HKLM "SOFTWARE\RapidI\RapidMiner\CurrentVersion\RapidMiner" "RapidMiner Home" $INSTDIR\RapidMiner-${VERSION}
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\RapidMiner-${VERSION}\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\RapidMiner-${VERSION}\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o un.Main UNSEC0000
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\RapidMiner web site.lnk"
    Delete /REBOOTOK $SMPROGRAMS\$StartMenuGroup\Readme.lnk
    Delete /REBOOTOK $SMPROGRAMS\$StartMenuGroup\RapidMiner.lnk
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\RapidMiner (console).lnk"
    Delete /REBOOTOK $DESKTOP\RapidMiner.lnk
    RmDir /r /REBOOTOK $INSTDIR\RapidMiner-${VERSION}
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section un.post UNSEC0001
    DeleteRegValue HKLM "SOFTWARE\RapidI\RapidMiner\CurrentVersion\RapidMiner" "RapidMiner Home"
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
    Delete /REBOOTOK $INSTDIR\RapidMiner-${VERSION}\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
 
  ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" "UninstallString"
  StrCmp $R0 "" done
 
  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
  "$(^Name) is already installed. $\n$\nClick `OK` to remove the \
  previous version or `Cancel` to cancel this upgrade." \
  IDOK uninst
  Abort
  
  ;Run the uninstaller
  uninst:
  ClearErrors
  ExecWait '$R0 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file
 
  IfErrors no_remove_uninstaller
    ;You can either use Delete /REBOOTOK in the uninstaller or add some code
    ;here to remove the uninstaller. Use a registry key to check
    ;whether the user has chosen to uninstall. If you are using an uninstaller
    ;components page, make sure all sections are uninstalled.
  no_remove_uninstaller:
  
  done:
  
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd

