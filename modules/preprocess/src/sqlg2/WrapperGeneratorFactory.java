package sqlg2;

import java.io.File;

/**
 * Factory for wrapper generators
 */
public interface WrapperGeneratorFactory {

    /**
     * Returns list of wrapper generators.
     * @param pack package of main class ("" for no package)
     * @param className main class name (without package)
     * @param separate subpackage name to put generated files into
     * @param dir directory corresponding to package <code>pack</code> (so to get directory corresponding
     * to newly generated wrapper you should go to <code>separate</code> subdir)
     * @return wrapper generator array
     */
    WrapperGenerator[] newGenerators(String pack, String className, String separate, File dir);
}
