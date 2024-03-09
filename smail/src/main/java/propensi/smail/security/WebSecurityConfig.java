package propensi.smail.security;

import java.util.*;
import java.io.IOException;
import java.security.Permission;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

import propensi.smail.model.user.*;
import propensi.smail.repository.PenggunaDb;
import propensi.smail.service.AuthService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    AuthService authService;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/profile").hasAnyAuthority("ADMIN", "STAF", "DOSEN", "MAHASISWA", "PENGURUS")
                    .requestMatchers(HttpMethod.POST, "/surat-masuk/upload").hasAuthority("ADMIN") // Izinkan akses POST untuk testing
                    .requestMatchers("/surat-masuk/all", "/surat-masuk/detail/*", "/surat-masuk/download/*").hasAnyAuthority("PENGURUS", "ADMIN")
                    .requestMatchers("/surat-masuk/**").hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/template/new-template", "/template/update/*").hasAuthority("ADMIN") // Izinkan akses POST untuk testing
                    .requestMatchers("/template/**").hasAuthority("ADMIN")

                    // .requestMatchers("/staf").hasAnyAuthority("STAF")
                    // .requestMatchers("/admin").hasAnyAuthority("ADMIN")
                    .anyRequest().authenticated())
                
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(authoritiesMapper()))
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                                System.out.println("SEBELUMMM CASTING");

                                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                                String email = oidcUser.getEmail();
                                System.out.println("SESUDAH CASTING");
    
                                if (authService.validatePengguna(email)) {
                                        response.sendRedirect("/");
                                } else {
                                        response.sendRedirect("/invalid-auth");
                                }
                        }
                    })
                    .authorizationEndpoint()
                    .authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository))
                )
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").permitAll();
        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        return (authorities) -> {
            String emailAttrName = "email";
            String email = authorities.stream()
                    .filter(OAuth2UserAuthority.class::isInstance)
                    .map(OAuth2UserAuthority.class::cast)
                    .filter(userAuthority -> userAuthority.getAttributes().containsKey(emailAttrName))
                    .map(userAuthority -> userAuthority.getAttributes().get(emailAttrName).toString())
                    .findFirst()
                    .orElse(null);

            if (email == null) return authorities;

            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            Optional<Pengguna> optionalUser = penggunaDb.findByEmail(email);

            if (!optionalUser.isPresent()) {
                return authorities;
            } else {
                Pengguna user = optionalUser.get();

                if (user instanceof Admin) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));
                } else if (user instanceof Pengurus) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("PENGURUS"));
                } else if (user instanceof Dosen) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("DOSEN"));
                } else if (user instanceof Staf) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("STAF"));
                } else if (user instanceof Mahasiswa) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("MAHASISWA"));
                }
            }
            return mappedAuthorities;

        };
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

}