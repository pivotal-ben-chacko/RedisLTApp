package chacko.ben.redis;

import lombok.Data;

@Data
public class RedisInstanceInfo {
    private String host;
    private int port;
    private String password;
}
