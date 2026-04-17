# Dafydio Booth Device UAT Checklist

Checklist ini mengikuti fase coding Android agar setiap penambahan fitur bisa langsung dites.

## Environment

- [ ] Device menggunakan build variant yang benar: dev, staging, atau prod.
- [ ] Base URL mengarah ke Photobooth Station yang benar.
- [ ] Android device/emulator bisa membuka aplikasi.
- [ ] Jika pakai station lokal, Android dan station berada di jaringan yang sama.

## Phase 1: Dashboard Local Flow

- [ ] Compose Preview tersedia untuk Dashboard Mobile.
- [ ] Compose Preview tersedia untuk Dashboard Tablet.
- [ ] Compose Preview tersedia untuk Settings Tablet.
- [ ] Compose Preview tersedia untuk Finish Tablet.
- [ ] App membuka Splash Screen.
- [ ] Splash otomatis masuk Dashboard.
- [ ] Dashboard menampilkan Start Now Photo.
- [ ] Dashboard menampilkan Create Custom Template.
- [ ] Dashboard menampilkan List Default Template.
- [ ] Dashboard menampilkan Settings.
- [ ] Start Now Photo membuka pilihan template.
- [ ] Pilih template membuka Camera.
- [ ] Capture membuka Preview Capture.
- [ ] Retake kembali ke Camera.
- [ ] Done membuka Preview Template.
- [ ] Finish membuka halaman finish.
- [ ] Finish menampilkan Download, Print, dan Share.
- [ ] Tanpa station connection, app menampilkan mode lokal.
- [ ] Tanpa station connection, tidak ada voucher/payment gate.

## Phase 2: Settings Persistence

- [ ] Status implementation: DataStore persistence tersedia untuk station, camera, external camera, dan printer settings.
- [ ] Station IP bisa diisi.
- [ ] Device ID bisa diisi.
- [ ] Token bisa diisi.
- [ ] Camera source bisa dipilih: Android default atau External Canon.
- [ ] External camera status bisa berubah scan/pairing/connected.
- [ ] Mirror live view bisa enable/disable.
- [ ] Mirror capture bisa enable/disable.
- [ ] Image quality bisa dipilih.
- [ ] Back camera dan front camera bisa dipilih.
- [ ] Denoise foto bisa enable/disable.
- [ ] Timer countdown bisa dipilih.
- [ ] Countdown audio bisa enable/disable.
- [ ] Shutter sound bisa enable/disable.
- [ ] Default printing bisa enable/disable.
- [ ] Print use Photobooth Station bisa enable/disable.
- [ ] Setelah app ditutup dan dibuka ulang, settings tidak reset.

## Phase 3: Station Connection And Connected Dashboard

- [ ] Status implementation: Station connection checker tersedia dan memakai Station IP dari Settings.
- [ ] Photobooth Station berjalan di `http://10.10.116.4:8000/`.
- [ ] Station dijalankan dengan `php artisan serve --host 10.10.116.4`.
- [ ] Device fisik memakai Station IP `10.10.116.4:8000`.
- [ ] Emulator memakai Station IP `10.0.2.2:8000`.
- [ ] Input `10.10.116.4` otomatis dinormalisasi ke port `8000`.
- [ ] Connect ke Photobooth Station berhasil dengan IP lokal.
- [ ] Device ID valid diterima station.
- [ ] Token valid diterima station.
- [ ] Token invalid menampilkan error.
- [ ] Station offline menampilkan error network.
- [ ] Sebelum connected, Dashboard tidak menampilkan Launch Event.
- [ ] Sebelum connected, Dashboard tidak menampilkan Setting Event.
- [ ] Setelah connected, Dashboard menampilkan Launch Event.
- [ ] Setelah connected, Dashboard menampilkan Setting Event.
- [ ] Print use Photobooth Station hanya aktif jika station connected.

## Phase 4: Connected Event, Voucher, And Payment Gate

- [ ] Launch Event membuka event gate.
- [ ] Setting Event membuka setting event.
- [ ] Voucher/payment tidak muncul di mode lokal.
- [ ] Voucher/payment tidak muncul sebelum Launch Event.
- [ ] Voucher valid melanjutkan flow sesuai rule event.
- [ ] Voucher expired ditolak dengan pesan jelas.
- [ ] Voucher usage full ditolak dengan pesan jelas.
- [ ] Payment quote menampilkan total yang harus dibayar.
- [ ] Payment success membuka akses ke template/camera.
- [ ] Manual payment masuk Waiting Approval.
- [ ] Waiting Approval tidak bisa dilewati dari Android.
- [ ] Approval dari Photobooth Station membuka akses ke template/camera.
- [ ] Rejected/expired approval tetap menahan user di event gate.
- [ ] Check status/retry tersedia saat waiting approval.

## Phase 5: Real Camera Capture

- [ ] Permission camera diminta saat diperlukan.
- [ ] Permission denied ditangani tanpa crash.
- [ ] Android default camera menampilkan preview.
- [ ] Back camera bisa digunakan.
- [ ] Front camera bisa digunakan.
- [ ] Countdown berjalan sebelum capture.
- [ ] Countdown audio mengikuti setting.
- [ ] Shutter sound mengikuti setting.
- [ ] Capture menghasilkan file lokal.
- [ ] Retake mengganti file hasil capture.

## Phase 6: External Canon Camera Interface

- [ ] Android default camera tetap berjalan saat external camera disconnected.
- [ ] External Canon scan mengubah status ke scanning.
- [ ] Pairing mengubah status ke pairing.
- [ ] Connected mengubah status ke connected.
- [ ] Mirror live view diterapkan pada preview external.
- [ ] Mirror capture diterapkan pada hasil capture external.
- [ ] Jika Canon tidak tersedia, app tidak crash.

## Phase 7: Template Render And Finish Actions

- [ ] Template default tampil di list.
- [ ] Custom template bisa dipilih.
- [ ] Preview template memakai hasil capture.
- [ ] Download membuat file output.
- [ ] Share membuka Android share sheet.
- [ ] Default print membuka Android print flow.
- [ ] Print via Photobooth Station diblokir saat station disconnected.
- [ ] Print via Photobooth Station berjalan saat station connected.

## Phase 8: Station Sync Queue

- [ ] Mode lokal tidak membuat session di database station.
- [ ] Mode connected membuat session di database station.
- [ ] Capture tersinkron ke station saat connected.
- [ ] Finish action tersinkron ke station saat connected.
- [ ] Saat station offline, data masuk local queue.
- [ ] Setelah station online, queue tersinkron.
- [ ] Retry sync tidak membuat duplicate session.
- [ ] Idempotency key mencegah duplicate upload/print.

## Regression Checklist

- [ ] `.\gradlew.bat testDevDebugUnitTest` lulus.
- [ ] `.\gradlew.bat assembleStagingDebug` lulus.
- [ ] App bisa dijalankan di emulator/device.
- [ ] Mode lokal tetap bisa dipakai tanpa station.
- [ ] Connected mode tidak aktif tanpa login/connect.
- [ ] Voucher/payment hanya tersedia setelah Launch Event.
- [ ] Manual payment selalu menunggu approval station.
