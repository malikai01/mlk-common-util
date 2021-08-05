package com.mlk.util.resubmit.config;

import com.mlk.util.resubmit.aspect.ResubmitDataAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author malikai
 * @date 2021年06月18日 13:43
 */
@Configuration
public class ResubmitDataConfig {

    @Bean
    public ResubmitDataAspect resubmitAspect() {
        return new ResubmitDataAspect();
    }
}
