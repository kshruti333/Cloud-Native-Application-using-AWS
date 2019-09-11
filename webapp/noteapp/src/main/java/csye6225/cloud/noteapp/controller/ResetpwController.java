package csye6225.cloud.noteapp.controller;

import com.amazonaws.Response;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import csye6225.cloud.noteapp.exception.AppException;
import csye6225.cloud.noteapp.model.ResetUser;
import csye6225.cloud.noteapp.model.User;
import csye6225.cloud.noteapp.repository.UserRepository;
import csye6225.cloud.noteapp.service.AmazonClient;
import csye6225.cloud.noteapp.service.MetricsConfig;
import csye6225.cloud.noteapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import javax.validation.Valid;
import java.nio.ByteBuffer;
import java.util.List;

@RestController
public class ResetpwController {

    private static final Logger logger = LoggerFactory.getLogger(ResetpwController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    public MetricsConfig metricsConfig;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/reset")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetUser user) throws AppException {

        metricsConfig.statsDClient().incrementCounter("ResetPassword_API");
        logger.info("ResetPassword called");
        JsonObject jsonObject = new JsonObject();
        User up = userService.findUserByEmail(user.getEmail());
        if(up != null)
        {
            AmazonSNS snsClient = AmazonSNSAsyncClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();
            List<Topic> topics = snsClient.listTopics().getTopics();

            logger.info("all topics : " + topics.isEmpty());
            for(Topic topic: topics)
            {
                if(topic.getTopicArn().endsWith("SNSTopicResetPassword")){
                    logger.info("inside topic : " + topic.toString());
                    PublishRequest req = new PublishRequest(topic.getTopicArn(),user.getEmail());
                    snsClient.publish(req);
                    logger.info("published topic : " + req.toString());
                    break;
                }
            }
            jsonObject.addProperty("message","Successful");
            jsonObject.addProperty("Success","Password reset instruction sent.");
        }
        else{
            jsonObject.addProperty("message","User not found");
        }
        return ResponseEntity.status(201).body(jsonObject.toString());
    }
}
