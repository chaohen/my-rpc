package tianda.chaohen.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
public class RpcConfig {

    private String name = "my-rpc";

    private String version = "1.0";

    private String serverHost = "localhost";

    private Integer serverPort = 8080;

    private boolean mock = true;
}
