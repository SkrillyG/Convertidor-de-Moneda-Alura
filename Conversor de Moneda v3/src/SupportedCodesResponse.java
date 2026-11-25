import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SupportedCodesResponse {
    private String result;

    @SerializedName("supported_codes")
    private List<List<String>> supportedCodes;

    public String getResult() {
        return result;
    }

    public List<List<String>> getSupportedCodes() {
        return supportedCodes;
    }

    public Map<String, String> toMap() {
        Map<String, String> codes = new LinkedHashMap<>();
        if (supportedCodes != null) {
            for (List<String> entry : supportedCodes) {
                if (entry.size() >= 2) {
                    codes.put(entry.get(0), entry.get(1));
                }
            }
        }
        return codes;
    }
}
