package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					out.writeUTF("disconnect");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
				super.windowClosing(e);
			}
		});

		//JTextArea ta = new JTextArea();


		JList<String> list = new JList<>(getFileList().toArray(new String[0]));


		JButton uploadButton = new JButton("Upload");
		JButton downloadButton = new JButton("Download");
		JButton removeButton = new JButton("Remove");

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(removeButton, FlowLayout.LEFT);
		buttonPanel.add(downloadButton, FlowLayout.LEFT);
		buttonPanel.add(uploadButton, FlowLayout.LEFT);

		frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
		frame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(list.getSelectedValue()));
		});

		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(list.getSelectedValue()));
		});

		removeButton.addActionListener(a -> {
			System.out.println(removeFile(list.getSelectedValue()));
		});



	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
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
				out.writeUTF("download");
				out.writeUTF(filename);
				out.flush();
				long length = in.readLong();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[256];
				for (int i = 0; i < (length + 255) / 256; i++) {
					int read = in.read(buffer);
					fos.write(buffer, 0, read);
				}
				fos.close();
				String status = in.readUTF();
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
			out.writeUTF("remove");
			out.writeUTF(filename);
			out.flush();
			String status = in.readUTF();
			return status;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	private ArrayList<String> getFileList () {
		ArrayList<String> fileList = new ArrayList<>();
		try {
			out.writeUTF("filelist");
			long fileNumber = in.readLong();
			for (int i = 0; i < fileNumber; i++) {
				fileList.add(in.readUTF());
			}
			return fileList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static void main(String[] args) throws IOException {
		new Client();
	}
}
