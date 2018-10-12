/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aula05.oracleinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author junio
 */
public class JanelaPrincipal {

    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTextArea jtAreaDDL;
    JTabbedPane tabbedPane;
    JPanel pPainelDeExibicaoDeDados;
    JTable jt;
    JTable query;
    JPanel pPainelDeInsecaoDeDados;
    JPanel pPainelDeBuscaDeDados;
    JPanel pPainelDDL;
    JButton deleteButton;
    JButton searchButton;
    JButton updateButton;
    JButton insertButton;
    JButton ddlButton;
    DBFuncionalidades bd;
    String tableName;
    String[] data; 

    public void ExibeJanelaPrincipal() {
        /*Janela*/
        j = new JFrame("ICMC-USP - SCC0241 - Pratica 5");
        j.setSize(700, 500);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte superior (north) - com combobox e outras informaÃ§Ãµes*/
        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox();
        pPainelDeCima.add(jc);

        /*Painel da parte inferior (south) - com Ã¡rea de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setText("Aqui é sua Área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

        /*Tab de exibicao*/
        pPainelDeExibicaoDeDados = new JPanel();
        pPainelDeExibicaoDeDados.setLayout(new GridLayout(2, 1));
        deleteButton = new JButton("Delete");
          
        tabbedPane.add(pPainelDeExibicaoDeDados, "Exibição");
        
        bd = new DBFuncionalidades(jtAreaDeStatus);
        if (bd.conectar()) {
            bd.pegarNomesDeTabelas(jc);
        }
        
        tableName = (String) jc.getSelectedItem();
        String s = bd.getMeta(tableName, "ALL");
        jtAreaDeStatus.setText("TABLE_NAME " + tableName + "\n" + s);
        
        this.DefineEventos();   
        
        deleteButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                bd.deleteData(tableName, (String)jt.getValueAt(jt.getSelectedRow(), 0));
            }
        });
        
        searchButton = new JButton("Pesquisar");
        searchButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> columns = getComponents(); //To change body of generated methods, choose Tools | Templates.
                ArrayList<String> columnsName = bd.splitString(bd.getMeta(tableName, "COLUMN_NAME"), ",");
                int nColunas = columnsName.size();
                String colunas[] = new String[nColunas];
                colunas = columns.toArray(colunas);
                String res = bd.searchData(tableName, colunas);
                
                colunas = bd.splitString(bd.getMeta(tableName, "COLUMN_NAME"), ",").toArray(colunas);
                
                String dados[][] = new String[1][nColunas];
                
                data = dados[0] = bd.splitString(res, ",").toArray(dados[0]);
                
                //System.out.println(dados[0].length);
                
                pPainelDeBuscaDeDados.remove(1);
                
                query = new JTable(dados, colunas);
                JScrollPane jsp = new JScrollPane(query);
                
                pPainelDeBuscaDeDados.add(jsp);
                
                tabbedPane.repaint();
 
            }
        });
        
        updateButton = new JButton("Editar");
        
        updateButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> columns = getComponents();
                String[] colunas = new String[columns.size()];
                colunas = columns.toArray(colunas);
                bd.updateData(tableName, colunas, data);
            }
        });
        
        insertButton = new JButton("Inserir");
        
        insertButton.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                ArrayList<String> domains = bd.splitString(bd.getDomain(tableName), "/");
                    System.out.println(domains.size());
                
                for(int i = 0; i < domains.size(); i++){
                    System.out.println(domains.get(i));
                }
                
                domains = filterChecks(domains);
                System.out.println("-----DEPOIS-----");
                for(int i = 0; i < domains.size(); i++){
                    System.out.println(domains.get(i));
                }
            }
        });
        
        ddlButton = new JButton("Gerar");
        
        ddlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String usuario = ((JTextField)((JPanel)pPainelDDL.getComponent(0)).getComponent(0)).getText();
                String senha = ((JTextField)((JPanel)pPainelDDL.getComponent(0)).getComponent(1)).getText();
                String text = bd.generateDLL(usuario, senha);
                jtAreaDDL.setText(text);
            }
        });
        
        this.showDisplayPanel();
        /*Tab de inserÃ§Ã£o*/
        pPainelDeInsecaoDeDados = new JPanel();
        tabbedPane.add(pPainelDeInsecaoDeDados, "Inserção");
        updateInsertPanel();
        pPainelDeBuscaDeDados = new JPanel();
        tabbedPane.add(pPainelDeBuscaDeDados, "Busca e Update");
        updateSearchPanel();
        
        pPainelDDL = new JPanel();
        tabbedPane.add(pPainelDDL, "Gerar DDL");
        updateDDLPanel();
        j.setVisible(true);
        
    }
    
    private ArrayList<String> filterChecks(ArrayList<String> domains){
        for(int i = 0; i < domains.size(); i++){
            if(!domains.get(i).contains("IN")){
                domains.remove(i);
            }
        }
        
        return domains;
    }
    
    public void updateDDLPanel(){
        pPainelDDL.removeAll();
        showDDLPanel();
    }
    
    public void updateSearchPanel(){
        pPainelDeBuscaDeDados.removeAll();
        showSearchPanel();
    }
    
    public void updateInsertPanel(){
        pPainelDeInsecaoDeDados.removeAll();
        showInsertPanel();
    }
    
    public void updateDisplayPanel(){
        pPainelDeExibicaoDeDados.removeAll();
        showDisplayPanel(); 
        String s = bd.getMeta(tableName, "ALL");
        jtAreaDeStatus.setText("TABLE_NAME " + tableName + "\n" + s);
    }

    private void DefineEventos() {
        jc.addItemListener(
                new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                JComboBox jcTemp = (JComboBox) e.getSource();
                tableName = (String) jcTemp.getSelectedItem();
                updateDisplayPanel();
                updateInsertPanel();
                updateSearchPanel();
            }
        });
    }
    
    private void showDDLPanel(){
        pPainelDDL.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1;
        c.weighty = 0.05;
        JPanel userPanel = new JPanel();
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        userPanel.setLayout(new GridLayout(1, 3));
        userPanel.add(new JTextField("Usuário"));
        userPanel.add(new JTextField("Senha"));
        userPanel.add(ddlButton);
        pPainelDDL.add(userPanel, c);
        
        jtAreaDDL = new JTextArea();
        JScrollPane ddl = new JScrollPane(jtAreaDDL);
        jtAreaDDL.setEditable(false);
        c.weighty = 0.95;
        c.gridheight = 9;
        c.gridx = 0;
        c.gridy = 1;
        pPainelDDL.add(ddl, c);
    }
    
    private void showSearchPanel(){
        String columnNames = this.bd.getPrimaryKeys(tableName);
        //String columnNames = this.bd.getMeta(tableName, "COLUMN_NAME");
        ArrayList<String> arr = this.bd.splitString(columnNames, ",");
        int nLinhas = arr.size();
        pPainelDeBuscaDeDados.setLayout(new GridLayout(2, 1));
        pPainelDeBuscaDeDados.add(new JPanel());
        ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).setLayout(new GridLayout(nLinhas + 1, 2));
        
        for(int i = 0; i< nLinhas; i++){
            ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).add(new JLabel(arr.get(i)));
            ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).add(new JTextField("Digite aqui"));
        }
        
        ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).add(searchButton);
        ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).add(updateButton);
        
        query =  new JTable();
        pPainelDeBuscaDeDados.add(query);
        
    }
    
    private void showInsertPanel(){
        String columnNames = this.bd.getMeta(tableName, "COLUMN_NAME");
        ArrayList<String> arr = this.bd.splitString(columnNames, ",");
        int nLinhas = arr.size();
        pPainelDeInsecaoDeDados.setLayout(new GridLayout(nLinhas + 1, 2));
        for(int i = 0; i< nLinhas; i++){
            pPainelDeInsecaoDeDados.add(new JLabel(arr.get(i)));
            pPainelDeInsecaoDeDados.add(new JTextField("Digite aqui"));
        }
        pPainelDeInsecaoDeDados.add(new JLabel(""));    
        pPainelDeInsecaoDeDados.add(insertButton);
    }
    
    private void showDisplayPanel(){
        /*Table de exibiÃ§Ã£o*/
        String columnNames = this.bd.getMeta(tableName, "COLUMN_NAME");
        ArrayList<String> arr = this.bd.splitString(columnNames, ",");
        int nColunas = arr.size();
        String colunas[] = new String[nColunas];
        colunas = arr.toArray(colunas);        
        ArrayList<ArrayList<String>> data = this.bd.getData(tableName, columnNames);
        int nLinhas = data.size();
        String dados[][] = new String[nLinhas][nColunas];
        
        for(int i = 0; i < nLinhas; i++){
            dados[i] = data.get(i).toArray(dados[i]);
        }
        
        jt = new JTable(dados, colunas);
        JScrollPane jsp = new JScrollPane(jt);
        pPainelDeExibicaoDeDados.add(jsp);
        pPainelDeExibicaoDeDados.add(deleteButton);

    }
    
    private ArrayList<String> getComponents(){
        Component[] components = ((JPanel) pPainelDeBuscaDeDados.getComponent(0)).getComponents();
        ArrayList<String> aux = new ArrayList<String>();
        for(int i = 1; i< (components.length - 2); i += 2){
            aux.add(((JTextField) components[i]).getText());
        }
        
        return aux;
    } 
}
