/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 22-May-18
 * Time: 1:41 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.coreEnum.PaymentMethod;

import java.util.Date;
import java.util.List;

public class VMUnPaidInvoicesModel {
    public Integer supplierID;
    public List<UnPaidInvoiceModel> unPaidInvoiceModelList;
    public Double balance;
    public Double paymentAmount;
    public String paymentDocumentNumber;
    public String reference;
    public Integer coaID;
    public String description;
    public String comment;
    public Date paymentDate;
    public Integer paymentMethod;
    public Boolean reconciled;
    public Double unAllocateAmount;
    public Integer documentTypeID;
    public VMUnPaidInvoicesModel(){
        this.paymentMethod = PaymentMethod.Cash.get();
        this.reconciled=false;
    }
    public Integer supplierPaymentID;
    public Boolean pageInEditState;
    public Boolean isPageEditable;

    public Integer supplierAdjustmentID;

    public Integer effectType;
    public Integer vatType;
    public Date adjustmentDate;

    public Double exclusive;
    public Double vat;
    public Double total;
}
