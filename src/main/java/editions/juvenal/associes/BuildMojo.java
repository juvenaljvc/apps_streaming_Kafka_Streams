package editions.juvenal.associes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * It adds - at the beginning of each line
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractMojo {

	/**
	 * Output directory path where HTML files are generated
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", property = "siteOutputDirectory", required = true)
	private File outputDirectory;

	/**
	 * A specific <code>fileSet</code> rule to select files and directories. Fileset
	 * spec: https://maven.apache.org/shared/file-management/fileset.html
	 */
	@Parameter
	private FileSet inputFiles;

	private final FileSetManager fileSetManager;

	@Inject
	public BuildMojo(FileSetManager fileSetManager) {
		this.fileSetManager = fileSetManager;
	}

	public void execute() throws MojoExecutionException {
		if (inputFiles == null) {
			setDefaultInput();
		}
		String[] includedFiles = fileSetManager.getIncludedFiles(inputFiles);
		if (includedFiles == null || includedFiles.length == 0) {
			getLog().warn("SKIP: There are no input files. " + getInputFilesToString());
		} else {
			try {
				Files.createDirectories(outputDirectory.toPath());
				for (String f : includedFiles) {
					build(Paths.get(f));
				}
			} catch (IOException e) {
				throw new MojoExecutionException("Error building file", e);
			}
		}
	}

	private void build(Path path) throws MojoExecutionException {
	    try (
	    	BufferedWriter bw = Files.newBufferedWriter(outputDirectory.toPath().resolve(path))) {
	    	Files.lines(path, StandardCharsets.UTF_8).forEach(s -> {
				try {
					bw.append("-").append(s).append("\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	    }
	    catch (IOException e) {
			throw new MojoExecutionException("Error building file " + path, e);
	    }
	}

	private void setDefaultInput() {
		this.inputFiles = new FileSet();
		this.inputFiles.addInclude("**/*.txt");
		this.inputFiles.setDirectory(".");
		getLog().info("'inputFiles' is not configured, using defaults: " + getInputFilesToString());
	}

	private String getInputFilesToString() {
		return "Fileset matching " + inputFiles.getIncludes() + " in " + inputFiles.getDirectory();
	}

}
