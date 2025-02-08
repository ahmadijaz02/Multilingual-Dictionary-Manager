# Multilingual Dictionary

## 📌 Project Overview
A **Java-based multilingual dictionary** featuring a **Swing GUI** for efficient word management. Supports **Urdu, Arabic, and Persian** meanings, root words, and part-of-speech tagging. The project is built with a **3-layer architecture** ensuring scalability and efficiency.

## 🚀 Features
- **CRUD operations** for words, meanings, and root words.
- **MySQL database** integration for persistent storage.
- **Search history tracking** (latest 5 searches stored).
- **Web scraping** for fetching Arabic and Persian meanings.
- **Custom dictionary generation** for an input passage.
- **Unit tests** for core database functionalities.
- **Swing-based GUI** for an interactive experience.

## 🏗️ Tech Stack
- **Programming Language:** Java (JDK 11+)
- **GUI:** Swing
- **Database:** MySQL
- **Architecture:** 3-layer (Presentation, Business, Data Access Layer)
- **Testing:** JUnit (Unit Testing)

## 🔧 Installation & Setup
1. **Clone the Repository**
   ```sh
   git clone https://github.com/yourusername/multilingual-dictionary.git
   cd multilingual-dictionary
   ```
2. **Set Up the Database**
   - Import `database_schema.sql` into MySQL.
   - Update `db.properties` with your MySQL credentials.

3. **Run the Application**
   - Open in **Eclipse/IntelliJ** and run `Main.java`.

## 📜 Database Schema
Tables used in MySQL:
- `words (id, word, root_word, part_of_speech)`
- `arabic_meanings (id, word_id, meaning)`
- `persian_meanings (id, word_id, meaning)`
- `recent_searches (id, word)`
- `stems (id, word_id, stem_word)`

## 🧪 Running Unit Tests
1. Open your IDE.
2. Run `DictionaryDAOTest.java` for unit tests.
3. Ensure **MySQL connection** is active before testing.

## 📌 Future Enhancements
- **API integration** for additional language support.
- **Mobile app version** with cloud-based sync.
- **More efficient search algorithms** for improved performance.

## Create the database
CREATE DATABASE dictionary CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE dictionary;

-- Create the words table
CREATE TABLE words (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) UNIQUE NOT NULL,  -- Urdu word
    root_word VARCHAR(255),
    favorite BOOLEAN DEFAULT FALSE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Create the urdu_meanings table
CREATE TABLE urdu_meanings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word_id INT NOT NULL,  -- Foreign key referencing `words`
    meaning TEXT,  -- Meaning in Arabic
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE persian_meanings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word_id INT NOT NULL,  -- Foreign key referencing `words`
    meaning TEXT,  -- Meaning in Persian
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE word_pos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word_id INT NOT NULL,  -- Foreign key referencing `words`
    part_of_speech VARCHAR(50) NOT NULL,  -- POS for the word
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE stems (
    id INT AUTO_INCREMENT PRIMARY KEY,        -- Unique identifier for each stem entry
    word_id INT NOT NULL,                     -- Foreign key referencing the 'words' table
    stem_word VARCHAR(255) NOT NULL,          -- The stem word
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE -- Ensure referential integrity
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE recent_searches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(255) NOT NULL UNIQUE,
    search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


