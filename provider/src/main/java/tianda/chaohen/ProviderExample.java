package tianda.chaohen;

import lombok.extern.slf4j.Slf4j;
import tianda.chaohen.registry.LocalRegistry;
import tianda.chaohen.server.HttpServer;
import tianda.chaohen.server.VertxHttpServer;
import tianda.chaohen.service.UserService;

public class ProviderExample {
    public static void main(String[] args){
        RpcApplication.init();
        LocalRegistry.register(UserService.class.getName(),UserServiceIml.class);

        HttpServer httpServer = new VertxHttpServer();

        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
