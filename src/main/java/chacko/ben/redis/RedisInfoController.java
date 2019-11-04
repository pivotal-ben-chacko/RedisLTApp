package chacko.ben.redis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RestController
public class RedisInfoController {
    private Logger LOG = Logger.getLogger(RedisInfoController.class.getName());
    private Jedis jedis = null;
    private JedisPool pool = null;
    private boolean _continue = false;
    //private boolean _lock = false;
    Object _lock = new Object();
    
    @SuppressWarnings("deprecation")
	@RequestMapping("/info")
    public RedisInstanceInfo getInfo() {
        LOG.log(Level.WARNING, "Getting Redis Instance Info in Spring controller...");
        // first we need to get the value of VCAP_SERVICES, the environment variable
        // where connection info is stored
        String vcap = System.getenv("VCAP_SERVICES");
        LOG.log(Level.WARNING, "VCAP_SERVICES content: " + vcap);


        // now we parse the json in VCAP_SERVICES
        LOG.log(Level.WARNING, "Using GSON to parse the json...");
        JsonElement root = new JsonParser().parse(vcap);
        JsonObject redis = null;
        if (root != null) {
            if (root.getAsJsonObject().has("p.redis")) {
                redis = root.getAsJsonObject().get("p.redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            }
            else if (root.getAsJsonObject().has("p-redis")) {
                redis = root.getAsJsonObject().get("p-redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            }
            else {
                LOG.log(Level.SEVERE, "ERROR: no redis instance found in VCAP_SERVICES");
            }
        }

        // then we pull out the credentials block and produce the output
        if (redis != null) {
            JsonObject creds = redis.get("credentials").getAsJsonObject();
            RedisInstanceInfo info = new RedisInstanceInfo();
            info.setHost(creds.get("host").getAsString());
            info.setPort(creds.get("port").getAsInt());
            info.setPassword(creds.get("password").getAsString());

            // the object will be json serialized automatically by Spring web - we just need to return it
            return info;
        }
        else return new RedisInstanceInfo();
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST) 
    public String setKey(@RequestParam("kn") String key, @RequestParam("kv") String val) {
        LOG.log(Level.WARNING, "Called the key set method, going to set key: " + key + " to val: " + val);
        pool = getJedisPoolInstance();
        jedis = pool.getResource();
        jedis.set(key, val);

        return "Set key: " + key + " to value: " + val;
    }

    @RequestMapping("/get")
    String getKey(@RequestParam("kn") String key) {
        LOG.log(Level.INFO, "Called the key get method, going to return val for key: " + key);
        pool = getJedisPoolInstance();
        jedis = pool.getResource();
        return jedis.get(key);
    }

    @RequestMapping("/kill") 
    void killServer() {
        LOG.log(Level.WARNING, "About to kill the service!");
        System.exit(0);
    }

    
    private JedisPool getJedisPoolInstance() {
    	
        if (pool == null) {
            synchronized (_lock) {
                if (pool == null) {
                    RedisInstanceInfo info = getInfo();
                    pool = new JedisPool(new JedisPoolConfig(), info.getHost(), info.getPort(),
                            Protocol.DEFAULT_TIMEOUT, info.getPassword());
                }
            }
        }
        return pool;
    }
    
    @RequestMapping("/start/{key}")
    void startLoadTest(@PathVariable(value="key") String key) throws InterruptedException {
    	_continue = true;
    	pool = getJedisPoolInstance();
    	String value = "";
    	while(_continue) {
    	    try
            {
    		jedis = pool.getResource();
                value = jedis.get(key);
            }
            catch (JedisConnectionException e)
            {
                
            	LOG.log(Level.WARNING, "About to kill the service!");
            }
            finally
            {
                jedis.close();
            }
    		//String value = getKey(key);
    		LOG.log(Level.INFO, "Got the value from Redis: " + value);
    		Thread.sleep(50);
    	}
    }
    
    @RequestMapping("/stop")
    void stopLoadTest() {
    	_continue = false;
    }

}
