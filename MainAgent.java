/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.PrintStream;
import java.util.ArrayList;

public class MainAgent extends Agent {

    private GUI gui;
    private AID[] playerAgents;
    private String[] playerNames;
    private int[] score;
    public int rondasTotales= 0;
   GameParametersStruct parameters = new GameParametersStruct();

    @Override
    protected void setup() {
        gui = new GUI(this);
        System.setOut(new PrintStream(gui.getLoggingOutputStream()));

        updatePlayers();
        gui.logLine("Agent " + getAID().getName() + " is ready.");
    }

    public int updatePlayers() {
        gui.logLine("Updating player list");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                gui.logLine("Found " + result.length + " players");
            }
            playerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                playerAgents[i] = result[i].getName();
            }
        } catch (FIPAException fe) {
            gui.logLine(fe.getMessage());
        }
        //Provisional
        playerNames = new String[playerAgents.length];
        score = new int[playerAgents.length];
        for (int i = 0; i < playerAgents.length; i++) {
            playerNames[i] = playerAgents[i].getName().split("@")[0];
        }
        gui.setPlayersUI(playerNames);
        return 0;
    }

    public int newGame() {
        addBehaviour(new GameManager());
        return 0;
    }

    /**
     * In this behavior this agent manages the course of a match during all the
     * rounds.
     */
    private class GameManager extends SimpleBehaviour {

        @Override
        public void action() {
            //Assign the IDs
            ArrayList<PlayerInformation> players = new ArrayList<>();
            int lastId = 0;
            for (AID a : playerAgents) {
                players.add(new PlayerInformation(a, lastId++));
            }

            //Initialize (inform ID)
            for (PlayerInformation player : players) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Id#" + player.id + "#" + parameters.N + "," + parameters.S + "," + parameters.R + "," + parameters.I + "," + parameters.P);
                msg.addReceiver(player.aid);
                send(msg);
            }
            //Organize the matches
         // for(int k=0;k<parameters.R;k++){ numero de rondaS?
         
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) { //too lazy to think, let's see if it works or it breaks
                    playGame(players.get(i), players.get(j));
                }
            }
         // }
        }

        private void playGame(PlayerInformation player1, PlayerInformation player2) {
            //Assuming player1.id < player2.id
            rondasTotales++;
            System.out.println("RONDA :" + rondasTotales);
            if(rondasTotales==parameters.I){
                rondasTotales=0;
                gui.updateMatrix();
            }
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("NewGame#" + player1.id + "," + player2.id);
            send(msg);

            int pos1=0, pos2=0;

            msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent("Position");
            msg.addReceiver(player1.aid);
            send(msg);

        //    gui.logLine("Main Waiting for movement");
            ACLMessage move1 = blockingReceive();
        //    gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
            pos1 = Integer.parseInt(move1.getContent().split("#")[1]);
           // gui.logLine("POSICION1: "+pos1);

            msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent("Position");
            msg.addReceiver(player2.aid);
            send(msg);

         //   gui.logLine("Main Waiting for movement");
            ACLMessage move2 = blockingReceive();
          //  gui.logLine("Main Received " + move2.getContent() + " from " + move2.getSender().getName());
            pos2 = Integer.parseInt(move2.getContent().split("#")[1]);
           // gui.logLine("POSICION2: "+pos2);
            
            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            
            Object resultado=GUI.data[pos1][pos2];
            msg.setContent("Results#"+pos1+","+pos2+"#"+resultado.toString());
            gui.logLine2("SCORE");
            for(int i=0; i<playerNames.length;i++){
                if(playerNames[i].equals(move1.getSender().getName().split("@")[0])){
                    score[i]= score[i]+ Integer.parseInt(resultado.toString().split(",")[0]);
                }
                if(playerNames[i].equals(move2.getSender().getName().split("@")[0])){
                    score[i]= score[i]+ Integer.parseInt(resultado.toString().split(",")[1]);
                }
                
            }
            
            for(int i=0; i< playerNames.length;i++){
                gui.logLine2(playerNames[i]+" - "+score[i]);
            }
           // gui.logLine2("PLAYER "+move1.getSender().getName().split("@")[0] +" -> "+ resultado.toString().split(",")[0]);
           // gui.logLine2("PLAYER "+move2.getSender().getName().split("@")[0] +" -> "+ resultado.toString().split(",")[1]);
            send(msg);
            msg.setContent("EndGame");
            send(msg);
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    public class PlayerInformation {

        AID aid;
        int id;

        public PlayerInformation(AID a, int i) {
            aid = a;
            id = i;
        }

        @Override
        public boolean equals(Object o) {
            return aid.equals(o);
        }
    }

    public class GameParametersStruct {

        int N;
        int S;
        int R;
        int I;
        int P;

        public GameParametersStruct() {
            N = 4;
            S = 4;
            R = 100;
            I = 20;
            P = 20;
        }
    }
}
