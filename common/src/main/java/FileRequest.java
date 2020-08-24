public class FileRequest extends AbstractMessage {
    private String filename;// имя файла по которому сервер понимает что хочет клиент

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename) {
        this.filename = filename;
    }
}