package database.firebase;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.*;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class offers an API for handling communication with
 * the storage bucket on Firebase. The online bucket communicated
 * with is responsible for holding user input images and files.
 *
 * @author Walker Willetts
 */
public class FileStorageConnector extends FirebaseConnector {

    /* Instance Variables */
    private Bucket storageBucket;
    private DatabaseReference storageDBRef;
    private Map<String, String> fileMap;

    /**
     * Creates a FileStorageConnector that can be used to
     * talk to the file storage bucket on Firebase
     */
    public FileStorageConnector() {
        storageBucket = StorageClient.getInstance().bucket();
        storageDBRef = FirebaseDatabase.getInstance().getReference("storage");
        fileMap = new HashMap<>();
        setupListening();
    }

    /**
     * Sets up listening with the realtime Firebase DB in order to properly keep
     * track of the location and list of files stored within the file storage
     * in Firebase
     */
    private void setupListening() {
        storageDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fileMap.put(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fileMap.put(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fileMap.remove(dataSnapshot.getKey());
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                fileMap.put(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });
    }

    /**
     * Saves the passed in File online to the Firebase file storage bucket.
     * @param file is a {@code File} representing the file to be saved online
     * @throws IOException when the File cannot be read
     */
    public void saveFile(File file) throws IOException {
        // Creates a formatted file name that hides the extension
        String fileName = file.getName().split("\\.")[0];

        // Try reading in the raw bytes of the file, and
        // throw an exception if the I/O fails
        byte[] fileBytes;
        try { fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) { throw e; }

        // Store the file in the storage bucket, and update db mapping
        storageBucket.create(file.getName(), fileBytes);
        storageDBRef.child(fileName).setValueAsync(file.getName());
        fileMap.put(fileName, file.getName());
    }

    /**
     * Returns the image requested that corresponds to the given name
     * @param image is a {@code String} representing the name of the image as saved online
     * @return An {@code Image} object that is the image saved online
     */
    public Image retrieveImage(String image) {
        // Get the raw bytes corresponding to the image from the storage bucket
        byte[] imageBytes = storageBucket.get(fileMap.get(image)).getContent();
        // Create a new image from the bytes and return it
        return new Image(new ByteArrayInputStream(imageBytes));
    }

    /**
     * @return A {@code String[]} that contains all of the names of files stored online
     */
    public String[] fileNames() {
        return fileMap.keySet().toArray(new String[0]);
    }
}
