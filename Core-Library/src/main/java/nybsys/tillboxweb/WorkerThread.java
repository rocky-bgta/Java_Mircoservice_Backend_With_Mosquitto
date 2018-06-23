/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 22-Dec-17
 * Time: 11:14 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */
package nybsys.tillboxweb;


import nybsys.tillboxweb.MessageModel.RequestMessage;

public class WorkerThread extends Core implements Runnable {

    private String publishedTopic;
    private String incomingBrokerMessage;
    private RequestMessage requestMessage;
    //private ResponseMessage responseMessage;

    private BaseController baseController;
    private String serviceName;
    private String messageId;

    public WorkerThread(String publishedTopic,Object baseController){
        this.publishedTopic = publishedTopic;
        this.baseController =(BaseController) baseController;
    }


    @Override
    public void run() {
        try {
            this.getRequestMessage();

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            java.lang.Runtime.getRuntime().freeMemory();
            java.lang.Runtime.getRuntime().gc();
        }
    }

    // this is final method
    public void getRequestMessage() {
        Thread thread;
        ServiceExecutorWorkerThread serviceExecutorWorkerThread;
        serviceExecutorWorkerThread = new ServiceExecutorWorkerThread(this.publishedTopic,this.requestMessage,this.baseController);
        serviceExecutorWorkerThread.setIncomingBrokerMessage(incomingBrokerMessage);
        thread = new Thread(serviceExecutorWorkerThread);
        thread.start();



       /* ResponseMessage responseMessage=null;
        try{
            Publisher publisher = new Publisher(publishedTopic);
            this.requestMessage = Core.jsonMapper.readValue(incomingBrokerMessage,RequestMessage.class);
            this.serviceName = this.requestMessage.brokerMessage.serviceName;
            this.messageId = this.requestMessage.brokerMessage.messageId;
            if(this.baseController!=null) {
                responseMessage = this.baseController.getResponseMessage(this.serviceName, this.requestMessage);
                //System.out.println("WorkerThread Response: "+responseMessage.responseObj);
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
            java.lang.System.gc();
        }
*/

        //return responseMessage;
    }

    public void setIncomingBrokerMessage(String incomingBrokerMessage) {
        this.incomingBrokerMessage = incomingBrokerMessage;
    }

    @Override
    public void finalize() {
        System.out.println("Garbage Called");
    }
}
