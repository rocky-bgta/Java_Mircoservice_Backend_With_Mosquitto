/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 12-Mar-18
 * Time: 5:45 PM
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
import nybsys.tillboxweb.coreEnum.DebitCreditIndicator;
import nybsys.tillboxweb.coreEnum.DefaultCOA;
import nybsys.tillboxweb.coreEnum.PartyType;
import nybsys.tillboxweb.coreEnum.ReferenceType;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.coreModels.DocumentNumberModel;
import nybsys.tillboxweb.coreModels.JournalModel;
import nybsys.tillboxweb.coreModels.VATRateModel;
import nybsys.tillboxweb.coreUtil.CoreUtils;
import nybsys.tillboxweb.entities.CustomerReceive;
import nybsys.tillboxweb.models.*;
import nybsys.tillboxweb.sales_enum.PaymentStatus;
import nybsys.tillboxweb.service.manager.CustomerAdjustmentServiceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("ALL")
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomerReceiveBllManager extends BaseBll<CustomerReceive> {

    private static final Logger log = LoggerFactory.getLogger(CustomerReceiveBllManager.class);

    private CustomerAdjustmentServiceManager customerAdjustmentServiceManager = new CustomerAdjustmentServiceManager();

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(CustomerReceive.class);
        Core.runTimeModelType.set(CustomerReceiveModel.class);
    }

    //@Autowired
    private CoreJournalBllManager coreJournalBllManager = new CoreJournalBllManager();

    //@Autowired
    private CustomerReceiveDetailBllManager customerReceiveDetailBllManager = new CustomerReceiveDetailBllManager();
    private CustomerInvoiceBllManager customerInvoiceBllManager = new CustomerInvoiceBllManager();

    private CustomerReturnBllManager customerReturnBllManager;
    private CustomerReturnDetailBllManager customerReturnDetailBllManager;
    private CustomerAdjustmentDetailBllManager customerAdjustmentDetailBllManager;
    private CustomerWriteOffDetailBllManager customerWriteOffDetailBllManager;
    private CustomerInvoiceDetailBllManager customerInvoiceDetailBllManager;
    private CoreDocumentNumberBllManager coreDocumentNumberBllManager;
    private CoreCurrencyBllManager coreCurrencyBllManager;
    private CoreVATRateBllManager coreVATRateBllManager;


    public BllResponseMessage saveOrUpdate(RequestMessage requestMessage) throws Exception {

        //Boolean isOperationSuccess = false;
        BllResponseMessage bllResponseMessage = this.getDefaultBllResponse();

        CustomerReceiveModel reqCustomerReceiveModel, savedCustomerReceiveModel, updatedCustomerReceiveModel;
        CustomerReceiveDetailModel whereConditionCusRecModel;
        List<CustomerReceiveDetailModel> reqCustomerReceiveDetailModelList;
        VMCustomerReceive reqVMCustomerReceive, vmCustomerReceive, updatedVMCustomerReceive;
        CurrencyModel currencyModel;


        //get base currency and exchange rate
        currencyModel = this.customerAdjustmentServiceManager.getBaseCurrency();
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
        reqVMCustomerReceive = Core.getRequestObject(requestMessage, VMCustomerReceive.class);
        reqCustomerReceiveModel = reqVMCustomerReceive.customerReceiveModel;
        reqCustomerReceiveDetailModelList = reqVMCustomerReceive.customerReceiveDetailModelList;
        // Extract data from VM ======================================

        Integer primaryKeyValue = reqCustomerReceiveModel.getCustomerReceiveID();

        JournalModel drJournalModel, crJournalModel;
        List<JournalModel> journalModelList = new ArrayList<>();
        Boolean isJournalEntrySuccess;
        String buildDbSequence, currentDBSequence = null;
        String hsql;
        String journalReferenceNo;
        List<CustomerReceiveModel> customerReceiveModelList;
        List<CustomerReceiveDetailModel> saveOrUpdatedCustomerRecDetailList,
                updatedCustomerReceiveDetailModelList, previousCustomerRecDetailModelList;

        CustomerReceiveDetailModel temCustomerReceiveDetailModel;
        CustomerInvoiceModel customerInvoiceModel;

        Double discount = 0.0, amount = 0.0, previousDiscount = 0.0;
        Double unAllocateAmount = 0.0;
        Double totalAmount = 0.0;
        Double totalDiscount = 0.0;
        Double previousPaymentTotal = 0.0, currentPayment = 0.0;

        vmCustomerReceive = new VMCustomerReceive();
        saveOrUpdatedCustomerRecDetailList = new ArrayList<>();

        try {
            journalReferenceNo = TillBoxUtils.getUUID();

            if (primaryKeyValue == null || primaryKeyValue == 0) { //primaryKeyValue.equals(bigZero)) {
                // Save Code

                // ============================= Create RCP0000001 =============================
                hsql = "SELECT e FROM CustomerReceive e ORDER BY e.customerReceiveID DESC";
                customerReceiveModelList = this.executeHqlQuery(hsql, CustomerReceiveModel.class, TillBoxAppEnum.QueryType.GetOne.get());
                if (customerReceiveModelList.size() > 0) {
                    currentDBSequence = customerReceiveModelList.get(0).getCustomerReceiveNo();
                }
                buildDbSequence = CoreUtils.getSequence(currentDBSequence, "RCP");
                // ==========================End Create RCP0000001 =============================

                reqCustomerReceiveModel.setCustomerReceiveNo(buildDbSequence);
                reqCustomerReceiveModel.setDocNumber(buildDbSequence);

                //add currency
                reqCustomerReceiveModel.setBaseCurrencyID(currencyModel.getCurrencyID());
                reqCustomerReceiveModel.setEntryCurrencyID(requestMessage.entryCurrencyID);
                reqCustomerReceiveModel.setBaseCurrencyAmount(reqCustomerReceiveModel.getTotalAmount() * reqCustomerReceiveModel.getExchangeRate());

                //Save master table
                savedCustomerReceiveModel = this.save(reqCustomerReceiveModel);


                //Save detail table
                for (CustomerReceiveDetailModel item : reqCustomerReceiveDetailModelList) {
                    item.setCustomerReceiveID(savedCustomerReceiveModel.getCustomerReceiveID());


                    //======================= due calculation logic ==========================================================================
                    customerInvoiceModel = this.customerInvoiceBllManager.getByIdActiveStatus(item.getCustomerInvoiceID());
                    // if payment status is due then
                    if (customerInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                            || customerInvoiceModel.getPaymentStatus() == PaymentStatus.Partial.get()) {

                        whereConditionCusRecModel = new CustomerReceiveDetailModel();
                        whereConditionCusRecModel.setCustomerInvoiceID(item.getCustomerInvoiceID());
                        previousCustomerRecDetailModelList =
                                this.customerReceiveDetailBllManager.getAllByConditionWithActive(whereConditionCusRecModel);

                        for (CustomerReceiveDetailModel previousCustomerRecDetailModel : previousCustomerRecDetailModelList) {
                            previousPaymentTotal += previousCustomerRecDetailModel.getDiscount() + previousCustomerRecDetailModel.getAmount();
                            previousDiscount += previousCustomerRecDetailModel.getDiscount();
                        }

                        currentPayment = item.getAmount() + item.getDiscount();
                        currentPayment = currentPayment + previousPaymentTotal;


                        if (customerInvoiceModel.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in CustomerInvoic Table of Column (paymentStatus)
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        } else if (customerInvoiceModel.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in CustomerInvoic Table of Column (paymentStatus)
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Partial.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        }
                        if (customerInvoiceModel.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in CustomerInvoic Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in CustomerInvoic Table
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                            unAllocateAmount = currentPayment - customerInvoiceModel.getTotalAmount();
                        }

                    }
                    //======================= End Due calculation logic ======================================================================

                    //Save detail table data
                    temCustomerReceiveDetailModel = this.customerReceiveDetailBllManager.saveOrUpdate(item);

                    // Hold datail table data for return
                    saveOrUpdatedCustomerRecDetailList.add(temCustomerReceiveDetailModel);


                    amount = item.getAmount();
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

              /*  if (totalAmount > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            null,
                            null,
                            totalAmount,
                            savedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeDebtors.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            savedCustomerReceiveModel.getCustomerID(),
                            PartyType.Customer.get(),
                            totalAmount,
                            savedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal entry for amount end =============================


                //=============== Journal Entry for discount ===============================
                //if(discount>0.0){
               /* if (totalDiscount > 0.0) {
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.DiscountGiven.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            null,
                            null,
                            totalDiscount,
                            savedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeDebtors.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            savedCustomerReceiveModel.getCustomerID(),
                            PartyType.Customer.get(),
                            totalDiscount,
                            savedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal for discount Entry End =============================


                savedCustomerReceiveModel.setTotalAmount(totalAmount);
                savedCustomerReceiveModel.setBaseCurrencyAmount(totalAmount);
                savedCustomerReceiveModel.setTotalDiscount(totalDiscount);
                savedCustomerReceiveModel.setUnAllocatedAmount(unAllocateAmount);
                savedCustomerReceiveModel = this.update(savedCustomerReceiveModel);


                //update VM for return
                vmCustomerReceive.customerReceiveModel = savedCustomerReceiveModel;
                vmCustomerReceive.customerReceiveDetailModelList = saveOrUpdatedCustomerRecDetailList;

                if (savedCustomerReceiveModel != null && saveOrUpdatedCustomerRecDetailList.size() > 0) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Customer Receive Save Successfully";

                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Save CustomerReceive";
                }
            } else {
                //Update Code

                // Update master table
                updatedCustomerReceiveModel = this.update(reqCustomerReceiveModel);
                totalAmount = 0.0;

                //================ Delete Journal before update detail table ========================
                Integer referenceID, referenceType;
                JournalModel journalModelForDelete = new JournalModel();

                journalModelList = new ArrayList<>();

                referenceID = updatedCustomerReceiveModel.getCustomerReceiveID();
                referenceType = ReferenceType.CustomerReceipt.get();

                journalModelForDelete.setReferenceID(referenceID);
                journalModelForDelete.setStatus(TillBoxAppEnum.Status.Active.get());
                journalModelForDelete.setReferenceType(referenceType);
                journalModelList = this.coreJournalBllManager.getAllByConditions(journalModelForDelete);

                for (JournalModel journalModel : journalModelList) {
                    this.coreJournalBllManager.softDelete(journalModel);
                }
                //=====================================================================================

                // Update detail table============================================================
                for (CustomerReceiveDetailModel item : reqCustomerReceiveDetailModelList) {


                    //======================= due calculation logic ==========================================================================
                    customerInvoiceModel = this.customerInvoiceBllManager.getByIdActiveStatus(item.getCustomerInvoiceID());
                    // if payment status is due then
                    if (customerInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                            || customerInvoiceModel.getPaymentStatus() == PaymentStatus.Partial.get()) {

                        whereConditionCusRecModel = new CustomerReceiveDetailModel();
                        whereConditionCusRecModel.setCustomerInvoiceID(item.getCustomerInvoiceID());
                        previousCustomerRecDetailModelList =
                                this.customerReceiveDetailBllManager.getAllByConditionWithActive(whereConditionCusRecModel);

                        for (CustomerReceiveDetailModel previousCustomerRecDetailModel : previousCustomerRecDetailModelList) {
                            previousPaymentTotal += previousCustomerRecDetailModel.getDiscount() + previousCustomerRecDetailModel.getAmount();
                            previousDiscount += previousCustomerRecDetailModel.getDiscount();
                        }

                        currentPayment = item.getAmount() + item.getDiscount();
                        currentPayment = currentPayment + previousPaymentTotal;


                        if (customerInvoiceModel.getTotalAmount().doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in CustomerInvoic Table of Column (paymentStatus)
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        } else if (customerInvoiceModel.getTotalAmount().doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in CustomerInvoic Table of Column (paymentStatus)
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Partial.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        }
                        if (customerInvoiceModel.getTotalAmount().doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in CustomerInvoic Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in CustomerInvoic Table
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                            unAllocateAmount = currentPayment - customerInvoiceModel.getTotalAmount();
                        }

                    }
                    //======================= End Due calculation logic ======================================================================


                    //update detail table
                    temCustomerReceiveDetailModel = this.customerReceiveDetailBllManager.saveOrUpdate(item);
                    // Hold datail table data for return
                    saveOrUpdatedCustomerRecDetailList.add(temCustomerReceiveDetailModel);

                    amount = item.getAmount();
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

               /* if (totalAmount > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            updatedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            null,
                            null,
                            totalAmount,
                            updatedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeDebtors.get(),
                            updatedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            updatedCustomerReceiveModel.getCustomerID(),
                            PartyType.Customer.get(),
                            totalAmount,
                            updatedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal entry for amount end =============================


                //=============== Journal Entry for discount ===============================
               /* if (totalDiscount > 0.0) {
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.BankAccount.get(),
                            updatedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            null,
                            null,
                            totalDiscount,
                            updatedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );

                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeDebtors.get(),
                            updatedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            updatedCustomerReceiveModel.getCustomerID(),
                            PartyType.Customer.get(),
                            totalDiscount,
                            updatedCustomerReceiveModel.getNote(),
                            currencyModel,
                            reqCustomerReceiveModel.getExchangeRate(),
                            requestMessage.entryCurrencyID
                    );
                    this.journalEntry(drJournalModel, crJournalModel);
                }*/
                //=============== Journal for discount Entry End =============================


                updatedCustomerReceiveModel.setTotalAmount(totalAmount);
                updatedCustomerReceiveModel.setBaseCurrencyAmount(totalAmount);
                updatedCustomerReceiveModel.setTotalDiscount(totalDiscount);
                updatedCustomerReceiveModel.setUnAllocatedAmount(unAllocateAmount);
                updatedCustomerReceiveModel = this.update(updatedCustomerReceiveModel);


                vmCustomerReceive.customerReceiveModel = updatedCustomerReceiveModel;
                vmCustomerReceive.customerReceiveDetailModelList = saveOrUpdatedCustomerRecDetailList;


                //updatedVMCustomerReceive = new VMCustomerReceive();
                if (updatedCustomerReceiveModel != null && saveOrUpdatedCustomerRecDetailList.size() > 0) {
                    Core.clientMessage.get().userMessage = "CustomerReceive Update Successfully";
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to Update CustomerReceive";
                }
            }


            bllResponseMessage.responseObject = vmCustomerReceive;
            bllResponseMessage.responseCode = Core.clientMessage.get().messageCode;
            bllResponseMessage.message = Core.clientMessage.get().userMessage;

        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> saveOrUpdate got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return bllResponseMessage;
    }

    public BllResponseMessage getUnpaidCustomerInvoicesByCustomerID(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage;
        BllResponseMessage bllResponseMessage = new BllResponseMessage();


        //this.customerReceiveDetailBllManager = new CustomerReceiveDetailBllManager();
        this.customerReturnBllManager = new CustomerReturnBllManager();
        this.customerReturnDetailBllManager = new CustomerReturnDetailBllManager();
        this.customerAdjustmentDetailBllManager = new CustomerAdjustmentDetailBllManager();
        this.customerWriteOffDetailBllManager = new CustomerWriteOffDetailBllManager();
        this.customerInvoiceDetailBllManager = new CustomerInvoiceDetailBllManager();


        VMUnpaidCustomerInvoice vmUnpaidCustomerInvoice;
        UnPaidCustomerInvoice unPaidCustomerInvoice;
        List<UnPaidCustomerInvoice> unPaidCustomerInvoiceArrayList = new ArrayList<>();

        CustomerModel customerModel;
        CustomerInvoiceModel whereConditionCustomerInvoiceModel;
        CustomerInvoiceDetailModel whereConditionCustomerInvoiceDetailModel;

        List<CustomerInvoiceModel> customerInvoiceModelList;
        List<CustomerInvoiceDetailModel> customerInvoiceDetailModelList;


        CustomerReceiveModel customerReceiveModel, whereConditionCustomerReceiveModel;

        Double totalAmount = 0.0;
        Double dueAmount = 0.0, paidAmount = 0.0, balance = 0.0;

        String documentNumber;


        Integer customerID, customerInvoiceID;


        Double customerInvoiceDetailTotal = 0.0, customerReturnTotal = 0.0, customerAdjustmentTotal = 0.0;
        Double customerWriteOffDetailTotal = 0.0, customerReceiveDetailTotal = 0.0;
        Double finalTotal = 0.0;

        CustomerReturnModel whereConditionCustomerReturnModel;
        List<CustomerReturnModel> customerReturnModelList;

        CustomerReturnDetailModel whereConditionCustomerReturnDetailModel;
        List<CustomerReturnDetailModel> customerReturnDetailModelList;

        CustomerAdjustmentDetailModel whereConditionCustomerAdjustmentDetailModel;
        List<CustomerAdjustmentDetailModel> customerAdjustmentDetailModelList;

        CustomerReceiveDetailModel whereConditionCustomerReceiveDetailModel;
        List<CustomerReceiveDetailModel> customerReceiveDetailModelList;

        CustomerWriteOffDetailModel whereConditionCustomerWriteOffDetailModel;
        List<CustomerWriteOffDetailModel> customerWriteOffDetailModelList;

        try {
            customerModel = Core.getRequestObject(requestMessage, CustomerModel.class);
            whereConditionCustomerInvoiceModel = new CustomerInvoiceModel();
            whereConditionCustomerInvoiceModel.setCustomerID(customerModel.getCustomerID());

            customerInvoiceModelList = this.customerInvoiceBllManager.getAllByConditions(whereConditionCustomerInvoiceModel);

            for (CustomerInvoiceModel customerInvoiceModel : customerInvoiceModelList) {

                if (customerInvoiceModel.getPaymentStatus() == PaymentStatus.Unpaid.get()
                        || customerInvoiceModel.getPaymentStatus() == PaymentStatus.Partial.get()) {

                    customerInvoiceID = customerInvoiceModel.getCustomerInvoiceID();

                    // =================== calculate Invoice total =================================
                    whereConditionCustomerInvoiceDetailModel = new CustomerInvoiceDetailModel();
                    whereConditionCustomerInvoiceDetailModel.setCustomerInvoiceID(customerInvoiceID);


                    customerInvoiceDetailModelList = this.customerInvoiceDetailBllManager.getAllByConditionWithActive(whereConditionCustomerInvoiceDetailModel);

                    for (CustomerInvoiceDetailModel customerInvoiceDetailModel : customerInvoiceDetailModelList) {
                        customerInvoiceDetailTotal += ((customerInvoiceDetailModel.getQuantity() * customerInvoiceDetailModel.getUnitPrice())
                                - customerInvoiceDetailModel.getDiscount()) + customerInvoiceDetailModel.getVat();
                    }
                    // =================== calculate Invoice total =================================


                    // =================== calculate return total =================================
                    whereConditionCustomerReturnModel = new CustomerReturnModel();
                    whereConditionCustomerReturnModel.setCustomerInvoiceID(customerInvoiceID);

                    customerReturnModelList = this.customerReturnBllManager.getAllByConditionWithActive(whereConditionCustomerReturnModel);
                    for (CustomerReturnModel customerReturnModel : customerReturnModelList) {

                        whereConditionCustomerReturnDetailModel = new CustomerReturnDetailModel();
                        whereConditionCustomerReturnDetailModel.setCustomerReturnID(customerReturnModel.getCustomerReturnID());

                        customerReturnDetailModelList = this.customerReturnDetailBllManager.getAllByConditionWithActive(whereConditionCustomerReturnDetailModel);

                        for (CustomerReturnDetailModel customerReturnDetailModel : customerReturnDetailModelList) {
                            customerReturnTotal += ((customerReturnDetailModel.getQuantity() * customerReturnDetailModel.getUnitPrice())
                                    - customerReturnDetailModel.getDiscount()) + customerReturnDetailModel.getVat();
                        }
                    }
                    // =================== calculate return total =================================


                    // =================== calculate Customer Adjustment Detail total =================================
                    whereConditionCustomerAdjustmentDetailModel = new CustomerAdjustmentDetailModel();
                    whereConditionCustomerAdjustmentDetailModel.setCustomerInvoiceID(customerInvoiceID);

                    customerAdjustmentDetailModelList = this.customerAdjustmentDetailBllManager.getAllByConditionWithActive(whereConditionCustomerAdjustmentDetailModel);
                    for (CustomerAdjustmentDetailModel customerAdjustmentDetailModel : customerAdjustmentDetailModelList) {
                        customerAdjustmentTotal += customerAdjustmentDetailModel.getAdjustAmount();
                    }
                    // =================== calculate Customer Adjustment Detail total =================================


                    //==================== calculate Customer Receive Detail total ====================================
                    whereConditionCustomerReceiveDetailModel = new CustomerReceiveDetailModel();
                    whereConditionCustomerReceiveDetailModel.setCustomerInvoiceID(customerInvoiceID);
                    customerReceiveDetailModelList = this.customerReceiveDetailBllManager.getAllByConditionWithActive(whereConditionCustomerReceiveDetailModel);
                    for (CustomerReceiveDetailModel customerReceiveDetailModel : customerReceiveDetailModelList) {
                        customerReceiveDetailTotal += customerReceiveDetailModel.getAmount() + customerReceiveDetailModel.getDiscount();
                    }
                    //==================== calculate Customer Receive Detail total ====================================


                    //==================== calculate Customer Write Off total =========================================
                    whereConditionCustomerWriteOffDetailModel = new CustomerWriteOffDetailModel();
                    whereConditionCustomerWriteOffDetailModel.setCustomerInvoiceID(customerInvoiceID);
                    customerWriteOffDetailModelList = this.customerWriteOffDetailBllManager.getAllByConditionWithActive(whereConditionCustomerWriteOffDetailModel);
                    for (CustomerWriteOffDetailModel customerWriteOffDetailModel : customerWriteOffDetailModelList) {
                        customerWriteOffDetailTotal += customerWriteOffDetailModel.getAmount();
                    }
                    //==================== calculate Customer Write Off total =========================================


                    finalTotal = customerInvoiceDetailTotal -
                            (customerReturnTotal + customerAdjustmentTotal + customerWriteOffDetailTotal + customerReceiveDetailTotal);

                    unPaidCustomerInvoice = new UnPaidCustomerInvoice();
                    unPaidCustomerInvoice.customerInvoiceID = customerInvoiceID;
                    unPaidCustomerInvoice.amountDue = finalTotal;
                    unPaidCustomerInvoice.total = customerInvoiceDetailTotal;
                    unPaidCustomerInvoice.date = customerInvoiceModel.getInvoiceDate();
                    //unPaidCustomerInvoice.documentNumber = "INV0000001";
                    unPaidCustomerInvoice.allocate = false;

                    finalTotal = 0.0;
                    customerInvoiceDetailTotal = 0.0;
                    customerReturnTotal = 0.0;
                    customerAdjustmentTotal = 0.0;
                    customerWriteOffDetailTotal = 0.0;
                    customerReceiveDetailTotal = 0.0;
                }

            }


            //========= Test Data ======================
            unPaidCustomerInvoice = new UnPaidCustomerInvoice();
            unPaidCustomerInvoice.customerInvoiceID = 1;
            unPaidCustomerInvoice.amountDue = 5000.0;
            unPaidCustomerInvoice.total = 1000.0;
            unPaidCustomerInvoice.date = new Date();
            unPaidCustomerInvoice.documentNumber = "INV0000001";
            unPaidCustomerInvoice.allocate = false;

            unPaidCustomerInvoiceArrayList.add(unPaidCustomerInvoice);

            unPaidCustomerInvoice = new UnPaidCustomerInvoice();
            unPaidCustomerInvoice.customerInvoiceID = 2;
            unPaidCustomerInvoice.amountDue = 6000.0;
            unPaidCustomerInvoice.total = 7000.0;
            unPaidCustomerInvoice.date = new Date();
            unPaidCustomerInvoice.documentNumber = "INV0000002";
            unPaidCustomerInvoice.allocate = false;

            unPaidCustomerInvoiceArrayList.add(unPaidCustomerInvoice);

            unPaidCustomerInvoice = new UnPaidCustomerInvoice();
            unPaidCustomerInvoice.customerInvoiceID = 3;
            unPaidCustomerInvoice.amountDue = 2000.0;
            unPaidCustomerInvoice.total = 7000.0;
            unPaidCustomerInvoice.date = new Date();
            unPaidCustomerInvoice.documentNumber = "INV0000003";
            unPaidCustomerInvoice.allocate = false;

            unPaidCustomerInvoiceArrayList.add(unPaidCustomerInvoice);

            //========= End Test Data ======================


            //balance = this.getBalanceByCustomerID(customerModel.getCustomerID(), ReferenceType.Customer.get());
            // Test Data
            balance = 80000.0;
            vmUnpaidCustomerInvoice = new VMUnpaidCustomerInvoice();
            vmUnpaidCustomerInvoice.unPaidCustomerInvoiceList = unPaidCustomerInvoiceArrayList;
            vmUnpaidCustomerInvoice.balance = balance;
/*


            if (customerModel.pageAction == PageAction.Edit.get()) {
                whereConditionCustomerReceiveModel = new CustomerReceiveModel();
                //whereConditionCustomerReceiveModel.setSupplierPaymentID(customerModel.supplierPaymentID);
                whereConditionCustomerReceiveModel.setCustomerReceiveID(customerModel.getCustomerID());
                customerReceiveModel = this.getAllByConditionWithActive(whereConditionCustomerReceiveModel).get(0);

                vmUnpaidCustomerInvoice.coaID = customerReceiveModel.getAccountID();
                vmUnpaidCustomerInvoice.description = customerReceiveModel.getDescription();
                vmUnpaidCustomerInvoice.paymentDate = customerReceiveModel.getDateTime();
                vmUnpaidCustomerInvoice.reference = customerReceiveModel.getReference();
                //vmUnpaidCustomerInvoice.paymentAmount = supplierPaymentModel.getPaidAmount();
                vmUnpaidCustomerInvoice.unAllocateAmount = customerReceiveModel.getUnAllocatedAmount();
            }
*/


            bllResponseMessage.responseObject = vmUnpaidCustomerInvoice;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return bllResponseMessage;

    }

    public BllResponseMessage saveUnpaidCustomerInvoicesByCustomerID(RequestMessage requestMessage) throws Exception {
        RequestMessage reqMessageForBllManager;
        ResponseMessage responseMessage = new ResponseMessage();
        BllResponseMessage bllResponseMessage = new BllResponseMessage();

        this.coreVATRateBllManager = new CoreVATRateBllManager();
        this.coreDocumentNumberBllManager = new CoreDocumentNumberBllManager();
        this.coreCurrencyBllManager = new CoreCurrencyBllManager();


        VMUnpaidCustomerInvoice vmUnpaidCustomerInvoice;
        List<UnPaidCustomerInvoice> unPaidCustomerInvoiceList;
        List<CustomerReceiveModel> customerReceiveModelList;
        CurrencyModel whereConditionCurrencyModel, currencyModel;
        List<CurrencyModel> currencyModelList;
        CustomerReceiveModel customerReceiveModel = new CustomerReceiveModel(),
                savedCustomerReceiveModel = null, customerReceiveForEdit, whereConditionCustomerReceiveModel;

        Double amountDue = 0.0, amountPaid = 0.0, discount = 0.0, currentPayment = 0.0;
        Double discountTotal = 0.0, amountPaidTotal = 0.0, actualPaidAmount = 0.0;
        Double amountReceive = 0.0;

        Integer documentTypeID;
        DocumentNumberModel whereConditionDocumentNumberModel, documentNumberModel;
        String firstThreeChar;
        String buildDbSequence, currentDBSequence = null, hsql;

        CustomerReceiveDetailModel customerReceiveDetailModel, whereConditionCustomerReceiveDetailModel;
        CustomerReceiveDetailModel savedCustomerReceiveDetailModel;
        List<CustomerReceiveDetailModel> customerReceiveDetailModelList;

        CustomerInvoiceModel customerInvoiceModel, whereConditionCustomerInvoiceModel;

        VATRateModel vatRateModel, whereConditionVatRateModel;
        Double vatRate = 1.0, govtVatAmount = 0.0;
        Double unAllocateAmount = 0.0;

        Double temDiscount = 0.0, temPaidAmount = 0.0, temAmountReceive = 0.0, temAmountDue = 0.0;
        String note;
        Boolean pageInEditState = false;
        Integer customerReceiveIDForEdit;

        JournalModel whereConditionJournalModel;
        JournalModel journalModelToUpdate;

        Double amountReceivedTotal = 0.0, amountDueTotal = 0.0;
        Double receiveAmount = 0.0;

        try {

            JournalModel drJournalModel, crJournalModel;
            String journalReferenceNo;
            journalReferenceNo = TillBoxUtils.getUUID();

            vmUnpaidCustomerInvoice = Core.getRequestObject(requestMessage, VMUnpaidCustomerInvoice.class);
            unPaidCustomerInvoiceList = vmUnpaidCustomerInvoice.unPaidCustomerInvoiceList;

            pageInEditState = vmUnpaidCustomerInvoice.pageInEditState;
            customerReceiveIDForEdit = vmUnpaidCustomerInvoice.customerReceiveID;
            note = vmUnpaidCustomerInvoice.comment;

            unAllocateAmount = vmUnpaidCustomerInvoice.unAllocateAmount;

            receiveAmount = vmUnpaidCustomerInvoice.receiveAmount;

            documentTypeID = vmUnpaidCustomerInvoice.documentTypeID;
            whereConditionDocumentNumberModel = new DocumentNumberModel();
            whereConditionDocumentNumberModel.setDocumentTypeID(documentTypeID);

            documentNumberModel = this.coreDocumentNumberBllManager.getAllByConditionWithActive(whereConditionDocumentNumberModel).get(0);
            firstThreeChar = StringUtils.substring(documentNumberModel.getCurrentDocumentNumber().toString(), 0, 3);

            //DocumentTypeID = 4
            // ============================= Create RCP0000001 =============================
            hsql = hsql = "SELECT e FROM CustomerReceive e ORDER BY e.customerReceiveID DESC";
            customerReceiveModelList = this.executeHqlQuery(hsql, CustomerReceiveModel.class, TillBoxAppEnum.QueryType.GetOne.get());
            if (customerReceiveModelList.size() > 0) {
                currentDBSequence = customerReceiveModelList.get(0).getDocNumber();
            }
            buildDbSequence = CoreUtils.getSequence(currentDBSequence, firstThreeChar);
            // ==========================End Create RCP0000001 =============================


            if (unPaidCustomerInvoiceList != null && unPaidCustomerInvoiceList.size() > 0) {
                for (UnPaidCustomerInvoice unPaidCustomerInvoice : unPaidCustomerInvoiceList) {

                    temAmountReceive = (unPaidCustomerInvoice.amountReceived != null) ? unPaidCustomerInvoice.amountReceived : 0.0;
                    temAmountDue = (unPaidCustomerInvoice.amountDue != null) ? unPaidCustomerInvoice.amountDue : 0.0;
                    temDiscount = (unPaidCustomerInvoice.discount != null) ? unPaidCustomerInvoice.discount : 0.0;


                    amountReceivedTotal+= temAmountReceive;
                    amountDueTotal+= temAmountDue;
                    discountTotal+= temDiscount;
                    //amountPaidTotal += temPaidAmount;

                    actualPaidAmount = temAmountReceive + temDiscount;

                    if (unPaidCustomerInvoice.amountDue < actualPaidAmount) {
                        bllResponseMessage.responseObject = "";
                        bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        bllResponseMessage.message = "Received amount greater than due amount";
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
            //*  currencyModel = this.productServiceManager.getBaseCurrency();
            if (currencyModel == null) {
                bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                bllResponseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return bllResponseMessage;
            }

            //check entry currency is present if not base currency will be entry currency
            if (requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0) {
                requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
            }


            if (currencyModel != null) {

                if (pageInEditState) {
                    whereConditionCustomerReceiveModel = new CustomerReceiveModel();
                    whereConditionCustomerReceiveModel.setCustomerReceiveID(customerReceiveIDForEdit);
                    customerReceiveForEdit = this.getAllByConditionWithActive(whereConditionCustomerReceiveModel).get(0);


                   /* whereConditionCustomerReceiveDetailModel = new CustomerReceiveDetailModel();
                    whereConditionCustomerReceiveDetailModel.setCustomerReceiveID(customerReceiveForEdit.getCustomerReceiveID());
                    customerReceiveDetailModelList = this.customerReceiveDetailBllManager.getAllByConditions(whereConditionCustomerReceiveDetailModel);

                    if (customerReceiveDetailModelList != null && customerReceiveDetailModelList.size() > 0) {
                        bllResponseMessage.responseObject = null;
                        bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        bllResponseMessage.message = "Update not allowed";
                        return bllResponseMessage;
                    } else {
                        customerReceiveModel = customerReceiveForEdit;
                    }*/

                    customerReceiveModel = customerReceiveForEdit;
                }

                customerReceiveModel.setBaseCurrencyID(currencyModel.getCurrencyID());
                customerReceiveModel.setEntryCurrencyID(currencyModel.getCurrencyID());
                customerReceiveModel.setExchangeRate(currencyModel.getExchangeRate());
                customerReceiveModel.setAccountID(vmUnpaidCustomerInvoice.coaID);
                customerReceiveModel.setBusinessID(requestMessage.businessID);
                customerReceiveModel.setDateTime(vmUnpaidCustomerInvoice.paymentDate);
                customerReceiveModel.setDescription(vmUnpaidCustomerInvoice.description);
                customerReceiveModel.setDocNumber(buildDbSequence);
                customerReceiveModel.setPaymentMethod(vmUnpaidCustomerInvoice.paymentMethod);
                customerReceiveModel.setCustomerID(vmUnpaidCustomerInvoice.customerID);
                //customerReceiveModel.setSupplierPaymentNumber(buildDbSequence);
                customerReceiveModel.setTotalDiscount(discountTotal);
                customerReceiveModel.setTotalAmount(vmUnpaidCustomerInvoice.receiveAmount);
                customerReceiveModel.setReference(vmUnpaidCustomerInvoice.reference);
                //customerReceiveModel.setPaidAmount(amountPaidTotal + discountTotal + vmUnpaidCustomerInvoice.unAllocateAmount);
                customerReceiveModel.setUnAllocatedAmount(vmUnpaidCustomerInvoice.unAllocateAmount);


                if (pageInEditState) {
                    savedCustomerReceiveModel = this.update(customerReceiveModel);
                } else {
                    savedCustomerReceiveModel = this.save(customerReceiveModel);
                }

            }


            if (pageInEditState) {
                // page in update state do nothing
                ;
            } else {
                // page in save state

                if (unPaidCustomerInvoiceList != null && unPaidCustomerInvoiceList.size() > 0) {
                    for (UnPaidCustomerInvoice unPaidCustomerInvoice : unPaidCustomerInvoiceList) {

                        customerReceiveDetailModel = new CustomerReceiveDetailModel();
                        //customerReceiveDetailModel.setReferenceID(unPaidCustomerInvoice.supplierInvoiceID);
                        //customerReceiveDetailModel.setSupplierPaymentID(savedCustomerReceiveModel.getSupplierPaymentID());

                        if (unPaidCustomerInvoice.discount != null)
                            customerReceiveDetailModel.setDiscount(unPaidCustomerInvoice.discount);
                        customerReceiveDetailModel.setAmount(unPaidCustomerInvoice.amountReceived);
                        customerReceiveDetailModel.setCustomerReceiveID(savedCustomerReceiveModel.getCustomerReceiveID());
                        customerReceiveDetailModel.setCustomerInvoiceID(unPaidCustomerInvoice.customerInvoiceID);


                        this.customerReceiveDetailBllManager.save(customerReceiveDetailModel);

                        if (unPaidCustomerInvoice.amountDue != null)
                            amountDue = unPaidCustomerInvoice.amountDue;

                        if (unPaidCustomerInvoice.amountReceived != null)
                            amountReceive = unPaidCustomerInvoice.amountReceived;

                        if (unPaidCustomerInvoice.discount != null)
                            discount = unPaidCustomerInvoice.discount;
                        else discount = 0.0;

                        currentPayment = 0.0;
                        currentPayment = amountReceive + discount;

                        whereConditionCustomerInvoiceModel = new CustomerInvoiceModel();
                        whereConditionCustomerInvoiceModel.setCustomerInvoiceID(unPaidCustomerInvoice.customerInvoiceID);
                        customerInvoiceModel = this.customerInvoiceBllManager.getAllByConditionWithActive(whereConditionCustomerInvoiceModel).get(0);

                        if (amountDue.doubleValue() == currentPayment.doubleValue()) {
                            //set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            //supplierInvoiceModel = new SupplierInvoiceModel();
                            customerInvoiceModel.setCustomerInvoiceID(unPaidCustomerInvoice.customerInvoiceID);
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            customerInvoiceModel.setNote(note);
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        } else if (amountDue.doubleValue() > currentPayment.doubleValue()) {
                            //set paid status (Partial) in SupplierInvoice Table of Column (paymentStatus)
                            //supplierInvoiceModel = new SupplierInvoiceModel();
                            customerInvoiceModel.setCustomerInvoiceID(unPaidCustomerInvoice.customerInvoiceID);
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Partial.get());
                            customerInvoiceModel.setNote(note);
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        } else if (amountDue.doubleValue() < currentPayment.doubleValue()) {
                            // set paid status (paid) in SupplierInvoice Table of Column (paymentStatus)
                            // and calculate the excess amount and set is as unallocate amount in supplierPayment Table
                            customerInvoiceModel.setCustomerInvoiceID(unPaidCustomerInvoice.customerInvoiceID);
                            customerInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                            customerInvoiceModel.setNote(note);
                            this.customerInvoiceBllManager.update(customerInvoiceModel);
                        }

                    }
                }


                /*

                //get base currency and exchange rate
                //*  currencyModel = this.productServiceManager.getBaseCurrency();
                if (currencyModel == null) {
                    bllResponseMessage.responseObject = null;
                    bllResponseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    bllResponseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                    return bllResponseMessage;
                } else {
                    currencyModel.setExchangeRate(1.00);
                }

                //check entry currency is present if not base currency will be entry currency
                if (requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0) {
                    requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
                }

                */



                if (pageInEditState) {
                    // page in update state
                    whereConditionJournalModel = new JournalModel();
                    whereConditionJournalModel.setReferenceType(ReferenceType.CustomerReceipt.get());
                    whereConditionJournalModel.setReferenceID(customerReceiveIDForEdit);
                    whereConditionCurrencyModel.setStatus(TillBoxAppEnum.Status.Active.get());

                    journalModelToUpdate = new JournalModel();
                    journalModelToUpdate.setStatus(TillBoxAppEnum.Status.Deleted.get());

                    this.coreCurrencyBllManager.updateByConditions(whereConditionJournalModel, journalModelToUpdate);
                }

                //Master Received amount
                if (receiveAmount > 0.0) {
                    //=============== Journal Entry for amount  ============================
                    drJournalModel = CoreUtils.buildDrJournalEntry(
                            journalReferenceNo,
                            vmUnpaidCustomerInvoice.coaID,//DefaultCOA.TradeCreditors.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            null,//savedCustomerReceiveModel.getCustomerID(),
                            null,//PartyType.Customer.get(),
                            receiveAmount,
                            note,
                            currencyModel,
                            requestMessage.entryCurrencyID
                    );


                    crJournalModel = CoreUtils.buildCrJournalEntry(
                            journalReferenceNo,
                            DefaultCOA.TradeDebtors.get(),
                            savedCustomerReceiveModel.getCustomerReceiveID(),
                            ReferenceType.CustomerReceipt.get(),
                            savedCustomerReceiveModel.getCustomerID(),
                            PartyType.Customer.get(),
                            receiveAmount,
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
                                DefaultCOA.DiscountGiven.get(),
                                savedCustomerReceiveModel.getCustomerReceiveID(),
                                ReferenceType.CustomerReceipt.get(),
                                null,//savedCustomerReceiveModel.getCustomerID(),
                                null,//PartyType.Supplier.get(),
                                discountTotal,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );

                        crJournalModel = CoreUtils.buildCrJournalEntry(
                                journalReferenceNo,
                                DefaultCOA.TradeDebtors.get(),
                                savedCustomerReceiveModel.getCustomerReceiveID(),
                                ReferenceType.CustomerReceipt.get(),
                                savedCustomerReceiveModel.getCustomerID(),//savedSupplierPaymentModel.getSupplierID(),
                                PartyType.Customer.get(),//PartyType.Supplier.get(),
                                discountTotal,
                                note,
                                currencyModel,
                                requestMessage.entryCurrencyID

                        );
                        this.journalEntry(drJournalModel, crJournalModel);
                    }

                    //=============== Journal for discount Entry End =============================
                }

              /*

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
                                savedCustomerReceiveModel.getCustomerReceiveID(),
                                ReferenceType.CustomerReceipt.get(),
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
                                savedCustomerReceiveModel.getCustomerReceiveID(),
                                ReferenceType.CustomerReceipt.get(),
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

                */


            }





            /*


            //=============== Journal Entry for un-allocate ===============================
            if (unAllocateAmount > 0.0) {
                drJournalModel = CoreUtils.buildDrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.TradeCreditors.get(),
                        savedCustomerReceiveModel.getCustomerReceiveID(),
                        ReferenceType.CustomerReceipt.get(),
                        savedCustomerReceiveModel.getCustomerID(),
                        PartyType.Supplier.get(),
                        unAllocateAmount,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID

                );

                crJournalModel = CoreUtils.buildCrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.BankAccount.get(),
                        savedCustomerReceiveModel.getCustomerReceiveID(),
                        ReferenceType.CustomerReceipt.get(),
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



            */



            bllResponseMessage.responseObject = vmUnpaidCustomerInvoice;
            bllResponseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            bllResponseMessage.message = TillBoxAppConstant.SUCCESS;


        } catch (Exception e) {
            log.error("Error", e);
        }

        return bllResponseMessage;

    }

    public List<CustomerReceiveModel> search(CustomerReceiveModel reqCustomerReceiveModel) throws Exception {
        List<CustomerReceiveModel> findCustomerReceiveList;
        try {
            findCustomerReceiveList = this.getAllByConditions(reqCustomerReceiveModel);
            if (findCustomerReceiveList.size() > 0) {
                //Core.clientMessage.get().userMessage = "Find the request CustomerReceive";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                Core.clientMessage.get().message = "Failed to find the requested CustomerReceive";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> search got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return findCustomerReceiveList;
    }

    public Integer deleteByConditions(RequestMessage requestMessage) throws Exception {
        CustomerReceiveModel req_CustomerReceiveModel =
                Core.getRequestObject(requestMessage, CustomerReceiveModel.class);
        Integer numberOfDeleteRow = 0;
        try {
            numberOfDeleteRow = this.deleteByConditions(req_CustomerReceiveModel);
            if (numberOfDeleteRow > 0) {
                //Core.clientMessage.get().userMessage = "Successfully deleted the requested CustomerReceive";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                Core.clientMessage.get().message = "Failed to deleted the requested CustomerReceive";
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> deleteByConditions got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return numberOfDeleteRow;
    }

    public CustomerReceiveModel inActive(RequestMessage requestMessage) throws Exception {
        CustomerReceiveModel reqCustomerReceiveModel =
                Core.getRequestObject(requestMessage, CustomerReceiveModel.class);
        CustomerReceiveModel _CustomerReceiveModel = null;
        try {
            if (reqCustomerReceiveModel != null) {
                _CustomerReceiveModel = this.inActive(reqCustomerReceiveModel);
                if (_CustomerReceiveModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    //Core.clientMessage.get().userMessage = "Successfully inactive the requested CustomerReceive";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to inactive the requested CustomerReceive";
                }
            }

        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> inActive got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return _CustomerReceiveModel;
    }

    public CustomerReceiveModel delete(CustomerReceiveModel reqCustomerReceiveModel) throws Exception {
        CustomerReceiveModel deletedCustomerReceiveModel = null;
        try {
            if (reqCustomerReceiveModel != null) {
                deletedCustomerReceiveModel = this.softDelete(reqCustomerReceiveModel);
                if (deletedCustomerReceiveModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    //Core.clientMessage.get().userMessage = "Successfully deleted the requested CustomerReceive";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to deleted the requested CustomerReceive";
                }
            }

        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> delete got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return deletedCustomerReceiveModel;
    }

    public CustomerReceiveModel getByReqId(CustomerReceiveModel reqCustomerReceiveModel) throws Exception {
        Integer primaryKeyValue = reqCustomerReceiveModel.getCustomerReceiveID();
        CustomerReceiveModel foundCustomerReceiveModel = null;
        try {
            if (primaryKeyValue != null) {
                foundCustomerReceiveModel = this.getById(primaryKeyValue);
                if (foundCustomerReceiveModel != null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.SUCCESS_CODE;
                    Core.clientMessage.get().userMessage = "Get the requested CustomerReceive successfully";
                } else {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = "Failed to the requested CustomerReceive";
                }
            }

        } catch (Exception ex) {
            log.error("CustomerReceiveBllManager -> getByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return foundCustomerReceiveModel;
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

    private Double getBalanceByCustomerID(Integer customerID, Integer referenceType) throws Exception {
        JournalModel whereConditionJournalModel;
        List<JournalModel> journalModelList;
        Double debitTotal = 0.0, creditTotal = 0.0, balance = 0.0;
        whereConditionJournalModel = new JournalModel();
        whereConditionJournalModel.setPartyID(customerID);
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
}