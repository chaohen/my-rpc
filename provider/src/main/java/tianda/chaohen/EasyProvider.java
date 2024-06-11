package tianda.chaohen;


import lombok.extern.slf4j.Slf4j;
import tianda.chaohen.registry.LocalRegistry;
import tianda.chaohen.server.HttpServer;
import tianda.chaohen.server.VertxHttpServer;
import tianda.chaohen.service.UserService;

public class EasyProvider {
    public static void main(String[] args){
        /**
         * 类名和对应的实现类
         */
        LocalRegistry.register(UserService.class.getName(),UserServiceIml.class);
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
