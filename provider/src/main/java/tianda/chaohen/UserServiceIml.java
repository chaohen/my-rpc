package tianda.chaohen;

import tianda.chaohen.model.User;
import tianda.chaohen.service.UserService;

public class UserServiceIml implements UserService {
    public User getUser(User user){
        System.out.println("用户名："+user.getName()+"8.6");
        return user;
    }
}
