package pl;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;



import bl.BusinessLayer;
import dal.IWordDAO;
import dto.WordDTO;

public class PresentationLayer extends JFrame {
	JTextField searchField;
	private JPanel recentSearchPanel;
	private DefaultTableModel recentSearchModel;
	private static final long serialVersionUID = 1L;
	private static final String NULL = null;
	private BusinessLayer bl;
	private CardLayout cardLayout;
	private JPanel mainPanel;
	
	public PresentationLayer(IWordDAO O) {
		bl = new BusinessLayer(O);

		setTitle("Word Management Application");
		setSize(800, 500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);

		// Adding Menu Bar
		setJMenuBar(createMenuBar());

		// Adding the main content panel
		mainPanel.add(createMainPanel(), "Menu");
		this.add(mainPanel);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// Edit Dropdown
		JMenu editMenu = new JMenu("≡");
		JMenuItem importItem = new JMenuItem("Import File");
		JMenuItem scrapeItem = new JMenuItem("Scrape Data");
		JMenuItem customDictionaryItem = new JMenuItem("Custom Dictionary");

		importItem.addActionListener(e -> openFileDialog());
		scrapeItem.addActionListener(e -> scrapeWordDetailsFromWebsite());
		customDictionaryItem.addActionListener(e -> showCustomDictionaryPanel());

		editMenu.add(importItem);
		editMenu.add(scrapeItem);
		editMenu.add(customDictionaryItem);

		// CRUD Dropdown
		JMenu manageMenu = new JMenu("Edit");
		JMenuItem addItem = new JMenuItem("Add Word");
		JMenuItem updateItem = new JMenuItem("Update Meaning");
		JMenuItem deleteItem = new JMenuItem("Delete Word");
		addItem.addActionListener(e -> showAddPanel());
		updateItem.addActionListener(e -> showUpdatePanel());
		deleteItem.addActionListener(e -> showDeletePanel());
		manageMenu.add(addItem);
		manageMenu.add(updateItem);
		manageMenu.add(deleteItem);

		// View Dictionary Dropdown
		JMenu viewMenu = new JMenu("View");
		JMenuItem viewAllWordsItem = new JMenuItem("View All Words");
		JMenuItem viewOneWordItem = new JMenuItem("View One Word");
		viewAllWordsItem.addActionListener(e -> displayAllWords());
		viewOneWordItem.addActionListener(e -> displayOneWord());
		viewMenu.add(viewAllWordsItem);
		viewMenu.add(viewOneWordItem);

		// Tools Dropdown for Stemmer, POS Tagger, and Segmentation
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem limitizationItem = new JMenuItem("Limitization");
		JMenuItem stemmerItem = new JMenuItem("Stemmer");
		JMenuItem tokenizationtem = new JMenuItem("Tokenization");
		JMenuItem posTaggerItem = new JMenuItem("POS Tagger");
		JMenuItem segmentationItem = new JMenuItem("Segmentation");

		limitizationItem.addActionListener(e -> searchRootWord());
		stemmerItem.addActionListener(e -> searchStem());
		tokenizationtem.addActionListener(e -> tokenRootWord());
		posTaggerItem.addActionListener(e -> tagPartOfSpeech());
		segmentationItem.addActionListener(e -> handleSegmentation()); 

		toolsMenu.add(limitizationItem);
		toolsMenu.add(stemmerItem); 
		toolsMenu.add(posTaggerItem);
		toolsMenu.add(tokenizationtem);
		toolsMenu.add(segmentationItem); 

		// Favorites Dropdown
		JMenu favoritesMenu = new JMenu("Favorites");
		JMenuItem viewFavoritesItem = new JMenuItem("View Favorites");
		viewFavoritesItem.addActionListener(e -> handleViewFavorites());
		favoritesMenu.add(viewFavoritesItem);

		menuBar.add(editMenu);
		menuBar.add(manageMenu);
		menuBar.add(viewMenu);
		menuBar.add(toolsMenu);
		menuBar.add(favoritesMenu); 

		return menuBar;
	}

	private void handleSegmentation() {
		String inputWord = JOptionPane.showInputDialog(this,"Enter an Arabic word for segmentation:");

		if (inputWord == null || inputWord.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No word entered. Exiting program.");
			return;
		}

		List<String>  segmentedResult = bl.segmentArabicWord(inputWord);

		JOptionPane.showMessageDialog(this, "Segmented Word: " + segmentedResult);
	}

	private JPanel createMainPanel() {
		// Main panel with background image
		JPanel mainPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
			    super.paintComponent(g);
			    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Background.jpg")) {
			        if (inputStream != null) {
			            Image backgroundImage = ImageIO.read(inputStream);
			            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
			        } else {
			            throw new IOException("Background image not found in the classpath.");
			        }
			    } catch (IOException e) {
			        System.out.println("Error loading background image: " + e.getMessage());
			        g.setColor(Color.LIGHT_GRAY);
			        g.fillRect(0, 0, getWidth(), getHeight());
			    }
			}
		};

		// Centered title and search bar
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setOpaque(false);

		JLabel titleLabel = new JLabel("فاسٹ ڈکشنری", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
		titleLabel.setForeground(Color.RED);

		JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		searchBarPanel.setOpaque(false);

		searchField = new JTextField(30);
		JButton searchButton1 = new JButton("Search by Meaning");
		JButton searchButton = new JButton("Search by Word");

		recentSearchPanel = createRecentSearchPanel();
		recentSearchPanel.setVisible(false);

		searchField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (!recentSearchPanel.isVisible()) {
					recentSearchPanel.setVisible(true); 
					updateRecentSearches(); 
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(() -> recentSearchPanel.setVisible(false)); 
			}
		});

		JPopupMenu suggestionMenu = new JPopupMenu();
		suggestionMenu.setFocusable(false); 

		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = searchField.getText().trim();
				new Thread(() -> {
					List<String> suggestions = bl.getSuggestions(text); 
					SwingUtilities.invokeLater(() -> updateSuggestions(suggestions, searchField, suggestionMenu));
				}).start();
			}
		});

		searchButton.addActionListener(e -> {
			String s = searchField.getText().trim();
			bl.addSearch(s); 
			List<WordDTO> results = bl.searchWord(s);
			displayResults(results,s);
			recentSearchPanel.setVisible(false); 
		});

		searchButton1.addActionListener(e -> {
			String s = searchField.getText().trim();
			bl.addSearch(s); 
			List<WordDTO> results = bl.searchByMeaning(s);
			displayResults1(results);
			recentSearchPanel.setVisible(false); 
		});

		searchBarPanel.add(searchField);
		searchBarPanel.add(searchButton);
		searchBarPanel.add(searchButton1);

		searchPanel.add(titleLabel, BorderLayout.NORTH);
		searchPanel.add(searchBarPanel, BorderLayout.CENTER);

		mainPanel.add(searchPanel, BorderLayout.NORTH);

		mainPanel.add(recentSearchPanel, BorderLayout.SOUTH);

		mainPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

		return mainPanel;
	}

	private void updateSuggestions(List<String> suggestions, JTextField searchField, JPopupMenu suggestionMenu) {
		if (suggestions == null || suggestions.isEmpty()) {
			suggestionMenu.setVisible(false); 
			return;
		}

		suggestionMenu.removeAll(); 
		for (String suggestion : suggestions) {
			JMenuItem item = new JMenuItem(suggestion);
			item.setFocusable(false); 
			item.addActionListener(e -> {
				searchField.setText(suggestion); 
				suggestionMenu.setVisible(false); 
			});
			suggestionMenu.add(item);
		}

		if (!suggestionMenu.isVisible()) {
			suggestionMenu.show(searchField, 0, searchField.getHeight());
		}
	}

	private JPanel createRecentSearchPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); 
		panel.setPreferredSize(new Dimension(300, 100));

		recentSearchModel = new DefaultTableModel(new String[]{"Recent Searches"}, 0);

		JTable recentSearchTable = new JTable(recentSearchModel);
		recentSearchTable.setFillsViewportHeight(true);


		recentSearchTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = recentSearchTable.rowAtPoint(e.getPoint());  

				if (selectedRow != -1) {
					String selectedSearch = (String) recentSearchModel.getValueAt(selectedRow, 0);


					if (searchField != null) {
						searchField.setText(selectedSearch);  
						recentSearchPanel.setVisible(false);  
					}
				} 
			}
		});




		JScrollPane scrollPane = new JScrollPane(recentSearchTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private void updateRecentSearches(){
		if (recentSearchModel == null || bl == null) {
			System.err.println("Model or Business Logic instance is not initialized.");
			return;
		}

		recentSearchModel.setRowCount(0);

		List<String> recentSearches = bl.getRecent();
		if (recentSearches != null && !recentSearches.isEmpty()) {
			for (String search : recentSearches) {
				recentSearchModel.addRow(new Object[]{search});
			}
		}
	}



	private void displayResults(List<WordDTO> results, String s) {
		if (results.isEmpty()) {
			JOptionPane.showMessageDialog(this, "<html><b>No meanings against this word</b></html>", "Results", JOptionPane.INFORMATION_MESSAGE);

			if (s == null || s.isEmpty()) {
				JOptionPane.showMessageDialog(this, "<html><b>No word entered for segmentation.</b></html>", "Segmentation", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			List<String> segmentedWords = bl.segmentArabicWord(s);

			if (segmentedWords != null && !segmentedWords.isEmpty()) {
				String[] segmentedWordsArray = segmentedWords.toArray(new String[0]);
				String selectedWord = (String) JOptionPane.showInputDialog(
						this, 
						"<html><b>Select a segmented word to search for its meaning:</b></html>", 
						"Segmented Words", 
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						segmentedWordsArray, 
						segmentedWordsArray[0]
						);

				if (selectedWord != null) {
					List<WordDTO> searchResults = bl.searchWord(selectedWord);

					if (searchResults != null && !searchResults.isEmpty()) {
						displayResults(searchResults, null);
					} else {
						JOptionPane.showMessageDialog(this, "<html><b>No meanings found for the selected word.</b></html>", "Search Result", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "<html><b>No word selected. Exiting.</b></html>", "Segmentation", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "<html><b>Segmentation result is empty.</b></html>", "Segmentation", JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}

		StringBuilder styledResults = new StringBuilder("<html><body>");

		styledResults.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
		styledResults.append("<tr style='background-color: #f0f0f0;'><th>Word</th><th>Part of Speech</th><th style='color: red;'>Persian Meaning</th><th style='color: blue;'>Urdu Meaning</th></tr>");

		for (WordDTO dto : results) {
			styledResults.append("<tr>")
			.append("<td>").append(dto.getWord()).append("</td>")
			.append("<td>").append(dto.getPartOfSpeech()).append("</td>")
			.append("<td style='color: red;'>").append(dto.getPersianMeaning()).append("</td>")
			.append("<td style='color: blue;'>").append(dto.getUrduMeaning()).append("</td>")
			.append("</tr>");
		}
		styledResults.append("</table></body></html>");


		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText(styledResults.toString());
		textPane.setEditable(false);
		textPane.setBackground(null);

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(500, 300));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);

		JButton favoriteButton = new JButton("Add to Favorite");
		favoriteButton.setBackground(Color.blue); 
		favoriteButton.setForeground(Color.WHITE); 
		favoriteButton.setFont(new Font("Arial", Font.BOLD, 12));

		favoriteButton.addActionListener(e -> {
			if (!results.isEmpty()) {
				String word = results.get(0).getWord(); 
				addToFavorite(word);

			}
		});

		panel.add(favoriteButton, BorderLayout.SOUTH);


		JOptionPane.showMessageDialog(this, panel, "Results", JOptionPane.PLAIN_MESSAGE);
	}

	private void addToFavorite(String word) {
		boolean success = bl.addToFavorite(word);
		if (success) {
			JOptionPane.showMessageDialog(this, "'" + word + "' has been added to your favorites.", "Favorite Added", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "'" + word + "' is already in your favorites or doesn't exist.", "Add Favorite Failed", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void displayAllWords() {
		List<WordDTO> words = bl.fetchWords();

		JFrame frame = new JFrame("Dictionary - All Words and Meanings");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 400);

		DefaultTableModel model = new DefaultTableModel(new String[]{"Word", "Persian Meaning", "Urdu Meaning"}, 0);
		JTable table = new JTable(model);

		for (WordDTO wordDTO : words) {
			String word = wordDTO.getWord();
			String persianMeaning = wordDTO.getPersianMeaning();
			String urduMeaning = wordDTO.getUrduMeaning();
			model.addRow(new Object[]{word, persianMeaning, urduMeaning});
		}
		JScrollPane scrollPane = new JScrollPane(table);

		JPanel buttonPanel = new JPanel();
		JButton deleteButton = new JButton("Delete Word");
		JButton updateButton = new JButton("Update Word");    
		JButton addButton = new JButton("ADD Word");
		JButton posButton = new JButton("POS");
		JButton rootButton = new JButton("Root Word");
		JButton favButton = new JButton("Add to Favorite");

		buttonPanel.add(addButton);
		buttonPanel.add(updateButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(posButton);
		buttonPanel.add(rootButton);
		buttonPanel.add(favButton);

		addButton.addActionListener(e -> showAddPanel());

		favButton.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String selectedWord = (String) model.getValueAt(selectedRow, 0);
				addToFavorite(selectedWord);
			}
		});


		posButton.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String selectedWord = (String) model.getValueAt(selectedRow, 0);
				String posTag = bl.tagPartOfSpeech(selectedWord);
				JOptionPane.showMessageDialog(this, "The part of speech for the word '" + selectedWord + "' is: " + posTag, "POS Tagging Result", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		rootButton.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String selectedWord = (String) model.getValueAt(selectedRow, 0);
				String root = bl.findRootWord(selectedWord);
				JOptionPane.showMessageDialog(this, "The Root Word for the word '" + selectedWord + "' is: " + root, "Result", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		deleteButton.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String selectedWord = (String) model.getValueAt(selectedRow, 0);
				int confirm = JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to delete the word: " + selectedWord + "?",
						"Confirm Delete",
						JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					bl.deleteWord(selectedWord);
					model.removeRow(selectedRow);
					JOptionPane.showMessageDialog(frame, "Word deleted successfully!");
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Please select a word to delete.");
			}
		});

		updateButton.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String selectedWord = (String) model.getValueAt(selectedRow, 0);

				JPanel updatePanel = new JPanel();
				JTextField updateWordField = new JTextField(selectedWord, 10);
				JTextField oldMeaningField = new JTextField(10);
				JTextField newMeaningField = new JTextField(10);
				String[] languages = {"Urdu", "Persian"};
				JComboBox<String> languageComboBox = new JComboBox<>(languages);
				JButton submitButton = new JButton("Update");

				languageComboBox.addActionListener(evt -> {
					String selectedLanguage = (String) languageComboBox.getSelectedItem();
					if ("Urdu".equals(selectedLanguage)) {
						oldMeaningField.setText((String) model.getValueAt(selectedRow, 2));
					} else if ("Persian".equals(selectedLanguage)) {
						oldMeaningField.setText((String) model.getValueAt(selectedRow, 1));
					}
				});

				languageComboBox.setSelectedIndex(0);

				submitButton.addActionListener(evt -> {
					String updatedWord = updateWordField.getText();
					String oldMeaning = oldMeaningField.getText();
					String newMeaning = newMeaningField.getText();
					String selectedLanguage = (String) languageComboBox.getSelectedItem();

					if (!newMeaning.isEmpty()) {
						bl.updateMeaning(updatedWord, oldMeaning, newMeaning, selectedLanguage);

						if ("Urdu".equals(selectedLanguage)) {
							model.setValueAt(newMeaning, selectedRow, 2);
						} else if ("Persian".equals(selectedLanguage)) {
							model.setValueAt(newMeaning, selectedRow, 1);
						}
						JOptionPane.showMessageDialog(frame, "Word updated successfully!");
					} else {
						JOptionPane.showMessageDialog(frame, "New meaning cannot be empty.");
					}
				});

				updatePanel.add(new JLabel("Word to Update:"));
				updatePanel.add(updateWordField);
				updatePanel.add(new JLabel("Old Meaning:"));
				updatePanel.add(oldMeaningField);
				updatePanel.add(new JLabel("New Meaning:"));
				updatePanel.add(newMeaningField);
				updatePanel.add(new JLabel("Language:"));
				updatePanel.add(languageComboBox);
				updatePanel.add(submitButton);

				int result = JOptionPane.showConfirmDialog(frame, updatePanel, 
						"Update Word", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (result != JOptionPane.OK_OPTION) {
					return; 
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Please select a word to update.");
			}
		});

		frame.setLayout(new BorderLayout());
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);

		frame.setVisible(true);
	}


	private void displayOneWord() {
		List<WordDTO> words = bl.fetchWords();

		JFrame frame = new JFrame("Dictionary - View Words and Show Meanings on Selection");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 300);

		DefaultTableModel model = new DefaultTableModel(new String[]{"Word", "Part of Speech", "Perian Meaning", "Urdu Meaning"}, 0);
		JTable table = new JTable(model);

		for (WordDTO wordDTO : words) {
			model.addRow(new Object[]{wordDTO.getWord(), "", "", ""});
		}

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1) {
					WordDTO wordDTO = words.get(selectedRow);
					model.setValueAt(wordDTO.getPartOfSpeech(), selectedRow, 1);
					model.setValueAt(wordDTO.getPersianMeaning(), selectedRow, 2);
					model.setValueAt(wordDTO.getUrduMeaning(), selectedRow, 3);
				}
			}
		});

		JScrollPane tableScrollPane = new JScrollPane(table);
		frame.add(tableScrollPane);
		frame.setVisible(true);
	}

	private void displayResults1(List<WordDTO> results) {
		if (results.isEmpty()) {
			JOptionPane.showMessageDialog(this, "<html><b>No words found for the given meaning.</b></html>", "Results", JOptionPane.INFORMATION_MESSAGE);
			return;
		}


		StringBuilder styledResults = new StringBuilder("<html><body>");
		styledResults.append("<h3 style='color: navy;'>Words associated with the provided meaning:</h3>");
		styledResults.append("<ul style='font-family: Arial; font-size: 14px;'>");

		for (WordDTO dto : results) {
			styledResults.append("<li>").append(dto.getWord()).append("</li>");
		}

		styledResults.append("</ul>");
		styledResults.append("</body></html>");


		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText(styledResults.toString());
		textPane.setEditable(false);
		textPane.setBackground(null);

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(500, 300));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);


		JButton favoriteButton = new JButton("Add to Favorite");
		favoriteButton.setBackground(Color.ORANGE); 
		favoriteButton.setForeground(Color.WHITE); 
		favoriteButton.setFont(new Font("Arial", Font.BOLD, 12)); 

		favoriteButton.addActionListener(e -> {
			if (!results.isEmpty()) {
				String word = results.get(0).getWord(); 
				addToFavorite(word);

			}
		});

		panel.add(favoriteButton, BorderLayout.SOUTH);


		JOptionPane.showMessageDialog(this, panel, "Results", JOptionPane.PLAIN_MESSAGE);
	}



	private void showAddPanel() {
		JPanel addPanel = new JPanel();
		JTextField wordField = new JTextField(10);
		JTextField urduMeaningField = new JTextField(10);      
		JTextField persianMeaningField = new JTextField(10);     
		JButton submitButton = new JButton("Add");

		submitButton.addActionListener(e -> addWord(wordField, urduMeaningField, persianMeaningField));

		addPanel.add(new JLabel("Word:"));
		addPanel.add(wordField);
		addPanel.add(new JLabel("Urdu Meaning:"));  
		addPanel.add(urduMeaningField);              
		addPanel.add(new JLabel("Persian Meaning:"));  
		addPanel.add(persianMeaningField);             
		addPanel.add(submitButton);
		addPanel.add(createBackButton());

		mainPanel.add(addPanel, "Add");
		cardLayout.show(mainPanel, "Add");
	}



	private void showUpdatePanel() {
		JPanel updatePanel = new JPanel();
		JTextField updateWordField = new JTextField(10);
		JTextField oldMeaningField = new JTextField(10);
		JTextField newMeaningField = new JTextField(10);
		String[] languages = {"Urdu", "Persian"};
		JComboBox<String> languageComboBox = new JComboBox<>(languages);  
		JButton submitButton = new JButton("Update");

		submitButton.addActionListener(e -> updateMeaning(updateWordField, oldMeaningField, newMeaningField, languageComboBox));

		updatePanel.add(new JLabel("Word to Update:"));
		updatePanel.add(updateWordField);
		updatePanel.add(new JLabel("Old Meaning:"));
		updatePanel.add(oldMeaningField);
		updatePanel.add(new JLabel("New Meaning:"));
		updatePanel.add(newMeaningField);
		updatePanel.add(new JLabel("Language:"));  
		updatePanel.add(languageComboBox);         
		updatePanel.add(submitButton);
		updatePanel.add(createBackButton());

		mainPanel.add(updatePanel, "Update");
		cardLayout.show(mainPanel, "Update");
	}


	private void showDeletePanel() {
		JPanel deletePanel = new JPanel();
		JTextField deleteWordField = new JTextField(10);
		JButton submitButton = new JButton("Delete");

		submitButton.addActionListener(e -> deleteWord(deleteWordField));

		deletePanel.add(new JLabel("Word to Delete:"));
		deletePanel.add(deleteWordField);
		deletePanel.add(submitButton);
		deletePanel.add(createBackButton());

		mainPanel.add(deletePanel, "Delete");
		cardLayout.show(mainPanel, "Delete");
	}


	private void addWord(JTextField wordField, JTextField urduMeaningField, JTextField persianMeaningField) {
		String word = wordField.getText();
		String urduMeaning = urduMeaningField.getText();
		String persianMeaning = persianMeaningField.getText();

		String result = bl.addWord(word, urduMeaning, persianMeaning);
		JOptionPane.showMessageDialog(this, result);

		wordField.setText("");
		urduMeaningField.setText("");
		persianMeaningField.setText("");
	}


	private void updateMeaning(JTextField updateWordField, JTextField oldMeaningField, JTextField newMeaningField, JComboBox<String> languageComboBox) {
		String wordToUpdate = updateWordField.getText();
		String oldMeaning = oldMeaningField.getText();
		String newMeaning = newMeaningField.getText();
		String language = (String) languageComboBox.getSelectedItem();

		String result = bl.updateMeaning(wordToUpdate, oldMeaning, newMeaning, language); 
		JOptionPane.showMessageDialog(this, result);
		updateWordField.setText("");
		oldMeaningField.setText("");
		newMeaningField.setText("");
		languageComboBox.setSelectedIndex(0);
	}


	private void deleteWord(JTextField deleteWordField) {
		String wordToDelete = deleteWordField.getText();
		String result = bl.deleteWord(wordToDelete); 
		JOptionPane.showMessageDialog(this, result);
		deleteWordField.setText("");
	}

	private JButton createBackButton() {
		JButton backButton = new JButton("Back to Menu");
		backButton.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
		return backButton;
	}



	private void openFileDialog() {
		Frame frame = new Frame();
		FileDialog fileDialog = new FileDialog(frame, "Select a file", FileDialog.LOAD);
		fileDialog.setVisible(true);

		String directory = fileDialog.getDirectory();
		String fileName = fileDialog.getFile();

		if (fileName != null) {
			String filePath = directory + fileName;
			importFromFile(filePath);
		} else {
			JOptionPane.showMessageDialog(this, "File selection cancelled.", "Input Error", JOptionPane.WARNING_MESSAGE);
		}

		frame.dispose();
	}

	public void importFromFile(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			JOptionPane.showMessageDialog(this, "File path is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			int[] result = bl.addWordsFromFileWithThreads(filePath);

			JOptionPane.showMessageDialog(
					this,
					result[0] + " new words and " + result[1] + " new meanings inserted successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE
					);
		}  catch (Exception e) {
			JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void scrapeWordDetailsFromWebsite() {
		String urduWord = JOptionPane.showInputDialog(this,"Enter an Arabic word to scrape details:");
		if (urduWord == null || urduWord.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Invalid input. Please enter a word.");
			return;
		}

		boolean success = bl.importWordDetailsFromWebsite(urduWord);
		if (success) {
			JOptionPane.showMessageDialog(this, "Data imported successfully!");
		} else {
			JOptionPane.showMessageDialog(this, "No data found for the given word.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void tagPartOfSpeech() {
		String word = JOptionPane.showInputDialog(this, "Enter word for POS tagging:");
		if (word != null && !word.trim().isEmpty()) {
			try {
				String posTag = bl.tagPartOfSpeech(word);

				if ("No part of speech found".equals(posTag)) {
					String manualPos = JOptionPane.showInputDialog(this, "No part of speech found for '" + word + "'. Please enter it manually:");

					if (manualPos != null && !manualPos.trim().isEmpty()) {
						bl.insertPartOfSpeech(word, manualPos); 
						JOptionPane.showMessageDialog(this, "The part of speech for the word '" + word + "' has been saved as: " + manualPos, "POS Tagging Result", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "No input provided. Operation canceled.", "Input Error", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "The part of speech for the word '" + word + "' is: " + posTag, "POS Tagging Result", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error tagging part of speech: " + e.getMessage(), "POS Tagging Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please enter a valid word.", "Input Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void searchRootWord() {
		String arabicWord = JOptionPane.showInputDialog(this, "Enter Arabic Word:", "Search Root Word", JOptionPane.QUESTION_MESSAGE);

		if (arabicWord != null && !arabicWord.trim().isEmpty()) {
			String root = bl.findRootWord(arabicWord);

			if (root != null && !root.isEmpty()) {
				int option = JOptionPane.showConfirmDialog(this, "Limi word found: " + root + "\nDo you want to view more details?", 
						"Limi Word ", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

				if (option == JOptionPane.YES_OPTION) {
					List<WordDTO> results = bl.searchWord(root);

					if (results != null && !results.isEmpty()) {
						displayResults(results,NULL);
					} else {
						JOptionPane.showMessageDialog(this, "No results found for the root word.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, "No root word found for the input.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Input cannot be empty. Please enter a valid Arabic word.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void tokenRootWord() {
		String arabicWord = JOptionPane.showInputDialog(this, "Enter Arabic Word:", "Token Root Word", JOptionPane.QUESTION_MESSAGE);

		if (arabicWord != null && !arabicWord.trim().isEmpty()) {
			String root = bl.tokenization(arabicWord);

			if (root != null && !root.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Root word found: " + root, "Root Word Result", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "No root word found for the input.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Input cannot be empty. Please enter a valid Arabic word.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}


	private void showCustomDictionaryPanel() {
		JPanel customDictionaryPanel = new JPanel(new BorderLayout());

		JLabel instructionLabel = new JLabel("Enter Arabic passage:");
		JTextArea passageArea = new JTextArea(23, 30);
		passageArea.setLineWrap(true);
		passageArea.setWrapStyleWord(true);

		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton fetchMeaningsButton = new JButton("Fetch Meanings");
		JButton backButton = new JButton("Go Back");

		fetchMeaningsButton.addActionListener(e -> {
			String passage = passageArea.getText().trim();
			if (passage.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please enter a passage.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			List<WordDTO> wordDTOList = bl.getMeaningsForCustomDictionary(passage);

			if (wordDTOList.isEmpty()) {
				JOptionPane.showMessageDialog(this, "No meanings found for the passage.", "Info", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String[] columnNames = {"Word", "POS", "Urdu Meaning", "Persian Meaning"};
				Object[][] data = new Object[wordDTOList.size()][4];

				for (int i = 0; i < wordDTOList.size(); i++) {
					WordDTO wordDTO = wordDTOList.get(i);
					data[i][0] = wordDTO.getWord();
					data[i][1] = wordDTO.getPartOfSpeech();
					data[i][2] = wordDTO.getUrduMeaning();
					data[i][3] = wordDTO.getPersianMeaning();
				}

				JTable resultTable = new JTable(data, columnNames);
				resultTable.setFillsViewportHeight(true);
				JScrollPane scrollPane = new JScrollPane(resultTable);


				JOptionPane.showMessageDialog(this, scrollPane, "Word Meanings", JOptionPane.INFORMATION_MESSAGE);

				int choice = JOptionPane.showConfirmDialog( this,"Would you like to save the meanings to a file?", "Save Meanings", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.YES_OPTION) {
					String filePath = bl.exportMeaningsToFile(wordDTOList);

					if (filePath != null) {
						JOptionPane.showMessageDialog(this, "Meanings saved successfully at: " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to save meanings.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}else {
				    JOptionPane.showMessageDialog( this, "Save operation canceled.", "Canceled",  JOptionPane.WARNING_MESSAGE);
				    }
			}
		});

		backButton.addActionListener(e -> {
			cardLayout.show(mainPanel, "Menu"); 
		});

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(instructionLabel);
		topPanel.add(Box.createVerticalStrut(2));
		topPanel.add(new JScrollPane(passageArea));

		footerPanel.add(fetchMeaningsButton);
		footerPanel.add(backButton);

		customDictionaryPanel.add(topPanel, BorderLayout.NORTH);
		customDictionaryPanel.add(footerPanel, BorderLayout.SOUTH);

		mainPanel.add(customDictionaryPanel, "CustomDictionary");
		cardLayout.show(mainPanel, "CustomDictionary");
	}


	private void showFavoriteWordsDialog(List<String> favoriteWords) {
	    JFrame favoritesFrame = new JFrame("Favorite Words");
	    favoritesFrame.setSize(400, 300);

	    DefaultListModel<String> listModel = new DefaultListModel<>();
	    favoriteWords.forEach(listModel::addElement);

	    JList<String> favoritesList = new JList<>(listModel);
	    JScrollPane scrollPane = new JScrollPane(favoritesList);

	    JButton removeButton = new JButton("Remove from Favorites");
	    removeButton.addActionListener(e -> {
	        String selectedWord = favoritesList.getSelectedValue();
	        if (selectedWord != null) {
	            int confirm = JOptionPane.showConfirmDialog(favoritesFrame,
	                    "Are you sure you want to remove '" + selectedWord + "' from favorites?",
	                    "Confirm Removal", JOptionPane.YES_NO_OPTION);
	            if (confirm == JOptionPane.YES_OPTION) {
	                bl.unmarkWordAsFavorite(selectedWord);
	                listModel.removeElement(selectedWord);
	                JOptionPane.showMessageDialog(favoritesFrame,
	                        "'" + selectedWord + "' removed from favorites.");
	            }
	        } else {
	            JOptionPane.showMessageDialog(favoritesFrame, "Please select a word to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
	        }
	    });

	    JButton removeAllButton = new JButton("Remove All Favorites");
	    removeAllButton.addActionListener(e -> {
	        int confirm = JOptionPane.showConfirmDialog(favoritesFrame,
	                "Are you sure you want to remove all words from favorites?",
	                "Confirm Removal", JOptionPane.YES_NO_OPTION);
	        if (confirm == JOptionPane.YES_OPTION) {
	            bl.removeAllFavoriteWords(); 
	            listModel.clear(); 
	            JOptionPane.showMessageDialog(favoritesFrame, "All favorite words have been removed.");
	        }
	    });

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(removeButton);
	    buttonPanel.add(removeAllButton); 

	    favoritesFrame.add(scrollPane, BorderLayout.CENTER);
	    favoritesFrame.add(buttonPanel, BorderLayout.SOUTH);
	    favoritesFrame.setVisible(true);
	}

	private void handleViewFavorites() {
		List<String> favoriteWords = bl.getAllFavoriteWords();
		if (favoriteWords.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No favorite words found.", "Favorites", JOptionPane.INFORMATION_MESSAGE);
		} else {
			showFavoriteWordsDialog(favoriteWords);
		}
	}

	public void searchStem() {
		try {
			String arabicText = JOptionPane.showInputDialog(this, "Enter Arabic Word:",  "Arabic Stemmer", JOptionPane.QUESTION_MESSAGE);
			if (arabicText == null || arabicText.isEmpty()) {
				JOptionPane.showMessageDialog(this, "No input provided!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			LinkedList<String> stemsList = bl.getStemsForWord(arabicText);

			String[] columns = {"Word", "Stem"};
			Object[][] data = new Object[stemsList.size()][2];

			for (int i = 0; i < stemsList.size(); i++) {
				data[i][0] = arabicText; 
				data[i][1] = stemsList.get(i); 
			}

			DefaultTableModel model = new DefaultTableModel(data, columns);
			JTable table = new JTable(model);
			JScrollPane scrollPane = new JScrollPane(table);

			JFrame frame = new JFrame("Arabic POS Stemmer");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 200);
			frame.add(scrollPane);
			frame.setVisible(true);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"Error: " + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}