/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 27-Feb-18
 * Time: 5:24 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.BllResponseMessage;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreBllManager.CoreCurrencyBllManager;
import nybsys.tillboxweb.coreBllManager.CoreDocumentNumberBllManager;
import nybsys.tillboxweb.coreBllManager.CoreJournalBllManager;
import nybsys.tillboxweb.coreBllManager.CoreVATRateBllManager;
import nybsys.tillboxweb.coreConstant.CurrencyConstant;
import nybsys.tillboxweb.coreEnum.*;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.coreModels.DocumentNumberModel;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.coreModels.VATRateModel;
import nybsys.tillboxweb.coreUtil.CoreUtils;
import nybsys.tillboxweb.entities.SupplierPayment;
import nybsys.tillboxweb.enumpurches.PaymentStatus;
import nybsys.tillboxweb.models.*;
import nybsys.tillboxweb.service.manager.SupplierAdjustmentServiceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("ALL")
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupplierPaymentBllManager extends BaseBll<SupplierPayment> {

    private static final Logger log = LoggerFactory.getLogger(SupplierPaymentBllManager.class);

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(SupplierPayment.class);
        Core.runTimeModelType.set(SupplierPaymentModel.class);
    }

    @Autowired
    private SupplierPaymentDetailBllManager supplierPaymentDetailBllManager;

    @Autowired
    private CoreJournalBllManager coreJournalBllManager = new CoreJournalBllManager();

    @Autowired
    private SupplierInvoiceBllManager supplierInvoiceBllManager = new SupplierInvoiceBllManager();

    private SupplierAdjustmentServiceManager supplierAdjustmentServiceManager;

    private SupplierInvoiceDetailBllManager supplierInvoiceDetailBllManager;

    private CoreCurrencyBllManager coreCurrencyBllManager;

    private CoreDocumentNumberBllManager coreDocumentNumberBllManager;

    private CoreVATRateBllManager coreVATRateBllManager;

    private SupplierBllManager supplierBllManager;


    public BllResponseMessage getSupplierPaymentList(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        this.supplierBllManager = new SupplierBllManager();
        this.supplierAdjustmentServiceManager = new SupplierAdjustmentServiceManager();

        List<SupplierPaymentModel> supplierPaymentModelList;
        SupplierPaymentModel supplierPaymentModel, whereConditionSupplierPaymentModel;
        SupplierModel supplierModel, whereConditionSupplierModel;

        VMSupplierPaymentList vmSupplierPaymentList;

        Integer supplierID;
        List<VMSupplierPaymentList> vmSupplierPaymentCollection = new ArrayList<>();

        try {
            whereConditionSupplierPaymentModel = new SupplierPaymentModel();
            whereConditionSupplierPaymentModel.setBusinessID(requestMessage.businessID);
            supplierPaymentModelList = this.getAllByConditions(whereConditionSupplierPaymentModel);


            for (SupplierPaymentModel forEachSupplierPaymentModel : supplierPaymentModelList) {
                supplierID = forEachSupplierPaymentModel.getSupplierID();
                whereConditionSupplierModel = new SupplierModel();
                whereConditionSupplierModel.setSupplierID(supplierID);
                supplierModel = this.supplierBllManager.getAllByConditionWithActive(whereConditionSupplierModel).get(0);
                vmSupplierPaymentList = new VMSupplierPaymentList();
                vmSupplierPaymentList.supplierName = supplierModel.getSupplierName();
                vmSupplierPaymentList.supplierID = supplierID;
                vmSupplierPaymentList.documentNumber = forEachSupplierPaymentModel.getSupplierPaymentNumber();
                vmSupplierPaymentList.coaID = forEachSupplierPaymentModel.getAccountID();
                vmSupplierPaymentList.reference = forEachSupplierPaymentModel.getReference();
                vmSupplierPaymentList.date = forEachSupplierPaymentModel.getDate();
                vmSupplierPaymentList.amountTotal =
                        forEachSupplierPaymentModel.getPaidAmount();
                vmSupplierPaymentList.printed = forEachSupplierPaymentModel.getIsPrinted();
                vmSupplierPaymentList.selectedSupplierPayment = false;
                vmSupplierPaymentList.supplierPaymentID = forEachSupplierPaymentModel.getSupplierPaymentID();
                vmSupplierPaymentCollection.add(vmSupplierPaymentList);

            }

            if (vmSupplierPaymentCollection.size() > 0) {
                bllResponseMessage.responseObject = vmSupplierPaymentCollection;
                bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                bllResponseMessage.message = TillBoxAppConstant.SUCCESS;
            } else {
                bllResponseMessage.responseObject = null;
                bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                bllResponseMessage.message = TillBoxAppConstant.OPERATION_FAILED;
            }

        } catch (Exception ex) {
            log.error("Error", ex);
        }

        return bllResponseMessage;
    }

    public BllResponseMessage saveOrUpdate(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        this.supplierPaymentDetailBllManager = new SupplierPaymentDetailBllManager();
        this.supplierInvoiceBllManager = new SupplierInvoiceBllManager();
        CurrencyModel currencyModel;

        VMSupplierPaymentModel vmSupplierPaymentModel;
        JournalModel drJournalModel, crJournalModel;

        vmSupplierPaymentModel = Core.getRequestObject(requestMessage, VMSupplierPaymentModel.class);
        SupplierPaymentDetailModel saveOrUpdateSupplierPaymentDetailModel;
        List<SupplierPaymentDetailModel> saveOrUpdateSupplierPaymentDetailModelList = new ArrayList<>();
        List<SupplierPaymentDetailModel> previousSupPayDetailModelList;

        SupplierPaymentDetailModel whereConditionSupPayDetailModel;
        SupplierInvoiceModel supplierInvoiceModel;

        //get base currency and exchange rate
        currencyModel = this.supplierAdjustmentServiceManager.getBaseCurrency();
        if (currencyModel == null) {
            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            Core.clientMessage.get().userMessage = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
            Core.clientMessage.get().message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
            return bllResponseMessage;
        }

        //check entry currency is present if not base currency will be entry currency
        if (requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0) {
            requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
        }

        // Extract data from VM ======================================
        SupplierPaymentModel supplierPaymentModel
                = vmSupplierPaymentModel.supplierPaymentModel;
        List<SupplierPaymentDetailModel> supplierPaymentDetailModelList
                = vmSupplierPaymentModel.supplierPaymentDetailModelList;
        // Extract data from VM =======================================

        Integer supplierPaymentID = supplierPaymentModel.getSupplierPaymentID();
        SupplierPaymentModel savedSupplierPaymentModel, updateSupplierPaymentModel = null;

        List<SupplierPaymentModel> supplierPaymentModelList = null;
        String supplierInvoiceNo = "0", buildSupplierInvoiceNo;
        Integer invoiceNo = 0, invoiceNoLength = 0, zeroFillLength = 0;
        String hsql, buildDbSequence, currentDBSequence = null;
        String journalReferenceNo;
        Double amount = 0.0;
        Double discount = 0.0, previousDiscount = 0.0;
        Double unAllocateAmount = 0.0;
        Double totalAmount = 0.0;
        Double totalDiscount = 0.0;
        Double previousPaymentTotal = 0.0, currentPayment = 0.0;

        try {
            journalReferenceNo = TillBoxUtils.getUUID();
            List<JournalModel> journalModelList = new ArrayList<>();

            // ============================= Create PMT0000001 =============================
            hsql = hsql = "SELECT e FROM SupplierPayment e ORDER BY e.supplierPaymentID DESC";
            supplierPaymentModelList = this.executeHqlQuery(hsql, SupplierPaymentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
            if (supplierPaymentModelList.size() > 0) {
                currentDBSequence = supplierPaymentModelList.get(0).getSupplierPaymentNumber();
            }
            buildDbSequence = CoreUtils.getSequence(currentDBSequence, "PMT");
            // ==========================End Create PMT0000001 =============================


            if (supplierPaymentID == null || supplierPaymentID == 0) {
                // Save Code
                supplierPaymentModel.setSupplierPaymentNumber(buildDbSequence);
                supplierPaymentModel.setDocNumber(buildDbSequence);

                //Save master table data
                savedSupplierPaymentModel = this.save(supplierPaymentModel);

                // save supplier payment details ============================================
                for (SupplierPaymentDetailModel item : supplierPaymentDetailModelList) {

                    //======================= due calculation logic ==========================================================================
                    supplierInvoiceModel = this.supplierInvoiceBllManager.getByIdActiveStatus(item.getReferenceID());
                    // if payment status is due then
                    if (supplierInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                            || supplierInvoiceModel.getPaymentStatus() == PaymentStatus.PartiallyPaid.get()) {

                        whereConditionSupPayDetailModel = new SupplierPaymentDetailModel();
                        whereConditionSupPayDetailModel.setReferenceID(item.getReferenceID());
                        previousSupPayDetailModelList =
                                this.supplierPaymentDetailBllManager.getAllByConditionWithActive(whereConditionSupPayDetailModel);

                        for (SupplierPaymentDetailModel previousSupPaymentDetailModel : previousSupPayDetailModelList) {
                            previousPaymentTotal += previousSupPaymentDetailModel.getDiscount() + previousSupPaymentDetailModel.getPaidAmount();
                            previousDiscount += previousSupPaymentDetailModel.getDiscount();
                        }


                        //input come from json
                        currentPayment = item.getPaidAmount() + item.getDiscount();
                        currentPayment = currentPayment + previousPaymentTotal;


                        if (supplierInvoiceModel.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        } else if (supplierInvoiceModel.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in SupplierInvoice Table of Column (paymentStatus)
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.PartiallyPaid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        }
                        if (supplierInvoiceModel.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in supplierPayment Table
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                            unAllocateAmount = currentPayment - supplierInvoiceModel.getTotalAmount();
                        }

                    }
                    //======================= End Due calculation logic ======================================================================


                    item.setSupplierPaymentID(savedSupplierPaymentModel.getSupplierPaymentID());
                    reqMessageForBllManager = this.getDefaultRequestMessage();
                    reqMessageForBllManager.requestObj = item;
                    //Save detail table data
                    saveOrUpdateSupplierPaymentDetailModel = this.supplierPaymentDetailBllManager.saveOrUpdate(reqMessageForBllManager);

                    // Hold datail table data for return
                    saveOrUpdateSupplierPaymentDetailModelList.add(saveOrUpdateSupplierPaymentDetailModel);


                    amount = item.getPaidAmount();
                    if (amount == 0.0 || amount == null)
                        amount = 0.0;
                    else
                        totalAmount += amount;

                    discount = item.getDiscount();
                    if (discount == 0 || discount == null)
                        discount = 0.0;
                    else
                        totalDiscount += discount;
                }
                // save supplier payment details End ====== for loop end ==================================
/*


                //if(amount>0.0) {
                if (totalAmount > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeCreditors.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            savedSupplierPaymentModel.getSupplierID(),
                            PartyType.Supplier.get(),
                            totalAmount,
                            savedSupplierPaymentModel.getDescription(),
                            currencyModel,
                            savedSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            null,
                            null,
                            totalAmount,
                            savedSupplierPaymentModel.getDescription(),
                            currencyModel,
                            savedSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }
                //=============== Journal entry for amount end =============================


                //=============== Journal Entry for discount ===============================
                //if(discount>0.0){
                if (totalDiscount > 0.0) {
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeCreditors.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            savedSupplierPaymentModel.getSupplierID(),
                            PartyType.Supplier.get(),
                            totalDiscount,
                            savedSupplierPaymentModel.getDescription(),
                            currencyModel,
                            savedSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.DiscountEarn.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            null,
                            null,
                            totalDiscount,
                            savedSupplierPaymentModel.getDescription(),
                            currencyModel,
                            savedSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }
                //=============== Journal for discount Entry End =============================
*/


                //totalAmount = totalAmount + unAllocateAmount;

                savedSupplierPaymentModel.setPaidAmount(totalAmount);
                //savedSupplierPaymentModel.setBaseCurrencyAmount(totalAmount);
                savedSupplierPaymentModel.setDiscount(totalDiscount);
                savedSupplierPaymentModel.setUnAllocatedAmount(unAllocateAmount);
                savedSupplierPaymentModel = this.update(savedSupplierPaymentModel);

                //update VM for return
                vmSupplierPaymentModel.supplierPaymentModel = savedSupplierPaymentModel;
                vmSupplierPaymentModel.supplierPaymentDetailModelList = saveOrUpdateSupplierPaymentDetailModelList;

                if (savedSupplierPaymentModel != null && saveOrUpdateSupplierPaymentDetailModelList.size() > 0) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "SupplierPayment Save Successfully";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Save SupplierPayment";
                }
            } else {
                // Update Code
                updateSupplierPaymentModel = this.update(supplierPaymentModel);
                totalAmount = 0.0;

                //================ Delete Journal before update =====================================
                Integer referenceID, referenceType;
                JournalModel journalModelForDelete = new JournalModel();

                journalModelList = new ArrayList<>();

                referenceID = updateSupplierPaymentModel.getSupplierPaymentID();
                referenceType = ReferenceType.SupplierPayment.get();

                journalModelForDelete.setReferenceID(referenceID);
                journalModelForDelete.setStatus(TillBoxAppEnum.Status.Active.get());
                journalModelForDelete.setReferenceType(referenceType);
                //this.coreJournalBllManager = new CoreJournalBllManager();
                journalModelList = this.coreJournalBllManager.getAllByConditions(journalModelForDelete);

                for (JournalModel journalModel : journalModelList) {
                    this.coreJournalBllManager.softDelete(journalModel);
                }
                //===================================================================================

                // Update supplier payment details ==================================================
                for (SupplierPaymentDetailModel item : supplierPaymentDetailModelList) {


                    //======================= due calculation logic =========================================================================
                    supplierInvoiceModel = this.supplierInvoiceBllManager.getByIdActiveStatus(item.getReferenceID());
                    // if payment status is due then
                    if (supplierInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                            || supplierInvoiceModel.getPaymentStatus() == PaymentStatus.PartiallyPaid.get()) {

                        whereConditionSupPayDetailModel = new SupplierPaymentDetailModel();
                        whereConditionSupPayDetailModel.setReferenceID(item.getReferenceID());
                        previousSupPayDetailModelList =
                                this.supplierPaymentDetailBllManager.getAllByConditionWithActive(whereConditionSupPayDetailModel);

                        for (SupplierPaymentDetailModel previousSupPaymentDetailModel : previousSupPayDetailModelList) {
                            previousPaymentTotal += previousSupPaymentDetailModel.getDiscount() + previousSupPaymentDetailModel.getPaidAmount();
                            previousDiscount += previousSupPaymentDetailModel.getDiscount();
                        }

                        currentPayment = item.getPaidAmount() + item.getDiscount();
                        currentPayment = currentPayment + previousPaymentTotal;


                        if (supplierInvoiceModel.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        } else if (supplierInvoiceModel.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in SupplierInvoice Table of Column (paymentStatus)
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.PartiallyPaid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        }
                        if (supplierInvoiceModel.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in supplierPayment Table
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                            unAllocateAmount = currentPayment - supplierInvoiceModel.getTotalAmount();
                        }

                    }
                    //======================= End Due calculation logic ======================================================================

                    item.setSupplierPaymentID(updateSupplierPaymentModel.getSupplierPaymentID());
                    reqMessageForBllManager = this.getDefaultRequestMessage();
                    reqMessageForBllManager.requestObj = item;
                    //Save detail table data
                    saveOrUpdateSupplierPaymentDetailModel = this.supplierPaymentDetailBllManager.saveOrUpdate(reqMessageForBllManager);

                    // Hold datail table data for return
                    saveOrUpdateSupplierPaymentDetailModelList.add(saveOrUpdateSupplierPaymentDetailModel);


                    amount = item.getPaidAmount();
                    if (amount == 0.0 || amount == null)
                        amount = 0.0;
                    else
                        totalAmount += amount;

                    discount = item.getDiscount();
                    if (discount == 0 || discount == null)
                        discount = 0.0;
                    else
                        totalDiscount += discount;
                }
                // update supplier payment details End ====== for loop end ==================================

/*
                if (totalAmount > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeCreditors.get(),
                            updateSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            updateSupplierPaymentModel.getSupplierID(),
                            PartyType.Supplier.get(),
                            totalAmount,
                            updateSupplierPaymentModel.getDescription(),
                            currencyModel,
                            updateSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            updateSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            null,
                            null,
                            totalAmount,
                            updateSupplierPaymentModel.getDescription(),
                            currencyModel,
                            updateSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal entry for amount end =============================


                //=============== Journal Entry for discount ===============================
                //if(discount>0.0){
              /*  if (totalDiscount > 0.0) {
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeCreditors.get(),
                            updateSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            updateSupplierPaymentModel.getSupplierID(),
                            PartyType.Supplier.get(),
                            totalDiscount,
                            updateSupplierPaymentModel.getDescription(),
                            currencyModel,
                            updateSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.DiscountEarn.get(),
                            updateSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            null,
                            null,
                            totalDiscount,
                            updateSupplierPaymentModel.getDescription(),
                            currencyModel,
                            updateSupplierPaymentModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal for discount Entry End =============================


                updateSupplierPaymentModel.setPaidAmount(totalAmount);
                //updateSupplierPaymentModel.setBaseCurrencyAmount(totalAmount);
                updateSupplierPaymentModel.setDiscount(totalDiscount);
                updateSupplierPaymentModel.setUnAllocatedAmount(unAllocateAmount);
                updateSupplierPaymentModel = this.update(updateSupplierPaymentModel);


                //update VM for return
                vmSupplierPaymentModel.supplierPaymentModel = updateSupplierPaymentModel;
                vmSupplierPaymentModel.supplierPaymentDetailModelList = saveOrUpdateSupplierPaymentDetailModelList;

                // Update supplier payment details ==============================================
                if (updateSupplierPaymentModel != null && saveOrUpdateSupplierPaymentDetailModelList.size() > 0) {
                    Core.clientMessage.get().userMessage = "Supplier Payment Update Successfully";
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Update Supplier Payment";
                }
            }


            bllResponseMessage.responseObject = vmSupplierPaymentModel;
            bllResponseMessage.responseCode = Core.clientMessage.get().messageCode;
            bllResponseMessage.message = Core.clientMessage.get().userMessage;


        } catch (Exception ex) {
            log.error("SupplierPaymentBllManager -> saveOrUpdate got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        //return savedSupplierPaymentModel;
        return bllResponseMessage;
    }


    public BllResponseMessage isSupplierPaymentEditable(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();

        VMUnPaidInvoicesModel vmUnpaidInvoicesModel = new VMUnPaidInvoicesModel();

        SupplierModel supplierModel;
        List<SupplierPaymentDetailModel> supplierPaymentDetailModelList;
        SupplierPaymentDetailModel whereConditionSupplierPaymentDetailModel;

        Integer supplierPaymentID;

        try {
            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            if (supplierModel != null) {
                supplierPaymentID = supplierModel.supplierPaymentID;
                whereConditionSupplierPaymentDetailModel = new SupplierPaymentDetailModel();
                whereConditionSupplierPaymentDetailModel.setSupplierPaymentID(supplierPaymentID);
                supplierPaymentDetailModelList = this.supplierPaymentDetailBllManager.getAllByConditionWithActive(whereConditionSupplierPaymentDetailModel);

                if (supplierPaymentDetailModelList != null && supplierPaymentDetailModelList.size() > 0) {
                    vmUnpaidInvoicesModel.isPageEditable = false;
                } else {
                    vmUnpaidInvoicesModel.isPageEditable = true;
                }
                bllResponseMessage.responseObject = vmUnpaidInvoicesModel;
                bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                bllResponseMessage.message = TillBoxAppConstant.SUCCESS;
            } else {
                bllResponseMessage.responseObject = null;
                bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                bllResponseMessage.message = TillBoxAppConstant.FAILED;
            }
        } catch (Exception ex) {
            log.error("Error", ex);
        }

        return bllResponseMessage;
    }

    public BllResponseMessage getUnpaidSupplierInvoicesBySupplierID(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        this.supplierPaymentDetailBllManager = new SupplierPaymentDetailBllManager();
        this.supplierInvoiceDetailBllManager = new SupplierInvoiceDetailBllManager();

        VMUnPaidInvoicesModel vmUnpaidInvoicesModel;
        UnPaidInvoiceModel unPaidInvoicesModel;
        List<UnPaidInvoiceModel> unPaidInvoicesModelList = new ArrayList<>();

        SupplierModel supplierModel;
        SupplierInvoiceModel whereConditionSupplierInvoiceModel;
        SupplierInvoiceDetailModel whereConditionSupplierInvoiceDetailModel;

        List<SupplierInvoiceModel> supplierInvoiceModelList;
        List<SupplierInvoiceDetailModel> supplierInvoiceDetailModelList;

        SupplierPaymentDetailModel whereConditionSupplierPaymentDetailModel;
        List<SupplierPaymentDetailModel> supplierPaymentDetailModelList;

        SupplierPaymentModel supplierPaymentModel, whereConditionSupplierPaymentModel;

        Double totalAmount = 0.0;
        Double dueAmount = 0.0, paidAmount = 0.0, balance = 0.0;

        String documentNumber;

        try {
            supplierModel = Core.getRequestObject(requestMessage, SupplierModel.class);
            whereConditionSupplierInvoiceModel = new SupplierInvoiceModel();
            whereConditionSupplierInvoiceModel.setSupplierID(supplierModel.getSupplierID());

            supplierInvoiceModelList = this.supplierInvoiceBllManager.getAllByConditions(whereConditionSupplierInvoiceModel);

            for (SupplierInvoiceModel supplierInvoiceModel : supplierInvoiceModelList) {

                if (supplierInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                        || supplierInvoiceModel.getPaymentStatus() == PaymentStatus.PartiallyPaid.get()) {

                    whereConditionSupplierInvoiceDetailModel = new SupplierInvoiceDetailModel();
                    whereConditionSupplierInvoiceDetailModel.setSupplierInvoiceID(supplierInvoiceModel.getSupplierInvoiceID());

                    supplierInvoiceDetailModelList = this.supplierInvoiceDetailBllManager.getAllByConditionWithActive(whereConditionSupplierInvoiceDetailModel);

                    for (SupplierInvoiceDetailModel supplierInvoiceDetailModel : supplierInvoiceDetailModelList) {
                        totalAmount += ((supplierInvoiceDetailModel.getQuantity() * supplierInvoiceDetailModel.getUnitPrice()) - supplierInvoiceDetailModel.getDiscount()) + supplierInvoiceDetailModel.getVat();
                    }

                    whereConditionSupplierPaymentDetailModel = new SupplierPaymentDetailModel();
                    whereConditionSupplierPaymentDetailModel.setReferenceID(supplierInvoiceModel.getSupplierInvoiceID());

                    supplierPaymentDetailModelList = this.supplierPaymentDetailBllManager.getAllByConditionWithActive(whereConditionSupplierPaymentDetailModel);
                    for (SupplierPaymentDetailModel supplierPaymentDetailModel : supplierPaymentDetailModelList) {
                        paidAmount += supplierPaymentDetailModel.getPaidAmount();
                    }

                    documentNumber = supplierInvoiceModel.getSupplierInvoiceNo();
                    dueAmount = Math.abs(totalAmount - paidAmount);

                    unPaidInvoicesModel = new UnPaidInvoiceModel();
                    unPaidInvoicesModel.supplierInvoiceID = supplierInvoiceModel.getSupplierInvoiceID();
                    unPaidInvoicesModel.amountDue = dueAmount;
                    unPaidInvoicesModel.total = totalAmount;
                    unPaidInvoicesModel.date = supplierInvoiceModel.getDate();
                    unPaidInvoicesModel.documentNumber = documentNumber;
                    unPaidInvoicesModel.allocate = false;

                    unPaidInvoicesModelList.add(unPaidInvoicesModel);
                    totalAmount = 0.0;
                    paidAmount = 0.0;
                    dueAmount = 0.0;

                }

            }

            //========= Test Data ======================
            unPaidInvoicesModel = new UnPaidInvoiceModel();
            unPaidInvoicesModel.supplierInvoiceID = 1;
            unPaidInvoicesModel.amountDue = 5000.0;
            unPaidInvoicesModel.total = 1000.0;
            unPaidInvoicesModel.date = new Date();
            unPaidInvoicesModel.documentNumber = "SIV0000001";
            unPaidInvoicesModel.allocate = false;

            unPaidInvoicesModelList.add(unPaidInvoicesModel);

            unPaidInvoicesModel = new UnPaidInvoiceModel();
            unPaidInvoicesModel.supplierInvoiceID = 2;
            unPaidInvoicesModel.amountDue = 6000.0;
            unPaidInvoicesModel.total = 7000.0;
            unPaidInvoicesModel.date = new Date();
            unPaidInvoicesModel.documentNumber = "SIV0000002";
            unPaidInvoicesModel.allocate = false;

            unPaidInvoicesModelList.add(unPaidInvoicesModel);

            unPaidInvoicesModel = new UnPaidInvoiceModel();
            unPaidInvoicesModel.supplierInvoiceID = 3;
            unPaidInvoicesModel.amountDue = 2000.0;
            unPaidInvoicesModel.total = 7000.0;
            unPaidInvoicesModel.date = new Date();
            unPaidInvoicesModel.documentNumber = "SIV0000003";
            unPaidInvoicesModel.allocate = false;

            unPaidInvoicesModelList.add(unPaidInvoicesModel);

            //========= End Test Data ======================


            balance = this.getBalanceBySupplierID(supplierModel.getSupplierID(), ReferenceType.SupplierInvoice.get());
            // Test Data
            balance = 80000.0;
            vmUnpaidInvoicesModel = new VMUnPaidInvoicesModel();
            vmUnpaidInvoicesModel.unPaidInvoiceModelList = unPaidInvoicesModelList;
            vmUnpaidInvoicesModel.balance = balance;


            if (supplierModel.pageAction == PageAction.Edit.get()) {
                whereConditionSupplierPaymentModel = new SupplierPaymentModel();
                whereConditionSupplierPaymentModel.setSupplierPaymentID(supplierModel.supplierPaymentID);
                supplierPaymentModel = this.getAllByConditionWithActive(whereConditionSupplierPaymentModel).get(0);

                vmUnpaidInvoicesModel.coaID = supplierPaymentModel.getAccountID();
                vmUnpaidInvoicesModel.description = supplierPaymentModel.getDescription();
                vmUnpaidInvoicesModel.paymentDate = supplierPaymentModel.getDate();
                vmUnpaidInvoicesModel.reference = supplierPaymentModel.getReference();
                vmUnpaidInvoicesModel.paymentAmount = supplierPaymentModel.getPaidAmount();
                vmUnpaidInvoicesModel.unAllocateAmount = supplierPaymentModel.getUnAllocatedAmount();
            }


            bllResponseMessage.responseObject = vmUnpaidInvoicesModel;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return bllResponseMessage;

    }

    public BllResponseMessage saveUnpaidSupplierInvoicesBySupplierID(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();
        this.supplierPaymentDetailBllManager = new SupplierPaymentDetailBllManager();
        this.coreVATRateBllManager = new CoreVATRateBllManager();
        this.coreDocumentNumberBllManager = new CoreDocumentNumberBllManager();
        this.coreCurrencyBllManager = new CoreCurrencyBllManager();


        VMUnPaidInvoicesModel vmUnpaidInvoicesModel;
        List<UnPaidInvoiceModel> unPaidInvoicesModelList;
        List<SupplierPaymentModel> supplierPaymentModelList;
        CurrencyModel whereConditionCurrencyModel, currencyModel;
        List<CurrencyModel> currencyModelList;
        SupplierPaymentModel supplierPaymentModel = new SupplierPaymentModel(),
                savedSupplierPaymentModel = null, supplierPaymentModelForEdit, whereConditionSupplierPaymentModel;

        Double amountDue = 0.0, amountPaid = 0.0, discount = 0.0, currentPayment = 0.0;
        Double discountTotal = 0.0, amountPaidTotal = 0.0, actualPaidAmount = 0.0;
        Integer documentTypeID;
        DocumentNumberModel whereConditionDocumentNumberModel, documentNumberModel;
        String firstThreeChar;
        String buildDbSequence, currentDBSequence = null, hsql;
        SupplierPaymentDetailModel supplierPaymentDetailModel, whereConditionSupplierPaymentDetailModel;
        List<SupplierPaymentDetailModel> supplierPaymentDetailModelList;
        SupplierInvoiceModel supplierInvoiceModel, whereConditionSupplierInvoiceModel;

        VATRateModel vatRateModel, whereConditionVatRateModel;
        Double vatRate = 1.0, govtVatAmount = 0.0;
        Double unAllocateAmount = 0.0;

        Double temDiscount = 0.0, temPaidAmount = 0.0;
        String note;
        Boolean pageInEditState = false;
        Integer supplierPaymentIDForEdit;

        JournalModel whereConditionJournalModel;
        JournalModel journalModelToUpdate;

        try {

            JournalModel drJournalModel, crJournalModel;
            String journalReferenceNo;
            journalReferenceNo = TillBoxUtils.getUUID();

            vmUnpaidInvoicesModel = Core.getRequestObject(requestMessage, VMUnPaidInvoicesModel.class);
            unPaidInvoicesModelList = vmUnpaidInvoicesModel.unPaidInvoiceModelList;

            pageInEditState = vmUnpaidInvoicesModel.pageInEditState;
            supplierPaymentIDForEdit = vmUnpaidInvoicesModel.supplierPaymentID;
            note = vmUnpaidInvoicesModel.comment;

            unAllocateAmount = vmUnpaidInvoicesModel.unAllocateAmount;

            documentTypeID = vmUnpaidInvoicesModel.documentTypeID;
            whereConditionDocumentNumberModel = new DocumentNumberModel();
            whereConditionDocumentNumberModel.setDocumentTypeID(documentTypeID);

            documentNumberModel = this.coreDocumentNumberBllManager.getAllByConditionWithActive(whereConditionDocumentNumberModel).get(0);
            firstThreeChar = StringUtils.substring(documentNumberModel.getCurrentDocumentNumber().toString(), 0, 3);

            // ============================= Create PAY0000001 =============================
            hsql = hsql = "SELECT e FROM SupplierPayment e ORDER BY e.supplierPaymentID DESC";
            supplierPaymentModelList = this.executeHqlQuery(hsql, SupplierPaymentModel.class, TillBoxAppEnum.QueryType.GetOne.get());
            if (supplierPaymentModelList.size() > 0) {
                currentDBSequence = supplierPaymentModelList.get(0).getSupplierPaymentNumber();
            }
            buildDbSequence = CoreUtils.getSequence(currentDBSequence, firstThreeChar);
            // ==========================End Create PAY0000001 =============================


            if (unPaidInvoicesModelList != null && unPaidInvoicesModelList.size() > 0) {
                for (UnPaidInvoiceModel unPaidInvoiceModel : unPaidInvoicesModelList) {

                    temDiscount = (unPaidInvoiceModel.discount != null) ? unPaidInvoiceModel.discount : 0.0;
                    temPaidAmount = (unPaidInvoiceModel.amountPaid != null) ? unPaidInvoiceModel.amountPaid : 0.0;
                    discountTotal += temDiscount;
                    amountPaidTotal += temPaidAmount;

                    actualPaidAmount = temPaidAmount - temDiscount;

                    if (unPaidInvoiceModel.amountDue < actualPaidAmount) {
                        bllResponseMessage.responseObject = "";
                        bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        bllResponseMessage.message = "Paid amount greater than due amount";
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
                    whereConditionSupplierPaymentModel = new SupplierPaymentModel();
                    whereConditionSupplierPaymentModel.setSupplierPaymentID(supplierPaymentIDForEdit);
                    supplierPaymentModelForEdit = this.getAllByConditionWithActive(whereConditionSupplierPaymentModel).get(0);


                    whereConditionSupplierPaymentDetailModel = new SupplierPaymentDetailModel();
                    whereConditionSupplierPaymentDetailModel.setSupplierPaymentID(supplierPaymentModelForEdit.getSupplierPaymentID());
                    supplierPaymentDetailModelList = this.supplierPaymentDetailBllManager.getAllByConditions(whereConditionSupplierPaymentDetailModel);

                    if (supplierPaymentDetailModelList != null && supplierPaymentDetailModelList.size() > 0) {
                        bllResponseMessage.responseObject = null;
                        bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        bllResponseMessage.message = "Update not allowed";
                        return bllResponseMessage;
                    } else {
                        supplierPaymentModel = supplierPaymentModelForEdit;
                    }
                }

                supplierPaymentModel.setBaseCurrencyID(currencyModel.getCurrencyID());
                supplierPaymentModel.setEntryCurrencyID(currencyModel.getCurrencyID());
                supplierPaymentModel.setExchangeRate(currencyModel.getExchangeRate());
                supplierPaymentModel.setAccountID(vmUnpaidInvoicesModel.coaID);
                supplierPaymentModel.setBusinessID(requestMessage.businessID);
                supplierPaymentModel.setDate(vmUnpaidInvoicesModel.paymentDate);
                supplierPaymentModel.setDescription(vmUnpaidInvoicesModel.description);
                supplierPaymentModel.setDocNumber(vmUnpaidInvoicesModel.paymentDocumentNumber);
                supplierPaymentModel.setPaymentMethod(vmUnpaidInvoicesModel.paymentMethod);
                supplierPaymentModel.setSupplierID(vmUnpaidInvoicesModel.supplierID);
                supplierPaymentModel.setSupplierPaymentNumber(buildDbSequence);
                supplierPaymentModel.setDiscount(discountTotal);
                supplierPaymentModel.setReference(vmUnpaidInvoicesModel.reference);
                supplierPaymentModel.setPaidAmount(amountPaidTotal + discountTotal + vmUnpaidInvoicesModel.unAllocateAmount);
                supplierPaymentModel.setUnAllocatedAmount(vmUnpaidInvoicesModel.unAllocateAmount);


                if (pageInEditState) {
                    savedSupplierPaymentModel = this.update(supplierPaymentModel);
                } else {
                    savedSupplierPaymentModel = this.save(supplierPaymentModel);
                }

            }


            if (pageInEditState) {
                // page in update state do nothing
                ;
            } else {
                // page in save state

                if (unPaidInvoicesModelList != null && unPaidInvoicesModelList.size() > 0) {
                    for (UnPaidInvoiceModel unPaidInvoiceModel : unPaidInvoicesModelList) {

                        supplierPaymentDetailModel = new SupplierPaymentDetailModel();
                        supplierPaymentDetailModel.setReferenceID(unPaidInvoiceModel.supplierInvoiceID);
                        supplierPaymentDetailModel.setSupplierPaymentID(savedSupplierPaymentModel.getSupplierPaymentID());
                        if (unPaidInvoiceModel.discount != null)
                            supplierPaymentDetailModel.setDiscount(unPaidInvoiceModel.discount);
                        supplierPaymentDetailModel.setPaidAmount(unPaidInvoiceModel.amountPaid);


                        this.supplierPaymentDetailBllManager.save(supplierPaymentDetailModel);

                        if (unPaidInvoiceModel.amountDue != null)
                            amountDue = unPaidInvoiceModel.amountDue;

                        if (unPaidInvoiceModel.amountPaid != null)
                            amountPaid = unPaidInvoiceModel.amountPaid;

                        if (unPaidInvoiceModel.discount != null)
                            discount = unPaidInvoiceModel.discount;
                        else discount = 0.0;

                        currentPayment = 0.0;
                        currentPayment = amountPaid + discount;

                        whereConditionSupplierInvoiceModel = new SupplierInvoiceModel();
                        whereConditionSupplierInvoiceModel.setSupplierInvoiceID(unPaidInvoiceModel.supplierInvoiceID);
                        supplierInvoiceModel = this.supplierInvoiceBllManager.getAllByConditionWithActive(whereConditionSupplierInvoiceModel).get(0);

                        if (amountDue.doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            //supplierInvoiceModel = new SupplierInvoiceModel();
                            supplierInvoiceModel.setSupplierInvoiceID(unPaidInvoiceModel.supplierInvoiceID);
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            supplierInvoiceModel.setNote(note);
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        } else if (amountDue.doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in SupplierInvoice Table of Column (paymentStatus)
                            //supplierInvoiceModel = new SupplierInvoiceModel();
                            supplierInvoiceModel.setSupplierInvoiceID(unPaidInvoiceModel.supplierInvoiceID);
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.PartiallyPaid.get());
                            supplierInvoiceModel.setNote(note);
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        } else if (amountDue.doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in supplierPayment Table
                            //supplierInvoiceModel = new SupplierInvoiceModel();
                            supplierInvoiceModel.setSupplierInvoiceID(unPaidInvoiceModel.supplierInvoiceID);
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            supplierInvoiceModel.setNote(note);
                            this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                        }

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
                    whereConditionJournalModel.setReferenceID(supplierPaymentIDForEdit);
                    whereConditionCurrencyModel.setStatus(TillBoxAppEnum.Status.Active.get());

                    journalModelToUpdate = new JournalModel();
                    journalModelToUpdate.setStatus(TillBoxAppEnum.Status.Deleted.get());

                    this.coreCurrencyBllManager.updateByConditions(whereConditionJournalModel, journalModelToUpdate);
                }

                if (amountPaidTotal > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeCreditors.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            savedSupplierPaymentModel.getSupplierID(),
                            PartyType.Supplier.get(),
                            amountPaidTotal,
                            note,
                            currencyModel,
                            requestMessage.entryCurrencyID
                    );


                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            savedSupplierPaymentModel.getSupplierPaymentID(),
                            ReferenceType.SupplierPayment.get(),
                            null,//savedSupplierPaymentModel.getSupplierID(),
                            null,//PartyType.Supplier.get(),
                            amountPaidTotal,
                            note,
                            currencyModel,
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }

                //=============== Journal entry for amount end =============================


                if (pageInEditState) {

                    ;
                } else {
                    //=============== Journal Entry for discount ===============================
                    if (discountTotal > 0.0) {
                        drJournalModel = CoreUtils.buildDrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.TradeCreditors.get(),
                                savedSupplierPaymentModel.getSupplierPaymentID(),
                                ReferenceType.SupplierPayment.get(),
                                savedSupplierPaymentModel.getSupplierID(),
                                PartyType.Supplier.get(),
                                discountTotal,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );

                        crJournalModel = CoreUtils.buildCrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.DiscountEarn.get(),
                                savedSupplierPaymentModel.getSupplierPaymentID(),
                                ReferenceType.SupplierPayment.get(),
                                null,//savedSupplierPaymentModel.getSupplierID(),
                                null,//PartyType.Supplier.get(),
                                discountTotal,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );
                        this.journalEntry(drJournalModel, crJournalModel);
                    }

                    //=============== Journal for discount Entry End =============================
                }

                //=============== Journal Entry for gov vat ==================================
                whereConditionVatRateModel = new VATRateModel();
                whereConditionVatRateModel.setIsDefault(true);

                vatRateModel = this.coreVATRateBllManager.getAllByConditionWithActive(whereConditionVatRateModel).get(0);
                vatRate = vatRateModel.getRate();
                govtVatAmount = (discountTotal * vatRate) / 100;


                if (pageInEditState) {
                    ;
                } else {

                    if (govtVatAmount > 0.0) {
                        drJournalModel = CoreUtils.buildDrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.DiscountEarn.get(),
                                savedSupplierPaymentModel.getSupplierPaymentID(),
                                ReferenceType.SupplierPayment.get(),
                                null,//savedSupplierPaymentModel.getSupplierID(),
                                null,//PartyType.Supplier.get(),
                                govtVatAmount,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );

                        crJournalModel = CoreUtils.buildCrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.VatPayable.get(),
                                savedSupplierPaymentModel.getSupplierPaymentID(),
                                ReferenceType.SupplierPayment.get(),
                                null,//savedSupplierPaymentModel.getSupplierID(),
                                null,//PartyType.Supplier.get(),
                                govtVatAmount,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );
                        this.journalEntry(drJournalModel, crJournalModel);
                    }
                    //=============== Journal End Entry of gov vat ==================================
                }
            }


            //=============== Journal Entry for un-allocate ===============================
            if (unAllocateAmount > 0.0) {
                drJournalModel = CoreUtils.buildDrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.TradeCreditors.get(),
                        savedSupplierPaymentModel.getSupplierPaymentID(),
                        ReferenceType.SupplierPayment.get(),
                        savedSupplierPaymentModel.getSupplierID(),
                        PartyType.Supplier.get(),
                        unAllocateAmount,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID

                );

                crJournalModel = CoreUtils.buildCrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.BankAccount.get(),
                        savedSupplierPaymentModel.getSupplierPaymentID(),
                        ReferenceType.SupplierPayment.get(),
                        null,//savedSupplierPaymentModel.getSupplierID(),
                        null,//PartyType.Supplier.get(),
                        unAllocateAmount,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID

                );
                this.journalEntry(drJournalModel, crJournalModel);
            }
            //=============== Journal for un-allocate Entry End =============================


            bllResponseMessage.responseObject = vmUnpaidInvoicesModel;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            log.error("Error", e);
        }

        return bllResponseMessage;

    }

    public Integer deleteByConditions(RequestMessage requestMessage) throws Exception {
        SupplierPaymentModel castRequestModel =
                Core.getRequestObject(requestMessage, SupplierPaymentModel.class);
        Integer numberOfDeleteRow = 0;
        try {
            numberOfDeleteRow = this.deleteByConditions(castRequestModel);
            if (numberOfDeleteRow > 0) {
                Core.clientMessage.get().userMessage = "Successfully deleted the requested SupplierPayment";
            } else {
                Core.clientMessage.get().message = "Failed to deleted the requested SupplierPayment";
            }
        } catch (Exception ex) {
            log.error("SupplierPaymentBllManager -> deleteByConditions got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return numberOfDeleteRow;
    }

    public SupplierPaymentModel inActive(RequestMessage requestMessage) throws Exception {
        SupplierPaymentModel castRequestModel =
                Core.getRequestObject(requestMessage, SupplierPaymentModel.class);
        SupplierPaymentModel processedModel = null;
        try {
            if (castRequestModel != null) {
                processedModel = this.inActive(castRequestModel);
                if (processedModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Successfully inactive the requested SupplierPayment";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to inactive the requested SupplierPayment";
                }
            }

        } catch (Exception ex) {
            log.error("SupplierPaymentBllManager -> inActive got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return processedModel;
    }

    public SupplierPaymentModel delete(RequestMessage requestMessage) throws Exception {
        SupplierPaymentModel castRequestModel =
                Core.getRequestObject(requestMessage, SupplierPaymentModel.class);
        SupplierPaymentModel processedModel = null;
        try {
            if (castRequestModel != null) {
                processedModel = this.softDelete(castRequestModel);
                if (processedModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Successfully deleted the requested SupplierPayment";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to deleted the requested SupplierPayment";
                }
            }

        } catch (Exception ex) {
            log.error("SupplierPaymentBllManager -> delete got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return processedModel;
    }

    private Double getBalanceBySupplierID(Integer supplierID, Integer referenceType) throws Exception {
        JournalModel whereConditionJournalModel;
        List<JournalModel> journalModelList;
        Double debitTotal = 0.0, creditTotal = 0.0, balance = 0.0;
        whereConditionJournalModel = new JournalModel();
        whereConditionJournalModel.setPartyID(supplierID);
        whereConditionJournalModel.setReferenceType(referenceType);

        journalModelList = this.coreJournalBllManager.getAllByConditionWithActive(whereConditionJournalModel);
        for (JournalModel journalModel : journalModelList) {
            if (journalModel.getDrCrIndicator() == DebitCreditIndicator.Debit.get()) {
                debitTotal += journalModel.getAmount();
            }

            if (journalModel.getDrCrIndicator() == DebitCreditIndicator.Credit.get()) {
                creditTotal += journalModel.getAmount();
            }
        }

        balance = creditTotal - debitTotal;

        return balance;

    }

    public List<SupplierPaymentModel> getUnAllocatedSupplierPayments(Integer supplierID, Integer businessID) throws Exception {
        List<SupplierPaymentModel> lstSupplierPaymentModel;
        try {

            String hql = "FROM SupplierPayment SP WHERE SP.status = " + TillBoxAppEnum.Status.Active.get() + " AND SP.businessID = " + businessID + " AND SP.supplierID = " + supplierID + " AND SP.paidAmount " +
                    "> (SELECT COALESCE(SUM(SPD.paidAmount),0) FROM SupplierPaymentDetail SPD WHERE SPD.status = " + TillBoxAppEnum.Status.Active.get() + ")";
            lstSupplierPaymentModel = this.executeHqlQuery(hql, SupplierPaymentModel.class, TillBoxAppEnum.QueryType.Select.get());

        } catch (Exception ex) {
            log.error("SupplierPaymentBllManager -> getUnAllocatedPayments got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstSupplierPaymentModel;
    }

    private void journalEntry(JournalModel drJournalModel, JournalModel crJournalModel) throws Exception {
        Boolean isCompleted = false;
        List<JournalModel> journalModelList = new ArrayList<>();
        journalModelList.add(drJournalModel);
        journalModelList.add(crJournalModel);


        isCompleted = this.coreJournalBllManager.saveOrUpdate(journalModelList);
        if (!isCompleted) {
            throw new Exception(Core.clientMessage.get().userMessage);
        }
    }

}