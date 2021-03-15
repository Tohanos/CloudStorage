package server;

import java.sql.*;

public class DatabaseConnector {
    //  Database credentials
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
    static final String USER = "postgres";
    static final String PASS = "postgres";

    private Connection connection;

    public DatabaseConnector() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
        }

        System.out.println("PostgreSQL JDBC Driver successfully connected");
        connection = null;

        try {
            connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static void main(String[] argv) throws SQLException {    //тестируем

        System.out.println("Testing connection to PostgreSQL JDBC");
        DatabaseConnector dbconnect = new DatabaseConnector();

        Statement stmt = null;

        try {
            Connection c = dbconnect.getConnection();
            //c.setAutoCommit(false);
            System.out.println("Successfully Connected.");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "select * from cloudstorage.users;" );
            while ( rs.next() ) {
                String name = rs.getString("username");
                String password = rs.getString("userpassword");
                Date dateAdd = rs.getDate("dateadd");
                System.out.printf( "User = %s , Password = %s, Date Added = %s ", name, password, dateAdd.toString() );
                System.out.println();
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        System.out.println(" Data Retrieved Successfully ..");
    }
}
