import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private int nextLength;//размер следующего куска ожидаемого файла
    private long fileLength;//длина файла
    private long receivedFileLength;//сколько байт уже получено
    private BufferedOutputStream out;//запись байтов в файл

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {//если пришло сообщение, пытаемся понять что это за сообщение
            byte readed = buf.readByte();//вычитываем первый сигнальный байт
            if (readed == 25) {//если пришел сигнальный байт соответствующий получению файла
                receivedFileLength = 0L;//считаем то не получили ни одного байта от присылаемого файла
                System.out.println("STATE CL: Start file receiving");//сообщаем о старте передачи файла
                gettingFileNameLength(buf);//получаем длину имени файла
                byte[] fileName = getFileName(buf);//получаем имя файла
                System.out.println("STATE CL: Filename received - " + new String(fileName, "UTF-8"));//собираем из байтового массива строку с именем файла
                out = new BufferedOutputStream(new FileOutputStream("client_storage/" + new String(fileName)));//открываем BufferedOutputStream для получения данных файла
                gettingFileLength(buf);//получаем длину файла
                getFile(buf);//получаем файл
            } else {
                System.out.println("ERROR CL: Invalid first byte - " + readed);//сообщаем об ошибке
            }
        }
    }

    //получаем длину имени файла
    private void gettingFileNameLength(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {//пришло более 4 байт (длина имени файла = 1 int)
            System.out.println("STATE CL: Get filename length");
            nextLength = buf.readInt();//вычитываем из входящего буфера 1 int
        }
    }

    //получаем имя файла
    private byte[] getFileName(ByteBuf buf) throws Exception {
        byte[] fileName = new byte[nextLength];//формируем байтовый массив для имени
        if (buf.readableBytes() >= nextLength) {//проверяем есть ли в буфере байтов столько сколько в длине имени что бы не прочитать только часть имени файла
            buf.readBytes(fileName);//вычитываем из буфера байты в байтовый массив
        }
        return fileName;
    }

    //получаем длину файла
    private void gettingFileLength(ByteBuf buf) {
        if (buf.readableBytes() >= 8) {//если пришло более 8 байт (длина файла = 1 long)
            fileLength = buf.readLong();//вычитываем из буфера 1 long (длину файла)
            System.out.println("STATE CL: File length received - " + fileLength);
        }
    }

    //получаем файл
    private void getFile(ByteBuf buf) throws Exception {
        while (buf.readableBytes() > 0) {//если в буфере есть непрочитанные байты
            out.write(buf.readByte());//пишем побайтово непрочитанные байты в файл
            receivedFileLength++;//говорим что получили еще один байт (подсчитываем количество полученных байтов)
            if (fileLength == receivedFileLength) {//если длина ожидаемого файла равна количеству полученных байтов
                System.out.println("CL File received");
                out.close();//закрываем файл в который писали байты
                break;//останавливаем получение
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
