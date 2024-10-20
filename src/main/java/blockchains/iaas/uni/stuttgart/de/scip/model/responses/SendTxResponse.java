package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class SendTxResponse extends AsyncScipResponse {
    private String timestamp;

    @Builder
    public SendTxResponse(@NonNull String correlationId, String timestamp) {
        super(correlationId);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SendTxResponse{" +
                "correlationId='" + getCorrelationId() + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
