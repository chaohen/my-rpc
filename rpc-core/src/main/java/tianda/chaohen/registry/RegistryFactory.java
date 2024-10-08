package tianda.chaohen.registry;

import tianda.chaohen.spi.SpiLoader;

public class RegistryFactory {
    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();


    public static Registry getInstance(String key){
        return SpiLoader.getInstance(Registry.class,key);
    }

}
