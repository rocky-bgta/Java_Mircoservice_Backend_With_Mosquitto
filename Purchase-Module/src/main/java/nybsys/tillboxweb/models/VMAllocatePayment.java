/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 2018-06-05
 * Time: 10:46
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import java.util.ArrayList;
import java.util.List;

public class VMAllocatePayment {
    public Integer supplierID;
    public List<VMInvoiceAndAdjustment> lstVMInvoiceAndAdjustment = new ArrayList<>();
    public List<SupplierPaymentModel> lstSupplierPaymentModel = new ArrayList<>();
    public List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
    public List<VMDetails> lstRemovedVMDetails = new ArrayList<>();
}
