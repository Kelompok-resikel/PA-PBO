/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.Database;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseController {
    protected Database database;

    public BaseController() {
        database = new Database();
    }

    // Method loadData yang akan diimplementasi oleh subclass
    public abstract void loadData(DefaultTableModel tableModel);

    // Method umum untuk query data
    public void executeQuery(DefaultTableModel tableModel, String query) {
        try (Connection conn = database.getNewConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Implementasi default
            }
        } catch (SQLException e) {
            System.err.println("Gagal menjalankan query: " + e.getMessage());
        }
    }
}
