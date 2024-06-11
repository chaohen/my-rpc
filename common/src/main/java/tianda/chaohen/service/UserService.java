package tianda.chaohen.service;

import tianda.chaohen.model.User;

public interface UserService {
    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);


    default short getNumber(){
        return 1;
    }
}
