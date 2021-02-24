package science.aist.neo4j;

import javassist.*;
import javassist.bytecode.AccessFlag;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Preprocessor for adding the hidden fields required by neo4J namespacing
 * @author Oliver Krauss
 * @since 1.0
 */
@Mojo(name = "namespace", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class Neo4JNamespacePreprocessorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Field that the Neo4J Syncing will happen in TODO #28 merge with ClassInfo field
     */
    public static final String NEO4J_SYNC_FIELD = "_Neo4JSync";

    /**
     * Field that the Neo4J Syncing will happen in TODO #28 merge with ClassInfo field
     */
    public static final String Neo4J_RELATION_FIELD = "_Neo4JRelationships";

    /**
     * Field that the labels will be stored in TODO #28 merge with ClassInfo field
     */
    public static final String NEO4J_LABELS_FIELD = "_Neo4JLabels";

    /**
     * Field that the extensions will be stored in TODO #28 merge with ClassInfo field
     * Extensions are a key value pair (key == extension identifier, value == content of extension)
     */
    public static final String NEO4J_EXTENSIONS_FIELD = "_Neo4JExtensions";

    /**
     * Field that identities of already saved complex relationships will be stored in. TODO #28 merge with ClassInfo field
     * Is a hash map of {@literal <KEY,RELATIONSHIP_ID>}
     */
    public static final String NEO4J_MAP_RELATIONSHIPS_FIELD = "_Neo4JMapRelationshipsField";

    /**
     * Constant to create a new Java {@link HashMap}
     */
    public static final String NEW_JAVA_UTIL_HASH_MAP = "new java.util.HashMap();";

    /**
     * Logger for logging to mvn commandline
     */
    private Log logger = this.getLog();

    /**
     * Comma separated List of output directories for modifying classes
     */
    @Parameter(defaultValue = "${project.build.outputDirectory},${project.build.testOutputDirectory}", property = "outputDir", required = true)
    private String outputDirectories;

    /**
     * Comma separated list of all java packages that contain namespace files
     */
    @Parameter(property = "packages", required = true)
    private String packageList;

    private ClassPool classPool = new ClassPool(ClassPool.getDefault());


    public void execute() {
        if (packageList == null || packageList.isEmpty()) {
            // without any packageList there is naught to do
            return;
        }

        try {
            project.getRuntimeClasspathElements().forEach(this::appendClassPool);
        } catch (DependencyResolutionRequiredException e) {
            logger.error(e);
            // not sure if an exception should be thrown here, probably not?
        }

        logger.info("Output directories: " + outputDirectories);
        logger.info("Packages: " + packageList);

        List<String> outputDirectory = Arrays.asList(outputDirectories.split(","));
        List<String> packages = Arrays.asList(packageList.split(","));


        // Collect all classes
        Map<String, File> classes = new HashMap<>();

        outputDirectory.forEach(x -> {
            listf(x, x, classes);
            try {
                // Configure the classpool to also look into our output directories
                classPool.appendClassPath(x);
            } catch (NotFoundException e) {
                logger.error(e);
            }
        });

        // move through every class
        for (Map.Entry<String, File> file : classes.entrySet()) {
            // skip unknown packages and package info classes
            if (packages.isEmpty() || packages.stream().anyMatch(x -> file.getKey().startsWith(x)) && !file.getKey().endsWith("package-info")) {
                try {
                    // identify classfile
                    CtClass ctClass = classPool.get(file.getKey());
                    if (Modifier.isAbstract(ctClass.getModifiers()) || ctClass.isInterface()) {
                        continue;
                    }

                    // prevent duplicate creation if someone is too lazy to clean their project
                    try {
                        if (ctClass.getDeclaredField(NEO4J_SYNC_FIELD) != null) {
                            logger.info("Skipping " + ctClass.getName() + " because it was already processed");
                            continue;
                        }
                    } catch (javassist.NotFoundException e) {
                        // we don't care. The default impl of Javassist throws instead of returning null. In case the field doesn't exist we must process
                    }

                    // add sync field
                    CtField f = new CtField(classPool.get(Map.class.getName()), NEO4J_SYNC_FIELD, ctClass);
                    f.setModifiers(AccessFlag.SYNTHETIC + Modifier.PUBLIC);
                    ctClass.addField(f, CtField.Initializer.byExpr(NEW_JAVA_UTIL_HASH_MAP));

                    // add relationships map
                    CtField fr = new CtField(classPool.get(Map.class.getName()), Neo4J_RELATION_FIELD, ctClass);
                    fr.setModifiers(AccessFlag.SYNTHETIC + Modifier.PUBLIC);
                    ctClass.addField(fr, CtField.Initializer.byExpr(NEW_JAVA_UTIL_HASH_MAP));

                    // add labels field
                    CtField fl = new CtField(classPool.get(List.class.getName()), NEO4J_LABELS_FIELD, ctClass);
                    fl.setModifiers(AccessFlag.SYNTHETIC + Modifier.PUBLIC);
                    ctClass.addField(fl);

                    // add extensions map
                    CtField fe = new CtField(classPool.get(Map.class.getName()), NEO4J_EXTENSIONS_FIELD, ctClass);
                    fe.setModifiers(AccessFlag.SYNTHETIC + Modifier.PUBLIC);
                    ctClass.addField(fe, CtField.Initializer.byExpr(NEW_JAVA_UTIL_HASH_MAP));

                    // add map relationship sync field
                    CtField mRs = new CtField(classPool.get(Map.class.getName()), NEO4J_MAP_RELATIONSHIPS_FIELD, ctClass);
                    mRs.setModifiers(AccessFlag.SYNTHETIC + Modifier.PUBLIC);
                    ctClass.addField(mRs, CtField.Initializer.byExpr(NEW_JAVA_UTIL_HASH_MAP));

                    // override the class file
                    DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file.getValue()));
                    ctClass.toBytecode(dataOutputStream);
                    dataOutputStream.close();
                    logger.info("Successfully modified " + ctClass.getName());

                } catch (NotFoundException | IOException | CannotCompileException e) {
                    logger.error(e);
                }
            }
        }
    }

    private void appendClassPool(String x) {
        try {
            classPool.appendClassPath(x);
        } catch (NotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }


    /**
     * Helper function loading all classes into a file map
     *
     * @param delimiter     what the root folder is
     * @param directoryName folder to load
     * @param files         map to load files into
     */
    public void listf(String delimiter, String directoryName, Map<String, File> files) {
        File directory = new File(directoryName);
        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if (fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    final String className = file.getAbsolutePath().substring(delimiter.length() + 1, file.getAbsolutePath().length() - ".clazz".length()).replace(File.separatorChar, '.');
                    files.put(className, file);
                } else if (file.isDirectory()) {
                    listf(delimiter, file.getAbsolutePath(), files);
                }
            }
    }
}