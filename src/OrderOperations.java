import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class OrderOperations {

    // Part (a)
    public static void insertOrders(Connection conn, Scanner scanner) {
        String insertSQL = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.print("How many records do you want to insert? (Minimum 3): ");
        int count = scanner.nextInt();
        scanner.nextLine();

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (int i = 1; i <= count; i++) {
                System.out.println("\n--- Enter details for Order #" + i + " ---");
                System.out.print("Order ID: ");
                int orderId = scanner.nextInt();
                scanner.nextLine();

                System.out.print("Customer Name: ");
                String customerName = scanner.nextLine();

                System.out.print("Product Name: ");
                String productName = scanner.nextLine();

                System.out.print("Quantity: ");
                int quantity = scanner.nextInt();

                System.out.print("Unit Price: ");
                double unitPrice = scanner.nextDouble();
                scanner.nextLine();

                System.out.print("Status (Pending/Shipped/Delivered/Cancelled): ");
                String status = scanner.nextLine();

                pstmt.setInt(1, orderId);
                pstmt.setString(2, customerName);
                pstmt.setString(3, productName);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, unitPrice);
                pstmt.setString(6, status);

                try {
                    if (pstmt.executeUpdate() > 0) {
                        System.out.println("Order ID " + orderId + " inserted successfully.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Error: Duplicate Order ID " + orderId + ". Skipping.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Part (b)
    public static void displayFilteredOrders(Connection conn, Scanner scanner) {
        System.out.print("Enter order status to search: ");
        String status = scanner.nextLine();
        String query = "SELECT * FROM Orders WHERE LOWER(status) = LOWER(?) AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            System.out.println("\n--- Matching Orders ---");
            while (rs.next()) {
                found = true;
                printOrderRow(rs);
            }
            if (!found) {
                System.out.println("No matching orders found.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Part (c)
    public static void increaseOrderQuantity(Connection conn, Scanner scanner) {
        System.out.print("Enter Order ID: ");
        int orderId = scanner.nextInt();
        System.out.print("Enter additional quantity: ");
        int addQty = scanner.nextInt();
        scanner.nextLine();

        String updateSQL = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setInt(1, addQty);
            pstmt.setInt(2, orderId);

            if (pstmt.executeUpdate() > 0) {
                System.out.println("Quantity updated successfully.");
            } else {
                System.out.println("Order ID " + orderId + " does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Part (d)
    public static void updateOrderStatus(Connection conn, Scanner scanner) {
        System.out.print("Enter Order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine();

        String selectSQL = "SELECT * FROM Orders WHERE order_id = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setInt(1, orderId);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Order ID " + orderId + " does not exist.");
                return;
            }

            System.out.println("Current Order Details:");
            printOrderRow(rs);

            System.out.print("Enter new status (Pending, Shipped, Delivered, Cancelled): ");
            String newStatus = scanner.nextLine().trim();

            List<String> validStatuses = Arrays.asList("Pending", "Shipped", "Delivered", "Cancelled");
            boolean isValid = validStatuses.stream().anyMatch(s -> s.equalsIgnoreCase(newStatus));

            if (!isValid) {
                System.out.println("Invalid status entered.");
                return;
            }

            String formattedStatus = newStatus.substring(0, 1).toUpperCase() + newStatus.substring(1).toLowerCase();
            String updateSQL = "UPDATE Orders SET status = ? WHERE order_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, formattedStatus);
                updateStmt.setInt(2, orderId);
                updateStmt.executeUpdate();
                System.out.println("Status updated successfully!");

                ResultSet updatedRs = selectStmt.executeQuery();
                if (updatedRs.next()) {
                    System.out.println("Updated Order Details:");
                    printOrderRow(updatedRs);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Part (e)
    public static void calculateTotalAndDeleteCancelled(Connection conn, Scanner scanner) {
        String sumSQL = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sumSQL)) {
            if (rs.next()) {
                System.out.printf("Total Order Value: %.2f\n", rs.getDouble("total_value"));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        System.out.print("Enter Order ID to delete: ");
        int orderId = scanner.nextInt();
        scanner.nextLine();

        String checkSQL = "SELECT status FROM Orders WHERE order_id = ?";
        String deleteSQL = "DELETE FROM Orders WHERE order_id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, orderId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Cannot delete: Order ID " + orderId + " does not exist.");
                return;
            }

            String currentStatus = rs.getString("status");
            if ("Cancelled".equalsIgnoreCase(currentStatus)) {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, orderId);
                    deleteStmt.executeUpdate();
                    System.out.println("Order ID " + orderId + " deleted successfully.");
                }
            } else {
                System.out.println("Cannot delete: Status is '" + currentStatus + "'. Only 'Cancelled' orders can be deleted.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    private static void printOrderRow(ResultSet rs) throws SQLException {
        System.out.printf("ID: %d | Customer: %s | Product: %s | Qty: %d | Price: %.2f | Status: %s\n",
                rs.getInt("order_id"),
                rs.getString("customer_name"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getString("status"));
    }
}