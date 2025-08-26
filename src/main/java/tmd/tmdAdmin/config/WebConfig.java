package tmd.tmdAdmin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tmd.tmdAdmin.data.repositories.UserRepository;
import tmd.tmdAdmin.security.LoginSuccessHandler;
import tmd.tmdAdmin.services.UserDetailsServiceImp;
import tmd.tmdAdmin.storage.FileStorageProperties;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableConfigurationProperties({FileStorageProperties.class})
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;
    private final DataSource dataSource;
    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImp(userRepository);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setPasswordEncoder(passwordEncoder());
        auth.setUserDetailsService(userDetailsService());
        return auth;
    }

    // New Bean for Remember Me Token Repository
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // If you want Spring Security to create the table automatically on startup (dev only, not recommended for prod)
//         tokenRepository.setCreateTableOnStartup(true); // Remove or set to false for production
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(configurer ->
                        configurer
                                // Allow access to static resources like CSS, JS, images for all
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/sliders/**", "/videos/**","/login").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true) // Redirect to home page on successful login
                        .successHandler(loginSuccessHandler)
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout") // Redirect to login page with logout success message
                        .deleteCookies("JSESSIONID", "remember-me") // Delete session cookie and remember-me cookie
                )
                .rememberMe(rememberMe -> rememberMe
                        .tokenRepository(persistentTokenRepository()) // Use the new token repository
                        .userDetailsService(userDetailsService()) // Essential for remember-me to work
                        .key("9e2c6a7f4b3d8c5a1f0e9d4a6c7b2f8d3e6a9c0b5d7f4e2c8a1b3d6f9c7e4a2") // IMPORTANT: Change this to a strong, unique secret key!
                        .tokenValiditySeconds(60 * 60 * 24 * 30) // 30 days validity for remember-me token
                        .rememberMeParameter("remember-me") // This name must match the checkbox name in your login form
                );

        // Reconsider disabling CSRF in a full web application.
        // If you enable it, ensure your login form includes the CSRF token:
        // <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
//        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/sliders/**")
//                .addResourceLocations("file:///C:/sliders/");
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:///C:/wood-images/");
//        registry.addResourceHandler("/videos/**")
//                .addResourceLocations("file:///C:/videos/");

        registry.addResourceHandler("/gallery/**")
                .addResourceLocations("file:../../../../Media/gallery/");
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:../../Media/videos/");
        registry.addResourceHandler("/slider/**")
                .addResourceLocations("file:../../Media/slider/");
        registry.addResourceHandler("/Products/**")
                .addResourceLocations("file:../../Media/Products/");

        // Also ensure default static resources are handled (Bootstrap, custom CSS/JS in src/main/resources/static)
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
    }
}
