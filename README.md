# EncDec-AES-GUI

**EncDec-AES-GUI** is a Java GUI application that allows users to **encrypt and decrypt text files securely** using AES encryption. The application uses **AES-GCM mode** with a 256-bit key derived from a password, ensuring strong security.  

---

## Features

- AES-GCM encryption with **password-derived 256-bit key** (PBKDF2WithHmacSHA256)
- Encrypt text and save to a file
- Decrypt encrypted files
- Displays:
  - Filename of the encrypted file
  - Encrypted content (Base64 encoded)
  - Decrypted content
- Drag-and-drop support for quick decryption
- Show/hide password toggle
- Copy output to clipboard easily
- Works on any PC with Java installed

---

## How it Works

1. **Encryption**
   - You enter the text to encrypt and a **password**.
   - The program generates a **salt** and **IV** (Initialization Vector) for AES-GCM.
   - The AES key is derived from your password using **PBKDF2WithHmacSHA256**.
   - The text is encrypted and saved to a file along with the salt and IV.
   - **Important:** You must remember this password to decrypt the file later.

2. **Decryption**
   - Enter the **same password** used for encryption.
   - Select the encrypted file or drag-and-drop it into the output area.
   - The program reads the salt and IV from the file, regenerates the AES key from the password, and decrypts the content.
   - Output shows:
     ```
     File: <filename>
     Encrypted Content:
     <base64 ciphertext>

     Decrypted Content:
     <original text>
     ```

---

## File Format

The encrypted file contains three lines:

1. **Salt** (Base64 encoded)
2. **IV** (Base64 encoded)
3. **Ciphertext** (Base64 encoded)

This allows the program to decrypt it anywhere as long as the correct password is provided.

---

## Usage

1. Compile and run:

```bash
javac EncDec.java
java EncDec
````

2. **Encrypting a file**

   * Enter text in the input area.
   * Enter a password.
   * Click **Encrypt & Save** and choose a location to save the file.
   * The encrypted file will contain all information needed for decryption (salt, IV, ciphertext).

3. **Decrypting a file**

   * Enter the **same password** used during encryption.
   * Click **Decrypt File** and select the encrypted file, or drag-and-drop it into the output area.
   * Output area will show filename, encrypted content, and decrypted text.

4. **Running on any PC**

   * Copy the `.java` file or the compiled `.class` file to another PC with Java installed.
   * Run the program, provide the password, and open the encrypted file. The content will decrypt correctly.

---

## Requirements

* Java 8 or higher
* No additional libraries required

---

## Project Structure

```text
EncDec-AES-GUI/
│
├── EncDec.java        # Main Java file
├── README.md          # This file
```

---

## Author

Jatin
