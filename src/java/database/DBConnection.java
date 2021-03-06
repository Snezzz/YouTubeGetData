package database;

import java.sql.*;

public class DBConnection {
    public Connection c;
    Statement stmt;
    public void makeConnection(String db) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5441/"+db, "postgres", "admin");
            c.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stmt = c.createStatement(); //открываем соединение
    }

    public ResultSet makeQuery(String sql) throws SQLException {
        return stmt.executeQuery(sql);
    }

}
