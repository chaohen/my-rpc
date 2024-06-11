package tianda.chaohen;

import lombok.extern.slf4j.Slf4j;
import tianda.chaohen.config.RpcConfig;
import tianda.chaohen.model.User;
import tianda.chaohen.proxy.ServiceProxyFactory;
import tianda.chaohen.service.UserService;
import tianda.chaohen.utils.ConfigUtils;

@Slf4j
public class ConsumerExample {

    public static void main(String[] args){
        /*RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class,"rpc");
        System.out.println(rpc);*/

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("chaohen-6.4");

        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println(newUser.getName());
        }
        else{
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);

    }
}
