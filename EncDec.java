import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.List;

public class EncDec extends JFrame {

    private JTextArea inputArea, outputArea;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;

    public EncDec() {
        setTitle("Advanced AES-GCM Encryption Tool");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        inputArea = new JTextArea();
        outputArea = new JTextArea();
        passwordField = new JPasswordField();
        showPasswordCheck = new JCheckBox("Show Password");

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(inputArea), new JScrollPane(outputArea));
        splitPane.setDividerLocation(200);

        // Top panel with password field + show/hide checkbox
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(new JLabel("Enter Password:"), BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordCheck, BorderLayout.EAST);

        topPanel.add(passwordPanel, BorderLayout.NORTH);

        // Toggle show/hide password
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordField.setEchoChar((char) 0); // show password
            } else {
                passwordField.setEchoChar('•'); // hide password
            }
        });

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton encryptBtn = new JButton("Encrypt & Save");
        JButton decryptBtn = new JButton("Decrypt File");
        JButton copyBtn = new JButton("Copy Output");
        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        buttonPanel.add(copyBtn);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        encryptBtn.addActionListener(e -> encryptAndSave());
        decryptBtn.addActionListener(e -> chooseAndDecrypt());
        copyBtn.addActionListener(e -> {
            String text = outputArea.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(text), null);
            JOptionPane.showMessageDialog(this, "Copied to clipboard!");
        });

        new DropTarget(outputArea, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) evt.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    decryptFile(files.get(0));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to drop file!");
                }
            }
        });

        setVisible(true);
    }

    private SecretKey getKeyFromPassword(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private byte[] generateIV() {
        byte[] iv = new byte[12]; // 12 bytes for GCM
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private String encryptText(String plaintext, char[] password) throws Exception {
        byte[] salt = generateSalt();
        byte[] iv = generateIV();
        SecretKey key = getKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());

        return Base64.getEncoder().encodeToString(salt) + "\n" +
               Base64.getEncoder().encodeToString(iv) + "\n" +
               Base64.getEncoder().encodeToString(encrypted);
    }

    private String decryptText(String cipherData, char[] password) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(cipherData));
        byte[] salt = Base64.getDecoder().decode(reader.readLine());
        byte[] iv = Base64.getDecoder().decode(reader.readLine());

        StringBuilder cipherTextBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            cipherTextBuilder.append(line);
        }
        byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBuilder.toString());

        SecretKey key = getKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return new String(cipher.doFinal(cipherBytes));
    }

    private void encryptAndSave() {
        try {
            String text = inputArea.getText();
            char[] password = passwordField.getPassword();
            if (text.isEmpty() || password.length == 0) {
                JOptionPane.showMessageDialog(this, "Enter text and password!");
                return;
            }

            String encryptedData = encryptText(text, password);

            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    writer.write(encryptedData);
                }
                // Display only ciphertext
                outputArea.setText(encryptedData);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Encryption failed: " + ex.getMessage());
        }
    }

    private void chooseAndDecrypt() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            decryptFile(chooser.getSelectedFile());
        }
    }

    private void decryptFile(File file) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            }

            // Show filename + encrypted content
            String encryptedText = sb.toString();
            String display = "File: " + file.getName() + "\n\nEncrypted Content:\n" + encryptedText;

            char[] password = passwordField.getPassword();
            if (password.length == 0) {
                JOptionPane.showMessageDialog(this, "Enter password to decrypt!");
                return;
            }

            String decrypted = decryptText(encryptedText, password);
            display += "\nDecrypted Content:\n" + decrypted;
            outputArea.setText(display);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Decryption failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EncDec::new);
    }
}
