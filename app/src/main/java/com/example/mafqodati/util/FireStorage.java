package com.example.mafqodati.util;

import android.net.Uri;

import com.example.mafqodati.models.Post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FireStorage {
    public static FirebaseStorage getStorageInstance() {
        return FirebaseStorage.getInstance();
    }

    public static StorageReference postRef = getStorageInstance().getReference().child("posts");
    public static StorageReference userPostsRef = postRef.child(Auth.getUserId() + "-" + Auth.getCurrentUser().getEmail());
    public static StorageReference userPicRef = getStorageInstance().getReference().child("pictures");

    public static Task<byte[]> getImageFromURL(String URL) {
        StorageReference profileReference = getStorageInstance().getReferenceFromUrl(URL);
        return profileReference.getBytes(1500000);
    }

    public static Task<List<String>> uploadImages(List<Uri> imageUris) {
        List<Task<Uri>> uploadTasks = new ArrayList<>();

        for (Uri imageUri : imageUris) {
            StorageReference imageRef = userPostsRef.child("images").child(UUID.randomUUID().toString());
            uploadTasks.add(imageRef.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Return the download URL
                return imageRef.getDownloadUrl();
            }));
        }
        // Combine all tasks into a single task
        return Tasks.whenAllSuccess(uploadTasks);
    }




    public static Task<UploadTask.TaskSnapshot> uploadPostImages(int i) {
        final int[] finalI = {i};
        Task<UploadTask.TaskSnapshot> tasks = null;
        StorageReference imageName = userPostsRef.child("post-at" + System.currentTimeMillis()).child("" + System.currentTimeMillis());
        tasks = imageName.putFile(Uri.parse(Post.getInstance().getImagesUri().get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (finalI[0] < Post.getInstance().getImagesUri().size()){
                            Post.getInstance().getImagesUri().set(finalI[0] , uri.toString());
                            uploadPostImages(finalI[0]++);
                        }
                    }
                });
            }
        });

//        for (int i = 0; i < Post.getInstance().getImagesUri().size(); i++) {
//            StorageReference imageName = userPostsRef.child("post-at" + System.currentTimeMillis()).child("" + System.currentTimeMillis());
//            int finalI = i;
//            tasks = imageName.putFile(Uri.parse(Post.getInstance().getImagesUri().get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//
//                        }
//                    });
//                }
//            });
//
//        }

        return tasks;
    }

    public static Task<String> uploadImage(Uri imageUri, String userId) {
        StorageReference imageRef = getStorageInstance().getReference().child("users").child(userId + System.currentTimeMillis()).child(UUID.randomUUID().toString());

        return imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Return the download URL
                    return imageRef.getDownloadUrl();
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            return downloadUri.toString();
                        } else {
                            throw new Exception("Download URL is null");
                        }
                    } else {
                        throw task.getException();
                    }
                });
    }

    public static Task<UploadTask.TaskSnapshot> uploadProfilePicture(Uri picUri) {
        Task<UploadTask.TaskSnapshot> tasks = null;
        tasks = userPicRef.child(Auth.getUserId() + System.currentTimeMillis()).putFile(picUri);
        return tasks;

    }
}
