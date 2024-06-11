package tianda.chaohen.server;

public interface HttpServer {
    /**
     * 统一的启动服务器接口
     * @param port
     */
    void doStart(int port);
}
