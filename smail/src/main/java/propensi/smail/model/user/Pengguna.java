package propensi.smail.model.user;

import lombok.*;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.experimental.SuperBuilder;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pengguna")
@Inheritance(strategy = InheritanceType.JOINED)

public class Pengguna implements UserDetails {
    
    @Id
    private String id;      // NPM - NIP 

    @Email
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "nama", nullable = false)
    private String nama;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this instanceof Admin) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        } else if (this instanceof Pengurus) {
            authorities.add(new SimpleGrantedAuthority("PENGURUS"));
        } else if (this instanceof Dosen) {
            authorities.add(new SimpleGrantedAuthority("DOSEN"));
        } else if (this instanceof Staf) {
            authorities.add(new SimpleGrantedAuthority("STAF"));
        } else if (this instanceof Mahasiswa) {
            authorities.add(new SimpleGrantedAuthority("MAHASISWA"));
        }

        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }
    
}
