
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;//исходящий поток для отправки
    private static ObjectDecoderInputStream in;//входящий поток для получения

    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //позволяет отправлять любые сообщения серверу
    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;// если успешная отправка сообщения
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;//если не удалось отправить сообщение
    }


    //получение объектов от сервера
    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();//блокирующая операция, может ожидать посылки от сервера
        return (AbstractMessage) obj;//возвращает полученный от сервера объект
    }
}
