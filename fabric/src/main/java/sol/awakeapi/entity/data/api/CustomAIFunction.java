package sol.awakeapi.entity.data.api;

import sol.awakeapi.entity.data.api.interfaces.AIFunction;
import sol.awakeapi.entity.data.api.interfaces.AIFunctionBase;
import sol.awakeapi.util.AIFunctionParams;
import java.util.function.Function;

public class CustomAIFunction implements AIFunctionBase {
    private final String name;
    private final AIFunction function;
    private final String description;
    private final Function<String, Object> extensionHandler;
    private final Function<Object, Boolean> accessControl;

    public CustomAIFunction(String name, AIFunction function, String description, Function<String, Object> extensionHandler, Function<Object, Boolean> accessControl) {
        this.name = name;
        this.function = function;
        this.description = description;
        this.extensionHandler = extensionHandler;
        this.accessControl = accessControl;
    }

    @Override
    public void run(AIFunctionParams params) {
        function.run(params);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean hasAccess(String mobName) {
        return accessControl == null || accessControl.apply(mobName);
    }

    public String getName() {
        return name;
    }

    public Object handleFunctionExtension(String extension) {
        return extensionHandler.apply(extension);
    }
}
