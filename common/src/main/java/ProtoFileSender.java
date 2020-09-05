import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


//как отправляются данные
public class ProtoFileSender {
    public static void sendFile(byte signalByte, Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));//создаем ссылку на кусок отправляемого файла (путь к файлу, начало куска, размер куска)

        byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1+4+fileNameBytes.length+8);
        buf.writeByte((byte)25);//сигнальный байт
        buf.writeInt(fileNameBytes.length);//длина имени файла
        buf.writeBytes(fileNameBytes);//имя файла
        buf.writeLong(Files.size(path));//размер файла
        channel.writeAndFlush(buf);//отправка буфера в сеть
//        //посылаем на сервер сигнальный байт
//        //25 байт - отправка файла
//        ByteBuf buf = null;//создаем буфер
//        buf = ByteBufAllocator.DEFAULT.directBuffer(1);//задаем размер буфера ()
//        buf.writeByte(signalByte);//записываем в буфер сигнальный байт
//        channel.write(buf);//отправляем на сервер
//
//        //посылаем на сервер длину имени файла
//        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);//создаем байтовый массив с именем файла (получаем из пути имя файла, преобразуем к строке, получаем из нее байты)
//        buf = ByteBufAllocator.DEFAULT.directBuffer(4);//создаем буфер на 4 байта для передачи длины имени файла
//        buf.writeInt(filenameBytes.length);//в буфер кладем int = длине массива с именем файла
//        channel.write(buf);//отправляем не сервер длину имени файла
//
//        //посылаем на сервер имя файла
//        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);//по длине имени файла выделяем память под буфер
//        buf.writeBytes(filenameBytes);//кладм в буфер имя файла
//        channel.write(buf);//отправляем не сервер имя файла
//
//        //посылаем на сервер размер файла
//        buf = ByteBufAllocator.DEFAULT.directBuffer(8);//выделяем под буфер 1 long для записи размера файла
//        buf.writeLong(Files.size(path));//записываем в буфер размер файла
//        channel.writeAndFlush(buf);//отправляем на сервер размер файла
//
        //отправка файла на сервер
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);//отправка файла в сеть
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);//как только файл передастся сработает finishListener (он может быть, а может и отсутствовать, он приходит в качестве аргумента в метод)
        }
    }

    public static void requestFile(byte signalByte, Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        //посылаем на сервер сигнальный байт
        // 26 байт - запрос файла
        byte[]fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1+4+fileNameBytes.length);
        buf.writeByte((byte)26);//сигнальный байт
        buf.writeInt(fileNameBytes.length);//длина имени файла
        buf.writeBytes(fileNameBytes);//имя файла
        channel.writeAndFlush(buf);//отправка на сервер буфера
    }
}


