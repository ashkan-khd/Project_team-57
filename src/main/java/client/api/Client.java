package client.api;

import client.Protocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import server.server.Response;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import static client.Protocol.CLIENT_COMPONENT;

public class Client {
    private static final String CACHE_FOLDER_URL = "cache\\";
    private static Client client = null;
    private Socket mySocket;
    private DataOutputStream outStream;
    private DataInputStream inStream;
    private String authToken;
    private String relic;
    private Gson gson;

    protected static Function<BufferedImage, Image> bufferedImage2Image;
    protected static Function<List<Integer>, Image> integerArray2Image;
    public static Function<File, String> file2Extension;

    static {
        bufferedImage2Image = new Function<BufferedImage, Image>() {
            @Override
            public Image apply(BufferedImage bufferedImage) {
                WritableImage wr = null;
                if (bufferedImage != null) {
                    wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                    PixelWriter pw = wr.getPixelWriter();
                    for (int x = 0; x < bufferedImage.getWidth(); x++) {
                        for (int y = 0; y < bufferedImage.getHeight(); y++) {
                            pw.setArgb(x, y, bufferedImage.getRGB(x, y));
                        }
                    }
                }

                return new ImageView(wr).getImage();
            }
        };

        integerArray2Image = new Function<List<Integer>, Image>() {
            @Override
            public Image apply(List<Integer> integers) {
                try {
                    byte[] bytes = new byte[integers.size()];

                    for (int i = 0; i < integers.size(); i++) {
                        bytes[i] = integers.get(i).byteValue();
                    }

                    InputStream inputStream = new ByteArrayInputStream(bytes);
                    return bufferedImage2Image.apply(ImageIO.read(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        file2Extension = (file) -> file.getPath().split("\\.")[file.getPath().split("\\.").length - 1];
    }


    public static Client getClient() {
        try {
            if(client == null)
                client = new Client();
            return client;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Client() throws IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        Command command = new Command("get relic", Command.HandleType.GENERAL);
        Response<String> response = postAndGet(command, Response.class, (Class<String>)String.class);
        this.relic = response.getDatum();

        File cacheDirectory = new File(CACHE_FOLDER_URL);
        if(cacheDirectory.exists()) {
            String[] entries = cacheDirectory.list();
            for(String s: entries){
                File currentFile = new File(cacheDirectory.getPath(),s);
                currentFile.delete();
            }
        } else {
            cacheDirectory.mkdir();
        }
    }

    public  <T, E, C extends Response> Response<T> postAndGet(Command<E> command, Class<C> responseType, Class<T> responseDataType){
        try {
            makeConnection();
            post(command);
            String responseStr = inStream.readUTF();
            Response<T> response = gson.fromJson(responseStr,  TypeToken.getParameterized(responseType, responseDataType).getType());
            closeConnection();
            return response;
        } catch (IOException e) {
            System.err.println("SHIT ERROR IN POST AND GET");
            e.printStackTrace();
        }
        return null;
    }

    public <E> Image getImage(Command<E> command) {
        try {
            makeConnection();
            post(command);

            //For Tof Zani, Bitch
            inStream.readUTF();

            ArrayList<Integer> integers = new ArrayList<>();
            int i;
            while ((i = inStream.read()) > -1) {
                integers.add(i);
            }

            return integerArray2Image.apply(integers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public <E> void sendImage(Command<E> command, File imageFile) {
        try {
            makeConnection();
            post(command);
            FileInputStream imageFileInputStream = new FileInputStream(imageFile);
            int i;
            while ((i = imageFileInputStream.read()) > -1) {
                outStream.write(i);
                outStream.flush();
            }
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <C> File getFile(Command<C> command) {
        try {
            makeConnection();
            post(command);
            String extension = inStream.readUTF();

            new File(CACHE_FOLDER_URL).mkdir();
            File file = new File(CACHE_FOLDER_URL + getFileName(extension));
            file.createNewFile();
            FileOutputStream fileOutStream = new FileOutputStream(file);
            int i;
            while ( (i = inStream.read()) > -1) {
                fileOutStream.write(i);
            }
            fileOutStream.flush();
            fileOutStream.close();
            closeConnection();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <E> void sendFile(Command<E> command, File file) {
        try {
            makeConnection();
            post(command);
            FileInputStream imageFileInputStream = new FileInputStream(file);
            int i;
            while ((i = imageFileInputStream.read()) > -1) {
                outStream.write(i);
                outStream.flush();
            }
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(String extension) {
        String fileName = "";
        do {
            fileName = generateRandomFileName();
            fileName += "." + extension;
        }while (new File(CACHE_FOLDER_URL + fileName).exists());
        return fileName;
    }

    private String generateRandomFileName() {
        char[] validChars = {'0', '2', '1', '3', '5', '8', '4', '9', '7', '6'};
        //THE TWO POPES
        StringBuilder ID = new StringBuilder(Math.random() <= 0.5 ? ("jesus") : ("moses"));
        for(int i = 0; i < 10; ++i)
        {
            ID.append(validChars[((int) (Math.random() * 1000000)) % validChars.length]);
        }
        return ID.toString();
    }

    private void post(Command command) throws IOException {
        command.setAuthToken(authToken); command.setRelic(relic);
        String commandStr = gson.toJson(command);
        outStream.writeUTF(commandStr);
        outStream.flush();
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private void closeConnection() throws IOException {
        inStream.close();
        outStream.close();
        mySocket.close();
    }

    private void makeConnection() throws IOException {
        mySocket = new Socket(CLIENT_COMPONENT.Ip, CLIENT_COMPONENT.port);
        inStream = new DataInputStream(new BufferedInputStream(mySocket.getInputStream()));
        outStream = new DataOutputStream(new BufferedOutputStream(mySocket.getOutputStream()));
    }
}
