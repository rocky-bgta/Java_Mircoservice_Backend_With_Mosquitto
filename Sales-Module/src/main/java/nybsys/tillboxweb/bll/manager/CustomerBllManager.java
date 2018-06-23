/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 12/03/2018
 * Time: 4:15
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
import nybsys.tillboxweb.coreModels.DebitCreditBalanceModel;
import nybsys.tillboxweb.coreModels.CustomerCategoryModel;
import nybsys.tillboxweb.entities.Customer;
import nybsys.tillboxweb.models.*;
import nybsys.tillboxweb.sales_enum.CustomerContactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomerBllManager extends BaseBll<Customer> {
    private static final Logger log = LoggerFactory.getLogger(CustomerBllManager.class);

    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Customer.class);
        Core.runTimeModelType.set(CustomerModel.class);
    }

    public CustomerModel saveOrUpdate(CustomerModel customerModelReq) throws Exception {
        CustomerModel customerModel = new CustomerModel();
        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        try {
            customerModel = customerModelReq;
            //save
            if (customerModel.getCustomerID() == null || customerModel.getCustomerID() == 0) {
                customerModel = this.save(customerModel);
                if (customerModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.CUSTOMER_SAVE_FAILED;
                }
            } else { //update

                customerModel = this.update(customerModel);
                if (customerModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.CUSTOMER_UPDATE_FAILED;
                }
            }

        } catch (Exception ex) {
            log.error("CustomerBllManager -> saveOrUpdate got exception :" + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return customerModel;
    }

    public List<CustomerModel> searchCustomer(CustomerModel customerModelReq) throws Exception {
        CustomerModel customerModel = new CustomerModel();
        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        try {
            customerModel = customerModelReq;
            lstCustomerModel = this.getAllByConditions(customerModel);
            if (lstCustomerModel.size() == 0) {
                Core.clientMessage.get().message = MessageConstant.CUSTOMER_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("CustomerBllManager -> searchCustomer got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return lstCustomerModel;
    }

    public CustomerModel searchCustomerByID(int customerID, int businessID) throws Exception {
        CustomerModel customerModel = new CustomerModel();
        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        try {
            customerModel.setBusinessID(businessID);
            customerModel.setCustomerID(customerID);
            customerModel.setStatus(TillBoxAppEnum.Status.Active.get());
            lstCustomerModel = this.getAllByConditions(customerModel);
            if (lstCustomerModel.size() > 0) {
                customerModel = lstCustomerModel.get(0);
            } else {
                Core.clientMessage.get().message = MessageConstant.CUSTOMER_GET_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("CustomerBllManager -> searchCustomerByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return customerModel;
    }

    public CustomerModel deleteCustomerByID(int customerID, int businessID) throws Exception {
        CustomerModel customerModel = new CustomerModel();
        try {
            customerModel.setCustomerID(customerID);
            customerModel.setBusinessID(businessID);
            customerModel = this.softDelete(customerModel);

            if (customerModel == null) {
                Core.clientMessage.get().message = MessageConstant.CUSTOMER_DELETE_FAILED;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            }
        } catch (Exception ex) {
            log.error("CustomerBllManager -> deleteCustomerByID got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return customerModel;
    }

    public List<VMCustomerList> getCustomerList(CustomerModel CustomerModelReq) throws Exception {

        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        List<VMCustomerList> lstVMCustomerList = new ArrayList<>();
        CustomerCategoryBllManager CustomerCategoryBllManager = new CustomerCategoryBllManager();
        CustomerContactBllManager CustomerContactBllManager = new CustomerContactBllManager();

        try {

            lstCustomerModel = this.getAllByConditions(CustomerModelReq);
            for (CustomerModel sModel : lstCustomerModel) {
                VMCustomerList vmCustomerList = new VMCustomerList();
                vmCustomerList.customerID = sModel.getCustomerID();
                vmCustomerList.customerName = sModel.getCustomerName();
                vmCustomerList.balance = getCustomerBalance(sModel);

                CustomerCategoryModel searchCustomerCategoryModel = new CustomerCategoryModel();
                searchCustomerCategoryModel.setBusinessID(CustomerModelReq.getBusinessID());
                searchCustomerCategoryModel.setCustomerCategoryID(sModel.getCustomerCategoryID());
                List<CustomerCategoryModel> CustomerCategoryModels = new ArrayList<>();
                CustomerCategoryModels = CustomerCategoryBllManager.getAllByConditionWithActive(searchCustomerCategoryModel);
                if (CustomerCategoryModels.size() > 0) {
                    vmCustomerList.category = CustomerCategoryModels.get(0).getCategoryName();
                }

                CustomerContactModel searchCustomerContactModel = new CustomerContactModel();
                searchCustomerContactModel.setCustomerID(sModel.getCustomerID());
                searchCustomerContactModel.setContactTypeID(CustomerContactType.Default.get());

                List<CustomerContactModel> CustomerContactModels = new ArrayList<>();
                CustomerContactModels = CustomerContactBllManager.getAllByConditions(searchCustomerContactModel);
                if (CustomerContactModels.size() > 0) {
                    vmCustomerList.contactName = CustomerContactModels.get(0).getName();
                    vmCustomerList.telephone = (CustomerContactModels.get(0).getPhone() != null) ? CustomerContactModels.get(0).getPhone() : "";
                    vmCustomerList.mobile = (CustomerContactModels.get(0).getPhone() != null) ? CustomerContactModels.get(0).getPhone() : "";
                }
                vmCustomerList.status = sModel.getStatus();
                vmCustomerList.balance = getCustomerBalance(sModel);
                lstVMCustomerList.add(vmCustomerList);
            }

        } catch (Exception ex) {
            log.error("CustomerBllManager -> searchCustomer got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return lstVMCustomerList;
    }  
    
       
    
    public Double getCustomerBalance(CustomerModel customerModel) throws Exception {
        List<DebitCreditBalanceModel> lstDebitCreditBalanceModel = new ArrayList<>();

        Double debitAmountSum = 0.0, creditAmountSum = 0.0, balance = 0.0;
        try {

            String hql = "SELECT sum(J.amount) as  amountSum, J.drCrIndicator as drCrIndicator FROM Journal J WHERE J.status = " + TillBoxAppEnum.Status.Active.get() + " AND J.businessID = " + customerModel.getBusinessID() + " AND J.partyID = " + customerModel.getCustomerID() + " AND J.partyType = " + PartyType.Customer.get() + " GROUP BY J.drCrIndicator";
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
            log.error("CustomerInvoiceBllManager -> get party balance got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return balance;
    }



    public VMCustomerModel getFilteredVMCustomer(CustomerModel CustomerModelReq) throws Exception {

        List<CustomerModel> lstCustomerModel = new ArrayList<>();
        VMCustomerModel vmCustomerModel = new VMCustomerModel();

        CustomerCategoryBllManager CustomerCategoryBllManager = new CustomerCategoryBllManager();
        CustomerContactBllManager CustomerContactBllManager = new CustomerContactBllManager();
        CustomerAddressBllManger CustomerAddressBllManger = new CustomerAddressBllManger();

        try {

            lstCustomerModel = this.getAllByConditions(CustomerModelReq);
            if (lstCustomerModel.size() > 0) {
                vmCustomerModel.customerModel = lstCustomerModel.get(0);

                int CustomerID = lstCustomerModel.get(0).getCustomerID();
                CustomerAddressModel searchCustomerAddress = new CustomerAddressModel();
                searchCustomerAddress.setCustomerID(CustomerID);
                vmCustomerModel.lstCustomerAddressModel = CustomerAddressBllManger.getAllByConditions(searchCustomerAddress);

                CustomerContactModel CustomerContactModel = new CustomerContactModel();
                CustomerContactModel.setCustomerID(CustomerID);
                vmCustomerModel.lstCustomerContactModel = CustomerContactBllManager.getAllByConditions(CustomerContactModel);
            }


        } catch (Exception ex) {
            log.error("CustomerBllManager -> searchCustomer got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return vmCustomerModel;
    }


}
