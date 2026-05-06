# Station Layer Architecture

Tanggal acuan: 2026-05-06

## Tujuan
Dokumen ini merangkum layer station client Android yang dipakai untuk koneksi ke Dafydio Photobooth Station, heartbeat, dan offline queue ringan.

## Lokasi Kode
- `app/src/main/java/com/errymaricha/dafydiobooth/station/model`
- `app/src/main/java/com/errymaricha/dafydiobooth/station/network`
- `app/src/main/java/com/errymaricha/dafydiobooth/station/repository`
- `app/src/main/java/com/errymaricha/dafydiobooth/station/local`
- `app/src/main/java/com/errymaricha/dafydiobooth/station/worker`

## Komponen Utama

### 1) API Service
- `DeviceApiService`
- Endpoint utama: auth, heartbeat, templates, voucher verify, payment quote, sessions, payment-check, upload photo, complete session, upload rendered-output.

### 2) Network Core
- `safeApiCall` mengubah exception ke `AppResult` (`Unauthorized`, `Forbidden`, `Validation`, `Server`, `Network`, `Unknown`).
- Interceptor menambahkan header:
  - `Accept: application/json`
  - `X-Requested-With: XMLHttpRequest`
- Base URL dinamis via `baseUrlProvider`.

### 3) Token Security
- `SecureTokenStore` (`EncryptedSharedPreferences`).
- Dipakai oleh station repositories.
- Token disimpan saat login sukses dan dihapus saat disconnect.

### 4) Repositories
- `AuthRepository`: login + token management.
- `DeviceRepository`: heartbeat + flush offline queue.
- `TemplateRepository`: refresh template + observe cache.
- `SessionRepository`: voucher/quote/session/check + upload photo + complete session.

### 5) Offline Queue
Saat request gagal, payload disimpan terstruktur (`OfflineQueuePayload`) di queue lokal.
Tipe payload saat ini:
- `Heartbeat`
- `SessionPhotoUpload`
- `SessionComplete`

Worker `OfflineQueueWorker` melakukan replay queue periodik.

### 6) Worker
- `HeartbeatWorker`
  - Periodic heartbeat + one-time immediate enqueue.
  - Simpan snapshot status heartbeat.
- `OfflineQueueWorker`
  - Flush queue payload yang tertunda.
- `AppWorkerFactory`
  - Inject dependency repository/store ke worker.

### 7) Bootstrap
- `StationClientBootstrap`
  - Inisialisasi API/repository/store.
  - Menyediakan `workerConfiguration()`.
  - Menjalankan `startHeartbeat()`.

## Integrasi ke App
- `DafydioApplication` implement `Configuration.Provider` untuk WorkManager.
- `MainActivity` sinkronkan station URL ke bootstrap.
- `BoothViewModel` konsumsi heartbeat status store untuk ditampilkan di Settings.

## Local-Only Start Now Photo (Rule Kritis)
- `Start Now Photo` berjalan local-only.
- Tidak boleh mengirim data ke station pada flow ini:
  - upload capture
  - complete session
  - upload rendered output

## Batasan Saat Ini
- Cache template/queue station layer memakai in-memory DAO (bukan Room persistent) agar startup stabil di konfigurasi project sekarang.
- Jika ingin persistent queue penuh, perlu aktivasi Room codegen yang kompatibel dengan konfigurasi AGP built-in Kotlin pada repo ini.
