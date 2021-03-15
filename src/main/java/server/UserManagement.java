package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagement {

    private final static String SELECT_ALL_USERS = "SELECT * FROM cloudstorage.users;";
    private final static String SELECT_LAST_USER = "SELECT * FROM cloudstorage.users WHERE userid = " +
                                                    "(SELECT MAX (userid) FROM cloudstorage.users);";
    private final static String INSERT_USER = "INSERT INTO cloudstorage.users VALUES " +
                                                "(DEFAULT, ?, ?, NOW(), ?);";
    private final static String CHANGE_NAME = "UPDATE cloudstorage.users SET username = ? WHERE userid = ?";
    private final static String CHANGE_PASSWORD = "UPDATE cloudstorage.users SET userpassword = ? WHERE userid = ?";

    private final static String ID_FIELD = "userid";
    private final static String NAME_FIELD = "username";
    private final static String PASSWORD_FIELD = "userpassword";
    private final static String DATEADD_FIELD = "dateadd";
    private final static String ROOTDIR_FIELD = "rootdir";

    private static List<User> users;

    public static void readAllUsers () {

        if (users == null) {
            users = new ArrayList<>();
        }

        Connection conn = new DatabaseConnector().getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SELECT_ALL_USERS);
            while ( rs.next() ) {
                int id = rs.getInt(ID_FIELD);
                String name = rs.getString(NAME_FIELD);
                String password = rs.getString(PASSWORD_FIELD);
                Date dateAdd = rs.getDate(DATEADD_FIELD);
                String rootDir = rs.getString(ROOTDIR_FIELD);
                System.out.printf( "User = %s , Password = %s, Date Added = %s ", name, password, dateAdd.toString() );
                User user = new User(id, name, password, dateAdd, rootDir);
                users.add(user);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        System.out.println(" Data Retrieved Successfully ..");
    }

    public static void createNewUser (String name, String password, String rootDir) {

        Connection conn = new DatabaseConnector().getConnection();
        if (!exists(name)) {
            Statement stmt = null;
            try {
                conn.setAutoCommit(true);
                PreparedStatement pst = conn.prepareStatement(INSERT_USER);
                pst.setString(1, name);
                pst.setString(2, password);
                pst.setString(3, rootDir);
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    stmt = conn.createStatement();
                    try (ResultSet rs = stmt.executeQuery(SELECT_LAST_USER)) {
                        if (rs.next()) {
                            users.add(new User(
                                    rs.getInt(ID_FIELD),
                                    rs.getString(NAME_FIELD),
                                    rs.getString(PASSWORD_FIELD),
                                    rs.getDate(DATEADD_FIELD),
                                    rs.getString(ROOTDIR_FIELD)));
                        }
                    }
                }
                conn.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
            System.out.println(" User created Successfully ..");
        }

    }

    public static boolean changeUserName (String name, int id) {

        Connection conn = new DatabaseConnector().getConnection();
        if (!exists(name)) {
            Statement stmt = null;
            try {
                conn.setAutoCommit(true);
                PreparedStatement pst = conn.prepareStatement(CHANGE_NAME);
                pst.setString(1, name);
                pst.setString(2, String.valueOf(id));
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    stmt = conn.createStatement();
                    try (ResultSet rs = stmt.executeQuery(SELECT_LAST_USER)) {
                        if (rs.next()) {
                            for (User user : users) {
                                if (user.getUserId() == id) {
                                    System.out.print(" User " + user.getName() + " changed name to ");
                                    user.setName(name);
                                    System.out.println(name + " successfully ..");
                                    return true;
                                }
                            }
                        }
                    }
                }
                conn.close();
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
        }
        return false;
    }

    public static boolean changeUserPassword(String password, int id) {

        Connection conn = new DatabaseConnector().getConnection();
        Statement stmt = null;
        try {
            conn.setAutoCommit(true);
            PreparedStatement pst = conn.prepareStatement(CHANGE_PASSWORD);
            pst.setString(1, password);
            pst.setString(2, String.valueOf(id));
            int rows = pst.executeUpdate();
            if (rows > 0) {
                stmt = conn.createStatement();
                try (ResultSet rs = stmt.executeQuery(SELECT_LAST_USER)) {
                    if (rs.next()) {
                        for (User user : users) {
                            if (user.getUserId() == id) {
                                user.setPassword(password);
                                System.out.println(" Password for user " + user.getName() +
                                        " changed successfully ..");
                                return true;
                            }
                        }
                    }
                }
            }
            conn.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return false;
    }

    public static boolean exists (String name) {
        for (User user : users) {
            if (user.getName() == name) return true;
        }
        return false;
    }

    public static void main(String[] args) throws SQLException {

        UserManagement.readAllUsers();
        UserManagement.createNewUser("Petya", "3333", "/User3");

    }

}
