package propensi.smail.model.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "mahasiswa")

public class Mahasiswa extends Pengguna {

    @NotNull
    @Column(name = "jurusan", nullable = false)
    private String jurusan;

    @NotNull
    @Column(name = "semester", nullable = false)
    private String semester;

    @NotNull
    @Column(name = "ipk", nullable = false)
    private float ipk;
    
}
