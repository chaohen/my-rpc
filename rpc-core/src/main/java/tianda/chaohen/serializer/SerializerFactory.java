package tianda.chaohen.serializer;

import tianda.chaohen.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    //使用设计模式中的工厂模式+单例模式
   /* private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>();

    static{
        KEY_SERIALIZER_MAP.put(SerializerKeys.JDK,new JdkSerializer());
        KEY_SERIALIZER_MAP.put(SerializerKeys.JSON,new JsonSerializer());
        KEY_SERIALIZER_MAP.put(SerializerKeys.HESSIAN,new HessianSerializer());
    }

    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get("jdk");

    public static Serializer getInstance(String key){
        return KEY_SERIALIZER_MAP.getOrDefault(key,DEFAULT_SERIALIZER);
    }*/

    static {
        SpiLoader.load(Serializer.class);//加载Serializer接口的实现类，放进SpiLoader的静态哈希表中
    }

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     */
    public static Serializer getInstance(String key){//key是实现类的键名，如jdk
        return SpiLoader.getInstance(Serializer.class,key); //Serializer.class是接口名
    }

}
