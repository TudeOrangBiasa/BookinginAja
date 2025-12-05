package com.example.opp.util;

/**
 * Simple tool untuk generate password hash
 * Run: mvn exec:java -Dexec.mainClass="com.example.opp.util.PasswordHashGenerator" -Dexec.args="yourpassword"
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: mvn exec:java -Dexec.mainClass=\"com.example.opp.util.PasswordHashGenerator\" -Dexec.args=\"yourpassword\"");
            System.out.println("\nOr run without args for interactive mode:");
            interactiveMode();
            return;
        }

        String password = args[0];
        String hashed = PasswordUtil.hash(password);

        System.out.println("\n=================================");
        System.out.println("Password Hash Generator");
        System.out.println("=================================");
        System.out.println("Original: " + password);
        System.out.println("Hashed:   " + hashed);
        System.out.println("=================================\n");
        System.out.println("SQL Insert Example:");
        System.out.println("INSERT INTO users (username, password, email, full_name) VALUES");
        System.out.println("('admin', '" + hashed + "', 'admin@example.com', 'Administrator');\n");
    }

    private static void interactiveMode() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.println("\n=================================");
        System.out.println("  Password Hash Generator");
        System.out.println("=================================\n");
        
        while (true) {
            System.out.print("Enter password (or 'quit' to exit): ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("quit")) {
                break;
            }
            
            if (input.isEmpty()) {
                System.out.println("Password cannot be empty!\n");
                continue;
            }
            
            String hashed = PasswordUtil.hash(input);
            
            System.out.println("\nOriginal: " + input);
            System.out.println("Hashed:   " + hashed);
            System.out.println("\nSQL Update:");
            System.out.println("UPDATE users SET password = '" + hashed + "' WHERE username = 'your_username';\n");
            System.out.println("---------------------------------\n");
        }
        
        scanner.close();
        System.out.println("Goodbye!");
    }
}
