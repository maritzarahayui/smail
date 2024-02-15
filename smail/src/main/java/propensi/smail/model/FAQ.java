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
@Table(name = "faq")

public class FAQ {
    
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @Column(name = "pertanyaan", nullable = false)
    private String pertanyaan;

    @NotNull
    @Column(name = "jawaban", nullable = false)
    private String jawaban;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "tanggal_post", nullable = false)
    private Date tanggalPost;

}
