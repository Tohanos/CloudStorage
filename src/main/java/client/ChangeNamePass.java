package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChangeNamePass {
    private Client client;

    JFrame namePassWindowFrame;

    public ChangeNamePass(Client client) {
        this.client = client;
    }

    public void run() {

        if (namePassWindowFrame == null) {
            namePassWindowFrame = new JFrame("Cloud Storage change name and password");
            namePassWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            namePassWindowFrame.setSize(400, 300);  //<-Magic number
            namePassWindowFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    client.setState(Client.ClientState.WORK);

                }
            });

            JLabel message = new JLabel("Enter new login and/o password");
            JPanel mainPanel = new JPanel();
            JLabel newNameText = new JLabel("New name:");
            JLabel newPasswordText = new JLabel("New Password:");
            JTextField newNameField = new JTextField("", 15);   //<-Magic number
            JPasswordField newPassField = new JPasswordField("", 15);   //<-Magic number

            SpringLayout layout = new SpringLayout();
            mainPanel.setLayout(layout);
            mainPanel.add(newNameField);
            mainPanel.add(newNameText);
            mainPanel.add(newPassField);
            mainPanel.add(newPasswordText);

            layout.getConstraints(newNameText).setX(Spring.constant(5));    //<-Magic number
            layout.getConstraints(newNameText).setY(Spring.constant(35));
            layout.getConstraints(newNameField).setX(Spring.constant(105));
            layout.getConstraints(newNameField).setY(Spring.constant(35));
            layout.getConstraints(newPasswordText).setX(Spring.constant(5));
            layout.getConstraints(newPasswordText).setY(Spring.constant(65));
            layout.getConstraints(newPassField).setX(Spring.constant(105));
            layout.getConstraints(newPassField).setY(Spring.constant(65));

            namePassWindowFrame.getContentPane().add(BorderLayout.NORTH, message);
            namePassWindowFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);

            JButton changeNameButton = new JButton("Change user name");
            JButton changePassButton = new JButton("Change user password");
            JButton exitButton = new JButton("Exit");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(changeNameButton);
            buttonPanel.add(changePassButton);
            buttonPanel.add(exitButton);
            namePassWindowFrame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

            namePassWindowFrame.setVisible(true);

            changeNameButton.addActionListener(a -> {
                String name = newNameField.getText();
                if (name.length() <= 0) {
                    message.setText("Name cannot be blank!");
                } else {
                    int result = client.changeName(name);
                    if (result != 0) {
                        message.setText("User name changed to " + name + " successfully");
                    } else {
                        message.setText("User with this name exists!!!");
                    }
                }

            });

            changePassButton.addActionListener(a -> {
                String password = String.valueOf(newPassField.getPassword());
                if (password.length() <= 0) {
                    message.setText("Password cannot be blank!");
                } else {
                    int result = client.changePass(password);
                    if (result != 0) {
                        message.setText("User password changed successfully");
                    } else  {
                        message.setText("Invalid password!!!");
                    }
                }
            });

            exitButton.addActionListener(a -> {
                client.setState(Client.ClientState.WORK);
                namePassWindowFrame.dispose();
            });
        }

    }


}
