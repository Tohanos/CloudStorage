package server;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
		System.out.println("Client "+ socket.getInetAddress().toString() + " connected");
	}

	@Override
	public void run() {
		try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		     DataInputStream in = new DataInputStream(socket.getInputStream())){
			while (true) {
				String command = in.readUTF();
				System.out.println("Incomming command: " + command);
				if ("upload".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());
						if (!file.exists()) {
							file.createNewFile();
						}
						long size = in.readLong();
						FileOutputStream fos = new FileOutputStream(file);
						byte[] buffer = new byte[256];
						for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						fos.close();
						out.writeUTF("DONE");
					} catch (Exception e) {
						out.writeUTF("ERROR");
					}
				} else if ("download".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());
						if (file.exists()) {
							out.writeLong(file.length());
							long length = file.length();
							out.writeLong(length);
							FileInputStream fis = new FileInputStream(file);
							int read = 0;
							byte[] buffer = new byte[256];
							while ((read = fis.read(buffer)) != -1) {
								out.write(buffer, 0, read);
							}
							out.writeUTF("DONE");
							out.flush();
						} else {
							out.writeUTF("File does not exist");
						}
					} catch (Exception e) {
						out.writeUTF("ERROR");
					}

				} else if ("remove".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());
						if (file.exists()) {
							if (file.delete()) {
								out.writeUTF("DONE");
							}
						} else {
							out.writeUTF("ERROR");
						}
					} catch (Exception e) {
						out.writeUTF("ERROR");
					}

				} else if ("disconnect".equals(command)) {
					return;
				} else if ("filelist".equals(command)) {
					try {
						File file = new File("server" + File.separator);
						String[] fileNames = file.list();
						out.writeLong(fileNames.length);
						for (String filename :
								fileNames) {
							out.writeUTF(filename);
						}
						out.flush();
					} catch (Exception e) {
						out.writeUTF("ERROR");
					}
				}


			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
