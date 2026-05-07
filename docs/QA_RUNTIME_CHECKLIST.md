# QA Runtime Checklist (Device)

Tanggal acuan: 2026-05-07

## Scope
- Start Now Photo harus jalan tanpa koneksi Station.
- Heartbeat harus bisa sukses saat terkoneksi Station.
- Capture page harus mendukung switch kamera depan/belakang jika device punya keduanya.

## Environment
- APK: `app-dev-debug.apk` terbaru dari branch `chore/stabilize-localflow-heartbeat`.
- Device Android fisik (disarankan Android 13+).
- Wi-Fi yang sama dengan server Station untuk skenario online.
- URL Station valid, contoh: `http://192.168.88.248:8000/`.

## Checklist A: Start Now Photo Offline
1. Putuskan koneksi internet/Wi-Fi device.
2. Buka aplikasi, masuk ke flow `Start Now Photo`.
3. Pilih template lokal dan lanjutkan capture.
4. Ambil foto sampai halaman finish.
5. Validasi:
- Tidak ada error wajib koneksi Station.
- Tidak ada request session/upload ke endpoint `/api/device/*`.
- Flow finish tetap selesai normal.

Expected:
- Status `PASS` jika flow end-to-end berjalan tanpa network.

## Checklist B: Heartbeat Online
1. Sambungkan device ke jaringan yang sama dengan Station.
2. Pastikan login device sukses (token valid tersimpan).
3. Buka Settings dan tekan `Send Heartbeat Now`.
4. Validasi UI:
- `Heartbeat status` berubah dari `PENDING` menjadi `SUCCESS` (atau status sukses yang setara).
- Field terisi: `IP`, `App`, `OS`, `Capabilities`, `Last heartbeat`, `Last sync`.
5. Validasi server log:
- Ada request `POST /api/device/heartbeat`.
- Response sukses JSON:
  - `{"status":"ok","server_time":"..."}`

Expected:
- Status `PASS` jika request masuk dan UI sinkron.

## Checklist C: Camera Switch (Front/Back)
1. Gunakan device yang memiliki kamera depan dan belakang.
2. Buka halaman capture.
3. Pastikan tombol switch kamera muncul.
4. Tap tombol switch:
- Preview berpindah rear -> front.
- Tap lagi berpindah front -> rear.
5. Validasi fitur lain tetap normal:
- Tap-to-focus.
- Grid/safe area overlay.
- Countdown + capture.

Expected:
- Status `PASS` jika toggle kamera stabil tanpa crash.

## Checklist D: 16KB Warning Quick Recheck
1. Build APK terbaru (`assembleDevDebug` atau varian release yang dipakai rilis).
2. Verifikasi library native tidak lagi memicu warning lama pada proses validasi distribusi.
3. Jika upload internal testing dilakukan:
- Pastikan tidak muncul warning `not aligned at 16 KB boundaries`.

Expected:
- Status `PASS` jika warning tidak muncul lagi pada artefak terbaru.

## Result Template
- Build Version:
- Device Model / Android Version:
- Station URL:
- A Offline Start Now Photo: `PASS/FAIL`
- B Heartbeat Online: `PASS/FAIL`
- C Camera Switch: `PASS/FAIL`
- D 16KB Recheck: `PASS/FAIL`
- Catatan bug:
