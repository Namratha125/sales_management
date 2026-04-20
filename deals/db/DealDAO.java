package deals.db;

import deals.exception.DealException.*;
import deals.model.Deal;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DealDAO {

    private AbstractSubsystem facade;

    public DealDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in DealDAO");
        }
    }

    public void addDeal(Deal deal) throws DealCreationFailed {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_id", deal.getCustomerId());
            data.put("amount", deal.getAmount());
            data.put("stage", deal.getStage());
            data.put("status", deal.getStatus());
            
            facade.create("deals", data, "integration_lead");
        } catch (Exception e) {
            throw new DealCreationFailed("Failed to save deal to AWS.");
        }
    }

    public void createDeal(Deal deal) throws DealCreationFailed {
        addDeal(deal);
    }

    public Deal getDeal(int dealId) throws DealNotFound {
        try {
            Map<String, Object> rs = facade.readById("deals", "deal_id", dealId, "integration_lead");
            
            if (rs != null && !rs.isEmpty()) {
                return mapRowToDeal(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new DealNotFound("Deal ID " + dealId + " not found.");
    }

    public Deal getDealById(int dealId) throws DealNotFound {
        return getDeal(dealId);
    }

    public List<Deal> getAllDeals() {
        List<Deal> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("deals", new HashMap<>(), "integration_lead");
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToDeal(row));
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public List<Deal> getDealsByCustomer(int customerId) {
        return getAllDeals().stream()
                .filter(deal -> deal.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public List<Deal> getDealsByStage(String stage) {
        return getAllDeals().stream()
                .filter(deal -> stage.equals(deal.getStage()))
                .collect(Collectors.toList());
    }

    public boolean updateDealStage(int dealId, String newStage, String newStatus) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("stage", newStage);
            data.put("status", newStatus);

            facade.update("deals", "deal_id", dealId, data, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteDeal(int dealId) {
        try {
            facade.delete("deals", "deal_id", dealId, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Deal mapRowToDeal(Map<String, Object> rs) {
        Deal deal = new Deal();
        if (rs.get("deal_id") instanceof Number) {
            deal.setDealId(((Number) rs.get("deal_id")).intValue());
        }
        if (rs.get("customer_id") instanceof Number) {
            deal.setCustomerId(((Number) rs.get("customer_id")).intValue());
        }
        if (rs.get("amount") instanceof Number) {
            deal.setAmount(((Number) rs.get("amount")).doubleValue());
        }
        deal.setStage((String) rs.get("stage"));
        deal.setStatus((String) rs.get("status"));
        return deal;
    }
}