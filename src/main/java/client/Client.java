package client;

import command.Command;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
	private final Socket commandSocket;
	private final Socket dataSocket;
	private final ObjectInputStream commandInputStream;
	private final ObjectOutputStream commandOutputStream;
	private final ObjectInputStream dataInputStream;
	private final ObjectOutputStream dataOutputStream;


	enum ClientState {
		AUTH,
		WORK,
		CLOSE
	}

	ClientState state;

	public Client() throws IOException {
		state = ClientState.AUTH;

		commandSocket = new Socket("localhost", 1234);
		dataSocket = new Socket("localhost", 1235);
		commandInputStream = new ObjectInputStream(commandSocket.getInputStream());
		commandOutputStream = new ObjectOutputStream(commandSocket.getOutputStream());
		dataInputStream = new ObjectInputStream(dataSocket.getInputStream());
		dataOutputStream = new ObjectOutputStream(dataSocket.getOutputStream());

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
				commandOutputStream.writeObject(new Command("upload "+ filename));
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
				commandOutputStream.writeObject(new Command("download " + filename));
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
			commandOutputStream.writeObject(new Command("remove " + filename));
			commandOutputStream.flush();
			Command status = (Command) commandInputStream.readObject();
			return status.getCommand().get(0);

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	public ArrayList<String> getFileList () {
		try {
			commandOutputStream.writeObject(new Command("filelist"));
			Command answer = (Command) commandInputStream.readObject();

			return (ArrayList<String>) answer.getCommand();
		} catch (IOException | ClassNotFoundException e) {
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
		Command status = null;
		try {
			commandOutputStream.writeObject(new Command("auth " + name + " " + password));
			status = (Command) commandInputStream.readObject();
			if (!status.getCommand().get(0).equals("DECLINE")) {
				return 0;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return status != null?
				Integer.parseInt(status.getCommand().get(0)):
				0;
	}

	public void setState(ClientState state) {
		this.state = state;
	}

	public static void main(String[] args) throws IOException{
		new Client();
	}
}
