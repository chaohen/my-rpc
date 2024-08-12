package tianda.chaohen.fault.tolerant;

import tianda.chaohen.model.RpcResponse;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface TolerantStrategy {
    /**
     * 容错
     * @param context 上下文，用于传递数据
     * @param e       异常
     * @Return RpcResponse
     */
    RpcResponse doTolerant(Map<String,Object> context, Exception e);
}
