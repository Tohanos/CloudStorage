package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	public Client() throws IOException {
		socket = new Socket("localhost", 1234);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		
		// создание списка файлов
		JList<String> list = new JList<>();
		DefaultListModel<String> myModel = new DefaultListModel<>();
		list.setModel(myModel);

		JTextArea ta = new JTextArea();
		// TODO: 02.03.2021
		// list file - JList
		JButton uploadButton = new JButton("Upload");

		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
		frame.getContentPane().add(BorderLayout.SOUTH, uploadButton);
		
		fillList(myModel);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
		});
	}

	private void fillList(DefaultListModel<String> myModel) {
		java.util.List<String> list = downloadFileList();
		myModel.clear();
		for (String filename : list) {
			myModel.addElement(filename);
		}
	}

	private List<String> downloadFileList() {
		List<String> list = new ArrayList<>();
		try {
			StringBuilder sb = new StringBuilder();
			out.write("list-files".getBytes(StandardCharsets.UTF_8));
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if (sb.toString().endsWith("end")) {
					break;
				}
			}
			String fileString = sb.substring(0, sb.toString().length() - 4);
			list = Arrays.asList(fileString.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
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

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
