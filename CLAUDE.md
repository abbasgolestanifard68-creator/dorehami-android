# Dorehami — project guide for the agent team

This file is read first by every agent. The owner speaks Persian: **reply to the owner in Persian**; keep code, identifiers, commit messages and PR titles in English unless they are user-facing text.

## Product
Dorehami (دورهمی) is a Persian (RTL) social/chat mobile app. The owner has not finalized scope: plain messenger vs. map-based meetup app (nearby people/groups). Do not assume the larger scope; ask the owner when a decision depends on it.

## Stack and layout (this repo: `dorehami-android`)
- Android: Kotlin, Jetpack Compose, Material3. minSdk 26, targetSdk 35. Package `ir.dorehami.app` (debug build: `ir.dorehami.app.debug`).
- `app/src/main/kotlin/ir/dorehami/app/`: `ui/` screens, `chat/` models + `ChatRepository` (Fake and Api implementations), `data/` (`SessionStore`, `TokenCrypto` with Android Keystore), `net/Api.kt`, `util/` (`Validators`, `Jalali`), `Config.kt` (`API_BASE`, `USE_FAKE_CHAT`).
- API base: `https://api.dorehami405.ir/v1`. Draft contract: `docs/API_CONTRACT.md` (unconfirmed by the backend). The backend does not exist yet; chat runs on sample data.
- `docs/`: ROADMAP, TASKS, MAPS (Neshan map license notes), AGENT_TEAM.
- iOS: SwiftUI skeleton in `ios/` on branch `claude/ios-foundation` (XcodeGen, not compiled yet).
- Map plan: Neshan SDK. License is tied to package name + signing SHA1. Debug key is committed as `app/debug-keystore.b64` (debug only, SHA1 in `docs/MAPS.md`).

## Product rules already decided
- Birth date: user picks a Jalali date from a calendar; the app sends Gregorian `YYYY-MM-DD`.
- Passwords at sign-up: 8+ chars, lowercase, uppercase, digit, one symbol (ASCII only). Login has no rule so old accounts still work. The backend must enforce the same rule.
- Phone format sent to the server: `09123456789` (Persian/Arabic digits and +98 are normalized).
- Tokens only in Keystore-encrypted storage. Never log tokens.

## Working rules (all agents)
1. **Never commit to `main`.** Use a branch named `claude/<topic>` and open a pull request.
2. **No secrets in the repo:** no API keys, passwords, tokens, release keystores, `.env`, license files you were not given. The committed debug key is the only exception and is for debug builds only.
3. **Be honest about verification.** Say exactly one of: *compiled in CI*, *unit tests passed*, *tested on a device*, or *written but not built*. Never claim more. You usually cannot build or run the app yourself.
4. **Ask before** anything paid, irreversible, public, or that changes scope (domains, servers, ads, store publishing, making the repo public/private, data deletion).
5. **Privacy first:** never send exact coordinates of other users to a client; location sharing is opt-in; foreground location only; support an invisible mode and fast deletion.
6. **No dark patterns** (fake urgency, shaming, hidden opt-outs, manipulative notification spam).
7. Keep changes small and reviewable; update `docs/` when a decision or contract changes.
8. The owner works from a Windows 7 PC: he cannot run Claude Code locally, Xcode, or Android Studio. Prefer steps that work from a browser (GitHub web, Actions artifacts).
9. CI: `.github/workflows/android.yml` builds a debug APK on every push. Agents cannot edit workflow files unless the GitHub app has the Workflows permission.

## How the team works
The main Claude session is the **team lead**: it breaks the owner's request into tasks, delegates to the specialist subagents in `.claude/agents/`, collects results, and reports to the owner in Persian with: what was done, what blocks, next step. Subagents cannot delegate further; they return results to the lead. Reviews (QA, security) must come from a different agent than the author.
