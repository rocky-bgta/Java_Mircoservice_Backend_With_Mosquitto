/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 12/03/2018
 * Time: 03:59
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
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.bll.manager.CustomerAddressBllManger;
import nybsys.tillboxweb.bll.manager.CustomerBllManager;
import nybsys.tillboxweb.bll.manager.CustomerCategoryBllManager;
import nybsys.tillboxweb.bll.manager.CustomerContactBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreEnum.*;
import nybsys.tillboxweb.coreModels.DebitCreditBalanceModel;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.coreModels.SupplierCategoryModel;
import nybsys.tillboxweb.models.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomerServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceManager.class);
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    @Autowired
    private CustomerBllManager customerBllManager = new CustomerBllManager();
    @Autowired
    private CustomerAddressBllManger customerAddressBllManger = new CustomerAddressBllManger();
    @Autowired
    private CustomerContactBllManager customerContactBllManager = new CustomerContactBllManager();

    public ResponseMessage searchCustomer(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerModel customerModel = new CustomerModel();
        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        try {
            customerModel = Core.getRequestObject(requestMessage, CustomerModel.class);

            lstCustomerModel = this.customerBllManager.searchCustomer(customerModel);
            responseMessage.responseObj = lstCustomerModel;
            if (lstCustomerModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("CustomerServiceManager -> searchCustomer got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage saveCustomerVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMCustomerModel vmCustomerModel;
        CustomerModel customerModel;
        nybsys.tillboxweb.coreModels.OpeningBalanceModel openingBalanceModel;
        List<CustomerAddressModel> lstCustomerAddressModel;
        List<CustomerContactModel> lstCustomerContactModel;
        VMRememberNoteModel vmRememberNoteModel;
        VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel;
        ReportingLayoutModel reportingLayoutModel;

        try {
            vmCustomerModel = Core.getRequestObject(requestMessage, VMCustomerModel.class);

            customerModel = vmCustomerModel.customerModel;
            customerModel.setBusinessID(requestMessage.businessID);
            openingBalanceModel = vmCustomerModel.openingBalanceModel;
            lstCustomerAddressModel = vmCustomerModel.lstCustomerAddressModel;
            lstCustomerContactModel = vmCustomerModel.lstCustomerContactModel;


            //(1)
            customerModel = this.customerBllManager.saveOrUpdate(customerModel);
            vmCustomerModel.customerModel.setCustomerID(customerModel.getCustomerID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(2)
            for (CustomerAddressModel customerAddressModel : lstCustomerAddressModel) {
                customerAddressModel.setCustomerID(customerModel.getCustomerID());
                this.customerAddressBllManger.saveOrUpdate(customerAddressModel);
                if (Core.clientMessage.get().messageCode != null) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    if (Core.clientMessage.get().userMessage == null) {
                        responseMessage.message = MessageConstant.CUSTOMER_SAVE_FAILED;
                    } else {
                        responseMessage.message = Core.clientMessage.get().userMessage;
                    }
                    this.rollBack();
                    return responseMessage;
                }
            }
            //(3)
            for (CustomerContactModel customerContactModel : lstCustomerContactModel) {
                customerContactModel.setCustomerID(customerModel.getCustomerID());
                this.customerContactBllManager.saveOrUpdate(customerContactModel);
                if (Core.clientMessage.get().messageCode != null) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    if (Core.clientMessage.get().userMessage == null) {
                        responseMessage.message = MessageConstant.CUSTOMER_SAVE_FAILED;
                    } else {
                        responseMessage.message = Core.clientMessage.get().userMessage;
                    }
                    this.rollBack();
                    return responseMessage;
                }
            }

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            } else {
                this.checkInterCom(requestMessage, vmCustomerModel);
            }

            this.commit();
            responseMessage.message = MessageConstant.CUSTOMER_SAVE_SUCCESSFULLY;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
        } catch (
                Exception ex)

        {
            log.error("CustomerServiceManager -> saveCustomerVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage checkInterCom(RequestMessage requestMessage, VMCustomerModel vmCustomerModel) {

        MqttClient mqttClientSaveOpeningBalance,  mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail;
        CallBack callBackSaveJournal, callBackSaveRememberNote, callBackSaveUserDefineSettingDetail;

        //CallBack callBackDeleteJournal = null;


        ResponseMessage responseMessage;// = new ResponseMessage();
        ResponseMessage responseMessageSaveJournal;


        RequestMessage reqJournalSaveMessage, reqRememberNoteSaveMessage, reqUserDefineSettingDetailSaveMessage, reqMessForWorkerOpeningBalanceSave;

        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();

            reqJournalSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqRememberNoteSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqUserDefineSettingDetailSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqMessForWorkerOpeningBalanceSave = Core.getDefaultWorkerRequestMessage();

            reqJournalSaveMessage.token = requestMessage.token;
            reqRememberNoteSaveMessage.token = requestMessage.token;
            reqUserDefineSettingDetailSaveMessage.token = requestMessage.token;
            reqMessForWorkerOpeningBalanceSave.token = requestMessage.token;

            String accountingTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            String commonTopic = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(3, lockObject);

            nybsys.tillboxweb.coreModels.OpeningBalanceModel openingBalanceModel = new nybsys.tillboxweb.coreModels.OpeningBalanceModel();
            openingBalanceModel.setAmount(vmCustomerModel.customerModel.getOpeningBalanceAmount());
            openingBalanceModel.setAccountID(DefaultCOA.TradeCreditors.get());
            openingBalanceModel.setBusinessID(requestMessage.businessID);
            openingBalanceModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());
            openingBalanceModel.setReferenceType(ReferenceType.CustomerOpeningBalance.get());

            reqMessForWorkerOpeningBalanceSave.requestObj = openingBalanceModel;
            reqMessForWorkerOpeningBalanceSave.token = Core.requestToken.get();
            reqMessForWorkerOpeningBalanceSave.brokerMessage.serviceName = "api/openingBalance/save";

            SubscriberForWorker subWorkerForOpeningBalance = new SubscriberForWorker(reqJournalSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveOpeningBalance = subWorkerForOpeningBalance.subscribe();
            callBackSaveJournal = subWorkerForOpeningBalance.getCallBack();
            PublisherForWorker pubForWorkerGetOpeningBalance = new PublisherForWorker(accountingTopic, mqttClientSaveOpeningBalance);
            pubForWorkerGetOpeningBalance.publishedMessageToWorker(reqMessForWorkerOpeningBalanceSave);


            for (nybsys.tillboxweb.coreModels.RememberNoteModel rememberNoteModel : vmCustomerModel.lstRememberNoteModels) {
                rememberNoteModel.setReferenceType(RememberNoteReferenceType.Customer.get());
                rememberNoteModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());
            }

            nybsys.tillboxweb.coreModels.VMRememberNoteModel vmRememberNoteModel = new nybsys.tillboxweb.coreModels.VMRememberNoteModel();
            vmRememberNoteModel.lstRememberNoteModel = vmCustomerModel.lstRememberNoteModels;
            reqRememberNoteSaveMessage.requestObj = vmRememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/save";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);

            nybsys.tillboxweb.coreModels.VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel = new nybsys.tillboxweb.coreModels.VMUserDetailSettingDetailModel();


            for (nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel : vmCustomerModel.lstUserDefineSettingDetailModels) {
                userDefineSettingDetailModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());
                userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Customer.get());
            }


            vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = vmCustomerModel.lstUserDefineSettingDetailModels;
            reqUserDefineSettingDetailSaveMessage.requestObj = vmUserDetailSettingDetailModel;
            reqUserDefineSettingDetailSaveMessage.token = Core.requestToken.get();
            reqUserDefineSettingDetailSaveMessage.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/save";

            SubscriberForWorker subForUserDefineSettingDetail = new SubscriberForWorker(reqUserDefineSettingDetailSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveUserDefineSettingDetail = subForUserDefineSettingDetail.subscribe();
            callBackSaveUserDefineSettingDetail = subForUserDefineSettingDetail.getCallBack();
            PublisherForWorker pubForWorkerUserDefineSettingDetailList = new PublisherForWorker(commonTopic, mqttClientSaveUserDefineSettingDetail);
            pubForWorkerUserDefineSettingDetailList.publishedMessageToWorker(reqUserDefineSettingDetailSaveMessage);


            synchronized (lockObject) {
                responseMessage = Core.buildDefaultResponseMessage();
                long startTime = System.nanoTime();
                lockObject.wait(this.allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {
                    responseMessageSaveJournal = callBackSaveJournal.getResponseMessage();
                    callBackSaveRememberNote.getResponseMessage();
                    callBackSaveUserDefineSettingDetail.getResponseMessage();
                    responseMessage.message = "Inter module communication successful";
                    responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

                } else {
                    //timeout
                    log.info("Response time out");
                    log.info("RollBack checkInterCom Operation");
                    responseMessage.message = "Inter module communication Failed";
                    responseMessage.responseCode = TillBoxAppConstant.UN_PROCESSABLE_REQUEST;
                }
            }


            this.closeBrokerClient(mqttClientSaveOpeningBalance, reqJournalSaveMessage.brokerMessage.messageId);
            // this.closeBrokerClient(mqttClientSaveInventoryTransaction, reqObjInventoryTransactionMessage.brokerMessage.messageId);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from checkInterCom Module communication ProductServiceManager");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
        }
        return responseMessage;
    }

    public ResponseMessage getAll(RequestMessage requestMessage) {
        ResponseMessage responseMessage = this.getDefaultResponseMessage();
        CustomerModel whereConditionCustomerModel;
        List<CustomerModel> customerModelList;

        try {
            whereConditionCustomerModel = new CustomerModel();
            whereConditionCustomerModel.setBusinessID(requestMessage.businessID);

            customerModelList = this.customerBllManager.getAllByConditionWithActive(whereConditionCustomerModel);
            if (customerModelList != null && customerModelList.size() > 0) {
                responseMessage.responseObj = customerModelList;
                responseMessage.message = "Find all customer successful";
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseObj = null;
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = "Failed to find customers";
            }
        } catch (Exception e) {
            log.error("CustomerServiceManager -> getAll got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(e);
        }
        return responseMessage;
    }

    public ResponseMessage searchCustomerVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerModel customerModel;
        List<CustomerModel> lstCustomerModel;
        List<VMCustomerModel> lstVmCustomerModel = new ArrayList<>();
        try {
            customerModel = Core.getRequestObject(requestMessage, CustomerModel.class);

            lstCustomerModel = this.customerBllManager.searchCustomer(customerModel);
            if (lstCustomerModel.size() > 0) {
                for (CustomerModel customerModelObj : lstCustomerModel) {
                    VMCustomerModel vmCustomerModel;
                    vmCustomerModel = singleCustomerVMSearch(customerModelObj, requestMessage.businessID);
                    lstVmCustomerModel.add(vmCustomerModel);
                }
            }
            responseMessage.responseObj = lstVmCustomerModel;
            if (lstVmCustomerModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("CustomerServiceManager -> searchCustomerVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public VMCustomerModel singleCustomerVMSearch(CustomerModel customerModel, Integer businessID) throws Exception {
        VMCustomerModel vmCustomerModel = new VMCustomerModel();
        ResponseMessage responseMessageFromInterModule = new ResponseMessage();
        try {
            //(1)
            vmCustomerModel.customerModel = customerModel;
            //(2)
            CustomerAddressModel addressWhereCondition = new CustomerAddressModel();
            addressWhereCondition.setCustomerID(customerModel.getCustomerID());
            addressWhereCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            vmCustomerModel.lstCustomerAddressModel = this.customerAddressBllManger.searchCustomerAddress(addressWhereCondition);
            //(3)
            CustomerContactModel contactWhereCondition = new CustomerContactModel();
            contactWhereCondition.setCustomerID(customerModel.getCustomerID());
            contactWhereCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            vmCustomerModel.lstCustomerContactModel = this.customerContactBllManager.searchCustomerContact(contactWhereCondition);

            //inter module calls start
            RequestMessage reqMessForWorkerOpeningBalanceSearch = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerRememberNoteSearch = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerUserDefineSettingDetailSearch = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerReportingLayoutSearch = Core.getDefaultWorkerRequestMessage();

            ResponseMessage responseFromOpeningBalanceSearch = new ResponseMessage();
            ResponseMessage responseFromRememberNoteSearch = new ResponseMessage();
            ResponseMessage responseFromUserDefineSettingDetailSearch = new ResponseMessage();
            ResponseMessage responseFromReportingLayoutSearch = new ResponseMessage();

            MqttClient mqttClientOpeningBalanceSearch;
            MqttClient mqttClientRememberNoteSearch;
            MqttClient mqttClientUserDefineSettingDetailSearch;
            MqttClient mqttClientReportingLayoutSearch;

            CallBack callBackOpeningBalanceSearch;
            CallBack callBackRememberNoteSearch;
            CallBack callBackUserDefineSettingDetailSearch;
            CallBack callBackReportingLayoutSearch;

            String pubTopicAccounting = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            String pubTopicCommon = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            boolean workCompleteWithInAllowTime;
            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);//to do number

            //(a) making chunk request OpeningBalanceSearch
            nybsys.tillboxweb.coreModels.OpeningBalanceModel openingBalanceModel = new nybsys.tillboxweb.coreModels.OpeningBalanceModel();
            openingBalanceModel.setAccountID(DefaultCOA.TradeDebtors.get());
            openingBalanceModel.setReferenceID(customerModel.getCustomerID());
            openingBalanceModel.setReferenceType(ReferenceType.CustomerOpeningBalance.get());
            reqMessForWorkerOpeningBalanceSearch.brokerMessage.serviceName = "api/openingBalance/getByAccountID";
            reqMessForWorkerOpeningBalanceSearch.requestObj = openingBalanceModel;
            reqMessForWorkerOpeningBalanceSearch.token = Core.requestToken.get();

            SubscriberForWorker subForWorkerOpeningBalanceSearch = new SubscriberForWorker(reqMessForWorkerOpeningBalanceSearch.brokerMessage.messageId, this.barrier);
            mqttClientOpeningBalanceSearch = subForWorkerOpeningBalanceSearch.subscribe();
            callBackOpeningBalanceSearch = subForWorkerOpeningBalanceSearch.getCallBack();
            PublisherForWorker pubForWorkerOpeningBalanceSearch = new PublisherForWorker(pubTopicAccounting, mqttClientOpeningBalanceSearch);
            pubForWorkerOpeningBalanceSearch.publishedMessageToWorker(reqMessForWorkerOpeningBalanceSearch);

            //(b) making chunk request RememberNoteSearch
//            RememberNoteModel rememberNoteModel = new RememberNoteModel();
//            rememberNoteModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Customer.get());
//            rememberNoteModel.setReferenceID(customerModel.getCustomerID());
//            rememberNoteModel.setBusinessID(businessID);
//            reqMessForWorkerRememberNoteSearch.brokerMessage.serviceName = "api/commonModule/rememberNote/search";
//            reqMessForWorkerRememberNoteSearch.requestObj = rememberNoteModel;
//            reqMessForWorkerRememberNoteSearch.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerRememberNoteSearch = new SubscriberForWorker(reqMessForWorkerRememberNoteSearch.brokerMessage.messageId, this.barrier);
//            mqttClientRememberNoteSearch = subForWorkerRememberNoteSearch.subscribe();
//            callBackRememberNoteSearch = subForWorkerRememberNoteSearch.getCallBack();
//            PublisherForWorker pubForWorkerRememberNoteSearch = new PublisherForWorker(pubTopicCommon, mqttClientRememberNoteSearch);
//            pubForWorkerRememberNoteSearch.publishedMessageToWorker(reqMessForWorkerRememberNoteSearch);

            //(c) making chunk request UserDefineSettingDetailSearch
//            UserDefineSettingDetailModel userDefineSettingDetailModel = new UserDefineSettingDetailModel();
//            userDefineSettingDetailModel.setBusinessID(businessID);
//            userDefineSettingDetailModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Customer.get());
//            reqMessForWorkerUserDefineSettingDetailSearch.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/search";
//            reqMessForWorkerUserDefineSettingDetailSearch.requestObj = userDefineSettingDetailModel;
//            reqMessForWorkerUserDefineSettingDetailSearch.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerUserDefineSettingDetailSearch = new SubscriberForWorker(reqMessForWorkerUserDefineSettingDetailSearch.brokerMessage.messageId, this.barrier);
//            mqttClientUserDefineSettingDetailSearch = subForWorkerUserDefineSettingDetailSearch.subscribe();
//            callBackUserDefineSettingDetailSearch = subForWorkerUserDefineSettingDetailSearch.getCallBack();
//            PublisherForWorker pubForWorkerUserDefineSettingDetailSearch = new PublisherForWorker(pubTopicCommon, mqttClientUserDefineSettingDetailSearch);
//            pubForWorkerUserDefineSettingDetailSearch.publishedMessageToWorker(reqMessForWorkerUserDefineSettingDetailSearch);

            //(d) making chunk request ReportingLayoutSearch
//            ReportingLayoutModel reportingLayoutModel = new ReportingLayoutModel();
//            reportingLayoutModel.setBusinessID(businessID);
//            //------------type to do customer/supplier/else------------
//            reqMessForWorkerReportingLayoutSearch.brokerMessage.serviceName = "api/commonModule/reportingLayout/search";
//            reqMessForWorkerReportingLayoutSearch.requestObj = reportingLayoutModel;
//            reqMessForWorkerReportingLayoutSearch.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerReportingLayoutSearch = new SubscriberForWorker(reqMessForWorkerReportingLayoutSearch.brokerMessage.messageId, this.barrier);
//            mqttClientReportingLayoutSearch = subForWorkerOpeningBalanceSearch.subscribe();
//            callBackReportingLayoutSearch = subForWorkerOpeningBalanceSearch.getCallBack();
//            PublisherForWorker pubForWorkerReportingLayoutSearch = new PublisherForWorker(pubTopicCommon, mqttClientReportingLayoutSearch);
//            pubForWorkerReportingLayoutSearch.publishedMessageToWorker(reqMessForWorkerReportingLayoutSearch);

            //make single receive point
            synchronized (lockObject) {
                long startTime = System.nanoTime();
                lockObject.wait(allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {
                    responseFromOpeningBalanceSearch = callBackOpeningBalanceSearch.getResponseMessage();
//                    responseFromRememberNoteSearch = callBackRememberNoteSearch.getResponseMessage();
//                    responseFromReportingLayoutSearch = callBackReportingLayoutSearch.getResponseMessage();
//                    responseFromUserDefineSettingDetailSearch = callBackUserDefineSettingDetailSearch.getResponseMessage();
                } else {
                    //timeout
                    this.rollBack();
                }
            }

            //close broker clint connections
            this.closeBrokerClient(mqttClientOpeningBalanceSearch, reqMessForWorkerOpeningBalanceSearch.brokerMessage.messageId);
//            this.closeBrokerClient(mqttClientRememberNoteSearch, reqMessForWorkerRememberNoteSearch.brokerMessage.messageId);
//            this.closeBrokerClient(mqttClientReportingLayoutSearch, reqMessForWorkerReportingLayoutSearch.brokerMessage.messageId);
            //           this.closeBrokerClient(mqttClientUserDefineSettingDetailSearch, reqMessForWorkerUserDefineSettingDetailSearch.brokerMessage.messageId);
            //inter module calls end

            //(4)
            if (responseFromOpeningBalanceSearch.responseObj != null) {
                vmCustomerModel.openingBalanceModel = Core.modelMapper.map(responseFromOpeningBalanceSearch.responseObj, nybsys.tillboxweb.coreModels.OpeningBalanceModel.class);
            }

            //(5)
//            vmCustomerModel.vmRememberNoteModel.lstRememberNoteModel = Core.convertResponseToList(responseFromRememberNoteSearch,rememberNoteModel);

            //(6)
//            vmCustomerModel.vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = Core.convertResponseToList(responseFromUserDefineSettingDetailSearch,userDefineSettingDetailModel);

            //(7)
//            List<ReportingLayoutModel> lstReportingLayoutModel = new ArrayList<>();
//            lstReportingLayoutModel = Core.convertResponseToList(responseFromReportingLayoutSearch,reportingLayoutModel);
//            if(lstReportingLayoutModel.size() > 0)
//            {
//                reportingLayoutModel = lstReportingLayoutModel.get(0);
//            }else
//            {
//                reportingLayoutModel = null;
//            }
//            vmCustomerModel.reportingLayoutModel = reportingLayoutModel;


        } catch (Exception ex) {
            log.error("CustomerServiceManager -> singleCustomerVMSearch got exception");
            this.WriteExceptionLog(ex);
            throw ex;
        }
        return vmCustomerModel;
    }


    public ResponseMessage deleteCustomerVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ResponseMessage responseFromInterModule = new ResponseMessage();
        CustomerModel customerModel = new CustomerModel();
        try {
            customerModel = Core.getRequestObject(requestMessage, CustomerModel.class);
            Integer businessID = requestMessage.businessID;

            //(1)
            //check journal data exists or not ;exclude opening balance;
            responseFromInterModule = getJournalExistsExcludeOpeningBalance(customerModel.getBusinessID(), customerModel.getCustomerID());
            if ((responseFromInterModule.responseCode != null && responseFromInterModule.responseCode != TillBoxAppConstant.SUCCESS_CODE) || responseFromInterModule.responseCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (responseFromInterModule.message == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
                } else {
                    responseMessage.message = responseFromInterModule.message;
                }
                return responseMessage;
            } else {
                Boolean existsFlag = (Boolean) responseFromInterModule.responseObj;
                if (existsFlag.booleanValue() == true) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = MessageConstant.CUSTOMER_JOURNAL_EXISTS;
                    return responseMessage;
                }
            }
            //(2)
            this.customerBllManager.deleteCustomerByID(customerModel.getCustomerID(), businessID);
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(3)
            this.customerAddressBllManger.deleteCustomerAddress(customerModel.getCustomerID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(4)
            this.customerContactBllManager.deleteCustomerContact(customerModel.getCustomerID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }

            //inter module calls start
            RequestMessage reqMessForWorkerOpeningBalanceDelete = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerRememberNoteDelete = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerUserDefineSettingDetailDelete = Core.getDefaultWorkerRequestMessage();
            RequestMessage reqMessForWorkerReportingLayoutDelete = Core.getDefaultWorkerRequestMessage();

            ResponseMessage responseFromOpeningBalanceDelete = new ResponseMessage();
            ResponseMessage responseFromRememberNoteDelete = new ResponseMessage();
            ResponseMessage responseFromUserDefineSettingDetailDelete = new ResponseMessage();
            ResponseMessage responseFromReportingLayoutDelete = new ResponseMessage();

            MqttClient mqttClientOpeningBalanceDelete;
            MqttClient mqttClientRememberNoteDelete;
            MqttClient mqttClientUserDefineSettingDetailDelete;
            MqttClient mqttClientReportingLayoutDelete;

            CallBack callBackOpeningBalanceDelete;
            CallBack callBackRememberNoteDelete;
            CallBack callBackUserDefineSettingDetailDelete;
            CallBack callBackReportingLayoutDelete;

            String pubTopicAccounting = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            String pubTopicCommon = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            boolean workCompleteWithInAllowTime;
            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);//to do number

            //(a) making chunk request OpeningBalanceDelete
            OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
            openingBalanceModel.setReferenceID(customerModel.getCustomerID());
            openingBalanceModel.setAccountID(DefaultCOA.TradeDebtors.get());
            openingBalanceModel.setReferenceType(ReferenceType.CustomerOpeningBalance.get());
            openingBalanceModel.setBusinessID(businessID);
            reqMessForWorkerOpeningBalanceDelete.brokerMessage.serviceName = "api/openingBalance/delete";
            reqMessForWorkerOpeningBalanceDelete.requestObj = openingBalanceModel;
            reqMessForWorkerOpeningBalanceDelete.token = Core.requestToken.get();

            SubscriberForWorker subForWorkerOpeningBalanceDelete = new SubscriberForWorker(reqMessForWorkerOpeningBalanceDelete.brokerMessage.messageId, this.barrier);
            mqttClientOpeningBalanceDelete = subForWorkerOpeningBalanceDelete.subscribe();
            callBackOpeningBalanceDelete = subForWorkerOpeningBalanceDelete.getCallBack();
            PublisherForWorker pubForWorkerOpeningBalanceDelete = new PublisherForWorker(pubTopicAccounting, mqttClientOpeningBalanceDelete);
            pubForWorkerOpeningBalanceDelete.publishedMessageToWorker(reqMessForWorkerOpeningBalanceDelete);

            //(b) making chunk request RememberNoteDelete
//            RememberNoteModel rememberNoteModel = new RememberNoteModel();
//            rememberNoteModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Customer.get());
//            rememberNoteModel.setReferenceID(customerModel.getCustomerID());
//            rememberNoteModel.setBusinessID(businessID);
//            reqMessForWorkerRememberNoteDelete.brokerMessage.serviceName = "api/commonModule/rememberNote/delete";
//            reqMessForWorkerRememberNoteDelete.requestObj = rememberNoteModel;
//            reqMessForWorkerRememberNoteDelete.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerRememberNoteDelete = new SubscriberForWorker(reqMessForWorkerRememberNoteDelete.brokerMessage.messageId, this.barrier);
//            mqttClientRememberNoteDelete = subForWorkerRememberNoteDelete.subscribe();
//            callBackRememberNoteDelete = subForWorkerRememberNoteDelete.getCallBack();
//            PublisherForWorker pubForWorkerRememberNoteDelete = new PublisherForWorker(pubTopicCommon, mqttClientRememberNoteDelete);
//            pubForWorkerRememberNoteDelete.publishedMessageToWorker(reqMessForWorkerRememberNoteDelete);

            //(c) making chunk request UserDefineSettingDetailDelete
//            UserDefineSettingDetailModel userDefineSettingDetailModel = new UserDefineSettingDetailModel();
//            userDefineSettingDetailModel.setBusinessID(businessID);
//            userDefineSettingDetailModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Customer.get());
//            reqMessForWorkerUserDefineSettingDetailDelete.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/delete";
//            reqMessForWorkerUserDefineSettingDetailDelete.requestObj = userDefineSettingDetailModel;
//            reqMessForWorkerUserDefineSettingDetailDelete.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerUserDefineSettingDetailDelete = new SubscriberForWorker(reqMessForWorkerUserDefineSettingDetailDelete.brokerMessage.messageId, this.barrier);
//            mqttClientUserDefineSettingDetailDelete = subForWorkerUserDefineSettingDetailDelete.subscribe();
//            callBackUserDefineSettingDetailDelete = subForWorkerUserDefineSettingDetailDelete.getCallBack();
//            PublisherForWorker pubForWorkerUserDefineSettingDetailDelete = new PublisherForWorker(pubTopicCommon, mqttClientUserDefineSettingDetailDelete);
//            pubForWorkerUserDefineSettingDetailDelete.publishedMessageToWorker(reqMessForWorkerUserDefineSettingDetailDelete);

            //(d) making chunk request ReportingLayoutDelete
//            ReportingLayoutModel reportingLayoutModel = new ReportingLayoutModel();
//            reportingLayoutModel.setBusinessID(businessID);
//            //------------type to do customer/supplier/else------------
//            reqMessForWorkerReportingLayoutDelete.brokerMessage.serviceName = "api/commonModule/reportingLayout/delete";
//            reqMessForWorkerReportingLayoutDelete.requestObj = reportingLayoutModel;
//            reqMessForWorkerReportingLayoutDelete.token = Core.requestToken.get();
//
//            SubscriberForWorker subForWorkerReportingLayoutDelete = new SubscriberForWorker(reqMessForWorkerReportingLayoutDelete.brokerMessage.messageId, this.barrier);
//            mqttClientReportingLayoutDelete = subForWorkerOpeningBalanceDelete.subscribe();
//            callBackReportingLayoutDelete = subForWorkerOpeningBalanceDelete.getCallBack();
//            PublisherForWorker pubForWorkerReportingLayoutDelete = new PublisherForWorker(pubTopicCommon, mqttClientReportingLayoutDelete);
//            pubForWorkerReportingLayoutDelete.publishedMessageToWorker(reqMessForWorkerReportingLayoutDelete);

            //make single receive point
            synchronized (lockObject) {
                long startTime = System.nanoTime();
                lockObject.wait(allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {
                    responseFromOpeningBalanceDelete = callBackOpeningBalanceDelete.getResponseMessage();
//                    responseFromRememberNoteDelete = callBackRememberNoteDelete.getResponseMessage();
//                    responseFromReportingLayoutDelete = callBackReportingLayoutDelete.getResponseMessage();
//                    responseFromUserDefineSettingDetailDelete = callBackUserDefineSettingDetailDelete.getResponseMessage();
                } else {
                    //timeout
                    this.rollBack();
                }
            }

            //close broker clint connections
            this.closeBrokerClient(mqttClientOpeningBalanceDelete, reqMessForWorkerOpeningBalanceDelete.brokerMessage.messageId);
//            this.closeBrokerClient(mqttClientRememberNoteDelete, reqMessForWorkerRememberNoteDelete.brokerMessage.messageId);
//            this.closeBrokerClient(mqttClientReportingLayoutDelete, reqMessForWorkerReportingLayoutDelete.brokerMessage.messageId);
//            this.closeBrokerClient(mqttClientUserDefineSettingDetailDelete, reqMessForWorkerUserDefineSettingDetailDelete.brokerMessage.messageId);
            //inter module calls end

            //(5)
            if (responseFromOpeningBalanceDelete.responseCode != null && responseFromOpeningBalanceDelete.responseCode != 200) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (responseFromOpeningBalanceDelete.message == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
                } else {
                    responseMessage.message = responseFromOpeningBalanceDelete.message;
                }
                this.rollBack();
                return responseMessage;
            }
            //(6)
//            if (responseFromRememberNoteDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromRememberNoteDelete.message == null) {
//                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromRememberNoteDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }
            //(7)
//            if (responseFromReportingLayoutDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromReportingLayoutDelete.message == null) {
//                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromReportingLayoutDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }
            //(8)
//            if (responseFromUserDefineSettingDetailDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromUserDefineSettingDetailDelete.message == null) {
//                    responseMessage.message = MessageConstant.CUSTOMER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromUserDefineSettingDetailDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }

            this.commit();
            responseMessage.responseObj = customerModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.message = MessageConstant.CUSTOMER_DELETE_SUCCESSFULLY;

        } catch (Exception ex) {
            log.error("CustomerServiceManager -> deleteCustomerVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    private ResponseMessage getJournalExistsExcludeOpeningBalance(Integer businessID, Integer partyID) {
        MqttClient mqttClient;
        ResponseMessage responseMessage = new ResponseMessage();
        RequestMessage reqMessForWorker;
        boolean workCompleteWithInAllowTime;
        JournalModel journalModel = new JournalModel();
        try {

            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);

            CallBack callBack;
            reqMessForWorker = Core.getDefaultWorkerRequestMessage();

            String pubTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            reqMessForWorker.brokerMessage.serviceName = "api/journal/dataExistsExcludeOpeningBalance";
            journalModel.setBusinessID(businessID);
            journalModel.setPartyType(PartyType.Customer.get());
            journalModel.setPartyID(partyID);
            journalModel.setReferenceType(ReferenceType.CustomerOpeningBalance.get());
            reqMessForWorker.requestObj = journalModel;
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
                } else {
                    //timeout
                    //TODO Implement role back logic
                }
            }
            this.closeBrokerClient(mqttClient, reqMessForWorker.brokerMessage.messageId);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("CustomerServiceManager -> inter module communication getJournalExistsExcludeOpeningBalance got exception");
        }
        return responseMessage;
    }

    public ResponseMessage searchCustomerVMList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerModel customerModel = new CustomerModel();
        List<VMCustomerList> lstVmSupplierModel = new ArrayList<>();
        try {
            customerModel = Core.getRequestObject(requestMessage, CustomerModel.class);
            customerModel.setBusinessID(requestMessage.businessID);
            lstVmSupplierModel = this.customerBllManager.getCustomerList(customerModel);
            if (lstVmSupplierModel.size() > 0) {
                responseMessage.responseObj = lstVmSupplierModel;
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            }

        } catch (Exception ex) {
            log.error("SupplierServiceManager -> searchSupplierVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public ResponseMessage getVMCustomer(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {

            VMCustomerModel vmCustomerModel = new VMCustomerModel();
            CustomerModel supplierModel = new CustomerModel();
            CustomerModel searchCustomerModel = new CustomerModel();
            searchCustomerModel = Core.getRequestObject(requestMessage, CustomerModel.class);
            searchCustomerModel.setBusinessID(requestMessage.businessID);

            vmCustomerModel = this.customerBllManager.getFilteredVMCustomer(searchCustomerModel);


            VMCustomerModel vmInterModuleSupplier = this.checkInterComForGetCustomer(requestMessage, vmCustomerModel);
            vmCustomerModel.lstRememberNoteModels = vmInterModuleSupplier.lstRememberNoteModels;
            vmCustomerModel.lstUserDefineSettingDetailModels = vmInterModuleSupplier.lstUserDefineSettingDetailModels;
            vmCustomerModel.openingBalanceModel = vmInterModuleSupplier.openingBalanceModel;
            responseMessage.responseObj = vmCustomerModel;

            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public VMCustomerModel checkInterComForGetCustomer(RequestMessage requestMessage, VMCustomerModel vmCustomerModel) {

        MqttClient mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail, mqttClientOpeningBalance;
        CallBack callBackSaveRememberNote, callBackSaveUserDefineSettingDetail, callBackOpeningBalance;

        ResponseMessage responseMessage;
        VMCustomerModel vmFilterSupplier = new VMCustomerModel();

        RequestMessage reqRememberNoteSaveMessage, reqUserDefineSettingDetailSaveMessage, reqOpeningBalance;

        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();
            reqRememberNoteSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqUserDefineSettingDetailSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqOpeningBalance = Core.getDefaultWorkerRequestMessage();

            reqRememberNoteSaveMessage.token = requestMessage.token;
            reqUserDefineSettingDetailSaveMessage.token = requestMessage.token;
            reqOpeningBalance.token = requestMessage.token;

            String commonTopic = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;
            String accountingTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(3, lockObject);


            nybsys.tillboxweb.coreModels.RememberNoteModel rememberNoteModel = new nybsys.tillboxweb.coreModels.RememberNoteModel();
            rememberNoteModel.setReferenceType(RememberNoteReferenceType.Customer.get());
            rememberNoteModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());

            reqRememberNoteSaveMessage.requestObj = rememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/search";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);


            nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel = new nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel();
            userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Customer.get());
            userDefineSettingDetailModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());

            reqUserDefineSettingDetailSaveMessage.requestObj = userDefineSettingDetailModel;
            reqUserDefineSettingDetailSaveMessage.token = Core.requestToken.get();
            reqUserDefineSettingDetailSaveMessage.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/search";

            SubscriberForWorker subForUserDefineSettingDetail = new SubscriberForWorker(reqUserDefineSettingDetailSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveUserDefineSettingDetail = subForUserDefineSettingDetail.subscribe();
            callBackSaveUserDefineSettingDetail = subForUserDefineSettingDetail.getCallBack();
            PublisherForWorker pubForWorkerUserDefineSettingDetailList = new PublisherForWorker(commonTopic, mqttClientSaveUserDefineSettingDetail);
            pubForWorkerUserDefineSettingDetailList.publishedMessageToWorker(reqUserDefineSettingDetailSaveMessage);

            nybsys.tillboxweb.coreModels.OpeningBalanceModel openingBalanceModel = new nybsys.tillboxweb.coreModels.OpeningBalanceModel();
            openingBalanceModel.setReferenceID(vmCustomerModel.customerModel.getCustomerID());
            openingBalanceModel.setReferenceType(ReferenceType.CustomerOpeningBalance.get());

            reqOpeningBalance.requestObj = openingBalanceModel;
            reqOpeningBalance.token = Core.requestToken.get();
            reqOpeningBalance.brokerMessage.serviceName = "api/openingBalance/search";

            SubscriberForWorker subForOpeningBalance = new SubscriberForWorker(reqOpeningBalance.brokerMessage.messageId, this.barrier);
            mqttClientOpeningBalance = subForOpeningBalance.subscribe();
            callBackOpeningBalance = subForOpeningBalance.getCallBack();
            PublisherForWorker pubForWorkerOpeningBalance = new PublisherForWorker(accountingTopic, mqttClientOpeningBalance);
            pubForWorkerOpeningBalance.publishedMessageToWorker(reqOpeningBalance);


            synchronized (lockObject) {
                responseMessage = Core.buildDefaultResponseMessage();
                long startTime = System.nanoTime();
                lockObject.wait(this.allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {

                    responseMessage = callBackSaveRememberNote.getResponseMessage();
                    List<nybsys.tillboxweb.coreModels.RememberNoteModel> lstRememberNoteModel = new ArrayList<>();
                    lstRememberNoteModel = Core.convertResponseToList(responseMessage, rememberNoteModel); //(List) responseMessage.responseObj;
                    vmFilterSupplier.lstRememberNoteModels = lstRememberNoteModel;


                    responseMessage = callBackSaveUserDefineSettingDetail.getResponseMessage();
                    List<nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel> userDefineSettingDetails = new ArrayList<>();
                    userDefineSettingDetails = Core.convertResponseToList(responseMessage, userDefineSettingDetailModel); //(List) responseMessage.responseObj;
                    vmFilterSupplier.lstUserDefineSettingDetailModels = userDefineSettingDetails;

                    List<nybsys.tillboxweb.coreModels.OpeningBalanceModel> openingBalanceModels = new ArrayList<>();
                    responseMessage = callBackOpeningBalance.getResponseMessage();


                    openingBalanceModels = Core.convertResponseToList(responseMessage, openingBalanceModel);

                    if (openingBalanceModels.size() > 0) {
                        vmFilterSupplier.openingBalanceModel = openingBalanceModels.get(0);
                    }

                    responseMessage.message = "Inter module communication successful";
                    responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

                } else {
                    //timeout
                    log.info("Response time out");
                    log.info("RollBack checkInterCom Operation");
                    responseMessage.message = "Inter module communication Failed";
                    responseMessage.responseCode = TillBoxAppConstant.UN_PROCESSABLE_REQUEST;
                }
            }


            // this.closeBrokerClient(mqttClientSaveInventoryTransaction, reqObjInventoryTransactionMessage.brokerMessage.messageId);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from checkInterCom Module communication ProductServiceManager");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
        }
        return vmFilterSupplier;
    }


}