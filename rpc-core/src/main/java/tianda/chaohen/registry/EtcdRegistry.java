package tianda.chaohen.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import tianda.chaohen.config.RegistryConfig;
import tianda.chaohen.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    private Client client;

    private KV kvClient;

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    @Override
    public void init(RegistryConfig registryConfig){
        client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
        kvClient = client.getKVClient();
        heartBeat();
    }


    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        //创建Lease和KV客户端
        Lease leaseClient = client.getLeaseClient();

        //创建一个30s的租约，并返回租约的唯一标识符
        long leaseId = leaseClient.grant(180).get().getID();//get()是一个同步方法，等待前面的租约请求完成，并返回租约响应对象。

        //设置要存储的键值对，服务的唯一标识符：服务实例的详细信息
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo),StandardCharsets.UTF_8);

        //将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        //PutOption是etcd客户端中的一个配置类，用于定义put操作的参数。在etcd中put操作用于存储键值对，PutOption允许配置这个操作的各种参数。
        kvClient.put(key,value,putOption).get();

        localRegisterNodeKeySet.add(registerKey);
    }


    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        System.out.println(ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey());
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey(),StandardCharsets.UTF_8)).get();//根据键值注销服务
        // 也要从本地缓存移除
        localRegisterNodeKeySet.remove(registerKey);
    }


    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey){

        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if(cachedServiceMetaInfoList != null){
            return cachedServiceMetaInfoList;
        }

        String searchPrefix = ETCD_ROOT_PATH + serviceKey + '/';


        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            //GetOption用于配置从ETCD中获取数据时的选项。上面的配置是开启前缀查询，即返回所有符合前缀的键值对
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);//将字节序列转换为字符串
                        // 监听 key 的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
            // 写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    public void destroy(){
        System.out.println("当前节点下线");
        // 下线节点
        // 遍历本节点所有的 key
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        //释放资源
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        // 10 秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的 key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 该节点已过期（需要重启节点才能重新注册）
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }
                        // 节点未过期，重新注册（相当于续签）
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }


    /**
     * 监听（消费端）
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }









    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Client client = Client.builder().endpoints("http://localhost:2379").build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key_chaohen/test1".getBytes());
        ByteSequence value = ByteSequence.from("test_value_chaohen".getBytes());

        ByteSequence key2 = ByteSequence.from("test_key_chaohen/test2".getBytes());
        ByteSequence value2 = ByteSequence.from("test_value_chaohen2".getBytes());
        ByteSequence key3 = ByteSequence.from("test_key_chaohen2".getBytes());
        kvClient.put(key,value).get();
        kvClient.put(key2,value2).get();
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        GetResponse response = getFuture.get();

        kvClient.delete(key).get();
        kvClient.delete(key2).get();
        kvClient.delete(key3).get();

        /*ByteSequence key4 = ByteSequence.from("/rpc/myService:1.0/localhost:1234".getBytes());
        kvClient.delete(key4).get();*/
    }
}
