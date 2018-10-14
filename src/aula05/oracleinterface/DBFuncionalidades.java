/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aula05.oracleinterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
                    "pratica5",
                    "pratica5");
            return true;
        } catch(SQLException ex){
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
        }
        return false;
    }
    
    public Connection conectar_usuario(String usuario, String senha){       
        try {
            DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
            Connection con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                    usuario,
                    senha);
            return con;
        } catch(SQLException ex){
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
        }
        return null;
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
    
    
    public String getDomain(String tableName){
        String s = "";
        String res = "";
        
        try{
            s = "SELECT SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE TABLE_NAME = '"+ tableName +"'";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(s);
            
            while(rs.next()){
                res += rs.getString("SEARCH_CONDITION") + "/";
            }
            
            rs.close();
            stmt.close();
            
            res = res.substring(0, res.length()-1);
            System.out.println(res);
            return res;
     
        }catch(SQLException ex){
            //System.out.println("foi aqui");
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return res;
    }
    public String getPrimaryKeys(String tableName){
        String s = "";
        String res = "";
        try{
            
            s = "SELECT b.column_name FROM USER_CONSTRAINTS A, USER_CONS_COLUMNS B WHERE B.TABLE_NAME LIKE '"+ tableName +"' AND CONSTRAINT_TYPE = 'P' AND A.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND A.TABLE_NAME = '" + tableName + "'";
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
                }else if(dataTypes.get(i).equals("VARCHAR2") || dataTypes.get(i).equals("CHAR")){
                    s += columns.get(i) + " = "  + "'" + data[i] + "'";
                }else if(dataTypes.get(i).equals("DATE")){
                    s += columns.get(i) + " = TO_DATE("  + "'" + splitString(data[i], " ").get(0) + "', 'YYYY-MM-DD')";
                }
                
                if(i < columns.size() - 1){
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
                
                if(i != ids.length - 1){
                    s +=  " AND ";
                }
            }
            
            
            Statement stmt = this.connection.createStatement();
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(null, "Deseja mesmo remover a tupla selecionada?", "", dialogButton);
            if(dialogResult == 0) {
                System.out.println(s);
                ResultSet rs = stmt.executeQuery(s);  
                rs.close();
            }
             
            stmt.close();

        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
    }
    
    public String generateDLL(String usuario, String senha){
        Connection connect = conectar_usuario(usuario, senha);
        try{
            String tables = "SELECT TABLE_NAME FROM USER_ALL_TABLES ORDER BY TABLE_NAME";
            Statement stmt = connect.createStatement();
            String config = "begin\n" +
                            "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'STORAGE',false);\n" +
                            "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SEGMENT_ATTRIBUTES',false);\n" +
                            "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SQLTERMINATOR',true);\n"+
                            "end;";
            String ddl = "";
            ResultSet rs_tables = stmt.executeQuery(tables);
            Connection c = conectar_usuario(usuario, senha);
            Statement s = c.createStatement();
            s.execute(config);
            while(rs_tables.next()){
                String table = rs_tables.getString("TABLE_NAME");
                ResultSet rs = s.executeQuery("select dbms_metadata.get_ddl('TABLE', '"+table+"') ddl from dual");
                rs.next();
                String d = rs.getString("DDL");
                ddl += d + "\n\n";
                rs.close();
            }
            s.close();
            rs_tables.close();                 
            stmt.close();
            return ddl;
        }catch(SQLException ex){
            this.jtAreaDeStatus.setText(ex.getMessage());
        }
        
        return null;
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
