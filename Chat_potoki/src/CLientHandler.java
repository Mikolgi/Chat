import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class CLientHandler implements Runnable {
    private static ArrayList<CLientHandler> cLientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String clientUid;

    public CLientHandler(Socket socket) {
        try {
            this.socket = socket;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


            String[] clientInfo = bufferedReader.readLine().split(":");
            this.clientUid = clientInfo[0];
            this.clientUsername = clientInfo[1];

            cLientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has connected.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (CLientHandler clientHandler : cLientHandlers) {
            try {
                if (!clientHandler.clientUid.equals(clientUid)) { // Проверяем UID для исключения отправителя
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(clientUsername + ": " + messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void removeClientHandler() {
        cLientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has disconnected.");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            removeClientHandler();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
