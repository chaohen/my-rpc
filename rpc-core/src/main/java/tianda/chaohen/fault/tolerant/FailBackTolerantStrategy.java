package tianda.chaohen.fault.tolerant;

import tianda.chaohen.model.RpcResponse;

import java.util.Map;

//降级到其他服务
public class FailBackTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 根据应用场景扩展，获取降级的服务并调用
        return null;
    }
}
