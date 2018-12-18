/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;

import java.util.Random;

public class IntelligentStateAgent extends Agent {

    private State state;
    private AID mainAgent;
    private int myId, opponentId;
    private int N, S, R, I, P;
    private ACLMessage msg;
    public Object[][] matrizTemporal;
    public String id;
    public boolean id1= false;
    public boolean matrizcargada=false;
    public int seleccion=0;
    public int rellenoMatriz;
    public int contador=0;
    public float[][] QValues;
    
    
    public int Qstate=0;
    public int contadorRepetidas=0;
    public int valorAnterior=0;
    
    public double dDecFactorLR = 0.999;			
    public double dEpsilon = 0.8; // el 80 % de las veces cogerá el mas probable				
    public double dLearnRate = 0.8;
    public double reward= 1;
    public double y = 0.95;
    private float outcomeActionValue;
    protected void setup() {
        state = State.s0NoConfig;
        
        //Register in the yellow pages as a player
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("Game");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new Play());
        System.out.println("RandomAgent " + getAID().getName() + " is ready.");

    }

    protected void takeDown() {
        //Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("RandomPlayer " + getAID().getName() + " terminating.");
    }

    private enum State {
        s0NoConfig, s1AwaitingGame, s2Round, s3AwaitingResult
    }

    private class Play extends CyclicBehaviour {
        Random random = new Random(1000);
        @Override
        public void action() {
            System.out.println(getAID().getName() + ":" + state.name());
            msg = blockingReceive();
            if (msg != null) {
               // System.out.println(getAID().getName() + " received " + msg.getContent() + " from " + msg.getSender().getName()); //DELETEME
                //-------- Agent logic
                switch (state) {
                    case s0NoConfig:
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> go to state 1
                        //Else ERROR
                        if (msg.getContent().startsWith("Id#") && msg.getPerformative() == ACLMessage.INFORM) {
                            boolean parametersUpdated = false;
                            id= msg.getContent().split("#")[1];
                            try {
                                parametersUpdated = validateSetupMessage(msg);
                                QValues= new float[2][S];
                                 matrizTemporal = new Object[S][S];
                                 rellenoMatriz= S*S;
                                    for(int i=0;i<S;i++){
                                        for(int j=0;j<S;j++){
                                               matrizTemporal[i][j]="0";
                                            }
                                      }
                                    
                            } catch (NumberFormatException e) {
                                System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                            }
                            if (parametersUpdated) state = State.s1AwaitingGame;

                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                    case s1AwaitingGame:
                        //If INFORM NEWGAME#_,_ PROCESS NEWGAME --> go to state 2
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> stay at s1
                        //Else ERROR
                        //TODO I probably should check if the new game message comes from the main agent who sent the parameters
                        
                        Qstate=0;
                        
                        
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            if (msg.getContent().startsWith("Id#")) { //Game settings updated
                                try {
                                    validateSetupMessage(msg);
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                            } else if (msg.getContent().startsWith("NewGame#")) {
                                id1 = msg.getContent().split("#")[1].split(",")[0].equals(id);
                                boolean gameStarted = false;
                                try {
                                    gameStarted = validateNewGame(msg.getContent());
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                                if (gameStarted) state = State.s2Round;
                            }
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                    case s2Round:
                        //If REQUEST POSITION --> INFORM POSITION --> go to state 3
                        //If INFORM CHANGED stay at state 2
                        //If INFORM ENDGAME go to state 1
                        //Else error
                        if (msg.getPerformative() == ACLMessage.REQUEST /*&& msg.getContent().startsWith("Position")*/) {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(mainAgent);
                            if(matrizcargada){
                                if(Qstate==1){
                                     seleccion=filaMasProbable(QValues[1]);
                                }else{
                                     seleccion=filaMasProbable(QValues[0]);
                                }
                                
                            }else{
                                 seleccion= (int)(Math.random()*(S));
                            }
                           System.out.println("ESTADO 0");
                           System.out.println(Arrays.toString(QValues[0]));
                           System.out.println("ESTADO 1");
                           System.out.println(Arrays.toString(QValues[1]));
                            msg.setContent("Position#" + seleccion);
                            System.out.println(getAID().getName() + " sent " + msg.getContent());
                            send(msg);
                            state = State.s3AwaitingResult;
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Changed#")) {
                            // Process changed message, in this case nothing
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("EndGame")) {
                            state = State.s1AwaitingGame;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message:" + msg.getContent());
                        }
                        break;
                    case s3AwaitingResult:
                        //If INFORM RESULTS --> go to state 2
                        //Else error
                        if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Results#")) {
                            //Process results
                            
                            String[] valores = msg.getContent().split("#");  // valores[1]-> posiciones [2]-> valores
                            int fila = Integer.parseInt(valores[1].split(",")[0]);
                            int columna = Integer.parseInt(valores[1].split(",")[1]);
                            int valor1= 0;
                            int valor2= 0;
                            if(id1){
                                 valor1 = Integer.parseInt(valores[2].split(",")[0]);
                                 valor2 = Integer.parseInt(valores[2].split(",")[1]);
                                 if(valor2==valorAnterior){
                                     contadorRepetidas++;
                                     if(contadorRepetidas==5){
                                         Qstate=1;
                                         QLearning(valor1, fila,Qstate);
                                     }else{
                                          QLearning(valor1, fila,Qstate);
                                     }
                                 }else{
                                     valorAnterior=valor2;
                                     QLearning(valor1, fila,Qstate);
                                 }
                                 
                            }else{                              
                                 valor2 = Integer.parseInt(valores[2].split(",")[0]);
                                 valor1 = Integer.parseInt(valores[2].split(",")[1]);
                                 if(valor1==valorAnterior){
                                     contadorRepetidas++;
                                     if(contadorRepetidas==5){
                                         Qstate=1;
                                         QLearning(valor2, fila,Qstate);
                                     }else{
                                         QLearning(valor2, fila,Qstate);
                                     }
                                 }else{
                                     valorAnterior=valor2;
                                     QLearning(valor2, fila,Qstate);
                                 }
                                
                            }
                          
                         //  System.out.println(Arrays.toString(QValues));
                            matrizTemporal[fila][columna]=valor1;
                            matrizTemporal[columna][fila]= valor2;
                            contador++;
                            if(contador==rellenoMatriz){
                              matrizcargada=true; 
                              System.out.println("AAAAAAAAAAAAAAAA SE HA CARGADO LA MATRIZ ENTERA");
                            }
                          
                            state = State.s2Round;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                }
            }
        }

        /**
         * Validates and extracts the parameters from the setup message
         *
         * @param msg ACLMessage to process
         * @return true on success, false on failure
         */
        private boolean validateSetupMessage(ACLMessage msg) throws NumberFormatException {
            int tN, tS, tR, tI, tP, tMyId;
            String msgContent = msg.getContent();

            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3) return false;
            if (!contentSplit[0].equals("Id")) return false;
            tMyId = Integer.parseInt(contentSplit[1]);

            String[] parametersSplit = contentSplit[2].split(",");
            if (parametersSplit.length != 5) return false;
            tN = Integer.parseInt(parametersSplit[0]);
            tS = Integer.parseInt(parametersSplit[1]);
            tR = Integer.parseInt(parametersSplit[2]);
            tI = Integer.parseInt(parametersSplit[3]);
            tP = Integer.parseInt(parametersSplit[4]);

            //At this point everything should be fine, updating class variables
            mainAgent = msg.getSender();
            N = tN;
            S = tS;
            R = tR;
            I = tI;
            P = tP;
            myId = tMyId;
            return true;
        }

        /**
         * Processes the contents of the New Game message
         * @param msgContent Content of the message
         * @return true if the message is valid
         */
        public boolean validateNewGame(String msgContent) {
            int msgId0, msgId1;
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 2) return false;
            if (!contentSplit[0].equals("NewGame")) return false;
            String[] idSplit = contentSplit[1].split(",");
            if (idSplit.length != 2) return false;
            msgId0 = Integer.parseInt(idSplit[0]);
            msgId1 = Integer.parseInt(idSplit[1]);
            if (myId == msgId0) {
                opponentId = msgId1;
                return true;
            } else if (myId == msgId1) {
                opponentId = msgId0;
                return true;
            }
            return false;
        }
        
        public void QLearning(int valor, int fila, int estado){
            dLearnRate *= dDecFactorLR;
            float addValue=0;
            if(valor>7){
                reward=0.9f;
            }else if(valor>4){
                reward= 0.3f;
            }else{
                reward= 0f;
            }
            if(estado==0){
              QValues[estado][fila] = (float)(QValues[estado][fila] + dLearnRate * (reward + y - QValues[estado][fila]));
            }else{
                if(valor==9){
                    reward = 3f;
                }
               QValues[estado][fila] = (float)(QValues[estado][fila] + dLearnRate * (reward + y - QValues[estado][fila]));
            }
           
            
          //   QValues[fila] = (float)(QValues[fila] + 0.7 * (reward)*dLearnRate);
        }
          private int filaMasProbable(float[] numbers)
        {
            float m = numbers[0];
            int v=0;

            for (int i = 0; i < numbers.length; i++)
                if (m < numbers[i])
                {
                    m = numbers[i];
                    v=i;
                }
            if(Math.random()>dEpsilon){ // un 20% de las veces no elegirá la mas probable
              v=  (int) (Math.random() * (double) numbers.length);
              System.out.println("ALEATORIOOOOOOOO");
            }
            return v;

        }
    }
}
