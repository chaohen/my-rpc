package tianda.chaohen.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 */
public class ConfigUtils {


    public static <T> T loadConfig(Class<T> tClass,String prefix){
        return loadConfig(tClass,prefix,"");
    }

    /**
     * 后续就可以调用loadConfig方法，传入配置类（RpcConfig），前缀和配置文件路径，将配置文件内容加载到RpcConfig对象中，这样就能读取配置了
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass,String prefix,String environment){//注意这里Class<T> class，表示传入的是一个类（不是实例），T就是类名。比如本项目中传入的就是RpcConfig类
        StringBuilder configFileBuilder = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)){
            configFileBuilder.append('-').append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());//加载指定路径的配置文件
        return props.toBean(tClass,prefix);//这是Hutool包下的方法，将配置文件内容转换为指定类型的java对象，并使用前缀过滤相关配置项
    }


}
