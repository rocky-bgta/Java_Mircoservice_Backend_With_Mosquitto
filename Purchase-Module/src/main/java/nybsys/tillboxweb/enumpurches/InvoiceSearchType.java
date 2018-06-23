/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 2018-06-05
 * Time: 10:59
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.enumpurches;

public enum InvoiceSearchType {

    AllInvoice(1),
    InvoiceBetween(2),
    SpecificInvoiceNumber(3);

    private int invoiceSearchType;

    InvoiceSearchType(int invoiceSearchType) {
        this.invoiceSearchType = invoiceSearchType;
    }

    public int get() {
        return this.invoiceSearchType;
    }
}
