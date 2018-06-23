/*
*
 * Created By: Md. Nazmus Salahin
 * Created Date: 5/12/2018
 * Time: 1:11 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.


*/


package nybsys.tillboxweb;

import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.broker.client.Publisher;

public class ServiceExecutorWorkerThread implements Runnable {

    private String publishedTopic;
    private String incomingBrokerMessage;
    private RequestMessage requestMessage;
    //private ResponseMessage responseMessage;

    private BaseController baseController;
    private String serviceName;
    private String messageId;

    public ServiceExecutorWorkerThread(String publishedTopic,
                                       RequestMessage requestMessage,
                                       BaseController baseController){
        this.publishedTopic=publishedTopic;
        this.requestMessage = requestMessage;
        this.baseController = baseController;
    }


    @Override
    public void run() {
        ResponseMessage responseMessage=null;
        try{
            Publisher publisher = new Publisher(publishedTopic);
            this.requestMessage = Core.jsonMapper.readValue(incomingBrokerMessage,RequestMessage.class);

            this.serviceName = this.requestMessage.brokerMessage.serviceName;
            this.messageId = this.requestMessage.brokerMessage.messageId;
            if(this.baseController!=null) {
                responseMessage = this.baseController.getResponseMessage(this.serviceName, this.requestMessage);
                responseMessage.brokerMessageId = this.messageId;

                if(this.requestMessage.brokerMessage.requestFrom ==
                        TillBoxAppEnum.BrokerRequestType.API_CONTROLLER.get()) {
                    publisher.publishedMessageToBroker(responseMessage);

                }else if(this.requestMessage.brokerMessage.requestFrom ==
                        TillBoxAppEnum.BrokerRequestType.WORKER.get()){
                    publisher.publishedMessageToBroker(responseMessage,this.messageId);
                }

            }else {
                throw new Exception("Router Implementation not provided");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            this.baseController=null;
            //java.lang.Runtime.getRuntime().freeMemory();
            java.lang.System.gc();
        }
    }

    public void setIncomingBrokerMessage(String incomingBrokerMessage) {
        this.incomingBrokerMessage = incomingBrokerMessage;
    }

    @Override
    public void finalize() {
        System.out.println("Garbage Called");
    }
}
