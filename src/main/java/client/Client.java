package client;

import command.Command;
import server.utils.FileChunk;
import server.utils.MachineType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static client.utils.Utils.*;

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


	enum ClientState {		//состояние клиентского приложения
		AUTH,				//аутентификация
		INIT,				//инициализация
		WORK,				//работа
		CHANGE_NAME_PASS,	//Смена пароля и/или имени
		CLOSE				//завершение
	}

	enum TransmissionPhase {//фазы передачи данных
		SENDING,			//отправка
		RECIEVING			//получение
	}

	ClientState state;

	public Client() throws IOException {
		state = ClientState.AUTH;
		currentClientDir = "client";
		currentServerDir = "";

		//готовим сокеты для двух каналов
		commandSocket = new Socket ("localhost", 1234);
		dataSocket = new Socket("localhost", 1235);
		commandInputStream = new DataInputStream(commandSocket.getInputStream());
		commandOutputStream = new DataOutputStream(commandSocket.getOutputStream());
		dataInputStream = new DataInputStream(dataSocket.getInputStream());
		dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());


		//готовим формы
		ClientAuthorization auth = new ClientAuthorization(this);
		ClientWindowApp app = new ClientWindowApp(this);
		ChangeNamePass changeNamePass = new ChangeNamePass(this);

		//основной цикл
		while (state != ClientState.CLOSE) {
			switch (state) {
				case AUTH -> {
					auth.run();
				}
				case INIT -> {

					state = ClientState.WORK;
				}
				case WORK -> {
					app.run();
				}
				case CHANGE_NAME_PASS -> {
					changeNamePass.run();
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

	/***
	 * Чтение размера отрезка, заданного на сервере
	 */
	private void readChunkSize() {
		try {
			commandOutputStream.writeUTF("chunksize");
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

			chunkSize = Integer.parseInt(answer.getCommand().get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Отправка файла
 	 * @param filename
	 * @return
	 */
	public String sendFile(String filename) {
		String serverAnswer = "";
		int position = 0;
		boolean last = false;
		try {
			File file = new File(currentClientDir + File.separator + filename);
			if (file.exists()) {

				commandOutputStream.writeUTF("upload " + filename);
				commandOutputStream.flush();
				Command answer = commandReceive(commandInputStream);
				serverAnswer = answer.getCommand().get(0);
				if (serverAnswer.equals("READY")) {
					int bytesToSend = chunkSize - 17 - filename.length();
					byte[] buffer = new byte[bytesToSend];
					FileInputStream fis = new FileInputStream(file);
					int chunkNumber = 0;
					while (!last) {																			//нарезка на отрезки
						int read = 0;
						read = fis.read(buffer, 0, bytesToSend);
						last = read < bytesToSend;
						FileChunk chunk = new FileChunk(userId, read, position, last, filename, buffer);
						sendFileChunk(chunk, dataOutputStream);																//и отправка
						position += read;
						commandOutputStream.writeUTF(String.valueOf(chunkNumber));
						commandOutputStream.flush();
						answer = commandReceive(commandInputStream);
						serverAnswer = answer.getCommand().get(0);
						if (serverAnswer.equals("ERROR")) {
							System.out.println("Receiving file error");
							return serverAnswer;
						}
						chunkNumber++;
					}
					fis.close();
					commandOutputStream.writeUTF("uploaddone");
					commandOutputStream.flush();
					answer = commandReceive(commandInputStream);
					serverAnswer = answer.getCommand().get(0);
					System.out.println(serverAnswer);
					return serverAnswer;
				}
			} else {
				return "File does not exist";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	/***
	 * Скачивание файла
	 * @param filename
	 * @return
	 */
	public String downloadFile(String filename) {
		String serverAnswer = "";
		try {
			File file = new File(currentClientDir + File.separator + filename);
			if(!file.exists()) {
				RandomAccessFile raf = new RandomAccessFile(currentClientDir + File.separator + filename, "rw");
				commandOutputStream.writeUTF("download " + filename);
				commandOutputStream.flush();

				Command answer = commandReceive(commandInputStream);
				serverAnswer = answer.getCommand().get(0);
				System.out.println(serverAnswer);

				if (serverAnswer.equals("READY")) {
					boolean last = false;
					while (!last) {
						commandOutputStream.writeUTF("NEXT");
						commandOutputStream.flush();

						FileChunk chunk = recieveFileChunk(dataInputStream);					//скачивание каждого отрезка
						raf.write(chunk.getBuffer(), 0, chunk.getSize());	//и запись их в файл
						last = chunk.isLast();

						answer = commandReceive(commandInputStream);
						serverAnswer = answer.getCommand().get(0);
						System.out.println(serverAnswer);
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

	/***
	 * Удаление файла с сервера
	 * @param filename
	 * @return
	 */
	public String removeFile(String filename) {
		try {
			commandOutputStream.writeUTF("rm " + filename);
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

			return answer.getCommand().get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something went wrong!";
	}

	/***
	 * Удаление файла с клиента
	 * @param filename
	 */
	public void removeLocalFile(String filename) {
		File file = new File(currentClientDir + File.separator + filename);
		file.delete();
	}

	/***
	 * Получение списка файлов текущей директории репозитория подключенного пользователя
	 * @return
	 */
	public ArrayList<String> getFileList () {
		try {
			commandOutputStream.writeUTF("ls");
			commandOutputStream.flush();
			Command answer = commandReceive(commandInputStream);

			return new ArrayList<>(answer.getCommand());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * Получение списка файлов локальной текущей директории
	 * @return
	 */
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

	/***
	 * Создание директории на сервере
	 * @param dirName
	 * @return
	 */
	public int createDir (String dirName) {
		try {
			commandOutputStream.writeUTF("mkdir " + dirName);

			Command answer = commandReceive(commandInputStream);

			if (answer.getCommand().get(0).equals("OK")) {
				return 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/***
	 * Смена директории
	 * @param dirName
	 * @return
	 */
	public int changeDir (String dirName) {
		try {
			commandOutputStream.writeUTF("cd " + dirName);

			Command answer = commandReceive(commandInputStream);

			if (answer.getCommand().get(0).equals("OK")) {
				return 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/***
	 * Авторизация
	 * @param name
	 * @param password
	 * @return
	 */
	public int authorize (String name, String password) {
		try {
			commandOutputStream.writeUTF("auth " + name + " " + password);
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

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
			readChunkSize();

			sendFileChunk(new FileChunk(userId, 0, 0, true, "", new byte[chunkSize]), dataOutputStream);

			return userId;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/***
	 * Создание нового пользователя
	 * @param name
	 * @param password
	 * @return
	 */
	public int createNewUser (String name, String password) {
		try {
			commandOutputStream.writeUTF("create " + name + " " + password);
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

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

	public int changeName (String name) {
		try {
			commandOutputStream.writeUTF("name " + name);
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

			if (!answer.getCommand().get(0).equals("true")) {
				return 0;
			}
			setUserName(name);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public int changePass (String pass) {
		try {
			commandOutputStream.writeUTF("pass " + pass);
			commandOutputStream.flush();

			Command answer = commandReceive(commandInputStream);

			if (!answer.getCommand().get(0).equals("true")) {
				return 0;
			}
			setPassword(pass);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
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
