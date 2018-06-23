/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/14/2018
 * Time: 10:50 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.Enum.InventoryProductType;
import nybsys.tillboxweb.Enum.ProductAdjustmentType;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.bll.manager.InventoryTransactionBllManager;
import nybsys.tillboxweb.bll.manager.ProductBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.companySettingUtils.RoundingUtils;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreBllManager.RoundingBllManager;
import nybsys.tillboxweb.coreConstant.CompanySettingConstant;
import nybsys.tillboxweb.coreConstant.CurrencyConstant;
import nybsys.tillboxweb.coreEnum.*;
import nybsys.tillboxweb.coreModels.*;
import nybsys.tillboxweb.coreModels.RememberNoteModel;
import nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel;
import nybsys.tillboxweb.models.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProductServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceManager.class);

    @Autowired
    private ProductBllManager productBllManager = new ProductBllManager();

    private RoundingBllManager roundingBllManager;

    private InventoryTransactionBllManager inventoryTransactionBllManager = new InventoryTransactionBllManager();

    public ResponseMessage saveProduct(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        boolean isUpdateRequest = false;
        CurrencyModel currencyModel;
        VMProduct vmProduct;
        try {

            //get base currency and exchange rate
            currencyModel = getBaseCurrency();
            if (currencyModel == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return responseMessage;
            } else {
                currencyModel.setExchangeRate(1.00);
            }

            //check entry currency is present if not base currency will be entry currency
            if (requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0) {
                requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
            }

            vmProduct = Core.getRequestObject(requestMessage, VMProduct.class);

            /*Set<ConstraintViolation<ProductAttributeModel>> violations = this.validator.validate(productAttributeModel);
            if (violations.size() > 0) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/
            if (vmProduct.productModel.getProductID() != null && vmProduct.productModel.getProductID() > 0) {
                isUpdateRequest = true;
            }

            vmProduct = this.productBllManager.saveProduct(vmProduct, requestMessage.businessID);


            responseMessage.responseObj = vmProduct;

            if (Core.clientMessage.get().messageCode == null) {
                if (isUpdateRequest) {
                    if (vmProduct.productModel.getOpeningQuantity() != null && vmProduct.productModel.getOpeningQuantity() > 0) {
                        responseMessage = this.checkInterComForUpdate(requestMessage, vmProduct, currencyModel, requestMessage.entryCurrencyID);
                    }
                } else {
                    if (vmProduct.productModel.getOpeningQuantity() != null && vmProduct.productModel.getOpeningQuantity() > 0) {
                        responseMessage = this.checkInterCom(requestMessage, vmProduct, currencyModel, requestMessage.entryCurrencyID);
                    }

                }

                if (responseMessage.responseCode != TillBoxAppConstant.SUCCESS_CODE) {
                    this.rollBack();
                    if (responseMessage.message != null && responseMessage.message != "") {
                        responseMessage.message = MessageConstant.FAILED_TO_SAVE_PRODUCT;
                    }
                } else {
                    responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                    responseMessage.message = MessageConstant.SAVE_PRODUCT;
                    this.commit();
                }
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.FAILED_TO_SAVE_PRODUCT;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> Product Service Manager got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
//            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage updateProductCode(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMReNumberItemCode vmReNumberItemCode;
        try {
            vmReNumberItemCode = Core.getRequestObject(requestMessage, VMReNumberItemCode.class);

            this.productBllManager.updateProductCode(vmReNumberItemCode);

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.PRODUCT_CODE_UPDATE_FAILED;
                this.rollBack();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PRODUCT_CODE_UPDATE_SUCCESSFULLY;
                this.commit();
            }
        } catch (Exception ex) {
            log.error("ProductServiceManager -> updateProductCode got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage getAllProductCode(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {

            responseMessage.responseObj = this.productBllManager.getProductCodeList(requestMessage.businessID);

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.PRODUCT_CODE_GET_FAILED;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            }
        } catch (Exception ex) {
            log.error("ProductServiceManager -> getAllProductCode got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage saveProductOpeningBalance(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        AdjustItemOpeningBalance adjustItemOpeningBalance = new AdjustItemOpeningBalance();
        ProductModel productModel;
        try {

            adjustItemOpeningBalance = Core.getRequestObject(requestMessage, AdjustItemOpeningBalance.class);

            //(1) update product table
            productModel = this.productBllManager.saveProductOpeningBalance(requestMessage.businessID, adjustItemOpeningBalance.getItemID(), adjustItemOpeningBalance.getNewOpeningCost(), adjustItemOpeningBalance.getNewOpeningQuantity());

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.ITEM_OPENING_BALANCE_SAVE_FAILED;
                this.rollBack();
                return responseMessage;
            }

            //(2) update inventory transaction table table
            this.inventoryTransactionBllManager.saveInventoryTransactionOpeningBalance(adjustItemOpeningBalance, productModel, requestMessage.businessID);

            //(3)add product opening balance
            String serviceLink = "api/openingBalance/item/save";
            String topicName = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;

            OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
            openingBalanceModel.setReferenceID(adjustItemOpeningBalance.getItemID());
            openingBalanceModel.setDate(adjustItemOpeningBalance.getOpeningBalanceAsAt());
            openingBalanceModel.setAmount(adjustItemOpeningBalance.getNewOpeningQuantity() * adjustItemOpeningBalance.getNewOpeningCost());
            if (adjustItemOpeningBalance.getOpeningBalanceAsAt() != null) {
                openingBalanceModel.setDate(adjustItemOpeningBalance.getOpeningBalanceAsAt());
            } else {
                openingBalanceModel.setDate(new Date());
            }
            openingBalanceModel.setAmount(adjustItemOpeningBalance.getNewOpeningQuantity() * adjustItemOpeningBalance.getNewOpeningCost());
            openingBalanceModel.setNote(adjustItemOpeningBalance.getReason());

            ResponseMessage interModuleResponse;
            interModuleResponse = this.callInterModuleFunction(serviceLink, topicName, openingBalanceModel);

            if (interModuleResponse.responseCode != null && interModuleResponse.responseCode != 200) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.ITEM_OPENING_BALANCE_SAVE_FAILED;
                this.rollBack();
                return responseMessage;
            }

            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.message = MessageConstant.ITEM_OPENING_BALANCE_SAVE_SUCCESSFULLY;
            this.commit();
        } catch (Exception ex) {
            log.error("ProductServiceManager -> saveProductOpeningBalance got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }


    private List<JournalModel> getJournalModelFromInvoice(VMProduct vmProduct, CurrencyModel currencyModel, Integer entryCurrencyID) {
        List<JournalModel> lstJournalModel = new ArrayList<>();

        double totalAmount = (vmProduct.productModel.getOpeningQuantity() * vmProduct.productModel.getOpeningCost());

        //Debit journal
        JournalModel journalModelDebit = new JournalModel();
        journalModelDebit.setBusinessID(vmProduct.productModel.getBusinessID());
        journalModelDebit.setAmount(totalAmount);
        journalModelDebit.setAccountID(DefaultCOA.Inventory.get());
        journalModelDebit.setReferenceID(vmProduct.productModel.getProductID());
        journalModelDebit.setReferenceType(ReferenceType.Product.get());
        journalModelDebit.setDrCrIndicator(DebitCreditIndicator.Debit.get());
        journalModelDebit.setDate(new Date());

        journalModelDebit.setBaseCurrencyID(currencyModel.getCurrencyID());
        journalModelDebit.setEntryCurrencyID(entryCurrencyID);
        journalModelDebit.setExchangeRate(currencyModel.getExchangeRate());
        journalModelDebit.setBaseCurrencyAmount(totalAmount * currencyModel.getExchangeRate());

        //Credit journal
        JournalModel journalModelCredit = new JournalModel();
        journalModelCredit.setBusinessID(vmProduct.productModel.getBusinessID());
        journalModelCredit.setAmount(totalAmount);
        journalModelCredit.setAccountID(DefaultCOA.HistoricalBalance.get());
        journalModelCredit.setReferenceID(vmProduct.productModel.getProductID());
        journalModelCredit.setReferenceType(ReferenceType.Product.get());
        journalModelCredit.setDrCrIndicator(DebitCreditIndicator.Credit.get());
        journalModelCredit.setDate(new Date());

        journalModelCredit.setBaseCurrencyID(currencyModel.getCurrencyID());
        journalModelCredit.setEntryCurrencyID(entryCurrencyID);
        journalModelCredit.setExchangeRate(currencyModel.getExchangeRate());
        journalModelCredit.setBaseCurrencyAmount(totalAmount * currencyModel.getExchangeRate());

        lstJournalModel.add(journalModelDebit);
        lstJournalModel.add(journalModelCredit);


        return lstJournalModel;
    }


    public ResponseMessage checkInterCom(RequestMessage requestMessage, VMProduct vmProduct, CurrencyModel currencyModel, Integer entryCurrencyID) {

        MqttClient mqttClientSaveJournal, mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail;
        CallBack callBackSaveJournal, callBackSaveRememberNote, callBackSaveUserDefineSettingDetail;

        //CallBack callBackDeleteJournal = null;


        ResponseMessage responseMessage;// = new ResponseMessage();
        ResponseMessage responseMessageSaveJournal;


        RequestMessage reqJournalSaveMessage, reqRememberNoteSaveMessage, reqUserDefineSettingDetailSaveMessage;

        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();

            reqJournalSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqRememberNoteSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqUserDefineSettingDetailSaveMessage = Core.getDefaultWorkerRequestMessage();

            reqJournalSaveMessage.token = requestMessage.token;
            reqRememberNoteSaveMessage.token = requestMessage.token;
            reqUserDefineSettingDetailSaveMessage.token = requestMessage.token;

            String accountingTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            String commonTopic = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(3, lockObject);


            VMJournalListModel vmJournalListModel = new VMJournalListModel();
            vmJournalListModel.lstJournalModel = this.getJournalModelFromInvoice(vmProduct, currencyModel, entryCurrencyID);
            reqJournalSaveMessage.requestObj = vmJournalListModel;
            reqJournalSaveMessage.token = Core.requestToken.get();
            reqJournalSaveMessage.brokerMessage.serviceName = "api/journal/save";

            SubscriberForWorker subForWorker = new SubscriberForWorker(reqJournalSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveJournal = subForWorker.subscribe();
            callBackSaveJournal = subForWorker.getCallBack();
            PublisherForWorker pubForWorkerGetJournalList = new PublisherForWorker(accountingTopic, mqttClientSaveJournal);
            pubForWorkerGetJournalList.publishedMessageToWorker(reqJournalSaveMessage);

            for (RememberNoteModel rememberNoteModel : vmProduct.lstRememberNoteModels) {
                rememberNoteModel.setReferenceType(RememberNoteReferenceType.Product.get());
                rememberNoteModel.setReferenceID(vmProduct.productModel.getProductID());
            }

            VMRememberNoteModel vmRememberNoteModel = new VMRememberNoteModel();
            vmRememberNoteModel.lstRememberNoteModel = vmProduct.lstRememberNoteModels;
            reqRememberNoteSaveMessage.requestObj = vmRememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/save";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);

            VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel = new VMUserDetailSettingDetailModel();


            for (nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel : vmProduct.lstUserDefineSettingDetailModels) {
                userDefineSettingDetailModel.setReferenceID(vmProduct.productModel.getProductID());
                userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Product.get());
            }


            vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = vmProduct.lstUserDefineSettingDetailModels;
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


            this.closeBrokerClient(mqttClientSaveJournal, reqJournalSaveMessage.brokerMessage.messageId);
            // this.closeBrokerClient(mqttClientSaveInventoryTransaction, reqObjInventoryTransactionMessage.brokerMessage.messageId);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from checkInterCom Module communication ProductServiceManager");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
        }
        return responseMessage;
    }

    public ResponseMessage checkInterComForUpdate(RequestMessage requestMessage, VMProduct vmProduct, CurrencyModel currencyModel, Integer entryCurrencyID) {

        MqttClient mqttClientDeleteJournal, mqttClientSaveJournal, mqttClientDeleteRememberNote, mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail;
        CallBack callBackDeleteJournal, callBackSaveJournal, callBackDeleteRememberNote, callBackSaveRememberNote, callBackSaveUserDefineSettingDetail;

        //CallBack callBackDeleteJournal = null;


        ResponseMessage responseMessage;// = new ResponseMessage();
        ResponseMessage responseMessageSaveJournal;


        RequestMessage journalDeletedRequestMessage,
                reqJournalSaveMessage, rememberNoteDeleteMessage, reqRememberNoteSaveMessage,
                reqUserDefineSettingDetailSaveMessage;

        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();
            journalDeletedRequestMessage = Core.getDefaultWorkerRequestMessage();
            reqJournalSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqRememberNoteSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqUserDefineSettingDetailSaveMessage = Core.getDefaultWorkerRequestMessage();
            rememberNoteDeleteMessage = Core.getDefaultWorkerRequestMessage();

            journalDeletedRequestMessage.token = requestMessage.token;
            reqJournalSaveMessage.token = requestMessage.token;
            reqRememberNoteSaveMessage.token = requestMessage.token;
            reqUserDefineSettingDetailSaveMessage.token = requestMessage.token;

            String accountingTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
            String commonTopic = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(4, lockObject);


            JournalModel searchJournalModel = new JournalModel();
            searchJournalModel.setReferenceType(ReferenceType.Product.get());
            searchJournalModel.setReferenceID(vmProduct.productModel.getProductID());

            journalDeletedRequestMessage.requestObj = searchJournalModel;
            journalDeletedRequestMessage.brokerMessage.serviceName = "api/journal/delete";
            SubscriberForWorker subForWorkerJournalDelete = new SubscriberForWorker(journalDeletedRequestMessage.brokerMessage.messageId, this.barrier);
            mqttClientDeleteJournal = subForWorkerJournalDelete.subscribe();
            callBackDeleteJournal = subForWorkerJournalDelete.getCallBack();
            PublisherForWorker pubForWorkerForJournalDelete = new PublisherForWorker(accountingTopic, mqttClientDeleteJournal);
            pubForWorkerForJournalDelete.publishedMessageToWorker(journalDeletedRequestMessage);


            VMJournalListModel vmJournalListModel = new VMJournalListModel();
            vmJournalListModel.lstJournalModel = this.getJournalModelFromInvoice(vmProduct, currencyModel, entryCurrencyID);
            reqJournalSaveMessage.requestObj = vmJournalListModel;
            reqJournalSaveMessage.token = Core.requestToken.get();
            reqJournalSaveMessage.brokerMessage.serviceName = "api/journal/save";

            SubscriberForWorker subForWorker = new SubscriberForWorker(reqJournalSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveJournal = subForWorker.subscribe();
            callBackSaveJournal = subForWorker.getCallBack();
            PublisherForWorker pubForWorkerGetJournalList = new PublisherForWorker(accountingTopic, mqttClientSaveJournal);
            pubForWorkerGetJournalList.publishedMessageToWorker(reqJournalSaveMessage);

            for (RememberNoteModel rememberNoteModel : vmProduct.lstRememberNoteModels) {
                rememberNoteModel.setReferenceType(RememberNoteReferenceType.Product.get());
                rememberNoteModel.setReferenceID(vmProduct.productModel.getProductID());
            }

            VMRememberNoteModel vmRememberNoteModel = new VMRememberNoteModel();
            vmRememberNoteModel.lstRememberNoteModel = vmProduct.lstRememberNoteModels;
            reqRememberNoteSaveMessage.requestObj = vmRememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/save";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);

            VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel = new VMUserDetailSettingDetailModel();


            for (nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel : vmProduct.lstUserDefineSettingDetailModels) {
                userDefineSettingDetailModel.setReferenceID(vmProduct.productModel.getProductID());
                userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Product.get());
            }


            vmUserDetailSettingDetailModel.lstUserDefineSettingDetailModel = vmProduct.lstUserDefineSettingDetailModels;
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


            this.closeBrokerClient(mqttClientSaveJournal, reqJournalSaveMessage.brokerMessage.messageId);
            // this.closeBrokerClient(mqttClientSaveInventoryTransaction, reqObjInventoryTransactionMessage.brokerMessage.messageId);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from checkInterCom Module communication ProductServiceManager");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
        }
        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<ProductModel> lstProductModel = new ArrayList<>();
        ProductModel productModel = new ProductModel();
        try {
            productModel = Core.getRequestObject(requestMessage, ProductModel.class);
            productModel.setBusinessID(requestMessage.businessID);
            responseMessage.responseObj = this.productBllManager.getFilteredVMProduct(productModel);


        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage likeSearch(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<VMProduct> lstVmProduct = new ArrayList<>();
        ProductModel productModel = new ProductModel();
        try {
            productModel = Core.getRequestObject(requestMessage, ProductModel.class);
            Integer businessID = requestMessage.businessID;
            lstVmProduct = this.productBllManager.getLikeFilteredProduct(productModel, businessID);

            responseMessage.responseObj = lstVmProduct;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PRODUCT_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.PRODUCT_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getByID(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ProductModel productModel = new ProductModel();
        try {
            productModel = Core.getRequestObject(requestMessage, ProductModel.class);

            /*Set<ConstraintViolation<ProductTypeModel>> violations = this.validator.validate(productTypeModel);
            if (violations.size() > 0) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/

            productModel = this.productBllManager.getById(productModel.getProductID());

            responseMessage.responseObj = productModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SUCCESSFULLY_INACTIVE_PRODUCT_TYPE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.FAILED_TO_INACTIVE_PRODUCT_TYPE;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> filterProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public ResponseMessage getAllProduct(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ProductModel productModel = new ProductModel();
        List<ProductModel> productModelList;
        try {

            productModel.setBusinessID(requestMessage.businessID);
            productModelList = this.productBllManager.getAllByConditionWithActive(productModel);

            if (productModelList != null && productModelList.size() > 0) {
                responseMessage.responseObj = productModelList;
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PRODUCT_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.PRODUCT_GET_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> getAllProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getAllActiveAndInactiveProduct(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<ProductModel> productModelList;
        try {

            productModelList = this.productBllManager.getActiveAndInactiveProducts(requestMessage.businessID);

            if (productModelList != null && productModelList.size() > 0) {
                responseMessage.responseObj = productModelList;
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PRODUCT_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.PRODUCT_GET_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> getAllProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getProductWithStockAndPrice(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ProductModel productModel = new ProductModel();
        List<VMProductWithStockAndPrice> lstVmProductWithStockAndPrice = new ArrayList<>();
        try {
            productModel = Core.getRequestObject(requestMessage, ProductModel.class);

            lstVmProductWithStockAndPrice = this.productBllManager.getProductWithStockAndPrice(productModel);

            responseMessage.responseObj = lstVmProductWithStockAndPrice;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PRODUCT_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.PRODUCT_GET_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> getProductWithStockAndPrice got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage callInterModuleFunction(String apiurl, String topicName, Object reqObj) {
        ResponseMessage responseMessage = new ResponseMessage();
        RequestMessage reqMessForWorker = Core.getDefaultWorkerRequestMessage();
        try {

            MqttClient mqttClient;
            responseMessage = Core.buildDefaultResponseMessage();

            boolean workCompleteWithInAllowTime;
            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);

            CallBack callBack;
            String pubTopic = topicName;
            reqMessForWorker.brokerMessage.serviceName = apiurl;
            reqMessForWorker.token = Core.requestToken.get();
            reqMessForWorker.requestObj = reqObj;
           /* UserDefineSettingModel userDefineSettingModel = new UserDefineSettingModel();
            userDefineSettingModel.setReferenceID(TillBoxAppEnum.UserDefineSettingReferenceType.Product.get());

            reqMessForWorker.requestObj = userDefineSettingModel;*/
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
            log.error("ProductServiceManager -> interModuelFunction got exception");
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
            log.error("ProductServiceManager -> inter module communication getBaseCurrencyAndExchangeRate got exception");
        }
        return currencyModel;
    }


    public ResponseMessage searchProductList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<VMProductView> lstVMProductView = new ArrayList<>();
        ProductModel productModel = new ProductModel();
        try {
            productModel = Core.getRequestObject(requestMessage, ProductModel.class);
            productModel.setBusinessID(requestMessage.businessID);
            lstVMProductView = this.productBllManager.getAllProductView(productModel);

            responseMessage.responseObj = lstVMProductView;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage searchUnsavedProductList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.roundingBllManager = new RoundingBllManager();
        List<VMProductView> lstVMProductView = new ArrayList<>();
        List<VMProductView> lstConvertedVMProductView = new ArrayList<>();
        VMItemListingReportQuery vmItemListingReportQuery;
        ProductModel productModel = new ProductModel();
        RoundingModel roundingModel;
        boolean bothActiveAndInactive = false;
        Integer numberOfDigitAfterDecimalPoint = 2;
        try {

            roundingModel = this.roundingBllManager.getRoundingSetting(requestMessage.businessID);
            if (roundingModel != null) {
                numberOfDigitAfterDecimalPoint = roundingModel.getNumberOfDigitAfterDecimalPoint();
            }

            vmItemListingReportQuery = Core.getRequestObject(requestMessage, VMItemListingReportQuery.class);
            //set filters
            if (vmItemListingReportQuery.getStatus() != null) {
                if (vmItemListingReportQuery.getStatus().intValue() == TillBoxAppEnum.Status.Active.get() || vmItemListingReportQuery.getStatus().intValue() == TillBoxAppEnum.Status.Inactive.get()) {
                    productModel.setStatus(vmItemListingReportQuery.getStatus());
                } else {
                    bothActiveAndInactive = true;
                }
            }
            if (vmItemListingReportQuery.getItemType() != null) {
                if (vmItemListingReportQuery.getItemType().intValue() == InventoryProductType.Physical.get() || vmItemListingReportQuery.getItemType().intValue() == InventoryProductType.Service.get()) {
                    productModel.setProductTypeID(vmItemListingReportQuery.getItemType());
                }
                //else both
            }
            if (vmItemListingReportQuery.getPriceListID() != null) {
                productModel.setProductCategoryID(vmItemListingReportQuery.getPriceListID());
            }
            productModel.setBusinessID(requestMessage.businessID);

            //get item report
            if (bothActiveAndInactive == false) {
                lstVMProductView = this.productBllManager.getAllProductView(productModel);
            } else {
                //active
                productModel.setStatus(TillBoxAppEnum.Status.Active.get());
                lstVMProductView = this.productBllManager.getAllProductView(productModel);
                //inactive
                productModel.setStatus(TillBoxAppEnum.Status.Inactive.get());
                lstVMProductView.addAll(this.productBllManager.getAllProductView(productModel));
            }

            int adjustmentType = vmItemListingReportQuery.getAdjustmentType().intValue();
            if (vmItemListingReportQuery.getRoundingType() == null) {
                vmItemListingReportQuery.setRoundingType(RoundingType.RoundDown.get());
            }

            //modify report
            for (VMProductView vmProductView : lstVMProductView) {
                VMProductView vmSavedProductView = new VMProductView();
                vmSavedProductView = vmProductView;
                if (adjustmentType == ProductAdjustmentType.Selling_Prices_based_on_Selling_Price.get()) {
                    if (vmItemListingReportQuery.getEffectType().intValue() == EffectType.Increase.get()) {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost + (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost + (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice + (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice + (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    } else {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost - (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost - (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice - (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice - (vmProductView.exclusiveSellingPrice * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    }
                } else if (adjustmentType == ProductAdjustmentType.Selling_Prices_based_on_Average_Cost.get()) {
                    if (vmItemListingReportQuery.getEffectType().intValue() == EffectType.Increase.get()) {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost + (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost + (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice + (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice + (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    } else {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost - (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost - (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice - (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice - (vmProductView.averageCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    }
                } else if (adjustmentType == ProductAdjustmentType.Selling_Prices_based_on_Last_Cost.get()) {
                    if (vmItemListingReportQuery.getEffectType().intValue() == EffectType.Increase.get()) {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost + (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost + (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice + (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice + (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    } else {
                        vmSavedProductView.averageCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.averageCost - (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.lastCost = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.lastCost - (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.exclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.exclusiveSellingPrice - (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                        vmSavedProductView.inclusiveSellingPrice = RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmItemListingReportQuery.getRoundingNearestTo(), vmItemListingReportQuery.getRoundingType(), vmProductView.inclusiveSellingPrice - (vmProductView.lastCost * vmItemListingReportQuery.getAdjustmentPercentage() / 100));
                    }
                }
                lstConvertedVMProductView.add(vmSavedProductView);
            }
            responseMessage.responseObj = lstConvertedVMProductView;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getVMProduct(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {

            VMProduct vmProduct = new VMProduct();
            ProductModel productModel = new ProductModel();
            ProductModel searchProductModel = new ProductModel();
            searchProductModel = Core.getRequestObject(requestMessage, ProductModel.class);
            searchProductModel.setBusinessID(requestMessage.businessID);

            vmProduct = this.productBllManager.getFilteredVMProduct(searchProductModel).get(0);


            VMProduct vmIntermoduleProduct = this.checkInterComForGetProduct(requestMessage, vmProduct);

            vmProduct.lstRememberNoteModels = vmIntermoduleProduct.lstRememberNoteModels;
            vmProduct.lstUserDefineSettingDetailModels = vmIntermoduleProduct.lstUserDefineSettingDetailModels;

            responseMessage.responseObj = vmProduct;
        } catch (Exception ex) {
            log.error("ProductServiceManager -> searchProduct got exception");
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


    public VMProduct checkInterComForGetProduct(RequestMessage requestMessage, VMProduct vmProduct) {

        MqttClient mqttClientSaveRememberNote, mqttClientSaveUserDefineSettingDetail;
        CallBack callBackSaveRememberNote, callBackSaveUserDefineSettingDetail;

        ResponseMessage responseMessage;
        VMProduct vmFilterProduct = new VMProduct();

        RequestMessage reqRememberNoteSaveMessage, reqUserDefineSettingDetailSaveMessage;

        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();
            reqRememberNoteSaveMessage = Core.getDefaultWorkerRequestMessage();
            reqUserDefineSettingDetailSaveMessage = Core.getDefaultWorkerRequestMessage();

            reqRememberNoteSaveMessage.token = requestMessage.token;
            reqUserDefineSettingDetailSaveMessage.token = requestMessage.token;

            String commonTopic = WorkerSubscriptionConstants.WORKER_COMMON_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(2, lockObject);


            RememberNoteModel rememberNoteModel = new RememberNoteModel();
            rememberNoteModel.setReferenceType(RememberNoteReferenceType.Product.get());
            rememberNoteModel.setReferenceID(vmProduct.productModel.getProductID());

            reqRememberNoteSaveMessage.requestObj = rememberNoteModel;
            reqRememberNoteSaveMessage.token = Core.requestToken.get();
            reqRememberNoteSaveMessage.brokerMessage.serviceName = "api/commonModule/rememberNote/search";

            SubscriberForWorker subForRememberNoteWorker = new SubscriberForWorker(reqRememberNoteSaveMessage.brokerMessage.messageId, this.barrier);
            mqttClientSaveRememberNote = subForRememberNoteWorker.subscribe();
            callBackSaveRememberNote = subForRememberNoteWorker.getCallBack();
            PublisherForWorker pubForWorkerRememberNoteList = new PublisherForWorker(commonTopic, mqttClientSaveRememberNote);
            pubForWorkerRememberNoteList.publishedMessageToWorker(reqRememberNoteSaveMessage);


            nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel userDefineSettingDetailModel = new nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel();
            userDefineSettingDetailModel.setReferenceType(UserDefineSettingReferenceType.Product.get());
            userDefineSettingDetailModel.setReferenceID(vmProduct.productModel.getProductID());

            reqUserDefineSettingDetailSaveMessage.requestObj = userDefineSettingDetailModel;
            reqUserDefineSettingDetailSaveMessage.token = Core.requestToken.get();
            reqUserDefineSettingDetailSaveMessage.brokerMessage.serviceName = "api/commonModule/userDefineSettingDetail/search";

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

                    responseMessage = callBackSaveRememberNote.getResponseMessage();
                    List<RememberNoteModel> lstRememberNoteModel = new ArrayList<>();
                    lstRememberNoteModel = (List) responseMessage.responseObj;
                    vmFilterProduct.lstRememberNoteModels = lstRememberNoteModel;


                    responseMessage = callBackSaveUserDefineSettingDetail.getResponseMessage();
                    List<UserDefineSettingDetailModel> userDefineSettingDetails = new ArrayList<>();
                    userDefineSettingDetails = (List) responseMessage.responseObj;
                    vmFilterProduct.lstUserDefineSettingDetailModels = userDefineSettingDetails;


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
        return vmFilterProduct;
    }

}
