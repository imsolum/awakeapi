package sol.awakeapi.entity.data.api;

import sol.awakeapi.entity.behaviour.AIBehaviours;
import sol.awakeapi.entity.data.api.interfaces.AIFunction;
import sol.awakeapi.entity.data.api.interfaces.AIFunctionBase;
import sol.awakeapi.util.AIFunctionParams;

public enum AIFunctions implements AIFunctionBase {
    NONE(AIBehaviours::none, "Do nothing. Usage: NONE");
    private final AIFunction function;
    private final String description;

    AIFunctions(AIFunction function, String description) {
        this.function = function;
        this.description = description;
    }

    public void run(AIFunctionParams params) {
        function.run(params);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Object handleFunctionExtension(String extension) {
        return null;
    }
}
