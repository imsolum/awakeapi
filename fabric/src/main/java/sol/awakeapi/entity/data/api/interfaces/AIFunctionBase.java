package sol.awakeapi.entity.data.api.interfaces;

import sol.awakeapi.util.AIFunctionParams;

public interface AIFunctionBase {
    void run(AIFunctionParams params);
    String getDescription();
    Object handleFunctionExtension(String extension);
}
