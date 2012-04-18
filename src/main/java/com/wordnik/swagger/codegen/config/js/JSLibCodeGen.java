package com.wordnik.swagger.codegen.config.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.wordnik.swagger.codegen.LibraryCodeGenerator;
import com.wordnik.swagger.codegen.config.LanguageConfiguration;
import com.wordnik.swagger.codegen.config.NamingPolicyProvider;
import com.wordnik.swagger.codegen.exception.CodeGenerationException;
import com.wordnik.swagger.codegen.resource.Model;
import com.wordnik.swagger.codegen.resource.Resource;
import com.wordnik.swagger.codegen.util.FileUtil;
import com.yahoo.platform.yui.compressor.JarClassLoader;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.compressor.YUICompressor;

/**
 * @author ayush
 * @since 10/24/11 7:47 PM
 */
public class JSLibCodeGen extends LibraryCodeGenerator {
    private static final String DEFAULT_SERVICE_BASE_CLASS = "SwaggerApi";

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new CodeGenerationException("Invalid number of arguments passed: No command line argument was passed to the program for config json");
        }
        String libraryHome = null;
        NamingPolicyProvider nameGenerator = null;
        List<Resource> resources = new ArrayList<Resource>();
        if (args.length == 1) {
            String configPath = args[0];
            JSLibCodeGen codeGenerator = new JSLibCodeGen(configPath);
            resources = codeGenerator.generateCode();
            nameGenerator = codeGenerator.getNameGenerator();

            libraryHome = codeGenerator.getLanguageConfig().getLibraryHome();
        }
        if (args.length == 3) {
            String apiServerURL = args[0];
            if (!apiServerURL.endsWith("/")) {
                apiServerURL = apiServerURL + "/";
            }
            String apiKey = args[1];
            String packageName = args[2];
            libraryHome = args[2];
            if (libraryHome.endsWith("/")) {
                libraryHome = libraryHome.substring(0, libraryHome.length() - 1);
            }
            String modelPackageName = "";
            String apiPackageName = "";
            String classOutputDir = libraryHome + "/src/main/js/";
            JSLibCodeGen codeGenerator = new JSLibCodeGen(apiServerURL, apiKey, modelPackageName,
                    apiPackageName, classOutputDir, libraryHome);
            codeGenerator.getConfig().setDefaultServiceBaseClass(DEFAULT_SERVICE_BASE_CLASS);
            resources = codeGenerator.generateCode();
            nameGenerator = codeGenerator.getNameGenerator();
        }

        try {
            if (libraryHome != null) {
            	File sourcePath = new File(libraryHome + "/src/main/js/");
            	File distPath = new File(libraryHome + "/dist/"); 
            	if(!distPath.exists()){
            		distPath.mkdir();
            	}
                PrintWriter pw = new PrintWriter(new FileOutputStream(new File(distPath, "common.js")));
				appendFiles(pw, sourcePath.listFiles());
				pw.close();

            	for(Resource resource : resources){
            		concatinateFiles(sourcePath, new File(distPath, resource.generateClassName(nameGenerator) + ".js"), resource.getModels());
            	}
            	
            	// compress concatenated js files
            	compressFiles(distPath);
            }
        } catch (IOException e) {
            System.out.println("Unable to combine files");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unable to compress files");
            e.printStackTrace();
        }
    }

	private static void concatinateFiles(File sourcePath, File outFile, List<Model> filter) throws IOException {
    	final PrintWriter pw = new PrintWriter(new FileOutputStream(outFile));

        final List<String> modelsFilter = new ArrayList<String>();
        for (Model model : filter) {
			modelsFilter.add(model.getGenratedClassName() + ".js");
		}
        final File modelSource = new File(sourcePath, "model");
        System.out.println("Scanning " + modelSource.getAbsolutePath());
        appendFiles(pw, modelSource.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(modelsFilter.contains(name)){
					return true;
				} else {
					return false;
				}
			}
		}));

        final String apiFileName = outFile.getName();
        final File apiSource = new File(sourcePath, "api");
        System.out.println("Scanning " + apiSource.getAbsolutePath());
        appendFiles(pw, apiSource.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.equals(apiFileName)){
					return true;
				} else {
					return false;
				}
			}
		}));

        pw.close();
        System.out.println("Concatenated to " + apiFileName);
    }

    private static void appendFiles(PrintWriter pw, File[] files) throws IOException {
        for (int i = 0; i < files.length; i++) {
            System.out.println("Processing " + files[i].getPath() + "...");

            if(!files[i].isDirectory()) {
                BufferedReader br = new BufferedReader(new FileReader(files[i]
                        .getPath()));
                String line = br.readLine();
                while (line != null) {
                    pw.println(line);
                    line = br.readLine();
                }
                br.close();
            }
        }
    }

    private static void compressFiles(File distPath) throws Exception {
    	System.out.println("Trying to compress files using YUI compressor...");
    	ClassLoader loader = new JarClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        Class c = loader.loadClass(YUICompressor.class.getName());
        Method main = c.getMethod("main", new Class[]{String[].class});
        
        List<String> args = new ArrayList<String>();
        args.add("-o");
        args.add(".js$:-min.js");
        for (File file : distPath.listFiles()) {
			args.add(file.getAbsolutePath());
		}
        main.invoke(null, new Object[]{ args.toArray(new String[args.size()]) });
	}

    public JSLibCodeGen(String apiServerURL, String apiKey, String modelPackageName, String apiPackageName,
                        String classOutputDir, String libraryHome) {
        super(apiServerURL, apiKey, modelPackageName, apiPackageName, classOutputDir, libraryHome);
        this.setDataTypeMappingProvider(new JSDataTypeMappingProvider());
        this.setNameGenerator(new JSNamingPolicyProvider());
    }

    public JSLibCodeGen(String configPath) {
        super(configPath);
        this.setDataTypeMappingProvider(new JSDataTypeMappingProvider());
        this.setNameGenerator(new JSNamingPolicyProvider());
    }

    @Override
    protected LanguageConfiguration initializeLangConfig(LanguageConfiguration jsConfiguration) {
        jsConfiguration.setClassFileExtension(".js");
        jsConfiguration.setTemplateLocation("conf/js/templates");
        jsConfiguration.setStructureLocation("conf/js/structure");
        jsConfiguration.setExceptionPackageName("com.wordnik.swagger.exception");
        jsConfiguration.setAnnotationPackageName("com.wordnik.swagger.annotations");

        //create ouput directories
        FileUtil.createOutputDirectories(jsConfiguration.getModelClassLocation(), jsConfiguration.getClassFileExtension());
        FileUtil.createOutputDirectories(jsConfiguration.getResourceClassLocation(), jsConfiguration.getClassFileExtension());
        //delete previously generated files
        FileUtil.clearFolder(jsConfiguration.getModelClassLocation());
        FileUtil.clearFolder(jsConfiguration.getResourceClassLocation());
        FileUtil.clearFolder(jsConfiguration.getLibraryHome() + "/src/main/js/com/wordnik/swagger/common");
        FileUtil.clearFolder(jsConfiguration.getLibraryHome() + "/src/main/js/com/wordnik/swagger/exception");
        FileUtil.clearFolder(jsConfiguration.getLibraryHome() + "/src/main/js/com/wordnik/swagger/event");
        FileUtil.copyDirectoryFromUrl(this.getClass().getClassLoader().getResource(jsConfiguration.getStructureLocation()), new File(jsConfiguration.getLibraryHome()));

        jsConfiguration.setModelEnumRequired(false);
        jsConfiguration.setOutputWrapperRequired(true);
        return jsConfiguration;
    }
}
