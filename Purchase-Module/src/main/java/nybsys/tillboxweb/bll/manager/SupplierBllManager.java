/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 23/02/2018
 * Time: 11:15
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreEnum.BankReferenceType;
import nybsys.tillboxweb.coreEnum.DebitCreditIndicator;
import nybsys.tillboxweb.coreEnum.PartyType;
import nybsys.tillboxweb.coreEnum.ReferenceType;
import nybsys.tillboxweb.coreModels.DebitCreditBalanceModel;
import nybsys.tillboxweb.coreModels.SupplierCategoryModel;
import nybsys.tillboxweb.entities.Supplier;
import nybsys.tillboxweb.entities.SupplierContact;
import nybsys.tillboxweb.enumpurches.SupplierContactType;
import nybsys.tillboxweb.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupplierBllManager extends BaseBll<Supplier> {
    private static final Logger log = LoggerFactory.getLogger(SupplierBllManager.class);

    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Supplier.class);
        Core.runTimeModelType.set(SupplierModel.class);
    }

    public SupplierModel saveOrUpdate(SupplierModel supplierModelReq) throws Exception {
        SupplierModel supplierModel = new SupplierModel();
        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        try {
            supplierModel = supplierModelReq;
            //save
            if (supplierModel.getSupplierID() == null || supplierModel.getSupplierID() == 0) {
                supplierModel = this.save(supplierModel);
                if (supplierModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.SUPPLIER_SAVE_FAILED;
                }
            } else { //update

                supplierModel = this.update(supplierModel);
                if (supplierModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.SUPPLIER_UPDATE_FAILED;
                }
            }

        } catch (Exception ex) {
            log.error("SupplierBllManager -> saveOrUpdate got exception :" + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return supplierModel;
    }

    public List<SupplierModel> searchSupplier(SupplierModel supplierModelReq) throws Exception {
        SupplierModel supplierModel = new SupplierModel();
        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        try {
            supplierModel = supplierModelReq;
            lstSupplierModel = this.getAllByConditions(supplierModel);
            if (lstSupplierModel.size() == 0) {
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierBllManager -> searchSupplier got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return lstSupplierModel;
    }

    public SupplierModel searchSupplierByID(int supplierID, int businessID) throws Exception {
        SupplierModel supplierModel = new SupplierModel();
        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        try {
            supplierModel.setBusinessID(businessID);
            supplierModel.setSupplierID(supplierID);
            supplierModel.setStatus(TillBoxAppEnum.Status.Active.get());
            lstSupplierModel = this.getAllByConditions(supplierModel);
            if (lstSupplierModel.size() > 0) {
                supplierModel = lstSupplierModel.get(0);
            } else {
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierBllManager -> searchSupplierByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return supplierModel;
    }

    public SupplierModel deleteSupplierByID(int supplierID, int businessID) throws Exception {
        SupplierModel supplierModel = new SupplierModel();

        try {
            supplierModel.setSupplierID(supplierID);
            supplierModel.setBusinessID(businessID);
            supplierModel = this.softDelete(supplierModel);
            if (supplierModel == null) {
                Core.clientMessage.get().message = MessageConstant.SUPPLIER_DELETE_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("SupplierBllManager -> deleteSupplierByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return supplierModel;
    }


    public List<VMSupplierList> getSupplierList(SupplierModel supplierModelReq) throws Exception {

        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        List<VMSupplierList> lstVMSupplierList = new ArrayList<>();
        SupplierCategoryBllManager supplierCategoryBllManager = new SupplierCategoryBllManager();
        SupplierContactBllManager supplierContactBllManager = new SupplierContactBllManager();

        try {

            lstSupplierModel = this.getAllByConditions(supplierModelReq);
            for (SupplierModel sModel : lstSupplierModel) {
                VMSupplierList vmSupplierList = new VMSupplierList();
                vmSupplierList.supplierID = sModel.getSupplierID();
                vmSupplierList.supplierName = sModel.getSupplierName();
                vmSupplierList.balance = getSupplierBalance(sModel);

                SupplierCategoryModel searchSupplierCategoryModel = new SupplierCategoryModel();
                searchSupplierCategoryModel.setBusinessID(supplierModelReq.getBusinessID());
                searchSupplierCategoryModel.setSupplierCategoryID(sModel.getSupplierCategoryID());
                List<SupplierCategoryModel> supplierCategoryModels = new ArrayList<>();
                supplierCategoryModels = supplierCategoryBllManager.getAllByConditionWithActive(searchSupplierCategoryModel);
                if (supplierCategoryModels.size() > 0) {
                    vmSupplierList.category = supplierCategoryModels.get(0).getCategoryName();
                }

                SupplierContactModel searchSupplierContactModel = new SupplierContactModel();
                searchSupplierContactModel.setSupplierID(sModel.getSupplierID());
                searchSupplierContactModel.setContactTypeID(SupplierContactType.Default.get());

                List<SupplierContactModel> supplierContactModels = new ArrayList<>();
                supplierContactModels = supplierContactBllManager.getAllByConditions(searchSupplierContactModel);
                if (supplierContactModels.size() > 0) {
                    vmSupplierList.contactName = supplierContactModels.get(0).getName();
                    vmSupplierList.telephone = (supplierContactModels.get(0).getPhone() != null) ? supplierContactModels.get(0).getPhone() : "";
                    vmSupplierList.mobile = (supplierContactModels.get(0).getMobile() != null) ? supplierContactModels.get(0).getMobile() : "";
                }
                vmSupplierList.status = sModel.getStatus();
                vmSupplierList.balance = getSupplierBalance(sModel);
                lstVMSupplierList.add(vmSupplierList);
            }

        } catch (Exception ex) {
            log.error("SupplierBllManager -> searchSupplier got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return lstVMSupplierList;
    }

    public VMSupplierModel getFilteredVMSupplier(SupplierModel supplierModelReq) throws Exception {

        List<SupplierModel> lstSupplierModel = new ArrayList<>();
        VMSupplierModel vmSupplierModel = new VMSupplierModel();

        SupplierCategoryBllManager supplierCategoryBllManager = new SupplierCategoryBllManager();
        SupplierContactBllManager supplierContactBllManager = new SupplierContactBllManager();
        SupplierAddressBllManger supplierAddressBllManger = new SupplierAddressBllManger();
        BankingDetailsBllManager bankingDetailsBllManager = new BankingDetailsBllManager();

        try {

            lstSupplierModel = this.getAllByConditions(supplierModelReq);
            if (lstSupplierModel.size() > 0) {
                vmSupplierModel.supplierModel = lstSupplierModel.get(0);

                int supplierID = lstSupplierModel.get(0).getSupplierID();
                SupplierAddressModel searchSupplierAddress = new SupplierAddressModel();
                searchSupplierAddress.setSupplierID(supplierID);
                vmSupplierModel.lstSupplierAddressModel = supplierAddressBllManger.getAllByConditions(searchSupplierAddress);

                SupplierContactModel supplierContactModel = new SupplierContactModel();
                supplierContactModel.setSupplierID(supplierID);
                vmSupplierModel.lstSupplierContactModel = supplierContactBllManager.getAllByConditions(supplierContactModel);

                BankingDetailsModel bankingDetailsModel = new BankingDetailsModel();
                bankingDetailsModel.setReferenceID(supplierID);
                bankingDetailsModel.setReferenceType(BankReferenceType.Supplier.get());
                List<BankingDetailsModel> bankingDetailsModels = new ArrayList<>();
                bankingDetailsModels = bankingDetailsBllManager.getAllByConditions(bankingDetailsModel);
                if (bankingDetailsModels.size() > 0) {
                    vmSupplierModel.bankingDetailsModel = bankingDetailsModels.get(0);
                }
            }


        } catch (Exception ex) {
            log.error("SupplierBllManager -> searchSupplier got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return vmSupplierModel;
    }


    public Double getSupplierBalance(SupplierModel supplierModel) throws Exception {
        List<DebitCreditBalanceModel> lstDebitCreditBalanceModel = new ArrayList<>();

        Double debitAmountSum = 0.0, creditAmountSum = 0.0, balance = 0.0;
        try {

            String hql = "SELECT sum(J.amount) as  amountSum, J.drCrIndicator as drCrIndicator FROM Journal J WHERE J.status = " + TillBoxAppEnum.Status.Active.get() + " AND J.businessID = " + supplierModel.getBusinessID() + " AND J.partyID = " + supplierModel.getSupplierID() + " AND J.partyType = " + PartyType.Supplier.get() + " GROUP BY J.drCrIndicator";
            lstDebitCreditBalanceModel = this.executeHqlQuery(hql, DebitCreditBalanceModel.class, TillBoxAppEnum.QueryType.Join.get());
            if (lstDebitCreditBalanceModel.size() > 0) {
                for (DebitCreditBalanceModel journalBalanceResult : lstDebitCreditBalanceModel) {
                    if (journalBalanceResult.drCrIndicator == DebitCreditIndicator.Debit.get()) {
                        debitAmountSum = journalBalanceResult.amountSum;
                    } else if (journalBalanceResult.drCrIndicator == DebitCreditIndicator.Credit.get()) {
                        creditAmountSum = journalBalanceResult.amountSum;
                    }
                }
            }
            balance = creditAmountSum - debitAmountSum;

        } catch (Exception ex) {
            log.error("SupplierInvoiceBllManager -> get party balance got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return balance;
    }
}
