
// src/main/java/com/example/grocery/config/DynamicPromotionBeanRegistrar.java
package com.example.grocery.config;

import com.example.grocery.api.dto.PromotionFileDTO;
import com.example.grocery.api.dto.PromotionRuleDTO;
import com.example.grocery.domain.ItemType;
import com.example.grocery.service.promo.PromotionCtor;
import com.example.grocery.service.promo.PromotionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.*;

@Configuration
public class DynamicPromotionBeanRegistrar
        implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            // 1) Load JSON (DTOs in com.example.grocery.api.dto)
            PromotionFileDTO file = readPromotionsFile();
            List<PromotionRuleDTO> rules = Optional.ofNullable(file.getPromotions()).orElseGet(Collections::emptyList);
            if (rules.isEmpty()) {
                return; // nothing to register
            }

            // 2) Discover concrete strategy classes annotated with @PromotionType
            Map<String, Class<?>> ruleKeyToClass =
                    scanPromotionClasses("com.example.grocery.service.promo");

            // 3) Register one bean per rule
            for (PromotionRuleDTO rule : rules) {
                String key = normalize(rule.getRuleType());
                Class<?> clazz = ruleKeyToClass.get(key);
                if (clazz == null) {
                    throw new IllegalStateException("No @PromotionType strategy found for ruleType: " + rule.getRuleType());
                }

                Constructor<?> ctor = findPromotionConstructor(clazz);
                Object[] args = buildConstructorArgs(ctor, rule);

                String beanName = "promo." + key + "." +
                        (rule.getItemType() == null ? "ALL" : rule.getItemType().name()) +
                        "." + UUID.randomUUID();

                // ✅ Explicit bean class + instance supplier (fixes "No bean class specified")
                RootBeanDefinition rbd = new RootBeanDefinition(clazz);
                // Optional target type hint:
                rbd.setTargetType(clazz);
                rbd.setInstanceSupplier(() -> {
                    try {
                        return ctor.newInstance(args);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to instantiate " + clazz.getSimpleName(), e);
                    }
                });

                registry.registerBeanDefinition(beanName, rbd);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Dynamic promotion registration failed", e);
        }
    }

    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) { /* no-op */ }

    // --- helpers ---

    private PromotionFileDTO readPromotionsFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Your current implementation expects only 'promotions.json'
        Resource resource = resourceLoader.getResource("classpath:promotions.json");
        if (resource.exists()) {
            return mapper.readValue(resource.getInputStream(), PromotionFileDTO.class);
        }
        throw new IllegalStateException("'promotions.json' not found on classpath");
    }

    private Map<String, Class<?>> scanPromotionClasses(String basePackage) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PromotionType.class));
        Map<String, Class<?>> map = new HashMap<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());
            if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;
            PromotionType ann = clazz.getAnnotation(PromotionType.class);
            map.put(normalize(ann.value()), clazz);
        }
        return map;
    }

    private Constructor<?> findPromotionConstructor(Class<?> clazz) {
        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.isAnnotationPresent(PromotionCtor.class)) {
                return c;
            }
        }
        throw new IllegalStateException("No @PromotionCtor constructor found in " + clazz.getName());
    }

    private Object[] buildConstructorArgs(Constructor<?> ctor, PromotionRuleDTO rule) {
        PromotionCtor meta = ctor.getAnnotation(PromotionCtor.class);
        String[] keys = meta.keys();

        Class<?>[] types = ctor.getParameterTypes();
        List<Object> args = new ArrayList<>(types.length);
        int idx = 0;

        // First parameter may be ItemType (item-scoped strategy)
        if (types.length > 0 && types[0] == ItemType.class) {
            if (rule.getItemType() == null) {
                throw new IllegalArgumentException("Missing 'itemType' for ruleType: " + rule.getRuleType());
            }
            args.add(rule.getItemType());
            idx = 1;
        }

        // Remaining parameters from params using keys (declared order)
        Map<String, String> p = rule.getParams();
        for (String k : keys) {
            Class<?> t = types[idx++];
            String raw = require(p, k, rule.getRuleType());
            Object value =
                    t == int.class || t == Integer.class ? Integer.parseInt(raw) :
                            t == long.class || t == Long.class ? Long.parseLong(raw) :
                                    t == BigDecimal.class ? new BigDecimal(raw) :
                                            t == String.class ? raw :
                                                    unsupported(t, k);
            args.add(value);
        }

        if (idx != types.length) {
            throw new IllegalStateException("Constructor arity mismatch for " + ctor.getDeclaringClass().getName());
        }
        return args.toArray();
    }

    private static String require(Map<String, String> p, String key, String rule) {
        String v = (p == null) ? null : p.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing param '" + key + "' for ruleType: " + rule);
        }
        return v;
    }

    private static Object unsupported(Class<?> t, String k) {
        throw new IllegalArgumentException("Unsupported constructor param type: " + t.getName() + " for key '" + k + "'");
    }

    private static String normalize(String s) {
        return (s == null ? "" : s.trim().toUpperCase());
    }
}
