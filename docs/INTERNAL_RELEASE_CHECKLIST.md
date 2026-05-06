# Internal Release Checklist

Tanggal acuan: 2026-05-06

## 1) Build Integrity
- [ ] `./gradlew.bat --no-daemon :app:compileDevDebugKotlin` sukses.
- [ ] `./gradlew.bat --no-daemon :app:testDevDebugUnitTest` sukses.
- [ ] `./gradlew.bat --no-daemon :app:assembleDevDebug` sukses.
- [ ] APK terbaru ada di `app/build/outputs/apk/dev/debug/app-dev-debug.apk`.

## 2) 16KB Page Size Compliance
- [ ] Upload APK/AAB ke Play pre-check (internal test).
- [ ] Tidak ada warning `not compatible with 16 KB devices`.
- [ ] CameraX minimal versi `1.5.0` tetap terpakai.

## 3) Station Connectivity
- [ ] Settings -> Station URL valid (`http://<ip>:8000/`).
- [ ] `Connect Photobooth Station` sukses.
- [ ] `Logout / Disconnect Station` memutus koneksi dan clear token state.

## 4) Heartbeat Verification
- [ ] Tekan `Send Heartbeat Now`.
- [ ] Status berubah dari `PENDING` ke `SUCCESS` atau `FAILED`.
- [ ] Jika `FAILED`, error detail muncul (401/403/422/network).
- [ ] Field `IP/App/OS/Capabilities/Last heartbeat/Last sync` terisi benar.

## 5) Start Now Photo Local-Only
- [ ] `Start Now Photo` bisa dipakai tanpa koneksi station.
- [ ] Capture -> Preview -> Finish berjalan penuh.
- [ ] Tidak ada upload capture ke station.
- [ ] Tidak ada complete session ke station.
- [ ] Tidak ada upload rendered output ke station.
- [ ] Print memakai printer Android lokal/default device.

## 6) Launch Event Connected Flow
- [ ] `Launch Event` hanya untuk mode connected.
- [ ] Voucher/payment gate tetap bekerja sesuai station.
- [ ] Upload capture/sync station hanya berjalan di flow connected non-local-only.

## 7) Regression Smoke
- [ ] Dashboard load normal.
- [ ] Camera front/back switch berjalan jika device support.
- [ ] Safe area overlay slot aktif tetap sesuai aspect ratio.
- [ ] Capture button + countdown UX normal.

## 8) Release Notes Minimum
- [ ] Catat perubahan penting: local-only Start Now Photo, heartbeat manual, 16KB fix.
- [ ] Catat known issues jika ada.
