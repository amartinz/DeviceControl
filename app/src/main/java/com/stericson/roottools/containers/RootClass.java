/*
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 *
 * The terms of each license can be found in the root directory of this project's repository as
 * well as at:
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.roottools.containers;

import org.namelessrom.devicecontrol.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* #ANNOTATIONS @SupportedAnnotationTypes("com.stericson.RootTools.containers.RootClass
.Candidate") */
/* #ANNOTATIONS @SupportedSourceVersion(SourceVersion.RELEASE_6) */
public class RootClass /* #ANNOTATIONS extends AbstractProcessor */ {

    /* #ANNOTATIONS
    @Override
    public boolean process(Set<? extends TypeElement> typeElements,
    RoundEnvironment roundEnvironment) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I was invoked!!!");

        return false;
    }
    */

    static String PATH_TO_DX = "/Users/Chris/Projects/android-sdk-macosx/build-tools/18.0.1/dx";

    enum READ_STATE {STARTING, FOUND_ANNOTATION}

    public RootClass(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        // Note: rather than calling System.load("/system/lib/libandroid_runtime.so");
        // which would leave a bunch of unresolved JNI references,
        // we are using the 'withFramework' class as a preloader.
        // So, yeah, russian dolls: withFramework > RootClass > actual method

        String className = args[0];
        RootArgs actualArgs = new RootArgs();
        actualArgs.args = new String[args.length - 1];
        System.arraycopy(args, 1, actualArgs.args, 0, args.length - 1);
        Class<?> classHandler = Class.forName(className);
        Constructor<?> classConstructor = classHandler.getConstructor(RootArgs.class);
        classConstructor.newInstance(actualArgs);
    }

    public @interface Candidate {
    }

    public class RootArgs {
        public String args[];
    }

    static void displayError(final Exception e) {
        // Not using system.err to make it easier to capture from
        // calling library.
        System.out.println("##ERR##" + e.getMessage() + "##");
        e.printStackTrace();
    }

    // I reckon it would be better to investigate classes using getAttribute()
    // however this method allows the developer to simply select "Run" on RootClass
    // and immediately re-generate the necessary jar file.
    static public class AnnotationsFinder {

        private final String AVOIDDIRPATH =
                "stericson" + File.separator + "RootTools" + File.separator;
        private final List<File> classFiles;

        public AnnotationsFinder() throws IOException {
            System.out.println("Discovering root class annotations...");
            classFiles = new ArrayList<>();
            lookup(new File("src"), classFiles);
            System.out.println("Done discovering annotations. Building jar file.");
            File builtPath = getBuiltPath();
            if (null != builtPath) {
                // Android! Y U no have com.google.common.base.Joiner class?
                final String rc1 = "com" + File.separator
                        + "stericson" + File.separator
                        + "RootTools" + File.separator
                        + "containers" + File.separator
                        + "RootClass.class";
                final String rc2 = "com" + File.separator
                        + "stericson" + File.separator
                        + "RootTools" + File.separator
                        + "containers" + File.separator
                        + "RootClass$RootArgs.class";
                final String rc3 = "com" + File.separator
                        + "stericson" + File.separator
                        + "RootTools" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder.class";
                final String rc4 = "com" + File.separator
                        + "stericson" + File.separator
                        + "RootTools" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder$1.class";
                final String rc5 = "com" + File.separator
                        + "stericson" + File.separator
                        + "RootTools" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder$2.class";
                String[] cmd;
                boolean onWindows = (System.getProperty("os.name").toLowerCase().contains("win"));
                if (onWindows) {
                    StringBuilder sb = new StringBuilder(
                            ' ' + rc1 + ' ' + rc2 + ' ' + rc3 + ' ' + rc4 + ' ' + rc5
                    );
                    for (File file : classFiles) {
                        sb.append(' ').append(file.getPath());
                    }
                    cmd = new String[]{
                            "cmd", "/C",
                            "jar cvf" +
                                    " anbuild.jar" +
                                    sb.toString()
                    };
                } else {
                    final ArrayList<String> al = new ArrayList<>();
                    al.add("jar");
                    al.add("cf");
                    al.add("anbuild.jar");
                    al.add(rc1);
                    al.add(rc2);
                    al.add(rc3);
                    al.add(rc4);
                    al.add(rc5);
                    for (final File file : classFiles) {
                        al.add(file.getPath());
                    }
                    cmd = al.toArray(new String[al.size()]);
                }
                ProcessBuilder jarBuilder = new ProcessBuilder(cmd);
                jarBuilder.directory(builtPath);
                try {
                    jarBuilder.start().waitFor();
                } catch (Exception ignored) { }

                System.out.println("Done building jar file. Creating dex file.");
                if (onWindows) {
                    cmd = new String[]{
                            "cmd", "/C",
                            "dx --dex --output=res/raw/anbuild.dex "
                                    + builtPath + File.separator + "anbuild.jar"
                    };
                } else {
                    cmd = new String[]{
                            getPathToDx(),
                            "--dex",
                            "--output=res/raw/anbuild.dex",
                            builtPath + File.separator + "anbuild.jar"
                    };
                }
                ProcessBuilder dexBuilder = new ProcessBuilder(cmd);
                try {
                    dexBuilder.start().waitFor();
                } catch (Exception ignored) { }
            }
            System.out.println(
                    "All done. ::: anbuild.dex should now be in your project's res/raw/ folder " +
                            ":::"
            );
        }

        protected void lookup(final File path, final List<File> fileList) {
            final String desourcedPath = path.toString().replace("src/", "");
            final File[] files = path.listFiles();
            if (files == null) return;
            for (final File file : files) {
                if (file.isDirectory()) {
                    if (!file.getAbsolutePath().contains(AVOIDDIRPATH)) {
                        lookup(file, fileList);
                    }
                } else {
                    if (file.getName().endsWith(".java")) {
                        if (hasClassAnnotation(file)) {
                            final String fileNamePrefix = file.getName().replace(".java", "");
                            final File compiledPath = new File(
                                    getBuiltPath().toString() + File.separator + desourcedPath);
                            File[] classAndInnerClassFiles =
                                    compiledPath.listFiles(new FilenameFilter() {
                                        @Override
                                        public boolean accept(File dir, String filename) {
                                            return filename.startsWith(fileNamePrefix);
                                        }
                                    });
                            for (final File matchingFile : classAndInnerClassFiles) {
                                fileList.add(new File(
                                        desourcedPath + File.separator + matchingFile.getName()));
                            }

                        }
                    }
                }
            }
        }

        protected boolean hasClassAnnotation(File file) {
            READ_STATE readState = READ_STATE.STARTING;
            final Pattern p = Pattern.compile(" class ([A-Za-z0-9_]+)");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while (null != (line = reader.readLine())) {
                    switch (readState) {
                        case STARTING:
                            if (line.contains("@RootClass.Candidate")) {
                                readState = READ_STATE.FOUND_ANNOTATION;
                            }
                            break;
                        case FOUND_ANNOTATION:
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                System.out.println(" Found annotated class: " + m.group(0));
                                return true;
                            } else {
                                System.err.println("Error: unmatched annotation in " +
                                        file.getAbsolutePath());
                                readState = READ_STATE.STARTING;
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception ignored) { }
            }
            return false;
        }

        protected String getPathToDx() throws IOException {
            String androidHome = System.getenv("ANDROID_HOME");
            if (null == androidHome) {
                throw new IOException("Error: you need to set $ANDROID_HOME globally");
            }
            String dxPath = null;
            final File[] files = new File(androidHome + File.separator + "build-tools").listFiles();
            if (files == null) throw new IOException("Error: files do not exist");
            int recentSdkVersion = 0;
            for (final File file : files) {
                int sdkVersion;
                String[] sdkVersionBits = file.getName().split("[.]");
                sdkVersion = Utils.parseInt(sdkVersionBits[0]) * 10000;
                if (sdkVersionBits.length > 1) {
                    sdkVersion += Utils.parseInt(sdkVersionBits[1]) * 100;
                    if (sdkVersionBits.length > 2) {
                        sdkVersion += Utils.parseInt(sdkVersionBits[2]);
                    }
                }
                if (sdkVersion > recentSdkVersion) {
                    String tentativePath = file.getAbsolutePath() + File.separator + "dx";
                    if (new File(tentativePath).exists()) {
                        recentSdkVersion = sdkVersion;
                        dxPath = tentativePath;
                    }
                }
            }
            if (dxPath == null) {
                throw new IOException("Error: unable to find dx binary in $ANDROID_HOME");
            }
            return dxPath;
        }

        protected File getBuiltPath() {
            File foundPath = null;

            File ideaPath = new File("out" + File.separator + "production"); // IntelliJ
            if (ideaPath.isDirectory()) {
                File[] children = ideaPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (children.length > 0) {
                    foundPath = new File(
                            ideaPath.getAbsolutePath() + File.separator + children[0].getName());
                }
            }
            if (null == foundPath) {
                File eclipsePath = new File("bin" + File.separator + "classes"); // Eclipse IDE
                if (eclipsePath.isDirectory()) {
                    foundPath = eclipsePath;
                }
            }

            return foundPath;
        }


    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                new AnnotationsFinder();
            } else {
                new RootClass(args);
            }
        } catch (Exception e) {
            displayError(e);
        }
    }
}
