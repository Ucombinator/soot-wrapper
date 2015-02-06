package org.ucombinator;

import java.util.Collections;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;
import soot.util.Chain;
import soot.G;
import soot.shimple.Shimple;

/** A functional API around Soot for simple uses. Soot relies on lots of global state,
 *  so this class uses locking to attempt thread safety. Note that it is not thread safe
 *  in combination with any other code using the Soot API and will destroy any Soot state
 *  present before execution. */
public class SootWrapper {
    public static abstract class Source {
        /** Configures Soot options. Specific sources override this method to configure soot
         *  to read classes from that source or override these defaults. */
        public void configure() {
            Options.v().set_verbose(false);

            // we need to link instructions to source line for display
            Options.v().set_keep_line_number(true);

            // Called methods without jar files or source are considered phantom
            Options.v().set_allow_phantom_refs(true);
        }

        /** Runs soot with the Options currently configured plus those produced by configure(). */
        private void run() {
            configure();

            // Compute dependent options
            Main.v().autoSetOptions();

            // Load classes according to the configured options
            Scene.v().loadNecessaryClasses();

            // Run transformations and analyses according to the configured options.
            // Transformation could include jimple, shimple, and CFG generation
            PackManager.v().runPacks();
        }

        /** Implementation common to functions for accessing the AST. Assumes the output format
         *  is set before this method is called. */
        private Chain<SootClass> getAST() {
            synchronized(Source.class) {
                run();

                Chain<SootClass> result = Scene.v().getApplicationClasses();

                // Make sure we don't leave soot state around
                G.reset();

                return result;
            }
        }

        /** Loads classes and produces Shimple. Note that this function
         *  resets soot, so it will destroy any existing soot state and not respect
         *  soot options set before it is run. */
        public Chain<SootClass> getShimple() {
            synchronized(Source.class) {
                // Make sure previous state doesn't impact this run.
                G.reset();

                Options.v().set_output_format(Options.output_format_shimple);

                return getAST();
            }
        }

        /** Loads classes and produces Jimple. Note that this function
         *  resets soot, so it will destroy any existing soot state and not respect
         *  soot options set before it is run. */
        public Chain<SootClass> getJimple() {
            synchronized(Source.class) {
                // Make sure previous state doesn't impact this run.
                G.reset();

                Options.v().set_output_format(Options.output_format_jimple);

                return getAST();
            }
        }

        /** Runs call graph analysis without a specific entry point; all code is
         *  considered reachable and included in the result. Note that this function
         *  resets soot, so it will destroy any existing soot state and not respect
         *  soot options set before it is run. */
        public CallGraph getCallGraph() {
            synchronized(Source.class) {
                // Make sure previous state doesn't impact this run.
                G.reset();

                // Turn on soot's whole program analysis phase, which includes call graph generation.
                Options.v().set_whole_program(true);

                // Don't develop the call graph from a specific entry point; consider
                // the whole program reachable.
                Options.v().setPhaseOption("cg", "all-reachable:true");

                run();

                CallGraph result = Scene.v().getCallGraph();

                // Make sure we don't leave soot state around
                G.reset();

                return result;
            }
        }
    }

    private static class FromAPK extends Source {
        private String apk;
        private String androidJars;

        public FromAPK(String apk, String androidJars) {
            this.apk = apk;
            this.androidJars = androidJars;
        }

        public void configure() {
            super.configure();

            // Prefer definitions from the apk over sources or class files
            Options.v().set_src_prec(Options.src_prec_apk);

            Options.v().set_android_jars(androidJars);

            String classPath = Scene.v().getAndroidJarPath(androidJars, apk);

            Options.v().set_process_dir(Collections.singletonList(apk));
            Options.v().set_soot_classpath(classPath);
        }
    }

    private static class FromDir extends Source {
        private String classesDir;
        private String classPath;

        public FromDir(String classesDir, String classPath) {
            this.classesDir = classesDir;
            this.classPath = classPath;
        }

        public void configure() {
            super.configure();

            // Include the default classpath, which should include the Java SDK rt.jar.
            Options.v().set_prepend_classpath(true);

            Options.v().set_process_dir(Collections.singletonList(classesDir));

            // Include the classesDir on the class path.
            Options.v().set_soot_classpath(classesDir + ":" + classPath);
        }
    }

    private static class FromClasses extends FromDir {
        public FromClasses(String classesDir, String classPath) {
            super(classesDir, classPath);
        }

        public void configure() {
            super.configure();

            // Prefer definitions from class files over source files
            Options.v().set_src_prec(Options.src_prec_class);
        }
    }

    private static class FromSource extends FromDir {
        public FromSource(String sourceDir, String classPath) {
            super(sourceDir, classPath);
        }

        public void configure() {
            super.configure();

            // Prefer definitions from Java source files over class files.
            Options.v().set_src_prec(Options.src_prec_java);
        }
    }

    /** Load classes from an APK and a matching android platform SDK located
     *  in the folder referenced by androidJars. Operate on classes in the APK. */
    public static Source fromApk(String apk, String androidJars) {
        return new FromAPK(apk, androidJars);
    }

    /** Operate on Java source files in the sourcesDir and load dependencies
     *  from the classPath provided. */
    public static Source fromSource(String sourcesDir, String classPath) {
        return new FromSource(sourcesDir, classPath);
    }

    /** Operate on Java class files in the classesDir and load dependencies
     *  from the classPath provided. */
    public static Source fromClasses(String classesDir, String classPath) {
        return new FromClasses(classesDir, classPath);
    }
}
