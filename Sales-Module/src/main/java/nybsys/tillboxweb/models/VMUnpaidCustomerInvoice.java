package nybsys.tillboxweb.models;

import nybsys.tillboxweb.coreEnum.PaymentMethod;

import java.util.Date;
import java.util.List;

public class VMUnpaidCustomerInvoice {

    public Integer customerID;
    public List<UnPaidCustomerInvoice> unPaidCustomerInvoiceList;
    public Double balance;

    public String customerReceiveDocumentNumber;
    public Double receiveAmount;
    public String reference;
    public Integer coaID;
    public String description;
    public String comment;
    public Date paymentDate;
    public Integer paymentMethod;
    public Boolean reconciled;
    public Double unAllocateAmount;
    public Integer documentTypeID;
    public VMUnpaidCustomerInvoice(){
        this.paymentMethod = PaymentMethod.Cash.get();
        this.reconciled=false;
    }
    public Integer customerReceiveID;
    public Boolean pageInEditState;
    public Boolean isPageEditable;

    public Integer supplierAdjustmentID;

}
