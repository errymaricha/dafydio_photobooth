# CameraX Android Guide (AI Reference)

Dokumen ini jadi acuan implementasi CameraX di halaman `Camera` untuk mode kamera Android.

## Goals

1. `Tap-to-focus + exposure metering`
2. `Aspect ratio lock` sesuai template (`4:3`, `16:9`, atau rasio canvas template)
3. `Grid overlay + safe area template`
4. `Countdown UX` yang jelas (overlay angka besar + beep + status)
5. `Post-capture quick review` yang lebih jelas (`Retake` / `Use Photo`)

## Implementation Notes

### 1) Tap-to-focus + exposure metering

- Pakai `FocusMeteringAction` dari CameraX.
- Saat user tap preview:
  - buat metering point dari `previewView.meteringPointFactory`.
  - trigger `camera.cameraControl.startFocusAndMetering(action)`.
- Tampilkan indikator fokus sementara di titik tap (ring kecil 600-800ms).

### 2) Aspect ratio lock

- Ambil rasio dari:
  - `selectedTemplateCanvasWidth` / `selectedTemplateCanvasHeight` jika tersedia.
  - fallback `4:3`.
- Untuk binding CameraX:
  - gunakan `AspectRatio.RATIO_4_3` jika rasio dekat 1.33
  - gunakan `AspectRatio.RATIO_16_9` jika rasio dekat 1.77
- Untuk UI preview:
  - gunakan `Modifier.aspectRatio(ratio)` agar framing konsisten dengan template.

### 3) Grid overlay + safe area template

- Overlay grid 3x3 di atas preview.
- Jika `selectedTemplateSlots` tersedia:
  - render safe area box berdasarkan koordinat slot terhadap canvas template.
  - gunakan stroke kontras tipis.

### 4) Countdown UX

- Saat capture:
  - tampilkan overlay gelap transparan.
  - angka countdown besar di tengah.
  - label status (`Get ready...`, `Capturing...`).
- Beep tiap tick countdown jika `countdownAudio=true`.

### 5) Post-capture quick review

- Di screen preview capture:
  - tetap tampilkan foto hasil.
  - tombol primer: `Use Photo`
  - tombol sekunder: `Retake`
  - copy teks jelas untuk keputusan user.

## UX Rules

- Jika kamera belum siap / permission belum ada:
  - tampilkan status jelas + CTA `Allow Camera`.
- Jangan trigger capture saat countdown berjalan.
- Jangan hide error; tampilkan pesan ringkas.

## Tech Constraints

- Kompatibel dengan arsitektur state saat ini:
  - `BoothUiState`
  - `BoothViewModel`
  - `CapturePreviewScreen`
- Hindari menambah dependency baru bila tidak perlu.
