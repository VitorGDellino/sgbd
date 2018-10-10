/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aula05.oracleinterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 *
 * @author junio
 */
public class DBFuncionalidades {
    Connection connection;
    Statement stmt;
    ResultSet rs;
    JTextArea jtAreaDeStatus;
    
    public DBFuncionalidades(JTextArea jtaTextArea){
        jtAreaDeStatus = jtaTextArea;
    }
    
    public boolean conectar(){       
        try {
            DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                    "L9277875",
                    "L9277875");
            return true;
        } catch(SQLException ex){
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
        }
        return false;
    }
    public void pegarNomesDeTabelas(JComboBox jc){
        String s = "";
        try {
            this.getTableNames();
            while (rs.next()) {
                jc.addItem(rs.getString("table_name"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }        
    }
    
    public void getTableNames(){
        String s = "SELECT table_name FROM user_tables";
        try {
            
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
        } catch (SQLException ex) {
            Logger.getLogger(DBFuncionalidades.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    public void exibeDados(JTable tATable, String sTableName){
        /*Aqui preencho a tabela com os dados*/
    }
    //public void preencheComboBoxComRestricoesDeCheck
    //public void preencheComboBoxComValoresReferenciados
    //
    public String getAllMeta(ResultSet rs){
        String res = "";
        try{
            while(rs.next()){
                res += "ID ";
                res += rs.getString("COLUMN_ID") + ", NAME ";
                res += rs.getString("COLUMN_NAME") + ", DATA_TYPE ";
                res += rs.getString("DATA_TYPE") + ", NULLABLE ";
                res += rs.getString("NULLABLE") + "\n";  
            }
            
            return res;
            
        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;  
    }
    
    public String getPrimaryKeys(String tableName){
        String s = "";
        String res = "";
        try{
            
            s = "SELECT b.column_name FROM USER_CONSTRAINTS A, USER_CONS_COLUMNS B WHERE B.TABLE_NAME LIKE 'LBD%' AND CONSTRAINT_TYPE = 'P' AND A.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND A.TABLE_NAME = '" + tableName + "'";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(s);
            
            while(rs.next()){
                res += rs.getString("COLUMN_NAME") + ",";
            }
            
            rs.close();
            stmt.close();
            
            return res;
     
        }catch(SQLException ex){
            //System.out.println("foi aqui");
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;
    }
    
    public String getColumn(ResultSet rs, String op){
        String res = "";
        try{
            if(op.equals("COLUMN_NAME")){
                while(rs.next()){
                    res += rs.getString(op) + ",";    
                }   
            }else if(op.equals("DATA_TYPE")){
                while(rs.next()){
                    res += rs.getString(op) + ",";    
                } 
            }
 
            res = res.substring(0, res.length()-1);
            
            return res;
            
        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;
    }
    
    
      
    public String getMeta(String tableName, String op){
        String s = "";
        String res = "";
        try{
            s = "SELECT COLUMN_ID, COLUMN_NAME, DATA_TYPE, NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '" + tableName + "'";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(s);
            
            if(op.equals("ALL")){
                res = getAllMeta(rs);
            }else{
                res = getColumn(rs, op);
            }
            
            rs.close();
            stmt.close();
            
            return res;
     
        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;
        
    }
    
    public void updateData(String tableName, String[] ids, String[] data){
        try{
            String s = "UPDATE " + tableName + " SET ";
            ArrayList<String> columns = this.splitString(this.getMeta(tableName, "COLUMN_NAME"), ",");
            ArrayList<String> dataTypes = this.splitString(this.getMeta(tableName, "DATA_TYPE"), ",");
            for(int i = 0; i< columns.size(); i++){
                if(dataTypes.get(i).equals("NUMBER")){
                    s += columns.get(i) + " = "  + data[i];
                }else{
                    s += s += columns.get(i) + " = "  + "'" + data[i] + "'";
                }
                
                if(i != columns.size() - 1){
                    s +=  ",";
                }
            }
            
            s += " WHERE ";
            for(int i = 0; i < ids.length; i++){
                if(dataTypes.get(i).equals("NUMBER")){
                    s += columns.get(i) + " = "  + ids[i];
                }else{
                    s += s += columns.get(i) + " = "  + "'" + ids[i] + "'";
                }
                
                if(i != columns.size() - 1){
                    s +=  " AND ";
                }
            }
            System.out.println("ta por aqui");
            System.out.println(s);
            
            Statement stmt = this.connection.createStatement();
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(null, "Deseja mesmo remover a tupla selecionada?", "", dialogButton);
            if(dialogResult == 0) {
                ResultSet rs = stmt.executeQuery(s);  
                rs.close();
            }

            stmt.close();

        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
    }
    
    public String searchData(String tableName, String[] ids){
        String res = "";
        try{
            String columnNames = getMeta(tableName, "COLUMN_NAME");
            ArrayList<String> arr = splitString(columnNames, ",");
            String s = "SELECT * FROM " + tableName + " WHERE ";
            ArrayList<String> columns = this.splitString(this.getPrimaryKeys(tableName), ",");
            ArrayList<String> dataTypes = this.splitString(this.getMeta(tableName, "DATA_TYPE"), ",");
            Statement stmt = this.connection.createStatement();
            
            for(int i = 0; i< columns.size(); i++){
                if(dataTypes.get(i).equals("NUMBER")){
                    s += columns.get(i) + " = " + Integer.valueOf(ids[i]); 
                }else{
                    s += columns.get(i) + " = " + "'" + ids[i] + "'";
                }
                
                if(i != columns.size() -1){
                    s += " AND ";
                }
            }
            
            //System.out.println(s);
            ResultSet rs = stmt.executeQuery(s);
            
            rs.next();
            for(String str : arr){
                res += rs.getString(str) + ","; 
            }
            
            res = res.substring(0, res.length()-1);
            
            rs.close();
            stmt.close();
            
            return res;

        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;
    }
    
    public void deleteData(String tableName, String id){
        try{
            String s = "";
            ArrayList<String> columns = this.splitString(this.getMeta(tableName, "COLUMN_NAME"), ",");
            ArrayList<String> dataTypes = this.splitString(this.getMeta(tableName, "DATA_TYPE"), ",");
            String type =  dataTypes.get(0);
            if(type.equals("NUMBER")){
                s = "DELETE FROM " + tableName + " WHERE " + columns.get(0) + " = " + Double.valueOf(id);
            }else{
                s = "DELETE FROM " + tableName + " WHERE " + columns.get(0) + " = " + "'" + id + "'";
            }
            
            //System.out.println(s);
            Statement stmt = this.connection.createStatement();
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(null, "Deseja mesmo remover a tupla selecionada?", "", dialogButton);
            if(dialogResult == 0) {
                ResultSet rs = stmt.executeQuery(s);  
                rs.close();
            }

            stmt.close();

        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
    }
    
    public ArrayList<ArrayList<String>> getData(String tableName, String columns){
        String res = "";
        try{
            String s = "SELECT " + columns + " FROM " + tableName;
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(s);
            
            ArrayList<String> arr = this.splitString(columns, ",");
            ArrayList<ArrayList<String>> queryRes =  new ArrayList<ArrayList<String>>();
            
            while(rs.next()){
                ArrayList<String> aux = new ArrayList<String>();
                for(String str : arr){
                    aux.add(rs.getString(str));
                }
                
                queryRes.add(aux);
            }
            
            rs.close();
            stmt.close();
            
            return queryRes;
            
     
        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return null;
      
    }
    
    public ArrayList<String> splitString(String s, String delimeter){
        ArrayList<String> splited = new ArrayList<String>();
        String[] aux = s.split(delimeter);
        for(int i = 0; i <aux.length; i++){
            splited.add(aux[i]);
        }
        
        return splited;
    }
}