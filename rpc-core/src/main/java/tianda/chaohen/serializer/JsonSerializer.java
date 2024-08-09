package tianda.chaohen.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import tianda.chaohen.model.RpcRequest;
import tianda.chaohen.model.RpcResponse;

import java.io.IOException;

public class JsonSerializer implements Serializer{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public <T> byte[] serialize(T obj) throws IOException{
        return OBJECT_MAPPER.writeValueAsBytes(obj);//调用该方法将传入的obj对象序列化为一个字节数组。
    }

    public <T> T deserialize(byte[] bytes,Class<T> classType) throws IOException{
        T obj = OBJECT_MAPPER.readValue(bytes,classType);
        if(obj instanceof RpcRequest){
            return handleRequest((RpcRequest) obj,classType);
        }
        if(obj instanceof RpcResponse){
            return handleResponse((RpcResponse) obj,classType);
        }
        return obj;
    }

    /**
     * 为了解决类型擦除问题
     * @param rpcRequest
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T handleRequest(RpcRequest rpcRequest,Class<T> type) throws IOException{
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        for(int i=0;i<parameterTypes.length;i++){
            Class<?> clazz = parameterTypes[i];
            if(!clazz.isAssignableFrom(args[i].getClass())){
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i]=OBJECT_MAPPER.readValue(argBytes,clazz);
            }
        }
        return type.cast(rpcRequest);//检查rpcRequest是否是type类型或其子类型的实例。如果是返回引用即rpcRequest，不是的话抛出 ClassCastException
    }

    private <T> T handleResponse(RpcResponse rpcResponse,Class<T> type) throws IOException{
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes,rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }

}
