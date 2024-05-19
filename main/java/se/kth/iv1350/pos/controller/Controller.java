package se.kth.iv1350.pos.controller;

import se.kth.iv1350.pos.model.Receipt;
import se.kth.iv1350.pos.model.CashPayment;
import se.kth.iv1350.pos.integration.SystemCreator;
import se.kth.iv1350.pos.model.CashRegister;

import java.util.ArrayList;
import java.util.List;

import se.kth.iv1350.pos.integration.DatabaseUnavailableException;
import se.kth.iv1350.pos.integration.ItemDTO;
import se.kth.iv1350.pos.integration.ItemNotFoundException;
import se.kth.iv1350.pos.integration.ReceiptPrinter;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.util.Amount;
import se.kth.iv1350.pos.util.FileLogHandler;
import se.kth.iv1350.pos.discount.DiscountHandler;

/**
 * This is the application's only controller.
 * All calls passing to the model go through this class.
 */
public class Controller {
    private SystemCreator systemCreator;
    public Sale sale;
    private CashRegister cashRegister;
    private ReceiptPrinter printer;
    private FileLogHandler logger;
    private DiscountHandler discountHandler;
    private List<SaleObserver> saleObservers = new ArrayList<>();

    /**
     * Creates a new instance of controller.
     * 
     * @param systemCreator Initializes systems for accounting and inventory.
     * @param cashRegister The cash register used for processing payments.
     * @param printer The printer used for printing receipts.
     * @param logger The logger used for logging exceptions.
     * @param discountHandler The discount handler for managing discounts.
     */
    public Controller(SystemCreator systemCreator, CashRegister cashRegister, ReceiptPrinter printer, FileLogHandler logger, DiscountHandler discountHandler) {
        this.systemCreator = systemCreator;
        systemCreator.getAccountingSystem();
        systemCreator.getInventorySystem();
        this.cashRegister = cashRegister;
        this.printer = printer;
        this.logger = logger;
        this.discountHandler = discountHandler;
    }

    /**
     * Starts a new sale.
     */
    public void makeNewSale() {
        this.sale = new Sale();
        sale.addSaleObservers(saleObservers);  // Add observers to the sale
    }

    /**
     * Adds an item to the current sale after retrieving its details from the external inventory system.
     * 
     * @param itemIdentifier The unique identifier for the item.
     * @param quantity The quantity of the item to add.
     * @throws ItemNotFoundException if the item does not exist.
     * @throws OperationFailedException if there is a failure in the operation.
     * @return The item details if found, throws exception otherwise.
     */
    public ItemDTO enterItem(String itemIdentifier, Amount quantity) throws ItemNotFoundException, OperationFailedException {
        if (itemIdentifier == null || itemIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Item identifier cannot be null or empty.");
        }
        if (quantity == null || quantity.getAmount() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    
        try {
            ItemDTO foundItem = systemCreator.getInventorySystem().findItem(itemIdentifier);
            if (foundItem != null) {
                sale.addItemToSale(foundItem, (int) quantity.getAmount());
                return foundItem;
            } else {
                throw new ItemNotFoundException("Item with identifier: " + itemIdentifier + " not found, please try again.");
            }
        } catch (DatabaseUnavailableException dataExc) {
            logger.logException(dataExc);  
            throw new OperationFailedException("Inventory system server is down.", dataExc);
        } catch (ItemNotFoundException itemNotFoundExc) {
            logger.logException(itemNotFoundExc);  
            throw itemNotFoundExc;
        } catch (Exception exp) {
            logger.logException(exp);  
            throw new OperationFailedException("An unexpected error occurred while processing the item.", exp);
        }
    }

    /**
     * Processes payment for the items in the current sale, calculates change, updates systems, and prints the receipt.
     * 
     * @param paidAmount The amount of money paid by the customer.
     * @param customerID The ID of the customer for applying discounts.
     * @return The change to be returned to the customer.
     */
    /**
     * Processes the payment with optional customer ID for discount.
     */
    public Amount payment(Amount paidAmount, String customerID) {
        if (customerID != null && !customerID.isEmpty()) {
            Amount discountAmount = discountHandler.getDiscountAmount(sale, customerID);
            sale.applyDiscount(discountAmount);
        }

        CashPayment payment = new CashPayment(paidAmount, sale.getTotalPriceIncludingVAT());
        Amount change = cashRegister.addPayment(payment);

        systemCreator.getInventorySystem().updateInventory(sale);
        systemCreator.getAccountingSystem().updateAccounting(sale);

        Receipt receipt = new Receipt(sale, payment);
        printer.printReceipt(receipt);

        notifyObservers();

        return change;
    }

    /**
     * Processes the payment without customer ID.
     */
    public Amount paymentWithoutCustomerID(Amount paidAmount) {
        return payment(paidAmount, null);
    }

    /**
     * Notifies all registered observers of a new sale.
     */
    private void notifyObservers() {
        for (SaleObserver observer : saleObservers) {
            observer.newSale(sale.getTotalPriceIncludingVAT().getAmount());
        }
    }

    /**
     * Adds the specified observer to the list of saleObservers.
     * 
     * @param observer The observer to be added.
     */
    public void addSaleObserver(SaleObserver observer) {
        saleObservers.add(observer);
    }
}
