package server.model.db;

import notification.Notification;
import server.model.existence.Account;
import server.model.existence.Account.*;

import java.io.*;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountTable extends Database {
    public static boolean isUsernameFree(String username) throws SQLException, ClassNotFoundException {
        String command = "SELECT * From Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        return !preparedStatement.executeQuery().next();
    }

    public static boolean isPasswordCorrect(String username, String password) throws SQLException, ClassNotFoundException {
        String command = "SELECT Password From Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return password.equals(resultSet.getString("Password"));
    }

    public static boolean isUserApproved(String username) throws SQLException, ClassNotFoundException {
        String command = "SELECT IsApproved FROM Accounts WHERE Username = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        return preparedStatement.executeQuery().getBoolean("IsApproved");
    }

    public static void addAccount(Account account) throws SQLException, ClassNotFoundException {
        String command = "INSERT INTO Accounts (Username, Password, AccType, IsApproved, FirstName, LastName) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, account.getUsername());
        preparedStatement.setString(2, account.getPassword());
        preparedStatement.setString(3, account.getType());
        preparedStatement.setString(5, account.getFirstName());
        preparedStatement.setString(6, account.getLastName());
        if (!account.getType().equals("Vendor")) {
            preparedStatement.setBoolean(4, true);
        } else
            preparedStatement.setBoolean(4, false);
        preparedStatement.execute();
    }

    public static String getTypeByUsername(String username) throws SQLException, ClassNotFoundException {
        String command = "SELECT AccType From Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("AccType");
    }

    public static boolean isThereAdmin() throws SQLException, ClassNotFoundException {
        String command = "SELECT * From Accounts WHERE AccType = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Admin");
        return preparedStatement.executeQuery().next();
    }

    public static Account getAccountByUsername(String username) throws SQLException, ClassNotFoundException {
        String command = "SELECT * From Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        return Account.makeAccount(preparedStatement.executeQuery());
    }

    public static void editField(String username, String fieldName, String value) throws SQLException, ClassNotFoundException {
        String command = "UPDATE Accounts SET " + fieldName + " = ? WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, value);
        preparedStatement.setString(2, username);
        preparedStatement.execute();
    }

    public static void changeCredit(String username, double money) throws SQLException, ClassNotFoundException {
        double credit = getCredit(username);
        credit += money;
        String command = "UPDATE Accounts SET Credit = ? WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setDouble(1, credit);
        preparedStatement.setString(2, username);
        preparedStatement.execute();
    }

    public static double getCredit(String username) throws SQLException, ClassNotFoundException {
        String command = "SELECT Credit From Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getDouble("Credit");
    }

    public static ArrayList<Account> getAllAccounts() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Accounts WHERE IsApproved = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setBoolean(1, true);
        ArrayList<Account> allAccounts = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            allAccounts.add(Account.makeAccount(resultSet));
        }
        return allAccounts;
    }

    public static void deleteUserWithUsername(String username) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Accounts WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, username);
        preparedStatement.execute();
    }

    public static ArrayList<Account> getAllUsers() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Accounts WHERE (AccType = ? OR AccType = ?) AND IsApproved = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Vendor");
        preparedStatement.setString(2, "Customer");
        preparedStatement.setBoolean(3, true);
        ArrayList<Account> allUsers = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            allUsers.add(Account.makeAccount(resultSet));
        }
        return allUsers;
    }

    public static ArrayList<Account> getAllAdmins() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Accounts WHERE AccType = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Admin");
        ArrayList<Account> allAdmins = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            allAdmins.add(Account.makeAccount(resultSet));
        }
        return allAdmins;
    }

    public static ArrayList<Account> getAllVendors() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Accounts WHERE AccType = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Vendor");
        ArrayList<Account> allAdmins = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            allAdmins.add(Account.makeAccount(resultSet));
        }
        return allAdmins;
    }

    public static ArrayList<Account> getAllCustomers() throws SQLException, ClassNotFoundException {
        ArrayList<Account> allCustomers = new ArrayList<>();
        String command = "SELECT * FROM Accounts WHERE AccType = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Customer");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            allCustomers.add(Account.makeAccount(resultSet));
        return allCustomers;
    }

    public static boolean didPeriodPass(String ID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM TimeLapse WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, ID);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()){
            if (resultSet.getDate("FinishDate").compareTo(new Date(System.currentTimeMillis())) < 1)
                return true;
            return false;
        }
        return false;
    }

    public static void updatePeriod(String ID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM TimeLapse WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, ID);
        ResultSet resultSet = preparedStatement.executeQuery();
        Date startDate = resultSet.getDate("StartDate"); Date finishDate = resultSet.getDate("FinishDate");
        String newCommand = "UPDATE TimeLapse SET StartDate = ?, FinishDate = ? WHERE ID = ?";
        preparedStatement = getConnection().prepareStatement(newCommand);
        preparedStatement.setString(3, ID);
        preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
        preparedStatement.setDate(2, new Date(System.currentTimeMillis() + (finishDate.getTime() - startDate.getTime())));
        preparedStatement.execute();
    }

    public static HashMap<Date, Date> getTimeLapse(String ID) throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM TimeLapse WHERE ID = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, ID);
        ResultSet resultSet = preparedStatement.executeQuery();
        HashMap<Date, Date> map = new HashMap<>();
        map.put(resultSet.getDate("StartDate"), resultSet.getDate("FinishDate"));
        return map;
    }

    public static String getUserImageFilePath(String username) {
        String fileName = "database\\Images\\Users\\" + username;
        String[] validImageExtensions = {"jpg" , "jpeg" , "png", "bmp"};
        for (String validImageExtension : validImageExtensions) {
            String filePath = fileName + "." + validImageExtension;
            File file = new File(filePath);
            if(file.exists()){
                return filePath;
            }
        }
        return null;
    }

    public static FileInputStream getProfileImageInputStream(String username) throws FileNotFoundException {
        System.err.println(username);
        return new FileInputStream(getUserImageFilePath(username));
    }

    public static void setProfileImage(String username, File pictureFile) throws IOException {
        String[] splitPath = pictureFile.getPath().split("\\.");
        String fileExtension = splitPath[splitPath.length - 1];
        File saveImage = new File("database\\Images\\Users\\" + username + "." + fileExtension);
        Files.copy(pictureFile.toPath(), saveImage.toPath());
    }

    public static FileOutputStream getProfileImageOutputStream(String username, String imageExtension) throws IOException {
        File profileImageFile = new File("database\\Images\\Users\\" + username + "." + imageExtension);

        if(!profileImageFile.exists()) {
            System.err.println("File Doesn't Exists. #getProfileImageOutputStream");
            profileImageFile.createNewFile();
        }

        return new FileOutputStream(profileImageFile);
    }

    public static void deleteProfileImage(String username) {
        if(getUserImageFilePath(username) != null) {
            File file = new File(getUserImageFilePath(username));
            file.delete();
        }
    }

    public static void setMarketWage(Double wage) throws SQLException, ClassNotFoundException {
        String command = "UPDATE MarketCredit" +
                " SET Holder = ?" +
                " WHERE Specifier = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(2, "Market Wage");
        preparedStatement.setDouble(1, wage);
        preparedStatement.execute();
    }

    public static void setMinimumWallet(Double minimumWallet) throws SQLException, ClassNotFoundException {
        String command = "UPDATE MarketCredit" +
                " SET Holder = ?" +
                " WHERE Specifier = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(2, "Minimum Wallet");
        preparedStatement.setDouble(1, minimumWallet);
        preparedStatement.execute();
    }

    public static Double getWage() throws SQLException, ClassNotFoundException {
        String command = "SELECT Holder From MarketCredit WHERE Specifier = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Market Wage");
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.getDouble("Holder");
    }

    public static Double getMinimumWallet() throws SQLException, ClassNotFoundException {
        String command = "SELECT Holder From MarketCredit WHERE Specifier = ?;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, "Minimum Wallet");
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.getDouble("Holder");
    }

    public static ArrayList<Supporter> getAllSupporters() throws SQLException, ClassNotFoundException {
        String command = "SELECT * FROM Supporters;";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        ArrayList<Supporter> allUsers = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            allUsers.add(new Supporter(resultSet));
        }
        return allUsers;
    }

    public static void addSupporter(Supporter supporter) throws SQLException, ClassNotFoundException {
        String command = "INSERT INTO Supporters (Username, Password, FirstName, LastName) " +
                "VALUES (?, ?, ?, ?);";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, supporter.getUsername());
        preparedStatement.setString(2, supporter.getPassword());
        preparedStatement.setString(3, supporter.getFirstName());
        preparedStatement.setString(4, supporter.getLastName());
        preparedStatement.execute();
    }

    public static boolean isUsernameFreeForSupporter(String supporterUsername) throws SQLException, ClassNotFoundException {
        String command = "SELECT * From Supporters WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, supporterUsername);
        return !preparedStatement.executeQuery().next();
    }

    public static FileInputStream getSupporterDefaultImageInputStream() throws FileNotFoundException {
        String fileName = "database\\Images\\Supporter\\1.png";
        return new FileInputStream(fileName);
    }

    public static boolean isPasswordCorrectForSupporter(String supporterUsername, String supporterPassword) throws SQLException, ClassNotFoundException {
        String command = "SELECT Password From Supporters WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, supporterUsername);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return supporterPassword.equals(resultSet.getString("Password"));
    }

    public static void deleteSupporter(String supporterUsername) throws SQLException, ClassNotFoundException {
        String command = "DELETE FROM Supporters WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, supporterUsername);
        preparedStatement.execute();
    }

    public static Supporter getSupporter(String supporterUsername) throws SQLException, ClassNotFoundException {
        String command = "SELECT * From Supporters WHERE Username = ?";
        PreparedStatement preparedStatement = getConnection().prepareStatement(command);
        preparedStatement.setString(1, supporterUsername);
        return new Supporter(preparedStatement.executeQuery());
    }
}
