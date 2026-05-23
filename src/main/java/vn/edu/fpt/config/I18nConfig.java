package vn.edu.fpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import java.util.Arrays;
import java.util.Locale;

@Configuration
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        // Mặc định nếu không ai yêu cầu gì thì dùng tiếng Anh
        resolver.setDefaultLocale(Locale.ENGLISH);
        // Hỗ trợ tiếng Anh và tiếng Việt
        resolver.setSupportedLocales(Arrays.asList(Locale.ENGLISH, new Locale("vi")));
        return resolver;
    }
}