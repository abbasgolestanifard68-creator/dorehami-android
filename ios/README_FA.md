# دورهمی — نسخهٔ iOS (SwiftUI)

همان محدودهٔ نسخهٔ اندروید: ورود، ثبت‌نام (با تقویم شمسی و قوانین رمز)، نشست امن در Keychain، لیست گفتگوها و صفحهٔ چت روی دادهٔ نمونه. بدون وابستگی خارجی.

**وضعیت:** این کد هنوز هیچ‌جا کامپایل نشده است (محیط ساختش Xcode روی macOS لازم دارد). اولین اجرای CI یا Xcode اولین آزمون واقعی است.

## ساخت روی مک
```bash
brew install xcodegen
cd ios
xcodegen generate        # فایل Dorehami.xcodeproj را می‌سازد (در مخزن نیست)
open Dorehami.xcodeproj
```
سپس در Xcode شمارهٔ شبیه‌ساز را انتخاب و Run کنید. در نسخهٔ Debug دکمهٔ «ورود آزمایشی» بدون سرور به صفحهٔ اصلی می‌برد.

## آزمایش بدون مک (گیت‌هاب Actions)
فایل `docs/ios-ci-workflow.yml` را در مسیر `.github/workflows/ios.yml` بگذارید. این ورک‌فلو روی رانر macOS پروژه را می‌سازد و تست‌های واحد را روی شبیه‌ساز اجرا می‌کند. توجه: رانر macOS در مخازن Private ده برابر دقیقهٔ اندروید/لینوکس از سهمیهٔ رایگان مصرف می‌کند؛ برای همین ورک‌فلو فقط با تغییر فایل‌های `ios/` اجرا می‌شود.

## نصب روی آیفون و انتشار
نصب روی آیفون واقعی و TestFlight/App Store نیاز به **Apple Developer Program** (حدود ۹۹ دلار در سال) و گواهی امضا دارد. بدون آن فقط شبیه‌ساز ممکن است. کلیدها و گواهی‌ها (`.p12`, `.mobileprovision`) هرگز در مخزن قرار نگیرند (در `.gitignore` هستند)؛ برای CI از GitHub Secrets استفاده شود.

## ساختار
```
ios/
  project.yml                  مشخصات پروژه (XcodeGen)
  Dorehami/
    DorehamiApp.swift          نقطهٔ ورود + RootView
    Config.swift               آدرس سرور و پرچم useFakeChat
    Core/                      Validators, Jalali, SessionStore (Keychain), Api
    Chat/                      مدل‌ها، Fake و Api repository
    Features/{Auth,Home,Chat}/ صفحات SwiftUI
    UI/Theme.swift             رنگ‌های برند
  DorehamiTests/               تست‌های واحد
```

## فرض‌های مشترک با اندروید
همان قرارداد API (docs/API_CONTRACT.md): شمارهٔ موبایل `09123456789`، تاریخ تولد میلادی، مسیر `/auth/refresh`. همه هنوز نیازمند تأیید بک‌اندند.
