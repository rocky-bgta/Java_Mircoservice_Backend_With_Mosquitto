/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 26/02/2018
 * Time: 04:41
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.BllResponseMessage;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreBllManager.CoreCurrencyBllManager;
import nybsys.tillboxweb.coreBllManager.CoreDocumentNumberBllManager;
import nybsys.tillboxweb.coreBllManager.CoreJournalBllManager;
import nybsys.tillboxweb.coreBllManager.CoreVATRateBllManager;
import nybsys.tillboxweb.coreEnum.*;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.coreModels.DocumentNumberModel;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.coreUtil.CoreUtils;
import nybsys.tillboxweb.entities.SupplierAdjustment;
import nybsys.tillboxweb.enumpurches.AdjustmentEffect;
import nybsys.tillboxweb.enumpurches.InvoiceSearchType;
import nybsys.tillboxweb.enumpurches.PaymentAdjustmentReferenceType;
import nybsys.tillboxweb.enumpurches.PaymentStatus;
import nybsys.tillboxweb.models.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupplierAdjustmentBllManager extends BaseBll<SupplierAdjustment> {
    private static final Logger log = LoggerFactory.getLogger(SupplierAdjustmentBllManager.class);

    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(SupplierAdjustment.class);
        Core.runTimeModelType.set(SupplierAdjustmentModel.class);
    }

    private SupplierAdjustmentDetailBllManager supplierAdjustmentDetailBllManager;// = new SupplierAdjustmentDetailBllManager();

    private CoreCurrencyBllManager coreCurrencyBllManager;

    private CoreDocumentNumberBllManager coreDocumentNumberBllManager;

    private CoreVATRateBllManager coreVATRateBllManager;

    private SupplierInvoiceBllManager supplierInvoiceBllManager = new SupplierInvoiceBllManager();

    private CoreJournalBllManager coreJournalBllManager = new CoreJournalBllManager();

    private SupplierBllManager supplierBllManager;


    public SupplierAdjustmentModel saveOrUpdate(SupplierAdjustmentModel supplierAdjustmentModelReq) throws Exception {
        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel();
        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
        try {
            supplierAdjustmentModel = supplierAdjustmentModelReq;
            //save
            if (supplierAdjustmentModel.getSupplierAdjustmentID() == null || supplierAdjustmentModel.getSupplierAdjustmentID() == 0) {
                // ============================= Create SAD0000001 =============================
                String currentDBSequence = null, buildDbSequence, hsql;
                hsql = "SELECT sa FROM SupplierAdjustment sa ORDER BY sa.supplierAdjustmentID DESC";
                lstSupplierAdjustmentModel = this.executeHqlQuery(hsql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
                if (lstSupplierAdjustmentModel.size() > 0) {
                    currentDBSequence = lstSupplierAdjustmentModel.get(0).getDocNumber();
                }
                buildDbSequence = CoreUtils.getSequence(currentDBSequence, "SAD");
                // ==========================End Create SAD0000001 =============================

                supplierAdjustmentModel.setDocNumber(buildDbSequence);
                supplierAdjustmentModel = this.save(supplierAdjustmentModel);
                if (supplierAdjustmentModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.SUPPLIER_ADJUSTMENT_SAVE_FAILED;
                }
            } else { //update

                supplierAdjustmentModel = this.update(supplierAdjustmentModel);
                if (supplierAdjustmentModel == null) {
                    supplierAdjustmentModel = null;
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.SUPPLIER_ADJUSTMENT_UPDATE_FAILED;
                }
            }

        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> saveOrUpdate got exception :" + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return supplierAdjustmentModel;
    }

    public SupplierAdjustmentModel searchSupplierAdjustmentByID(int supplierAdjustmentID, int businessID) throws Exception {
        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel();
        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
        try {
            supplierAdjustmentModel.setBusinessID(businessID);
            supplierAdjustmentModel.setSupplierAdjustmentID(supplierAdjustmentID);
            supplierAdjustmentModel.setStatus(TillBoxAppEnum.Status.Active.get());
            lstSupplierAdjustmentModel = this.getAllByConditions(supplierAdjustmentModel);
            if (lstSupplierAdjustmentModel.size() > 0) {
                supplierAdjustmentModel = lstSupplierAdjustmentModel.get(0);
            } else {
                supplierAdjustmentModel = null;
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_ADJUSTMENT_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> searchSupplierAdjustmentByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return supplierAdjustmentModel;
    }

    public List<SupplierAdjustmentModel> searchSupplierAdjustment(SupplierAdjustmentModel supplierAdjustmentModelReq) throws Exception {
        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel();
        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
        try {
            supplierAdjustmentModel = supplierAdjustmentModelReq;

            supplierAdjustmentModel.setStatus(TillBoxAppEnum.Status.Active.get());
            lstSupplierAdjustmentModel = this.getAllByConditions(supplierAdjustmentModel);
            if (lstSupplierAdjustmentModel.size() == 0) {
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_ADJUSTMENT_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> searchSupplierAdjustmentByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return lstSupplierAdjustmentModel;
    }

    public SupplierAdjustmentModel deleteSupplierAdjustmentBySupplierAdjustmentID(Integer supplierAdjustmentID) throws Exception {
        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel();
        try {
            supplierAdjustmentModel.setSupplierAdjustmentID(supplierAdjustmentID);
            supplierAdjustmentModel = this.softDelete(supplierAdjustmentModel);
            if (supplierAdjustmentModel == null) {
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_ADJUSTMENT_DELETE_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> deleteSupplierAdjustmentBySupplierAdjustmentID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return supplierAdjustmentModel;
    }

    public List<SupplierAdjustmentModel> getUnAllocatedDecreasedSupplierAdjustments(Integer supplierID, Integer businessID) throws Exception {
        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel;
        try {

            String hql = "FROM SupplierAdjustment SA WHERE SA.status = " + TillBoxAppEnum.Status.Active.get() + " AND SA.businessID = " + businessID + " AND SA.supplierID = " + supplierID + " AND SA.effectType = " + EffectType.Decrease.get() + " AND SA.unAllocatedAmount > 0";
            lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());

        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> getUnAllocatedAdjustments got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstSupplierAdjustmentModel;
    }

    public List<SupplierAdjustmentModel> getUnAllocatedSupplierAdjustments(Integer effectType, Integer supplierID, Integer businessID) throws Exception {
        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel;
        try {

            String hql = "FROM SupplierAdjustment SA WHERE SA.status = " + TillBoxAppEnum.Status.Active.get() + " AND SA.businessID = " + businessID + " AND SA.supplierID = " + supplierID + " AND SA.effectType = " + effectType + " AND  SA.exclusive " +
                    "> (SELECT COALESCE(SUM(SAD.adjustAmount),0) FROM SupplierAdjustmentDetail SAD WHERE SAD.status = " + TillBoxAppEnum.Status.Active.get() + ")";
            lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());

        } catch (Exception ex) {
            log.error("SupplierAdjustmentBllManager -> getUnAllocatedAdjustments got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstSupplierAdjustmentModel;
    }

    public List<SupplierAdjustmentModel> filterForAllocatePayment(VMAllocatePaymentReq vmAllocatePaymentReq, Integer businessID, Integer effectType) throws Exception {

        List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel();
        String hql = "";
        try {
            if (vmAllocatePaymentReq.getInvoiceType().intValue() == InvoiceSearchType.AllInvoice.get()) {

                //all
                if (vmAllocatePaymentReq.getShowOutStandingOnly() == null || vmAllocatePaymentReq.getShowOutStandingOnly() == false) {
                    supplierAdjustmentModel.setBusinessID(businessID);
                    supplierAdjustmentModel.setSupplierID(vmAllocatePaymentReq.getSelectedSupplierID());
                    supplierAdjustmentModel.setEffectType(effectType);

                    lstSupplierAdjustmentModel = this.getAllByConditionWithActive(supplierAdjustmentModel);

                } else { //un-paid/partially-paid

                    hql = "FROM SupplierAdjustment SAD WHERE SAD.status = " + TillBoxAppEnum.Status.Active.get() + " AND SAD.businessID = " + businessID + " AND SAD.supplierID = " + vmAllocatePaymentReq.getSelectedSupplierID() + " AND SAD.effectType = " + effectType + " AND (SAD.adjustmentStatus = " + PaymentStatus.Unpaid.get() + " OR SAD.adjustmentStatus = " + PaymentStatus.PartiallyPaid.get() + ")";
                    lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());
                }

            } else if (vmAllocatePaymentReq.getInvoiceType().intValue() == InvoiceSearchType.SpecificInvoiceNumber.get()) {

                if (vmAllocatePaymentReq.getShowOutStandingOnly() == null || vmAllocatePaymentReq.getShowOutStandingOnly() == false) {
                    supplierAdjustmentModel.setBusinessID(businessID);
                    supplierAdjustmentModel.setEffectType(effectType);
                    supplierAdjustmentModel.setSupplierID(vmAllocatePaymentReq.getSelectedSupplierID());
                    supplierAdjustmentModel.setDocNumber(vmAllocatePaymentReq.getDocNumber());

                    lstSupplierAdjustmentModel = this.getAllByConditionWithActive(supplierAdjustmentModel);
                } else {

                    hql = "FROM SupplierAdjustment SAD WHERE SAD.status = " + TillBoxAppEnum.Status.Active.get() + " AND SAD.businessID = " + businessID + " AND SAD.supplierID = " + vmAllocatePaymentReq.getSelectedSupplierID() + " AND SAD.effectType = " + effectType + " AND SAD.docNumber = '" + vmAllocatePaymentReq.getDocNumber() + "' AND (SAD.adjustmentStatus = " + PaymentStatus.Unpaid.get() + " OR SAD.adjustmentStatus = " + PaymentStatus.PartiallyPaid.get() + ")";
                    lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());
                }

            } else if (vmAllocatePaymentReq.getInvoiceType().intValue() == InvoiceSearchType.InvoiceBetween.get()) {

                String dateQuery = "";
                if (vmAllocatePaymentReq.getFromDate() != null) {
                    dateQuery = " AND SAD.date > '" + vmAllocatePaymentReq.getFromDate() + "'";
                }
                if (vmAllocatePaymentReq.getToDate() != null) {
                    dateQuery += " AND SAD.date < '" + vmAllocatePaymentReq.getToDate() + "'";
                }

                if (vmAllocatePaymentReq.getShowOutStandingOnly() == null || vmAllocatePaymentReq.getShowOutStandingOnly() == false) {

                    hql = "FROM SupplierAdjustment SAD WHERE SAD.status = " + TillBoxAppEnum.Status.Active.get() + " AND SAD.businessID = " + businessID + " AND SAD.supplierID = " + vmAllocatePaymentReq.getSelectedSupplierID() + " AND SAD.effectType = " + effectType + dateQuery;
                    lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());

                } else {

                    hql = "FROM SupplierAdjustment SAD WHERE SAD.status = " + TillBoxAppEnum.Status.Active.get() + " AND SAD.businessID = " + businessID + " AND SAD.supplierID = " + vmAllocatePaymentReq.getSelectedSupplierID() + " AND SAD.effectType = " + effectType + dateQuery + " AND (SAD.adjustmentStatus = " + PaymentStatus.Unpaid.get() + " OR SAD.adjustmentStatus = " + PaymentStatus.PartiallyPaid.get() + ")";
                    lstSupplierAdjustmentModel = this.executeHqlQuery(hql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.Select.get());
                }
            }

        } catch (Exception ex) {
            log.error("SupplierInvoiceBllManager -> filterForAllocatePayment got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstSupplierAdjustmentModel;
    }

    public BllResponseMessage saveSupplierAdjustmentAmount(RequestMessage requestMessage) throws Exception {
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        this.supplierAdjustmentDetailBllManager = new SupplierAdjustmentDetailBllManager();
        //this.coreVATRateBllManager = new CoreVATRateBllManager();
        this.coreDocumentNumberBllManager = new CoreDocumentNumberBllManager();
        this.coreCurrencyBllManager = new CoreCurrencyBllManager();


        VMUnPaidInvoicesModel vmUnpaidInvoicesModel;
        List<UnPaidInvoiceModel> unPaidInvoicesModelList;
        List<SupplierAdjustmentModel> supplierAdjustmentModelList;
        CurrencyModel whereConditionCurrencyModel, currencyModel;

        SupplierAdjustmentModel supplierAdjustmentModel = new SupplierAdjustmentModel(), savedSupplierAdjustmentModel = null, supplierAdjustmentModelForEdit, whereConditionSupplierAdjustmentModel;

        Double amountDue = 0.0, currentPayment, adjustAmount = 0.0;

        Double total, exclusive;

        Integer documentTypeID;

        DocumentNumberModel whereConditionDocumentNumberModel, documentNumberModel;

        String firstThreeChar;
        String buildDbSequence, currentDBSequence = null, hsql;

        SupplierAdjustmentDetailModel supplierAdjustmentDetailModel;

        //List<SupplierAdjustmentDetailModel> supplierAdjustmentDetailModelList;
        SupplierInvoiceModel supplierInvoiceModel, whereConditionSupplierInvoiceModel;


        Integer adjustmentEffectType;

        Double govtVatAmount;


        Double dueAmountTotal = 0.0, adjustAmountTotal = 0.0;

        String note;

        Boolean pageInEditState;

        Integer supplierAdjustmentIDForEdit;

        JournalModel whereConditionJournalModel;
        JournalModel journalModelToUpdate;

        try {

            JournalModel drJournalModel, crJournalModel;
            String journalReferenceNo;
            journalReferenceNo = TillBoxUtils.getUUID();

            vmUnpaidInvoicesModel = Core.getRequestObject(requestMessage, VMUnPaidInvoicesModel.class);
            unPaidInvoicesModelList = vmUnpaidInvoicesModel.unPaidInvoiceModelList;

            pageInEditState = vmUnpaidInvoicesModel.pageInEditState;
            supplierAdjustmentIDForEdit = vmUnpaidInvoicesModel.supplierAdjustmentID;
            note = vmUnpaidInvoicesModel.comment;
            adjustmentEffectType = vmUnpaidInvoicesModel.effectType;
            total = vmUnpaidInvoicesModel.total;
            exclusive = vmUnpaidInvoicesModel.exclusive;

            //unAllocateAmount = vmUnpaidInvoicesModel.unAllocateAmount;

            documentTypeID = vmUnpaidInvoicesModel.documentTypeID;
            whereConditionDocumentNumberModel = new DocumentNumberModel();
            whereConditionDocumentNumberModel.setDocumentTypeID(documentTypeID);

            documentNumberModel = this.coreDocumentNumberBllManager.getAllByConditionWithActive(whereConditionDocumentNumberModel).get(0);
            firstThreeChar = StringUtils.substring(documentNumberModel.getCurrentDocumentNumber().toString(), 0, 3);

            // ============================= Create PAY0000001 =============================
            hsql = "SELECT e FROM SupplierAdjustment e ORDER BY e.supplierAdjustmentID DESC";
            supplierAdjustmentModelList = this.executeHqlQuery(hsql, SupplierAdjustmentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
            if (supplierAdjustmentModelList.size() > 0) {
                currentDBSequence = supplierAdjustmentModelList.get(0).getDocNumber();
            }
            buildDbSequence = CoreUtils.getSequence(currentDBSequence, firstThreeChar);
            // ==========================End Create PAY0000001 =============================


            if (unPaidInvoicesModelList != null && unPaidInvoicesModelList.size() > 0) {
                for (UnPaidInvoiceModel unPaidInvoiceModel : unPaidInvoicesModelList) {

                    dueAmountTotal += (unPaidInvoiceModel.amountDue != null) ? unPaidInvoiceModel.amountDue : 0.0;
                    adjustAmountTotal += (unPaidInvoiceModel.adjustAmount != null) ? unPaidInvoiceModel.adjustAmount : 0.0;


                    if (dueAmountTotal < adjustAmountTotal) {
                        bllResponseMessage.responseObject = "";
                        bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        bllResponseMessage.message = "Adjust amount greater than total due amount";
                        return bllResponseMessage;
                    }
                }
            }


            whereConditionCurrencyModel = new CurrencyModel();
            whereConditionCurrencyModel.setBusinessID(requestMessage.businessID);
            whereConditionCurrencyModel.setBaseCurrency(true);

            currencyModel = this.coreCurrencyBllManager.getAllByConditionWithActive(whereConditionCurrencyModel).get(0);

            currencyModel.setExchangeRate(1.0);
            // set currency manually
            //get base currency and exchange rate
            /*  currencyModel = this.productServiceManager.getBaseCurrency();
            if (currencyModel == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return responseMessage;
            }

            //check entry currency is present if not base currency will be entry currency
            if(requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0)
            {
                requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
            }*/


            if (currencyModel != null) {

                if (pageInEditState) {
                    whereConditionSupplierAdjustmentModel = new SupplierAdjustmentModel();
                    whereConditionSupplierAdjustmentModel.setSupplierAdjustmentID(supplierAdjustmentIDForEdit);
                    supplierAdjustmentModelForEdit = this.getAllByConditionWithActive(whereConditionSupplierAdjustmentModel).get(0);
                    supplierAdjustmentModel = supplierAdjustmentModelForEdit;
                }

                supplierAdjustmentModel.setBaseCurrencyID(currencyModel.getCurrencyID());
                supplierAdjustmentModel.setBaseCurrencyID(currencyModel.getCurrencyID());
                supplierAdjustmentModel.setEntryCurrencyID(currencyModel.getCurrencyID());
                supplierAdjustmentModel.setExchangeRate(currencyModel.getExchangeRate());
                supplierAdjustmentModel.setAccountID(vmUnpaidInvoicesModel.coaID);
                supplierAdjustmentModel.setBusinessID(requestMessage.businessID);
                supplierAdjustmentModel.setDate(vmUnpaidInvoicesModel.paymentDate);
                supplierAdjustmentModel.setDescription(vmUnpaidInvoicesModel.description);
                supplierAdjustmentModel.setDocNumber(buildDbSequence);
                supplierAdjustmentModel.setSupplierID(vmUnpaidInvoicesModel.supplierID);
                supplierAdjustmentModel.setDocNumber(buildDbSequence);
                supplierAdjustmentModel.setReference(vmUnpaidInvoicesModel.reference);
                supplierAdjustmentModel.setTotal(vmUnpaidInvoicesModel.total);
                supplierAdjustmentModel.setExclusive(vmUnpaidInvoicesModel.exclusive);
                supplierAdjustmentModel.setUnAllocatedAmount(vmUnpaidInvoicesModel.unAllocateAmount);


                if (pageInEditState) {
                    savedSupplierAdjustmentModel = this.update(supplierAdjustmentModel);
                } else {
                    savedSupplierAdjustmentModel = this.save(supplierAdjustmentModel);
                }

            }


            if (pageInEditState) {
                // page in update state do nothing for detail table
                ;
            } else {
                // page in save state to save detail table

                if (unPaidInvoicesModelList != null && unPaidInvoicesModelList.size() > 0) {

                    for (UnPaidInvoiceModel unPaidInvoiceModel : unPaidInvoicesModelList) {

                        supplierAdjustmentDetailModel = new SupplierAdjustmentDetailModel();
                        supplierAdjustmentDetailModel.setReferenceID(unPaidInvoiceModel.supplierInvoiceID);
                        supplierAdjustmentDetailModel.setReferenceType(PaymentAdjustmentReferenceType.Invoice.get());
                        supplierAdjustmentDetailModel.setSupplierAdjustmentID(savedSupplierAdjustmentModel.getSupplierAdjustmentID());
                        supplierAdjustmentDetailModel.setAdjustAmount(unPaidInvoiceModel.adjustAmount);

                        this.supplierAdjustmentDetailBllManager.save(supplierAdjustmentDetailModel);

                        if (unPaidInvoiceModel.amountDue != null)
                            amountDue = unPaidInvoiceModel.amountDue;

                        if (unPaidInvoiceModel.adjustAmount != null)
                            adjustAmount = unPaidInvoiceModel.adjustAmount;

                        currentPayment = adjustAmount;

                        whereConditionSupplierInvoiceModel = new SupplierInvoiceModel();
                        whereConditionSupplierInvoiceModel.setSupplierInvoiceID(unPaidInvoiceModel.supplierInvoiceID);
                        supplierInvoiceModel = this.supplierInvoiceBllManager.getAllByConditionWithActive(whereConditionSupplierInvoiceModel).get(0);

                        updateInvoicePaymentStatus(unPaidInvoiceModel.supplierInvoiceID
                                , amountDue
                                , currentPayment
                                , note
                                , supplierInvoiceModel);


                    }
                }

                //get base currency and exchange rate
              /*  currencyModel = this.productServiceManager.getBaseCurrency();
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
                }*/


                if (pageInEditState) {
                    // page in update state
                    whereConditionJournalModel = new JournalModel();
                    whereConditionJournalModel.setReferenceType(ReferenceType.SupplierPayment.get());
                    whereConditionJournalModel.setReferenceID(supplierAdjustmentIDForEdit);
                    whereConditionCurrencyModel.setStatus(TillBoxAppEnum.Status.Active.get());

                    journalModelToUpdate = new JournalModel();
                    journalModelToUpdate.setStatus(TillBoxAppEnum.Status.Deleted.get());

                    this.coreCurrencyBllManager.updateByConditions(whereConditionJournalModel, journalModelToUpdate);
                }

                //=============== Journal Entry for amount  Decrease ============================
                if (adjustmentEffectType == AdjustmentEffect.Decrease.get()) {

                    if (total > 0.0) {

                        drJournalModel = CoreUtils.buildDrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.TradeCreditors.get(),
                                savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                ReferenceType.SupplierAdjustment.get(),
                                savedSupplierAdjustmentModel.getSupplierID(),
                                PartyType.Supplier.get(),
                                total,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID
                        );


                        crJournalModel = CoreUtils.buildCrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.AdjustmentIncome.get(),
                                savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                ReferenceType.SupplierAdjustment.get(),
                                null,//savedSupplierPaymentModel.getSupplierID(),
                                null,//PartyType.Supplier.get(),
                                total,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID
                        );
                        this.journalEntry(drJournalModel, crJournalModel);
                    }
                }
                //=============== Journal entry for amount Decrease end =============================


                //=============== Journal Entry for amount Increase ============================
                if (adjustmentEffectType == AdjustmentEffect.Increase.get()) {
                    if (exclusive > 0.0) {
                        drJournalModel = CoreUtils.buildDrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.AdjustmentLoss.get(),
                                savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                ReferenceType.SupplierAdjustment.get(),
                                null,//savedSupplierAdjustmentModel.getSupplierID(),
                                null,//PartyType.Supplier.get(),
                                exclusive,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID
                        );


                        crJournalModel = CoreUtils.buildCrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.TradeCreditors.get(),
                                savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                ReferenceType.SupplierAdjustment.get(),
                                savedSupplierAdjustmentModel.getSupplierID(),
                                PartyType.Supplier.get(),
                                exclusive,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID
                        );
                        this.journalEntry(drJournalModel, crJournalModel);
                    }
                }
                //=============== Journal entry for amount Increase end =============================


                govtVatAmount = vmUnpaidInvoicesModel.vat;

                //=============== Journal Entry for gov vat  Decrease ==================================
                if (adjustmentEffectType == AdjustmentEffect.Decrease.get()) {

                    if (pageInEditState) {
                        ;
                    } else {

                        // Decrease
                        if (govtVatAmount > 0.0) {
                            drJournalModel = CoreUtils.buildDrJournalEntry(
                                    journalReferenceNo,
                                    DefaultCOA.TradeCreditors.get(),
                                    savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                    ReferenceType.SupplierAdjustment.get(),
                                    savedSupplierAdjustmentModel.getSupplierID(),
                                    PartyType.Supplier.get(),
                                    govtVatAmount,
                                    note,
                                    currencyModel,
                                    requestMessage.entryCurrencyID

                            );

                            crJournalModel = CoreUtils.buildCrJournalEntry(
                                    journalReferenceNo,
                                    DefaultCOA.VatPayable.get(),
                                    savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                    ReferenceType.SupplierAdjustment.get(),
                                    null,//
                                    null,//
                                    govtVatAmount,
                                    note,
                                    currencyModel,
                                    requestMessage.entryCurrencyID

                            );
                            this.journalEntry(drJournalModel, crJournalModel);
                        }
                    }
                }
                //=============== Journal Entry for gov vat  Decrease==================================


                //=============== Journal Entry for gov vat  Increase ==================================
                if (adjustmentEffectType == AdjustmentEffect.Increase.get()) {
                    if (pageInEditState) {
                        ;
                    } else {

                        if (govtVatAmount > 0.0) {
                            drJournalModel = CoreUtils.buildDrJournalEntry(
                                    journalReferenceNo,
                                    DefaultCOA.VatPayable.get(),
                                    savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                    ReferenceType.SupplierAdjustment.get(),
                                    null,//savedSupplierPaymentModel.getSupplierID(),
                                    null,//PartyType.Supplier.get(),
                                    govtVatAmount,
                                    note,
                                    currencyModel,
                                    requestMessage.entryCurrencyID

                            );

                            crJournalModel = CoreUtils.buildCrJournalEntry(
                                    journalReferenceNo,
                                    DefaultCOA.TradeCreditors.get(),
                                    savedSupplierAdjustmentModel.getSupplierAdjustmentID(),
                                    ReferenceType.SupplierAdjustment.get(),
                                    savedSupplierAdjustmentModel.getSupplierID(),
                                    PartyType.Supplier.get(),
                                    govtVatAmount,
                                    note,
                                    currencyModel,
                                    requestMessage.entryCurrencyID

                            );
                            this.journalEntry(drJournalModel, crJournalModel);
                        }
                    }
                }
                //=============== Journal Entry for gov vat  Increase ==================================
            }


            bllResponseMessage.responseObject = vmUnpaidInvoicesModel;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            log.error("Error", e);
            throw e;
        }

        return bllResponseMessage;

    }

    public BllResponseMessage getSupplierAdjustmentList(RequestMessage requestMessage) throws Exception {
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        VMSupplierAdjustmentList vmSupplierAdjustmentList;
        List<SupplierAdjustmentModel> supplierAdjustmentModelList;
        SupplierAdjustmentModel whereConditionSupplierAdjustmentModel = new SupplierAdjustmentModel();

        this.supplierBllManager = new SupplierBllManager();

        SupplierModel supplierModel, whereConditionSupplierModel;
        //List<SupplierModel> supplierModelList;
        List<VMSupplierAdjustmentList> supplierAdjustmentList = new ArrayList<>();

        try {

            whereConditionSupplierAdjustmentModel.setBusinessID(requestMessage.businessID);
            supplierAdjustmentModelList = this.getAllByConditionWithActive(whereConditionSupplierAdjustmentModel);

            for (SupplierAdjustmentModel supplierAdjustmentModel : supplierAdjustmentModelList) {
                whereConditionSupplierModel = new SupplierModel();
                whereConditionSupplierModel.setSupplierID(supplierAdjustmentModel.getSupplierID());
                supplierModel = this.supplierBllManager.getAllByConditionWithActive(whereConditionSupplierModel).get(0);
                vmSupplierAdjustmentList = new VMSupplierAdjustmentList();
                vmSupplierAdjustmentList.supplierName = supplierModel.getSupplierName();
                vmSupplierAdjustmentList.supplierID = supplierModel.getSupplierID();
                vmSupplierAdjustmentList.action = PageAction.QuickView.get();
                vmSupplierAdjustmentList.date = supplierAdjustmentModel.getDate();
                vmSupplierAdjustmentList.docNo = supplierAdjustmentModel.getDocNumber();
                vmSupplierAdjustmentList.total = supplierAdjustmentModel.getTotal();
                vmSupplierAdjustmentList.ref = supplierAdjustmentModel.getReference();
                vmSupplierAdjustmentList.supplierAdjustmentID = supplierAdjustmentModel.getSupplierAdjustmentID();
                supplierAdjustmentList.add(vmSupplierAdjustmentList);
            }

            bllResponseMessage.responseObject = supplierAdjustmentList;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            log.error("Error", e);
            throw e;
        }

        return bllResponseMessage;
    }

    private void journalEntry(JournalModel drJournalModel, JournalModel crJournalModel) throws Exception {
        Boolean isCompleted;
        List<JournalModel> journalModelList = new ArrayList<>();
        journalModelList.add(drJournalModel);
        journalModelList.add(crJournalModel);


        isCompleted = this.coreJournalBllManager.saveOrUpdate(journalModelList);
        if (!isCompleted) {
            throw new Exception(Core.clientMessage.get().userMessage);
        }
    }

    private void updateInvoicePaymentStatus(Integer supplierInvoiceID
            , Double amountDue
            , Double currentPayment
            , String note
            , SupplierInvoiceModel supplierInvoiceModel) throws Exception {
        if (amountDue.doubleValue() == currentPayment.doubleValue()) {
            //set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
            //supplierInvoiceModel = new SupplierInvoiceModel();
            supplierInvoiceModel.setSupplierInvoiceID(supplierInvoiceID);
            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
            supplierInvoiceModel.setNote(note);
            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
        } else if (amountDue.doubleValue() > currentPayment.doubleValue()) {
            //set paid status (Partial) in SupplierInvoice Table of Column (paymentStatus)
            //supplierInvoiceModel = new SupplierInvoiceModel();
            supplierInvoiceModel.setSupplierInvoiceID(supplierInvoiceID);
            supplierInvoiceModel.setPaymentStatus(PaymentStatus.PartiallyPaid.get());
            supplierInvoiceModel.setNote(note);
            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
        } else if (amountDue.doubleValue() < currentPayment.doubleValue()) {
            // set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
            // and calculate the excess amount and set is as unallocate amount in supplierPayment Table
            //supplierInvoiceModel = new SupplierInvoiceModel();
            supplierInvoiceModel.setSupplierInvoiceID(supplierInvoiceID);
            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
            supplierInvoiceModel.setNote(note);
            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
        }
    }
}
