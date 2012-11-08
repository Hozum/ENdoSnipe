@echo off

rem ---------------------------------------------------------------------------
rem ���̊��ϐ��̓r���h�X�N���v�g�ɂ���Ď����I�� build.properties ��
rem �e�v���W�F�N�g�� MANIFEST.INF �֔��f����܂�
rem VER��ύX����ۂ́A�ȉ��̃t�@�C���������Ƀo�[�W������ύX����K�v������܂��B
rem javelin.pro,pom.xml,arrowvision.pro,bottleneckeye.pro
rem ---------------------------------------------------------------------------
set VER=5.0.0
set BUILD=016
rem ---------------------------------------------------------------------------

if exist "C:\Program Files (x86)" goto 64BIT

:32BIT
echo 32 bit environment
set JAVA_HOME=C:\Program Files\Java\jdk1.5.0_22
set JAVA6_HOME=C:\Program Files\Java\jdk1.6.0_35
goto SETTING

:64BIT
echo 64 bit environment
set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
set JAVA6_HOME=C:\Program Files\Java\jdk1.6.0_35

:SETTING


set WORK_DIR=%~dp0deploy
set TAGS=Version_%VER%-%BUILD%_build
set PATH=%JAVA_HOME%\bin;%PATH%
set SVN_PATH=https://wisteria.proma-c.com/svn/ENdoSnipe
set SVN_TRUNK_PATH=%SVN_PATH%/branches/Ver5.0/
set SVN_TAGS_PATH=%SVN_PATH%/tags/ENdoSnipe/
set SVN_DOC_PATH=%SVN_TRUNK_PATH%/Document
set SVN_SRC_PATH=%SVN_TRUNK_PATH%/MasterSource

if exist "%WORK_DIR%" rmdir "%WORK_DIR%" /S /Q


echo �r���h���J�n���܂��B
echo ===============================
echo ��JAVA�o�[�W����
java -version
echo ���^�O
echo %TAGS%
echo ===============================

pause

echo SVN��Ńr���h�ԍ�"%TAGS%"�Ń^�O�𔭍s���܂��B

svn mkdir %SVN_TAGS_PATH%%TAGS%/              -m %TAGS%-�^�O�f�B���N�g���쐬


echo ------------------------------------------------------------
echo �r���h (JAR�t�@�C���̐���)
echo ------------------------------------------------------------

svn copy %SVN_DOC_PATH% %SVN_TAGS_PATH%%TAGS% -m %TAGS%-�h�L�������g�R�s�[
svn copy %SVN_SRC_PATH% %SVN_TAGS_PATH%%TAGS% -m %TAGS%-�\�[�X�R�s�[

echo �R�s�[���������܂����B
echo �����āA�^�O�𔭍s�����t�@�C�����A���[�J����"%WORK_DIR%\svn"�Ƀ`�F�b�N�A�E�g���܂��B

svn co %SVN_TAGS_PATH%%TAGS%/MasterSource "%WORK_DIR%\svn"

cd /d "%WORK_DIR%\svn\ENdoSnipe"

echo Ant�����s���܂��B

call build_java.bat

echo �r���h���������܂����B
echo WebDashboard ���r���h���܂��B

set JAVA_HOME_BAK=%JAVA_HOME%
set JAVA_HOME=%JAVA6_HOME%

cd ..\WebDashboard

call ant dist

echo �r���h���������܂����B

cd ..\ENdoSnipe
set JAVA_HOME=%JAVA_HOME_BAK%


echo ------------------------------------------------------------
echo �X�V�t�@�C���̃R�~�b�g
echo ------------------------------------------------------------
echo �r���h���ɍX�V���ꂽ�t�@�C���� %SVN_TAGS_PATH%%TAGS% �փR�~�b�g���܂��B

cd /d "%WORK_DIR%\svn"
svn commit . -m %TAGS%-�r���h���X�V�t�@�C���̃R�~�b�g

echo �R�~�b�g���������܂����B

echo ��ƃR�s�[�� %SVN_SRC_PATH% �ɐ؂�ւ��܂��B
svn switch %SVN_SRC_PATH%
echo �؂�ւ����������܂����B

echo �r���h���ɍX�V���ꂽ�t�@�C������ƃR�s�[�Ƀ}�[�W���܂��B
svn merge --force %SVN_TAGS_PATH%%TAGS%/MasterSource .
echo �}�[�W���������܂����B

echo �}�[�W���ʂ��R�~�b�g���܂��B
svn commit . -m %TAGS%-�r���h���ɍX�V���ꂽ�t�@�C���̃R�~�b�g
echo �R�~�b�g���������܂����B

echo ------------------------------------------------------------
echo �f�v���C�t�@�C���̃R�~�b�g
echo ------------------------------------------------------------

svn mkdir %SVN_TAGS_PATH%%TAGS%/Product -m %TAGS%-���ʕ��p�f�B���N�g���쐬
svn mkdir %SVN_TAGS_PATH%%TAGS%/Product/Software -m %TAGS%-���ʕ��p�f�B���N�g���쐬

cd /d "%WORK_DIR%"

svn co %SVN_TAGS_PATH%%TAGS%/Product

cd /d "%WORK_DIR%\Product\Software"
copy "%WORK_DIR%\svn\ENdoSnipe\release\*" .
svn add *
svn commit . -m %TAGS%-���ʕ��R�~�b�g

echo ���ׂẴr���h�v���Z�X���������܂����B
pause
