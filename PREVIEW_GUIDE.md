# Preview Guide

Panduan ini dipakai untuk menjaga konsistensi preview UI Jetpack Compose di project ini.

## Struktur Folder

- File UI production:
  - `app/src/main/java/com/errymaricha/dafydiobooth/ui/booth/*.kt`
- File preview:
  - `app/src/main/java/com/errymaricha/dafydiobooth/ui/booth/preview/*.kt`
- Shared preview state:
  - `app/src/main/java/com/errymaricha/dafydiobooth/ui/booth/preview/PreviewStateProvider.kt`

## Naming Preview

Format nama preview:

`"<Page/Section> <Device> - <State>"`

Contoh:
- `Dashboard Mobile - Connected`
- `Dashboard Tablet - Loading Error`
- `Camera Tablet`
- `Template Preview Mobile`

## Device Preset

Gunakan preset konsisten:

- Mobile:
  - `widthDp = 390`
  - `heightDp = 844`
- Tablet:
  - `widthDp = 1280`
  - `heightDp = 800`

Selalu set:
- `showBackground = true`

## State Matrix Minimum

Untuk setiap halaman utama, minimal sediakan:

1. Mobile default
2. Tablet default
3. State penting (pilih yang relevan):
   - Connected / Disconnected
   - Loading
   - Error
   - Empty data

## Aturan Penempatan

1. Jangan simpan `@Preview` di file page production.
2. Simpan preview di package `ui.booth.preview`.
3. Reuse state dari `PreviewStateProvider`, jangan duplikasi state literal di banyak file.
4. Jika butuh state khusus, buat turunan dengan `.copy(...)`.

## Checklist Saat Menambah Page Baru

1. Tambah composable production di file page.
2. Tambah state dasar di `PreviewStateProvider` bila belum ada.
3. Tambah file preview atau section preview baru di folder `preview`.
4. Tambah preview Mobile + Tablet.
5. Tambah minimal satu preview state khusus (Loading/Error/Empty jika relevan).
6. Jalankan compile check:
   - `.\gradlew.bat :app:compileDevDebugKotlin -q`
