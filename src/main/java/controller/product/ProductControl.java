package controller.product;

import controller.Control;
import model.db.EditingProductTable;
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

            if (checkFieldEquality(fieldName, newField, ID))
                return Notification.SAME_FIELD_ERROR;

            if(ProductTable.getProductByID(ID).getStatus() == 1)
                ProductTable.setProductStatus(ID, 3);

            if (EditingProductTable.isIDFree(ID))
                EditingProductTable.addProduct(ProductTable.getProductByID(ID));

            editSpecificField(fieldName, newField, ID);
            return Notification.EDIT_FIELD_SUCCESSFULLY;
        } catch (SQLException e) {
            return Notification.UNKNOWN_ERROR;
        } catch (ClassNotFoundException e) {
            return Notification.UNKNOWN_ERROR;
        }
    }

    private void editSpecificField(String fieldName, String newField, String ID) {
        if(fieldName.equals("Name") || fieldName.equals("Brand") ||
                fieldName.equals("Category") || fieldName.equals("Description"))
            EditingProductTable.editFieldWithName(fieldName, newField, ID);

        else if(fieldName.equals("Count"))
            EditingProductTable.changeProductCount(Integer.parseInt(newField), ID);

        else if(fieldName.equals("Amount"))
            EditingProductTable.changeProductAmount(Double.parseDouble(newField), ID);

        else if(fieldName.equals("Price"))
            EditingProductTable.changeProductPrice(Double.parseDouble(newField), ID);

    }

    private boolean checkFieldEquality(String fieldName, String newField, String ID) {
        Product product = getEditedProductByID(ID);

        if(fieldName.equals("Name")) {
            return product.getName().equals(newField);
        } else if(fieldName.equals("Brand")) {
            return product.getBrand().equals(newField);
        } else if(fieldName.equals("Count")) {
            return Integer.parseInt(newField) == product.getCount();
        } else if(fieldName.equals("Amount")) {
            return Double.parseDouble(newField) == product.getAmount();
        } else if(fieldName.equals("Category")) {
            return product.getCategory().equals(newField);
        } else if(fieldName.equals("Description")) {
            return product.getDescription().equals(newField);
        } else if(fieldName.equals("Price")) {
            return Double.parseDouble(newField) == product.getPrice();
        }

        return true;
    }

    public Product getEditedProductByID(String ID) {
        try {
            if(EditingProductTable.isIDFree(ID)) {
                return ProductTable.getProductByID(ID);
            } else {
                return EditingProductTable.getEditingProductWithID(ID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
