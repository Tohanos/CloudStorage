package user;

import java.sql.Date;

public class User {
    private int userId;
    private String name;
    private String password;
    private Date dateAdd;
    private String rootDir;
    //private userURL;

    public User(int userId, String name, String password, Date dateAdd, String rootDir) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.dateAdd = dateAdd;
        this.rootDir = rootDir;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Date getDateAdd() {
        return dateAdd;
    }

    public String getRootDir() {
        return rootDir;
    }

    public int getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
