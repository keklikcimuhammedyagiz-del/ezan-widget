# Ezan Vakti – Android Widget

Ücretsiz şekilde APK’ya çevirip telefonuna kurabileceğin basit ezan vakti uygulaması + **gerçek ana ekran widget’ı**.

## Ne var?

- Uygulama içinde günlük vakitler
- Ana ekrana eklenebilen **gerçek widget**
- Konum izni ile otomatik şehir
- Kaynak: cihaz içi astronomik hesap (internet şart değil)

## Ücretsiz APK yapma adımları (Windows / Mac / Linux)

### 1. Android Studio kur (ücretsiz)
https://developer.android.com/studio  
İndir → kur → ilk açılışta SDK’ları indirmesine izin ver.

### 2. Projeyi aç
1. Android Studio’yu aç
2. **Open** → bu `EzanWidget` klasörünü seç
3. Birkaç dakika Gradle’ın indirmesini bekle

### 3. Telefonda USB hata ayıklama aç
- Ayarlar → Telefon hakkında → Yapı numarasına 7 kez bas
- Geliştirici seçenekleri → **USB hata ayıklama** aç

### 4. APK oluştur
- Üst menü: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- Bitince “locate” / “find” ile APK’yı bul
- Dosya genelde:  
  `app/build/outputs/apk/debug/app-debug.apk`

### 5. Telefona kur
- APK’yı telefona kopyala (USB, Google Drive, Telegram…)
- Dosya yöneticisinden aç → **Yükle**
- “Bilinmeyen kaynaklar” izni isteyebilir → izin ver

### 6. Widget ekle
1. Ana ekranda **boş yere uzun bas**
2. **Widget’lar** seç
3. **Ezan Vakitleri** bul
4. Sürükle bırak

## Notlar

- İlk açılışta konum izni isteyecek.
- Widget yaklaşık 30 dakikada bir kendini günceller.
- Daha doğru Diyanet verisi istersen sonradan API eklenebilir.

## Klasör yapısı

```
EzanWidget/
├── app/
│   └── src/main/
│       ├── java/com/ezan/widget/   ← Kotlin kodları
│       ├── res/                    ← arayüz + widget layout
│       └── AndroidManifest.xml
├── build.gradle
└── README.md
```
