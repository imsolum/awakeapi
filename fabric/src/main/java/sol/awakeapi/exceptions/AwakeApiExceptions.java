package sol.awakeapi.exceptions;

public class AwakeApiExceptions {
    public static class AwakeApiDuplicateLocalFunction extends Exception {
        public AwakeApiDuplicateLocalFunction(String message) {
            super(message);
        }
    }

    public static class AwakeApiDuplicateGlobalFunction extends Exception {
        public AwakeApiDuplicateGlobalFunction(String message) {
            super(message);
        }
    }
}
