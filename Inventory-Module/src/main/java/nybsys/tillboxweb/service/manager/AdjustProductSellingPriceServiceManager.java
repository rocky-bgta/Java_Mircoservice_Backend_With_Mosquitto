/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/05/2018
 * Time: 03:07
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.bll.manager.ProductSalesPriceBllManager;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreEnum.RoundingType;
import nybsys.tillboxweb.models.VMAdjustProductSellingPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdjustProductSellingPriceServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(AdjustProductSellingPriceServiceManager.class);

    private ProductSalesPriceBllManager productSalesPriceBllManager;

    public ResponseMessage saveAdjustProductSellingPrice(RequestMessage requestMessage) {

        this.productSalesPriceBllManager = new ProductSalesPriceBllManager();
        ResponseMessage responseMessage = new ResponseMessage();
        VMAdjustProductSellingPrice vmAdjustProductSellingPrice;
        try {
            if (requestMessage.businessID == null || requestMessage.businessID == 0) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SELECT_A_BUSINESS;
                Core.clientMessage.get().userMessage = MessageConstant.SELECT_A_BUSINESS;
                return responseMessage;
            }

            vmAdjustProductSellingPrice = Core.getRequestObject(requestMessage, VMAdjustProductSellingPrice.class);

            for (int index = 0; index < vmAdjustProductSellingPrice.getLstSelectedProduct().size(); index++) {
                vmAdjustProductSellingPrice.getLstSelectedProduct().get(index).setBusinessID(requestMessage.businessID);
            }

            if (vmAdjustProductSellingPrice.getRoundingType() == null) {
                vmAdjustProductSellingPrice.setAdjustmentType(RoundingType.RoundUp.get());
            }

            this.productSalesPriceBllManager.saveAdjustedProductSalesPrice(vmAdjustProductSellingPrice);

            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.ITEM_SALES_PRICE_ADJUST_SAVE_FAILED;
                this.rollBack();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.ITEM_SALES_PRICE_ADJUST_SAVE_SUCCESSFULLY;
                this.commit();
            }

        } catch (Exception ex) {
            log.error("ProductServiceManager -> saveAdjustProductSellingPrice got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }
}
