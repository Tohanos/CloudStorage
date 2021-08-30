package client.utils;

import command.Command;
import server.utils.FileChunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class Utils {
    /***
     * Отправка отрезка файла
     * @param chunk - собственно сам отрезок
     * @param dataOutputStream
     * @throws IOException
     */
    public static void sendFileChunk(FileChunk chunk, DataOutputStream dataOutputStream) throws IOException {   //<- Нарушение DRY, т.к. функционал уже реализован для серверной части
        dataOutputStream.writeBytes("CH");
        dataOutputStream.writeInt(chunk.getUserId());
        dataOutputStream.writeInt(chunk.getSize());
        dataOutputStream.writeInt(chunk.getPosition());
        dataOutputStream.writeBoolean(chunk.isLast());
        dataOutputStream.writeUTF(chunk.getFilename());
        dataOutputStream.write(chunk.getBuffer(), 0, chunk.getBuffer().length);
        dataOutputStream.flush();
    }

    /***
     * Получение отрезка файла
     * @return	сам отрезок
     * @throws IOException
     */
    public static FileChunk recieveFileChunk (DataInputStream dataInputStream) throws IOException { //<- Нарушение DRY, т.к. функционал уже реализован для серверной части

        String header = new String(dataInputStream.readNBytes(2), Charset.defaultCharset());
        if (!header.equals("CH")) return null;															//если заголовок из стрима не подходит - не считываем дальше, но можем получить NullPointerException
        int userId = dataInputStream.readInt();
        int size = dataInputStream.readInt();
        int position = dataInputStream.readInt();
        boolean isLast = dataInputStream.readBoolean();
        short fileNameLength = dataInputStream.readShort();
        byte[] buf = new byte[fileNameLength];
        dataInputStream.read(buf, 0, fileNameLength);
        String filename = new String(buf);
        buf = new byte[size];
        dataInputStream.read(buf, 0, size);						//читаем тело отрезка
        if (isLast) {												//если отрезок последний, то, возможно, в его конце будет паддинг
            int bytesLeft = dataInputStream.available();			//узнаём количество оставшихся в стриме байт
            byte[] trash = new byte[bytesLeft];						//создаём буфер для них
            dataInputStream.read(trash);							//считываем
        }
        return new FileChunk(userId, size, position, isLast, filename, buf);
    }

    /***
     * Получение команды-ответа от сервера
     * @return
     * @throws IOException
     */
    public static Command commandReceive (DataInputStream commandInputStream) throws IOException {
        byte[] buf = new byte[10000];
        int num = commandInputStream.read(buf);
        String s = new String(buf, Charset.defaultCharset()).trim();
        System.out.println(s);
        return new Command(s);
    }

    public static void commandSend (DataOutputStream commandOutputStream, Command command) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String s : command.getCommand()) {
            sb.append(s);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        commandOutputStream.writeUTF(sb.toString());
        commandOutputStream.flush();
    }
}
