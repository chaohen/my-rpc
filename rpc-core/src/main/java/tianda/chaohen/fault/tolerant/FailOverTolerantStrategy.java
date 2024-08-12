package tianda.chaohen.fault.tolerant;

import tianda.chaohen.model.RpcResponse;

import java.util.Map;

//转移到其他服务节点
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 根据应用场景进行扩展，获取其他服务节点并调用
        return null;
    }
}
