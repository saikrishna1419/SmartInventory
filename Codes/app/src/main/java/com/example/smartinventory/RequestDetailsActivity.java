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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import java.io.InputStream;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private TextView usernameET, productNameTv, upcTv, quantityET, labelNumberET, addressET, pincodeET, stateET, countryET, pdfNameTV;
    private Button uploadButton, sendRequestButton;
    private Uri pdfUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private ActivityResultLauncher<Intent> pdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Initialize Firestore, FirebaseAuth, and Firebase Storage
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize your views
        usernameET = findViewById(R.id.usernameET);
        productNameTv = findViewById(R.id.productNameTV);
        upcTv = findViewById(R.id.upcTV);
        quantityET = findViewById(R.id.quantityET);
        labelNumberET = findViewById(R.id.labelNumberET);
        addressET = findViewById(R.id.addressET);
        pincodeET = findViewById(R.id.pincodeET);
        stateET = findViewById(R.id.stateET);
        countryET = findViewById(R.id.countryET);
        uploadButton = findViewById(R.id.uploadPdfButton);
        sendRequestButton = findViewById(R.id.sendButton);
        pdfNameTV = findViewById(R.id.pdfNameTV);

        // Get product details from Intent
        String productName = getIntent().getStringExtra("productName");
        String upc = getIntent().getStringExtra("upc");
        String quantity = getIntent().getStringExtra("quantity");

        // Set product details to the UI
        productNameTv.setText(productName);
        upcTv.setText(upc);
        quantityET.setText(quantity);

        // Setup the PDF upload launcher
        pdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            pdfUri = uri; // Set the PDF Uri
                            pdfNameTV.setText(uri.getLastPathSegment()); // Display the PDF name
                            extractDataFromPDF(uri); // Extract text from the PDF
                        }
                    }
                });

        // Set upload button click listener
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            pdfLauncher.launch(intent);
        });

        // Set send request button listener
        sendRequestButton.setOnClickListener(view -> uploadPdfAndSaveRequestData());
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

    // Method to upload PDF and then save request data
    private void uploadPdfAndSaveRequestData() {
        if (pdfUri == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return; // Exit if no PDF is selected
        }

        // Upload the PDF to Firebase Storage
        StorageReference storageRef = storage.getReference().child("pdfs/" + System.currentTimeMillis() + ".pdf");
        storageRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded PDF
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String pdfUrl = uri.toString();
                        saveRequestData(pdfUrl); // Save request data with PDF URL
                    }).addOnFailureListener(e -> {
                        Log.e("Download URL Error", "Error getting download URL", e);
                        Toast.makeText(RequestDetailsActivity.this, "Error getting download URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Upload Error", "Error uploading PDF", e);
                    Toast.makeText(RequestDetailsActivity.this, "Error uploading PDF", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to save request data to Firestore
    private void saveRequestData(String pdfUrl) {
        // Get the user's email from Firebase Auth
        FirebaseUser user = auth.getCurrentUser();
        String email = user.getEmail();

        // Retrieve the username based on the email
        db.collection("customers")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                        DocumentSnapshot userDocument = userTask.getResult().getDocuments().get(0);
                        String username = userDocument.getString("userName");

                        // Fetch tracking IDs for the user
                        db.collection("users")
                                .document(username)
                                .collection("inventory")
                                .get()
                                .addOnCompleteListener(trackingIdTask -> {
                                    if (trackingIdTask.isSuccessful()) {
                                        for (DocumentSnapshot trackingIdDocument : trackingIdTask.getResult()) {
                                            String trackingId = trackingIdDocument.getId(); // Get the tracking ID
                                            fetchProductFromInventory(username, trackingId, productNameTv.getText().toString(), quantityET.getText().toString(), pdfUrl, labelNumberET.getText().toString(), addressET.getText().toString(), pincodeET.getText().toString(), stateET.getText().toString(), countryET.getText().toString());
                                        }
                                    } else {
                                        Toast.makeText(this, "Error fetching tracking IDs", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to fetch product data from inventory based on tracking ID
    private void fetchProductFromInventory(String username, String trackingId, String productName, String quantityStr, String pdfUrl, String labelNumber, String address, String pincode, String state, String country) {
        // Fetch items for the specific tracking ID
        db.collection("users")
                .document(username)
                .collection("inventory")
                .document(trackingId).collection("items")
                .get()
                .addOnCompleteListener(inventoryTask -> {
                    if (inventoryTask.isSuccessful()) {
                        QuerySnapshot inventorySnapshot = inventoryTask.getResult();
                        boolean productFound = false;

                        for (DocumentSnapshot inventoryDocument : inventorySnapshot.getDocuments()) {
                            String inventoryProductName = inventoryDocument.getString("productName");
                            if (productName.equals(inventoryProductName)) {
                                productFound = true;
                                String currentQuantityStr = inventoryDocument.getString("quantity");
                                int currentQuantity = Integer.parseInt(currentQuantityStr);
                                int requestedQuantity = Integer.parseInt(quantityStr);

                                if (requestedQuantity > currentQuantity) {
                                    Toast.makeText(this, "Requested quantity exceeds available inventory", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Append requested quantity instead of subtracting
                                String updatedQuantity = currentQuantity + "(-" + quantityStr + ")";

                                // Add request data with the username
                                RequestItem requestItem = new RequestItem(
                                        productName, upcTv.getText().toString(), quantityStr, username,
                                        labelNumber, address, pincode, state, country,
                                        pdfUrl, "New", Timestamp.now());

                                db.collection("requests")
                                        .add(requestItem)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update inventory quantity for the matching product
                                            inventoryDocument.getReference().update("quantity", updatedQuantity)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(RequestDetailsActivity.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
                                                        finish(); // Close activity
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Error updating product quantity", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error submitting request", Toast.LENGTH_SHORT).show();
                                        });
                                break; // Stop the loop once the correct product is found and updated
                            }
                        }

                        if (!productFound) {
                            Toast.makeText(this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching product data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
