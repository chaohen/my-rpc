package tianda.chaohen.server;

import io.vertx.core.Vertx;
import tianda.chaohen.server.HttpServer;
import tianda.chaohen.server.HttpServerHandler;

/**
 * 基于Vert.x实现的web服务器，能够监听指定端口并处理请求
 */
public class VertxHttpServer implements HttpServer {
    public void doStart(int port){
        Vertx vertx = Vertx.vertx();//创建vert.x实例
        //创建HTTP服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        //监听端口并处理请求
        server.requestHandler(new HttpServerHandler());

        /*server.requestHandler(request->{
            //处理HTTP请求
            System.out.println("Received request:"+request.method()+" "+request.uri());
            //发送HTTP响应
            request.response()
                    .putHeader("content-type","text/plain")
                    .end("Hello from Vert.x HTTP server!");
        });*/
        //启动HTTP服务器并监听指定端口
        server.listen(port,result->{
            if(result.succeeded()){
                System.out.println("Server is now listening on port"+port);
            }
            else{
                System.out.println("Failed to start server:"+result.cause());
            }
        });
    }
}
