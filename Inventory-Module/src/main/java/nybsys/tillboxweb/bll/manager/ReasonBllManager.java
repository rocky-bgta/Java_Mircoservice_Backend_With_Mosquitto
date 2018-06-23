/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 18-May-18
 * Time: 12:10 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.BllResponseMessage;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.entities.Reason;
import nybsys.tillboxweb.models.ReasonModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReasonBllManager extends BaseBll<Reason> {

    private static final Logger log = LoggerFactory.getLogger(ReasonBllManager.class);

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Reason.class);
        Core.runTimeModelType.set(ReasonModel.class);
    }

    public BllResponseMessage saveOrUpdate(RequestMessage requestMessage) throws Exception {
        BllResponseMessage bllResponseMessage = new BllResponseMessage();

        ReasonModel reqReasonModel =
                Core.getRequestObject(requestMessage, ReasonModel.class);

        Integer primaryKeyValue = reqReasonModel.getReasonID();
        ReasonModel savedReasonModel = null, updatedReasonModel;
        JournalModel drJournalModel, crJournalModel;
        List<JournalModel> journalModelList = new ArrayList<>();
        Boolean isJournalEntrySuccess;

        try {

            if (primaryKeyValue == null || primaryKeyValue == 0) {
                // Save Code
                savedReasonModel = this.save(reqReasonModel);
                if (savedReasonModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Reason Save Successfully";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Save Reason";
                }
            } else {
                // Update Code
                updatedReasonModel = this.update(reqReasonModel);
                if (updatedReasonModel != null) {
                    Core.clientMessage.get().userMessage = "Reason Update Successfully";
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Update Reason";
                }

                savedReasonModel = updatedReasonModel;
            }

        } catch (Exception ex) {
            log.error("ReasonBllManager -> saveOrUpdate got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        bllResponseMessage.responseObject = savedReasonModel;
        bllResponseMessage.responseCode = Core.clientMessage.get().messageCode;
        bllResponseMessage.message = Core.clientMessage.get().message;

        return bllResponseMessage;
    }

    public List<ReasonModel> search(ReasonModel reqReasonModel) throws Exception {
        List<ReasonModel> findReasonList;
        try {
            findReasonList = this.getAllByConditions(reqReasonModel);
            if (findReasonList.size() > 0) {
                Core.clientMessage.get().userMessage = "Find the request Reason";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                Core.clientMessage.get().message = "Failed to find the requested Reason";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("ReasonBllManager -> search got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return findReasonList;
    }

    public Integer deleteByConditions(RequestMessage requestMessage) throws Exception {
        ReasonModel req_ReasonModel =
                Core.getRequestObject(requestMessage, ReasonModel.class);
        Integer numberOfDeleteRow = 0;
        try {
            numberOfDeleteRow = this.deleteByConditions(req_ReasonModel);
            if (numberOfDeleteRow > 0) {
                //Core.clientMessage.get().userMessage = "Successfully deleted the requested Reason";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                Core.clientMessage.get().message = "Failed to deleted the requested Reason";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("ReasonBllManager -> deleteByConditions got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return numberOfDeleteRow;
    }


    public ReasonModel inActive(RequestMessage requestMessage) throws Exception {
        ReasonModel reqReasonModel =
                Core.getRequestObject(requestMessage, ReasonModel.class);
        ReasonModel _ReasonModel = null;
        try {
            if (reqReasonModel != null) {
                _ReasonModel = this.inActive(reqReasonModel);
                if (_ReasonModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Successfully inactive the requested Reason";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to inactive the requested Reason";
                }
            }

        } catch (Exception ex) {
            log.error("ReasonBllManager -> inActive got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return _ReasonModel;
    }


    public ReasonModel delete(ReasonModel reqReasonModel) throws Exception {
        ReasonModel deletedReasonModel = null;
        try {
            if (reqReasonModel != null) {
                deletedReasonModel = this.softDelete(reqReasonModel);
                if (deletedReasonModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    //Core.clientMessage.get().userMessage = "Successfully deleted the requested Reason";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to deleted the requested Reason";
                }
            }

        } catch (Exception ex) {
            log.error("ReasonBllManager -> delete got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return deletedReasonModel;
    }

    public ReasonModel getByReqId(ReasonModel reqReasonModel) throws Exception {
        Integer primaryKeyValue = reqReasonModel.getReasonID();
        ReasonModel foundReasonModel = null;
        try {
            if (primaryKeyValue != null) {
                foundReasonModel = this.getByIdActiveStatus(primaryKeyValue);
                if (foundReasonModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    //Core.clientMessage.get().userMessage = "Get the requested Reason successfully";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to the requested Reason";
                }
            }

        } catch (Exception ex) {
            log.error("ReasonBllManager -> getByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return foundReasonModel;
    }
    
    /*
    
    private ResponseMessage journalEntry(ReasonModel modelForJournalEntry) throws Exception {
        List<JournalModel> lstJournalModel = new ArrayList<>();
        JournalModel drJournalModel = new JournalModel();
        JournalModel crJournalModel = new JournalModel();
       

        if(modelForJournalEntry.getAdjustmentType() == Adjustment.Decrease.get()){
            drJournalModel.setAccountID(DefaultCOA.AdjustmentLoss.get());
            drJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
            drJournalModel.setDrCrIndicator(DebitCreditIndicator.Debit.get());
            drJournalModel.setNote(modelForJournalEntry.getReason());
            drJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
            drJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());

            crJournalModel.setAccountID(DefaultCOA.Inventory.get());
            crJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
            crJournalModel.setDrCrIndicator(DebitCreditIndicator.Credit.get());
            crJournalModel.setNote(modelForJournalEntry.getReason());
            crJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
            crJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
        }

        if(modelForJournalEntry.getAdjustmentType() == Adjustment.Increase.get()){
            drJournalModel.setAccountID(DefaultCOA.Inventory.get());
            drJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
            drJournalModel.setDrCrIndicator(DebitCreditIndicator.Debit.get());
            drJournalModel.setNote(modelForJournalEntry.getReason());
            drJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
            drJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());

            crJournalModel.setAccountID(DefaultCOA.AdjustmentIncome.get());
            crJournalModel.setAmount(modelForJournalEntry.getTotalPrice());
            crJournalModel.setDrCrIndicator(DebitCreditIndicator.Credit.get());
            crJournalModel.setNote(modelForJournalEntry.getReason());
            crJournalModel.setReferenceID(modelForJournalEntry.getProductAdjustmentID());
            crJournalModel.setReferenceType(ReferenceType.ProductAdjustment.get());
        }

        // add double entry
        lstJournalModel.add(drJournalModel);
        lstJournalModel.add(crJournalModel);
        VMJournalListModel vmJournalListModel = new VMJournalListModel();

        //================== inter-module communication for journal save ===========================
        boolean workCompleteWithInAllowTime;
        ResponseMessage responseMessage;
        RequestMessage requestMessage;
        CallBack callBackForJournal;
        MqttClient mqttClientForJournal;
        Object lockObject = new Object();
        String pubTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;
        this.barrier = TillBoxUtils.getBarrier(1, lockObject);

        //======================= Start of one ===========================
        requestMessage = Core.getDefaultWorkerRequestMessage();
        requestMessage.brokerMessage.serviceName = "api/journal/save";
        vmJournalListModel.lstJournalModel = lstJournalModel;
        requestMessage.requestObject =  vmJournalListModel;
        SubscriberForWorker subForWorker = new SubscriberForWorker(requestMessage.brokerMessage.messageId, this.barrier);
        mqttClientForJournal = subForWorker.subscribe();
        callBackForJournal = subForWorker.getCallBack();
        PublisherForWorker pubForWorkerToSaveJournal = new PublisherForWorker(pubTopic, mqttClientForJournal);
        pubForWorkerToSaveJournal.publishedMessageToWorker(requestMessage);
        //======================= End of one ===========================

        synchronized (lockObject) {
            long startTime = System.nanoTime();
            lockObject.wait(this.allowedTime);
            workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);
            if (workCompleteWithInAllowTime) {
                responseMessage = callBackForJournal.getResponseMessage();
            } else {
                //timeout
                throw new Exception("Response not get within allowed time");
            }
        }
        this.closeBrokerClient(mqttClientForJournal, requestMessage.brokerMessage.messageId);
        return responseMessage;
    }
    
    */

}