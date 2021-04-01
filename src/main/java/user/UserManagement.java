package user;

import server.utils.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagement {

    private final static String SELECT_ALL_USERS = "SELECT * FROM cloudstorage.users;";
    private final static String SELECT_LAST_USER = "SELECT * FROM cloudstorage.users WHERE userid = " +
                                                    "(SELECT MAX (userid) FROM cloudstorage.users);";
    private final static String SELECT_USER_BY_ID = "SELECT * FROM cloudstorage.users WHERE userid = ?;";

    private final static String INSERT_USER = "INSERT INTO cloudstorage.users VALUES " +
                                                "(DEFAULT, ?, ?, NOW(), ?, ?);";
    private final static String CHANGE_NAME = "UPDATE cloudstorage.users SET username = ? WHERE userid = ?";
    private final static String CHANGE_PASSWORD = "UPDATE cloudstorage.users SET userpassword = ? WHERE userid = ?";

    private final static String ID_FIELD = "userid";
    private final static String NAME_FIELD = "username";
    private final static String PASSWORD_FIELD = "userpassword";
    private final static String DATEADD_FIELD = "dateadd";
    private final static String ROOTDIR_FIELD = "rootdir";
    private final static String RIGHTS_FIELD = "userrights";

    private static final List<User> users = new ArrayList<>();

    public static void readAllUsers () {

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
                String rights = rs.getString(RIGHTS_FIELD);
                System.out.printf( "User = %s , Password = %s, Date Added = %s ", name, password, dateAdd.toString() );
                User user = new User(id, name, password, dateAdd, rootDir, rights);
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

    public static User createNewUser (String name, String password, String rootDir) {

        User user = null;

        Connection conn = new DatabaseConnector().getConnection();
        if (!exists(name)) {
            Statement stmt = null;
            try {
                conn.setAutoCommit(true);
                PreparedStatement pst = conn.prepareStatement(INSERT_USER);
                pst.setString(1, name);
                pst.setString(2, password);
                pst.setString(3, rootDir);
                pst.setString(4, "r+");
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    stmt = conn.createStatement();
                    try (ResultSet rs = stmt.executeQuery(SELECT_LAST_USER)) {
                        if (rs.next()) {
                            user = new User(
                                    rs.getInt(ID_FIELD),
                                    rs.getString(NAME_FIELD),
                                    rs.getString(PASSWORD_FIELD),
                                    rs.getDate(DATEADD_FIELD),
                                    rs.getString(ROOTDIR_FIELD),
                                    rs.getString(RIGHTS_FIELD));
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
        return user;
    }

    public static boolean changeUserName (String name, User user) {

        Connection conn = new DatabaseConnector().getConnection();
        if (!exists(name)) {
            Statement stmt = null;
            try {
                conn.setAutoCommit(true);
                PreparedStatement pst = conn.prepareStatement(CHANGE_NAME);
                pst.setString(1, name);
                pst.setInt(2, user.getUserId());;
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    System.out.print("user " + user.getName() + " changed name to ");
                    user.setName(name);
                    System.out.println(name + " successfully ..");
                    conn.close();
                    return true;
                }
                conn.close();
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
        }
        return false;
    }

    public static boolean changeUserPassword(String password, User user) {

        Connection conn = new DatabaseConnector().getConnection();
        try {
            conn.setAutoCommit(true);
            PreparedStatement pst = conn.prepareStatement(CHANGE_PASSWORD);
            pst.setString(1, password);
            pst.setInt(2, user.getUserId());
            int rows = pst.executeUpdate();
            if (rows > 0) {
                user.setPassword(password);
                System.out.println(" Password for user " + user.getName() +
                        " changed successfully ..");
                conn.close();
                return true;
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
            if (user.getName().equals(name)) return true;
        }
        return false;
    }

    public static boolean checkPassword (String name, String password) {
        for (User user : users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) return true;
        }
        return false;
    }

    public static User getUser (String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public static User getUser (int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    public static void main(String[] args) throws SQLException {    //тестируем

        //UserManagement.readAllUsers();
        User user = UserManagement.createNewUser("Lola", "5555", "Lola");
        System.out.println(UserManagement.checkPassword("Sasha", "2222"));
        System.out.println(UserManagement.checkPassword("Sasha", "3333"));
        changeUserName("Vova", user);

    }

}
