package fr.asynconf.bot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    
    private final Connection connection;
    
    public DBConnector() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:db.sqlite");
        init();
    }
    
    private void init() throws SQLException {
        this.connection
                .prepareStatement("""
                        CREATE TABLE IF NOT EXISTS submissions(
                            id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
                            user_id INTEGER(255) NOT NULL,
                            repl_link VARCHAR(255) NOT NULL,
                            state VARCHAR(255) NOT NULL DEFAULT 'pending',
                            message_id VARCHAR(255) UNIQUE DEFAULT ''
                        );
                        """)
                .execute();
    }
    
    public void disconnect() throws SQLException {
        this.connection.close();
    }
    
    public Connection getConnection() {
        return connection;
    }
}
