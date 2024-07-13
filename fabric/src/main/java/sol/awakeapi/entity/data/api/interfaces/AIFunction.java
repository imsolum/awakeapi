package sol.awakeapi.entity.data.api.interfaces;

import sol.awakeapi.util.AIFunctionParams;

@FunctionalInterface
public interface AIFunction {
    void run(AIFunctionParams params);
}
