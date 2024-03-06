package propensi.smail.security;


import java.io.IOException;
import java.security.Permission;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
// import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import propensi.smail.model.user.*;
import propensi.smail.repository.PenggunaDb;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import propensi.smail.service.AuthService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    PenggunaDb penggunaDb;

    @Autowired
    AuthService authService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         System.out.println("Configuring http filterChain");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/secured").hasAnyAuthority("ADMIN","STAF","DOSEN","MAHASISWA","PENGURUS")
                        .requestMatchers("/staf").hasAnyAuthority("STAF")
                        .requestMatchers("/admin").hasAnyAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login()
                        .userInfoEndpoint()
                        .userAuthoritiesMapper(authoritiesMapper())
                        .and()
                        .successHandler(new AuthenticationSuccessHandler() {
                                @Override
                                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication auth) throws IOException, ServletException {
                                System.out.println("SEBELUMMM CASTING");
                                // DefaultOidcUser oauthUser = (DefaultOidcUser) auth.getPrincipal();
                                OidcUser oauthUser = (OidcUser) auth.getPrincipal();
                                System.out.println("SESUDAH CASTING");

                                String email = oauthUser.getEmail();

                                        if (authService.validatePengguna(email)) {
                                                response.sendRedirect("/");
                                        } else {
                                                response.sendRedirect("/invalid-auth");
                                        }

                                }
                        });
//                .and()
//                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                .logoutSuccessUrl("/").permitAll();

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

//     @Bean
//     public SpringSecurityDialect springSecurityDialect() {
//         return new SpringSecurityDialect();
//     }


}


            
