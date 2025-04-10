/*
package com.example.authentification.Config;


import org.freeswitch.esl.client.inbound.Client;

import org.freeswitch.esl.client.transport.message.EslMessage;
import org.springframework.stereotype.Service;

@Service
public class FreeSwitchService {

    private final Client client;

    public FreeSwitchService() {
        this.client = new Client();
    }

    public void connectToFreeSwitch() {
        try {
            client.connect("51.77.158.159", 8021, "ClueCon", 10); // Le mot de passe par défaut ESL est "ClueCon"
            System.out.println("Connecté à FreeSWITCH via ESL !");

            // Utiliser EslMessage au lieu de EslEvent
            EslMessage message = client.sendSyncApiCommand("status", "");
            System.out.println("Réponse : " + message.getBodyLines());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la connexion à FreeSWITCH");
        }
    }

}
*/
