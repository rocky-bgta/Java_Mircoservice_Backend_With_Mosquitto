/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 23/02/2018
 * Time: 10:10
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
import nybsys.tillboxweb.bll.manager.BankingDetailsBllManager;
import nybsys.tillboxweb.bll.manager.SupplierAddressBllManger;
import nybsys.tillboxweb.bll.manager.SupplierBllManager;
import nybsys.tillboxweb.bll.manager.SupplierContactBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreEnum.*;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.coreModels.OpeningBalanceModel;
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
public class SupplierServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(SupplierServiceManager.class);
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Autowired
    private SupplierBllManager supplierBllManager = new SupplierBllManager();
    @Autowired
    private SupplierAddressBllManger supplierAddressBllManger = new SupplierAddressBllManger();
    @Autowired
    private SupplierContactBllManager supplierContactBllManager = new SupplierContactBllManager();
    @Autowired
    private BankingDetailsBllManager bankingDetailsBllManager = new BankingDetailsBllManager();

    public ResponseMessage searchSupplier(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        SupplierModel supplierModel, whereConditionModel;
        List<SupplierModel> lstSupplierModel = new ArrayList<>();

        try {

            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            if (supplierModel == null) {
                whereConditionModel = new SupplierModel();
                whereConditionModel.setBusinessID(requestMessage.businessID);
                lstSupplierModel = this.supplierBllManager.searchSupplier(whereConditionModel);
            } else {
                lstSupplierModel = this.supplierBllManager.searchSupplier(supplierModel);
            }


            responseMessage.responseObj = lstSupplierModel;
            if (lstSupplierModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SUPPLIER_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SUPPLIER_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("SupplierServiceManager -> searchSupplier got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public ResponseMessage saveSupplierVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMSupplierModel vmSupplierModel;
        SupplierModel supplierModel;
        BankingDetailsModel bankingDetailsModel;
        OpeningBalanceModel openingBalanceModel;
        List<SupplierAddressModel> lstSupplierAddressModel;
        List<SupplierContactModel> lstSupplierContactModel;
        VMRememberNoteModel vmRememberNoteModel;
        VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel;
        ReportingLayoutModel reportingLayoutModel;

        try {
            vmSupplierModel = Core.getRequestObject(requestMessage, VMSupplierModel.class);
            supplierModel = vmSupplierModel.supplierModel;
            openingBalanceModel = vmSupplierModel.openingBalanceModel;
            lstSupplierAddressModel = vmSupplierModel.lstSupplierAddressModel;
            lstSupplierContactModel = vmSupplierModel.lstSupplierContactModel;
            bankingDetailsModel = vmSupplierModel.bankingDetailsModel;
            // vmRememberNoteModel = vmSupplierModel.vmRememberNoteModel;
            vmUserDetailSettingDetailModel = vmSupplierModel.vmUserDetailSettingDetailModel;
            reportingLayoutModel = vmSupplierModel.reportingLayoutModel;

            /*Set<ConstraintViolation<AddressTypeModel>> violations = this.validator.validate(supplierAddressTypeModel);
            if (violations.size() > 0) {
                responseMessage = this.buildResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/

            //(1)
            supplierModel.setBusinessID(requestMessage.businessID);
            supplierModel = this.supplierBllManager.saveOrUpdate(supplierModel);
            vmSupplierModel.supplierModel.setSupplierID(supplierModel.getSupplierID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(2)
            for (SupplierAddressModel supplierAddressModel : lstSupplierAddressModel) {

                supplierAddressModel.setSupplierID(supplierModel.getSupplierID());
                this.supplierAddressBllManger.saveOrUpdate(supplierAddressModel);
                if (Core.clientMessage.get().messageCode != null) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    if (Core.clientMessage.get().userMessage == null) {
                        responseMessage.message = MessageConstant.SUPPLIER_SAVE_FAILED;
                    } else {
                        responseMessage.message = Core.clientMessage.get().userMessage;
                    }
                    this.rollBack();
                    return responseMessage;
                }
            }
            //(3)
            for (SupplierContactModel supplierContactModel : lstSupplierContactModel) {
                supplierContactModel.setSupplierID(supplierModel.getSupplierID());
                this.supplierContactBllManager.saveOrUpdate(supplierContactModel);
                if (Core.clientMessage.get().messageCode != null) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    if (Core.clientMessage.get().userMessage == null) {
                        responseMessage.message = MessageConstant.SUPPLIER_SAVE_FAILED;
                    } else {
                        responseMessage.message = Core.clientMessage.get().userMessage;
                    }
                    this.rollBack();
                    return responseMessage;
                }
            }
            //(4)
            bankingDetailsModel.setBusinessID(requestMessage.businessID);
            bankingDetailsModel.setReferenceID(supplierModel.getSupplierID());
            bankingDetailsModel.setReferenceType(BankReferenceType.Supplier.get());
            this.bankingDetailsBllManager.saveOrUpdate(bankingDetailsModel);

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            } else {
                this.checkInterCom(requestMessage, vmSupplierModel);
            }

            this.commit();
            responseMessage.message = MessageConstant.SUPPLIER_SAVE_SUCCESSFULLY;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
        } catch (Exception ex) {
            log.error("SupplierServiceManager -> saveSupplierVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }


    public ResponseMessage searchSupplierVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        SupplierModel supplierModel = new SupplierModel();
        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        List<VMSupplierModel> lstVmSupplierModel = new ArrayList<>();
        try {
            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            Integer businessID = requestMessage.businessID;

            lstSupplierModel = this.supplierBllManager.searchSupplier(supplierModel);
            if (lstSupplierModel.size() > 0) {
                for (SupplierModel supplierModelObj : lstSupplierModel) {
                    VMSupplierModel vmSupplierModel = new VMSupplierModel();
                    vmSupplierModel = singleSupplierVMSearch(supplierModelObj, businessID);
                    lstVmSupplierModel.add(vmSupplierModel);
                }
            }
            responseMessage.responseObj = lstVmSupplierModel;
            if (lstVmSupplierModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SUPPLIER_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SUPPLIER_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("SupplierServiceManager -> searchSupplierVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public VMSupplierModel singleSupplierVMSearch(SupplierModel supplierModel, Integer businessID) throws Exception {
        VMSupplierModel vmSupplierModel = new VMSupplierModel();
        ResponseMessage responseMessageFromInterModule = new ResponseMessage();
        try {
            //(1)
            vmSupplierModel.supplierModel = supplierModel;
            //(2)
            SupplierAddressModel addressWhereCondition = new SupplierAddressModel();
            addressWhereCondition.setSupplierID(supplierModel.getSupplierID());
            addressWhereCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            vmSupplierModel.lstSupplierAddressModel = this.supplierAddressBllManger.searchSupplierAddress(addressWhereCondition);
            //(3)
            SupplierContactModel contactWhereCondition = new SupplierContactModel();
            contactWhereCondition.setSupplierID(supplierModel.getSupplierID());
            contactWhereCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            vmSupplierModel.lstSupplierContactModel = this.supplierContactBllManager.searchSupplierContact(contactWhereCondition);
            //(4)
            vmSupplierModel.bankingDetailsModel = this.bankingDetailsBllManager.searchBankingDetailsByReferenceIDAndReferenceType(supplierModel.getSupplierID(), BankReferenceType.Supplier.get(), businessID);

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
            OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
            openingBalanceModel.setReferenceID(supplierModel.getSupplierID());
            openingBalanceModel.setBusinessID(businessID);
            openingBalanceModel.setAccountID(DefaultCOA.TradeCreditors.get());
            openingBalanceModel.setReferenceType(ReferenceType.SupplierOpeningBalance.get());
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
//            rememberNoteModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Supplier.get());
//            rememberNoteModel.setReferenceID(supplierModel.getSupplierID());
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
//            userDefineSettingDetailModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Supplier.get());
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
//            //------------type to do supplier/supplier/else------------
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

            //(5)
            if (responseFromOpeningBalanceSearch.responseObj != null) {
                vmSupplierModel.openingBalanceModel = Core.modelMapper.map(responseFromOpeningBalanceSearch.responseObj, OpeningBalanceModel.class);
            }

            //(6)
//            vmSupplierModel.vmRememberNoteModel.lstRememberNoteModel = Core.convertResponseToList(responseFromRememberNoteSearch,rememberNoteModel);

            //(7)
//            vmSupplierModel.vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = Core.convertResponseToList(responseFromUserDefineSettingDetailSearch,userDefineSettingDetailModel);

            //(8)
//            List<ReportingLayoutModel> lstReportingLayoutModel = new ArrayList<>();
//            lstReportingLayoutModel = Core.convertResponseToList(responseFromReportingLayoutSearch,reportingLayoutModel);
//            if(lstReportingLayoutModel.size() > 0)
//            {
//                reportingLayoutModel = lstReportingLayoutModel.get(0);
//            }else
//            {
//                reportingLayoutModel = null;
//            }
//            vmSupplierModel.reportingLayoutModel = reportingLayoutModel;


        } catch (Exception ex) {
            log.error("SupplierServiceManager -> singleSupplierVMSearch got exception");
            this.WriteExceptionLog(ex);
            throw ex;
        }
        return vmSupplierModel;
    }

    public ResponseMessage deleteSupplierVM(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ResponseMessage responseFromInterModule = new ResponseMessage();
        SupplierModel supplierModel = new SupplierModel();
        try {
            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            Integer businessID = requestMessage.businessID;

            //(1)
            //check journal data exists or not ;exclude opening balance;
            responseFromInterModule = getJournalExistsExcludeOpeningBalance(businessID, supplierModel.getSupplierID());
            if ((responseFromInterModule.responseCode != null && responseFromInterModule.responseCode != TillBoxAppConstant.SUCCESS_CODE) || responseFromInterModule.responseCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (responseFromInterModule.message == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
                } else {
                    responseMessage.message = responseFromInterModule.message;
                }
                return responseMessage;
            } else {
                Boolean existsFlag = (Boolean) responseFromInterModule.responseObj;
                if (existsFlag.booleanValue() == true) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = MessageConstant.SUPPLIER_JOURNAL_EXISTS;
                    return responseMessage;
                }
            }
            //(2)
            this.supplierBllManager.deleteSupplierByID(supplierModel.getSupplierID(), businessID);
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(3)
            this.supplierAddressBllManger.deleteSupplierAddress(supplierModel.getSupplierID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(4)
            this.supplierContactBllManager.deleteSupplierContact(supplierModel.getSupplierID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
                return responseMessage;
            }
            //(5)
            this.bankingDetailsBllManager.deleteBankingDetailByReferenceIDAndReferenceType(supplierModel.getSupplierID(), BankReferenceType.Supplier.get(), supplierModel.getBusinessID());
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
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
            openingBalanceModel.setAccountID(DefaultCOA.TradeCreditors.get());
            openingBalanceModel.setReferenceID(supplierModel.getSupplierID());
            openingBalanceModel.setReferenceType(ReferenceType.SupplierOpeningBalance.get());
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
//            rememberNoteModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Supplier.get());
//            rememberNoteModel.setReferenceID(supplierModel.getSupplierID());
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
//            userDefineSettingDetailModel.setReferenceType(TillBoxAppEnum.BankReferenceType.Supplier.get());
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
//            //------------type to do supplier/supplier/else------------
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

            //(6)
            if (responseFromOpeningBalanceDelete.responseCode != null && responseFromOpeningBalanceDelete.responseCode != 200) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (responseFromOpeningBalanceDelete.message == null) {
                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
                } else {
                    responseMessage.message = responseFromOpeningBalanceDelete.message;
                }
                this.rollBack();
                return responseMessage;
            }
            //(7)
//            if (responseFromRememberNoteDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromRememberNoteDelete.message == null) {
//                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromRememberNoteDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }
            //(8)
//            if (responseFromReportingLayoutDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromReportingLayoutDelete.message == null) {
//                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromReportingLayoutDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }
            //(9)
//            if (responseFromUserDefineSettingDetailDelete.responseCode != 200) {
//                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                if (responseFromUserDefineSettingDetailDelete.message == null) {
//                    responseMessage.message = MessageConstant.SUPPLIER_DELETE_FAILED;
//                } else {
//                    responseMessage.message = responseFromUserDefineSettingDetailDelete.message;
//                }
//                this.rollBack();
//                return responseMessage;
//            }

            this.commit();
            responseMessage.responseObj = supplierModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.message = MessageConstant.SUPPLIER_DELETE_SUCCESSFULLY;

        } catch (Exception ex) {
            log.error("SupplierServiceManager -> deleteSupplierVM got exception");
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
            journalModel.setPartyType(PartyType.Supplier.get());
            journalModel.setPartyID(partyID);
            journalModel.setReferenceType(ReferenceType.SupplierOpeningBalance.get());
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
            log.error("SupplierServiceManager -> inter module communication getJournalExistsExcludeOpeningBalance got exception");
        }
        return responseMessage;
    }

    public ResponseMessage searchSupplierVMList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        SupplierModel supplierModel = new SupplierModel();
        List<VMSupplierList> lstVmSupplierModel = new ArrayList<>();
        try {
            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            supplierModel.setBusinessID(requestMessage.businessID);
            lstVmSupplierModel = this.supplierBllManager.getSupplierList(supplierModel);
            responseMessage.responseObj = lstVmSupplierModel;

        } catch (Exception ex) {
            log.error("SupplierServiceManager -> searchSupplierVM got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getVMSupplier(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {

            VMSupplierModel vmSupplierModel = new VMSupplierModel();
            SupplierModel supplierModel = new SupplierModel();
            SupplierModel searchSupplierModel = new SupplierModel();
            searchSupplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            searchSupplierModel.setBusinessID(requestMessage.businessID);

            vmSupplierModel = this.supplierBllManager.getFilteredVMSupplier(searchSupplierModel);


            VMSupplierModel vmInterModuleSupplier = this.checkInterComForGetProduct(requestMessage, vmSupplierModel);
            vmSupplierModel.lstRememberNoteModels = vmInterModuleSupplier.lstRememberNoteModels;
            vmSupplierModel.lstUserDefineSettingDetailModels = vmInterModuleSupplier.lstUserDefineSettingDetailModels;
            vmSupplierModel.openingBalanceModel = vmInterModuleSupplier.openingBalanceModel;
            responseMessage.responseObj = vmSupplierModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public VMSupplierModel checkInterComForGetProduct(RequestMessage requestMessage, VMSupplierModel vmProduct) {

        MqttClient mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail, mqttClientOpeningBalance;
        CallBack callBackSaveRememberNote, callBackSaveUserDefineSettingDetail, callBackOpeningBalance;

        ResponseMessage responseMessage;
        VMSupplierModel vmFilterSupplier = new VMSupplierModel();

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
            rememberNoteModel.setReferenceType(RememberNoteReferenceType.Supplier.get());
            rememberNoteModel.setReferenceID(vmProduct.supplierModel.getSupplierID());

            reqRememberNoteSaveMessage.requestObj = rememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/search";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);


            nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel = new nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel();
            userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Supplier.get());
            userDefineSettingDetailModel.setReferenceID(vmProduct.supplierModel.getSupplierID());

            reqUserDefineSettingDetailSaveMessage.requestObj = userDefineSettingDetailModel;
            reqUserDefineSettingDetailSaveMessage.token = Core.requestToken.get();
            reqUserDefineSettingDetailSaveMessage.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/search";

            SubscriberForWorker subForUserDefineSettingDetail = new SubscriberForWorker(reqUserDefineSettingDetailSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveUserDefineSettingDetail = subForUserDefineSettingDetail.subscribe();
            callBackSaveUserDefineSettingDetail = subForUserDefineSettingDetail.getCallBack();
            PublisherForWorker pubForWorkerUserDefineSettingDetailList = new PublisherForWorker(commonTopic, mqttClientSaveUserDefineSettingDetail);
            pubForWorkerUserDefineSettingDetailList.publishedMessageToWorker(reqUserDefineSettingDetailSaveMessage);

            OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
            openingBalanceModel.setReferenceID(vmProduct.supplierModel.getSupplierID());
            openingBalanceModel.setReferenceType(ReferenceType.SupplierOpeningBalance.get());

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
                    lstRememberNoteModel =  Core.convertResponseToList(responseMessage, rememberNoteModel); //(List) responseMessage.responseObj;
                    vmFilterSupplier.lstRememberNoteModels = lstRememberNoteModel;


                    responseMessage = callBackSaveUserDefineSettingDetail.getResponseMessage();
                    List<nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel> userDefineSettingDetails = new ArrayList<>();
                    userDefineSettingDetails =  Core.convertResponseToList(responseMessage, userDefineSettingDetailModel); //(List) responseMessage.responseObj;
                    vmFilterSupplier.lstUserDefineSettingDetailModels = userDefineSettingDetails;

                    List<OpeningBalanceModel> openingBalanceModels = new ArrayList<>();
                    responseMessage = callBackOpeningBalance.getResponseMessage();



                    openingBalanceModels =   Core.convertResponseToList(responseMessage, openingBalanceModel);

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


    public ResponseMessage checkInterCom(RequestMessage requestMessage, VMSupplierModel vmSupplierModel) {

        MqttClient mqttClientSaveOpeningBalance, mqttClientSaveJournal, mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail;
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


//            VMJournalListModel vmJournalListModel = new VMJournalListModel();
//            vmJournalListModel.lstJournalModel = this.getJournalModelFromInvoice(vmSupplierModel, currencyModel, entryCurrencyID);
//            reqJournalSaveMessage.requestObj = vmJournalListModel;
//            reqJournalSaveMessage.token = Core.requestToken.get();
//            reqJournalSaveMessage.brokerMessage.serviceName = "api/journal/save";
//
//            SubscriberForWorker subForWorker = new SubscriberForWorker(reqJournalSaveMessage.brokerMessage.messageId, this.barrier);
//            mqttClientSaveJournal = subForWorker.subscribe();
//            callBackSaveJournal = subForWorker.getCallBack();
//            PublisherForWorker pubForWorkerGetJournalList = new PublisherForWorker(accountingTopic, mqttClientSaveJournal);
//            pubForWorkerGetJournalList.publishedMessageToWorker(reqJournalSaveMessage);


            OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
            openingBalanceModel.setAmount(vmSupplierModel.supplierModel.getOpeningBalanceAmount());
            openingBalanceModel.setAccountID(DefaultCOA.TradeCreditors.get());
            openingBalanceModel.setBusinessID(requestMessage.businessID);
            openingBalanceModel.setReferenceID(vmSupplierModel.supplierModel.getSupplierID());
            openingBalanceModel.setReferenceType(ReferenceType.SupplierOpeningBalance.get());

            reqMessForWorkerOpeningBalanceSave.requestObj = openingBalanceModel;
            reqMessForWorkerOpeningBalanceSave.token = Core.requestToken.get();
            reqMessForWorkerOpeningBalanceSave.brokerMessage.serviceName = "api/openingBalance/save";

            SubscriberForWorker subWorkerForOpeningBalance = new SubscriberForWorker(reqJournalSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveOpeningBalance = subWorkerForOpeningBalance.subscribe();
            callBackSaveJournal = subWorkerForOpeningBalance.getCallBack();
            PublisherForWorker pubForWorkerGetOpeningBalance = new PublisherForWorker(accountingTopic, mqttClientSaveOpeningBalance);
            pubForWorkerGetOpeningBalance.publishedMessageToWorker(reqMessForWorkerOpeningBalanceSave);


            for (nybsys.tillboxweb.coreModels.RememberNoteModel rememberNoteModel : vmSupplierModel.lstRememberNoteModels) {
                rememberNoteModel.setReferenceType(RememberNoteReferenceType.Supplier.get());
                rememberNoteModel.setReferenceID(vmSupplierModel.supplierModel.getSupplierID());
            }

            nybsys.tillboxweb.coreModels.VMRememberNoteModel vmRememberNoteModel = new nybsys.tillboxweb.coreModels.VMRememberNoteModel();
            vmRememberNoteModel.lstRememberNoteModel = vmSupplierModel.lstRememberNoteModels;
            reqRememberNoteSaveMessage.requestObj = vmRememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/save";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);

            nybsys.tillboxweb.coreModels.VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel = new nybsys.tillboxweb.coreModels.VMUserDetailSettingDetailModel();


            for (nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel : vmSupplierModel.lstUserDefineSettingDetailModels) {
                userDefineSettingDetailModel.setReferenceID(vmSupplierModel.supplierModel.getSupplierID());
                userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Supplier.get());
            }


            vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = vmSupplierModel.lstUserDefineSettingDetailModels;
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


}