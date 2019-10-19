package com.nepxion.discovery.plugin.framework.context;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.constant.ConsulConstant;
import com.nepxion.discovery.plugin.framework.decorator.ConsulServiceRegistryDecorator;
import com.nepxion.discovery.plugin.framework.util.MetadataUtil;

public class ConsulApplicationContextInitializer extends PluginApplicationContextInitializer {
    @Override
    protected Object afterInitialization(ConfigurableApplicationContext applicationContext, Object bean, String beanName) throws BeansException {
        if (bean instanceof ConsulServiceRegistry) {
            ConsulServiceRegistry consulServiceRegistry = (ConsulServiceRegistry) bean;

            return new ConsulServiceRegistryDecorator(consulServiceRegistry, applicationContext);
        } else if (bean instanceof ConsulDiscoveryProperties) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            ConsulDiscoveryProperties consulDiscoveryProperties = (ConsulDiscoveryProperties) bean;
            consulDiscoveryProperties.setPreferIpAddress(true);

            List<String> tags = consulDiscoveryProperties.getTags();
            if (!containsKey(tags, DiscoveryConstant.GROUP + "=")) {
                tags.add(DiscoveryConstant.GROUP + "=" + DiscoveryConstant.DEFAULT);
            }
            if (!containsKey(tags, DiscoveryConstant.VERSION + "=")) {
                tags.add(DiscoveryConstant.VERSION + "=" + DiscoveryConstant.DEFAULT);
            }
            if (!containsKey(tags, DiscoveryConstant.REGION + "=")) {
                tags.add(DiscoveryConstant.REGION + "=" + DiscoveryConstant.DEFAULT);
            }
            tags.add(DiscoveryConstant.SPRING_APPLICATION_NAME + "=" + PluginContextAware.getApplicationName(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_TYPE + "=" + PluginContextAware.getApplicationType(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_PLUGIN + "=" + ConsulConstant.CONSUL_TYPE);
            tags.add(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_VERSION + "=" + DiscoveryConstant.DISCOVERY_VERSION);
            tags.add(DiscoveryConstant.SPRING_APPLICATION_REGISTER_CONTROL_ENABLED + "=" + PluginContextAware.isRegisterControlEnabled(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED + "=" + PluginContextAware.isDiscoveryControlEnabled(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_CONFIG_REST_CONTROL_ENABLED + "=" + PluginContextAware.isConfigRestControlEnabled(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_GROUP_KEY + "=" + PluginContextAware.getGroupKey(environment));
            tags.add(DiscoveryConstant.SPRING_APPLICATION_CONTEXT_PATH + "=" + PluginContextAware.getContextPath(environment));

            MetadataUtil.filter(tags);

            String gitVersion = getGitVersion(applicationContext);
            if (StringUtils.isNotEmpty(gitVersion)) {
                tags.set(MetadataUtil.getIndex(tags, DiscoveryConstant.VERSION), DiscoveryConstant.VERSION + "=" + gitVersion);
            }

            return bean;
        } else {
            return bean;
        }
    }

    private boolean containsKey(List<String> tags, String key) {
        for (String tag : tags) {
            if (tag.contains(key)) {
                return true;
            }
        }

        return false;
    }
}