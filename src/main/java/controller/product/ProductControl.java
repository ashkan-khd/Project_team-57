package controller.product;

import controller.Control;
import model.db.ProductTable;
import model.db.VendorTable;
import model.existence.Product;
import notification.Notification;

import java.sql.SQLException;
import java.util.ArrayList;

public class ProductControl extends Control {
    private static ProductControl productControl = null;


    public Product getProductById(String productId) {
        try {
            return ProductTable.getProductByID(productId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Notification removeProductById(String productId) {
        try {
            ProductTable.removeProductByID(productId);
            return Notification.REMOVE_PRODUCT_SUCCESSFULLY;
        } catch (Exception e) {
            return Notification.UNKNOWN_ERROR;
        }
    }


    public String getProductMenuType()
    {
        if(isLoggedIn())
        {
            if(getType().equals("Admin"))
                return "Admin Product Menu";
            else if(getType().equals("Customer")) {
                //TODO
            }
            else{
                //TODO
            }
        }
        else
        {
            return "Not Logged In Product Menu";
        }
        return null;
    }

    public static ProductControl getController(){
        if (productControl == null)
            productControl = new ProductControl();
        return productControl;
    }

    public ArrayList<String> getAllProductNames()
    {
        ArrayList<String> allProductNames = new ArrayList<>();
        try {
            for (Product product : ProductTable.getAllProducts()) {
                allProductNames.add(product.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return allProductNames;
    }

    public ArrayList<String> getAllProductIDs()
    {
        ArrayList<String> allProductIDs = new ArrayList<>();
        try {
            for (Product product : ProductTable.getAllProducts()) {
                allProductIDs.add(product.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return allProductIDs;
    }

    public Notification editField(String fieldName, String newField, String ID) {
        try {
            if(ProductTable.getProductByID(ID).getStatus() == 2)
                return Notification.PRODUCT_NOT_AVAILABLE;
            else if (VendorTable.getFieldWithName(fieldName, ID).equals(newField))
                return Notification.SAME_FIELD_ERROR;

            if (!VendorTable.isThereProductWithID(ID))
                VendorTable.addToTable(ProductTable.getProductByID(ID));


            VendorTable.editFieldWithName(fieldName, newField, ID);
            return Notification.EDIT_FIELD_SUCCESSFULLY;
        } catch (SQLException e) {
            return Notification.UNKNOWN_ERROR;
        } catch (ClassNotFoundException e) {
            return Notification.UNKNOWN_ERROR;
        }
    }

    public Product getEditedProductByID(String ID) {
        return ProductTable.getEditingProductWithID(ID);
    }
}
