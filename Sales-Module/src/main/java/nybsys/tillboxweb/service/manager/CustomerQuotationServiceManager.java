/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 3/19/2018
 * Time: 11:109 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.bll.manager.CustomerQuotationBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreConstant.CurrencyConstant;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.models.CustomerQuotationModel;
import nybsys.tillboxweb.models.CustomerModel;
import nybsys.tillboxweb.models.VMCustomerQuotationList;
import nybsys.tillboxweb.models.VMCustomerQuotationModel;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomerQuotationServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(CustomerQuotationServiceManager.class);
    @Autowired
    private CustomerQuotationBllManager customerQuotationBllManager = new CustomerQuotationBllManager();


    public ResponseMessage save(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMCustomerQuotationModel vmCustomerQuotation = new VMCustomerQuotationModel();
        CustomerModel supplierModel = new CustomerModel();
        CurrencyModel currencyModel;

        try {
            vmCustomerQuotation = Core.getRequestObject(requestMessage, VMCustomerQuotationModel.class);
            
            /*Set<ConstraintViolation<AddressTypeModel>> violations = this.validator.validate(supplierAddressTypeModel);
            if (violations.size() > 0) {
                responseMessage = this.buildResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/
            currencyModel = this.getBaseCurrency();
            if (currencyModel == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                Core.clientMessage.get().userMessage = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return responseMessage;
            } else {
                currencyModel.setExchangeRate(1.00);
            }

            vmCustomerQuotation.customerQuotationModel.setBaseCurrencyID(currencyModel.getCurrencyID());
            vmCustomerQuotation.customerQuotationModel.setEntryCurrencyID(currencyModel.getCurrencyID());
            vmCustomerQuotation.customerQuotationModel.setExchangeRate(currencyModel.getExchangeRate());
            vmCustomerQuotation.customerQuotationModel.setBaseCurrencyAmount(vmCustomerQuotation.customerQuotationModel.getTotalAmount() * currencyModel.getExchangeRate());
            vmCustomerQuotation.customerQuotationModel.setExchangeRate(1.00);


            vmCustomerQuotation.customerQuotationModel.setBusinessID(requestMessage.businessID);
            this.customerQuotationBllManager.saveCustomerQuotation(vmCustomerQuotation);
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_QUOTATION_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_QUOTATION_SAVE_SUCCESSFULLY;
                this.commit();
            }
            responseMessage.responseObj = vmCustomerQuotation;

        } catch (Exception ex) {
            log.error("CustomerQuotationServiceManager -> save got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerQuotationModel customerQuotationModel = new CustomerQuotationModel();

        try {
            customerQuotationModel = Core.getRequestObject(requestMessage, CustomerQuotationModel.class);
            List<VMCustomerQuotationModel> lstVMCustomerQuotationModel = new ArrayList<>();
            lstVMCustomerQuotationModel = this.customerQuotationBllManager.searchVMCustomerQuotationModel(customerQuotationModel);
            responseMessage.responseObj = lstVMCustomerQuotationModel;

            if (lstVMCustomerQuotationModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_CUSTOMER_QUOTATION_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("CustomerQuotationServiceManager -> search got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getByID(RequestMessage requestMessage) {

        ResponseMessage responseMessage = new ResponseMessage();
        VMCustomerQuotationModel vmCustomerQuotationModel = new VMCustomerQuotationModel();
        CustomerQuotationModel customerQuotationModel = new CustomerQuotationModel();
        try {

            customerQuotationModel = Core.getRequestObject(requestMessage, CustomerQuotationModel.class);

            CustomerQuotationModel whereCondition = new CustomerQuotationModel();
            whereCondition.setCustomerQuotationID(customerQuotationModel.getCustomerQuotationID());

            vmCustomerQuotationModel = this.customerQuotationBllManager.getVMCustomerQuotationModel(whereCondition);
            responseMessage.responseObj = vmCustomerQuotationModel;
            if (vmCustomerQuotationModel != null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_CUSTOMER_QUOTATION_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("CustomerQuotationServiceManager -> getByID got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage delete(RequestMessage requestMessage) {

        ResponseMessage responseMessage = new ResponseMessage();
        CustomerQuotationModel customerQuotationModel = new CustomerQuotationModel();
        try {
            customerQuotationModel = Core.getRequestObject(requestMessage, CustomerQuotationModel.class);
            customerQuotationModel = this.customerQuotationBllManager.deleteCustomerQuotation(customerQuotationModel);

            responseMessage.responseObj = customerQuotationModel;
            if (customerQuotationModel != null && customerQuotationModel.getCustomerQuotationID() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_QUOTATION_DELETE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage != null) {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                } else {
                    responseMessage.message = MessageConstant.CUSTOMER_QUOTATION_DELETE_FAILED;
                }
                this.rollBack();
            }

        } catch (Exception ex) {
            log.error("CustomerQuotationServiceManager -> delete exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getCustomerQuotationByID(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerQuotationModel customerQuotationModel = new CustomerQuotationModel();

        try {
            customerQuotationModel = Core.getRequestObject(requestMessage, CustomerQuotationModel.class);
            VMCustomerQuotationModel vmCustomerQuotationModel = new VMCustomerQuotationModel();
            vmCustomerQuotationModel = this.customerQuotationBllManager.getVMCustomerQuotationModel(customerQuotationModel);
            responseMessage.responseObj = vmCustomerQuotationModel;

        } catch (Exception ex) {
            log.error("CustomerQuotationServiceManager -> search got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getCustomerQuotationList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerQuotationModel customerQuotationModel = new CustomerQuotationModel();

        try {
            customerQuotationModel = Core.getRequestObject(requestMessage, CustomerQuotationModel.class);
            List<VMCustomerQuotationList> lstVMCustomerQuotationList = new ArrayList<>();
            customerQuotationModel.setBusinessID(requestMessage.businessID);
            lstVMCustomerQuotationList = this.customerQuotationBllManager.getCustomerQuotationList(customerQuotationModel);
            responseMessage.responseObj = lstVMCustomerQuotationList;

            if (lstVMCustomerQuotationList.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> search purchase order vm got exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public CurrencyModel getBaseCurrency() {
        MqttClient mqttClient;
        ResponseMessage responseMessage = new ResponseMessage();
        CurrencyModel currencyModel = new CurrencyModel();
        RequestMessage reqMessForWorker;
        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);

            CallBack callBack;
            reqMessForWorker = Core.getDefaultWorkerRequestMessage();

            String pubTopic = WorkerSubscriptionConstants.WORKER_USER_REGISTRATION_MODULE_TOPIC;
            reqMessForWorker.brokerMessage.serviceName = "api/currency/getBaseCurrency";
            reqMessForWorker.token = Core.requestToken.get();

            SubscriberForWorker subForWorker = new SubscriberForWorker(reqMessForWorker.brokerMessage.messageId, this.barrier);
            mqttClient = subForWorker.subscribe();
            callBack = subForWorker.getCallBack();
            PublisherForWorker pubForWorker = new PublisherForWorker(pubTopic, mqttClient);
            pubForWorker.publishedMessageToWorker(reqMessForWorker);

            synchronized (lockObject) {
                long startTime = System.nanoTime();
                lockObject.wait(allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {
                    responseMessage = callBack.getResponseMessage();
                    if (responseMessage.responseObj != null) {
                        currencyModel = Core.jsonMapper.convertValue(responseMessage.responseObj, CurrencyModel.class);
                    } else {
                        currencyModel = null;
                    }
                } else {
                    //timeout
                    //TODO Implement role back logic
                    currencyModel = null;
                }
            }
            this.closeBrokerClient(mqttClient, reqMessForWorker.brokerMessage.messageId);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("CustomerQuotationService -> inter module communication getBaseCurrencyAndExchangeRate got exception");
        }
        return currencyModel;
    }

}
