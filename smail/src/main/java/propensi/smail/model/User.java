package propensi.smail.model;

import lombok.*;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "user_model")

public class User {
    
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Column(name = "nama", nullable = false)
    private String nama;

    @NotNull
    @Column(name = "no_induk", nullable = false)
    private Long noInduk;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @NotNull
    @Column(name = "request_surat", nullable = false)
    private RequestSurat requestSurat; 

    @NotNull
    @Column(name = "surat_masuk", nullable = false)
    private SuratMasuk suratMasuk; 
    
}