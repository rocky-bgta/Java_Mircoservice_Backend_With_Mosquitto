/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 23-May-18
 * Time: 1:37 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import java.util.Date;

public class UnPaidInvoiceModel {
    public Integer supplierInvoiceID;
    public Boolean allocate;
    public String documentNumber;
    public Date date;
    public Double total;
    public Double amountDue;
    public Double amountPaid;
    public Double discount;
    public Double unAllocate;
    public Boolean paymentStatus;
    public Double adjustAmount;
}
