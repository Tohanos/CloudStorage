package client.utils;

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
    public static void sendFileChunk(FileChunk chunk, DataOutputStream dataOutputStream) throws IOException {
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
    public static FileChunk recieveFileChunk (DataInputStream dataInputStream) throws IOException {

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
}
