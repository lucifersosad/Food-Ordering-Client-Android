package com.uteating.foodapp.activity.orderSellerManagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.uteating.foodapp.R;
import com.uteating.foodapp.databinding.ActivityDetailOfOrderDeliveryManagementBinding;
import com.uteating.foodapp.model.BillInfo;

import java.util.List;

public class DetailOfOrderDeliveryManagementActivity extends AppCompatActivity {
    private ActivityDetailOfOrderDeliveryManagementBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailOfOrderDeliveryManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));

        Intent intent = getIntent();
        if (null != intent)
        {
            String billId = intent.getStringExtra("billId");
            String addressId = intent.getStringExtra("addressId");
            String recipientId = intent.getStringExtra("recipientId");
            String orderStatus = intent.getStringExtra("orderStatus");
            long price = intent.getLongExtra("totalBill",-1);
            try {
                binding.txtOrderIdDetail.setText("Order Id: "+billId);
                binding.txtBillTotalInDetail.setText(convertToMoney(price)+ "đ");
                binding.txtStatusOrderDetail.setText(orderStatus);
                if (orderStatus.equals("Completed"))
                {
                    binding.txtStatusOrderDetail.setTextColor(Color.parseColor("#48DC7D"));
                }

            }
            catch (Exception ex)
            {

            }
        }

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private String convertToMoney(long price) {
        String temp = String.valueOf(price);
        String output = "";
        int count = 3;
        for (int i = temp.length() - 1; i >= 0; i--) {
            count--;
            if (count == 0) {
                count = 3;
                output = "," + temp.charAt(i) + output;
            }
            else {
                output = temp.charAt(i) + output;
            }
        }

        if (output.charAt(0) == ',')
            return output.substring(1);

        return output;
    }
}