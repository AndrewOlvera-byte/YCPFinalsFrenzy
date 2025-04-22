package database;

/**
 * Singleton for providing database access throughout the application.
 */
public class DatabaseProvider {
    private static IDatabase instance;
    
    /**
     * Sets the database instance to be used by the application.
     * 
     * @param db The database implementation to use
     */
    public static void setInstance(IDatabase db) {
        instance = db;
    }
    
    /**
     * Gets the database implementation instance.
     * 
     * @return The database implementation
     */
    public static IDatabase getInstance() {
        if (instance == null) {
            // Default to Derby database if not explicitly set
            DerbyDatabase db = new DerbyDatabase();
            instance = db;
            
            // Initialize the database only if tables don't exist
            if (!db.tablesExist()) {
                instance.createTables();
                instance.loadInitialData();
            }
        }
        return instance;
    }
} 