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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class RedisSampler extends AbstractSampler implements ThreadListener, TestStateListener, Interruptible {
    protected static final ThreadLocal<JedisPool> THREAD_LOCAL_CACHED_CONNECTION = new ThreadLocal<>();
    private static final Logger log = LoggerFactory.getLogger(RedisSampler.class);
    private static final long serialVersionUID = -7043041976771463703L;
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

    public JedisPool initConnectionPool() {
        JedisPool pool = THREAD_LOCAL_CACHED_CONNECTION.get();
        if (pool == null) {
            String host = getPropertyAsString(REDIS_HOST_PROP);
            int port = getPropertyAsInt(REDIS_PORT_PROP);
            String password = getPropertyAsString(REDIS_PASSWORD_PROP);
            int timeout = getPropertyAsInt(REDIS_TIMEOUT_PROP);
            int database = getPropertyAsInt(REDIS_DATABASE_PROP);
            String clientName = getPropertyAsString(REDIS_CLIENT_NAME_PROP);

            log.debug("initConnectionPool()");
            log.debug("%s:%s;%s;%s;%s;%s".formatted(host, port, timeout, password, database, clientName));
            if (password.isEmpty()) {
                password = null;
            }
            if (clientName.isEmpty()) {
                clientName = null;
            }
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(1000);
            jedisPoolConfig.setMaxIdle(1000);
            pool = new JedisPool(jedisPoolConfig, host, port, timeout, password, database, clientName);
            THREAD_LOCAL_CACHED_CONNECTION.set(pool);
        }
        log.debug(pool.toString());
        return pool;
    }


    @Override
    public void testStarted() {
        log.info("testStarted() Redis Sampler version 0.4");
        initConnectionPool();
    }

    @Override
    public void testStarted(String host) {
        log.info("testStarted(%s) Redis Sampler version 0.4".formatted(host));
        initConnectionPool();
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
        JedisPool pool = THREAD_LOCAL_CACHED_CONNECTION.get();
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public SampleResult sample(Entry entry) {
        log.debug("SampleResult sample(%s)".formatted(entry));
        JedisPool pool = THREAD_LOCAL_CACHED_CONNECTION.get();
        if (pool == null) {
            pool = initConnectionPool();
        }
        String operation = getPropertyAsString(REDIS_OPERATION_PROP);
        String key = getPropertyAsString(REDIS_KEY_PROP);
        String value = getPropertyAsString(REDIS_VALUE_PROP);
        long expire = getPropertyAsLong(REDIS_EXPIRE_PROP);
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());

        try (Jedis jedis = pool.getResource()) {
            long start = System.nanoTime();
            result.sampleStart(); // start stopwatch
            var response = switch (operation) {
                case "GET" -> jedis.get(key);
                case "SETEX" -> jedis.setex(key, expire, value);
                case "SET" -> jedis.set(key, value);
                case "PEXPIRE" -> jedis.pexpire(key, expire);
                case "EXISTS" -> jedis.exists(key);
                case "DEL" -> jedis.del(key);
                default -> throw new IllegalStateException("Unexpected value: " + operation);
            };
            result.sampleEnd(); // stop stopwatch
            long end = System.nanoTime() - start;
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
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), Charset.defaultCharset().name());
            result.setDataType(SampleResult.TEXT);
            result.setResponseCode("500");
        }
        return result;
    }

    @Override
    public boolean interrupt() {
        return false;
    }
}
