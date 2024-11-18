import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.UIManager;
import com.toedter.calendar.JDateChooser; // Untuk JDateChooser
import java.sql.*; // Untuk SQLite
import javax.swing.*; // Untuk Swing
import javax.swing.filechooser.FileNameExtensionFilter; // Untuk FileChooser
import java.io.*; // Untuk ekspor dan impor CSV
import java.text.SimpleDateFormat; // Untuk format tanggal


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Lenovo
 */
public class CatatanFrame extends javax.swing.JFrame {

    /**
     * Creates new form CatatanFrame
     */
    public CatatanFrame() {
        initComponents();
        connectToDatabase(); // Hubungkan ke database
        listModel = new DefaultListModel<>();
        listCatatan.setModel(listModel);
        loadCatatan(); // Muat catatan dari database
    }
    
    private Connection conn; // Koneksi ke SQLite
    private DefaultListModel<String> listModel; // Model untuk JList
    
    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:E:/kuliah/PBO2/Muhammad_Husnu_Elwafa-2210010318-UTS/src/catatan.db");
            System.out.println("Koneksi berhasil ke database!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal: " + e.getMessage());
        }
    }
    
    private void loadCatatan() {
        listModel.clear();
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM catatan")) {
            while (rs.next()) {
                listModel.addElement(rs.getString("judul"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }
    
    private void simpanCatatan(String judul, String catatan, String tanggal) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO catatan (judul, catatan, tanggal) VALUES (?, ?, ?)")) {
            stmt.setString(1, judul);
            stmt.setString(2, catatan);
            stmt.setString(3, tanggal);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil disimpan!");
            loadCatatan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan catatan: " + e.getMessage());
        }
    }
    
    private void updateCatatan(int id, String judul, String catatan, String tanggal) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE catatan SET judul = ?, catatan = ?, tanggal = ? WHERE id = ?")) {
            stmt.setString(1, judul);
            stmt.setString(2, catatan);
            stmt.setString(3, tanggal);
            stmt.setInt(4, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil diupdate!");
            loadCatatan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate catatan: " + e.getMessage());
        }
    }
    
    private void tampilkanDataCatatan(String judul) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM catatan WHERE judul = ?")) {
            stmt.setString(1, judul);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Ambil data dari database
                    String tanggal = rs.getString("tanggal");
                    String isiCatatan = rs.getString("catatan");

                    // Tampilkan data ke form
                    jDateChooser.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(tanggal));
                    txtJudul.setText(judul);
                    txtCatatan.setText(isiCatatan);

                    // Nonaktifkan form (hanya untuk tampilan, tidak bisa diubah)
                    txtJudul.setEnabled(false);
                    txtCatatan.setEnabled(false);
                    jDateChooser.setEnabled(false);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data: " + e.getMessage());
        }
    }

    
    private void hapusCatatan(int id) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM catatan WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil dihapus!");
            loadCatatan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus catatan: " + e.getMessage());
        }
    }
    
    private int getIdFromJudul(String judul) {
        int id = -1; // Default jika tidak ditemukan
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM catatan WHERE judul = ?")) {
            stmt.setString(1, judul); // Ganti placeholder (?) dengan judul
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id"); // Ambil ID dari hasil query
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mendapatkan ID dari judul: " + e.getMessage());
        }
        return id; // Kembalikan ID
    }

    
    private void eksporCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ID,Judul,Catatan,Tanggal");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM catatan")) {
                while (rs.next()) {
                    writer.println(rs.getInt("id") + "," +
                                   rs.getString("judul") + "," +
                                   rs.getString("catatan") + "," +
                                   rs.getString("tanggal"));
                }
            }
            JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke CSV!");
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengekspor CSV: " + e.getMessage());
        }
    }
    
    private void imporCSV(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                simpanCatatan(data[1], data[2], data[3]);
            }
            JOptionPane.showMessageDialog(this, "Data berhasil diimpor dari CSV!");
            loadCatatan();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengimpor CSV: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtCatatan = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        btnSimpan = new javax.swing.JButton();
        btnSimpanEdit = new javax.swing.JButton();
        txtJudul = new javax.swing.JTextField();
        jDateChooser = new com.toedter.calendar.JDateChooser();
        jPanel3 = new javax.swing.JPanel();
        btnCatatanBaru = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listCatatan = new javax.swing.JList<>();
        btnEkspor = new javax.swing.JButton();
        btnImpor = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(230, 139, 69));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(new java.awt.Color(230, 139, 69));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        txtCatatan.setBackground(new java.awt.Color(230, 139, 69));
        txtCatatan.setColumns(20);
        txtCatatan.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtCatatan.setForeground(new java.awt.Color(255, 255, 255));
        txtCatatan.setRows(5);
        txtCatatan.setBorder(null);
        txtCatatan.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txtCatatan.setEnabled(false);
        jScrollPane1.setViewportView(txtCatatan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 451;
        gridBagConstraints.ipady = 300;
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jPanel5.setBackground(new java.awt.Color(230, 186, 69));
        jPanel5.setLayout(new java.awt.GridBagLayout());

        btnSimpan.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Save.png"))); // NOI18N
        btnSimpan.setText("Simpan");
        btnSimpan.setEnabled(false);
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 7;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel5.add(btnSimpan, gridBagConstraints);

        btnSimpanEdit.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSimpanEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Done.png"))); // NOI18N
        btnSimpanEdit.setEnabled(false);
        btnSimpanEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 7;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel5.add(btnSimpanEdit, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jPanel5, gridBagConstraints);

        txtJudul.setBackground(new java.awt.Color(230, 139, 69));
        txtJudul.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtJudul.setForeground(new java.awt.Color(255, 255, 255));
        txtJudul.setText("Judul");
        txtJudul.setBorder(null);
        txtJudul.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txtJudul.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        jPanel2.add(txtJudul, gridBagConstraints);

        jDateChooser.setBackground(new java.awt.Color(230, 139, 69));
        jDateChooser.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel2.add(jDateChooser, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.ipady = 60;
        jPanel1.add(jPanel2, gridBagConstraints);

        jPanel3.setBackground(new java.awt.Color(230, 139, 69));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        btnCatatanBaru.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnCatatanBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Plus.png"))); // NOI18N
        btnCatatanBaru.setText("Catatan Harian Baru");
        btnCatatanBaru.setToolTipText("");
        btnCatatanBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatatanBaruActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 16;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel3.add(btnCatatanBaru, gridBagConstraints);

        listCatatan.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        listCatatan.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listCatatanValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listCatatan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.ipady = 200;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel3.add(jScrollPane2, gridBagConstraints);

        btnEkspor.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnEkspor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Import CSV.png"))); // NOI18N
        btnEkspor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEksporActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel3.add(btnEkspor, gridBagConstraints);

        btnImpor.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnImpor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Export CSV.png"))); // NOI18N
        btnImpor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImporActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(btnImpor, gridBagConstraints);

        btnHapus.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Trash.png"))); // NOI18N
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel3.add(btnHapus, gridBagConstraints);

        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Edit.png"))); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel3.add(btnEdit, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(30, 35, 0, 20);
        jPanel1.add(jPanel3, gridBagConstraints);

        jPanel6.setBackground(new java.awt.Color(230, 186, 69));
        jPanel6.setForeground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("APLIKASI CATATAN HARIAN");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 24;
        gridBagConstraints.ipady = 24;
        jPanel6.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jPanel6, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCatatanBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatatanBaruActionPerformed
        jDateChooser.setEnabled(true);
        txtJudul.setEnabled(true);
        txtCatatan.setEnabled(true);
        btnSimpan.setEnabled(true);
        
        jDateChooser.setCalendar(null);
        txtJudul.setText("Judul");
        txtCatatan.setText("");
        jDateChooser.requestFocusInWindow();
    }//GEN-LAST:event_btnCatatanBaruActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        String judul = txtJudul.getText();
        String catatan = txtCatatan.getText();
        String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(jDateChooser.getDate());
        simpanCatatan(judul, catatan, tanggal);
        jDateChooser.setCalendar(null);
        txtJudul.setText("Judul");
        txtCatatan.setText("");
        
        jDateChooser.setEnabled(false);
        txtJudul.setEnabled(false);
        txtCatatan.setEnabled(false);
        btnSimpan.setEnabled(false);
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        jDateChooser.setEnabled(true);
        txtJudul.setEnabled(true);
        txtCatatan.setEnabled(true);
        btnSimpan.setEnabled(false);
        btnSimpanEdit.setEnabled(true);
    }//GEN-LAST:event_btnEditActionPerformed

    
    
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        String selectedJudul = listCatatan.getSelectedValue(); // Ambil judul yang dipilih di JList
        if (selectedJudul != null) {
            int result = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus catatan \"" + selectedJudul + "\"?");
            if (result == JOptionPane.YES_OPTION) {
                int id = getIdFromJudul(selectedJudul); // Ambil ID dari judul
                if (id != -1) {
                    hapusCatatan(id); // Hapus catatan berdasarkan ID
                } else {
                    JOptionPane.showMessageDialog(this, "ID tidak ditemukan untuk judul: " + selectedJudul);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih catatan untuk dihapus!");
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnEksporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEksporActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            eksporCSV(fileChooser.getSelectedFile());
        }
    }//GEN-LAST:event_btnEksporActionPerformed

    private void btnImporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImporActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imporCSV(fileChooser.getSelectedFile());
        }
    }//GEN-LAST:event_btnImporActionPerformed

    private void listCatatanValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listCatatanValueChanged
        if (!evt.getValueIsAdjusting()) { // Mencegah event dipicu dua kali
            String selectedJudul = listCatatan.getSelectedValue(); // Ambil judul yang dipilih
            if (selectedJudul != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM catatan WHERE judul = ?")) {
                    stmt.setString(1, selectedJudul); // Cari data berdasarkan judul
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // Tampilkan data ke komponen input
                            txtJudul.setText(rs.getString("judul"));
                            txtCatatan.setText(rs.getString("catatan"));
                            jDateChooser.setDate(new SimpleDateFormat("yyyy-MM-dd")
                                    .parse(rs.getString("tanggal")));

                            // Pastikan komponen tetap tidak aktif
                            txtJudul.setEnabled(false);
                            txtCatatan.setEnabled(false);
                            jDateChooser.setEnabled(false);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage());
                }
            }
        }
    }//GEN-LAST:event_listCatatanValueChanged

    private void btnSimpanEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanEditActionPerformed
        String judulBaru = txtJudul.getText();
        String catatanBaru = txtCatatan.getText();
        String tanggalBaru = new SimpleDateFormat("yyyy-MM-dd").format(jDateChooser.getDate());
        String selectedJudul = listCatatan.getSelectedValue(); // Ambil judul asli dari JList

        if (selectedJudul != null) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE catatan SET judul = ?, catatan = ?, tanggal = ? WHERE judul = ?")) {
                stmt.setString(1, judulBaru); // Judul baru
                stmt.setString(2, catatanBaru); // Isi catatan baru
                stmt.setString(3, tanggalBaru); // Tanggal baru
                stmt.setString(4, selectedJudul); // Judul asli untuk menemukan data yang akan diupdate
                stmt.executeUpdate(); // Eksekusi query

                JOptionPane.showMessageDialog(this, "Catatan berhasil diperbarui!");
                loadCatatan(); // Refresh daftar catatan di JList

                // Nonaktifkan kembali komponen input
                txtJudul.setEnabled(false);
                txtCatatan.setEnabled(false);
                jDateChooser.setEnabled(false);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui catatan: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih catatan untuk diperbarui!");
        }
    }//GEN-LAST:event_btnSimpanEditActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Set FlatLaf sebagai Look and Feel
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize FlatLaf");
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CatatanFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCatatanBaru;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEkspor;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImpor;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnSimpanEdit;
    private com.toedter.calendar.JDateChooser jDateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> listCatatan;
    private javax.swing.JTextArea txtCatatan;
    private javax.swing.JTextField txtJudul;
    // End of variables declaration//GEN-END:variables
}
