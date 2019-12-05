package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.io.*;

public class ServiceAgentEx3 extends Agent {
    @Override
    protected void setup() {
        // Service Registration at DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());

        // Service 1
        ServiceDescription sd = new ServiceDescription();
        sd.setType("answers");
        sd.setName("Ex3");

        // Add service
        dfad.addServices(sd);

        // Service registration
        try {
            DFService.register(this, dfad);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        addBehaviour(new Ex3CyclicBehavior(this));
    }

    @Override
    protected void takeDown() {
        // Service de-registration
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }
    }

    public String makeRequest(String serviceName, String word) {
        StringBuffer response = new StringBuffer();

        try {
            URL url = new URL("http://www.dict.org/bin/Dict");
            URLConnection urlConnection;
            DataOutputStream dataOutputStream;
            DataInputStream dataInputStream;

            urlConnection = url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(serviceName) + "&Query=" + URLEncoder.encode(word);

            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();

            dataInputStream = new DataInputStream(urlConnection.getInputStream());

            String string;

            while (((string = dataInputStream.readLine())) != null) {
                response.append(string);
            }

            dataInputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response.substring(response.indexOf("<hr>") + 4, response.lastIndexOf("<hr>"));
    }
}

class Ex3CyclicBehavior extends CyclicBehaviour {
    private ServiceAgentEx3 agent;

    public Ex3CyclicBehavior(ServiceAgentEx3 agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage message = agent.receive();

        if (message == null) {
            block();
        } else  {
            String content = message.getContent();

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String response = "";

            try {
                response = agent.makeRequest(message.getOntology(), content);
            } catch (NumberFormatException ex) {
                response = ex.getMessage();
            }

            reply.setContent(response);
            agent.send(reply);
        }
    }
}

