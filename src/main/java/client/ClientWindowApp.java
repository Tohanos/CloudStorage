package client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

            JScrollPane serverFilesPane = new JScrollPane(serverFilesList);
            JScrollPane clientFilesPane = new JScrollPane(clientFilesList);



            JButton uploadButton = new JButton("Upload");
            JButton downloadButton = new JButton("Download");
            JButton removeButton = new JButton("Remove");
            JButton createDirButton = new JButton("Create Dir");

            JPanel filesPanel = new JPanel();
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(removeButton, FlowLayout.LEFT);
            buttonPanel.add(downloadButton, FlowLayout.LEFT);
            buttonPanel.add(uploadButton, FlowLayout.LEFT);
            buttonPanel.add(createDirButton, FlowLayout.LEFT);

            filesPanel.add(serverFilesPane, BoxLayout.X_AXIS);
            filesPanel.add(clientFilesPane, BoxLayout.X_AXIS);

            serverFilesPane.setPreferredSize(new Dimension(150, 220));
            clientFilesPane.setPreferredSize(new Dimension(150, 220));

            serverFilesList.addListSelectionListener(e -> clientFilesList.clearSelection());
            clientFilesList.addListSelectionListener(e -> serverFilesList.clearSelection());

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

            createDirButton.addActionListener(a -> {
                System.out.println(client.removeFile(serverFilesList.getSelectedValue()));
            });
        }


    }
}
