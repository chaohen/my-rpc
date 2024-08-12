package tianda.chaohen.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import tianda.chaohen.RpcApplication;
import tianda.chaohen.config.RpcConfig;
import tianda.chaohen.constant.RpcConstant;
import tianda.chaohen.fault.retry.RetryStrategy;
import tianda.chaohen.fault.retry.RetryStrategyFactory;
import tianda.chaohen.fault.tolerant.TolerantStrategy;
import tianda.chaohen.fault.tolerant.TolerantStrategyFactory;
import tianda.chaohen.loadbalancer.LoadBalancer;
import tianda.chaohen.loadbalancer.LoadBalancerFactory;
import tianda.chaohen.model.RpcRequest;
import tianda.chaohen.model.RpcResponse;
import tianda.chaohen.model.ServiceMetaInfo;
import tianda.chaohen.registry.Registry;
import tianda.chaohen.registry.RegistryFactory;
import tianda.chaohen.serializer.Serializer;
import tianda.chaohen.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //Serializer serializer = new JdkSerializer();

        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try{
            //序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);

            //从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException("暂无服务地址");
            }
            //负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            Map<String, Object> requestParms = new HashMap<>();
            requestParms.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParms, serviceMetaInfoList);

            //发送请求
            // 使用重试机制
            RpcResponse rpcResponse;
            try {
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(()-> {
                    try(HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                            .body(bodyBytes)
                            .execute()) {
                        byte[] result = httpResponse.bodyBytes();
                        RpcResponse rpcResponseRes = serializer.deserialize(result, RpcResponse.class);
                        return rpcResponseRes;
                    }
                });
            } catch (Exception e){
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(null, e);
            }
            return rpcResponse.getData();
            /*try(HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                RpcResponse rpcResponse = serializer.deserialize(result,RpcResponse.class);
                return rpcResponse.getData();
            }*/
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
