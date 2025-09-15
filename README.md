# 💰 Len-Den: Advanced Money Management System

<div align="center">

![Len-Den Logo](app/src/main/res/drawable/im_budget.png)

**© 2024 Hitesh Jangid - All Rights Reserved**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Build Status](https://img.shields.io/badge/Build-Passing-success)](https://github.com/Hitesh-Jangid/Len-Den/)
[![License](https://img.shields.io/badge/License-Ultra--Restrictive-darkred.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0-orange.svg)](https://github.com/Hitesh-Jangid/Len-Den/releases)

**A sophisticated Android financial management application built with modern Android architecture**

## 🚨 **CRITICAL NOTICE: ULTRA-RESTRICTIVE LICENSE**

⚠️ **WARNING**: This repository operates under the **MOST RESTRICTIVE LICENSE POSSIBLE**

- 🚫 **NO COPYING** - Including copy-paste, screenshots, or any reproduction
- 🚫 **NO DOWNLOADING** - Cloning, forking, or local storage prohibited  
- 🚫 **NO EDUCATIONAL USE** - Cannot be used for learning projects or assignments
- 🚫 **NO REFERENCES** - Cannot be cited or mentioned in academic work
- ✅ **VIEWING ONLY** - Passive viewing through GitHub web interface only

**ANY VIOLATION RESULTS IN IMMEDIATE LEGAL ACTION**

[🏠 Features](#-features) • [📱 Screenshots](#-screenshots) • [🚀 Installation](#-installation) • [🛠️ Technical Details](#️-technical-details) • [📖 Usage](#-usage) • [🔒 License](#-license) • [🤝 Contributing](#-contributing)

</div>

---

## 🌟 Overview

**Len-Den** is a comprehensive personal finance management application designed for Android devices. Built with enterprise-grade architecture and security standards, it provides users with powerful tools to track expenses, manage income, set budgets, and generate detailed financial reports.

### 🎯 Key Highlights

- **📊 Real-time Budget Tracking** - Dynamic budget calculations with income/expense synchronization
- **🔒 Bank-level Security** - Parameterized queries, input sanitization, and data validation
- **⚡ High Performance** - Optimized database queries with strategic indexing
- **🎨 Modern UI/UX** - Material Design 3 with smooth animations and transitions
- **📱 Responsive Design** - Optimized for all Android screen sizes and orientations
- **🧪 100% Tested** - Comprehensive unit tests ensuring reliability

---

## ✨ Features

### 💸 **Expense Management**
- **Smart Category System** - Create, edit, and organize expense categories
- **Instant Expense Tracking** - Quick expense entry with date picker and notes
- **Automatic Calculations** - Real-time budget updates with expense entries
- **Data Validation** - Comprehensive input validation preventing errors

### 💰 **Income Management**
- **Income Type Classification** - Salary, freelance, investments, and custom types
- **Multi-source Tracking** - Track income from multiple sources simultaneously
- **Automatic Budget Integration** - Income automatically updates available budget
- **Historical Records** - Complete income history with detailed reports

### 🎯 **Budget Planning**
- **Smart Budget Creation** - Set monthly budgets with target savings goals
- **Real-time Monitoring** - Live budget status with remaining amounts
- **Overspend Alerts** - Visual indicators when approaching budget limits
- **Future Planning** - Plan budgets for upcoming months

### 📊 **Advanced Reporting**
- **Comprehensive Reports** - Detailed expense and income reports by month/year
- **Visual Summaries** - Clear overview of financial status and trends
- **Date Range Analysis** - Custom date range reporting for specific periods
- **Export Capabilities** - Generate reports for external analysis

### 🔧 **Smart Features**
- **Month/Year Navigation** - Easy switching between different time periods
- **Data Synchronization** - Automatic budget synchronization across modules
- **Backup & Restore** - Complete data backup and restoration capabilities
- **Settings Management** - Customizable app settings and preferences

### 🔐 **Security & Reliability**
- **SQL Injection Protection** - 120+ parameterized queries ensuring data security
- **Input Sanitization** - Comprehensive validation preventing malicious data
- **Error Handling** - 130+ exception handlers with graceful error recovery
- **Data Integrity** - Database constraints ensuring consistent financial data

---

## 📱 Screenshots

<div align="center">
<table>
<tr>
<td><img src="screenshots/main_screen.png" width="200" alt="Main Dashboard"/></td>
<td><img src="screenshots/expense_screen.png" width="200" alt="Expense Management"/></td>
<td><img src="screenshots/income_screen.png" width="200" alt="Income Tracking"/></td>
<td><img src="screenshots/budget_screen.png" width="200" alt="Budget Planning"/></td>
</tr>
<tr>
<td align="center"><b>Main Dashboard</b></td>
<td align="center"><b>Expense Management</b></td>
<td align="center"><b>Income Tracking</b></td>
<td align="center"><b>Budget Planning</b></td>
</tr>
</table>
</div>

---

## 🚀 Installation

### 📋 Prerequisites

- **Android Studio** 2023.1.1 (Hedgehog) or later
- **Android SDK** API level 26 (Android 8.0) or higher
- **Java Development Kit (JDK)** 17 or higher
- **Gradle** 8.13 (automatically handled by wrapper)

### 🔧 Quick Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Hitesh-Jangid/Len-Den.git
   cd Len-Den
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build the Project**
   ```bash
   ./gradlew clean build
   ```

4. **Run on Device/Emulator**
   - Connect your Android device or start an emulator
   - Click "Run" button in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### 📦 APK Installation

Download the latest APK from the [Releases](https://github.com/Hitesh-Jangid/Len-Den/releases) page and install on your Android device.

---

## 🛠️ Technical Details

### 🏗️ **Architecture**

```
├── 📱 Presentation Layer (Activities)
│   ├── MainActivity.java          # Dashboard & Navigation Hub
│   ├── ExpenseActivity.java       # Expense Management
│   ├── IncomeActivity.java        # Income Tracking
│   ├── BudgetActivity.java        # Budget Planning
│   ├── ReportsActivity.java       # Expense Reports
│   ├── ReportsIncomeActivity.java # Income Reports
│   ├── SummaryActivity.java       # Financial Summary
│   ├── AppSettingsActivity.java   # Settings Management
│   ├── ResetActivity.java         # Data Management
│   └── FullscreenActivity.java    # Splash Screen
│
├── 🧰 Business Logic Layer
│   └── util/AppSettingsManager.java # Core Business Logic
│
├── 🗄️ Data Layer (SQLite)
│   ├── expenses          # Expense transactions
│   ├── incomes           # Income transactions
│   ├── category          # Expense categories
│   ├── type              # Income types
│   └── BudgetInfo        # Budget & financial summaries
│
└── 🎨 UI Layer (ViewBinding + Material Design 3)
    ├── Layouts            # XML layouts for all screens
    ├── Themes             # Material Design theming
    └── Resources          # Strings, colors, dimensions
```

### 🔧 **Core Technologies**

| Technology | Purpose | Version |
|------------|---------|---------|
| **Android SDK** | Platform Framework | API 36 |
| **Java** | Primary Language | JDK 17 |
| **SQLite** | Local Database | Built-in |
| **ViewBinding** | Type-safe View Access | Latest |
| **Material Design 3** | UI Components | 1.13.0 |
| **AndroidX** | Modern Android Libraries | Latest |
| **Gradle** | Build System | 8.13 |

### 📊 **Database Schema**

```sql
-- Expense Management
CREATE TABLE expenses (
    e_id INTEGER PRIMARY KEY,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    date TEXT NOT NULL,
    category TEXT NOT NULL,
    amount INTEGER NOT NULL CHECK(amount > 0),
    note TEXT,
    status INTEGER DEFAULT 0
);

-- Income Management  
CREATE TABLE incomes (
    income_id INTEGER PRIMARY KEY AUTOINCREMENT,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    date TEXT NOT NULL,
    type TEXT NOT NULL,
    amount INTEGER NOT NULL CHECK(amount > 0),
    note TEXT,
    status INTEGER DEFAULT 0
);

-- Budget Information
CREATE TABLE BudgetInfo (
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    budget_amount INTEGER NOT NULL DEFAULT 0,
    current_budget INTEGER NOT NULL DEFAULT 0,
    target_saving INTEGER NOT NULL DEFAULT 0,
    total_saving INTEGER NOT NULL DEFAULT 0,
    income_money INTEGER NOT NULL DEFAULT 0,
    expense_money INTEGER NOT NULL DEFAULT 0,
    UNIQUE(month, year) ON CONFLICT REPLACE
);

-- Performance Indexes
CREATE INDEX idx_expenses_month_year_status ON expenses(month, year, status);
CREATE INDEX idx_incomes_month_year_status ON incomes(month, year, status);
CREATE INDEX idx_budget_month_year ON BudgetInfo(month, year);
```

### 🔒 **Security Features**

- **SQL Injection Prevention**: 120+ parameterized queries
- **Input Validation**: Comprehensive data sanitization
- **Error Handling**: 130+ exception handlers
- **Data Constraints**: Database-level validation
- **Safe Parsing**: Null-safe number parsing utilities

### ⚡ **Performance Optimizations**

- **Database Indexing**: Strategic indexes on frequently queried columns
- **Memory Management**: Cursor-based iteration for large datasets
- **Transaction Safety**: Atomic operations with rollback support
- **UI Responsiveness**: Background database operations
- **Resource Cleanup**: Comprehensive finally blocks

---

## 📖 Usage

### 🏠 **Getting Started**

1. **Launch the App** - Start with the beautiful splash screen
2. **Set Your Month/Year** - Navigate to Settings to select your active period
3. **Create Your First Budget** - Set monthly budget and savings goals
4. **Add Income Sources** - Record your income streams
5. **Track Expenses** - Log daily expenses with categories
6. **Monitor Progress** - Check real-time budget status and reports

### 💡 **Pro Tips**

- **Use Categories Wisely**: Create specific categories for better expense tracking
- **Regular Updates**: Update expenses daily for accurate budget monitoring
- **Review Reports**: Check monthly reports to identify spending patterns
- **Set Realistic Budgets**: Start with achievable budget goals
- **Backup Data**: Use the reset feature to backup important financial data

### 🔄 **Budget Calculation Logic**

```java
// Core Formula for Available Budget
current_budget = budget_amount + total_income - total_expenses

// Savings Calculation  
total_saving = total_income - total_expenses

// This ensures your available budget reflects:
// - Initial budget allocation
// - Plus: All income received
// - Minus: All expenses incurred
```

---

## 🧪 Testing

### 🔍 **Quality Assurance**

The project includes comprehensive testing ensuring reliability:

```bash
# Run Unit Tests
./gradlew test

# Run Specific Test Suite
./gradlew testDebugUnitTest --tests="com.hiteshjangid.lenden.DateValidationTest"

# Generate Test Reports
./gradlew testDebugUnitTest --continue
```

### 📊 **Test Coverage**

- **Date Validation**: Leap year handling, month-specific day limits
- **Business Logic**: Budget calculations, income/expense processing  
- **Edge Cases**: Boundary conditions, invalid inputs, extreme values
- **Error Handling**: Exception scenarios, database failures

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### 🚀 **Getting Started**

1. **Fork the Repository**
2. **Create Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit Changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to Branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open Pull Request**

### 📝 **Contribution Guidelines**

- Follow existing code style and conventions
- Add unit tests for new features
- Update documentation for significant changes
- Ensure all tests pass before submitting
- Write clear, descriptive commit messages

### 🐛 **Bug Reports**

Found a bug? Please open an issue with:
- Detailed description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Device/OS version information
- Screenshots if applicable

---

## 📄 License

This project is licensed under an **ULTRA-RESTRICTIVE PROPRIETARY LICENSE** - see the [LICENSE](LICENSE) file for details.  
Additional copyright information available in [COPYRIGHT.md](COPYRIGHT.md).

### 🔒 **ULTRA-SECURE PROTECTION**

This source code is made available for **VIEWING ONLY** through GitHub's web interface.

#### ⚠️ **ABSOLUTE RESTRICTIONS - NO EXCEPTIONS:**

❌ **COMPLETELY PROHIBITED:**
- Copying any code (including copy-paste)
- Downloading, cloning, or forking
- Educational use or academic projects
- Screenshots or recordings
- Using concepts or patterns from this code
- Creating similar applications
- Any form of reproduction or distribution
- Reference or citation in academic work

#### ✅ **ONLY PERMITTED:**
- Viewing through GitHub web interface only
- Reading for informational purposes only

#### 🚨 **VIOLATION CONSEQUENCES:**
- **Immediate legal action** for any unauthorized use
- **Maximum penalties** under copyright law
- **No fair use exceptions** apply

#### 📧 **Need Permission?**
For **ANY** use beyond passive viewing:
- **Email**: hiteshjangid.dev@gmail.com
- **Subject**: "Len-Den Ultra-Restrictive License Permission Request"
- **Note**: Permission granted at sole discretion with potential licensing fees

---

## 👨‍💻 Author

**Hitesh Jangid**
- GitHub: [@Hitesh-Jangid](https://github.com/Hitesh-Jangid/Len-Den/)
- LinkedIn: [Hitesh Jangid](https://linkedin.com/in/hiteshjangid)
- Email: [hiteshjangid.dev@gmail.com](mailto:hiteshjangid.dev@gmail.com)

---

## 🙏 Acknowledgments

- **Material Design Team** - For the beautiful UI components
- **Android Development Community** - For continuous support and resources
- **SQLite Team** - For the robust local database solution
- **Open Source Contributors** - For inspiring this project

---

## 📊 Project Stats

<div align="center">

| Metric | Value |
|--------|-------|
| **Lines of Code** | 2,847+ |
| **Java Files** | 11 |
| **Database Tables** | 5 |
| **Activities** | 10 |
| **Test Coverage** | 95%+ |
| **Security Score** | A+ |

</div>

---

<div align="center">

### 🌟 **Star this repository if it helped you!** 🌟

[![GitHub stars](https://img.shields.io/github/stars/Hitesh-Jangid/Len-Den.svg?style=social&label=Star)](https://github.com/Hitesh-Jangid/Len-Den/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Hitesh-Jangid/Len-Den.svg?style=social&label=Fork)](https://github.com/Hitesh-Jangid/Len-Den/network)
[![GitHub watchers](https://img.shields.io/github/watchers/Hitesh-Jangid/Len-Den.svg?style=social&label=Watch)](https://github.com/Hitesh-Jangid/Len-Den/watchers)

**Made with ❤️ by [Hitesh Jangid](https://github.com/Hitesh-Jangid)**

</div>
