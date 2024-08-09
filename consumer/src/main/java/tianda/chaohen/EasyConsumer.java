package tianda.chaohen;

import tianda.chaohen.model.User;
import tianda.chaohen.proxy.ServiceProxyFactory;
import tianda.chaohen.service.UserService;

import java.util.*;

public class EasyConsumer {
    public static void main(String[] args){
     //   UserService userService = null; //这里先预留为null。后续目标是通过RPC框架，快速得到一个支持远程
        //调用服务提供者的代理对象，像调用本地方法一样调用UserService的方法。
        //UserService userService = new UserServiceProxy(); //静态代理

        //调用工厂为UserService获取动态代理对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("chaohen");
        //调用
        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println(newUser.getName());
        }
        else{
            System.out.println("user==null");
        }


    }
}
