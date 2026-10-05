# Universal AI Autofill Assistant (Java & Maven Edition)

An offline-first, intelligent Android autofill engine completely ported to **pure Java (Java 17)** with **Apache Maven** build management.

---

## 🏗️ Architecture & Modules

```
android-java/
├── pom.xml                                   # Maven POM file (Java 17, Room, Gson, JUnit 5)
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml               # Service & permission declarations
│   │   ├── res/xml/
│   │   │   └── accessibility_service_config.xml # Accessibility service flags
│   │   └── java/com/example/smartautofiller/
│   │       ├── model/                        # Core Data Models
│   │       │   ├── SectionField.java         # Key-value field POJO
│   │       │   ├── ProfileSection.java       # Structured section (Marksheet, ID, etc.)
│   │       │   └── UserProfile.java          # Room Entity with custom fields & sections
│   │       ├── database/                     # Persistence & JSON Serialization
│   │       │   ├── AppDatabase.java          # Room Database (v5) with ExecutorService
│   │       │   ├── UserProfileDao.java       # DAO with LiveData & synchronous queries
│   │       │   ├── Converters.java           # Room Gson TypeConverters
│   │       │   └── ProfileJsonSerializer.java# Export/Import profiles as JSON
│   │       ├── matcher/                      # Heuristic Label Matching Engine
│   │       │   ├── FieldMatcher.java         # Multi-tiered heuristic & fuzzy matcher
│   │       │   ├── LevenshteinDistance.java  # String distance metric calculation
│   │       │   ├── ShortcutExpander.java     # Inline text shortcut expander (name-, mob-)
│   │       │   └── TranslationDictionary.java# Instant offline regional language dictionary
│   │       ├── security/                     # Security & Sandboxing
│   │       │   └── PinManager.java           # EncryptedSharedPreferences (AES256-GCM) + Lockout
│   │       ├── ocr/                          # Document & Marksheet Parsing
│   │       │   ├── ScannedData.java          # OCR parsed output model
│   │       │   └── DocumentParser.java       # Regex & heuristic extractors (PAN, Aadhaar, etc.)
│   │       └── service/                      # Background & System Services
│   │           ├── SmartAccessibilityService.java # Core window traversal & text injection
│   │           ├── AiFillTileService.java    # Quick Settings toggle tile
│   │           ├── QuickCopyService.java     # Foreground notification quick-copy buttons
│   │           └── CopyReceiver.java         # 30-second clipboard auto-clear
│   └── test/java/com/example/smartautofiller/
│       ├── matcher/
│       │   ├── LevenshteinDistanceTest.java  # String similarity unit tests
│       │   ├── FieldMatcherTest.java         # Label matching & translation unit tests
│       │   └── ShortcutExpanderTest.java     # Text expansion unit tests
│       ├── database/
│       │   └── ProfileJsonSerializerTest.java# JSON export/import unit tests
│       └── ocr/
│           └── DocumentParserTest.java       # PAN, Aadhaar & Marksheet parsing tests
```

---

## ⚡ Key Improvements in the Java Version

1. **Deterministic Concurrency:** Replaced coroutine suspension state-machines with clean `ExecutorService` thread pools and `Handler(Looper.getMainLooper())`.
2. **Instant In-Memory Translation:** Built-in `TranslationDictionary` provides sub-millisecond translations for Hindi, Tamil, Telugu, Marathi, Bengali, and Gujarati form labels without requiring heavy offline ML model downloads.
3. **Robust Fuzzy Matching:** Integrated `LevenshteinDistance` calculation with 80% threshold to handle form variations like `student_name`, `candidate name`, and typos.
4. **Zero-Latency Clipboard Security:** `CopyReceiver` automatically purges sensitive data from the Android clipboard 30 seconds after copy actions.
5. **16KB Page-Size Safe:** Pure Room SQLite persistence avoids native C++ crashes on Android 15 and 16.

---

## 🧪 Running Unit Tests via Maven

To build the project and execute all unit tests using Maven:

```bash
cd android-java
mvn clean test
```

To package the JAR library:
```bash
mvn clean package
```
