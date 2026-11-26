# قرارداد API (پیش‌نویس)

پایه: `{API_BASE}/v1`، بدنه JSON، هدر `Authorization: Bearer <accessToken>` برای مسیرهای نیازمند ورود.
خطا: `{ "code": "...", "message": "..." }` با کد وضعیت HTTP مناسب.

## موجود در کلاینت فعلی
| مسیر | بدنه | پاسخ |
|------|------|------|
| `POST /auth/login` | `phone` یا `username`، `password` | `{ user:{firstName,lastName}, tokens:{accessToken,refreshToken} }` |
| `POST /auth/register` | `firstName,lastName,username,phone,password,gender,birthDate,acceptTerms,instagramId?` | مثل ورود |

شمارهٔ موبایل اکنون به شکل `09123456789` ارسال می‌شود و تاریخ تولد `YYYY-MM-DD` میلادی. **بک‌اند باید تأیید کند.**

## پیشنهادی — نیازمند تأیید تیم بک‌اند
- `POST /auth/refresh` ← `{refreshToken}` ⇒ `{tokens}` (کلاینت همین را فرض کرده است)
- `POST /auth/logout` ← ابطال refreshToken در سرور
- `GET /conversations`، `GET /conversations/{id}/messages?before=`، `POST /conversations/{id}/messages`
- WebSocket `/v1/ws` برای پیام لحظه‌ای و وضعیت تحویل
- `POST /devices` برای ثبت توکن FCM
