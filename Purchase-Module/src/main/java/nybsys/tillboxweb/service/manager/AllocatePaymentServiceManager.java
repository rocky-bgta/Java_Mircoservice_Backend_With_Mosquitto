/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 05-Mar-18
 * Time: 4:16 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.bll.manager.*;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreEnum.EffectType;
import nybsys.tillboxweb.coreEnum.ReferenceType;
import nybsys.tillboxweb.enumpurches.PaymentAdjustmentReferenceType;
import nybsys.tillboxweb.enumpurches.PaymentStatus;
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
public class AllocatePaymentServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(AllocatePaymentServiceManager.class);

    private SupplierPaymentBllManager supplierPaymentBllManager;

    private SupplierPaymentDetailBllManager supplierPaymentDetailBllManager;

    private SupplierAdjustmentBllManager supplierAdjustmentBllManager;

    private SupplierAdjustmentDetailBllManager supplierAdjustmentDetailBllManager;

    private SupplierInvoiceBllManager supplierInvoiceBllManager;

    public ResponseMessage saveAllocatePayment(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.supplierPaymentBllManager = new SupplierPaymentBllManager();
        this.supplierPaymentDetailBllManager = new SupplierPaymentDetailBllManager();
        this.supplierInvoiceBllManager = new SupplierInvoiceBllManager();
        this.supplierAdjustmentBllManager = new SupplierAdjustmentBllManager();
        this.supplierAdjustmentDetailBllManager = new SupplierAdjustmentDetailBllManager();
        VMAllocatePayment vmAllocatePayment;
        try {
            vmAllocatePayment = Core.getRequestObject(requestMessage, VMAllocatePayment.class);

            //(1)removed deleted details from db
            for (VMDetails vmDetails : vmAllocatePayment.lstRemovedVMDetails) {
                if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get()) {
                    SupplierPaymentDetailModel supplierPaymentDetailModel;
                    supplierPaymentDetailModel = this.supplierPaymentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                    if (supplierPaymentDetailModel != null) {
                        this.supplierPaymentDetailBllManager.softDelete(supplierPaymentDetailModel);
                    }
                } else {
                    SupplierAdjustmentDetailModel supplierAdjustmentDetailModel;
                    supplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                    if (supplierAdjustmentDetailModel != null) {
                        this.supplierAdjustmentDetailBllManager.softDelete(supplierAdjustmentDetailModel);
                    }
                }
            }
            //(2a)get all payment of this supplier
            List<SupplierPaymentModel> lstSupplierPaymentModel = new ArrayList<>();
            SupplierPaymentModel whereConditionAllPayment = new SupplierPaymentModel();
            whereConditionAllPayment.setSupplierID(vmAllocatePayment.supplierID);
            whereConditionAllPayment.setBusinessID(requestMessage.businessID);
            lstSupplierPaymentModel = this.supplierPaymentBllManager.getAllByConditionWithActive(whereConditionAllPayment);

            //(2b)get all adjustment of this supplier
            List<SupplierAdjustmentModel> lstSupplierAdjustmentModel = new ArrayList<>();
            SupplierAdjustmentModel whereConditionAllAdjustment = new SupplierAdjustmentModel();
            whereConditionAllAdjustment.setSupplierID(vmAllocatePayment.supplierID);
            whereConditionAllAdjustment.setBusinessID(requestMessage.businessID);
            lstSupplierAdjustmentModel = this.supplierAdjustmentBllManager.getAllByConditionWithActive(whereConditionAllAdjustment);

            //(3)check save condition
            //(iii.a)check payment total amount is greater then or equal detail amount
            for (SupplierPaymentModel supplierPaymentModel : lstSupplierPaymentModel) {
                //picked saved detail amount and marge with new detail amount
                SupplierPaymentDetailModel whereCondition = new SupplierPaymentDetailModel();
                List<SupplierPaymentDetailModel> savedSupplierPaymentDetailModel = new ArrayList<>();
                whereCondition.setSupplierPaymentID(supplierPaymentModel.getSupplierPaymentID());
                savedSupplierPaymentDetailModel = this.supplierPaymentDetailBllManager.getAllByConditionWithActive(whereCondition);
                Double amountSum = 0.0;
                //saved
                for (SupplierPaymentDetailModel supplierPaymentDetailModel : savedSupplierPaymentDetailModel) {
                    //check is in update
                    boolean foundFlag = false;
                    for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                        for (VMDetails vmDetails : vmInvoiceAndAdjustment.lstVMDetails) {
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get() && (vmDetails.getDetailID() != null && vmDetails.getDetailID().intValue() == supplierPaymentDetailModel.getSupplierPaymentID().intValue())) {
                                amountSum += vmDetails.getAmount();
                                foundFlag = true;
                            }
                        }
                    }
                    if (!foundFlag) { // saved from other page
                        amountSum += supplierPaymentDetailModel.getPaidAmount();
                    }
                }
                //new
                for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                    for (VMDetails vmDetails : vmInvoiceAndAdjustment.lstVMDetails) {
                        if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get() && (vmDetails.getDetailID() == null || vmDetails.getDetailID() == 0)) {
                            amountSum += vmDetails.getAmount();
                        }
                    }
                }
                //violate condition
                if (supplierPaymentModel.getPaidAmount().doubleValue() < amountSum.doubleValue()) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = MessageConstant.ALLOCATE_PAYMENT_AMOUNT_IS_GREATER_THAN_SUPPLIER_PAYMENT_AMOUNT;
                    this.rollBack();
                    return responseMessage;

                } else { //*****update master un allocate amount
                    supplierPaymentModel.setUnAllocatedAmount(supplierPaymentModel.getPaidAmount() - amountSum);
                    this.supplierPaymentBllManager.update(supplierPaymentModel);
                }
            }

            //(iii.b)check adjustment total amount is greater then or equal detail amount
            for (SupplierAdjustmentModel supplierAdjustmentModel : lstSupplierAdjustmentModel) {
                //picked saved detail amount and marge with new detail amount
                SupplierAdjustmentDetailModel whereCondition = new SupplierAdjustmentDetailModel();
                List<SupplierAdjustmentDetailModel> savedSupplierAdjustmentDetailModel = new ArrayList<>();
                whereCondition.setSupplierAdjustmentID(supplierAdjustmentModel.getSupplierAdjustmentID());
                savedSupplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.getAllByConditionWithActive(whereCondition);
                Double amountSum = 0.0;
                //saved
                for (SupplierAdjustmentDetailModel supplierAdjustmentDetailModel : savedSupplierAdjustmentDetailModel) {
                    //check is in update
                    boolean foundFlag = false;
                    for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                        for (VMDetails vmDetails : vmInvoiceAndAdjustment.lstVMDetails) {
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierAdjustment.get() && (vmDetails.getDetailID() != null && vmDetails.getDetailID().intValue() == supplierAdjustmentDetailModel.getSupplierAdjustmentDetailID().intValue())) {
                                amountSum += vmDetails.getAmount();
                                foundFlag = true;
                            }
                        }
                    }
                    if (!foundFlag) { // saved from other page
                        amountSum += supplierAdjustmentDetailModel.getAdjustAmount();
                    }
                }
                //new
                for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                    for (VMDetails vmDetails : vmInvoiceAndAdjustment.lstVMDetails) {
                        if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierAdjustment.get() && (vmDetails.getDetailID() == null || vmDetails.getDetailID() == 0)) {
                            amountSum += vmDetails.getAmount();
                        }
                    }
                }
                //violate condition
                if (supplierAdjustmentModel.getExclusive().doubleValue() < amountSum.doubleValue()) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = MessageConstant.ALLOCATE_PAYMENT_AMOUNT_IS_GREATER_THAN_SUPPLIER_ADJUSTMENT_AMOUNT;
                    this.rollBack();
                    return responseMessage;

                } else { //*****update master un allocate amount
                    supplierAdjustmentModel.setUnAllocatedAmount(supplierAdjustmentModel.getExclusive() - amountSum);
                    this.supplierAdjustmentBllManager.update(supplierAdjustmentModel);
                }
            }

            //(4) save or update details
            for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                for (VMDetails vmDetails : vmInvoiceAndAdjustment.lstVMDetails) {
                    //save
                    if (vmDetails.getDetailID() == null || vmDetails.getDetailID() == 0) {
                        //invoice
                        if (vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getReferenceType().intValue() == PaymentAdjustmentReferenceType.Invoice.get()) {

                            //payment
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get()) {
                                SupplierPaymentDetailModel supplierPaymentDetailModel = new SupplierPaymentDetailModel();
                                supplierPaymentDetailModel.setSupplierPaymentID(vmDetails.getMasterID());
                                supplierPaymentDetailModel.setReferenceID(vmDetails.getReferenceID());
                                supplierPaymentDetailModel.setReferenceType(PaymentAdjustmentReferenceType.Invoice.get());
                                supplierPaymentDetailModel.setPaidAmount(vmDetails.getAmount());
                                this.supplierPaymentDetailBllManager.save(supplierPaymentDetailModel);

                            } else {//adjustment
                                SupplierAdjustmentDetailModel supplierAdjustmentDetailModel = new SupplierAdjustmentDetailModel();
                                supplierAdjustmentDetailModel.setSupplierAdjustmentID(vmDetails.getMasterID());
                                supplierAdjustmentDetailModel.setReferenceID(vmDetails.getReferenceID());
                                supplierAdjustmentDetailModel.setReferenceType(PaymentAdjustmentReferenceType.Invoice.get());
                                supplierAdjustmentDetailModel.setAdjustAmount(vmDetails.getAmount());
                                this.supplierAdjustmentDetailBllManager.save(supplierAdjustmentDetailModel);
                            }
                        } else {//adjustment increase

                            //payment
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get()) {
                                SupplierPaymentDetailModel supplierPaymentDetailModel = new SupplierPaymentDetailModel();
                                supplierPaymentDetailModel.setSupplierPaymentID(vmDetails.getMasterID());
                                supplierPaymentDetailModel.setReferenceID(vmDetails.getReferenceID());
                                supplierPaymentDetailModel.setReferenceType(PaymentAdjustmentReferenceType.AdjustmentIncrease.get());
                                supplierPaymentDetailModel.setPaidAmount(vmDetails.getAmount());
                                this.supplierPaymentDetailBllManager.save(supplierPaymentDetailModel);

                            } else {//adjustment
                                SupplierAdjustmentDetailModel supplierAdjustmentDetailModel = new SupplierAdjustmentDetailModel();
                                supplierAdjustmentDetailModel.setSupplierAdjustmentID(vmDetails.getMasterID());
                                supplierAdjustmentDetailModel.setReferenceID(vmDetails.getReferenceID());
                                supplierAdjustmentDetailModel.setReferenceType(PaymentAdjustmentReferenceType.AdjustmentIncrease.get());
                                supplierAdjustmentDetailModel.setAdjustAmount(vmDetails.getAmount());
                                this.supplierAdjustmentDetailBllManager.save(supplierAdjustmentDetailModel);
                            }
                        }
                    } else {//update

                        //invoice
                        if (vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getReferenceType().intValue() == PaymentAdjustmentReferenceType.Invoice.get()) {

                            //payment
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get()) {
                                SupplierPaymentDetailModel supplierPaymentDetailModel;
                                supplierPaymentDetailModel = this.supplierPaymentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                                if (supplierPaymentDetailModel.getPaidAmount().doubleValue() != vmDetails.getAmount().doubleValue()) {
                                    supplierPaymentDetailModel.setPaidAmount(vmDetails.getAmount());
                                    this.supplierPaymentDetailBllManager.update(supplierPaymentDetailModel);
                                }

                            } else {//adjustment
                                SupplierAdjustmentDetailModel supplierAdjustmentDetailModel;
                                supplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                                if (supplierAdjustmentDetailModel.getAdjustAmount().doubleValue() != vmDetails.getAmount().doubleValue()) {
                                    supplierAdjustmentDetailModel.setAdjustAmount(vmDetails.getAmount());
                                    this.supplierAdjustmentDetailBllManager.update(supplierAdjustmentDetailModel);
                                }
                            }
                        } else {//adjustment increase

                            //payment
                            if (vmDetails.getDetailType().intValue() == ReferenceType.SupplierPayment.get()) {
                                SupplierPaymentDetailModel supplierPaymentDetailModel;
                                supplierPaymentDetailModel = this.supplierPaymentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                                if (supplierPaymentDetailModel.getPaidAmount().doubleValue() != vmDetails.getAmount().doubleValue()) {
                                    supplierPaymentDetailModel.setPaidAmount(vmDetails.getAmount());
                                    this.supplierPaymentDetailBllManager.update(supplierPaymentDetailModel);
                                }

                            } else {//adjustment
                                SupplierAdjustmentDetailModel supplierAdjustmentDetailModel;
                                supplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.getByIdActiveStatus(vmDetails.getDetailID());
                                if (supplierAdjustmentDetailModel.getAdjustAmount().doubleValue() != vmDetails.getAmount().doubleValue()) {
                                    supplierAdjustmentDetailModel.setAdjustAmount(vmDetails.getAmount());
                                    this.supplierAdjustmentDetailBllManager.update(supplierAdjustmentDetailModel);
                                }
                            }
                        }
                    }
                }
            }

            //(5) update invoice or adjustment increase status
            for (VMInvoiceAndAdjustment vmInvoiceAndAdjustment : vmAllocatePayment.lstVMInvoiceAndAdjustment) {
                //(a)update invoice status
                if (vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getReferenceType().intValue() == PaymentAdjustmentReferenceType.Invoice.get()) {
                    Double dueAmount = this.supplierInvoiceBllManager.getDueAmount(vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getMasterID());
                    SupplierInvoiceModel supplierInvoiceModel = this.supplierInvoiceBllManager.getByIdActiveStatus(vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getMasterID());
                    if (supplierInvoiceModel != null) {
                        Double zero = 0.0;
                        if (supplierInvoiceModel.getTotalAmount().doubleValue() == dueAmount.doubleValue()) {
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Unpaid.get());
                        } else if (dueAmount.doubleValue() == zero.doubleValue()) {
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.Paid.get());
                        } else {
                            supplierInvoiceModel.setPaymentStatus(PaymentStatus.PartiallyPaid.get());
                        }
                        this.supplierInvoiceBllManager.update(supplierInvoiceModel);
                    }
                } else {//(b)update adjustment increase status
                    Double dueAmount = this.supplierInvoiceBllManager.getDueAmountOfAdjustmentIncrease(vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getMasterID());
                    SupplierAdjustmentModel supplierAdjustmentModel = this.supplierAdjustmentBllManager.getByIdActiveStatus(vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster.getMasterID());
                    if (supplierAdjustmentModel != null) {
                        Double zero = 0.0;
                        if (supplierAdjustmentModel.getExclusive().doubleValue() == dueAmount.doubleValue()) {
                            supplierAdjustmentModel.setAdjustmentStatus(PaymentStatus.Unpaid.get());
                        } else if (dueAmount.doubleValue() == zero.doubleValue()) {
                            supplierAdjustmentModel.setAdjustmentStatus(PaymentStatus.Paid.get());
                        } else {
                            supplierAdjustmentModel.setAdjustmentStatus(PaymentStatus.PartiallyPaid.get());
                        }
                        this.supplierAdjustmentBllManager.update(supplierAdjustmentModel);
                    }
                }
            }

            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.message = MessageConstant.ALLOCATE_PAYMENT_SAVED;
            this.commit();

        } catch (Exception ex) {
            log.error("AllocatePaymentServiceManager -> saveAllocatePayment got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {

        ResponseMessage responseMessage = new ResponseMessage();
        this.supplierPaymentBllManager = new SupplierPaymentBllManager();
        this.supplierPaymentDetailBllManager = new SupplierPaymentDetailBllManager();
        this.supplierInvoiceBllManager = new SupplierInvoiceBllManager();
        this.supplierAdjustmentBllManager = new SupplierAdjustmentBllManager();
        this.supplierAdjustmentDetailBllManager = new SupplierAdjustmentDetailBllManager();

        List<SupplierInvoiceModel> lstSupplierInvoiceModel = new ArrayList<>();
        List<SupplierPaymentDetailModel> lstSupplierPaymentDetailModel = new ArrayList<>();
        List<SupplierAdjustmentDetailModel> lstDecreasedSupplierAdjustmentDetailModel = new ArrayList<>();
        List<SupplierAdjustmentModel> lstIncreasedSupplierAdjustmentModel = new ArrayList<>();
        VMAllocatePaymentReq vmAllocatePaymentReq;
        VMAllocatePayment vmAllocatePayment = new VMAllocatePayment();
        try {
            vmAllocatePaymentReq = Core.getRequestObject(requestMessage, VMAllocatePaymentReq.class);

            //(1) get all filtered invoice
            lstSupplierInvoiceModel = this.supplierInvoiceBllManager.filterForAllocatePayment(vmAllocatePaymentReq, requestMessage.businessID);

            //(2)get all filtered supplierAdjustment(increase)
            lstIncreasedSupplierAdjustmentModel = this.supplierAdjustmentBllManager.filterForAllocatePayment(vmAllocatePaymentReq, requestMessage.businessID, EffectType.Increase.get());

            //(3)get supplier payments
            vmAllocatePayment.lstSupplierPaymentModel = this.supplierPaymentBllManager.getUnAllocatedSupplierPayments(vmAllocatePaymentReq.getSelectedSupplierID(), requestMessage.businessID);

            //initially set paid amount as unAllocate amount than detail sum will be minus
            for (int index = 0; index < vmAllocatePayment.lstSupplierPaymentModel.size(); index++) {
                Double paidAmount = vmAllocatePayment.lstSupplierPaymentModel.get(index).getPaidAmount();
                vmAllocatePayment.lstSupplierPaymentModel.get(index).setUnAllocatedAmount(paidAmount);
            }
            //(4)get supplier adjustments (decrease)
            vmAllocatePayment.lstSupplierAdjustmentModel = this.supplierAdjustmentBllManager.getUnAllocatedSupplierAdjustments(EffectType.Decrease.get(), vmAllocatePaymentReq.getSelectedSupplierID(), requestMessage.businessID);

            //initially set exclusive amount as unAllocate amount than detail sum will be minus
            for (int index = 0; index < vmAllocatePayment.lstSupplierAdjustmentModel.size(); index++) {
                Double exclusiveAmount = vmAllocatePayment.lstSupplierAdjustmentModel.get(index).getExclusive();
                vmAllocatePayment.lstSupplierAdjustmentModel.get(index).setUnAllocatedAmount(exclusiveAmount);
            }

            //(5a)map invoice with detail and make VM

            for (SupplierInvoiceModel supplierInvoiceModel : lstSupplierInvoiceModel) {

                VMInvoiceAndAdjustment vmInvoiceAndAdjustment = new VMInvoiceAndAdjustment();
                //(i)master (invoice)
                VMInvoiceAndAdjustmentMaster vmInvoiceAndAdjustmentMaster = new VMInvoiceAndAdjustmentMaster();
                vmInvoiceAndAdjustmentMaster.setMasterID(supplierInvoiceModel.getSupplierInvoiceID());
                vmInvoiceAndAdjustmentMaster.setDate(supplierInvoiceModel.getDate());
                vmInvoiceAndAdjustmentMaster.setDocNumber(supplierInvoiceModel.getDocNumber());
                vmInvoiceAndAdjustmentMaster.setReference("");
                vmInvoiceAndAdjustmentMaster.setTotalAmount(supplierInvoiceModel.getTotalAmount());
                vmInvoiceAndAdjustmentMaster.setDueAmount(this.supplierInvoiceBllManager.getDueAmount(supplierInvoiceModel.getSupplierInvoiceID()));
                vmInvoiceAndAdjustmentMaster.setReferenceType(PaymentAdjustmentReferenceType.Invoice.get());

                vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster = vmInvoiceAndAdjustmentMaster;

                //(ii)get supplier payments detail
                lstSupplierPaymentDetailModel = this.supplierPaymentDetailBllManager.search(supplierInvoiceModel.getSupplierInvoiceID(), PaymentAdjustmentReferenceType.Invoice.get());

                for (SupplierPaymentDetailModel supplierPaymentDetailModel : lstSupplierPaymentDetailModel) {
                    VMDetails vmDetails = new VMDetails();
                    vmDetails.setDetailID(supplierPaymentDetailModel.getSupplierPaymentDetailID());
                    vmDetails.setAmount(supplierPaymentDetailModel.getPaidAmount());
                    vmDetails.setMasterID(supplierPaymentDetailModel.getSupplierPaymentID());
                    vmDetails.setReferenceID(supplierPaymentDetailModel.getReferenceID());
                    vmDetails.setDetailType(ReferenceType.SupplierPayment.get());

                    SupplierPaymentModel supplierPaymentModel = this.supplierPaymentBllManager.getByIdActiveStatus(supplierPaymentDetailModel.getSupplierPaymentID());
                    vmDetails.setDocNumber(supplierPaymentModel.getDocNumber());

                    int index = 0;
                    for (SupplierPaymentModel supplierPaymentModelWhichHaveUnAllocatedAmount : vmAllocatePayment.lstSupplierPaymentModel) {
                        if (supplierPaymentDetailModel.getSupplierPaymentID().intValue() == supplierPaymentModelWhichHaveUnAllocatedAmount.getSupplierPaymentID().intValue()) {
                            //calculate un allocate amount
                            Double tempUnAllocatedAmount = vmAllocatePayment.lstSupplierPaymentModel.get(index).getUnAllocatedAmount();
                            vmAllocatePayment.lstSupplierPaymentModel.get(index).setUnAllocatedAmount(tempUnAllocatedAmount - supplierPaymentDetailModel.getPaidAmount());
                        }
                        index++;
                    }
                    vmInvoiceAndAdjustment.lstVMDetails.add(vmDetails);
                }

                //(iii)get supplier Adjustment detail
                lstDecreasedSupplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.searchSupplierAdjustmentDetail(supplierInvoiceModel.getSupplierInvoiceID(), PaymentAdjustmentReferenceType.Invoice.get());

                for (SupplierAdjustmentDetailModel supplierAdjustmentDetailModel : lstDecreasedSupplierAdjustmentDetailModel) {
                    VMDetails vmDetails = new VMDetails();
                    vmDetails.setDetailID(supplierAdjustmentDetailModel.getSupplierAdjustmentDetailID());
                    vmDetails.setAmount(supplierAdjustmentDetailModel.getAdjustAmount());
                    vmDetails.setMasterID(supplierAdjustmentDetailModel.getSupplierAdjustmentID());
                    vmDetails.setReferenceID(supplierAdjustmentDetailModel.getReferenceID());
                    vmDetails.setDetailType(ReferenceType.SupplierAdjustment.get());

                    SupplierAdjustmentModel supplierAdjustmentModel = this.supplierAdjustmentBllManager.getByIdActiveStatus(supplierAdjustmentDetailModel.getSupplierAdjustmentID());
                    vmDetails.setDocNumber(supplierAdjustmentModel.getDocNumber());

                    int index = 0;
                    for (SupplierAdjustmentModel supplierAdjustmentModelWhichHaveUnAllocateAmount : vmAllocatePayment.lstSupplierAdjustmentModel) {
                        if (supplierAdjustmentDetailModel.getSupplierAdjustmentID().intValue() == supplierAdjustmentModelWhichHaveUnAllocateAmount.getSupplierAdjustmentID().intValue()) {

                            //calculate un allocate amount
                            Double tempUnAllocatedAmount = vmAllocatePayment.lstSupplierAdjustmentModel.get(index).getUnAllocatedAmount();
                            vmAllocatePayment.lstSupplierAdjustmentModel.get(index).setUnAllocatedAmount(tempUnAllocatedAmount - supplierAdjustmentDetailModel.getAdjustAmount());
                        }
                        index++;
                    }
                    vmInvoiceAndAdjustment.lstVMDetails.add(vmDetails);
                }
                vmAllocatePayment.lstVMInvoiceAndAdjustment.add(vmInvoiceAndAdjustment);
            }

            //(5b)map adjustment increase with detail and make VM
            for (SupplierAdjustmentModel supplierAdjustmentIncreaseModel : lstIncreasedSupplierAdjustmentModel) {

                VMInvoiceAndAdjustment vmInvoiceAndAdjustment = new VMInvoiceAndAdjustment();
                //(i)master (adjustment increase)
                VMInvoiceAndAdjustmentMaster vmInvoiceAndAdjustmentMaster = new VMInvoiceAndAdjustmentMaster();
                vmInvoiceAndAdjustmentMaster.setMasterID(supplierAdjustmentIncreaseModel.getSupplierAdjustmentID());
                vmInvoiceAndAdjustmentMaster.setDate(supplierAdjustmentIncreaseModel.getDate());
                vmInvoiceAndAdjustmentMaster.setDocNumber(supplierAdjustmentIncreaseModel.getDocNumber());
                vmInvoiceAndAdjustmentMaster.setReference(supplierAdjustmentIncreaseModel.getReference());
                vmInvoiceAndAdjustmentMaster.setTotalAmount(supplierAdjustmentIncreaseModel.getExclusive());
                vmInvoiceAndAdjustmentMaster.setDueAmount(this.supplierInvoiceBllManager.getDueAmountOfAdjustmentIncrease(supplierAdjustmentIncreaseModel.getSupplierAdjustmentID()));
                vmInvoiceAndAdjustmentMaster.setReferenceType(PaymentAdjustmentReferenceType.AdjustmentIncrease.get());

                vmInvoiceAndAdjustment.vmInvoiceAndAdjustmentMaster = vmInvoiceAndAdjustmentMaster;

                //(ii)get supplier payments detail
                lstSupplierPaymentDetailModel = this.supplierPaymentDetailBllManager.search(supplierAdjustmentIncreaseModel.getSupplierAdjustmentID(), PaymentAdjustmentReferenceType.AdjustmentIncrease.get());

                for (SupplierPaymentDetailModel supplierPaymentDetailModel : lstSupplierPaymentDetailModel) {

                    VMDetails vmDetails = new VMDetails();
                    vmDetails.setDetailID(supplierPaymentDetailModel.getSupplierPaymentDetailID());
                    vmDetails.setAmount(supplierPaymentDetailModel.getPaidAmount());
                    vmDetails.setMasterID(supplierPaymentDetailModel.getSupplierPaymentID());
                    vmDetails.setReferenceID(supplierPaymentDetailModel.getReferenceID());
                    vmDetails.setDetailType(ReferenceType.SupplierPayment.get());

                    SupplierPaymentModel supplierPaymentModel = this.supplierPaymentBllManager.getByIdActiveStatus(supplierPaymentDetailModel.getSupplierPaymentID());
                    vmDetails.setDocNumber(supplierPaymentModel.getDocNumber());

                    int index = 0;
                    for (SupplierPaymentModel supplierPaymentModelWhichHaveUnAllocatedAmount : vmAllocatePayment.lstSupplierPaymentModel) {
                        if (supplierPaymentDetailModel.getSupplierPaymentID().intValue() == supplierPaymentModelWhichHaveUnAllocatedAmount.getSupplierPaymentID().intValue()) {
                            //calculate un allocate amount
                            Double tempUnAllocatedAmount = vmAllocatePayment.lstSupplierPaymentModel.get(index).getUnAllocatedAmount();
                            vmAllocatePayment.lstSupplierPaymentModel.get(index).setUnAllocatedAmount(tempUnAllocatedAmount - supplierPaymentDetailModel.getPaidAmount());
                        }
                        index++;
                    }
                    vmInvoiceAndAdjustment.lstVMDetails.add(vmDetails);
                }

                //(iii)get supplier Adjustment detail
                lstDecreasedSupplierAdjustmentDetailModel = this.supplierAdjustmentDetailBllManager.searchSupplierAdjustmentDetail(supplierAdjustmentIncreaseModel.getSupplierAdjustmentID(), PaymentAdjustmentReferenceType.AdjustmentIncrease.get());

                for (SupplierAdjustmentDetailModel supplierAdjustmentDetailModel : lstDecreasedSupplierAdjustmentDetailModel) {

                    VMDetails vmDetails = new VMDetails();
                    vmDetails.setDetailID(supplierAdjustmentDetailModel.getSupplierAdjustmentDetailID());
                    vmDetails.setAmount(supplierAdjustmentDetailModel.getAdjustAmount());
                    vmDetails.setMasterID(supplierAdjustmentDetailModel.getSupplierAdjustmentID());
                    vmDetails.setReferenceID(supplierAdjustmentDetailModel.getReferenceID());
                    vmDetails.setDetailType(ReferenceType.SupplierAdjustment.get());

                    SupplierAdjustmentModel supplierAdjustmentModel = this.supplierAdjustmentBllManager.getByIdActiveStatus(supplierAdjustmentDetailModel.getSupplierAdjustmentID());
                    vmDetails.setDocNumber(supplierAdjustmentModel.getDocNumber());

                    int index = 0;
                    for (SupplierAdjustmentModel supplierAdjustmentModelWhichHaveUnAllocateAmount : vmAllocatePayment.lstSupplierAdjustmentModel) {
                        if (supplierAdjustmentDetailModel.getSupplierAdjustmentID().intValue() == supplierAdjustmentModelWhichHaveUnAllocateAmount.getSupplierAdjustmentID().intValue()) {

                            //calculate un allocate amount
                            Double tempUnAllocatedAmount = vmAllocatePayment.lstSupplierAdjustmentModel.get(index).getUnAllocatedAmount();
                            vmAllocatePayment.lstSupplierAdjustmentModel.get(index).setUnAllocatedAmount(tempUnAllocatedAmount - supplierAdjustmentDetailModel.getAdjustAmount());
                        }
                        index++;
                    }
                    vmInvoiceAndAdjustment.lstVMDetails.add(vmDetails);
                }
                vmAllocatePayment.lstVMInvoiceAndAdjustment.add(vmInvoiceAndAdjustment);
            }
            responseMessage.responseObj = vmAllocatePayment;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception e) {
            log.error("AllocatePaymentServiceManager -> search got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(e);
        }

        return responseMessage;
    }
}