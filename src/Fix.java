import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Fix {
	public static void main(String[] args) throws IOException {
		File classPathFile = new File(".classpath");
		File raylibPath = new File("jaylib.jar");
		
		Scanner scan = new Scanner(classPathFile);
		String contents = "";
		
		while (scan.hasNext()) {
			contents += scan.nextLine() + "\n";
		}
		scan.close();
		
		contents = contents.replace(
				"<classpathentry kind=\"lib\" path=\"C:/Users/Student/eclipse-workspace/apcs/jaylib.jar\"/>", 
				"<classpathentry kind=\"lib\" path=\"" + raylibPath.getAbsolutePath() + "\"/>");
		
		FileWriter writer = new FileWriter(classPathFile);
		writer.write(contents);
		writer.close();
		System.out.println("Fixed! Restart eclipse.");
	}
}
