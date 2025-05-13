An Android application built with Kotlin, Java, and Jetpack Compose, designed to allow users to search for artists, explore their artworks, and manage personalized favorites — all powered by the Artsy API. This project demonstrates modern Android development practices, including declarative UI design, session persistence, and responsive state management.

Tech Stack :
Languages: Kotlin, Java
UI Framework: Jetpack Compose
Session Management: Persistent login using PersistentCookieJar and SharedPreferences
Image Loading: Coil – asynchronous image rendering with built-in caching
State Handling: Kotlin Coroutines
API Communication: Custom HTTP requests with asynchronous handling
UI Components: Material Design 3, LazyColumn, Snackbar, Dialogs, CardViews
Backend Integration: Artsy API + session-based authentication
Testing Platform: Pixel 8 Pro emulator, API level 34

The application offers a seamless and interactive user experience for discovering artists through the Artsy API. It features real-time artist search with dynamic updates, tabbed navigation for viewing detailed artist profiles, artworks, and similar artists, as well as user authentication for managing favorites. Users can register, log in, and persist their session across app restarts using cookie-based authentication. The app also includes responsive UI elements like progress indicators, snackbars, dialogs, and adaptive layouts—all built with Material Design 3 principles and Jetpack Compose.

The app leverages Kotlin, Java, and Jetpack Compose to implement a clean and modular architecture. Asynchronous operations are handled using Kotlin coroutines, enabling a smooth and non-blocking user experience. Instead of using Retrofit, custom HTTP handling was implemented to maintain full control over API interactions. Image loading is powered by Coil, ensuring efficient, responsive media rendering. Emphasis was placed on code reusability, UI composability, and proper lifecycle management, resulting in a robust, maintainable, and modern Android application.
