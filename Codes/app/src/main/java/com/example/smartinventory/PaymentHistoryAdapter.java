package com.example.smartinventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {

    private List<Payment> paymentList;

    public PaymentHistoryAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView paymentDetailsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            paymentDetailsTextView = itemView.findViewById(R.id.paymentDetailsTextView);
        }

        public void bind(Payment payment) {
            String details = "Amount: $" + payment.getAmount() + "\n" +
                    "Tracking ID: " + payment.getTrackingId() + "\n" +
                    "Date: " + payment.getDate(); // Update this to match your Payment class properties
            paymentDetailsTextView.setText(details);
        }
    }
}
