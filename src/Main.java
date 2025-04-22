import database.*;

public class Main {
    public static void main(String[] args) {
        // Set the database instance
        DatabaseProvider.setInstance(new DerbyDatabase());
        
        // Initialize the database (create tables and load initial data)
        IDatabase db = DatabaseProvider.getInstance();
        
        // The getInstance method now handles table creation if needed
        
        System.out.println("Database initialized successfully!");
    }
} 