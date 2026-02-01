package com.house.agents.security;
import com.house.agents.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 6/14/2023 8:24 PM
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //关闭csrf
                .csrf(csrf -> csrf.disable())
                //开启跨域以便前端调用接口
                .cors(cors -> cors.and())
                .authorizeHttpRequests(auth -> auth
                        //指定某些接口不需要通过验证即可访问,登录接口不需要认证
                        .requestMatchers("/admin/user/login").permitAll()
                        .requestMatchers("/admin/house/shareHouse").permitAll()
                        .requestMatchers("/admin/user/wxLogin").permitAll() // 微信小程序的登录的入口
                        .requestMatchers("/admin/house/unLogin/houseInfo").permitAll() // 未登录的时候可以允许用户获取前10条数据
                        .requestMatchers("/admin/house/subway").permitAll()
                        .requestMatchers("/admin/house/getHouseInfo").permitAll()
                        //健康检查接口
                        .requestMatchers("/monitor/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        //Swagger UI (SpringDoc OpenAPI)
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/doc.html").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/wx/**").permitAll()
                        //这里的意思是其他的所有的接口需要认证才能访问
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(new TokenAuthenticationFilter(redisTemplate), TokenLoginFilter.class)
                .addFilter(new TokenLoginFilter(authenticationManager(), redisTemplate));
        //禁用session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
