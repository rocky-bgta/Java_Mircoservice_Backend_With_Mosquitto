/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 16-Jan-18
 * Time: 12:05 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */
package nybsys.tillboxweb.broker.client;

import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublisherForWorker extends Core {
    private static final Logger log = LoggerFactory.getLogger(PublisherForWorker.class);

    private String publishedTopic;
    private static MqttClient mqttClient;
    private MqttMessage mqttMessage;

    static {
        try {
            mqttClient = MqttUtils.getMqttClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public PublisherForWorker(){}

    public PublisherForWorker(String publishedTopic, MqttClient mqttClient) {
        this.publishedTopic = publishedTopic;
        //this.mqttClient = mqttClient;
    }

    public void publishedMessageToWorker(RequestMessage requestMessage) throws MqttException {
        String jsonString;
        try {
            this.mqttMessage = MqttUtils.getMqttDefaultMessage();

            if (mqttClient.isConnected()) {
                jsonString = Core.jsonMapper.writeValueAsString(requestMessage);
                this.mqttMessage.setPayload(jsonString.getBytes());
                mqttClient.publish(publishedTopic, this.mqttMessage);
            } else
                throw new Exception("Connected to Message broker Failed from PublisherForWorker");
            log.info("Message published from Worker End");

        } catch (Exception e) {
            log.error("Exception from Worker Publisher");
            e.printStackTrace();
        }
    }

    public void setPublishedTopic(String publishedTopic) {
        this.publishedTopic = publishedTopic;
    }
}
