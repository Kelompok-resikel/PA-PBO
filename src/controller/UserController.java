package controller;

import model.Database;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController extends BaseController {

    public UserController() {
        super(); // Panggil konstruktor superclass
    }

    @Override
    public void loadData(DefaultTableModel tableModel) {
        
        String query = "SELECT u.nama_lengkap, p.kontak, p.domisili, u.username, u.password "
                     + "FROM users u "
                     + "LEFT JOIN pengguna p ON u.id_user = p.id_user";
        try (Connection connection = database.getNewConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("nama_lengkap"),
                    rs.getString("kontak"),
                    rs.getString("domisili"),
                    rs.getString("username"),
                    rs.getString("password")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat data masyarakat: " + e.getMessage());
        executeQuery(tableModel, query); // Gunakan helper method di BaseController
    }
    }

    public boolean updateUser(String username, String fullName, String kontak, String domisili) {
        String query = "UPDATE users u "
                     + "JOIN pengguna p ON u.id_user = p.id_user "
                     + "SET u.nama_lengkap = ?, p.kontak = ?, p.domisili = ? "
                     + "WHERE u.username = ?";

        try (Connection connection = database.getNewConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, fullName);
            statement.setString(2, kontak);
            statement.setString(3, domisili);
            statement.setString(4, username);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui pengguna: " + e.getMessage());
        }
        return false;
    }


    /**
     * Update user data with additional fields (e.g., password and username).
     * @param currentUsername Current username of the user.
     * @param newUsername     New username.
     * @param newPassword     New password.
     * @param newKontak       New contact.
     * @param newDomisili     New domicile.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateUserData(String currentUsername, String newUsername, String newPassword, String newKontak, String newDomisili) {
        String query = "UPDATE users u "
                     + "JOIN pengguna p ON u.id_user = p.id_user "
                     + "SET u.username = ?, u.password = ?, p.kontak = ?, p.domisili = ? "
                     + "WHERE u.username = ?";

        try (Connection connection = database.getNewConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newUsername);
            statement.setString(2, newPassword);
            statement.setString(3, newKontak);
            statement.setString(4, newDomisili);
            statement.setString(5, currentUsername);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui data pengguna: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a user and their associated data from the database.
     * @param username Username of the user to be deleted.
     * @return True if the user was deleted successfully, false otherwise.
     */
    public boolean deleteUser(String username) {
        String deletePenggunaQuery = "DELETE FROM pengguna WHERE id_user = (SELECT id_user FROM users WHERE username = ?)";
        String deleteUsersQuery = "DELETE FROM users WHERE username = ?";

        try (Connection conn = database.getNewConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Koneksi database gagal.");
            }

            // Mulai transaksi
            conn.setAutoCommit(false);

            try {
                // Hapus data dari tabel `pengguna`
                try (PreparedStatement stmtPengguna = conn.prepareStatement(deletePenggunaQuery)) {
                    stmtPengguna.setString(1, username);
                    stmtPengguna.executeUpdate();
                }

                // Hapus data dari tabel `users`
                try (PreparedStatement stmtUsers = conn.prepareStatement(deleteUsersQuery)) {
                    stmtUsers.setString(1, username);
                    int rowsAffected = stmtUsers.executeUpdate();

                    if (rowsAffected > 0) {
                        // Commit jika semua operasi berhasil
                        conn.commit();
                        return true;
                    } else {
                        // Rollback jika tidak ada data yang dihapus
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                // Rollback jika ada kesalahan selama proses
                conn.rollback();
                throw e;
            } finally {
                // Kembalikan mode autocommit ke default
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Gagal menghapus pengguna: " + e.getMessage());
        }
        return false;
    }
}

