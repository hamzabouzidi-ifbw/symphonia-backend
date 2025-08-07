package com.example.tenant.Services;

import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.util.List;

@Service
public class TrunkService {

    @Autowired
    private TrunkRepository trunkRepository;

    @Autowired
    private TenantRepository tenantRepository;

    public Trunk createTrunk(Trunk trunk, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        trunk.setTenant(tenant);
        Trunk savedTrunk = trunkRepository.save(trunk);

        regenerateGatewayConfig(); // <== Génère le XML
        return savedTrunk;
    }

    public List<Trunk> getTrunksByTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return trunkRepository.findByTenant(tenant);
    }

    public void deleteTrunk(Long id) {
        trunkRepository.deleteById(id);
        regenerateGatewayConfig();
    }

/*
    public void regenerateGatewayConfig() {
        List<Trunk> trunks = trunkRepository.findAll();
        StringBuilder xml = new StringBuilder();

        xml.append("<include>\n");

        for (Trunk trunk : trunks) {
            if (!trunk.isActive()) continue;

            xml.append("  <gateway name=\"").append(trunk.getName()).append("\">\n");
            xml.append("    <param name=\"username\" value=\"").append(trunk.getUsername()).append("\"/>\n");
            xml.append("    <param name=\"password\" value=\"").append(trunk.getPassword()).append("\"/>\n");
            xml.append("    <param name=\"realm\" value=\"").append(trunk.getRealm()).append("\"/>\n");
            xml.append("    <param name=\"proxy\" value=\"").append(trunk.getProxy()).append("\"/>\n");
            xml.append("    <param name=\"register\" value=\"").append(trunk.isRegisterEnabled() ? "true" : "false").append("\"/>\n");
            xml.append("  </gateway>\n");
        }

        xml.append("</include>");

        try (FileWriter writer = new FileWriter("/etc/freeswitch/sip_profiles/external/gateways.xml")) {
            writer.write(xml.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write gateway config", e);
        }

        try {
            Runtime.getRuntime().exec("fs_cli -x 'reload mod_sofia'");
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload mod_sofia", e);
        }
    }
*/

    public String regenerateGatewayConfig() {
        List<Trunk> trunks = trunkRepository.findAll();
        StringBuilder xml = new StringBuilder();

        xml.append("<include>\n");

        for (Trunk trunk : trunks) {
            if (!trunk.isActive()) continue;

            xml.append("  <gateway name=\"").append(trunk.getName()).append("\">\n");
            xml.append("    <param name=\"username\" value=\"").append(trunk.getUsername()).append("\"/>\n");
            xml.append("    <param name=\"password\" value=\"").append(trunk.getPassword()).append("\"/>\n");
            xml.append("    <param name=\"realm\" value=\"").append(trunk.getRealm()).append("\"/>\n");
            xml.append("    <param name=\"proxy\" value=\"").append(trunk.getProxy()).append("\"/>\n");
            xml.append("    <param name=\"register\" value=\"").append(trunk.isRegisterEnabled() ? "true" : "false").append("\"/>\n");
            xml.append("  </gateway>\n");
        }

        xml.append("</include>");

        return xml.toString();
    }

}
