package propensi.smail.dto;

import java.util.List;

import lombok.*;
import propensi.smail.model.FieldData;

@Getter
@Setter
public class RequestAndFieldDataDTO {

    private String kategori;
    private String jenisSurat;
    private List<String> bentukSurat;
    private String bahasa;
    private String keperluan;

    private List<FieldData> listFieldData;

}
