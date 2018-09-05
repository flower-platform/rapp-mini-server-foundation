package rapp_mini_server_sample1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class RappService {
		
	private static final String ARDUINO_CLI_PATH = "D:\\Arduino\\Arduino_IDEs\\arduino-1.8.5\\arduino_debug.exe";

	private Path tmpDir;
	
	public String buildRappForESP8266(String rappName, @JsonTypeInfo(use = Id.CUSTOM, defaultImpl = RappServiceFile.class) List<RappServiceFile> files) throws IOException, Exception {
		if (tmpDir == null) {
			tmpDir = Files.createTempDirectory("flower_platform_build");
			tmpDir.toFile().deleteOnExit();
		}
		Path srcDir = tmpDir.resolve(rappName);
		Path buildDir = tmpDir.resolve(rappName + "_build");
		
		srcDir.toFile().mkdirs();
		for (RappServiceFile file : files) {
			Path p = srcDir.resolve(file.getPath());
			File parent = p.getParent().toFile(); 
			if (!parent.exists()) {
				parent.mkdirs();
			}
			try (FileOutputStream out = new FileOutputStream(p.toFile())) {
				out.write(file.getContent().getBytes());
			}
		}
		
		Path filePath = srcDir.resolve(rappName +".ino");
		System.out.println("MAIN: " + filePath);
		
		ProcessBuilder pb = new ProcessBuilder(ARDUINO_CLI_PATH, 
//				 "--upload", "-v",
				 "--verify", "-v",
				 "--board", "esp8266:esp8266:nodemcuv2:CpuFrequency=80,UploadSpeed=921600,FlashSize=4M3M",
				"--pref",  "build.path=" + buildDir,  
//				"--pref",  "sketchbook.path=c:\\Users\\Flower\\Desktop\\SourceFolderTests",
				filePath.toString());
		
		pb.inheritIO();
		Process buildProcess = pb.start();
		buildProcess.waitFor();
		
		Path binaryPath = buildDir.resolve(rappName +".ino.bin");
		byte[] data = Files.readAllBytes(binaryPath);
		String binaryBase64 = Base64.getEncoder().encodeToString(data);
		
		return binaryBase64;
	}

}
