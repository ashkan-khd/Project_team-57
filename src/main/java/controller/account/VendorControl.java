package controller.account;

import controller.Control;
import model.db.*;
import model.existence.Log;
import model.existence.Off;
import model.existence.Product;
import notification.Notification;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class VendorControl extends AccountControl{
    private static VendorControl vendorControl = null;
    private String currentProduct;


    public static VendorControl getController() {
        if (vendorControl == null)
            vendorControl = new VendorControl();

        return vendorControl;
    }

    public ArrayList<String> getVendorProductNames() {
        ArrayList<String> productNames = new ArrayList<>();
        try {
            for (Product product : VendorTable.getProductsWithUsername(Control.getUsername())) {
                productNames.add(product.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return productNames;
    }

    public ArrayList<String> getVendorProductIDs() {
        ArrayList<String> productsIDs = new ArrayList<>();
        try {
            for (Product product : VendorTable.getProductsWithUsername(Control.getUsername())) {
                productsIDs.add(product.getID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return productsIDs;
    }

    public ArrayList<Notification> addProduct(Product product)
    {
        ArrayList<Notification> addingProductNotifications = new ArrayList<>();

        try {
            addingProductNotifications.addAll(checkProductFields(product));

            if (addingProductNotifications.isEmpty()) {
                while (true) {
                    String productId = generateProductID();
                    if (ProductTable.isIDFree(productId)) {
                        product.setID(productId);
                        break;
                    }
                }
                if (product.isCountable())
                    VendorTable.addCountableProduct(product, getUsername());
                else
                    VendorTable.addUnCountableProduct(product, getUsername());

                addingProductNotifications.add(Notification.ADD_PRODUCT);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return addingProductNotifications;
    }

    public ArrayList<Notification> editProduct(Product product) {
        //Todo
        return null;
    }

    private ArrayList<Notification> checkProductFields(Product product) throws SQLException, ClassNotFoundException {
        ArrayList<Notification> checkNotifications = new ArrayList<>();

        if(product.getPrice() == 0) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_PRICE);
        } else if(product.getName() == null || product.getName().isEmpty()) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_NAME);
        } else if(product.getCategory() == null || product.getCategory().isEmpty()) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_CATEGORY);
        } else if(product.getCategory() != null && !CategoryTable.isThereCategoryWithName(product.getCategory())) {
            checkNotifications.add(Notification.INVALID_PRODUCT_CATEGORY);
        } else if(product.isCountable() && product.getCount() == 0) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_COUNT);
        } else if(!product.isCountable() && product.getAmount() == 0) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_AMOUNT);
        } else if(product.getBrand() == null || product.getBrand().isEmpty()) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_BRAND);
        } else if(product.getDescription() == null || product.getDescription().isEmpty()) {
            checkNotifications.add(Notification.EMPTY_PRODUCT_DESCRIPTION);
        }

        return checkNotifications;
    }

    private String generateProductID()
    {
        char[] validChars = {'0', '2', '1', '3', '5', '8', '4', '9', '7', '6'};
        StringBuilder ID = new StringBuilder("p");
        for(int i = 0; i < 7; ++i)
        {
            ID.append(validChars[((int) (Math.random() * 1000000)) % validChars.length]);
        }
        return ID.toString();
    }

    public boolean isThereCategoryWithName(String categoryName) {
        try {
            return CategoryTable.isThereCategoryWithName(categoryName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> getAllOffNames(){
        ArrayList<String> offs = new ArrayList<>();
        try {
            for (Off vendorOff : OffTable.getVendorOffs(Control.getUsername())) {
                offs.add(vendorOff.getOffName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return offs;
    }

    public ArrayList<String> getAllOffIDs(){
        ArrayList<String> offs = new ArrayList<>();
        try {
            for (Off vendorOff : OffTable.getVendorOffs(Control.getUsername())) {
                offs.add(vendorOff.getOffID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return offs;
    }

    public Notification addOff(Off off){
       if (off.getOffName() == null)
           return Notification.UNCOMPLETED_OFF_NAME;
       if (off.getFinishDate() == null)
           return Notification.NOT_SET_FINISH_DATE;
       if (off.getOffPercent() <= 0 || off.getOffPercent() >= 100)
           return Notification.OUT_BOUND_OF_PERCENT;
       if(off.getProductIDs() == null || off.getProductIDs().size() == 0)
           return Notification.EMPTY_OFF_PRODUCTS;
       off.setVendorUsername(Control.getUsername());
       try {
           do {
               off.setOffID(setOffID());
           } while (OffTable.isThereOffWithID(off.getOffID()));
           off.setStatus(2);
           OffTable.addOff(off);
           return Notification.ADD_OFF;
       } catch (SQLException e) {
           return Notification.UNKNOWN_ERROR;
       } catch (ClassNotFoundException e) {
           return Notification.UNKNOWN_ERROR;
       }
    }

    private String setOffID(){
        char[] validChars = {'0', '2', '1', '3', '5', '8', '4', '9', '7', '6'};
        StringBuilder offID = new StringBuilder("o");

        for(int i = 0; i < 7; ++i)
            offID.append(validChars[((int) (Math.random() * 1000000)) % validChars.length]);

        return offID.toString();
    }

    public ArrayList<String> getNonOffProductsNames() {
        ArrayList<String> nonOffProducts = new ArrayList<>();
        try {
            for (Product product : VendorTable.getProductsWithUsername(Control.getUsername())) {
                if(!OffTable.isThereProductInOff(product.getID()))
                    nonOffProducts.add(product.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return nonOffProducts;
    }

    public ArrayList<String> getNonOffProductsIDs() {
        ArrayList<String> nonOffProducts = new ArrayList<>();
        try {
            for (Product product : VendorTable.getProductsWithUsername(Control.getUsername())) {
                if(!OffTable.isThereProductInOff(product.getID()))
                    nonOffProducts.add(product.getID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return nonOffProducts;
    }

    public Notification editOffName(String offID, String offName)
    {
        try {
            if(OffTable.isThereEditingOffWithID(offID)) {
                Off off = OffTable.getSpecificEditingOffByID(offID);
                if(off.getOffName().equals(offName))
                    return Notification.DUPLICATE_OFF_VALUE;
                OffTable.editEditingOffName(off.getOffID() ,offName);
            } else {
                Off off = OffTable.getSpecificOff(offID);
                if(off.getOffName().equals(offName))
                    return Notification.DUPLICATE_OFF_VALUE;
                OffTable.changeOffStatus(offID, 3);
                off.setOffName(offName);
                off.setStatus(3);
                OffTable.addEditingOff(off);
            }
            return Notification.OFF_EDITED;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Notification.UNKNOWN_ERROR;
    }

    public Notification editOffFinishDate(String offID, Date date)
    {
        try {
            if(OffTable.isThereEditingOffWithID(offID)) {
                Off off = OffTable.getSpecificEditingOffByID(offID);
                if(off.getFinishDate().compareTo(date) == 0)
                    return Notification.DUPLICATE_OFF_VALUE;
                if(date.compareTo(new Date(System.currentTimeMillis())) != 1 || date.compareTo(off.getStartDate()) != 1)
                    return Notification.WRONG_OFF_FINISH_DATE;
                OffTable.editEditingOffFinishDate(off.getOffID() ,date);
            } else {
                Off off = OffTable.getSpecificOff(offID);
                if(off.getFinishDate().compareTo(date) == 0)
                    return Notification.DUPLICATE_OFF_VALUE;
                if(date.compareTo(new Date(System.currentTimeMillis())) != 1 || date.compareTo(off.getStartDate()) != 1)
                    return Notification.WRONG_OFF_FINISH_DATE;
                OffTable.changeOffStatus(offID, 3);
                off.setFinishDate(date);
                off.setStatus(3);
                OffTable.addEditingOff(off);
            }
            return Notification.OFF_EDITED;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Notification.UNKNOWN_ERROR;
    }

    public Notification editOffPercent(String offID, Double percent)
    {
        try {
            if(OffTable.isThereEditingOffWithID(offID)) {
                Off off = OffTable.getSpecificEditingOffByID(offID);
                if(off.getOffPercent() == percent)
                    return Notification.DUPLICATE_OFF_VALUE;
                if(!(percent > 0 && percent <= 100))
                    return Notification.INVALID_OFF_PERCENT;
                OffTable.editEditingOffPercent(off.getOffID(), percent);
            } else {
                Off off = OffTable.getSpecificOff(offID);
                if(off.getOffPercent() == percent)
                    return Notification.DUPLICATE_OFF_VALUE;
                if(!(percent > 0 && percent <= 100))
                    return Notification.INVALID_OFF_PERCENT;
                OffTable.changeOffStatus(offID, 3);
                off.setOffPercent(percent);
                off.setStatus(3);
                OffTable.addEditingOff(off);
            }
            return Notification.OFF_EDITED;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Notification.UNKNOWN_ERROR;
    }


    public Notification removeOffWithID(String offID) {
        try {
            if(OffTable.isThereEditingOffWithID(offID))
                OffTable.removeEditingOff(offID);
            OffTable.removeOffByID(offID);
            return Notification.OFF_REMOVED;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Notification.UNKNOWN_ERROR;
    }

    public ArrayList<String> getALlVendorLogsName() {
        try {
            ArrayList<String> allLogsName = new ArrayList<>();
            for (Log log : LogTable.getAllVendorLogs(Control.getUsername())) {
                java.util.Date date = new java.util.Date(log.getDate().getTime());
                allLogsName.add(date.toString());
            }
            return allLogsName;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getALlVendorLogsID() {
        try {
            ArrayList<String> allLogsID = new ArrayList<>();
            for (Log log : LogTable.getAllVendorLogs(Control.getUsername())) {
                allLogsID.add(log.getLogID());
            }
            return allLogsID;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Product> getAllProductsInSpecificLog() {
        try {
            ArrayList<Product> allProducts = new ArrayList<>();
            for (Log.ProductOfLog product : LogTable.getVendorLogByID(getCurrentLogID(), Control.getUsername()).getAllProducts()) {
                allProducts.add(ProductTable.getProductByID(product.getProductID()));
            }
            return allProducts;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getAllProductsNamesInSpecificLog() {
        ArrayList<String> allNames = new ArrayList<>();
        for (Product product : getAllProductsInSpecificLog()) {
            allNames.add(product.getName());
        }
        return allNames;
    }

    public ArrayList<String> getAllProductsIdsInSpecificLog(){
        ArrayList<String> allIDs = new ArrayList<>();
        for (Product product : getAllProductsInSpecificLog()) {
            allIDs.add(product.getID());
        }
        return allIDs;
    }

    public String getCustomerName(){
        try {
            return LogTable.getVendorLogByID(getCurrentLogID(), Control.getUsername()).getCustomerName();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Log getCurrentLog() {
        try {
            return LogTable.getVendorLogByID(AccountControl.getCurrentLogID(), Control.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getProductBuyerUsernames() {
        ArrayList<String> buyers = new ArrayList<>();
        try {
            for(String account: LogTable.getAllCustomerUsernamesForProduct(currentProduct))
            {
                buyers.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return buyers;
    }

    public String getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(String currentProduct) {
        this.currentProduct = currentProduct;
    }

    public double getMaxSale() {
        try {
            return LogTable.getMaxSaleByID(currentProduct);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getMaxCountOfSale() {
        try {
            return LogTable.getMaxCountOfSaleByProductID(currentProduct);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public double getMaxAmountOfSale() {
        try {
            return LogTable.getMaxAmountOfSaleByProductID(currentProduct);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public ArrayList<Product> getAllProducts() {
        try {
            return VendorTable.getProductsWithUsername(Control.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
