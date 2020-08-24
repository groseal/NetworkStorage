import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField loginField;//поле ввода логина
    @FXML
    public TextField passwordField;//поле ввода пароля
    @FXML
    public Button connectButton;//кнопка подключения
    @FXML
    public Button disconnectButton;//кнопка отключения
    @FXML
    public Button downloadServerButton;//кнопка загрузки файла на сервер
    @FXML
    public Button uploadServerButton;//кнопка загрузки файла на клиент
    @FXML
    public ListView<String> clientListView; //список файлов на клиенте (в client_storage)
    @FXML
    public ListView<String> serverListView; //список файлов на сервере (в server_storage)
    @FXML
    public TextField fileNameField;//имя интересующего файла

    public void authorization(ActionEvent actionEvent) {
    }

    public void initialize(URL location, ResourceBundle resources) {//инплементированный метод
        Network.start();
        Thread t = new Thread(() -> {//демон поток ожидающий файлы от сервера
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();//ожидает любые сообщения от сервера
                    if (am instanceof FileMessage) {//если сервер прислал FileMessage
                        FileMessage fm = (FileMessage) am;//кастует полученное сообщение к FileMessage
                        Files.write(Paths.get("client_storage/"
                                + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);//записывает полученный файл в client_storage
                        refreshLocalFilesList();//обновляет список файлов в client_storage
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }


    //действие по кнопке uploadServerButton
    public void uploadFileClient(ActionEvent actionEvent) {
        if (fileNameField.getLength() > 0) {
            Network.sendMsg(new FileRequest(fileNameField.getText()));//клиент посылает серверу FileRequest с именем интересующего файла
            fileNameField.clear();//очищает форму в интерфейсе
        }
    }

    //действие по кнопке downloadServerButton
    public void downloadFileServer(ActionEvent actionEvent) throws IOException {
        if (fileNameField.getLength() > 0) {
            if (Files.exists(Paths.get("client_storage/" + fileNameField))) {
                Network.sendMsg(new FileMessage(Paths.get("client_storage/" + fileNameField)));
                fileNameField.clear();
            }
        }
    }

    //обновление списка локальных файлов
    public void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                clientListView.getItems().clear();
                Files.list(Paths.get("client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientListView.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
