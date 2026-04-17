# AI Agent Guide: Dafydio Booth

Dokumen ini adalah panduan kerja untuk AI agent saat membaca, mengubah, atau melanjutkan pengembangan Dafydio Booth.

## Active Direction

Status aktif:

- Fokus utama saat ini adalah pengembangan fitur aplikasi photobooth station.
- Scramble/OpenAPI documentation track sedang diparkir sementara.
- Jangan menghabiskan sprint time untuk tambahan Scramble sampai milestone aplikasi stabil.

Prioritas saat ini:

1. Stabilkan flow Android photobooth station berbasis dashboard.
2. Jadikan koneksi Photobooth Station opsional.
3. Tambahkan event mode setelah Android connect ke Photobooth Station.
4. Lanjutkan camera capture, template preview, finish actions, voucher/payment, dan station sync.
5. Resume Scramble/API docs setelah flow station utama stabil.

## Product Understanding

Dafydio Booth adalah sistem photobooth end-to-end:

```text
Android Device -> Session -> Upload -> Edit -> Render -> Print -> Queue -> Agent -> Logs
```

Komponen sistem:

- Android Device: client photobooth untuk splash, dashboard, template, capture, preview, finish, optional station sync, dan auto print.
- Photobooth Station: aplikasi lokal untuk menerima file, mengatur voucher/skip, template, print queue, dan fallback saat internet bermasalah.
- Photobooth Cloud: portal client untuk melihat riwayat session, foto, render, thumbnail, dan re-order print.
- Photobooth Admin: control plane untuk monitoring session, printer, queue, order, dan log.

## Android App Architecture

Android app memakai arsitektur 3 layer:

- `ui`: Compose screen, UDF state, navigation, loading/error/retry.
- `domain`: use case bisnis, model domain, repository contract, validasi dasar.
- `data`: Retrofit API, DTO backend, mapper, DataStore, HTTP error mapping.

Lokasi utama:

- `app/src/main/java/com/errymaricha/dafydiobooth/ui/booth`
- `app/src/main/java/com/errymaricha/dafydiobooth/domain`
- `app/src/main/java/com/errymaricha/dafydiobooth/data`

Prinsip layer:

- UI tidak boleh langsung memanggil API.
- UI mengirim event ke ViewModel.
- ViewModel mengubah `StateFlow<BoothUiState>`.
- Domain tidak bergantung pada Retrofit, Compose, Android UI, atau DTO backend.
- Data layer mempertahankan key JSON backend dengan `@SerialName`.

## Important Android Files

- `MainActivity.kt`: entry point dan dependency wiring manual sementara.
- `ui/booth/BoothUiState.kt`: state tunggal flow booth.
- `ui/booth/BoothViewModel.kt`: pusat UDF dan orchestration use case.
- `ui/booth/BoothApp.kt`: Navigation Compose dan screen MVP.
- `domain/model/BoothModels.kt`: model domain, `BoothResult`, `BoothError`.
- `domain/repository/PhotoboothRepository.kt`: kontrak repository.
- `domain/usecase/PhotoboothUseCases.kt`: use case voucher, payment, session.
- `data/api/PhotoboothContracts.kt`: DTO request/response backend.
- `data/api/PhotoboothApi.kt`: Retrofit endpoint.
- `data/repository/ApiPhotoboothRepository.kt`: implementasi repository.
- `data/local/DeviceConfigStore.kt`: DataStore untuk `device_id` dan `token`.

## Android MVP Flow

Urutan screen:

1. `Splash`
2. `Dashboard`
3. `TemplatePicker` atau `CustomTemplate`
4. `Camera`
5. `CapturePreview`
6. `TemplatePreview`
7. `Finish`
8. `Settings`

Rules:

- `BoothStep` menentukan route aktif.
- Navigation dilakukan dari `BoothApp` berdasarkan `state.step`.
- Jika tidak connect ke Photobooth Station, data hanya lokal dan tidak disimpan ke database.
- Jika connect ke Photobooth Station, semua session/capture/print akan direkap ke database station.
- Default camera memakai kamera bawaan Android.
- External camera memakai jalur Canon DSLR sebagai integrasi lanjutan.
- Finish action minimal: download, print, share.
- Voucher/payment tidak muncul di mode lokal.
- Voucher/payment hanya muncul setelah operator connect ke Photobooth Station lalu launch event.

## Compose UI Guidance

- Project memakai Jetpack Compose, bukan XML layout.
- Target utama desain adalah tablet view, mobile tetap responsive.
- Screen penting wajib punya preview bila layout berubah.
- Preview minimal:
  - Dashboard mobile.
  - Dashboard tablet.
  - Settings tablet.
  - Finish tablet.
- Gunakan state dummy untuk preview, jangan instantiate ViewModel di preview.
- Gunakan komponen kecil yang bisa dipreview tanpa dependency runtime.
- Tablet layout boleh memakai two-column/panel layout.
- Mobile layout harus tetap single-column dan tidak overflow.

## Android Event Flow

Event flow adalah mode online/connected yang aktif setelah Settings berhasil login/connect ke Photobooth Station.

Urutan connected event:

1. Buka `Settings`.
2. Login/connect ke Photobooth Station memakai IP local network, Device ID, dan token.
3. Kembali ke `Dashboard`.
4. Dashboard menampilkan menu tambahan:
   - `Launch Event`
   - `Setting Event`
5. Operator memilih `Launch Event`.
6. App masuk ke event gate:
   - Voucher check.
   - Payment check atau payment quote.
   - Manual payment waiting approval jika metode pembayaran manual.
7. Setelah payment approved atau voucher bypass valid, user lanjut ke pilih template dan camera flow.

Rules event:

- `Launch Event` dan `Setting Event` hanya muncul saat `isStationConnected=true`.
- Voucher/payment berada setelah `Launch Event`, bukan di startup app dan bukan di mode lokal.
- Mode lokal tetap boleh Start Now Photo tanpa voucher/payment dan tanpa database sync.
- Jika manual payment dipilih, Android harus menunggu approval dari Photobooth Station sebelum lanjut capture atau sebelum membuka fitur yang dikunci event.
- Approval manual berasal dari station/admin, bukan dari Android sendiri.
- Status waiting approval harus punya retry/check status.
- Setelah approval, event/session baru boleh direkap ke database station.

## Android Settings Scope

Settings wajib mencakup:

- Photobooth Station connection via local network IP.
- Device ID dan token station.
- Camera source: Android default atau External Canon.
- External camera status: scan, pairing, connected.
- Mirror live view enable/disable.
- Mirror capture enable/disable.
- Image quality.
- Back camera/front camera.
- Denoise foto.
- Timer countdown.
- Countdown audio.
- Shutter sound.
- Default printing.
- Print use Photobooth Station enable/disable.

External Canon reference:

- `https://github.com/errymaricha/canonDSLR`

Catatan: repo reference belum terbaca sebagai repo publik dari environment ini. Implementasi native/SDK Canon harus dibuat di belakang interface camera agar UI tetap stabil.

## Implementation Breakdown

Kerjakan fitur per fase kecil agar setiap fase bisa dites dan tidak mencampur terlalu banyak risiko.

### Phase 1: Dashboard Local Flow

Tujuan:

- App selalu masuk `Splash -> Dashboard`.
- Mode lokal bisa berjalan tanpa station/database.
- User bisa `Start Now Photo -> pilih template -> Camera placeholder -> Capture Preview -> Retake/Done -> Template Preview -> Finish`.
- Dashboard sudah punya responsive mobile/tablet preview.

Coding scope:

- `BoothStep` untuk dashboard-first flow.
- State untuk selected template dan captured photo lokal.
- Compose screen: Splash, Dashboard, TemplatePicker, Camera, CapturePreview, TemplatePreview, Finish.
- Unit/compile test.

Testing:

- `.\gradlew.bat testDevDebugUnitTest`
- `.\gradlew.bat assembleStagingDebug`
- UAT Phase 1 di `docs/UAT_DEVICE_CHECKLIST.md`.

### Phase 2: Settings Persistence

Tujuan:

- Semua setting operator tersimpan dan kembali setelah app restart.
- Status: implemented with DataStore for station, camera, external camera, and printer settings.

Coding scope:

- Perluas DataStore untuk station IP, camera source, mirror flags, image quality, camera side, denoise, countdown, audio, shutter sound, printer flags.
- ViewModel load/save settings.
- UI Settings tetap UDF.

Testing:

- Unit test mapper/settings jika dibuat wrapper.
- Restart app dan cek setting tidak reset.
- UAT Phase 2.

### Phase 3: Station Connection And Connected Dashboard

Tujuan:

- Android bisa connect ke Photobooth Station via IP lokal.
- Dashboard menampilkan `Launch Event` dan `Setting Event` hanya setelah connected.
- Status: implemented with HTTP station connection checker against the configured Station IP.

Coding scope:

- Station connection state.
- HTTP ping/handshake station memakai configured Station IP.
- Auth header tetap memakai token/device id.
- Dashboard conditional menu.
- Error state untuk offline/401/403.

Testing:

- Connect success dengan `PB-DEVICE-01` dan `secret-device-key`.
- Disconnect/offline tidak memunculkan Launch Event.
- Current local station: `http://10.10.116.4:8000/`, run with `php artisan serve --host 10.10.116.4`.
- If the app runs on Android emulator, use `10.0.2.2:8000` in Settings.
- If the app runs on physical device, use `10.10.116.4:8000` in Settings.
- Station IP input accepts raw IP and auto-adds `http://` plus default port `:8000` when no port is provided.
- UAT Phase 3.

### Phase 4: Connected Event Gate

Tujuan:

- Voucher/payment hanya muncul setelah `Launch Event`.
- Manual payment menunggu approval station.

Coding scope:

- Step baru untuk Launch Event, Setting Event, VoucherCheck, PaymentGate, WaitingApproval.
- Use case/repository untuk event launch dan approval status jika backend contract sudah tersedia.
- Poll/check status untuk waiting approval.
- Block lanjut ke camera sebelum approved.

Testing:

- Voucher/payment tidak muncul di mode lokal.
- Manual payment tidak bisa dilewati.
- UAT Phase 4.

### Phase 5: Real Camera Capture

Tujuan:

- Android default camera benar-benar capture file lokal.

Coding scope:

- CameraX.
- Permission camera.
- Countdown.
- Back/front camera.
- Shutter sound/countdown audio.
- Retake mengganti capture.
- Denoise placeholder atau pipeline awal.

Testing:

- Device/emulator camera.
- Permission denied.
- Retake.
- UAT Phase 5.

### Phase 6: External Canon Camera Interface

Tujuan:

- Siapkan integrasi Canon DSLR tanpa merusak Android default camera.

Coding scope:

- Interface `CameraController`.
- Implementasi `AndroidCameraController`.
- Stub/adapter `CanonCameraController`.
- State scan, pairing, connected.
- Mirror live view/capture diterapkan di preview/capture pipeline.

Testing:

- Android default tetap jalan saat Canon disconnected.
- Status external camera berubah sesuai action.
- UAT Phase 6.

### Phase 7: Template Render And Finish Actions

Tujuan:

- Preview template dan action finish bisa dipakai.

Coding scope:

- Template model lokal.
- Render preview sederhana.
- Save/download output.
- Android share sheet.
- Android print default.
- Print via station jika connected.

Testing:

- Output file dibuat.
- Share intent muncul.
- Print default intent muncul.
- Station print blocked saat disconnected.
- UAT Phase 7.

### Phase 8: Station Sync Queue

Tujuan:

- Data hanya direkap saat connected dan tidak hilang saat network bermasalah.

Coding scope:

- Local queue.
- Sync session/capture/finish event.
- Idempotency key.
- Retry/backoff.
- Duplicate prevention.

Testing:

- Mode lokal tidak hit database.
- Connected mode sync.
- Offline queue lalu online sync.
- UAT Phase 8.

## Android Device Contract

Contract version saat ini:

- `2026-04-15`

Endpoint device:

- `POST /api/device/auth`
- `POST /api/device/vouchers/verify`
- `POST /api/device/payment-quote`
- `POST /api/device/sessions`
- `GET /api/device/sessions/{session}/payment-check`
- `POST /api/device/sessions/{session}/confirm-payment`

Endpoint `POST /api/device/auth` dipakai saat Settings melakukan connect ke Photobooth Station. Input token di Settings adalah API key device awal. Jika auth sukses, response Sanctum token disimpan dan dipakai sebagai bearer token untuk endpoint protected berikutnya.

Endpoint event/payment berada di jalur connected event, setelah `Launch Event`. Endpoint ini tidak boleh dipanggil saat user menjalankan mode lokal tanpa station connection.

Field yang harus dijaga:

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

Contract rules:

- Jangan rename key backend tanpa update backend, DTO, mapper, test, dan UAT.
- Tambahkan field baru di `PhotoboothContracts.kt` dengan `@SerialName("backend_key")`.
- Map ke domain hanya field yang dipakai app.
- Error `401`, `403`, dan `422` harus ditangani eksplisit.
- Upload/complete wajib ditolak bila payment masih required atau unpaid.

## Android Payload Reference

Verify voucher:

```json
{
  "contract_version": "2026-04-15",
  "device_id": "PB-DEVICE-01",
  "voucher_code": "PROMO-E2E-20K",
  "voucher_type": "promo"
}
```

Payment quote:

```json
{
  "contract_version": "2026-04-15",
  "device_id": "PB-DEVICE-01",
  "voucher_code": "PROMO-E2E-20K",
  "voucher_type": "promo",
  "session_type": "photo"
}
```

Create session:

```json
{
  "contract_version": "2026-04-15",
  "device_id": "PB-DEVICE-01",
  "voucher_code": "PROMO-E2E-20K",
  "voucher_type": "promo",
  "quote_id": "quote-1",
  "session_type": "photo"
}
```

Confirm payment:

```json
{
  "contract_version": "2026-04-15",
  "device_id": "PB-DEVICE-01"
}
```

Catatan: backend full system pernah memakai payload payment detail seperti `payment_ref`, `payment_method`, `amount`, dan `currency`. Jika contract final payment berubah ke format itu, update DTO Android dan test contract sekaligus.

## Backend And Station Context

Backend/station memiliki area utama:

- Device API
- Editor API
- Print Agent API
- Voucher management
- Template management
- Session editor
- Render pipeline
- Print queue
- Printer binding
- Audit log

Core backend rules:

- Controllers thin.
- Gunakan FormRequest untuk validasi.
- Gunakan DB transaction untuk critical flow.
- Prevent duplicate actions dengan idempotency.
- Enforce ownership, terutama printer binding.
- Jangan rename model/table/status secara acak.

Status penting:

- `PhotoSession`: `created`, `uploaded`, `editing`, `ready_print`, `queued_print`, `failed_print`, `printed`
- `PrintOrder`: `queued`, `printing`, `failed`, `printed`
- `PrintQueueJob`: `pending`, `processing`, `failed`, `completed`
- `Printer`: `ready`, `printing`, `offline`, `error`, `paused`

Security context:

- Auth memakai Sanctum.
- Role utama: `admin`, `editor`, `print-agent`.
- Print-agent wajib terikat ke `printer_id`.

## Build Variants

Android flavor:

- `dev`
- `staging`
- `prod`

Konfigurasi:

- `app/build.gradle.kts`
- `BuildConfig.BASE_URL`

Saat mengganti backend URL:

- Update `buildConfigField("String", "BASE_URL", ...)`.
- Pastikan URL berakhiran `/` karena Retrofit membutuhkan trailing slash.

## Testing

Android unit test:

```powershell
.\gradlew.bat testDevDebugUnitTest
```

Test Android yang wajib dijaga:

- Mapper DTO ke domain.
- Use case validation.
- Integration-style flow voucher -> payment quote -> create session.
- Error mapping `401`, `403`, `422`.
- Upload blocked saat unpaid.
- Dashboard-first flow compile.
- Settings state tidak merusak station auth.
- Launch Event hanya muncul saat station connected.
- Voucher/payment gate hanya aktif pada connected event.
- Manual payment waiting approval tidak bisa di-approve sepihak dari Android.

Backend/station test yang wajib dijaga saat area terkait disentuh:

- Seeder repeat-run/idempotency.
- Voucher verify/quote edge cases.
- Payment gate regression.
- Upload blocked before paid.
- Payment confirmed unlocks upload.
- Print queue retry/idempotency.
- Auth/ownership tests.

## Current Seeded Demo Context

Default seeded accounts:

- Admin: `admin@photobooth.local`
- Print Agent: `agent@photobooth.local`

Default seeded resources:

- Station: `STATION-01`
- Device: `PB-DEVICE-01`
- Printer: `PRINTER-01`
- Template: `TPL-2SLOT`

Demo workflow data includes:

- session lifecycle,
- thumbnails,
- rendered output,
- print orders,
- queue jobs,
- print logs,
- voucher examples,
- template slot examples.

## AI Agent Working Rules

- Baca struktur file sebelum mengedit.
- Ikuti arsitektur dan pola yang sudah ada.
- Jangan taruh API call di Compose screen.
- Jangan taruh DTO backend di domain.
- Jangan ubah key contract tanpa test dan update dokumen.
- Pertahankan UDF: UI event -> ViewModel -> use case -> repository -> state.
- Tambahkan test untuk perubahan contract, mapper, use case, payment, atau print flow.
- Jangan hapus handling `401`, `403`, `422`.
- Jangan jadikan station connection wajib untuk mode lokal.
- Jangan izinkan sync database saat station belum connected.
- Jangan tampilkan voucher/payment sebelum `Launch Event`.
- Jangan biarkan manual payment lanjut sebelum station approval.
- Jangan lanjutkan Scramble enhancements kecuali sprint aktif meminta.

## Definition Of Done

- Route/flow yang disentuh berjalan.
- Tidak ada duplicate data/action pada flow kritikal.
- Status transition benar.
- Auth dan ownership aman.
- Unit/feature test relevan lulus.
- Contract Android tetap konsisten.
- UAT checklist diperbarui jika behavior device berubah.

## Related Documents

- `ROADMAP.md`: arah pengembangan produk.
- `docs/UAT_DEVICE_CHECKLIST.md`: checklist UAT device.
