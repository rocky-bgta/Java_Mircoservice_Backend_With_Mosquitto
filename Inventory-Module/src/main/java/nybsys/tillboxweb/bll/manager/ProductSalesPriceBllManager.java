/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/14/2018
 * Time: 11:01 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.Enum.ProductAdjustmentType;
import nybsys.tillboxweb.MessageModel.ClientMessage;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.companySettingUtils.RoundingUtils;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreBllManager.RoundingBllManager;
import nybsys.tillboxweb.coreConstant.CompanySettingConstant;
import nybsys.tillboxweb.coreEnum.EffectType;
import nybsys.tillboxweb.coreModels.RoundingModel;
import nybsys.tillboxweb.entities.ProductSalesPrice;
import nybsys.tillboxweb.models.PriceCategoryModel;
import nybsys.tillboxweb.models.ProductModel;
import nybsys.tillboxweb.models.ProductSalesPriceModel;
import nybsys.tillboxweb.models.VMAdjustProductSellingPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProductSalesPriceBllManager extends BaseBll<ProductSalesPrice> {

    private static final Logger log = LoggerFactory.getLogger(ProductSalesPriceBllManager.class);

    private ProductBllManager productBllManager;
    private ProductPriceListBllManager productPriceListBllManager;
    private RoundingBllManager roundingBllManager;

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(ProductSalesPrice.class);
        Core.runTimeModelType.set(ProductSalesPriceModel.class);
    }


    public ProductSalesPriceModel saveProductSalesPriceModel(ProductSalesPriceModel productSalesPriceModel, ClientMessage wrapperModel) throws Exception {

        try {

            if (productSalesPriceModel.getProductSalesPriceID() != null && productSalesPriceModel.getProductSalesPriceID() > 0) {
                productSalesPriceModel.setUpdatedDate(new Date());
                productSalesPriceModel = this.update(productSalesPriceModel);
            } else {
                productSalesPriceModel.setStatus(TillBoxAppEnum.Status.Active.get());
                productSalesPriceModel.setCreatedBy("");
                productSalesPriceModel.setCreatedDate(new Date());
                productSalesPriceModel = this.save(productSalesPriceModel);
            }

            if (productSalesPriceModel == null) {
                wrapperModel.messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                wrapperModel.message = MessageConstant.FAILED_TO_SAVE_PRODUCT_ATTRIBUTE;
            }

        } catch (Exception ex) {
            log.error("ProductSalesPriceBllManager -> saveProductSalesPriceModel got exception " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return productSalesPriceModel;
    }

    public void saveAdjustedProductSalesPrice(VMAdjustProductSellingPrice vmAdjustProductSellingPrice) throws Exception {
        this.productBllManager = new ProductBllManager();
        this.productPriceListBllManager = new ProductPriceListBllManager();
        this.roundingBllManager = new RoundingBllManager();
        ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
        List<ProductSalesPriceModel> lstProductSalesPriceModel = new ArrayList<>();
        RoundingModel roundingModel;
        Integer numberOfDigitAfterDecimalPoint = 2;
        try {
            roundingModel = this.roundingBllManager.getRoundingSetting(vmAdjustProductSellingPrice.getLstSelectedProduct().get(0).getBusinessID());
            if(roundingModel == null){
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = CompanySettingConstant.ROUNDING_GET_FAILED;
                Core.clientMessage.get().userMessage = CompanySettingConstant.ROUNDING_GET_FAILED;
                return;
            }else {
                numberOfDigitAfterDecimalPoint = roundingModel.getNumberOfDigitAfterDecimalPoint();
            }


            for (ProductModel productModel : vmAdjustProductSellingPrice.getLstSelectedProduct()) {
                productSalesPriceModel.setProductID(productModel.getProductID());

                //get active price category of this business
                PriceCategoryModel priceCategoryModel = new PriceCategoryModel();
                List<PriceCategoryModel> lstPriceCategoryModel;
                priceCategoryModel.setBusinessID(productModel.getBusinessID());
                priceCategoryModel.setDefault(true);
                priceCategoryModel.setStatus(TillBoxAppEnum.Status.Active.get());
                lstPriceCategoryModel = this.productPriceListBllManager.searchPriceCategory(priceCategoryModel);
                if (lstPriceCategoryModel.size() == 0) {
                    return;
                } else {

                    //get current sales price
                    productSalesPriceModel.setPriceCategoryID(lstPriceCategoryModel.get(0).getPriceCategoryID());
                    lstProductSalesPriceModel = this.getAllByConditionWithActive(productSalesPriceModel);
                    if (lstProductSalesPriceModel.size() > 0) {
                        for (ProductSalesPriceModel savedProductSalesPriceModel : lstProductSalesPriceModel) {

                            // first de-active all found price by product id and category id
                            savedProductSalesPriceModel.setStatus(TillBoxAppEnum.Status.Inactive.get());
                            this.update(savedProductSalesPriceModel);

                            //then insert again with updated price
                            //check adjustment type
                            int adjustmentType = vmAdjustProductSellingPrice.getAdjustmentType().intValue();

                            if (adjustmentType == ProductAdjustmentType.Selling_Prices_based_on_Average_Cost.get()) {

                                Double averageCost = 0.0;
                                averageCost = this.productBllManager.calculateAveragePrice(productModel.getProductID(), productModel.getBusinessID());

                                if (vmAdjustProductSellingPrice.getEffectType().intValue() == EffectType.Increase.get()) {

                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), averageCost + (averageCost * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() + (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                } else {
                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), averageCost - (averageCost * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() - (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                }
                            } else if (adjustmentType == ProductAdjustmentType.Selling_Prices_based_on_Last_Cost.get()) {

                                Double lastCost = 0.0;
                                lastCost = this.productBllManager.calculateLastPrice(productModel.getProductID(), productModel.getBusinessID());

                                if (vmAdjustProductSellingPrice.getEffectType().intValue() == EffectType.Increase.get()) {
                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), lastCost + (lastCost * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() + (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                } else {
                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), lastCost - (lastCost * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() - (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                }
                            } else { //based on selling price

                                if (vmAdjustProductSellingPrice.getEffectType().intValue() == EffectType.Increase.get()) {
                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() + (savedProductSalesPriceModel.getExclusiveSalesPrice() * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() + (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                } else {
                                    savedProductSalesPriceModel.setExclusiveSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() - (savedProductSalesPriceModel.getExclusiveSalesPrice() * vmAdjustProductSellingPrice.getAdjustmentPercentage() / 100)));
                                    savedProductSalesPriceModel.setSalesPrice(RoundingUtils.getRoundedAmount(numberOfDigitAfterDecimalPoint, vmAdjustProductSellingPrice.getRoundingNearestTo(), vmAdjustProductSellingPrice.getRoundingType(), savedProductSalesPriceModel.getExclusiveSalesPrice() - (savedProductSalesPriceModel.getExclusiveSalesPrice() * productModel.getSalesVatRate() / 100)));
                                }
                            }

                            savedProductSalesPriceModel = this.save(savedProductSalesPriceModel);
                            if (savedProductSalesPriceModel == null) {
                                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                                Core.clientMessage.get().message = MessageConstant.ITEM_SALES_PRICE_SAVE_FAILED;
                                return;
                            }
                        }
                    } else {
                        Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        Core.clientMessage.get().message = MessageConstant.ITEM_SALES_PRICE_SAVE_FAILED;
                    }
                }
            }

        } catch (Exception ex) {
            log.error("ProductSalesPriceBllManager -> saveAdjustedProductSalesPrice got exception " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
    }
}
