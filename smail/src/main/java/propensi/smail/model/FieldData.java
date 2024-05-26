package propensi.smail.model;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "field_data")

public class FieldData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "key", nullable = false)
    private String key;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    /* RELATIONSHIPS */
    @ManyToOne
    @JoinColumn(name = "request_surat")
    private RequestSurat requestSurat;

    @ManyToOne
    @JoinColumn(name = "request_template")
    private RequestTemplate requestTemplate;

}
