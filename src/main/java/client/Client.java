package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
	private final Socket commandSocket;
	private final Socket dataSocket;
	private final DataInputStream commandInputStream;
	private final DataOutputStream commandOutputStream;
	private final ObjectInputStream dataInputStream;
	private final ObjectOutputStream dataOutputStream;

	private String userName;
	private String password;
	private int userId;

	enum ClientState {
		AUTH,
		WORK,
		CLOSE
	}

	ClientState state;
	JFrame authWindowFrame;
	JFrame mainWindowFrame;

	public Client() throws IOException, ClassNotFoundException {
		state = ClientState.AUTH;

		commandSocket = new Socket("localhost", 1234);
		dataSocket = new Socket("localhost", 1235);
		commandInputStream = new DataInputStream(commandSocket.getInputStream());
		commandOutputStream = new DataOutputStream(commandSocket.getOutputStream());
		dataInputStream = new ObjectInputStream(dataSocket.getInputStream());
		dataOutputStream = new ObjectOutputStream(dataSocket.getOutputStream());

		while (state != ClientState.CLOSE) {
			switch (state) {
				case AUTH -> {
					runAuthorization();
				}
				case WORK -> {
					runClient();
				}
			}
		}
		commandOutputStream.writeUTF("disconnect");
		try {
			commandSocket.close();
			dataSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private void runAuthorization() {
		if (authWindowFrame == null) {
			authWindowFrame = new JFrame("Cloud Storage Authorization");
			authWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			authWindowFrame.setSize(400, 300);
			authWindowFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					state = ClientState.CLOSE;
					super.windowClosing(e);
				}
			});

			JLabel message = new JLabel("Enter login and password");
			JPanel mainPanel = new JPanel();
			JLabel loginText = new JLabel("Login:");
			JLabel passwordText = new JLabel("Password:");
			JTextField loginField = new JTextField("", 15);
			JPasswordField passwordField = new JPasswordField("", 15);

//			loginField.setSize(100, 15);
//			passwordField.setSize(100, 15);

			SpringLayout layout = new SpringLayout();
			mainPanel.setLayout(layout);
			mainPanel.add(loginField);
			mainPanel.add(loginText);
			mainPanel.add(passwordField);
			mainPanel.add(passwordText);

			layout.getConstraints(loginText).setX(Spring.constant(5));
			layout.getConstraints(loginText).setY(Spring.constant(5));
			layout.getConstraints(loginField).setX(Spring.constant(105));
			layout.getConstraints(loginField).setY(Spring.constant(5));
			layout.getConstraints(passwordText).setX(Spring.constant(5));
			layout.getConstraints(passwordText).setY(Spring.constant(25));
			layout.getConstraints(passwordField).setX(Spring.constant(105));
			layout.getConstraints(passwordField).setY(Spring.constant(25));

			authWindowFrame.getContentPane().add(BorderLayout.NORTH, message);
			authWindowFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);



			JButton newUserButton = new JButton("New user");
			JButton loginButton = new JButton("Login");
			JButton exitButton = new JButton("Exit");

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(newUserButton);
			buttonPanel.add(loginButton);
			buttonPanel.add(exitButton);
			authWindowFrame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

			authWindowFrame.setVisible(true);

			newUserButton.addActionListener(a -> {

			});

			loginButton.addActionListener(a -> {
				String login = loginField.getText();
				String password = String.valueOf(passwordField.getPassword());
				if (login.length() <= 0 || password.length() <= 0) {
					message.setText("Login or password cannot be blank!");
				} else {
					int result = authorize(login, password);
					if (result != 0) {
						userId = result;
						userName = loginField.getText();
					} else {
						message.setText("Invalid password!!!");
					}
				}
			});

			exitButton.addActionListener(a -> {
				state = ClientState.CLOSE;
			});
		}

	}

	private void runClient() {

		if (mainWindowFrame == null) {
			mainWindowFrame = new JFrame("Cloud Storage");
			mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainWindowFrame.setSize(400, 300);
			mainWindowFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					try {
						commandOutputStream.writeUTF("disconnect");
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
					super.windowClosing(e);
				}
			});

			//JTextArea ta = new JTextArea();


			JList<String> serverFilesList = new JList<>(getFileList().toArray(new String[0]));
			JList<String> clientFilesList = new JList<>(getLocalFileList().toArray(new String[0]));


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
				System.out.println(sendFile(clientFilesList.getSelectedValue()));
				serverFilesList.setListData(getFileList().toArray(new String[0]));
			});

			downloadButton.addActionListener(a -> {
				System.out.println(downloadFile(serverFilesList.getSelectedValue()));
				clientFilesList.setListData(getFileList().toArray(new String[0]));
			});

			removeButton.addActionListener(a -> {
				System.out.println(removeFile(serverFilesList.getSelectedValue()));
				serverFilesList.setListData(getFileList().toArray(new String[0]));
			});
		}


	}

	private int authorize (String name, String password) {
		String status = "";
		try {
			commandOutputStream.writeUTF("auth " + name + " " + password);
			status = commandInputStream.readUTF();
			if (!status.equals("DECLINE")) {
				return 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Integer.parseInt(status);
	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				commandOutputStream.writeUTF("upload");
				commandOutputStream.writeUTF(filename);
				long length = file.length();
				commandOutputStream.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					commandOutputStream.write(buffer, 0, read);
				}
				commandOutputStream.flush();
				String status = commandInputStream.readUTF();
				return status;
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String downloadFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if(!file.exists()) {
				commandOutputStream.writeUTF("download");
				commandOutputStream.writeUTF(filename);
				commandOutputStream.flush();
				long length = commandInputStream.readLong();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[256];
				for (int i = 0; i < (length + 255) / 256; i++) {
					int read = commandInputStream.read(buffer);
					fos.write(buffer, 0, read);
				}
				fos.close();
				String status = commandInputStream.readUTF();
				return status;
			} else {
				return "File exists!";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	private String removeFile(String filename) {
		try {
			commandOutputStream.writeUTF("remove");
			commandOutputStream.writeUTF(filename);
			commandOutputStream.flush();
			String status = commandInputStream.readUTF();
			return status;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	private ArrayList<String> getFileList () {
		ArrayList<String> fileList = new ArrayList<>();
		try {
			commandOutputStream.writeUTF("filelist");
			long fileNumber = commandInputStream.readLong();
			for (int i = 0; i < fileNumber; i++) {
				fileList.add(commandInputStream.readUTF());
			}
			return fileList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<String> getLocalFileList() {
		try {
			File file = new File("client" + File.separator);
			String[] fileNames = file.list();
			return new ArrayList<>(Arrays.asList(fileNames));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new Client();
	}
}
