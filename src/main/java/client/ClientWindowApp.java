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
            mainWindowFrame.setSize(400, 300);
            mainWindowFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    client.setState(Client.ClientState.CLOSE);
                    super.windowClosing(e);
                }
            });

            //JTextArea ta = new JTextArea();


            JList<String> serverFilesList = new JList<>(client.getFileList().toArray(new String[0]));
            JList<String> clientFilesList = new JList<>(client.getLocalFileList().toArray(new String[0]));


            JButton uploadButton = new JButton("Upload");
            JButton downloadButton = new JButton("Download");
            JButton removeButton = new JButton("Remove");

            JPanel filesPanel = new JPanel();
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(removeButton, FlowLayout.LEFT);
            buttonPanel.add(downloadButton, FlowLayout.LEFT);
            buttonPanel.add(uploadButton, FlowLayout.LEFT);

            filesPanel.add(new JScrollPane(serverFilesList), BoxLayout.X_AXIS);
            filesPanel.add(new JScrollPane(clientFilesList), BoxLayout.X_AXIS);

            mainWindowFrame.getContentPane().add(filesPanel, BorderLayout.CENTER);

            mainWindowFrame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

            mainWindowFrame.setVisible(true);

            uploadButton.addActionListener(a -> {
                System.out.println(client.sendFile(clientFilesList.getSelectedValue()));
                serverFilesList.setListData(client.getFileList().toArray(new String[0]));
            });

            downloadButton.addActionListener(a -> {
                System.out.println(client.downloadFile(serverFilesList.getSelectedValue()));
                clientFilesList.setListData(client.getFileList().toArray(new String[0]));
            });

            removeButton.addActionListener(a -> {
                System.out.println(client.removeFile(serverFilesList.getSelectedValue()));
                serverFilesList.setListData(client.getFileList().toArray(new String[0]));
            });
        }


    }
}
