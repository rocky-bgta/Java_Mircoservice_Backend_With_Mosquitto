package nybsys.tillboxweb;


import nybsys.tillboxweb.MessageModel.*;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.broker.client.*;
import nybsys.tillboxweb.constant.BrokerConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class BaseController extends Core {
    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    public abstract ResponseMessage getResponseMessage(String serviceName, RequestMessage requestMessage);


    protected abstract void executeServiceManager(String serviceName, RequestMessage requestMessage);

    private static CallBackForSecurity callBackForSecurity;// = new CallBackForSecurity();

    //private static final PublisherForSecurity publisherForSecurity = new PublisherForSecurity();
    private static final String securityResponseTopic = "user_verification";

    private static MqttClient mqttClientForUserVerification;

    static {
        try {
            mqttClientForUserVerification = MqttUtils.getMqttClient();
            callBackForSecurity = new CallBackForSecurity();
            mqttClientForUserVerification.setCallback(callBackForSecurity);
            mqttClientForUserVerification.subscribe(securityResponseTopic, BrokerConstant.oneQoS);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    protected ResponseMessage responseMessage;
    protected SecurityResMessage securityResMessage;
    protected String dataBaseName;
    protected String token;

    protected void setDefaultBusinessValue(RequestMessage requestMessage) {

        ClientMessage clientMessage = new ClientMessage();

        Core.requestToken.remove();
        Core.requestToken.set(requestMessage.token);

        Core.messageId.remove();
        Core.messageId.set(requestMessage.brokerMessage.messageId);

        Core.userId.remove();
        Core.userId.set(requestMessage.userID);

        Core.businessId.remove();
        Core.businessId.set(requestMessage.businessID);

        Core.clientMessage.remove();
        Core.clientMessage.set(clientMessage);

        Core.baseCurrencyID.remove();
        Core.baseCurrencyID.set(requestMessage.baseCurrencyID);

        Core.entryCurrencyID.remove();
        Core.entryCurrencyID.set(requestMessage.entryCurrencyID);

        Core.exchangeRate.remove();
        Core.exchangeRate.set(requestMessage.exchangeRate);

    }


    public SecurityReqMessage getDefaultSecurityMessage() {
        SecurityReqMessage securityReqMessage = new SecurityReqMessage();
        securityReqMessage.messageId = TillBoxUtils.getUUID();
        return securityReqMessage;
    }

    protected SecurityResMessage checkSecurity(RequestMessage requestMessage) {
        //String responseMessageId,publishedMessageId;

        SecurityResMessage securityResMessage=null;
        SecurityReqMessage securityReqMessage = this.getDefaultSecurityMessage();
        securityReqMessage.token = requestMessage.token;
        securityReqMessage.serviceUrl = requestMessage.brokerMessage.serviceName;

        // ================ Inter module communication =======================
        MqttClient mqttClient;
        CallBack callBackForIMC;
        ResponseMessage responseMessageFromSecurity;
        RequestMessage requestMessageForSecurity;
        boolean workCompleteWithInAllowTime;
        Object lock = new Object();
        String pubTopic = WorkerSubscriptionConstants.WORKER_SECURITY_MODULE_TOPIC;


        try {

            this.barrier = TillBoxUtils.getBarrier(1, lock);
            requestMessageForSecurity = Core.getDefaultWorkerRequestMessage();
            requestMessageForSecurity.brokerMessage.serviceName = "api/security/authorization";
            requestMessageForSecurity.requestObj = securityReqMessage;
            SubscriberForWorker subForWorker = new SubscriberForWorker(requestMessageForSecurity.brokerMessage.messageId, this.barrier);
            mqttClient = subForWorker.subscribe();
            callBackForIMC = subForWorker.getCallBack();
            PublisherForWorker pubForWorkerGetUserList = new PublisherForWorker(pubTopic, mqttClient);
            pubForWorkerGetUserList.publishedMessageToWorker(requestMessageForSecurity);


            synchronized (lock) {
                long startTime = System.nanoTime();
                lock.wait(this.allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {

                    responseMessageFromSecurity = callBackForIMC.getResponseMessage();
                    securityResMessage = Core.getResponseObject(responseMessageFromSecurity, SecurityResMessage.class);
                    //securityResMessage = (SecurityResMessage) responseMessageFromSecurity.responseObj;

                } else {
                    log.info("Response time out");
                }
            }

            this.closeBrokerClient(mqttClient, requestMessageForSecurity.brokerMessage.messageId);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        /*//securityReqMessage.messageId = requestMessage.brokerMessage.messageId;

        publishedMessageId = securityReqMessage.messageId;
        lock = callBackForSecurity.getLock();
        Boolean foundResponse=false;
        //boolean workCompleteWithInAllowTime = false;
        try {
            do{
                synchronized (lock) {
                    publisherForSecurity.publishedMessage(securityReqMessage);
                    long startTime = System.nanoTime();
                    lock.wait(12000);
                    workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime,10000);
                    //If response don't get with in allowed time then discarded
                    if(!workCompleteWithInAllowTime)
                        break;

                    for (Map.Entry<String, SecurityResMessage> entry : Core.securityResponseCollection.entrySet()) {
                        responseMessageId = entry.getKey().toString();
                        securityResMessage = entry.getValue();
                        if (publishedMessageId.equals(responseMessageId)) {
                            foundResponse=true;
                            securityResMessage = Core.securityResponseCollection.get(responseMessageId);
                            break;
                        }
                    }
                }
            }while (!foundResponse);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error while security check: " + ex.getMessage());
        }finally {
            Core.securityResponseCollection.remove(publishedMessageId);
            System.out.println("Response collection size: "+Core.securityResponseCollection.size());
        }
*/
        return securityResMessage;
    }

    protected void setSecurityForApi(RequestMessage requestMessage, SecurityResMessage securityResMessage) {
        this.dataBaseName = securityResMessage.businessDBName;
        Core.userDataBase.set(this.dataBaseName);

        Core.userDataBase.set(this.dataBaseName);
        this.token = securityResMessage.token;

        //Core.businessId.set(securityResMessage.businessID);
        Core.userId.set(requestMessage.userID);

        if (securityResMessage.isDefaultDB) {
            this.setDefaultDateBase();
        } else {
            this.selectDataBase(this.dataBaseName);
        }
    }

    protected void checkSecurityAndExecuteService(String serviceName, RequestMessage requestMessage) {
        this.securityResMessage = this.checkSecurity(requestMessage);
        this.responseMessage = this.buildDefaultResponseMessage();

        try {
            if (this.securityResMessage != null && this.securityResMessage.isPermitted) {

                this.setSecurityForApi(requestMessage, this.securityResMessage);

                this.setDefaultBusinessValue(requestMessage);


                //Update request message after security pass
                requestMessage.businessID = this.securityResMessage.businessID;
                requestMessage.userID = this.securityResMessage.userID;
                requestMessage.entryCurrencyID = this.securityResMessage.currentCurrencyID;
                //==========================================

                this.responseMessage.token = this.token;
                this.executeServiceManager(serviceName, requestMessage);
                this.responseMessage.businessID = requestMessage.businessID;

                if(!this.securityResMessage.isDefaultDB) {
                    //this.closeDBSession();
                }



                //give error message on development mode
                this.responseMessage.errorMessage = Core.clientMessage.get().message;
                //give only user message to message field for inter module communication
                if (requestMessage.brokerMessage.requestFrom == TillBoxAppEnum.BrokerRequestType.WORKER.get()) {
                    this.responseMessage.message = Core.clientMessage.get().userMessage;
                }
            } else {
                this.responseMessage.responseCode = TillBoxAppConstant.UNAUTHORIZED_CODE;
                this.responseMessage.message = TillBoxAppConstant.UNAUTHORIZED_USER;
                this.responseMessage.responseObj = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected void closeDBSession() {
        SessionFactory sessionFactory;
        sessionFactory = Core.sessionFactoryThreadLocal.get();
        if (sessionFactory != null) {
            sessionFactory.close();
            log.warn("Closed Database Session Factory");
        }
    }
}
