@echo off
echo Cleaning up Derby database...

rem Kill any Java processes that might be holding the database lock
taskkill /F /IM java.exe

rem Wait a moment for processes to terminate
timeout /t 2 /nobreak

rem Remove the lock files
if exist "derpydb\db.lck" del /F "derpydb\db.lck"
if exist "derpydb\dbex.lck" del /F "derpydb\dbex.lck"

echo Cleanup complete! 