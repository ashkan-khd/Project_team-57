package view.menu;

import controller.Control;
import model.existence.Account;
import view.process.Processor;

public class UserMenu extends ListicOptionMenu {
    private Account account;

    public Menu execute(){
        processor = Processor.findProcessorWithName(processorName);
        Menu nextMenu = this;
        boolean flag = true;
        int input = 0;

        while(flag){
            try {
                input = Integer.parseInt(scanner.nextLine().trim());

                if (input > options.size() || input < 0)
                    throw new InputIsBiggerThanExistingNumbers("Invalid Number!!! \nWhat are you doing, man?!");
                else
                    flag = false;

            } catch (NumberFormatException e) {
                System.out.println("Please Enter An Integer");
            } catch (NullPointerException e) {
                System.out.println("Please Enter An Integer");
            } catch (InputIsBiggerThanExistingNumbers e) {
                System.out.println(e.getMessage());
            }
        }

        if(input != 0 && processor.isThereFunctionWithName(options.get(input - 1)))
        {
            nextMenu = processor.executeTheFunctionWithName(options.get(input - 1), this.getParentMenu(), account.getUsername());
        }

        return nextMenu;
    }

    protected void printOptionSpecs(){
        printCustomLine();

        printWithNullChecking("UserName", account.getUsername());
        printCustomLine();

        printWithNullChecking("PassWord", "************");
        printCustomLine();

        printWithNullChecking("Account Type", account.getType());
        printCustomLine();

        printWithNullChecking("FirstName", account.getFirstName());
        printCustomLine();

        printWithNullChecking("LastName", account.getLastName());
        printCustomLine();

        printWithNullChecking("Email", account.getEmail());
        printCustomLine();

        if(account.getType().equals("Vendor")){
            printWithNullChecking("Brand", account.getBrand());
            printCustomLine();
        }
        if(!account.getType().equals("Admin"))
        {
            System.out.format("| %-15s | %-35f | %n", "Credit", account.getCredit());
            printCustomLine();
        }
    }

    private void printWithNullChecking(String fieldName, String fieldValue){
        if(fieldValue == null)
            System.out.format("| %-15s | %-35s | %n", fieldName, "Not Assigned");
        else
            System.out.format("| %-15s | %-35s | %n", fieldName, fieldValue);
    }

    private void printCustomLine(){
        System.out.println("+-----------------+-------------------------------------+");
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}