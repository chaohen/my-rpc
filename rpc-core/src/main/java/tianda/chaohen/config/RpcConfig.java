package tianda.chaohen.config;

import lombok.Data;
import tianda.chaohen.fault.retry.RetryStrategyKeys;
import tianda.chaohen.fault.tolerant.TolerantStrategy;
import tianda.chaohen.fault.tolerant.TolerantStrategyKeys;
import tianda.chaohen.loadbalancer.LoadBalancerKeys;
import tianda.chaohen.serializer.SerializerKeys;

@Data
public class RpcConfig {

    private String name = "my-rpc";

    private String version = "1.0";

    private String serverHost = "localhost";

    private Integer serverPort = 8080;

    private boolean mock = true;

    private RegistryConfig registryConfig = new RegistryConfig();

    private String serializer = SerializerKeys.JDK;

    //负载均衡
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    //重试机制
    private String retryStrategy = RetryStrategyKeys.No;

    //容错策略
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
}
