/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/28/2018
 * Time: 3:49 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.bll.manager.InventoryTransactionBllManager;
import nybsys.tillboxweb.bll.manager.ProductAdjustmentBllManager;
import nybsys.tillboxweb.bll.manager.ProductAdjustmentDetailBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreBllManager.CoreJournalBllManager;
import nybsys.tillboxweb.coreConstant.CurrencyConstant;
import nybsys.tillboxweb.coreEnum.Adjustment;
import nybsys.tillboxweb.coreEnum.DefaultCOA;
import nybsys.tillboxweb.coreEnum.ReferenceType;
import nybsys.tillboxweb.coreModels.*;
import nybsys.tillboxweb.coreUtil.CoreUtils;
import nybsys.tillboxweb.models.ItemAdjustmentModel;
import nybsys.tillboxweb.models.ItemAdjustmentSaveModel;
import nybsys.tillboxweb.models.ProductAdjustmentDetailModel;
import nybsys.tillboxweb.models.ProductAdjustmentModel;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SuppressWarnings("Duplicates")
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InventoryTransactionServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(InventoryTransactionServiceManager.class);

    @Autowired
    private InventoryTransactionBllManager inventoryTransactionBllManager = new InventoryTransactionBllManager();

    private ProductAdjustmentBllManager productAdjustmentBllManager;
    private ProductAdjustmentDetailBllManager productAdjustmentDetailBllManager;

    private ProductServiceManager productServiceManager;
    private CoreJournalBllManager coreJournalBllManager = new CoreJournalBllManager();


    public ResponseMessage saveInventoryTransaction(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMInventoryTransaction vmInventoryTransaction = new VMInventoryTransaction();
        try {
            vmInventoryTransaction = Core.getRequestObject(requestMessage, VMInventoryTransaction.class);

            this.inventoryTransactionBllManager.saveInventoryTransaction(vmInventoryTransaction.lstInventoryTransactionModel);
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SAVE_PRODUCT;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.FAILED_TO_SAVE_PRODUCT;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
            }

        } catch (Exception ex) {
            log.error("InventoryTransactionServiceManager -> saveInventoryTransaction got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
//            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage getQuantityOnHand(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();

        VMItemAdjustmentModel vmItemAdjustmentModel;
        Integer productID;
        InventoryTransactionModel whereCondition;
        List<InventoryTransactionModel> inventoryTransactionModelList;

        Double totalInQuantity = 0.0, totalOutQuantity = 0.0, qtyOnHand = 0.0;

        try {
            vmItemAdjustmentModel = Core.getRequestObject(requestMessage, VMItemAdjustmentModel.class);
            productID = vmItemAdjustmentModel.productID;

            whereCondition = new InventoryTransactionModel();
            whereCondition.setProductID(productID);

            inventoryTransactionModelList = this.inventoryTransactionBllManager.getAllByConditionWithActive(whereCondition);
            for (InventoryTransactionModel item : inventoryTransactionModelList) {
                totalInQuantity += item.getInQuantity();
                totalOutQuantity += item.getOutQuantity();
            }

            qtyOnHand = totalInQuantity - totalOutQuantity;
            vmItemAdjustmentModel.qtyOnHand = qtyOnHand;

            responseMessage.responseObj = vmItemAdjustmentModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            log.error("InventoryTransactionServiceManager -> getQuantityOnHand got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
//            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage searchInventoryTransaction(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        InventoryTransactionModel inventoryTransactionModel = new InventoryTransactionModel();
        List<InventoryTransactionModel> lstInventoryTransactionModel = new ArrayList<>();
        try {
            inventoryTransactionModel = Core.getRequestObject(requestMessage, InventoryTransactionModel.class);
            lstInventoryTransactionModel = this.inventoryTransactionBllManager.getAllByConditions(inventoryTransactionModel);

            responseMessage.responseObj = lstInventoryTransactionModel;
            if (lstInventoryTransactionModel.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> Product Service Manager got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage delete(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        InventoryTransactionModel inventoryTransactionModel = new InventoryTransactionModel();
        List<InventoryTransactionModel> lstInventoryTransactionModel = new ArrayList<>();
        Integer numberOfRowDeleted = 0;
        try {
            inventoryTransactionModel = Core.getRequestObject(requestMessage, InventoryTransactionModel.class);
            numberOfRowDeleted = this.inventoryTransactionBllManager.deleteInventoryTransaction(inventoryTransactionModel);

            responseMessage.responseObj = numberOfRowDeleted;
            if (numberOfRowDeleted > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
                this.rollBack();
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> Product Service Manager got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage updateInventoryTransaction(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<VMItemAdjustmentModel> vmItemAdjustmentModelList = new ArrayList<>();
        VMItemAdjustmentModel vmItemAdjustmentModel = new VMItemAdjustmentModel();
        try {
            vmItemAdjustmentModelList = Core.getRequestObject(requestMessage, vmItemAdjustmentModelList.getClass());


           /* this.inventoryTransactionBllManager.saveInventoryTransaction(vmItemAdjustmentModel.lstInventoryTransactionModel);
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SAVE_PRODUCT;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.FAILED_TO_SAVE_PRODUCT;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
            }*/

        } catch (Exception ex) {
            log.error("InventoryTransactionServiceManager -> saveInventoryTransaction got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage getAllAccountThroughInterModuleCommunication(RequestMessage requestMessage) {
        MqttClient mqttClientForAccount;
        CallBack callBackGetAllAccount;
        ResponseMessage responseMessage;// = new ResponseMessage();
        ResponseMessage responseGetAllAccount;


        RequestMessage reqMessForWorkerForAccountWorker;
        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();

            String pubTopic = WorkerSubscriptionConstants.WORKER_ACCOUNTING_MODULE_TOPIC;

            this.barrier = TillBoxUtils.getBarrier(1, lockObject);


            //======================= Start of three ===========================
            reqMessForWorkerForAccountWorker = Core.getDefaultWorkerRequestMessage();
            reqMessForWorkerForAccountWorker.brokerMessage.serviceName = "api/account/get";
            SubscriberForWorker subForAccount = new SubscriberForWorker(reqMessForWorkerForAccountWorker.brokerMessage.messageId, this.barrier);
            mqttClientForAccount = subForAccount.subscribe();
            callBackGetAllAccount = subForAccount.getCallBack();
            PublisherForWorker pubForWorkerAccount = new PublisherForWorker(pubTopic, mqttClientForAccount);
            pubForWorkerAccount.publishedMessageToWorker(reqMessForWorkerForAccountWorker);
            //======================= End of three ===========================


            synchronized (lockObject) {
                responseMessage = Core.buildDefaultResponseMessage();
                long startTime = System.nanoTime();
                lockObject.wait(this.allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {

                    responseGetAllAccount = callBackGetAllAccount.getResponseMessage();
                    responseMessage.responseObj = responseGetAllAccount.responseObj;
                    responseMessage.message = "Inter module communication successful";
                    responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

                } else {
                    //timeout
                    log.info("Response time out");


                    log.info("RollBack checkInterCom Operation");

                    responseMessage.message = "Inter module communication Failed";
                    responseMessage.responseCode = TillBoxAppConstant.UN_PROCESSABLE_REQUEST;
                }
            }


            this.closeBrokerClient(mqttClientForAccount, reqMessForWorkerForAccountWorker.brokerMessage.messageId);


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from checkInterCom Module communication UserServiceManager");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
        }
        return responseMessage;
    }


    public ResponseMessage inventoryItemAdjustment(RequestMessage requestMessage) {
        ResponseMessage responseMessage = this.getDefaultResponseMessage();
        ItemAdjustmentModel reqItemAdjustmentFrontEndViewModel, resultingItemAdjustmentFrontEndViewModel;
        Integer productID, lifoFifoORavg, referenceType;
        InventoryTransactionModel whereConditionInventoryTransactionModel;
        List<InventoryTransactionModel> inventoryTransactionModelList, selectedInventoryTransactionList;
        Double inQuantity = 0.0, outQuantity = 0.0, currentQty, inputQty, remainingQty = 0.0, currentStockValue = 0.0, adjustQty = 0.0;
        List<SupplierInvoiceDetailModel> supplierInvoiceDetailModelList = null;
        try {
            reqItemAdjustmentFrontEndViewModel = Core.getRequestObject(requestMessage, ItemAdjustmentModel.class);

            productID = reqItemAdjustmentFrontEndViewModel.productID;
            //inputQty = reqItemAdjustmentFrontEndViewModel.quantityAtHand;
            lifoFifoORavg = reqItemAdjustmentFrontEndViewModel.adjustmentMethod;
            adjustQty = (reqItemAdjustmentFrontEndViewModel.adjustmentQuantity == null) ? 0 : reqItemAdjustmentFrontEndViewModel.adjustmentQuantity;
            referenceType = ReferenceType.Product.get();


            whereConditionInventoryTransactionModel = new InventoryTransactionModel();
            whereConditionInventoryTransactionModel.setProductID(productID);
            //whereConditionInventoryTransactionModel.setReferenceType(referenceType);

            inventoryTransactionModelList = this.inventoryTransactionBllManager.getAllByConditionWithActive(whereConditionInventoryTransactionModel);

            for (InventoryTransactionModel inventoryTransactionModel : inventoryTransactionModelList) {
                if (inventoryTransactionModel.getInQuantity() != null)
                    inQuantity += inventoryTransactionModel.getInQuantity();
                if (inventoryTransactionModel.getOutQuantity() != null)
                    outQuantity += inventoryTransactionModel.getOutQuantity();
            }

            currentQty = Math.abs(inQuantity - outQuantity);

            inputQty = currentQty;

            String hsql;
            hsql = "SELECT it FROM InventoryTransaction it WHERE it.productID = " + productID +
                    " AND it.status=1 ORDER BY it.inventoryTransactionID DESC"; //" AND it.referenceType = " + referenceType +

            inventoryTransactionModelList = this.inventoryTransactionBllManager.executeHqlQuery(hsql, InventoryTransactionModel.class, TillBoxAppEnum.QueryType.Select.get());

            selectedInventoryTransactionList = new ArrayList<>();


            // select invoice base on in quantity
            for (InventoryTransactionModel inventoryTransactionModel : inventoryTransactionModelList) {
                if (inventoryTransactionModel.getInQuantity() != null && inventoryTransactionModel.getInQuantity() > 0.0) {
                    remainingQty += inventoryTransactionModel.getInQuantity();
                    selectedInventoryTransactionList.add(inventoryTransactionModel);
                }

                if (lifoFifoORavg != Adjustment.Average.get()) {
                    if (remainingQty >= currentQty)
                        break;
                }
            }


            Double processCurrentQty, calculateRemainQty = 0.0, totalPrice = 0.0, unitPrice, oldUnitPrice;
            boolean initialLoad = false;
            if (adjustQty == 0) {
                initialLoad = true;
                adjustQty = currentQty;
            }
            unitPrice = CalculatePrice(adjustQty, lifoFifoORavg, inventoryTransactionModelList);

            oldUnitPrice = CalculatePrice(currentQty, lifoFifoORavg, inventoryTransactionModelList);
//            if (adjustQty != null) {
//
//            } else {
//                unitPrice = CalculatePrice(0, lifoFifoORavg, inventoryTransactionModelList);
//            }

            currentStockValue = oldUnitPrice * currentQty;


            resultingItemAdjustmentFrontEndViewModel = new ItemAdjustmentModel();
            resultingItemAdjustmentFrontEndViewModel.productID = productID;
            resultingItemAdjustmentFrontEndViewModel.quantityAtHand = currentQty;
            resultingItemAdjustmentFrontEndViewModel.currentStockValue = currentStockValue;
            resultingItemAdjustmentFrontEndViewModel.adjustmentMethod = lifoFifoORavg;
            resultingItemAdjustmentFrontEndViewModel.price = unitPrice;
            resultingItemAdjustmentFrontEndViewModel.oldPrice = oldUnitPrice;

            if (adjustQty != null && adjustQty > 0.0) {
                resultingItemAdjustmentFrontEndViewModel.newStockValue = oldUnitPrice * adjustQty;
                resultingItemAdjustmentFrontEndViewModel.adjustmentQuantity = adjustQty;
            } else {
                resultingItemAdjustmentFrontEndViewModel.newStockValue = currentStockValue;
                resultingItemAdjustmentFrontEndViewModel.adjustmentQuantity = currentQty;
            }


            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.responseObj = resultingItemAdjustmentFrontEndViewModel;
            responseMessage.message = TillBoxAppConstant.SUCCESS;

        } catch (Exception ex) {
            log.error("InventoryItemAdjustment Manager -> inventoryItemAdjustment method got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    //code added by hannan
    private double CalculatePrice(double adjustQuantity, int lifoFifoORavg, List<InventoryTransactionModel> inventoryTransactionModelList) {
        double price = 0;
        double totalPrice = 0;
        int totalQuantity = 0;
        double totalValue = 0;
        List<InventoryTransactionModel> lstNewForFifo = new ArrayList<>();
        if (lifoFifoORavg == Adjustment.LIFO.get()) {
            for (InventoryTransactionModel inventoryTransactionModel : inventoryTransactionModelList) {


                if (inventoryTransactionModel.getInQuantity() != null && inventoryTransactionModel.getInQuantity() > 0.0) {
                    if (adjustQuantity >= inventoryTransactionModel.getInQuantity()) {
                        totalQuantity += inventoryTransactionModel.getInQuantity();
                        lstNewForFifo.add(inventoryTransactionModel);
                    }
                }
            }
            Collections.reverse(lstNewForFifo);
            inventoryTransactionModelList = lstNewForFifo;
        }


        if (adjustQuantity == 0) {

            for (InventoryTransactionModel inventoryTransactionModel : inventoryTransactionModelList) {
                totalQuantity += inventoryTransactionModel.getInQuantity();
                totalValue += inventoryTransactionModel.getInQuantity() * inventoryTransactionModel.getPrice();
            }
        } else {

            double restQuantity = adjustQuantity;

            for (InventoryTransactionModel inventoryTransactionModel : inventoryTransactionModelList) {

                if (lifoFifoORavg == Adjustment.FIFO.get()) {
                    if (inventoryTransactionModel.getInQuantity() != null) {
                        if (adjustQuantity >= inventoryTransactionModel.getInQuantity()) {
                            if (restQuantity > inventoryTransactionModel.getInQuantity()) {
                                totalQuantity += inventoryTransactionModel.getInQuantity();
                                totalValue += inventoryTransactionModel.getInQuantity() * inventoryTransactionModel.getPrice();
                            } else {
                                totalQuantity += inventoryTransactionModel.getInQuantity();
                                totalValue += restQuantity * inventoryTransactionModel.getPrice();
                            }
                            restQuantity -= inventoryTransactionModel.getInQuantity();

                        } else {
                            break;
                        }
                    }

                } else if (lifoFifoORavg == Adjustment.LIFO.get()) {
                    if (inventoryTransactionModel.getInQuantity() != null) {
                        if (adjustQuantity >= inventoryTransactionModel.getInQuantity()) {
                            if (restQuantity > inventoryTransactionModel.getInQuantity()) {
                                totalQuantity += inventoryTransactionModel.getInQuantity();
                                totalValue += inventoryTransactionModel.getInQuantity() * inventoryTransactionModel.getPrice();
                            } else {
                                totalQuantity += inventoryTransactionModel.getInQuantity();
                                totalValue += restQuantity * inventoryTransactionModel.getPrice();
                            }
                            restQuantity -= inventoryTransactionModel.getInQuantity();
                        } else {
                            break;
                        }
                    }
                } else {
                    if (inventoryTransactionModel.getInQuantity() != null) {
                        totalQuantity += inventoryTransactionModel.getInQuantity();
                        totalValue += inventoryTransactionModel.getInQuantity() * inventoryTransactionModel.getPrice();
                    }

                }
            }
        }
        if (lifoFifoORavg == Adjustment.Average.get()) {
            price = (totalValue > 0 && totalQuantity > 0) ? totalValue / totalQuantity : 0;
        } else {
            price = (totalValue > 0 && adjustQuantity > 0) ? totalValue / adjustQuantity : 0;
        }


        return price;
    }


    public ResponseMessage saveItemAdjustment(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        ItemAdjustmentSaveModel itemAdjustmentSaveModel;
        String note;
        Date adjustmentDate;
        List<ItemAdjustmentModel> itemAdjustmentModelList;

        this.productAdjustmentBllManager = new ProductAdjustmentBllManager();
        this.productAdjustmentDetailBllManager = new ProductAdjustmentDetailBllManager();

        ProductAdjustmentModel productAdjustmentModel, saveProductAdjustmentModel;
        productAdjustmentModel = new ProductAdjustmentModel();
        ProductAdjustmentDetailModel productAdjustmentDetailModel;
        InventoryTransactionModel inventoryTransactionModel;// = new InventoryTransactionModel();
        Double quantity, unitPrice;


        Double increaseProductPrice = 0.0, decreaseProductPrice = 0.0;


        JournalModel drJournalModel, crJournalModel;
        List<JournalModel> journalModelList = new ArrayList<>();

        Boolean isJournalEntrySuccess = false;

        try {
            itemAdjustmentSaveModel = Core.getRequestObject(requestMessage, ItemAdjustmentSaveModel.class);
            note = itemAdjustmentSaveModel.note;
            adjustmentDate = itemAdjustmentSaveModel.adjustmentDate;
            itemAdjustmentModelList = itemAdjustmentSaveModel.vmItemAdjustmentModelList;


            productAdjustmentModel.setApproved(true);
            productAdjustmentModel.setBusinessID(requestMessage.businessID);
            productAdjustmentModel.setDate(adjustmentDate);
            productAdjustmentModel.setNote(note);

            saveProductAdjustmentModel = this.productAdjustmentBllManager.save(productAdjustmentModel);

            for (ItemAdjustmentModel itemAdjustmentModel : itemAdjustmentModelList) {
                productAdjustmentDetailModel = new ProductAdjustmentDetailModel();
                productAdjustmentDetailModel.setProductAdjustmentID(saveProductAdjustmentModel.getProductAdjustmentID());
                productAdjustmentDetailModel.setAdjustmentMethodID(itemAdjustmentModel.adjustmentMethod);
                productAdjustmentDetailModel.setReasonID(itemAdjustmentModel.reasondID);
                productAdjustmentDetailModel.setProductID(itemAdjustmentModel.productID);

                unitPrice = itemAdjustmentModel.currentStockValue / itemAdjustmentModel.quantityAtHand;

                if (itemAdjustmentModel.quantityAtHand < itemAdjustmentModel.adjustmentQuantity) {
                    productAdjustmentDetailModel.setInOut(Adjustment.Increase.get());
                    increaseProductPrice += ((itemAdjustmentModel.adjustmentQuantity - itemAdjustmentModel.quantityAtHand) * unitPrice);
                } else {
                    productAdjustmentDetailModel.setInOut(Adjustment.Decrease.get());
                    decreaseProductPrice += ((itemAdjustmentModel.quantityAtHand - itemAdjustmentModel.adjustmentQuantity) * unitPrice);
                }


                quantity = Math.abs(itemAdjustmentModel.quantityAtHand - itemAdjustmentModel.adjustmentQuantity);
                productAdjustmentDetailModel.setQuantity(quantity);

                productAdjustmentDetailModel.setUnitPrice(unitPrice);

                this.productAdjustmentDetailBllManager.save(productAdjustmentDetailModel);

                inventoryTransactionModel = new InventoryTransactionModel();
                inventoryTransactionModel.setProductID(itemAdjustmentModel.productID);
                inventoryTransactionModel.setProductTypeID(itemAdjustmentModel.productTypeID);
                inventoryTransactionModel.setProductCategoryID(itemAdjustmentModel.productCategoryID);
                inventoryTransactionModel.setReferenceType(ReferenceType.ProductAdjustment.get());
                inventoryTransactionModel.setReferenceID(saveProductAdjustmentModel.getProductAdjustmentID());
                inventoryTransactionModel.setBusinessID(requestMessage.businessID);
                inventoryTransactionModel.setDate(adjustmentDate);

                if (itemAdjustmentModel.quantityAtHand < itemAdjustmentModel.adjustmentQuantity)
                    inventoryTransactionModel.setInQuantity(quantity);
                else
                    inventoryTransactionModel.setOutQuantity(quantity);

                inventoryTransactionModel.setPrice(unitPrice);

                this.inventoryTransactionBllManager.save(inventoryTransactionModel);

            }

            CurrencyModel currencyModel;
            this.productServiceManager = new ProductServiceManager();


            //get base currency and exchange rate
            currencyModel = this.productServiceManager.getBaseCurrency();
            if (currencyModel == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return responseMessage;
            } else {
                currencyModel.setExchangeRate(1.00);
            }

            //check entry currency is present if not base currency will be entry currency
            if (requestMessage.entryCurrencyID == null || requestMessage.entryCurrencyID == 0) {
                requestMessage.entryCurrencyID = currencyModel.getCurrencyID();
            }

            // save journal
            String journalReferenceNo = TillBoxUtils.getUUID();
            if (decreaseProductPrice > 0.0) {
                drJournalModel = CoreUtils.buildDrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.StockAdjustmentLoss.get(),
                        saveProductAdjustmentModel.getProductAdjustmentID(),
                        ReferenceType.ProductAdjustment.get(),
                        null,
                        null,
                        decreaseProductPrice,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID
                );

                crJournalModel = CoreUtils.buildCrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.Inventory.get(),
                        saveProductAdjustmentModel.getProductAdjustmentID(),
                        ReferenceType.ProductAdjustment.get(),
                        null,
                        null,
                        decreaseProductPrice,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID
                );

                journalModelList.add(drJournalModel);
                journalModelList.add(crJournalModel);

                isJournalEntrySuccess = this.coreJournalBllManager.saveOrUpdate(journalModelList);
                if (isJournalEntrySuccess) {
                    log.info("Journal save successfully");
                }

            }
            if (increaseProductPrice > 0.0) {
                drJournalModel = CoreUtils.buildDrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.Inventory.get(),
                        saveProductAdjustmentModel.getProductAdjustmentID(),
                        ReferenceType.ProductAdjustment.get(),
                        null,
                        null,
                        increaseProductPrice,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID
                );

                crJournalModel = CoreUtils.buildCrJournalEntry(
                        journalReferenceNo,
                        DefaultCOA.StockAdjustmentIncome.get(),
                        saveProductAdjustmentModel.getProductAdjustmentID(),
                        ReferenceType.ProductAdjustment.get(),
                        null,
                        null,
                        increaseProductPrice,
                        note,
                        currencyModel,
                        requestMessage.entryCurrencyID
                );

                journalModelList.add(drJournalModel);
                journalModelList.add(crJournalModel);

                isJournalEntrySuccess = this.coreJournalBllManager.saveOrUpdate(journalModelList);
                if (isJournalEntrySuccess) {
                    log.info("Journal save successfully");
                }
            }

            //code added by hannan
            if (isJournalEntrySuccess) {
                if (responseMessage.message != null && responseMessage.message != "") {
                    responseMessage.message = MessageConstant.FAILED_TO_SAVE_PRODUCT;
                }
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.SAVE_PRODUCT_ADJUSTMENT;
            }

        } catch (Exception ex) {
            log.error("InventoryTransactionServiceManager -> saveInventoryTransaction got exception");
            responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            responseMessage.errorMessage = ex.getMessage();
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }
}
