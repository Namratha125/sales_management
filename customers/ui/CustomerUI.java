package customers.ui;

import customers.facade.CustomerFacade;
import customers.command.CustomerCommand;
import customers.command.AddCustomerCommand;
import java.util.Scanner;

@SuppressWarnings("resource")
public class CustomerUI {
    // Added for integration with SalesManagementSystem
    public void displayMenu() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        CustomerFacade customerFacade = new CustomerFacade();
        // Do not close this Scanner to avoid closing System.in
        Scanner scanner = new Scanner(System.in); // NOSONAR

        System.out.println("===================================");
        System.out.println("   SALES SYSTEM: CUSTOMER MODULE   ");
        System.out.println("===================================");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add New Customer");
            System.out.println("2. Exit");
            System.out.print("Enter your choice (1-2): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    addNewCustomer(customerFacade, scanner);
                    break;
                case "2":
                    running = false;
                    System.out.println("Exiting Customer Module...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addNewCustomer(CustomerFacade customerFacade, Scanner scanner) {
        System.out.print("\nEnter Customer Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Customer Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Customer Phone: ");
        String phone = scanner.nextLine();

        System.out.print("Enter Customer Region: ");
        String region = scanner.nextLine();

        System.out.println("\nProcessing...");

        // Passing the real user input into the Command
        CustomerCommand addCmd = new AddCustomerCommand(
                customerFacade, name, email, phone, region);

        addCmd.execute();
        System.out.println("Customer added successfully!");
    }
}