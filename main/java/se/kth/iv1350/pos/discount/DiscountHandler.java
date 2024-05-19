package se.kth.iv1350.pos.discount;

import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.util.Amount;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles discount calculations based on customer ID and sale details.
 */
public class DiscountHandler {
    private Map<String, Double> customerDiscounts;

    /**
     * Creates a new instance of DiscountHandler with predefined discounts.
     */
    public DiscountHandler() {
        customerDiscounts = new HashMap<>();
        customerDiscounts.put("1234567890", 0.10); // 10% discount for customer ID 1234567890
        customerDiscounts.put("9876543210", 0.15); // 15% discount for customer ID 9876543210
    }

    /**
     * Calculates the discount amount for the given sale and customer ID.
     * 
     * @param sale The sale to calculate the discount for.
     * @param customerID The ID of the customer to apply discounts.
     * @return The calculated discount amount.
     */
    public Amount getDiscountAmount(Sale sale, String customerID) {
        double discountRate = 0;

        // Check for customer ID based discount
        if (customerDiscounts.containsKey(customerID)) {
            discountRate = customerDiscounts.get(customerID);
        }

        Amount itemBasedDiscount = calculateItemBasedDiscount(sale);
        Amount totalCostBasedDiscount = calculateTotalCostBasedDiscount(sale, discountRate);
        Amount totalDiscount = new Amount(itemBasedDiscount.getAmount() + totalCostBasedDiscount.getAmount());

        // Ensure discount does not exceed total price
        if (totalDiscount.getAmount() > sale.getTotalPriceIncludingVAT().getAmount()) {
            totalDiscount = sale.getTotalPriceIncludingVAT();
        }

        return totalDiscount;
    }

    private Amount calculateItemBasedDiscount(Sale sale) {
        return new Amount(20);
    }

    private Amount calculateTotalCostBasedDiscount(Sale sale, double discountRate) {
        // Implement total cost based discount logic here
        double totalCost = sale.getTotalPriceIncludingVAT().getAmount();
        double discountAmount = totalCost * discountRate;
        return new Amount((int) discountAmount);
    }
}
