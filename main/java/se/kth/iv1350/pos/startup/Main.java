package se.kth.iv1350.pos.startup;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.integration.SystemCreator;
import se.kth.iv1350.pos.integration.ReceiptPrinter;
import se.kth.iv1350.pos.model.CashRegister;
import se.kth.iv1350.pos.util.FileLogHandler;
import se.kth.iv1350.pos.view.View;
import se.kth.iv1350.pos.discount.DiscountHandler;

import java.io.IOException;

/**
 * This is the start sequence of the entire application.
 */
public class Main {
    /**
     * Starts the application.
     * 
     * @param args The application does not take any command line parameters.
     */
    public static void main(String[] args) {
        SystemCreator systemCreator = new SystemCreator();
        ReceiptPrinter printer = new ReceiptPrinter();
        CashRegister cashRegister = new CashRegister();
        DiscountHandler discountHandler = new DiscountHandler();

        FileLogHandler logHandler = null;
        try {
            logHandler = FileLogHandler.getInstance();
        } catch (IOException e) {
            System.err.println("Failed to initialize log handler: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            Controller controller = new Controller(systemCreator, cashRegister, printer, logHandler, discountHandler);
            View view = new View(controller, logHandler);
            view.sampleExecutionWithoutDiscount();
        } catch (IOException e) {
            System.err.println("Failed to initialize view: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
