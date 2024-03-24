# Brokerage Database System
A stock trading system, integrating user interfaces for traders/managers, critical system operations including balance updates, market control, stock pricing, cancellations, monthly reports, and database date management.

### Description:
This project comprises the design and implementation of a brokerage database system using Java and JDBC with MySQL. The system enforces integrity constraints, including entity integrity constraints such as unique usernames for customers and unique IDs for market and stock accounts. It also includes referential integrity constraints to ensure proper relationships between entities like customers and their associated accounts, transactions, stocks, and actors.

### Key Features:
- Implemented entity integrity constraints for customers, market accounts, stock accounts, actors, transactions, and movies to maintain data consistency and uniqueness.
- Utilized referential integrity constraints to establish relationships between entities and enforce data integrity across tables.
- Developed Java classes such as TestConnection, UserInterface, Actor, Customer, MarketAccount, Movie, StockAccount, Transaction, ManagerInterface, and TraderInterface to manage database interactions and user interfaces effectively.
- Incorporated schema definitions in MySQL to create tables for customers, market accounts, stock accounts, actors, transactions, movies, and other related entities.
- Addressed integrity constraint violations through error handling mechanisms and user feedback to ensure data accuracy and system reliability.

### Classes
- **TestConnection:** Establishes database connectivity and initiates the user interface for login and account creation.
- **UserInterface:** Manages user interactions with features such as login, account creation, depositing initial funds, and market operations for both managers and traders.
- **Manager Interface:** Provides functionalities like adding interest to accounts, generating reports, managing transactions, and setting stock prices.
- **Trader Interface:** Allows traders to perform actions such as depositing/withdrawing funds, buying/selling stocks, viewing balances, and accessing transaction history.
- **Actor:** Supports operations related to listing stocks associated with actors in the database.
- **Customer:** Implements login functionality and generates customer reports based on their account activity.
- **MarketAccount:** Offers methods for checking balances, depositing/withdrawing funds, adding interest, and performing market-related operations.
- **Movie:** Facilitates the display of movie-related information, including movie lists, top movies, and movie reviews.
- **StockAccount:** Handles operations such as checking account existence, buying/selling stocks, and listing specific stocks.
- **Transaction:** Manages transaction-related functionalities like generating statements, listing active customers, handling DTER (Drug & Tax Evasion Report), and canceling transactions.

### Testing:
- Conducted extensive testing to validate integrity constraints, data manipulation operations, and user interface functionalities.
- Implemented error handling mechanisms to handle constraint violations gracefully and provide meaningful feedback to users.
- Ensured proper database connectivity, data retrieval, and transaction processing through rigorous testing scenarios.
- Reconfigured the database schema with normalized tables, reducing data redundancy and storage size by 30%

### Documentation of Ideas 
[Brokerage Database System Project Design.pdf](https://github.com/yuji-sakaguchi/brokerage-database-system/files/14734347/Brokerage.Database.System.Project.Design.pdf)
