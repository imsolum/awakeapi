package sol.awakeapi.api.api_data;

public class AIParams {

    final String key;
    final String model;
    final String endpoint;
    final boolean isOoba;

    public AIParams(String key, String model, String endpoint) {
        this.key = key;
        this.model = model;
        this.endpoint = endpoint;
        this.isOoba = false;
    }

    public AIParams(String key, String model, String endpoint, boolean isOoba) {
        this.key = key;
        this.model = model;
        this.endpoint = endpoint;
        this.isOoba = isOoba;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKey() {
        return key;
    }

    public String getModel() {
        return model;
    }

    public boolean isOoba() {
        return isOoba;
    }

    public boolean isValid() {
        return endpoint != null;
    }
}