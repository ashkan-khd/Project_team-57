package model.existence;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Off {
    private String offID;
    private String offName;
    private String vendorUsername;
    private int status;
    private Date startDate;
    private Date finishDate;
    private double offPercent;

    private ArrayList<String> productIDs = new ArrayList<>();

    /*public Off(ResultSet resultSet) throws SQLException {
        String offID = resultSet.getString("OffID");
        this.offID = resultSet.getString("OffID");
        this.offName = resultSet.getString("OffName");
        this.vendorUsername = resultSet.getString("VendorUsername");
        this.status = resultSet.getInt("Status");
        this.startDate = resultSet.getDate("StartDate");
        this.finishDate = resultSet.getDate("FinishDate");
        this.offPercent = resultSet.getDouble("OffPercent");
        productIDs.add(resultSet.getString("ProductID"));
        while (resultSet.getString("OffID").equals(offID) && resultSet.next()) {
            productIDs.add(resultSet.getString("ProductID"));
        }
    }*/

    public static Off makeOffByID(ResultSet resultSet) throws SQLException {
        Off off = new Off();
        String offID = resultSet.getString("OffID");
        off.setOffID(resultSet.getString("OffID"));
        off.setOffName(resultSet.getString("OffName"));
        off.setVendorUsername(resultSet.getString("VendorUsername"));
        off.setStartDate(resultSet.getDate("StartDate"));
        off.setFinishDate(resultSet.getDate("FinishDate"));
        off.setStatus(resultSet.getInt("Status"));
        off.setOffPercent(resultSet.getDouble("OffPercent"));
        ArrayList<String> productIDs = new ArrayList<>();
        productIDs.add(resultSet.getString("ProductID"));
        while (resultSet.getString("OffID").equals(offID) && resultSet.next()) {
            productIDs.add(resultSet.getString("ProductID"));
        }
        return off;
    }

    public Off() {
    }

    public String getOffID() {
        return offID;
    }

    public void setOffID(String offID) {
        this.offID = offID;
    }

    public String getOffName() {
        return offName;
    }

    public void setOffName(String offName) {
        this.offName = offName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public double getOffPercent() {
        return offPercent;
    }

    public void setOffPercent(double offPercent) {
        this.offPercent = offPercent;
    }

    public ArrayList<String> getProductIDs() {
        return productIDs;
    }

    public void setProductIDs(ArrayList<String> productIDs) {
        this.productIDs = productIDs;
    }

    public String getVendorUsername() {
        return vendorUsername;
    }

    public void setVendorUsername(String vendorUsername) {
        this.vendorUsername = vendorUsername;
    }

    public void addProductToOff(String productID) {
        this.productIDs.add(productID);
    }

    public void removeProductFromOff(String productID) {
        this.productIDs.remove(productID);
    }

    public boolean isThereProductInOff(String productID) {
        return this.productIDs.contains(productID);
    }
}
