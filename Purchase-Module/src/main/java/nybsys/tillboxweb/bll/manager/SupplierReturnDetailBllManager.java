/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/23/2018
 * Time: 4:38 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.entities.SupplierReturnDetail;
import nybsys.tillboxweb.models.SupplierReturnDetailModel;
import nybsys.tillboxweb.models.VMSupplierReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupplierReturnDetailBllManager extends BaseBll<SupplierReturnDetail> {

    private static final Logger log = LoggerFactory.getLogger(SupplierReturnDetailBllManager.class);

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(SupplierReturnDetail.class);
        Core.runTimeModelType.set(SupplierReturnDetailModel.class);
    }


    public VMSupplierReturn saveSupplierReturnDetail(VMSupplierReturn vmSupplierReturn) throws Exception {

        try {
            for (SupplierReturnDetailModel supplierReturnDetailModel : vmSupplierReturn.lstSupplierReturnDetailModel) {

                supplierReturnDetailModel.setSupplierReturnID(vmSupplierReturn.supplierReturnModel.getSupplierReturnID());
                supplierReturnDetailModel.setStatus(TillBoxAppEnum.Status.Active.get());
                supplierReturnDetailModel.setCreatedBy("");
                supplierReturnDetailModel.setCreatedDate(new Date());
                supplierReturnDetailModel = this.save(supplierReturnDetailModel);
            }


        } catch (Exception ex) {
            log.error("PurchaseOrderDetailBllManager -> save Purchase OrderDetail manager got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return vmSupplierReturn;
    }

    public Double getPriceSumByInvoiceID(Integer invoiceID) throws Exception {
        List<SupplierReturnDetailModel> lstSupplierReturnDetailModel;
        SupplierReturnDetailModel whereCondition = new SupplierReturnDetailModel();
        Double priceSum = 0.0;
        Double tempRowSum = 0.0;
        try {
            whereCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            whereCondition.setStatus(invoiceID);
            lstSupplierReturnDetailModel = this.getAllByConditions(whereCondition);
            for (SupplierReturnDetailModel supplierReturnDetailModel : lstSupplierReturnDetailModel) {
                if (supplierReturnDetailModel.getDiscount() == null) {
                    supplierReturnDetailModel.setDiscount(0.0);
                }
                if (supplierReturnDetailModel.getVat() == null) {
                    supplierReturnDetailModel.setVat(0.0);
                }
                tempRowSum = (supplierReturnDetailModel.getQuantity() * supplierReturnDetailModel.getUnitPrice()) - supplierReturnDetailModel.getDiscount() + supplierReturnDetailModel.getVat();
                priceSum += tempRowSum;
            }
        } catch (Exception ex) {
            log.error("PurchaseOrderDetailBllManager -> getPriceSumByInvoiceID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return priceSum;
    }


}
