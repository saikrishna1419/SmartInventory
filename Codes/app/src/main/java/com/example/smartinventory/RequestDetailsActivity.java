package com.example.smartinventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import java.io.InputStream;

public class RequestDetailsActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;
    private TextView usernameET;
    private TextView productNameTv;
    private TextView upcTv;
    private TextView quantityET; // Added quantityET
    private TextView labelNumberET; // Added labelNumberET
    private TextView addressET; // Added addressET
    private TextView pincodeET; // Added pincodeET
    private TextView stateET; // Added stateET
    private TextView countryET; // Added countryET
    private Button uploadButton;
    private Button sendRequestButton;
    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth auth; // Firebase Authentication instance

    private ActivityResultLauncher<Intent> pdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize your views
        usernameET = findViewById(R.id.usernameET);
        productNameTv = findViewById(R.id.productNameTV);
        upcTv = findViewById(R.id.upcTV);
        quantityET = findViewById(R.id.quantityET); // Initialize quantityET
        labelNumberET = findViewById(R.id.labelNumberET); // Initialize labelNumberET
        addressET = findViewById(R.id.addressET); // Initialize addressET
        pincodeET = findViewById(R.id.pincodeET); // Initialize pincodeET
        stateET = findViewById(R.id.stateET); // Initialize stateET
        countryET = findViewById(R.id.countryET); // Initialize countryET
        uploadButton = findViewById(R.id.uploadPdfButton);
        sendRequestButton = findViewById(R.id.sendButton);

        // Get product details from Intent
        String productName = getIntent().getStringExtra("productName");
        String upc = getIntent().getStringExtra("upc");
        String quantity = String.valueOf(getIntent().getIntExtra("quantity", 0));

        // Set product details to the UI
        productNameTv.setText(productName);
        upcTv.setText(upc);
        quantityET.setText(String.valueOf(quantity));

        // Setup the PDF upload launcher
        pdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            extractDataFromPDF(uri);
                        }
                    }
                });

        // Set upload button click listener
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            pdfLauncher.launch(intent);
        });

        sendRequestButton.setOnClickListener(view -> saveRequestData());
    }

    // Method to extract data from PDF
    private void extractDataFromPDF(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                PdfReader pdfReader = new PdfReader(inputStream);
                PdfDocument pdfDocument = new PdfDocument(pdfReader);
                StringBuilder pdfText = new StringBuilder();

                // Extract text from each page of the PDF
                for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
                    SimpleTextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
                    String pageText = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i), strategy);
                    pdfText.append(pageText);
                }

                pdfDocument.close(); // Close the document after extraction

                String extractedText = pdfText.toString();
                Log.d("Extracted PDF Text", extractedText); // Log the extracted text for debugging

                // Parse the extracted text
                String[] lines = extractedText.split("\n");
                String username = "", productName = "", upc = "", labelNumber = "", address = "", pincode = "", state = "", country = "";
                for (String line : lines) {
                    if (line.startsWith("Product Name:")) {
                        productName = line.substring("Product Name:".length()).trim();
                    } else if (line.startsWith("UPC:")) {
                        upc = line.substring("UPC:".length()).trim();
                    } else if (line.startsWith("Quantity:")) {
                        String quantityStr = line.substring("Quantity:".length()).trim();
                        quantityET.setText(quantityStr); // Set the quantity in quantityET
                    } else if (line.startsWith("Username:")) {
                        username = line.substring("Username:".length()).trim();
                    } else if (line.startsWith("labelNumber:")) {
                        labelNumber = line.substring("labelNumber:".length()).trim();
                    } else if (line.startsWith("Address:")) {
                        address = line.substring("Address:".length()).trim();
                    } else if (line.startsWith("Pincode:")) {
                        pincode = line.substring("Pincode:".length()).trim();
                    } else if (line.startsWith("State:")) {
                        state = line.substring("State:".length()).trim();
                    } else if (line.startsWith("Country:")) {
                        country = line.substring("Country:".length()).trim();
                    }
                }

                // Set the extracted values to the respective fields
                usernameET.setText(username);
                productNameTv.setText(productName);
                upcTv.setText(upc);
                labelNumberET.setText(labelNumber);
                addressET.setText(address);
                pincodeET.setText(pincode);
                stateET.setText(state);
                countryET.setText(country);
            }
        } catch (Exception e) {
            Log.e("PDF Error", "Error reading PDF file", e);
            Toast.makeText(this, "Error reading PDF file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle submission logic
    private void saveRequestData() {
        String productName = productNameTv.getText().toString().trim();
        String upc = upcTv.getText().toString().trim();
        String quantityStr = quantityET.getText().toString().trim();
        String labelNumber = labelNumberET.getText().toString().trim();
        String address = addressET.getText().toString().trim();
        String pincode = pincodeET.getText().toString().trim();
        String state = stateET.getText().toString().trim();
        String country = countryET.getText().toString().trim();

        // Fetch the logged-in user's email
        FirebaseUser currentUser = auth.getCurrentUser();
        String username = currentUser != null ? currentUser.getEmail() : null;

        // Validate inputs
        if (productName.isEmpty() || upc.isEmpty() || quantityStr.isEmpty() || username == null ||
                labelNumber.isEmpty() || address.isEmpty() || pincode.isEmpty() || state.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return; // Prevent submission if fields are empty
        }

        // Construct the correct path to the inventory
        String inventoryPath = String.format("/users/%s/inventory/T1234/items", username);
        Log.d("Inventory Path", inventoryPath);

        // Check the inventory quantity from Firestore
        db.collection(inventoryPath).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No products found in inventory", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean productFound = false;
                        for (DocumentSnapshot document : task.getResult()) {
                            String inventoryProductName = document.getString("productName");
                            String inventoryUPC = document.getString("upc");
                            Log.d("Inventory Data", "Checking Product: " + inventoryProductName + ", UPC: " + inventoryUPC);

                            if (inventoryProductName.equals(productName) && inventoryUPC.equals(upc)) {
                                productFound = true;

                                // Update the request collection with the data, including the "status" field
                                db.collection("requests").add(new RequestItem(productName, upc, quantityStr, username, labelNumber, address, pincode, state, country, "New"))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Request submitted successfully", Toast.LENGTH_SHORT).show();

                                            // Update the inventory quantity
                                            String currentQuantity = document.getString("quantity");
                                            String updatedQuantity = currentQuantity + "(-" + quantityStr + ")";
                                            document.getReference().update("quantity", updatedQuantity)
                                                    .addOnSuccessListener(aVoid1 -> Log.d("Inventory Update", "Inventory quantity updated successfully"))
                                                    .addOnFailureListener(e -> Log.e("Inventory Update", "Error updating inventory quantity", e));
                                        })
                                        .addOnFailureListener(e -> Log.e("Request Error", "Error submitting request", e));
                            }
                        }
                        if (!productFound) {
                            Toast.makeText(this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

}
