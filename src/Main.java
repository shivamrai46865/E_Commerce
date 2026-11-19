import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to ecommerce_db successfully!");

            while (true) {
                System.out.println("\n=== E-Commerce Order Management System ===");
                System.out.println("1. Insert Order Records (Part a)");
                System.out.println("2. Search Orders by Status & Quantity > 2 (Part b)");
                System.out.println("3. Increase Order Quantity (Part c)");
                System.out.println("4. Update Order Status (Part d)");
                System.out.println("5. Total Order Value & Delete Cancelled Order (Part e)");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        OrderOperations.insertOrders(conn, scanner);
                        break;
                    case 2:
                        OrderOperations.displayFilteredOrders(conn, scanner);
                        break;
                    case 3:
                        OrderOperations.increaseOrderQuantity(conn, scanner);
                        break;
                    case 4:
                        OrderOperations.updateOrderStatus(conn, scanner);
                        break;
                    case 5:
                        OrderOperations.calculateTotalAndDeleteCancelled(conn, scanner);
                        break;
                    case 6:
                        System.out.println("Exiting application...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}