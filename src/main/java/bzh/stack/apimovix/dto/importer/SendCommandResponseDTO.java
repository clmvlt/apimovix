package bzh.stack.apimovix.dto.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import lombok.Data;

@Data
public class SendCommandResponseDTO {

    private String status;
    private String message;
    private UUID id_command;
    List<PackageDTO> packages = new ArrayList<>();
}
