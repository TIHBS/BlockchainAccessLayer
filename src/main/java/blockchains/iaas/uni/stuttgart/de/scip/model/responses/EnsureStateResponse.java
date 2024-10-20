package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import lombok.Builder;
import lombok.Getter;;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class EnsureStateResponse extends AsyncScipResponse {

    @Builder
    public EnsureStateResponse(@NonNull String correlationId) {
        super(correlationId);
    }
}
