# Dafydio Booth

Dafydio Booth adalah aplikasi Android untuk photobooth station. Aplikasi ini mendukung mode lokal tanpa database dan mode connected dengan Photobooth Station di jaringan lokal.

## Current Android Flow

Flow utama saat ini:

```text
Splash
-> Dashboard
-> Start Now Photo
-> Pilih Template
-> Camera
-> Capture Preview
-> Retake atau Done
-> Template Preview
-> Finish
-> Download / Print / Share
```

Mode connected:

```text
Settings
-> Connect Photobooth Station
-> Dashboard menampilkan Launch Event dan Setting Event
-> Launch Event
-> Input ID Customer / ID Pelanggan optional
-> Voucher/payment gate
-> Template/camera flow
```

Voucher/payment hanya berjalan setelah device connect ke Photobooth Station dan operator membuka `Launch Event`.
Field `ID Customer / ID Pelanggan` di Launch Event memakai nomor WA yang sudah terdaftar di station. Jika dikosongkan, backend station memakai default customer yang disiapkan di Photobooth Station.
Paket launch sudah tersedia untuk login device, sync pricing dari master-data, hitung final amount, dan open manual session dengan `customer_whatsapp`, `payment_method=manual`, serta `additional_print_count`.

## Tech Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- Coroutines + Flow
- DataStore Preferences
- Retrofit
- OkHttp
- Kotlin Serialization
- Coil

## Architecture

Aplikasi mengikuti 3 layer:

- `ui`: Compose screen, UDF state, navigation.
- `domain`: use case, domain model, repository contract.
- `data`: API client, DTO contract, mapper, local storage.

Lokasi utama:

```text
app/src/main/java/com/errymaricha/dafydiobooth/ui/booth
app/src/main/java/com/errymaricha/dafydiobooth/domain
app/src/main/java/com/errymaricha/dafydiobooth/data
```

## Build Variants

Variant tersedia:

- `dev`
- `staging`
- `prod`

Staging saat ini mengarah ke Photobooth Station lokal:

```text
http://10.10.116.4:8000/
```

Untuk menjalankan Laravel station lokal:

```powershell
php artisan serve --host 10.10.116.4
```

Jika IP jaringan berubah, update `BASE_URL` staging di:

```text
app/build.gradle.kts
```

## Device Credential For Local Testing

Seeder station menggunakan:

```text
Device ID: PB-DEVICE-01
Token: secret-device-key
```

Android connect ke station dengan:

```http
POST /api/device/auth
{
  "device_code": "PB-DEVICE-01",
  "api_key": "secret-device-key"
}
```

Setelah auth sukses, Android menyimpan token Sanctum dari response dan request berikutnya memakai:

```http
Authorization: Bearer <sanctum-token>
X-Device-Id: PB-DEVICE-01
Accept: application/json
```

Android menyimpan API key device dan Sanctum token di field berbeda. Field Settings `API Key / Token` tetap berisi API key awal, sedangkan token Sanctum disimpan internal sebagai `authToken`.

Master data station tersedia dari:

```http
GET /api/device/master-data
Authorization: Bearer <sanctum-token>
```

## Commands

Run unit test:

```powershell
.\gradlew.bat testDevDebugUnitTest
```

Build staging APK:

```powershell
.\gradlew.bat assembleStagingDebug
```

APK output:

```text
app/build/outputs/apk/staging/debug/app-staging-debug.apk
```

Install and launch on emulator:

```powershell
C:\Android\new_sdk\platform-tools\adb.exe -s emulator-5554 install -r app\build\outputs\apk\staging\debug\app-staging-debug.apk
C:\Android\new_sdk\platform-tools\adb.exe -s emulator-5554 shell monkey -p com.errymaricha.dafydiobooth.staging -c android.intent.category.LAUNCHER 1
```

Install and launch on physical device:

```powershell
C:\Android\new_sdk\platform-tools\adb.exe devices
C:\Android\new_sdk\platform-tools\adb.exe -s <device-id> install -r app\build\outputs\apk\staging\debug\app-staging-debug.apk
C:\Android\new_sdk\platform-tools\adb.exe -s <device-id> shell monkey -p com.errymaricha.dafydiobooth.staging -c android.intent.category.LAUNCHER 1
```

## Implementation Phases

Current phase status:

- Phase 1: Dashboard Local Flow. Implemented.
- Phase 2: Settings Persistence. Implemented.
- Phase 3: Station Connection And Connected Dashboard. Implemented.
- Phase 4: Connected Event Gate. Implemented, including optional customer ID payload.
- Phase 5: Real Camera Capture.
- Phase 6: External Canon Camera Interface.
- Phase 7: Template Render And Finish Actions.
- Phase 8: Station Sync Queue.

## Documentation

Project docs:

- `AI_AGENT_GUIDE.md`: panduan kerja AI agent dan aturan arsitektur.
- `ROADMAP.md`: roadmap produk dan breakdown fase implementasi.
- `docs/UAT_DEVICE_CHECKLIST.md`: checklist UAT per fase.

## Important Rules

- Mode lokal tidak boleh menulis database station.
- `Launch Event` dan `Setting Event` hanya muncul setelah station connected.
- Voucher/payment hanya muncul setelah `Launch Event`.
- Manual payment harus menunggu approval dari Photobooth Station.
- Jangan taruh API call langsung di Compose screen.
- Jangan ubah key contract backend tanpa update DTO, mapper, test, dan dokumen.
