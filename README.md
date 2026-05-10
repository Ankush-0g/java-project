# Restaurant Management System (RMS)

A comprehensive Java-based desktop application for managing restaurant operations including staff management, menu management, order processing, and financial reporting.

## Features

- **User Authentication**: Support for different user types (Anonymous, Employee, Manager)
- **Order Management**: Create, modify, and track orders with real-time status updates
- **Menu Management**: Manage menu items with pricing and availability
- **Staff Management**: Employee and manager profiles with role-based access
- **Financial Reporting**: Generate daily sales reports and payment records
- **Database Integration**: PostgreSQL backend for persistent data storage
- **GUI Interface**: User-friendly Swing-based graphical interface
- **Web UI Support**: Basic web interface components

## Project Structure

```
Restaurant-management-system/
├── RMS_GUI.java              # Main GUI entry point
├── RMS.java                  # Console entry point
├── Controller_GUI.java       # GUI controller logic
├── Controller.java           # Business logic controller
├── Database.java             # Database operations
├── DatabaseException.java    # Custom exception class
├── UserInterface_GUI.java    # GUI components and panels
├── UserInterface.java        # Console interface
├── WebUI.java                # Web interface components
├── Employee.java             # Employee model
├── Manager.java              # Manager model
├── Staff.java                # Staff base class
├── Order.java                # Order model
├── OrderDetail.java          # Order detail items
├── MenuItem.java             # Menu item model
├── dataFiles/                # Configuration and data files
│   ├── manager.txt           # Manager credentials
│   ├── staff.txt             # Staff data
│   ├── menu_item.txt         # Menu items
│   └── wage_info.txt         # Wage information
├── images/                   # GUI resource images
├── lib/                      # External dependencies
├── META-INF/                 # JAR manifest
└── RMS_GUI.jar              # Compiled executable JAR

```

## Requirements

- **Java**: JDK 8 or higher
- **PostgreSQL**: 9.6 or higher
- **Maven** (optional, for build management)

## Setup Instructions

### 1. Prerequisites

Ensure PostgreSQL is installed and running:
```bash
# Windows
postgresql-x.x-setup.exe

# macOS
brew install postgresql

# Linux
sudo apt-get install postgresql postgresql-contrib
```

### 2. Clone the Repository

```bash
git clone <repository-url>
cd Restaurant-management-system
```

### 3. Database Configuration

Update database connection details in `Database.java` if needed:
- Default: `localhost:5432`
- Database: `restaurant_db`
- User: `postgres`
- Password: Configure as per your PostgreSQL setup

### 4. Build the Project

```bash
# Compile all Java files
javac -cp lib/* *.java

# Or run the pre-compiled JAR
java -cp lib/*;. RMS_GUI
```

## Running the Application

### GUI Version (Recommended)
```bash
java -cp lib/*;. RMS_GUI
```

### Console Version
```bash
java -cp lib/*;. RMS
```

### Using the JAR File
```bash
java -jar RMS_GUI.jar
```

## Usage

1. **Start the Application**: Run using one of the methods above
2. **Login**: Use manager credentials or employee login
3. **Navigate**: Use the menu to access different features
4. **Create Orders**: Add items and process orders
5. **View Reports**: Generate and view sales reports

## File Descriptions

| File | Purpose |
|------|---------|
| `Controller_GUI.java` | Manages GUI interactions and business logic |
| `Database.java` | Handles all database operations |
| `UserInterface_GUI.java` | Swing components and UI panels |
| `Order.java` | Order entity with state management |
| `MenuItem.java` | Menu item details and pricing |
| `Staff.java` | Base class for employees and managers |

## Data Files

- **manager.txt**: Stores manager credentials
- **staff.txt**: Employee information
- **menu_item.txt**: Menu items with prices
- **wage_info.txt**: Wage and compensation data

## Technologies Used

- **Language**: Java
- **GUI Framework**: Swing
- **Database**: PostgreSQL
- **JDBC**: For database connectivity

## Known Issues

- Unchecked type warnings in `UserInterface_GUI.java` (non-critical)
- Requires manual database setup before first run

## Future Enhancements

- Migration to modern UI frameworks (JavaFX)
- REST API implementation
- Real-time inventory management
- Mobile application support
- Enhanced reporting capabilities

## Author

**Kazunori Hayashi**  
Version 1.0 - 02/08/2013

## License

This project is provided as-is for educational and commercial use.

## Support

For issues or questions, please refer to the project documentation or contact the development team.

---

**Last Updated**: May 2026
