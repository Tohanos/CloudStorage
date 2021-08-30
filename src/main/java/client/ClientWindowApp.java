package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientWindowApp {

    JFrame mainWindowFrame;

    Client client;

    public ClientWindowApp(Client client) {
        this.client = client;
    }

    public void run() {

        if (mainWindowFrame == null) {
            mainWindowFrame = new JFrame("Cloud Storage");
            mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainWindowFrame.setSize(500, 300);  //<-Magic number
            mainWindowFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    client.setState(Client.ClientState.CLOSE);
                    super.windowClosing(e);
                }
            });

            JList<String> serverFilesList = new JList<>(client.getFileList().toArray(new String[0]));
            JList<String> clientFilesList = new JList<>(client.getLocalFileList().toArray(new String[0]));

            JScrollPane serverFilesPane = new JScrollPane(serverFilesList);
            JScrollPane clientFilesPane = new JScrollPane(clientFilesList);

            JTextField createDirField = new JTextField();
            createDirField.setColumns(10);  //<-Magic number

            JButton uploadButton = new JButton("Upload");
            JButton downloadButton = new JButton("Download");
            JButton removeButton = new JButton("Remove");
            JButton createDirButton = new JButton("Create Dir");
            JButton enterDirButton = new JButton("Enter Dir");
            JButton changeNamePassButton = new JButton("Change name/password");

            JPanel filesPanel = new JPanel();
            JPanel buttonPanel = new JPanel();
            JPanel createDirPanel = new JPanel();
            JPanel controlPanel = new JPanel();

            buttonPanel.add(removeButton, FlowLayout.LEFT);
            buttonPanel.add(downloadButton, FlowLayout.LEFT);
            buttonPanel.add(uploadButton, FlowLayout.LEFT);
            buttonPanel.add(changeNamePassButton, FlowLayout.LEFT);


            filesPanel.add(serverFilesPane, BoxLayout.X_AXIS);
            filesPanel.add(clientFilesPane, BoxLayout.X_AXIS);

            createDirPanel.add(enterDirButton, FlowLayout.LEFT);
            createDirPanel.add(createDirField, FlowLayout.LEFT);
            createDirPanel.add(createDirButton, FlowLayout.LEFT);


            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.add(createDirPanel);
            controlPanel.add(buttonPanel);

            serverFilesPane.setPreferredSize(new Dimension(150, 200));  //<-Magic number
            clientFilesPane.setPreferredSize(new Dimension(150, 200));

            serverFilesList.addListSelectionListener(e -> clientFilesList.clearSelection());
            clientFilesList.addListSelectionListener(e -> serverFilesList.clearSelection());

            mainWindowFrame.getContentPane().add(filesPanel, BorderLayout.CENTER);

            mainWindowFrame.getContentPane().add(BorderLayout.SOUTH, controlPanel);

            uploadButton.addActionListener(a -> {
                if (clientFilesList.getSelectedValue() != null) {
                    uploadButton.setEnabled(false);
                    System.out.println(client.sendFile(clientFilesList.getSelectedValue()));
                    serverFilesList.setListData(client.getFileList().toArray(new String[0]));
                    uploadButton.setEnabled(true);
                }
            });

            downloadButton.addActionListener(a -> {
                if (serverFilesList.getSelectedValue() != null) {
                    downloadButton.setEnabled(false);
                    System.out.println(client.downloadFile(serverFilesList.getSelectedValue()));
                    clientFilesList.setListData(client.getLocalFileList().toArray(new String[0]));
                    downloadButton.setEnabled(true);
                }
            });

            removeButton.addActionListener(a -> {
                if (serverFilesList.getSelectedValue() != null) {
                    System.out.println(client.removeFile(serverFilesList.getSelectedValue()));
                    serverFilesList.setListData(client.getFileList().toArray(new String[0]));
                } else if (clientFilesList.getSelectedValue() != null) {
                    client.removeLocalFile(clientFilesList.getSelectedValue());
                    clientFilesList.setListData(client.getLocalFileList().toArray(new String[0]));
                }

            });

            createDirButton.addActionListener(a -> {
                if (createDirField.getText() != null) {
                    System.out.println(client.createDir(createDirField.getText()));
                    serverFilesList.setListData(client.getFileList().toArray(new String[0]));
                }
            });

            enterDirButton.addActionListener(a -> {
                if (serverFilesList.getSelectedValue() != null) {
                    System.out.println(client.changeDir(serverFilesList.getSelectedValue()));
                    serverFilesList.setListData(client.getFileList().toArray(new String[0]));
                }
            });

            changeNamePassButton.addActionListener(a -> {
                client.setState(Client.ClientState.CHANGE_NAME_PASS);
                mainWindowFrame.setVisible(false);
            });

        }

        if (!mainWindowFrame.isVisible()) mainWindowFrame.setVisible(true);

    }
}
