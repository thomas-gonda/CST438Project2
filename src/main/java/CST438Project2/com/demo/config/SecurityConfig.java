package CST438Project2.com.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/games", "/hello").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/games").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/games/*").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .httpBasic(httpBasic -> { });
        return http.build();
    }

    @Bean
    InMemoryUserDetailsManager users(
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.admin-username}") String adminUsername,
            @Value("${app.auth.admin-password}") String adminPassword,
            @Value("${app.auth.user-username}") String userUsername,
            @Value("${app.auth.user-password}") String userPassword) {
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        UserDetails user = User.withUsername(userUsername)
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
