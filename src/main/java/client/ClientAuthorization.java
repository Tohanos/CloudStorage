package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientAuthorization {

    private Client client;

    JFrame authWindowFrame;

    public ClientAuthorization(Client client) {
        this.client = client;
    }

    public void run() {

        if (authWindowFrame == null) {
            authWindowFrame = new JFrame("Cloud Storage Authorization");
            authWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            authWindowFrame.setSize(400, 300);
            authWindowFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    client.setState(Client.ClientState.CLOSE);
                    super.windowClosing(e);
                }
            });

            JLabel message = new JLabel("Enter login and password");
            JPanel mainPanel = new JPanel();
            JLabel loginText = new JLabel("Login:");
            JLabel passwordText = new JLabel("Password:");
            JLabel newPasswordText = new JLabel("New password");
            JTextField loginField = new JTextField("", 15);
            JPasswordField passwordField = new JPasswordField("", 15);
            JPasswordField newPasswordField = new JPasswordField("", 15);

//			loginField.setSize(100, 15);
//			passwordField.setSize(100, 15);

            SpringLayout layout = new SpringLayout();
            mainPanel.setLayout(layout);
            mainPanel.add(loginField);
            mainPanel.add(loginText);
            mainPanel.add(passwordField);
            mainPanel.add(passwordText);
            mainPanel.add(newPasswordField);
            mainPanel.add(newPasswordText);

            layout.getConstraints(loginText).setX(Spring.constant(5));
            layout.getConstraints(loginText).setY(Spring.constant(5));
            layout.getConstraints(loginField).setX(Spring.constant(105));
            layout.getConstraints(loginField).setY(Spring.constant(5));
            layout.getConstraints(passwordText).setX(Spring.constant(5));
            layout.getConstraints(passwordText).setY(Spring.constant(25));
            layout.getConstraints(passwordField).setX(Spring.constant(105));
            layout.getConstraints(passwordField).setY(Spring.constant(25));
            layout.getConstraints(newPasswordField).setX(Spring.constant(5));
            layout.getConstraints(newPasswordField).setY(Spring.constant(45));
            layout.getConstraints(newPasswordText).setX(Spring.constant(105));
            layout.getConstraints(newPasswordText).setY(Spring.constant(45));

            authWindowFrame.getContentPane().add(BorderLayout.NORTH, message);
            authWindowFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);



            JButton newUserButton = new JButton("New user");
            JButton loginButton = new JButton("Login");
            JButton exitButton = new JButton("Exit");
            JButton changePassword = new JButton("Change pass");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(newUserButton);
            buttonPanel.add(loginButton);
            buttonPanel.add(exitButton);
            buttonPanel.add(changePassword);
            authWindowFrame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

            authWindowFrame.setVisible(true);

            newUserButton.addActionListener(a -> {
                String login = loginField.getText();
                String password = String.valueOf(passwordField.getPassword());
                if (login.length() <= 0 || password.length() <= 0) {
                    message.setText("Login or password cannot be blank!");
                } else {
                    int result = client.createNewUser (login, password);
                    if (result != 0) {
                        authWindowFrame.setVisible(false);
                        authWindowFrame.dispose();
                    } else {
                        message.setText("User exists!!!");
                    }
                }

            });

            loginButton.addActionListener(a -> {
                String login = loginField.getText();
                String password = String.valueOf(passwordField.getPassword());
                if (login.length() <= 0 || password.length() <= 0) {
                    message.setText("Login or password cannot be blank!");
                } else {
                    int result = client.authorize(login, password);
                    if (result != 0 && result != -1) {
                        authWindowFrame.setVisible(false);
                        authWindowFrame.dispose();
                    }
                    if (result == 0) {
                        message.setText("Invalid username or password!!!");
                    }
                    if (result == -1) {
                        message.setText("User is already logged in!!!");
                    }
                }
            });

            changePassword.addActionListener(a -> {
                String login = loginField.getText();
                String password = String.valueOf(passwordField.getPassword());
                String newPassword = String.valueOf(newPasswordField.getPassword());

                if (login.length() <= 0 || password.length() <= 0) {
                    message.setText("Login or password cannot be blank!");
                } else {
                    if (newPassword.length() <= 0) {
                        message.setText("New password filed cannot be blank!");
                    } else {
                        int result = client.authorize(login, password);
                        if (result != 0 && result != -1) {
                            authWindowFrame.setVisible(false);
                            authWindowFrame.dispose();
                        }
                        if (result == 0) {
                            message.setText("Invalid username or password!!!");
                        }
                        if (result == -1) {
                            message.setText("User is already logged in!!!");
                        }
                    }
                }
            });

            exitButton.addActionListener(a -> {
                client.setState(Client.ClientState.CLOSE);
                authWindowFrame.dispose();
            });
        }

    }

}
