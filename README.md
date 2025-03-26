---

# BudgetManager

*A simple yet powerful Android application to manage your personal finances.*

## Overview

BudgetManager is an Android application designed to help users track their income, expenses, and overall financial health. With a user-friendly interface, it provides features like transaction logging, monthly reports, budget warnings, and profile management. Built with modern Android development practices, it leverages SQLite for local data storage and Material Design for an intuitive UI.

---

## Features

- **User Authentication**: Secure login and registration with password hashing (SHA-256).
- **Transaction Management**: Add, edit, and delete transactions with support for one-time and recurring entries.
- **Dashboard**: View current balance and recent transactions at a glance.
- **Monthly Reports**: Generate detailed reports of income and expenses by category for any selected month.
- **Recent Transactions**: Filter and view transaction history by name, category, and date range.
- **Notifications**: Opt-in for monthly report summaries and budget warnings.
- **Data Export**: Export transaction data to CSV files for external analysis.
- **Profile Customization**: Manage notification preferences and sign out securely.


## Installation

### Prerequisites
- Android Studio (version 4.0 or higher)
- Android SDK (API level 21 or higher)
- Java 8 or higher
- An Android device or emulator running Android 5.0 (Lollipop) or later

### Steps
1. **Clone the Repository**  
   ```bash
   git clone https://github.com/AAAAA11331/BudgetManager.git
   cd BudgetManager
   ```

2. **Open in Android Studio**  
   - Launch Android Studio.
   - Select "Open an existing project" and choose the cloned `BudgetManager` directory.

3. **Sync Project**  
   - Click "Sync Project with Gradle Files" to download dependencies.

4. **Build and Run**  
   - Connect an Android device or start an emulator.
   - Click "Run" in Android Studio to build and install the app.

5. **Permissions**  
   - Grant storage permissions when prompted to enable CSV export functionality.

---

## Usage

1. **Register an Account**  
   - Open the app and navigate to the "Register" screen.
   - Enter a username and password to create an account.

2. **Log In**  
   - Use your credentials to log in. Check "Remember Me" to save your username for future logins.

3. **Add Transactions**  
   - From the Dashboard, tap the "+" button.
   - Fill in details (type, category, amount, date, etc.) and submit.

4. **View Reports**  
   - Go to the "Reports" tab, select a month, and review your financial summary.

5. **Manage Profile**  
   - Visit the "Profile" tab to toggle notifications or sign out.

6. **Export Data**  
   - In the "Reports" tab, click "Export Report" to save transactions as a CSV file.

---

## Dependencies

- **AndroidX**: Core libraries for modern Android development.
- **Material Components**: UI components for a consistent Material Design experience.
- **SQLite**: Local database for storing transactions and user data.

Add these to your `app/build.gradle` if not already present:
```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
}
```

---

## Project Structure

```
BudgetManager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/budgetmanager/
│   │   │   │   ├── BaseActivity.java
│   │   │   │   ├── AddTransactionActivity.java
│   │   │   │   ├── DatabaseHelper.java
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── MainDashboardActivity.java
│   │   │   │   ├── MonthlyReportActivity.java
│   │   │   │   ├── RecentTransactionsActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── UserProfileActivity.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionAdapter.java
│   │   │   │   ├── NotificationHelper.java
│   │   │   │   ├── NotificationTypes.java
│   │   │   │   └── SecurityUtils.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── drawable/
│   └── build.gradle
├── README.md
└── gradle/
```

---

## Technical Stack

- **Language**: Java
- **Database**: SQLite
- **UI Framework**: Material Design with AndroidX
- **Security**: SHA-256 password hashing
- **Storage**: Local file system for CSV exports

---
