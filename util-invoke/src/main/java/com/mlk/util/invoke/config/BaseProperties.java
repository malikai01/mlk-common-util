package com.mlk.util.invoke.config;

import com.google.common.collect.Maps;
import com.mlk.util.invoke.exception.SysErrorConsts;
import com.mlk.util.invoke.exception.SysException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.*;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * spring中获取资源文件
 *
 * @author huangxin
 */
public class BaseProperties extends PropertyOverrideConfigurer {

    private static Map<String, Object> ctxPropertiesMap = new ConcurrentHashMap<>();

    private static ConfigurableConversionService conversionService = new DefaultConversionService();

    private static final AtomicBoolean LOADED_FLAG = new AtomicBoolean(true);

    private static final String APP_PROPERTIES = "applicationConfigurationProperties";

    private static final String ENV_KEY = "spring.profiles.active";

    private static final String CONFIG_CHECK_ENABLE = "config.check.enable";

    private static final String CLASS_PATH_RESOURCE = "class path resource";

    private static final String CHARACTER = "[";

    private static final String CHARACTERTWO = "]";

    private static final String IP_REGEX = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";

    /**
     * http|s 包含qa,yz
     */
    private static final String DOMAIN_REGEX = "^(https?):\\/\\/((qa|yz).+?)$";

    private volatile static String env;

    private volatile static boolean isCheck;

    private static Environment event;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) {
        super.processProperties(beanFactoryToProcess, props);
        if (LOADED_FLAG.get()) {
            loadData(props);
        }
    }

    protected static void setHjConfig(String key, String value) {
        ctxPropertiesMap.put(key, value);
    }

    protected static void remove(String key) {
        ctxPropertiesMap.remove(key);
    }

    public static String getEnv() {
        String env = (String) ctxPropertiesMap.get(ENV_KEY);
        if (env != null) {
            env = env.trim();
        }

        return env;
    }

    public static Object getProperty(String key) {
        return ctxPropertiesMap.get(key);
    }

    public static String getString(String key) {
        return (String) ctxPropertiesMap.get(key);
    }

    public static Map<String, Object> getAll() {
        return Collections.unmodifiableMap(ctxPropertiesMap);
    }

    public static boolean containsProperty(String key) {
        return ctxPropertiesMap.containsKey(key);
    }

    public static boolean setProperty(String key, String value) {
        if (key == null || value == null) {
            return false;
        }
        if (isCheck) {
            checkConfig(key, value);
        }
        ctxPropertiesMap.put(key, value);

        return true;

    }

    public static String getProperty(String key, String defaultValue) {
        Object value = ctxPropertiesMap.get(key);
        return value == null ? defaultValue : (String) value;
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        Object value = ctxPropertiesMap.get(key);
        if (value == null) {
            return null;
        }
        return conversionService.convert(value, targetType);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        Object value = ctxPropertiesMap.get(key);
        if (value == null) {
            return defaultValue;
        }
        return conversionService.convert(value, targetType);
    }

    public static synchronized void loadData(Environment event) {
        if (LOADED_FLAG.getAndSet(false)) {
            BaseProperties.event = event;
            env = event.getProperty(ENV_KEY);
            isCheck = Boolean.getBoolean(event.getProperty(CONFIG_CHECK_ENABLE, "false"));
            ConfigurableEnvironment environment = (ConfigurableEnvironment) event;

            Iterator<PropertySource<?>> iter = environment.getPropertySources().iterator();
            while (iter.hasNext()) {
                propertyHandler(iter);
            }

            MutablePropertySources sources = environment.getPropertySources();
            for (PropertySource<?> item : sources) {
                if (item.getName().contains("applicationConfig:")) {
                    BaseProperties.setPropertySource(item);
                }
            }


            ctxPropertiesMap.putAll(getSysArgs());
        }
    }

    private static Map<String, String> getSysArgs() {
        List<String> sysArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        Map<String, String> data = Collections.emptyMap();
        if (sysArgs.size() > 0) {
            data = Maps.newHashMap();
        }

        for (String sysArg : sysArgs) {
            if (sysArg.startsWith("-D")) {
                String[] argKV = sysArg.split("=");
                if (argKV.length == 2) {
                    data.put(argKV[0].replace("-D", ""), argKV[1]);
                }
            }
        }
        return data;
    }


    private static void setPropertySource(PropertySource<?> ps) {
        EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) ps;
        for (String key : eps.getPropertyNames()) {
            Object value = eps.getProperty(key);
            if (isCheck) {
                checkConfig(key, value);
            }
            ctxPropertiesMap.put(key, BaseProperties.event.getProperty(key));
        }
    }

    private static void propertyHandler(Iterator<PropertySource<?>> iter) {
        PropertySource<?> ps = iter.next();
        String name = ps.getName();
        if (name != null && name.startsWith(CLASS_PATH_RESOURCE)) {
            try {
                String propertiesName = name.substring(name.indexOf(CHARACTER) + 1, name.lastIndexOf(CHARACTERTWO));
                loadData(PropertiesLoaderUtils.loadAllProperties(propertiesName));
            } catch (IOException e) {
                throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
            }
        }
    }

    private static void loadData(Properties props) {
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            if (isCheck) {
                checkConfig(keyStr, value);
            }
            ctxPropertiesMap.put(keyStr, BaseProperties.event.getProperty(keyStr));
        }

    }

    private static void checkConfig(String key, Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (env != null && env.toLowerCase().startsWith("prod")) {
                checkIp(key, str);
                checkDomain(key, str);
            }
        }

    }

    private static void checkIp(String key, String value) {
        Pattern pattern = Pattern.compile(IP_REGEX);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            String ip = matcher.group();
            if (!ip.startsWith("10.")) {
                throw new SysException(SysErrorConsts.SYS_ERROR_CODE, String.format("The application-prod.properties contains misconfig! %s=%s", key, value));
            }
            String subValue = new String(value);
            subValue = subValue.replace(ip, "");
            checkIp(key, subValue);
        }

    }

    private static void checkDomain(String key, String value) {

        Pattern pattern = Pattern.compile(DOMAIN_REGEX);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, String.format("The application-prod.properties contains misconfig! %s=%s", key, value));
        }
    }


    public static void main(String[] args) {
        checkDomain("test", "https://www.baidu.com");
        checkDomain("testqa", "https://qawww.baidu.com");
        checkDomain("testyz", "https://yzwww.baidu.com");
        checkDomain("testprod", "https://qywww.baidu.com");
    }
}
