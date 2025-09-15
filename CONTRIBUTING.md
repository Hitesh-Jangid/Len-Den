# Contributing to Len-Den

## ⚠️ **ULTRA-RESTRICTIVE LICENSE NOTICE**

This project is licensed under an **ULTRA-RESTRICTIVE PROPRIETARY LICENSE**. Before even considering contributing, please understand:

### � **SEVERE RESTRICTIONS**
- This codebase has the **MOST RESTRICTIVE** license possible
- **NO** copying, downloading, or educational use permitted
- **NO** fair use or educational exceptions apply
- **VIEWING ONLY** through GitHub web interface
- **ANY** unauthorized use results in immediate legal action

### 🔒 **Contribution Terms**
- Contributing requires **explicit written agreement**
- All contributions become ultra-restrictively licensed
- Contributors must sign comprehensive legal agreements
- The project owner retains **absolute control** over all code

### 📧 **MANDATORY LEGAL AGREEMENT**
Before making ANY contributions, you MUST contact:
- **Email**: hiteshjangid.dev@gmail.com
- **Subject**: "Len-Den Ultra-Restrictive Contributor Agreement"
- **Requirement**: Legal documentation must be completed

### ⚖️ **LEGAL WARNING**
Attempting to contribute without proper agreements may result in legal consequences.

First off, thank you for considering contributing to Len-Den! It's people like you that make Len-Den such a great tool for financial management.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

## 📜 Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and inclusive environment. By participating, you are expected to uphold these standards:

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have:

- **Android Studio** 2023.1.1 (Hedgehog) or later
- **JDK 17** or higher
- **Git** for version control
- Basic knowledge of Android development and Java

### Development Environment Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/Hitesh-Jangid/Len-Den.git
   cd Len-Den
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build the Project**
   ```bash
   ./gradlew clean build
   ```

4. **Run Tests**
   ```bash
   ./gradlew test
   ```

## 🤝 How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Screenshots** if applicable
- **Device information** (Android version, device model)
- **App version** you're using

Use this template:
```markdown
**Bug Description:**
A clear description of what the bug is.

**To Reproduce:**
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected Behavior:**
What you expected to happen.

**Screenshots:**
If applicable, add screenshots.

**Device Info:**
- Device: [e.g. Samsung Galaxy S21]
- OS: [e.g. Android 12]
- App Version: [e.g. 1.0]
```

### 💡 Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear title** that describes the enhancement
- **Provide detailed description** of the proposed functionality
- **Explain why this enhancement would be useful**
- **Consider alternatives** you've considered

### 🔧 Contributing Code

We welcome code contributions! Here are areas where you can help:

#### 🌟 Priority Areas
- **UI/UX Improvements**: Better visual design, animations, accessibility
- **Performance Optimization**: Database queries, memory usage, app responsiveness
- **Security Enhancements**: Additional validation, encryption, secure storage
- **Feature Development**: New financial tracking features, reporting capabilities
- **Testing**: Unit tests, integration tests, UI tests
- **Documentation**: Code comments, user guides, technical documentation

#### 🛠️ Technical Improvements
- **Database Optimization**: Query performance, schema improvements
- **Architecture**: Clean architecture patterns, dependency injection
- **Error Handling**: Better error messages, recovery mechanisms
- **Internationalization**: Multi-language support
- **Accessibility**: Screen reader support, keyboard navigation

## 💻 Development Setup

### Project Structure
```
app/src/main/java/com/hiteshjangid/lenden/
├── MainActivity.java              # Main dashboard
├── ExpenseActivity.java           # Expense management
├── IncomeActivity.java            # Income tracking
├── BudgetActivity.java            # Budget planning
├── ReportsActivity.java           # Expense reports
├── ReportsIncomeActivity.java     # Income reports
├── SummaryActivity.java           # Financial summary
├── AppSettingsActivity.java       # Settings
├── ResetActivity.java             # Data management
├── FullscreenActivity.java        # Splash screen
└── util/
    └── AppSettingsManager.java    # Core utilities
```

### Database Schema
- **expenses**: Transaction records with categories
- **incomes**: Income records with types
- **category**: Expense category definitions
- **type**: Income type definitions
- **BudgetInfo**: Budget and summary information

## 📏 Coding Standards

### Java Style Guidelines

1. **Naming Conventions**
   ```java
   // Classes: PascalCase
   public class ExpenseActivity extends AppCompatActivity

   // Methods: camelCase
   private void updateExpenseHeader()

   // Variables: camelCase
   private SQLiteDatabase sqLiteDatabase

   // Constants: UPPER_SNAKE_CASE
   public static final int MAX_AMOUNT_LENGTH = 20
   ```

2. **Code Organization**
   - Group related methods together
   - Order methods logically (lifecycle → business logic → helpers)
   - Use clear, descriptive method names
   - Keep methods focused and single-purpose

3. **Error Handling**
   ```java
   try {
       // Database operations
   } catch (SQLException e) {
       AppSettingsManager.showToast(this, "Error message: " + e.getMessage());
   } finally {
       AppSettingsManager.closeCursor(cursor);
   }
   ```

4. **Database Best Practices**
   ```java
   // Always use parameterized queries
   String query = "SELECT * FROM expenses WHERE month = ? AND year = ?";
   Cursor cursor = database.rawQuery(query, new String[]{month, year});

   // Always close resources
   try {
       // Use cursor
   } finally {
       AppSettingsManager.closeCursor(cursor);
   }
   ```

### XML Guidelines

1. **Layout Files**
   - Use descriptive IDs: `@+id/expense_amount_input`
   - Follow Material Design guidelines
   - Ensure accessibility attributes
   - Use appropriate ViewGroups for performance

2. **Resource Naming**
   ```xml
   <!-- Strings -->
   <string name="expense_add_button_text">Add Expense</string>
   
   <!-- Colors -->
   <color name="primary_blue">#2196F3</color>
   
   <!-- Dimensions -->
   <dimen name="card_margin">16dp</dimen>
   ```

## 🧪 Testing Guidelines

### Unit Testing

1. **Test Critical Business Logic**
   ```java
   @Test
   public void testBudgetCalculation() {
       // Test the core budget calculation logic
       int budget = 10000;
       int income = 15000;
       int expense = 8000;
       int expected = budget + income - expense; // 17000
       
       assertEquals(expected, calculateCurrentBudget(budget, income, expense));
   }
   ```

2. **Test Edge Cases**
   ```java
   @Test
   public void testDateValidation() {
       // Test leap year dates
       assertTrue(AppSettingsManager.isValidDateFormat("29-02-2024"));
       assertFalse(AppSettingsManager.isValidDateFormat("29-02-2023"));
       
       // Test invalid dates
       assertFalse(AppSettingsManager.isValidDateFormat("31-04-2024"));
   }
   ```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests="DateValidationTest"

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

## 📝 Pull Request Process

### Before Submitting

1. **Update your fork**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow coding standards
   - Add/update tests as needed
   - Update documentation if necessary

4. **Test thoroughly**
   ```bash
   ./gradlew clean build test
   ```

### Submission

1. **Commit with clear messages**
   ```bash
   git commit -m "Add expense category validation

   - Implement category name length validation
   - Add unit tests for edge cases
   - Update error messages for better UX"
   ```

2. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create Pull Request**
   - Use descriptive title
   - Reference related issues
   - Provide clear description of changes
   - Include testing information

### PR Template

```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] New tests added (if applicable)

## Screenshots
Include screenshots for UI changes.

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
```

## 🚨 Issue Guidelines

### Issue Labels

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Improvements or additions to docs
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `priority-high` - High priority issue
- `priority-low` - Low priority issue

### Issue Templates

Use appropriate templates when creating issues:
- **Bug Report**: For reporting bugs
- **Feature Request**: For suggesting new features
- **Documentation**: For documentation improvements

## 🎯 Development Roadmap

### Short Term Goals
- [ ] Enhanced UI animations
- [ ] Improved error handling
- [ ] Additional unit tests
- [ ] Performance optimizations

### Long Term Goals
- [ ] Cloud backup/sync
- [ ] Multi-currency support
- [ ] Advanced reporting
- [ ] Machine learning insights

## 🤔 Questions?

If you have questions about contributing or licensing:

1. **Check existing issues** for similar questions
2. **Search documentation** for answers
3. **Contact the maintainer** for licensing questions:
   - Email: hiteshjangid.dev@gmail.com
   - Subject: "Len-Den Licensing Question"
4. **Create new issue** with `question` label for technical questions

## 🔒 **Intellectual Property**

### **Important Notes:**
- This is a **proprietary project** with controlled licensing
- Contributions become part of the protected codebase
- Commercial use requires explicit permission
- Educational use is encouraged with proper attribution

### **For Commercial Use:**
Contact Hitesh Jangid at hiteshjangid.dev@gmail.com for ultra-restrictive licensing arrangements.

### **REMEMBER: VIEWING ONLY**
This repository permits **ONLY** passive viewing through GitHub. Any other use is strictly prohibited and monitored.

## 🙏 Recognition

Contributors will be:
- Listed in the README.md
- Mentioned in release notes
- Given appropriate GitHub repository permissions (for regular contributors)

Thank you for contributing to Len-Den! 🎉
