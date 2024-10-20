package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
abstract public class AsyncScipResponse {
    @NonNull private String correlationId;

    @Override
    public String toString() {
        return "AsyncScipResponse{" +
                "correlationId='" + correlationId + '\'' +
                '}';
    }
}
