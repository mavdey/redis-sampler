package ru.beeline.lt;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.charset.Charset;

public class RedisSampler extends AbstractSampler implements ThreadListener, TestStateListener, Interruptible {
    protected static final ThreadLocal<JedisPool> threadLocalCachedConnection = new ThreadLocal<>();
    protected static final ThreadLocal<Jedis> threadLocalCachedJedis = new ThreadLocal<>();
    private static final Logger log = LoggerFactory.getLogger(RedisSampler.class);
    private static final long serialVersionUID = 242L;
    private static final String REDIS_HOST_PROP = "RedisSampler.connection.host";
    private static final String REDIS_PORT_PROP = "RedisSampler.connection.port";
    private static final String REDIS_CLIENT_NAME_PROP = "RedisSampler.connection.client_name";
    private static final String REDIS_PASSWORD_PROP = "RedisSampler.connection.password";
    private static final String REDIS_TIMEOUT_PROP = "RedisSampler.connection.timeout";
    private static final String REDIS_DATABASE_PROP = "RedisSampler.connection.database";
    private static final String REDIS_OPERATION_PROP = "RedisSampler.request.operation";
    private static final String REDIS_KEY_PROP = "RedisSampler.request.key";
    private static final String REDIS_VALUE_PROP = "RedisSampler.request.value";
    private static final String REDIS_EXPIRE_PROP = "RedisSampler.request.expire";
    private Jedis jedis;

    public RedisSampler() {
        super();
        log.debug("RedisSampler()");
    }

    public String getHost() {
        return getPropertyAsString(REDIS_HOST_PROP);
    }

    public void setHost(String host) {
        setProperty(REDIS_HOST_PROP, host);
    }

    public String getPort() {
        return getPropertyAsString(REDIS_PORT_PROP);
    }

    public void setPort(String port) {
        setProperty(REDIS_PORT_PROP, port);
    }

    public String getPassword() {
        return getPropertyAsString(REDIS_PASSWORD_PROP);
    }

    public void setPassword(String password) {
        setProperty(REDIS_PASSWORD_PROP, password);
    }

    public String getTimeout() {
        return getPropertyAsString(REDIS_TIMEOUT_PROP);
    }

    public void setTimeout(String timeout) {
        setProperty(REDIS_TIMEOUT_PROP, timeout);
    }

    public String getDatabase() {
        return getPropertyAsString(REDIS_DATABASE_PROP);
    }

    public void setDatabase(String database) {
        setProperty(REDIS_DATABASE_PROP, database);
    }

    public String getClientName() {
        return getPropertyAsString(REDIS_CLIENT_NAME_PROP);
    }

    public void setClientName(String clientName) {
        setProperty(REDIS_CLIENT_NAME_PROP, clientName);
    }

    public String getOperation() {
        return getPropertyAsString(REDIS_OPERATION_PROP);
    }

    public void setOperation(String operation) {
        setProperty(REDIS_OPERATION_PROP, operation);
    }

    public String getKey() {
        return getPropertyAsString(REDIS_KEY_PROP);
    }

    public void setKey(String key) {
        setProperty(REDIS_KEY_PROP, key);
    }

    public String getValue() {
        return getPropertyAsString(REDIS_VALUE_PROP);
    }

    public void setValue(String value) {
        setProperty(REDIS_VALUE_PROP, value);
    }

    public String getExpire() {
        return getPropertyAsString(REDIS_EXPIRE_PROP);
    }

    public void setExpire(String expire) {
        setProperty(REDIS_EXPIRE_PROP, expire);
    }


    public void initConnectionPool() {
        Jedis jedis = threadLocalCachedJedis.get();
        if (jedis == null) {
            JedisPool pool = threadLocalCachedConnection.get();
            if (pool == null) {
                String host = getPropertyAsString(REDIS_HOST_PROP);
                int port = getPropertyAsInt(REDIS_PORT_PROP);
                String password = getPropertyAsString(REDIS_PASSWORD_PROP);
                int timeout = getPropertyAsInt(REDIS_TIMEOUT_PROP);
                int database = getPropertyAsInt(REDIS_DATABASE_PROP);
                String clientName = getPropertyAsString(REDIS_CLIENT_NAME_PROP);

                log.debug("initConnectionPool()");
                log.debug("%s:%s;%s;%s;%s;%s".formatted(host, port, timeout, password, database, clientName));
                if (password.equals("")) {
                    password = null;
                }
                if (clientName.equals("")) {
                    clientName = null;
                }

                pool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password, database, clientName);
                threadLocalCachedConnection.set(pool);
            }
            log.info(pool.toString());
            jedis = pool.getResource();
            threadLocalCachedJedis.set(jedis);
        }
    }


    @Override
    public void testStarted() {
        log.info("testStarted() Redis Sampler version 0.3");
    }

    @Override
    public void testStarted(String host) {
        log.info("testStarted(%s) Redis Sampler version 0.3".formatted(host));
    }

    @Override
    public void testEnded() {
        log.info("testEnded()");

    }

    @Override
    public void testEnded(String host) {
        log.info("testEnded(%s)".formatted(host));

    }

    @Override
    public void threadStarted() {
        log.debug("threadStarted() + %s".formatted(Thread.currentThread().getName()));
    }

    @Override
    public void threadFinished() {
        log.debug("threadFinished() + %s".formatted(Thread.currentThread().getName()));
        JedisPool pool = threadLocalCachedConnection.get();
        Jedis jedis = threadLocalCachedJedis.get();
        if (jedis != null) {
            jedis.close();
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public SampleResult sample(Entry entry) {
        log.debug("SampleResult sample(%s)".formatted(entry));
        if (jedis == null) {
            initConnectionPool();
            jedis = threadLocalCachedJedis.get();
        }
        String operation = getPropertyAsString(REDIS_OPERATION_PROP);
        String key = getPropertyAsString(REDIS_KEY_PROP);
        String value = getPropertyAsString(REDIS_VALUE_PROP);
        long expire = getPropertyAsLong(REDIS_EXPIRE_PROP);
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.sampleStart(); // start stopwatch

        try {
            long start = System.nanoTime();
            var response = switch (operation) {
                case "GET" -> jedis.get(key);
                case "SETEX" -> jedis.setex(key, expire, value);
                case "PEXPIRE" -> jedis.pexpire(key, expire);
                case "EXISTS" -> jedis.exists(key);
                case "DEL" -> jedis.del(key);
                default -> throw new Exception();
            };
            long end = System.nanoTime() - start;
            result.sampleEnd(); // stop stopwatch
            result.setResponseData("""
                    {
                        "operation": "%s",
                        "key": "%s",
                        "response": "%s",
                        "duration": %d
                    }
                    """.formatted(operation, key, response == null ? "null" : response.toString(), end), Charset.defaultCharset().name());
            result.setSuccessful(true);
            result.setResponseMessage(response == null ? "null" : response.toString());
            result.setResponseCodeOK(); // 200 code
        } catch (Throwable e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e);

            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), Charset.defaultCharset().name());
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("500");
        }
        return result;
    }

    @Override
    public boolean interrupt() {
        return false;
    }
}
