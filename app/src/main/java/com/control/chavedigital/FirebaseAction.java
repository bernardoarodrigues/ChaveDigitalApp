package com.control.chavedigital;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseAction {
    // OnUser Interface
    public interface UserListener {
        void onData(UserModel userData);
    }

    // Begins realtime updates listener from user data in Firebase
    public ValueEventListener onUser(String userId, UserListener userListener) {
        return FirebaseDatabase.getInstance().getReference("/users/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userData = snapshot.getValue(UserModel.class);
                userListener.onData(userData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Stops realtime updates listener from user data in Firebase
    public void offUser(String userId, ValueEventListener userListener) {
        FirebaseDatabase.getInstance().getReference("/users/" + userId).removeEventListener(userListener);
    }

    // Gets snapshot from user data in Firebase
    public void onceUser(String userId, UserListener userListener) {
        FirebaseDatabase.getInstance().getReference("/users/" + userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userData = snapshot.getValue(UserModel.class);
                userListener.onData(userData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // setUserChild Interface
    public interface SetUserChildCallback {
        void onComplete(Exception error);
    }

    // Changes user data in Firebase
    public void setUserChild(String userId, String child, String value, SetUserChildCallback callback) {
        FirebaseDatabase
            .getInstance()
            .getReference("/users/" + userId)
            .child(child)
            .setValue(value)
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()) callback.onComplete(null);
                else callback.onComplete(task.getException());
            });
    }

    // onDevice Interface
    public interface DeviceListener {
        void onData(DeviceModel deviceData);
    }

    // Begins realtime updates listener from device data in Firebase
    public ValueEventListener onDevice(String deviceId, DeviceListener deviceListener) {
        return FirebaseDatabase.getInstance().getReference("/devices/" + deviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DeviceModel deviceData = snapshot.getValue(DeviceModel.class);
                deviceListener.onData(deviceData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Stops realtime updates listener from user data in Firebase
    public void offDevice(String deviceId, ValueEventListener deviceListener) {
        FirebaseDatabase.getInstance().getReference("/devices/" + deviceId).removeEventListener(deviceListener);
    }

    // onDeviceChild Interface
    public interface DeviceChildListener {
        void onData(String deviceChildData);
    }

    // Begins realtime updates listener from device child data in Firebase
    public ValueEventListener onDeviceChild(String deviceId, String child, DeviceChildListener deviceChildListener) {
        return FirebaseDatabase.getInstance().getReference("/devices/" + deviceId).child(child).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String deviceChildData = snapshot.getValue(String.class);
                deviceChildListener.onData(deviceChildData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Stops realtime updates listener from device child data in Firebase
    public void offDeviceChild(String deviceId, String child, ValueEventListener deviceChildListener) {
        FirebaseDatabase.getInstance().getReference("/devices/" + deviceId).child(child).removeEventListener(deviceChildListener);
    }

    // Gets snapshot from device data in Firebase
    public void onceDevice(String deviceId, DeviceListener deviceListener) {
        FirebaseDatabase.getInstance().getReference("/devices/" + deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DeviceModel deviceData = snapshot.getValue(DeviceModel.class);
                deviceListener.onData(deviceData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                deviceListener.onData(null);
            }
        });
    }

    // setDeviceChild Interface
    public interface SetDeviceChildCallback {
        void onComplete(Exception error);
    }

    // Changes device data in Firebase
    public void setDeviceChild(String deviceId, String child, String value, SetDeviceChildCallback callback) {
        FirebaseDatabase
            .getInstance()
            .getReference("/devices/" + deviceId)
            .child(child)
            .setValue(value)
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()) callback.onComplete(null);
                else callback.onComplete(task.getException());
            });
    }
}
