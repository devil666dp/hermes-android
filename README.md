# Hermes Android (Material 3)

Native Android client for the **Hermes AI Agent**, featuring a modern **Material 3 (M3)** design system and full real-time streaming integration with remote Hermes deployments on **Zerops**.

Based on deep architectural research and feature extraction from the [Hermes Desktop](https://github.com/fathah/hermes-desktop) codebase.

---

## 🚀 Module 1 Implementation: Interactive Chat & Streaming Engine

This repository contains the complete implementation of **Module 1**:
1. **Zero-Latency WebSocket Streaming**: Bidirectional JSON-RPC 2.0 client communicating with `wss://hermes-3238-9119.prg1.zerops.app/api/ws`.
2. **Lossy Text Reconciliation (`LossyTextReconciler.kt`)**: Port of the desktop's algorithm (`isLossyChunkCopy`, `mergeStreamedWithFinal`, `tailHeadOverlap`, `commonSuffixLength`) supporting CJK dense scripts, multi-turn tool runs, and dropped chunk correction.
3. **Collapsible Thought / Reasoning Accordion**: Material 3 collapsible reasoning block displaying the model's live chain-of-thought.
4. **Tool Activity Accordion (`ToolActivityCard.kt`)**: Status badges for running, completed, and failed tool calls (e.g. bash, python, web search) with expandable output disclosures.
5. **Interactive Clarify Cards (`ClarifyCard.kt`)**: Displays choices as `SuggestionChip`s with text input fallback and skip ("Let Hermes decide") actions.
6. **Command Approval Cards (`ApprovalCard.kt`)**: Privilege escalation prompt with command preview, Allow Once, Always Allow (with confirmation sheet), and Deny actions.
7. **Side Questions (`/btw`, `/bg`)**: Concurrently dispatches background prompts without interrupting the active main turn.
8. **Slash Commands Pipeline**: Executes local UI actions, gateway commands, and model directives.
9. **Usage & Context Telemetry**: Context occupancy percentage gauge, token usage tracking, and estimated USD cost.

---

## 🌐 Remote Zerops Agent Configuration

The app is pre-configured to connect to your live Zerops cluster:
- **Dashboard & WebSocket API**: `https://hermes-3238-9119.prg1.zerops.app`
- **Direct Gateway Daemon (Port 8642)**: `https://hermes-3238-8642.prg1.zerops.app`
- **Default Credentials**: `sunil` / `rashmoni$034`

### Authentication & Handshake Flow:
```
1. POST /auth/password-login  --> Stores hermes_session_at / hermes_session_rt cookies
2. POST /api/auth/ws-ticket   --> Mints a 30-second single-use ticket
3. WSS  /api/ws?ticket=<T>    --> Establishes persistent real-time streaming channel
```

---

## 🛠️ GitHub Actions Workflow (Build Debug APK)

A manual workflow is configured at `.github/workflows/build-debug-apk.yml`.

### How to trigger manual build:
1. Navigate to the **Actions** tab on GitHub.
2. Select **Build Debug APK** from the left workflow sidebar.
3. Click **Run workflow** -> Select branch `main` -> Click **Run workflow**.
4. Once completed, download `hermes-android-debug-apk` from the **Artifacts** section!

---

## 💻 Local Build Instructions

### Prerequisites
- JDK 17+
- Android SDK 35 (Platform 35, Build Tools 35.0.0)

### Commands
```bash
# Clone the repository
git clone <your-repo-url>
cd hermes-android

# Make gradlew executable
chmod +x ./gradlew

# Build the debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏗️ Architecture Stack

- **UI**: 100% Jetpack Compose with Material 3 (`androidx.compose.material3:1.3.1`)
- **Architecture**: MVI / MVVM with unidirectional data flow (`StateFlow`, Kotlin Coroutines)
- **Networking**: OkHttp 4.12+ (CookieJar persistence, WebSocketListener)
- **Serialization**: Kotlinx Serialization JSON (`kotlinx.serialization`)
- **Security**: EncryptedSharedPreferences with AES-256 GCM master key
