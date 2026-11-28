# نقشه نشان (Neshan) — مجوز و کلید debug

مجوز SDK نشان به **نام بسته** و **اثر انگشت SHA1 کلید امضا** بسته است. از platform.neshan.org گرفته می‌شود.

## نسخهٔ debug (همان APK آزمایشی گیت‌هاب Actions)
- نام بسته: `ir.dorehami.app.debug`  (کلید applicationIdSuffix = ".debug")
- SHA1: `BF:17:FA:D9:02:F9:4B:A3:A3:30:6D:C6:81:0A:72:4F:F9:7F:19:93`
- SHA256: `CE:09:37:37:1D:8E:9E:D4:EB:B7:C4:5D:AB:0D:FA:A4:A9:5C:C7:B5:CC:93:8F:D5:68:61:8A:F4:ED:EB:23:B9`

کلید به شکل متن base64 در `app/debug-keystore.b64` نگهداری می‌شود و Gradle هر بار آن را به `app/build/debug.keystore` تبدیل و برای امضای debug استفاده می‌کند. رمز آن `android` است (همان رمز استاندارد debug). این کلید محرمانه نیست و **هرگز برای release استفاده نشود**.

توجه: اگر قبلاً یک APK آزمایشی با کلید تصادفی نصب کرده‌ای، برای نصب نسخهٔ جدید باید ابتدا آن را حذف (Uninstall) کنید؛ امضای آن‌ها با هم فرق دارد.

## نسخهٔ release
هنوز کلید release ساخته نشده (نام بسته `ir.dorehami.app`). مجوز نشان برای آن بعداً باید با SHA1 کلید release گرفته شود. کلید release را هرگز در مخزن قرار ندهید.
