package science.aist.neo4j;

import science.aist.neo4j.annotation.Transaction;
import javassist.*;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>Mojo that preprocesses </p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
@Mojo(name = "transaction-service", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class Neo4jServiceTransactionPreprocessorMojo extends AbstractMojo {
    /**
     * Comma separated list of all java packages that contain namespace files
     */
    @Parameter(required = true)
    private String packageList;

    private List<String> packages;

    private ClassPool classPool = new ClassPool(ClassPool.getDefault());


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Logger for logging to mvn commandline
     */
    private Log logger = this.getLog();

    /**
     * Comma separated List of output directories for modifying classes
     */
    @Parameter(defaultValue = "${project.build.outputDirectory},${project.build.testOutputDirectory}", property = "outputDir", required = true)
    private String outputDirectories;

    @Override
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

        List<String> outputDirectory = Arrays.asList(outputDirectories.split(","));
        packages = Arrays.asList(packageList.split(","));

        logger.info("Output directories: " + outputDirectory);
        logger.info("Packages: " + packages);

        // Collect all classes
        Map<String, File> classes = new HashMap<>();

        outputDirectory.forEach(this::appendClassPool);
        outputDirectory.forEach(x -> listf(x, x, classes));

        classes.forEach((name, file) -> {
            CtClass clazz = getByname(name);
            if (!containsPackage(clazz)) return;

            boolean write = false;
            for (CtMethod method : clazz.getDeclaredMethods()) {
                if (!method.hasAnnotation(Transaction.class)) continue;
                write = true;
                Transaction annotation = getTransactionAnnotation(method);
                CtMethod newMethod = copyAndAddMethod(clazz, method);
                String parameters = createParameterCall(newMethod);
                String returnTypeName = getReturnType(newMethod).getName();

                setMethodBody(method, "{" +
                    "org.neo4j.driver.Transaction transaction = null;" +
                    "try {" +
                    "  transaction = " + annotation.transactionManager() + ".beginTransaction(org.neo4j.driver.AccessMode." + annotation.mode().name() + ");" +
                    (returnTypeName.equals("void")
                        ? newMethod.getName() + "(" + parameters + ");" + "transaction.commit();"
                        : returnTypeName + " result = " + newMethod.getName() + "(" + parameters + ");" + "transaction.commit();" + "return result;") +
                    "} catch (Throwable e) {" +
                    "  if (transaction != null) transaction.rollback(); " +
                    "  throw e; " +
                    "} finally {" +
                    "  if (transaction != null) transaction.close();" +
                    "}" +
                    "}");

            }

            if (write) writeClass(file, clazz);
        });
    }

    private void appendClassPool(String x) {
        try {
            classPool.appendClassPath(x);
        } catch (NotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private void setMethodBody(CtMethod method, String body) {
        try {
            method.setBody(body);
        } catch (CannotCompileException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private CtMethod copyAndAddMethod(CtClass to, CtMethod from) {
        CtMethod newMethod;
        try {
            newMethod = CtNewMethod.copy(from, from.getName() + "Body", to, null);
            to.addMethod(newMethod);
        } catch (CannotCompileException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }

        return newMethod;
    }

    private CtClass getReturnType(CtMethod method) {
        try {
            return method.getReturnType();
        } catch (NotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private String createParameterCall(CtMethod method) {
        try {
            return IntStream
                .rangeClosed(1, method.getParameterTypes().length)
                .mapToObj(i -> "$" + i)
                .collect(Collectors.joining(", "));
        } catch (NotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private Transaction getTransactionAnnotation(CtMethod method) {
        try {
            return (Transaction) method.getAnnotation(Transaction.class);
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private CtClass getByname(String name) {
        try {
            return classPool.get(name);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeClass(File file, CtClass clazz) {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
            clazz.getClassFile().write(dataOutputStream);
            logger.info("Successfully modified " + clazz.getName());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean containsPackage(CtClass ctClass) {
        return packages.stream()
            .anyMatch(ctClass.getName()::startsWith);
    }

    /**
     * Helper function loading all classes into a file map
     *
     * @param delimiter     what the root folder is
     * @param directoryName folder to load
     * @param files         map to load files into
     */
    private void listf(String delimiter, String directoryName, Map<String, File> files) {
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
