package dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import dto.WordDTO;

public class DataAccessLayer implements IWordDAO {
	private static final String PROPERTIES_FILE = "configure.properties";
	private Connection conn;
	
	public DataAccessLayer() {
		Properties props = new Properties();
		 try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
	            if (input == null) {
	                throw new IOException("Unable to find " + PROPERTIES_FILE);
	            }

	           
	            props.load(input);
	            String url = props.getProperty("db.url");
	            String user = props.getProperty("db.user");
	            String password = props.getProperty("db.password");

	            
	            conn = DriverManager.getConnection(url, user, password);

	            
	        } catch (IOException e) {
	            System.out.println("Error loading properties file: " + e.getMessage());
	        } catch (SQLException e) {
	            System.out.println("Database connection error: " + e.getMessage());
	        }
	    
	}

	@Override
	public List<WordDTO> searchByMeaning(String meaning) {
	    List<WordDTO> wordDTOList = new ArrayList<>();
	    String query = "SELECT w.id, w.word, wp.part_of_speech, um.meaning AS urdu_meaning, pm.meaning AS persian_meaning " +
	            "FROM words w " +
	            "LEFT JOIN urdu_meanings um ON w.id = um.word_id " +
	            "LEFT JOIN persian_meanings pm ON w.id = pm.word_id " +
	            "LEFT JOIN word_pos wp ON w.id = wp.word_id " +
	            "WHERE um.meaning = ? OR pm.meaning = ?";

	    try (PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setString(1, meaning);
	        ps.setString(2, meaning);
	        ResultSet rs = ps.executeQuery();
	        Map<Integer, WordDTO> wordMap = new HashMap<>();

	        while (rs.next()) {
	            int wordId = rs.getInt("id");
	            String word = rs.getString("word");
	            String partOfSpeech = rs.getString("part_of_speech");
	            String urdu_meaning = rs.getString("urdu_meaning");
	            String persianMeaning = rs.getString("persian_meaning");

	            WordDTO wordDTO = wordMap.get(wordId);

	            if (wordDTO == null) {
	                wordDTO = new WordDTO(wordId, word, new ArrayList<>(), persianMeaning, urdu_meaning);
	                wordMap.put(wordId, wordDTO);
	            }
	            if (partOfSpeech != null) {
	                wordDTO.getPartOfSpeech().add(partOfSpeech);
	            }
	        }
	        wordDTOList.addAll(wordMap.values());

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return wordDTOList;
	}


	@Override
	public List<WordDTO> searchWord(String word) {
	    List<WordDTO> wordDTOList = new ArrayList<>();
	    String query = "SELECT w.id, w.word, wp.part_of_speech, um.meaning AS urdu_meaning, pm.meaning AS persian_meaning " +
	            "FROM words w " +
	            "LEFT JOIN urdu_meanings um ON w.id = um.word_id " +
	            "LEFT JOIN persian_meanings pm ON w.id = pm.word_id " +
	            "LEFT JOIN word_pos wp ON w.id = wp.word_id " +
	            "WHERE w.word = ?";

	    try (PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setString(1, word);
	        ResultSet rs = ps.executeQuery();
	        Map<Integer, WordDTO> wordMap = new HashMap<>();

	        while (rs.next()) {
	            int wordId = rs.getInt("id");
	            String partOfSpeech = rs.getString("part_of_speech");
	            String urdu_meaning = rs.getString("urdu_meaning");
	            String persianMeaning = rs.getString("persian_meaning");
	            WordDTO wordDTO = wordMap.get(wordId);

	            if (wordDTO == null) {
	                wordDTO = new WordDTO(wordId, word, new ArrayList<>(), persianMeaning, urdu_meaning);
	                wordMap.put(wordId, wordDTO);
	            }
	            if (partOfSpeech != null) {
	                wordDTO.getPartOfSpeech().add(partOfSpeech);
	            }
	        }
	        wordDTOList.addAll(wordMap.values());

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return wordDTOList;
	}


	@Override
	public List<WordDTO> getAllWordDTOs() {
		    List<WordDTO> wordDTOs = new ArrayList<>();
		    String query = "SELECT w.id AS wordId, w.word, wp.part_of_speech, " +
		            "um.meaning AS urdu_meaning, pm.meaning AS persianMeaning " +
		            "FROM words w " +
		            "LEFT JOIN urdu_meanings um ON w.id = um.word_id " +
		            "LEFT JOIN persian_meanings pm ON w.id = pm.word_id " +
		            "LEFT JOIN word_pos wp ON w.id = wp.word_id"; 

		    try (Statement stmt = conn.createStatement();
		         ResultSet rs = stmt.executeQuery(query)) {
		        Map<Integer, WordDTO> wordMap = new HashMap<>();

		        while (rs.next()) {
		            int wordId = rs.getInt("wordId");
		            String word = rs.getString("word");
		            String partOfSpeech = rs.getString("part_of_speech");
		            String urdu_meaning = rs.getString("urdu_meaning");
		            String persianMeaning = rs.getString("persianMeaning");

		            WordDTO wordDTO = wordMap.get(wordId);
		            if (wordDTO == null) {
		                wordDTO = new WordDTO(wordId, word, new ArrayList<>(), persianMeaning, urdu_meaning);
		                wordMap.put(wordId, wordDTO);
		            }
		            if (partOfSpeech != null) {
		                wordDTO.getPartOfSpeech().add(partOfSpeech);
		            }
		        }
		        wordDTOs.addAll(wordMap.values());

		    } catch (SQLException e) {
		        e.printStackTrace();
		    }

		    return wordDTOs;
		}

	@Override   
	public String addWord(String word, String urdu_meaning, String persianMeaning) {
		if (word == null || word.isEmpty()) {
	        throw new IllegalArgumentException("Word cannot be null or empty");
	    }
		
		int wordId = getWordId(word);  

		if (wordId != -1) {  
			if (urdu_meaning != null && !urduMeaningExists(wordId, urdu_meaning)) {
				insertUrduMeaning(wordId, urdu_meaning);
			}

			if (persianMeaning != null && !persianMeaningExists(wordId, persianMeaning)) {
				insertPersianMeaning(wordId, persianMeaning);
			}

			return "Meanings added to existing word (if not already present).";

		} else { 
			wordId = insertWord(word);  
			if (wordId != -1) {
				if (urdu_meaning != null) {
					insertUrduMeaning(wordId, urdu_meaning);
				}
				if (persianMeaning != null) {
					insertPersianMeaning(wordId, persianMeaning);
				}
				return "Word and meanings added successfully.";
			} else {
				return "Failed to add word.";
			}
		}
	}

	@Override
	public String updateMeaning(String word, String oldMeaning, String newMeaning, String language) {

		String checkWordSQL = "SELECT id FROM words WHERE word = ?";

		String updateMeaningSQL;
		if (language.equalsIgnoreCase("Urdu")) {
			updateMeaningSQL = "UPDATE urdu_meanings SET meaning = ? WHERE word_id = ? AND meaning = ?";
		} else if (language.equalsIgnoreCase("persian")) {
			updateMeaningSQL = "UPDATE persian_meanings SET meaning = ? WHERE word_id = ? AND meaning = ?";
		} else {
			return "Unsupported language. Please choose either 'Urdu' or 'persian'.";
		}

		try (PreparedStatement checkWordStmt = conn.prepareStatement(checkWordSQL);
				PreparedStatement updateMeaningStmt = conn.prepareStatement(updateMeaningSQL)) {

			checkWordStmt.setString(1, word);
			ResultSet rs = checkWordStmt.executeQuery();

			if (rs.next()) {
				int wordId = rs.getInt("id");

				updateMeaningStmt.setString(1, newMeaning);
				updateMeaningStmt.setInt(2, wordId);
				updateMeaningStmt.setString(3, oldMeaning);

				int rowsAffected = updateMeaningStmt.executeUpdate();
				if (rowsAffected > 0) {
					return language + " meaning updated successfully.";
				} else {
					return "Old " + language + " meaning not found for this word.";
				}
			} else {
				return "Word not found in the database.";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "An error occurred while updating the meaning: " + e.getMessage();
		}
	}

	@Override
	public String deleteWord(String word) {
		if (word == null || word.trim().isEmpty()) {
			return "Word cannot be empty";
		}

		String checkWordSQL = "SELECT id FROM words WHERE word = ?";
		String deleteUrduMeaningSQL = "DELETE FROM urdu_meanings WHERE word_id = (SELECT id FROM words WHERE word = ?)";
		String deletePersianMeaningSQL = "DELETE FROM persian_meanings WHERE word_id = (SELECT id FROM words WHERE word = ?)";
		String deleteWordSQL = "DELETE FROM words WHERE word = ?";

		try (PreparedStatement checkWordStmt = conn.prepareStatement(checkWordSQL);
				PreparedStatement deleteUrduMeaningStmt = conn.prepareStatement(deleteUrduMeaningSQL);
				PreparedStatement deletePersianMeaningStmt = conn.prepareStatement(deletePersianMeaningSQL);
				PreparedStatement deleteWordStmt = conn.prepareStatement(deleteWordSQL)) {

			checkWordStmt.setString(1, word);
			ResultSet rs = checkWordStmt.executeQuery();

			if (rs.next()) {
				deleteUrduMeaningStmt.setString(1, word);
				deleteUrduMeaningStmt.executeUpdate();

				deletePersianMeaningStmt.setString(1, word);
				deletePersianMeaningStmt.executeUpdate();

				deleteWordStmt.setString(1, word);
				deleteWordStmt.executeUpdate();

				return "Word and all its meanings deleted successfully.";
			} else {
				return "Word not found.";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getWordId(String word) {
		String query = "SELECT id FROM words WHERE word = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, word);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("id");
			}
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int insertWord(String word) {
	    String checkDuplicateQuery = "SELECT id FROM words WHERE word = ?";
	    String insertQuery = "INSERT INTO words (word) VALUES (?)";
	    
	    try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateQuery);
	         PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

	    	checkStmt.setString(1, word);
	        ResultSet checkRs = checkStmt.executeQuery();
	        if (checkRs.next()) {
	            return checkRs.getInt("id"); 
	        }

	        insertStmt.setString(1, word);
	        insertStmt.executeUpdate();
	        ResultSet rs = insertStmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1); 
	        }
	        return -1;

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}

	@Override
	public void insertUrduMeaning(int wordId, String meaning) {
		String query = "INSERT INTO urdu_meanings (word_id, meaning) VALUES (?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			ps.setString(2, meaning);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void insertPersianMeaning(int wordId, String meaning) {
		String query = "INSERT INTO persian_meanings (word_id, meaning) VALUES (?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			ps.setString(2, meaning);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean urduMeaningExists(int wordId, String meaning) {
		String query = "SELECT * FROM urdu_meanings WHERE word_id = ? AND meaning = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			ps.setString(2, meaning);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean persianMeaningExists(int wordId, String meaning) {
		String query = "SELECT * FROM persian_meanings WHERE word_id = ? AND meaning = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			ps.setString(2, meaning);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int[] processWordFromFile(String line) {
		String[] parts = line.split(",");
		int[] insertCounts = {0, 0}; 

		if (parts.length >= 4) { 
			String word = parts[0].trim(); 
			String urdu_meaning = parts[1].trim(); 
			String persianMeaning = parts[2].trim(); 
			String partOfSpeech = parts[3].trim(); 

			int wordId = getWordId(word);

			if (wordId == -1) {
				wordId = insertWord(word);
				insertPartOfSpeech(wordId,partOfSpeech);
				insertCounts[0]++;
			}

			if (!urduMeaningExists(wordId, urdu_meaning)) {
				insertUrduMeaning(wordId, urdu_meaning);
				insertCounts[1]++;
			}

			if (!persianMeaningExists(wordId, persianMeaning)) {
				insertPersianMeaning(wordId, persianMeaning);
				insertCounts[1]++;
			}
		}

		return insertCounts; 
	}
	
	@Override  
	public void insertPartOfSpeech(int wordId, String partOfSpeech) {
	    String query = "INSERT INTO word_pos (word_id, part_of_speech) VALUES (?, ?)";
	    try (PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setInt(1, wordId);  
	        ps.setString(2, partOfSpeech);  
	        ps.executeUpdate();  
	    } catch (SQLException e) {
	        e.printStackTrace(); 
	    }
	}

	@Override
	public List<String> getPartOfSpeech(String word) {
	    int wordId = getWordId(word);  
	    if (wordId == -1) {
	        return null; 
	    }

	    List<String> posList = new ArrayList<>();  
	    String query = "SELECT part_of_speech FROM word_pos WHERE word_id = ?";  

	    try (PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setInt(1, wordId);  
	        ResultSet rs = ps.executeQuery(); 
	        while (rs.next()) {
	            posList.add(rs.getString("part_of_speech"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); 
	    }
	    return posList;  
	}

	@Override
	public void insertRootWord(String word, String rootWord) {
		String query = "UPDATE words SET root_word = ? WHERE word = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, rootWord);
			ps.setString(2, word);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	@Override
	public String getRootWord(String word) {
		int wordId = getWordId(word); 
		if (wordId == -1) {
			return null; 
		}

		String query = "SELECT root_word FROM words WHERE id = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId); 
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getString("root_word"); 
			} else {
				return null; 
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null; 
		}
	}


	@Override
	public WordDTO getMeaningsForWord(String word) {
	    String query = "SELECT w.id AS word_id, w.word, wp.part_of_speech, am.meaning AS urdu_meaning, pm.meaning AS persian_meaning " +
	                   "FROM words w " +
	                   "LEFT JOIN urdu_meanings am ON w.id = am.word_id " +
	                   "LEFT JOIN persian_meanings pm ON w.id = pm.word_id " +
	                   "LEFT JOIN word_pos wp ON w.id = wp.word_id " +
	                   "WHERE w.word = ?";
	    
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, word);
	        ResultSet rs = stmt.executeQuery();

	        WordDTO wordDTO = null;
	        List<String> partOfSpeechList = new ArrayList<>();
	        String urduMeaning = null;
	        String persianMeaning = null;

	        while (rs.next()) { 
	            if (wordDTO == null) {
	                urduMeaning = rs.getString("urdu_meaning");
	                persianMeaning = rs.getString("persian_meaning");
	                wordDTO = new WordDTO(
	                    rs.getInt("word_id"),
	                    rs.getString("word"),
	                    partOfSpeechList, 
	                    persianMeaning,
	                    urduMeaning
	                );
	            }

	            String partOfSpeech = rs.getString("part_of_speech");
	            if (partOfSpeech != null && !partOfSpeechList.contains(partOfSpeech)) {
	                partOfSpeechList.add(partOfSpeech);
	            }
	        }

	        return wordDTO;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}




	@Override
	public boolean markWordAsFavorite(int wordId) {
		String query = "UPDATE words SET favorite = TRUE WHERE id = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0; 
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public boolean unmarkWordAsFavorite(int wordId) {
		String query = "UPDATE words SET favorite = FALSE WHERE id = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, wordId);
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public List<String> getFavoriteWords() {
		List<String> favoriteWords = new ArrayList<>();
		String query = "SELECT word FROM words WHERE favorite = TRUE";
		try (PreparedStatement ps = conn.prepareStatement(query);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				favoriteWords.add(rs.getString("word"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteWords;
	}
	@Override
	public boolean isFavorite(String word) {
		String query = "SELECT favorite FROM words WHERE word = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, word);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getBoolean("favorite");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void addSearchToHistory(String word) {
	    String checkDuplicateQuery = "SELECT 1 FROM recent_searches WHERE word = ?";
	    String insertQuery = "INSERT INTO recent_searches (word) VALUES (?)";
	    String deleteOldestQuery = "DELETE FROM recent_searches WHERE id = (SELECT MIN(id) FROM recent_searches)";
	    String countQuery = "SELECT COUNT(*) FROM recent_searches";

	    try (PreparedStatement countStmt = conn.prepareStatement(countQuery);
	         PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateQuery);
	         PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
	         PreparedStatement deleteStmt = conn.prepareStatement(deleteOldestQuery)) {

	        checkStmt.setString(1, word);
	        ResultSet checkRs = checkStmt.executeQuery();
	        if (checkRs.next()) {
	            return; 
	        }
	        ResultSet countRs = countStmt.executeQuery();
	        if (countRs.next() && countRs.getInt(1) >= 5) {
	            deleteStmt.executeUpdate(); 
	        }

	        insertStmt.setString(1, word);
	        insertStmt.executeUpdate();

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public List<String> getRecentSearches() {
		List<String> recentWords = new ArrayList<>();
		String query = "SELECT word FROM recent_searches ORDER BY id DESC";

		try (PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				recentWords.add(rs.getString("word"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return recentWords;
	}
	
	
	
	@Override
	 public void saveStems(int wordId, LinkedList<String> stemsList) {
	        String insertQuery = "INSERT INTO stems (word_id, stem_word) VALUES (?, ?)";

	        try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
	            for (String stem : stemsList) {
	                ps.setInt(1, wordId);
	                ps.setString(2, stem);
	                ps.executeUpdate();
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	
	@Override
	public List<String> fetchSuggestions(String text) {
	    List<String> suggestions = new ArrayList<>();
	    String query = "SELECT word FROM words WHERE word LIKE ? LIMIT 10";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, text + "%");
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                suggestions.add(rs.getString("word"));
	            }
	        }
	    } catch (SQLException e) {
	    	e.printStackTrace();	    }

	    return suggestions;
	}
	@Override
	public void clearRecentSearchesTable()  {
        String clearQuery = "DELETE FROM recent_searches";
        try (PreparedStatement stmt = conn.prepareStatement(clearQuery)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
			e.printStackTrace();
		}
    }


	public void close() throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
