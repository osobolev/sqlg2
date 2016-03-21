package sqlg2;

import java.io.File;

/**
 * Default implementation of {@link WrapperGeneratorFactory}.
 * Generates data access interface and RMI and local wrappers implementing it.
 */
public final class DefaultWrapperGeneratorFactory implements WrapperGeneratorFactory {

    public WrapperGenerator[] newGenerators(String pack, String className, String separate, File dir) {
        return new WrapperGenerator[] {
            new RemoteInterfaceGenerator(pack, className, separate, dir)
        };
    }
}
