package com.library;

import com.library.dao.BookDao;
import com.library.dao.TransactionDao;
import com.library.dao.UserDao;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.model.User;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LibraryManagementSystem {

	private final BookDao bookDao = new BookDao();
	private final UserDao userDao = new UserDao();
	private final TransactionDao transactionDao = new TransactionDao();

	private JFrame frame;
	private JPanel rootCards;
	private User loggedInUser;

	// Tables for admin views
	private JTable usersTable;
	private JTable booksTable;
	private JTable transactionsTable;

	// Tables for student views
	private JTable browseBooksTable;
	private JTable myBooksTable;
	private JTable myHistoryTable;

	public LibraryManagementSystem() {
		initUI();
	}

	private void initUI() {
		frame = new JFrame("Library Management System");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1100, 750);

		rootCards = new JPanel(new CardLayout());
		rootCards.add(createLoginPanel(), "login");
		frame.setContentPane(rootCards);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createLoginPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(8, 8, 8, 8);
		gc.fill = GridBagConstraints.HORIZONTAL;

		JLabel title = new JLabel("Library Management System");
		gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
		panel.add(title, gc);

		gc.gridwidth = 1;
		gc.gridx = 0; gc.gridy = 1;
		panel.add(new JLabel("Email:"), gc);
		JTextField emailField = new JTextField();
		gc.gridx = 1; gc.gridy = 1;
		panel.add(emailField, gc);

		gc.gridx = 0; gc.gridy = 2;
		panel.add(new JLabel("Password:"), gc);
		JPasswordField passwordField = new JPasswordField();
		gc.gridx = 1; gc.gridy = 2;
		panel.add(passwordField, gc);

		JButton loginBtn = new JButton("Sign in");
		JButton registerBtn = new JButton("Register");
		JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonRow.add(registerBtn);
		buttonRow.add(loginBtn);
		gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
		panel.add(buttonRow, gc);

		registerBtn.addActionListener(e -> showCard("register"));
		loginBtn.addActionListener(e -> {
			String email = emailField.getText().trim();
			String password = new String(passwordField.getPassword());
			User user = userDao.authenticate(email, password).orElse(null);
			if (user == null) {
				showInfo("Login Failed", "Invalid email or password.");
				return;
			}
			loggedInUser = user;
			if ("Administrator".equals(user.getRole())) {
				showAdminDashboard();
			} else {
				showStudentDashboard();
			}
		});

		// Also prepare registration card
		rootCards.add(createRegisterPanel(), "register");
		return panel;
	}

	private JPanel createRegisterPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(8, 8, 8, 8);
		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
		panel.add(new JLabel("Create New Student Account"), gc);
		gc.gridwidth = 1;

		gc.gridx = 0; gc.gridy = 1; panel.add(new JLabel("Full Name:"), gc);
		JTextField nameField = new JTextField();
		gc.gridx = 1; panel.add(nameField, gc);

		gc.gridx = 0; gc.gridy = 2; panel.add(new JLabel("Email:"), gc);
		JTextField emailField = new JTextField();
		gc.gridx = 1; panel.add(emailField, gc);

		gc.gridx = 0; gc.gridy = 3; panel.add(new JLabel("Password:"), gc);
		JPasswordField passwordField = new JPasswordField();
		gc.gridx = 1; panel.add(passwordField, gc);

		JButton backBtn = new JButton("Back to Login");
		JButton registerBtn = new JButton("Register");
		JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		row.add(backBtn); row.add(registerBtn);
		gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
		panel.add(row, gc);

		backBtn.addActionListener(e -> showCard("login"));
		registerBtn.addActionListener(e -> {
			String name = nameField.getText().trim();
			String email = emailField.getText().trim();
			String password = new String(passwordField.getPassword());
			if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
				showInfo("Form Error", "All fields are required.");
				return;
			}
			User newUser = new User(name, email, password, "Student", LocalDate.now());
			try {
				userDao.addUser(newUser);
				showInfo("Success", "Registration successful! Please log in.");
				showCard("login");
			} catch (SQLException ex) {
				showInfo("Database Error", "Could not register user. Email might already exist.");
			}
		});
		return panel;
	}

	private void showAdminDashboard() {
		JPanel admin = new JPanel(new BorderLayout());
		admin.add(createHeaderPanel("Admin Dashboard"), BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Dashboard", createAdminWelcomePanel());
		tabs.addTab("User Management", createUsersPanel());
		tabs.addTab("Book Management", createBooksPanel());
		tabs.addTab("Transactions", createTransactionsPanel());
		admin.add(tabs, BorderLayout.CENTER);

		rootCards.add(admin, "admin");
		showCard("admin");
		refreshAdminData();
	}

	private void showStudentDashboard() {
		JPanel student = new JPanel(new BorderLayout());
		student.add(createHeaderPanel("Student Dashboard"), BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Browse Books", createBrowseBooksPanel());
		tabs.addTab("My Issued Books", createMyBooksPanel());
		tabs.addTab("My Book History", createMyHistoryPanel());
		student.add(tabs, BorderLayout.CENTER);

		rootCards.add(student, "student");
		showCard("student");
		refreshStudentData();
	}

	private JPanel createHeaderPanel(String title) {
		JPanel header = new JPanel(new BorderLayout());
		header.add(new JLabel(title), BorderLayout.WEST);
		JButton logout = new JButton("Logout");
		logout.addActionListener(e -> {
			loggedInUser = null;
			showCard("login");
		});
		header.add(logout, BorderLayout.EAST);
		return header;
	}

	private JPanel createAdminWelcomePanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Welcome to the Admin Dashboard. Use the tabs to manage the library."));
		return panel;
	}

	private JPanel createUsersPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		usersTable = new JTable(new DefaultTableModel(new Object[]{"Name", "Email", "Role", "Join Date"}, 0));
		panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton add = new JButton("Add");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		actions.add(add); actions.add(edit); actions.add(delete);
		panel.add(actions, BorderLayout.SOUTH);

		add.addActionListener(e -> onAddUser());
		edit.addActionListener(e -> onEditUser());
		delete.addActionListener(e -> onDeleteUser());
		return panel;
	}

	private JPanel createBooksPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		booksTable = new JTable(new DefaultTableModel(new Object[]{"Title", "Author", "Genre", "ISBN", "Available"}, 0));
		panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton add = new JButton("Add");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		actions.add(add); actions.add(edit); actions.add(delete);
		panel.add(actions, BorderLayout.SOUTH);

		add.addActionListener(e -> onAddBook());
		edit.addActionListener(e -> onEditBook());
		delete.addActionListener(e -> onDeleteBook());
		return panel;
	}

	private JPanel createTransactionsPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel issueRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField isbnField = new JTextField(12);
		JTextField emailField = new JTextField(16);
		JButton issueBtn = new JButton("Issue Book");
		issueRow.add(new JLabel("Book ISBN:")); issueRow.add(isbnField);
		issueRow.add(new JLabel("User Email:")); issueRow.add(emailField);
		issueRow.add(issueBtn);
		panel.add(issueRow, BorderLayout.NORTH);

		transactionsTable = new JTable(new DefaultTableModel(new Object[]{"Book", "User", "Issue Date", "Due Date", "Returned", "ID", "ISBN"}, 0));
		panel.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton markReturned = new JButton("Return Selected");
		actions.add(markReturned);
		panel.add(actions, BorderLayout.SOUTH);

		issueBtn.addActionListener(e -> {
			try {
				transactionDao.issueBook(isbnField.getText().trim(), emailField.getText().trim());
				showInfo("Success", "Book issued successfully.");
				refreshAdminData();
			} catch (SQLException ex) {
				showInfo("Error", "Could not issue book: " + ex.getMessage());
			}
		});

		markReturned.addActionListener(e -> {
			int row = transactionsTable.getSelectedRow();
			if (row < 0) return;
			DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
			int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 5)));
			String isbn = String.valueOf(model.getValueAt(row, 6));
			try {
				transactionDao.returnBook(id, isbn);
				showInfo("Success", "Book returned successfully.");
				refreshAdminData();
			} catch (SQLException ex) {
				showInfo("Error", "Could not return book: " + ex.getMessage());
			}
		});

		return panel;
	}

	private JPanel createBrowseBooksPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		browseBooksTable = new JTable(new DefaultTableModel(new Object[]{"Title", "Author", "Genre", "Available"}, 0));
		panel.add(new JScrollPane(browseBooksTable), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createMyBooksPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		myBooksTable = new JTable(new DefaultTableModel(new Object[]{"Book Title", "Issue Date", "Due Date"}, 0));
		panel.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createMyHistoryPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		myHistoryTable = new JTable(new DefaultTableModel(new Object[]{"Book Title", "Issue Date", "Returned"}, 0));
		panel.add(new JScrollPane(myHistoryTable), BorderLayout.CENTER);
		return panel;
	}

	private void onAddBook() {
		JTextField title = new JTextField();
		JTextField author = new JTextField();
		JTextField genre = new JTextField();
		JTextField isbn = new JTextField();
		JComboBox<String> available = new JComboBox<>(new String[]{"true", "false"});
		Object[] fields = {"Title", title, "Author", author, "Genre", genre, "ISBN", isbn, "Available", available};
		int res = JOptionPane.showConfirmDialog(frame, fields, "Add New Book", JOptionPane.OK_CANCEL_OPTION);
		if (res == JOptionPane.OK_OPTION) {
			try {
				bookDao.addBook(new Book(title.getText(), author.getText(), genre.getText(), isbn.getText(), Boolean.parseBoolean((String) available.getSelectedItem())));
				refreshAdminData();
			} catch (SQLException e) {
				showInfo("Database Error", "Could not add book. ISBN might already exist.");
			}
		}
	}

	private void onEditBook() {
		int row = booksTable.getSelectedRow();
		if (row < 0) return;
		DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
		String currentIsbn = String.valueOf(model.getValueAt(row, 3));
		JTextField title = new JTextField(String.valueOf(model.getValueAt(row, 0)));
		JTextField author = new JTextField(String.valueOf(model.getValueAt(row, 1)));
		JTextField genre = new JTextField(String.valueOf(model.getValueAt(row, 2)));
		JComboBox<String> available = new JComboBox<>(new String[]{"true", "false"});
		available.setSelectedItem(String.valueOf(model.getValueAt(row, 4)));
		Object[] fields = {"Title", title, "Author", author, "Genre", genre, "Available", available};
		int res = JOptionPane.showConfirmDialog(frame, fields, "Edit Book", JOptionPane.OK_CANCEL_OPTION);
		if (res == JOptionPane.OK_OPTION) {
			try {
				bookDao.updateBook(new Book(title.getText(), author.getText(), genre.getText(), currentIsbn, Boolean.parseBoolean((String) available.getSelectedItem())));
				refreshAdminData();
			} catch (SQLException e) {
				showInfo("Database Error", "Could not update book.");
			}
		}
	}

	private void onDeleteBook() {
		int row = booksTable.getSelectedRow();
		if (row < 0) return;
		DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
		String isbn = String.valueOf(model.getValueAt(row, 3));
		int confirm = JOptionPane.showConfirmDialog(frame, "Delete book with ISBN " + isbn + "?", "Confirm", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				bookDao.deleteBook(isbn);
				refreshAdminData();
			} catch (SQLException e) {
				showInfo("Error", "Could not delete book.");
			}
		}
	}

	private void onAddUser() {
		JTextField name = new JTextField();
		JTextField email = new JTextField();
		JPasswordField password = new JPasswordField();
		JComboBox<String> role = new JComboBox<>(new String[]{"Student", "Administrator"});
		Object[] fields = {"Name", name, "Email", email, "Password", password, "Role", role};
		int res = JOptionPane.showConfirmDialog(frame, fields, "Add New User", JOptionPane.OK_CANCEL_OPTION);
		if (res == JOptionPane.OK_OPTION) {
			try {
				userDao.addUser(new User(name.getText(), email.getText(), new String(password.getPassword()), (String) role.getSelectedItem(), LocalDate.now()));
				refreshAdminData();
			} catch (SQLException e) {
				showInfo("Database Error", "Could not add user. Email might already exist.");
			}
		}
	}

	private void onEditUser() {
		int row = usersTable.getSelectedRow();
		if (row < 0) return;
		DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
		String email = String.valueOf(model.getValueAt(row, 1));
		String currentName = String.valueOf(model.getValueAt(row, 0));
		String currentRole = String.valueOf(model.getValueAt(row, 2));
		JTextField name = new JTextField(currentName);
		JComboBox<String> role = new JComboBox<>(new String[]{"Student", "Administrator"});
		role.setSelectedItem(currentRole);
		Object[] fields = {"Name", name, "Role", role};
		int res = JOptionPane.showConfirmDialog(frame, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
		if (res == JOptionPane.OK_OPTION) {
			try {
				User existing = userDao.getAllUsers().stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
				if (existing != null) {
					User updated = new User(name.getText(), email, existing.getPassword(), (String) role.getSelectedItem(), existing.getJoinDate());
					userDao.updateUser(updated);
					refreshAdminData();
				}
			} catch (SQLException e) {
				showInfo("Database Error", "Could not update user.");
			}
		}
	}

	private void onDeleteUser() {
		int row = usersTable.getSelectedRow();
		if (row < 0) return;
		DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
		String email = String.valueOf(model.getValueAt(row, 1));
		if (loggedInUser != null && email.equals(loggedInUser.getEmail())) {
			showInfo("Action Denied", "You cannot delete your own account.");
			return;
		}
		if (userDao.hasActiveTransactions(email)) {
			showInfo("Deletion Failed", "Cannot delete user with outstanding books.");
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(frame, "Delete user " + email + "?", "Confirm", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				userDao.deleteUser(email);
				refreshAdminData();
			} catch (SQLException e) {
				showInfo("Error", "Could not delete user.");
			}
		}
	}

	private void refreshAdminData() {
		// Books
		List<Book> allBooks = bookDao.getAllBooks();
		DefaultTableModel bookModel = (DefaultTableModel) booksTable.getModel();
		bookModel.setRowCount(0);
		for (Book b : allBooks) {
			bookModel.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getGenre(), b.getIsbn(), b.isAvailable()});
		}

		// Users
		List<User> allUsers = userDao.getAllUsers();
		DefaultTableModel userModel = (DefaultTableModel) usersTable.getModel();
		userModel.setRowCount(0);
		for (User u : allUsers) {
			userModel.addRow(new Object[]{u.getName(), u.getEmail(), u.getRole(), u.getJoinDate()});
		}

		// Transactions
		List<Transaction> allTx = transactionDao.getAllTransactions();
		DefaultTableModel txModel = (DefaultTableModel) transactionsTable.getModel();
		txModel.setRowCount(0);
		for (Transaction t : allTx) {
			txModel.addRow(new Object[]{t.getBookTitle(), t.getUserName(), t.getIssueDate(), t.getDueDate(), t.isReturned(), t.getId(), t.getBookIsbn()});
		}
	}

	private void refreshStudentData() {
		// Browse
		List<Book> allBooks = bookDao.getAllBooks();
		DefaultTableModel browseModel = (DefaultTableModel) browseBooksTable.getModel();
		browseModel.setRowCount(0);
		for (Book b : allBooks) {
			browseModel.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getGenre(), b.isAvailable()});
		}

		// My active
		List<Transaction> myActive = transactionDao.getTransactionsForUser(loggedInUser.getEmail(), false);
		DefaultTableModel myModel = (DefaultTableModel) myBooksTable.getModel();
		myModel.setRowCount(0);
		for (Transaction t : myActive) {
			if (!t.isReturned()) {
				myModel.addRow(new Object[]{t.getBookTitle(), t.getIssueDate(), t.getDueDate()});
			}
		}

		// My history (returned only)
		DefaultTableModel histModel = (DefaultTableModel) myHistoryTable.getModel();
		histModel.setRowCount(0);
		for (Transaction t : myActive) {
			if (t.isReturned()) {
				histModel.addRow(new Object[]{t.getBookTitle(), t.getIssueDate(), true});
			}
		}
	}

	private void showCard(String name) {
		CardLayout cl = (CardLayout) rootCards.getLayout();
		cl.show(rootCards, name);
	}

	private void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(LibraryManagementSystem::new);
	}
}

