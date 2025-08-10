// package com.mgaye.banking_application.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.thymeleaf.TemplateEngine;
// import org.thymeleaf.spring6.SpringTemplateEngine;
// import org.thymeleaf.templatemode.TemplateMode;
// import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

// @Configuration
// public class ThymeleafConfig {

//     @Bean
//     public TemplateEngine templateEngine() {
//         SpringTemplateEngine engine = new SpringTemplateEngine();
//         ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
//         resolver.setPrefix("templates/"); // put your .html files in src/main/resources/templates
//         resolver.setSuffix(".html");
//         resolver.setTemplateMode(TemplateMode.HTML);
//         resolver.setCharacterEncoding("UTF-8");
//         engine.setTemplateResolver(resolver);
//         return engine;
//     }
// }

package com.mgaye.banking_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfig {
    @Bean
    public TemplateEngine myTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
