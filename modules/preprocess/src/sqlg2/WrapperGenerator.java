package sqlg2;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Wrapper generator interface.
 */
public interface WrapperGenerator {

    /**
     * Initialization.
     * @param tab tab string
     * @param cls main class for which wrapper is generated
     */
    void init(String encoding, String tab, Class<?> cls, String[] imports) throws IOException;

    /**
     * Method wrapper generation.
     * @param method method to wrap
     * @param methodHeader method header - its return type, name and parameters
     * @param returnType method return type
     * @param params parameter names
     */
    void addMethod(Method method, String javadoc, String methodHeader, String returnType, String[] params) throws IOException;

    /**
     * End of generation and closing of files.
     */
    void close();
}
