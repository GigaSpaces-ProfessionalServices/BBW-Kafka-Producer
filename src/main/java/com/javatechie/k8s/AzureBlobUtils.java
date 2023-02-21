package com.javatechie.k8s;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AzureBlobUtils {
    public static String getFileData(String accountName,String accountKey,String containerName,String fileName) {

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient(containerName);

        /*
         * Create a container in Storage blob account.
         */
        blobContainerClient.createIfNotExists();

        /*
         * Create a client that references a to-be-created blob in your Azure Storage account's container.
         * This returns a BlockBlobClient object that wraps the blob's endpoint, credential and a request pipeline
         * (inherited from containerClient). Note that blob names can be mixed case.
         */
        BlockBlobClient blobClient = blobContainerClient.getBlobClient(fileName).getBlockBlobClient();

        /*
         * Download the blob's content to output stream.
         */
        String dataFromFile = null;
        if(blobClient.exists()) {
            int dataSize = (int) blobClient.getProperties().getBlobSize();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
            blobClient.downloadStream(outputStream);
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            dataFromFile = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            System.out.printf("dataFromFile: " + dataFromFile);
        }
        return dataFromFile;
    }
}
