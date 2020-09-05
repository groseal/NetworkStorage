
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.currentThread;

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
        CountDownLatch networkStarter = new CountDownLatch(1);//создание защелки на 1 щелчок
        new Thread(() -> Network.getInstance().start(networkStarter)).start();//запуск сети в отдельном потоке
        try {
            networkStarter.await();//ожидает открытия сетевого соединения
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        refreshLocalFilesList();
    }

    //Получение файла с сервера
    public void uploadFileClient(ActionEvent actionEvent) throws Exception {
        ProtoFileSender.requestFile(Paths.get(fileNameField.getText()), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {//действие при неудачном получении файла
                future.cause().printStackTrace();
//                Network.getInstance().stop();
            }
            if (future.isSuccess()) {//действие при удачном получении файла
                System.out.println("Файл успешно получен");
//                Network.getInstance().stop();
            }
        });
        fileNameField.clear();//очищает форму в интерфейсе
    }

    //Отправка файла на сервер
    public void downloadFileServer(ActionEvent actionEvent) throws Exception {
        //Действия по finishListener (из ProtoFileSender, метод sendFile)
        ProtoFileSender.sendFile(Paths.get("client_storage/" + fileNameField.getText()), Network.getInstance().getCurrentChannel(), future -> {//указываем файл и сеть для отправки
            if (!future.isSuccess()) {//действие при неудачной передаче файла
                future.cause().printStackTrace();
//                Network.getInstance().stop();
            }
            if (future.isSuccess()) {//действие при удачной передаче файла
                System.out.println("Файл успешно передан");
//                Network.getInstance().stop();
            }
        });
        fileNameField.clear();//очищает форму в интерфейсе
    }


//        Network.start();
//        Thread t = new Thread(() -> {//демон поток ожидающий файлы от сервера
//            try {
//                while (true) {
//                    AbstractMessage am = Network.readObject();//ожидает любые сообщения от сервера
//                    if (am instanceof FileMessage) {//если сервер прислал FileMessage
//                        FileMessage fm = (FileMessage) am;//кастует полученное сообщение к FileMessage
//                        Files.write(Paths.get("client_storage/"
//                                + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);//записывает полученный файл в client_storage
//                        refreshLocalFilesList();//обновляет список файлов в client_storage
//                    }
//                }
//            } catch (ClassNotFoundException | IOException e) {
//                e.printStackTrace();
//            } finally {
//                Network.stop();
//            }
//        });
//        t.setDaemon(true);
//        t.start();
//        refreshLocalFilesList();
//    }

    //    //действие по кнопке downloadServerButton. Загрузка файла на сервер
//    public void downloadFileServer (ActionEvent actionEvent) {
//        if (fileNameField.getLength() > 0) {
//            Network.sendMsg(new FileRequest(fileNameField.getText()));//клиент посылает серверу FileRequest с именем интересующего файла
//            fileNameField.clear();//очищает форму в интерфейсе
//        }
//    }
//
//    //действие по кнопке uploadServerButton. Загрузка файла на клиент
//    public void uploadFileClient(ActionEvent actionEvent) throws IOException {
//        if (fileNameField.getLength() > 0) {
//            if (Files.exists(Paths.get("client_storage/" + fileNameField.getText()))) {
//                Network.sendMsg(new FileMessage(Paths.get("client_storage/" + fileNameField.getText())));
//                fileNameField.clear();
//            }
//        }
//    }
//
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


