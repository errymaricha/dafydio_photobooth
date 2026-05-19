# Go-Live Checklist Real Bisnis Photobooth

Checklist ini dipakai untuk menentukan apakah Dafydio Booth sudah layak dipakai di event komersial real.

## 1) Readiness Produk

- [ ] Flow utama lulus end-to-end: `Launch Event -> Payment -> Template -> Capture -> Finish`.
- [ ] Flow tanpa voucher lulus.
- [ ] Flow voucher valid/tidak valid lulus.
- [ ] Flow manual payment approved/rejected lulus.
- [ ] Semua create session membawa `event_id`.
- [ ] `Setting Event` menyimpan default: event aktif, default WA, default voucher, allowed template.
- [ ] Kamera Android internal stabil.
- [ ] Kamera eksternal (jika dipakai) stabil pada perangkat target.
- [ ] Render template final sesuai desain (posisi slot, overlay, kualitas).

## 2) Readiness Infrastruktur Station

- [ ] Endpoint station aktif dan bisa diakses device Android di jaringan lokal.
- [ ] Endpoint auth/device lulus test:
  - [ ] `POST /api/device/auth`
  - [ ] `GET /api/device/master-data`
  - [ ] `GET/POST/PATCH /api/device/events`
  - [ ] `POST /api/device/sessions`
  - [ ] `GET /api/device/sessions/{id}/payment-check`
- [ ] Database station sehat (backup, disk space cukup, migration terbaru).
- [ ] NTP/jam server benar.
- [ ] Printer service dan queue worker station aktif.

## 3) Uji Ketahanan Lapangan

- [ ] Soak test minimal 4 jam nonstop tanpa crash kritikal.
- [ ] Simulasi 100 session beruntun (atau target realistis per event).
- [ ] Simulasi internet/cloud putus:
  - [ ] session lokal tetap jalan.
  - [ ] retry sync aman saat koneksi pulih.
- [ ] Simulasi station restart saat event:
  - [ ] app recover.
  - [ ] tidak terjadi double session/payment.
- [ ] Simulasi printer error:
  - [ ] queue tidak hilang.
  - [ ] operator dapat pesan error jelas.

## 4) Kualitas Operasional

- [ ] Operator mode sederhana tersedia (aksi utama jelas, minim klik).
- [ ] Ada SOP ketika:
  - [ ] payment pending lama,
  - [ ] printer macet,
  - [ ] kamera gagal,
  - [ ] app freeze.
- [ ] Ada fallback manual untuk melanjutkan layanan pelanggan.
- [ ] Waktu rata-rata per session sesuai target bisnis.

## 5) Monitoring dan Logging

- [ ] Semua error penting tercatat dengan `session_id`.
- [ ] Log request gagal menyertakan endpoint + status code + reason.
- [ ] Ada metrik minimal:
  - [ ] jumlah session sukses/gagal,
  - [ ] median waktu approval payment,
  - [ ] print success rate,
  - [ ] retry rate sync/upload.
- [ ] Tim tahu lokasi log dan cara ambil log saat insiden.

## 6) Keamanan dan Data

- [ ] Token/auth tidak hardcoded di APK release.
- [ ] Data sensitif tidak bocor ke UI/log publik.
- [ ] Retensi file foto lokal punya aturan jelas.
- [ ] Consent/privacy notice siap jika diperlukan regulasi setempat.

## 7) Readiness Perangkat Event

- [ ] Perangkat Android utama + cadangan siap.
- [ ] Charger, kabel data, OTG, extension listrik siap.
- [ ] Kamera (internal/eksternal) + memory + baterai cadangan siap.
- [ ] Printer + kertas + tinta/ribbon cadangan siap.
- [ ] Jaringan lokal venue dites di lokasi H-1/H-0.

## 8) Release Governance

- [ ] Build release final dibekukan (version tag jelas).
- [ ] UAT sign-off teknis + operasional.
- [ ] Rollback plan tersedia (APK versi stabil sebelumnya).
- [ ] Hotfix SOP tersedia (siapa, kapan, bagaimana deploy).

## 9) Go/No-Go Gate

Go-live hanya jika:

- [ ] Semua checklist kritikal (auth/session/payment/capture/print) lulus.
- [ ] Tidak ada blocker severity tinggi.
- [ ] Tim operator dan on-call engineer sudah standby.

## 10) Post Go-Live (H+1 sampai H+7)

- [ ] Daily review metrik produksi.
- [ ] Catat insiden + root cause + tindakan perbaikan.
- [ ] Prioritaskan bugfix berdampak pendapatan/operasional.
- [ ] Update checklist berdasarkan temuan event nyata.
