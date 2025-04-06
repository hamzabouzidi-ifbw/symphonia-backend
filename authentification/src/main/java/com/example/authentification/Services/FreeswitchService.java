package com.example.authentification.Services;

/*import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.springframework.beans.factory.annotation.Value;*/
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FreeswitchService {

/*
    @Value("${freeswitch.host}")
    private String fsHost;

    @Value("${freeswitch.port}")
    private int fsPort;

    @Value("${freeswitch.password}")
    private String fsPassword;

    private Client client;

    @PostConstruct
    public void init() {
        client = new Client();
        try {
            client.connect(fsHost, fsPort, fsPassword, 10);
        } catch (InboundConnectionFailure e) {
            throw new RuntimeException(e);
        }
        System.out.println("✅ Connecté à FreeSWITCH via ESL");
    }

    public void sendApiCommand(String command, String args) {
        if (!client.canSend()) {
            throw new RuntimeException("Connexion FreeSWITCH non disponible !");
        }
        EslMessage response = client.sendSyncApiCommand(command, args);
        System.out.println("🔧 Réponse FreeSWITCH : " + response.getBodyLines());
    }

    public void createSuperAdminContext(String email) {
        // Exemple : créer une extension ou une config
        String username = email.split("@")[0];
        String command = "reloadxml";
        sendApiCommand(command, "");

        // Tu peux aussi écrire un fichier XML config via template si nécessaire
        System.out.println("📞 SuperAdmin ajouté à FreeSWITCH : " + username);
    }*/
}