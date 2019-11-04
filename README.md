# RedisLTApp
Redis load test app

## Usage

### Set key value pair

```curl -k -X POST "https://redis-info.apps.skynetsystems.io/set?kn=<KEY-SRING>&kv=<VALUE-STRING>"```
  
Example: 

```curl -k -X POST "https://redis-info.apps.skynetsystems.io/set?kn=Name&kv=James-Bond"```


### Get key value pair

```curl -k "https://redis-info.apps.skynetsystems.io/get?kn=<KEY-NAME>"```

Example

```curl -k "https://redis-info.apps.skynetsystems.io/get?kn=Name"```


### Start load test

Once a key-value pair has been set you can call the following endpoint to continously get the value from Redis.  The loop will not stop unless the `\stop` endpoint is called.

```curl -k "https://redis-info.apps.skynetsystems.io/start/<KEY-NAME>"```
  
Example

```curl -k "https://redis-info.apps.skynetsystems.io/start/Name"```

### Stop load test

```curl -k "https://redis-info.apps.skynetsystems.io/stop"```
  
Example

```curl -k "https://redis-info.apps.skynetsystems.io/stop"```
