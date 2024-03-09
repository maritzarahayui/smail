package propensi.smail.model.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
// @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "admin")

public class Admin extends Pengguna {
    
}
