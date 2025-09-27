# Bildirimle Öğren / Learn with Notifications

> ⚠️ This repository has two branches for UI:
> - **main**: XML layouts.
> - **compose**: Jetpack Compose layouts.

🇹🇷 Türkçe: Kullanıcılara bildirimlerle kelime öğrenmeyi sağlayan Android uygulaması. 

- **main branch**: XML tabanlı klasik Android UI kullanıyor.
- **compose branch**: UI tamamen **Jetpack Compose** ile yeniden yazılmıştır.
- 
  🇬🇧 English: Android app to learn languages with scheduled notifications.  

- **main branch**: Uses traditional XML-based Android UI.
- **compose branch**: UI is completely rewritten using **Jetpack Compose**.

---

## 📱 Screenshots

<p align="center">
  <img src="screenshots/screen1.jpeg" width="200" />
  <img src="screenshots/screen2.jpeg" width="200" />
  <img src="screenshots/screen3.jpeg" width="200" />
  <img src="screenshots/screen4.jpeg" width="200" />
</p>


---

## 🚀 Features

- Learn vocabulary and sentences with scheduled notifications.
- Customize notification frequency.
- Multiple language sets supported.
- Clean Material 3 design.
  - **main branch**: XML layouts.
  - **compose branch**: Jetpack Compose.
- Uses **Flow** for reactive data handling.


---

## 🛠️ Technologies & Libraries

- **Kotlin** – Main programming language.
- **Android SDK** – Target SDK 36.
- **Jetpack Components**:
  - **Room** (Local Database)
  - **WorkManager** (Notifications)
  - **ViewModel + Flow**
  - **Navigation Component**
- **DataStore Preferences** – Local key-value storage.
- **Dagger Hilt** – Dependency Injection.
- **Kotlinx Serialization** – JSON parsing.
- **Material 3** – Modern UI components.
- **Jetpack Compose** – Used in compose branch for modern declarative UI.
- **Compose Material3** – Material 3 components in Compose (compose branch).
- **OpenCSV** – CSV parsing.
- **KTX Extensions** – `activity-ktx`, `fragment-ktx`, `lifecycle-runtime-ktx`.
- **ConstraintLayout** – Flexible layouts.


---

## 📄 Usage

1. Prepare a vocabulary list in **Google Sheets** (either your own or someone else’s).  
2. In Google Sheets, select **File → Share → Publish to web** and copy the generated link.  
3. Open the app and paste the link inside the **Add Set** section.  
4. The app will download the dataset to your phone.  
5. Enable notifications for that set.  
6. Learn new words through scheduled notifications.


---

## 📂 File Structure

- `data/` – Room entities, DAO, Repository.
- `ui/` – Activities and Fragments.
- `notification/` – WorkManager worker for notifications.
- `di/` – Hilt modules.
- `screenshots/` – App screenshots used in README.

---

## 📝 License

MIT License © 2025 Levent YADIRGA





