package com.uteating.foodapp.activity.Cart_PlaceOrder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uteating.foodapp.Interface.IAdapterItemListener;
import com.uteating.foodapp.adapter.Cart.CartProductAdapter;
import com.uteating.foodapp.databinding.ActivityCartBinding;
import com.uteating.foodapp.model.Cart;
import com.uteating.foodapp.model.CartInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private String userId;
    private ActivityCartBinding binding;

    private CartProductAdapter cartProductAdapter;
    private List<CartInfo> cartInfoList;

    private boolean isCheckAll = false;
    private ArrayList<CartInfo> buyProducts = new ArrayList<>();
    private ActivityResultLauncher<Intent> proceedOrderLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("userId");

        initToolbar();
        initProceedOrderLauncher();

        binding.recyclerViewCartProduct.setHasFixedSize(true);
        binding.recyclerViewCartProduct.setLayoutManager(new LinearLayoutManager(this));

        cartInfoList = new ArrayList<>();

        getCartProducts();

        binding.proceedOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CartActivity.this, ProceedOrderActivity.class);
                intent.putExtra("buyProducts", buyProducts);
                String totalPriceDisplay = binding.totalPrice.getText().toString();
                intent.putExtra("totalPrice", totalPriceDisplay);
                intent.putExtra("userId",userId);
                proceedOrderLauncher.launch(intent);
            }
        });
    }

    private void initProceedOrderLauncher() {
        // Init launcher
        proceedOrderLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                finish();
            }
        });
    }

    private void reloadCartProducts() {
        FirebaseDatabase.getInstance().getReference().child("Carts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Cart cart = ds.getValue(Cart.class);
                    if (cart.getUserId().equals(userId)) {
                        FirebaseDatabase.getInstance().getReference().child("CartInfos").child(cart.getCartId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                cartInfoList.clear();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    CartInfo cartInfo = ds.getValue(CartInfo.class);
                                    cartInfoList.add(cartInfo);
                                }
                                Collections.reverse(cartInfoList);
                                cartProductAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cartProductAdapter != null) {
            cartProductAdapter.saveStates(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (cartProductAdapter != null) {
            cartProductAdapter.restoreStates(savedInstanceState);
        }
    }

    private void getCartProducts() {
        FirebaseDatabase.getInstance().getReference().child("Carts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Cart cart = ds.getValue(Cart.class);
                    if (cart.getUserId().equals(userId)) {
                        Collections.reverse(cartInfoList);
                        cartProductAdapter = new CartProductAdapter(CartActivity.this, cartInfoList, cart.getCartId(), isCheckAll,userId);
                        cartProductAdapter.setAdapterItemListener(new IAdapterItemListener() {
                            @Override
                            public void onCheckedItemCountChanged(int count, long price, ArrayList<CartInfo> selectedItems) {
                                binding.totalPrice.setText("" + convertToMoney(price) + "đ");
                                buyProducts = selectedItems;

                                if (count > 0) {
                                    binding.proceedOrder.setEnabled(true);
                                }
                                else {
                                    binding.proceedOrder.setEnabled(false);
                                }
                            }

                            @Override
                            public void onAddClicked() {
                                reloadCartProducts();
                            }

                            @Override
                            public void onSubtractClicked() {
                                reloadCartProducts();
                            }

                            @Override
                            public void onDeleteProduct() {
                                reloadCartProducts();
                            }
                        });
                        binding.recyclerViewCartProduct.setAdapter(cartProductAdapter);

                        FirebaseDatabase.getInstance().getReference().child("CartInfos").child(cart.getCartId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                cartInfoList.clear();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    CartInfo cartInfo = ds.getValue(CartInfo.class);
                                    cartInfoList.add(cartInfo);
                                }
                                Collections.reverse(cartInfoList);
                                cartProductAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initToolbar() {
        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("My cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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