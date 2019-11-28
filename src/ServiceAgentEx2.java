package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.io.*;

/**
 * This agent makes requests to fd-por-eng dictionary in dict.org
 */
public class ServiceAgentEx2 extends Agent {
    @Override
    protected void setup() {
        // Service Registration at DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());

        // Service 1
        ServiceDescription sd = new ServiceDescription();
        sd.setType("answers");
        sd.setName("por-eng");

        // Add service
        dfad.addServices(sd);

        // Service registration
        try {
            DFService.register(this, dfad);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        addBehaviour(new PorEngCyclicBehavior(this));
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

class PorEngCyclicBehavior extends CyclicBehaviour {
    private ServiceAgentEx2 agent;

    public PorEngCyclicBehavior(ServiceAgentEx2 agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.MatchOntology("por-eng");
        ACLMessage message = agent.receive(template);

        if (message == null) {
            block();
        } else  {
            String content = message.getContent();

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String response = "";

            try {
                response = agent.makeRequest("fd-por-eng", content);
            } catch (NumberFormatException ex) {
                response = ex.getMessage();
            }

            reply.setContent(response);
            agent.send(reply);
        }
    }


}
