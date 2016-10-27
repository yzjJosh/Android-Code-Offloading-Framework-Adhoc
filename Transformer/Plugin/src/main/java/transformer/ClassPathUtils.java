package transformer;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import mobilecloud.utils.FileUtils;

public class ClassPathUtils {
    
    public static String getClasspaths(Project project) {
        String libClasspath = getLibraryClasspath(project);
        String classClasspath = getIntermediateClassClasspath(project);
        StringBuilder res = new StringBuilder();
        if(!libClasspath.isEmpty()) {
            res.append(libClasspath);
        }
        if(!classClasspath.isEmpty()) {
            if(res.length() > 0) {
                res.append(":");
            }
            res.append(classClasspath);
        }
        return res.toString();
    }
    
    public static String getLibraryClasspath(Project project) {
        final Set<String> classpaths = new HashSet<>();
        final List<String> arrLibPaths = new LinkedList<>();
        project.getConfigurations().forEach(new Consumer<Configuration>() {
            @Override
            public void accept(Configuration t) {
                String path = t.getAsPath();
                for (String component : path.split("[:|\\s]+")) {
                    if(component.isEmpty()) {
                        continue;
                    }
                    if (FileUtils.fileExists(component)) {
                        if (!component.endsWith(".aar")) {
                            classpaths.add(component);
                        } else {
                            arrLibPaths.add(component);
                        }
                    }
                }
            }
        });
        classpaths.addAll(getExplodedAARClasspath(project, arrLibPaths));
        StringBuilder res = new StringBuilder();
        for(String classpath: classpaths) {
            if(res.length() > 0) {
                res.append(":");
            }
            res.append(classpath);
        }
        return res.toString();
    }
    
    private static List<String> getExplodedAARClasspath(Project project, List<String> libPaths) {
        List<String> res = new LinkedList<>();
        String aarFolder = project.getBuildDir().getAbsolutePath() + "/intermediates/exploded-aar";
        File arrFolderFile = new File(aarFolder);
        if(!arrFolderFile.exists()) {
            return res;
        }
        for (File libraryFolder : arrFolderFile.listFiles()) {
            if (!libraryFolder.isDirectory()) {
                continue;
            }
            for (File componentFolder : libraryFolder.listFiles()) {
                if (!componentFolder.isDirectory()) {
                    continue;
                }
                String version = getVersion(libraryFolder.getName() + "." + componentFolder.getName(), libPaths);
                if(version != null) {
                    List<String> jars = new LinkedList<>();
                    findJars(new File(componentFolder.getAbsolutePath() + "/" + version), jars);
                    res.addAll(jars);
                }
            }
        }
        return res;
    }
    
    private static void findJars(File f, List<String> res) {
        if(f == null || !f.exists()) {
            return;
        }
        if(f.isFile()) {
            if(f.getName().endsWith(".jar")) {
                res.add(f.getAbsolutePath());
            }
        } else {
            for(File next: f.listFiles()) {
                findJars(next, res);
            }
        }
    }
    
    private static String getVersion(String packageName, List<String> libPaths) {
        packageName = packageName.replaceAll("\\.", "/");
        Pattern p = Pattern.compile(packageName);
        for(String path: libPaths) {
            Matcher matcher = p.matcher(path);
            if(matcher.find()) {
                int index = matcher.end();
                String version = path.substring(index+1, path.indexOf('/', index+1));
                return version;
            }
        }
        return null;
    }
    
    public static String getIntermediateClassClasspath(Project project) {
        StringBuilder res = new StringBuilder();
        String classFolder = project.getBuildDir().getAbsolutePath() + "/intermediates/classes";
        File classFolderFile = new File(classFolder);
        if(classFolderFile.exists() && classFolderFile.isDirectory()) {
            for(File variant: classFolderFile.listFiles()) {
                if(!variant.isDirectory()) {
                    continue;
                }
                String path = variant.getAbsolutePath();
                if(FileUtils.hasFiles(path)) {
                    if(res.length() > 0) {
                        res.append(":");
                    }
                    res.append(path);
                }
            }
        }
        return res.toString();
    }
    
}
