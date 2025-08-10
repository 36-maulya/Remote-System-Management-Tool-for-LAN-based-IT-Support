import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        int port = 9999;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for connection...");
            Socket socket = serverSocket.accept();
            System.out.println("Client connected.");

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("\n--- Remote Commands ---");
                System.out.println("1. Screenshot");
                System.out.println("2. List Files in Folder");
                System.out.println("3. Delete File");
                System.out.println("4. Memory Usage");
                System.out.println("5. Search File (Live Results)");
                System.out.println("6. Shutdown");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");

                String choice = sc.nextLine();

                switch (choice) {
                    case "1": // Screenshot
                        dos.writeUTF("screenshot");
                        FileOutputStream fos = new FileOutputStream("received_screenshot.png");
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            if (bytesRead < 4096) break;
                        }
                        fos.close();
                        System.out.println("Screenshot saved.");
                        break;

                    case "2": // List files
                        System.out.print("Enter folder path: ");
                        String folderPath = sc.nextLine();
                        dos.writeUTF("listfiles");
                        dos.writeUTF(folderPath);
                        System.out.println("--- Files in Folder ---");
                        System.out.println(dis.readUTF());
                        break;

                    case "3": // Delete file
                        System.out.print("Enter file path to delete: ");
                        String fileName = sc.nextLine();
                        dos.writeUTF("deletefile");
                        dos.writeUTF(fileName);
                        System.out.println("Client: " + dis.readUTF());
                        break;

                    case "4": // Memory usage
                        dos.writeUTF("memory");
                        System.out.println("Memory Usage: " + dis.readUTF());
                        break;

                    case "5": // Search file (live)
                        System.out.print("Enter file name to search: ");
                        String searchName = sc.nextLine();
                        dos.writeUTF("searchfile");
                        dos.writeUTF(searchName);

                        System.out.println("--- Search Results ---");
                        while (true) {
                            String line = dis.readUTF();
                            if ("SEARCH_DONE".equals(line)) break;
                            System.out.println(line);
                        }
                        break;

                    case "6": // Shutdown
                        dos.writeUTF("shutdown");
                        System.out.println("Shutdown command sent.");
                        break;

                    case "7": // Exit
                        dos.writeUTF("exit");
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
