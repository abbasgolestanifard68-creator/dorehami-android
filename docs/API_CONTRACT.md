# قرارداد API (پیش‌نویس)

پایه: `{API_BASE}/v1`، بدنه JSON، هدر `Authorization: Bearer <accessToken>` برای مسیرهای نیازمند ورود.
خطا: `{ "code": "...", "message": "..." }` با کد وضعیت HTTP مناسب.

## موجود در کلاینت فعلی
| مسیر | بدنه | پاسخ |
|------|------|------|
| `POST /auth/login` | `phone` یا `username`، `password` | `{ user:{firstName,lastName}, tokens:{accessToken,refreshToken} }` |
| `POST /auth/register` | `firstName,lastName,username,phone,password,gender,birthDate,acceptTerms,instagramId?` | مثل ورود |

شمارهٔ موبایل اکنون به شکل `09123456789` ارسال می‌شود و تاریخ تولد `YYYY-MM-DD` میلادی (کاربر در اپ تاریخ شمسی را از تقویم انتخاب می‌کند و اپ آن را به میلادی تبدیل می‌کند). **بک‌اند باید تأیید کند.**

## پیشنهادی — نیازمند تأیید تیم بک‌اند
- `POST /auth/refresh` ← `{refreshToken}` ⇒ `{tokens}` (کلاینت همین را فرض کرده است)
- `POST /auth/logout` ← ابطال refreshToken در سرور
- `GET /conversations`، `GET /conversations/{id}/messages?before=`، `POST /conversations/{id}/messages`
- WebSocket `/v1/ws` برای پیام لحظه‌ای و وضعیت تحویل
- `POST /devices` برای ثبت توکن FCM

## شکل پاسخ‌های چت که کلاینت فرض کرده است (پیش‌نویس)
- `GET /conversations` ⇒ `{ "conversations": [ { "id", "title", "lastMessage", "lastMessageAt" (میلی‌ثانیه), "unread" } ] }`
- `GET /conversations/{id}/messages` ⇒ `{ "messages": [ { "id", "text", "mine" (بولی), "createdAt" (میلی‌ثانیه), "status" (sending|sent|delivered|read|failed) } ] }`
- `POST /conversations/{id}/messages` با `{ "text" }` ⇒ `{ "message": { ...همان شکل بالا } }`

کلاینت این‌ها را در `ApiChatRepository` پیاده کرده؛ با `Config.USE_FAKE_CHAT = false` فعال می‌شود.
