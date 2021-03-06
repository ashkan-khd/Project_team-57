package server.model.db;

import server.model.existence.Comment;
import server.model.existence.Product;

import java.io.*;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ProductTable extends Database {
    public static ArrayList<Product> getAllProducts() throws SQLException, ClassNotFoundException {
        String queryTask = "SELECT * FROM Products;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(queryTask);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Product> allProducts = new ArrayList<Product>();

        while (resultSet.next())
            allProducts.add(new Product(resultSet));

        return allProducts;
    }

    public static Product getProductByID(String ID) throws SQLException, ClassNotFoundException {
        String queryTask = "SELECT * FROM Products WHERE ID = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(queryTask);
        preparedStatement.setString(1, ID);
        Product product = new Product(preparedStatement.executeQuery());
        return product;
    }

    public static void removeProductByID(String ID) throws SQLException, ClassNotFoundException {
        String task = "DELETE FROM Products WHERE ID = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(task);
        preparedStatement.setString(1, ID);
        preparedStatement.execute();
    }

    public static boolean isIDFree(String productId) throws SQLException, ClassNotFoundException
    {
        String command = "SELECT * FROM Products WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productId);
        return !(preparedStatement.executeQuery().next());
    }

    public static void setProductStatus(String ID, int status) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET Status = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, status);
        preparedStatement.setString(2, ID);
        preparedStatement.execute();
    }

    public static ArrayList<Product> getAllUnApprovedProducts() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Products WHERE Status = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, 2);
        ArrayList<Product> products = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            products.add(new Product(resultSet));
        }
        return products;
    }

    public static ArrayList<Product> getAllShowingProducts() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Products WHERE Status = ? OR Status = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, 1);
        preparedStatement.setInt(2, 3);
        ArrayList<Product> products = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            products.add(new Product(resultSet));
        }
        return products;
    }

    public static void setProductApprovalDate(String productId) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET ApprovalDate = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
        preparedStatement.setString(2, productId);
        preparedStatement.execute();
    }

    public static ArrayList<Product> getProductsWithCategory(String categoryName) throws SQLException, ClassNotFoundException {
        ArrayList<Product> products = new ArrayList<>();
        String command = "SELECT * FROM Products WHERE Category = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, categoryName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            products.add(new Product(resultSet));
        return products;
    }

    public static void changeProductCategoryByID(String ID, String category) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET Category = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, category);
        preparedStatement.setString(2, ID);
        preparedStatement.execute();
    }

    public static boolean isThereProductWithSpecificCategory(String categoryName) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Products WHERE Category = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, categoryName);
        return preparedStatement.executeQuery().next();
    }

    public static void addSeenToProductWithID(String productID) throws SQLException, ClassNotFoundException {
        int firstSeen = ProductTable.getProductByID(productID).getSeen();
        String command = "UPDATE Products SET Seen = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, (firstSeen + 1));
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
        if(!EditingProductTable.isIDFree(productID))
        {
            command = "UPDATE EditingProducts SET Seen = ? WHERE ID = ?";
            preparedStatement = getConnection().prepareStatement(command);
            preparedStatement.setInt(1, (firstSeen + 1));
            preparedStatement.setString(2, productID);
            preparedStatement.execute();
        }
    }

    public static String getVendorName(String productID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Products WHERE ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productID);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("SellerUsername");
    }

    public static void reduceProductAmount(String productID, double amount) throws SQLException, ClassNotFoundException {
        double firstAmount = ProductTable.getProductByID(productID).getAmount();
        String command = "UPDATE Products SET Amount = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setDouble(1, firstAmount - amount);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

    public static void reduceProductCount(String productID, int count) throws SQLException, ClassNotFoundException {
        int firstCount = ProductTable.getProductByID(productID).getCount();
        String command = "UPDATE Products SET Count = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, firstCount - count);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

    public static ArrayList<Integer> getAllScores(String productID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Scores WHERE ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productID);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Integer> allScores = new ArrayList<>();
        while (resultSet.next()) {
            allScores.add(resultSet.getInt("Score"));
        }
        return allScores;
    }

    public static boolean didScore(String username, String productID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Scores WHERE CustomerUsername = ? AND ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, productID);
        return preparedStatement.executeQuery().next();
    }

    public static int getScore(String username, String productID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Scores WHERE CustomerUsername = ? AND ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, productID);
        return preparedStatement.executeQuery().getInt("Score");
    }

    public static void updateScore(String username, String productID, int score) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Scores SET Score = ? WHERE CustomerUsername = ? AND ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, score);
        preparedStatement.setString(2, username);
        preparedStatement.setString(3, productID);
        preparedStatement.execute();
    }

    public static void setScore(String username, String productID, int score) throws SQLException, ClassNotFoundException {
        String command = "INSERT INTO Scores(ProductID, CustomerUsername, Score) VALUES(?,?,?)";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productID);
        preparedStatement.setString(2, username);
        preparedStatement.setInt(3, score);
        preparedStatement.execute();
    }

    public static void deleteProductFromScores(String productID) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Scores WHERE ProductID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productID);
        preparedStatement.execute();
    }

    public static void updateProductsAvgScore(String productID) throws SQLException, ClassNotFoundException {
        double avgSc = 0;
        int size = 0;
        for (Integer score : getAllScores(productID)) {
            ++size;
            avgSc += score;
        }
        avgSc /= size;
        String command = "UPDATE Products SET AverageScore = ? WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setDouble(1, avgSc);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

    public static void addComment(Comment comment) throws SQLException, ClassNotFoundException {
        String command = "INSERT INTO Comments(CommentID, ProductID, Title, Content, Status, CustomerUsername)" +
                " VALUES(?,?,?,?,?,?)";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, comment.getCommentID());
        preparedStatement.setString(2, comment.getProductID());
        preparedStatement.setString(3, comment.getTitle());
        preparedStatement.setString(4, comment.getContent());
        preparedStatement.setInt(5, comment.getStatus());
        preparedStatement.setString(6, comment.getCustomerUsername());
        preparedStatement.execute();
    }

    public static boolean isThereCommentByID(String commentID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Comments WHERE CommentID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, commentID);
        return preparedStatement.executeQuery().next();
    }

    public static ArrayList<Comment> getAllUnApprovedComments() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Comments WHERE Status = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, 2);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Comment> allUnApprovedComments = new ArrayList<>();
        while (resultSet.next()){
            allUnApprovedComments.add(new Comment(resultSet));
        }
        return allUnApprovedComments;
    }

    public static void modifyCommentApproval(String commentID, int status) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Comments SET Status = ? WHERE CommentID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, status);
        preparedStatement.setString(2, commentID);
        preparedStatement.execute();
    }

    public static ArrayList<Comment> getAllLoggedInUserComment(String username, String currentProduct) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Comments WHERE ProductID = ? AND CustomerUsername = ? AND (Status = ? OR Status = ?) ";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, currentProduct);
        preparedStatement.setString(2, username);
        preparedStatement.setInt(3,2);
        preparedStatement.setInt(4, 3);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Comment> comments = new ArrayList<>();
        while (resultSet.next()){
            comments.add(new Comment(resultSet));
        }
        return comments;
    }

    public static ArrayList<Comment> getAllApprovedCommentsOnThisProduct(String currentProduct) throws SQLException, ClassNotFoundException {
        String comment = "SELECT * FROM Comments WHERE ProductID = ? AND Status = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(comment);
        preparedStatement.setString(1, currentProduct);
        preparedStatement.setInt(2,1);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Comment> comments = new ArrayList<>();
        while (resultSet.next()){
            comments.add(new Comment(resultSet));
        }
        return comments;
    }

    public static Comment getCommentByID(String commentID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Comments WHERE CommentID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1,commentID);
        return new Comment(preparedStatement.executeQuery());
    }


    public static String getProductImageFilePath(String productID, int number) {
        String fileName = "database\\Images\\Products\\" + productID + "\\" + number;
        String[] validImageExtensions = {"jpg" , "jpeg" , "png", "bmp"};
        for (String validImageExtension : validImageExtensions) {
            String filePath = fileName + "." + validImageExtension;
            if(new File(filePath).exists())
                return filePath;
        }
        return null;
    }

    public static String getProductImageFileExtension(String productID, int number) {
        String fileName = "database\\Images\\Products\\" + productID + "\\" + number;
        String[] validImageExtensions = {"jpg" , "jpeg" , "png", "bmp"};
        for (String validImageExtension : validImageExtensions) {
            String filePath = fileName + "." + validImageExtension;
            if(new File(filePath).exists())
                return validImageExtension;
        }

        return "png";
    }

    public static FileInputStream getProductImageInputStream(String ID, int number) throws FileNotFoundException {
        return new FileInputStream(getProductImageFilePath(ID, number));
    }

    public static void addImage(String productID, int productNumber, File pictureFile) throws IOException {
        File productFolder = new File("database\\Images\\Products\\" + productID );
        if(!productFolder.exists())
            productFolder.mkdir();
        String[] splitPath = pictureFile.getPath().split("\\.");
        String fileExtension = splitPath[splitPath.length - 1];
        File saveImage = new File("database\\Images\\Products\\" + productID + "\\" + productNumber + "." + fileExtension);
        Files.copy(pictureFile.toPath(), saveImage.toPath());
    }

    public static void deleteImage(String productID, int imageNumber) throws IOException {
        File deletingImage = new File(String.valueOf(getProductImageFilePath(productID, imageNumber)));
        deletingImage.delete();
    }

    public static void reNumProductImage(String productID, int firstNumber, int secondNumber) {
        File imageFile = new File(getProductImageFilePath(productID, firstNumber));
        File newImageFile =
                new File("database\\Images\\Products\\" + productID + "\\" + secondNumber +
                        getProductImageFilePath(productID, firstNumber).split("\\.")[getProductImageFilePath(productID, firstNumber).split("\\.").length - 1]);
        imageFile.renameTo(newImageFile);
    }

    public static ArrayList<Product> getAllNotApprovedProducts() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Products WHERE Status = ? OR Status = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, 2);
        preparedStatement.setInt(2, 3);
        ArrayList<Product> products = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            if(resultSet.getInt("Status") == 2)
                products.add(new Product(resultSet));
            else
                products.add(EditingProductTable.getEditingProductWithID(resultSet.getString("ID")));
        }
        return products;
    }

    public static void removeAllProductComments(String productId) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Carts WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, productId);
        preparedStatement.execute();
    }

    public static void removeAllProductImages(String productID) {
        File productFolder = new File("database\\Images\\Products\\" + productID);
        productFolder.mkdir();
        File tempFolder = new File("database\\Images\\Products\\" + productID);
        String[] entries = tempFolder.list();
        for(String s: entries){
            File currentFile = new File(tempFolder.getPath(),s);
            currentFile.delete();
        }
    }

    public static void removeAllUserComments(String username) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Comments WHERE CustomerUsername = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        preparedStatement.execute();
    }

    public static void removeAllUserScores(String username) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Scores WHERE CustomerUsername = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        preparedStatement.execute();
    }

    public static FileOutputStream getProductImageOutputStream(String productID, String fileExtension, int number) throws IOException {
        String folderFileName = "database\\Images\\Products\\" + productID;
        new File(folderFileName).mkdirs();
        String fileName = "database\\Images\\Products\\" + productID + "\\" + number + "." + fileExtension;
        File pictureFile = new File(fileName);
        if (!pictureFile.exists()) {

            pictureFile.createNewFile();
            return new FileOutputStream(pictureFile);
        } else {
            System.err.println("Error IN #getProductImageOutputStream");
            return null;
        }
    }

    public static void addProductFileInfo(String productID, String productFileInfoJson) throws IOException {
        new File("database\\Files\\Products\\" + productID).mkdirs();
        File file = new File("database\\Files\\Products\\" + productID + "\\Info.json" );
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(productFileInfoJson);
        fileWriter.flush();
        fileWriter.close();
    }

    public static FileOutputStream getProductFileOutputStream(String productID, String fileName, String fileExtension) throws FileNotFoundException {
        new File("database\\Files\\Products\\" + productID).mkdirs();
        File file = new File("database\\Files\\Products\\" + productID + "\\" + fileName + "." + fileExtension);
        return new FileOutputStream(file);
    }

    public static String getProductFileInfo(String productID) throws FileNotFoundException {
        File file = new File("database\\Files\\Products\\" + productID + "\\Info.json");
        Scanner fileScanner = new Scanner(file);
        StringBuilder json = new StringBuilder();

        while (fileScanner.hasNextLine()){
            json.append(fileScanner.nextLine());
            json.append("\n");
        }
        fileScanner.close();
        json = new StringBuilder(json.substring(0, json.length() - 1));
        return json.toString();
    }

    public static String getProductFilePath(String productID, String productName, String fileExtension) {
        return "database\\Files\\Products\\" + productID + "\\" + productName + "." + fileExtension;
    }

    public static boolean doesProductHaveFile(String productID) {
        String fileName = "database\\Files\\Products\\" + productID;
        File file = new File(fileName);
        return file.list() != null && file.list().length != 0;
    }

    public static FileInputStream getProductFileInputStream(String productID, String name, String extension) throws FileNotFoundException {
        return new FileInputStream(getProductFilePath(productID, name, extension));
    }

    public static void removeProductFileByID(String productID) {
        File editingProductFileFolder = new File("database\\Files\\Products\\" + productID);
        if(editingProductFileFolder.exists()) {
            String[] entries = editingProductFileFolder.list();
            if (entries != null) {
                for(String s: entries){
                    File currentFile = new File(editingProductFileFolder.getPath(),s);
                    currentFile.delete();
                }
            }
        }
    }

    public static void addFile(String productID, File productFile) throws IOException {
        File productFolder = new File("database\\Files\\Products\\" + productID );
        if(!productFolder.exists())
            productFolder.mkdir();
        String[] splitPath = productFile.getPath().split("\\.");
        String fileExtension = splitPath[splitPath.length - 1];
        File saveImage = new File("database\\Files\\Products\\" + productID + "\\" + productFile.getName().substring(0, productFile.getName().lastIndexOf('.')) + "." + fileExtension);
        Files.copy(productFile.toPath(), saveImage.toPath());
    }

    public static void setProductCountability(String productID, boolean isCountable) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET IsCountable = ? WHERE ID = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setBoolean(1, isCountable);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

    public static void setProductCount(String productID, int count) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET Count = ? WHERE ID = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setInt(1, count);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

    public static void setProductAmount(String productID, double amount) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Products SET Amount = ? WHERE ID = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setDouble(1, amount);
        preparedStatement.setString(2, productID);
        preparedStatement.execute();
    }

}

