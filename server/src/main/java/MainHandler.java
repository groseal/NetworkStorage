import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {//настройки соединения и FileRequest от клиента
        if (msg instanceof FileRequest) {//если полученое сообщение FileRequest
            FileRequest fr = (FileRequest) msg;//кастует полученное сообщение к FileRequest
            if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {//если в server_storage есть файл FileRequest
                FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));//формирует FileMessage по имени файла из FileRequest
                ctx.writeAndFlush(fm);//отправляет сформированный FileMessage клиенту
            }
        }
        else if (msg instanceof FileMessage){
            FileMessage fm = (FileMessage) msg;
            Files.write(Paths.get("server_storage/"
                    + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
        }
        //прописать тут логику если клиент прислал на сервер сообщение типа FileMessage (1.51 видео)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
