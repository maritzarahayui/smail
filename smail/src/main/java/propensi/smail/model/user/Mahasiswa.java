package propensi.smail.model.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "mahasiswa")

public class Mahasiswa extends Pengguna {

    // @NotNull
    @Column(name = "jurusan")
    private String jurusan;

    // @NotNull
    @Column(name = "semester")
    private String semester;

    // @NotNull
    @Column(name = "ipk")
    private float ipk;
    
}
