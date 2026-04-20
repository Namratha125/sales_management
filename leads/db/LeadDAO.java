package leads.db;

import leads.exception.LeadException;
import leads.model.Lead;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeadDAO {

    private AbstractSubsystem facade;

    public LeadDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in LeadDAO");
        }
    }

    public void createLead(Lead lead) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", lead.getName());
            data.put("company", lead.getCompany());
            data.put("status", lead.getStatus());

            facade.create("leads", data, "integration_lead");
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("Database error: " + e.getMessage(), e);
        }
    }

    public Lead getLeadById(int leadId) {
        try {
            Map<String, Object> rs = facade.readById("leads", "lead_id", leadId, "integration_lead");

            if (rs != null && !rs.isEmpty()) {
                return mapRowToLead(rs);
            }
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB error retrieving lead", e);
        }
        throw new LeadException.LeadNotFound(leadId);
    }

    public List<Lead> getAllLeads() {
        List<Lead> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("leads", new HashMap<>(), "integration_lead");

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToLead(row));
                }
            }
            return results;
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB error retrieving all leads", e);
        }
    }

    public List<Lead> getLeadsByStatus(String status) {
        return getAllLeads().stream()
                .filter(lead -> status.equals(lead.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean updateLeadStatus(int leadId, String newStatus) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("status", newStatus);

            facade.update("leads", "lead_id", leadId, data, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteLead(int leadId) {
        try {
            facade.delete("leads", "lead_id", leadId, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Lead mapRowToLead(Map<String, Object> rs) {
        Lead lead = new Lead();
        if (rs.get("lead_id") instanceof Number) {
            lead.setLeadId(((Number) rs.get("lead_id")).intValue());
        }
        lead.setName((String) rs.get("name"));
        lead.setCompany((String) rs.get("company"));
        lead.setStatus((String) rs.get("status"));

        Object ts = rs.get("created_at");
        if (ts instanceof java.sql.Timestamp) {
            lead.setCreatedAt(((java.sql.Timestamp) ts).toLocalDateTime());
        } else if (ts instanceof String) {
            try {
                lead.setCreatedAt(LocalDateTime.parse((String) ts, DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception ignored) {}
        }
        return lead;
    }
}