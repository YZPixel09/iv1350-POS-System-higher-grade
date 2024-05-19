package se.kth.iv1350.pos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import se.kth.iv1350.pos.integration.Item;
import se.kth.iv1350.pos.integration.ItemDTO;
import se.kth.iv1350.pos.integration.SystemCreator;
import se.kth.iv1350.pos.util.Amount;

/**
 * This class contains everything a sale should contain and methods to perform a sale.
 */
public class Sale {
    private LocalDateTime startTime;
    private List<Item> items;
    private Amount totalPrice;
    private double totalVAT;
    private SystemCreator externalSystemCreator;
    private List<SaleObserver> saleObservers = new ArrayList<>();
    private Amount discountAmount;

    /**
     * Creates a new instance of a sale, initializing the combined start time and date,
     * and initial empty lists for items.
     */
    public Sale() {
        this.startTime = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.totalPrice = new Amount(0);
        this.totalVAT = 0;
        this.discountAmount = new Amount(0);
    }

    /**
     * Sets the external systems creator for the sale.
     * @param systemCreator the external systems creator.
     */
    public void setSystemCreator(SystemCreator systemCreator) {
        this.externalSystemCreator = systemCreator;
    }

    /**
     * Adds an item to the current sale based on the provided item details and quantity.
     * If the item already exists in the sale, it increases the quantity;
     * otherwise, it adds a new item to the sale list.
     * @param itemDTO Item details to add, encapsulated in ItemDTO.
     * @param quantity Quantity of the item to add.
     */
    public void addItemToSale(ItemDTO itemDTO, int quantity) {
        Item item = findItem(itemDTO.getItemIdentifier());
        if (item != null) {
            item.increaseQuantity(quantity);
        } else {
            items.add(new Item(itemDTO, quantity));
        }
        updateTotalPriceAndVAT();
    }

    /**
     * Completes the sale by calculating the total price including VAT and notifying all registered observers.
     * This method should be called once all items have been added to the sale and the sale is ready to be finalized.
     */
    public void completeSale() {
        double totalPriceIncludingVAT = getTotalPriceIncludingVAT().getAmount();
        notifyObservers(totalPriceIncludingVAT);
    }

    /**
     * Notifies all observers that have been registered to this sale about the total price including VAT.
     * Each observer will receive the total price as part of the notification.
     * 
     * @param totalPriceIncludingVAT The total price of the sale that observers are notified about.
     */
    private void notifyObservers(double totalPriceIncludingVAT) {
        for (SaleObserver observer : saleObservers) {
            observer.newSale(totalPriceIncludingVAT);
        }
    }

    /**
     * Returns the total price of the sale including VAT.
     * @return Total price including VAT as an Amount.
     */
    public Amount getTotalPriceIncludingVAT() {
        return new Amount(totalPrice.getAmount() - discountAmount.getAmount());
    }

    /**
     * Returns the total VAT for the sale.
     * @return Total VAT as a double.
     */
    public double getTotalVAT() {
        return totalVAT;
    }

    /**
     * Processes the payment for the sale, updates the external systems, and calculates the change due.
     * @param cashPayment Payment made for the sale.
     * @param cashRegister Register to record the payment.
     * @return Change due to the customer.
     */
    public Amount payment(CashPayment cashPayment, CashRegister cashRegister) {
        Amount change = cashRegister.addPayment(cashPayment);
        externalSystemCreator.getInventorySystem().updateInventory(this);
        externalSystemCreator.getAccountingSystem().updateAccounting(this);
        return change;
    }

    /**
     * Returns a list of all items in the current sale.
     * @return A shallow copy of the items list.
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Retrieves the quantity of a specific item in the sale based on its identifier.
     * @param itemIdentifier Identifier of the item.
     * @return Quantity of the specified item.
     */
    public int getQuantityOfItem(String itemIdentifier) {
        Item item = findItem(itemIdentifier);
        return item != null ? item.getQuantity() : 0;
    }

    /**
     * Creates and returns a receipt for the sale.
     * @param totalCost The total amount paid for the sale.
     * @return A new Receipt object.
     */
    public Receipt createReceipt(Amount totalCost) {
        Amount paidAmount = totalCost;
        CashPayment payment = new CashPayment(paidAmount, totalCost);
        return new Receipt(this, payment);
    }

    /**
     * Adds an observer to be notified when the sale is completed.
     * @param observer The observer to add.
     */
    public void addSaleObserver(SaleObserver observer) {
        saleObservers.add(observer);
    }

    /**
     * Adds a list of observers to be notified when the sale is completed.
     * @param observers The list of observers to add.
     */
    public void addSaleObservers(List<SaleObserver> observers) {
        saleObservers.addAll(observers);
    }

    /**
     * Applies a discount to the total price of the sale.
     * @param discountAmount The amount to be discounted from the total price.
     */
    public void applyDiscount(Amount discountAmount) {
        this.discountAmount = discountAmount;
    }

    // Private helper methods

    /**
     * Updates the total price and VAT based on the items in the sale.
     */
    private void updateTotalPriceAndVAT() {
        double totalPrice = 0;
        double totalVAT = 0;
        for (Item item : items) {
            double itemTotal = item.getPrice() * item.getQuantity();
            totalPrice += itemTotal;
            totalVAT += itemTotal * item.getVATRate();
        }
        this.totalPrice = new Amount((int) totalPrice);
        this.totalVAT = totalVAT;
    }

    /**
     * Finds an item in the sale by its identifier.
     * @param itemId The identifier of the item to find.
     * @return The Item if found, otherwise null.
     */
    private Item findItem(String itemId) {
        for (Item item : items) {
            if (item.getItemIdentifier().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the start time of the sale.
     * @return The start time of the sale.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the total price of the sale excluding VAT.
     * @return Total price excluding VAT as an Amount.
     */
    public Amount getTotalPrice() {
        return totalPrice;
    }

      /**
 * Returns the discount amount for the sale.
 * @return The discount amount as an Amount.
 */
public Amount getDiscountAmount() {
    return discountAmount;
}

}
