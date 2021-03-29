package fileassembler;

import java.io.Serializable;

public class FileChunk implements Serializable {
    char[] header = {67, 72}; //CH
    int userId;         //ИД пользователя
    int size;           //размер отрезка
    int position;       //стартовая позиция отрезка в файле
    boolean last;       //флаг последнего отрезка в файле
    String filename;    //имя файла
    byte[] buffer;      //сам отрезок

    public FileChunk(int userId, int size, int position, boolean last, String filename, byte[] buffer) {
        this.userId = userId;
        this.size = size;
        this.position = position;
        this.last = last;
        this.filename = filename;
        this.buffer = buffer;
    }

    public int getUserId() {
        return userId;
    }

    public int getSize() {
        return size;
    }

    public int getPosition() {
        return position;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public boolean isLast() {
        return last;
    }
}
