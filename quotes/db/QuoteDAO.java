package quotes.db;

import quotes.model.Quote;
import quotes.exception.QuoteException.*;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;
import com.erp.sdk.exception.UnauthorizedResourceAccessException; 

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteDAO {

    private AbstractSubsystem facade;

    public QuoteDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in QuoteDAO");
        }
    }

    public void createQuote(Quote quote) throws QuoteGenerationFailed {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_id", quote.getCustomerId());
            data.put("deal_id", quote.getDealId());
            data.put("total_amount", quote.getTotalAmount());
            data.put("discount", quote.getDiscount());
            data.put("final_amount", quote.getFinalAmount());
            
            facade.create("quotes", data, "integration_lead");
            
        } catch (Exception e) {
            throw new QuoteGenerationFailed("Could not generate quote in the RDS database.");
        }
    }

    public Quote getQuoteById(int quoteId) throws QuoteGenerationFailed {
        try {
            Map<String, Object> rs = facade.readById("quotes", "quote_id", quoteId, "integration_lead");
            
            if (rs != null && !rs.isEmpty()) {
                return mapRowToQuote(rs);
            }
            throw new QuoteGenerationFailed("Quote ID " + quoteId + " not found.");
        } catch (QuoteGenerationFailed e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuoteGenerationFailed("Database error retrieving quote.");
        }
    }

    public List<Quote> getAllQuotes() {
        List<Quote> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("quotes", new HashMap<>(), "integration_lead");
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToQuote(row));
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public boolean updateQuoteDiscount(int quoteId, double newDiscount, double newFinalAmount) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("discount", newDiscount);
            data.put("final_amount", newFinalAmount);

            facade.update("quotes", "quote_id", quoteId, data, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteQuote(int quoteId) {
        try {
            facade.delete("quotes", "quote_id", quoteId, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteQuoteItem(int quoteItemId) {
        try {
            facade.delete("quote_items", "quote_item_id", quoteItemId, "integration_lead");
            System.out.println("Quote item deleted successfully.");
            
        } catch (UnauthorizedResourceAccessException e) {
            System.err.println("SECURITY BLOCK: The Integration Team has disabled DELETE permissions for Quote Items.");
        } catch (Exception e) {
            System.err.println("A database error occurred.");
        }
    }

    private Quote mapRowToQuote(Map<String, Object> rs) {
        Quote quote = new Quote();
        if (rs.get("quote_id") instanceof Number) {
            quote.setQuoteId(((Number) rs.get("quote_id")).intValue());
        }
        if (rs.get("customer_id") instanceof Number) {
            quote.setCustomerId(((Number) rs.get("customer_id")).intValue());
        }
        if (rs.get("deal_id") instanceof Number) {
            quote.setDealId(((Number) rs.get("deal_id")).intValue());
        }
        if (rs.get("total_amount") instanceof Number) {
            quote.setTotalAmount(((Number) rs.get("total_amount")).doubleValue());
        }
        if (rs.get("discount") instanceof Number) {
            quote.setDiscount(((Number) rs.get("discount")).doubleValue());
        }
        if (rs.get("final_amount") instanceof Number) {
            quote.setFinalAmount(((Number) rs.get("final_amount")).doubleValue());
        }
        return quote;
    }
}