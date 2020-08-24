import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String filename;//имия файла
    private byte[] data;//файловый массив(тело файла)

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FileMessage(Path path) throws IOException {//путь к файлу из которого можно создать FileMessage
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
