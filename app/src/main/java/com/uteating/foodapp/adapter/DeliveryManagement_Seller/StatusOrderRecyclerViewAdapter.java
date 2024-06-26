package com.uteating.foodapp.adapter.DeliveryManagement_Seller;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uteating.foodapp.Interface.APIService;
import com.uteating.foodapp.R;
import com.uteating.foodapp.RetrofitClient;
import com.uteating.foodapp.activity.orderSellerManagement.DetailOfOrderDeliveryManagementActivity;
import com.uteating.foodapp.custom.SuccessfulToast;
import com.uteating.foodapp.databinding.ItemOrderStatusListBinding;
import com.uteating.foodapp.helper.FirebaseNotificationHelper;
import com.uteating.foodapp.helper.FirebaseStatusOrderHelper;
import com.uteating.foodapp.model.Bill;
import com.uteating.foodapp.model.BillInfo;
import com.uteating.foodapp.model.Notification;
import com.uteating.foodapp.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusOrderRecyclerViewAdapter extends RecyclerView.Adapter<StatusOrderRecyclerViewAdapter.ViewHolder> {
    Context mContext;
    List<Bill> billList;
    APIService apiService;
    private int remainAmount;
    private int sold;
    public StatusOrderRecyclerViewAdapter(Context mContext, List<Bill> billList) {
        this.mContext = mContext;
        this.billList = billList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusOrderRecyclerViewAdapter.ViewHolder(ItemOrderStatusListBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Bill bill = billList.get(position);
        holder.binding.txtOrderId.setText(bill.getBillId());
        holder.binding.txtStatus.setText(bill.getOrderStatus());
        holder.binding.txtDateOfOrder.setText(bill.getOrderDate());
        holder.binding.txtOrderTotal.setText(convertToMoney(bill.getTotalPrice()) + "đ");


        holder.binding.imgProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(mContext)
                .asBitmap()
                .load(bill.getImageUrl())
                .into(holder.binding.imgProductImage);

        if (bill.getOrderStatus().equals("Confirm")) {
            holder.binding.btnChangeStatus.setText("Confirm");

            holder.binding.btnChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckRemainAmount(bill.getBillId(), new RemainAmountCallback() {
                        @Override
                        public void onCheckComplete(boolean canChangeStatus) {
                            if (canChangeStatus) {

                                new FirebaseStatusOrderHelper().setConfirmToShipping(bill.getBillId(), new FirebaseStatusOrderHelper.DataStatus() {
                                    @Override
                                    public void DataIsLoaded(List<Bill> bills, boolean isExistingBill) {
                                    }
                                    @Override
                                    public void DataIsInserted() {
                                    }
                                    @Override
                                    public void DataIsUpdated() {
                                        new SuccessfulToast(mContext, "Order has been changed to shipping state!").showToast();
                                        pushNotificationOrderStatusForReceiver(bill.getBillId(), " đang giao hàng", bill.getRecipientId(), bill.getImageUrl());
                                    }

                                    @Override
                                    public void DataIsDeleted() {
                                    }
                                });
                            } else {
                                holder.binding.btnChangeStatus.setEnabled(false);
                                holder.binding.btnChangeStatus.setBackgroundResource(R.drawable.background_feedback_disnable_button);
                            }
                        }
                    });
                }
            });
        }
        else if (bill.getOrderStatus().equals("Shipping"))
        {
            holder.binding.btnChangeStatus.setText("Completed");
            holder.binding.btnChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FirebaseStatusOrderHelper().setShippingToCompleted(bill.getBillId(), new FirebaseStatusOrderHelper.DataStatus() {
                        @Override
                        public void DataIsLoaded(List<Bill> bills, boolean isExistingBill) {

                        }

                        @Override
                        public void DataIsInserted() {

                        }

                        @Override
                        public void DataIsUpdated() {
                            new SuccessfulToast(mContext, "Order has been changed to completed state!").showToast();
                            pushNotificationOrderStatusForReceiver(bill.getBillId()," giao hàng thành công",bill.getRecipientId(), bill.getImageUrl());
                        }

                        @Override
                        public void DataIsDeleted() {

                        }
                    });
                }
            });
        }
        else {
            holder.binding.txtStatus.setTextColor(Color.parseColor("#48DC7D"));
            holder.binding.btnChangeStatus.setVisibility(View.GONE);
        }

        // view detail
        holder.binding.parentOfItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailOfOrderDeliveryManagementActivity.class);
                intent.putExtra("billId",bill.getBillId());
                intent.putExtra("addressId",bill.getAddressId());
                intent.putExtra("recipientId",bill.getRecipientId());
                intent.putExtra("totalBill",bill.getTotalPrice());
                intent.putExtra("orderStatus",bill.getOrderStatus());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ItemOrderStatusListBinding binding;

        public ViewHolder(@NonNull ItemOrderStatusListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
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

    public void pushNotificationOrderStatusForReceiver(String billId,String status,String receiverId,String productImage1) {
        String title = "Order status";
        String content = "Order "+ billId + " has been updated to "+ status+ ", go to My Order to check it.";
        Notification notification = FirebaseNotificationHelper.createNotification(title,content,productImage1,"None",billId,"None", null);
        new FirebaseNotificationHelper(mContext).addNotification(receiverId, notification, new FirebaseNotificationHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Notification> notificationList,List<Notification> notificationListToNotify) {

            }

            @Override
            public void DataIsInserted() {

            }

            @Override
            public void DataIsUpdated() {

            }

            @Override
            public void DataIsDeleted() {

            }
        });
    }

    public interface RemainAmountCallback {
        void onCheckComplete(boolean canChangeStatus);
    }
    private void CheckRemainAmount(String billId, RemainAmountCallback callback) {
        DatabaseReference billInfoRef = FirebaseDatabase.getInstance().getReference("BillInfos").child(billId);
        billInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    List<BillInfo> billInfoList = new ArrayList<>();

                    for (DataSnapshot billInfoSnapshot : snapshot.getChildren()) {
                        BillInfo billInfo = billInfoSnapshot.getValue(BillInfo.class);
                        if (billInfo != null) {
                            billInfoList.add(billInfo);
                        }
                    }

                    checkRemainAmountForAllProducts(billInfoList, callback);
                } else {
                    callback.onCheckComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCheckComplete(false);
            }
        });
    }

    private void checkRemainAmountForAllProducts(List<BillInfo> billInfoList, RemainAmountCallback callback) {
        AtomicInteger pendingRequests = new AtomicInteger(billInfoList.size());
        AtomicBoolean allValid = new AtomicBoolean(true);

        for (BillInfo billInfo : billInfoList) {
            int sold = billInfo.getAmount();
            APIService apiService = RetrofitClient.getRetrofit().create(APIService.class);
            apiService.getProductInfor(billInfo.getProductId()).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = response.body();
                        int remainAmount = product.getRemainAmount();
                        if (remainAmount < sold) {
                            allValid.set(false);
                        }
                    } else {
                        allValid.set(false);
                    }
                    if (pendingRequests.decrementAndGet() == 0) {
                        callback.onCheckComplete(allValid.get());
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    allValid.set(false);
                    if (pendingRequests.decrementAndGet() == 0) {
                        callback.onCheckComplete(false);
                    }
                }
            });
        }

        // If there are no items in the list, directly call the callback with false
        if (billInfoList.isEmpty()) {
            callback.onCheckComplete(false);
        }
    }


}
