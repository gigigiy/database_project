package org.example;

import java.sql.*;
import java.util.Scanner;



public class Main {
    private static final String DB_URL = "jdbc:postgresql:new_book";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            System.out.println("Connected to the database");
            Scanner scanner = new Scanner(System.in);

            boolean continueAdding = true;

            while (continueAdding) {
                System.out.println("Do you want to add a book to the cart? (Enter 1 to add, 2 to cancel)");
                int addBookChoice = scanner.nextInt();

                if (addBookChoice == 1) {
                    System.out.println("Enter the book ID:");
                    int bookId = scanner.nextInt();

                    System.out.println("Enter the quantity:");
                    int quantity = scanner.nextInt();

                    executeInsert(connection, "INSERT INTO cart (book_id, quantity) VALUES (?, ?)", bookId, quantity);

                    System.out.println("Do you want to continue adding books to the cart? (Enter 1 to continue, 2 to move to orders)");
                    int continueChoice = scanner.nextInt();

                    if (continueChoice == 1) {
                        continueAdding = true;
                    } else if (continueChoice == 2) {
                        callFunction(connection, "SELECT move_cart_to_orders()");
                        System.out.println("Books moved to orders.");
                        continueAdding = false;
                    } else {
                        System.out.println("Invalid choice. Cancelling operation.");
                        continueAdding = false;
                    }
                } else if (addBookChoice == 2) {
                    System.out.println("Book not added to the cart.");
                    continueAdding = false;
                } else {
                    System.out.println("Invalid choice. Book not added to the cart.");
                    continueAdding = false;
                }

                System.out.println("\nCart:");
                executeQueryCart(connection, "SELECT * FROM cart");

                System.out.println("\nOrders:");
                executeQueryOrder(connection, "SELECT * FROM orders");

                ResultSet ordersResultSet = getOrdersResultSet(connection);
                if (ordersResultSet != null) {
                    while (ordersResultSet.next()) {
                        int orderId = ordersResultSet.getInt("order_id");
                        String bookName = ordersResultSet.getString("book_name");
                        double bookPrice = ordersResultSet.getDouble("book_price");
                        int orderQuantity = ordersResultSet.getInt("quantity");

                        System.out.println("Order ID: " + orderId + ", Book Name: " + bookName +
                                ", Book Price: " + bookPrice + ", Quantity: " + orderQuantity);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    private static void executeInsert(Connection connection, String query, int bookId, int quantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bookId);
            statement.setInt(2, quantity);
            statement.executeUpdate();
        }
    }

    private static void callFunction(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
    }

    private static void executeQueryCart(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int cartId = resultSet.getInt("cart_id");
                int bookId = resultSet.getInt("book_id");
                double bookPrice = resultSet.getDouble("book_price");
                int quantity = resultSet.getInt("quantity");
                int userId = resultSet.getInt("user_id");

                System.out.println("Cart ID: " + cartId + ", Book ID: " + bookId +
                        ", Book Price: " + bookPrice + ", Quantity: " + quantity + ", User ID: " + userId);
            }
        }
    }

    private static void executeQueryOrder(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                String bookName = resultSet.getString("book_name");
                double bookPrice = resultSet.getDouble("book_price");
                int quantity = resultSet.getInt("quantity");

                System.out.println("Order ID: " + orderId + ", Book Name: " + bookName +
                        ", Book Price: " + bookPrice + ", Quantity: " + quantity);
            }
        }
    }

    private static ResultSet getOrdersResultSet(Connection connection) throws SQLException {
        String query = "SELECT * FROM orders";
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(query);
    }
}
