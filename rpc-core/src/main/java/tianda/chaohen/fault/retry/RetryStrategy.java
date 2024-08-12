package tianda.chaohen.fault.retry;

import tianda.chaohen.model.RpcResponse;

import java.util.concurrent.Callable;

public interface RetryStrategy {

    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;//Callable 类代表一个任务
}
