
## Default Release Policy (Mandatory)

Unless the user **explicitly** states not to release on the backend (e.g. "only test on virtual device, don't release"), every time changes are made to the Android app (`ZezAndroid`) or backend (`Zez-Backend`):

1. **Bump Version**: Query current `version_code` in `public.app_releases` (PostgreSQL) and bump `versionCode` (+1 strictly above live active release) and `versionName` in `ZezAndroid/app/build.gradle`.
2. **Build Release APK**: Run `.\gradlew.bat assembleRelease` inside `ZezAndroid` and verify `BUILD SUCCESSFUL`.
3. **Test Before/With Release**: Verify changed features on `emulator-5554` / backend tests.
4. **Publish APK to Backend**: Run `node scripts/publish_release.js --notes "<concise release notes>"` inside `Zez-Backend` so the signed release APK is uploaded to Backblaze B2 (`releases/...`) and marked active in `public.app_releases`.
5. **Deploy Backend & Verify Live Endpoints**: If `Zez-Backend` source files were modified, push commits to `origin` (`github.com/SayanBasani/ZEZ.git`) so Render redeploys cleanly, and verify `https://zez-chat-backend.onrender.com/api/app/latest` + `/api/storage/file/...` + `/downloads/app-release-latest.apk` return the new version!
