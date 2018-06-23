/**
 * Created By: Md. Abdul Hannan
 * Created Date: 6/4/2018
 * Time: 11:12 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.models;

import java.util.Date;

public class VMSupplierInvoiceList {
    public Integer supplierInvoiceID;
    public String supplierName;
    public String docNumber;
    public String invoiceNumber;
    public Date date;
    public Date dueDate;
    public Double total;
    public Double amountDue;
    public Boolean printed;
    public String status;
    public Integer parentInvoiceID;
}
