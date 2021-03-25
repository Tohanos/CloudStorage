package client;

import command.Command;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
	private final Socket commandSocket;
	private final Socket dataSocket;
	private final DataInputStream commandInputStream;
	private final DataOutputStream commandOutputStream;
	private final DataInputStream dataInputStream;
	private final DataOutputStream dataOutputStream;

	private String userName;
	private String password;
	private int userId;


	enum ClientState {
		AUTH,
		WORK,
		CLOSE
	}

	enum TransmissionPhase {
		SENDING,
		RECIEVING
	}

	ClientState state;

	public Client() throws IOException {
		state = ClientState.AUTH;

		commandSocket = new Socket ("localhost", 1234);
		dataSocket = new Socket("localhost", 1235);
		commandInputStream = new DataInputStream(commandSocket.getInputStream());
		commandOutputStream = new DataOutputStream(commandSocket.getOutputStream());
		dataInputStream = new DataInputStream(dataSocket.getInputStream());
		dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());

		ClientAuthorization auth = new ClientAuthorization(this);
		ClientWindowApp app = new ClientWindowApp(this);

		while (state != ClientState.CLOSE) {
			switch (state) {
				case AUTH -> {
					auth.run();
				}
				case WORK -> {
					app.run();
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

	public String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				commandOutputStream.writeUTF("upload "+ filename);
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
				return "File does not exist";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	public String downloadFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if(!file.exists()) {
				commandOutputStream.writeUTF("download " + filename);
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

	public String removeFile(String filename) {
		try {
			commandOutputStream.writeUTF("remove " + filename);
			commandOutputStream.flush();
			Command status = new Command(commandInputStream.readUTF()) ;
			return status.getCommand().get(0);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	public ArrayList<String> getFileList () {
		try {
			commandOutputStream.writeUTF("filelist");
			Command answer = new Command(commandInputStream.readUTF());

			return (ArrayList<String>) answer.getCommand();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getLocalFileList() {
		try {
			File file = new File("client" + File.separator);
			String[] fileNames = file.list();
			return new ArrayList<>(Arrays.asList(fileNames));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int authorize (String name, String password) {
		try {
			String s = "";
			commandOutputStream.writeUTF("auth " + name + " " + password);
			commandOutputStream.flush();
			byte buf[] = new byte[100];
			int num = commandInputStream.read(buf);
			s = buf.toString();
			if (!s.equals("DECLINE")) {
				return 0;
			}
			setUserName(name);
			setPassword(password);
			setUserId(Integer.parseInt(s));
			return Integer.parseInt(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void setState(ClientState state) {
		this.state = state;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public static void main(String[] args) throws IOException{
		new Client();
	}
}
