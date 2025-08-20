package com.example.tenant.Services;

import com.example.tenant.Dto.TrunkWithPoolDTO;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

        return savedTrunk;
    }

    public List<Trunk> getTrunksByTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return trunkRepository.findByTenant(tenant);
    }

    public void deleteTrunk(Long id) {
        trunkRepository.deleteById(id);
    }
    private void reloadFreeSwitch() {
        try {
            Runtime.getRuntime().exec("fs_cli -x 'reloadxml'");
            Runtime.getRuntime().exec("fs_cli -x 'sofia profile external rescan'");
            System.out.println("FreeSWITCH reloaded.");
        } catch (IOException e) {
            throw new RuntimeException("Impossible de recharger FreeSWITCH", e);
        }
    }


    public List<TrunkWithPoolDTO> getAllTrunksWithPools() {
        return trunkRepository.findAllTrunksWithPools();
    }
    public List<Trunk> getAllActiveTrunks() { return trunkRepository.findByActiveTrue(); }
    /*
    public void regenerateGatewayConfig() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            List<Trunk> trunks = trunkRepository.findByTenant(tenant);

            StringBuilder xml = new StringBuilder();
            xml.append("<include>\n");

            for (Trunk trunk : trunks) {
                if (!trunk.isActive()) continue;
                xml.append("  <gateway name=\"").append(trunk.getName()).append("\">\n")
                        .append("    <param name=\"username\" value=\"").append(trunk.getUsername()).append("\"/>\n")
                        .append("    <param name=\"password\" value=\"").append(trunk.getPassword()).append("\"/>\n")
                        .append("    <param name=\"realm\" value=\"").append(trunk.getRealm()).append("\"/>\n")
                        .append("    <param name=\"proxy\" value=\"").append(trunk.getProxy()).append("\"/>\n")
                        .append("    <param name=\"register\" value=\"").append(trunk.isRegisterEnabled() ? "true" : "false").append("\"/>\n")
                        .append("  </gateway>\n");
            }

            xml.append("</include>");

            try {
                String filePath = "/etc/freeswitch/sip_profiles/external/" + tenant.getDomainName() + "_gateways.xml";
                FileWriter writer = new FileWriter(filePath);
                writer.write(xml.toString());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Erreur d'écriture du fichier gateway pour " + tenant.getDomainName(), e);
            }
        }

        reloadFreeSwitch();
    }
*/


}