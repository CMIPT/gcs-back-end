package edu.cmipt.gcs.config;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import java.util.Arrays;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {
  @Bean
  @Profile(ApplicationConstant.DEV_PROFILE)
  FilterRegistrationBean<CorsFilter> corsFilterDev() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("*");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    addExposedHeader(config);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(Ordered.LOWEST_PRECEDENCE);
    return bean;
  }

  private void addExposedHeader(CorsConfiguration config) {
    Arrays.stream(HeaderParameter.class.getFields())
        .forEach(
            field -> {
              try {
                if (field.getType() == String.class && field.canAccess(null)) {
                  config.addExposedHeader((String) field.get(null));
                }
              } catch (IllegalAccessException e) {
                // ignore
              }
            });
  }
}
