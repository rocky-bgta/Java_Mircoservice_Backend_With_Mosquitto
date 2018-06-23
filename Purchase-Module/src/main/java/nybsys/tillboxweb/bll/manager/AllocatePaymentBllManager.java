///**
// * Created By: Md. Nazmus Salahin
// * Created Date: 05-Mar-18
// * Time: 4:14 PM
// * Modified By:
// * Modified date:
// * (C) CopyRight Nybsys ltd.
// */
//
//package nybsys.tillboxweb.bll.manager;
//
//import nybsys.tillboxweb.BaseBll;
//import nybsys.tillboxweb.Core;
//import nybsys.tillboxweb.MessageModel.BllResponseMessage;
//import nybsys.tillboxweb.MessageModel.RequestMessage;
//import nybsys.tillboxweb.appenum.TillBoxAppEnum;
//import nybsys.tillboxweb.constant.MessageConstant;
//import nybsys.tillboxweb.constant.TillBoxAppConstant;
//import nybsys.tillboxweb.coreEnum.PartyType;
//import nybsys.tillboxweb.coreEnum.ReferenceType;
//import nybsys.tillboxweb.coreUtil.CoreUtils;
//import nybsys.tillboxweb.entities.AllocatePayment;
//import nybsys.tillboxweb.enumpurches.PaymentStatus;
//import nybsys.tillboxweb.models.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@SuppressWarnings("ALL")
//@Service
//@Scope(BeanDefinition.SCOPE_PROTOTYPE)
//public class AllocatePaymentBllManager extends BaseBll<AllocatePayment> {
//
//    private static final Logger log = LoggerFactory.getLogger(AllocatePaymentBllManager.class);
//
//    @Override
//    protected void initEntityModel() {
//        Core.runTimeModelType.remove();
//        Core.runTimeEntityType.remove();
//        Core.runTimeEntityType.set(AllocatePayment.class);
//        Core.runTimeModelType.set(AllocatePaymentModel.class);
//    }
//
//
//    @Autowired
//    private SupplierInvoiceBllManager supplierInvoiceBllManager;// = new SupplierInvoiceBllManager();
//
//    public BllResponseMessage saveOrUpdate(RequestMessage requestMessage) throws Exception {
//        this.supplierInvoiceBllManager = new SupplierInvoiceBllManager();
//
//        VMAllocationPayment vmAllocationPaymentModel = Core.getRequestObject(requestMessage, VMAllocationPayment.class);
//
//        List<AllocatePaymentModel> allocatePaymentModelList;
//
//        // Extract data from VM
//        allocatePaymentModelList = vmAllocationPaymentModel.allocatePaymentModelList;
//
//        List<AllocatePaymentModel> saveOrUpdatedAllocatePaymentModelList;
//        List<AllocatePaymentModel> allocatePaymentModelListForSequence;
//        List<SupplierInvoiceModel> supplierInvoiceModelList;
//        List<AllocatePaymentModel> previousAllocatePaymentModelList;
//
//        SupplierInvoiceModel whereConditionSupplierInvoiceModel, temSupplierInvoiceModel;
//
//        AllocatePaymentModel whereConditionAllocateReceiveModel;
//
//        String hsql, buildDbSequence, currentDBSequence = null;
//
//        Double previousPaymentTotal = 0.0, currentPayment = 0.0;
//
//        Integer primaryKeyValue;
//        AllocatePaymentModel savedAllocatePaymentModel, updatedAllocatePaymentModel;
//
//        BllResponseMessage bllResponseMessage = new BllResponseMessage();
//        try {
//
//            saveOrUpdatedAllocatePaymentModelList = new ArrayList<>();
//
//            for (AllocatePaymentModel reqAllocatePaymentModel : allocatePaymentModelList) {
//                primaryKeyValue = reqAllocatePaymentModel.getAllocatePaymentID();
//
//                if (primaryKeyValue == null || primaryKeyValue == 0) {
//                    // Save Code
//
//                    //======================= due calculation logic ==========================================================================
//                    whereConditionSupplierInvoiceModel = new SupplierInvoiceModel();
//                    whereConditionSupplierInvoiceModel.setReferenceID(reqAllocatePaymentModel.getReferenceID());
//
//                    supplierInvoiceModelList = this.supplierInvoiceBllManager.getAllByConditionWithActive(whereConditionSupplierInvoiceModel);
//
//                    for (SupplierInvoiceModel item : supplierInvoiceModelList) {
//                        whereConditionAllocateReceiveModel = new AllocatePaymentModel();
//                        whereConditionAllocateReceiveModel.setReferenceID(item.getReferenceID());
//                        previousAllocatePaymentModelList = this.getAllByConditionWithActive(whereConditionAllocateReceiveModel);
//
//                        for (AllocatePaymentModel preAllocatePaymentModel : previousAllocatePaymentModelList) {
//                            previousPaymentTotal += preAllocatePaymentModel.getAmount();
//                        }
//                        currentPayment = currentPayment + previousPaymentTotal;
//                        currentPayment = currentPayment + reqAllocatePaymentModel.getAmount();
//
//                        if (item.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
//                            temSupplierInvoiceModel = item;
//                            temSupplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
//                            this.supplierInvoiceBllManager.update(temSupplierInvoiceModel);
//                        } else if (item.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
//                            temSupplierInvoiceModel = item;
//                            temSupplierInvoiceModel.setPaymentStatus(PaymentStatus.Partial.get());
//                            this.supplierInvoiceBllManager.update(temSupplierInvoiceModel);
//                        } else if (item.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
//                            bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                            Core.clientMessage.get().userMessage = "Invoice amount less than Allocate amount!";
//                            Core.clientMessage.get().message = "Invoice amount less than Allocate amount!";
//                            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                            return bllResponseMessage;
//                        }
//                        currentPayment = 0.0;
//                        previousPaymentTotal = 0.0;
//                    }
//                    //======================= End Due calculation logic ======================================================================
//
//
//                    // ============================= Create PMT0000001 =============================
//                    hsql = hsql = "SELECT e FROM AllocatePayment e ORDER BY e.allocatePaymentID DESC";
//                    allocatePaymentModelListForSequence = this.executeHqlQuery(hsql, AllocatePaymentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
//                    if (allocatePaymentModelListForSequence.size() > 0) {
//                        currentDBSequence = allocatePaymentModelListForSequence.get(0).getAllocatePaymentNo();
//                    }
//                    buildDbSequence = CoreUtils.getSequence(currentDBSequence, "ALP");
//                    // ==========================End Create PMT0000001 =============================
//
//
//                    reqAllocatePaymentModel.setAllocatePaymentNo(buildDbSequence);
//
//                    savedAllocatePaymentModel = this.save(reqAllocatePaymentModel);
//
//                    //Hold data for return
//                    saveOrUpdatedAllocatePaymentModelList.add(savedAllocatePaymentModel);
//
//                    if (saveOrUpdatedAllocatePaymentModelList.size() > 0) {
//                        Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//                        Core.clientMessage.get().userMessage = "Allocate Payment Save Successfully";
//                    } else {
//                        Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                        Core.clientMessage.get().message = "Failed to Save Allocate Payment";
//                    }
//                } else {
//                    // Update Code
//                    //======================= due calculation logic ==========================================================================
//                    whereConditionSupplierInvoiceModel = new SupplierInvoiceModel();
//                    whereConditionSupplierInvoiceModel.setReferenceID(reqAllocatePaymentModel.getReferenceID());
//
//                    supplierInvoiceModelList = this.supplierInvoiceBllManager.getAllByConditionWithActive(whereConditionSupplierInvoiceModel);
//
//                    for (SupplierInvoiceModel item : supplierInvoiceModelList) {
//                        whereConditionAllocateReceiveModel = new AllocatePaymentModel();
//                        whereConditionAllocateReceiveModel.setReferenceID(item.getReferenceID());
//                        previousAllocatePaymentModelList = this.getAllByConditionWithActive(whereConditionAllocateReceiveModel);
//
//                        for (AllocatePaymentModel preAllocatePaymentModel : previousAllocatePaymentModelList) {
//                            if (primaryKeyValue == preAllocatePaymentModel.getAllocatePaymentID())
//                                continue;
//                            else
//                                previousPaymentTotal += preAllocatePaymentModel.getAmount();
//                        }
//                        currentPayment = currentPayment + previousPaymentTotal;
//                        currentPayment = currentPayment + reqAllocatePaymentModel.getAmount();
//
//                        if (item.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
//                            temSupplierInvoiceModel = item;
//                            temSupplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
//                            this.supplierInvoiceBllManager.update(temSupplierInvoiceModel);
//                        } else if (item.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
//                            temSupplierInvoiceModel = item;
//                            temSupplierInvoiceModel.setPaymentStatus(PaymentStatus.Partial.get());
//                            this.supplierInvoiceBllManager.update(temSupplierInvoiceModel);
//                        } else if (item.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
//                            bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                            Core.clientMessage.get().userMessage = "Invoice amount less than Allocate amount!";
//                            Core.clientMessage.get().message = "Invoice amount less than Allocate amount!";
//                            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                            return bllResponseMessage;
//                        }
//                        currentPayment = 0.0;
//                        previousPaymentTotal = 0.0;
//                    }
//                    //======================= End Due calculation logic ======================================================================
//
//                    updatedAllocatePaymentModel = this.update(reqAllocatePaymentModel);
//                    //Hold data for return
//
//                    saveOrUpdatedAllocatePaymentModelList.add(updatedAllocatePaymentModel);
//
//                    if (saveOrUpdatedAllocatePaymentModelList.size() > 0) {
//                        Core.clientMessage.get().userMessage = "Allocate Payment Update Successfully";
//                        Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//                    } else {
//                        Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                        Core.clientMessage.get().message = "Failed to Update AllocatePayment";
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> saveOrUpdate got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//
//        //prepere VM for return
//        vmAllocationPaymentModel.allocatePaymentModelList = saveOrUpdatedAllocatePaymentModelList;
//
//        bllResponseMessage.responseObject = vmAllocationPaymentModel;
//        bllResponseMessage.responseCode = Core.clientMessage.get().messageCode;
//        bllResponseMessage.message = Core.clientMessage.get().message;
//
//        return bllResponseMessage;
//    }
//
//    public boolean updateAllocatePaymentListForSupplierPaymentAllocation(List<AllocatePaymentModel> lstNewAllocatePaymentModel, Integer supplierID, Integer supplierInvoiceID, Integer businessID) throws Exception {
//        AllocatePaymentModel whereCondition = new AllocatePaymentModel();
//        List<AllocatePaymentModel> lstSavedAllocatePaymentModel;
//        List<AllocatePaymentModel> lstAllocatePaymentModel  = new ArrayList<>();
//        try {
//            whereCondition.setReferenceID(supplierInvoiceID);
//            whereCondition.setBusinessID(businessID);
//            lstSavedAllocatePaymentModel = this.getAllByConditionWithActive(whereCondition);
//
//            //(1) delete those which is not in new list
//            boolean foundFlag = false;
//            for (AllocatePaymentModel savedAllocatePaymentModel : lstSavedAllocatePaymentModel) {
//                for (AllocatePaymentModel newAllocatePaymentModel : lstNewAllocatePaymentModel) {
//                    if (savedAllocatePaymentModel.getPaymentID().intValue() == newAllocatePaymentModel.getPaymentID().intValue()) {
//                        foundFlag = true;
//                    }
//                }
//                if (foundFlag == false) {
//                    this.softDelete(savedAllocatePaymentModel);
//                }
//            }
//            //(2) save or update
//            for (AllocatePaymentModel newAllocatePaymentModel : lstNewAllocatePaymentModel) {
//                //update
//                if (newAllocatePaymentModel.getAllocatePaymentID() != null) {
//                    newAllocatePaymentModel.setAmount(newAllocatePaymentModel.getAmount());
//                    this.update(newAllocatePaymentModel);
//                } else {
//                    //save
//                    newAllocatePaymentModel.setBusinessID(businessID);
//                    newAllocatePaymentModel.setReferenceType(PartyType.Supplier.get());
//                    newAllocatePaymentModel.setReferenceID(supplierID);
//
//                    // ============================= Create AlP0000001 =============================
//                    String currentDBSequence = null , buildDbSequence, hsql;
//                    hsql = "SELECT ap FROM AllocatePayment ap ORDER BY ap.allocatePaymentID DESC";
//
//                    lstAllocatePaymentModel = this.executeHqlQuery(hsql, AllocatePaymentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
//                    if (lstAllocatePaymentModel.size() > 0) {
//                        currentDBSequence = lstAllocatePaymentModel.get(0).getAllocatePaymentNo();
//                    }
//                    buildDbSequence = CoreUtils.getSequence(currentDBSequence,"AlP");
//                    // ==========================End Create AlP0000001 =============================
//
//                    newAllocatePaymentModel.setAllocatePaymentNo(buildDbSequence);
//
//                    this.save(newAllocatePaymentModel);
//
//                    lstAllocatePaymentModel = new ArrayList<>();
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> updateAllocatePaymentListForSupplierPaymentAllocation got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//
//        return true;
//    }
//
//    public List<AllocatePaymentModel> search(Integer supplierID, Integer invoiceID, Integer businessID) throws Exception {
//        List<AllocatePaymentModel> lstAllocatePaymentModel = new ArrayList<>();
//        AllocatePaymentModel allocatePaymentModel = new AllocatePaymentModel();
//        try {
//            allocatePaymentModel.setBusinessID(businessID);
//            allocatePaymentModel.setReferenceID(invoiceID);
//            allocatePaymentModel.setReferenceID(supplierID);
//            allocatePaymentModel.setReferenceType(ReferenceType.SupplierPayment.get());
//
//            lstAllocatePaymentModel = this.getAllByConditionWithActive(allocatePaymentModel);
//            if (lstAllocatePaymentModel.size() == 0) {
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                Core.clientMessage.get().message = MessageConstant.FAILED_TO_GET_ALLOCATE_PAYMENT;
//            }
//
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> search got exception: " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return lstAllocatePaymentModel;
//    }
//
//    public List<AllocatePaymentModel> getAllByRequestModel() throws Exception {
//        List<AllocatePaymentModel> resultingList;
//        try {
//            resultingList = this.getAll();
//            if (resultingList.size() > 0) {
//                //Core.clientMessage.get().userMessage = "Get all AllocatePayment successfully";
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//            } else {
//                Core.clientMessage.get().message = "Failed to get AllocatePayment";
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//            }
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> getAllModels got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return resultingList;
//    }
//
//    public Integer deleteByConditions(RequestMessage requestMessage) throws Exception {
//        AllocatePaymentModel castRequestModel =
//                Core.getRequestObject(requestMessage, AllocatePaymentModel.class);
//        Integer numberOfDeleteRow = 0;
//        try {
//            numberOfDeleteRow = this.deleteByConditions(castRequestModel);
//            if (numberOfDeleteRow > 0) {
//                //Core.clientMessage.get().userMessage = "Successfully deleted the requested AllocatePayment";
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//            } else {
//                Core.clientMessage.get().message = "Failed to deleted the requested AllocatePayment";
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//            }
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> deleteByConditions got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return numberOfDeleteRow;
//    }
//
//    public Integer inactiveByConditions(RequestMessage requestMessage) throws Exception {
//        return null;
//    }
//
//    public AllocatePaymentModel inActive(RequestMessage requestMessage) throws Exception {
//        AllocatePaymentModel castRequestModel =
//                Core.getRequestObject(requestMessage, AllocatePaymentModel.class);
//        AllocatePaymentModel processedModel = null;
//        try {
//            if (castRequestModel != null) {
//                processedModel = this.inActive(castRequestModel);
//                if (processedModel != null) {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//                    //Core.clientMessage.get().userMessage = "Successfully inactive the requested AllocatePayment";
//                } else {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                    Core.clientMessage.get().message = "Failed to inactive the requested AllocatePayment";
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> inActive got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return processedModel;
//    }
//
//    public AllocatePaymentModel delete(RequestMessage requestMessage) throws Exception {
//        AllocatePaymentModel castRequestModel =
//                Core.getRequestObject(requestMessage, AllocatePaymentModel.class);
//        AllocatePaymentModel processedModel = null;
//        try {
//            if (castRequestModel != null) {
//                processedModel = this.softDelete(castRequestModel);
//                if (processedModel != null) {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//                    //Core.clientMessage.get().userMessage = "Successfully deleted the requested AllocatePayment";
//                } else {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                    Core.clientMessage.get().message = "Failed to deleted the requested AllocatePayment";
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> delete got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return processedModel;
//    }
//
//    public AllocatePaymentModel getByRequestId(RequestMessage requestMessage) throws Exception {
//        AllocatePaymentModel castRequestModel =
//                Core.getRequestObject(requestMessage, AllocatePaymentModel.class);
//        Integer primaryKeyValue = castRequestModel.getAllocatePaymentID();
//        AllocatePaymentModel processedModel = null;
//        try {
//            if (primaryKeyValue != null) {
//                processedModel = this.getById(primaryKeyValue);
//                if (processedModel != null) {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
//                    //Core.clientMessage.get().userMessage = "Get the requested AllocatePayment successfully";
//                } else {
//                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                    Core.clientMessage.get().message = "Failed to the requested AllocatePayment";
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("AllocatePaymentBllManager -> getByID got exception : " + ex.getMessage());
//            for (Throwable throwable : ex.getSuppressed()) {
//                log.error("suppressed: " + throwable);
//            }
//            throw ex;
//        }
//        return processedModel;
//    }
//
//    /*
//
//    private ResponseMessage journalEntry(AllocatePaymentModel modelForJournalEntry) throws Exception {
//        List<JournalModel> lstJournalModel = new ArrayList<>();
//        JournalModel drJournalModel = new JournalModel();
//        JournalModel crJournalModel = new JournalModel();
//
//
//        if(modelForJournalEntry.getAdjustmentType() == Adjustment.Decrease.get()){
//            drJournalModel.setAccountID(DefaultCOA.AdjustmentLoss.get());
//            drJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
//            drJournalModel.setDrCrIndicator(DebitCreditIndicator.Debit.get());
//            drJournalModel.setNote(modelForJournalEntry.getReason());
//            drJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
//            drJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
//
//            crJournalModel.setAccountID(DefaultCOA.Inventory.get());
//            crJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
//            crJournalModel.setDrCrIndicator(DebitCreditIndicator.Credit.get());
//            crJournalModel.setNote(modelForJournalEntry.getReason());
//            crJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
//            crJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
//        }
//
//        if(modelForJournalEntry.getAdjustmentType() == Adjustment.Increase.get()){
//            drJournalModel.setAccountID(DefaultCOA.Inventory.get());
//            drJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
//            drJournalModel.setDrCrIndicator(DebitCreditIndicator.Debit.get());
//            drJournalModel.setNote(modelForJournalEntry.getReason());
//            drJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
//            drJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
//
//            crJournalModel.setAccountID(DefaultCOA.AdjustmentIncome.get());
//            crJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
//            crJournalModel.setDrCrIndicator(DebitCreditIndicator.Credit.get());
//            crJournalModel.setNote(modelForJournalEntry.getReason());
//            crJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
//            crJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
//        }
//
//        // add double entry
//        lstJournalModel.add(drJournalModel);
//        lstJournalModel.add(crJournalModel);
//        VMJournalListModel vmJournalListModel = new VMJournalListModel();
//
//        //================== inter-module communication for journal save ===========================
//        boolean workCompleteWithInAllowTime;
//        ResponseMessage responseMessage;
//        RequestMessage requestMessage;
//        CallBack callBackForJournal;
//        MqttClient mqttClientForJournal;
//        Object lockObject = new Object();
//        String pubTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
//        this.barrier = TillBoxUtils.getBarrier(1, lockObject);
//
//        //======================= Start of one ===========================
//        requestMessage = Core.getDefaultWorkerRequestMessage();
//        requestMessage.brokerMessage.serviceName = "api/journal/save";
//        vmJournalListModel.lstJournalModel = lstJournalModel;
//        requestMessage.requestObject =  vmJournalListModel;
//        SubscriberForWorker subForWorker = new SubscriberForWorker(requestMessage.brokerMessage.messageId, this.barrier);
//        mqttClientForJournal = subForWorker.subscribe();
//        callBackForJournal = subForWorker.getCallBack();
//        PublisherForWorker pubForWorkerToSaveJournal = new PublisherForWorker(pubTopic, mqttClientForJournal);
//        pubForWorkerToSaveJournal.publishedMessageToWorker(requestMessage);
//        //======================= End of one ===========================
//
//        synchronized (lockObject) {
//            long startTime = System.nanoTime();
//            lockObject.wait(this.allowedTime);
//            workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);
//            if (workCompleteWithInAllowTime) {
//                responseMessage = callBackForJournal.getResponseMessage();
//            } else {
//                //timeout
//                throw new Exception("Response not get within allowed time");
//            }
//        }
//        this.closeBrokerClient(mqttClientForJournal, requestMessage.brokerMessage.messageId);
//        return responseMessage;
//    }
//
//    */
//}