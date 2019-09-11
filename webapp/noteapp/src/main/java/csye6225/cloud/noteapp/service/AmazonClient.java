package csye6225.cloud.noteapp.service;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AmazonClient {

    private static final Logger logger = LoggerFactory.getLogger(AmazonClient.class);
    private AmazonS3 s3client;

    @Value("${spring.amazonProperties.endpoint}")
    private String endpointUrl;
    @Value("${spring.amazonProperties.bucketName}")
    private String bucketName;
    @Value("${spring.datasource.url}")
    private String rdsUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    private void initializeAmazon() {
        logger.info(" Initializing Amazon S3 client ");
        this.s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        try {
            logger.info("Converting Multipart file to a file ");
            File convFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + file.getOriginalFilename());
            file.transferTo(convFile);
            return convFile;
        } catch (IOException e) {
            logger.error("Error in converting file : "+e);
            throw new IOException("Error converting file : ",e);
        }
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        logger.info("Uploading file to s3 bucket");
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public String uploadFile(MultipartFile multipartFile, String uuid) {

        String fileUrl = "";
        try {
            logger.info("Uploading multipart file ");
            File file = convertMultiPartToFile(multipartFile);
            String fileName = uuid + "-" + multipartFile.getOriginalFilename();
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            file.delete();
        } catch (Exception e) {
            logger.error("Error in uploading multipart file : ",e);
            e.printStackTrace();
        }
        return fileUrl;
    }

    public String deleteFileFromS3Bucket(String fileUrl) {
        logger.info("Deleting file from url "+fileUrl);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return " Successfully deleted file from S3 bucket ";
    }

    public Connection getRemoteConnection() {
            try {
                logger.info(" Getting remote connection ");
                Class.forName("com.mysql.cj.jdbc.Driver");
                String jdbcUrl = rdsUrl + "?user=" + username + "&password=" + password;
                Connection con = DriverManager.getConnection(jdbcUrl);
                return con;
            }
            catch (ClassNotFoundException e) {
                logger.error("Class not found exception in getting remote connection : ",e);
                e.printStackTrace();
            }
            catch (SQLException e) {
                logger.error("SQL Exception in getting remote connection : ",e);
                e.printStackTrace();
            }

        return null;
    }
}