package server.controller;

import server.controller.account.AdminControl;
import server.model.db.AccountTable;
import server.model.db.CartTable;
import server.model.db.DiscountTable;
import server.model.db.OffTable;
import server.model.existence.Account;
import notification.Notification;
import static server.controller.Lock.*;


import java.sql.SQLException;

public class IOControl implements Validity {
    private static IOControl ioControl = null;

    public Notification register(Account account) {
        synchronized (IO_LOCK) {
            if (account.getUsername().length() < 6 || account.getUsername().length() > 16)
                return Notification.ERROR_USERNAME_LENGTH;
            if (!isUsernameValid(account.getUsername()))
                return Notification.ERROR_USERNAME_FORMAT;
            if (account.getPassword().length() < 8 || account.getPassword().length() > 16)
                return Notification.ERROR_PASSWORD_LENGTH;
            if (!this.isPasswordValid(account.getPassword()))
                return Notification.ERROR_PASSWORD_FORMAT;
            if (account.getFirstName().length() > 25)
                return Notification.ERROR_FIRST_NAME_LENGTH;
            if (account.getLastName().length() > 25)
                return Notification.ERROR_LAST_NAME_LENGTH;
            try {
                if (AccountTable.isUsernameFree(account.getUsername())) {
                    AccountTable.addAccount(account);
                    return Notification.REGISTER_SUCCESSFUL;
                } else
                    return Notification.ERROR_FULL_USERNAME;
            } catch (SQLException | ClassNotFoundException e) {
                return Notification.UNKNOWN_ERROR;
            }
        }
    }

    public Notification login(Account account){
        try {
            if(isUsernameValid(account.getUsername())) {
                synchronized (SUPPORTER_LOCK) {
                    if (!AccountTable.isUsernameFreeForSupporter(account.getUsername())) {
                        if (AccountTable.isPasswordCorrectForSupporter(account.getUsername(), account.getPassword())) {
                            return Notification.LOGIN_SUCCESSFUL;
                        } else
                            return Notification.WRONG_PASSWORD;
                    }
                }
                synchronized (IO_LOCK) {
                    if (!AccountTable.isUsernameFree(account.getUsername())) {
                        if (AccountTable.isPasswordCorrect(account.getUsername(), account.getPassword())) {
                            if (AccountTable.isUserApproved(account.getUsername())) {
                                String type = AccountTable.getTypeByUsername(account.getUsername());
                                if (type.equals("Customer")) {
                                    CartTable.addTempToUsername(account.getUsername());
                                }
                                CartTable.removeTemp();
                                if (type.equals("Customer") && AccountTable.didPeriodPass("Ya Zahra"))
                                    AdminControl.getController().getGiftDiscount();
                                DiscountTable.removeOutDatedDiscounts();
                                OffTable.removeOutDatedOffs();
                                return Notification.LOGIN_SUCCESSFUL;
                            } else {
                                return Notification.USER_NOT_APPROVED;
                            }
                        } else
                            return Notification.WRONG_PASSWORD;
                    } else
                        return Notification.ERROR_FREE_USERNAME;
                }
            } else {
                return Notification.FUCK_YOU;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return Notification.UNKNOWN_ERROR;
        }
    }

    public boolean isThereAdmin() {
        synchronized (IO_LOCK) {
            try {
                return AccountTable.isThereAdmin();
            } catch (Exception e) {
                //:)
                return false;
            }
        }
    }

    public static IOControl getController() {
        if (ioControl == null)
            ioControl = new IOControl();
        return ioControl;
    }
}