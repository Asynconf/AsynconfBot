/*
 * The Asynconf Bot is a bot with the purpose of managing the members and the 'tournoi' of the conference.
 *
 *     Asynconf Bot  Copyright (C) 2021  RedsTom
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
