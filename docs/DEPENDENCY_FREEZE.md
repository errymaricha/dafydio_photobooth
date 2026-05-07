# Dependency Freeze Baseline

Tanggal freeze: 2026-05-06

## Tujuan
Menjaga build stabil untuk siklus release berikutnya, terutama terkait:
- kompatibilitas 16KB page size
- stabilitas CameraX capture flow
- integrasi worker + networking station layer

## Versi Baseline (jangan diubah tanpa validasi)
- AGP: `9.1.1`
- Kotlin: `2.2.10`
- Compose BOM: `2026.02.01`
- Retrofit: `3.0.0`
- OkHttp: `5.3.2`
- Coroutines: `1.10.2`
- CameraX: `1.5.0`  (kritis untuk compliance 16KB)
- WorkManager: `2.10.0`
- Room runtime/ktx: `2.8.2`
- Security Crypto: `1.1.0`

Sumber versi: `gradle/libs.versions.toml`

## Rule Perubahan Dependency
Sebelum update dependency kritikal:
1. `./gradlew.bat --no-daemon :app:compileDevDebugKotlin`
2. `./gradlew.bat --no-daemon :app:testDevDebugUnitTest`
3. `./gradlew.bat --no-daemon :app:assembleDevDebug`
4. Uji device untuk:
   - Start Now Photo local-only
   - Heartbeat manual
   - Camera capture + safe area
5. Verifikasi Play pre-check 16KB (untuk dependency native).

## Dependency Kritis Native
- `androidx.camera:*` membawa native `.so` seperti `libimage_processing_util_jni.so`.
- Setiap upgrade CameraX wajib re-cek warning 16KB.

## Catatan
Jika perlu roll-forward dependency, buat changelog singkat di PR:
- alasan update
- risiko
- hasil verifikasi build/test/device/pre-check
