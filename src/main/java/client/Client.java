package client;

import command.Command;
import fileassembler.FileChunk;
import fileassembler.MachineType;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
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
	private int chunkSize;
	private MachineType machineType = MachineType.CLIENT;
	private String currentServerDir;
	private String currentClientDir;


	enum ClientState {
		AUTH,
		INIT,
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
		currentClientDir = "client";
		currentServerDir = "";

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
				case INIT -> {
					readChunkSize();
//					sendFileChunk(new FileChunk(userId, 2, 0, true, "", ));
					state = ClientState.WORK;
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

	private void sendFileChunk (FileChunk chunk) throws IOException {
		dataOutputStream.writeInt(chunk.getUserId());
		dataOutputStream.writeInt(chunk.getSize());
		dataOutputStream.writeInt(chunk.getPosition());
		dataOutputStream.writeBoolean(chunk.isLast());
		dataOutputStream.writeUTF(chunk.getFilename());
		dataOutputStream.write(chunk.getBuffer(), 0, chunk.getBuffer().length);
		dataOutputStream.flush();
	}

	private FileChunk recieveFileChunk () throws IOException {
		int userId = dataInputStream.readInt();
		int size = dataInputStream.readInt();
		int position = dataInputStream.readInt();
		boolean isLast = dataInputStream.readBoolean();
		short fileNameLength = dataInputStream.readShort();
		byte[] buf = new byte[fileNameLength];
		dataInputStream.read(buf, 0, fileNameLength);
		String filename = new String(buf);
		buf = new byte[size];
		dataInputStream.read(buf, 0, size);
		return new FileChunk(userId, size, position, isLast, filename, buf);
	}

	private void readChunkSize() {
		try {
			commandOutputStream.writeUTF("chunksize");
			commandOutputStream.flush();

			Command answer = commandReceive();

			chunkSize = Integer.parseInt(answer.getCommand().get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String sendFile(String filename) {
		String serverAnswer = "";
		int position = 0;
		boolean last = false;
		try {
			File file = new File(currentClientDir + File.separator + filename);
			if (file.exists()) {

				commandOutputStream.writeUTF("upload");
				commandOutputStream.flush();
				int bytesToSend = chunkSize - 15 - filename.length();
				FileInputStream fis = new FileInputStream(file);
				int chunkNumber = 0;
				while (!last) {
					Command answer = commandReceive();
					System.out.println(answer.getCommand().toString());
					if (answer.getCommand().get(0).equals("NEXT")) {
						int read = 0;
						byte[] buffer = new byte[bytesToSend];
						read = fis.read(buffer, position, bytesToSend);
						if (read < bytesToSend) last = true;
						FileChunk chunk = new FileChunk(userId, (int) file.length(), position, last, filename, buffer);
						sendFileChunk(chunk);
						position += bytesToSend;
						commandOutputStream.writeUTF(String.valueOf(chunkNumber));
						commandOutputStream.flush();
						chunkNumber++;
					} else return "ERROR";
				}
				Command answer = commandReceive();
				serverAnswer = answer.getCommand().get(0);
				return serverAnswer;
			} else {
				return "File does not exist";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	public String downloadFile(String filename) {
		String serverAnswer = "";
		try {
			File file = new File("client" + File.separator + filename);
			if(!file.exists()) {
				RandomAccessFile raf = new RandomAccessFile(currentClientDir + File.separator + filename, "rw");
				commandOutputStream.writeUTF("download " + filename);
				commandOutputStream.flush();

				Command answer = commandReceive();
				if (answer.getCommand().get(0).equals("READY")) {
					boolean last = false;
					while (!last) {
						commandOutputStream.writeUTF("NEXT");
						commandOutputStream.flush();
						FileChunk chunk = recieveFileChunk();
						raf.write(chunk.getBuffer(), chunk.getPosition(), chunk.getSize());
						last = chunk.isLast();
						answer = commandReceive();
						serverAnswer = answer.getCommand().get(0);
					}
					raf.close();
					return "DONE";
				}
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
			commandOutputStream.writeUTF("rm " + filename);
			commandOutputStream.flush();

			Command answer = commandReceive();

			return answer.getCommand().get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	public void removeLocalFile(String filename) {
		File file = new File(currentClientDir + File.separator + filename);
		file.delete();
	}

	public ArrayList<String> getFileList () {
		try {
			commandOutputStream.writeUTF("ls");

			Command answer = commandReceive();

			return new ArrayList<>(answer.getCommand());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getLocalFileList() {
		try {
			File file = new File(currentClientDir + File.separator);
			String[] fileNames = file.list();
			assert fileNames != null;
			return new ArrayList<>(Arrays.asList(fileNames));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int createDir (String dirName) {
		try {
			commandOutputStream.writeUTF("mkdir " + dirName);

			Command answer = commandReceive();

			if (answer.getCommand().get(0).equals("OK")) {
				return 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int changeDir (String dirName) {
		try {
			commandOutputStream.writeUTF("cd " + dirName);

			Command answer = commandReceive();

			if (answer.getCommand().get(0).equals("OK")) {
				return 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int authorize (String name, String password) {
		try {
			commandOutputStream.writeUTF("auth " + name + " " + password);
			commandOutputStream.flush();

			Command answer = commandReceive();

			if (answer.getCommand().get(0).equals("DECLINE")) {
				return 0;
			}
			if (answer.getCommand().get(0).equals("EXIST")) {
				return -1;
			}
			setUserName(name);
			setPassword(password);
			setUserId(Integer.parseInt(answer.getCommand().get(0)));
			state = ClientState.INIT;

			sendFileChunk(new FileChunk(userId, 0, 0, true, "", new byte[256]));

			return userId;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int createNewUser (String name, String password) {
		try {
			commandOutputStream.writeUTF("create " + name + " " + password);
			commandOutputStream.flush();

			Command answer = commandReceive();

			if (answer.getCommand().get(0).equals("EXISTS")) {
				return 0;
			}
			setUserName(name);
			setPassword(password);
			setUserId(Integer.parseInt(answer.getCommand().get(0)));
			state = ClientState.INIT;
			return userId;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private Command commandReceive () throws IOException {
		byte[] buf = new byte[10000];
		int num = commandInputStream.read(buf);
		String s = new String(buf, Charset.defaultCharset()).trim();
		return new Command(s);
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
