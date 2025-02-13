/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GUI extends JFrame implements ActionListener {
    JLabel leftPanelRoundsLabel;
    JLabel leftPanelExtraInformation;
    JList<String> list;
    JList<String> listRanking;
    private MainAgent mainAgent;
    private JPanel rightPanel;
    private JPanel rightPanel2;
    private JTextArea rightPanelLoggingTextArea;
    private JTextArea rightPanelLoggingTextArea2;
    private JPanel pane;
    private JPanel leftPanel;
    private  JPanel centralPanel;
    private JCheckBox verboseBox;
    private boolean verbose=false;
    private JPanel centralBottomSubpanel;
    private JTable payoffTable;
    private JScrollPane player1ScrollPane;
    private LoggingOutputStream loggingOutputStream;
    public static Object[][] data;
    private int rondaActual=0;
    private boolean primeraUpdate = true;

    public GUI() {
        initUI();
    }

    public GUI(MainAgent agent) {
        mainAgent = agent;
        initUI();
       
        loggingOutputStream = new LoggingOutputStream(rightPanelLoggingTextArea);
         
        
    }

    public void log(String s) {
        Runnable appendLine = () -> {
            
            rightPanelLoggingTextArea.append('[' + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] - " + s);
            rightPanelLoggingTextArea.setCaretPosition(rightPanelLoggingTextArea.getDocument().getLength());
        };
        SwingUtilities.invokeLater(appendLine);
    }
        public void log2(String s) {
        Runnable appendLine2 = () -> {
            rightPanelLoggingTextArea2.append(s);
            rightPanelLoggingTextArea2.setCaretPosition(rightPanelLoggingTextArea2.getDocument().getLength());
        };
        SwingUtilities.invokeLater(appendLine2);
    }

    public OutputStream getLoggingOutputStream() {
        return loggingOutputStream;
    }

    public void logLine(String s) {
       
        log(s + "\n");
        
    }
     public void logLine2(String s) {
        log2(s + "\n");
    }
    public void setPlayersUI(String[] players) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String s : players) {
            listModel.addElement(s);
        }
        list.setModel(listModel);
    }

    public void initUI() {
    
        
        setTitle("GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(1000, 600));
        setJMenuBar(createMainMenuBar());
        setContentPane(createMainContentPane());
        pack();
        setVisible(true);
    }

    private Container createMainContentPane() {
        pane = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        //LEFT PANEL
        gc.gridx = 0;
        gc.weightx = 1;
        pane.add(createLeftPanel(), gc);

        //CENTRAL PANEL
        gc.gridx = 1;
        gc.weightx = 8;
        pane.add(createCentralPanel(), gc);

        //RIGHT PANEL
        gc.gridx = 2;
        gc.weightx = 8;
        pane.add(createRightPanel(), gc);
         //RIGHT PANEL 2
        gc.gridx = 3;
        gc.weightx = 8;
      
        
        pane.add(createRightPanel2(), gc);
      
        return pane;
    }

    private JPanel createLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        int rondas= mainAgent.parameters.R;
        leftPanelRoundsLabel = new JLabel("Round "+rondaActual+" / "+rondas);
        JButton leftPanelNewButton = new JButton("New");
        leftPanelNewButton.addActionListener(actionEvent -> executeGame());
        JButton leftPanelStopButton = new JButton("Stop");
        leftPanelStopButton.addActionListener(actionEvent -> clearTexts());
        JButton leftPanelContinueButton = new JButton("Continue");
        verboseBox = new JCheckBox();
        verboseBox.setSelected(true);
        verboseBox.setText("Verbose");
        verboseBox.setToolTipText("");
        verboseBox.addActionListener(actionEvent -> rightPanel.setVisible(verboseActivado()));
        leftPanelContinueButton.addActionListener(this);
        

        leftPanelExtraInformation = new JLabel("N: "+mainAgent.parameters.N+" S: "+mainAgent.parameters.S+
                " R: "+mainAgent.parameters.R+" I: "+mainAgent.parameters.I+" P :"+mainAgent.parameters.P);

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        gc.gridy = 0;
        leftPanel.add(leftPanelRoundsLabel, gc);
        gc.gridy = 1;
        leftPanel.add(leftPanelNewButton, gc);
        gc.gridy = 2;
        leftPanel.add(leftPanelStopButton, gc);
        gc.gridy = 3;
        leftPanel.add(leftPanelContinueButton, gc);
        gc.gridy = 4;
        leftPanel.add(verboseBox,gc);
        gc.gridy = 5;
        
        gc.weighty = 10;
        leftPanel.add(leftPanelExtraInformation, gc);

        return leftPanel;
    }

    private JPanel createCentralPanel() {
        centralPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;

        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;

        gc.gridy = 0;
        gc.weighty = 1;
        centralPanel.add(createCentralTopSubpanel(), gc);
        gc.gridy = 1;
        gc.weighty = 4;
       
        centralPanel.add(createCentralBottomSubpanel(), gc);
       

        return centralPanel;
    }

    private JPanel createCentralTopSubpanel() {
        JPanel centralTopSubpanel = new JPanel(new GridBagLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Empty");
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        
        JScrollPane listScrollPane = new JScrollPane(list);

        JLabel info1 = new JLabel("Selected player info");
        JButton leftPanelUpdateButton = new JButton("Update Matrix");
        leftPanelUpdateButton.addActionListener(actionEvent -> updateMatrix());
        JButton updatePlayersButton = new JButton("Update players");
        updatePlayersButton.addActionListener(actionEvent -> mainAgent.updatePlayers());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.weighty = 0.5;
        gc.anchor = GridBagConstraints.CENTER;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridheight = 666;
        gc.fill = GridBagConstraints.BOTH;
        centralTopSubpanel.add(listScrollPane, gc);
        gc.gridx = 1;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        centralTopSubpanel.add(info1, gc);
        gc.gridy = 1;
        centralTopSubpanel.add(updatePlayersButton, gc);
        gc.gridy = 2;
        centralTopSubpanel.add(leftPanelUpdateButton,gc);

        return centralTopSubpanel;
    }

    private JPanel createCentralBottomSubpanel() {
        centralBottomSubpanel = new JPanel(new GridBagLayout());
        
        int size=mainAgent.parameters.S;
         data = new Object[size][size];
        Object[] nullPointerWorkAround = new Object[size];
        for(int i=0;i<size;i++){
            nullPointerWorkAround[i]="C"+(i+1);
        }
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                String valor1= Integer.toString((int)(Math.random() * 9) + 1);
                String valor2= Integer.toString((int)(Math.random() * 9) + 1);
                String valorM= valor1+","+valor2;
                String valorM2= valor2+","+valor1;
                data[i][j]= valorM;
                data[j][i]= valorM2;
            }
        }

       
        JLabel payoffLabel = new JLabel("Payoff matrix");
        payoffTable = new JTable(data, nullPointerWorkAround);
        //payoffTable.setTableHeader(null);
        //payoffTable.setEnabled(false);
        
        player1ScrollPane = new JScrollPane(payoffTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weighty = 0.5;
        centralBottomSubpanel.add(payoffLabel, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        gc.weighty = 2;
        centralBottomSubpanel.add(player1ScrollPane, gc);

        return centralBottomSubpanel;
    }
   

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 1d;
        c.weightx = 1d;

        rightPanelLoggingTextArea = new JTextArea("");
        rightPanelLoggingTextArea.setEditable(false); 
        JScrollPane jScrollPane = new JScrollPane(rightPanelLoggingTextArea);
        rightPanel.add(jScrollPane, c);
        return rightPanel;
    }
    private JPanel createRightPanel2() {
        rightPanel2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 1d;
        c.weightx = 1d;
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Empty");
        listRanking = new JList<>(listModel);
        listRanking.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listRanking.setSelectedIndex(0);
        listRanking.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(listRanking);

        rightPanelLoggingTextArea2 = new JTextArea("");
        rightPanelLoggingTextArea2.setEditable(false);
        rightPanelLoggingTextArea2.setForeground(Color.red); 
        JScrollPane jScrollPane = new JScrollPane(rightPanelLoggingTextArea2);
        rightPanel2.add(jScrollPane, c);
        return rightPanel2;
    }

    private JMenuBar createMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem exitFileMenu = new JMenuItem("Exit");
        exitFileMenu.setToolTipText("Exit application");
        exitFileMenu.addActionListener(this);

        JMenuItem newGameFileMenu = new JMenuItem("New Game");
        newGameFileMenu.setToolTipText("Start a new game");
        newGameFileMenu.addActionListener(this);

        menuFile.add(newGameFileMenu);
        menuFile.add(exitFileMenu);
        menuBar.add(menuFile);

        JMenu menuEdit = new JMenu("Edit");
        JMenuItem resetPlayerEditMenu = new JMenuItem("Reset Players");
        resetPlayerEditMenu.setToolTipText("Reset all player");
        resetPlayerEditMenu.setActionCommand("reset_players");
        resetPlayerEditMenu.addActionListener(this);

        JMenuItem parametersEditMenu = new JMenuItem("Parameters");
        parametersEditMenu.setToolTipText("Modify the parameters of the game");
        parametersEditMenu.addActionListener(actionEvent -> logLine("Parameters: " + JOptionPane.showInputDialog(new Frame("Configure parameters"), "Enter parameters N,S,R,I,P")));

        menuEdit.add(resetPlayerEditMenu);
        menuEdit.add(parametersEditMenu);
        menuBar.add(menuEdit);

        JMenu menuRun = new JMenu("Run");

        JMenuItem newRunMenu = new JMenuItem("New");
        newRunMenu.setToolTipText("Starts a new series of games");
        newRunMenu.addActionListener(this);

        JMenuItem stopRunMenu = new JMenuItem("Stop");
        stopRunMenu.setToolTipText("Stops the execution of the current round");
        stopRunMenu.addActionListener(this);

        JMenuItem continueRunMenu = new JMenuItem("Continue");
        continueRunMenu.setToolTipText("Resume the execution");
        continueRunMenu.addActionListener(this);

        JMenuItem roundNumberRunMenu = new JMenuItem("Number Of rounds");
        roundNumberRunMenu.setToolTipText("Change the number of rounds");
        roundNumberRunMenu.addActionListener(actionEvent -> logLine(JOptionPane.showInputDialog(new Frame("Configure rounds"), "How many rounds?") + " rounds"));

        menuRun.add(newRunMenu);
        menuRun.add(stopRunMenu);
        menuRun.add(continueRunMenu);
        menuRun.add(roundNumberRunMenu);
        menuBar.add(menuRun);

        JMenu menuWindow = new JMenu("Window");

        JCheckBoxMenuItem toggleVerboseWindowMenu = new JCheckBoxMenuItem("Verbose", true);
        toggleVerboseWindowMenu.addActionListener(actionEvent -> rightPanel.setVisible(toggleVerboseWindowMenu.getState()));

        menuWindow.add(toggleVerboseWindowMenu);
        menuBar.add(menuWindow);

        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            logLine("Button " + button.getText());
        } else if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            logLine("Menu " + menuItem.getText());
        }
    }
    public void clearTexts(){
        rightPanelLoggingTextArea.setText(null);
        rightPanelLoggingTextArea2.setText(null);
        rondaActual=0;
         GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 0.5;
        gc.gridx = 0;
        gc.weightx = 1;
        pane.remove(leftPanel);
        pane.add(createLeftPanel(), gc);
        
       pane.repaint();
       pane.updateUI();
      pack();
      mainAgent.updatePlayers();
    }
    public void updateMatrix(){
        int size=mainAgent.parameters.S;
        int cambio = mainAgent.parameters.P;
       
          Object[] nullPointerWorkAround = new Object[size];
        for(int i=0;i<size;i++){
            nullPointerWorkAround[i]="C"+(i+1);
        }
        if(cambio==0 || primeraUpdate){
             data = new Object[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                String valor1= Integer.toString((int)(Math.random() * 9) + 1);
                String valor2= Integer.toString((int)(Math.random() * 9) + 1);
                String valorM= valor1+","+valor2;
                String valorM2= valor2+","+valor1;
                data[i][j]= valorM;
                data[j][i]= valorM2;
                primeraUpdate=false;
            }
        }
        }else{
            int cambioPorcentaje= (int)size*size*(1/cambio);
            for(int i=0;i<cambioPorcentaje;i++){
                int filacolumna1= (int)(Math.random() * size);
                int filacolumna2= (int)(Math.random() * size);
              
                String valor1= Integer.toString((int)(Math.random() * 9) + 1);
                String valor2= Integer.toString((int)(Math.random() * 9) + 1);
                String valorM= valor1+","+valor2;
                String valorM2= valor2+","+valor1;
                data[filacolumna1][filacolumna2]= valorM;
                data[filacolumna2][filacolumna1]= valorM2;      
            }
        }

     JTable newTable =new JTable(data,nullPointerWorkAround);

        player1ScrollPane.setViewportView(newTable);

     
        
        pack();
        setVisible(true);
    }
    public void executeGame(){
        int rounds = mainAgent.parameters.R;
        for(int i=0;i<rounds;i++){
        rondaActual++;
         GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 0.5;
        gc.gridx = 0;
        gc.weightx = 1;
        pane.remove(leftPanel);
        pane.add(createLeftPanel(), gc);
        
       pane.repaint();
       pane.updateUI();
      pack();
       
        mainAgent.newGame();
        
        }
    }
    private boolean verboseActivado(){
        return verboseBox.isSelected();
    }
   
    public class LoggingOutputStream extends OutputStream {
        private JTextArea textArea;

        public LoggingOutputStream(JTextArea jTextArea) {
            textArea = jTextArea;
        }

        @Override
        public void write(int i) throws IOException {
            textArea.append(String.valueOf((char) i));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
