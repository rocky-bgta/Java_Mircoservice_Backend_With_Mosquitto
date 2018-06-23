/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 08-Feb-18
 * Time: 12:56 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.broker.client;

import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.constant.BrokerMessageTopic;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PublisherForSecurity extends Core {
    private static final Logger log = LoggerFactory.getLogger(PublisherForSecurity.class);
    private static final String securityRequestTopicTopic = BrokerMessageTopic.SECURITY_REQUEST_TOPIC;
    private static MqttClient mqttClientForUserVerification;
    static
    {
        try {
            mqttClientForUserVerification = MqttUtils.getMqttClient();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private MqttMessage mqttMessage;


    public void publishedMessage(Object requestMessage) throws MqttException {
        String jsonString;
        try {
            this.mqttMessage = MqttUtils.getMqttDefaultMessage();
            if (mqttClientForUserVerification.isConnected()) {
                jsonString = Core.jsonMapper.writeValueAsString(requestMessage);
                this.mqttMessage.setPayload(jsonString.getBytes());

                mqttClientForUserVerification.publish(securityRequestTopicTopic, this.mqttMessage);

            } else
                throw new Exception("Broker Connection Failed");

            log.info("Message published from PublisherForSecurity Worker End");

        } catch (Exception e) {
            log.error("Exception from PublisherForSecurity Worker Publisher");
            e.printStackTrace();
        }finally {
            this.mqttMessage = null;
        }
    }
}