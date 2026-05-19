# Playbook Bisnis Photobooth (Satu File)

Dokumen ini menggabungkan semua catatan non-developing app agar mudah dibaca menyeluruh.

## Daftar Isi

1. [Aturan Dasar Operasional](#aturan-dasar-operasional)
2. [SOP Harian Event](#sop-harian-event)
3. [Form Harga dan Paket](#form-harga-dan-paket)
4. [Template WA Booking](#template-wa-booking)
5. [Template Kontrak Sederhana](#template-kontrak-sederhana)
6. [Go-Live Checklist Real Bisnis](#go-live-checklist-real-bisnis)
7. [Template Rekap Event](#template-rekap-event)

## Aturan Dasar Operasional

1. Jangan berangkat event tanpa checklist perangkat.
2. Jangan mulai melayani tamu sebelum test 1 sesi penuh berhasil.
3. Semua transaksi harus tercatat.
4. Jika ada error, utamakan layanan tamu dulu.
5. Setelah event selesai, selalu rekap hasil dan masalah.

## SOP Harian Event

### Sebelum Event

- Konfirmasi tanggal, lokasi, jam, PIC.
- Cek paket customer dan include.
- Siapkan alat inti:
  - Android utama + cadangan
  - charger + kabel + terminal
  - kamera (jika ada) + baterai
  - printer + media print cadangan
- Nyalakan station dan test 1 flow penuh:
  - create event -> create session -> capture -> print -> finish

### Saat Event

- Gunakan flow layanan tetap:
  1. Cek event aktif
  2. Input WA/voucher (jika perlu)
  3. Jalankan sesi foto
  4. Pastikan hasil print/share
- Catat tambahan transaksi langsung.
- Tiap 30-60 menit cek kondisi alat + media print.

### Setelah Event

- Stop sesi baru.
- Pastikan pending sync selesai.
- Rekap:
  - jumlah sesi
  - jumlah print
  - omzet
  - kendala utama

## Form Harga dan Paket

> Isi angka sesuai kondisi kamu. Untuk Klaten bisa mulai dari angka realistis event-based.

### Struktur Paket

- Basic (2 jam): Rp __________
- Standard (3 jam): Rp __________
- Premium (4-5 jam): Rp __________

### Add-On

- Extra 1 jam: Rp __________
- Extra print: Rp __________
- Backdrop premium: Rp __________
- Guestbook station: Rp __________

### Rumus Aman Harga

`Harga minimum = total biaya operasional event + target laba`

Biaya operasional contoh:
- transport
- operator/helper
- media print
- maintenance alat
- biaya tak terduga

### Skema Pembayaran Disarankan

- DP 30%-50% saat booking
- Pelunasan H-1 atau sebelum layanan mulai
- Overtime per jam jelas di awal

## Template WA Booking

### Balasan Pertama

Halo Kak [Nama], terima kasih sudah hubungi [Brand].
Boleh info:
1. Tanggal acara
2. Lokasi
3. Jenis acara
4. Durasi yang diinginkan

### Kirim Penawaran

Halo Kak [Nama], opsi paket untuk [Jenis Acara]:
- Basic [durasi] - Rp [harga]
- Standard [durasi] - Rp [harga]
- Premium [durasi] - Rp [harga]

### Follow-up

Halo Kak [Nama], izin follow up.
Untuk tanggal [Tanggal], slot saat ini [tersedia/terbatas].

### Konfirmasi Booking

Booking:
- Tanggal: [Tanggal]
- Lokasi: [Lokasi]
- Paket: [Paket]
- Total: Rp [Total]

Aktif setelah DP [x%] sebesar Rp [Nominal].

## Template Kontrak Sederhana

Bagian minimal wajib:

- Data Vendor dan Klien
- Nama acara, lokasi, tanggal, durasi
- Paket dan include
- Nilai kontrak
- DP dan pelunasan
- Overtime dan add-on
- Reschedule dan pembatalan
- Force majeure
- Tanda tangan kedua pihak

Checklist kontrak cepat:
- [ ] Angka total benar
- [ ] Deadline pelunasan jelas
- [ ] Kebijakan pembatalan tertulis
- [ ] Overtime tertulis

## Go-Live Checklist Real Bisnis

### Produk

- [ ] Flow launch -> payment -> capture -> finish lulus
- [ ] Event aktif terpilih dan `event_id` terbawa saat create session
- [ ] Printer + share flow lulus

### Infrastruktur

- [ ] Endpoint station inti sehat
- [ ] DB station sehat + storage cukup
- [ ] Waktu server benar

### Ketahanan

- [ ] Soak test minimal 4 jam
- [ ] Simulasi internet/station putus
- [ ] Simulasi printer fail + recovery

### Operasional

- [ ] SOP panic plan dipahami operator
- [ ] Device cadangan siap
- [ ] Stok media print cadangan siap

### Gate Go/No-Go

Go-live jika:
- [ ] Semua item kritikal lulus
- [ ] Tidak ada blocker severity tinggi

## Template Rekap Event

Nama Event:
Tanggal:
Lokasi:
Durasi:

Jumlah sesi:
Jumlah print:
Omzet kontrak:
Omzet tambahan:
Biaya operasional:
Laba bersih:

Masalah utama:
Solusi saat event:
Perbaikan event berikutnya:

---

### Referensi Dokumen Asal

- [SOP_BISNIS_PHOTOBOOTH_SIMPel.md](C:/Users/erm/StudioProjects/DafydioBooth/docs/SOP_BISNIS_PHOTOBOOTH_SIMPel.md)
- [FORM_HARGA_DAN_PAKET_PHOTOBOOTH.md](C:/Users/erm/StudioProjects/DafydioBooth/docs/FORM_HARGA_DAN_PAKET_PHOTOBOOTH.md)
- [TEMPLATE_WA_BOOKING_PHOTOBOOTH.md](C:/Users/erm/StudioProjects/DafydioBooth/docs/TEMPLATE_WA_BOOKING_PHOTOBOOTH.md)
- [TEMPLATE_KONTRAK_Sederhana_PHOTOBOOTH.md](C:/Users/erm/StudioProjects/DafydioBooth/docs/TEMPLATE_KONTRAK_Sederhana_PHOTOBOOTH.md)
- [GO_LIVE_CHECKLIST_REAL_BISNIS.md](C:/Users/erm/StudioProjects/DafydioBooth/docs/GO_LIVE_CHECKLIST_REAL_BISNIS.md)
