package tmd.tmdAdmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tmd.tmdAdmin.services.UserDetailsServiceImp;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsServiceImp();
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){

        return  new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
      DaoAuthenticationProvider auth=new DaoAuthenticationProvider();
      auth.setPasswordEncoder(passwordEncoder());
      auth.setUserDetailsService(userDetailsService());
      return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(configurer ->
                configurer  .requestMatchers("/dashboard").hasAnyRole("ADMIN","SUPERADMIN")
                        .anyRequest().authenticated())
                .formLogin( form -> form
                        .loginPage("/loginForm")
                        .loginProcessingUrl("/authenticateTheUser")
                        .permitAll()
                        )
                .logout(logout -> logout.permitAll()
                      );
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Override
    public  void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/sliders/**")
                .addResourceLocations("file:///C:/sliders/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/wood-images/");
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:///C:/videos/");

    }
}
