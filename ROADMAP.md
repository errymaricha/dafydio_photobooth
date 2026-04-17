# Roadmap: Dafydio Booth Photobooth Station

Dokumen ini menjadi roadmap produk untuk sistem Dafydio Booth, mencakup Android device, station lokal, cloud portal, admin, payment, upload, dan print pipeline.

## Product Goal

Dafydio Booth ditargetkan menjadi sistem photobooth production-ready:

- Device booth bisa login dan dikenali backend.
- User masuk dari splash ke dashboard.
- User bisa start photo, pilih template, capture, retake, preview template, lalu finish.
- User bisa download, print, dan share hasil.
- Koneksi ke Photobooth Station bersifat opsional.
- Jika tidak connect ke station, data tidak disimpan ke database.
- Jika connect ke station, session/capture/print direkap ke database station.
- Setelah connect ke station, dashboard menampilkan `Launch Event` dan `Setting Event`.
- Voucher/payment berada di dalam `Launch Event`.
- Jika pembayaran manual, Android menunggu approval dari Photobooth Station.
- Station lokal bisa menangani print queue dan printer failure.
- Cloud portal bisa menyimpan riwayat session dan mendukung re-order.
- Operator mendapat error jelas, retry aman, dan UAT checklist bisa dipakai di lapangan.

## Current Focus

Fokus aktif:

- Core application development.
- Dashboard-first Android flow.
- Camera and template preview readiness.
- Optional Photobooth Station sync.
- Connected event flow: Launch Event, Setting Event, voucher/payment gate.
- Print reliability.

Parked sementara:

- Scramble/OpenAPI docs lanjutan.

## Current Status Summary

Android foundation:

- Arsitektur 3 layer: `ui`, `domain`, `data`.
- UDF dengan `BoothUiState` dan `BoothViewModel`.
- Retrofit/OkHttp, Kotlin Serialization, Coroutines/Flow, DataStore, Navigation Compose.
- Build variant `dev`, `staging`, `prod`.
- MVP flow baru: Splash -> Dashboard -> Template -> Camera -> Capture Preview -> Template Preview -> Finish.
- Settings mencakup station connection, camera source, external camera status, camera options, dan printer options.
- Settings persistence tersambung ke DataStore untuk station, camera, dan printer options.
- Station connection checker tersambung ke Station IP dan dashboard connected menu.
- Compose preview tersedia untuk Dashboard mobile/tablet, Settings tablet, dan Finish tablet.
- Jika station connected, Dashboard menampilkan Launch Event dan Setting Event.
- Voucher/payment gate berjalan setelah Launch Event.
- Unit test mapper dan integration-style test voucher -> payment -> create session.

Backend/station foundation:

- Seeder baseline dibuat idempotent.
- Demo workflow seeding tersedia untuk visual session, render, queue, print, dan log.
- Default admin, print-agent, station, device, printer, dan template tersedia.
- Dashboard/session/queue/printer/order/log UI sudah punya summary chips, counters, refresh, dan highlight error.
- Template management mencakup create, rename, archive, duplicate, delete, slot editor, overlay PNG, snap-to-grid, dan lock aspect ratio.
- Session editor mendukung drag photo placement, duplicate to next slot, dan render status summary.
- Voucher management mencakup master voucher, apply/revoke, quote simulator, discount rules, health indicator, dan copy code.
- Device payment gate sudah ada di backend: verify voucher, payment quote, create session, payment check, confirm payment, dan upload blocked before paid.

## Code Implementation Plan

Gunakan urutan ini untuk mulai coding fitur. Setiap fase harus bisa dites sendiri sebelum lanjut ke fase berikutnya.

### Phase 1: Dashboard Local Flow

Build:

- Splash screen.
- Dashboard.
- Start Now Photo.
- List Default Template.
- Create Custom Template.
- Camera placeholder.
- Capture preview.
- Retake/done.
- Template preview.
- Finish actions placeholder.
- Compose previews untuk mobile dan tablet.

Testable output:

- APK bisa menjalankan flow lokal tanpa koneksi station.
- Tidak ada API call wajib.
- Layout tablet bisa dicek dari Compose Preview.
- UAT Phase 1 selesai.

### Phase 2: Settings Persistence

Build:

- Persist station IP. Done.
- Persist device ID/token. Done.
- Persist camera source. Done.
- Persist external camera flags. Done.
- Persist camera settings. Done.
- Persist printer settings. Done.

Testable output:

- Setting tetap ada setelah app restart.
- UAT Phase 2 selesai.

### Phase 3: Station Connection And Connected Dashboard

Build:

- Connect Photobooth Station via IP lokal. Done.
- Device auth dengan token. Done.
- Connected/disconnected state. Done.
- Dashboard conditional menu: Launch Event dan Setting Event. Done.

Testable output:

- Menu event tidak muncul sebelum connected.
- Menu event muncul setelah connected.
- Current local station: `http://10.10.116.4:8000/`.
- Station command: `php artisan serve --host 10.10.116.4`.
- Physical device Settings value: `10.10.116.4:8000`.
- Emulator Settings value: `10.0.2.2:8000`.
- Raw IP input is normalized to `http://<ip>:8000/` when no scheme/port is provided.
- UAT Phase 3 selesai.

### Phase 4: Connected Event Gate

Build:

- Launch Event screen. Done.
- Setting Event screen. Done.
- Voucher check screen. Done.
- Payment gate screen. Done.
- Manual payment waiting approval. Done.
- Check approval status. Done.

Testable output:

- Voucher/payment hanya muncul setelah Launch Event.
- Manual payment menunggu station approval.
- Android continues to template only after `payment-check` returns approved/unlocked status.
- UAT Phase 4 selesai.

### Phase 5: Real Camera Capture

Build:

- CameraX preview.
- Permission handling.
- Countdown.
- Back/front camera.
- Capture file.
- Retake.

Testable output:

- Capture file lokal berhasil.
- Retake mengganti hasil.
- UAT Phase 5 selesai.

### Phase 6: External Canon Camera Interface

Build:

- Camera controller abstraction.
- Android default controller.
- Canon external controller stub/adapter.
- Scan/pair/connect status.
- Mirror live view.
- Mirror capture.

Testable output:

- Android camera tetap jalan tanpa Canon.
- External status bisa diuji.
- UAT Phase 6 selesai.

### Phase 7: Template Render And Finish Actions

Build:

- Template model.
- Preview render sederhana.
- Download output.
- Share output.
- Default print.
- Print via station.

Testable output:

- Download/share/print bisa dipicu.
- Station print hanya aktif saat connected.
- UAT Phase 7 selesai.

### Phase 8: Station Sync Queue

Build:

- Create station session.
- Upload capture.
- Sync finish actions.
- Local queue.
- Retry/backoff.
- Idempotency key.

Testable output:

- Mode lokal tidak menulis database.
- Mode connected sync ke station.
- Offline queue tidak duplicate setelah online.
- UAT Phase 8 selesai.

## System Topology

```text
Android Device -> Photobooth Station -> Photobooth Cloud -> Photobooth Admin
```

Android Device:

- Splash.
- Dashboard.
- Start Now Photo.
- Create Custom Template.
- List Default Template.
- Settings.
- Optional station connect.
- Launch Event jika station connected.
- Setting Event jika station connected.
- Voucher/payment gate di dalam Launch Event.
- Template select.
- Capture.
- Preview/retake.
- Template preview.
- Finish: download, print, share.
- Auto print request ke station lokal.

Photobooth Station:

- Local network gateway.
- Receive photo/print files from Android.
- Manage voucher/skip.
- Template editor.
- Print queue.
- Printer status.
- Offline fallback.

Photobooth Cloud:

- Client login.
- Session history.
- Original/render/thumbnail access.
- Re-order print.

Photobooth Admin:

- Monitoring session.
- Monitoring printer.
- Monitoring print queue.
- Log/audit visibility.

## Phase A: Android Dashboard MVP

Target: device bisa menjalankan flow photobooth lokal tanpa wajib connect station.

Scope:

- Splash Screen.
- Dashboard.
- Start Now Photo.
- List Default Template.
- Create Custom Template.
- Camera placeholder.
- Done capture.
- Preview and retake photo.
- Preview dengan template.
- Finish actions: download, print, share.
- Mode lokal tanpa database sync.

Acceptance criteria:

- App selalu masuk Splash -> Dashboard.
- Start Now membuka list template.
- Pilih template membuka Camera.
- Capture membuka preview.
- Retake kembali ke Camera.
- Done membuka preview template.
- Finish menampilkan download, print, share.
- `.\gradlew.bat testDevDebugUnitTest` lulus.

## Phase B: Settings And Station Connection

Target: station connection dan setting device siap dipakai operator.

Scope:

- Finalisasi base URL dev/staging/prod.
- Token auth interceptor.
- Login/connect memakai Photobooth Station via IP jaringan lokal.
- Camera source: Android default atau External Canon.
- External camera status: scan, pairing, connected.
- Mirror live view.
- Mirror capture.
- Image quality.
- Back/front camera.
- Denoise foto.
- Timer countdown.
- Countdown audio.
- Shutter sound.
- Default printing.
- Print use Photobooth Station.
- Dashboard connected menu: Launch Event dan Setting Event.

Acceptance criteria:

- Tanpa station connection, data tidak disimpan ke database.
- Dengan station connection, data siap direkap ke database station.
- Launch Event dan Setting Event tidak muncul saat station belum connected.
- Launch Event dan Setting Event muncul setelah connect sukses.
- Setting tersimpan dan tidak reset saat app restart.
- Print station hanya aktif jika station connected.

## Phase C: Connected Event, Voucher, And Payment Gate

Target: voucher/payment hanya berjalan pada event yang dilaunch setelah Android connect ke Photobooth Station.

Scope:

- Dashboard menampilkan `Launch Event` setelah station connected.
- Dashboard menampilkan `Setting Event` setelah station connected.
- Launch Event membuat/memilih event aktif.
- Event gate melakukan voucher check jika event membutuhkan voucher.
- Event gate melakukan payment quote/check jika event membutuhkan payment.
- Manual payment masuk status waiting approval.
- Android polling/check status approval dari Photobooth Station.
- Jika approved, user lanjut ke template/camera flow.
- Jika rejected/expired, user tetap di event gate dengan retry.

Acceptance criteria:

- Voucher/payment tidak muncul di mode lokal.
- Voucher/payment tidak muncul sebelum Launch Event.
- Manual payment tidak bisa lanjut sebelum station approval.
- Approval dari station membuka akses ke template/camera flow.
- Status waiting approval jelas untuk operator.
- Retry/check status tersedia.

## Phase D: Real Capture Integration

Target: screen Capture memakai kamera device booth secara nyata.

Scope:

- Integrasi CameraX.
- Permission camera.
- Preview kamera full screen.
- Integrasi external Canon berdasarkan `https://github.com/errymaricha/canonDSLR`.
- Countdown capture.
- Multi-shot capture.
- Save temporary photo files.
- Retake photo.
- Basic compression.

Acceptance criteria:

- Camera preview stabil di device target.
- Capture menghasilkan file lokal.
- Retake mengganti file lama.
- Permission denied tertangani.
- Android default camera tetap jalan walaupun external camera tidak connected.
- Mirror live view dan mirror capture diterapkan pada jalur preview/capture.

## Phase E: Template And Finish Pipeline

Target: hasil capture bisa digabung dengan template dan diselesaikan operator.

Scope:

- Template default dari app.
- Custom template lokal.
- Preview hasil dengan template.
- Download output.
- Share output.
- Print default Android.
- Print via Photobooth Station.

Acceptance criteria:

- Preview template sesuai template terpilih.
- Download menyimpan file hasil.
- Share membuka Android share sheet.
- Print default memakai Android print.
- Print station hanya berjalan saat station connected.

## Phase F: Station Sync Pipeline

Target: data direkap ke database hanya saat connect ke Photobooth Station.

Scope:

- Create station session saat start/capture jika connected.
- Upload capture ke station.
- Store selected template.
- Store finish actions.
- Queue lokal saat station offline.
- Retry sync.

Acceptance criteria:

- Mode lokal tidak menulis database.
- Mode connected menulis session/capture/print/share/download event.
- Offline tidak menghapus file lokal.
- Retry sync tidak membuat duplicate session.

## Phase G: Payment Real Integration

Target: payment gateway real terhubung ke flow Android dan backend.

Scope:

- Payment init service.
- QRIS/e-wallet payload untuk Android.
- Webhook callback.
- Signature verification.
- Payment reconciliation scheduler.
- Audit events.
- Ops UI status visibility.

Definition of done:

- Session `pending -> paid` bisa otomatis via webhook.
- Duplicate webhook tidak membuat double transition.
- Out-of-order callback aman.
- Reconciliation bisa memperbaiki pending/unknown transaction.
- Android contract payment freeze selesai.

Suggested daily plan:

1. Finalisasi contract dan data model payment.
2. Implement payment init service.
3. Implement webhook handler dan signature verification.
4. Implement reconciliation job.
5. Tambah status payment di UI ops.
6. Tambah failure-path regression tests.
7. Mini UAT dan handover contract Android.

## Phase H: Print Reliability

Target: print pipeline aman untuk event ramai.

Scope:

- Retry policy print job.
- Attempt counter dan cooldown.
- Idempotency key anti-double print.
- Fallback printer rule.
- Local station queue.
- Printer offline detection.

Acceptance criteria:

- Print job gagal bisa retry otomatis.
- Tidak ada double print untuk request yang sama.
- Fallback printer bekerja saat printer utama error/offline.
- Failed rows terlihat jelas untuk operator.

## Phase I: Offline-First Station

Target: station tetap bisa beroperasi saat koneksi tidak stabil.

Scope:

- Deteksi online/offline.
- Local queue untuk action kritikal.
- Sync worker.
- Persist session draft.
- Resume session setelah app restart.
- Conflict handling sederhana.

Acceptance criteria:

- App tidak crash saat offline.
- Session unpaid tidak bisa upload walaupun offline.
- Capture yang sudah dibuat bisa dilanjutkan setelah online.
- Queue sync tidak membuat data duplikat.

## Phase J: Cloud Portal And Re-order

Target: client bisa melihat history dan melakukan re-order print.

Scope:

- Sync session/render/print summary ke cloud.
- Client session history.
- Original/render/thumbnail access.
- Re-order print dari cloud ke station/admin.

Acceptance criteria:

- Session history tampil konsisten.
- Re-order masuk ke workflow print station.
- Ownership client terjaga.

## Phase K: Security And Audit

Target: sistem aman untuk produksi dan investigasi operasional.

Scope:

- Device credential rotation.
- Token revocation.
- Rate limiting endpoint kritikal.
- Audit log untuk aksi sensitif.
- TLS/certificate validation.
- Secure token storage.
- Kiosk hardening.
- Log sanitization.

Acceptance criteria:

- Endpoint sensitif terproteksi throttle.
- Credential bisa di-rotate.
- Token tidak tampil di log.
- Print-agent hanya bisa memakai bound printer.
- Audit trail cukup untuk investigasi.

## Phase L: API Docs And CI Governance

Target: dokumentasi API dan CI gate kembali aktif setelah milestone aplikasi stabil.

Scope:

- Resume Scramble docs.
- Coverage endpoint device, editor, dan print-agent.
- CI gate untuk Scramble analyze.
- CI gate untuk voucher/payment critical path.
- Publish docs internal untuk tim Android dan ops.

Acceptance criteria:

- Dokumen API bisa dipakai lintas tim.
- CI gagal jika contract/test kritikal regress.
- Docs access policy production jelas.

## Phase M: Pilot UAT Lapangan

Target: keputusan go/no-go produksi berdasarkan simulasi event nyata.

Scope:

- Multi-device test.
- Multi-session test.
- Multi-print test.
- Chaos test jaringan lokal.
- Printer failure drill.
- Gateway payment failure drill.
- Final bugfix batch.

Acceptance criteria:

- Pilot checklist lulus.
- Bug high/critical selesai.
- Rollback plan tersedia.
- Go/no-go production decision siap.

## Implemented Capability Notes

Dynamic Layer:

- Text layer dan QR layer sudah dirender ke output.
- Token variables tersedia untuk session, station, render time/date, dan device name.
- Layer bisa di-enable/disable.
- Drag/drop layer di canvas preview.
- QR preview real via API.

Smart Template Fit:

- v1: auto fit saat assign foto, toggle, apply all, zoom indicator.
- v2: heuristic crop bias ke area atas tanpa OpenCV.
- v3: face-aware endpoint dengan OpenCV, disabled di testing environment.

Voucher And Payment Gate:

- Voucher types: `promo`, `skip`, `free`, `override`.
- Edge cases: expired, not started, usage full, min purchase not met.
- Audit events: `voucher_applied`, `payment_gate_blocked`, `payment_confirmed`.
- Promo voucher tetap pending payment.
- Bypass voucher bisa unlock capture sesuai backend rule.
- Voucher/payment sekarang berada di connected event flow setelah Launch Event.
- Manual payment harus menunggu approval dari Photobooth Station.

## Backend Contract Priorities

Contract yang harus dijaga:

- `POST /api/device/auth` untuk connect Settings ke Photobooth Station.
- `contract_version`
- `device_id`
- `voucher_code`
- `voucher_type`
- `session_type`
- `quote_id`
- `session_id`
- `session_code`
- `payment_status`
- `payment_required`
- `payment_unlocked`
- `unlock_photo`
- `can_upload`

Perubahan contract wajib disertai:

- Update DTO.
- Update mapper.
- Update unit test mapper.
- Update integration-style flow test.
- Update UAT checklist jika behavior berubah.
- Update `AI_AGENT_GUIDE.md` jika agent rule berubah.

## Quality Gates

Sebelum merge atau delivery:

- [ ] Compile varian yang disentuh.
- [ ] Unit/feature test relevan lulus.
- [ ] Flow voucher -> payment -> create session lulus.
- [ ] Voucher/payment hanya muncul setelah station connected dan Launch Event.
- [ ] Manual payment waiting approval tidak bisa dilewati dari Android.
- [ ] Error `401`, `403`, `422` tetap jelas.
- [ ] Upload unpaid tetap blocked.
- [ ] Base URL sesuai environment.
- [ ] Tidak ada duplicate critical action.
- [ ] Auth dan ownership aman.
- [ ] UAT checklist diperbarui.

## UAT Device Checklist

Checklist operasional:

- `docs/UAT_DEVICE_CHECKLIST.md`

Minimal sebelum event:

- Online success flow.
- Offline retry flow.
- Voucher valid.
- Voucher expired.
- Voucher full.
- Payment success.
- Payment fail/unpaid.
- Upload blocked saat unpaid.
- Upload allowed saat paid.
- Print success.
- Print retry/fallback.

## Next Recommended Work

Urutan paling praktis:

1. Finalisasi URL backend dev/staging/prod.
2. Persist semua settings Android ke DataStore.
3. Tambahkan dashboard connected menu: Launch Event dan Setting Event.
4. Implement connected event gate: voucher/payment/manual waiting approval.
5. Integrasi CameraX untuk real capture.
6. Buat interface external camera Canon.
7. Implement template preview/render lokal.
8. Implement download, print, share.
9. Definisikan station sync contract dan local queue.
10. Hardening print retry, idempotency, dan fallback printer.
